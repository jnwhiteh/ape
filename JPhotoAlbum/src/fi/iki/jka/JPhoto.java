/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhoto.java,v 1.12 2010/04/11 21:38:16 jkarjala Exp $
 */

/** Container for a single photo, may not always contain a real photo, but
 * just text element.
 * @see JPhotoAlbumLink.java
 */

package fi.iki.jka;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;

public class JPhoto extends Observable implements Transferable, Serializable {

    public static final DataFlavor PHOTO_FLAVOR
        = new DataFlavor(JPhoto.class, "Single JPG Photograph");
    
    protected DataFlavor[] myFlavors = {PHOTO_FLAVOR};

    protected int width = -1, height = -1;
    protected int fullWidth = -1, fullHeight = -1;
    protected String name = "";
    protected String category = "";
    protected String text = "";
    protected String status = "";
    protected String albumlink = null;
    protected File original = null;
    protected JPhotoExif exif = null;
    protected boolean badFile = false;
    protected static int MIN_DIMENSION = 360; // minimum dimension with panoramas
    protected static Dimension limits = new Dimension(640, 640);
    protected static Dimension thumbLimits = new Dimension(160, 160);
    protected static Dimension slideLimits1 = new Dimension(800, 600);    
    protected static Dimension slideLimits2 = new Dimension(1024, 768);
    protected static Dimension slideLimits3 = new Dimension(1280, 1024);
    protected Properties transformParams = null;
    
    // Not serialized
    transient protected boolean dirtyExif = false; // exif mismatches real JPG size 
    transient protected ExifDirectory directory = null;
    transient protected SoftReference thumbImage = null;

    // Cache at most one full photo, and even that with a soft reference
    // A 5 megapixel photo can take 20 megabytes of memory...
    transient static protected JPhoto fullImagePhoto = null;
    transient static protected SoftReference fullImage = null;

    // Owner to get target directory of the JPhoto file 
    transient protected JPhotoCollection owner = null;
    
    // Default owner, needed for castor unmarshaling
    transient protected static JPhotoCollection defaultOwner = null;
        
    transient Font font = null;
    transient Font waterFont = null;
    transient static ThumbLoader loader = null;
    /*    
    protected static ImageReader jpgReader;
    static {
        Iterator readers = ImageIO.getImageReadersBySuffix("jpg");
        jpgReader = (ImageReader)readers.next();
    }
    */
    public JPhoto() {
        this(defaultOwner, null);
    }

    public JPhoto(JPhotoCollection owner) {
        this(owner, null);
    }

    public JPhoto(File original) {
        this(defaultOwner, original);
    }

    public JPhoto(JPhotoCollection owner, File original) {
        width = 0;
        height = 0;
        this.owner = owner;
        this.original = original;
        if (original!=null)
            name = original.getName();
    }

    public JPhoto(IPhotoCollection photos) {
        new JPhoto(photos);
    }

    public static void setDefaultOwner(JPhotoCollection owner) {
        defaultOwner = owner;
    }

    public void setOwner(JPhotoCollection owner) {
        this.owner = owner;
    }
    
    public BufferedImage getThumbImage() {
        BufferedImage thumb = null;
        if (thumbImage!=null)
            thumb = (BufferedImage)thumbImage.get();
        
        if (thumb==null) {
            try {
                getExifDirectory();
                if (directory!=null) {
                    byte thumbData[] = directory.getThumbnailData();
                    if (thumbData!=null) {
                        Iterator readers = ImageIO.getImageReadersBySuffix("jpg");
                        ImageReader jpgReader = (ImageReader)readers.next();
                        jpgReader.setInput(new MemoryCacheImageInputStream
                                           (new ByteArrayInputStream(thumbData)));
                        thumb = jpgReader.read(0);
                        jpgReader.dispose();
                        thumbImage = new SoftReference(thumb);
                    }
                }
            }
            catch (Exception e) {
                JPhotoStatus.showStatus(null, "JPhoto "+getOriginalFile()
                                        +" cannot read thumb "+e);
            }
        }
        return thumb;
    }

