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

package org.web3d.vrml.renderer.j3d.nodes.surface;

// External imports
import java.awt.image.*;
import javax.media.j3d.*;


import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.Buffer;
import java.util.Map;

import org.j3d.renderer.java3d.overlay.Overlay;
import org.j3d.renderer.java3d.overlay.InteractiveTextureOverlay;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.j3d.nodes.*;
import org.web3d.vrml.renderer.common.nodes.surface.BaseImage2D;
import org.web3d.vrml.renderer.common.nodes.shape.TextureStage;

/**
 * Java3D implementation of a Image2D node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.26 $
 */
public class J3DImage2D extends BaseImage2D
    implements J3DOverlayItemNodeType,
               VRMLTextureListener,
               VRMLTimeDependentNodeType,
               MouseListener,
               MouseMotionListener {

    /** The overlay holding the image */
    private InteractiveTextureOverlay overlay;

    /** The J3D representation of the texture node */
    private VRMLTexture2DNodeType jTexture;

    /** The sim clock this node uses */
    private VRMLClock vrmlClock;

    /**
     * Construct a new default Overlay object
     */
    public J3DImage2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DImage2D(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSensorNodeType
    //----------------------------------------------------------

    /**
     * Accessor method to set a new value for the enabled field
     *
     * @param state The new enabled state
     */
    public void setEnabled(boolean state) {
        super.setEnabled(state);

        if(state) {
            overlay.addMouseListener(this);
            overlay.addMouseMotionListener(this);
        } else {
            overlay.removeMouseListener(this);
            overlay.removeMouseMotionListener(this);
        }
    }

    //-------------------------------------------------------------------
    // Methods defined by VRMLTimeDependentNodeType
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSurfaceChildNodeType
    //----------------------------------------------------------

    /**
     * Set the visibility state of the surface. A non-visible surface will
     * still take events and update, just not be rendered.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setVisible(boolean state) {
        super.setVisible(state);

        if(overlay != null)
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

        if (overlay != null) {
            if (parentVisibility == false)
                overlay.setVisible(false);
            else
                if (vfVisible)
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
    // Methods defined by J3DTextureListener
    //----------------------------------------------------------

    /**
     * Notification a texture has changed.
     *
     * @param tex The new texture impl
     * @param alpha Does this texture have an alpha channel
     */
    public void textureImplChanged(VRMLTextureNodeType node,
                                   Texture[] tex,
                                   boolean[] alpha,
                                   TextureAttributes[] attrs) {
        if(inSetup)
            return;

        int w = tex[0].getWidth();
        int h = tex[0].getHeight();

        overlay.setSize(w, h);
        overlay.setTexture((Texture2D)tex[0]);
        overlay.setVisible(vfVisible && parentVisibility);

        // only update the screen bounds if the auto compute is on
        if(vfBboxSize[0] == -1)
            screenBounds.width = w;

        if(vfBboxSize[1] == -1)
            screenBounds.height = h;

        if((vfBboxSize[0] == -1) || (vfBboxSize[1] == -1))
            fireSizeChange(screenBounds.width, screenBounds.height);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    RenderedImage image,
                                    String url) {
        Texture tex = createTexture((VRMLTextureNodeType)node, image);

        if (tex == null)
            return;

        int w = tex.getWidth();
        int h = tex.getHeight();

        overlay.setSize(w, h);
        overlay.setTexture((Texture2D)tex);
        overlay.setVisible(vfVisible && parentVisibility);

        // only update the screen bounds if the auto compute is on
        if(vfBboxSize[0] == -1)
            screenBounds.width = w;

        if(vfBboxSize[1] == -1)
            screenBounds.height = h;

        if((vfBboxSize[0] == -1) || (vfBboxSize[1] == -1))
            fireSizeChange(screenBounds.width, screenBounds.height);

System.out.println("Image2D not setup well for dynamic changes");
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    RenderedImage[] image,
                                    String[] url) {
        Texture tex = createTexture((VRMLTextureNodeType)node[0], image[0]);
        int w = tex.getWidth();
        int h = tex.getHeight();

        overlay.setSize(w, h);
        overlay.setTexture((Texture2D)tex);
        overlay.setVisible(vfVisible && parentVisibility);

        // only update the screen bounds if the auto compute is on
        if(vfBboxSize[0] == -1)
            screenBounds.width = w;

        if(vfBboxSize[1] == -1)
            screenBounds.height = h;

        if((vfBboxSize[0] == -1) || (vfBboxSize[1] == -1))
            fireSizeChange(screenBounds.width, screenBounds.height);

System.out.println("Image2D not setup well for dynamic changes");
    }

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image as a data buffer for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    Buffer image,
                                    String url) {
        // Not implemented yet
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images as data buffers for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    Buffer[] image,
                                    String[] url) {
        // Not implemented yet
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  3 Component color.
     */
    public void textureParamsChanged(int idx,
                                     int mode,
                                     int source,
                                     int function,
                                     float alpha,
                                     float[] color) {
        // Not implemented yet
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @len The number of valid entries in the arrays.
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3 component colors.
     */
    public void textureParamsChanged(int len,
                                     int mode[],
                                     int[] source,
                                     int[] function,
                                     float alpha,
                                     float[] color) {
        // Not implemented yet
    }

    //------------------------------------------------------------------------
    // Methods for MouseListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt) {
        vfIsActive = true;
        hasChanged[FIELD_ISACTIVE] = true;
        fireFieldChanged(FIELD_ISACTIVE);
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt) {
        vfIsActive = false;
        vfTouchTime = vrmlClock.getTime();

        hasChanged[FIELD_ISACTIVE] = true;
        hasChanged[FIELD_TOUCHTIME] = true;
        fireFieldChanged(FIELD_ISACTIVE);
        fireFieldChanged(FIELD_TOUCHTIME);
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt) {
        vfTouchTime = vrmlClock.getTime();

        hasChanged[FIELD_TOUCHTIME] = true;
        fireFieldChanged(FIELD_TOUCHTIME);
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt) {
        vfIsOver = true;
        hasChanged[FIELD_ISOVER] = true;
        fireFieldChanged(FIELD_ISOVER);
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt) {
        vfIsOver = false;
        hasChanged[FIELD_ISOVER] = true;
        fireFieldChanged(FIELD_ISOVER);
    }

    //------------------------------------------------------------------------
    // Methods for MouseMotionListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt) {
        vfTrackPoint[0] = evt.getX();
        vfTrackPoint[1] = evt.getY();

        if(vfWindowRelative) {
            vfTrackPoint[0] += screenLocation[0];
            vfTrackPoint[1] += screenLocation[1];
        }

        hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_TRACKPOINT_CHANGED);
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt) {
        vfTrackPoint[0] = evt.getX();
        vfTrackPoint[1] = evt.getY();

        if(vfWindowRelative) {
            vfTrackPoint[0] += screenLocation[0];
            vfTrackPoint[1] += screenLocation[1];

        }

        hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_TRACKPOINT_CHANGED);
    }


    //----------------------------------------------------------
    // Methods defined by J3DOverlayItemNodeType
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

        // Create overlay to handle implChanged events
        Dimension d = new Dimension();
        Texture2D texture = null;
        overlay = new InteractiveTextureOverlay(null, d, texture);
        overlay.initialize();


        if(jTexture != null) {
            Texture tex = createTexture(jTexture,null);
            if (tex != null)
                overlay.setTexture((Texture2D)tex);

/*
            Texture[] all_tex = jTexture.getTextures();

            if(all_tex != null) {
                if(!all_tex[0].isLive())
                    all_tex[0].setMagFilter(Texture2D.NICEST);

                d.width = all_tex[0].getWidth();
                d.height = all_tex[0].getHeight();

                texture = (Texture2D) all_tex[0];
            }
*/
        }

        if(jTexture == null)
            overlay.setVisible(false);
        else {
            overlay.setVisible(vfVisible && parentVisibility);
        }

        if(vfEnabled) {
            overlay.addMouseListener(this);
            overlay.addMouseMotionListener(this);
        }

        if(vfBboxSize[0] == -1 && vfBboxSize[1] == -1) {
            Rectangle b = overlay.getBounds();
            screenBounds.setBounds(b);
        }
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
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
     * Called to set the texture node to be used. May be overridden by the
     * derived class, but must also call this version first to ensure
     * everything is valid node types and the fields correctly set.
     *
     * @param texture The new texture node instance to use
     * @throws InvalidFieldValueException The node is not the required type
     */
    protected void setTextureNode(VRMLNodeType texture)
        throws InvalidFieldValueException {

        if((texture != null) && !(texture instanceof J3DVRMLNode))
            throw new InvalidFieldValueException("Texture not a J3D node");

        super.setTextureNode(texture);

        if(jTexture != null)
            jTexture.removeTextureListener(this);

        jTexture = (VRMLTexture2DNodeType)vfTexture;
        jTexture.addTextureListener(this);

        if(inSetup)
            return;

        Texture tex = createTexture(jTexture,null);

        if(tex == null)
            return;

        overlay.setTexture((Texture2D)tex);

        int w = tex.getWidth();
        int h = tex.getHeight();

        if(vfBboxSize[0] == -1)
            screenBounds.width = w;

        if(vfBboxSize[1] == -1)
            screenBounds.height = h;

        if((vfBboxSize[0] == -1) || (vfBboxSize[1] == -1))
            fireSizeChange(screenBounds.width, screenBounds.height);
    }

    /**
     * Create the Texture object for this stage.
     *
     * @param tex The VRML node instance to create the texture from
     * @param rim The AWT image to create the texture from
     */
    private Texture createTexture(VRMLTextureNodeType tex,
                                  RenderedImage rim) {

        int type = tex.getTextureType();
        RenderedImage[] images = null;
        RenderedImage img;

        switch(type) {
            case TextureConstants.TYPE_SINGLE_2D:
                // TODO: Images are now released which means getImage fails.
                if (rim != null)
                    img = rim;
                else
                    img = ((VRMLTexture2DNodeType)tex).getImage();

                if (img != null) {
                    images = new RenderedImage[1];
                    images[0] = img;
                }
                break;
            default:
                System.out.println("Unsupported Texture type in Image2D: " + type);
        }

        if (images == null) {
            return null;
        }

        Texture ret_val = null;

        int format = getFormat(images[0]);
        TextureStage tstage = new TextureStage(0);
        tstage.images = images;
        tstage.minFilter = TextureConstants.MINFILTER_NICEST;
        tstage.minFilter = TextureConstants.MAGFILTER_NICEST;

        ImageComponent[] comps;
        int len;
        int texType=0;
        int width;
        int height;

        VRMLTexture2DNodeType tex2d = (VRMLTexture2DNodeType) jTexture;
        if (tex2d.getRepeatS() == true)
            tstage.boundaryModeS = TextureConstants.BM_WRAP;
        else
            tstage.boundaryModeS = TextureConstants.BM_CLAMP;

        if (tex2d.getRepeatT() == true)
            tstage.boundaryModeT = TextureConstants.BM_WRAP;
        else
            tstage.boundaryModeT = TextureConstants.BM_CLAMP;

        // Create the Texture object
        len = tstage.images.length;

        comps = new ImageComponent2D[len];

        for(int i=0; i < len; i++) {
            // Force a copy so we know we can release the memory
            comps[i] = new ImageComponent2D(format,tstage.images[i],
                false,tstage.yUp);
            comps[i].setCapability(ImageComponent.ALLOW_FORMAT_READ);
            comps[i].setCapability(ImageComponent.ALLOW_SIZE_READ);
        }

        width = comps[0].getWidth();
        height = comps[0].getHeight();
        texType = getTextureFormat(comps[0]);

        if (tstage.generateMipMaps == false) {
            ret_val = new Texture2D(Texture2D.BASE_LEVEL, texType, width, height);
        } else {
            ret_val = new Texture2D(Texture2D.MULTI_LEVEL_MIPMAP, texType, width, height);
        }

        int val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeS);
        ret_val.setBoundaryModeS(val);

        val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeT);
        ret_val.setBoundaryModeT(val);

        val = J3DTextureConstConverter.convertMinFilter(tstage.minFilter);
        ret_val.setMinFilter(val);

        val = J3DTextureConstConverter.convertMagFilter(tstage.magFilter);
        ret_val.setMagFilter(val);

        ret_val.setImages(comps);
        ret_val.setCapability(Texture.ALLOW_FORMAT_READ);

        // Release reference to save memory
        for(int i=0; i < len; i++) {
            if (tstage.images[i] instanceof BufferedImage)
                ((BufferedImage)tstage.images[i]).flush();
        }

        tstage.images = null;

        return ret_val;
    }

    /**
      * From the image component format, generate the appropriate texture
      * format.
      *
      * @param comp The image component to get the value from
      * @return The appropriate corresponding texture format value
      */
     protected int getTextureFormat(ImageComponent comp)
     {
         int ret_val = Texture.RGB;

         switch(comp.getFormat())
         {
             case ImageComponent.FORMAT_CHANNEL8:
                 // could also be alpha, but we'll punt for now. We really need
                 // the user to pass in this information. Need to think of a
                 // good way of doing this.
                 ret_val = Texture.LUMINANCE;
                 break;

             case ImageComponent.FORMAT_LUM4_ALPHA4:
             case ImageComponent.FORMAT_LUM8_ALPHA8:
                 ret_val = Texture.LUMINANCE_ALPHA;
                 break;

             case ImageComponent.FORMAT_R3_G3_B2:
             case ImageComponent.FORMAT_RGB:
             case ImageComponent.FORMAT_RGB4:
             case ImageComponent.FORMAT_RGB5:
                ret_val = Texture.RGB;
                break;

             case ImageComponent.FORMAT_RGB5_A1:
 //            case ImageComponent.FORMAT_RGB8:
             case ImageComponent.FORMAT_RGBA:
             case ImageComponent.FORMAT_RGBA4:
 //            case ImageComponent.FORMAT_RGBA8:
                 ret_val = Texture.RGBA;
                 break;
         }

         return ret_val;
    }

    /**
      * From the image information, generate the appropriate ImageComponent type.
      *
      *
      * @param comp The image component to get the value from
      * @return The appropriate corresponding texture format value
      */
     protected int getFormat(RenderedImage image) {
        int format=0;

        if (image instanceof BufferedImage) {
            BufferedImage buffImage = (BufferedImage) image;
            boolean alpha = false;

            // Determine Alpha
            ColorModel cm = buffImage.getColorModel();

            alpha = cm.hasAlpha();

            switch(buffImage.getType()) {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_RGB:
                    format = ImageComponent2D.FORMAT_RGB;
                    break;

                case BufferedImage.TYPE_CUSTOM:
                    // no idea what this should be, so default to RGBA
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                    format = ImageComponent2D.FORMAT_RGBA;
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_USHORT_GRAY:
                    format = ImageComponent2D.FORMAT_CHANNEL8;
                    break;

                case BufferedImage.TYPE_BYTE_INDEXED:
                    if (alpha)
                        format = ImageComponent2D.FORMAT_RGBA;
                    else
                        format = ImageComponent2D.FORMAT_RGB;

                    break;

                case BufferedImage.TYPE_USHORT_555_RGB:
                    format = ImageComponent2D.FORMAT_RGB5;
                    break;

                case BufferedImage.TYPE_USHORT_565_RGB:
                    format = ImageComponent2D.FORMAT_RGB5;
                    break;
                default:
                    System.out.println("Unknown FORMAT for image: " + buffImage);
            }
        } else {
            System.out.println("RenderedImage assumed to be RGBA");
            format = ImageComponent2D.FORMAT_RGBA;
        }

        return format;
    }
}
