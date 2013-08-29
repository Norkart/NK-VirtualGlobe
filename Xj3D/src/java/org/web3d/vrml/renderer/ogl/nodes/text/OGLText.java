/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.text;

// External imports
import java.awt.Font;
import java.util.HashMap;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.geom.CharacterCreator;
import org.j3d.renderer.aviatrix3d.geom.Text2D;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFontStyleNodeType;

import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.text.BaseText;
import org.web3d.vrml.renderer.common.nodes.text.DefaultFontStyle;

/**
 * NoRender implementation of a Text
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class OGLText extends BaseText
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The aviatrix3d text representation */
    private Text2D implText;

    /** Cache for character creators */
    private static HashMap characterCache;

    static {
        characterCache = new HashMap();
    }

    /**
     * Construct a new default instance of this class.
     */
    public OGLText() {
        super(false);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLText(VRMLNodeType node) {
        super(node, false);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        implText.setText(vfString, numString);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implText;
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return implText;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // Character creator needed first
        VRMLFontStyleNodeType font_style =
            (vfFontStyle == null) ?
            DefaultFontStyle.getDefaultFontStyle() :
            vfFontStyle;

        Font f = font_style.getFont();

        CharacterCreator chcr;

        chcr = (CharacterCreator) characterCache.get(f);

        if (chcr == null) {
            chcr = new CharacterCreator(f, 0.01);

            // TODO: It seems like strings of size characters below this number don't work
            characterCache.put(f, chcr);
        }

        implText = new Text2D(chcr);
// ogl.input.DefaultUserInputHandler is currently casting all picked geometry
// to VertexGeometry. This causes a crash, so stop any form of picking on this
// text for now
//implText.setPickMask(0);
        implText.setText(vfString, numString);
        implText.setSize(font_style.getSize());
        implText.setSpacing(font_style.getSpacing());
        implText.setLeftToRight(font_style.isLeftToRight());
        implText.setTopToBottom(font_style.isTopToBottom());

        switch(font_style.getHorizontalJustification()) {
            case VRMLFontStyleNodeType.BEGIN_JUSTIFY:
            case VRMLFontStyleNodeType.FIRST_JUSTIFY:
                implText.setHorizontalJustification(Text2D.JUSTIFY_BEGIN);
                break;

            case VRMLFontStyleNodeType.MIDDLE_JUSTIFY:
                implText.setHorizontalJustification(Text2D.JUSTIFY_MIDDLE);
                break;

            case VRMLFontStyleNodeType.END_JUSTIFY:
                implText.setHorizontalJustification(Text2D.JUSTIFY_END);
                break;
        }

        switch(font_style.getVerticalJustification()) {
            case VRMLFontStyleNodeType.BEGIN_JUSTIFY:
            case VRMLFontStyleNodeType.FIRST_JUSTIFY:
                implText.setVerticalJustification(Text2D.JUSTIFY_BEGIN);
                break;

            case VRMLFontStyleNodeType.MIDDLE_JUSTIFY:
                implText.setVerticalJustification(Text2D.JUSTIFY_MIDDLE);
                break;

            case VRMLFontStyleNodeType.END_JUSTIFY:
                implText.setVerticalJustification(Text2D.JUSTIFY_END);
                break;
        }

        // Lengths and max extent not handled currently.
    }

    /**
     * Convenience method to set the text information. May be overridden by
     * the derived class, but should call this method first to ensure the
     * field values are properly set.
     *
     * @param str The string(s) to set
     */
    public void setText(String[] str) {
        super.setText(str);

        if (inSetup)
            return;

        if (implText.isLive())
            implText.boundsChanged(this);
        else
            updateNodeBoundsChanges(implText);
    }
}
