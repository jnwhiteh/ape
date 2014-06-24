/*
 * This file is part of JPhotoAlbum.
 * Copyright 2004 Jari Karjala <jpkware.com> & Tarja Hakala <hakalat.net>
 *
 * @version $Id: JPhotoAction.java,v 1.1.1.1 2004/05/21 18:24:59 jkarjala Exp $
 */

package fi.iki.jka;

import java.awt.event.*;
import javax.swing.*;

class JPhotoAction extends AbstractAction {
    ActionListener listener = null;
    
    public JPhotoAction(ActionListener listener,
                        String text) {
        this(listener, text, 0, null);
    }
    
    public JPhotoAction(ActionListener listener,
                        String text, int mnemonic) {
        this(listener, text, mnemonic, null);
    }
    
    public JPhotoAction(ActionListener listener,
                        String text, int mnemonic, KeyStroke accelerator) {
        super(text, null);
        this.listener = listener;
        if (accelerator!=null)
            putValue(ACCELERATOR_KEY, accelerator);
        if (mnemonic!=0)
            putValue(MNEMONIC_KEY, new Integer(mnemonic));
    }
    public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(e);
    }
}

