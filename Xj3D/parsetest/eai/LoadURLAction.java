/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTextField;

import vrml.eai.Browser;

/**
 * LoadURLAction is a simple ActionListener
 * which gets a String from a designated JTextField
 * and calls loadURL on a designated Browser instance.
 * Originally intended to call setTitle on a designated
 * JFrame.
 */
class LoadURLAction implements ActionListener {
    /** The designated Browser */
    Browser theBrowser;

    /** The designated JTextField */
    JTextField URL;

    /** The designated JFrame. */
    JFrame windowFrame;

    /**
     * Basic constructor
     * @param b The designated Browser.
     * @param jta The designated JTextField.
     * @param jf The designated JFrame
     */
    LoadURLAction(Browser b, JTextField jta, JFrame jf) {
      theBrowser=b;
      URL=jta;
      windowFrame=jf;
    }

    /** Replace the targetted browser instance. */
    public void setBrowser(Browser newBrowser) {
        theBrowser=newBrowser;
    }

    /** Deal with the mouse click */
    public void actionPerformed(ActionEvent e) {
        String args[]=new String[1];
        args[0]=URL.getText(); 
        theBrowser.loadURL(args,null);
    }
}
