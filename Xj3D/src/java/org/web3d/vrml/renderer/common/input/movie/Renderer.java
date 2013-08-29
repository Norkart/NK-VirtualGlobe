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
import javax.media.*;
import javax.media.format.*;

// Application specific imports
// none

// DEBUGGING ONLY - REMOVE ME
//import org.web3d.util.Debug;

/**
 * This base class is used for the VideoRenderer and AudioRenderer classes.
 * It defines a minimal renderer implementation.  The subclasses override
 * methods they need to hook.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.3 $
 */
class Renderer implements javax.media.Renderer {

    //----------------------------------------------------------------------
    // Controls interface
    //----------------------------------------------------------------------

    /**
     * Obtain the collection of objects that control the object that
     * implements this interface.
     * If no controls are supported, a zero length array is returned.
     *
     * @return the collection of object controls
     */
    public Object[] getControls()
    {
        //Debug.trace("");
        return new Object[0];
    }

    /**
     * Obtain the object that implements the specified Class or
     * Interface The full class or interface name must be used.  If
     * the control is not supported then null is returned.
     *
     * @return the object that implements the control, or null
     */
    public Object getControl(String controlType)
    {
        //Debug.trace("");
        return null;
    }

    //----------------------------------------------------------------------
    // PlugIn interface
    //----------------------------------------------------------------------

    /**
     * Gets the name of this plug-in as a human-readable string.
     *
     * @return A String that contains the descriptive name of the plug-in.
     */
    public String getName()
    {
        //Debug.trace("");
        return "Xj3D Renderer";
    }

    /**
     * Opens the plug-in software or hardware component and acquires
     * the resources that the plug-in needs to operate. All required
     * input and/or output formats have to be set on the plug-in
     * before open is called. Buffers should not be passed into the
     * plug-in without first calling this method.
     *
     * @throws ResourceUnavailableException - If all of the required
     * resources cannot be acquired.
     */
    public void open() throws ResourceUnavailableException
    {
        //Debug.trace("");
    }
    /**
     * Closes the plug-in component and releases the resources it was
     * using. No more data will be accepted by the plug-in after close
     * is called. A closed plug-in can be reinstated by calling open
     * again.
     */
    public void close()
    {
        //Debug.trace("");
    }

    /**
     * Resets the state of the plug-in. The reset method is typically
     * called if the end of media is reached or the media is
     * repositioned.
     */
    public void reset()
    {
        //Debug.trace("");
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
        return new Format[0];
    }

    /**
     * Sets the Format of the input data.
     *
     * @return The Format that was set. This is typically the
     * supported Format that most closely matches the specified
     * Format. If possible, the format fields that were not specified
     * are set to the preferred values in the returned Format. Returns
     * null if the specified Format is not supported.
     */
    public Format setInputFormat(Format format)
    {
        //Debug.trace("Format="+format.toString());
        return format;
    }

    /**
     * Initiates the rendering process. When start is called, the
     * renderer begins rendering any data available in its internal
     * buffers.
     */
    public void start()
    {
        //Debug.trace("");
    }

    /**
     * Halts the rendering process.
     */
    public void stop()
    {
        //Debug.trace("");
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
        //Debug.trace("");
        return BUFFER_PROCESSED_OK;
    }

}

