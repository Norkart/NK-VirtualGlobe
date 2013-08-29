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

package org.xj3d.ui.awt.device.j3d.elumens;

// External imports
import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Local imports
import org.j3d.device.output.elumens.MouseCoordinateSource;
import org.j3d.device.output.elumens.MouseCoordinateConverter;
import org.web3d.j3d.device.mouse.MouseTracker;

import org.xj3d.device.Tracker;
import org.xj3d.device.TrackerState;
import org.xj3d.device.ButtonModeConstants;

import org.web3d.vrml.renderer.j3d.input.DefaultSensorManager;

public class ElumensMouseTracker extends MouseTracker
    implements MouseCoordinateSource {
    private static final int mask = MASK_PICKING | MASK_POSITION | MASK_ORIENTATION;
    TrackerState tstate;

    /** Representation of the direction that we pick in */
    private Vector3d mousePickDirection;

    /** Representation of the eye in the world */
    private Point3d mouseEyePosition;

    /** The eye position in the image plate (canvas) world coords */
    private Point3d mousePosition;

    /** The Transform3D to read the view VWorld coordinates */
    private Transform3D viewTransform;

    private double[] coord;
    private MouseCoordinateConverter conv;
    private Canvas3D canvas;
    private int lastState;
    private boolean converting;

    public ElumensMouseTracker(MouseCoordinateConverter conv) {
        mousePickDirection = new Vector3d(0, 0, -1);
        mouseEyePosition = new Point3d();
        mousePosition = new Point3d();

        viewTransform = new Transform3D();
        tstate = new TrackerState();
        coord = new double[3];
        this.conv = conv;
        converting = false;
    }

    //------------------------------------------------------------------------
    // Methods for Tracker
    //------------------------------------------------------------------------

    /**
     * What action types does this sensor return.  This a combination
     * of ACTION masks.
     *
     * @param The action mask.
     */
    public int getActionMask() {
        return mask;
    }

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

        state.buttonMode[0] = ButtonModeConstants.NAV1;
        state.buttonMode[1] = ButtonModeConstants.NAV2;
        state.buttonMode[2] = ButtonModeConstants.NAV3;

        for(int i=0; i < tstate.buttonState.length; i++) {
            state.buttonState[i] = tstate.buttonState[i];
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
        if (!converting) {
            lastState = TrackerState.TYPE_PRESS;

            coord[0] = evt.getX();
            coord[1] = evt.getY();
            canvas = (Canvas3D) evt.getSource();

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

            converting = true;
            conv.registerInterest(this);
        }
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt) {
        // Get this even if we are converting, assume the last data is ok
        lastState = TrackerState.TYPE_RELEASE;
        if (!converting) {
            coord[0] = evt.getX();
            coord[1] = evt.getY();
            canvas = (Canvas3D) evt.getSource();

            converting = true;
            conv.registerInterest(this);
        }
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
        if (!converting) {
            if (tstate.actionType == TrackerState.TYPE_NONE ||
                tstate.actionType == TrackerState.TYPE_DRAG) {

                lastState = TrackerState.TYPE_DRAG;
                coord[0] = evt.getX();
                coord[1] = evt.getY();
                canvas = (Canvas3D) evt.getSource();

                converting = true;
                conv.registerInterest(this);
            }
        }
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt) {
        if (!converting) {
            if (tstate.actionType == TrackerState.TYPE_NONE ||
                tstate.actionType == TrackerState.TYPE_DRAG) {

                lastState = TrackerState.TYPE_MOVE;
                coord[0] = evt.getX();
                coord[1] = evt.getY();
                canvas = (Canvas3D) evt.getSource();

                converting = true;
                conv.registerInterest(this);
            }
        }
    }

    //------------------------------------------------------------------------
    // Methods for MouseCoordinateSource methods
    //------------------------------------------------------------------------
    public void update() {
        float screenHeight = canvas.getHeight() / 1.5f;
        float screenWidth = canvas.getWidth() / 1.5f;

        tstate.devicePos[0] = (float) coord[0] / screenWidth;
        tstate.devicePos[1] = (float) coord[1] / screenHeight;
        tstate.devicePos[2] = 0;

        conv.warpMouseCoordinate(coord);

        tstate.actionType = lastState;

        // TODO: Can we avoid this copy?
        canvas.getImagePlateToVworld(viewTransform);

        mousePosition.x = coord[0];
        mousePosition.y = coord[1];
        mousePosition.z = coord[2];

        // TODO: Don't do this every frame

        canvas.getCenterEyeInImagePlate(mouseEyePosition);

        viewTransform.transform(mouseEyePosition);

        mousePickDirection.sub(mousePosition, mouseEyePosition);

        tstate.worldPos[0] = (float) mouseEyePosition.x;
        tstate.worldPos[1] = (float) mouseEyePosition.y;
        tstate.worldPos[2] = (float) mouseEyePosition.z;

        tstate.worldOri[0] = (float) mousePickDirection.x;
        tstate.worldOri[1] = (float) mousePickDirection.y;
        tstate.worldOri[2] = (float) mousePickDirection.z;

        converting = false;
    }
}
