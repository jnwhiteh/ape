/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoMenu.java,v 1.2 2005/06/05 18:21:13 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.event.*;
import javax.swing.*;

public class JPhotoMenu {
    public static String A_NEW = "New";
    public static String A_OPEN = "Open...";
    public static String A_SAVE = "Save";
    public static String A_SAVEAS = "Save As...";
    public static String A_IMPORT_DIR =  "Import Photos from a Directory...";
    public static String A_IMPORT =  "Import Selected Photos...";
    public static String A_FIND_ORIGINALS =  "Find Original Photos...";
    public static String A_EXPORT =  "Export HTML";
    public static String A_EXPORT_1 =  "as Single page";
    public static String A_EXPORT_2 =  "as Framed Thumbnails";
    public static String A_EXPORT_3 =  "as Thumbnail Table";
    public static String A_EXPORT_TEMPLATE =  "Export Template";
    public static String A_EXPORT_TEMPLATE_1 =  "for Single page";
    public static String A_EXPORT_TEMPLATE_2 =  "for Framed Thumbnails";
    public static String A_EXPORT_TEMPLATE_INDEX =  "for Index of All Linked Albums";
    public static String A_EXPORT_SLIDESHOW_INDEX =  "Export Slideshow";
    public static String A_EXPORT_SLIDESHOW_1 =  "800 X 600";
    public static String A_EXPORT_SLIDESHOW_2 =  "1024 X 768";
    public static String A_EXPORT_SLIDESHOW_3 =  "1280 X 1024";
    public static String A_EXPORT_SUBTITLED =  "Export Subtitled Photos";
    public static String A_EXPORT_INDEX =  "Export Index of All Linked Albums";
    public static String A_COPY_ORIGINALS =  "Copy and Watermark Original Photos";
    public static String A_EXIT = "Exit";
    
    public static String A_CUT = "Cut";
    public static String A_COPY = "Copy";
    public static String A_PASTE = "Paste";
    public static String A_DELETE = "Delete";
    public static String A_INSERT = "Insert Text Pane";
    public static String A_INSERT_ALBUM = "Insert Link to Album";
    
    public static String A_TITLE = "Title...";
    public static String A_DESCRIPTION = "Description...";
    public static String A_KEYWORDS = "Keywords...";
    public static String A_BACKGROUND = "Background Color...";
    public static String A_FOREGROUND = "Foreground Color...";
    public static String A_WATERMARK = "Image Watermark...";
    
    public static String A_SAVE_DEFAULTS = "Save as Page Defaults";

    public static String A_COVERPHOTO = "Use Selected Photo as Album Cover";
    
    public static String A_FULLVIEW = "Show Fullscreen";
    public static String A_SHOWEXIF = "Exif information...";
    public static String A_SLIDESHOW = "Start Slideshow";
    
    public static String A_HELP = "Contents...";
    public static String A_ABOUT = "About...";

    protected JMenuBar menuBar = null;
    protected ActionListener listener = null;
    
    public JPhotoMenu() {
    }

