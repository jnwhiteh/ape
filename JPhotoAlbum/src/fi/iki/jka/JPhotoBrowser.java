/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoBrowser.java,v 1.6 2005/06/05 19:35:17 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** Browser for selecting photos
 */
public class JPhotoBrowser extends JDialog
    implements ListSelectionListener, ActionListener {

    protected JScrollPane scrollPane = null;
    protected JPhotoList list = null;
    protected JPhotoCollection photos = null;
    protected JTextField textInput = null;
    protected File directory = null;
    protected JPhotoPanel magnifyPanel = null;
    protected JFrame magnifyFrame = null;
    
    protected static String result[] = null;
    protected static File defaultDirectory = null;
    protected int thumbWidth = 80;
    protected int thumbBorder = 2;
    
    protected JPhoto magnified = null;
    protected Object selected[] = new Object[0];
    
    public JPhotoBrowser(JFrame parent, String initialDir)  {
        super(parent, true);
        
        setTitle("Select photos to import (use Shift-click or Ctrl-click to select multiple photos)");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new Label("Photo directory:"), BorderLayout.WEST);
        textInput = new JTextField();
        // textInput.addFocusListener(new FocusHandler());
        panel.add(textInput, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(this);
        panel.add(button, BorderLayout.EAST);
        
        setDirectory(initialDir);
        
        list = new JPhotoList(photos, 550);
        list.setThumbSize(thumbWidth ,thumbWidth);
        list.setThumbBorder(thumbBorder);
        list.setSelectedIndex(0);
        list.setDragEnabled(true);
        list.addListSelectionListener(this);
        list.addMouseListener(new MouseListener());
        
        scrollPane = new JScrollPane(list);
        scrollPane.setFocusable(false);
        scrollPane.addComponentListener(new ResizeAdapter());

        magnifyPanel = new JPhotoPanel(false);
        magnifyFrame = new JFrame();
        magnifyFrame.setUndecorated(true);
        magnifyFrame.getContentPane().add(magnifyPanel);      
        magnifyFrame.getContentPane().setFocusable(false);
        magnifyFrame.getContentPane().addFocusListener(null);
        magnifyFrame.setFocusableWindowState(false);
        magnifyFrame.addMouseListener(new MouseListener());             

        JPanel panelB = new JPanel();
        panelB.setLayout(new FlowLayout());
        button = new JButton("OK");
        button.addActionListener(this);
        panelB.add(button);
        button = new JButton("Cancel");
        button.addActionListener(this);
        panelB.add(button);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(panelB, BorderLayout.SOUTH);
        //        getContentPane().setBackground(Color.black);
        
        addWindowListener(new closeAdapter());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        InputMap im = textInput.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = textInput.getActionMap();        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "SelectDirectory");
        am.put("SelectDirectory", new SelectDirectory());

        im = list.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        am = list.getActionMap();        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
        am.put("OK", new SelectOK());


        pack(); // for some reason this must be called before setSize()!?
        
        list.requestFocus(); // This must be after pack but before setVisible!

        defaultDirectory = null;
        
        setSize(700, 550);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2 - 700/2, 
                    screenSize.height/2 - 550/2);
        setVisible(true);
    }

    public void setDirectory(String dir) {
        if (magnifyFrame!=null)
            hideMagnify();
        
        File dirFile = new File(dir);
        dir = dirFile.getAbsolutePath();
        String files[] = Utils.expandDirectory(dir, true);
        if (dirFile.getParent()!=null) {
            int len = 1;
            if (files!=null)
                len = files.length+1;
        
            String filesAndParent[] = new String[len];
            filesAndParent[0] = dirFile.getParent();
            if (len>1)
                System.arraycopy(files, 0, filesAndParent, 1, len-1);
            files = filesAndParent;
        }
        
        if (files==null || files.length==0) {
            JOptionPane.showMessageDialog(this, "No photos in "+dir,
                                          "Browse", JOptionPane.ERROR_MESSAGE);
            if (directory!=null)
                textInput.setText(directory.toString());
        }
        else {
            photos = new JPhotoCollection(files);
            textInput.setText(dir);
            directory = new File(dir);
            if (list!=null) { // null on first call
                list.setPhotoModel(photos);
                list.setSelectedIndex(0);
                list.setVisibleBounds(getSize());
                list.recalculateVisibleRows();
            }
        }
    }
    
    /** ActionListener
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("...")) {
            String dir = Utils.getDirectory(this, directory);
            if (dir!=null)
                setDirectory(dir);
        }
        else if (cmd.equals("OK")) {
            selectedPhotos(list.getSelectedValues());
        }
        else if (cmd.equals("Cancel")) {
            hideMagnify();
            dispose();
        }
        else {
            System.out.println("Not implemented: "+cmd);
        }
    }

    /** Window closing
     */
    class closeAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent evt) {
            hideMagnify();            
        }
    }
    
    /** Tell the list how big a view there is so that it can adjust display rows.
    */
    class ResizeAdapter extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            Dimension dim = scrollPane.getViewport().getSize();
            list.setVisibleBounds(dim);
        }
    }

    /** List selection changed
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        Object newSelected[] = list.getSelectedValues();
        int newIndex = -1;
        for (int i=0; i<newSelected.length && i<selected.length; i++)
            if (newSelected[i] != selected[i]) {
                newIndex = i;
                break;
            }
        if (newIndex<0) {
            if (newSelected.length>0)
                magnified = (JPhoto)newSelected[newSelected.length-1];
        }
        else {
            magnified = (JPhoto)newSelected[newIndex];
        }
        selected = newSelected;
        
        if (magnified==null)
            hideMagnify();
        
        magnify(magnified);
    }

    /** DoubleClick selects
     */
    class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e.getClickCount() == 2) {
                selectedPhotos(list.getSelectedValues());
            }
        };
    };
    
    public void magnify(JPhoto photo) {
        if (!(photo instanceof JPhotoDirectory)) {
            magnifyPanel.setPhoto(photo);
            magnifyPanel.setSelected(true);
            magnifyFrame.setSize(160,160);

            Point selPoint = list.getPhotoLocation(photo);
            if (selPoint!=null) {
                Point basePoint = list.getLocationOnScreen();
                magnifyFrame.setLocation(basePoint.x+selPoint.x-((160-thumbWidth)/2), 
                        basePoint.y+selPoint.y-((160-thumbWidth)/2));
                if (!magnifyFrame.isVisible())
                    magnifyFrame.setVisible(true);
                magnifyFrame.toFront();
            }
        }
        else
            hideMagnify();
    }
    
    public void hideMagnify() {
        magnifyFrame.setVisible(false);        
        magnified = null;
    }
    
    /**
     * When edit focus is lost, save results.
     */
    class FocusHandler extends FocusAdapter {
        public void focusLost(FocusEvent e) {
            setDirectory(textInput.getText());
        }
    }

    /**
     * Select new directory
     */
    class SelectDirectory extends AbstractAction {
        public SelectDirectory() {
        }
        public void actionPerformed(ActionEvent ae) {
            setDirectory(textInput.getText());
        }
    }
    
    class SelectOK extends AbstractAction {
        public SelectOK() {
        }
        public void actionPerformed(ActionEvent ae) {
            selectedPhotos(list.getSelectedValues());
        }
    }

    public boolean selectedPhotos(Object photos[]) {
        if (photos==null)
            return false;

        result = new String[photos.length];
        for (int i=0; i<photos.length; i++) {
            result[i] = ((JPhoto)photos[i]).getFullOriginalName();
            if (new File(result[i]).isDirectory()) {
                setDirectory(result[i]);
                return false;
            }
        }

        defaultDirectory = directory;
        dispose();
        hideMagnify();
        return true;
    }

    static File getDefaultDirectory() {
        return defaultDirectory;
    }
    
    static String[] getFiles(JFrame parent, File directory) {
        JPhotoBrowser frame = new JPhotoBrowser(parent, directory.toString());
        frame.setTitle("Select photos...");
        if (getDefaultDirectory()!=null)
            return result; // OK selection
        else
            return null; // Cancel/Close button
    }
    
    public static void main(String args[]) throws Exception {
        JPhotoBrowser frame = null;
        frame = new JPhotoBrowser(null, args[0]);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Select photos...");
    }
}
