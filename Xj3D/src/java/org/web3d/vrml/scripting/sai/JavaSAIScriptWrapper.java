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

package org.web3d.vrml.scripting.sai;

// External imports
import java.util.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

// Local imports
import org.web3d.x3d.sai.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.scripting.ScriptWrapper;

/**
 * A wrapper class used to convert between the Xj3D implementation specific
 * details and the X3D spec requirements for a script.
 * <p>
 *
 * Note that as this class does not wrap VRML97 scripts, the class does not
 * need to take an instance of VRMLScriptNodeType as we cannot change the field
 * listing dynamically.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class JavaSAIScriptWrapper
    implements ScriptWrapper, FieldAccessListener {

    /**
     * The standard names we don't pass into the script system. Filled in
     * by the static initializer.
     */
    private static final HashSet NON_USE_FIELDS;

    /** The script class that we are dealing with */
    private X3DScriptImplementation script;

    /** Reference to the up-cast version of the script if doing per-frame */
    private X3DPerFrameObserverScript perFrameScript;

    /** The current error reporter used by this wrapper */
    private ErrorReporter errorReporter;

    /** Mapping of each field name to it's corresponding field object */
    private HashMap fieldMap;

    /** The list of all possible values that have been set for propogation */
    private ArrayList pendingEvents;

    /** Listing of all the fields that are not input-only */
    private BaseField[] updatableFields;

    /** Convenience listing of all the fields */
    private BaseField[] allFields;

    /**
     * Fields that are either SFNode or MFNode that are either initOnly or
     * inOut. We need to make sure these get updated every time an event is
     * sent into the scene, just in case they have had something change to due
     * routes elsewhere.
     */
    private BaseField[] usedNodes;

    /** Timestamp of the current set of events to send */
    private double timestamp;

    /** The browser reference for passing to included nodes */
    private InternalBrowser browser;

    /** The field factory instance */
    private FieldFactory fieldFactory;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** The node wrapper factory instance */
    private BaseNodeFactory baseNodeFactory;

    /** Flag to mirror the setting of the directOutput field */
    private boolean directOutput;

    /** Has this script been initialized */
    private boolean initialized;

    /**
     * Flag to say this script received an event since last processing
     * and therefore eventsProcessed should be called.
     */
    private boolean hadInputEvent;

    /**
     * Static initializer to build the list of fields we shouldn't pass
     * through.
     */
    static {
        NON_USE_FIELDS = new HashSet();
        NON_USE_FIELDS.add("mustEvaluate");
        NON_USE_FIELDS.add("directOutput");
    }

    /**
     * Create a new script wrapper for the given script class
     *
     * @param sc The script that this class is wrapping
     */
    public JavaSAIScriptWrapper(X3DScriptImplementation sc) {

        script = sc;
        fieldMap = new HashMap();

        initialized = false;
        hadInputEvent = false;

        if(sc instanceof X3DPerFrameObserverScript) {
            perFrameScript = (X3DPerFrameObserverScript)sc;
        }

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    //----------------------------------------------------------
    // Methods defined by ScriptWrapper
    //----------------------------------------------------------

    /**
     * Initialise the underlying script, based on the surrounding node's
     * details.
     *
     * @param node The working node from the live scene graph
     */
    public void initialize(VRMLScriptNodeType node) {

        // Check & set the directOutput flag
        int field_index;
        VRMLFieldData data;

        field_index = node.getFieldIndex("directOutput");

        try {
            data = node.getFieldValue(field_index);
            directOutput = data.booleanValue;
        } catch(InvalidFieldException ife) {
            // Should _never_ happen
            ife.printStackTrace();
        }

        // Find all of the fields declared in the node and create representative
        // field instances of them.
        List field_list = node.getAllFields();
        Iterator itr = field_list.iterator();
        ArrayList updatables = new ArrayList();
        ArrayList node_fields = new ArrayList();

        allFields = new BaseField[field_list.size()];

        X3DField field;
        VRMLFieldDeclaration decl;
        String name;
        int cnt = 0;

        while(itr.hasNext()) {
            decl = (VRMLFieldDeclaration)itr.next();

            name = decl.getName();

            field = fieldFactory.createField(node,
                                             name,
                                             true,
                                             true,
                                             fieldQueue,
                                             baseNodeFactory);

            BaseField b_field = (BaseField)field;
            b_field.setFieldAccessListener(this);

            fieldMap.put(name, field);
            allFields[cnt++] = b_field;

            // If one of the special fields that the user cannot
            // modify, mark it as such.
            if(NON_USE_FIELDS.contains(name))
                b_field.setReadOnly();

            int access = decl.getAccessType();
            if(access != FieldConstants.EVENTIN) {
                updatables.add(field);

                if(access != FieldConstants.EVENTOUT)
                    b_field.updateField();

                if((access == FieldConstants.FIELD ||
                    access == FieldConstants.EXPOSEDFIELD)  &&
                   (b_field instanceof NodeField))
                   node_fields.add(b_field);
            }
        }

        pendingEvents = new ArrayList(node.getFieldCount());
        updatableFields = new BaseField[updatables.size()];
        updatables.toArray(updatableFields);

        usedNodes = new BaseField[node_fields.size()];
        node_fields.toArray(usedNodes);

        initialized = true;

        X3DScriptNode external_wrapper =
            new SAIScriptNodeImpl(node, fieldQueue, fieldFactory, this, baseNodeFactory);

        try {
            enableFields();

            script.setBrowser(browser);
            script.setFields(external_wrapper,
                             Collections.unmodifiableMap(fieldMap));
            script.initialize();
        } catch(Exception e) {
            errorReporter.errorReport("Unable to initialize script", e);
        } finally {
            disableFields();
        }

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
        if(perFrameScript != null) {

            if(directOutput) {
                for(int i = 0; i < usedNodes.length; i++)
                    ((NodeField)usedNodes[i]).updateFieldAndChildren();
            }

            enableFields();

            try {
                perFrameScript.prepareEvents();
            } catch(Exception e) {
                errorReporter.errorReport("Per-frame observer error", e);
            }

            disableFields();

            if(directOutput) {
                for(int i = 0; i < usedNodes.length; i++)
                    ((NodeField)usedNodes[i]).updateNodeAndChildren();
            }

            return true;
        } else
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
        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
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
        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
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
        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
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
    public void queueEvent(int type,
                           String name,
                           long[] value,
                           int numValid) {
        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed SFBool event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, boolean value) {
        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
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
    public void queueEvent(int type,
                           String name,
                           boolean[] value,
                           int numValid) {

        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed SFFloat event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, float value) {
        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
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
    public void queueEvent(int type,
                           String name,
                           float[] value,
                           int numValid) {

        // Ignore events before initialization
        if (!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed SFTime event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, double value) {
        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
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
    public void queueEvent(int type,
                           String name,
                           double[] value,
                           int numValid) {

        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed SFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, String value) {
        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed MFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     * @param numValid The number of valid values to copy from the array
     */
    public void queueEvent(int type,
                           String name,
                           String[] value,
                           int numValid) {

        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
    }

    /**
     * Queue a changed SFNode event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, VRMLNodeType value) {
        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
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
        // Ignore events before initialization
        if(!initialized)
            return;

        pendingEvents.add(name);
    }


    /**
     * Send the events to the real script now, and then call eventsProcessed.
     *
     * @return true if events were processed this time around
     */
    public boolean sendEvents() {
        // Ignore events before initialization
        if(!initialized)
            return false;

        // batch and send....
        hadInputEvent = true;

        int num_events = pendingEvents.size();

        if(num_events == 0)
            return false;

        // NOTE:
        // Technically we would want to enable and disable between every call
        // because on a multi-threaded system we may well be interrupted part
        // way through and have an external thread attempt to run and access
        // the fields. That's fairly costly from a performance perspective, so
        // let's not do it for now and see what happens.

        if(directOutput) {
            for(int i = 0; i < usedNodes.length; i++)
                ((NodeField)usedNodes[i]).updateFieldAndChildren();
        }

        enableFields();

        for(int i = 0; i < num_events; i++) {
            String name = (String)pendingEvents.get(i);
            BaseField field = (BaseField)fieldMap.get(name);
            field.updateField();

            try {
                field.fireEventNotification(timestamp);
            } catch(Exception e) {
                errorReporter.errorReport("Error while sending event", e);
            }
        }

        disableFields();
        pendingEvents.clear();

        return true;
    }

    /**
     * Process the eventOuts of the script now. It should do that by calling
     * setValue() on the script node instance passed in as part of the the
     * initialize() method.
     */
    public void updateEventOuts() {
        // Loop through a list of them and check if they have changed. If so,
        // call updateNode() on them.
        if(!directOutput) {
            for(int i = 0; i < updatableFields.length; i++) {
                if(updatableFields[i].hasChanged())
                    updatableFields[i].updateNode();
            }
        } else {
            for(int i = 0; i < updatableFields.length; i++) {
                if(updatableFields[i] instanceof NodeField)
                    ((NodeField)updatableFields[i]).updateNodeAndChildren();
                else if(updatableFields[i].hasChanged())
                    updatableFields[i].updateNode();
            }

            browser.updateEventOuts();
        }
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
            enableFields();
            script.eventsProcessed();
        } catch(Exception e) {
            errorReporter.errorReport("Error during script eventProcessed", e);
        } finally {
            disableFields();
        }
    }

    /**
     * Called when the Script node is deleted. We free everything here to
     * allow the GC to do its magic.
     */
    public void shutdown() {
        try {
            enableFields();
            script.shutdown();
            script = null;
        } catch(Exception e) {
            errorReporter.errorReport("Error during script shutdown", e);
        } finally {
            disableFields();
        }
    }

    //----------------------------------------------------------
    // Methods defined by FieldAccessListener
    //----------------------------------------------------------

    /**
     * Notify that the child field now requires access to be valid or not valid
     * as the case may be from the various flags.
     *
     * @param state true if access should be currently valid.
     */
    public void childRequiresAccessStateChange(boolean state) {
        if(state) {
            if(directOutput) {
                for(int i = 0; i < usedNodes.length; i++)
                    ((NodeField)usedNodes[i]).updateFieldAndChildren();
            }

            enableFields();
        } else
            disableFields();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the internal browser reference to use. This should be called
     * immediately after the constructor.
     *
     * @param b The browser instance to use.
     */
    void setBrowser(InternalBrowser b) {
        browser = b;
        fieldFactory = b.getFieldFactory();
        fieldQueue = b.getSharedFieldQueue();
        baseNodeFactory = b.getBaseNodeFactory();
    }

    /**
     * Run through all the fields and enable them to allow the user to write
     * to them now.
     */
    private void enableFields() {
        for(int i = 0; i < allFields.length; i++)
            allFields[i].setAccessValid(true);
    }

    /**
     * Run through all the fields and disable them to prevent the user from
     * writing to them now.
     */
    private void disableFields() {
        for(int i = 0; i < allFields.length; i++)
            allFields[i].setAccessValid(false);

        // Go through all fields to be garbage collected and update their values
        Reference ref;
        BaseField f;

        while((ref = fieldQueue.poll()) != null) {
            f = (BaseField) ref.get();
            if (f == null)
               continue;

            if(f instanceof NodeField)
                ((NodeField)f).updateNodeAndChildren();
            else if(f.hasChanged())
                f.updateNode();
        }
    }
}
