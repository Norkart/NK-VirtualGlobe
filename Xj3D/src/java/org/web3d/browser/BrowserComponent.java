/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.browser;

// External imports

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * Abstraction of a specific rendering component that can be placed on
 * screen regardless of renderer type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface BrowserComponent {

    /**
     * Get the spec version that is supported.
     *
     * @return a number representing the spec major version
     */
    public int supportedSpecificationVersion();

    /**
     * Get the UI toolkit specific component holding this browser.
     *
     * @return The component
     */
    public Object getCanvas();

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType();

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore();

    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter();

    /**
     * Set the minimum frame interval time to limit the CPU resources taken up
     * by the 3D renderer. By default it will use all of them. The second
     * parameter is used to control whether this is a user-set hard minimum or
     * something set by the browser internals. User set values are always
     * treated as the minimum unless the browser internals set a value that is
     * a slower framerate than the user set. If the browser then sets a faster
     * framerate than the user set value, the user value is used instead.
     *
     * @param millis The minimum time in milleseconds.
     * @param userSet true if this is an end-user set minimum
     */
    public void setMinimumFrameInterval(int millis, boolean userSet);

    /**
     * Called to instruct the component instance to start rendering now.
     */
    public void start();

    /**
     * Called to instruct the component instance to stop and suspend its state.
     * The renderer should stop at this point.
     */
    public void stop();

    /**
     * Called to instruct the component instance to destroy itself and any
     * used resources. It will not be used again.
     */
    public void destroy();
}
