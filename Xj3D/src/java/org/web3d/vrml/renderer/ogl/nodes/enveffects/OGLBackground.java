/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.enveffects;

// External imports
import java.awt.image.AffineTransformOp;

import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.Map;

import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.util.PropertyTools;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.ogl.nodes.OGLBackgroundNodeType;
import org.web3d.vrml.renderer.ogl.nodes.TextureCache;

import org.web3d.vrml.renderer.common.nodes.enveffects.BaseBackground;

/**
 * OGL implementation of a Background node
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class OGLBackground extends BaseBackground
    implements OGLBackgroundNodeType {

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
    private static final int DEFAULT_MAGFILTER = Texture.MAGFILTER_NICEST;
    //private static final int DEFAULT_MAGFILTER = Texture.BASE_LEVEL_LINEAR;

    /** The value read from the system property for MAXFILTER */
    /** The default filter to use for minification. */
    private static final int DEFAULT_MINFILTER = Texture.MAGFILTER_NICEST;
    //private static final int DEFAULT_MINFILTER = Texture.BASE_LEVEL_LINEAR;

    /** The default rescale method */
    private static final int DEFAULT_RESCALE =
        AffineTransformOp.TYPE_BILINEAR;

    /** The default useTextureCache value */
    private static final boolean DEFAULT_USETEXTURECACHE = true;

    /** Property describing the rescalling method to use */
    private static final String USETEXTURECACHE_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.useTextureCache";

    /** The value read from the system property for TEXTURECACHE */
    private static final boolean useTextureCache;

    /** The Texture cache in use */
    private TextureCache cache;

    /** The value read from the system property for MAXFILTER */
    private static final int magfilter;

    /** The value read from the system property for MINFILTER */
    private static final int minfilter;

    /** The value read from the system property for RESCALE */
    private static final int rescale;

    /** Textures for each side */
    private Texture2D[] textureList;

    /** List of items that have changed since last frame */
    private boolean[] textureChangeFlags;

    /** A simple object to represent the background's place in the scene graph*/
    private Group implGroup;

    /**
     * Static initializer for setting up the system properties
     */
    static {
        final HashMap minMap = new HashMap(8);
        final HashMap magMap = new HashMap(8);
        magMap.put("NICEST", new Integer(Texture.MAGFILTER_NICEST));
        magMap.put("FASTEST", new Integer(Texture.MAGFILTER_FASTEST));
        magMap.put("BASE_LEVEL_POINT",
            new Integer(Texture.MAGFILTER_BASE_LEVEL_POINT));
        magMap.put("BASE_LEVEL_LINEAR",
            new Integer(Texture.MAGFILTER_BASE_LEVEL_LINEAR));
        magMap.put("LINEAR_SHARPEN",
            new Integer(Texture.MAGFILTER_LINEAR_DETAIL));
        magMap.put("LINEAR_SHARPEN_RGB",
            new Integer(Texture.MAGFILTER_LINEAR_DETAIL_RGB));
        magMap.put("LINEAR_SHARPEN_ALPHA",
            new Integer(Texture.MAGFILTER_LINEAR_DETAIL_ALPHA));
        //        magMap.put("FILTER4", new Integer(Texture.FILTER4));

        minMap.put("NICEST", new Integer(Texture.MINFILTER_NICEST));
        minMap.put("FASTEST", new Integer(Texture.MINFILTER_FASTEST));
        minMap.put("BASE_LEVEL_POINT",
            new Integer(Texture.MINFILTER_BASE_LEVEL_POINT));
        minMap.put("BASE_LEVEL_LINEAR",
            new Integer(Texture.MINFILTER_BASE_LEVEL_LINEAR));

        final HashMap rescaleMap = new HashMap(2);
        rescaleMap.put("BILINEAR",
            new Integer(AffineTransformOp.TYPE_BILINEAR));
        rescaleMap.put("NEAREST_NEIGHBOR",
            new Integer(AffineTransformOp.TYPE_NEAREST_NEIGHBOR));

        int[] vars = (int[])AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    int[] ret_val = new int[3];
                    Integer i;
                    String prop = System.getProperty(MINFILTER_PROP);
                    if(prop != null) {
                        i = (Integer)minMap.get(prop);
                        ret_val[0] =
                            (i != null) ? i.intValue() : DEFAULT_MINFILTER;
                    } else
                        ret_val[0] = DEFAULT_MINFILTER;

                    prop = System.getProperty(MAGFILTER_PROP);
                    if(prop != null) {
                        i = (Integer)magMap.get(prop);
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

        useTextureCache = PropertyTools.fetchSystemProperty(USETEXTURECACHE_PROP,
            DEFAULT_USETEXTURECACHE);
    }

    /**
     * Default constructor for a OGLBackground
     */
    public OGLBackground() {
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
    public OGLBackground(VRMLNodeType node) {
        super(node);

        init();
    }

    /**
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The null representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiExternalNodeType
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

        // rem: /////////////////////////////////////////////////////////////////
        // ??? - I have no idea what this comment means.
        /////////////////////////////////////////////////////////////////////////
        // All of these are screwed currently because we don't know which URL was
        // the final one that got loaded. So we punt and use the first one.
        /////////////////////////////////////////////////////////////////////////
        switch(index) {
        case FIELD_BACK_URL:
            buildTexture(content, BACK);
            textureChangeFlags[BACK] = true;
            break;

        case FIELD_FRONT_URL:
            buildTexture(content, FRONT);
            textureChangeFlags[FRONT] = true;
            break;

        case FIELD_LEFT_URL:
            buildTexture(content, LEFT);
            textureChangeFlags[LEFT] = true;
            break;

        case FIELD_RIGHT_URL:
            buildTexture(content, RIGHT);
            textureChangeFlags[RIGHT] = true;
            break;

        case FIELD_TOP_URL:
            buildTexture(content, TOP);
            textureChangeFlags[TOP] = true;
            break;

        case FIELD_BOTTOM_URL:
            buildTexture(content, BOTTOM);
            textureChangeFlags[BOTTOM] = true;
            break;
        }
    }

    /**
     * Get the list of textures defined for this background that have changed
     * since the last frame. The array contains the textures in the order
     * back, front, left, right, top, bottom. If the texture hasn't changed
     * is no texture defined, then that array element is null.
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
    // Local methods
    //----------------------------------------------------------

    /**
     * Internal initialization method used to construct the OGL geometry.
     */
    private void init() {
        implGroup = new Group();

        textureList = new Texture2D[6];
        textureChangeFlags = new boolean[6];

        if ( useTextureCache ) {
            cache = TextureCache.getInstance( );
        }
    }

    /**
     * Convenience method to take a pre-built Image object and turn it into a
     * texture. Also register it in the cache.
     *
     * @param content The content object to process
     * @param index The texture index for the textureList
     */
    private synchronized void buildTexture(Object content, int index) {

        String url = loadedUri[urlFieldIndexList[index]];

        NIOBufferImage img = null;
        if ( content instanceof NIOBufferImage ) {
            img = (NIOBufferImage)content;
        } else {
            return;
        }

        ByteBufferTextureComponent2D img_comp = new ByteBufferTextureComponent2D(
            getFormat( img ),
            img.getWidth( ),
            img.getHeight( ),
            img.getBuffer( ) );

        if ( img_comp != null ) {
            Texture2D texture = new Texture2D();
            texture.setMinFilter(Texture.MINFILTER_NICEST);
            texture.setMagFilter(Texture.MAGFILTER_NICEST);
            texture.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
            texture.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
            texture.setSources(Texture.MODE_BASE_LEVEL,
                Texture.FORMAT_RGB,
                new TextureSource[] { img_comp },
                1);

            if ( useTextureCache ) {
                textureList[index] = (Texture2D)cache.register( url, texture );
            }
            else {
                textureList[index] = texture;
            }
        }
    }

    /**
     * From the image information, generate the appropriate TextureComponent type.
     *
     * @param image The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    protected int getFormat( NIOBufferImage image ) {
        int format=0;
        NIOBufferImageType type = image.getType( );

        if ( type == NIOBufferImageType.INTENSITY ) {

            format = TextureComponent.FORMAT_SINGLE_COMPONENT;

        } else if ( type == NIOBufferImageType.INTENSITY_ALPHA ) {

            format = TextureComponent.FORMAT_INTENSITY_ALPHA;

        } else if ( type == NIOBufferImageType.RGB ) {

            format = TextureComponent.FORMAT_RGB;

        } else if ( type == NIOBufferImageType.RGBA ) {

            format = TextureComponent.FORMAT_RGBA;

        } else {

            System.out.println("Unknown NIOBufferImageType: " + type.name);
        }

        return( format );
    }
}
