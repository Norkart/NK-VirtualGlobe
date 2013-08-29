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
import java.io.File;
import java.io.IOException;

import java.util.Properties;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.ietf.uri.*;
import org.ietf.uri.event.ProgressListener;


// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;

import org.web3d.util.FileHandler;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.impl.core.loading.FramerateThrottle;

/**
 * An swt widget that implements the capabilities of the URL/Location
 * toolbar.
 * <p>
 *
 *
 * @author Justin Couch, Rex Melton
 * @version $Revision: 1.2 $
 */
public class LocationToolbar extends Composite implements 
    FileHandler, BrowserCoreListener, Runnable {
    
    /** File is a directory error message */
    private static final String FILE_IS_DIRECTORY_MESSAGE = 
        "File is a directory";
    
    /** Location label text */
    private static final String LOCATION_LABEL = " Location: ";
    
    /** Go button text */
    private static final String GO_BUTTON_TEXT = " Go! ";
    
    /** Go button tool tip text */
    private static final String GO_BUTTON_TOOLTIP_TEXT = 
        "Go to the new location";
    
    /** Open button text */
    private static final String OPEN_BUTTON_TEXT = "Open";
    
    /** Open button tool tip text */
    private static final String OPEN_BUTTON_TOOLTIP_TEXT = 
        "Open File";
    
    /** Reload button text */
    private static final String RELOAD_BUTTON_TEXT = "Reload";
    
    /** Reloaded button tool tip text */
    private static final String RELOAD_BUTTON_TOOLTIP_TEXT = 
        "Reload current location";
    
    /** Empty skin definition for default */
    private static final Properties DEFAULT_SKIN = new Properties( );
    
    /** Property in browser skin which determines 'Go' image */
    private static final String GO_BUTTON_PROPERTY = "GO.button";
    
    /** Property in browser skin which determines 'open' image */
    private static final String OPEN_BUTTON_PROPERTY = "OPEN.button";
    
    /** Property in browser skin which determines 'open' image */
    private static final String RELOAD_BUTTON_PROPERTY = "RELOAD.button";
    
    /** Default image to use for go button */
    private static final String DEFAULT_GO_BUTTON =
        "images/locationbar/goIcon32x32.gif";
    
    /** Default image to use for reload button */
    private static final String DEFAULT_OPEN_BUTTON =
        "images/locationbar/openIcon32x32.gif";
    
    /** Default image to use for reload button */
    private static final String DEFAULT_RELOAD_BUTTON =
        "images/locationbar/reloadIcon32x32.gif";
    
    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;
    
    /** The textfield to read the values from */
    private Text urlTextField;
    
    /** The go button on the URl panel */
    private Button locationGoButton;
    
    /** The open button on the URl panel */
    private Button openButton;
    
    /** The reload button on the URl panel */
    private Button reloadButton;
    
    /** Display instance, used to put updates on the display thread */
    private Display display;
    
    /** The content directory to load content from.  NULL if none provided */
    private String contentDirectory;
    
    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;
    
    /** A world loader manager for loading worlds */
    private WorldLoaderManager loader;

    /** The framerate throttle if in use */
    private FramerateThrottle throttle;

    /** Side pocketed copy of text destined for the urlTextField,
     *  which will be set into the field on the display thread */
    private String urlText;
    
    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param parent - The SWT Composite widget that this will be added to
     * @param wlm - The world loader manager.
     * @param core - The browser core implementation to send scene loads to
     * @param urlReadOnly - true to make the location bar read only
     * @param showOpenButton - true to put an open button with the URL location bar
     * @param showReloadButton - true to put a reload button with the URL location bar
     * @param contentDir - The initial directory to load content from.  Must be a full path.
     * @param reporter - The reporter instance to use or null
     */
    public LocationToolbar(
        Composite parent,
        BrowserCore core,
        WorldLoaderManager wlm,
        boolean urlReadOnly,
        boolean showOpenButton,
        boolean showReloadButton,
        String contentDir,
        ErrorReporter reporter) {
        
        this(
            parent,
            core,
            wlm,
            urlReadOnly,
            showOpenButton,
            showReloadButton,
            contentDir,
            null,
            reporter);
    }
    
    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param parent - The SWT Composite widget that this will be added to
     * @param wlm - The world loader manager.
     * @param core - The browser core implementation to send scene loads to
     * @param urlReadOnly - true to make the location bar read only
     * @param showOpenButton - true to put an open button with the URL location bar
     * @param showReloadButton - true to put a reload button with the URL location bar
     * @param contentDir - T initial directory to load content from.  Must be a full path.
     * @param reporter - The reporter instance to use or null
     * @param skinProperties - Customization of the browser buttons etc
     */
    public LocationToolbar(
        Composite parent,
        BrowserCore core,
        WorldLoaderManager wlm,
        boolean urlReadOnly,
        boolean showOpenButton,
        boolean showReloadButton,
        String contentDir,
        Properties skinProperties,
        ErrorReporter reporter) {
        
        super( parent, SWT.NONE );
        
        loader = wlm;
        
        browserCore = core;
        if ( browserCore != null ) {
            browserCore.addCoreListener( this );
        }
        contentDir = ( contentDirectory == null ) ?
            System.getProperty( "user.dir" ) : contentDirectory;
        
        errorReporter = ( reporter == null ) ? 
            DefaultErrorReporter.getDefaultReporter( ) : reporter;
        
        Properties skin = ( skinProperties == null ) ?
            DEFAULT_SKIN : skinProperties;
        
        GridLayout gridLayout = new GridLayout( );
        gridLayout.marginHeight = 1;
        gridLayout.marginWidth = 1;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        int columns = 1;
        if ( showOpenButton | showReloadButton ) {
            columns++;
        }
        gridLayout.numColumns = columns;
        super.setLayout( gridLayout );
        
        GridData gridData;
        display = getDisplay( );
        if ( showOpenButton || showReloadButton ) {
            
            int optColumns = 0;
            if ( showOpenButton ) {
                optColumns++;
            }
            if ( showReloadButton ) {
                optColumns++;
            }
            Composite optComposite = new Composite( this, SWT.NONE );
            gridLayout = new GridLayout( );
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 0;
            gridLayout.numColumns = optColumns;
            gridLayout.makeColumnsEqualWidth = true;
            optComposite.setLayout( gridLayout );
            
            if ( showOpenButton ) {
                String img_name = skinProperties.getProperty(
                    OPEN_BUTTON_PROPERTY,
                    DEFAULT_OPEN_BUTTON);
                Image image = ImageLoader.loadImage(display, img_name, reporter);
                
                openButton = new Button( optComposite, SWT.PUSH );
                if ( image != null ) {
                    openButton.setImage( image );
                }
                else {
                    openButton.setText( OPEN_BUTTON_TEXT );
                }
                openButton.setToolTipText( OPEN_BUTTON_TOOLTIP_TEXT );
                gridData = new GridData( );
                gridData.horizontalAlignment = SWT.FILL;
                gridData.verticalAlignment = SWT.FILL;
                openButton.setLayoutData( gridData );
            }
            if ( showReloadButton ) {
                String img_name = skinProperties.getProperty(
                    RELOAD_BUTTON_PROPERTY,
                    DEFAULT_RELOAD_BUTTON);
                Image image = ImageLoader.loadImage(display, img_name, reporter);
                
                reloadButton = new Button( optComposite, SWT.PUSH );
                if ( image != null ) {
                    reloadButton.setImage( image );
                }
                else {
                    reloadButton.setText( RELOAD_BUTTON_TEXT );
                }
                reloadButton.setToolTipText( RELOAD_BUTTON_TOOLTIP_TEXT );
                gridData = new GridData( );
                gridData.horizontalAlignment = SWT.FILL;
                gridData.verticalAlignment = SWT.FILL;
                reloadButton.setLayoutData( gridData );
            }
        }
        Composite locComposite = new Composite( this, SWT.NONE );
        
        gridData = new GridData( );
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        locComposite.setLayoutData( gridData );
        
        gridLayout = new GridLayout( );
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.numColumns = 3;
        locComposite.setLayout( gridLayout );
        
        Label locLabel = new Label( locComposite, SWT.NONE );
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // this is a lot of work just to set the
        // font to bold, must be a better way...
        Font initialFont = locLabel.getFont( );
        FontData[] fontData = initialFont.getFontData( );
        for ( int i = 0; i < fontData.length; i++ ) {
            fontData[i].setStyle( SWT.BOLD );
        }
        Font newFont = new Font( display, fontData );
        locLabel.setFont( newFont );
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        locLabel.setText( LOCATION_LABEL );
        gridData = new GridData( );
        gridData.horizontalIndent = 5;
        locLabel.setLayoutData( gridData );
        
        urlTextField = new Text( locComposite, SWT.SINGLE|SWT.LEFT|SWT.BORDER );
        urlTextField.setText( contentDir );
        urlTextField.setEditable( !urlReadOnly );
        gridData = new GridData( );
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        urlTextField.setLayoutData( gridData );
        
        String img_name = skinProperties.getProperty(
            GO_BUTTON_PROPERTY,
            DEFAULT_GO_BUTTON);
        Image image = ImageLoader.loadImage(display, img_name, reporter);
        
        locationGoButton = new Button( locComposite, SWT.PUSH );
        if ( image != null ) {
            locationGoButton.setImage( image );
        }
        else {
            locationGoButton.setText( GO_BUTTON_TEXT );
        }
        
        locationGoButton.setEnabled( !urlReadOnly );
        locationGoButton.setToolTipText( GO_BUTTON_TOOLTIP_TEXT );
        gridData = new GridData( );
        gridData.verticalAlignment = SWT.FILL;
        locationGoButton.setLayoutData( gridData );
        
        // set up the external event handlers
        if ( !urlReadOnly ) {
            LoadURLAction loadURLAction =
                new LoadURLAction( this, urlTextField );
            
            locationGoButton.addSelectionListener( loadURLAction );
            urlTextField.addSelectionListener( loadURLAction );
        }
        if ( showReloadButton ) {
            ReloadAction reloadAction =
                new ReloadAction( this, urlTextField );
            reloadButton.addSelectionListener( reloadAction );
        }
        if ( showOpenButton ) {
            OpenAction openAction = 
                new OpenAction( parent.getShell( ), this, contentDirectory );
            openButton.addSelectionListener( openAction );
        }
    }
    
    //---------------------------------------------------------
    // Methods defined by Composite
    //---------------------------------------------------------
    
    /** Do nothing, we do our own layout */
    public void setLayout( Layout layout ) {
    }
    
    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------
    
    /**
     * Update the text widget on the display thread.
     */
    public void run( ) {
        urlTextField.setText( urlText );
    }
    
    //---------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------
    
    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized( VRMLScene scene ) {
        String uri = scene.getLoadedURI();
        urlText = ( uri == null ) ? "" : uri;
        display.asyncExec( this );
        errorReporter.messageReport("Main scene: " + urlText + " loaded.");
    }
    
    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
    }
    
    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
    }
    
    /**
     * The browser has been disposed.
     */
    public void browserDisposed() {
    }
    
    //---------------------------------------------------------
    // Methods defined by FileHandler
    //---------------------------------------------------------
    
    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
    
    /**
     * Change the panels content to the provided URL.
     *
     * @param url The URL to load.
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(String url) throws IOException {
        
        String url_str = null;

        // try a file first
        File f = new File(url);
        if(f.exists()) {
            if(f.isDirectory())
                errorReporter.errorReport( FILE_IS_DIRECTORY_MESSAGE, null);
            else {
                url_str = f.toURL().toExternalForm();
            }
        } else {
            // Try a URL
            URL url_obj = new URL(url);
            url_str = url_obj.toExternalForm();
        }

        urlText = ( url_str == null ) ? "" : url_str;
        display.asyncExec( this );
        
        if (throttle != null)
            throttle.startedLoading();

        loader.queueLoadURL(new String[] {url_str}, null);
    }
    
    /**
     * Change the panels content to the provided URL.
     *
     * @param src The source representation to load
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL( InputSource src ) throws IOException {
        loadURL( src.getURL( ) );
    }
    
    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set a progress listener for download progress.  Null will clear.
     *
     * @param listener The progress listener.
     */
    public void setProgressListener(ProgressListener listener) {
        loader.setProgressListener(listener);
    }

    /**
     * Set a Frame throttler.  Null is ok.
     */
    public void setThrottle(FramerateThrottle throttle) {
        this.throttle = throttle;
    }
}
