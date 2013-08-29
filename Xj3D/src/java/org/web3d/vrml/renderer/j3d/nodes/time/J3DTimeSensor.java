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

package org.web3d.vrml.renderer.j3d.nodes.time;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.time.BaseTimeSensor;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a TimeSensor node.
 * <p>
 *
 * The implementation uses the standard VRML time vrmlClock to send and retrieve
 * time information. As an efficiency measure, if the time sensor is disabled
 * it will remove itself as a listener to the global vrmlClock. When it becomes
 * re-enabled that listener will be added back again.
 * <p>
 *
 * When setting values we always set the variable first and then set the flag
 * indicating that the field has changed. This is so that we don't end up with
 * a multi-threaded access thinking that a value has changed when it really
 * hasn't (yet) and then ignoring the value. As we desperately try to avoid
 * synchronized access internally, we try to set the real value first so that
 * if someone does check and find it's changed, we can then give them the real
 * value as soon as they ask for it.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class J3DTimeSensor extends BaseTimeSensor
    implements J3DVRMLNode {

    /**
     * Construct a new time sensor object.
     */
    public J3DTimeSensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DTimeSensor(VRMLNodeType node) {
        super(node);
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
}
