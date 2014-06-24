/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoExif.java,v 1.4 2005/08/30 17:59:39 jkarjala Exp $
 */
package fi.iki.jka;

import java.io.Serializable;

import com.drew.lang.Rational;
import com.drew.metadata.exif.ExifDirectory;

public class JPhotoExif implements Serializable {
    public int width = -1;
    public int height = -1;
    public String aperture = "";
    public String exposureTime = "";
    public String exposureBias = "";
    public String iso = "";
    public String date = "";

    public JPhotoExif() {
        // For unmarshall only
    }
    
    public JPhotoExif(ExifDirectory dir) {
        if (dir==null)
            return;
        
        try {
            if (dir.containsTag(ExifDirectory.TAG_EXIF_IMAGE_WIDTH))
                width = dir.getInt(ExifDirectory.TAG_EXIF_IMAGE_WIDTH);
            
            if (dir.containsTag(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT))
                height = dir.getInt(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT);
            
            aperture = dir.getDescription(ExifDirectory.TAG_APERTURE);
            if (aperture==null)
                aperture = getRational(dir, ExifDirectory.TAG_FNUMBER);
            if (aperture==null)
                aperture = "";
            if (aperture.length()>0 && aperture.charAt(0)!='F')
                aperture = "F"+aperture;
                
            
            exposureTime = getRational(dir, ExifDirectory.TAG_EXPOSURE_TIME);
            if (exposureTime==null)
                exposureTime = "";
            if (exposureTime.length()>0 && exposureTime.indexOf('s')<0)
                exposureTime += "s";

            exposureBias = getRational(dir, ExifDirectory.TAG_EXPOSURE_BIAS);
            if (exposureBias==null || exposureBias.equals("0"))
                exposureBias = "";
            
            date = dir.getDescription(ExifDirectory.TAG_DATETIME);
            if (date==null) {
            	date = dir.getDescription(ExifDirectory.TAG_DATETIME_DIGITIZED);
            	if (date==null)
            		date = "";
            }
            
            iso = dir.getDescription(ExifDirectory.TAG_ISO_EQUIVALENT);
            if (iso!=null && iso.charAt(0)!='I')
                iso = "ISO"+iso;
            else
                iso = "";
            
        } catch (Exception e) {
            System.out.println("Cannot set Exif:"+e);
        }
    }

    protected String getRational(ExifDirectory dir, int id) throws Exception {
        Object o = dir.getObject(id);
        String str = dir.getDescription(id);
        if (o instanceof Rational) {
            Rational r = (Rational)o;
            if (!str.startsWith("1/"))
                return fixed_format(r.doubleValue(), 5);
            else // Default is good, if it starts with 1/
                return str;
        }
        else
            return str;
    }
    
    private String fixed_format(double d, int precision)
    {  boolean removeTrailing = true;

       if (precision == 0) 
          return (long)(d + 0.5) + (removeTrailing ? "" : ".");

       long whole = (long)d;
       double fr = d - whole; // fractional part

       double factor = 1;
       String leading_zeroes = "";
       for (int i = 1; i <= precision && factor <= 0x7FFFFFFFFFFFFFFFL; i++) 
       {  factor *= 10; 
          leading_zeroes = leading_zeroes + "0"; 
       }
       long l = (long) (factor * fr + 0.5);
       if (l >= factor) { l = 0; whole++; } // CSH 10-25-97
       
       String z = leading_zeroes + l;
       z = "." + z.substring(z.length() - precision, z.length());

       if (removeTrailing)
       {  int t = z.length() - 1;
          while (t >= 0 && z.charAt(t) == '0') t--;
          if (t >= 0 && z.charAt(t) == '.') t--;
          z = z.substring(0, t + 1);
       }

       return whole + z;
    }

    public String toString() {
        if (aperture.length()==0 || exposureTime.length()==0)
            return "No Exif";
        return width+" x "+height
            + ", "+date  
            + ", "+aperture
            + ", "+exposureTime
            + (exposureBias.length()>0 ? ", "+exposureBias : "" )
            + (iso.length()>0 ? ", "+iso : "");
    }
}
