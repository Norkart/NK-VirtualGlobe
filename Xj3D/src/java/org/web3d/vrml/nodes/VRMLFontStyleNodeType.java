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
import java.awt.Font;

// Application specific imports
// none

/**
 * Describes a font in VRML.
 * </p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.8 $
 */
public interface VRMLFontStyleNodeType extends VRMLNodeType {

    /** The font style is PLAIN */
    public int PLAIN_STYLE = Font.PLAIN;

    /** The font style is ITALIC */
    public int ITALIC_STYLE = Font.ITALIC;

    /** The font style is BOLD */
    public int BOLD_STYLE = Font.BOLD;

    /** The font style is BOLDITALIC */
    public int BOLDITALIC_STYLE = Font.BOLD | Font.ITALIC;

    /** Justify to the beginning */
    public int BEGIN_JUSTIFY = 1;

    /** Justify to the end of the string */
    public int END_JUSTIFY = 2;

    /** Justify about the middle */
    public int MIDDLE_JUSTIFY = 3;

    /**
     * Justify about the first character of the line. FIRST and BEGIN are
     * equivalent values as per VRML spec.
     */
    public int FIRST_JUSTIFY = 4;

    /**
     * Fetch the AWT font description that matches the internal field settings.
     *
     * @return The font that is based on the fields
     */
    public Font getFont();

    /**
     * Get the horizontal justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justfication value
     */
    public int getHorizontalJustification();

    /**
     * Get the vertical justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justfication value
     */
    public int getVerticalJustification();

    /**
     * Get the spacing defintion for the lines of text.
     *
     * @return The font spacing information
     */
    public float getSpacing();

    /**
     * Get the size information for a single line of text. Font size is
     * already incorporated into the AWT Font information, but may also be
     * needed for the inter-line spacing.
     *
     * @return The font size information
     */
    public float getSize();

    /**
     * Get the value of the topToBottom flag. Returns true if the text strings
     * should be rendered from the top first, false for bottom first.
     *
     * @return true if rendering top to bottom
     */
    public boolean isTopToBottom();

    /**
     * Get the value of the leftToRight flag. Returns true if the text strings
     * should be rendered from the left side to the right or in reverse -
     * regardless of the original character encoding.
     *
     * @return true if rendering left to right
     */
    public boolean isLeftToRight();
}
