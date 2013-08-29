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
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.nodes.VRMLExternProtoDeclare;
import org.web3d.vrml.util.NodeArray;

/**
 * An abstract definition of managers for loading files that are external to
 * the currently loading file eg Textures, inlines and protos.
 * <p>
 *
 * The loader is given a scene and told to start loading the contents. During
 * this time items progress from pending to loading to loaded. The load manager
 * is cancelable so that a particular scene can be interrupted part way through
 * loading. The manager is designed to load multiple scenes in parallel or to
 * have parallel instances of this manager loading data.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface ContentLoadManager {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Queue for loading a single externproto implementation. Used as a
     * way of getting externprotos in to load from the scripting APIs and
     * their createProto or createVRMLFromString methods that have queued up
     * content to load but have no other way of pushing it into the system.
     *
     * @param proto The proto declaration that needs to be queued up
     */
    public void queueExternProtoLoad(VRMLExternProtoDeclare proto);

    /**
     * Start loading the contents of the given scene. If there are other scenes
     * already loading, this is placed in the queue behind them. If the scene
     * does not contain any external nodes or is null then it is not queued for
     * loading. This is not a recursive call through all the contained scenes.
     * The caller is responsible for making sure all sub-scenes are loaded as
     * required.
     *
     * @param scene The scene to load content for
     */
    public void queueSceneLoad(BasicScene scene);

    /**
     * Queue an arbitrary collection of nodes for loading.
     *
     * @param nodes Array of nodes to add
     */
    public void queueNodesLoad(NodeArray nodes);

    /**
     * Stop the named scene from loading. If there are any loads in progress
     * this action forces them to stop. If this is not a known scene then the
     * request is ignored.
     *
     * @param scene The scene to stop loading
     */
    public void stopSceneLoad(BasicScene scene);

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear();

    /**
     * Notification that the manager needs to shut down all the currently
     * running threads. Normally called when the application is exiting, so it
     * is not expected that the manager needs to act sanely after this method
     * has been called.
     */
    public void shutdown();

    /**
     * Get the number of items that need to be loaded.
     *
     * @return The number of items queued.
     */
    public int getNumberInProgress();
}
