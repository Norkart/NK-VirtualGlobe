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

package org.web3d.vrml.scripting;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;

/**
 * A wrapper abstract interface used to convert between the Xj3D
 * implementation specific details and the spec requirements for a script.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface ScriptWrapper {

    /**
     * Initialise the underlying script, based on the surrounding node's
     * details.
     *
     * @param node The implementation node describing the script
     */
    public void initialize(VRMLScriptNodeType node);

    /**
     * Set the timestamp that we shall start the next queue with.
     *
     * @param time The timestamp to use (in seconds)
     */
    public void setTimestamp(double time);

    /**
     * Call the prepareEvents() method on the script, if it has one. If it does
     * not, this becomes a no-op. Return true if something executed and therefore
     * we need to check on further events being sent.
     *
     * @return true if something executed
     */
    public boolean prepareEvents();

    /**
     * Queue a changed SFInt32 event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, int value);

    /**
     * Queue a changed MFInt32 event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, int[] value, int numValid);

    /**
     * Queue a changed SFLong event value ready for processing by
     * the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, long value);

    /**
     * Queue a changed MFLong event value ready for processing
     * by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, long[] value, int numValid);

    /**
     * Queue a changed SFBool event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, boolean value);

    /**
     * Queue a changed MFBool event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, boolean[] value, int numValid);

    /**
     * Queue a changed SFFloat event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, float value);

    /**
     * Queue a changed event value ready for processing by the script. Used to
     * set SFColor, SFRotation, SFVec2f, SFVec3f or MFFloat fields
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, float[] value, int numValid);

    /**
     * Queue a changed SFTime or SFDouble event value ready for processing by
     * the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, double value);

    /**
     * Queue a changed MFTime or MFDouble event value ready for processing
     * by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, double[] value, int numValid);

    /**
     * Queue a changed SFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, String value);

    /**
     * Queue a changed MFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, String[] value, int numValid);

    /**
     * Queue a changed SFNode event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, VRMLNodeType value);

    /**
     * Queue a changed MFNode event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type,
                           String name,
                           VRMLNodeType[] value,
                           int numValid);


    /**
     * Send the events to the real script now. The return value indicates if
     * any eventOuts of the script have changed. It is assumed that in this
     * period the directOutputs have changed (if the flag is set), but
     * eventOuts are not processed.
     *
     * @return true One or more output events nee to be sent
     */
    public boolean sendEvents();

    /**
     * Process the eventOuts of the script now. It should do that by calling
     * setValue() on the script node instance passed in as part of the the
     * initialize() method.
     */
    public void updateEventOuts();

    /**
     * Notification that the eventsProcessed() functionality should be called
     * on the script code now.
     */
    public void eventsProcessed();

    /**
     * Called when the Script node is deleted. We free everything here to
     * allow the GC to do its magic.
     */
    public void shutdown();
}

