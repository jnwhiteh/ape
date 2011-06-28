/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoPageInfo.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */
package fi.iki.jka;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class JPhotoPageInfo implements Serializable {
    public String title = "";
    public String description = "";
    public String keywords = "";
    public String foreground = "#000000";
    public String background = "#FFFFF0";
    public String watermark = "";
    public String coverPhotoName = "";
    // Should be used by transformers only
    public String outputDirectory = "";
    public String albumName = "";

    protected transient static JPhotoPageInfo defPage = null;
    
    public JPhotoPageInfo() {
        // For unmarshall only, init from defs
        if (defPage!=null) {
            title = defPage.title;
            description = defPage.description;
            keywords = defPage.keywords;
            foreground = defPage.foreground;
            background = defPage.background;
            watermark = defPage.watermark;
            coverPhotoName = defPage.coverPhotoName;
        }
    }

    public String marshal() {
        StringWriter w = new StringWriter();
        try {
            Marshaller.marshal(this, w);
            return w.toString();
        } catch (Exception e) {
            System.out.println("marshal:"+e);
            return null;
        }
    }
    
    public static void setDefault(String def) {
        if (def==null) {
            defPage = null;
            return;
        }
        
        StringReader r = new StringReader(def);
        try {
            Unmarshaller m = new Unmarshaller(JPhotoPageInfo.class);
            defPage = (JPhotoPageInfo)m.unmarshal(r);
            System.out.println("JPhotoPageInfo:"+defPage);
        } catch (Exception e) {
            System.out.println("JPhotoPageInfo:"+e);
        }
        // No default for these!
        defPage.coverPhotoName = ""; 
        defPage.albumName = "";
        defPage.outputDirectory = "";
    }
    
    public String toString() {
        return title+";"+keywords+";"+description+";"
            +foreground+";"+background+";"+watermark;
    }
}
