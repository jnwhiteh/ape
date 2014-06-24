package fi.iki.jka;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Properties;


import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 * 
 * Base class for interactive image transformers
 * 
 * @author Jari Karjala
 * @version $Id: JPhotoTransform.java,v 1.5 2004/07/23 18:14:19 jkarjala Exp $
 */
public class JPhotoTransform {

    public static final String TRANSFORM_CROP = "CROP";
    
    protected JPhotoPanel panel = null;
    protected JFrame parent = null;
    protected JDialog editor = null;
    
    protected Point points[] = new Point[4];
    protected int SIZE = 8;
    protected int current = -1;
    protected Point beginMove = null;
    protected boolean first = true;
    protected boolean dirty = false;
    protected JPhoto currentPhoto = null;
    
    protected int originalWidth = 0;
    protected int originalHeight = 0;
    protected int originalX = 0;
    protected int originalY = 0;
    
    // Crop bounds in screen coords?
    protected Rectangle bounds = new Rectangle(0,0,0,0);
    
    // Crop bounds in image coords?
    protected Rectangle realBounds = new Rectangle(0,0,0,0);
    
    /** 
     * Construct a new transformer.
     */
    public JPhotoTransform() {
        
    }
    
    public void setParentAndPanel(JFrame frame, JPhotoPanel panel) {
        this.parent = frame;
        this.panel = panel;
    }
    
    /** 
     * Start editing the tranform for the JPhoto in the panel.
     * @param panel
     */
    public void startEditing() {
        editor = new JDialog();
        current = -1;
        
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event)
            {  
                if (editor==null || panel.getPhoto()==null)
                    return;
                
                getCurrentParams(panel.getPhoto(), originalX, originalY);

                Point p = event.getPoint();
                for (int i = 0; i < points.length; i++)
                {  
                    double x = points[i].getX() - SIZE / 2;
                    double y = points[i].getY() - SIZE / 2;
                    Rectangle2D r = new Rectangle2D.Double(x, y, SIZE, SIZE);
                    if (r.contains(p)) {  
                        current = i;
                        break;
                    }
                }
                beginMove = null;
                if (current==-1) {
                    if (bounds.contains(p)) {
                        beginMove = event.getPoint();
                        panel.repaint();
                    }             
                }
                else
                    panel.repaint();

                first = true;
            }
            
