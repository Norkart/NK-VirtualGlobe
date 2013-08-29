/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript;

// External imports
import java.util.*;

import org.mozilla.javascript.*;

// Local imports
import org.web3d.vrml.scripting.ecmascript.builtin.*;

import org.web3d.util.ArrayUtils;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.scripting.ScriptWrapper;
import org.web3d.vrml.scripting.ecmascript.x3d.Browser;

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
 * @version $Revision: 1.46 $
 */
class ECMAScriptWrapper implements ScriptWrapper {

    /** Name of the script source when no URL is supplied */
    private static final String INTERNAL_SOURCE = "Inline script";

    /** Representation of an empty argument list */
    private static final Object[] EMPTY_ARGS = {};

    /**
     * The standard names we don't pass into the script system. Filled in
     * by the static initializer.
     */
    private static final HashSet NON_USE_FIELDS;

    /** The context of the script that we are dealing with */
    private Context context;

    /** The Javascript scope we use for this script */
    private X3DScriptContext scope;

    /** The current error reporter used by this wrapper */
    private ReportAdapter externalReporter;

    /** The current error reporter used by this wrapper */
    private ErrorReporter errorReporter;

    /** The node that the script wrapper uses */
    private VRMLScriptNodeType scriptNode;

    /** The string representing the actual script */
    private String sourceString;

    /** The URL of the source. Non-null, defaults to the internal src. */
    private String sourceUrl;

    /** Reusable string array to pass arguments to the Javascript engine */
    private Object[] functionArgs;

    /** Reusable array of length 1 for sending the timestamp to prepareEvents */
    private Object[] timestampArg;

    /** Mapping of field names to scriptable field objects */
    private HashMap eventOutMap;

    /**
     * Set of field names that represent SFNode and MFNode event outs and
     * fields because we need to treat them separately.
     */
    private HashMap nodeEventOuts;

    /**
     * Just a straight listing of all the field names for rapid access to
     * fields when performing eventOut checking. Saves on creating garbage
     * by asking for iterators.
     */
    private String[] eventOutNames;

    /**
     * Straight listing of node field names for rapid access. Only assigned if
     * directOutput is TRUE;
     */
    private String[] nodeFieldNames;

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
     * Flag indicating that the scene object itself has some changed data.
     * Typically this is someone messing with scene.rootNodes, but may be
     * others like new proto definitions etc.
     */
    private boolean sceneChanged;

    /** Function object representing the prepareEvents method. Null if none */
    private Function prepareEventsFunction;

    /** Function object representing the prepareEvents method. Null if none */
    private Function eventsProcessedFunction;

    /** Temporary working values */
    private float[] float2;
    private float[] float3;
    private float[] float4;
    private double[] double2;
    private double[] double3;

    /**
     * Static initializer to build the list of fields we shouldn't pass
     * through.
     */
    static {
        NON_USE_FIELDS = new HashSet();
        NON_USE_FIELDS.add("url");
        NON_USE_FIELDS.add("mustEvaluate");
        NON_USE_FIELDS.add("directOutput");
    }

    /**
     * Create a new script wrapper for the given script string. It is assumed
     * that the string has the "javascript:" removed from the leading
     * characters. If the script is fetched from an external URL then the URL
     * can be provided for documentation/error output purposes. If the URL is
     * set to null, then any errors will be nominated as coming from an
     * internal script.
     *
     * @param ctx The javascript global context to use
     * @param sc The script (as a string) that this class is wraping
     * @param url The URL string this was fetched from, or null for internal
     * @param b The browser instance to use for this script
     * @throws IllegalArgumentException Either of the arguments was null
     */
    ECMAScriptWrapper(String sc,
                      String url,
                      Browser b,
                      Scriptable globalScope,
                      FieldFactory fac) {

        if((sc == null) || (b == null))
            throw new IllegalArgumentException("Bad script init. Null node");

        context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setCachingEnabled(true);
        context.setLanguageVersion(Context.VERSION_1_5);

        sourceString = sc.trim();
        sourceUrl = (url != null) ? url : INTERNAL_SOURCE;

        float2 = new float[2];
        float3 = new float[3];
        float4 = new float[4];

        double2 = new double[2];
        double3 = new double[3];

        functionArgs = new Object[2];
        timestampArg = new Object[1];

        scope = new X3DScriptContext(b, globalScope, fac);
        scope.setParentScope(globalScope);

        b.setParentScope(scope);

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        context.exit();
        context = null;
        initialized = false;
        hadInputEvent = false;
        sceneChanged = false;
    }

