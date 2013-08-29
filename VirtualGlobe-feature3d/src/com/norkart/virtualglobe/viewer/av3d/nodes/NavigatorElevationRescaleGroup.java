//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * NavigatorElevationRescaleGroup.java
 *
 * Created on 20. februar 2007, 12:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer.av3d.nodes;

import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
// import com.norkart.VirtualGlobe.Util.Navigation.GlobeNavigatorUpdateListener;

// External imports
import javax.vecmath.*;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

/**
 * A class that automatically orients its children towards the camera location.
 * <p>
 *
 * This class is design to operate in a shared scene graph structure. It works with
 * the Cullable interface to make sure that regardless of the traversal path, it will
 * have the children pointing towards the camera location. This makes it safe to share
 * between layers as well as normal scene graph usage. Correctness is ensured regardless
 * of culling traversal.
 * <p>
 * Bounds of this object is represented as a spherical object that is based on the
 * largest dimension of all the children combined.
 *
 * @author Rune Aasgaard, (c) SINTEF, Justin Couch
 * @version $Revision: 1.7 $
 */
public class NavigatorElevationRescaleGroup extends BaseGroup
        implements CustomCullable, CustomPickTarget, PickableObject, NodeUpdateListener {
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
    
    /** Sharable version of the null bounds object for those that need it */
    private static final BoundingVoid INVALID_BOUNDS = new BoundingVoid();
    
    /** Flag indicating if this object is pickable currently */
    private int pickFlags;
    
    /** Working vector needed for bounds calculation */
    private float[] wkVec1;
    
    /** Working vector needed for bounds calculation */
    private float[] wkVec2;
    
    private float mult;
    private float scale;
    private float last_bound_scale;
    private GlobeNavigator navigator;
    
    private float[] center;
    
    /**
     * Construct a default billboard that uses point mode for the default axis.
     */
    public NavigatorElevationRescaleGroup(GlobeNavigator navigator, float mult) {
        this.mult = mult;
        this.navigator = navigator;
        
        double ele = navigator.getEllipsHeight();
        ele = Math.max(ele, navigator.getTerrainHeight());
        if (ele < 10)
            ele = 10;
        if (ele > 6e6)
            ele = 6e6;
        scale = (float)ele*mult;
        
        pickFlags = 0xFFFFFFFF;
        bounds = null;
        wkVec1 = new float[3];
        wkVec2 = new float[3];
        center = new float[3];
    }
    
    //-------------------------------------------------
    // Methods defined by BaseGroup
    //-------------------------------------------------
    
    protected void recomputeBounds() {
        if(!implicitBounds)
            return;
        
        super.recomputeBounds();
        if(bounds instanceof BoundingVoid)
            return;
        
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setScale(scale);
        
        /*
        float min[] = new float[3];
        float max[] = new float[3];
        bounds.getExtents(min, max);
        BoundingBox b = new BoundingBox(min,max);
        b.transform(mat);
        bounds = b;
         */
        
        bounds.transform(mat);
        bounds.getCenter(center);
        last_bound_scale = scale;
    }
    
    //-------------------------------------------------------
    // NodeUpdateListener methods
    //-------------------------------------------------------
    public void updateNodeBoundsChanges(java.lang.Object src) {}
    
    public void	updateNodeDataChanges(java.lang.Object src) {}
    
    //-------------------------------------------------
    // Methods defined by CustomCullable
    //-------------------------------------------------
    
    /**
     * Check this node for children to traverse. The angular resolution is
     * defined as Field Of View (in radians) / viewport width in pixels.
     *
     * @param output Fill in the child information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     */
    public void cullChildren(CullInstructions output,
            Matrix4f vworldTx,
            Matrix4f viewTransform,
            Vector4f[] frustumPlanes,
            float angularRes) {
        if (dirtyBoundsCount != 0)
            throw new IllegalStateException("dirtyBoundsCount != 0");
        
        if(BoundingVolume.FRUSTUM_ALLOUT ==
                bounds.checkIntersectionFrustum(frustumPlanes, vworldTx)) {
            output.hasTransform = false;
            output.numChildren = 0;
            return;
        }
        
        double ele = Math.max(navigator.getTerrainHeight(), navigator.getEllipsHeight());
        if (ele < 10)
            ele = 10;
        if (ele > 6e6)
            ele = 6e6;
       
        double camera_x = viewTransform.m03 - vworldTx.m03 + center[0];
        double camera_y = viewTransform.m13 - vworldTx.m13 + center[1];
        double camera_z = viewTransform.m23 - vworldTx.m23 + center[2];
        
        double dist = Math.sqrt(camera_x*camera_x+camera_y*camera_y+camera_z*camera_z);
        scale = (float)Math.min(ele, dist)*mult;
        
        output.localTransform.setIdentity();
        output.localTransform.setScale(scale);
        
        if (scale > last_bound_scale*1.1f || scale < last_bound_scale/1.1f)
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(this, this, AV3DViewerManager.UPDATE_BOUNDS);
        
        
        // And the rest of the output data...
        output.hasTransform = true;
        if(output.children.length <lastChild)
            output.resizeChildren(lastChild);
        output.numChildren = 0;
        
        for(int i=0; i<lastChild; ++i) {
            if(childList[i] instanceof Cullable)
                output.children[output.numChildren++] = (Cullable)childList[i];
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by CustomPickTarget
    //---------------------------------------------------------------
    
    /**
     * This node is being subjected to picking, so process the provided data and return
     * the instructions on the list of available children and any transformation
     * information to the system.
     *
     * @param output Fill in the picking results here
     * @param vworldTx The transformation from the root of the scene to this node
     *   according to the current traversal path
     * @param req The details of the picking request that are to be processed
     */
    public void pickChildren(PickInstructions output,
            Matrix4f vworldTx,
            PickRequest req) {
        
        double ele = navigator.getEllipsHeight();
        ele = Math.max(ele, navigator.getTerrainHeight());
        if (ele < 10)
            ele = 10;
        if (ele > 6e6)
            ele = 6e6;
        
        output.localTransform.setIdentity();
        output.localTransform.setScale((float)ele*mult);
        
        output.hasTransform = true;
        output.resizeChildren(lastChild);
        output.numChildren = 0;
        
        for(int i = 0; i < lastChild; ++i) {
            if(childList[i] instanceof PickTarget)
                output.children[output.numChildren++] = (PickTarget)childList[i];
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------
    
    /**
     * Check the given pick mask against the node's internal pick mask representation.
     * If there is a match in one or more bitfields then this will return true,
     * allowing picking to continue to process for this target.
     *
     * @param mask The bit mask to check against
     * @return True if the mask has an overlapping set of bitfields
     */
    public boolean checkPickMask(int mask) {
        return ((pickFlags & mask) != 0);
    }
    
    /**
     * Get the bounds of this picking target so that testing can be performed on the
     *  object.
     *
     * @return The bounding volume defining the pickable target space
     */
    public BoundingVolume getPickableBounds() {
        /*
        double ele = navigator.getEllipsHeight();
        ele = Math.max(ele, navigator.getTerrainHeight());
        if (ele < 10)
            ele = 10;
        if (ele > 6e6)
            ele = 6e6;
         
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setScale((float)ele*mult);
         
        float min[] = new float[3];
        float max[] = new float[3];
        bounds.getExtents(min, max);
        BoundingBox b = new BoundingBox(min,max);
        b.transform(mat);
        return b;
         */
        return getBounds();
    }
    
    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return The CUSTOM_PICK_TYPE constant
     */
    public int getPickTargetType() {
        return PickTarget.CUSTOM_PICK_TYPE;
    }
    
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
    
    
}
