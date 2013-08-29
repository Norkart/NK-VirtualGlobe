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

package org.web3d.vrml.renderer.j3d.nodes.surface;

// Standard imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

import org.j3d.renderer.java3d.overlay.Overlay;
import org.j3d.renderer.java3d.overlay.LabelOverlay;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFontStyleNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DOverlayItemNodeType;
import org.web3d.vrml.renderer.common.nodes.surface.BaseText2D;
import org.web3d.vrml.renderer.common.nodes.text.DefaultFontStyle;

/**
 * Java3D implementation of a Text2D node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class J3DText2D extends BaseText2D
    implements J3DOverlayItemNodeType {

    /** The overlay holding the image */
    private LabelOverlay overlay;

    /**
     * Construct a new default Overlay object
     */
    public J3DText2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DText2D(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLSurfaceChildNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the visibility state of the surface. A non-visible surface will
     * still take events and update, just not be rendered.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setVisible(boolean state) {
        super.setVisible(state);

        if(!inSetup)
            overlay.setVisible(state && parentVisibility);
    }

    /**
     * Notification from the parent node about this node's visiblity state.
     * Used to control the rendering so that if a parent is not visible it can
     * inform this node that it is also not visible without needing to stuff
     * with the local visibility state.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setParentVisible(boolean state) {
        super.setParentVisible(state);

        if(overlay != null) {
            if(parentVisibility == false)
                overlay.setVisible(false);
            else if(vfVisible)
                overlay.setVisible(true);
        }
    }

    /**
     * Tell this overlay that it's position in window coordinates has been
     * changed to this new value. Overrides the base class to set the overlay
     * position.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     */
    public void setLocation(int x, int y) {
        super.setLocation(x, y);

        if(!inSetup)
            overlay.setLocation(x, y);
    }

    //----------------------------------------------------------
    // Methods required by the J3DOverlayItemNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the overlay implementation used by this item.
     *
     * @return The overlay instance in use
     */
    public Overlay getOverlay() {
        return overlay;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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

        Dimension size = null;
        boolean fixed_size = false;

        if(screenBounds.width != 0 && screenBounds.height != 0) {
            size = new Dimension(screenBounds.width, screenBounds.height);
            fixed_size = true;
        }


        Color text_color = new Color(vfTextColor[0],
                                     vfTextColor[1],
                                     vfTextColor[2]);

        Color bg_color = new Color(vfBackgroundColor[0],
                                   vfBackgroundColor[1],
                                   vfBackgroundColor[2],
                                   vfTransparency);

        VRMLFontStyleNodeType style;

        if(vfFontStyle == null)
            style = DefaultFontStyle.getDefaultFontStyle();
        else
            style = vfFontStyle;

        Font font = style.getFont();

        int h_align = 0;
        int v_align = 0;

        switch(style.getHorizontalJustification()) {
            case VRMLFontStyleNodeType.FIRST_JUSTIFY:
            case VRMLFontStyleNodeType.BEGIN_JUSTIFY:
                h_align = LabelOverlay.LEFT_ALIGN;
                break;

            case VRMLFontStyleNodeType.MIDDLE_JUSTIFY:
                h_align = LabelOverlay.CENTER_ALIGN;
                break;

            case VRMLFontStyleNodeType.END_JUSTIFY:
                h_align = LabelOverlay.RIGHT_ALIGN;
                break;
        }

        switch(style.getVerticalJustification()) {
            case VRMLFontStyleNodeType.FIRST_JUSTIFY:
            case VRMLFontStyleNodeType.BEGIN_JUSTIFY:
                v_align = LabelOverlay.TOP_ALIGN;
                break;

            case VRMLFontStyleNodeType.MIDDLE_JUSTIFY:
                v_align = LabelOverlay.CENTER_ALIGN;
                break;

            case VRMLFontStyleNodeType.END_JUSTIFY:
                v_align = LabelOverlay.BOTTOM_ALIGN;
                break;
        }

        overlay = new LabelOverlay(null, size, vfString);
        overlay.setColor(text_color);
        overlay.setBackgroundColor(bg_color);
        overlay.setAntialiased(ANTIALIAS);
        overlay.setVisible(vfVisible && parentVisibility);
        overlay.setFont(font);
        overlay.setHorizontalAlignment(h_align);
        overlay.setVerticalAlignment(v_align);

        overlay.initialize();

        // If not fixed size, then update the screen bounds now that the
        // initialisation is complete
        if(vfBboxSize[0] == -1 && vfBboxSize[1] == -1) {
            Rectangle b = overlay.getBounds();
            screenBounds.setBounds(b);
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return overlay.getRoot();
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Notification by the base class to the derived class that the node should
     * be updated because one of the fields changed. The index is the field
     * index of the value that changed.
     *
     * @param index The index of the field that changed
     */
    protected void updateSurface(int index) {

        if(overlay == null) {
            System.out.println("Updating surface with a null no overlay, ignoring");
            return;
        }

        switch(index) {
            case FIELD_STRING:
                overlay.setVisibleLength(-1);
                overlay.setText(vfString);

                if((vfBboxSize[0] == -1) || (vfBboxSize[1] == -1)) {
                    Rectangle b = overlay.getBounds();
                    screenBounds.setBounds(b);
                    fireSizeChange(screenBounds.width, screenBounds.height);
                }
                break;

            case FIELD_TEXTCOLOR:
                Color c = new Color(vfTextColor[0],
                                    vfTextColor[1],
                                    vfTextColor[2]);
                overlay.setColor(c);
                break;

            case FIELD_BGCOLOR:
            case FIELD_TRANSPARENCY:
                c = new Color(vfBackgroundColor[0],
                              vfBackgroundColor[1],
                              vfBackgroundColor[2],
                              vfTransparency);
                overlay.setBackgroundColor(c);
                break;
        }

        overlay.repaint();
    }
}