    public BufferedImage getCachedThumb() {
        if (thumbImage==null)
            return null;
        else
            return (BufferedImage)thumbImage.get();
    }
    
    public BufferedImage getThumbImageAsync() {
        if (loader==null) {
            loader = new ThumbLoader();
            // loader.setPriority(Thread.MIN_PRIORITY);
            loader.start();
        }

        if (getCachedThumb()==null)
            loader.startLoading(this);
        
        return getCachedThumb();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String stat) {
        status = stat;
        setChanged();
        notifyObservers(stat);
    }
    
    public BufferedImage getFullImage() {

        if (getOriginalFile()==null)
            return null; // No original photo!

        BufferedImage im = null;        
        if (fullImage!=null && fullImagePhoto==this)
            im = (BufferedImage)fullImage.get();

        ImageReader imageReader = null;
        if (im==null) {
            try {
                long ticks = System.currentTimeMillis();
                System.out.println("Loading "+getOriginalFile()+"...");
                String suffix = Utils.getFileExt(getOriginalFile().getName(), "jpg");
//                Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
//                imageReader = (ImageReader)readers.next();
//                imageReader.setInput(new FileImageInputStream(getOriginalFile()));
//                im = imageReader.read(0);
//                imageReader.dispose();
                im = readImage(getOriginalFile());
                JPhotoStatus.showStatus(null, getOriginalFile()+" loaded in "+(System.currentTimeMillis()-ticks)+"ms. ");
                
                if (fullWidth!=im.getWidth() || fullHeight!=im.getHeight()) {
                    fullWidth = im.getWidth();
                    fullHeight = im.getHeight();                	
                    setWebSize();
                }
                	
                if (getCachedThumb()==null
                    && (getExifDirectory()==null
                        || getExifDirectory().getThumbnailData()==null)) {
                    try {
                        ticks = System.currentTimeMillis();
                        
                        fullWidth = im.getWidth();
                        fullHeight = im.getHeight();
                        // getScaledInstance() is very slow with some images,
                        // so let's use drawImage which is accelerated.
                        // Image img = im.getScaledInstance(getWidth()/4,
                        // getHeight()/4, Image.SCALE_FAST);
                        
                        Dimension dim = Utils.getScaledSize(thumbLimits,fullWidth,fullHeight, MIN_DIMENSION/4);
                        // System.out.println("w="+fullWidth+" h="+fullHeight+" dim="+dim);
                        BufferedImage bit = new BufferedImage(dim.width,
                                                              dim.height,
                                                              BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = bit.createGraphics();
                        g2.drawImage(im, 0,0,bit.getWidth(null), bit.getHeight(null), null, null);
                        g2.dispose();
                        
                        thumbImage = new SoftReference(bit);
                        System.out.println("Generated display thumb for "+getOriginalFile()
                                           +" in "+(System.currentTimeMillis()-ticks)+"ms.");
                    }
                    catch (Exception e) {
                        setStatus(e.toString());
                        JPhotoStatus.showStatus(null, "Cannot generate thumbnail:"+e);
                    }
                }
                fullImage = new SoftReference(im);
                fullImagePhoto = this;
            }
            catch (Throwable e) {
                if (imageReader!=null)
                    imageReader.dispose();
                setStatus(e.toString());
                badFile = true;
                JPhotoStatus.showStatus(null, "Cannot load "+getOriginalFile()+":"+e);
                im = null;
            }
        }
        else
            System.out.println("Found "+getOriginalFile()+" from cache");
        return im;
    }
    
    /** Workaround bug 4705399 in Java color space handling */
    public static BufferedImage readImage(File source) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(source);
        ImageReader reader = (ImageReader)ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);
        ImageReadParam param = reader.getDefaultReadParam();
        
