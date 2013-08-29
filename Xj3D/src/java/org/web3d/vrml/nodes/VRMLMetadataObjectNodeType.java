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
package org.web3d.vrml.nodes;

import org.web3d.vrml.lang.InvalidFieldValueException;
/**
 * Marker interface that represents the X3DMetadataObject abstract data type
 * as basic datatype.
 * <p>
 *
 * All base nodes have a metadata ability added to them. However, only some
 * nodes can be the metadata itself. Therefore this type does not extend the
 * usual VRMLNodeType base interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLMetadataObjectNodeType {

    /**
     * Get the currently set name associated with this metadata object. If
     * none is set, this returns null.
     *
     * @return The current name
     */
    public String getName();

    /**
     * Set the name value for the metadata object. Use null to clear the
     * currently set name.
     *
     * @param name The name to use
     */
    public void setName(String name);

    /**
     * Get the currently set reference associated with this metadata object.
     * If none is set, this returns null.
     *
     * @return The current reference
     */
    public String getReference();

    /**
     * Set the reference value for the metadata object. Use null to clear the
     * currently set reference.
     *
     * @param reference The reference to use
     */
    public void setReference(String reference);
}
