/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoPanel.java,v 1.4 2004/06/20 19:51:51 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * Display single JPG photo. Scales the photo to the panel size using
 * proportional scaling if fullView is true, otherwise scales the image at
 * most to the thumbNail size.
 */
public class JPhotoPanel extends JPanel implements ListCellRenderer {

    public final static int NO_DROP = 0;
    public final static int DROP_BEFORE = 1;
    public final static int DROP_AFTER  = 2;
    
    protected JPhoto photo;
    protected int border = 0;
    protected boolean hasFocus = false;
    protected boolean selected = false;
    protected boolean fullView = false;
    protected boolean showtext = false;
    protected int showDrop = NO_DROP;
    protected BufferedImage fullImage = null;
    protected Font font = null;
    protected String watermark = "";
    protected JPhotoTransform transformer;
    
    public JPhotoPanel() {
        photo = null;
        font = new Font("SansSerif", Font.BOLD, 12);
        setBackground(Color.black);
        repaint(0, 0,0, getWidth(),getHeight());
    }
    
    public JPhotoPanel(boolean fullView) {
        this();
        this.fullView = fullView;
    }
    
    public JPhotoPanel(String photoName) {
        this();
        setPhoto(new JPhoto(new File(photoName)));
    }

    public void setPhoto(JPhoto photo) {
        if (photo!=this.photo)
            fullImage = null;
        this.photo = photo;
        repaint(0, 0,0, getWidth(), getHeight());
    }

    public JPhoto getPhoto() {
        return photo;
    }

    public void setTransform(JPhotoTransform transform) {
        transformer = transform;
    }
    
    public void setBorder(int w) {
        border = w;
    }
    
    public void setSelected(boolean val) {
        selected = val;
    }
    
    public void setHasFocus(boolean val) {
        hasFocus = val;
    }

    public void setFullView(boolean val) {
        fullView = val;
    }
    
    public void setShowtext(boolean val) {
        showtext = val;
    }

    public void setWatermark(String mark) {
        watermark = mark;
    }
    
    public void setFont(Font font) {
        this.font = font;
    }
    
