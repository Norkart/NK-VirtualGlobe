//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d.nodes;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;

import javax.vecmath.*;

import java.lang.ref.*;

/** Auto load node works together with a user supplied NodeLoader to load
 *  a child automatically when the child becomes visible (when the
 *  getCullableChild is called).<p>
 *
 *  When the node has been invisible for a while it may be removed by the
 *  garbage collector.
 *
 * @author rune.aasgaard@norkart.no
 */
public class AutoLoadNode
        extends BaseNode
        implements SingleCullable, SinglePickTarget, PickableObject, NodeUpdateListener {
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_MSG =
            "Picking not permitted right now. Picking is only permitted during " +
            "the update observer callbacks.";
    
    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_MSG =
            "This node has been marked as not pickable by the user";
    
    /** Soft reference to the loaded child */
    protected SoftReference child_ref;
    
    protected PhantomReference ph_ref;
    
    /** The child node loader */
    protected NodeLoader    loader;
    
    /** Number of currently known dirty child bounds */
    protected int dirtyBoundsCount;
    
    /** Flag indicating if this object is pickable currently */
    private int pickFlags;
    
    private class ChildKiller {
        Node child;
        
        ChildKiller(Node child) {
            this.child = child;
        }
        
        protected void finalize() {
            if (child != null)
                setLive(child, false);
            child = null;
        }
    }
    
    private Node getChild() {
        ChildKiller ck = (ChildKiller)(child_ref != null ? child_ref.get() : null);
        if (ck != null)
            return ck.child;
        return null;
    }
    
    //-------------------------------------------------
    // Constructor methods
    //-------------------------------------------------
    public AutoLoadNode(NodeLoader loader) {
        this.loader = loader;
        dirtyBoundsCount = 0;
        pickFlags = 0xFFFFFFFF;
    }
    
    // Stupid hack needed by class InternalGroup
    private NodeUpdateHandler getUpdateHandler() {
        return updateHandler;
    }
    
    
    //-------------------------------------------------
    // SingleCullable methods
    //-------------------------------------------------
    public Cullable getCullableParent() {
        return (Cullable)this.getParent();
    }
    
    public boolean hasMultipleParents() {
        return false;
    }
    
    public Cullable getCullableChild() {
        if (dirtyBoundsCount != 0)
            throw new IllegalStateException("dirtyBoundsCount != 0");
        
        Node child = getChild();
        if (child == null) {
            child_ref = null;
            if (loader != null) {
                loader.requestLoad();
                child = loader.takeNode();
                if (child != null) {
                    child_ref = new SoftReference(new ChildKiller(child));
                    setParent(child, this);
                    if(child.isLive() != alive)
                        setLive(child, alive);
                    setUpdateHandler(child);
                    if (isLive()) {
                        ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(this, this, AV3DViewerManager.UPDATE_BOUNDS);
                    }
                }
            }
        }
        return (Cullable)child;
    }
    
      //-------------------------------------------------------
    // NodeUpdateListener methods
    //-------------------------------------------------------
    public void updateNodeBoundsChanges(java.lang.Object src) {}
    
    public void	updateNodeDataChanges(java.lang.Object src) {}
    
    //---------------------------------------------------------------
    // Methods defined by PickableObject
    //---------------------------------------------------------------
    
    /**
     * Set the node as being pickable currently using the given bit mask.
     * A mask of 0 will completely disable picking.
     *
     * @param state A bit mask of available options to pick for
     */
    public void setPickMask(int state) {
        pickFlags = state;
    }
    
    /**
     * Get the current pickable state mask of this object. A value of zero
     * means it is completely unpickable.
     *
     * @return A bit mask of available options to pick for
     */
    public int getPickMask() {
        return pickFlags;
    }
    
    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param reqs The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickBatch(PickRequest[] reqs, int numRequests)
    throws NotPickableException, InvalidPickTimingException {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
            throw new InvalidPickTimingException(PICK_TIMING_MSG);
        
        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);
        
        PickingManager picker = updateHandler.getPickingManager();
        
        picker.pickBatch(this, reqs, numRequests);
    }
    
    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickSingle(PickRequest req)
    throws NotPickableException, InvalidPickTimingException {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
            throw new InvalidPickTimingException(PICK_TIMING_MSG);
        
        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);
        
        PickingManager picker = updateHandler.getPickingManager();
        
        picker.pickSingle(this, req);
    }
    
    //---------------------------------------------------------------
    // Methods defined by SinglePickTarget
    //---------------------------------------------------------------
    
    /**
     * Return the child that is pickable of from this target. If there is none
     * then return null.
     *
     * @return The child pickable object or null
     */
    public PickTarget getPickableChild() {
        Node child = getChild();
        return (child instanceof PickTarget) ? (PickTarget)child : null;
    }
    
    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------
    
    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return One of the _PICK_TYPE constants
     */
    public final int getPickTargetType() {
        return SINGLE_PICK_TYPE;
    }
    
    /**
     * Check the given pick mask against the node's internal pick mask
     * representation. If there is a match in one or more bitfields then this
     * will return true, allowing picking to continue to process for this
     * target.
     *
     * @param mask The bit mask to check against
     * @return true if the mask has an overlapping set of bitfields
     */
    public boolean checkPickMask(int mask) {
        return ((pickFlags & mask) != 0);
    }
    
    /**
     * Get the bounds of this picking target so that testing can be performed
     * on the object.
     *
     * @return A representation of the volume representing the pickable objects
     */
    public BoundingVolume getPickableBounds() {
        return bounds;
    }
    
    
    //-------------------------------------------------
    // Node methods
    //-------------------------------------------------
    
    protected void setUpdateHandler(NodeUpdateHandler handler) {
        super.setUpdateHandler(handler);
        Node child = getChild();
        if (child != null)
            setUpdateHandler(child);
    }
    
    
    protected void setLive(boolean state) {
        Node child = getChild();
        if (child != null)
            setLive(child, state);
        
        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);
        
        dirtyBoundsCount = 0;
    }
    protected void markBoundsDirty() {
        dirtyBoundsCount++;
        
        // Only notify the parents that the bounds need to be updated if there
        // are implicit bounds being set.
        if(dirtyBoundsCount == 1) {
            super.markBoundsDirty();
        }
    }
    
    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds() {
        if(--dirtyBoundsCount != 0 || !implicitBounds)
            return;
        
        // Need to set this up to set a point source bounds if there are no
        // children.
        
        recomputeBounds();
        
        this.updateParentBounds();
    }
    
    protected void recomputeBounds() {
        if(!implicitBounds)
            return;
        
        Node child = getChild();
        if (child != null) 
            bounds = child.getBounds();
        else if (loader != null) 
            bounds = loader.getBounds();
        else
            bounds = INVALID_BOUNDS;
    }
    
    public void requestBoundsUpdate() {
        if(alive || !implicitBounds)
            return;
        
        Node child = getChild();
        if (child != null)
            child.requestBoundsUpdate();
        // Clear this as we are no longer dirty.
        recomputeBounds();
    }
}