    public JPhotoMenu(ActionListener listener) {
        menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem item;
        
        this.listener = listener;
        
        menu = new JMenu(new JPhotoAction(listener, "File", KeyEvent.VK_F));
        menuBar.add(menu);
        add(menu, A_NEW, KeyEvent.VK_N);
        add(menu, A_OPEN, KeyEvent.VK_O,
            KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK) );
        add(menu, A_SAVE, KeyEvent.VK_S,
            KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK) );
        add(menu, A_SAVEAS, KeyEvent.VK_A);
        menu.addSeparator();
        add(menu, A_IMPORT, KeyEvent.VK_I);
        add(menu, A_IMPORT_DIR, KeyEvent.VK_D);
        add(menu, A_FIND_ORIGINALS, KeyEvent.VK_F);
        menu.addSeparator();
        
        JMenu subMenu = new JMenu(new JPhotoAction(listener, A_EXPORT, KeyEvent.VK_H));
        add(subMenu, A_EXPORT_1, KeyEvent.VK_S,
            KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK) );
        add(subMenu, A_EXPORT_2, KeyEvent.VK_F,
            KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK) );
        //   add(subMenu, A_EXPORT_3, KeyEvent.VK_C,
        //    KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK) );
     //   add(subMenu, A_EXPORT_4, KeyEvent.VK_T,
     //       KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK) );
        menu.add(subMenu);
        
        subMenu = new JMenu(new JPhotoAction(listener, A_EXPORT_TEMPLATE, KeyEvent.VK_T));
        add(subMenu, A_EXPORT_TEMPLATE_1, KeyEvent.VK_S);
        add(subMenu, A_EXPORT_TEMPLATE_2, KeyEvent.VK_F);
        add(subMenu, A_EXPORT_TEMPLATE_INDEX, KeyEvent.VK_I);
        // add(subMenu, A_EXPORT_TEMPLATE_4, KeyEvent.VK_T);
        menu.add(subMenu);
        add(menu, A_EXPORT_SUBTITLED, KeyEvent.VK_T,
            KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));
        
        add(menu, A_EXPORT_INDEX, KeyEvent.VK_A,
            KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));

        subMenu = new JMenu(new JPhotoAction(listener, A_EXPORT_SLIDESHOW_INDEX, KeyEvent.VK_L));
        add(subMenu, A_EXPORT_SLIDESHOW_1, KeyEvent.VK_1);
        add(subMenu, A_EXPORT_SLIDESHOW_2, KeyEvent.VK_2);
        add(subMenu, A_EXPORT_SLIDESHOW_3, KeyEvent.VK_3);
        menu.add(subMenu);        

        add(menu, A_COPY_ORIGINALS, KeyEvent.VK_C);
        
        menu.addSeparator();
        add(menu, A_EXIT, KeyEvent.VK_X);

        menu = new JMenu(new JPhotoAction(listener, "Edit", KeyEvent.VK_E));
        menuBar.add(menu);
        // menu.add(new JMenuItem(TransferHandler.getCutAction()));
        add(menu, A_CUT, KeyEvent.VK_T,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK) );
        add(menu, A_COPY, KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) );
        add(menu, A_PASTE, KeyEvent.VK_P,
            KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK) );
        menu.addSeparator();
        
        add(menu, A_DELETE, KeyEvent.VK_DELETE,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) );
        
        menu.addSeparator();
        add(menu, A_INSERT, KeyEvent.VK_I);
        add(menu, A_INSERT_ALBUM, KeyEvent.VK_A);

        menu = new JMenu(new JPhotoAction(listener, "Page", KeyEvent.VK_P));
        menuBar.add(menu);
        add(menu, A_TITLE, KeyEvent.VK_T);
        add(menu, A_DESCRIPTION, KeyEvent.VK_D);
        add(menu, A_KEYWORDS, KeyEvent.VK_K);
        add(menu, A_FOREGROUND, KeyEvent.VK_F);
        add(menu, A_BACKGROUND, KeyEvent.VK_B);
        add(menu, A_WATERMARK, KeyEvent.VK_W);
        menu.addSeparator();
        add(menu, A_SAVE_DEFAULTS, KeyEvent.VK_A);
        menu.addSeparator();
        add(menu, A_COVERPHOTO, KeyEvent.VK_C);

        menu = new JMenu(new JPhotoAction(listener, "View", KeyEvent.VK_V));
        menuBar.add(menu);
        add(menu, A_FULLVIEW, KeyEvent.VK_T,
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0) );
        add(menu, A_SHOWEXIF, KeyEvent.VK_E,
            KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK) );
        add(menu, A_SLIDESHOW, KeyEvent.VK_S);

        
        menu = new JMenu(new JPhotoAction(listener, "Help", KeyEvent.VK_H));
        menuBar.add(menu);
        add(menu, A_HELP, KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0) );
        add(menu, A_ABOUT, KeyEvent.VK_A);
    }

    public void add(JMenu menu, String text, int memonic) {
        add(menu,text,memonic,null);
    }
    
    public void add(JMenu menu, String text, int memonic, KeyStroke accelerator) {
        menu.add(new JMenuItem(new JPhotoAction(listener,
                                                text,
                                                memonic,
                                                accelerator)));
    }
    
    public JMenuBar getMenuBar() {
        return menuBar;
    }
}
