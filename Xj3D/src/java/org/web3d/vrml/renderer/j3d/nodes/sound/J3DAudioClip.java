/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.sound;

// External imports
import java.util.Map;

import javax.media.j3d.MediaContainer;
import javax.media.j3d.SceneGraphObject;
import java.io.InputStream;
import java.io.BufferedInputStream;

// Local imports
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.common.nodes.sound.BaseAudioClip;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.io.StreamContentContainer;

/**
 * AudioClip node implementation for Java3D.
 * <p>
 *
 *
 * @author Guy Carpenter
 * @version $Revision: 1.8 $
 */
public class J3DAudioClip extends BaseAudioClip
    implements J3DVRMLNode {

    // java3d audio container for audio
    private MediaContainer clip;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Empty constructor.
     */
    public J3DAudioClip() {
        clip = new MediaContainer();
        clip.setCapability(MediaContainer.ALLOW_URL_READ);
        clip.setCapability(MediaContainer.ALLOW_URL_WRITE);
        clip.setCapability(MediaContainer.ALLOW_CACHE_WRITE);
        clip.setCacheEnable(true);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Sound node, an exception will be
     * thrown. It does not copy the source node, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a compatible node
     */
    public J3DAudioClip(VRMLNodeType node) {
        // because of the way this is called by the traversal
        // engine, it doesn't find the method in the parent class
        // so we call it explicitly
        this();
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

    public void setupFinished() {
        super.setupFinished();
//        fireSoundStateChanged();
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

    //----------------------------------------------------------------------
    // VRMLSingleExternalNodeType interface
    //----------------------------------------------------------------------

    /**
     * setContent is called when the content has been loaded.
     * We require that the content object be an InputStream
     */
    public void setContent(String mimetype,
                           Object content)
        throws IllegalArgumentException
    {
        if (content instanceof StreamContentContainer) {
            StreamContentContainer container = (StreamContentContainer)content;
            InputStream stream = container.getInputStream();
            clip.setInputStream(new BufferedInputStream(stream));
        } else {
            throw new IllegalArgumentException
                ("AudioClip object must be a StreamContentContainer");
        }
    }

    //----------------------------------------------------------------------
    // REVISIT
    // May need to move this into a new interface to support other
    // source sources (like a movie clip).
    //----------------------------------------------------------------------
    /**
     * Returns the renderer-specific media container for the
     * sound source.
     */
    public MediaContainer getMediaContainer()
    {
        return clip;
    }

}



