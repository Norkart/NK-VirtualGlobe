/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.sound;

// External imports
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import net.java.games.joal.util.ALut;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.AudioComponent;
import org.j3d.aviatrix3d.ByteAudioComponent;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.sound.BaseAudioClip;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.io.StreamContentContainer;

/**
 * AudioClip node implementation for OpenGL.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class OGLAudioClip extends BaseAudioClip
    implements OGLVRMLNode {

    /** The impl */
    private ByteAudioComponent clip;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Empty constructor.
     */
    public OGLAudioClip() {
        clip = new ByteAudioComponent();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Sound node, an exception will be
     * thrown. It does not copy the source node, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a compatible node
     */
    public OGLAudioClip(VRMLNodeType node) {
        // because of the way this is called by the traversal
        // engine, it doesn't find the method in the parent class
        // so we call it explicitly
        this();
        copy(node);
    }

    //----------------------------------------------------------------------
    // OGLVRMLNodeTypeType interface.
    //----------------------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject()
    {
        return clip;
    }

    public void setupFinished() {
        super.setupFinished();
    }

    /**
     * Set the loop variable.  Override the base class.
     */
    public void setLoop(boolean loop) {
        super.setLoop(loop);
        
        clip.setLoop(loop);
    }


    /**
     * Sets a new value for this node's pitch. The value must be > 0.
     *
     * @param newPitch New value for pitch.
     * @throws InvalidFieldValueException Pitch is <= 0
     */
    public void setPitch(float newPitch) throws InvalidFieldValueException {
        super.setPitch(newPitch);

        clip.setPitch(newPitch);
    }

    //----------------------------------------------------------------------
    // OGLVRMLNode interface
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
            StreamContentContainer stream = (StreamContentContainer) content;
            
            // handle various mimetypes
            if (mimetype.equals("audio/x-wav")) {
                               
                int[] format = new int[1];
                int[] size = new int[1];
                ByteBuffer[] data = new ByteBuffer[1];
                int[] freq = new int[1];
                int[] loop = new int[1];

                ALut.alutLoadWAVFile(
                    stream.getInputStream(),
                    format,
                    data,
                    size,
                    freq,
                    loop);

                clip.setData(format[0], freq[0], data[0], 0);
                //clip.setData(stream.getFormat(),stream.getFrequency(),stream.getBuffer(),0);
      
            } else {
                throw new IllegalArgumentException("Unsupported Audio MIME type, " + mimetype);
            }
            
        } else {
            throw new IllegalArgumentException
                ("AudioClip object must be a StreamContentContainer");
        }
    }
}



