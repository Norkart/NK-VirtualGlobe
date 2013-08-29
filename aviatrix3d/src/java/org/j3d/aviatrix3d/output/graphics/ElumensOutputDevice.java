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

package org.j3d.aviatrix3d.output.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

import org.j3d.device.output.elumens.SPI;

/**
 * An extended form of the {@link GraphicsOutputDevice} interface that adds
 * methods that are specific to managing Elumens setup functionality.
 * <p>
 *
 * This implementation is capable of handling multichannel dome support with
 * configurable number of channels. The number of channels used can be modified
 * on the fly, but it is costly to do so as the render has to be reinitialized
 * each time.
 * <p>
 *
 * Most of the methods take which channel to write to. The constants are defined
 * both in this interface, which are just aliased values from
 * {@link org.j3d.device.output.elumens.SPI}. These are a set of bitmasks used
 * to control the channels.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ElumensOutputDevice extends GraphicsOutputDevice
{
    /** The only channel of a single-channel display */
    public static final int SPI_1C_FRONT = SPI.SPI_1C_FRONT;

    /** The left channel of a 2-channel display */
    public static final int SPI_2C_LEFT = SPI.SPI_2C_LEFT;

    /** The right channel of a 2-channel display */
    public static final int SPI_2C_RIGHT = SPI.SPI_2C_RIGHT;


    /** The left channel of a 3-channel display */
    public static final int SPI_3C_LEFT = SPI.SPI_3C_LEFT;

    /** The right channel of a 3-channel display */
    public static final int SPI_3C_RIGHT = SPI.SPI_3C_RIGHT;

    /** The top channel of a 3-channel display */
    public static final int SPI_3C_TOP = SPI.SPI_3C_TOP;

    /** The left channel of a 4-channel display */
    public static final int SPI_4C_LEFT = SPI.SPI_4C_LEFT;

    /** The left channel of a 4-channel display */
    public static final int SPI_4C_RIGHT = SPI.SPI_4C_RIGHT;

    /** The left channel of a 4-channel display */
    public static final int SPI_4C_TOP = SPI.SPI_4C_TOP;

    /** The left channel of a 4-channel display */
    public static final int SPI_4C_BOTTOM = SPI.SPI_4C_BOTTOM;

    /** The overlay channel above any projection */
    public static final int SPI_OC_FRONT  = SPI.SPI_OC_FRONT;

    /** The overlay channel above any projection for the 2-channel display */
    public static final int SPI_2C_INSERT = SPI.SPI_2C_INSERT;

    /** The border channel surrounding the 2-channel display */
    public static final int SPI_2C_BORDER = SPI.SPI_2C_BORDER;

    /** Select all available channels */
    public static final int SPI_ALL_CHAN = SPI.SPI_ALL_CHAN;

    /** Select all channels for a 2-channel display */
    public static final int SPI_ALL_2_CHAN = SPI.SPI_ALL_2_CHAN;

    /** Select all channels for a 3-channel display */
    public static final int SPI_ALL_3_CHAN = SPI.SPI_ALL_3_CHAN;

    /** Select all channels for a 4-channel display */
    public static final int SPI_ALL_4_CHAN = SPI.SPI_ALL_4_CHAN;


    /**
     * Set the number of channels to display.  Calling this
     * will cause a reinitialization of renderer.
     *
     * @param channels The number of channels to render.
     */
    public void setNumberOfChannels(int channels);

    /**
     * Set the channel lens position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelLensPosition(int channel, float x, float y, float z);

    /**
     * Set the channel eye position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelEyePosition(int channel, float x, float y, float z);

    /**
     * Set the screen orientation.  Allows the project to rotated in software
     * for different hardware setups.
     *
     * @param r The roll
     * @param p The pitch
     * @param v The yaw
     */
    public void setScreenOrientation(double r, double p, double v);

    /**
     * Set the channel size in pixels.
     *
     * @param channel The ID of the channel(s) to affect
     * @param height The height in pixels
     * @param width The width in pixels
     */
    public void setChannelSize(int channel, int height, int width);
}
