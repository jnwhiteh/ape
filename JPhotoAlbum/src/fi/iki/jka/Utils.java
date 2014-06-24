/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: Utils.java,v 1.2 2007/01/14 17:54:31 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/** Static utility methods
 */
public class Utils {
    
    /**
     * Converts Color to String value between #000000 and #FFFFFF
     */
    public static String convertColorToValue(Color color) {
        return "#" + (color.getRed()<16?"0":"")+Integer.toHexString(color.getRed()).toUpperCase() +
            (color.getGreen()<16?"0":"")+Integer.toHexString(color.getGreen()).toUpperCase() +
            (color.getBlue()<16?"0":"")+Integer.toHexString(color.getBlue()).toUpperCase();
    }

    /**
     * Converts String value (#000000 - #FFFFFF) to Color
     */
    public static Color convertValueToColor(String value) {
        return Color.decode(value);
    }

    /** Expand all image directories in given files
     */
    public static String[] expandAllDirectories(String files[]) {
        ArrayList list = new ArrayList(files.length);
        for (int i=0; i<files.length; i++) {
            String subFiles[] = Utils.expandDirectory(files[i]);
            if (subFiles!=null)
                list.addAll(Arrays.asList(subFiles));
        }
        String res[] = new String[list.size()];
        return (String[])list.toArray(res);
    }
            

