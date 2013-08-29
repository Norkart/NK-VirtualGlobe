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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
// None

// Local imports
// None

/**
 * Abstraction allowing for the management and coordination of the string-based
 * flags used by X3D to the integer masks used by Aviatrix3D.
 * <p>
 *
 * The implementation of this interface needs to keep track of who is using the
 * individual flags and to keep an appropriate mask set. Since both picking
 * groups and the picking sensors use the strings to filter the picking, we
 * need to make sure that the strings on both sides equate to the same bit
 * mask. That's the job of this class.
 * <p>
 *
 * X3D allows for unlimited numbers of potential object type flags, but AV3D
 * is limited to 31 flags due to using an int for the collection. Though it is
 * not expected that the user will use more than 31, the implementation will
 * make it's best attempt at making sure it can. It will reference count the
 * number used for a given name and when the count is back to zero, will return
 * that bit mask back to the available pool.
 *
 * <p>
 * <b>Special Cases</b>
 *
 * <ul>
 * <li>The <code>ALL</code> string is always converted to 0xFFFFFFFF.</li>
 * <li>The <code>NONE</code> string is always converted to 0x0</li>
 * <li>If more than 30 flags are currently registered, 0x8FFFFFFF is returned</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface OGLPickingFlagConvertor {

    /**
     * Add a new flag to the system and get told what bitmask to use.
     *
     * @param type The type string to use
     * @return An int bit mask to be applied
     */
    public int addObjectType(String type);

    /**
     * Notify the system that the flag is no longer being used by this node.
     *
     * @param type The type string to use
     */
    public void removeObjectType(String type);
}
