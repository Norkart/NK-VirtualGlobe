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

package org.xj3d.ui.awt.device.j3d.mouse;

// External imports
import java.awt.Component;

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Local imports
import org.xj3d.device.Tracker;
import org.xj3d.device.TrackerState;
import org.xj3d.device.ButtonModeConstants;

/**
 * A tracker implementation for mouse devices under Java3D.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class MouseTracker extends Tracker
    implements MouseListener, MouseMotionListener {

    /** What actions might we perform */
    private static final int mask = MASK_PICKING | MASK_POSITION | MASK_ORIENTATION;

    /** The latest state */
    private TrackerState tstate;

    /** Representation of the direction that we pick in */
    private Vector3d mousePickDirection;

    /** Representation of the eye in the world */
    private Point3d mouseEyePosition;

    /** The eye position in the image plate (canvas) world coords */
    private Point3d mousePosition;

    /** The Transform3D to read the view VWorld coordinates */
    private Transform3D viewTransform;

    public MouseTracker() {
        mousePickDirection = new Vector3d(0, 0, -1);
        mouseEyePosition = new Point3d();
        mousePosition = new Point3d();

        viewTransform = new Transform3D();
        tstate = new TrackerState();
        tstate.numButtons = 3;

        tstate.buttonMode[0] = ButtonModeConstants.NAV1;
        tstate.pickingEnabled[0] = true;
        tstate.buttonMode[1] = ButtonModeConstants.NOTHING;
        tstate.pickingEnabled[1] = false;
        tstate.buttonMode[2] = ButtonModeConstants.NAV1;
        tstate.pickingEnabled[2] = false;
    }

    //------------------------------------------------------------------------
    // Methods for Tracker
    //------------------------------------------------------------------------

    /**
     * Notification that tracker polling is beginning.
     */
    public void beginPolling() {
    }

    /**
     * Notification that tracker polling is ending.
     */
    public void endPolling() {
    }

    /**
     * What action types does this sensor return.  This a combination
     * of ACTION masks.
     *
     * @return The action mask.
     */
    public int getActionMask() {
        return mask;
    }

    /**
     * Get the current state of this tracker.
     *
     * @param state The current state
     */
    public void getState(TrackerState state) {
        state.actionMask = mask;
        state.actionType = tstate.actionType;
        state.devicePos[0] = tstate.devicePos[0];
        state.devicePos[1] = tstate.devicePos[1];
        state.devicePos[2] = tstate.devicePos[2];
        state.deviceOri[0] = tstate.deviceOri[0];
        state.deviceOri[1] = tstate.deviceOri[1];
        state.deviceOri[2] = tstate.deviceOri[2];
        state.worldPos[0] = tstate.worldPos[0];
        state.worldPos[1] = tstate.worldPos[1];
        state.worldPos[2] = tstate.worldPos[2];
        state.worldOri[0] = tstate.worldOri[0];
        state.worldOri[1] = tstate.worldOri[1];
        state.worldOri[2] = tstate.worldOri[2];

        state.numButtons = tstate.numButtons;
        for(int i=0; i < tstate.numButtons; i++) {
            state.buttonMode[i] = tstate.buttonMode[i];
            state.buttonState[i] = tstate.buttonState[i];
            state.pickingEnabled[i] = tstate.pickingEnabled[i];
        }

        tstate.actionType = TrackerState.TYPE_NONE;
    }

    //------------------------------------------------------------------------
    // Methods for MouseListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt) {
        tstate.actionType = TrackerState.TYPE_PRESS;

        int mods = evt.getModifiersEx();

        if ((mods & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
            tstate.buttonState[0] = true;
        else
            tstate.buttonState[0] = false;

        if ((mods & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK)
            tstate.buttonState[1] = true;
        else
            tstate.buttonState[1] = false;

        if ((mods & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
            tstate.buttonState[2] = true;
        else
            tstate.buttonState[2] = false;

        transformMouse(evt);
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt) {
        if (tstate.actionType == TrackerState.TYPE_PRESS)
            tstate.actionType = TrackerState.TYPE_CLICK;
        else
            tstate.actionType = TrackerState.TYPE_RELEASE;

        transformMouse(evt);
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt) {
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt) {
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt) {
    }

    //------------------------------------------------------------------------
    // Methods for MouseMotionListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt) {
        if (tstate.actionType == TrackerState.TYPE_NONE ||
            tstate.actionType == TrackerState.TYPE_DRAG) {

            tstate.actionType = TrackerState.TYPE_DRAG;
            transformMouse(evt);
        }
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt) {
        if (tstate.actionType == TrackerState.TYPE_NONE ||
            tstate.actionType == TrackerState.TYPE_MOVE) {

            tstate.actionType = TrackerState.TYPE_MOVE;

            transformMouse(evt);
        }
    }

    /**
     * Generate the mouse pick position and shape in the world coordinates
     * based on its current screen position. The results of the position and
     * orientation will be left in mouseEyePosition and mousePickDirection.
     *
     * @param evt The mouse event
     */
    private void transformMouse(MouseEvent evt) {

        Component comp = (Component)evt.getSource();
        float screenHeight = comp.getHeight();
        float screenWidth = comp.getWidth();

        tstate.devicePos[0] = evt.getX() / screenWidth;
        tstate.devicePos[1] = evt.getY() / screenHeight;
        tstate.devicePos[2] = 0;

        // TODO: Might optimize these as the canvas shouldn't change
        Canvas3D canvas = (Canvas3D)evt.getSource();
        canvas.getCenterEyeInImagePlate(mouseEyePosition);
        canvas.getPixelLocationInImagePlate(evt.getX(),
                                            evt.getY(),
                                            mousePosition);
        canvas.getImagePlateToVworld(viewTransform);

        viewTransform.transform(mouseEyePosition);
        viewTransform.transform(mousePosition);

        mousePickDirection.sub(mousePosition, mouseEyePosition);

        tstate.worldPos[0] = (float) mouseEyePosition.x;
        tstate.worldPos[1] = (float) mouseEyePosition.y;
        tstate.worldPos[2] = (float) mouseEyePosition.z;

        tstate.worldOri[0] = (float) mousePickDirection.x;
        tstate.worldOri[1] = (float) mousePickDirection.y;
        tstate.worldOri[2] = (float) mousePickDirection.z;
    }
}
