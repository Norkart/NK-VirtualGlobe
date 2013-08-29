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
import java.util.Properties;
import java.util.Timer;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

/**
 * A Composite that implements a simple status bar capability with a
 * text readout and frames per second counter.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class StatusBar extends Composite implements 
    Runnable, AlarmListener, StatusReporter, BrowserCoreListener {
    
    /** Default properties object */
    //private static final Properties DEFAULT_PROPERTIES = new Properties();
    
    /** Update period for the fps timer */
    private static final int UPDATE_PERIOD_MILLIS = 500;
    
    /** The label for status messages */
    private Text statusLabel;
    
    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;
    
    /** Label for frames per second. */
    private Text fpsLabel;
    
    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;
    
    /** Flag indicating that the status field requires updating */
    private boolean statusUpdate;
    
    /** Sidepocket of the status field text - for the display thread update */
    private String statusString;
    
    /** The timer task generating our periodic events to update the status
     *  thread */
    private Alarm alarm;
    
    /** Timer for driving the fps display */
    private Timer fpsTimer;
    
    /** Flag indicating that the fps field requires updating */
    private boolean fpsUpdate;
    
    /** Sidepocket of the fps field text - for the display thread update */
    private String fpsString;
    
    /** Display instance, used to put updates on the display thread */
    private Display display;
    
    /**
     * Create an instance of the status bar configured to show or hide the
     * controls as described.
     *
     * @param parent - The SWT Composite widget that this will be added to
     * @param core - The browser core implementation, source of frame rate data
     * @param showStatusBar - true to show a status bar
     * @param showFPS - true to show the current FPS
     * @param skinProperties - The properties object specifying any image names
     */
    public StatusBar(
        Composite parent,
        BrowserCore core,
        boolean showStatusBar,
        boolean showFPS,
        Properties skinProperties ) {
        
        super( parent, SWT.NONE );
        GridLayout layout = new GridLayout( );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.numColumns = 2;
        super.setLayout( layout );
        
        browserCore = core;
        browserCore.addCoreListener( this );
        
        display = getDisplay( );
        
        statusLabel = new Text( this, SWT.HORIZONTAL | SWT.SINGLE | SWT.READ_ONLY | SWT.LEFT );
        GridData gridData = new GridData( );
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        statusLabel.setLayoutData( gridData );
        
        fpsLabel = new Text( this, SWT.HORIZONTAL | SWT.SINGLE | SWT.READ_ONLY | SWT.RIGHT );
        gridData = new GridData( );
        gridData.horizontalAlignment = SWT.FILL;
        fpsLabel.setLayoutData( gridData );
        if( showFPS ) {
            fpsTimer = new Timer( );
            alarm = new Alarm( );
            alarm.addAlarmListener( this );
            fpsTimer.schedule( 
                alarm, 
                UPDATE_PERIOD_MILLIS, 
                UPDATE_PERIOD_MILLIS );
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg - An error message to go with the failure
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
     * The browser has been disposed, all resources may be freed.
     * Dispose of our periodic timers.
     */
    public void browserDisposed() {
        if ( alarm != null ) {
            alarm.cancel( );
            alarm.removeAlarmListener( this );
            alarm = null;
        }
        if ( fpsTimer != null ) {
            fpsTimer.cancel( );
            fpsTimer = null;
        }
    }

    //---------------------------------------------------------
    // Methods defined by Composite
    //---------------------------------------------------------
    
    /** Override - Do nothing, we do our own layout. */
    public void setLayout( Layout layout ) {
    }
    
    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------
    
    /**
     * Method for the display thread to update frames per second 
     * and status bar text.
     */
    public void run( ) {
        if ( fpsUpdate && !fpsLabel.isDisposed( ) ) {
            fpsLabel.setText( fpsString );
            fpsUpdate = false;
        }
        if ( statusUpdate && !statusLabel.isDisposed( ) ) {
            statusLabel.setText( statusString );
            statusUpdate = false;
        }
    }
    
    //----------------------------------------------------------
    // Methods defined by AlarmListener
    //----------------------------------------------------------
    
    /** 
     * Invoked when the alarm has expired. Update the frame rate
     * display if necessary.
     * 
     * @param ae - The event that caused the alarm
     */
    public void alarmAction( AlarmEvent ae ) {
        float fps = browserCore.getCurrentFrameRate( );
        if ( !Float.isInfinite( fps ) ) { 
            if( Math.abs( lastFPS - fps ) > 0.01 ) {
                int integer = (int)fps;
                fps -= integer;
                int decimal = (int)( fps * 100000 );
                // note: this does not i18n, need to get the decimal
                // separator from DecimalSymbols which needs a locale...
                fpsString = integer +"."+ decimal;
                fpsUpdate = true;
                if ( !display.isDisposed( ) ) {
                    display.asyncExec( this );
                }
                lastFPS = fps;
            }
        }
    }
    
    //---------------------------------------------------------
    // Methods defined by StatusReporter
    //---------------------------------------------------------
    
    /**
     * Update the status bar text message to say this.
     *
     * @param msg - The message to display
     */
    public void setStatusText( String msg ) {
        if ( ( statusLabel != null ) && ( msg != null ) ) {
            if ( msg != null ) {
                statusString = msg;
            } else { 
                // note: widgets take Exception to null strings...
                statusString = ""; 
            }
            statusUpdate = true;
            display.asyncExec( this );
        }
    }
}
