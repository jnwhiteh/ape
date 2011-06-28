/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoShow.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.KeyStroke;

public class JPhotoShow extends JFrame {
    JPhotoCollection photos = null;
    JPhotoPanel panel = null;
    JList parentList = null;
    int selected = 0;
    javax.swing.Timer timer = null;
    static boolean isStandalone = false;
    
    public static final String A_CLOSE = "close";
    public static final String A_NEXT = "next";
    public static final String A_PREV = "prev";
    public static final String A_EXIT = "exit";
    
    public JPhotoShow(JPhotoCollection photos) {
        this(photos, 5000, null);
    }
    
    public JPhotoShow(JPhotoCollection photos, int interval, JList list) {
        this.photos = photos;
        
        panel = new JPhotoPanel();
        panel.setFullView(true);
        Container picPane = getContentPane();
        picPane.add(panel);

        if (list!=null) {
            if (!list.isSelectionEmpty()) {
                selected = list.getSelectedIndex();
            }
            parentList = list;
        }
        panel.setPhoto((JPhoto)photos.getElementAt(selected));
        panel.setForeground(Color.green);
        panel.setBackground(Color.black);
        panel.setShowtext(true);
        panel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        setUndecorated(true);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());

        if (isStandalone)
            addAction(A_EXIT, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        else
            addAction(A_CLOSE, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        

        addAction(A_CLOSE, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        addAction(A_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        addAction(A_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));

        if (interval==0) // Full view closes with enter
            addAction(A_CLOSE, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        // XXX Should register ListDataListener to photos to get async updates

        if (interval>0) {
            timer = new javax.swing.Timer(interval, new ShowAction(A_NEXT));
            timer.start();
        }
    }
    
    void addAction(String code, KeyStroke key) {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();        
        im.put(key, code);
        am.put(code, new ShowAction(code));
    }
    
    /**
     * keyboard actions
     */
    class ShowAction extends AbstractAction {
        String cmd = null;
        public ShowAction(String code) {
            super(code);
            cmd = code;
        }
        public void actionPerformed(ActionEvent ae) {
            System.out.println("ShowAction: cmd="+cmd);
            if (cmd.equals(A_CLOSE)) {
                setVisible(false);
                if (timer!=null)
                    timer.stop();
                if (parentList!=null)
                    parentList.setSelectedValue(photos.getElementAt(selected), true);
                dispose();
            }
            else
            if (cmd.equals(A_NEXT)) {
                moveSelection(1);
                if (timer!=null)
                    timer.restart();
            }
            else
            if (cmd.equals(A_PREV)) {
                moveSelection(-1); 
                if (timer!=null)
                    timer.restart();
            }

            else
            if (cmd.equals(A_EXIT))
                System.exit(0);
        }
    }

    void moveSelection(int delta) {
        int max = photos.getSize()-1;
        selected += delta;
        if (selected<0)
            selected = max;
        if (selected>max)
            selected = 0;
        panel.setPhoto((JPhoto)photos.getElementAt(selected));
    }
    
    public static void main(String args[]) {
        isStandalone = true;
        System.out.println("This is a standalone JShow");

        JPhotoShow show = new JPhotoShow(new JPhotoCollection(args[0]), 5000, null);

        show.setVisible(true);
    }
}
