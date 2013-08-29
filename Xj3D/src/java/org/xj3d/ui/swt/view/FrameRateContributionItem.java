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

package org.xj3d.ui.swt.view;

//External imports
import java.util.Timer;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.CLabel;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

//Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;

import org.web3d.vrml.nodes.VRMLScene;

import org.xj3d.ui.swt.widgets.Alarm;
import org.xj3d.ui.swt.widgets.AlarmListener;
import org.xj3d.ui.swt.widgets.AlarmEvent;

/**
 * An implementation of a contribution item for the status bar. This item
 * consists of a label that displays the browser's current frame rate.
 * 
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class FrameRateContributionItem extends ContributionItem implements
        BrowserCoreListener, DisposeListener, AlarmListener, Runnable {

    /** Update period for the fps timer */
    private static final int UPDATE_PERIOD_MILLIS = 500;

    /** FPS Tooltip text */
    private static final String FPS_TOOLTIP_TEXT = "Frames Per Second";

    /** Prototype fpsString */
    private static final String PROTO = "00000.00000";

    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;

    /** The timer task generating our periodic events to update 
     *  the status thread */
    private Alarm alarm;

    /** Timer for driving the fps display */
    private Timer fpsTimer;

    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;

    /** Label for frames per second. */
    private CLabel fpsLabel;

    /** Sidepocket of the fpe field text - for the display thread update */
    private String fpsString;

    /** Display instance, used to put updates on the display thread */
    private Display display;

    /**
     * Constructor
     * @param id - The identifier for this ContributionItem.
     * @param core - The BrowserCore, source of our frame rate data.
     */
    public FrameRateContributionItem(String id, BrowserCore core) {
        super(id);
        browserCore = core;
        browserCore.addCoreListener(this);

        fpsTimer = new Timer();
        alarm = new Alarm();
        alarm.addAlarmListener(this);
        fpsTimer.schedule(alarm, UPDATE_PERIOD_MILLIS, UPDATE_PERIOD_MILLIS);
    }

    /**
     * Construct the item.
     *
     * @param parent - The parent composite that this item will be added to,
     * presumably the status bar.
     */
    public void fill(Composite parent) {

        display = parent.getDisplay();

        Label separator = new Label( parent, SWT.SEPARATOR );
        fpsLabel = new CLabel(parent, SWT.SHADOW_NONE);

        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        FontMetrics fm = gc.getFontMetrics();
        Point ext2 = gc.textExtent(PROTO);
        int widthHint = ext2.x;
        int heightHint = fm.getHeight();
        gc.dispose();

        StatusLineLayoutData statusLineLayoutData = new StatusLineLayoutData();
        statusLineLayoutData.widthHint = widthHint;
        statusLineLayoutData.heightHint = heightHint;
        fpsLabel.setLayoutData(statusLineLayoutData);
        
        statusLineLayoutData = new StatusLineLayoutData();
        statusLineLayoutData.heightHint = heightHint;
        separator.setLayoutData(statusLineLayoutData);

        fpsLabel.setToolTipText(FPS_TOOLTIP_TEXT);

        fpsTimer = new Timer();
        alarm = new Alarm();
        alarm.addAlarmListener(this);
        fpsTimer.schedule(alarm, UPDATE_PERIOD_MILLIS, UPDATE_PERIOD_MILLIS);
    }

    // ---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    // ---------------------------------------------------------------

    /**
     * The browser has been initialized with new content. The content given is
     * found in the accompanying scene and description.
     * 
     * @param scene - The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
    }

    /**
     * The tried to load a URL and failed. It is typically because none of the
     * URLs resolved to anything valid or there were network failures.
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
     */
    public void browserDisposed() {
        deleteTimer( );
    }

    //----------------------------------------------------------
    // Methods defined by DisposeListener
    //----------------------------------------------------------
    
    /**
     * We're being disposed of - clean up
     */
    public void widgetDisposed( DisposeEvent evt ) {
        deleteTimer( );
    }
    
    // ----------------------------------------------------------
    // Methods defined by AlarmListener
    // ----------------------------------------------------------

    /**
     * Invoked when the alarm has expired. Update the frame rate display if
     * necessary.
     * 
     * @param ae - The event that caused the alarm
     */
    public void alarmAction(AlarmEvent ae) {
        float fps = browserCore.getCurrentFrameRate();
        if (!Float.isInfinite(fps)) {
            if (Math.abs(lastFPS - fps) > 0.01) {
                int integer = (int) fps;
                fps -= integer;
                int decimal = (int) (fps * 100000);
                // note: this does not i18n, need to get the decimal
                // separator from DecimalSymbols which needs a locale...
                fpsString = integer + "." + decimal;
            }
        } else {
            fpsString = "            ";
        }
        if ((fpsTimer != null) && !display.isDisposed()) {
            display.asyncExec(this);
        }
        lastFPS = fps;
    }

    // ---------------------------------------------------------
    // Methods defined by Runnable
    // ---------------------------------------------------------

    /**
     * Method for the display thread to update frames per second and status bar
     * text.
     */
    public void run() {
        if ((fpsLabel != null) && !fpsLabel.isDisposed()) {
            fpsLabel.setText(fpsString);
        }
    }

    // ---------------------------------------------------------
    // Local Methods 
    // ---------------------------------------------------------

    /**
     * Delete the timers and alarms
     */
    private void deleteTimer( ) {
        if ( alarm != null ) {
            alarm.removeAlarmListener(this);
            alarm.cancel( );
            alarm = null;
        }
        if (fpsTimer != null) {
            fpsTimer.cancel();
            fpsTimer = null;
        }
    }
}
