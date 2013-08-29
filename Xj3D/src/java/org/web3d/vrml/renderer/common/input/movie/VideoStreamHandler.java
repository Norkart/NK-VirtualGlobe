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

package org.web3d.vrml.renderer.common.input.movie;

// External imports
// None

// Local imports
import org.web3d.image.NIOBufferImage;

/**
 * Defines the interface used by the MovieDecoder to return
 * a video stream to the caller.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.4 $
 */
public interface VideoStreamHandler {

    /**
     * Called when a frame is available for display.
     *
     * @param image - the next image as an RGB format NIOBufferImage
     */
    public void videoStreamFrame(NIOBufferImage image);

    /**
     * Called when the format is known.
     *
     * @param width - horizontal image size in pixels
     * @param height - vertical image size in pixels
     */
    public void videoStreamFormat(int width, int height);

    /**
     * Called when the duration of the stream is known.
     *
     * @param seconds - number of seconds the stream runs for, or -1 if unknown.
     */
    public void videoStreamDuration(double seconds);

    /**
     * Called once before the first frame is sent.
     */
    public void videoStreamStart();

    /**
     * Called after the last frame is sent.
     */
    public void videoStreamStop();

}
