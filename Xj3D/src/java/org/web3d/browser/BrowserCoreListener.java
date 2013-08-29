/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports

// Application specific imports
import org.web3d.vrml.nodes.VRMLScene;

/**
 * A listener for changes in the core of the browser.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface BrowserCoreListener {

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene);

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg);

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown();

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed();
}