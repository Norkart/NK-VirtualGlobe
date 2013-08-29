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

package org.web3d.vrml.renderer.j3d.nodes.enveffects;

// Standard imports
import java.awt.image.*;

import javax.media.j3d.*;

import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.Map;

import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;
import org.j3d.texture.TextureCacheFactory;
import org.j3d.util.ImageUtils;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseBackground;
import org.web3d.vrml.renderer.j3d.nodes.J3DBackgroundNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;

/**
 * A node that can represents a VRML Background node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class J3DBackground extends BaseBackground
    implements J3DBackgroundNodeType, J3DPathAwareNodeType  {

    /** Property describing the minification filter to use */
    private static final String MINFILTER_PROP =
        "org.web3d.vrml.nodes.loader.minfilter";

    /** Property describing the maxification filter to use */
    private static final String MAGFILTER_PROP =
        "org.web3d.vrml.nodes.loader.maxfilter";

    /** Property describing the rescalling method to use */
    private static final String RESCALE_PROP =
        "org.web3d.vrml.nodes.loader.rescale";

    /** The default filter to use for magnification. */
    private static final int DEFAULT_MAGFILTER = Texture.NICEST;
    //private static final int DEFAULT_MAGFILTER = Texture.BASE_LEVEL_LINEAR;

    /** The value read from the system property for MAXFILTER */
    /** The default filter to use for minification. */
    private static final int DEFAULT_MINFILTER = Texture.NICEST;
    //private static final int DEFAULT_MINFILTER = Texture.BASE_LEVEL_LINEAR;

    /** The default rescale method */
    private static final int DEFAULT_RESCALE =
        AffineTransformOp.TYPE_BILINEAR;

    /** The value read from the system property for MAXFILTER */
    private static final int magfilter;

    /** The value read from the system property for MINFILTER */
    private static final int minfilter;

    /** The value read from the system property for RESCALE */
    private static final int rescale;


    /** J3D Implementation that we put the background geometry in */
    private Group backgroundImpl;

    /** Textures for each side */
    private Texture2D[] textureList;

    /** List of items that have changed since last frame */
    private boolean[] textureChangeFlags;

    /** The Texture cache in use */
    private J3DTextureCache cache;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    // Temp arrays for copying stuff for the scene graph path
    private Object[] tmpPathArray;
    private Node[] tmpNodeArray;

    /**
     * Static initializer for setting up the system properties
     */
    static {
        final HashMap minmagMap = new HashMap(8);
        minmagMap.put("NICEST", new Integer(Texture.NICEST));
        minmagMap.put("FASTEST", new Integer(Texture.FASTEST));
        minmagMap.put("BASE_LEVEL_POINT", new Integer(Texture.BASE_LEVEL_POINT));
        minmagMap.put("BASE_LEVEL_LINEAR", new Integer(Texture.BASE_LEVEL_LINEAR));
        minmagMap.put("LINEAR_SHARPEN", new Integer(Texture.LINEAR_SHARPEN));
        minmagMap.put("LINEAR_SHARPEN_RGB", new Integer(Texture.LINEAR_SHARPEN_RGB));
        minmagMap.put("LINEAR_SHARPEN_ALPHA", new Integer(Texture.LINEAR_SHARPEN_ALPHA));
        minmagMap.put("FILTER4", new Integer(Texture.FILTER4));

        final HashMap rescaleMap = new HashMap(2);
        rescaleMap.put("BILINEAR", new Integer(AffineTransformOp.TYPE_BILINEAR));
        rescaleMap.put("NEAREST_NEIGHBOR", new Integer(AffineTransformOp.TYPE_NEAREST_NEIGHBOR));

        int[] vars = (int[])AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    int[] ret_val = new int[3];
                    Integer i;
                    String prop = System.getProperty(MINFILTER_PROP);
                    if(prop != null) {
                        i = (Integer)minmagMap.get(prop);
                        ret_val[0] =
                            (i != null) ? i.intValue() : DEFAULT_MINFILTER;
                    } else
                        ret_val[0] = DEFAULT_MINFILTER;

                    prop = System.getProperty(MAGFILTER_PROP);
                    if(prop != null) {
                        i = (Integer)minmagMap.get(prop);
                        ret_val[1] =
                            (i != null) ? i.intValue() : DEFAULT_MAGFILTER;
                    } else
                        ret_val[1] = DEFAULT_MAGFILTER;

                    prop = System.getProperty(RESCALE_PROP);
                    if(prop != null) {
                        i = (Integer)rescaleMap.get(prop);
                        ret_val[2] = (i != null) ? i.intValue() : DEFAULT_RESCALE;
                    } else
                        ret_val[2] = DEFAULT_RESCALE;

                    return ret_val;
                }
            }
        );

        minfilter = vars[0];
        magfilter = vars[1];
        rescale = vars[2];
    }

    /**
     * Create a new, default instance of this class.
     */
    public J3DBackground() {
        super();

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     * <P>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    public J3DBackground(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DBackgroundNodeType interface.
    //----------------------------------------------------------

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling. If
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here or null
     */
    public SceneGraphPath getSceneGraphPath() {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        ObjectArray path_array = parentPathHandler.getParentPath(this);

        if(path_array == null)
            return null;

        int path_size = path_array.size();
        if((tmpPathArray == null) || tmpPathArray.length < path_size) {
            tmpPathArray = new Object[path_size];
            tmpNodeArray = new Node[path_size - 1];
        }

        path_array.toArray(tmpPathArray);
        Locale locale = (Locale)tmpPathArray[0];
        for(int i = 1; i < path_size; i++)
            tmpNodeArray[i - 1] = (Node)tmpPathArray[i];

        return new SceneGraphPath(locale, tmpNodeArray, backgroundImpl);
    }

    /**
     * Get the list of textures defined for this background that have changed
     * since the last frame. The array contains the textures in the order
     * back, front, left, right, top, bottom. If the texture hasn't changed is no texture defined, then that array element is null.
     *
     * @param changes An array to copy in the flags of the individual textures
     *   that have changed
     * @param textures The list of textures that have changed for this background.
     * @return true if anything changed since the last time
     */
    public boolean getChangedTextures(Texture2D[] textures, boolean[] changes) {
        boolean ret_val = false;

        for(int i = 0; i < 6; i++) {
            changes[i] = textureChangeFlags[i];
            if(textureChangeFlags[i]) {
                textures[i] = textureList[i];
                ret_val = true;
                textureChangeFlags[i] = false;
            }
        }

        return ret_val;
    }

    /**
     * Get the list of textures defined for this background. The array contains
     * the textures in the order front, back, left, right, top, bottom. If
     * there is no texture defined, then that array element is null.
     *
     * @return The list of textures for this background.
     */
    public Texture2D[] getBackgroundTextures() {
        return textureList;
    }

    //----------------------------------------------------------
    // Methods from the J3DPathAwareNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLMultiExternalNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This should
     * be one of the forms that the prefered class type call generates.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(int index, String mimetype, Object content)
        throws IllegalArgumentException {

        if(content == null)
            return;

        // All of these are screwed currently because we don't know which URL was
        // the final one that got loaded. So we punt and use the first one.
        switch(index) {
            case FIELD_BACK_URL:
                if(!checkForCached(vfBackUrl, BACK)) {
                    buildTexture(content, mimetype, BACK);
                    cache.registerTexture(textureList[BACK],
                                          loadedUri[FIELD_BACK_URL]);
                }

                textureChangeFlags[BACK] = true;
                break;

            case FIELD_FRONT_URL:
                if(!checkForCached(vfFrontUrl, FRONT)) {
                    buildTexture(content, mimetype, FRONT);
                    cache.registerTexture(textureList[FRONT],
                                          loadedUri[FIELD_FRONT_URL]);
                }

                textureChangeFlags[FRONT] = true;
                break;

            case FIELD_LEFT_URL:
                if(!checkForCached(vfLeftUrl, LEFT)) {
                    buildTexture(content, mimetype, LEFT);
                    cache.registerTexture(textureList[LEFT],
                                          loadedUri[FIELD_LEFT_URL]);
                }

                textureChangeFlags[LEFT] = true;
                break;

            case FIELD_RIGHT_URL:
                if(!checkForCached(vfRightUrl, RIGHT)) {
                    buildTexture(content, mimetype, RIGHT);
                    cache.registerTexture(textureList[RIGHT],
                                          loadedUri[FIELD_RIGHT_URL]);
                }

                textureChangeFlags[RIGHT] = true;
                break;

            case FIELD_TOP_URL:
                if(!checkForCached(vfTopUrl, TOP)) {
                    buildTexture(content, mimetype, TOP);
                    cache.registerTexture(textureList[TOP],
                                          loadedUri[FIELD_TOP_URL]);
                }

                textureChangeFlags[TOP] = true;
                break;

            case FIELD_BOTTOM_URL:
                if(!checkForCached(vfBottomUrl, BOTTOM)) {
                    buildTexture(content, mimetype, BOTTOM);
                    cache.registerTexture(textureList[BOTTOM],
                                          loadedUri[FIELD_BOTTOM_URL]);
                }

                textureChangeFlags[BOTTOM] = true;
                break;
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

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
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return backgroundImpl;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        backgroundImpl.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        backgroundImpl.setCapability(BranchGroup.ALLOW_DETACH);
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        checkForCached(vfBackUrl,  BACK);
        checkForCached(vfFrontUrl, FRONT);
        checkForCached(vfLeftUrl,  LEFT);
        checkForCached(vfRightUrl, RIGHT);
        checkForCached(vfTopUrl,   TOP);
        checkForCached(vfBottomUrl, BOTTOM);
    }


    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Internal initialization method used to construct the Java3D geometry.
     */
    private void init() {
        allParentPaths = new ObjectArray();
        backgroundImpl = new BranchGroup();

        textureList = new Texture2D[6];
        textureChangeFlags = new boolean[6];

        cache = J3DTextureCacheFactory.getCache(TextureCacheFactory.WEAKREF_CACHE);
    }

    /**
     * Internal convenience method to check for a cached texture
     *
     * @param urls The list of URLs to check
     * @param int The index in the texture list to look at
     * @return true if a cached version was found
     */
    private boolean checkForCached(String[] urls, int index) {

        boolean ret_val = false;
        if((urls != null) && (urls.length > 0)) {
            for(int i = 0; (i < urls.length) && !ret_val; i++) {
                if(cache.checkTexture(urls[i]) == true) {
                    try {
                        textureList[index] = (Texture2D)cache.fetchTexture(urls[i]);
                        loadState[index] = LOAD_COMPLETE;
                        ret_val = true;
                    } catch(IOException io) {
                        // ignore and reload
                    }
                }
            }
        }

        return ret_val;
    }

    /**
     * Convenience method to take a pre-built Image object and turn it into a
     * texture. Also register it in the cache.
     *
     * @param content The content object to process
     * @param mime The mime type accompanying the object
     * @param index The texture index for the textureList
     */
    private void buildTexture(Object content, String mime, int index) {

        BufferedImage img = null;
        boolean alpha = false;
        boolean premultAlpha = false;

        if(content instanceof BufferedImage) {
            img = (BufferedImage)content;

            // Hack for handling ImageLoader problems
            if(mime.equals("image/jpeg"))
                alpha = false;
            else if(mime.equals("image/png"))
                alpha = true;
            else if(mime.equals("image/gif"))
                alpha = false;
            else {
                System.out.println("Unknown type for BufferedImage, " +
                                   "assume alpa=false type:" + mime);
                alpha = false;
            }
        }
        else if(content instanceof ImageProducer) {
            img = ImageUtils.createBufferedImage((ImageProducer)content);

            // Determine Alpha
            ColorModel cm = img.getColorModel();
            alpha = cm.hasAlpha();
            premultAlpha = cm.isAlphaPremultiplied();
        } else {
            System.out.println("Unknown content type: " + content +
                               " for field " + getFieldDeclaration(index));
            return;
        }

        if(premultAlpha) {
            System.out.println("J3DBackground: Unhandled case where " +
                               "isAlphaPremultiplied = true");
        }

        int texType;
        int format = ImageComponent2D.FORMAT_RGBA;

        switch(img.getType()) {
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
                if(alpha)
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
                System.out.println("Unknown FORMAT for image: " + img);
        }

        int newWidth = nearestPowerTwo(img.getWidth());
        int newHeight = nearestPowerTwo(img.getHeight());

        img = scaleTexture(img, newWidth, newHeight);
        ImageComponent2D img_comp = null;

        try {
            img_comp = new ImageComponent2D(format, img, false, false);
            // These are needed for cacheing
            img_comp.setCapability(ImageComponent.ALLOW_FORMAT_READ);
            img_comp.setCapability(ImageComponent.ALLOW_SIZE_READ);
        } catch (Exception e) {
            System.out.println("Error creating background image: ");
            e.printStackTrace();
            return;
        }

        textureList[index] = createTexture(img_comp, alpha);
    }

    /**
     * Given an image component setup the texture for this node.
     * @param
     */
    private Texture2D createTexture(ImageComponent image, boolean alpha) {
        int tex_type = getTextureFormat(image);

        int width = image.getWidth();
        int height = image.getHeight();

        Texture2D texture =
            new Texture2D(Texture2D.BASE_LEVEL, tex_type, width, height);

        texture.setMinFilter(minfilter);
        texture.setMagFilter(magfilter);
        texture.setBoundaryModeS(Texture.CLAMP_TO_EDGE);
        texture.setBoundaryModeT(Texture.CLAMP_TO_EDGE);
        texture.setImage(0,image);
        texture.setCapability(Texture.ALLOW_IMAGE_READ);
        texture.setCapability(Texture.ALLOW_SIZE_READ);
        texture.setCapability(Texture.ALLOW_FORMAT_READ);

        return texture;
    }

    /**
     * Scale a texture.  Generally used to scale a texture to a power of 2.
     *
     * @param bi The texture to scale
     * @param newWidth The new width
     * @param newHeight The new height
     */
    private BufferedImage scaleTexture(BufferedImage bi, int newWidth, int newHeight) {
        int width = bi.getWidth();
        int height = bi.getHeight();
        if (width == newWidth && height == newHeight)
            return bi;

        System.out.println("Rescaling background to: " + newWidth +
                           " x " + newHeight);

        double xScale = (float)newWidth / (float)width;
        double yScale = (float)newHeight / (float)height;
        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);

        AffineTransformOp atop = new AffineTransformOp(at,rescale);

        return atop.filter(bi, null);
    }

    /**
      * From the image component format, generate the appropriate texture
      * format.
      *
      * @param comp The image component to get the value from
      * @return The appropriate corresponding texture format value
      */
     private int getTextureFormat(ImageComponent comp)
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
     * Determine the nearest power of two value for a given argument.
     * This function uses the formal ln(x) / ln(2) = log2(x)
     *
     * @return The power-of-two-ized value
     */
    private int nearestPowerTwo(int val) {
        int log = (int) Math.ceil(Math.log(val) / Math.log(2));
        return (int) Math.pow(2,log);
    }
}
