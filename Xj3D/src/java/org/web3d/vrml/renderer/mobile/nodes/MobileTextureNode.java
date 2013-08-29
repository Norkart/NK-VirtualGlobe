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

package org.web3d.vrml.renderer.mobile.nodes;

// Standard imports
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.renderer.mobile.nodes.MobileNode;
import org.web3d.vrml.renderer.mobile.nodes.MobileTextureNodeType;

/**
 * Abstract implementation of a texture object.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public abstract class MobileTextureNode extends MobileNode
    implements MobileTextureNodeType {

    /**
     * Construct a new base representation of a texture node
     *
     * @param name The name of the type of node
     */
    protected MobileTextureNode(String name) {
        super(name);
    }

    //----------------------------------------------------------
    // Helper methods for all textures
    //----------------------------------------------------------

    /**
     * Determine the nearest power of two value for a given argument.
     * This function uses the formal ln(x) / ln(2) = log2(x)
     *
     * @return The power-of-two-ized value
     */
    public int nearestPowerTwo(int val) {
        int log = (int) Math.ceil(Math.log(val) / Math.log(2));
        return (int) Math.pow(2,log);
    }
 }
