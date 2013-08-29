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

// External imports
import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;

import java.util.prefs.Preferences;

//import javax.swing.AbstractAction;
//import javax.swing.KeyStroke;
//import javax.swing.JFileChooser;

// Local imports
import org.web3d.util.FileHandler;
import org.web3d.util.ErrorReporter;

/**
 * A Selection handler used to select a file from the local
 * system, then pass the file's url along to a file handler
 * for loading.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class OpenAction implements SelectionListener, Runnable {
    
    /** The open file dialog title */
    private static final String OPEN_FILE_DIALOG_TITLE = "Open File";
    
    /** Conversion of file name to url failure error message */
    private static final String FILENAME_TO_URL_FAILURE_MESSAGE = 
        "Filename conversion to URL failed";
    
    /** IOException while attempting to load a file message */
    private static final String UNABLE_TO_LOAD_MESSAGE = 
        "Unable to load: ";
    
    /** The set of file extension filter names 
     *  (as they appear in the file dialog) */
    private static String[] filterName = new String[] {
        "All Files",
        "Just X3D Files",
        "Just VRML Files",
		"Just GZIP'ed Files",
    };
    
    /** The cooresponding file extensions */
    private static String[] filterExtension = new String[] {
        "*.*",
        "*.x3d*",
        "*.wr*",
		"*.gz",
    };
	
    /** The last directory property */
    private static final String LASTDIR_PROPERTY = "History_";
	
    /** The handler for dealing with file open actions */
    private FileHandler fileHandler;
    
    /** Reporter instance for distributing error notifications */
    private ErrorReporter errorReporter;
    
    /** Parent shell used to handle the file dialog */
    private Shell parent;
    
    /** The intial content directory for the dialog to use */
    private String contentDir;
    
    /** The file dialog */
    private FileDialog dialog;
        
    /** The url String of the file to load */
    private String urlToLoad;

    /**
     * Create an instance of the open selection handler.
     *
     * @param shell - The parent shell
     * @param handler - A handler for opening files
     * @param contentDirectory - The initial directory to load
     * content from. Must be a full path.
     */
    public OpenAction( 
        Shell shell,
        FileHandler handler,
        String contentDirectory ) {
        
        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_O,
        //                                           KeyEvent.CTRL_MASK);
        //
        //putValue(ACCELERATOR_KEY, acc_key);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        //putValue(SHORT_DESCRIPTION, "Open A new VRML or X3D file");
        
        parent = shell;
        fileHandler = handler;
        errorReporter = fileHandler.getErrorReporter( );
        
        if ( contentDirectory != null ) {
            contentDir = contentDirectory;
        } else {
			Preferences prefs = Preferences.userNodeForPackage( OpenAction.class );
            String last_dir = prefs.get( LASTDIR_PROPERTY, null );
            if ( last_dir != null ) {
                contentDir = last_dir;
			} else {
                contentDir = System.getProperty( "user.dir" );
			}
        }
    }
    
    //------------------------------------------------------------------------
    // Methods defined by SelectionListener
    //------------------------------------------------------------------------
    
    /**
     * A selection has been performed that requires opening a file dialog
     * to allow the user the opportunity to select the file to load. If a
     * valid file selection has been made, check that it exists in the local
     * file system, then forward it's URL to the file handler for loading.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetSelected( SelectionEvent se ) {
        if ( dialog == null ) {
            createDialog( );
        }
        try {
            String fileName = dialog.open( );
            if ( fileName != null ) {
				
                File file = new File( fileName );
				
				Preferences prefs = Preferences.userNodeForPackage( OpenAction.class );
                prefs.put( LASTDIR_PROPERTY, file.getParent( ) );
				
                if ( file.exists( ) ) {
                    urlToLoad = file.toURL( ).toString( );
                    // move the actual loading off the display thread
                    new Thread(this).start( );
                }
            }
        } catch( MalformedURLException mue ) {
            errorReporter.errorReport( FILENAME_TO_URL_FAILURE_MESSAGE, mue );
        }
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
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
    /** 
     * Create the dialog 
     */
    private void createDialog( ) {
        dialog = new FileDialog( parent, SWT.OPEN );
        dialog.setText( OPEN_FILE_DIALOG_TITLE );
        dialog.setFilterPath( contentDir );
        dialog.setFilterNames( filterName );
        dialog.setFilterExtensions( filterExtension );
    }
}
