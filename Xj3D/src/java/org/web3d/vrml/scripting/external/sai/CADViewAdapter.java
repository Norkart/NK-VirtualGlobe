/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.NodeObserver;

import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLExecutionSpace;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.x3d.sai.X3DNode;

import org.xj3d.sai.Xj3DCADView;
import org.xj3d.sai.Xj3DCADViewListener;

/**
 * Wrapper interface for the internal viewpoint representation, to allow
 * abstraction of the user interface description of viewpoints from the
 * underlying node representation.
 * <p>
 *
 * This class deliberately does not give access to the SAI X3DNode that
 * represents the viewpoint.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class CADViewAdapter implements Xj3DCADView, NodeObserver {

    /** Error message when the user code barfs */
    private static final String ADD_ERROR_MSG =
        "Error sending CAD node addtition update: ";

    /** Error message when the user code barfs */
    private static final String REMOVE_ERROR_MSG =
        "Error sending CAD node removal update: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending CAD View listener event: ";

    /** listener(s) for CAD view change events */
    private Xj3DCADViewListener viewListener;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The browser core instance that we use to get info about CAD nodes */
    private BrowserCore browserCore;

    /**
     * The factory for use in mapping between VRMLNodeType and X3DNode
     * instances.
     */
    private SAINodeFactory saiNodeFactory;

    /**
     * Create an instance of this class.
     */
    CADViewAdapter(BrowserCore core, SAINodeFactory nodeFactory) {
        browserCore = core;
        saiNodeFactory = nodeFactory;
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DCADView
    //----------------------------------------------------------

    /**
     * Get the list of layers declared in the current scene. If the scene does
     * not contain any layers, it will return null.
     */
    public X3DNode[] getCADLayers() {
        VRMLExecutionSpace space = browserCore.getWorldExecutionSpace();
        BasicScene scene = space.getContainedScene();

        ArrayList<X3DNode> output_nodes = new ArrayList<X3DNode>();

        ArrayList internal_nodes =
            scene.getByPrimaryType(TypeConstants.CADLayerNodeType);

        for(int i = 0; i < internal_nodes.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)internal_nodes.get(i);
            X3DNode x = saiNodeFactory.getSAINode(n);
            output_nodes.add(x);
        }

        internal_nodes = scene.getBySecondaryType(TypeConstants.CADLayerNodeType);

        for(int i = 0; i < internal_nodes.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)internal_nodes.get(i);
            X3DNode x = saiNodeFactory.getSAINode(n);
            output_nodes.add(x);
        }

        if(output_nodes.size() != 0)
            return (X3DNode[])output_nodes.toArray(new X3DNode[output_nodes.size()]);
        else
            return null;
    }

    /**
     * Get the list of layers declared in the current scene. If the scene does
     * not contain any layers, it will return null.
     */
    public X3DNode[] getCADAssemblies() {
        VRMLExecutionSpace space = browserCore.getWorldExecutionSpace();
        BasicScene scene = space.getContainedScene();

        ArrayList<X3DNode> output_nodes = new ArrayList<X3DNode>();

        ArrayList internal_nodes =
            scene.getByPrimaryType(TypeConstants.CADAssemblyNodeType);

        for(int i = 0; i < internal_nodes.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)internal_nodes.get(i);
            X3DNode x = saiNodeFactory.getSAINode(n);
            output_nodes.add(x);
        }

        internal_nodes = scene.getBySecondaryType(TypeConstants.CADAssemblyNodeType);

        for(int i = 0; i < internal_nodes.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)internal_nodes.get(i);
            X3DNode x = saiNodeFactory.getSAINode(n);
            output_nodes.add(x);
        }

        if(output_nodes.size() != 0)
            return (X3DNode[])output_nodes.toArray(new X3DNode[output_nodes.size()]);
        else
            return null;
    }

    /**
     * Highlight the given part or assembly using the given alternate
     * appearance attributes. This will temporarily override the appearance
     * on all sub-children until one of the following conditions are met:
     * <ul>
     * <li>The world is replaced</li>
     * <li>The parent scene graph containing this structure node is removed</li>
     * <li>Another part or structure is requested to be highlighted</li>
     * <li>This same node is requested, but with a null material parameter</li>
     * </ul>
     *
     * Once highlighting has stopped, the ordinary material values are
     * returned.
     *
     * @param structureNode A X3DProductStructureNode to be highlighted
     *    (or PROTO wrapper of one)
     * @param material A X3DmaterialNode to use as the highlight colour, or
     *    null to clear the current highlighted object
     */
    public void highlightPart(X3DNode structureNode, X3DNode material) {
        // TBD
    }

    /**
     * Add a listener for CAD-style changes. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addCADViewListener(Xj3DCADViewListener l) {
        if(viewListener == null && l != null) {
            browserCore.addNodeObserver(TypeConstants.CADLayerNodeType, this);
            browserCore.addNodeObserver(TypeConstants.CADAssemblyNodeType, this);
        }

        viewListener = CADViewListenerMulticaster.add(viewListener, l);
    }

    /**
     * Remove a listener for CAD-style changes. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeCADViewListener(Xj3DCADViewListener l) {
        viewListener = CADViewListenerMulticaster.remove(viewListener, l);

        if(viewListener == null) {
            browserCore.removeNodeObserver(TypeConstants.CADLayerNodeType, this);
            browserCore.removeNodeObserver(TypeConstants.CADAssemblyNodeType, this);
        }
    }

    //----------------------------------------------------------
    // Methods defined by NodeObserver
    //----------------------------------------------------------

    /**
     * Notification that nodes of the require type have been added. The node
     * representations will either be the real node, or if the parent of a
     * proto, the proto instance they came from.
     *
     * @param nodeType The node type id that this notification refers to
     * @param nodes The node instances that have just been added.
     */
    public void nodesAdded(int nodeType, VRMLNodeType[] nodes, int numNodes) {
        if(viewListener == null)
            return;

        for(int i = 0; i < numNodes; i++) {
            try {
                X3DNode x = saiNodeFactory.getSAINode(nodes[i]);

                if(nodeType == TypeConstants.CADLayerNodeType)
                    viewListener.layerAdded(x);
                else
                    viewListener.assemblyAdded(x);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(ADD_ERROR_MSG + viewListener,
                                              (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Notification that nodes of the require type have been removed. The node
     * representations will either be the real node, or if the parent of a
     * proto, the proto instance they came from.
     *
     * @param nodeType The node type id that this notification refers to
     * @param nodes The node instances that have just been removed.
     */
    public void nodesRemoved(int nodeType, VRMLNodeType[] nodes, int numNodes) {
        if(viewListener == null)
            return;

        for(int i = 0; i < numNodes; i++) {
            try {
                X3DNode x = saiNodeFactory.getSAINode(nodes[i]);

                if(nodeType == TypeConstants.CADLayerNodeType)
                    viewListener.layerRemoved(x);
                else
                    viewListener.assemblyRemoved(x);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(REMOVE_ERROR_MSG + viewListener,
                                              (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Force clearing all currently managed nodes from this observer now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        browserCore.removeNodeObserver(TypeConstants.CADLayerNodeType, this);
        browserCore.removeNodeObserver(TypeConstants.CADAssemblyNodeType, this);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}
