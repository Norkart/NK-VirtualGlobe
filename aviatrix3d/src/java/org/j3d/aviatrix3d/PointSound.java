/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import net.java.games.joal.AL;

// Local imports
import org.j3d.util.MatrixUtils;

/**
 * A PointSound class which emits sound in all directions from a point.
 * <p>
 * The sound will attenuate by distance based on the refDistance
 * and maxDistance parameters.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class PointSound extends Sound
{
    /** The OpenAL source */
    private int source;

    /** PointSound Position */
    private Point3f position;

    /** Scratch point */
    private Point3f tmpPoint;

    /** Scratch matrix val */
    private Matrix4f tmpMatrix;

    /** Matrix Utilities to invert matrices */
    private MatrixUtils matrixUtils;

    /** The distance when gain rolloff starts */
    protected float refDistance;

    /** Maximum distance of a source.  Used to clamp distance attenuation */
    protected float maxDistance;

    /** How quickly does the sound decrease over distance. */
    protected float rolloffFactor;

    /**
     * Creates a sound.
     */
    public PointSound()
    {
        position = new Point3f();
        tmpPoint = new Point3f();
        tmpMatrix = new Matrix4f();
        matrixUtils = new MatrixUtils();

        refDistance = 0;
        maxDistance = Float.MAX_VALUE;
        rolloffFactor = 1;
    }

    //----------------------------------------------------------
    // Methods defined by AudioRenderable
    //----------------------------------------------------------

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param al The al context to render with
     * @param transform The transformation stack to this node
     */
    public void render(AL al, Matrix4f transform)
    {
        transform.transform(position,tmpPoint);

        if (dataChanged)
        {
            buffer = soundSource.getBufferId(al, seq);

            if (buffer == -1)
                return;

            dataChanged = false;
            playChanged = true;

            // Bind buffer with a source.
            values.rewind();

            // Bind buffer with a source.
            al.alGenSources(1, values);
            source = values.get(0);

            if (al.alGetError() != AL.AL_NO_ERROR)
                System.out.println("Error creating OpenAL Source");

            al.alSourcei(source, AL.AL_BUFFER, buffer);
            al.alSourcef(source, AL.AL_REFERENCE_DISTANCE, refDistance);
            al.alSourcef(source, AL.AL_ROLLOFF_FACTOR, rolloffFactor);
            al.alSourcef(source, AL.AL_MAX_DISTANCE, maxDistance);
            al.alSourcef(source, AL.AL_PITCH, 1.0f);
            al.alSourcef(source, AL.AL_GAIN, 1.0f);

            al.alSource3f(source, AL.AL_POSITION, tmpPoint.x, tmpPoint.y,tmpPoint.z);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);
            al.alSourcef(source, AL.AL_PITCH, pitch);

            if (al.alGetError() != AL.AL_NO_ERROR)
                System.out.println("Error setting OpenAL Source params");

        }

        if (paramsChanged) {
            al.alSourcef(source, AL.AL_PITCH, pitch);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);

            paramsChanged = false;
        }

        al.alSource3f(source, AL.AL_POSITION, tmpPoint.x, tmpPoint.y,tmpPoint.z);

        if (playChanged)
        {
            if (playing && paused)
            {
                al.alSourcePlay(source);
            }

            if (playing)
            {
                if (paused)
                    al.alSourcePause(source);
                else {
                    al.alSourcePlay(source);
                }
            } else
                al.alSourceStop(source);

            playChanged = false;
        } else if (playing)
        {
            // Bind buffer with a source.
            values.rewind();

            al.alGetSourcei(source, AL.AL_SOURCE_STATE, values);
            if (values.get(0) == AL.AL_STOPPED)
            {
                playing = false;
            }
        }
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param al The al context to draw with
     */
    public void postRender(AL al)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        PointSound app = (PointSound)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof PointSound))
            return false;
        else
            return equals((PointSound)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the distance when gain rolloff starts.
     *
     * @param distance The distance in units
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setRefDistance(float distance)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        refDistance = distance;
    }

    /**
     * Set the distance when gain rolloff reaches zero.
     *
     * @param distance The distance in units
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setMaxDistance(float distance)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        maxDistance = distance;
    }

    /**
     * Set the rollloffFactor.
     *
     * @param factor
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setRolloffFactor(float factor)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        rolloffFactor = factor;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ps The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(PointSound ps)
    {
        if(ps == null)
            return 1;

        if(ps == this)
            return 0;

        return super.compareTo(ps);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ps The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(PointSound ps)
    {
        if(ps == this)
            return true;

        if(ps == null)
            return false;

        return super.equals(ps);
    }
}
