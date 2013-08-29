/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.dis;

// External imports
import java.util.ArrayList;
import java.util.Map;
import javax.media.j3d.SceneGraphObject;
import mil.navy.nps.dis.EntityStatePdu;
import mil.navy.nps.dis.EntityID;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLDISNodeType;
import org.web3d.vrml.renderer.common.nodes.dis.BaseDISEntityManager;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.j3d.nodes.dis.J3DEspduTransform;
import org.web3d.xmsf.dis.*;

/**
 * J3D renderer implementation of a DISEntityManager Node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class J3DDISEntityManager extends BaseDISEntityManager
    implements J3DVRMLNode {

    /** New entities, will become addedEntities at end of frame */
    private ArrayList addedEntities;

    /** Removed entities, will become removedEntities at end of frame */
    private ArrayList removedEntities;

    /** Are there new added entities */
    private boolean newAddedEntities;

    /** Are there new removed entities */
    private boolean newRemovedEntities;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public J3DDISEntityManager() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DDISEntityManager(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
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

    //----------------------------------------------------------
    // Methods overriding BaseGroup class.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        if (newAddedEntities) {
            vfAddedEntities.clear();

            synchronized(addedEntities) {
                vfAddedEntities.addAll(addedEntities);
                addedEntities.clear();
                newAddedEntities = false;
            }

            hasChanged[FIELD_ADDED_ENTITIES] = true;
            fireFieldChanged(FIELD_ADDED_ENTITIES);
        }

        if (newRemovedEntities) {
            vfRemovedEntities.clear();

            synchronized(removedEntities) {
                vfRemovedEntities.addAll(removedEntities);
                removedEntities.clear();
                newRemovedEntities = false;
            }

            hasChanged[FIELD_REMOVED_ENTITIES] = true;
            fireFieldChanged(FIELD_REMOVED_ENTITIES);
        }
    }

    /**
     * An entity has been removed from the simulation.
     *
     * @param espdu The entity being removed
     */
    public void entityRemoved(VRMLDISNodeType node) {
        synchronized(removedEntities) {
            removedEntities.add(node);
            newRemovedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * A new entity has arrived.
     *
     * @param entity The new entity.
     */
    public void entityArrived(EntityStatePduType espdu) {
        System.out.println("entityArrived ignore for J3D");
    }

    /**
     * A new entity has arrived.
     *
     * @param entity The new entity.
     */
    public void entityArrived(EntityStatePdu espdu) {
        if (nodeFactory == null) {
            nodeFactory = DefaultNodeFactory.createFactory(
                DefaultNodeFactory.JAVA3D_RENDERER );
        }

        try {
            // Is this available from AbstractNode?
            nodeFactory.setSpecVersion(vrmlMajorVersion, vrmlMinorVersion);
            nodeFactory.setProfile("Interchange");
            nodeFactory.addComponent("DIS",1);
        } catch(UnsupportedProfileException upe) {
            upe.printStackTrace();
        }

        J3DEspduTransform node =
            (J3DEspduTransform)nodeFactory.createVRMLNode("EspduTransform", false);

        int idx = node.getFieldIndex("entityID");
        EntityID entityID = espdu.getEntityID();
        node.setValue(idx, entityID.getEntityIDValue());
        idx = node.getFieldIndex("applicationID");
        node.setValue(idx, entityID.getApplicationIDValue());
        idx = node.getFieldIndex("siteID");
        node.setValue(idx, entityID.getSiteIDValue());
        idx = node.getFieldIndex("networkMode");
        node.setValue(idx, "networkReader");
        idx = node.getFieldIndex("address");
        node.setValue(idx, vfAddress);
        idx = node.getFieldIndex("port");
        node.setValue(idx, vfPort);
        node.setFrameStateManager(stateManager);
        node.setupFinished();

        synchronized(addedEntities) {
            addedEntities.add(node);
            newAddedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
        addedEntities = new ArrayList();
        removedEntities = new ArrayList();
        newAddedEntities = false;
        newRemovedEntities = false;
    }
}
