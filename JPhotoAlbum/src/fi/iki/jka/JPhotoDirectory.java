/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoDirectory.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */

/** Container for a single photo, may not always contain a real photo, but
 * just text element.
 * @see JPhotoAlbumLink.java
 */

package fi.iki.jka;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileCacheImageInputStream;

import com.drew.metadata.exif.ExifDirectory;

public class JPhotoDirectory extends JPhoto {

    public JPhotoDirectory() {
        this(defaultOwner, null);
    }

    public JPhotoDirectory(JPhotoCollection owner) {
        this(owner, null);
    }

    public JPhotoDirectory(File original) {
        this(defaultOwner, original);
    }

    public JPhotoDirectory(JPhotoCollection owner, File original) {
        super(owner, original);
    }

    public BufferedImage getThumbImage() {
        InputStream ins = getClass().getClassLoader().getResourceAsStream("pics/directory.png");
        Iterator readers = ImageIO.getImageReadersBySuffix("png");
        ImageReader imageReader = (ImageReader)readers.next();
        BufferedImage thumb = null;
        try {
            imageReader.setInput(new FileCacheImageInputStream(ins, null));
            thumb = imageReader.read(0);
            ins.close();
        }
        catch (Exception e) {
            System.out.println("getThumbImage:"+e);
        }
        imageReader.dispose();
        return thumb;
    }

    public BufferedImage getCachedThumb() {
        return getThumbImage();
    }
    
    public BufferedImage getThumbImageAsync() {
        return getThumbImage();
    }

    public BufferedImage getFullImage() {
        return null;
    }

    public ExifDirectory getExifDirectory() {
        return null;
    }

    public String toString() {
        return "Directory='"+getImageName()+"'";
    }

}
