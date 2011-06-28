/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoTransferHandler.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class JPhotoTransferHandler extends TransferHandler {

    JPhotoList list = null;

    // Take the list as parameter, this way we can bind the same handler to
    // JPhotoPanels, too!
    public JPhotoTransferHandler(JPhotoList list) {
        super();
        this.list = list;
    }
    
    public int getSourceActions(JComponent c) {
        // System.out.println("getSourceActions:"+c.getClass());
        int res = super.getSourceActions(c);
        res = TransferHandler.MOVE|TransferHandler.COPY;
        // System.out.println("getSourceActions return:"+res);
        return res;
    }

    public Transferable createTransferable(JComponent comp) {
        // System.out.println("createTransferable: "+comp.getClass());
        // JPhotoList list = (JPhotoList)comp;
        JPhotoCollection selected = new JPhotoCollection();
        selected.setPhotos(new ArrayList(Arrays.asList(list.getSelectedValues())));
        Transferable transferable = selected;
        System.out.println("createTransferable result "+transferable);
        return transferable;
    }

    public void exportAsDrag(JComponent comp,
                             InputEvent e,
                             int action) {
        System.out.println("exportAsDrag:"+comp.getClass()+", "+action);
        // JPhotoList list = (JPhotoList)comp;
        list.setDragBegin(list.getSelectedIndex());
        super.exportAsDrag(comp, e, action);
        return;
    }

    protected void exportDone(JComponent source,
                              Transferable data,
                              int action) {
        try {
            // JPhotoList list = (JPhotoList)source;
            list.setDragBegin(-1);
            
            if (data==null) {
                System.out.println("exportDone: No transferable");
                return;
            }
            JPhotoCollection photos
                = (JPhotoCollection)data.getTransferData(JPhotoCollection.PHOTOCOLLECTION_FLAVOR);

            int index = 0;
            if (action==TransferHandler.MOVE) {
                Iterator i = photos.iterator();
                while (i.hasNext()) {
                    JPhoto photo = (JPhoto)i.next();
                    if (list.getPhotoModel().remove(photo))
                        System.out.println("exportDone: removed photo "+photo);
                }
            }            
            // list.repaint(0,0,0,list.getWidth(), list.getHeight());
        } catch (Exception e) {
            System.out.println("exportDone error:"+e); 
        }
        return;
    }
    
    public boolean canImport(JComponent comp, DataFlavor flavor[]) {
        // System.out.println("canImport:"+comp.getClass()+" "+flavor);
        boolean res = true;
        // System.out.println("canImport return:"+res);
        return res;
    }

    public boolean importData(JComponent comp, Transferable t) {
        try {
            JPhoto photo = null;
            //JPhotoList list = (JPhotoList)comp;
            
            int index = list.getSelectedIndex();
            int begin = list.getDragBegin();
            if (index<0)
                index = 0;
            
            System.out.println("importData: index="+index+" begin="+begin);
            
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                index++; // Import from external program always after selection
                // It is a file list from some external source
                java.util.List fileList = (java.util.List)
                    t.getTransferData(DataFlavor.javaFileListFlavor);
                // Alpha sort them since filemanager gives them in random order!
                Collections.sort(fileList);
                Iterator iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    File file = (File)iterator.next();
                    String name = file.getName().toLowerCase();
                    if (Utils.isImageName(name)) {
                        photo = new JPhoto(list.getPhotoModel(), file);
                        list.getPhotoModel().add(index, photo);
                        index++;
                    }
                    else if (name.endsWith(JPhotoFrame.FILE_EXT)) {
                        // System.out.println("import: album "+name);
                        photo = new JPhotoAlbumLink(list.getPhotoModel(),
                                                    file.getAbsolutePath());
                        list.getPhotoModel().add(index, photo);
                        index++;
                    }
                }
            }
            else {
                JPhotoCollection photos
                    = (JPhotoCollection)t.getTransferData(JPhotoCollection.PHOTOCOLLECTION_FLAVOR);
                if (begin>=0 && begin<index)
                    index++;
                Iterator i = photos.iterator();
                while (i.hasNext()) {
                    photo = (JPhoto)i.next();
                    list.getPhotoModel().add(index, photo);
                    index++;
                }
            }
            list.recalculateVisibleRows();            
            return true;
        } catch (Exception e) {
            System.out.println("importData error:"+e); 
            e.printStackTrace();
        }
        return false;
    }

    // Not called in JDK 1.4.x???!!
    public Icon getVisualRepresentation(Transferable t) {
        try {
            JPhoto photo = (JPhoto)t.getTransferData(JPhoto.PHOTO_FLAVOR);
            System.out.println("getVisualRepresentation for "+photo); 
            return new ImageIcon(photo.getThumbImage());
        }
        catch (Exception e) {
            System.out.println("getVisualRepresentation error:"+e); 
            return null;
        }
    }
}