    /**
     * Initialise the underlying script, based on the surrounding node's
     * details.
     *
     * @param node The working node from the live scene graph
     */
    public void initialize(VRMLScriptNodeType node) {
        scriptNode = node;
        scope.setNodeImpl(node);

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

        eventOutMap = new HashMap();
        nodeEventOuts = new HashMap();

        // Find all of the fields declared in the node and create representative
        // field instances of them.
        List field_list = node.getAllFields();
        Iterator itr = field_list.iterator();
        HashMap fields_map = new HashMap();

        ArrayList eo_names = new ArrayList(field_list.size() - 3);


        VRMLFieldDeclaration decl;
        String name;
        Object field_wrapper = null;
        int access_type;
        float[] f_arg;
        double[] d_arg;
        VRMLNodeType n_arg;
        VRMLNodeType[] na_arg;
        String[] s_arg;
        int[] i_arg;

        while(itr.hasNext()) {
            decl = (VRMLFieldDeclaration)itr.next();

            name = decl.getName();

            if(NON_USE_FIELDS.contains(name))
                continue;

            access_type = decl.getAccessType();

            // don't defined eventIn fields as objects in the JavascriptScript space.
            // eventIns are represented by the function call.
            // eventOuts are defined as properties with classes, but with a
            // default value.

            if(access_type == FieldConstants.EVENTIN)
               continue;

            // All other field access types.
            try {
                field_index = node.getFieldIndex(name);
                data = node.getFieldValue(field_index);

                switch(decl.getFieldType()) {
                    // Primitive types first
                    case FieldConstants.SFBOOL:
                            if(data == null)
                            field_wrapper = Boolean.TRUE;
                        else
                            field_wrapper = data.booleanValue ?
                                             Boolean.TRUE :
                                             Boolean.FALSE;
                        break;

                    case FieldConstants.SFINT32:
                        field_wrapper = (data == null) ?
                                        new Integer(0) :
                                        new Integer(data.intValue);
                        break;

                    case FieldConstants.SFFLOAT:
                        field_wrapper = (data == null) ?
                                        new Float(0) :
                                        new Float(data.floatValue);
                        break;

                    case FieldConstants.SFTIME:
                        field_wrapper = (data == null) ?
                                        new Double(0) :
                                        new Double(data.doubleValue);
                        break;

                    case FieldConstants.SFSTRING:
                        if(data != null)
                            field_wrapper = data.stringValue;
                        break;

                    // Now the object types:

                    case FieldConstants.SFCOLOR:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new SFColor(f_arg);
                        f_arg = null;
                        break;

                    case FieldConstants.SFCOLORRGBA:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new SFColorRGBA(f_arg);
                        f_arg = null;
                        break;

                    case FieldConstants.SFROTATION:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new SFRotation(f_arg);
                        f_arg = null;
                        break;

                    case FieldConstants.SFVEC2F:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new SFVec2f(f_arg);
                        f_arg = null;
                        break;

                    case FieldConstants.SFVEC2D:
                        d_arg = (data == null) ? null : data.doubleArrayValue;
                        field_wrapper = new SFVec2d(d_arg);
                        d_arg = null;
                        break;

                    case FieldConstants.SFVEC3F:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new SFVec3f(f_arg);
                        f_arg = null;
                        break;

                    case FieldConstants.SFVEC3D:
                        d_arg = (data == null) ? null : data.doubleArrayValue;
                        field_wrapper = new SFVec3d(d_arg);
                        d_arg = null;
                        break;

                    case FieldConstants.SFIMAGE:
                        i_arg = (data == null) ? null : data.intArrayValue;
                        field_wrapper = new SFImage(i_arg, data.numElements);
                        i_arg = null;
                        break;

                    case FieldConstants.SFNODE:
                        n_arg = (data == null) ? null : (VRMLNodeType)data.nodeValue;

                        if(n_arg != null) {
                            field_wrapper = new SFNode(n_arg);
                        } else
                            field_wrapper = new SFNode();

                        nodeEventOuts.put(name, field_wrapper);
                        n_arg = null;
                        break;

                    case FieldConstants.MFCOLOR:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFColor(f_arg, data.numElements * 3);
                        f_arg = null;
                        break;

                    case FieldConstants.MFCOLORRGBA:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFColorRGBA(f_arg, data.numElements * 4);
                        f_arg = null;
                        break;

                    case FieldConstants.MFINT32:
                        i_arg = (data == null) ? null : data.intArrayValue;
                        field_wrapper = new MFInt32(i_arg, data.numElements);
                        i_arg = null;
                        break;

                    case FieldConstants.MFBOOL:
                        boolean[] b_arg =
                            (data == null) ? null : data.booleanArrayValue;
                        field_wrapper = new MFBool(b_arg, data.numElements);
                        b_arg = null;
                        break;

                    case FieldConstants.MFFLOAT:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFFloat(f_arg, data.numElements);
                        f_arg = null;
                        break;

                    case FieldConstants.MFTIME:
                        d_arg = (data == null) ? null : data.doubleArrayValue;
                        field_wrapper = new MFTime(d_arg, data.numElements);
                        d_arg = null;
                        break;

                    case FieldConstants.MFSTRING:
                        s_arg = (data == null) ? null : data.stringArrayValue;
                        field_wrapper = new MFString(s_arg, data.numElements);
                        s_arg = null;
                        break;

                    case FieldConstants.MFNODE:
                        na_arg = (data == null) ?
                                 null :
                                 (VRMLNodeType[])data.nodeArrayValue;

                        field_wrapper = new MFNode(node,
                                                   field_index,
                                                   na_arg,
                                                   data.numElements);

                        nodeEventOuts.put(name, field_wrapper);
                        na_arg = null;
                        break;

                    case FieldConstants.MFROTATION:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFRotation(f_arg, data.numElements * 4);
                        f_arg = null;
                        break;

                    case FieldConstants.MFVEC2F:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFVec2f(f_arg, data.numElements * 2);
                        f_arg = null;
                        break;

                    case FieldConstants.MFVEC3F:
                        f_arg = (data == null) ? null : data.floatArrayValue;
                        field_wrapper = new MFVec3f(f_arg, data.numElements * 3);
                        f_arg = null;
                        break;

                    case FieldConstants.MFVEC2D:
                        d_arg = (data == null) ? null : data.doubleArrayValue;
                        field_wrapper = new MFVec2d(d_arg, data.numElements * 2);
                        d_arg = null;
                        break;

                    case FieldConstants.MFVEC3D:
                        d_arg = (data == null) ? null : data.doubleArrayValue;
                        field_wrapper = new MFVec3d(d_arg, data.numElements * 3);
                        d_arg = null;
                        break;

                    case FieldConstants.MFIMAGE:
                        i_arg = (data == null) ? null : data.intArrayValue;
                        field_wrapper = new MFImage(i_arg, data.numElements);
                        i_arg = null;
                        break;

                    default:
                        field_wrapper = null;
                        System.out.println("Unknown X3D field type " +
                                           decl.getFieldTypeString());
                }
            } catch(InvalidFieldException ife) {
                // Que???? Should never happen.
                ife.printStackTrace();
            }

            if(field_wrapper != null) {

                // Only add the value to the map if we need to know the details
                // and have to pass that on as an event out - ie on eventOut
                // and exposedFields are needed to be kept.
                if(!directOutput &&
                   (access_type != FieldConstants.EVENTOUT) &&
                   (field_wrapper instanceof FieldScriptableObject)) {
                    FieldScriptableObject fso =
                        (FieldScriptableObject)field_wrapper;
                    fso.setReadOnly();
                    fso.setScriptField();
                }

                if(access_type != FieldConstants.FIELD) {
                    eventOutMap.put(name, field_wrapper);
                    scope.addEventOut(name, field_index, field_wrapper);
                    eo_names.add(name);
                } else {
                    scope.addField(name, field_wrapper);
                }
            }
        }

        eventOutNames = new String[eo_names.size()];
        eo_names.toArray(eventOutNames);

        if(directOutput) {
            nodeFieldNames = new String[nodeEventOuts.size()];
            Set keys = nodeEventOuts.keySet();
            keys.toArray(nodeFieldNames);
        }

        initialized = true;
        enterContext();

        // dump the script string into the scope now that all the fields have
        // been registered as properties. Ignore the return result, because
        // there is none at this point.
        try {
            context.evaluateString(scope, sourceString, sourceUrl, 1, null);

            // call initialise on the script.
            Object function = scope.get("initialize", scope);

            if((function instanceof Function)) {
                Function init = (Function)function;
                init.call(context, scope, scope, EMPTY_ARGS);
            }

            // Lets also try to see if the prepareEvents() function is set up
            function = scope.get("prepareEvents", scope);
            if(function instanceof Function)
                prepareEventsFunction = (Function)function;

            function = scope.get("eventsProcessed", scope);
            if(function instanceof Function)
                eventsProcessedFunction = (Function)function;

        } catch(JavaScriptException jse) {
            Context.reportError(jse.getMessage() +
                                " in the initialize() function");
        }

        exitContext();
    }

