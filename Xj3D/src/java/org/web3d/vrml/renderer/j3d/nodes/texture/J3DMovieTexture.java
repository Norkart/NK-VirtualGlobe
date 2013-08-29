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

package org.web3d.vrml.renderer.j3d.nodes.texture;

// Standard imports
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Texture;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.security.AccessController;
import java.security.PrivilegedAction;


// Application specific imports
import org.web3d.util.PropertyTools;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMovieTexture;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

// DEBUGGING ONLY - REMOVE ME
//import org.web3d.util.Debug;

/**
 * Java3D implementation of a MovieTexture node.
 * <p>
 *
 * @author Guy Carpenter
 * @version $Revision: 1.9 $
 */
public class J3DMovieTexture extends BaseMovieTexture
   implements J3DVRMLNode {

    /** The array of listeners for isActive changes */
    private ArrayList textureListeners;

    /** Property describing the rescalling method to use */
    private static final String RESCALE_PROP =
        "org.web3d.vrml.nodes.loader.rescale";

    /** The default rescale method */
    private static final int DEFAULT_RESCALE =
        AffineTransformOp.TYPE_BILINEAR;

    private AffineTransformOp textureRescale;

    private BufferedImage nextFrame;
    private ImageComponent2D textureImageComponent;
    int textureWidth;
    int textureHeight;

    // REVISIT - maybe some of these can be locals?
    Texture2D textureImpl;
    BufferedImage textureImage;
    Texture[] textureArray;
    TextureAttributes[] textureAttributeArray;
    boolean[] textureAlphaArray;
    boolean firstFrame;


    /**
     * Constructors
     */
    public J3DMovieTexture()
    {
        textureListeners = new ArrayList();
        firstFrame = true;

        //Debug.trace("Constructing J3DMovieTexture");
    }

    public J3DMovieTexture(VRMLNodeType node)
    {
        this();
        //Debug.trace("Constructing J3DMovieTexture");
        // because of the way this is called by the traversal
        // engine, it doesn't find the method in the parent class
        // so we call it explicitly
        copy(node);
    }

    //----------------------------------------------------------------------
    // J3DVRMLNodeTypeType interface.
    //----------------------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject()
    {
        return null;
    }

    //----------------------------------------------------------------------
    // J3DVRMLNode interface
    //----------------------------------------------------------------------

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

    /**
     * In conjunction with the J3DTextureNodeType interface
     * we notify all of the texture listeners when something
     * changes.
     */
/*
    private void notifyTextureListeners()
    {
        //Debug.trace("notifyTextureListeners");

        J3DTextureListener listener;
        int n = textureListeners.size();
        for (int i=0;i<n;i++) {
            listener = (J3DTextureListener)textureListeners.get(i);
            listener.textureImplChanged(this,
                                        textureArray,
                                        textureAlphaArray,
                                        textureAttributeArray);
        }
    }
*/
    //----------------------------------------------------------------------
    // VideoStreamHandler interface
    //----------------------------------------------------------------------
    public void videoStreamFrame(BufferedImage image)
    {
        //Debug.trace();
        //nextFrame = scaleVideoFrame(image);
        //stateManager.addEndOfThisFrameListener(this);

        fireTextureImageChanged(0, this, image, (String)null);
    }

    public void videoStreamFormat(int width, int height)
    {
        //Debug.trace();
        textureWidth = nearestPowerTwo(width);
        textureHeight = nearestPowerTwo(height);
        initScale(width, height, textureWidth, textureHeight);
        initTexture();
    }

    public void videoStreamStart()
    {
        //Debug.trace();
        super.videoStreamStart();
    }

    public void videoStreamStop()
    {
        //Debug.trace();
        super.videoStreamStop();
    }

    //----------------------------------------------------------------------
    // Interface FrameStateListener
    //----------------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. If the node needs to update itself for this
     * frame, it should do so now before the render pass takes place.
     */
    public void allEventsComplete()
    {
        //Debug.trace("allEventsComplete");
        try {
            updateTextureImage(nextFrame);
        } catch (Exception e) {
            //Debug.trace("Exception "+e);
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------
    // Private methods
    //----------------------------------------------------------------------


    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    private BufferedImage scaleVideoFrame(BufferedImage imageIn)
    {
        BufferedImage imageOut;
        if (textureRescale==null) {
            imageOut = imageIn;
        } else {
            imageOut = textureRescale.filter(imageIn, null);
        }
        return imageOut;
    }
    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    private void initScale(int widthIn,
                           int heightIn,
                           int widthOut,
                           int heightOut)
    {
        if (widthIn==widthOut && heightIn==heightOut) {
            textureRescale = null;
        } else {
            // initialize affine transformation
            double xScale = (float)widthOut / (float)widthIn;
            double yScale = (float)heightOut / (float)heightIn;

            HashMap rescaleMap;
            rescaleMap = new HashMap(2);
            rescaleMap.put("BILINEAR",
                           new Integer(AffineTransformOp.TYPE_BILINEAR));
            rescaleMap.put("NEAREST_NEIGBOR",
                           new Integer(AffineTransformOp.TYPE_NEAREST_NEIGHBOR));
            int rescale = PropertyTools.fetchSystemProperty(RESCALE_PROP,
                                              DEFAULT_RESCALE,
                                              rescaleMap);
            AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
            textureRescale = new AffineTransformOp(at,rescale);
        }
    }

    private void initTexture()
    {
        //Debug.trace();
        textureImage =
            new BufferedImage(textureWidth,
                              textureHeight,
                              BufferedImage.TYPE_INT_RGB);

        textureImageComponent =
            new ImageComponent2D(ImageComponent2D.FORMAT_RGB,
                                 textureImage);
        textureImageComponent.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);

        textureImpl = new Texture2D(Texture2D.BASE_LEVEL,
                                    Texture2D.RGB,
                                    textureWidth,
                                    textureHeight);
        textureImpl.setImage(0, textureImageComponent);

        // Handle repeatS and repeatT fields
        textureImpl.setBoundaryModeS(vfRepeatS ? Texture2D.WRAP : Texture2D.CLAMP);
        textureImpl.setBoundaryModeT(vfRepeatT ? Texture2D.WRAP : Texture2D.CLAMP);

        textureArray = new Texture[] { textureImpl };
        textureAlphaArray = new boolean[] { false };
        TextureAttributes attr = new TextureAttributes();
        textureAttributeArray = new TextureAttributes[] { attr };
        firstFrame = true;
    }


    private void updateTextureImage(BufferedImage image)
    {
        //Debug.trace();
        //Debug.trace("textureHeight="+textureHeight+",Width="+textureWidth);
        //Debug.trace("image="+image.toString());
        //Debug.trace("component="+textureImageComponent.toString());

System.out.println("Updating texture");
        textureImageComponent.setSubImage(image,
                                   textureWidth,textureHeight,  // size
                                   0,0,      // source coord
                                   0,0);     // dest coord

        // we only need to notify the listeners on the first frame.
        // thereafter the above setSubImage call is all that we need.
/*
        if (firstFrame) {
            notifyTextureListeners();
            firstFrame = false;
        }
*/
    }
}

