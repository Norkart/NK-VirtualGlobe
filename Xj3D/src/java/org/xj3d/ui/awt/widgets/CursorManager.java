/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2007
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.util.Properties;
import java.util.Hashtable;
import java.util.HashMap;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.SimpleStack;

import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.Xj3DConstants;

import org.xj3d.core.eventmodel.CursorFilter;

/**
 * Manages cursor icon changes based on state listeners.
 * <p>
 *
 * Cursor definitions are loaded from a Properties object.
 * The properties object is located in the following sequence:
 * <ol>
 * <li>Supplied Properties object if not null</li>
 * <li>Properties loaded from a properties file if it exists </li>
 * <li>A default empty Properties object<\li>
 * </ol>
 *
 * Cursors whose values are not defined in the effective
 * Properties object are given predefined values.  The
 * predefined values not used in the event of error loading
 * user supplied values.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class CursorManager
    implements org.xj3d.core.eventmodel.CursorManager, SensorStatusListener,
        NavigationStateListener, KeyListener {

    /** Message when the constructor contains a null canvas reference */
    private static final String NULL_CANVAS_MSG =
        "The canvas reference cannot be null";

    /** Property to define anchor node cursor */
    private static final String ANCHOR_CURSOR_PROPERTY = "anchor.cursor";

    /** Property to define drag mode cursor */
    private static final String DRAGSENSOR_CURSOR_PROPERTY = "dragSensor.cursor";

    /** Property to define examine mode cursor */
    private static final String EXAMINE_CURSOR_PROPERTY = "EXAMINE.cursor";

    /** Property to define lookat mode cursor */
    private static final String LOOKAT_CURSOR_PROPERTY = "LOOKAT.cursor";

    /** Property to define fly mode cursor */
    private static final String FLY_CURSOR_PROPERTY = "FLY.cursor";

    /** Property to define pan mode cursor */
    private static final String PAN_CURSOR_PROPERTY = "PAN.cursor";

    /** Property to define tilt mode cursor */
    private static final String TILT_CURSOR_PROPERTY = "TILT.cursor";

    /** Property to define active touchsensor cursor */
    private static final String TOUCHSENSOR_CURSOR_PROPERTY = "touchSensor.cursor";

    /** Property to define walk mode cursor */
    private static final String WALK_CURSOR_PROPERTY = "WALK.cursor";

    /** Property to define none mode cursor */
    private static final String NONE_CURSOR_PROPERTY = "NONE.cursor";

    /** Property to define tracking examine mode cursor */
    private static final String TRACK_EXAMINE_CURSOR_PROPERTY = "TEXAMINE.cursor";

    /** Property to define tracking pan mode cursor */
    private static final String TRACK_PAN_CURSOR_PROPERTY = "TPAN.cursor";

    // Default cursor property values

    /** Default image name for anchor mode cursor */
    private static final String DEFAULT_ANCHOR_CURSOR_VALUE =
        "images/navigation/link.gif";

    /** Default image name for drag mode cursor */
    private static final String DEFAULT_DRAGSENSOR_CURSOR_VALUE =
        "images/navigation/drag.gif";

    /** Default image name for examine mode cursor */
    private static final String DEFAULT_EXAMINE_CURSOR_VALUE =
        "images/navigation/CursorExamine.gif";

    /** Default image name for lookat mode cursor */
    private static final String DEFAULT_LOOKAT_CURSOR_VALUE =
        "images/navigation/CursorLookat.gif";

    /** Default image name for fly mode cursor */
    private static final String DEFAULT_FLY_CURSOR_VALUE =
        "images/navigation/CursorFly.gif";

    /** Default image name for pan mode cursor */
    private static final String DEFAULT_PAN_CURSOR_VALUE =
        "images/navigation/CursorPan.gif";

    /** Default image name for tilt cursor mode */
    private static final String DEFAULT_TILT_CURSOR_VALUE =
        "images/navigation/CursorTilt.gif";

    /** Default image name for active touchsensor cursor */
    private static final String DEFAULT_TOUCHSENSOR_CURSOR_VALUE =
        "images/navigation/CursorTarget.gif";

    /** Default image name for walk mode cursor */
    private static final String DEFAULT_WALK_CURSOR_VALUE =
        "images/navigation/CursorWalk.gif";

    /** Default image name for none mode cursor */
    private static final String DEFAULT_NONE_CURSOR_VALUE =
        "images/navigation/CursorNone.gif";

    /** Default image name for tracking examine mode cursor */
    private static final String DEFAULT_TRACK_EXAMINE_CURSOR_VALUE =
        "images/navigation/CursorTExamine.gif";

    /** Default image name for tracking pan mode cursor */
    private static final String DEFAULT_TRACK_PAN_CURSOR_VALUE =
        "images/navigation/CursorTPan.gif";

    /** Default empty Properties object for loading cursors */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Listener for navigation status changes so we can change the cursor */
    private NavigationStateListener navListener;

    /** The canvas that we're changing the cursors on */
    private Component canvas;

    /** The cursor file for an active touchSensors */
    private Cursor tsCursor;

    /** The cursor file for an active dragSensors */
    private Cursor dsCursor;

    /** The cursor file for an active anchors */
    private Cursor anchorCursor;

    /** The last cursor */
    private Cursor navStateCursor;

    /** A stack of sensor cursors */
    private SimpleStack sensorStack;

    /** A table containing cursors, key = name, val = cursor object */
    private Hashtable navCursors;

    /** Currently selected nav Info modes available */
    private String[] navModes;

    /** The current nav mode */
    private String currentNavMode;

    /** Are we in tracking navigation mode */
    private boolean trackNavMode;

    /** Shift key modifier */
    public boolean shiftModifier;

    /** Alt key modifier */
    public boolean altModifier;

    /** Ctrl key modifier */
    public boolean ctrlModifier;

    /** The cursor filter */
    private CursorFilter filter;

    /** A mapping of cursors to urls */
    private HashMap<Cursor, String> cursorUrlMap;

    /** A mapping of urls to cursors */
    private HashMap<String, Cursor> urlCursorMap;

    /** The last cursor set */
    private Cursor lastCursor;

    /**
     * Create a new instance of the cursor manager using the default set
     * of images.
     *
     * @param canvas The canvas to manage cursors for
     * @param reporter The reporter instance to use or null
     * @throws IllegalArgumentException The canvas reference is null
     */
    public CursorManager(Component canvas, ErrorReporter reporter)
        throws IllegalArgumentException {

        this(canvas, DEFAULT_PROPERTIES, reporter);
    }

    /**
     * Create a new instance of the cursor manager with a customised set
     * of images.
     *
     * @param canvas The canvas to manage cursors for
     * @param skinProperties Properties object specifying cursor image names
     * @param reporter The reporter instance to use or null
     * @throws IllegalArgumentException The canvas reference is null
     */
    public CursorManager(Component canvas,
        Properties skinProperties,
        ErrorReporter reporter)
        throws IllegalArgumentException {

        if(canvas == null)
            throw new IllegalArgumentException(NULL_CANVAS_MSG);

        this.canvas = canvas;
        canvas.addKeyListener( this );
        navCursors = new Hashtable();
        navModes = new String[0];
        sensorStack = new SimpleStack();
        navStateCursor = Cursor.getDefaultCursor();
        filter = null;
        cursorUrlMap = new HashMap<Cursor, String>();
        urlCursorMap = new HashMap<String, Cursor>();

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;

        loadCursors(skinProperties);
    }

    //----------------------------------------------------------
    // Methods defined by CursorFilter
    //----------------------------------------------------------

    /**
     * Set the cursor cursor filter.  NULL will disable filtering
     *
     * @param cf The filter
     */
    public void setCursorFilter(CursorFilter cf) {
        filter = cf;
    }

    /**
     * Get the cursor cursor filter.  NULL means no filtering.
     *
     * @return The filter
     */
    public CursorFilter getCursorFilter() {
        return filter;
    }

    /**
     * Set the cursor to the currently specified image.  Normal changes
     * can still occur.  Monitor the cursorFilter for changes.
     *
     * @param url The image to use.  Null returns the cursor to its current state
     * @param x The center x coordinate
     * @param y The center y coordinate
     */
    public void setCursor(String url, int x, int y) {

        if (url == null) {
            canvas.setCursor(lastCursor);
            return;
        }

        Cursor c = urlCursorMap.get(url);

//if (c != null) System.out.println("Cached cursor: " + c.hashCode() + " url: " + newUrl);
        if (c == null) {
            if (url.startsWith("PREDEFINED.")) {
                int pos = url.indexOf(".");
                int predefined = Integer.valueOf(url.substring(pos+1));  // int defined in Cursor
                c = Cursor.getPredefinedCursor(predefined);
            } else {
                Point center = new Point();
                center.x = x;
                center.y = y;

                Image img = IconLoader.loadImage(url, errorReporter);

                MediaTracker mt = new MediaTracker(canvas);
                Toolkit tk = Toolkit.getDefaultToolkit();

                mt.addImage(img, 0);

                try {
                    mt.waitForAll();
                } catch(InterruptedException ie) {
                    // ignore
                }

                if(img != null) {
                    center.x = img.getWidth(null) / 2;
                    center.y = img.getHeight(null) / 2;
//    System.out.println("Got img: " + img + " size: " + img.getWidth(null));

                    c = tk.createCustomCursor(img, center ,null);
//    System.out.println("New cursor: " + c.hashCode());
                    cursorUrlMap.put(c, url);
                    urlCursorMap.put(url, c);
                } else {
                    errorReporter.warningReport("Unable to load cursor image: " + url, null);
                }
            }
//System.out.println("Final cursor: " + c.hashCode());
            canvas.setCursor(c);
        }
    }

    //----------------------------------------------------------
    // Methods defined by SensorStatusListener
    //----------------------------------------------------------

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    public void deviceOver(int type, String desc) {
        Cursor cursor = null;
        switch(type) {
        case SensorStatusListener.TYPE_ANCHOR:
            cursor = anchorCursor;
            break;
        case SensorStatusListener.TYPE_TOUCH_SENSOR:
            cursor = tsCursor;
            break;
        case SensorStatusListener.TYPE_DRAG_SENSOR:
            cursor = dsCursor;
            break;
        }

        sensorStack.push(cursor);

        if (!(trackNavMode && shiftModifier)) {
            setCursor(cursor);
        }
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    public void deviceNotOver(int type) {
        if(!sensorStack.isEmpty()) {
            sensorStack.pop();
        }

        if (!(trackNavMode && shiftModifier)) {
            if(!sensorStack.isEmpty()) {
                setCursor((Cursor)sensorStack.peek());
            } else {
                setCursor(navStateCursor);
            }
        }
    }

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    public void deviceActivated(int type) {
    }

    /**
     * Invoked when a tracker follows a link.
     *
     * @param url The url to load.
     */
    public void linkActivated(String[] url) {
    }

    //----------------------------------------------------------
    // Methods defined by NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx) {

        currentNavMode = navModes[idx];
        trackNavMode = Xj3DConstants.TRACK_EXAMINE_NAV_MODE.equals(currentNavMode) ||
            Xj3DConstants.TRACK_PAN_NAV_MODE.equals(currentNavMode);

        Cursor cursor = (Cursor) navCursors.get(navModes[idx]);

        if(cursor == null) {
            // Load the cursor
            System.out.println("Cursor node not found");
        } else {
            navStateCursor = cursor;
            setCursor(cursor);
        }
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numModes The number of modes in the list
     */
    public void navigationListChanged(String[] modes, int numModes) {
        if(navModes.length!=numModes)
            navModes = new String[numModes];

        System.arraycopy(modes, 0, navModes, 0, numModes);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    /**
     * Process a key press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyPressed(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        switch(keyCode){
        case KeyEvent.VK_ALT:
            altModifier = true;
            if (trackNavMode) {
                navStateCursor = (Cursor)navCursors.get(Xj3DConstants.TRACK_PAN_NAV_MODE);
                boolean sensorIsActive = !sensorStack.isEmpty();
                if (!sensorIsActive || (sensorIsActive && shiftModifier)) {
                    setCursor(navStateCursor);
                }
            }
            break;
        case KeyEvent.VK_CONTROL:
            ctrlModifier = true;
            if (trackNavMode) {
                navStateCursor = (Cursor)navCursors.get(Xj3DConstants.FLY_NAV_MODE);
                boolean sensorIsActive = !sensorStack.isEmpty();
                if (!sensorIsActive || (sensorIsActive && shiftModifier)) {
                    setCursor(navStateCursor);
                }
            }
            break;
        case KeyEvent.VK_SHIFT:
            shiftModifier = true;
            if (trackNavMode && !sensorStack.isEmpty()) {
                setCursor(navStateCursor);
            }
            break;
        }
    }

    /**
     * Process a key release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyReleased(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        switch(keyCode){
        case KeyEvent.VK_ALT:
            altModifier = false;
            if(trackNavMode) {
                navStateCursor = (Cursor)navCursors.get(Xj3DConstants.TRACK_EXAMINE_NAV_MODE);
                boolean sensorIsActive = !sensorStack.isEmpty();
                if (!sensorIsActive || (sensorIsActive && shiftModifier)) {
                    setCursor(navStateCursor);
                }
            }
            break;
        case KeyEvent.VK_CONTROL:
            ctrlModifier = false;
            if(trackNavMode) {
                navStateCursor = (Cursor)navCursors.get(Xj3DConstants.TRACK_EXAMINE_NAV_MODE);
                boolean sensorIsActive = !sensorStack.isEmpty();
                if (!sensorIsActive || (sensorIsActive && shiftModifier)) {
                    setCursor(navStateCursor);
                }
            }
            break;
        case KeyEvent.VK_SHIFT:
            shiftModifier = false;
            if(trackNavMode && !sensorStack.isEmpty()) {
                setCursor((Cursor)sensorStack.peek());
            }
            break;
        }
    }

    /**
     * Process a key click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyTyped(KeyEvent evt) {
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Private convenience method to load cursors for use by this handler
     * @param cursorProperties Definitions for cursor overrides
     */
    private void loadCursors(Properties cursorProperties) {
        String tsSensor = cursorProperties.getProperty(
            TOUCHSENSOR_CURSOR_PROPERTY,
            DEFAULT_TOUCHSENSOR_CURSOR_VALUE
            );

        String dsSensor = cursorProperties.getProperty(
            DRAGSENSOR_CURSOR_PROPERTY,
            DEFAULT_DRAGSENSOR_CURSOR_VALUE
            );

        String anchor = cursorProperties.getProperty(
            ANCHOR_CURSOR_PROPERTY,
            DEFAULT_ANCHOR_CURSOR_VALUE
            );

        // Load required navigation cursors
        String walk = cursorProperties.getProperty(
            WALK_CURSOR_PROPERTY,
            DEFAULT_WALK_CURSOR_VALUE
            );

        String fly = cursorProperties.getProperty(
            FLY_CURSOR_PROPERTY,
            DEFAULT_FLY_CURSOR_VALUE
            );

        String pan = cursorProperties.getProperty(
            PAN_CURSOR_PROPERTY,
            DEFAULT_PAN_CURSOR_VALUE
            );

        String tilt = cursorProperties.getProperty(
            TILT_CURSOR_PROPERTY,
            DEFAULT_TILT_CURSOR_VALUE
            );

        String examine = cursorProperties.getProperty(
            EXAMINE_CURSOR_PROPERTY,
            DEFAULT_EXAMINE_CURSOR_VALUE
            );

        String lookat = cursorProperties.getProperty(
            LOOKAT_CURSOR_PROPERTY,
            DEFAULT_LOOKAT_CURSOR_VALUE
            );

        String none = cursorProperties.getProperty(
            NONE_CURSOR_PROPERTY,
            DEFAULT_NONE_CURSOR_VALUE
            );

        String texamine = cursorProperties.getProperty(
            TRACK_EXAMINE_CURSOR_PROPERTY,
            DEFAULT_TRACK_EXAMINE_CURSOR_VALUE
            );

        String tpan = cursorProperties.getProperty(
            TRACK_PAN_CURSOR_PROPERTY,
            DEFAULT_TRACK_PAN_CURSOR_VALUE
            );

        Image img1 = IconLoader.loadImage(tsSensor, errorReporter);
        Image img2 = IconLoader.loadImage(dsSensor, errorReporter);
        Image img3 = IconLoader.loadImage(anchor, errorReporter);
        Image img4 = IconLoader.loadImage(walk, errorReporter);
        Image img5 = IconLoader.loadImage(fly, errorReporter);
        Image img6 = IconLoader.loadImage(examine, errorReporter);
        Image img7 = IconLoader.loadImage(pan, errorReporter);
        Image img8 = IconLoader.loadImage(tilt, errorReporter);
        Image img9 = IconLoader.loadImage(lookat, errorReporter);
        Image img10 = IconLoader.loadImage(none, errorReporter);
        Image img11 = IconLoader.loadImage(texamine, errorReporter);
        Image img12 = IconLoader.loadImage(tpan, errorReporter);

        MediaTracker mt = new MediaTracker(canvas);
        Toolkit tk = Toolkit.getDefaultToolkit();

        mt.addImage(img1, 0);
        mt.addImage(img2, 0);
        mt.addImage(img3, 0);
        mt.addImage(img4, 0);
        mt.addImage(img5, 0);
        mt.addImage(img6, 0);
        mt.addImage(img7, 0);
        mt.addImage(img8, 0);
        mt.addImage(img9, 0);
        mt.addImage(img10,0);
        mt.addImage(img11,0);
        mt.addImage(img12,0);

        try {
            mt.waitForAll();
        } catch(InterruptedException ie) {
            // ignore
        }

        Point center = new Point();

        if(img1 != null) {
            center.x = img1.getWidth(null) / 2;
            center.y = img1.getHeight(null) / 2;

            tsCursor = tk.createCustomCursor(img1, center ,null);
            cursorUrlMap.put(tsCursor, tsSensor);
            urlCursorMap.put(tsSensor, tsCursor);
        } else {
            errorReporter.warningReport("Unable to load touchSensor cursor image: " + tsSensor, null);
            tsCursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
        }

        if(img2 != null) {
            center.x = img2.getWidth(null) / 2;
            center.y = img2.getHeight(null) / 2;

            dsCursor = tk.createCustomCursor(img2, center ,null);
            cursorUrlMap.put(dsCursor, dsSensor);
            urlCursorMap.put(dsSensor, dsCursor);
        } else {
            errorReporter.warningReport("Unable to load dragSensor cursor image: " + dsSensor, null);
            dsCursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
        }

        if(img3 != null) {
            center.x = img3.getWidth(null) / 2;
            center.y = img3.getHeight(null) / 2;

            anchorCursor = tk.createCustomCursor(img3, center ,null);
            cursorUrlMap.put(anchorCursor, anchor);
            urlCursorMap.put(anchor, anchorCursor);
        } else {
            errorReporter.warningReport("Unable to load anchor cursor image: " + anchor, null);
            anchorCursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
        }

        if(img4 != null) {
            center.x = img4.getWidth(null) / 2;
            center.y = img4.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img4, center ,null);
            navCursors.put(Xj3DConstants.WALK_NAV_MODE, c);
            cursorUrlMap.put(c, walk);
            urlCursorMap.put(walk, c);
        } else {
            errorReporter.warningReport("Unable to load WALK cursor image: " + walk, null);
            navCursors.put(Xj3DConstants.WALK_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img5 != null) {
            center.x = img5.getWidth(null) / 2;
            center.y = img5.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img5, center ,null);
            navCursors.put(Xj3DConstants.FLY_NAV_MODE, c);

            cursorUrlMap.put(c, fly);
            urlCursorMap.put(fly, c);
        } else {
            errorReporter.warningReport("Unable to load FLY cursor image: " + fly, null);
            navCursors.put(Xj3DConstants.FLY_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img6 != null) {
            center.x = img6.getWidth(null) / 2;
            center.y = img6.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img6, center ,null);
            navCursors.put(Xj3DConstants.EXAMINE_NAV_MODE,c);
            cursorUrlMap.put(c, examine);
            urlCursorMap.put(examine, c);
        } else {
            errorReporter.warningReport("Unable to load EXAMINE cursor image: " + examine, null);
            navCursors.put(Xj3DConstants.EXAMINE_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img7 != null) {
            center.x = img7.getWidth(null) / 2;
            center.y = img7.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img7, center ,null);
            navCursors.put(Xj3DConstants.PAN_NAV_MODE,c);
            cursorUrlMap.put(c, pan);
            urlCursorMap.put(pan, c);
        } else {
            errorReporter.warningReport("Unable to load xj3d_pan cursor image: " + pan, null);
            navCursors.put(Xj3DConstants.PAN_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img8 != null) {
            center.x = img8.getWidth(null) / 2;
            center.y = img8.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img8, center ,null);

            navCursors.put(Xj3DConstants.TILT_NAV_MODE,c);
            cursorUrlMap.put(c, tilt);
            urlCursorMap.put(tilt, c);
        } else {
            errorReporter.warningReport("Unable to load xj3d_tilt cursor image: " + tilt, null);
            navCursors.put(Xj3DConstants.TILT_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img9 != null) {
            center.x = img9.getWidth(null) / 2;
            center.y = img9.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img9, center ,null);

            navCursors.put(Xj3DConstants.LOOKAT_NAV_MODE,c);
            cursorUrlMap.put(c, lookat);
            urlCursorMap.put(lookat, c);
        } else {
            errorReporter.warningReport("Unable to load LOOKAT cursor image: " + lookat, null);
            navCursors.put(Xj3DConstants.LOOKAT_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img10 != null) {
            center.x = img10.getWidth(null) / 2;
            center.y = img10.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img10, center ,null);

            navCursors.put(Xj3DConstants.NONE_NAV_MODE,c);
            cursorUrlMap.put(c, none);
            urlCursorMap.put(none, c);
        } else {
            errorReporter.warningReport("Unable to load NONE cursor image: " + none, null);
            navCursors.put(Xj3DConstants.NONE_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img11 != null) {
            center.x = img11.getWidth(null) / 2;
            center.y = img11.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img11, center ,null);

            navCursors.put(Xj3DConstants.TRACK_EXAMINE_NAV_MODE,c);
            cursorUrlMap.put(c, texamine);
            urlCursorMap.put(texamine, c);
        } else {
            errorReporter.warningReport("Unable to load TRACK EXAMINE cursor image: " + texamine, null);
            navCursors.put(Xj3DConstants.TRACK_EXAMINE_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }

        if(img12 != null) {
            center.x = img12.getWidth(null) / 2;
            center.y = img12.getHeight(null) / 2;

            Cursor c = tk.createCustomCursor(img12, center ,null);

            navCursors.put(Xj3DConstants.TRACK_PAN_NAV_MODE,c);
            cursorUrlMap.put(c, tpan);
            urlCursorMap.put(tpan, c);
        } else {
            errorReporter.warningReport("Unable to load TRACK PAN cursor image: " + tpan, null);
            navCursors.put(Xj3DConstants.TRACK_PAN_NAV_MODE,
                Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
        }
    }

    /**
     * Set the new cursor.  This will handle cursor filter overrides and then
     * set the underlying canvas.
     *
     * @param cursor The new cursor
     */
    private void setCursor(Cursor cursor) {
        lastCursor = cursor;

        if (filter == null) {
            canvas.setCursor(cursor);
            return;
        }

        String url = cursorUrlMap.get(cursor);
        String newUrl = filter.cursorChanged(url);
        if (url.equals(newUrl)) {
            canvas.setCursor(cursor);
            return;
        }

        Cursor c = urlCursorMap.get(newUrl);

//if (c != null) System.out.println("Cached cursor: " + c.hashCode() + " url: " + newUrl);
        if (c == null) {
            if (newUrl.startsWith("PREDEFINED.")) {
                int pos = newUrl.indexOf(".");
                int predefined = Integer.valueOf(newUrl.substring(pos+1));  // int defined in Cursor
                c = Cursor.getPredefinedCursor(predefined);
            } else {
                Point center = new Point();

                Image img = IconLoader.loadImage(url, errorReporter);

                MediaTracker mt = new MediaTracker(canvas);
                Toolkit tk = Toolkit.getDefaultToolkit();

                mt.addImage(img, 0);

                try {
                    mt.waitForAll();
                } catch(InterruptedException ie) {
                    // ignore
                }

                if(img != null) {
                    center.x = img.getWidth(null) / 2;
                    center.y = img.getHeight(null) / 2;
//    System.out.println("Got img: " + img + " size: " + img.getWidth(null));

                    c = tk.createCustomCursor(img, center ,null);
//    System.out.println("New cursor: " + c.hashCode());
                    cursorUrlMap.put(c, newUrl);
                    urlCursorMap.put(newUrl, c);
                } else {
                    errorReporter.warningReport("Unable to load cursor image: " + newUrl, null);
                    c = cursor;
                }
            }
//System.out.println("Final cursor: " + c.hashCode());
            canvas.setCursor(c);
        }
    }
}
