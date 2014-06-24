/*
 * JPhotoSlideShowMaker.java
 * This file is part of JPhotoAlbum.
 */

package fi.iki.jka;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/** 
 * Creates a self-executable slideshow from the given pictures and comments.
 * @created on 1. april 2005, 2:37
 * @author Zbynek Muzik 
 */

public class JPhotoSlideShowMaker extends Thread {

    String targArchName = null;     // name of target jar file
    String [] picSet;               // array of names of original picture files
    String [] commSet;              // array of comments of pictures
    JarOutputStream out;            // stream to the target jar file
    ArrayList photoList;            // arraylist of JPhotos to be imported
    int nPics = 0;                  // number of pictures in the collection
    int progress = 0;               // aprox. percentage of export progress
    int resolution;             // resolution of the slides
    final String [] prFiles = {"JPhotoSlideShowFrame$1.class", 
        "JPhotoSlideShowFrame$ShowAction.class",
        "JPhotoSlideShowFrame.class", 
        "JPhotoSlideShow.class"};   // list of class files to be exported

    /**
     * @param targetFile .jar file to be created
     * @param pl ArrayList of JPhoto objects to be imported into the jar file
     * @param resol resolution of the pictures [1..3] 1..800x600; 2..1024x768; 3..1280x1024
     */    
    public JPhotoSlideShowMaker(String targetFile, ArrayList pl, int resol) {
        targArchName = targetFile;
        photoList = pl;
        loadPicsData();
        resolution = resol;
    }
    
    /**
     * creates the jar file, corresponding to the instance
     */
    public void run() {
        FileOutputStream f = null;
        JarOutputStream out = null;
        JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT_SLIDESHOW_INDEX, "Preparing export...", 0);
        try {        
            f = new FileOutputStream(targArchName);
            out = new JarOutputStream(new BufferedOutputStream(f),getManifest());
            // writing the pictures
            for(int i = 0; i < nPics; i++) {
                progress = Math.round(i*100 / (float)(nPics));
                String src = picSet[i];
                String dest = getJarPath(makePicName(i)+getFileExtension(picSet[i]));
                JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT_SLIDESHOW_INDEX, src, progress);
                JPhoto jp = new JPhoto(new File(src));
                BufferedInputStream in = new BufferedInputStream(jp.getSlideStream(resolution));
                JarEntry entry = new JarEntry(dest);
                byte[] file = new byte[in.available()];
                in.read(file, 0, file.length);
                out.putNextEntry(entry);
                out.write(file, 0, file.length);                
            }            
        }     
        catch(Exception e1) {
            e1.printStackTrace();
            System.out.println("Error occured while writing the pictures into the jar archive...");
        }
        try {
            // exporting the class files
            for(int i = 0; i < prFiles.length; i++) {
                String src = prFiles[i];
                String dest = getJarPath(prFiles[i]);
                BufferedInputStream in = 
                        new BufferedInputStream((this.getClass().getResourceAsStream(prFiles[i])));
                JarEntry entry = new JarEntry(dest);
                byte[] file = new byte[in.available()];
                in.read(file, 0, file.length);
                out.putNextEntry(entry);
                out.write(file, 0, file.length);                
            }
        }
        catch(Exception e2) {
            e2.printStackTrace();
            System.out.println("Error occured while writing the class into the jar archive...");
        }
        try {
            // creates the photos.lst file directly in the jar archive...
            JPhotoStatus.showProgress(JPhotoMenu.A_EXPORT_SLIDESHOW_INDEX, "list of pictures" , 100);
            String line = null;
            JarEntry entry = new JarEntry("gallery/photos.lst");
            out.putNextEntry(entry);
            for(int i=0; i < nPics; i++) {
                line = "/"+getJarPath(makePicName(i)+getFileExtension(picSet[i]))+"\n";
                out.write(line.getBytes());
                line = commSet[i]+"\n";
                out.write(line.getBytes());
            }               
            out.close(); // close the jar file
        }
        catch(Exception e3) {
            e3.printStackTrace();
            System.out.println("Error occured while writing the picture list into the jar archive...");
        }
    }

    /**
     * loads the data about the gallery from the photoList
     */
    void loadPicsData() {
        nPics = photoList.size();
        picSet = new String [nPics];
        commSet = new String [nPics];
        for(int i=0; i < nPics; i++) {
            picSet[i] = ((JPhoto)photoList.get(i)).getFullOriginalName();
            commSet[i] = ((JPhoto)photoList.get(i)).getText();                        
        }
    }
    
    /**
     * creates new manifest for the new jar file
     * @return manifest for the new jar file
     */
    Manifest getManifest() {
        String [] manLines = {
            "Manifest-Version: 1.0\n", 
            "X-COMMENT: Main-Class will be added automatically by build\n",
            "Created-By: 1.4.2_07-b05 (Sun Microsystems Inc.)\n", 
            "Ant-Version: Apache Ant 1.6.2\n",
            "Main-Class: fi.iki.jka.JPhotoSlideShowFrame\n"};
        
        Manifest man = new Manifest();
        try {
            for (int i=0; i < manLines.length; i++) {
                ByteArrayInputStream ba = new ByteArrayInputStream(manLines[i].getBytes());
                man.read(ba);
            }
        }
        catch (IOException e3) {
            System.out.println("error while creating the manifest...");
        }
        return man;
    }
    
    /**
     * for a given file returns path in the archive
     * which differs according to the file type
     * @return path of a file in the future jar archive
     * @param name name of the file to be imported
     */
    static String getJarPath(String name){
        String ext = getFileExtension(name);
        if (ext.equalsIgnoreCase(".class")) {
            return "fi/iki/jka/"+getFileName(name); 
        }
        else {
            return "gallery/"+getFileName(name);
        }
    }

    /** 
     * returns file name without path
     * @return file name without path
     * @param completeName complete name of a file including path
     */
    static String getFileName(String completeName){
        String fn = new String();
        StringTokenizer st = new StringTokenizer(completeName,File.pathSeparator);
        while (st.hasMoreTokens())
            fn = st.nextToken();
        return fn;
    }

    /**
     * returns the extension of the file from its file name
     * @return extension of the file
     * @param completeName complete name of the file
     */
    static String getFileExtension(String completeName) {
           String fn = new String();
        StringTokenizer st = new StringTokenizer(completeName,".");
        while (st.hasMoreTokens())
            fn = st.nextToken();
        return "." + fn; 
    }

    /**
     * returns new filename for picture according to its order in the field 
     * @return filename following the pattern ####.extension
     * @param picnumber order nmber of the picture in the list
     */
    static String makePicName(int picnumber) {
        if (picnumber < 10) return "000" + new Integer(picnumber).toString();
        if (picnumber < 100) return "00" + new Integer(picnumber).toString();
        if (picnumber < 1000) return "0" + new Integer(picnumber).toString();
        return new Integer(picnumber).toString();
    }
}