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

package org.web3d.vrml.scripting.jsai;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// Application specific imports
import vrml.field.*;

import vrml.Browser;
import vrml.ConstField;
import vrml.Field;
import vrml.node.Node;
import vrml.node.Script;

import org.web3d.util.HashSet;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.scripting.ScriptWrapper;

/**
 * A wrapper class used to convert between the Xj3D implementation specific
 * details and the VRML97 spec requirements for a script.
 * <p>
 *
 * Note that as this class does not wrap VRML200x scripts, the class does not
 * need to take an instance of VRMLScriptNodeType as we cannot change the field
 * listing dynamically.
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class VRML97ScriptWrapper implements ScriptWrapper {

    /** The script class that we are dealing with */
    private Script script;

    /** The list of all possible values that have been set for propogation */
    private ArrayList pendingEvents;

    /** A collection of currently un-used event instances */
    private LinkedList unusedEvents;

    /** Working list of the last sent events */
    private VRML97ScriptEvent[] eventArray;

    /** Timestamp of the current set of events to send */
    private double timestamp;

    /** The browser reference for passing to included nodes */
    private Browser browser;

    /** The field factory instance */
    private FieldFactory fieldFactory;

    /** Has this script been initialized */
    private boolean initialized;

    /**
     * Flag to say this script received an event since last processing
     * and therefore eventsProcessed should be called.
     */
    private boolean hadInputEvent;

    /** Collections of fields that relate to eventOuts*/
    private HashSet eventOuts;


    /**
     * Create a new script wrapper for the given script class
     *
     * @param sc The script that this class is wrapping
     * @throws IllegalArgumentException Either of the arguments was null
     */
    public VRML97ScriptWrapper(Script sc, Browser b) {
        if((sc == null) || (b == null))
            throw new IllegalArgumentException("Bad script init. Null node");

        script = sc;
        browser = b;
        fieldFactory = new JSAIFieldFactory();
        initialized = false;
        hadInputEvent = false;
    }

    /**
     * Initialise the underlying script, based on the surrounding node's
     * details.
     *
     * @param node The working node from the live scene graph
     */
    public void initialize(VRMLScriptNodeType node) {

        eventOuts = new HashSet();

        // Find all of the fields declared in the node and create representative
        // field instances of them.
        List field_list = node.getAllFields();
        Iterator itr = field_list.iterator();
        HashMap fields_map = new HashMap();

        Field field;
        VRMLFieldDeclaration decl;
        String name;

        while(itr.hasNext()) {
            decl = (VRMLFieldDeclaration)itr.next();

            name = decl.getName();

            if(decl.getAccessType() == FieldConstants.EVENTOUT)
                eventOuts.add(name);

            field = fieldFactory.createField(node, name, false);
            if(field instanceof NodeField)
                ((NodeField)field).initialize(browser, fieldFactory);

            fields_map.put(name, field);
        }

        pendingEvents = new ArrayList(node.getFieldCount());
        unusedEvents = new LinkedList();
        eventArray = new VRML97ScriptEvent[node.getFieldCount()];

        initialized = true;
        script.prepareScript(browser, fields_map, node);
        script.initialize();
    }

    /**
     * Set the timestamp that we shall start the next queue with.
     *
     * @param time The timestamp to use (in seconds)
     */
    public void setTimestamp(double time) {
        timestamp = time;
    }

    /**
     * Call the prepareEvents() method on the script, if it has one. If it does
     * not, this becomes a no-op. Return true if something executed and therefore
     * we need to check on further events being sent.
     *
     * @return true if something executed
     */
    public boolean prepareEvents() {
        return false;
    }

    /**
     * Queue a changed SFInt32 event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, int value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstSFInt32(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed MFInt32 event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, int[] value, int numValid) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstMFInt32(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed SFLong event value ready for processing by
     * the script. Not used as VRML97 does not support these types.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, long value) {
    }

    /**
     * Queue a changed MFLong event value ready for processing
     * by the script. Not used as VRML97 does not support these types.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, long[] value, int numValid) {
    }

    /**
     * Queue a changed SFBool event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, boolean value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstSFBool(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed MFBool event value ready for processing by the script.
     * Not used as VRML97 does not have MFBool.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, boolean[] value, int numValid) {
    }

    /**
     * Queue a changed SFFloat event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, float value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstSFFloat(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed event value ready for processing by the script. Used to
     * set SFColor, SFRotation, SFVec2f, SFVec3f or MFFloat fields
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, float[] value, int numValid) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = null;

        switch(type) {
            case FieldConstants.SFCOLOR:
                field = new ConstSFColor(value[0], value[1], value[2]);
                break;

            case FieldConstants.SFROTATION:
                field = new ConstSFRotation(value[0],
                                            value[1],
                                            value[2],
                                            value[3]);
                break;

            case FieldConstants.SFVEC2F:
                field = new ConstSFVec2f(value[0], value[1]);
                break;

            case FieldConstants.SFVEC3F:
                field = new ConstSFVec3f(value[0], value[1], value[2]);
                break;

            case FieldConstants.MFFLOAT:
                field = new ConstMFFloat(value);
                break;

            default:
                System.err.println("Invalid field queue type in float[]");
                return;
        }

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed SFTime event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, double value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstSFTime(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed MFTime or MFDouble event value ready for processing
     * by the script. Not used as VRML97 does not support these types.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, double[] value, int numValid) {
    }

    /**
     * Queue a changed SFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, String value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstSFString(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed MFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type, String name, String[] value, int numValid) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        ConstField field = new ConstMFString(value);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

    /**
     * Queue a changed SFNode event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, VRMLNodeType value) {
        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        Node node = new JSAINode(value, browser, fieldFactory);

        ConstField field = new ConstSFNode(node);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }

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
                           int numValid) {

        // Ignore events before initialization or event out
        if(!initialized || eventOuts.contains(name))
            return;

        VRML97ScriptEvent evt = getUnusedEvent();

        Node[] node_list = new Node[value.length];

        for(int i = 0; i < value.length; i++) {
            node_list[i] = new JSAINode(value[i], browser, fieldFactory);
        }

        ConstField field = new ConstMFNode(node_list);

        evt.reInit(name, timestamp, field);
        pendingEvents.add(evt);
    }


    /**
     * Send the events to the real script now, and then call eventsProcessed.
     */
    public boolean sendEvents() {
        // Ignore events before initialization
        if(!initialized)
            return false;

        // batch and send....
        int num_events = pendingEvents.size();

        if(num_events == 0)
            return false;

        if(num_events > eventArray.length)
            eventArray = new VRML97ScriptEvent[num_events];

        hadInputEvent = true;
        pendingEvents.toArray(eventArray);

        try {
            script.processEvents(num_events, eventArray);
        } catch(Throwable th) {
            System.err.println("Error during script event propogation");
            System.err.println(th.getMessage());
            th.printStackTrace();
        }

        unusedEvents.addAll(pendingEvents);
        pendingEvents.clear();

        return false;
    }

    /**
     * Process the eventOuts of the script now. It should do that by calling
     * setValue() on the script node instance passed in as part of the the
     * initialize() method.
     */
    public void updateEventOuts() {
        // Not used for VRML97 because Java writes the events immediately
        // and directly to the event out. This may cause problems, so we need
        // to check on this and modify the behaviour. - JC.
    }

    /**
     * Notification that the eventsProcessed() functionality should be called
     * on the script code now.
     */
    public void eventsProcessed() {
        if(!hadInputEvent)
            return;

        hadInputEvent = false;

        try {
            script.eventsProcessed();
        } catch(Throwable th) {
            System.err.println("Error during script event propogation");
            System.err.println(th.getMessage());
            th.printStackTrace();
        }
    }

    /**
     * Called when the Script node is deleted. We free everything here to
     * allow the GC to do its magic.
     */
    public void shutdown() {
        script.shutdown();
        script = null;
    }

    /**
     * Convenience method to fetch a fresh event instance to queue
     *
     * @return An event instance ready to use
     */
    private VRML97ScriptEvent getUnusedEvent() {
        VRML97ScriptEvent ret_val = null;

        if(unusedEvents.size() == 0)
            ret_val = new VRML97ScriptEvent();
        else
            ret_val = (VRML97ScriptEvent)unusedEvents.remove(0);

        return ret_val;
    }
}

