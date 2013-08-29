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

package org.web3d.vrml.renderer.j3d.nodes.core;

// Standard imports
import java.util.*;

import javax.media.j3d.*;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.InvalidNodeTypeException;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.nodes.VRMLChildNodeType;

import org.web3d.vrml.renderer.common.nodes.core.BaseWorldRoot;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;

/**
 * Implementation of the world root class.
 * <p>
 *
 * Extends the basic root node functionality with Java3D specific capabilities.
 * As the world root node should never be directly added to the scene graph,
 * Therefore <code>getSceneGraphObject()</code> will always return null.
 * In addition, the world root will never be cloned as part of a proto so
 * there is no copy constructor. If an attempt is made to clone, it will
 * generate an exception - quite deliberately.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class J3DWorldRoot extends BaseWorldRoot
    implements J3DVRMLNode,
               J3DParentPathRequestHandler,
               J3DPathAwareNodeType {

    /** The node that gets returned to the caller of getSceneGraphObject() */
    private Group j3dImplGroup;

    /**
     * The number of children nodes we added to J3D, not the total. We don't
     * Add nodes to J3D that have no Java3D scenegraph object (such as
     * interpolators and timesensors). This count is so that we don't have
     * to enable the ALLOW_CHILDREN_READ on the group (an optimisation step)
     */
    private int j3dChildCount;

    /**
     * Mapping of the VRMLNodeType to the J3D Link instance if used. Note that
     * if someone does something like this:
     * Group { children [ DEF G Group USE G ] }
     * then this map is going to have some problems. We hope that nobody is that
     * stupid, but it is an area that we have to consider will be a source of
     * bugs at some point in the future.
     */
    private HashMap j3dLinkMap;

    /** Mapping of the nodes to the actual scene graph objects by which they are
     *  attached to the scene.  Necessary due to inability to ask a live Java3D node its
     *  parent(s), and because its not reliable to ask the J3DVRMLNode what it was added as.
     */
    private HashMap j3dChildMap;
    
    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    /**
     * Construct an instance of this node.
     */
    public J3DWorldRoot() {
        j3dLinkMap = new HashMap();
        j3dChildMap = new HashMap();
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

        Link link = (Link)j3dLinkMap.get(requestingChild);

        ObjectArray p_path = parentPathHandler.getParentPath(this);

        if(p_path != null) {
            if(link != null)
                p_path.add(link);
        }

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
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dImplGroup;
    }

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
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if (inSetup)
            return;

        J3DVRMLNode kid = (J3DVRMLNode) node;

        if(isStatic) {
            // Make sure the child is finished first.
            Node j3d_node = (Node)kid.getSceneGraphObject();

            if(j3d_node == null)
                return;

            // In the static case, it if is not a SharedGroup, we don't
            // need to do anything with it at all and just add it directly
            // to the grouping node parent.
            if(j3d_node instanceof SharedGroup) {
                j3d_node = new Link((SharedGroup)j3d_node);
                j3dLinkMap.put(kid, j3d_node);
            }

            j3dChildCount++;
            j3dImplGroup.addChild(j3d_node);
            j3dChildMap.put(node,j3d_node);
        } else {
            // Make sure the child is finished first.
            Node j3d_node = (Node)kid.getSceneGraphObject();

            if(j3d_node == null)
                return;

            // If the child is a DEF'd node (This could be a use) then we always
            // add a Link and then a BranchGroup above that so that it can be
            // detached later. If there is no child, don't add anything.
            if(j3d_node instanceof SharedGroup) {
                Link link = new Link((SharedGroup)j3d_node);
                BranchGroup bg = new BranchGroup();
                bg.setCapability(BranchGroup.ALLOW_DETACH);
                bg.addChild(link);

                j3dLinkMap.put(kid, link);
                j3d_node = bg;
            } else {
                // just make sure that we can actually detach it later on
                if(!j3d_node.isCompiled())
                    j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
            }
            j3dChildCount++;
            j3dImplGroup.addChild(j3d_node);
            j3dChildMap.put(node,j3d_node);
        }
     }

    /**
     * Notification that the construction phase of this node has finished.
     * This implementation just sets the capability bits. It checks to
     * see if the implGroup has a parent node. If it does not, then it
     * assumes that there needs to be one provided and automatically adds the
     * implGroup as a child of the j3dImplGroup. If there is a parent, it
     * does nothing.
     * <p>
     * Derived classes that do not like this behaviour should override this
     * method or ensure that the implGroup has a parent before this method
     * is called.
     */
    public void setupFinished() {

        if(!inSetup)
            return;

        super.setupFinished();

        // If someone asks for this and it is not a USEd DEF node then this won't
        // be set yet. Just make sure it is here.

        if(j3dImplGroup != null)
            throw new RuntimeException("j3d group is not null in setupFinish()");

        if(isStatic) {
                j3dImplGroup = new Group();
        } else {
            if(isDEF)
                j3dImplGroup = new SharedGroup();
            else {
                j3dImplGroup = new BranchGroup();
                j3dImplGroup.setCapability(BranchGroup.ALLOW_DETACH);
                j3dImplGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
                j3dImplGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            }
        }

        j3dImplGroup.setCapability(Node.ENABLE_PICK_REPORTING);
        j3dImplGroup.setPickable(true);

        int num_kids = vfChildren.size();
        J3DVRMLNode kid;

        if(isStatic) {
            for(int i = 0; i < num_kids; i++) {
                kid = (J3DVRMLNode)vfChildren.get(i);

                if (!(kid instanceof VRMLChildNodeType) && !(kid instanceof VRMLProtoInstance)) {
                    throw new InvalidNodeTypeException(kid.getVRMLNodeName(),"Scenes can only contain ChildNodes");
                }

                // Make sure the child is finished first.
                Node j3d_node = (Node)kid.getSceneGraphObject();

                if(j3d_node == null)
                    continue;

                // In the static case, it if is not a SharedGroup, we don't
                // need to do anything with it at all and just add it directly
                // to the grouping node parent.
                if(j3d_node instanceof SharedGroup) {
                    j3d_node = new Link((SharedGroup)j3d_node);
                    j3dLinkMap.put(kid, j3d_node);
                }

                j3dChildCount++;
                j3dImplGroup.addChild(j3d_node);
                j3dChildMap.put(kid,j3d_node);
            }
        } else {
            for(int i = 0; i < num_kids; i++) {
                kid = (J3DVRMLNode)vfChildren.get(i);

                if (!(kid instanceof VRMLChildNodeType) && !(kid instanceof VRMLProtoInstance)) {
                    throw new InvalidNodeTypeException(kid.getVRMLNodeName(),"Scenes can only contain ChildNodes");
                }

                Node j3d_node=null;

                j3d_node = (Node)kid.getSceneGraphObject();

                if(j3d_node == null)
                    continue;

                // If the child is a DEF'd node (This could be a use) then we always
                // add a Link and then a BranchGroup above that so that it can be
                // detached later. If there is no child, don't add anything.
                if(j3d_node instanceof SharedGroup) {
                    Link link = new Link((SharedGroup)j3d_node);
                    BranchGroup bg = new BranchGroup();
                    bg.setCapability(BranchGroup.ALLOW_DETACH);
                    bg.addChild(link);

                    j3dLinkMap.put(kid, link);
                    j3d_node = bg;
                } else {
                    // just make sure that we can actually detach it later on
                    if(!j3d_node.isCompiled())
                        j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
                }

                j3dChildCount++;
                j3dImplGroup.addChild(j3d_node);
                j3dChildMap.put(kid,j3d_node);
            }
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearChildren() {

        // This could just use the Group.removeAllChildren() call, but that
        // is J3D 1.3 specific. Not good right ATM.
        for(int i = j3dChildCount; --i >= 0; )
            j3dImplGroup.removeChild(0);

        j3dChildCount = 0;
        int num_child = vfChildren.size();

        for(int i = 0; i < num_child; i++) {
            Object obj = vfChildren.get(i);
            if(obj instanceof J3DPathAwareNodeType)
                ((J3DPathAwareNodeType)obj).removeParentPathListener(this);
        }

        j3dLinkMap.clear();
        j3dChildMap.clear();
        super.clearChildren();
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate.
     *
     * @param node The node to view
     */
    protected void addChildNode(VRMLNodeType node) {
        if(node instanceof J3DVRMLNode)
            super.addChildNode(node);
        else
            throw new InvalidFieldValueException("Node is not a J3DVRMLNode");

        if(node instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)node).addParentPathListener(this);

        if(inSetup)
           return;

        Node j3d_node = (Node)((J3DVRMLNode)node).getSceneGraphObject();

        if(j3d_node == null)
            return;


        // If the child is a DEF'd node (This could be a use) then we always
        // add a Link and then a BranchGroup above that so that it can be
        // detached later. If there is no child, don't add anything.
        if(j3d_node instanceof SharedGroup) {
            Link link = new Link((SharedGroup)j3d_node);
            BranchGroup bg = new BranchGroup();
            bg.setCapability(BranchGroup.ALLOW_DETACH);
            bg.addChild(link);

            j3dLinkMap.put(node, link);
            j3d_node = bg;
        } else {
            // just make sure that we can actually detach it later on
            if(!j3d_node.isCompiled())
                j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
        }

        j3dChildCount++;
        j3dImplGroup.addChild(j3d_node);
        j3dChildMap.put(node,j3d_node);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(node instanceof J3DVRMLNode)
            super.removeChildNode(node);
        else
            throw new InvalidFieldValueException("Node is not a J3DVRMLNode");

        if(node instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)node).removeParentPathListener(this);

        if(inSetup)
           return;

        Node j3d_node = (Node) j3dChildMap.get(node);
        if (j3d_node!=null)
        	j3dImplGroup.removeChild(j3d_node);
        j3dChildMap.remove(node);
        j3dLinkMap.remove(node);
    }
}
