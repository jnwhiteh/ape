/*
 * Created on 17.8.2007
  */
package fi.iki.jka;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;


public class MakeWorkQueue {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        if (args.length<2) {
            System.err.println("Usage: MakeWorkQueue queuename jph-file [jph-file ...]");
            System.exit(1);
        }
        String output = "d:/bibble/work/"+args[0]+".work";
        PrintStream out = new PrintStream(new FileOutputStream(output));
        out.println(args[0]);
        out.println("");
        for (int i=1; i<args.length; i++) {
            JPhotoCollection col = new JPhotoCollection(args[i]);
            Iterator iter = col.getPhotos().iterator();        
            while (iter.hasNext()) {
                JPhoto p = (JPhoto)iter.next();
                if (p.getOriginalFile()!=null) {
                    File f = p.getOriginalFile();
                    File raw = new File(f.getParentFile(), "raw/"+Utils.getFileBase(f.getName(), "jpg")+"CR2");
                    out.println(raw.getAbsolutePath());
                }
            }
        }
        out.close();
        System.out.println("Wrote "+output);
    }
}
