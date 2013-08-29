/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Browser interface that represents the additional abilities an external
 * application is granted to the VRML browser.
 * <P>
 * A number of the methods in this applicationcan take strings representing URLs.
 * Relative URL strings contained in URL fields of nodes or these method
 * arguments are interpreted as follows:
 * <P>
 * Relative URLs are treated as per clause B.3.5 of the EAI Java Bindings
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface ExternalBrowser extends Browser {

    /**
     * Lock the output from the external interface to the browser as the code
     * is about to begin a series of updates. No events will be passed to the
     * VRML world. They will be buffered pending release due to a subsequent
     * call to endUpdate.
     * <P>
     * This call is a nesting call which means subsequent calls to beginUpdate
     * are kept on a stack. No events will be released to the VRML browser
     * until as many endUpdates have been called as beginUpdate.
     *
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void beginUpdate()
      throws InvalidBrowserException;

    /**
     * Release the output of events from the external interface into the
     * VRML browser. All events posted to this point from the last time that
     * beginUpdate was called are released into the VRML browser for
     * processing at the next available oppourtunity.
     * <P>
     * This call is a nesting call which means subsequent calls to beginUpdate
     * are kept on a stack. No events will be released to the VRML browser
     * until as many endUpdates have been called as beginUpdate.
     * <P>
     * If no beginUpdate has been called before calling this method, it has
     * no effect.
     *
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void endUpdate()
      throws InvalidBrowserException;

    /**
     * Add a listener for browser events. Any changes in the browser will be
     * sent to this listener. The order of calling listeners is not guarenteed.
     * Checking is performed on whether the nominated listener is already
     * registered to ensure that multiple registration cannot take place.
     * Therefore it is possible to multiply register the one class
     * instance while only receiving one event.
     *
     * @param l The listener to add.
     * @exception NullPointerException If the provided listener reference is
     *     null
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void addBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

    /**
     * Remove a listener for browser events. After calling this method, the
     * listener will no longer recieve events from this browser instance. If the
     * listener passed as an argument is not currently registered, the method
     * will silently exit.
     *
     * @param l The listener to remove
     * @exception NullPointerException If the provided listener reference is
     *     null
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void removeBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

    /**
     * Dispose the resources that are used by this instance. Should be called
     * just prior to leaving the application.
     */
    public void dispose();

    /**
     * Start the render and event cascade evaluation system again after a pause
     * or stop. If this is a start after a stop, it should be treated as the
     * world having just been loaded. All scripts and sensors should be told to
     * initialise again. If it is after a pause, the world continues as before
     * as though nothing has happened, except TimeSensors will have their time
     * values updated to reflect the current time and the fraction adjusted
     * accordingly.
     */
    public void startRender();

    /**
     * Pause the render and event cascade evaluation system.
     */
    public void pauseRender();

    /**
     * Stop the render and event cascade evaluation system completely. This will
     * trigger the proper shutdown notifications being sent to all nodes and
     * scripts.
     */
    public void stopRender();
}
