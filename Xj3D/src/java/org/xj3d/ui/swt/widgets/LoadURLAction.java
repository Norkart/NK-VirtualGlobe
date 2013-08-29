/*****************************************************************************
 * Copyright North Dakota State University and Web3d.org, 2004 - 2006
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.swt.widgets;

// External Imports
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

// Local imports
import org.web3d.util.FileHandler;
import org.web3d.util.ErrorReporter;

/**
 * Simple utility class for processing scene loading requests from the
 * location bar.
 *
 * @author Brad Vender, Rex Melton
 * @version $Revision: 1.1 $
 */
public class LoadURLAction implements SelectionListener, Runnable {

    /** Attempting to load a file message */
    private static final String ATTEMPTING_TO_LOAD_MESSAGE = 
        "Attempting to load: ";
    
    /** Conversion of file name to url failure error message */
    private static final String INVALID_URL_MESSAGE = 
        "Not a valid URL: ";
    
    /** IOException while attempting to load a file message */
    private static final String UNABLE_TO_LOAD_MESSAGE = 
        "Unable to load: ";
    
    /** Unanticipated Exception while attempting to load a file message */
    private static final String OTHER_EXCEPTION_WHILE_LOADING_MESSAGE = 
        "Unusual exception trying to load: ";
    
    /** File is a directory error message */
    private static final String FILE_IS_DIRECTORY_MESSAGE = 
        "File is a directory";
    
    /** The panel to load using */
    private FileHandler fileHandler;

    /** Reporter instance for distributing error notifications */
    private ErrorReporter errorReporter;
    
    /** Where to get String */
    private Text urlText;
    
    /** The url String to load */
    private String urlToLoad;

    /**
     * Basic constructor using a swing component.
     *
     * @param handler - A handler for opening files
     * @param source - The text widget from which URLs will be gotten
     */
    public LoadURLAction( FileHandler handler, Text source ) {
        fileHandler = handler;
        errorReporter = fileHandler.getErrorReporter( );
        urlText = source;
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
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
        urlToLoad = urlText.getText( );
        new Thread(this).start( );
    } 
    
    //------------------------------------------------------------------------
    // Methods defined by Runnable
    //------------------------------------------------------------------------

    /**
     * Get the URL from the text field and calls loadURL
     */
    public void run( ) {
        try {
            errorReporter.messageReport( ATTEMPTING_TO_LOAD_MESSAGE + urlToLoad );

            String url_str = null;

            // try a file first
            File f = new File( urlToLoad );
            if( f.exists( ) ) {
                if( f.isDirectory( ) )
                    errorReporter.errorReport( FILE_IS_DIRECTORY_MESSAGE, null );
                else {
                    url_str = f.toURL( ).toExternalForm( );
                }
            } else {
                // Try a URL
                URL url = new URL( urlToLoad );
                url_str = url.toExternalForm( );
            }

            fileHandler.loadURL( url_str );
            
        } catch( MalformedURLException mue ) {
            errorReporter.errorReport( INVALID_URL_MESSAGE, mue );
        } catch ( IOException ioe ) {
            // note: while the FileHandler interface claims to throw an IOException,
            // the LocationToolbar does not seem to....
            errorReporter.errorReport( UNABLE_TO_LOAD_MESSAGE + urlToLoad, ioe );
        } catch ( Exception e ) {
            // Paranoia - big destroia.......
            // It is said that.....
            // the regular error reporter chops out exception name and traceback
            errorReporter.errorReport( OTHER_EXCEPTION_WHILE_LOADING_MESSAGE +
                                   urlToLoad, e );
        }
    }
}
