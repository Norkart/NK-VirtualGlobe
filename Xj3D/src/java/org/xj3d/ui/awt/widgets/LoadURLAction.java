/*****************************************************************************
 * Copyright North Dakota State University and Web3d.org, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External Imports
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JTextField;

// Local imports
import org.web3d.util.FileHandler;
import org.web3d.util.ErrorReporter;

/**
 * Simple utility class for processing scene loading requests from the
 * location bar.
 *
 * @author Brad Vender
 * @version $Revision: 1.2 $
 */
public class LoadURLAction implements ActionListener, Runnable {

    /** The panel to load using */
    private FileHandler handler;

    /** Where to get String */
    private JTextField urlSourceA;

    /** Where to get String */
    private TextField urlSourceB;

    /**
     * Basic constructor using a swing component.
     *
     * @param target The panel to modify
     * @param source The component from which URLs will be gotten
     */
    public LoadURLAction(FileHandler target, JTextField source) {
        handler = target;
        urlSourceA = source;
    }

    /**
     * Basic constructor using an AWT component.
     *
     * @param target The panel to modify
     * @param source The component from which URLs will be gotten
     */
    public LoadURLAction(FileHandler target, TextField source) {
        handler = target;
        urlSourceB = source;
    }

    //------------------------------------------------------------------------
    // Methods defined by ActionListener
    //------------------------------------------------------------------------

    /**
     * Starts a new loading thread.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        new Thread(this).start();
    }

    //------------------------------------------------------------------------
    // Methods defined by Runnable
    //------------------------------------------------------------------------

    /**
     * Just gets the URL from the text field and calls loadURL
     */
    public void run() {

        String urlToLoad = urlSourceA != null ?
                           urlSourceA.getText() :
                           urlSourceB.getText();

        ErrorReporter reporter = handler.getErrorReporter();

        try {
            reporter.messageReport("Attempting to load:" + urlToLoad);

            String url_str = null;

            // try a file first
            File f = new File(urlToLoad);
            if(f.exists()) {
                if(f.isDirectory())
                    reporter.errorReport("File is a directory", null);
                else {
                    url_str = f.toURL().toExternalForm();
                }
            } else {
                // Try a URL
                URL url = new URL(urlToLoad);
                url_str = url.toExternalForm();
            }

            handler.loadURL(url_str);
        } catch (IOException ioe) {
            reporter.errorReport("Unable to load " + urlToLoad, ioe);
        } catch (Exception e) {
            // Would send this to the error reporter but
            // the regular error reporter chops out exception name and traceback
            reporter.errorReport("Unusual exception trying to load:"+
                                   urlToLoad, e);

        }
    }
}
