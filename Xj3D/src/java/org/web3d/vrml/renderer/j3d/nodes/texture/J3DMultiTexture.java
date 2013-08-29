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

package org.web3d.vrml.renderer.j3d.nodes.texture;

// External imports
import javax.media.j3d.SceneGraphObject;

import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTexture;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a MultiTexture node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class J3DMultiTexture extends BaseMultiTexture
    implements J3DVRMLNode {

    /** Mapping between a Tex handle and position */
    private HashMap texMap;
    private int texCnt;
    private boolean[] alphas;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Construct a default instance of this node
     */
    public J3DMultiTexture() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DMultiTexture(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        alphas = new boolean[texCnt];
    }


    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
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
     * format that includes its own animation engine.
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
     * Common internal initialisation stuff.
     */
    private void init() {
        texCnt = 0;
        texMap = new HashMap();
    }
}