    /**
     * Set the timestamp that we shall start the next queue with.
     *
     * @param time The timestamp to use (in seconds)
     */
    public void setTimestamp(double time) {
        // could be expensive and create lots of garbage
        functionArgs[1] = new Double(time);
        timestampArg[0] = functionArgs[1];
    }

    /**
     * Call the prepareEvents() method on the script, if it has one. If it does
     * not, this becomes a no-op. Return true if something executed and therefore
     * we need to check on further events being sent.
     *
     * @return true if something executed
     */
    public boolean prepareEvents() {
        if(prepareEventsFunction == null)
            return false;

        enterContext();

        try {
            prepareEventsFunction.call(context, scope, scope, timestampArg);
        } catch(JavaScriptException jse) {
            Context.reportError(jse.getMessage());
        }

        exitContext();
        return true;
    }

    /**
     * Queue a changed SFInt32 event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, int value) {
        callScriptFunction(name, new Integer(value));
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

        Object field = null;

        switch(type) {
            case FieldConstants.SFIMAGE:
                field = new SFImage(value, numValid);
                break;

            case FieldConstants.MFIMAGE:
                field = new MFImage(value, numValid);
                break;

            case FieldConstants.MFINT32:
                field = new MFInt32(value, numValid);
                break;

            default:
                System.out.println("Unknown type in queueEvent(int[])");
                return;
        }

        callScriptFunction(name, field);
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
        callScriptFunction(name, new Long(value));
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
        callScriptFunction(name, value);
    }

    /**
     * Queue a changed SFBool event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, boolean value) {

        // Don't pass mustEvaluate and directOutput through
        if(NON_USE_FIELDS.contains(name))
            return;

        callScriptFunction(name, Boolean.valueOf(value));
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
        callScriptFunction(name, value);
    }

    /**
     * Queue a changed SFFloat event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, float value) {
          callScriptFunction(name, new Double(value));
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
        context = Context.enter();

        Object field;

        switch(type) {
            case FieldConstants.SFCOLOR:
                field = new SFColor(value);
                break;

            case FieldConstants.SFCOLORRGBA:
                field = new SFColorRGBA(value);
                break;

            case FieldConstants.SFROTATION:
                field = new SFRotation(value);
                break;

            case FieldConstants.SFVEC2F:
                field = new SFVec2f(value);
                break;

            case FieldConstants.SFVEC3F:
                field = new SFVec3f(value);
                break;

            case FieldConstants.MFFLOAT:
                field = new MFFloat(value, numValid);
                break;

            case FieldConstants.MFCOLOR:
                field = new MFColor(value, numValid);
                break;

            case FieldConstants.MFCOLORRGBA:
                field = new MFColorRGBA(value, numValid);
                break;

            case FieldConstants.MFROTATION:
                field = new MFRotation(value, numValid);
                break;

            case FieldConstants.MFVEC2F:
                field = new MFVec2f(value, numValid);
                break;

            case FieldConstants.MFVEC3F:
                field = new MFVec3f(value, numValid);
                break;
            default:
                System.err.println("Invalid field queue type in float[]");
                return;
        }

        callScriptFunction(name, field);
    }

    /**
     * Queue a changed SFTime event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, double value) {
        callScriptFunction(name, new Double(value));
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
        Object field;

        switch(type) {
            case FieldConstants.MFTIME:
                field = new MFTime(value, numValid);
                break;
            case FieldConstants.SFVEC2D:
                field = new SFVec2d(value);
                break;
            case FieldConstants.SFVEC3D:
                field = new SFVec3d(value);
                break;
            case FieldConstants.MFVEC2D:
                field = new MFVec2d(value, numValid);
                break;
            case FieldConstants.MFVEC3D:
                field = new MFVec3d(value, numValid);
                break;
            default:
                field = value;
        }

        callScriptFunction(name, field);
    }

    /**
     * Queue a changed SFString event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, String value) {
        // Don't pass set_url
        if(NON_USE_FIELDS.contains(name))
            return;

        callScriptFunction(name, value);
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
        // Don't pass set_url
        if(NON_USE_FIELDS.contains(name))
            return;

        callScriptFunction(name, new MFString(value, numValid));
    }

    /**
     * Queue a changed SFNode event value ready for processing by the script.
     *
     * @param type The type of field to process
     * @param name The field name to process
     * @param value The new value of the field
     */
    public void queueEvent(int type, String name, VRMLNodeType value) {
        callScriptFunction(name, new SFNode(value));
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

        int idx = scriptNode.getFieldIndex(name);

        callScriptFunction(name, new MFNode(scriptNode, idx, value, numValid));
    }

