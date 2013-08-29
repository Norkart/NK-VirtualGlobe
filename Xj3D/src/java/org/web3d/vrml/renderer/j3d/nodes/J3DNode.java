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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Base node for all J3D implementations.
 * <p>
 * Each node will keep its own fieldDeclarations and fieldMaps.  These will be
 * created in a static constructor so only one copy per class will be created.
 * <p>
 * Each node will maintain its own LAST_*_INDEX which tells others what the
 * last field declared by this node.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class J3DNode extends AbstractNode implements J3DVRMLNode {

    /** Flag for the API being new enough to have frquency bit setting */
    protected static final boolean haveFreqBitsAPI =
        J3DGlobalStatus.haveFreqBitsAPI ;

    /**
     * Create a new instance of this node with the given node type name.
     * The isDEF field is set to false and inSetup set to true.
     *
     * @param name The name of the type of node
     */
    public J3DNode(String name) {
        super(name);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
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
