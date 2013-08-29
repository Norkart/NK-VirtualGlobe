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

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Describes a Text string output geometry node in VRML .
 * </p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLTextNodeType extends VRMLGeometryNodeType {

    /**
     * Set the new fontstyle to render the text with. A null value will
     * clear the fontstyle and return the text to default rendering.
     *
     * @param fs The new fonstyle to use
     * @throws InvalidFieldValueException The node passed in is not a
     *    fontstyle or fontstyle proto.
     */
    public void setFontStyle(VRMLNodeType fs)
        throws InvalidFieldValueException;

    /**
     * Get the currently set fontstyle. If none is set, null is returned.
     *
     * @return The current fontstyle information
     */
    public VRMLNodeType getFontStyle();

    /**
     * Change the text to the new value. A null value will clear the current
     * text.
     *
     * @param text The new text to see
     */
    public void setText(String[] text);

    /**
     * Get the current text being rendered. If no text is available, returns
     * null.
     *
     * @return The text being rendered
     */
    public String[] getText();
}
