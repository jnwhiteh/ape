/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoFrame.java,v 1.13 2007/01/14 17:54:31 jkarjala Exp $
 */
package fi.iki.jka;

import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;

/** Top-level frame containing a BrowsePanel which shows scaled versions
 * images from the given list of images.
 */
public class JPhotoFrame extends JFrame
    implements ListSelectionListener, ActionListener {

    public static String FILE_EXT = ".jph";
    public static String APP_NAME = "JPhotoAlbum";
    public static String FRAME_X = "frame_x";
    public static String FRAME_Y = "frame_y";
    public static String FRAME_W = "frame_w";
    public static String FRAME_H = "frame_h";
    public static String SPLIT  = "split";
    public static String EXIF_X = "exif_x";
    public static String EXIF_Y = "exif_y";
    public static String EXIF_W = "exif_w";
    public static String EXIF_H = "exif_h";
    public static String HELP_X = "help_x";
    public static String HELP_Y = "help_y";
    public static String HELP_W = "help_w";
    public static String HELP_H = "help_h";
    public static String PREV_FILE = "prev_file";
    public static String PAGEINFO = "pageinfo";
    public static String PHOTO_DIR = "photo_directory";

    protected Preferences prefs = null;
    protected String albumFileName = null;
    protected JPhotoList list = null;
    protected IPhotoCollection photos = null;
    protected JLabel statusLine = null;
    protected JTextField textInput = null;
    protected JPhoto editingPhoto = null;
    protected JFrame fullView = null;
    protected JPhotoPanel fullViewPanel = null;
    protected JScrollPane scrollPane = null;
    protected JSplitPane splitPane = null;
    protected JMenuBar menuBar = null;
    protected JPhotoTransferHandler transferHandler = null;
    protected JPhotoFrame frame = null;
    protected JPhotoExifDialog exifDialog = null;
    protected JFrame helpFrame = null;
    protected File photoDirectory = null;
    
    protected static HashMap allFrames = new HashMap();
    
    protected JPhotoFrame() throws Exception {
        // Do nothing... needed for inheritance !
    }

    public JPhotoFrame(String frameName, IPhotoCollection photos) throws Exception {
        init(frameName, photos);
    }

    public JPhotoFrame(String frameName, String files[]) throws Exception {
        init(frameName, new JPhotoCollection(files));
    }

    /** Real init called from different constructors
     */
    protected void init(String frameName, IPhotoCollection photos) throws Exception {
        prefs = Preferences.userRoot().node("/fi/iki/jka/jphotoframe");        
        JPhotoPageInfo.setDefault(prefs.get(PAGEINFO, null));
        photoDirectory = new File(prefs.get(PHOTO_DIR, System.getProperty("user.dir")));
            
        if (photos==null)
            photos = new JPhotoCollection();
        this.photos = photos;
        
        if (frameName!=null) {
            albumFileName = frameName;
            if (!photos.load(albumFileName))
                JOptionPane.showMessageDialog(null, "Cannot open "+albumFileName,
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
        
        int splitWidth = prefs.getInt(SPLIT, 190);
        // For some reason the list does not get initial viewport resize
        // event, correctly, so we need to give the splitWidth to photolist
        // which can fake it.
        list = new JPhotoList(photos, splitWidth);
        transferHandler = new JPhotoTransferHandler(list);
        list.setTransferHandler(transferHandler);
        list.setDragEnabled(true);
        list.addListSelectionListener(this);
        list.addMouseListener(new MouseListener());

        scrollPane = new JScrollPane(list);
        scrollPane.setFocusable(false);
        scrollPane.addComponentListener(new ResizeAdapter());
        
        statusLine = new JLabel();
        statusLine.setFocusable(false);
        statusLine.setBackground(Color.white);        
        statusLine.setForeground(Color.black);
        JPhotoStatus.setStatusLineLabel(this, statusLine);
        
        //JPanel textPanel = new JPanel();
        //textPanel.setLayout(new BorderLayout());
        //textPanel.add(new Label("Description:"), BorderLayout.WEST);
        textInput = new JTextField();
        textInput.addFocusListener(new FocusHandler());
        //textPanel.add(textInput, BorderLayout.CENTER);

        fullViewPanel = new JPhotoPanel();
        fullViewPanel.setFullView(true);
        fullViewPanel.setTransferHandler(transferHandler);
        fullViewPanel.setBackground(photos.getBackgroundColor());
                                        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   scrollPane, fullViewPanel);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(splitWidth);
        splitPane.setOneTouchExpandable(true);
                           
        menuBar = new JPhotoMenu(this).getMenuBar();
        setJMenuBar(menuBar);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusLine, BorderLayout.SOUTH);
        getContentPane().add(textInput, BorderLayout.NORTH);
        //        getContentPane().setBackground(Color.black);
        list.requestFocus();
        
        addWindowListener(new closeAdapter());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Select first by default
        JPhoto photo = (JPhoto)photos.getElementAt(0);
        if (photo!=null)
            selectPhoto(photo);
        else {
            setTitle();
            statusLine.setText("No album given on command line...");
        }        
        InputMap im = textInput.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = textInput.getActionMap();        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveSelectionUp");
        am.put("moveSelectionUp", new MoveSelection(-1));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveSelectionDn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "moveSelectionDn");
        am.put("moveSelectionDn", new MoveSelection(1));

        pack(); // for some reason this must be called before setSize()!?
        
        setLocation(prefs.getInt(FRAME_X,50)+20*allFrames.size(),
                    prefs.getInt(FRAME_Y,20)+20*allFrames.size());
        setSize(prefs.getInt(FRAME_W,700), prefs.getInt(FRAME_H,550));

        frame = this;
        setFrameIcon();
        allFrames.put(frameName, this);
        setVisible(true);

        Object[] options = {
                "Open Existing Album",
                "Create New, Import Images",
                "Create New Album"};
    
        if (frameName==null && photo==null) {
            int n = JOptionPane.showOptionDialog(frame,
            "How would you like to start?",
            "How to start...",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
            if (n==1) {
                ActionEvent event = new ActionEvent(this, 0, JPhotoMenu.A_IMPORT);
                actionPerformed(event);
            }
            else
            if (n==0) {
                ActionEvent event = new ActionEvent(this, 0, JPhotoMenu.A_OPEN);
                actionPerformed(event);
            }   
        }        
        
        statusLine.setText("Drag and drop photos or use File/Import operations to add photos."
                +" Original photos are never modified.");
        
        /*
        JPhotoTransform transform = new JPhotoTransform();
        fullViewPanel.setTransform(transform);
        transform.setParentAndPanel(this, fullViewPanel);
        transform.startEditing();
        */
    }

    /** ActionListener
     */
    public void actionPerformed(ActionEvent event) {
        saveEdit(); // Just in case...

        String cmd = event.getActionCommand();
        if (cmd.equals(JPhotoMenu.A_NEW) || cmd.equals(JPhotoMenu.A_OPEN)) {
            if (confirmedSave()!=JOptionPane.CANCEL_OPTION) {
                photos = new JPhotoCollection();
                list.setPhotoModel(photos);
                albumFileName = null;
                editingPhoto = null;
                if (cmd.equals(JPhotoMenu.A_OPEN)) {
                    String file = askFileName(FILE_EXT, FileDialog.LOAD);
                    if (file!=null) {
                        try {
                            if (photos.load(file)) {
                                albumFileName = file;
                                list.setSelectedIndex(0);
                                list.recalculateVisibleRows();
                                editingPhoto = null;
                                setFrameIcon();
                            }
                            else
                                JOptionPane.showMessageDialog(this, file+" is not a valid JPhoto file.");
                        } catch (Exception e) {
                            System.out.println("load error:"+e);
                            JOptionPane.showMessageDialog(this, "Cannot open "+file,
                                                          APP_NAME, JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                JPhoto photo = (JPhoto)photos.getElementAt(0);
                selectPhoto(photo);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_SAVE)) {
            askNameAndSave();
        }
        else if (cmd.equals(JPhotoMenu.A_SAVEAS)) {
            String file = askFileName(FILE_EXT, FileDialog.SAVE);
            if (file!=null) {
                try {
                    if (photos.save(file)) {
                        albumFileName = file;
                        setTitle();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Cannot save as "+file,
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else if (cmd.equals(JPhotoMenu.A_IMPORT_DIR)
                 || cmd.equals(JPhotoMenu.A_IMPORT)) {
            
            String files[] = null;
            if (cmd.equals(JPhotoMenu.A_IMPORT_DIR)) {
                String dir = Utils.getDirectory(this, photoDirectory);
                if (dir!=null) {
                    String input[] = { dir+File.separator };
                    files = Utils.expandAllDirectories(input);
                    if (files==null || files.length==0)
                        JOptionPane.showMessageDialog(this, "No photos in "+dir,
                                                      APP_NAME, JOptionPane.ERROR_MESSAGE);
                    photoDirectory = new File(dir);
                }
            }
            else {
                files = JPhotoBrowser.getFiles(this, photoDirectory);
                if (JPhotoBrowser.getDefaultDirectory()!=null)
                    photoDirectory = JPhotoBrowser.getDefaultDirectory();
            }
            
            if (files!=null) {
                int index = list.getSelectedIndex();
                if (index<0)
                    index = 0;
                else
                    index++;
                
                list.getPhotoModel().addAll(index, files);
                list.setSelectedIndex(index-1);
                
                Dimension dim = scrollPane.getViewport().getSize();
                list.setVisibleBounds(dim);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_FIND_ORIGINALS)) {           
            String files[] = null;
            String dir = Utils.getDirectory(this, photoDirectory);
            if (dir!=null) {
                String input[] = { dir+File.separator };
                files = Utils.expandAllDirectories(input);
                if (files==null || files.length==0)
                    JOptionPane.showMessageDialog(this, "No photos in "+dir,
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
                else
                    photoDirectory = new File(dir);
            }
            
            if (files!=null) {
                list.getPhotoModel().findOriginals(files);
            }
        }
        else if (cmd.startsWith("as ")) {

            if (JPhotoStatus.inProgress()) {
                JOptionPane.showMessageDialog(this, "Already exporting, please wait.",
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            else {
                // Save the XML first, ask from the user if neccessary, then generate the html files.
                boolean status = false;

                if (askNameAndSave()) {
                    try {
                        setTitle();
                        if (cmd.equals(JPhotoMenu.A_EXPORT_1))
                            status = photos.exportHtmlJari1(albumFileName);
                        else if (cmd.equals(JPhotoMenu.A_EXPORT_2))
                            status = photos.exportHtmlJari2(albumFileName);
                        // else if (cmd.equals(JPhotoMenu.A_EXPORT_3)) 
                        //    status = photos.exportHtmlTarja(albumFileName);

                        if (status==false)
                            JOptionPane.showMessageDialog(this, "Export of "+albumFileName+ " failed.",
                                                          APP_NAME, JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Cannot save "+albumFileName,
                                                      APP_NAME, JOptionPane.ERROR_MESSAGE);
                    }                
                }
            }
        }
        else if (cmd.startsWith("for ")) {
            String htmlFile = null;
            if (askNameAndSave()) {
                File target = new File(albumFileName).getAbsoluteFile().getParentFile();
                statusLine.setText("Exporting Template to "+target+"...");
                boolean status = false;
                if (cmd.equals(JPhotoMenu.A_EXPORT_TEMPLATE_1))
                    status = photos.exportTemplateJari1(target);
                else if (cmd.equals(JPhotoMenu.A_EXPORT_TEMPLATE_2))
                    status = photos.exportTemplateJari2(target);
                else if (cmd.equals(JPhotoMenu.A_EXPORT_TEMPLATE_INDEX))
                    status = photos.exportTemplateJari3(target);
                /* else if (cmd.equals(JPhotoMenu.A_EXPORT_TEMPLATE_4)) 
                    status = photos.exportTemplateTarja(target);
                */
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export to "+target+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
                else
                    statusLine.setText("Exported template(s) to "+target);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_EXPORT_SUBTITLED)) {
            if (askNameAndSave()) {
                boolean status = photos.exportSubtitledPhotos(albumFileName);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export subtitled to "+albumFileName+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_COPY_ORIGINALS)) {
            if (askNameAndSave()) {
                boolean status = photos.copyOriginals(albumFileName, "originals", true);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Copy originals to "+albumFileName+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_EXPORT_INDEX)) {
            if (askNameAndSave()) {
                boolean status = photos.exportHtmlJari3(albumFileName);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export index failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(this, "Exported index of all linked albums.",
                                                  APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_EXPORT_SLIDESHOW_1)) {
            // exports slideshow with max. resolution 800x600
            if (askNameAndSave()) {
                String targetFile = askFileName(".jar", FileDialog.SAVE);
                if (targetFile==null) {
                    JOptionPane.showMessageDialog(this, "File not set");
                    return;
                }
                boolean status = photos.exportSlideshow(targetFile, 1);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export of slideshow to "+albumFileName+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_EXPORT_SLIDESHOW_2)) {
            // exports slideshow with max. resolution 1024x768
            if (askNameAndSave()) {
                String targetFile = askFileName(".jar", FileDialog.SAVE);
                if (targetFile==null) {
                    JOptionPane.showMessageDialog(this, "File not set");
                    return;
                }
                boolean status = photos.exportSlideshow(targetFile, 2);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export of slideshow to "+albumFileName+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }        
        else if (cmd.equals(JPhotoMenu.A_EXPORT_SLIDESHOW_3)) {
            // exports slideshow with max. resolution 1280x1024
            if (askNameAndSave()) {
                String targetFile = askFileName(".jar", FileDialog.SAVE);
                if (targetFile==null) {
                    JOptionPane.showMessageDialog(this, "File not set");
                    return;
                }
                boolean status = photos.exportSlideshow(targetFile, 3);
                if (status==false)
                    JOptionPane.showMessageDialog(this, "Export of slideshow to "+albumFileName+ " failed.",
                                                  APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }        
        else if (cmd.equals(JPhotoMenu.A_EXIT)) {
            exitConfirmedSave();
        }
        else if (cmd.equals(JPhotoMenu.A_CUT)) {
            int index = list.getSelectedIndex();
            transferHandler.exportToClipboard(list, getToolkit().getSystemClipboard(),
                                              TransferHandler.MOVE);
            list.setSelectedIndex(index);
        }
        else if (cmd.equals(JPhotoMenu.A_COPY)) {
            transferHandler.exportToClipboard(list, getToolkit().getSystemClipboard(),
                                              TransferHandler.COPY);
        }
        else if (cmd.equals(JPhotoMenu.A_PASTE)) {
            transferHandler
                .importData(list,getToolkit().getSystemClipboard().getContents(list));
        }
        else if (cmd.equals(JPhotoMenu.A_DELETE)) {
            int index = list.getSelectedIndex();
            Object selected[] = list.getSelectedValues();
            if (list!=null)
                for (int i=0; i<selected.length; i++) {
                    JPhoto photo = (JPhoto)selected[i];
                    photos.remove(photo);
                }
            list.setSelectedIndex(index);
        }
        else if (cmd.equals(JPhotoMenu.A_INSERT)) {
            int index = list.getSelectedIndex();
            photos.add(index, new JPhoto(photos));
            Dimension dim = scrollPane.getViewport().getSize();
            list.setVisibleBounds(dim);
            list.setSelectedIndex(index);
        }
        else if (cmd.equals(JPhotoMenu.A_INSERT_ALBUM)) {
            String file = askFileName(FILE_EXT, FileDialog.LOAD);
            if (file!=null) {
                int index = list.getSelectedIndex();
                if (index<0)
                    index = photos.size();
                JPhoto item = new JPhotoAlbumLink(photos, file);
                photos.add(index, item);
                Dimension dim = scrollPane.getViewport().getSize();
                list.setVisibleBounds(dim);
                list.setSelectedIndex(index);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_TITLE)) {
            String res = JOptionPane.showInputDialog(this, "Page title",
                                                     photos.getTitle());
            if (res!=null)
                photos.setTitle(res);
            setTitle();
        }
        else if (cmd.equals(JPhotoMenu.A_DESCRIPTION)) {
            String res = JOptionPane.showInputDialog(this, "Page description",
                                                     photos.getDescription());
            if (res!=null)
                photos.setDescription(res);
            setTitle();
        }
        else if (cmd.equals(JPhotoMenu.A_KEYWORDS)) {
            String def = photos.getKeywords();
            if (def.equals(""))
                def = "digital photos ";
            String res = JOptionPane.showInputDialog(this, "Keywords",
                                                     def);
            if (res!=null)
                photos.setKeywords(res);
            setTitle();
        }
        else if (cmd.equals(JPhotoMenu.A_WATERMARK)) {
            String def = photos.getWatermark();
            if (def.equals(""))
                def = "� "+Calendar.getInstance().get(Calendar.YEAR)+" ";
            String res = JOptionPane.showInputDialog(this, "Watermark",
                                                     def);
            if (res!=null)
                photos.setWatermark(res);
            setTitle();
        }
        else if (cmd.equals(JPhotoMenu.A_COVERPHOTO)) {
            int index = list.getSelectedIndex();
            if (index<0)
                index = 0;
            IJPhoto photo = photos.get(index);
            if (photo.getImageName()!=null) {
                photos.setCoverPhoto(photos.get(index));
                setFrameIcon();
            }
            else {
                JOptionPane.showMessageDialog(this, "Cover photo must be a real image.",
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_SAVE_DEFAULTS)) {
            prefs.put(PAGEINFO, photos.getPageInfo().marshal());
            JOptionPane.showMessageDialog(this, "Saved current page attributes as defaults.",
                                          APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else if (cmd.equals(JPhotoMenu.A_FULLVIEW)) {
            // list.toggleFullView();
            startFullView();                
        }
        else if (cmd.equals(JPhotoMenu.A_SHOWEXIF)) {
            showExif();
        }
        else if (cmd.equals(JPhotoMenu.A_SLIDESHOW)) {
            showSlideshow(photos);

        }
        else if (cmd.equals(JPhotoMenu.A_HELP)) {
            displayHelp();
        }
        else if (cmd.equals(JPhotoMenu.A_ABOUT)) {
            JOptionPane.showMessageDialog(this, APP_NAME+" v1.4.5 - Organize and Publish Your Digital Photos.\n"+
                                          "Copyright 2005-2007 Jari Karjala [www.jpkware.com],\n"
                                          +"Tarja Hakala [www.hakalat.net]"
                                          +" and Zbynek Mu��k [zbynek.muzik@email.cz]\n"
                                          +"This is free software, licenced under the GNU General Public License.",
                                          JPhotoMenu.A_ABOUT, JOptionPane.INFORMATION_MESSAGE);
        }
        else if (cmd.equals(JPhotoMenu.A_BACKGROUND)) {
            Color color = Utils.showColorDialog(this, "Choose Background Color",
                                                list.getBackground());
            
            if (color != null){
                list.setBackground(color);
                fullViewPanel.setBackground(color);
                photos.setBackgroundColor(color);
            }
        }
        else if (cmd.equals(JPhotoMenu.A_FOREGROUND)) {
            Color color = Utils.showColorDialog(this, "Choose Foreground Color",
                                                list.getForeground());
            
            if (color != null){
                list.setForeground(color);
                fullViewPanel.setForeground(color);
                photos.setForegroundColor(color);
            }
        }
        else
            System.out.println("Not implemented: "+cmd);
        
        setTitle();
    }

    void showSlideshow(IPhotoCollection photos) {
        if (photos.getSize()>0) {
            JPhotoShow show = new JPhotoShow(this.photos, 5000, list);
            show.setVisible(true);
        }
        else
            JOptionPane.showMessageDialog(this, "No photos to show!",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public void insertPhotos(String files[]) {
        if (files!=null) {
            int index = list.getSelectedIndex();
            if (index<0)
                index = 0;
            else
                index++;

            list.getPhotoModel().addAll(index, files);
            list.setSelectedIndex(index-1);

            Dimension dim = scrollPane.getViewport().getSize();
            list.setVisibleBounds(dim);
        }
    }
    
    public void setFrameIcon() {
        BufferedImage icon = photos.getCoverIcon();
        if (icon==null) {
            URL res = getClass().getClassLoader().getResource("pics/JPhotoAlbum64.png");
            Image bi = new ImageIcon(res).getImage();
            setIconImage(bi);
        }
        else {
            Dimension limits = new Dimension(64,64);
            limits = Utils.getScaledSize(limits,icon.getWidth(),icon.getHeight());
            BufferedImage bi = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(icon, (64-limits.width)/2,(64-limits.height)/2,
                         limits.width,limits.height,null, null);
            //g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            //g2.drawString("Jph",0,12);
            g2.dispose();
            setIconImage(bi);
        }
        
    }
    
    public void showExif() {
                    
        JPhoto photo = (JPhoto)list.getSelectedValue();

        if (photo == null) {
            System.out.println("No picture selected");
            return;
        }
        ArrayList tagValues = new ArrayList();
        ExifDirectory directory = photo.getExifDirectory();
        
        Iterator tags = directory.getTagIterator();
        while (tags.hasNext()) {
            Tag tag = (Tag)tags.next();
            tagValues.add(tag);
        }
        if (directory.hasErrors()) {
            Iterator errors = directory.getErrors();
            while (errors.hasNext()) {
                System.out.println("ERROR: " + errors.next());
            }
        }

        JDialog dialog = getExifDialog(tagValues);
        exifDialog.setTitle(photo.getImageName());
        exifDialog.setVisible(true);
    }

    public JDialog getExifDialog(java.util.List tagValues) {

        if (exifDialog == null) 
            exifDialog = new JPhotoExifDialog(this, tagValues, prefs);
        else 
            exifDialog.changeTagValues(tagValues);
        
        return exifDialog;
    }
    
    public String askFileName(String extension, int mode) {
        String file = null;
        FileDialog dialog = new FileDialog(this, "Select Album filename", mode);
        dialog.setFile("*"+extension);
        dialog.show();
        
        if (dialog.getFile()!=null) {
            file = dialog.getDirectory() + dialog.getFile();
            System.out.println("askFileName: " + file);
        if (! file.toLowerCase().endsWith(extension))
            file += extension;
        }
        dialog.dispose();
        return file;
    }
    public void exitConfirmedSave() {
        savePrefs();
        if (confirmedSave()!=JOptionPane.CANCEL_OPTION) {
            if (allFrames.size()>1) {
                setVisible(false);
                allFrames.remove(getFrameName());
                System.out.println("removed "+getFrameName()+", no exit yet");
            }
            else {
                System.out.println("Last window closed, exit");
                System.exit(0);
            }
        }
    }

    public void savePrefs() {
        try {
            Rectangle b = getBounds();
            prefs.putInt(FRAME_X,b.x);
            prefs.putInt(FRAME_Y,b.y);
            prefs.putInt(FRAME_W,b.width);
            prefs.putInt(FRAME_H,b.height);
            prefs.putInt(SPLIT, splitPane.getDividerLocation());
            if (exifDialog!=null) {
                b = exifDialog.getBounds();
                prefs.putInt(EXIF_X,b.x);
                prefs.putInt(EXIF_Y,b.y);
                prefs.putInt(EXIF_W,b.width);
                prefs.putInt(EXIF_H,b.height);
            }
            if (helpFrame!=null) {
                b = helpFrame.getBounds();
                prefs.putInt(HELP_X,b.x);
                prefs.putInt(HELP_Y,b.y);
                prefs.putInt(HELP_W,b.width);
                prefs.putInt(HELP_H,b.height);
            }
            prefs.put(PHOTO_DIR, photoDirectory.toString());
        }
        catch (Exception e) {
            System.out.println("savePrefs:"+e);
        }
    }

    public boolean askNameAndSave() {
        if (albumFileName==null) {
            albumFileName = askFileName(FILE_EXT, FileDialog.SAVE);
        }
        else
            if (!photos.isDirty())
                return true;  // No need to save if already saved once
        
        if (albumFileName!=null) {
            try {
                if (photos.save(albumFileName)) {
                    setTitle();
                    return true;
                }
                JOptionPane.showMessageDialog(this, "Saving "+albumFileName+" failed",
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Save exception "
                                              +albumFileName+":"+e.getMessage(),
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }
    
    public int confirmedSave() {
        if (!photos.isDirty())
            return JOptionPane.NO_OPTION;  // No need to ask or save
        
        String prompt = "Save "+(albumFileName==null ? "" : albumFileName)+"?";
        int res = JOptionPane.showConfirmDialog(frame, prompt, APP_NAME,
                                                JOptionPane.YES_NO_CANCEL_OPTION);
        if (res==JOptionPane.YES_OPTION) {
            if (albumFileName==null)
                albumFileName = askFileName(FILE_EXT, FileDialog.SAVE);
            if (albumFileName!=null)
                frame.photos.save(albumFileName);
            else
                res = JOptionPane.CANCEL_OPTION;
        }
        setTitle();
        return res;
    }
    
    public void saveEdit() {
        // System.out.println("saveEdit:"+editingPhoto+"/"+textInput.getText());
        if (editingPhoto!=null
            && !textInput.getText().equals(editingPhoto.getText())) {
            editingPhoto.setText(textInput.getText());
            photos.setDirty(true);
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

    public void selectPhoto(JPhoto photo) {
        // System.out.println("valueChanged, selected="+photo);
        
        setTitle();
        saveEdit();
        editingPhoto = photo;
        
        if (photo==null) {
            statusLine.setText("Nothing selected");
            textInput.setText("");
            setFullViewPhoto(null);
            return;
        }

        if (photo.getAlbumLink()!=null) {
            // It is a link to another album, could show it on right side...
            statusLine.setText("Link to "+photo.getFullAlbumLink());
        }
        else
        if (photo.getOriginalName()!=null) {
            statusLine.setText(photo.getOriginalName()+": "+photo.getExif().toString());
        }
        else
            statusLine.setText("");
        textInput.setText(photo.getText());

        if (exifDialog != null && exifDialog.isVisible())
            showExif();
        
        setFullViewPhoto(photo);
    }

    public void setTitle() {
        int sel = list.getSelectedIndex();
        setTitle((photos.getTitle().equals("") ? getFrameName() : photos.getTitle())
                 + " - "
                 + (editingPhoto!=null ? (sel+1)+"/" : "")
                 + photos.getSize() + " Photos "
                 + (photos.isDirty() ? " [modified]" : "")
                 + " - "+APP_NAME);

        list.setForeground(photos.getForegroundColor());
        list.setBackground(photos.getBackgroundColor());
    }
    
    public void setFullViewPhoto(JPhoto photo) {
        fullViewPanel.setForeground(photos.getForegroundColor());
        fullViewPanel.setBackground(photos.getBackgroundColor());
        fullViewPanel.setWatermark(photos.getWatermark());
        fullViewPanel.setPhoto(photo);
    }

    /** Window closing
     */
    class closeAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent evt) {
            exitConfirmedSave();
            System.out.println("Exit cancelled.");
        }
    }

    /** List selection changed
     */
    public void valueChanged(ListSelectionEvent e) {
        JPhoto photo = (JPhoto)list.getSelectedValue();
        if (e.getValueIsAdjusting())
            return;
        
        selectPhoto(photo);
    }

    /** Fullscreen view */
    public void startFullView() {
        if (list.getSelectedIndex()>=0) {
            JPhoto photo = (JPhoto)photos.getElementAt(list.getSelectedIndex());
            if (photo.getAlbumLink()!=null) {
                // It is a link to another album, start new instance
                String newFile = photo.getFullAlbumLink();
                if (!showExistingFrame(newFile)) {
                    JPhotoFrame newFrame = null;
                    try {
                        newFrame = new JPhotoFrame(newFile, new JPhotoCollection(newFile));
                        newFrame.setVisible(true);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Cannot open "+newFile,
                                                      APP_NAME, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            else {
                JPhotoShow show = new JPhotoShow(photos, 0, list);
                show.setVisible(true);
            }
        }
        else
            JOptionPane.showMessageDialog(this, "Select a photo to view!",
                                          APP_NAME, JOptionPane.ERROR_MESSAGE);
    }
    
    public String getFrameName() {
        if (albumFileName==null)
            return "[Unsaved]";
        else
            return albumFileName;
    }

    public boolean showExistingFrame(String frameName) {
        JPhotoFrame oldOne = (JPhotoFrame)allFrames.get(frameName);
        if (oldOne!=null) {
            oldOne.setVisible(true);
            return true;
        }
        return false;
    }
    
    public void toggleView() {
        int max = splitPane.getWidth() - splitPane.getDividerSize();
        if (splitPane.getDividerLocation() < max)
            splitPane.setDividerLocation(max);
        else
            splitPane.setDividerLocation(splitPane.getLastDividerLocation());
    }
    
    /** Single click shows exif, Double click toggles full view
     */
    class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e.getClickCount() == 1) {
                JPhoto photo = (JPhoto)list.getSelectedValue();
                selectPhoto(photo);
            }
            
            if (e.getClickCount() == 2) {
                startFullView();
            }
        };
    };
    
    /**
     * When edit focus is lost, save results.
     */
    class FocusHandler extends FocusAdapter {
        public void focusLost(FocusEvent e) {
            saveEdit();
        }
    }

    /**
     * Save any editing, and move selection to given direction.
     */
    class MoveSelection extends AbstractAction {
        int direction;
        public MoveSelection(int dir) {
            direction = dir;
        }
        public void actionPerformed(ActionEvent ae) {
            list.moveSelection(direction);
        }
    }

    /** Display Help file.
     */
    public void displayHelp() {
        if (helpFrame==null) {
            JTextPane tp = new JTextPane();
            tp.setEditable(false);
            JScrollPane js = new JScrollPane();
            js.getViewport().add(tp);
            helpFrame = new JFrame();
            helpFrame.setTitle(APP_NAME+" Help");
            helpFrame.getContentPane().add(js);
            helpFrame.pack();
            helpFrame.setSize(prefs.getInt(HELP_W,400), prefs.getInt(HELP_H,400));
            helpFrame.setLocation(prefs.getInt(HELP_X,500), prefs.getInt(HELP_Y,50));

            try {
                URL help = getClass().getClassLoader().getResource("jphotohelp.html");
                tp.setPage(help);
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        helpFrame.setVisible(true);
    }

    public static boolean exportAll(String jphFile, String flags) {
        boolean ok = false;
        try {
            jphFile = new File(jphFile).getAbsolutePath();
            JPhotoCollection photos = new JPhotoCollection(jphFile);
            photos.disableGui();

            if (flags.indexOf('h')>=0)
                JPhotoCollection.setHtmlOnlyMode(true);
            
            if (flags.indexOf('0')>=0)
                ok |= photos.exportSubtitledPhotos(jphFile);
            
            if (flags.indexOf('1')>=0)
                ok |= photos.exportHtmlJari1(jphFile);
            
            if (flags.indexOf('2')>=0)
                ok |= photos.exportHtmlJari2(jphFile);
            
            // ok |= photos.exportHtmlJuha(target);
            if (flags.indexOf('3')>=0)
                ok |= photos.exportHtmlTarja2(jphFile);
        }
        catch (Exception e) {
            System.out.println("exportAll:");
            e.printStackTrace(System.out);
        }
        return ok;
    }
    
    public static void main(String args[]) throws Exception {
        JPhotoFrame frame = null;
        if (args.length==0) {
            frame = new JPhotoFrame(null, (JPhotoCollection)null);
        }
        else if (args[0].equals("show") && args.length>1) {
            String newArgs[]  = { args[1] };
            JPhotoShow.main(newArgs);
        }
        else if (args[0].startsWith("export")) {
            String flags = "0123";
            if (args[0].length()>5)
                flags = args[0].substring(6);
            for (int i=1; i<args.length; i++)
                exportAll(args[i], flags);
        }
        else if (args[0].indexOf(FILE_EXT)<0) {
            frame = new JPhotoFrame(null, Utils.expandAllDirectories(args));
        }
        else {
            try {
                String fileName = new File(args[0]).getAbsolutePath();
                frame = new JPhotoFrame(fileName, (JPhotoCollection)null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot open "+args[0]+":"+e,
                                              APP_NAME, JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
}
