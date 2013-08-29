/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// Standard imports

// Application specific imports
import org.web3d.vrml.nodes.*;
import mil.navy.nps.dis.*;

/**
 * Live List entries structure.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */

class LiveListEntry extends ListEntry {
    /** The DIS node */
    public VRMLDISNodeType node;

    /** Have new packets arrived */
    public boolean newPackets;

    /** The lastTime updated */
    public long lastTime;

    /** The average time of updates */
    public float avgTime;

    /** The last timestamp */
    public long espduTimestamp;

    /** The current espdu */
    public EntityStatePdu currEspdu;

    /** The current detonate pdu */
    public ProtocolDataUnit currDetonate;

    /** The current fire pdu */
    public ProtocolDataUnit currFire;

    /** A converger for translation */
    public OrderNVector3dConverger translationConverger;

    /** A converger for rotation */
    public OrderNQuat4dConverger rotationConverger;

    /** The last espdu, used to get deltas */
    public EntityStatePdu lastEspdu;

    /** The amount of time between packets */
    public float prevDt;

    /** Are we close enough to stop smoothing */
    public boolean closeEnough;

    /**
     * Constructor.
     */
    public LiveListEntry(VRMLDISNodeType node, long lastTime) {
        this.node = node;
        this.lastTime = lastTime;
        closeEnough = true;
        newPackets = false;
    }
}

