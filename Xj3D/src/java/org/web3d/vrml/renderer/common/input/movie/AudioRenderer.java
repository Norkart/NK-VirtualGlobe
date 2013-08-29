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

package org.web3d.vrml.renderer.common.input.movie;

// Standard imports
import javax.media.Format;
import javax.media.Buffer;
import javax.media.format.AudioFormat;

// Application specific imports
// none

// DEBUGGING ONLY - REMOVE ME
//import org.web3d.util.Debug;

/**
 * Implementation of the javax.media.renderer.AudioRenderer interface
 * for use within the MovieTexture node.  Captures the audio frames and
 * delivers them to the AudioStreamHandler.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.3 $
 */
class AudioRenderer extends Renderer
{
    private static AudioFormat supportedFormats[];

    //----------------------------------------------------------------------
    // static initializer
    //----------------------------------------------------------------------
    static {
        AudioFormat supportedPCM = new AudioFormat(AudioFormat.LINEAR);
        supportedFormats = new AudioFormat[1];
        supportedFormats[0] = supportedPCM;
    }

    //----------------------------------------------------------------------
    // PlugIn interface
    //----------------------------------------------------------------------
    /**
     * Gets the name of this plug-in as a human-readable string.
     *
     * @return - A String that contains the descriptive name of the plug-in.
     */
    public String getName()
    {
        //Debug.trace("");
        return "Xj3D Audio Renderer";
    }

    //----------------------------------------------------------------------
    // Renderer interface
    //----------------------------------------------------------------------
    /**
     * Lists the input formats supported by this Renderer.
     *
     * @return - An array of Format objects that represent the input
     * formats supported by this Renderer.
     */
    public Format[] getSupportedInputFormats()
    {
        //Debug.trace("");
        return supportedFormats;
    }

    /**
     * Processes the data and renders it to the output device
     * represented by this Renderer.
     *
     * @return BUFFER_PROCESSED_OK if the processing is
     * successful. Other possible return codes are defined in PlugIn.
     */
    public int process(Buffer buffer)
    {
        //Debug.trace();
        return BUFFER_PROCESSED_OK;
    }

}
