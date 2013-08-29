/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Description of a renderable object
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public interface VRMLShapeNodeType
    extends VRMLChildNodeType {
    /**
     * Get node content for the appearance field.
     *
     * @return The current appearance
     */
    public VRMLNodeType getAppearance();

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param newAppearance The new appearance.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    void setAppearance(VRMLNodeType newAppearance)
        throws InvalidFieldValueException;

    /**
     * Get node content for the geometry field.
     *
     * @return The current geoemtry field
     */
    public VRMLNodeType getGeometry();

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param newGeometry The new value for geometry.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType newGeometry)
        throws InvalidFieldValueException;
}
