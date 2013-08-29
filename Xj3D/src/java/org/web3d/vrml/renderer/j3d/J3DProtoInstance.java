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

package org.web3d.vrml.renderer.j3d;

// Standard imports
import java.util.Map;

import javax.media.j3d.Leaf;
import javax.media.j3d.Group;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;

// Application specific imports
import org.web3d.util.ObjectArray;

import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;

/**
 * A concrete instance of a Prototype in the Java 3D realm.
 *<p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class J3DProtoInstance extends CRProtoInstance
    implements J3DVRMLNode,
               J3DParentPathRequestHandler,
               J3DPathAwareNodeType {

    /**
     * The top implementation node of the root node. May be altered
     * depending on whether this is a DEF or not.
     */
    private SceneGraphObject j3dImplNode;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    /**
     * Create an instance for the proto with the number of fields. To set the
     * values of these fields, use the normal setValue methods. The fields are
     * list does not care if it contains null values.
     *
     * @param name The node name of the proto
     * @param vrml97 true if this is a VRML97 issue proto
     * @param The fields that need to be set here
     * @param numBodyNodes The number of nodes in the body of the proto
     */
    J3DProtoInstance(String name,
                     boolean vrml97,
                     VRMLFieldDeclaration[] fields,
                     int numBodyNodes) {

        super(name, vrml97, fields, numBodyNodes);

        allParentPaths = new ObjectArray();
    }

    //----------------------------------------------------------
    // Methods from the J3DParentPathRequestHandler interface.
    //----------------------------------------------------------

    /**
     * Check to see if the parent path to the root of the scene graph has
     * changed in structure and the scene graph path needs to be regenerated.
     * This is a query only and if this level has not changed then the parent
     * level above should be automatically requested until the root of the
     * live scene graph is reached.
     *
     * @return true if this or a parent of this path has changed
     */
    public boolean hasParentPathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the world to this node.
     * If this node's SceneGraphObject is represented by a SharedGroup, then
     * the last item in the given path will be the Link node that is attached
     * to this object.
     *
     * @param requestingChild A reference to the child that's making the request
     * @return The list of locales and nodes in the path down to this node or null
     */
    public ObjectArray getParentPath(J3DVRMLNode requestingChild) {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        ObjectArray p_path = parentPathHandler.getParentPath(this);

        return p_path;
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
     * format that includes its own internal animation engine, so be very
     * careful with this request.
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

        if((j3dImplNode == null) && (bodyNodeCount != 0)) {
            J3DVRMLNode rootNode = (J3DVRMLNode)bodyNodes[0];
            j3dImplNode = rootNode.getSceneGraphObject();
        }

        return j3dImplNode;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // Always set the impl to be the default and only modify it when we
        // have to because DEF doesn't like us
        J3DVRMLNode root = (J3DVRMLNode)rootNode;

        // An Extern proto might not be loaded yet
        if(root != null) {
            j3dImplNode = root.getSceneGraphObject();

            if(root instanceof J3DPathAwareNodeType)
                ((J3DPathAwareNodeType)root).addParentPathListener(this);
        }
    }
}
