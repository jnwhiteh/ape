/*
 * This file is part of JPhotoAlbum.
 * Copyright 2003 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoStatus.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */
package fi.iki.jka;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

public class JPhotoStatus {
    protected static JFrame parent = null;
    protected static JLabel statusLine = null;
    protected static ProgressMonitor progressMonitor = null;
    protected static boolean canceled = false;

    private JDialog dialog;
    private JProgressBar bar;
    private JLabel label;
    
    /** Main application class should set this to receive status updates from
     * lower level classes.
     */
    static void setStatusLineLabel(JFrame parentFrame, JLabel text) {
        parent = parentFrame;
        statusLine = text;
    }

    /** Show status message in status line component, if it has been set.
     * This method may be called from non-swing threads, too.
     */
    static void showStatus(String caller, String status) {
        if (statusLine!=null) {
            // I wonder if this Swing method is thread-safe...
            statusLine.setText(status);
        }
        if (caller!=null)
            System.out.println(caller+":"+status);
        else
            System.out.println(status);
    }

    /** Show status message and progress indication. Should be called at least 
     * once with percentDone==0 and once with percentDone>100. The caller can
     * be null after the first call.
     * This method can be called from non-swing threads, too.
     */
    static void showProgress(final String caller, final String status, final int percentDone) {
        if (parent!=null && ! EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        showProgress(caller, status, percentDone);
                    }
                });
            } catch (Exception e) {
                System.out.println("showProgress invokeAndWait failed:"+e);
                e.printStackTrace();
                e.getCause().printStackTrace();
            }
            return;
        }
        
        System.out.println((caller!=null ? caller+":" : "")
                           +status+":"+percentDone+"%");
        if (parent==null)
            return; // called from command line
        
        if (percentDone==0 && progressMonitor==null) {
            parent.setEnabled(false);
            //parent.setFocusableWindowState(false);
            progressMonitor = new ProgressMonitor(parent,caller,status, 0,100);
            progressMonitor.setProgress(0);
            progressMonitor.setMillisToDecideToPopup(0);
            setCanceled(false);
        }

        if (progressMonitor.isCanceled())
            setCanceled(true);
        else {
            progressMonitor.setNote(status);
            progressMonitor.setProgress(percentDone);
        }
        
        if (percentDone>=100) {
            progressMonitor.close();
            progressMonitor = null;
            parent.setEnabled(true);
            parent.setFocusableWindowState(true);
            parent.requestFocus();
        }
    }

    static boolean inProgress() {
        return progressMonitor!=null;
    }
    
    static void setCanceled(boolean val) {
        canceled = val;
    }
    
    /** True if user canceled long running operation. This is possible if
     * showProgress has been called and a progress dialog has been shown.
     * The caller should terminate processing and return when possible.
     * It must call showProgress with percentage of 100 to close progress
     * dialog even if canceled. 
     */
    static boolean isCanceled() {
        return canceled;
    }
}
