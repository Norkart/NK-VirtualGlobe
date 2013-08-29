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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.util.ObjectArray;
import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.util.NodeTemplateArray;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.lang.VRMLNodeTemplate;

/**
 * A representation of a manager that handles the current frame state and
 * the listeners that wish to know about it.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public interface FrameStateManager {

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Instruct the state manager that you're interested in keeping track of
     * nodes that have been added or removed of the given list of types. These
     * are the primary node types.
     *
     * @param types An array of types to listen for
     */
    public void listenFor(int[] types);

    /**
     * Remove a type or types(s) that were previously registered as something
     * to listen for. If one or more of the values were not previously
     * registered we ignore that value.
     *
     * @param types An array of types to remove
     */
    public void removeListenFor(int[] types);

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now. This does not clear the listened-for types list.
     */
    public void clear();

    /**
     * Notification that the current frame is finished. This should never be
     * called by user code (node implementations). The only caller should be
     * the rendering traversal code that is specific to each engine.
     */
    public void frameFinished();

    /**
     * Add a listener that is interested in knowing when the end of this frame
     * is reached. When this end of frame is reached, the listener will be
     * removed from the internal list. A listener may call this method to
     * register itself more than once in this frame. The manager will
     * automatically remove duplicates and ensure each listener is called only
     * once each frame.
     *
     * @param l The listener to add
     */
    public void addEndOfThisFrameListener(FrameStateListener l);

    /**
     * Register a node that has been removed this frame. The caller should make
     * sure the reference count has already been decremented before calling
     * this method.
     *
     * @param node The reference to the node that has been removed
     */
    public void registerRemovedNode(VRMLNode node);

    /**
     * Register nodes that have been removed this frame. The caller should make
     * sure the reference count has already been decremented before calling
     * this method.
     *
     * @param nodes The reference to the nodes that has been removed
     */
    public void registerRemovedNodes(VRMLNode[] nodes);

    /**
     * Register an execution space that has been removed in this frame.
     *
     * @param space The space that has been removed this frame
     */
    public void registerRemovedScene(VRMLExecutionSpace space);

    /**
     * Register a node that has been added this frame. The caller should make
     * sure the reference count has already been decremented before calling
     * this method.
     *
     * @param node The reference to the node that has been removed
     */
    public void registerAddedNode(VRMLNode node);

    /**
     * Register nodes that have been added this frame. The caller should make
     * sure the reference count has already been decremented before calling
     * this method.
     *
     * @param nodes The reference to the nodes that has been added
     */
    public void registerAddedNodes(VRMLNode[] nodes);

    /**
     * Register an execution space that has been added in this frame.
     *
     * @param space The space that has been added this frame
     */
    public void registerAddedScene(VRMLExecutionSpace space);

    /**
     * Register an externproto declaration as having been loaded this frame.
     * This will be automatically registered as a URL node.
     *
     * @param proto The proto declaration instance to add
     */
    public void registerAddedExternProto(VRMLNodeTemplate proto);

    /**
     * Clear all registered removed nodes now
     */
    public void clearRemovedNodes();

    /**
     * Clear all registered added nodes now
     */
    public void clearAddedNodes();


    /**
     * Clear all registered removed scenes now
     */
    public void clearRemovedScenes();

    /**
     * Clear all registered added scenes now
     */
    public void clearAddedScenes();

    /**
     * Get all the removed nodes of the named type. This shall return an array
     * all the time regardless of whether there is something to process or not.
     * If there is nothing to process, the list shall be empty.
     *
     * @param type The TypeConstant primary type to get the list for
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedNodes(int type);

    /**
     * Get all the removed script nodes. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedScripts();

    /**
     * Get the removed sensors. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedSensors();

    /**
     * Get the scenes that have removed or replaced during this last frame.
     * The contents of the array shall be instances of
     * {@link org.web3d.vrml.lang.BasicScene}.
     *
     * @return The list of nodes that need to be processed
     */
    public ObjectArray getRemovedScenes();

    /**
     * Get the list of nodes that require view-dependent updates.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedViewDependents();

    /**
     * Get all the removed bindable nodes. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedBindables();

    /**
     * Get the removed nodes from this last frame of this specified type.  This
     * shall return an array all the time regardless of whether there is
     * something to process or not. If there is nothing to process, the list
     * shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedTerrains();

    /**
     * Get the removed externally synchronised nodes from this last frame. This
     * shall return an array all the time regardless of whether there is
     * something to process or not. If there is nothing to process, the list
     * shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getRemovedExtSynchronizedNodes();

    /**
     * Get all the added nodes of the named type. This shall return an array
     * all the time regardless of whether there is something to process or not.
     * If there is nothing to process, the list shall be empty.
     *
     * @param type The TypeConstant primary type to get the list for
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedNodes(int type);

    /**
     * Get the added Sensors. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedSensors();

    /**
     * Get the scenes that have inserted during this last frame. The scenes
     * are the guts of an externproto or Inline node that will need fitting
     * into the processing mechanism. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty. The contents of
     * the array shall be instances of {@link org.web3d.vrml.lang.BasicScene}.
     *
     * @return The list of nodes that need to be processed
     */
    public ObjectArray getAddedScenes();

    /**
     * Get the added nodes with URL fields that will need to have their content
     * loaded for the first time. This list should not include scripts. This
     * shall return an array all the time regardless of whether there is
     * something to process or not. If there is nothing to process, the list
     * shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedUrlNodes();

    /**
     * Get the added scripts from this last frame. This shall return an array all the
     * time regardless of whether there is something to process or not. If
     * there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedScripts();

    /**
     * Get the list of nodes that require view-dependent updates.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedViewDependents();

    /**
     * Get the added bindable nodes from this last frame. This shall return an
     * array all the time regardless of whether there is something to process
     * or not. If there is nothing to process, the list shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedBindables();

    /**
     * Get the added terrain sources from this last frame.  This
     * shall return an array all the time regardless of whether there is
     * something to process or not. If there is nothing to process, the list
     * shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedTerrains();

    /**
     * Get the added externally synchronised nodes from this last frame. This
     * shall return an array all the time regardless of whether there is
     * something to process or not. If there is nothing to process, the list
     * shall be empty.
     *
     * @return The list of nodes that need to be processed
     */
    public NodeArray getAddedExtSynchronizedNodes();

    /**
     * Get the added extern proto instances. This shall return an array all
     * the time regardless of whether there is something to process or not.
     * If there is nothing to process, the list shall be empty.
     *
     * @return The list of templates that need to be processed
     */
    public NodeTemplateArray getAddedExternProtos();
}
