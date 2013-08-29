/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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

import net.java.games.joal.AL;

// Local imports
// None

/**
 * A BackgroundSound class whichs emits a sound which doesn't change
 * by distance or orientation.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class BackgroundSound extends Sound
{
    /** The OpenAL source */
    private int source;

    /**
     * Creates a new background sound.
     */
    public BackgroundSound()
    {
    }

    //----------------------------------------------------------
    // Methods defined by AudioRenderable
    //----------------------------------------------------------

    /**
     * Check to see if this renderable is spatialised in any way. Spatialised
     * means it would require proper head tracking, where non-spatialised just
     * represents a basic noise such as background sound.
     * <p>
     * Background sound is never spatialised.
     *
     * @return false always
     */
    public boolean isSpatialised()
    {
        return false;
    }

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
        if (dataChanged)
        {
            buffer = soundSource.getBufferId(al, seq);

            if (buffer == -1)
                return;

            dataChanged = false;
            playChanged = true;

            // Bind buffer with a source.
            values.rewind();

            al.alGenSources(1, values);
            source = values.get(0);

            if (al.alGetError() != AL.AL_NO_ERROR)
                System.out.println("Error creating OpenAL Source");

            al.alSourcei(source, AL.AL_BUFFER, buffer);
            al.alSourcef(source, AL.AL_GAIN, 1.0f);

            al.alSource3f(source, AL.AL_POSITION, 0, 0, 0);
            al.alSourcei(source, AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);
            al.alSourcef(source, AL.AL_PITCH, pitch);

            if (al.alGetError() != AL.AL_NO_ERROR)
                System.out.println("Error setting OpenAL Source params");
        }

        if (paramsChanged)
        {
            al.alSourcef(source, AL.AL_PITCH, pitch);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);

            paramsChanged = false;
        }

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
                else
                    al.alSourcePlay(source);
            }
            else
                al.alSourceStop(source);

            playChanged = false;
        }
        else if (playing)
        {

            values.rewind();
            al.alGetSourcei(source, AL.AL_SOURCE_STATE, values);
            if (values.get(0) == AL.AL_STOPPED)
                playing = false;
        }
    }

    /**
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
        BackgroundSound app = (BackgroundSound)o;
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
        if(!(o instanceof BackgroundSound))
            return false;
        else
            return equals((BackgroundSound)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param bg The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(BackgroundSound bg)
    {
        if(bg == null)
            return 1;

        if(bg == this)
            return 0;

        return super.compareTo(bg);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param bg The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(BackgroundSound bg)
    {
        if(bg == this)
            return true;

        if(bg == null)
            return false;

        return super.equals(bg);
    }
}