        ImageTypeSpecifier typeToUse = null;
        for (Iterator i = reader.getImageTypes(0); i.hasNext(); ) {
            ImageTypeSpecifier type = (ImageTypeSpecifier) i.next();
            if (type.getColorModel().getColorSpace().isCS_sRGB()) 
                typeToUse = type;
        }
        if (typeToUse!=null) param.setDestinationType(typeToUse);
        BufferedImage b = reader.read(0, param);
        reader.dispose();
        stream.close();
        return b;
    }
    
    public ExifDirectory getExifDirectory() {
        if (directory==null && getOriginalFile()!=null) {
            try {
                Metadata metadata = JpegMetadataReader.readMetadata(getOriginalFile());
                directory = (ExifDirectory)metadata.getDirectory(ExifDirectory.class);
            }
            catch (Exception e) {
                setStatus(e.toString());
                JPhotoStatus.showStatus(null, getOriginalFile()+": cannot read exif "+e);
            }
        }
        return directory;
    }

    public int getOriginalWidth() {
        if (fullWidth<=0)
            setWebSize();
        return fullWidth;
    }
    
    public int getOriginalHeight() {
        if (fullHeight<=0)
            setWebSize();
        return fullHeight;
    }
    
    /** Web site image size */
    public int getWidth() {
        if (width<=0)
            setWebSize();
        return width;
    }
    public void setWidth(int w) {
        width = w;
    }
    
    /** Web site image size */
    public int getHeight() {
        if (height<=0)
            setWebSize();
        return height;
    }
    
    public void setHeight(int h) {
        height = h;
    }
    
   
    public String getImageName() {
        return name;
    }

    public void setImageName(String n) {
        badFile = false;
        name = n;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String n) {
        category = n;
    }

    public void setTransformParams(String transformName, Properties params) {
        if (transformParams==null)
            transformParams = new Properties();
        transformParams.put(transformName, params);
    }
    
    public Properties getTransformParams(String transformName) {
        if (transformParams==null)
            return null;
        return (Properties)transformParams.get(transformName);
    }
    
    public String getAlbumLink() {
        return makeRelative(albumlink); 
    }
    
    public void setAlbumLink(String n) {
        albumlink = makeCanonical(n);
    }
    
    public String getFullAlbumLink() {
        if (albumlink==null)
            return null;
        return new File(albumlink).getAbsolutePath();
    }

    public File getOriginalFile() {
        return original;
    }
    
    /** Return relative path to original as seen from target dir or . if
     * targetdir not defined.
     */
    public String getOriginalName() {
        return makeRelative(getOriginalFile());
    }

    public String getFullOriginalName() {
        if (original==null)
            return null;
        return getOriginalFile().getAbsolutePath();
    }

    public void setOriginalName(String n) {
        setOriginalName(new File(n));
    }
    public void setOriginalName(File f) {
        original = makeCanonical(f);
        setImageName(original.getName());
        clearCaches();
    }

    public String makeCanonical(String n) {
        return makeCanonical(new File(n)).getPath();
    }
    public File makeCanonical(File f) {
        File abs = f;
        if (!abs.isAbsolute() && getTargetDir()!=null) {
            abs = new File(getTargetDir(), abs.getPath());
        }
        
        // System.out.println("makeCanonical:"+f+"->"+abs);
        
        try {
            abs = abs.getCanonicalFile();
        }
        catch (Exception e) {
            System.out.println("makeCanonical:"+e);
        }
        
        return abs;
    }

    public String makeRelative(String name) {
        if (name==null)
            return null;
        return makeRelative(new File(name));
    }
    public String makeRelative(File name) {
        if (name==null)
            return null;
        
        String result;
        if (getTargetDir()!=null)
            result = Utils.getRelativePath(name, getTargetDir());
        else
            result = Utils.getRelativePath(name, new File("."));
        return result;
    }
    
    public File getTargetDir() {
        if (owner==null)
            return null;

        return owner.getTargetDir();
    }
        
    public String getText() {
        return text;
    }
    public void setText(String t) {
        // Replace all multiple white spaces with a single one
        // Also trim out extra whitespace from begin/end
        text = t.replaceAll("\\s\\s+", " ").trim();
    }

    public JPhotoExif getExif() {
        if (exif==null) {
            getExifDirectory();
            if (directory!=null) {
                exif = new JPhotoExif(directory);
            }
            else
                exif = new JPhotoExif(directory);
        }
        return exif;
    }

    public void setExif(JPhotoExif e) {
        exif = e;
    }

    public Font getFont() {
        if (font==null)
            font = new Font("SansSerif", Font.BOLD, 16);
        return font;
    }
    
    public Font getWatermarkFont() {
        if (waterFont==null)
            waterFont = new Font("SansSerif", Font.BOLD, 10);
        return waterFont;
    }
    
    // Transferable IF method
    public Object getTransferData(DataFlavor flavor) {
        // System.out.println("getTransferData:"+flavor);
        return this;
    }
    // Transferable IF method
    public DataFlavor[] getTransferDataFlavors() {
        // System.out.println("getTransferDataFlavors");
        return myFlavors;
    }
    // Transferable IF method
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // System.out.println("isDataFlavorSupported:"+flavor);
        return flavor.equals(myFlavors[0]);
    }
    
    public String toString() {
        return "src='"+getImageName()+"' w="+getWidth()+" h="+getHeight()
            +" text='"+getText()+"'";
    }

    public void setWebSize() {
        if (getImageName()==null || getImageName().equals(""))
            return; // no image, link or description
        
        if (fullWidth<0 || fullHeight<0) {
            // Full image not yet loaded, try to find size from exif.
            ExifDirectory exif = getExifDirectory();
            if (exif!=null && exif.containsTag(ExifDirectory.TAG_EXIF_IMAGE_WIDTH)) {
                try {
                    fullWidth = exif.getInt(ExifDirectory.TAG_EXIF_IMAGE_WIDTH);
                    fullHeight = exif.getInt(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT);
                } catch (Exception e) {
                    System.out.println("setWebSize from exif failed"+e);
                }
            }
        } 
        else {
            JPhotoExif exif = getExif();
            if (exif.height!=fullHeight || exif.width!=fullWidth) {
                // EXIF width/height is quite often broken in potraits, fix them when loading image
                exif.height = fullHeight;
                exif.width = fullWidth;
                setDirtyExif(true);
                setStatus(getStatus()); // notify observers
                JPhotoStatus.showStatus(null, getOriginalFile()+": Invalid EXIF width/height, save album!");
            }            
        }
        
        if (fullWidth>0 && fullHeight>0) {
            Dimension dim = Utils.getScaledSize(limits,fullWidth,fullHeight, MIN_DIMENSION);
            width = dim.width;
            height = dim.height;
        }
        else {
            System.out.println("setWebSize: failed to get full width and height for '"
                               +getImageName()+"', "+exif);
        }
    }

    /** Save thumbnail from original file as JPG to given file.
     */
    public boolean saveThumb(File target) {
        try {
            byte thumbData[] = directory.getThumbnailData();
            if (thumbData==null)
                return false;
            FileOutputStream out = new FileOutputStream(target);
            out.write(thumbData);
            out.close();
            return true;
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(null, "saveThumb "+target+" error " + e);
            return false;
        }
    }
    
    /** Save web size image to given file. Optionally save the thumb as well
     * (the thumb will fit in thumbLimits (160x160)). Images are written
     * only if they do not exist already. Write watermark in the bottom left
     * corner if it was given.
     */
    public boolean saveWebImages(File target, File thumbTarget, String watermark) {
        boolean later = false;
        if (original!=null && target!=null)
            later = target.lastModified()<original.lastModified();
        if (!target.exists() 
                || (thumbTarget!=null && !thumbTarget.exists()) 
                || (later)) {
            BufferedImage fullImg = getFullImage();
            if (fullImg==null)
                return false;
            
            BufferedImage thumbImg = null;
            if (!target.exists() || later) {
                long ticks = System.currentTimeMillis();
                Image img = fullImg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                
                BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null),
                                                     BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, null, null);
                g2.dispose();
                
                UnsharpMaskFilter usm = new UnsharpMaskFilter();
                // This is much faster than scaling the original to thumb size
                thumbImg = usm.filter(generateThumb(bi), null);
                
                // Sharpen the bigger one only after creating the thumb
                bi = usm.filter(bi, null);
                
                // Filter changed bi instance, must create new one
                g2 = bi.createGraphics();
                if (watermark!=null) {
                    g2.setFont(getWatermarkFont());
                    Utils.drawWrappedText(g2, 1, -(getHeight()-1),
                                          getWidth(), watermark);
                }
                g2.dispose();
                if (!saveJpg(target, bi, 0.9f))
                    return false;
                else
                    JPhotoStatus.showStatus(null, target+" scaled and saved"
                                            + " in " +(System.currentTimeMillis()-ticks)+"ms. ");
                Thread.yield();
            }
        
            if (thumbTarget!=null && (!thumbTarget.exists() || later)) {
                long ticks = System.currentTimeMillis();
                
                if (thumbImg==null)
                    thumbImg = generateThumb(fullImg);

                if (!saveJpg(thumbTarget, thumbImg, 0.7f))
                    return false;
                else
                    JPhotoStatus.showStatus(null, thumbTarget+" scaled and saved"
                                            +" in "+(System.currentTimeMillis()-ticks)+"ms. ");
                Thread.yield();
            }
        }
        return true;
    }

    public boolean saveTransformedImage(File target, String watermark) {
        BufferedImage fullImg = getFullImage();
        if (fullImg==null)
            return false;
        
        long ticks = System.currentTimeMillis();
        BufferedImage bi = new BufferedImage(fullImg.getWidth(),
                                             fullImg.getHeight(),
                                             BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(fullImg, null, null);

        if (watermark!=null) {
            int fontSize = 2*(fullImg.getHeight()/160);
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            System.out.println("font:"+fontSize);
            Utils.drawWrappedText(g2, 1, -(fullImg.getHeight()-1),
                                  fullImg.getWidth(), watermark);
        }
        g2.dispose();
        if (!saveJpg(target, bi, 0.8f))
            return false;
        
        JPhotoStatus.showStatus(null, target+" transformed and saved"
                                + " in " +(System.currentTimeMillis()-ticks)+"ms. ");
        return true;
    }
    
    /** Generate a high-quality thumbnail for given image.
     * Thumb is always one fourth of the web image dimensions
     * (Stylesheets assume this as well as this method.)
     */
    public BufferedImage generateThumb(BufferedImage img) {
        long ticks = System.currentTimeMillis();
        Dimension dim = new Dimension(img.getWidth()/4,img.getHeight()/4);
        BufferedImage thumbImg = new BufferedImage(dim.width, dim.height,
                                                   BufferedImage.TYPE_INT_RGB);
        Image scaledImg = img.getScaledInstance(dim.width, dim.height, Image.SCALE_SMOOTH);
        Graphics2D g2 = thumbImg.createGraphics();
        g2.drawImage(scaledImg, null, null);
        g2.dispose();
        System.out.println("Scaled thumb to "+dim+" in "+(System.currentTimeMillis()-ticks)+"ms. ");
        return thumbImg;
    }

    /**
     *  Returns the image resized to fit the screen of a given size
     * @return BufferedImage of the resized picture
     * @param img BufferedImage of the source picture
     * @param slideLimits Dimension of the maximum size the picture should reach
     */
    public BufferedImage generateSlide(BufferedImage img, Dimension slideLimits) {
        Dimension dim = Utils.getScaledSize(slideLimits,img.getWidth(),img.getHeight());
        BufferedImage sldImg = new BufferedImage(dim.width, dim.height,
                                                   BufferedImage.TYPE_INT_RGB);
        Image scaledImg = img.getScaledInstance(dim.width, dim.height, Image.SCALE_SMOOTH);
        Graphics2D g2 = sldImg.createGraphics();
        g2.drawImage(scaledImg, null, null);
        g2.dispose();
        return sldImg;
    }

    /**
     * returns output stream of the slide image
     * @return ByteArrayInputStream of the image, can be writen directly into file
     * @param resol resolution of the image [1-3] 1...800x600; 2...1024x768; 3...1280x1024
     */
    public ByteArrayInputStream getSlideStream(int resol) {
        Dimension d = new Dimension(slideLimits1);
        if (resol == 2) d = slideLimits2;
        if (resol == 3) d = slideLimits3;
        
        BufferedImage bi = generateSlide(this.getFullImage(),d);
        ByteArrayInputStream slide = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality(0.8f,false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(bi);
            bi.flush();
            slide = new ByteArrayInputStream(out.toByteArray());
            return slide;
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(null, "getSlideStream error " + e);
            return null;
        }
        finally {
            try {
                out.close();
            }
            catch (Exception e) {
                JPhotoStatus.showStatus(null, "getSlideStream error " + e);
            }
        }
    }

    /** Save subtitled image to given file. The image is scaled to fit 640x480
     * and centered. Text is drawn in the bottom.
     */
    public boolean saveSubtitledImage(File target, String subtitle) {
        Image fullImg = getFullImage();

        long ticks = System.currentTimeMillis();
        
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        if (h>w) { // Potraits must be scaled down further
            w = (480*getWidth())/640;
            h = (480*getHeight())/640;
            x = 20; // Add some offset since some TVs don't show the full image
        }
        
        BufferedImage bi = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        if (fullImg!=null) {
            Image img = fullImg.getScaledInstance(w,h, Image.SCALE_SMOOTH);
            g2.drawImage(img, new AffineTransform(1f,0f,0f,1f,x,y), null);
        }
        if (subtitle!=null) {
            g2.setFont(getFont());
            if (h>w)
                Utils.drawWrappedText(g2, w+x+4, -480, 640-x-w-2*4, subtitle);
            else
                Utils.drawWrappedText(g2, 25, -480, 640-35, subtitle);
                
        }
                    
        g2.dispose();
        if (!saveJpg(target, bi, 0.9f))
            return false;
        else
            JPhotoStatus.showStatus(null, target+" subtitled, scaled and saved"
                                    + " in " +(System.currentTimeMillis()-ticks)+"ms. ");
        Thread.yield();
        return true;
        
    }

    public boolean saveJpg(File target, BufferedImage bi, float quality) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(target); 
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality(quality,false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(bi);
            bi.flush();
            return true;
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(null, "saveWebImage "+target+" error " + e);
            return false;
        }
        finally {
            try {
                out.close();
            }
            catch (Exception e) {
                JPhotoStatus.showStatus(null, "saveWebImage "+target+" close error " + e);
            }
        }

    }
    
    public void clearCaches() {
        badFile = false;
        exif = null;
        directory = null;
        thumbImage = null;
        fullImage = null;
        width = -1;
        height = -1;
        fullWidth = -1;
        fullHeight = -1;
    }

    protected class ThumbLoader extends Thread {
        java.util.List list = Collections.synchronizedList(new LinkedList());
        public ThumbLoader() {
            // Nothing
        }

        public void startLoading(JPhoto photo) {
            if (photo.badFile || photo.getOriginalFile()==null)
                return;
            
            synchronized(list) {
                // System.out.println("ThumbLoader: start "+photo.getImageName());
                if (list.indexOf(photo)<0) {
                    photo.setStatus("Loading...");
                    list.add(0, photo); // Not yet in load list, add it
                    list.notify();
                }
            }
        }
        
        public void run() {
            System.out.println("ThumbLoader started");
            while (true) {
                JPhoto photo = null;
                try {
                    while (list.size()>0) {
                        photo = (JPhoto)list.remove(0);
                        // System.out.println("ThumbLoader: loading "+photo.getImageName());
                        photo.getThumbImage();
                        if (photo.getCachedThumb()==null) {
                            // No exif, must read the full image to generate thumb
                            // This method will do it as a side-effect.
                            photo.getFullImage();
                        }
                        if (photo.getCachedThumb()!=null)
                            photo.setStatus("Loaded.");
                        photo = null;
                    }
                    synchronized(list) {
                        list.wait();
                    }
                }
                catch (Exception e) {
                    if (photo!=null)
                        photo.setStatus(e.toString());
                    System.out.println("ThumbLoader:"+e);
                }
            }
        }
    }
	
    /**
	 * @return Returns the dirtyExif.
	 */
	public boolean isDirtyExif() {
		return dirtyExif;
	}
	/**
	 * @param dirtyExif The dirtyExif to set.
	 */
	public void setDirtyExif(boolean dirtyExif) {
		this.dirtyExif = dirtyExif;
	}
}
