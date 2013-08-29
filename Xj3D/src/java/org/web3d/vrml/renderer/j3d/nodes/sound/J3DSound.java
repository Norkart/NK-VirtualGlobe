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

package org.web3d.vrml.renderer.j3d.nodes.sound;

// Standard imports
import java.util.Map;
import java.util.Vector;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.ConeSound;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.MediaContainer;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point2f;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.common.nodes.sound.BaseSound;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLTimeListener;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;

/**
 * Sound node implementation.
 * <p>
 *
 *
 * @author Guy Carpenter
 * @version $Revision: 1.7 $
 */

public class J3DSound extends BaseSound
    implements J3DVRMLNode, VRMLTimeListener {

    /** Specifies the number of gain steps in the distance/gain ramp */
    private static final int DISTANCEGAIN_RESOLUTION = 10;
    /** Sound node used for audio output */
    private ConeSound sound;
    /** Sound distance ramp for front */
    private float[] frontDistance;
    /** Sound distance ramp for back */
    private float[] backDistance;
    /** Sound gain ramp for both front and back */
    private float[] gainRamp;
    /** Local copy of the audioclip isActive state */
    private boolean isActive;
    /** Local copy  of the audioclip loop state */
    private boolean loop;
    /** Local copy  of the audioclip pitch value */
    private float pitch;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Empty constructor.
     */
    public J3DSound() {

        // Check for Java3D > 1.3.1Beta or issue a warning
        Package pack = Package.getPackage("javax.media.j3d");
        if (pack != null) {
            if (!pack.getImplementationVersion().equals("1.3.1")) {
                System.out.println("WARNING: Sound support is dodgy prior to Java3D 1.3.1");
            }
        }

        loop = false;
        isActive = false;
        pitch = 1;
        int i;

        //-----------------------------------------------------------------
        // About Distance/Gain arrays.
        // The Java3D ConeSound maps pretty closely to the VRML sound node.
        // Unfortunately the implementation of the ConeSound seems pretty spotty.
        // In particular the elliptical attenuation parameters don't work
        // as advertised.  The problems include:
        // 1) linear interpolation doesn't seem to work.  Instead we do our
        //    own interpolation, using 10 steps between minFront & maxFront.
        // 2) setting parameters from 4xfloat[] doesn't work.  Use 2x2xfloat[] calls.
        // 3) different front/back parameters don't work.  Attenuation field
        //    seems to be spherical, not elliptical.  We live with that for now.
        // 4) attenuation is not clamped at the maximum value, but resets (to 1.0?)
        //    when you exceed the maximum distance.  To counter this, we
        //    add a final distance/gain pair at infinity.
        //-----------------------------------------------------------------

        // allocate distance/gain arrays
        frontDistance = new float[DISTANCEGAIN_RESOLUTION+2];
        backDistance = new float[DISTANCEGAIN_RESOLUTION+2];
        gainRamp = new float[DISTANCEGAIN_RESOLUTION+2];

        // initialize gain array from 1 to RESOLUTION with
        // linear descending ramp from 1 to 0.
        // Then set first and last
        // values to 1 and 0 respectively
        for (i=0;i<DISTANCEGAIN_RESOLUTION;i++) {
            gainRamp[i+1]=1.0f-(float)i/(float)DISTANCEGAIN_RESOLUTION;
        }
        gainRamp[0]=1.0f;
        gainRamp[DISTANCEGAIN_RESOLUTION+1]=0.0f;

        // the front and backDistance arrays will be populated
        // only when the appropriate fields are changed.  Here we
        // just initialize the static end points 0 and infinity.
        frontDistance[0]=0;
        backDistance[0]=0;
        frontDistance[DISTANCEGAIN_RESOLUTION+1]=Float.POSITIVE_INFINITY;
        backDistance[DISTANCEGAIN_RESOLUTION+1]=Float.POSITIVE_INFINITY;

        sound = new ConeSound();
        Point3f soundPos = new Point3f(0.0f, 0.0f, 0.0f);
        sound.setPosition(soundPos);

        // the default Java3D angular attenuation behaviour doesn't
        // match the VRML model.  Disable angular attenuation.
        // The following is from the ConeSound API:
        //   If the distance from the listener-sound-position
        //   vector and the sound's direction vector is greater
        //   than the last distance in the array, the last gain//
        //   scale factor and last filter are applied to the sound source.
        Point2f[] attenuation = new Point2f[] {new Point2f(0.0f,1.0f),
                                               new Point2f(1.5f,1.0f)};
        sound.setAngularAttenuation(attenuation);

        // REVISIT - should scheduling bounds be updated when
        // location and gain ramps are changed?
        BoundingSphere soundBounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        sound.setSchedulingBounds(soundBounds);
        sound.setCapability(ConeSound.ALLOW_SOUND_DATA_WRITE);
        sound.setCapability(ConeSound.ALLOW_ENABLE_WRITE);
        sound.setCapability(ConeSound.ALLOW_LOOP_WRITE);
        sound.setCapability(ConeSound.ALLOW_POSITION_WRITE);
        sound.setCapability(ConeSound.ALLOW_SCHEDULING_BOUNDS_WRITE);
        sound.setCapability(ConeSound.ALLOW_SCHEDULING_BOUNDS_WRITE);
        sound.setCapability(ConeSound.ALLOW_IS_PLAYING_READ);
        sound.setCapability(ConeSound.ALLOW_PAUSE_WRITE);
        sound.setCapability(ConeSound.ALLOW_PAUSE_READ);
        sound.setCapability(ConeSound.ALLOW_RELEASE_WRITE);
        sound.setCapability(ConeSound.ALLOW_LOOP_WRITE);
        sound.setCapability(ConeSound.ALLOW_DURATION_READ);
        sound.setCapability(ConeSound.ALLOW_RATE_SCALE_FACTOR_WRITE);
        sound.setCapability(ConeSound.ALLOW_DIRECTION_WRITE);
        sound.setCapability(ConeSound.ALLOW_INITIAL_GAIN_WRITE);
        sound.setCapability(ConeSound.ALLOW_DISTANCE_GAIN_WRITE);
        sound.setCapability(ConeSound.ALLOW_PRIORITY_WRITE);
        sound.setCapability(ConeSound.ALLOW_DISTANCE_GAIN_READ); // DEBUG ONLY
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Sound node, an exception will be
     * thrown. It does not copy the source node, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a compatible node
     */
    public J3DSound(VRMLNodeType node) {
        // because of the way this is called by the traversal
        // engine, it doesn't find the method in the parent class
        // so we call it explicitly
        this();
        copy(node);
    }

    //----------------------------------------------------------------------
    // regular class methods
    //----------------------------------------------------------------------

    /**
     * Called when the direction of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new direction values will be applied.
     *
     * @param direction float[3] vector indicating new direction.
     * @return true if the new value was accepted and applied.
     */
    public boolean setDirection(float[] direction)
    {
        if (super.setDirection(direction) && !inSetup) {
            sound.setDirection(direction[0],direction[1],direction[2]);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the intensity of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new intensity value will be used.
     *
     * @param intensity New intensity value [0,1]
     * @return true if the new value was accepted and applied.
     */
    public boolean setIntensity(float intensity)
    {
        if (super.setIntensity(intensity) && !inSetup) {
            sound.setInitialGain(intensity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the location of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new location value will be used.
     *
     * @param location New location value
     * @return true if the new value was accepted and applied.
     */
    public boolean setLocation(float[] location)
    {
        if (super.setLocation(location) && !inSetup) {
            sound.setPosition(location[0],location[1],location[2]);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the maxback distance of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new maxback value will be applied.
     *
     * @param maxBack New maxback distance
     * @return true if the new value was accepted and applied.
     */
    public boolean setMaxBack(float maxBack)
    {
        if (super.setMaxBack(maxBack) && !inSetup) {
            recomputeGain();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the maxfront distance of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new maxfront value will be applied.
     *
     * @param maxFront New maxfront distance
     * @return true if the new value was accepted and applied.
     */
    public boolean setMaxFront(float maxFront)
    {
        if (super.setMaxFront(maxFront) && !inSetup) {
            recomputeGain();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the minback distance of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new minback value will be applied.
     *
     * @param minBack New minback distance
     * @return true if the new value was accepted and applied.
     */
    public boolean setMinBack(float minBack)
    {
        if (super.setMinBack(minBack) && !inSetup) {
            recomputeGain();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Called when the minfront distance of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new minfront value will be applied.
     *
     * @param minFront New minfront distance
     * @return true if the new value was accepted and applied.
     */
    public boolean setMinFront(float minFront)
    {
        if (super.setMinFront(minFront) && !inSetup) {
            recomputeGain();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when the priority of the sound changes.
     * Invokes superclass method first, and if the returned
     * value is true, the new priority value will be applied.
     *
     * @params priority New priority value.
     * @return true if the new value was accepted and applied.
     */
    public boolean setPriority(float priority)
    {
        // NOTE - the Web3D priority scheme is much
        // more complicated than this.  Priority must be determined by:
        // a) decreasing priority;
        // b) for sounds with priority > 0.5, increasing (now-startTime);
        // c) decreasing intensity at viewer location
        //    (intensity×"intensity attenuation");
        // However as of Java3D 1.3.1b1, it appears that you cannot
        // modify the priority of an active sound.  This means that
        // (b) and (c) cannot be applied, so we can only implement (a).

        if (super.setPriority(priority)) {
            // called even if inSetup
            sound.setPriority(priority);
            return true;
        } else {
            return false;
        }
    }

    //----------------------------------------------------------------------
    // J3DVRMLNodeTypeType interface
    //----------------------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject()
    {
        return sound;
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
    // VRMLContentStateListener
    //----------------------------------------------------------------------
    public void contentStateChanged(VRMLNodeType node, int index, int state)
    {
        super.contentStateChanged(node,index,state);

        if (state==VRMLSingleExternalNodeType.LOAD_COMPLETE) {
            // get source node from BaseSound superclass
            // REVISIT - to support other audio sources,
            // we need to move getMediaContainer into a new
            // interface.
            J3DAudioClip clip = (J3DAudioClip)vfSource;

            //---- prepare audio source

            MediaContainer soundData = clip.getMediaContainer();

            sound.setSoundData(soundData);
            soundData.setCacheEnable(true);
            {
                long duration = sound.getDuration();
                double fduration;
                if (duration==ConeSound.DURATION_UNKNOWN) {
                    // the spec requires -1 for unknown or unspecified duration
                    fduration=-1.0;
                } else {
                    fduration = (double)duration/1000.0;
                }

                vfSource.setDuration(fduration);
            }
        }
    }

    //----------------------------------------------------------------------
    // VRMLSoundStateListener
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
                    sound.setLoop(1);
                    sound.setReleaseEnable(true);
                    sound.setEnable(false);
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
            if (isActive) {
                sound.setRateScaleFactor(pitch);
            }
        }

        // see if activestate has changed
        if (newIsActive != isActive) {
            isActive = newIsActive;
            if (newIsActive) {
                // source has gone active
                if (loop) {
                     // TODO: This doesn't seem to work
//                    sound.setLoop(ConeSound.INFINITE_LOOPS);
                    sound.setLoop(950000);

                    if (vrmlClock == null) {
                        // TODO: Why does this happen sometimes
                        System.out.println("vrmlClock Null in Sound");
                    } else
                        vrmlClock.addTimeListener(this);
                } else {
                    sound.setLoop(0);
                    // while active and not looping,
                    // we check each frame for completion

                    vrmlClock.addTimeListener(this);
                }
                soundSetup();
                sound.setEnable(true);
            } else {
                // source has gone inactive, force stop
                sound.setReleaseEnable(false);
                sound.setEnable(false);
                // This is required because of a bug in Java3D 1.3.1b1
                // sound scheduler.  By setting SetLoop(0) we ensure
                // the sound will be cleaned from the active list.
                sound.setLoop(0);

                vrmlClock.removeTimeListener(this);
            }
        }
    }

    //----------------------------------------------------------------------
    // VRMLTimeListener
    //----------------------------------------------------------------------

    /**
     * Check on each clock tick to see if the sound has
     * stopped playing.  If it has, we must either
     * restart it (if loop is true) or tell the sound
     * source that it has finished by calling setIsActive(false)
     *
     * @param time Current time
     */
    public void timeClick(long time)
    {
        // since we were called, we know audio is active

        boolean playing = sound.isPlaying();

        boolean playingSilently = sound.isPlayingSilently();
        if (!sound.isPlaying() &&
            !sound.isPlayingSilently()) {
            if (loop) {
//                sound.setLoop(ConeSound.INFINITE_LOOPS);
                sound.setLoop(950000);
                sound.setEnable(true);
            } else {
                // Loop is not set, so we set the sound to inactive
                // this will result in this listener being unregistered
                vfSource.setIsActive(false);
            }
        } else {
            // sound is still playing
        }
    }
    //----------------------------------------------------------------------
    // class private methods
    //----------------------------------------------------------------------

    /**
     * When any of the min/max-front/back distances
     * change, we recompute the new distance ramps here
     * and apply them to the sound.
     */
    private void recomputeGain()
    {
        int i;

        float minFront = vfMinFront;
        float maxFront = vfMaxFront;
        float minBack  = vfMinBack;
        float maxBack  = vfMaxBack;

        // Conesound requires that front gain is >= back gain.
        // however in J3D1.3 and J3D 1.3.1 beta it appears that
        // frontDistance must be > backDistance, so we nudge it
        // up slightly.
        if (minFront<=minBack) {
            minFront=minBack+0.1f;
        }
        if (maxFront<=maxBack) {
            maxFront=maxBack+0.1f;
        }

        for (i=0;i<DISTANCEGAIN_RESOLUTION;i++) {
            float f=(float)i/(float)DISTANCEGAIN_RESOLUTION;
            frontDistance[i+1]=minFront+f*(maxFront-minFront);
            backDistance[i+1]=minBack+f*(maxBack-minBack);
        }

        // NOTE - the call that sets both front and
        // back distance gains in the same call doesn't
        // work in the original release of the Java3D API.
        // Use these two discrete calls instead.
        sound.setDistanceGain(frontDistance,gainRamp);
        sound.setBackDistanceGain(backDistance,gainRamp);
    }

    /**
     * Applies the current direction, intensity, pitch,
     * location, min/max-front/back settings to the
     * sound object.
     */
    private void soundSetup()
    {
        sound.setDirection(vfDirection[0],vfDirection[1],vfDirection[2]);
        sound.setInitialGain(vfIntensity);
        sound.setRateScaleFactor(pitch);
        sound.setPosition(vfLocation[0],vfLocation[1],vfLocation[2]);
        recomputeGain();
    }
}

