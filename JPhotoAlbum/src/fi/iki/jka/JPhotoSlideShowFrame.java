/**
 * JPhotoSlideShowFrame.java
 * This file is part of JPhotoAlbum. 
 */

package fi.iki.jka;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.*;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Main class of a standalone picture browser intended to be contained in a jar archive.
 * @created on 25. march 2005, 23:26
 * @author Zbynek Muzik
 */

public class JPhotoSlideShowFrame extends JFrame {

    JPhotoSlideShow canvas; // 
   
    static final String A_NEXT = "next";
    static final String A_PREV = "prev";
    static final String A_EXIT = "exit";
   
    /**
     * Creates a new instance of the class and runs the slideshow
     */
    public JPhotoSlideShowFrame() {

        canvas = new JPhotoSlideShow("/gallery/photos.lst");
        getContentPane().add(canvas);
        setUndecorated(true);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());

        addAction(A_EXIT, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        addAction(A_EXIT, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

        addWindowListener(new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                System.exit(0);
            }
        });
        
        // window atributes
        setExtendedState(MAXIMIZED_BOTH);
        setLocation(0,0);
        setVisible(true);
    }
    
    /**
     * adds keyboard action
     */
    void addAction(String code, KeyStroke key) {
        InputMap im = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = canvas.getActionMap();        
        im.put(key, code);
        am.put(code, new ShowAction(code));
    }
    
    /**
     * Handles all the keyboard actions
     */
    class ShowAction extends AbstractAction {
    
        String cmd = null;
        
        public ShowAction(String code) {
            super(code);
            cmd = code;
        }
        
        public void actionPerformed(ActionEvent ae) {
            if (cmd.equals(A_NEXT)) {
                canvas.selectNext();
            }
            
            if (cmd.equals(A_PREV)) {
                canvas.selectPrev(); 
            }

            if (cmd.equals(A_EXIT))
                System.exit(0);
        }
    }
    public static void main(String[] args) {
        JPhotoSlideShowFrame sl = new JPhotoSlideShowFrame();
    } 
}