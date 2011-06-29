/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoCollection.java,v 1.6 2006/10/15 17:36:48 jkarjala Exp $
 */

/** A collection of JPhoto objects describing a page of photos
 */

package fi.iki.jka;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class JPhotoCollection extends AbstractListModel
    implements Transferable, Serializable, Observer, IPhotoCollection {

    public static String THUMBS_DIRECTORY = "thumbs/";
	public static String PICTURES_DIRECTORY= "pictures/";

    public static final String MAPPING_FILE = "jphoto.mapping.xml";
    
    public static final DataFlavor PHOTOCOLLECTION_FLAVOR
        = new DataFlavor(JPhotoCollection.class, "List of JPG Photographs");
    
    protected DataFlavor[] myFlavors = {PHOTOCOLLECTION_FLAVOR};

    boolean haveGui = true;
    boolean dirty = false;
    
    File targetDir = null;
    
    JPhotoPageInfo pageInfo = new JPhotoPageInfo();
    ArrayList photos = new ArrayList();
    ArrayList categoryNames = new ArrayList();
    static HashMap allCollections = new HashMap();
    private static boolean htmlOnly;
        
    public JPhotoCollection() {
        // For unmarshaling
    }

    public JPhotoCollection(String xmlName) {
        System.out.println("JPhotoCollection("+xmlName+")");
        load(xmlName);
    }
    
    public JPhotoCollection(String names[]) {
        addAll(0,names);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean v) {
        dirty = v;
        // System.out.println("setDirty:"+v+" from "+Utils.getCallingMethod());
    }
    
    // For unmarshaling only
    public ArrayList getPhotos() {
        return photos;
    }

    // For unmarshaling only
    public void setPhotos(ArrayList photos) {
        this.photos = photos;
        Iterator i = photos.iterator();        
        while (i.hasNext()) {
            JPhoto p = (JPhoto)i.next();
            p.addObserver(this);
            p.setOwner(this);
        }
        setDirty(true);
        fireIntervalAdded(this, 0, photos.size());
    }

    public void clear() {
        int count = photos.size();
        Iterator i = photos.iterator();        
        while (i.hasNext()) {
            JPhoto p = (JPhoto)i.next();
            p.deleteObserver(this);
            p.setOwner(null);
        }
        photos.clear();
        photos = null;
        setDirty(true);
        if (count>0)
            fireIntervalRemoved(this, 0, count);
    }
    
    // For unmarshaling only
    public ArrayList getCategoryNames() {
        return categoryNames;
    }

    // For unmarshaling only
    public void setCategoryNames(ArrayList categoryNames) {
        this.categoryNames = categoryNames;
        setDirty(true);
    }

    public String getTitle() {
        return getPageInfo().title;
    }
    public void setTitle(String title) {
        pageInfo.title = title;
        setDirty(true);
    }

    public String getDescription() {
        return getPageInfo().description;
    }
    public void setDescription(String description) {
        pageInfo.description = description;
        setDirty(true);
    }
    
    public String getKeywords() {
        return getPageInfo().keywords;
    }
    public void setKeywords(String s) {
        pageInfo.keywords = s;
        setDirty(true);
    }

    public String getWatermark() {
        return getPageInfo().watermark;
    }
    public void setWatermark(String s) {
        pageInfo.watermark = s;
        setDirty(true);
    }

    public Color getForegroundColor() {
        try {
            return Utils.convertValueToColor(getPageInfo().foreground);
        } catch (Exception e) {
            System.out.println("Invalid foreground color:"+getPageInfo().foreground);
            setForegroundColor(Color.black);
            return Color.white;
        }
    }
    public void setForegroundColor(Color color) {
        pageInfo.foreground = Utils.convertColorToValue(color);
        setDirty(true);
    }

    public Color getBackgroundColor() {
        try {
            return Utils.convertValueToColor(getPageInfo().background);
        }
        catch (Exception e) {
            System.out.println("Invalid background color:"+getPageInfo().background);
            setBackgroundColor(Color.white);
            return Color.black;
        }
        
    }
    public void setBackgroundColor(Color color) {
        pageInfo.background = Utils.convertColorToValue(color);
        setDirty(true);
    }

    public String getOutputDirectory() {
        return getPageInfo().outputDirectory;
    }
    public void setOutputDirectory(String s) {
        pageInfo.outputDirectory = s;
        // setDirty(true); let's not make document dirty every time we save it
    }

    public JPhotoPageInfo getPageInfo() {
        return pageInfo;
    }
    
    public void setPageInfo(JPhotoPageInfo info) {
        pageInfo = info;
        setDirty(true);
    }

    public void setCoverPhoto(JPhoto photo) {
        // XXX Should somehow notify other collections which might link to us,
        // since they should update their display!
        pageInfo.coverPhotoName = photo.getImageName();
        setDirty(true);
    }
    
    public JPhoto getCoverPhoto() {
        if (photos.size()>0) {
            Iterator iter = photos.iterator();        
            while (iter.hasNext()) {
                JPhoto p = (JPhoto)iter.next();
				String name = p.getImageName();
				if ( !name.equals("") && pageInfo.coverPhotoName.equals(""))
					return p;
				else {
        			if (name.equals(pageInfo.coverPhotoName))
                    	return p;
            	}
            }                
			System.out.println("getCoverPhoto: cover photo "+pageInfo.coverPhotoName+" not found");
        }
        return null;
    }

    public BufferedImage getCoverIcon() {
        JPhoto coverPhoto = getCoverPhoto();
        if (coverPhoto!=null) {
            BufferedImage icon = coverPhoto.getThumbImage();
            if (icon!=null) {
                Dimension limits = new Dimension(64,64);
                limits = Utils.getScaledSize(limits,icon.getWidth(),icon.getHeight());
                BufferedImage bi = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(icon, (64-limits.width)/2,(64-limits.height)/2,
                             limits.width,limits.height,null, null);
                //g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                //g2.drawString("Jph",0,12);
                g2.dispose();
                return bi;
            }
        }
        return null;
    }
    
    public Iterator iterator() {
        return photos.iterator();
    }
        
    public void add(int index, JPhoto photo) {
        if (index<photos.size())
            photos.add(index, photo);
        else
            photos.add(photo);
        photo.addObserver(this);
        photo.setOwner(this);
        setDirty(true);
        fireIntervalAdded(this, index,index);
    }
    
    public void addAll(int index, String files[]) {
        for (int i=0; i<files.length; i++) {
            JPhoto photo = null;
            File file = new File(files[i]);
            if (file.isDirectory())
                photo = new JPhotoDirectory(this, file);
            else
                photo = new JPhoto(this, file);
            add(index, photo);
            index++;
        }
    }
    
    public boolean remove(JPhoto photo) {
        int index = photos.indexOf(photo);
        if (index<0)
            return false;
        else
            photos.remove(index);
        photo.deleteObserver(this);
        setDirty(true);
        fireIntervalRemoved(this, index,index);
        return true;
    }

    public int indexOf(JPhoto photo) {
        return photos.indexOf(photo);
    }

    public JPhoto get(int index) {
        return (JPhoto)photos.get(index);
    }
    
    public void findOriginals(String files[]) {
        for (int i=0; i<files.length; i++) {
            File file = new File(files[i]);
            String targetName = file.getName().toLowerCase();
            
            Iterator iter = photos.iterator();        
            while (iter.hasNext()) {
                JPhoto p = (JPhoto)iter.next();
                if (p.getImageName().toLowerCase().equals(targetName)) {
                    System.out.println("Found "+file);
                    p.setOriginalName(file);
                }
            }
        }
    }

    public int size() {
        return photos.size();
    }
    
    // Observer method called from Observable.notifyObservers()
    // This is called from non-swing thread, must invokeLater the list notify.
    public void update(Observable o, Object arg) {
        JPhoto photo = null;
        if (arg instanceof JPhoto)
            photo = (JPhoto)arg;
        else
            photo = (JPhoto)o;

        final int index = photos.indexOf(photo);
        // System.out.println("update:"+pageInfo.albumName+":"+(photo).getImageName()+":"+index+":"+(photo).getStatus());
        if (index<0)
            System.out.println("JPhotoCollection.update:"+photo+" not found from collection?");
        else {
            try {
            	if (photo.isDirtyExif()) {
            		// Exif mismatches image, so the album should be resaved to get EXIF fixed
            		photo.setDirtyExif(false);
            		setDirty(true);
            	}
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            fireContentsChanged(this, index,index);
                        }
                    });
            }
            catch (Exception e) {
                System.out.println("JPhotoCollection.update:"+e);
            }
        }
    }

    // ListModel method
    public Object getElementAt(int index) {
        if (index<photos.size())
            return photos.get(index);
        else
            return null;
    }

    // ListModel method
    public int getSize() {
        return photos.size();
    }

    // Transferable method
    public Object getTransferData(DataFlavor flavor) {
        // System.out.println("getTransferData:"+flavor);
        return this;
    }
    // Transferable method
    public DataFlavor[] getTransferDataFlavors() {
        // System.out.println("getTransferDataFlavors");
        return myFlavors;
    }
    // Transferable method
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // System.out.println("isDataFlavorSupported:"+flavor);
        return flavor.equals(myFlavors[0]);
    }

    public void setTargetDir(String filepath) {
        pageInfo.albumName = Utils.getFileName(new File(filepath).getName());
        try {
            targetDir = new File(filepath).getCanonicalFile();
            if (!targetDir.isDirectory())
                targetDir = targetDir.getParentFile();
        } catch (Exception e) {
            System.out.println("setTargetDir:"+e);
            targetDir = null;
        }
    }

    public File getTargetDir() {
        return targetDir;
    }

    public boolean load(String filename) {
        try {
            JPhotoCollection collection = (JPhotoCollection)allCollections.get(filename);
            
            if (allCollections.get(filename)==null) {
                JPhotoStatus.showStatus(null, "Loading "+filename);
                JPhoto.setDefaultOwner(this);
                setTargetDir(filename);
                
                Mapping mapping = getMapping();
                Unmarshaller unmarshaller = new Unmarshaller(mapping);
                collection = (JPhotoCollection)unmarshaller
                    .unmarshal(new InputSource(new FileInputStream(filename)));

                allCollections.put(filename, collection);
                JPhotoStatus.showStatus(null, "Loaded from "+filename);
            }
            else {
                System.out.println("Using cached "+filename);
            }
            
            this.setPhotos(collection.getPhotos());
            this.setPageInfo(collection.getPageInfo());
            this.setCategoryNames(collection.getCategoryNames());
            setTargetDir(filename); // Update directory and albumname in pageInfo
            setDirty(false);
            
            allCollections.put(filename, this);
            return true;
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(null, "Load error:"+e);
            return false;
        }
    }
    
    public boolean save(String filename) {
    	// Remove old instance in case this is a Save As...
		allCollections.values().remove(this);
		
        JPhoto.setDefaultOwner(this);
        setTargetDir(filename);
		
        dirty = (save(new File(filename), null)==false);
        if (!dirty) {
            // Update cache only on normal save
            allCollections.put(filename, this);            
            JPhotoStatus.showStatus(null, "Saved to "+filename);
        }
        return !dirty;
    }

    public boolean save(File target, String stylesheet) {
        System.out.println("Start saving to "+target+ " with style "+stylesheet);
        OutputStream outs = null;
        try {
            // useful for external XML transformers
            setOutputDirectory(target.getParent());
            
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document photoDoc = builder.newDocument();
            Mapping mapping = getMapping();
            Marshaller marshaller = new Marshaller(photoDoc);
            marshaller.setMapping(mapping);
            marshaller.marshal(this);           

            DOMSource src = new DOMSource(photoDoc);
            outs = new FileOutputStream(target);
            StreamResult res = new StreamResult(outs);
            Transformer trans;
            if (stylesheet==null) { // Plain XML output
                trans = TransformerFactory.newInstance().newTransformer();
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            else
                trans = getTransformer(target, stylesheet);
            
            trans.transform(src, res);

            System.out.println("Saved using "+stylesheet);
            JPhotoStatus.showStatus(null, "Wrote "+target
                                    +(stylesheet!=null ? " using template "+stylesheet : ""));
            return true;
        } catch (Exception e) {
            JPhotoStatus.showStatus(null, "Save error:"+e);
            //e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (outs!=null)
                    outs.close();
            } catch (Exception e) {
                JPhotoStatus.showStatus(null, "Save failed close:"+e);
                // e.printStackTrace();
            }
        }
    }

    /** Concatenate all albums into one and transform it with stylesheet
     */
    public boolean exportTree(File target, String stylesheet) {
        System.out.println("Start exportTree to "+target+ " with style "+stylesheet);
        OutputStream outs = null;
        try {
            // useful for external XML transformers
            setOutputDirectory(target.getParent());
            
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document collectedDocs = builder.newDocument();
            Element rootElement = collectedDocs.createElement("all");
            Attr a = collectedDocs.createAttribute("root");
            a.setValue(target.getParent());
            rootElement.setAttributeNode(a);
            collectedDocs.appendChild(rootElement);
            albumToDocument(this, rootElement, target.getParent());

            DOMSource src = new DOMSource(collectedDocs);
            outs = new FileOutputStream(target);
            StreamResult res = new StreamResult(outs);
            Transformer trans;
            trans = getTransformer(target, stylesheet);
            
            trans.transform(src, res);

            System.out.println("Saved using "+stylesheet);
            JPhotoStatus.showStatus(null, "Wrote "+target
                                    +(stylesheet!=null ? " using template "+stylesheet : ""));
            return true;
        } catch (Exception e) {
            JPhotoStatus.showStatus(null, "Export error:"+e);
            //e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (outs!=null)
                    outs.close();
            } catch (Exception e) {
                JPhotoStatus.showStatus(null, "Save failed close:"+e);
                // e.printStackTrace();
            }
        }
    }

    public boolean albumToDocument(JPhotoCollection album, Element root, String basePath) {
        System.out.println("Add "+album.pageInfo.outputDirectory+" "+album.pageInfo.albumName);
        try {
            // relative path is useful for external XML transformers
            String path = "";
            String oldpath = album.pageInfo.outputDirectory;
            
            if (basePath.length()+1 < oldpath.length()) {
                path = oldpath.substring(basePath.length()+1);
                path = path.replace('/', '\\');
                if (path.length()>0 && !path.endsWith("/"))
                    path = path+"/";
            }
            album.setOutputDirectory(path);
            
            Mapping mapping = getMapping();
            Marshaller marshaller = new Marshaller(root);
            marshaller.setMapping(mapping);
            marshaller.marshal(album);

            Iterator iter = album.photos.iterator();
            while (iter.hasNext()) {
                JPhoto p = (JPhoto)iter.next();
                if (p instanceof JPhotoAlbumLink) {
                    JPhotoCollection a = new JPhotoCollection(p.getFullAlbumLink());
                    System.out.println("Add next...");
                    albumToDocument(a, root, basePath);
                }
            }
            // Restore original path
            album.pageInfo.outputDirectory = oldpath;
            return true;
        } catch (Exception e) {
            JPhotoStatus.showStatus(null, "error:"+e);
            //e.printStackTrace();
            return false;
        }
    }
    
    
    public Mapping getMapping() throws Exception {
        Mapping mapping = new Mapping();
        Reader mappingReader = null;
        try {
            InputStream res = getClass().getClassLoader().getResourceAsStream(MAPPING_FILE);
            mappingReader = new InputStreamReader(res);
        }
        catch (Exception e) {
            System.out.println("Not in resource, trying from "+MAPPING_FILE);
            mappingReader  = new FileReader(new File(MAPPING_FILE));
        }
        mapping.loadMapping(new InputSource(mappingReader));
        mappingReader.close();
        return mapping;
    }

    public Transformer getTransformer(File target, String stylesheet) throws Exception {
        InputStream ins = null;
        File f = new File(target.getParentFile(), stylesheet);
        if (!f.exists()) {
            if (target.getParentFile()!=null)
                f = new File(target.getParentFile().getParentFile(), stylesheet);
            if (!f.exists())
                f = new File("templates/"+stylesheet);
        }
        if (f.exists()) {
            stylesheet = f.getAbsolutePath();
            JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "Using stylesheet "+stylesheet);
            try {
                Transformer trans = TransformerFactory.newInstance()
                    .newTransformer(new StreamSource(stylesheet));
                return trans;            
            }
            catch (Exception e) {
                JPhotoStatus.showStatus(null, "Cannot read "+stylesheet+":"+e);
                return null;
            }
        }
        else {
            System.out.println("Using templates/"+stylesheet+" from resources.");
            ins = getClass().getClassLoader().getResourceAsStream("templates/"+stylesheet);
            StreamSource src
                = new StreamSource(ins,getClass().getClassLoader().getResource("templates/").toString());
            Transformer trans = TransformerFactory.newInstance()
                .newTransformer(src);
            ins.close();
            return trans;
        }
    }

    public boolean exportSubtitledPhotos(String album) {
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT_SUBTITLED, "Exporting subtitled photos for "+album, 0);
        return exportPhotos(new File(album), "subtitled/", null, true);
    }

    /**
     * exports instance of JPhotoCollection into self executable slideshow
     * @return true if everything goes right
     * @param targetFile path to the target .jar file
     * @param resol resolution of the image [1-3] 1...800x600; 2...1024x768; 3...1280x1024
     */
    public boolean exportSlideshow(String targetFile, int resol) {
        JPhotoSlideShowMaker ssm = new JPhotoSlideShowMaker(targetFile, photos, resol);
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT_SLIDESHOW_INDEX, "Preparing export...", 0);
        ssm.start(); //runs as a separate thread
        return(true);
    }

    public boolean exportHtmlJari1(String album) {
        String template = "jphotolist.xsl";
        File htmlTarget
            = new File(Utils.getFileBase(album, JPhotoFrame.FILE_EXT)+".html");
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Exporting to "+htmlTarget, 0);
        if (!save(htmlTarget, template)) {
            JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Failed", 100);
            return false;
        }
        if (htmlOnly)
            return true;
        return exportPhotos(htmlTarget, PICTURES_DIRECTORY, THUMBS_DIRECTORY, false);
    }
    
    public boolean exportTemplateJari1(File target) {
        boolean status = false;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("templates/jphotolist.xsl");
            status = Utils.copyStreamToFile(in, new File(target, "jphotolist.xsl"));
            in.close();
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT_TEMPLATE, "failed:"+e);
        }
        return status;
    }
    
    public boolean exportHtmlJari2(String album) {
        File htmlTarget
            = new File(Utils.getFileBase(album,JPhotoFrame.FILE_EXT)+"-thumbs.html");
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Exporting to "+htmlTarget, 0);
        if (!save(htmlTarget, "jphotothumbs.xsl")) {
            JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Failed", 100);
            return false;
        }
        File frameTarget
            = new File(Utils.getFileBase(album,JPhotoFrame.FILE_EXT)+"-frame.html");
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Exporting to "+frameTarget, 0);
        if (!save(frameTarget, "jphotoframe.xsl")) {
            JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Failed to write frame", 100);
            return false;
        }
        File jsTarget = new File(new File(album).getParent(), "jphoto.js");
        boolean status = false;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("templates/jphoto.js");
            status = Utils.copyStreamToFile(in, jsTarget);
            in.close();
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT_TEMPLATE, "failed:"+e);
            status = false;
        }
        if (status==false) {
            JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Failed to write JavaScript", 100);
            return false;
        }
        
        if (htmlOnly)
            return true;
        return exportPhotos(htmlTarget, PICTURES_DIRECTORY, THUMBS_DIRECTORY, false);
    }
    
    public boolean exportTemplateJari2(File target) {
        boolean status = false;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("templates/jphotolist.xsl");
            status = Utils.copyStreamToFile(in, new File(target, "jphotolist.xsl"));
            in.close();
            in = getClass().getClassLoader().getResourceAsStream("templates/jphotothumbs.xsl");
            status = Utils.copyStreamToFile(in, new File(target, "jphotothumbs.xsl"));
            in.close();
            in = getClass().getClassLoader().getResourceAsStream("templates/jphotoframe.xsl");
            status = Utils.copyStreamToFile(in, new File(target, "jphotoframe.xsl"));
            in.close();
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT_TEMPLATE, "failed:"+e);
        }
        return status;
    }

    public boolean exportHtmlJari3(String album) {
        String template = "jphotoindexall.xsl";
        File htmlTarget
            = new File(Utils.getFileBase(album, JPhotoFrame.FILE_EXT)+"-all.html");
        JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "Exporting to "+htmlTarget+"...");
        // JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Exporting to "+htmlTarget, 0);
        if (!exportTree(htmlTarget, template)) {
            // JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Failed", 100);
            return false;
        }
        // XXX Thumbs not saved
        JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "Exported to "+htmlTarget);
        return true;
    }
    
    public boolean exportTemplateJari3(File target) {
        boolean status = false;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("templates/jphotoindexall.xsl");
            status = Utils.copyStreamToFile(in, new File(target, "jphotoindexall.xsl"));
            in.close();
        }
        catch (Exception e) {
            JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT_TEMPLATE, "failed:"+e);
        }
        return status;
    }
    

    /*
    public boolean exportHtmlTarja(File target) {
        System.out.println("Start Tarjas export to "+target);
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Exporting to "+target, 0);
        
        String path = null;

        if (target == null || target.getParent() == null)
            path = ".";
        else
            path = target.getParent();
        JPhoto.setDefaultOwner(this);
        setTargetDir(path);
        // JPhoto.setTargetDir(path);

        Html.generateDirectories( path + File.separator);

        exportPhotos(target, Html.PICTURES_DIRECTORY, Html.THUMBS_DIRECTORY, false);

        Html.generateIndexFile(path, this, getTitle(), 5);
        Html.generatePictureHtml(path, null, this);
        JPhotoStatus.showStatus(null, "Wrote "+path+"/index.html");
        
        return true;
    }
    */
    
    public boolean exportHtmlTarja2(String albumFileName) {
        
        System.out.println("Start Tarjas HTML export from album "+ albumFileName);

        JPhoto.setDefaultOwner(this);
        setTargetDir(albumFileName);
        
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT, "Generating HTML to "+albumFileName, 0);

        if (photos.size() == 0){
            System.out.println("No photos in the album.");
            return true;
        }

        String argv[] = {"-in", albumFileName, "-xsl",  "templates/JPhotoChooser.xsl"};
        try {
            org.apache.xalan.xslt.Process.main(argv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        exportPhotos(getTargetDir(), "pictures", "thumbs", true);
        JPhotoStatus.showStatus(null, "Wrote "+getTargetDir()+"/index.html");
        
        return true;
    }
    
    public boolean exportHtmlJuha(File target) {
        return false;
    }
    
    public ArrayList getPhotos(String category) {
        ArrayList result = new ArrayList();
        for (int i = 0; i < photos.size(); i++) {
            JPhoto photo = (JPhoto)photos.get(i);
            if (photo.getCategory() != null && photo.getCategory().equals(category))
                result.add(photo);
        }
        return result;
    }

    public void disableGui() {
        haveGui = false;
    }
    
    public boolean exportPhotos(File target, String picPrefix, String thumbPrefix,
                                boolean subtitles) {
        ExportPhotosThread runner
            = new ExportPhotosThread(target, picPrefix, thumbPrefix, subtitles);

        if (haveGui) {
            runner.start();
            // NOte that the thread will continue exporting in the background,
            // the progress dialog will remain open and main window disabled
            // until the thread calls progress with 100% completion.
            return true;
        }
        else {
            // Run synchronously if do not have GUI
            runner.run();
            return true;
        }
    }


    public class ExportPhotosThread extends Thread {
        public boolean status = false;
        protected File dir;
        protected String targetBase;
        protected String picPrefix;
        protected String thumbPrefix;
        protected boolean subtitles;
        public ExportPhotosThread(File target, String picPrefix, String thumbPrefix,
                                  boolean subtitles) {
            target = target.getAbsoluteFile();
            targetBase = Utils.getFileName(target.getName());
            this.dir = target.getParentFile();
            this.subtitles = subtitles;
            dir.mkdirs();
            this.picPrefix = picPrefix;
            if (picPrefix.endsWith("/"))
                new File(dir,picPrefix).mkdir();
            this.thumbPrefix = thumbPrefix;
            if (thumbPrefix!=null && thumbPrefix.endsWith("/"))
                new File(dir,thumbPrefix).mkdir();
        }
        public void run() {
            try {
                boolean sizeMismatch = false;
                int count = 1;
                Iterator i = photos.iterator();        
                while (i.hasNext() && !JPhotoStatus.isCanceled()) {
                    JPhoto p = (JPhoto)i.next();
                    File web = null;
                    if (subtitles)
                        web = new File(dir, picPrefix+targetBase
                                       +(count<10?"0":"")
                                       +(count<100?"0":"")
                                       +Integer.toString(count)
                                       +"."+Utils.getFileExt(p.getImageName(), "jpg"));
                    else
                        web = new File(dir, picPrefix+p.getImageName());
                    File thumb = null;
                    if (thumbPrefix!=null)
                        thumb = new File(dir, thumbPrefix+p.getImageName());
                    JPhotoStatus.showProgress(null, "Processing "+p.getImageName()+"...",
                                              5+(count*95)/photos.size()-1);
                    if (subtitles)
                        p.saveSubtitledImage(web, p.getText());
                    else {
                        BufferedImage fullImg = p.getFullImage();
                        if (fullImg!=null
                                && fullImg.getHeight()!=p.exif.height) {
                            System.out.println("Fixing incorrect EXIF image size for "+p.getOriginalName());
                            p.fullHeight = p.exif.height = fullImg.getHeight();
                            p.fullWidth = p.exif.width = fullImg.getWidth();
                            p.setWebSize();
                            setDirty(true);
                            sizeMismatch = true;
                        }
                        
                        p.saveWebImages(web, thumb, pageInfo.watermark);
                    }
                    Thread.yield();
                    count++;
                }
                if (JPhotoStatus.isCanceled())
                    JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "Export cancelled.");
                else if (sizeMismatch) {
                    JOptionPane.showMessageDialog(null, "Fixed image sizes in Album, please save and export again.",
                            JPhotoFrame.APP_NAME, JOptionPane.ERROR_MESSAGE);
                    status = false;
                }
                else
                    status = true;
            } catch (Exception e) {
                JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "exportPhotos failed:"+e);
                e.printStackTrace();
                status = false;
            }
            JPhotoStatus.showProgress(null, "Done", 100);
        }
    };
    
    public boolean copyOriginals(String album, String picPrefix, boolean transfrom) {
        JPhotoStatus.showProgress("Copying and watermarking originals... ", "", 0);

        File target = new File(album).getParentFile();
        CopyThread runner = new CopyThread(target, picPrefix, transfrom);
        runner.start();
        // NOte that the thread will continue exporting in the background,
        // the progress dialog will remain open and main window disabled until
        // the thread calls progress with 100% completion.
        return true;
    }


    public class CopyThread extends Thread {
        public boolean status = false;
        protected File dir;
        protected String picPrefix;
        protected boolean transform;

        public CopyThread(File target, String picPrefix, boolean trans) {
            this.dir = new File(target.getAbsoluteFile(), picPrefix);
            dir.mkdirs();
            this.transform = trans;
        }
        public void run() {
            try {
                int count = 1;
                Iterator i = photos.iterator();        
                while (i.hasNext() && !JPhotoStatus.isCanceled()) {
                    JPhoto p = (JPhoto)i.next();
                    File target = new File(dir, p.getImageName());
                    JPhotoStatus.showProgress(null, "Processing "+p.getImageName()+"...",
                                              5+(count*95)/photos.size()-1);
                    if (transform)
                        p.saveTransformedImage(target, pageInfo.watermark);
                    else
                        p.saveTransformedImage(target, null);
                    Thread.yield();
                    count++;
                }
                if (JPhotoStatus.isCanceled())
                    JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "Export cancelled.");
                status = true;
            } catch (Exception e) {
                JPhotoStatus.showStatus(JPhotoMenu.A_EXPORT, "exportPhotos failed:"+e);
                e.printStackTrace();
                status = false;
            }
            JPhotoStatus.showProgress(null, "Done", 100);
        }
    };

    public String toString() {
        return "JPhotoCollection: "+photos.size()+" photos";
    }
    
    public static void main(String args[]) throws Exception {
        /**
        JPhotoCollection page = load(args[0]);

        Iterator i = page.photos.iterator();
        while (i.hasNext()) {
            JPhoto photo = (JPhoto)i.next();
            System.out.println(photo);
            System.out.println("exif="+photo.getExif());
        }
        **/
    }

    /**
     * If true skip size verification and image generation, just create HTMLs when exporting. 
     * Used in batch operations.
     * @param b
     */
    public static void setHtmlOnlyMode(boolean b) {
        htmlOnly = b;
    }
}