    /**
     * Send the events to the real script now. The return value indicates if
     * any eventOuts of the script have changed. It is assumed that in this
     * period the directOutputs have changed (if the flag is set), but
     * eventOuts are not processed.
     *
     * @return true One or more output events nee to be sent
     */
    public boolean sendEvents() {

        boolean ret_val = scope.hasAnyEventOutChanged();

        if(!directOutput)
            return ret_val;

        int size = nodeFieldNames.length;

        for(int i = 0; i < size; i++) {
            if(sendNodeOutput(nodeFieldNames[i], false))
                ret_val = true;
        }

        if(scope.hasSceneChanged()) {
            sceneChanged = true;
            ret_val = true;
        }

        return ret_val;
    }

    /**
     * Process the eventOuts of the script now. It should do that by calling
     * setValue() on the script node instance passed in as part of the the
     * initialize() method.
     */
    public void updateEventOuts() {
        // Need to check all the eventouts in the scope to see if anything
        // has changed by assignment. Some eventOuts will have changed by
        // just having one of their properties set. This first pass will
        // not detect that. We have to ask those fields separately in the
        // second check.
        int size = eventOutNames.length;

        for(int i = 0; i < size; i++) {

            if(!scope.hasEventOutChanged(eventOutNames[i]))
                continue;

            // so it has....
            try {
                int field_index = scriptNode.getFieldIndex(eventOutNames[i]);

                // Process the node eventOuts separately from the others.
                if(nodeEventOuts.containsKey(eventOutNames[i])) {
                    sendNodeOutput(eventOutNames[i], true);
                } else {
                    Object value = scope.get(eventOutNames[i], scope);

                    sendEvent(scriptNode, field_index, value);
                }
            } catch(FieldException ife) {
                // Generally this will be the field value not within range
                // (eg color value) so we'll just punt for a warning here.
                Context.reportWarning(ife.getMessage());
            }
        }

        if(sceneChanged) {
            sendSceneOutput();
            sceneChanged = false;
        }
    }

