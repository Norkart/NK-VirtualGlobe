/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
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

// Standard imports
import java.util.Map;
import java.util.Vector;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point2f;

import org.j3d.aviatrix3d.*;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.sound.BaseSound;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLTimeListener;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;

/**
 * Sound node implementation.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */

public class OGLSound extends BaseSound
    implements OGLVRMLNode, VRMLTimeListener, NodeUpdateListener {

    /** Local copy of the audioclip isActive state */
    private boolean isActive;

    /** Local copy  of the audioclip loop state */
    private boolean loop;

    /** Local copy  of the audioclip pitch value */
    private float pitch;

    /** The sound impl */
    private ConeSound impl;

    /** Newly loaded content */
    private AudioComponent newSource;

    /** Flag to say the source has changed for Aviatrix3D */
    private boolean sourceChanged;

    /** Flag to say the sound play state has changed */
    private boolean playStateChanged;

    /** Flag on what to do if the play state has changed */
    private boolean playSound;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Empty constructor.
     */
    public OGLSound() {

        loop = false;
        isActive = false;
        pitch = 1;

        playSound = false;
        playStateChanged = false;
        sourceChanged = false;

        impl = new ConeSound();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Sound node, an exception will be
     * thrown. It does not copy the source node, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a compatible node
     */
    public OGLSound(VRMLNodeType node) {
        // because of the way this is called by the traversal
        // engine, it doesn't find the method in the parent class
        // so we call it explicitly
        this();
        copy(node);
    }

    //----------------------------------------------------------------------
    // OGLVRMLNode interface
    //----------------------------------------------------------------------

    /**
     * Get the Aviatrix3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return impl;
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
        if (vfSpatialize == false) {
            impl.setRefDistance(Float.MAX_VALUE);
        }
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        
        if(sourceChanged) {
            impl.setAudioSource(newSource);
            
            sourceChanged = false;
            
            if (newSource == null) {
                impl.setEnabled(false);
            } else {
                impl.setEnabled(true);
            }
            
        }

        if(playStateChanged) {
            if(playSound)               
                impl.startSound();              
            else
                impl.stopSound();
        }

        newSource = null;
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        if(impl.isLive()) {
            impl.dataChanged(this);
        } else {
            updateNodeDataChanges(impl);
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLContentStateListener
    //----------------------------------------------------------------------

    public void contentStateChanged(VRMLNodeType node, int index, int state)
    {
        super.contentStateChanged(node,index,state);

        if (state==VRMLSingleExternalNodeType.LOAD_COMPLETE) {
            OGLAudioClip clip = (OGLAudioClip)vfSource;

            AudioComponent src = (AudioComponent) clip.getSceneGraphObject();

            if(impl.isLive()) {
                newSource = src;
                sourceChanged = true;
                stateManager.addEndOfThisFrameListener(this);
            } else {
                impl.setAudioSource(src);
                
                if (src == null) {
                    impl.setEnabled(false);
                } else {
                    impl.setEnabled(true);
                }

            }

            // TODO: Need to get duration
/*
                long duration = sound.getDuration();
                double fduration;
                if (duration==ConeSound.DURATION_UNKNOWN) {
                    // the spec requires -1 for unknown or unspecified duration
                    fduration=-1.0;
                } else {
                    fduration = (double)duration/1000.0;
                }

                vfSource.setDuration(fduration);
*/
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLSoundStateListener
    //----------------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param newIsActive The current value of the isActive field
     * @param newLoop The current value of the loop field
     * @param newPitch The current value of the pitch field
     */
    public void soundStateChanged(VRMLNodeType node,
                                  boolean newIsActive,
                                  boolean newLoop,
                                  float newPitch,
                                  double startTime)
    {
        
        if (newLoop != loop) {
            loop = newLoop;
            // loop changed
            if (isActive) {
                if (newLoop) {
                    // looping is being turned on
                    // have to wait until this playout finishes then reenable
                } else {
                    // looping was turned off
                }
                // in either case we have to start watching for the end
                // of the audio stream.  If looping was enabled then
                // we need to manually restart the stream when it ends.
                // If looping was diabled we need to set isActive(false)
                // when it completes.

                vrmlClock.addTimeListener(this);
            }
        }

        // update pitch if it has changed
        if (newPitch != pitch) {
            pitch = newPitch;
        }

        // see if activestate has changed
        if (newIsActive != isActive) {
            isActive = newIsActive;
            if (newIsActive) {
                // source has gone active
                if (loop) {
//                    sound.setLoop(ConeSound.INFINITE_LOOPS);

                    if (vrmlClock == null) {
                        // TODO: Why does this happen sometimes
                    } else
                        vrmlClock.addTimeListener(this);
                } else {
//                    sound.setLoop(0);
                    // while active and not looping,
                    // we check each frame for completion

                    vrmlClock.addTimeListener(this);
                }

                if(impl.isLive()) {
                    playStateChanged = true;
                    playSound = true;
                    impl.dataChanged(this);
                } else {
                    impl.startSound();
                }
            } else {
                if(impl.isLive()) {
                    playStateChanged = true;
                    playSound = false;
                    impl.dataChanged(this);
                } else
                    impl.stopSound();

                vrmlClock.removeTimeListener(this);
            }
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLTimeListener
    //----------------------------------------------------------------------

    /**
     * Check on each clock tick to see if the sound has
     * stopped playing.  If it has, we must either
     * restart it (if loop is true) or tell the sound
     * source that it has finished by calling setIsActive(false)
     *
     * @param time Current time
     */
    public void timeClick(long time) {
        boolean playing = impl.isPlaying();

        if (!playing && !loop) {
            vfSource.setIsActive(false);
        }
    }
}

