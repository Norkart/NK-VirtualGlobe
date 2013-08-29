/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External imports
import javax.media.opengl.GLCapabilities;

public interface SurfaceManager {
    /**
     * Reset the surface using the current capability bits.
     */
    public void resetSurface();

    /**
     * Get the current capability bits.
     */
    public GLCapabilities getCapabilities();
}