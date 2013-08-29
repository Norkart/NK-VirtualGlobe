/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.InvalidBrowserException;
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * Extension Xj3D-specific browser methods.
 * <p>
 * Only external browsers are able to make use of this interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface Xj3DBrowser extends ExternalBrowser {

    /** The rendering style uses point mode */
    public static final int RENDER_POINTS = 1;

    /** The rendering style uses wireframe mode */
    public static final int RENDER_LINES = 2;

    /** The rendering style uses flat shading mode */
    public static final int RENDER_FLAT = 3;

    /** The rendering style uses a generic shading model */
    public static final int RENDER_SHADED = 4;

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param millis The minimum time in milleseconds.
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void setMinimumFrameInterval(int millis)
        throws InvalidBrowserException;

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public int getMinimumFrameInterval()
        throws InvalidBrowserException;

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException, InvalidBrowserException;

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public int getRenderingStyle()
        throws InvalidBrowserException;

    /**
     * Set the handler for error messages. This can be used to replace the
     * stock console. Passing a value of null removes the currently registered
     * reporter. Setting this will replace the current reporter with this
     * instance. If the current reporter is the default system console, then
     * the console will not receive any further messages.
     *
     * @param reporter The error reporter instance to use
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void setErrorReporter(Xj3DErrorReporter reporter)
        throws InvalidBrowserException;

    /**
     * Add a listener for status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void addStatusListener(Xj3DStatusListener l)
        throws InvalidBrowserException;

    /**
     * Remove a listener for status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void removeStatusListener(Xj3DStatusListener l)
        throws InvalidBrowserException;

    /**
     * Fetch the interface that allows an external application to implement
     * their own navigation user interface. This is guaranteed to be unique
     * per browser instance.
     *
     * @return An interface allowing end-user code to manipulate the
     *    navigation.
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public Xj3DNavigationUIManager getNavigationManager()
        throws InvalidBrowserException;

    /**
     * Fetch the copontent-specific interface for managing a CAD scene. This
     * interface exposes CAD structures
     *
     * @return An interface allowing end-user code to manipulate the
     *    the CAD-specific structures in the scene.
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public Xj3DCADView getCADView()
        throws InvalidBrowserException;
}
