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

package org.web3d.vrml.renderer.common.nodes.text;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLFontStyleNodeType;

/**
 * Singleton representation of the default font information.
 * <p>
 *
 * Used by the individual renderers for when there is no fond information
 * declared, so that it saves the text node implementation from carrying
 * dual sets of variables.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultFontStyle extends BaseFontStyle {

    /** The global shared instance */
    private static DefaultFontStyle sharedInstance;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private DefaultFontStyle() {
    }

    /**
     * Fetch the globally shared instance of the default font information.
     * If one does not currently exist, it is created before returning.
     *
     * @return The global shared instance
     */
    public static VRMLFontStyleNodeType getDefaultFontStyle() {
        if(sharedInstance == null) {
            sharedInstance = new DefaultFontStyle();
            sharedInstance.setupFinished();
        }

        return sharedInstance;
    }
}
