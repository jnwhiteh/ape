/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoExifDialog.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

class JPhotoExifDialog extends JDialog {
        ExifModel model = null;
    
        public JPhotoExifDialog(Frame parent, java.util.List tagValues, Preferences prefs) {
            super(parent);
            model = new ExifModel(tagValues);
        
            JTable table = new JTable(model);
            table.setTableHeader(null);
    
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setPreferredSize(new Dimension(300,400));
            getContentPane().add(scrollpane);
            setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            getContentPane().setFocusable(false);
            getContentPane().addFocusListener(null);
            setFocusableWindowState(false);
            pack();
            
            if (prefs.getInt(JPhotoFrame.EXIF_X,0)>0) {
                setLocation(prefs.getInt(JPhotoFrame.EXIF_X,500), prefs.getInt(JPhotoFrame.EXIF_Y,50));
                setSize(prefs.getInt(JPhotoFrame.EXIF_W,310), prefs.getInt(JPhotoFrame.EXIF_H,420));
            }
            else
                setLocationRelativeTo(this);
        }
    
        public void changeTagValues(java.util.List tagValues) {
           model.changeTagValues(tagValues);
        }
    }
    
    class ExifModel extends AbstractTableModel {
        java.util.List tagValues = null;
    
        public ExifModel(java.util.List tagValues) {
            this.tagValues = tagValues;
        }
        public int getColumnCount() { 
            return 2;
        }
        public int getRowCount() { 
            return tagValues.size(); 
        }
        public Object getValueAt(int row, int col) { 
            Tag tag = (Tag)tagValues.get(row);
    
            if (col == 0)
                return tag.getTagName();
            else {
                try  {
                    return tag.getDescription();
                }
                catch (MetadataException e) {
                    return "<none>";
                }
            }
        }
    
        public void changeTagValues(java.util.List tagValues) {
            this.tagValues = tagValues;
            fireTableDataChanged();
        }
}