            public void mouseReleased(MouseEvent event)
            {  
                if (editor==null || panel.getPhoto()==null)
                    return;
                
                setBoundsFromPoints();
                dirty = true;
                saveCurrentParams();
                panel.repaint();
                current = -1;
                beginMove = null;
            }
                });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event)
            {  
                if (editor==null || panel.getPhoto()==null)
                    return;
                
                if (!first)
                    drawXorSelection();
                else
                    first = false;
                
                if (current == -1) {
                    if (beginMove!=null) {
                        int deltaX = event.getPoint().x - beginMove.x;
                        int deltaY = event.getPoint().y - beginMove.y;
                        boolean moveOK = true;
                        for (int i = 0; i < points.length; i++) {  
                            int newX = points[i].x + deltaX;
                            int newY = points[i].y + deltaY;
                            if (limitX(newX)!=newX || limitY(newY)!=newY)
                                moveOK = false;
                        }
                        if (moveOK)
                            for (int i = 0; i < points.length; i++) {  
                                points[i].x += deltaX;
                                points[i].x = limitX(points[i].x);
                                points[i].y += deltaY;
                                points[i].y = limitY(points[i].y);
                            }
                        beginMove = event.getPoint();
                    }
                }
                else {
                    int x = limitX(event.getPoint().x);
                    int y = limitY(event.getPoint().y);
                    points[current].x = x;
                    points[current].y = y;
                    if (current==0 || current==2) {
                        points[(current+1)%4].y = points[current].y;
                        points[(current+3)%4].x = points[current].x;
                    }
                    if (current==1 || current==3) {
                        points[(current+1)%4].x = points[current].x;
                        points[(current+3)%4].y = points[current].y;
                    }
                }
                drawXorSelection();
            }
                });
    }
    
    protected int limitX(int x) {
        if (x<originalX)
            return originalX;
        if (x>originalX + originalWidth-2*originalX)
            return originalX + originalWidth-2*originalX - 1;
        return x;
    }
    protected int limitY(int y) {
        if (y<originalY)
            return originalY;
        if (y>originalY + originalHeight-2*originalY)
            return originalY + originalHeight-2*originalY - 1;
        return y;
    }
    
    /**
     * 
     * Transform the given image which has been extracted from given photo
     * Do the transformation quickly for screen view/editing.
     * @param photo original photo
     * @param image already transformed photo
     * @param x photo coordinate within image
     * @param y photo coordinate within image
     * @return transformed copy of image
     */
    public BufferedImage paintTransform(JPhoto photo, BufferedImage image, int x, int y) {
        saveCurrentParams();
        getCurrentParams(photo, x, y);
        
        Graphics g = image.getGraphics();
        if (current==-1 && beginMove==null && points[0]!=null)
            drawSelection(g);
        return image;
    }

    /**
     * High quality transform the given image which has been extracted from given photo.
     * @param photo original photo
     * @param image already transformed photo
     * @return transformed copy of image
     */
    public BufferedImage transform(JPhoto photo, BufferedImage image) {
        return image;
    }


    /**
     * Draw selection on panel with xor.
     */
    protected void drawXorSelection() {
        Graphics g = panel.getGraphics();
        g.setXORMode(Color.WHITE);
        drawSelection(g);        
    }

    /** 
     * Draw current selection on given graphics
     * @param g
     */
    protected void drawSelection(Graphics g) {
        g.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
        g.drawLine(points[1].x, points[1].y, points[2].x, points[2].y);
        g.drawLine(points[2].x, points[2].y, points[3].x, points[3].y);
        g.drawLine(points[3].x, points[3].y, points[0].x, points[0].y);
        Graphics2D g2 = (Graphics2D)g;
        for (int i = 0; i < points.length; i++) {  
            double x = points[i].getX() - SIZE / 2;
            double y = points[i].getY() - SIZE / 2;
            g2.fill(new Rectangle2D.Double(x, y, SIZE, SIZE));
        }
    }
    
    /**
     * Get the parameters from current photo and set up points and bounds.
     */
    protected void getCurrentParams(JPhoto photo, int x, int y) {
        currentPhoto = photo;
        if (currentPhoto==null) 
            return;
        
        originalX = x;
        originalY = y;
        
        Properties params = currentPhoto.getTransformParams(TRANSFORM_CROP);
        if (params==null) {
            points[0] = new Point(10,10);
            points[1] = new Point(panel.getWidth()-10,10);
            points[2] = new Point(panel.getWidth()-10,panel.getHeight()-10);
            points[3] = new Point(10,panel.getHeight()-10);
            setBoundsFromPoints();
            return;
        }

        double wr = (double)currentPhoto.getOriginalWidth() / panel.getWidth();
        double hr = (double)currentPhoto.getOriginalHeight() / panel.getHeight();
        double scale = Math.max(wr,hr);        

        bounds.x = x+(int)(Integer.parseInt(params.getProperty("x", "0"))/wr);
        bounds.y = y+(int)(Integer.parseInt(params.getProperty("y", "0"))/hr);
        bounds.width = (int)(Integer.parseInt(params.getProperty("w", "0"))/scale);
        bounds.height= (int)(Integer.parseInt(params.getProperty("h", "0"))/scale);
        setPointsFromBounds();
        setRealBounds();
        
        System.out.println("get:"+bounds);
        
        originalHeight = panel.getHeight();
        originalWidth = panel.getWidth();
    }
    
    /**
     * Save the parameters from points and bounds.
     */
    protected void saveCurrentParams() {
        if (currentPhoto==null)
            return;
        
        if (originalWidth==0) {
            originalWidth = panel.getWidth();
            originalHeight = panel.getHeight();
        }
        
        if (!dirty)
            return;
        
        dirty = false;
        
        Properties params = new Properties();
        params.setProperty("x", Integer.toString(realBounds.x));
        params.setProperty("y", Integer.toString(realBounds.y));
        params.setProperty("w", Integer.toString(realBounds.width));
        params.setProperty("h", Integer.toString(realBounds.height));
        currentPhoto.setTransformParams(TRANSFORM_CROP, params);

        System.out.println("save:"+originalX+","+originalY);
        System.out.println("save:"+params);
    }

    /** Set points from bounds */
    protected void setPointsFromBounds() {
        points[0] = new Point(bounds.x,bounds.y);
        points[1] = new Point(bounds.x+bounds.width,bounds.y);
        points[2] = new Point(bounds.x+bounds.width,bounds.y+bounds.height);
        points[3] = new Point(bounds.x,bounds.y+bounds.height);
    }
    
    /** Set bounds from current points
     */
    protected void setBoundsFromPoints() {        
        bounds.x = Math.min(points[0].x, points[1].x);
        bounds.y = Math.min(points[0].y, points[3].y);
        bounds.width = Math.abs(points[0].x - points[1].x);
        bounds.height= Math.abs(points[0].y - points[3].y);
        setRealBounds();
    }

    /** Set real bounds from current bounds
     */
    protected void setRealBounds() {        
        double wr = (double)currentPhoto.getOriginalWidth()/originalWidth;
        double hr = (double)currentPhoto.getOriginalHeight()/originalHeight;
        double scale = Math.max(wr,hr);
        realBounds.x = (int)((bounds.x-originalX)*wr);
        realBounds.y = (int)((bounds.y-originalY)*hr);
        realBounds.width = (int)(bounds.width*scale);
        realBounds.height = (int)(bounds.height*scale);
    }
}