    public void paint(Graphics g) {
        int cellWidth = getWidth();
        int cellHeight = getHeight();

        Graphics2D g2 = (Graphics2D)g;
        g2.setBackground(getBackground());
        
        if (fullView) {
            g.setColor(getBackground());
            g.fillRect(0, 0, cellWidth, cellHeight);
        }
        else if (hasFocus || selected) {
            g.setColor(getBackground());
            g.fillRect(0, 0, cellWidth, cellHeight);
            g.setColor(Color.blue);
            if (showDrop!=NO_DROP) {
                if (showDrop==DROP_BEFORE)
                    g.fillRect(1, 1, 8, cellHeight-1);
                else
                    g.fillRect(cellWidth-8, 1, cellWidth-1, cellHeight-1);
            }
            else {
                if (selected)
                    g.fillRect(0, 0, cellWidth-1, cellHeight-1);
                
                if (hasFocus) {
                    g.setColor(Color.gray);
                    g.drawRect(0, 0, cellWidth-1, cellHeight-1);
                }
            }
        }
        
        if (photo==null)
            return;
        
        Dimension limits = new Dimension(cellWidth-2*border, cellHeight-2*border);

        if (fullView) {
            if (fullImage==null)
                fullImage = photo.getFullImage();
            if (fullImage!=null && fullImage.getHeight(this)>0) {
                int w = fullImage.getWidth(this);
                int h = fullImage.getHeight(this);
                Dimension dim = Utils.getScaledSize(limits,w,h);
                // Dimension dim = Utils.getAnyScaledSize(limits,w,h);
                int x = border+(cellWidth-dim.width)/2;
                int y = border+(cellHeight-dim.height)/2;
                BufferedImage bi = new BufferedImage(getWidth(), getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D imgGraphics = bi.createGraphics();

                imgGraphics.drawImage(fullImage, x,y, dim.width,dim.height, this);
                
                if (transformer!=null)
                    bi = transformer.paintTransform(photo, bi, x, y);
                
                imgGraphics.setColor(getForeground());
                imgGraphics.setFont(photo.getWatermarkFont());
                Utils.drawWrappedText(imgGraphics, x, -(y+dim.height),
                                      dim.width, watermark);
                
                g2.drawImage(bi, null, 0,0);
            }
            else {
                g.setColor(getForeground());
                Utils.drawWrappedText(g2, 1, 1,
                                      getWidth(), photo.getStatus());
            }
            
            if (photo.getText()!=null
                && (showtext || photo.getOriginalName()==null)) {
                g.setFont(font);
                g.setColor(getForeground());
                Utils.drawWrappedText(g2, 1, -(getHeight()-1),
                                      getWidth(), photo.getText());
                //g.drawString(photo.getText(), 10,20);
            }

        }
        else {
            BufferedImage thumbImage = photo.getThumbImageAsync();
            if (thumbImage!=null) {
                int w = thumbImage.getWidth();
                int h = thumbImage.getHeight();

                Dimension dim = Utils.getScaledSize(limits,w,h);
                int x = (cellWidth-dim.width)/2;
                int y = (cellHeight-dim.height)/2;

                g.drawImage(thumbImage, x, y, dim.width, dim.height, this);
                if (photo.getAlbumLink()!=null) {
                    g.setColor(Color.blue);
                    g.drawRect(x, y, w-1, h-1);
                    g.drawRect(x+1, y+1, w-3, h-3);
                    g.setColor(getForeground());
                }
            }
            else {
                // Still loading, or no readable photo available, show status
                g.setFont(font);
                g.setColor(getForeground());
                Utils.drawWrappedText(g2, 1, 20,getWidth(), photo.getStatus());
            }
            
            g.setFont(font);
            g.setColor(getForeground());

            if (photo.getAlbumLink()!=null) {
                String name = "[Link to "+photo.getAlbumLink()+"]";
                Utils.drawWrappedText(g2, 1, 2, getWidth()-2, name);
            }
            
            if (selected)
                g.setColor(Color.white); // Contrast with blue selection
            
            String text = photo.getText();
            if (text!=null && !text.equals("")) {
                if (thumbImage==null) {
                    Utils.drawWrappedText(g2, 1, -getHeight(), getWidth()-2, text);
                }
                else {
                    FontMetrics fm = g.getFontMetrics();
                    int margin = cellWidth - fm.stringWidth("...") - 3;
                    int x = 0;
                    int i = 0;
                    for ( ; i<text.length(); i++) {
                        x += fm.charWidth(text.charAt(i));
                        if (x > margin)
                            break;
                    }
                    if (i<text.length())
                        text = text.substring(0,i)+"...";
                    g.drawString(text, 3,cellHeight-3);
                }                
            }
            else
                g.drawString(photo.getImageName(), 3,cellHeight-3);
        }
    }

    // ListCellRenderer interface method
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        JPhotoList photolist = (JPhotoList)list;
        int dragbegin = photolist.getDragBegin();
        if (dragbegin>=0) {
            if (dragbegin>index)
                showDrop = DROP_BEFORE;
            else
                showDrop = DROP_AFTER;
        }
        else
            showDrop = NO_DROP;
                
        setSelected(isSelected);
        setHasFocus(cellHasFocus);
        setPhoto((JPhoto)value);
        return this;
    }

    public static void main(String args[]) {

        JFrame frames[] = new JFrame[args.length];
        for (int i=0; i<args.length; i++) {
            frames[i] = new JFrame(args[i]);

            Container picPane = frames[i].getContentPane();
            JPhotoPanel selected = new JPhotoPanel(args[i]);
            selected.setFullView(true);
            picPane.add(selected);
           
            // frames[i].setUndecorated(true);
            frames[i].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frames[i].setSize(700, 700);
            frames[i].setLocation(20*i, 20*i);
            frames[i].show();
        }
    }
    
}

