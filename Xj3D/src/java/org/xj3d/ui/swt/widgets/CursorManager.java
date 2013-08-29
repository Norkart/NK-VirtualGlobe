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
import java.io.*;

import java.net.URL;

import java.util.HashMap;
import java.util.Properties;

import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

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
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class CursorManager implements org.xj3d.core.eventmodel.CursorManager,
    SensorStatusListener, NavigationStateListener, Runnable {

    /** Message when the constructor contains a null canvas reference */
    private static final String NULL_CANVAS_MSG =
        "The canvas reference cannot be null";

    /** Cursor node not found error message */
    private static final String CURSOR_NOT_FOUND_ERR_MSG =
        "Cursor node not found";

    /** Cursor image load failure message */
    private static final String CURSOR_IMAGE_LOAD_FAILURE_MSG =
        "Unable to load cursor image: ";

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

    /** Index of the anchor cursor data in the sensor cursors array */
    private static int ANCHOR_CURSOR_INDEX = 0;

    /** Index of the dragsensor cursor data in the sensor cursors array */
    private static int DRAGSENSOR_CURSOR_INDEX = 1;

    /** Index of the touchsensor cursor data in the sensor cursors array */
    private static int TOUCHSENSOR_CURSOR_INDEX = 2;

    /** Index of the property name in the data arrays */
    private static int PROPERTY = 0;

    /** Index of the default property value in the data arrays */
    private static int VALUE = 1;

    /** Array of property name / default values for sensor cursors */
    private static String[][] SENSOR_DATA = {
        { ANCHOR_CURSOR_PROPERTY, DEFAULT_ANCHOR_CURSOR_VALUE },
        { DRAGSENSOR_CURSOR_PROPERTY, DEFAULT_DRAGSENSOR_CURSOR_VALUE },
        { TOUCHSENSOR_CURSOR_PROPERTY, DEFAULT_TOUCHSENSOR_CURSOR_VALUE },
    };

    /** Array of property name / default values for navigation cursors */
    private static String[][] NAVIGATION_DATA = {
        { WALK_CURSOR_PROPERTY, DEFAULT_WALK_CURSOR_VALUE },
        { FLY_CURSOR_PROPERTY, DEFAULT_FLY_CURSOR_VALUE },
        { EXAMINE_CURSOR_PROPERTY, DEFAULT_EXAMINE_CURSOR_VALUE },
        { PAN_CURSOR_PROPERTY, DEFAULT_PAN_CURSOR_VALUE },
        { TILT_CURSOR_PROPERTY, DEFAULT_TILT_CURSOR_VALUE },
        { LOOKAT_CURSOR_PROPERTY, DEFAULT_LOOKAT_CURSOR_VALUE },
        { NONE_CURSOR_PROPERTY, DEFAULT_NONE_CURSOR_VALUE },
    };

    /** Navigation mode identifiers - in matching sequence to navigation
     *  cursor property array */
    private static String[] NAV_MODE = new String[] {
        Xj3DConstants.WALK_NAV_MODE,
        Xj3DConstants.FLY_NAV_MODE,
        Xj3DConstants.EXAMINE_NAV_MODE,
        Xj3DConstants.PAN_NAV_MODE,
        Xj3DConstants.TILT_NAV_MODE,
        Xj3DConstants.LOOKAT_NAV_MODE,
        Xj3DConstants.NONE_NAV_MODE,
    };

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Listener for navigation status changes so we can change the cursor */
    private NavigationStateListener navListener;

    /** The canvas that we're managing the cursors for */
    private Canvas canvas;

    /** The last navigation cursor loaded */
    private Cursor navStateCursor;

    /** The default cursor */
    private Cursor defaultCursor;

    /** The curent active cursor */
    private Cursor activeCursor;

    /** The display object */
    private Display display;

    /** A stack of sensor cursors */
    private SimpleStack sensorStack;

    /** Array of sensor cursors */
    private Cursor[] sensorCursors;

    /** A map containing navigation cursors,
     *  key = navigation mode constant, val = cursor object */
    private HashMap navCursors;

    /** Currently selected nav Info modes available */
    private String[] navModes;

    /** The cursor filter */
    private CursorFilter filter;

    /**
     * Create a new instance of the cursor manager using the default set
     * of images.
     *
     * @param canvas - The canvas to manage cursors for
     * @param reporter - The reporter instance to use or null
     * @throws IllegalArgumentException The canvas reference is null
     */
    public CursorManager( Canvas canvas, ErrorReporter reporter )
        throws IllegalArgumentException {

        this( canvas, null, reporter );
    }

    /**
     * Create a new instance of the cursor manager with a customised set
     * of images.
     *
     * @param canvas - The canvas to manage cursors for
     * @param skinProperties - The properties object specifying cursor image names
     * @param reporter - The reporter instance to use or null
     * @throws IllegalArgumentException The canvas reference is null
     */
    public CursorManager( Canvas canvas,
        Properties skinProperties,
        ErrorReporter reporter )
        throws IllegalArgumentException {

        if ( canvas == null ) {
            throw new IllegalArgumentException( NULL_CANVAS_MSG );
        }

        this.canvas = canvas;
        display = canvas.getDisplay( );
        sensorCursors = new Cursor[SENSOR_DATA.length];
        navCursors = new HashMap( );
        navModes = new String[0];
        sensorStack = new SimpleStack( );
        defaultCursor = new Cursor( display, SWT.CURSOR_CROSS );
        navStateCursor = defaultCursor;

        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;

        Properties skin = ( skinProperties == null ) ?
            new Properties( ) : skinProperties;

        loadCursors( skin );
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

        System.out.println("Filtering not implemented yet in SWT CursorManager");
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
     * @param url The image to use
     * @param x The center x coordinate
     * @param y The center y coordinate
     */
    public void setCursor(String url, int x, int y) {
        System.out.println("SetCursor not implemented in SWT CursorManager");
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
    public void deviceOver( int type, String desc ) {
        switch( type ) {
        case SensorStatusListener.TYPE_ANCHOR:
            Cursor cursor = sensorCursors[ANCHOR_CURSOR_INDEX];
            sensorStack.push( cursor );
            activeCursor = cursor;
            break;
        case SensorStatusListener.TYPE_TOUCH_SENSOR:
            cursor = sensorCursors[TOUCHSENSOR_CURSOR_INDEX];
            sensorStack.push( cursor );
            activeCursor = cursor;
            break;
        case SensorStatusListener.TYPE_DRAG_SENSOR:
            cursor = sensorCursors[DRAGSENSOR_CURSOR_INDEX];
            sensorStack.push( cursor );
            activeCursor = cursor;
            break;
        }
        display.asyncExec( this );
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    public void deviceNotOver( int type ) {
        sensorStack.pop( );

        if( !sensorStack.isEmpty( ) ) {
            activeCursor = (Cursor)sensorStack.peek( );
        } else {
            activeCursor = navStateCursor;
        }
        display.asyncExec( this );
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
        Cursor cursor = (Cursor) navCursors.get(navModes[idx]);

        if( cursor == null ) {
            errorReporter.warningReport( CURSOR_NOT_FOUND_ERR_MSG, null );
            activeCursor = defaultCursor;
        } else {
            navStateCursor = cursor;
            activeCursor = cursor;
        }
        display.asyncExec( this );
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

    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------

    /**
     * Called on the display thread to set the cursor to the canvas.
     */
    public void run( ) {
        if ( activeCursor != null ) {
            canvas.setCursor( activeCursor );
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Called when the surface is being reset to set the new canvas object
     *
     * @param canvas - The new canvas object
     */
    public void resetCanvas( Canvas canvas ) {
        this.canvas = canvas;
    }

    /**
     * Called when the application is shutting down to explicitly
     * dispose of the referenced cursor objects
     */
    public void dispose( ) {
        defaultCursor.dispose( );
        for( int i = 0; i < sensorCursors.length; i++ ) {
            Cursor cursor = sensorCursors[i];
            if ( !cursor.isDisposed( ) ) {
                cursor.dispose( );
            }
        }
        Cursor[] nav_cursor = new Cursor[navCursors.size( )];
        nav_cursor = (Cursor[])navCursors.values( ).toArray( nav_cursor );
        for( int i = 0; i < nav_cursor.length; i++ ) {
            Cursor cursor = nav_cursor[i];
            if ( !cursor.isDisposed( ) ) {
                cursor.dispose( );
            }
        }
    }

    /**
     * Private convenience method to load cursors for use by this handler
     *
     * @param cursorProperties - Definitions for cursor overrides
     */
    private void loadCursors( Properties cursorProperties ) {

        for( int i = 0; i < SENSOR_DATA.length; i++ ) {
            String property_value = cursorProperties.getProperty(
                SENSOR_DATA[i][PROPERTY],
                SENSOR_DATA[i][VALUE]
                );
            Cursor cursor = getCursor( property_value );
            if ( cursor == null ) {
                sensorCursors[i] = defaultCursor;
            } else {
                sensorCursors[i] = cursor;
            }
        }

        for( int i = 0; i < NAVIGATION_DATA.length; i++ ) {
            String property_value = cursorProperties.getProperty(
                NAVIGATION_DATA[i][PROPERTY],
                NAVIGATION_DATA[i][VALUE]
                );
            Cursor cursor = getCursor( property_value );
            if ( cursor == null ) {
                navCursors.put( NAV_MODE[i], defaultCursor );
            } else {
                navCursors.put( NAV_MODE[i], cursor );
            }
        }
    }

    /**
     * Create a new cursor with the image retrieved from the argument url
     *
     * @param url_string - The url to load the cursor image from
     * @return a new cursor, or null if the image could not be loaded.
     */
    private Cursor getCursor( String url_string ) {

        Image image = ImageLoader.loadImage( display, url_string, errorReporter );

        if ( image != null ) {
            ImageData imageData = image.getImageData( );
            int x = imageData.width / 2;
            int y = imageData.height / 2;
            return( new Cursor( display, imageData, x, y ) );
        }
        else {
            errorReporter.warningReport( CURSOR_IMAGE_LOAD_FAILURE_MSG + url_string, null );
            return( null );
        }
    }
}
