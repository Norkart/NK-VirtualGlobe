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

package org.web3d.vrml.renderer.common.nodes;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.VRMLTimeDependentNodeType;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLFieldData;

/**
 * An abstract representation of any form of time dependent node for
 * subclassing by specific implementations.
 * <p>
 * The implementation performs the basic handling of the time fields but does
 * not create any data structures for them to run with.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseTimeDependentNode extends AbstractNode
    implements VRMLTimeDependentNodeType {

    /** The sim clock this node uses */
    protected VRMLClock vrmlClock;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    protected BaseTimeDependentNode(String name) {
        super(name);
    }

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     * The vrmlClock provides all the information and listeners for keeping track
     * of time. If we are enabled at the time that this method is called we
     * automatically register the listener. Then, all the events that need
     * to be generated will be handled at the next vrmlClock tick we get issued.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
    }
}