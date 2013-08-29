/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.sound;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

import  javax.sound.midi.*;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a MIDI Event Source.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class BaseMidiSource extends AbstractNode
    implements VRMLChildNodeType, Receiver  {

    /** Index of the selected field */
    private static final int FIELD_SELECTED = LAST_NODE_INDEX + 1;

    /** Index of the available field */
    private static final int FIELD_AVAILABLE = LAST_NODE_INDEX + 2;

    /** Index of the eventCommand field */
    private static final int FIELD_EVENT_COMMAND = LAST_NODE_INDEX + 3;

    /** Index of the eventChannel field */
    private static final int FIELD_EVENT_CHANNEL = LAST_NODE_INDEX + 4;

    /** Index of the eventData1 field */
    private static final int FIELD_EVENT_DATA1 = LAST_NODE_INDEX + 5;

    /** Index of the eventData1 field */
    private static final int FIELD_EVENT_DATA2 = LAST_NODE_INDEX + 6;

    /** The last field index used by this class */
    private static final int LAST_MIDI_SOURCE_INDEX = FIELD_EVENT_DATA2;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_MIDI_SOURCE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** inputOnly MFString selected */
    protected String[] vfSelected;

    /** outputOnly MFString available */
    protected String[] vfAvailable;

    /** outputOnly MFInt32 eventChannel */
    protected int[] vfEventChannel;

    /** outputOnly MFInt32 eventValue */
    protected int[] vfEventCommand;

    /** outputOnly MFInt32 eventData1 */
    protected int[] vfEventData1;

    /** outputOnly MFInt32 eventValue */
    protected int[] vfEventData2;

    /** The number of active items in the event fields */
    private int eventLen;

    private MidiDevice.Info info;

    /** The maximum number of events to buffer */

    private static final int EVENT_SIZE = 512;
    // Double buffered storage for midi events
    private int[][] commands;
    private int[][] channels;
    private int[][] data1;
    private int[][] data2;
    private int[] eventNum;
    private int buffNum;
    private MidiChannel[]  ochannels;
    private boolean on;
    private long lastTime;

    //----------------------------------------------------------
    // Methods internal to BaseMidiSource
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_SELECTED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "selected");
        fieldDecl[FIELD_AVAILABLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "available");
        fieldDecl[FIELD_EVENT_CHANNEL] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFInt32",
                                     "eventChannel");
        fieldDecl[FIELD_EVENT_COMMAND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFInt32",
                                     "eventCommand");
        fieldDecl[FIELD_EVENT_DATA1] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFInt32",
                                     "eventData1");
        fieldDecl[FIELD_EVENT_DATA2] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFInt32",
                                     "eventData2");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SELECTED);
        fieldMap.put("selected",idx);
        fieldMap.put("selected_changed",idx);
        fieldMap.put("set_selected",idx);

        idx = new Integer(FIELD_AVAILABLE);
        fieldMap.put("available",idx);
        fieldMap.put("available_changed",idx);

        idx = new Integer(FIELD_EVENT_CHANNEL);
        fieldMap.put("eventChannel",idx);
        fieldMap.put("eventChannel_changed",idx);

        idx = new Integer(FIELD_EVENT_COMMAND);
        fieldMap.put("eventCommand",idx);
        fieldMap.put("eventCommand_changed",idx);

        idx = new Integer(FIELD_EVENT_DATA1);
        fieldMap.put("eventData1",idx);
        fieldMap.put("eventData1_changed",idx);

        idx = new Integer(FIELD_EVENT_DATA2);
        fieldMap.put("eventData2",idx);
        fieldMap.put("eventData2_changed",idx);
    }

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public BaseMidiSource() {
        super("MidiSource");

        hasChanged = new boolean[NUM_FIELDS];
        vfSelected = null;

        channels = new int[2][EVENT_SIZE];
        commands = new int[2][EVENT_SIZE];
        data1 = new int[2][EVENT_SIZE];
        data2 = new int[2][EVENT_SIZE];
        eventNum = new int[2];

        vfEventChannel = new int[EVENT_SIZE];
        vfEventCommand = new int[EVENT_SIZE];
        vfEventData1 = new int[EVENT_SIZE];
        vfEventData2 = new int[EVENT_SIZE];

        buffNum = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseMidiSource(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("selected");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfSelected = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfSelected, 0,
                    field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        int buff = buffNum;

        buffNum++;
        if (buffNum > 1)
            buffNum = 0;

        if (eventNum[buff] > 0) {
            vfEventChannel = new int[eventNum[buff]];
            vfEventCommand = new int[eventNum[buff]];
            vfEventData1 = new int[eventNum[buff]];
            vfEventData2 = new int[eventNum[buff]];

            for(int i=0; i < eventNum[buff]; i++) {
                vfEventChannel[i] = channels[buff][i];
                vfEventCommand[i] = commands[buff][i];
                vfEventData1[i] = data1[buff][i];
                vfEventData2[i] = data2[buff][i];
            }

            eventLen = eventNum[buff];
            eventNum[buff] = 0;

            hasChanged[FIELD_EVENT_CHANNEL] = true;
            hasChanged[FIELD_EVENT_COMMAND] = true;
            hasChanged[FIELD_EVENT_DATA1] = true;
            hasChanged[FIELD_EVENT_DATA2] = true;
            fireFieldChanged(FIELD_EVENT_CHANNEL);
            fireFieldChanged(FIELD_EVENT_COMMAND);
            fireFieldChanged(FIELD_EVENT_DATA1);
            fireFieldChanged(FIELD_EVENT_DATA2);
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        stateManager.addEndOfThisFrameListener(this);
        initMidiDevice();

        try {
            Synthesizer synth = null;

            synth = MidiSystem.getSynthesizer();

            ochannels = synth.getChannels();

            synth.open();

        } catch (MidiUnavailableException e)
        { e.printStackTrace(); }


        inSetup = false;
    }

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_MIDI_SOURCE_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ChildNodeType;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_SELECTED:
                fieldData.clear();
                fieldData.stringArrayValue = vfSelected;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = (vfSelected == null) ? 0 : vfSelected.length;
                break;

            case FIELD_AVAILABLE:
                fieldData.clear();
                fieldData.stringArrayValue = vfAvailable;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = (vfAvailable == null) ? 0 : vfAvailable.length;
                break;

            case FIELD_EVENT_CHANNEL:
                fieldData.clear();
                fieldData.intArrayValue = vfEventChannel;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = (vfEventChannel == null) ? 0 : eventLen;
                break;

            case FIELD_EVENT_COMMAND:
                fieldData.clear();
                fieldData.intArrayValue = vfEventCommand;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = (vfEventCommand == null) ? 0 : eventLen;
                break;

            case FIELD_EVENT_DATA1:
                fieldData.clear();
                fieldData.intArrayValue = vfEventData1;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = (vfEventData1 == null) ? 0 : eventLen;
                break;

            case FIELD_EVENT_DATA2:
                fieldData.clear();
                fieldData.intArrayValue = vfEventData2;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = (vfEventData2 == null) ? 0 : eventLen;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_SELECTED:
                    destNode.setValue(destIndex, vfSelected, vfSelected.length);
                    break;

                case FIELD_AVAILABLE:
                    destNode.setValue(destIndex, vfAvailable, vfAvailable.length);
                    break;

                case FIELD_EVENT_CHANNEL:
                    destNode.setValue(destIndex, vfEventChannel, vfEventChannel.length);
                    break;

                case FIELD_EVENT_COMMAND:
                    destNode.setValue(destIndex, vfEventCommand, vfEventCommand.length);
                    break;

                case FIELD_EVENT_DATA1:
                    destNode.setValue(destIndex, vfEventData1, vfEventData1.length);
                    break;

                case FIELD_EVENT_DATA2:
                    destNode.setValue(destIndex, vfEventData2, vfEventData2.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseMidiSource sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseMidiSource sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field from the raw string. This requires the
     * implementation to parse the string in the given format for the field
     * type. If the field type does not match the requirements for that index
     * then an exception will be thrown. If the destination field is a string,
     * then the leading and trailing quote characters will be stripped before
     * calling this method.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, String value)
        throws InvalidFieldFormatException, InvalidFieldException {


        switch(index) {
            case FIELD_SELECTED :
                vfSelected = new String[] { value };
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field from the raw collection of strings. Like the
     * single value version above, this needs to process things into the
     * correct form. If this field represents an MFString field it will be
     * guaranteed to be one SFString per index - with leading and trailing
     * quotes already stripped.
     *
     * @param index The index of destination field to set
     * @param value The raw value strings to be parsed
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldFormatException, InvalidFieldException,
               InvalidFieldValueException {

        if(index == FIELD_SELECTED) {
            if(vfSelected == null || vfSelected.length != numValid)
                vfSelected = new String[numValid];

            System.arraycopy(value, 0, vfSelected, 0, numValid);

            hasChanged[index] = true;
            fireFieldChanged(index);
        } else {
            super.setValue(index, value, numValid);
        }
    }


    //----------------------------------------------------------
    // Methods required by the Reciever interface.
    //----------------------------------------------------------
    public void close() {
    }

    public void send(MidiMessage message, long lTimeStamp) {
        ShortMessage smsg;

        if (message instanceof ShortMessage) {
            smsg = (ShortMessage) message;

            commands[buffNum][eventNum[buffNum]] = smsg.getCommand();
            data1[buffNum][(eventNum[buffNum])] = smsg.getData1();
            data2[buffNum][(eventNum[buffNum])++] = smsg.getData2();
            channels[buffNum][eventNum[buffNum]] = smsg.getChannel();

            switch (smsg.getCommand()) {
                case ShortMessage.NOTE_OFF:
                    //strMessage = "note Off " + getKeyName(smsg.getData1()) + " velocity: " + smsg.getData2();
//                    System.out.println("note off: " + smsg.getData1() + " vel: " + smsg.getData2());
                    ochannels[0].noteOff(55);

                    break;

                case ShortMessage.NOTE_ON:
                    System.out.println("note on: " + smsg.getData1() + " vel: " + smsg.getData2());

                    //strMessage = "note On " + getKeyName(smsg.getData1()) + " velocity: " + smsg.getData2();
                    int vel = Math.min(127,smsg.getData2()*3);
                    int note = smsg.getData1();
                    if (vel > 0)
                        ochannels[0].noteOn(note, vel);
                    else
                        ochannels[0].noteOff(note);

                    break;

                case 0xa0:
                    //strMessage = "polyphonic key pressure " + getKeyName(smsg.getData1()) + " pressure: " + smsg.getData2();
                    break;

                case ShortMessage.CONTROL_CHANGE:
//                    System.out.println("Control change2: " + smsg.getData1() + " val: " + smsg.getData2());


                    //strMessage = "control change " + smsg.getData1() + " value: " + smsg.getData2();
                    break;

                case 0xc0:
                    //strMessage = "program change " + smsg.getData1();
                    break;

                case 0xd0:
                    //strMessage = "key pressure " + getKeyName(smsg.getData1()) + " pressure: " + smsg.getData2();
                    break;

                case 0xe0:
                    //strMessage = "pitch wheel change " + get14bitValue(smsg.getData1(), smsg.getData2());
                    break;

                    }

        }
        stateManager.addEndOfThisFrameListener(this);
    }

    // Local methods

    private void initMidiDevice() {
        info = getMidiDeviceInfo(vfSelected);

        if (info == null) {
            System.out.println("no device info found for name: ");
            for(int i=0; i < vfSelected.length; i++) {
                System.out.println("   " + vfSelected[i]);
            }

            return;
        }
        MidiDevice  inputDevice = null;
        try {
            inputDevice = MidiSystem.getMidiDevice(info);
            inputDevice.open();
        }
        catch (MidiUnavailableException e) {
           e.printStackTrace();
        }
        if (inputDevice == null) {
            System.out.println("wasn't able to retrieve MidiDevice");
            return;
        } else {
            System.out.println("MIDI initialized");
        }

        try {
            Transmitter t = inputDevice.getTransmitter();
            t.setReceiver(this);
        }
        catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    /*
     *  This method tries to return a MidiDevice.Info whose name
     *  matches the passed name. If no matching MidiDevice.Info is
     *  found, null is returned.
     */
    private MidiDevice.Info getMidiDeviceInfo(String[] names)
    {
        MidiDevice.Info[]   aInfos = MidiSystem.getMidiDeviceInfo();
        //out("Searching '"+strDeviceName+"' for "+(forOutput?"output":"input"));

        System.out.println("Midi Devices Found:");
        for(int j=0; j < names.length; j++) {
            for (int i = 0; i < aInfos.length; i++) {
                System.out.print(aInfos[i].getName());
                if (aInfos[i].getName().equals(names[j])) {
                    try
                    {
                        MidiDevice device = MidiSystem.getMidiDevice(aInfos[i]);
                        boolean bAllowsInput = (device.getMaxTransmitters() != 0);
                        boolean bAllowsOutput = (device.getMaxReceivers() != 0);

                        System.out.println(" inputs: " + device.getMaxTransmitters() + " outputs: " + device.getMaxReceivers());
                        if (bAllowsInput) {
                            return aInfos[i];
                        }
                    } catch (MidiUnavailableException mue)
                    {
                    }
                } else {
                    System.out.println("");
                }
            }
        }
        return null;
    }
}
