/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoList.java,v 1.4 2004/05/31 15:05:44 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A list of JPhotos.
 */
public class JPhotoList extends JList {

    int thumbWidth = 165;
    int thumbHeight = 165;
    
    JPhotoCollection photos = null;
    int dragbegin = -1;
    Dimension viewBounds;
    boolean fullView = false;
    JPhotoPanel cellRenderer = new JPhotoPanel();
    
    public JPhotoList(JPhotoCollection photos, int width) {
        super(photos);
        this.photos = photos;

        setCellRenderer(cellRenderer);
        setFixedCellWidth(thumbWidth);
        setFixedCellHeight(thumbHeight);
        setForeground(photos.getForegroundColor());
        setBackground(photos.getBackgroundColor());
        setLayoutOrientation(JList.HORIZONTAL_WRAP);

        Dimension dim = new Dimension(width, thumbHeight*getModel().getSize());
        setVisibleBounds(dim);
        /**
        int visibleColumns = width / getFixedCellWidth();
        if (visibleColumns<=0) visibleColumns = 1;
        int rows = getModel().getSize() / visibleColumns + (getModel().getSize()%visibleColumns>0 ? 1 : 0);
        // System.out.println("initial cols="+visibleColumns+" rows="+rows);
        setVisibleRowCount(rows);
        **/
        setSelectedIndex(0);
    }

    public JPhotoCollection getPhotoModel() {
        return photos;
    }

    public void setPhotoModel(JPhotoCollection newPhotos) {
        photos = newPhotos;
        super.setModel(newPhotos);
    }

    public int moveSelection(int delta) {
        int sel = getSelectedIndex();
        int max = photos.getSize()-1;
        sel += delta;
        if (sel<0)
            sel = max;
        if (sel>max)
            sel = 0;
        setSelectedIndex(sel);
        ensureIndexIsVisible(sel);
        return sel;
    }

    public void setForeground(Color fg) {
        if (cellRenderer!=null)
            cellRenderer.setForeground(fg);
        super.setForeground(fg);
    }

    public void setBackground(Color bg) {
        if (cellRenderer!=null)
            cellRenderer.setBackground(bg);
        super.setBackground(bg);
    }

    // JList method override
    public Dimension getPreferredScrollableViewportSize() {
        return viewBounds;
    }
    
    public void setVisibleBounds(Dimension dim) {
        // System.out.println("setVisibleBounds:"+dim);
        if (dim.width==0)
            return; // Ignore initial zero sizes
        
        viewBounds = dim;
        recalculateVisibleRows();
    }

    public void setThumbSize(int w, int h) {
        thumbWidth = w;
        thumbHeight = h;
        recalculateVisibleRows();
    }
    
    public void setThumbBorder(int w) {
        cellRenderer.setBorder(w);
    }
    
    public void recalculateVisibleRows() {
        int total = getModel().getSize();
        int rows = 1;
        if (fullView) {
            setFixedCellWidth(viewBounds.width);
            setFixedCellHeight(viewBounds.height);
            rows = total;
        }
        else if (total>0) {
            setFixedCellWidth(thumbWidth);
            setFixedCellHeight(thumbHeight);

            int visibleColumns = viewBounds.width / getFixedCellWidth();
            if (visibleColumns<=0) visibleColumns = 1;
            int visibleRows = viewBounds.height / getFixedCellHeight();
            if (visibleRows<=0) visibleRows = 1;
            //System.out.println("cols="+visibleColumns+" rows="+visibleRows);

            rows = total / visibleColumns + (total%visibleColumns>0 ? 1 : 0);
            /** If you want to get horizontal scroll for wide windows...
            if (viewBounds.width>viewBounds.height)
                rows = visibleRows;
            */
        }
        if (rows!=getVisibleRowCount()) {
            // System.out.println("New row count="+rows);
            setVisibleRowCount(rows);
        }

        // XXX For some reason this does not work when fullview goes to thumbs...
        ensureIndexIsVisible(getSelectedIndex());
    }

    public void setDragBegin(int val) {
        dragbegin = val;
    }
    public int getDragBegin() {
        return dragbegin;
    }

    public void toggleFullView() {
        fullView = !fullView;
        cellRenderer.setFullView(fullView);
        recalculateVisibleRows();
    }
    public Point getPhotoLocation(JPhoto photo) {
        int index = photos.indexOf(photo);
        if (index<0)
            return null;
        return indexToLocation(index);
    }
    
    public static void main(String args[]) {
        final JFrame frame = new JFrame("Thumbs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        frame.setSize(200, 700);

        final JFrame picFrame = new JFrame("Selected");
        Container picPane = picFrame.getContentPane();
        final JPhotoPanel selected = new JPhotoPanel(true);
        picPane.add(selected);
        // picFrame.setUndecorated(true);
        picFrame.setSize(700, 700);
        picFrame.setLocation(250, 0);

        final JPhotoList list = new JPhotoList(new JPhotoCollection(args), 200);
        list.setTransferHandler(new JPhotoTransferHandler(list));
        list.setDragEnabled(true);
                
        selected.setPhoto((JPhoto)list.getSelectedValue());
        
        final JScrollPane scrollPane = new JScrollPane(list);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting())
                        return;
                    /*                    selected.setPhoto((JPhoto)list.getSelectedValue());
                    if (!picFrame.isVisible()) {
                        picFrame.setTitle(selected.getPhoto().getImageName());
                        picFrame.setVisible(true);
                        list.requestFocus();
                    }
                    */
                }
            });
        
        frame.show();
        // picFrame.show();
    }
    
}

