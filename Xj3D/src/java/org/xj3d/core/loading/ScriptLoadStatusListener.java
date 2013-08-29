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

package org.xj3d.core.loading;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLScriptNodeType;

/**
 * An internal listener to allow the ScriptLoader and ScriptManager to
 * communicate load state for the scripts.
 * <p>
 *
 * The listener interface is used rather than a common array because the
 * loader is typically operating on a separate thread and having a queue
 * between the two will have multi-threaded issues. Using this listener
 * reduces the multi-threaded issues to just one class and thus making it
 * easy to manage.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ScriptLoadStatusListener {

    /**
     * Notification of a successful load of a script. Not used for failed
     * loads.
     *
     * @param script The script that was loaded correctly
     */
    public void loadCompleted(VRMLScriptNodeType script);

    /**
     * Notification of a failed load of a script when none of the URLs could
     * be loaded.
     *
     * @param script The script that failed to load
     */
    public void loadFailed(VRMLScriptNodeType script);
}