    /**
     * Notification that the eventsProcessed() functionality should be called
     * on the script code now.
     */
    public void eventsProcessed() {

        // Send no events before their time
        if(!initialized || eventsProcessedFunction == null || !hadInputEvent)
            return;

        enterContext();

        // need a check of a flag here to work out if the eventsProcessed
        // should be called.
        try {
            eventsProcessedFunction.call(context, scope, scope, EMPTY_ARGS);
        } catch(JavaScriptException jse) {
            jse.printStackTrace();
        }

        hadInputEvent = false;
        exitContext();
    }

    /**
     * Called when the Script node is deleted. We free everything here to
     * allow the GC to do its magic.
     */
    public void shutdown() {
        enterContext();

        try {
            // call initialise on the script.
            Object function = scope.get("shutdown", scope);

            if((function instanceof Function)) {
                Function shutdown = (Function)function;
                shutdown.call(context, scope, scope, EMPTY_ARGS);
            }
        } catch(JavaScriptException jse) {
            jse.printStackTrace();
        } finally {
            exitContext();
        }
    }

    /**
     * Convenience method to call the script with the given argument
     * information.
     *
     * @param name The name of the eventIn to call
     * @param value The value to send to the script
     */
    private void callScriptFunction(String name, Object value) {
        // Ignore events before the script is initialized
        if(!initialized)
            return;

        // Quick check to see if this is code that has looped around for
        // setting an eventOut. If this is an eventOut, don't bother going
        // any further. Unfortunately, this sort of conflicts with the same
        // check below in the loop. We want to be able to process
        // exposedFields which act as an eventIn and eventOut and this would
        // trap and kill the initial eventIn. For the moment, let's ignore it
        // as VRML97 does not permit exposedFields. Once X3D hits, then we'll
        // need to fix this properly.
        if(eventOutMap.containsKey(name))
            return;

        if(value instanceof FieldScriptableObject)
            ((FieldScriptableObject)value).setParentScope(scope);

        enterContext();

        try {
            // call a function correspoindto an eventIn on the script.
            Object function = scope.get(name, scope);

            if((function instanceof Function)) {
                Function process = (Function)function;
                functionArgs[0] = value;

// This is for dealing with an exposedField in X3D
//                if(eventOutMap.containsKey(name))
//                    eventOutMap.put(name, functionArgs[0]);

                process.call(context, scope, scope, functionArgs);
                hadInputEvent = true;
            } else {
                // This may end up being called on an eventOut being set.
            }
        } catch(JavaScriptException jse) {
            Context.reportError(jse.getMessage() + " in function: " + name);
        } catch(EcmaError ee) {
            Context.reportError(ee.getMessage() + " in function " + name);
        }

        exitContext();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ReportAdapter reporter) {
        externalReporter = reporter;
        errorReporter = reporter.getErrorReporter();

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Send a value directly to the given node. Does not use routing.
     *
     * @param node The target node to send the value to
     * @param index The index of the field of the node to send to
     * @param value The local Ecmascript value object to send
     * @throws FieldException Any sort of error related to sending the value
     */
    private void sendEvent(VRMLNodeType node,
                           int index,
                           Object value)
        throws FieldException {

        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        float[] float_data;
        double[] double_data;

        switch(decl.getFieldType()) {
            case FieldConstants.SFBOOL:
                if(!(value instanceof Boolean))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFBool field",
                                                null);
                else
                    node.setValue(index, ((Boolean)value).booleanValue());
                break;

            case FieldConstants.SFINT32:
                if(!(value instanceof Number))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFInt32 field",
                                                null);
                else
                    node.setValue(index, ((Number)value).intValue());
                break;

            case FieldConstants.SFFLOAT:
                if(!(value instanceof Number))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFFloat field",
                                                null);
                else
                    node.setValue(index, ((Number)value).floatValue());
                break;

