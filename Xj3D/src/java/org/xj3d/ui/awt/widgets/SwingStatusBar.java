/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.util.Properties;

import javax.swing.*;

import org.ietf.uri.ResourceConnection;

import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

/**
 * A swing panel that implements a simple status bar capability with a
 * text readout and frames per second counter.
 * <p>
 *
 * A status bar automatically registeres a global
 * {@link SwingProgressListener}, so there is no need to create your own in
 * your application.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class SwingStatusBar extends JPanel
    implements Runnable, BrowserCoreListener {

    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The label for status messages */
    private JLabel statusLabel;

    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;

    /** Label for frames per second. */
    private JLabel fpsLabel;

    /** A progress bar for main file loading */
    private JProgressBar progressBar;

    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;

    /** The progress listener */
    private SwingProgressListener dlListener;

    /** The periodic thread updating the status bar. */
    private Thread statusThread;

    /** The run state of the statusThread */
    private boolean runStatusThread;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param core The browser core implementation to send nav changes to
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public SwingStatusBar(BrowserCore core,
        boolean showStatusBar,
        boolean showFPS,
        Properties skinProperties,
        ErrorReporter reporter) {
        super(new BorderLayout());

        browserCore = core;
        browserCore.addCoreListener( this );

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;


        JPanel rightPanel = new JPanel(new BorderLayout());

        add(rightPanel, BorderLayout.EAST);

        if(showFPS) {
            fpsLabel = new JLabel();
            rightPanel.add(fpsLabel, BorderLayout.EAST);
            statusThread = new Thread(this, "Xj3D FPS updates");
            runStatusThread = true;
            statusThread.start();
        }

        if(showStatusBar) {
            statusLabel = new JLabel();
            add(statusLabel, BorderLayout.WEST);

            progressBar = new JProgressBar();

            dlListener =
                new SwingProgressListener(statusLabel, progressBar, rightPanel, BorderLayout.WEST, reporter);

            ResourceConnection.addGlobalProgressListener(dlListener);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------------

    /**
     * Ignored. The browser has been initialised with new content.
     *
     * @param scene The scene of new content
     */
    public void browserInitialized( VRMLScene scene ) {
    }

    /**
     * Ignored. The Browser tried to load a URL and failed.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed( String msg ) {
    }

    /**
     * Ignored. The browser has been shut down and the previous content
     * is no longer valid.
     */
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed, release the progress listener and
     * stop the fps status thread if necessary.
     */
    public void browserDisposed( ) {
        if ( dlListener != null ) {
            ResourceConnection.removeGlobalProgressListener( dlListener );
        }
        if ( statusThread != null ) {
            runStatusThread = false;
            statusThread.interrupt( );
        }
    }

    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------

    /**
     * Thread to update frames per second and status bar.
     */
    public void run() {
        while(runStatusThread) {
            try {
                Thread.sleep(500);
            } catch(Exception e) {
            }

            float fps = browserCore.getCurrentFrameRate();

            if(Math.abs(lastFPS - fps) > 0.1) {

                // TODO: Need todo this in a non-garbage generating way
                String txt = Float.toString(fps);
                if (txt.equals("Infinity")) {
                    lastFPS = 999.9f;
                    txt = "999.9";
                }

                int len = txt.length();

                txt = txt.substring(0, Math.min(5,len));
                if (len < 5) {
                    len = 5 - len;
                    for(int i=0; i < len; i++) {
                        txt += " ";
                    }
                }

                fpsLabel.setText(txt);
                lastFPS = fps;
            }
        }
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Return the progress listener for download progress.
     */
    public ProgressListener getProgressListener() {
        return dlListener;
    }

    /**
     * Update the status bar text message to say this.
     *
     * @param msg The message to display
     */
    public void setStatusText(String msg) {
        if(statusLabel != null)
            statusLabel.setText(msg);
    }
}
