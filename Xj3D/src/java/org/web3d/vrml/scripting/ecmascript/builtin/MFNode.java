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

package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * MFNode field object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.28 $
 */
public class MFNode extends NodeFieldObject {

    private static final String OBJECT_NOT_NODE_MSG =
        "The object you attempted to assign was not an SFNode instance";

    /** The properties of this class */
    private ArrayList<SFNode> nodeList;

    /** Representation of the length as a class */
    private ReusableInteger sizeInt;

    /**
     * The set of nodes that have changed either internally or through
     * direct assignment.
     */
    private ArrayList<SFNode> changedNodes;

    /**
     * Flag to say that we've changed our own local node references. This is
     * used to determine when we need to make up a field data holder for
     * ourselves, in addition to changes from the child nodes, during the
     * getChangedFields() method call.
     */
    private boolean localChanges;

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    /** The Javascript Undefined value */
    private static Object jsUndefined;

    static {
        propertyNames = new HashSet();
        propertyNames.add("length");

        functionNames = new HashSet();
        functionNames.add("toString");
        functionNames.add("equals");

        jsUndefined = Context.getUndefinedValue();
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public MFNode() {
        super("MFNode");

        nodeList = new ArrayList<SFNode>();
        changedNodes = new ArrayList<SFNode>();
        sizeInt = new ReusableInteger(0);
        localChanges = false;
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node). May be constructed with a null array reference to indicate
     * a field that contains no values.
     *
     * @param parent The parent node of this field
     * @param fieldIndex The index of the field that this field wraps
     * @param nodes The list of nodes to use or null
     * @param numValid The number of valid nodes to use from the array
     */
    public MFNode(VRMLNodeType parent,
                  int fieldIndex,
                  VRMLNodeType[] nodes,
                  int numValid) {
        this();

        parentNode = parent;
        parentFieldIndex = fieldIndex;

        if(numValid != 0) {
            nodeList.ensureCapacity(nodes.length);

            for(int i = 0; i < numValid; i++) {
                if(nodes[i] != null) {
                    SFNode node = new SFNode(parent, fieldIndex, nodes[i]);
                    node.setParentScope(this);
                    nodeList.add(node);
                } else
                    nodeList.add(null);
            }
        }

        sizeInt.setValue(numValid);
    }

    /**
     * Construct a field based on an array of SFNode objects.
     *
     * @param args the objects
     */
    public MFNode(Object[] args) {
        this();

        int cnt = 0;

        for(int i=0; i < args.length; i++) {
            if (args[i] == jsUndefined)
                continue;

            if(!(args[i] instanceof SFNode))
                    throw new IllegalArgumentException("Non SFNode given");

            SFNode a_node = (SFNode)args[i];

            // Not necessarily sure this is a good idea. What if we
            // are sharing an instance so it becomes an implicit DEF/USE
            // situation? Which parent scope really applies?
            if(a_node.getParentScope() == null)
                a_node.setParentScope(this);

            cnt++;
            nodeList.add(a_node);
        }

        sizeInt.setValue(cnt);
    }

    //----------------------------------------------------------
    // Methods used by ScriptableObject reflection
    //----------------------------------------------------------

    /**
     * Constructor for a new Rhino object
     *
     * @param nodes The list of nodes to use
     */
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean inNewExpr) {

        MFNode result = new MFNode(args);

        return result;
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    /**
     * Check for the indexed property presence.
     */
    public boolean has(int index, Scriptable start) {
        return (index >= 0);
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        boolean ret_val = false;

        if(propertyNames.contains(name))
            ret_val = true;
        else
            ret_val = super.has(name, start);

        return ret_val;
    }

    /**
     * Get the value at the given index.
     */
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;
        if((index >= 0) && (index < nodeList.size())) {
            ret_val = nodeList.get(index);

           // may have been set in a previous call, but for safety's sake.
            if(readOnly)
                ((FieldScriptableObject)ret_val).setReadOnly();
        }

        return ret_val;
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            ret_val = sizeInt;
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registex by the
            // runtime. Let's check to see if it is a standard method for
            // this object and then create and return a corresponding Function
            // instance.
            if((ret_val == null) && functionNames.contains(name))
                ret_val = locateFunction(name);
        }

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Sets a property based on the index. According to C.6.13.1 if the
     * index is greater than the current number of nodes, expand the size
     * by one and add the new value to the end.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(int index, Scriptable start, Object value) {
        if(readOnly && !scriptField) {
            Context.reportError(READONLY_MSG);
            return;
        }

        // Null values are acceptable, and also used to clear a reference
        // to a node already in this list.
        if((value != null) && !(value instanceof SFNode)) {
            Context.reportError(OBJECT_NOT_NODE_MSG);
            return;
        }

        if(value != null) {
            Scriptable node = (SFNode)value;

            // NOTE:
            // Always force the parent node's scope to be this node. Probably
            // not the best thing to do, but at least everything should be
            // correctly set then. May need to check on what happens with a
            // node that is in an array returned from createVrmlFromString()
            // that doesn't really have a parent. We need to check on sanity
            // there.
            node.setParentScope(this);
        }

        if(index >= nodeList.size()) {
            // Not in the array but the spec says we must expand to meet this
            // new size, setting the intermediate values to null and
            // and the addr the valid object.
            for(int i = nodeList.size(); i < index; i++)
                nodeList.add(null);

            nodeList.add((SFNode)value);
            sizeInt.setValue(nodeList.size());
        } else if(index >= 0) {
            nodeList.set(index, (SFNode)value);
        }

        if(value instanceof NodeFieldObject)
            ((NodeFieldObject)value).realize();

        if(!changedNodes.contains(value))
            changedNodes.add((SFNode)value);

        dataChanged = true;
        localChanges = true;
    }

