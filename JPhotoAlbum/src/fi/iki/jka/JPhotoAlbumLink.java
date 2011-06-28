/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoAlbumLink.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */

/**
 * Container for a link to another album.
 * Smells like JPhoto to outside world.
 */

package fi.iki.jka;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class JPhotoAlbumLink extends JPhoto implements Observer {

    protected String albumLink = null;
    protected JPhotoCollection albumPhotos = null;
    protected JPhoto cover = null;
    
    /** Required for castor */
    public JPhotoAlbumLink() {
        this(defaultOwner, null);
    }
    
    public JPhotoAlbumLink(JPhotoCollection owner, String targetAlbum) {
        super(owner);
        if (targetAlbum!=null) {
            setAlbumLink(targetAlbum);
            albumPhotos = new JPhotoCollection(getFullAlbumLink());
            setText(albumPhotos.getTitle()); // set initial text from album
        }
        //System.out.println("JPhotoAlbumLink("+targetAlbum+")");
    }

    /**
     * Override to return the current cover photo for the album.
     */
    public File getOriginalFile() {
        if (albumPhotos==null) {
            albumPhotos = new JPhotoCollection(getFullAlbumLink());
        }
        
        JPhoto newCover = albumPhotos.getCoverPhoto();
        if (newCover!=cover) {
            // Make sure the cover photo notifies us about its load status
            if (cover!=null)
                cover.deleteObserver(this);
            if (newCover!=null) {
                newCover.addObserver(this);
                name = newCover.getImageName();
            }
            cover = newCover;
            clearCaches();
        }

        if (cover!=null) 
            return cover.getOriginalFile();
        else
            return null;
    }

    // Note this link must not have extension, or XSL fails!
    public String getAlbumLink() {
        String link = makeRelative(albumlink);
        if (link!=null)
            link = Utils.getFileBase(link,JPhotoFrame.FILE_EXT);
        return link;
    }

    // Note: this method is only for unmarshaling from file, must not
    // instantiate the album, yet, or owners will get mixed up.
    public void setAlbumLink(String n) {
        albumlink = makeCanonical(n);
        //System.out.println("setAlbumLink:"+albumlink);
    }

    public String getFullAlbumLink() {
        if (albumlink==null)
            return null;
        
        String link = new File(albumlink).getAbsolutePath();
        if (! link.toLowerCase().endsWith(JPhotoFrame.FILE_EXT))
            link += JPhotoFrame.FILE_EXT;

        return link;
    }
    
    // Observer method called from Observable.notifyObservers()
    // This is called from a non-swing thread
    public void update(Observable o, Object arg) {
        // This will forward thumb notify to owner
        setStatus(cover.getStatus());
    }
    
    public String toString() {
        return "link='"+getAlbumLink()+"' w="+getWidth()+" h="+getHeight()
            +" text='"+getText()+"'";
    }

    public void clearCaches() {
        super.clearCaches();
    }

}
