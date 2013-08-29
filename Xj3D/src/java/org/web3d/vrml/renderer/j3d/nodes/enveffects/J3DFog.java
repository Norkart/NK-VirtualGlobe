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

package org.web3d.vrml.renderer.j3d.nodes.enveffects;

// Standard imports
import javax.media.j3d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.enveffects.BaseFog;
import org.web3d.vrml.renderer.j3d.nodes.J3DFogNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;

/**
 * Java3D implementation of a fog node.
 * <p>
 *
 * Like other bindable nodes in the J3D renderer, the implementation is not
 * kept here. It is mainly a data structure for holding the information and the
 * real implementation is in the
 * {@link org.web3d.vrml.renderer.j3d.browser.GlobalEffectsGroup}.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class J3DFog extends BaseFog
    implements J3DFogNodeType, J3DPathAwareNodeType {

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    // Temp arrays for copying stuff for the scene graph path
    private Object[] tmpPathArray;
    private Node[] tmpNodeArray;

    /** The current fog node being used */
    private BranchGroup fogImpl;

    /**
     * Construct a new default instance of this class.
     */
    public J3DFog() {
        super();

        allParentPaths = new ObjectArray();
        fogImpl = new BranchGroup();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DFog(VRMLNodeType node) {
        super(node);

        allParentPaths = new ObjectArray();
        fogImpl = new BranchGroup();
    }

    //----------------------------------------------------------
    // Methods required by the J3DFogNodeType interface.
    //----------------------------------------------------------

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling. If
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here or null
     */
    public SceneGraphPath getSceneGraphPath() {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        ObjectArray path_array = parentPathHandler.getParentPath(this);

        if(path_array == null)
            return null;

        int path_size = path_array.size();
        if((tmpPathArray == null) || tmpPathArray.length < path_size) {
            tmpPathArray = new Object[path_size];
            tmpNodeArray = new Node[path_size - 1];
        }

        path_array.toArray(tmpPathArray);
        Locale locale = (Locale)tmpPathArray[0];
        for(int i = 1; i < path_size; i++)
            tmpNodeArray[i - 1] = (Node)tmpPathArray[i];

        return new SceneGraphPath(locale, tmpNodeArray, fogImpl);
    }

    //----------------------------------------------------------
    // Methods from the J3DPathAwareNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own animation engine.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return fogImpl;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        fogImpl.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        fogImpl.setCapability(BranchGroup.ALLOW_DETACH);
    }
}
