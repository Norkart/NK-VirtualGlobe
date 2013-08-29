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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLAppearanceChildNodeType;

/**
 * Node specifies point properties for point set.
 */
public interface VRMLPointPropertiesNodeType extends VRMLAppearanceChildNodeType {

    /** Constant used to set the fog type to disabled. */
    public static final int DISABLE_COLOR_MODE = 0;

    /** Constant used to set the color mode to texture mode. */
    public static final int TEXTURE_COLOR_MODE = 1;

    /** Constant used to set the color mode to point mode. */
    public static final int POINT_COLOR_MODE = 2;
    
    /** Constant used to set the color mode to point and texture mode. */
    public static final int TEXTURE_AND_POINT_COLOR_MODE = 3;
    
    /**
     * Accessor method to set a new value for field colorMode.
     * @param newColorMode The new value of colorMode
     */
    public void setColorMode (String newColorMode)
        throws InvalidFieldValueException;

    /**
     * Accessor method to get current value of field colorMode.
     * @return The current value of colorMode
     */
    public int getColorMode();
}