    /** Expand all JPG image file names from the given directory. If it is not
     * a directory, but still has image extension, then return that in a one
     * element array, else return null.
     */
    public static String[] expandDirectory(String dir) {
        return expandDirectory(dir, false);
    }
    public static String[] expandDirectory(String dir, boolean includeDirs) {
        File info = new File(dir);
        if (info.isDirectory()) {
            final boolean dirsToo = includeDirs;
            String subFiles[] = info.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return isImageName(name)
                            || (dirsToo && new File(dir, name).isDirectory());
                    }
                });
            for (int i=0; i<subFiles.length; i++)
                subFiles[i] = dir+File.separator+subFiles[i];
            return subFiles;
        }
        else {
            if (isImageName(dir)) {
                String res[] = new String[1];
                res[0] = dir;
                return res;
            }
            else
                return null;
        }
    }

    /** Return true if the given name is a supported image name (has proper
     * extension).
     */
    public static boolean isImageName(String name) {
        name = name.toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png");
    }

    /** Return true if the given name is a supported image name (has proper
     * extension).
     */
    public static boolean isVideoName(String name) {
        name = name.toLowerCase();
        return name.endsWith(".avi");
    }


    /**Return fileName without extension and path.
     */
    public static String getFileName(String name) {
        
        if (name == null)
            return null;

        if (name.length() == 0)
            return name;
        
        int index = name.lastIndexOf(".");

        if (index > 0)
            name = name.substring(0, index);

        return name;
    }

    /** Get file path without given extension, or as is if ext not found
     */
    public static String getFileBase(String name, String ext) {
        int index = name.toLowerCase().lastIndexOf(ext.toLowerCase());
        if (index > 0)
            return name.substring(0,index);
        else
            return name;
        
    }
        
    /** Get file extension without dot or def if no extension in file.
     */
    public static String getFileExt(String name, String def) {
        int index = name.lastIndexOf(".");
        if (index > 0)
            return name.substring(index+1);
        else
            return def;
        
    }
    
    /** Return the dimensions for the image when scaled to fit to the given
     * limits. If the image is smaller than limits, it is not scaled.
     */
    public static Dimension getScaledSize(Dimension limits, int w, int h) {
        double wr = (double)w / limits.width;
        double hr = (double)h / limits.height;
        double scale = Math.max(wr,hr);
        scale = Math.max(scale, 1);
        return new Dimension((int)(w/scale), (int)(h/scale));
    }

    /** Return the dimensions for the image when scaled to fit to the given
     * limits, even if you'd had to scale upwards.
     */
    public static Dimension getAnyScaledSize(Dimension limits, int w, int h) {
        double wr = (double)w / limits.width;
        double hr = (double)h / limits.height;
        double scale = Math.max(wr,hr);
        return new Dimension((int)(w/scale), (int)(h/scale));
    }


    /** 
     * Return the dimensions for the image when scaled to fit to the given
     * limits, even if you'd had to scale upwards. If smaller dimension is >1000 
     * and the resulting scaled dimension would be less than "min", then limits 
     * are increased so that resulting dimension is at least "min" in shorter 
     * direction.
     */
    public static Dimension getScaledSize(Dimension limits, int w, int h, int min) {
        double wr = (double)w / limits.width;
        double hr = (double)h / limits.height;
        double scale = Math.max(wr,hr);
        Dimension res = new Dimension ((int)(w/scale), (int)(h/scale));
        
        int minDim = Math.min(w,h);
        if (minDim>1000) {
            int minScaled = Math.min(res.width, res.height);
            if (minScaled < min) {
                scale = (double)min / minScaled;
                res.width = (int)(res.width * scale);
                res.height = (int)(res.height * scale);                
            }
        }
        return res;
    }


    public static String getXmlFile(Frame parent) {

        FileDialog dialog = new FileDialog(parent, "Select PhotoAlbum file ...", FileDialog.LOAD);
  //      dialog.setDirectory(System.getProperty("user.dir"));
        dialog.setFile("*.xml");
        dialog.show();
        
        if (dialog.getFile() !=  null) {
            System.out.println("Selected file: " + dialog.getDirectory() + dialog.getFile());
        }

        String result = null;

        if (dialog.getFile() != null && dialog.getFile().endsWith(".xml"))
            result =  dialog.getDirectory() + dialog.getFile();
        else if (dialog.getFile() != null)
            JOptionPane.showMessageDialog(parent,
                                          "This is not a PhotoAlbum file.\n " + 
                                          "PhotoAlbum files end with .xml", 
                                          "Wrong file type",
                                          JOptionPane.ERROR_MESSAGE);

        dialog.dispose();

        return result;
    }

    public static String getPictureDirectory(Frame parent) {

        FileDialog dialog = new FileDialog(parent, "Select picture dicretory ...", FileDialog.LOAD);
     //   dialog.setDirectory(System.getProperty("user.dir"));
        //dialog.setFile("*.jpg, *.gif");
        dialog.show();
        
        if (dialog.getFile() !=  null) {
            System.out.println("Selected file: " + dialog.getDirectory() + dialog.getFile());
        }

        dialog.dispose();

        if (dialog.getDirectory() == null)
            return null;

        if (dialog.getFile() == null)
            return null;

        return dialog.getDirectory() + dialog.getFile();
    }

    public static String getDirectory(Component parent) {
        return getDirectory(parent, currentFileDir);
    }
    public static String getDirectory(Component parent, File dir) {
        JFileChooser chooser = null;
        if (dir==null)
            chooser = new JFileChooser();
        else
            chooser = new JFileChooser(dir);
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        DirFilter filter = new DirFilter();
        chooser.addChoosableFileFilter(filter);

        if (chooser.showDialog(parent, "Select a directory") != JFileChooser.APPROVE_OPTION)
            return null;
        else {
            currentFileDir = chooser.getCurrentDirectory();
            return chooser.getSelectedFile().getAbsolutePath();
        }
    }

    /**
     * Only directories allowed
     */
    static class DirFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File pathname) {
            if (pathname == null)
                return false;
            if (pathname.isDirectory())
                return true;
            return false;
        }

        public String getDescription() {
            return "Directory";
        }
    }

    static File currentFileDir = null;
    public static String[] getFiles(Component parent) {
        JFileChooser chooser = null;
        if (currentFileDir==null)
            chooser = new JFileChooser();
        else
            chooser = new JFileChooser(currentFileDir);
        
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        PicFilter filter = new PicFilter();
        chooser.addChoosableFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);

        if (chooser.showDialog(parent, "Select files") != JFileChooser.APPROVE_OPTION)
            return null;
        else {
             String[] strings = null;

             File[] files = chooser.getSelectedFiles();
             if (files != null && files.length > 0) {
                 strings = new String[files.length];

                 for (int i = 0; i < files.length; i++) {
                     File file = files[i];
                     strings[i] = file.getAbsolutePath();
                 }
             }
             chooser.setVisible(false);
             currentFileDir = chooser.getCurrentDirectory();
             return strings;
        }
    }

    /**
     */
    static class PicFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File pathname) {
            if (pathname == null)
                return false;
            if (pathname.isDirectory())
                return true;
            if (Utils.isImageName(pathname.getAbsolutePath()))
                return true;
            return false;
        }

        public String getDescription() {
            return ".jpg, .gif, .png";
        }
    }


    public static boolean generateDirectory(Component parent, String path) {
        File file = new File(path);

        if (!file.exists()) {
            int n = JOptionPane.showConfirmDialog(
                parent,
                "Directory " + path + "  does not exists.\nWould you like to create it ?",
                "Directory does not exists",
                JOptionPane.YES_NO_OPTION);

            if (n == JOptionPane.OK_OPTION) {
                return file.mkdir();
            }
        }
        return false;
    }

    /**
     * Returns the picture file name (no path)
     */
    public static String getImageName(String path) {
        if (path == null)
            return "";
        else {
            File file = new File(path);
            return file.getName();
        }
    }

    public static void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                                  message,
                                  "Error",
                                  JOptionPane.ERROR_MESSAGE);
    }

    public static String generateOriginalLink(String originalFile, String path) {

        if (path == null || originalFile == null)
            return null;

        originalFile = originalFile.toLowerCase();
        path = path.toLowerCase();

        StringBuffer link = new StringBuffer();

        int lastIndex = 0;

        while(true) {

            int index = path.lastIndexOf(File.separator);

            if (index <= 0)
                break;

            lastIndex = index;

            if (originalFile.startsWith(path.substring(0, index))) {
                break;
            }
            else
                link.append("../");

            path = path.substring(0, index-1);
        }

        link.append(originalFile.substring(lastIndex +1));

        if (link.indexOf(":") >= 0)
            return null;

        return link.toString();
    }

    /**
     * Return a relative path to original as seen from given directory.
     * It may be absolute if they are on different drives on Windows.
     * All file separators will be '/' characters to make the path usable in HTML.
     * e.g. \photos\foo\bar\photo.jpg and \photos\foo\xyz\ => ../bar/photo.jpg
     */
    public static String getRelativePath(File original, File directory) {
        String result = null;
        try {
            String o = original.getCanonicalPath();
            String dir = directory.getCanonicalPath() + File.separator;
            
            // Find common prefix
            int lastCommon = 0;
            int index = dir.indexOf(File.separator);
            while (index>=0) {
                if (!dir.regionMatches(true, lastCommon, o, lastCommon, index-lastCommon))
                    break; // No more matching prefix
                lastCommon = index;
                index = dir.indexOf(File.separator, index+1);
            }

            if (lastCommon==0) {
                return o; // No common prefix
            }

            StringBuffer resultbuf = new StringBuffer(o.length());
            if (index>0) {
                // Did not run out of directory path, build relative path
                // while it lasts.
                while (index>=0) {
                    resultbuf.append("../");
                    index=dir.indexOf(File.separator, index+1);
                }
            }
            resultbuf.append(o.substring(lastCommon+1));
            result = resultbuf.toString();
        }
        catch (Exception e) {
            System.out.println("getRelativePath:"+e);
            result = original.getAbsolutePath();
        }
        return result.replace('\\', '/');
    }
    
    public static Color showColorDialog(Component comp, String title, Color defaultColor) {
        Color newColor = JColorChooser.showDialog(
                        comp,
                        title,
                        defaultColor);

        return newColor;
    }

    /** Copy the contents of the given inputstream to given targetfile.
     * The inputstream in closed after copy.
     */
    public static boolean copyStreamToFile(InputStream in, File target) {
        try {
            OutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[16384];
            int c;
            
            while ((c = in.read(buf)) != -1)
                out.write(buf, 0, c);
            
            in.close();
            return true;
        }
        catch (Exception e) {
            System.out.println("copyStreamToFile:"+e);
            return false;
        }
    }

    /** Return the name of the method which called the method which called
     * this method. This is useful for debugging.
     */
    public static String getCallingMethod() {
        Throwable t = new Throwable();
        StackTraceElement elements[] = t.getStackTrace();
        if (elements.length<3)
            return "<outside JVM>";
        else
            return elements[2].getClassName()
                +"."+elements[2].getMethodName()
                +":"+elements[2].getLineNumber();
    }

    /** Draw given text to given Graphics2D view starting at x,y and wrapping
     * lines at width pixels. If the y coordinate is negative, take it as
     * maximum y position. Return new y.
     */
    public static float drawWrappedText(Graphics2D g2,
                                        float x, float y, float width,
                                        String text) {

        if (text==null || text.length()==0)
            return y;
        
        FontRenderContext frc = g2.getFontRenderContext();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   // Anti-alias!
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2.0f));                   // 2-pixel lines

        AttributedString astr = new AttributedString(text);
        astr.addAttribute(TextAttribute.FONT, g2.getFont(), 0, text.length());

        if (y<0) {
            // Calculate real Y by laying out the text without output
            y = -y;
            AttributedCharacterIterator chars = astr.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(chars, frc);
            while (measurer.getPosition() < chars.getEndIndex()) {
                TextLayout layout = measurer.nextLayout(width);
                y -= layout.getAscent();
                float dx = layout.isLeftToRight() ? 0 : (width - layout.getAdvance());
                float sw = (float)layout.getBounds().getWidth();
                float sh = (float)layout.getBounds().getHeight();
                y -= layout.getDescent() + layout.getLeading();
            }
        }
        
        AttributedCharacterIterator chars = astr.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(chars, frc);
        while (measurer.getPosition() < chars.getEndIndex()) {
            TextLayout layout = measurer.nextLayout(width);
            
            y += layout.getAscent();
            float dx = layout.isLeftToRight() ? 0 : (width - layout.getAdvance());
            
            float sw = (float)layout.getBounds().getWidth();
            float sh = (float)layout.getBounds().getHeight();
            Shape sha = layout.getOutline(AffineTransform.getTranslateInstance(x,y));

            Color oldColor = g2.getColor();
            g2.setColor(g2.getBackground());
            g2.draw(sha);
            
            g2.setColor(oldColor);
            g2.fill(sha);
            
            y += layout.getDescent() + layout.getLeading();
        }
        return y;
    }
    
    public static void main(String args[]) {
        System.out.println(getRelativePath(new File(args[0]), new File(args[1])));
    }
    
}
