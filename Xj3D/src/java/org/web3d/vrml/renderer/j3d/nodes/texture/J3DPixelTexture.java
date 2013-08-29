/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.texture;

// External imports
import java.awt.image.*;

import java.util.Map;

import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.Texture2D;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BasePixelTexture;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a PixelTexture node.
 * <p>
 *
 * Given a SFImage this will produce a Texture2D object
 *
 * TODO:
 *      Needs more testing
 * @author Alan Hudson
 * @version $Revision: 1.16 $
 */
public class J3DPixelTexture extends BasePixelTexture
    implements J3DVRMLNode {

    /** Class vars for performance */
    private int texHeight;

    /** The height of the texture */
    private int texWidth;


    // JC: These aren't being used right now, but they will be once we change
    // the getImage() call to return Object rather than RenderedImage.

    /** The TextureComponent format identifier */
    private int tcFormat;

    /** The AV3D Texture format identifier */
    private int texType;

    /**
     * Construct a default instance of this node.
     */
    public J3DPixelTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DPixelTexture(VRMLNodeType node) {
        super(node);
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
        return null;
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
     * Construct the texture from the image field data.
     *  All calls while inSetup is true are ignored by this method.
     */
    protected void processImageData() {
        if (vfImage == null)
            return;

        int width = vfImage[0];
        int height = vfImage[1];
        int components = vfImage[2];
        int imageType;
        DataBuffer buff = null;
        int[] bitMask;
        boolean hasAlpha = false;

        if ((width*height) != (vfImageLen-3))
            throw new InvalidFieldValueException(
                "Incorrect number of pixels. Expecting "+
                (width*height)+" and got "+(vfImageLen-3)+"."
            );

        if(components != 0) {
            switch(components) {
                case 1:
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                    tcFormat = ImageComponent.FORMAT_CHANNEL8;
                    texType = Texture2D.LUMINANCE;
                    bitMask = new int[1];
                    bitMask[0] = 0xFF;
                    break;

                case 2:
                    imageType = BufferedImage.TYPE_INT_ARGB;
//                    imageType = BufferedImage.TYPE_BYTE_GRAY;
//                    tcFormat = TextureComponent.FORMAT_INTENSITY_ALPHA;
//                    texType = Texture2D.FORMAT_INTENSITY_ALPHA;
                    bitMask = new int[4];
                    bitMask[0] = 0xFF000000;
                    bitMask[1] = 0x00FF0000;
                    bitMask[2] = 0x0000FF00;
                    bitMask[3] = 0x000000FF;
                    hasAlpha = true;

                    // Hack to satisfy Bug ID #113.
                    // Since Java has no way of directly creating an Image with
                    // grey + alpha, we create a 4-component version of same.
                    // Need to create a temporary array here to copy the image
                    // code over to an "rgb" image with all the colour
                    // components set the same.
                    int size = vfImageLen - 3;
                    int[] tmp_img = new int[size];
                    for(int i = 0; i < size; i++) {
                        int c = (vfImage[i + 3] & 0xFF00);
                        tmp_img[i] = (c << 16) | (c << 8) | vfImage[i + 3];
                    }

                    buff = new DataBufferInt(tmp_img, size, 0);
                    break;

                case 3:
                    imageType = BufferedImage.TYPE_INT_RGB;
                    tcFormat = ImageComponent.FORMAT_RGB;
                    texType = Texture2D.RGB;
                    bitMask = new int[3];
                    bitMask[0] = 0xFF0000;
                    bitMask[1] = 0x00FF00;
                    bitMask[2] = 0x0000FF;
                    break;

                case 4:
                    imageType = BufferedImage.TYPE_INT_ARGB;
                    tcFormat = ImageComponent.FORMAT_RGBA;
                    texType = Texture2D.RGBA;
                    bitMask = new int[4];
                    bitMask[0] = 0xFF000000;
                    bitMask[1] = 0x00FF0000;
                    bitMask[2] = 0x0000FF00;
                    bitMask[3] = 0x000000FF;
                    hasAlpha = true;
                    break;

                default:
                    errorReporter.warningReport("PixelTexture Unsupported #components: " +
                                                components, null);

                    // Its ugly, but leave in a safe state.
                    implImage = null;

                    if(!inSetup)
                        fireTextureImageChanged(0,this,null,null);
                    return;
            }

            // At this point, the image should be padded to
            // powers of two for width and height.

            // Is the performance worth it to keep these buffers?
            // If so, where in this pipeline do we start?
            if(buff == null)
                buff = new DataBufferInt(vfImage, vfImageLen - 3, 3);

            WritableRaster raster =
                java.awt.image.Raster.createPackedRaster(buff,
                                                         width,
                                                         height,
                                                         width,
                                                         bitMask,
                                                         null);

            implImage = new BufferedImage(width, height, imageType);
            ((BufferedImage)implImage).setData(raster);

        } else {
            // if you have no components, you see nothing.
            implImage = null;
        }

        if (!inSetup)
            fireTextureImageChanged(0, this,implImage, null);
    }
}
