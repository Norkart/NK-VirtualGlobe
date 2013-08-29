/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.widgets;

// Local imports
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;

// Local imports
import org.web3d.util.FileHandler;
import org.web3d.util.ErrorReporter;

/**
 * A selection handler that reloads the last file.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class ReloadAction implements SelectionListener, Runnable  {
    
    /** IOException while attempting to load a file message */
    private static final String UNABLE_TO_LOAD_MESSAGE = 
        "Unable to load: ";
    
    /** The handler for dealing with file open actions */
    private FileHandler fileHandler;
    
    /** Reporter instance for distributing error notifications */
    private ErrorReporter errorReporter;
    
    /** The text field containing the current url */
    private Text urlText;
    
    /** The url String to load */
    private String urlToLoad;

    /**
     * Create an instance of the action class.
     *
     * @param handler - A handler for opening files
     * @param source - The text widget from which URLs will be gotten
     */
    public ReloadAction( FileHandler handler, Text source ) {
        fileHandler = handler;
        errorReporter = fileHandler.getErrorReporter( );
        urlText = source;
        
        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_O,
        //    KeyEvent.CTRL_MASK);
        
        //putValue(ACCELERATOR_KEY, acc_key);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        //putValue(SHORT_DESCRIPTION, "Open A new VRML or X3D file");
    }   
    
    //------------------------------------------------------------------------
    // Methods defined by SelectionListener
    //------------------------------------------------------------------------
    
    /**
     * Process the selection generated from the user interface.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetSelected( SelectionEvent se ) {
        urlToLoad = urlText.getText( );
        new Thread(this).start( );
    }
    
    /**
     * Process the default selection generated from the user interface.
     * Ignored.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
    } 
    
    //------------------------------------------------------------------------
    // Methods defined by Runnable
    //------------------------------------------------------------------------
    
    /**
     * Get the URL String and call the file handler to load
     */
    public void run( ) {
        try {
            fileHandler.loadURL( urlToLoad );
        } catch( IOException ioe ) {
            // note: while the FileHandler interface claims to throw an IOException,
            // the LocationToolbar does not seem to....
            errorReporter.errorReport( UNABLE_TO_LOAD_MESSAGE, ioe );
        }
    }
}
