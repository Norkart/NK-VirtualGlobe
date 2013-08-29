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

// External imports
import javax.media.j3d.*;
import java.awt.image.*;

import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Map;

import org.j3d.util.ImageUtils;
import org.j3d.texture.TextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.texture.BaseImageTexture;

/**
 * Java3D implementation of a ImageTexture node.
 * <p>
 *
 * </ul>
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class J3DImageTexture extends BaseImageTexture
    implements J3DVRMLNode {

    /** Local shared cache of textures */
    private static J3DTextureCache cache;

    // Static constructor
    static {
        cache = J3DTextureCacheFactory.getCache(J3DTextureCacheFactory.WEAKREF_CACHE);
    }

    /**
     * Construct a default instance of this node
     */
    public J3DImageTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DImageTexture(VRMLNodeType node) {
        super(node);
    }

    //--------------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //--------------------------------------------------------------

    /**
     * Set the URL to a new value. If the value is null, it removes the old
     * contents (if set) and treats it as though there is no content.
     *
     * @param url The list of urls to set or null
     */
    public void setUrl(String[] newURL, int numValid) {
        super.setUrl(newURL, numValid);

        if(numValid == 0 && !inSetup)
            fireTextureImageChanged(0, this, null, null);
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {

        if (content == null) {
            return;
        }

        if (cache.checkTexture(loadedURI)) {
            fireTextureImageChanged(0, this, null, loadedURI);
            return;
        }

        RenderedImage implImage;

        if(content instanceof BufferedImage)
            implImage = (RenderedImage) content;
        else if (content instanceof ImageProducer) {
            implImage = (RenderedImage) ImageUtils.createBufferedImage((ImageProducer)content);

            if (implImage == null) {
                System.out.println("Failed to create buffered image for: " + loadedURI);
                // Notify listeners of new impl.
                fireTextureImageChanged(0, this, null, null);
                return;
            }
        }
        else {
            System.out.println("Unknown content type: " + content + " URL: " + loadedURI);
            // Notify listeners of new impl.
            fireTextureImageChanged(0, this, null, null);
            return;
        }

        fireTextureImageChanged(0, this, implImage, loadedURI);

        loadState = LOAD_COMPLETE;
        fireContentStateChanged();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureNodeType
    //----------------------------------------------------------

    /**
     * Get a string for cacheing this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    public String getCacheString(int stage) {
        return loadedURI;
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
}
