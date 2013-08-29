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

package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Marker interface that shows a class can contain an underlying
 * {@link VRMLNodeType} instance.
 * <p>
 *
 * This is used to allow both the ordinary SFNode as well as the script
 * wrapper ("this") to be used in calls like addRoute().
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface NodeImplSource {

    /**
     * Get the underlying node that this object represents.
     *
     * @return The node reference
     */
    public VRMLNodeType getImplNode();
}
