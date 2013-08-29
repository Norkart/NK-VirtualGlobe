/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.output.graphics;

// External imports
// None

// Local imports
// None

/**
 * Internal interface representing a renderer with stereo support.
 * <p>
 *
 * Provides some convenient abstraction methods.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface StereoRenderingProcessor extends RenderingProcessor
{
    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    public boolean isStereoAvailable();

    /**
     * Set the eye separation value when rendering stereo, defined as the
     * distance from the center axis to one eye. The default value is 0.33 for
     * most applications. The absolute value of the separation is always used.
     *
     * @param sep The amount of eye separation
     */
    public void setStereoEyeSeparation(float sep);

    /**
     * Get the current eye separation value, defined as the distance from the
     * center axis to one eye. If we are in no-stereo mode then this will
     * return zero.
     *
     * @return sep The amount of eye separation
     */
    public float getStereoEyeSeparation();
}