    /**
     * Sets the named property with a new value. We don't allow the users to
     * dynamically change the length property of this node. That would cause
     * all sorts of problems. Therefore it is read-only as far as this
     * implementation is concerned.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.20
    //

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    /**
     * Comparison of this object to another of the same type. Just calls
     * the standard equals() method of the object.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean jsFunction_equals(Object val) {
        return equals(val);
    }

    //----------------------------------------------------------
    // Methods defined by NodeFieldObject.
    //----------------------------------------------------------

    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    public Object getChangedFields() {
        int size = changedNodes.size();

        if(size == 0)
            return null;

        Object ret_val = null;

        if(size == 1) {
            SFNode node = (SFNode)changedNodes.get(0);
            if(node != null)
                ret_val = node.getChangedFields();
        } else {
            for(int i = 0; i < size; i++) {
                SFNode node = (SFNode)changedNodes.get(i);
                Object values = null;

                if(node != null)
                    values = node.getChangedFields();

                // Just keep going if nothing has changed.
                if(values == null)
                    continue;

                if(ret_val != null) {
                    ArrayList l;

                    if(ret_val instanceof ArrayList) {
                        l = (ArrayList)ret_val;
                    } else {
                        l = new ArrayList();
                        l.add(ret_val);
                    }

                    if(values instanceof ArrayList)
                        l.addAll((ArrayList)values);
                    else
                        l.add(values);

                    ret_val = l;
                } else {
                    ret_val = values;
                }
            }
        }

        changedNodes.clear();

        // Need to assemble the array of changed node values from the local
        // node.
        if(localChanges) {
            NodeFieldData data = new NodeFieldData();
            data.node = parentNode;
            data.fieldIndex = parentFieldIndex;
            data.value = this;

            localChanges = false;

            if(ret_val != null) {
                ArrayList l;

                if(ret_val instanceof ArrayList) {
                    l = (ArrayList)ret_val;
                } else {
                    l = new ArrayList();
                    l.add(ret_val);
                }

                l.add(data);

                ret_val = l;
            } else {
                ret_val = data;
            }
        }

        return ret_val;
    }

    /**
     * If the node contains a node instance, check and call its setupFinished
     * if needed.
     */
    public void realize() {
        for(int i = 0; i < nodeList.size(); i++) {
            SFNode n = nodeList.get(i);

            if(n != null)
                n.realize();
        }
    }

    //----------------------------------------------------------
    // Methods defined by FieldScriptableObject.
    //----------------------------------------------------------

    /**
     * Query this field object to see if it has changed since the last time
     * this method was called. In a single-threaded environment, calling this
     * method twice should return true and then false (assuming that data had
     * changed since the previous calls).
     *
     * @return true if the data has changed.
     */
    public boolean hasChanged() {
        boolean ret_val = super.hasChanged();

        // do an explicit check of the referenced fields to see if
        // something nested has changed. The super class will only
        // catch direct assignment changes of local variable values but
        // won't cant something like thisNode.color.r = 0.7;
        //
        // Note that we always check this regardless of whether the line
        // above has returned true. This is used to clear out changes
        // in nodes down the scene graph from this location. If we don't
        // then this leaves it open to signal a change the next time it
        // is queried, forcing extra routing to happen that should not.
        int size = nodeList.size();

        for(int i = 0; i < size; i++) {
            SFNode node = (SFNode)nodeList.get(i);

            if(node != null && node.hasChanged()) {
                if(!changedNodes.contains(node))
                    changedNodes.add(node);

                ret_val = true;
            }
        }

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by Object.
    //----------------------------------------------------------

    /**
     * Return the string representation of the node value. The string does
     * not have the surrounding brackets.
     *
     * @return String representations of the node
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("[\n");
        int size = nodeList.size();


        for(int i = 0; i < size; i++) {
            SFNode node = (SFNode)nodeList.get(i);

            if(node == null) {
                buf.append("NULL\n");
            } else {
                buf.append(node.toString());
                buf.append('\n');
            }
        }

        buf.append(" ]\n");

        return buf.toString();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Check to see if there are a local changes to this node instance.
     * This call is for the convenience of the X3D Browser/Scene objects
     * so they can tell when the local and should be called before any of the
     * normal methods. It will clear the local change flag as part of the
     * request.
     */
    public boolean hasLocalChanges() {
        boolean ret_val = localChanges;
        localChanges = false;
        return ret_val;
    }

    /**
     * Update the node's raw data from the underlying model. If this wrapper
     * has a local changed copy of the data that has not yet been committed to
     * the underlying model, this request is ignored and the current data
     * stays.
     *
     * @param nodes The list of nodes to update here
     * @param numValid The number of valid nodes to use from the array
     */
    public void updateRawData(VRMLNodeType[] nodes, int numValid) {
        if(dataChanged)
            return;

        if(numValid != 0) {
            nodeList.clear();
            nodeList.ensureCapacity(numValid);

            for(int i = 0; i < numValid; i++) {
                if(nodes[i] != null) {
                    SFNode node = new SFNode(parentNode,
                                             parentFieldIndex,
                                             nodes[i]);
                    node.setParentScope(this);
                    nodeList.add(node);
                } else
                    nodeList.add(null);
            }
        }

        sizeInt.setValue(numValid);
    }

    /**
     * Get the underlying nodes that this object represents. If there are
     * no referenced node then a zero length array is returned.
     *
     * @return The node references
     */
    public VRMLNodeType[] getRawData() {
        int size = nodeList.size();

        VRMLNodeType[] ret_val = new VRMLNodeType[size];

        for(int i = 0; i < size; i++) {
            SFNode node = (SFNode)nodeList.get(i);

            if(node != null)
                ret_val[i] = node.getImplNode();
        }

        return ret_val;
    }
}
