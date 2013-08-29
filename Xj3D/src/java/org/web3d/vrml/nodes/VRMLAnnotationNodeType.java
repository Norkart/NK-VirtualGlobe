/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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

/**
 * Defines a node that is an annotation to be used in an AnnotationTarget.
 * <p>
 * <pre>
 * X3DAnnotationNode : X3DChildNode {
 *   SFString [in,out] annotationGroupID ""
 *   SFString [in,out] displayPolicy     "NEVER"  ["POINTER_OVER", "POINTER_ACTIVATE", "ALWAYS", "WHEN_VISIBLE", "NEVER"]
 *   SFBool   [in,out] enabled           TRUE
 *   SFNode   [in,out] metadata          NULL     [X3DMetadataObject]
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface VRMLAnnotationNodeType extends VRMLChildNodeType {

    /**
     * Get the current annotation group ID. If there is none set, this will
     * return an empty string.
     *
     * @return A string representing the group ID
     */
    public String getAnnotationGroupID();

    /**
     * Set the annotation group ID field to the new value. A null value or
     * zero length string will be treated as clearing the current value.
     *
     * @param id The new ID string to use
     */
    public void setAnnotationGroupID(String id);

    /**
     * Set a new enabled state for the annotation.
     *
     * @param state The new enabled value
     */
    public void setEnabled(boolean state);

    /**
     * Get current value of the enabled field. The default value is
     * <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled();

    /**
     * Get the policy for when this annotation should be displayed. The default
     * value is "NEVER".
     *
     * @return A string representing the current display policy
     */
    public String getDisplayPolicy();

    /**
     * Set the display policy field to the new value. The field must be a valid
     * string from the collection described in the class overview documentation.
     * Null values are treated as an error.
     *
     * @param policy The new display policy to use
     */
    public void setDisplayPolicy(String policy)
        throws InvalidFieldValueException;

}