            case FieldConstants.SFTIME:
                if(!(value instanceof Number))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFTime field",
                                                null);
                else
                    node.setValue(index, ((Number)value).doubleValue());
                break;

            case FieldConstants.SFSTRING:
                if(!(value instanceof String))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFString field",
                                                null);
                else
                    node.setValue(index, (String)value);
                break;

            // Now the object types:

            case FieldConstants.SFCOLOR:
                if(!(value instanceof SFColor))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFColor field",
                                                null);
                else {
                    ((SFColor)value).getRawData(float3);
                    node.setValue(index, float3, 3);
                }
                break;

            case FieldConstants.SFCOLORRGBA:
                if(!(value instanceof SFColorRGBA))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFColorRGBA field",
                                                null);
                else {
                    ((SFColorRGBA)value).getRawData(float4);
                    node.setValue(index, float4, 4);
                }
                break;

            case FieldConstants.SFROTATION:
                if(!(value instanceof SFRotation))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFRotation field",
                                                null);
                else {
                    ((SFRotation)value).getRawData(float4);
                    node.setValue(index, float4, 4);
                }
                break;

            case FieldConstants.SFVEC2F:
                if(!(value instanceof SFVec2f))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFVec2f field",
                                                null);
                else {
                    ((SFVec2f)value).getRawData(float2);
                    node.setValue(index, float2, 2);
                }
                break;

            case FieldConstants.SFVEC3F:
                if(!(value instanceof SFVec3f))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFVec3f field",
                                                null);
                else {
                    ((SFVec3f)value).getRawData(float3);
                    node.setValue(index, float3, 3);
                }
                break;

            case FieldConstants.SFVEC2D:
                if(!(value instanceof SFVec2d))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFVec2d field",
                                                null);
                else {
                    ((SFVec2d)value).getRawData(double2);
                    node.setValue(index, double2, 2);
                }
                break;

            case FieldConstants.SFVEC3D:
                if(!(value instanceof SFVec3d))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFVec3d field",
                                                null);
                else {
                    ((SFVec3d)value).getRawData(double3);
                    node.setValue(index, double3, 3);
                }
                break;

            case FieldConstants.SFIMAGE:
                if(!(value instanceof SFImage))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFImage field",
                                                null);
                else {
                    int[] int_data = ((SFImage)value).getRawData();
                    node.setValue(index, int_data, int_data.length);
                }
                break;

            case FieldConstants.MFBOOL:
                if(!(value instanceof MFBool))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFBool field",
                                                null);
                else {
                    boolean[] bool_data = ((MFBool)value).getRawData();
                    node.setValue(index, bool_data, bool_data.length);
                }
                break;


            case FieldConstants.MFCOLOR:
                if(!(value instanceof MFColor))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFColor field",
                                                null);
                else {
                    float_data = ((MFColor)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFCOLORRGBA:
                if(!(value instanceof MFColorRGBA))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFColorRGBA field",
                                                null);
                else {
                    float_data = ((MFColorRGBA)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFDOUBLE:
                if(!(value instanceof MFDouble))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFDouble field",
                                                null);
                else {
                    double_data = ((MFDouble)value).getRawData();
                    node.setValue(index, double_data, double_data.length);
                }
                break;

            case FieldConstants.MFINT32:
                if(!(value instanceof MFInt32))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFInt32 field",
                                                null);
                else {
                    int[] int_data = ((MFInt32)value).getRawData();
                    node.setValue(index, int_data, int_data.length);
                }
                break;

            case FieldConstants.MFFLOAT:
                if(!(value instanceof MFFloat))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFFloat field",
                                                null);
                else {
                    float_data = ((MFFloat)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFTIME:
                if(!(value instanceof MFTime))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFTime field",
                                                null);
                else {
                    double_data = ((MFTime)value).getRawData();
                    node.setValue(index, double_data, double_data.length);
                }
                break;

            case FieldConstants.MFSTRING:
                if(!(value instanceof MFString))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFString field",
                                                null);
                else {
                    String[] str_data = ((MFString)value).getRawData();
                    node.setValue(index, str_data, str_data.length);
                }
                break;

            case FieldConstants.MFROTATION:
                if(!(value instanceof MFRotation))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFRotation field",
                                                null);
                else {
                    float_data = ((MFRotation)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFVEC2F:
                if(!(value instanceof MFVec2f))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFVec2f field",
                                                null);
                else {
                    float_data = ((MFVec2f)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFVEC3F:
                if(!(value instanceof MFVec3f))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFVec3f field",
                                                null);
                else {
                    float_data = ((MFVec3f)value).getRawData();
                    node.setValue(index, float_data, float_data.length);
                }
                break;

            case FieldConstants.MFVEC2D:
                if(!(value instanceof MFVec2d))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFVec2d field",
                                                null);
                else {
                    double_data = ((MFVec2d)value).getRawData();
                    node.setValue(index, double_data, double_data.length);
                }
                break;

            case FieldConstants.MFVEC3D:
                if(!(value instanceof MFVec3d))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFVec3d field",
                                                null);
                else {
                    double_data = ((MFVec3d)value).getRawData();
                    node.setValue(index, double_data, double_data.length);
                }
                break;

            case FieldConstants.SFNODE:
                if((value != null) && !(value instanceof SFNode))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to SFNode field",
                                                null);
                else {
                    if(value != null)
                        node.setValue(index, ((SFNode)value).getImplNode());
                    else
                        node.setValue(index, (VRMLNodeType)null);
                }
                break;

            case FieldConstants.MFNODE:
                if((value != null) && !(value instanceof MFNode))
                    errorReporter.warningReport("Attempting to set invalid " +
                                                "value type to MFNode field",
                                                null);
                else {
                    if(value != null) {
                        VRMLNodeType[] n_list = ((MFNode)value).getRawData();
                        node.setValue(index, n_list, n_list.length);
                    } else {
                        node.setValue(index, (VRMLNodeType[])null, 0);
                    }
                }
                break;

            default:
        }
    }

    /**
     * Convenience method to set up a context entry. Performs all the evaluation
     * work and sets up the error reporter if needed.
     */
    private void enterContext() {
        context = Context.enter();
        context.setOptimizationLevel(-1);

        // Implementation Note:
        // Set the error reporter. This is a bit ugly checking on every method
        // call, but we really have no guarantee that the reporter has been set
        // for this thread. As the context object may change each time this is
        // called, we have to check (there is one context per thread being
        // executed. It would be nice if we could come up with something more
        // simple.
        Object er = context.getErrorReporter();

        if(!(er instanceof ReportAdapter) && (externalReporter != null))
            context.setErrorReporter(externalReporter);
    }

    /**
     * Convenience method to close out the execution context.
     */
    private void exitContext() {
        context.exit();
        context = null;
    }

    /**
     * Convenience method to send a single node event out value.
     *
     * @param fieldName The name of the field to investigate
     * @param changeChecked true if the field has already been checked
     *   for a change prior to this call
     * @return true if something was sent
     */
    private boolean sendNodeOutput(String fieldName, boolean changeChecked) {
        NodeFieldObject nfo =
                (NodeFieldObject)nodeEventOuts.get(fieldName);

        Object curr = scope.get(fieldName, scope);

        if(curr == Scriptable.NOT_FOUND)
            return false;

        boolean ret_val = false;

        // Check to see if the entire field object has been replaced by another
        // field object instance. This happens when there is direct assignment
        // of a new SFNode instance. If we find it, replace the existing object
        // with the new instance and then process the new instance.
        if(curr != nfo) {
            nfo = (NodeFieldObject)curr;
            eventOutMap.put(fieldName, nfo);
            nodeEventOuts.put(fieldName, nfo);
            ret_val = true;
        }

        // This object may be null if the original declaration was:
        // field SFNode foo NULL
        // If so, ignore it.

        if((nfo == null) || (!changeChecked && !nfo.hasChanged()))
            return ret_val;

        NodeFieldData data;
        Object changed_items = nfo.getChangedFields();

        if(changed_items instanceof NodeFieldData) {
            data = (NodeFieldData)changed_items;
            try {
                VRMLNodeType n = data.node;
                int index = data.fieldIndex;

                if(n == null) {
                    n = scriptNode;
                    index = scriptNode.getFieldIndex(fieldName);
                }

                sendEvent(n, index, data.value);
            } catch(FieldException fe) {
                String msg = "Error processing output field " + fieldName +
                             ". Contained message: " + fe.getMessage();
                Context.reportWarning(msg);
            }

        } else if(changed_items instanceof ArrayList) {
            // must be an array list of stuff
            ArrayList list = (ArrayList)changed_items;

            int i_size = list.size();
            for(int j = 0; j < i_size; j++) {
                data = (NodeFieldData)list.get(j);
                try {
                    VRMLNodeType n = data.node;
                    int index = data.fieldIndex;

                    if(n == null) {
                        n = scriptNode;
                        index = scriptNode.getFieldIndex(fieldName);
                    }

                    sendEvent(n, index, data.value);
                } catch(FieldException fe) {
                    String msg = "Error processing output field " + fieldName +
                                 ". Contained message: " + fe.getMessage();
                    Context.reportWarning(msg);

                }
            }
        } else {
            // If there is no field value, it is because we replaced the entire
            // field with another field value that has not changed. For example:
            //
            // Script {
            //   outputOnly SFNode foo
            //   initializeOnly SFNode bar Shape {}
            //   url "ecmascript: initialize() { foo = bar; }"
            // }
            //
            // So read the value and send as is.

            try {
                VRMLNodeType n = scriptNode;
                int index = scriptNode.getFieldIndex(fieldName);

                sendEvent(n, index, nfo);
            } catch(FieldException fe) {
                String msg = "Error processing output field " + fieldName +
                             ". Contained message: " + fe.getMessage();
                Context.reportWarning(msg);
            }
        }

        return ret_val;
    }

    /**
     * Send output from the scene to the underlying nodes.
     */
    private void sendSceneOutput() {
        Object changed_items = scope.getChangedData();

        if(changed_items == null)
            return;

        if(changed_items instanceof NodeFieldData) {
            NodeFieldData data = (NodeFieldData)changed_items;
            try {
                sendEvent(data.node, data.fieldIndex, data.value);
            } catch(FieldException fe) {
                Context.reportWarning(fe.getMessage());
            }

        } else {
            // must be an array list of stuff
            ArrayList list = (ArrayList)changed_items;

            int i_size = list.size();
            for(int j = 0; j < i_size; j++) {
                NodeFieldData data = (NodeFieldData)list.get(j);
                try {
                    sendEvent(data.node, data.fieldIndex, data.value);
                } catch(FieldException fe) {
                    Context.reportWarning(fe.getMessage());
                }
            }
        }
    }
}
