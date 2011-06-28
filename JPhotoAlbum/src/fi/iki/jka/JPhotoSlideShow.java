/**
 * JPhotoSlideShow.java
 * This file is part of JPhotoAlbum.
 */

package fi.iki.jka;

import java.io.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.awt.Color;
import java.lang.Math;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.net.URL;

/**
 * Container for one picture of the slideshow. 
 * Contains methods for drawing on the screen etc.
 * @created on 26. march 2005, 0:25
 * @author Zbynek Muzik
 */

public class JPhotoSlideShow extends JPanel {

    String fileName = null;         //filename of the picture
    String comment = null;          //comment for the picture
    BufferedImage curImg = null;    //image currently shown
    int screenx, screeny;           //dimensions of the screen
    int picx, picy;                 //dimensions of the picture
    int scaledx, scaledy;           //dimensions of the picture scaled to fit the screen
    ArrayList picList;              //ArrayList with all the pictures to be viewed
    int nPics;                      //total number of pictures in the gallery
    int activePic = 0;              //picture currently viewed
    
    /**
     * @param fileName file name of the picture to be shown
     * @param comment comment on the picture to be shown as a subtitle
     */
    public JPhotoSlideShow(String list) {
        super();
        loadPictureList(list);
        curImg = getPic(0);
        comment = getComment(0);
    }
    
    /**
     * gets the BufferedImage of the certain image in the set
     * @return BufferedImage of the certain picture
     * @param index index of the picture in the set
     */
    public BufferedImage getPic(int index) {
        String fileName = (String)picList.get(index * 2);
        URL picURL = this.getClass().getResource(fileName);
        System.out.println(fileName);
        System.out.println(picURL.getPath());
        try {
            return ImageIO.read(picURL);
        }
        catch (Exception picE) {
            System.out.println("Error occured while loading a picture...");
            return null;
        }
    }
   
    /**
     * gets the comment string of the certain picture in the set
     * @return String of the text
     * @param index index of the picture in the set
     */    
    public String getComment(int index) {
        return (String)picList.get((index * 2) + 1);
    }
    
    /**
     * repaints the picture on the screen
     */
    public void paint(Graphics g) {
        reScaleImg();
        g.setColor(Color.BLACK);
        g.fillRect(0,0, screenx, screeny);
        g.drawImage(curImg, (screenx-scaledx)/2, (screeny-scaledy)/2, scaledx, scaledy, this);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        //drawing the comment in white color with black border
        int commentMove = g.getFontMetrics().stringWidth(comment)/2;
        g.drawString(comment,(screenx/2) - commentMove-1,screeny - 5-1);
        g.drawString(comment,(screenx/2) - commentMove+1,screeny - 5+1);
        g.drawString(comment,(screenx/2) - commentMove-1,screeny - 5+1);
        g.drawString(comment,(screenx/2) - commentMove+1,screeny - 5-1);        
        g.setColor(Color.WHITE);
        g.drawString(comment,(screenx/2) - commentMove,screeny - 5);
    }
    
    /**
     * sets the correct dimensions of the picture to fit the screen
     */
    void reScaleImg() {
        screeny = getHeight();
        screenx = getWidth();
        picy = curImg.getHeight(this);
        picx = curImg.getWidth(this);
        if ((screenx < picx) || (screeny < picy)) {
            float picRatio = (float) picx/picy;
            float screenRatio = (float) screenx/screeny;
            if (picRatio<screenRatio) {
                scaledy = screeny;
                scaledx = Math.round(screeny * picRatio);
            }
            else {
                scaledx = screenx;
                scaledy = Math.round(screenx / picRatio);
            }
        }
        else {
            scaledx = picx;
            scaledy = picy;
        }
    }

    /**
     * Loads the list of the picture filenames and comments
     * @param fileName file containing the list with picture filenames and comments
     */    
    void loadPictureList(String fileName) {
        // initialization of variables
        picList = new ArrayList();

        // loads all the file into ArrayList called picList
        try {
            BufferedReader vstup = 
                    new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
            String s;
            while ((s = vstup.readLine()) != null) {
                picList.add(s);
            }
            vstup.close();
        }
        catch (IOException e){
            System.out.println ("I/O Error while reading the picture list...");
        }
        nPics = picList.size()/2; //actual number of pictures in the gallery
    }
   
    /**
     * Jumps to the previous picture in the set
     */
    void selectPrev() {
        if (activePic > 0) {
            activePic--;
        }
        else {
            activePic = (nPics - 1);
        }
        curImg = getPic(activePic);
        comment = getComment(activePic);
        repaint();
    
    }
    
    /**
     * Jumps to the next picture in the set
     */
    void selectNext() {
        if (activePic < (nPics - 1)) {
            activePic++;
        }
        else {
            activePic = 0;
        }
        curImg = getPic(activePic);
        comment = getComment(activePic);
        repaint();
    }
}