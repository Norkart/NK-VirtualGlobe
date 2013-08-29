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

package org.web3d.vrml.nodes.proto;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.IntHashMap;

/**
 * Denotes an ordinary prototype node declaration.
 * <p>
 *
 * The prototype declaration has no concrete representation in the scenegraph.
 * It is used as a data creator for when someone declares an instance of the
 * proto within the current scenegraph.
 * <p>
 *
 * The proto can act in either VRML97 mode or VRML3.0 mode. The primary
 * difference is the handling of IS values when mapping fields. To catch errors
 * early, we check IS access type mapping as they are being inserted, rather
 * than at runtime. The handling of this is defined in
 * <a href="http://www.web3d.org/Specifications/VRML97/part1/concepts.html#Table4.4">
 * VRML97 Part 1, Section 4, Table 4.4</a>. For VRML 3.0 all field access is
 * expected to be exposedField so we can add an IS from any field to any
 * declaration field. Of course, when being added, we do check for fields types
 * and field names being valid too.
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public class PrototypeDecl extends AbstractProto
    implements VRMLProtoDeclare {

    /** Standard message when a requested field does not exist */
    private static final String NO_FIELD_MSG = "No field at index ";

    /** Standard message for asking for a value of an EventIn */
    private static final String EVENTIN_MSG =
        "No value, field is an eventIn ";

    /**
     * Message when the root for createNewInstance is not one of
     * VRMLWorldRootNodeType or VRMLProtoInstance.
     */
    private static final String INVALID_ROOT_MSG =
        "The instance creation function has an invalid root node type ";

    /** The group that holds the body information */
    private ProtoBodyGroupNode bodyGroup;

    /**
     * The mapping that holds is field index (key) to the list of destination
     * node information. (FieldInfo - value).
     */
    private HashMap<Integer, ArrayList<ProtoFieldInfo>> isMap;

    /** The set of routes registered for this definition */
    private HashSet<ProtoROUTE> routes;

    /** The set of ImportProxyNodes registered for this definition */
    private HashMap<String, ImportNodeProxy> imports;

    /** The set of DEF names to thier nodes */
    private HashMap<String, VRMLNode> defs;

    /** The set of prototype declarations that are included in this declaration */
    private ArrayList<VRMLNodeTemplate> protos;

    /**
     * A mapping of each field to the default value of that field in the
     * declaration. If the field is an eventIn or eventOut, then the value is
     * not stored. The field is kept in its raw form: String, String[], or
     * nodes. Nodes are kept as a linked list regardless of whether there is
     * one or many.
     */
    private IntHashMap fieldValueMap;

    /**
     * Create a new instance of a proto that has the given name that may have
     * its IS semantics defined according to the different specification
     * models.
     *
     * @param name The name of the proto to use
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public PrototypeDecl(String name,
                         int majorVersion,
                         int minorVersion,
                         NodeTemplateToInstanceCreator creator) {

        super(name, majorVersion, minorVersion, creator);

        bodyGroup = new ProtoBodyGroupNode();

        isMap = new HashMap<Integer, ArrayList<ProtoFieldInfo>>();
        fieldValueMap = new IntHashMap();

        routes = new HashSet<ProtoROUTE>();
        imports = new HashMap<String, ImportNodeProxy>();
        defs = new HashMap<String, VRMLNode>();
        protos = new ArrayList<VRMLNodeTemplate>();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeTemplate
    //----------------------------------------------------------

    /**
     * Create a new instance of a real node from this template. This will
     * ensure that all the internals are created as needed, based on the
     * current state of the node. Note that sometimes, creating an instance
     * of this template may result in an invalid node construction. Cases
     * where this could occur is when there's no node definition yet loaded
     * or that the loaded definition does not match this template.
     *
     * @param root The node that represents the root node of the
     *   VRMLExecutionSpace that we're in.
     * @param isStatic true if this is created within a StaticGroup
     * @return A new node instance from this template
     * @throws InvalidNodeTypeException The root node is not a node capable
     *    of representing a root of a scene graph
     * @see org.web3d.vrml.nodes.VRMLProtoInstance
     * @see org.web3d.vrml.nodes.VRMLWorldRootNodeType
     */
    public VRMLNode createNewInstance(VRMLNode root, boolean isStatic)
        throws InvalidNodeTypeException {

        VRMLNode ret_val = null;

        if(root instanceof VRMLWorldRootNodeType) {
            ret_val = protoCreator.newInstance(this,
                                               (VRMLWorldRootNodeType)root,
                                               vrmlMajorVersion,
                                               vrmlMinorVersion,
                                               isStatic);
        } else if(root instanceof VRMLProtoInstance) {
            ret_val = protoCreator.newInstance(this,
                                               (VRMLProtoInstance)root,
                                               vrmlMajorVersion,
                                               vrmlMinorVersion,
                                               isStatic);
        } else
            throw new InvalidNodeTypeException(INVALID_ROOT_MSG + root);

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLProtoDeclare
    //----------------------------------------------------------

    /**
     * Used to set the field value where the field represents a SFNode or
     * MFNode. The behavior of this call is dependent on whether the field is
     * single or multiple.
     *
     * @param index The index of destination field to set
     * @param node The node to set or add to this field
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void addFieldNodeValue(int index, VRMLNodeType node)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        List n_list = (List)fieldValueMap.get(index);

        if(n_list == null) {
            n_list = new LinkedList();
            fieldValueMap.put(index, n_list);
        }

        n_list.add(node);
    }

    /**
     * Get the value of a field. The return value will either be a String or
     * String[] representing the raw value of the field. The prototype
     * declaration does not bother attempting to parse and validate the
     * field values, so we only deal with raw items here.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {

        // check for the field existing first. It could be that the field
        // is an eventIn for VRML97 and so it doesn't have a value registered
        // or the value is a default. Use the field decl as the check for
        // validity, not the actual value.
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);
        else if(decl.getAccessType() == FieldConstants.EVENTIN)
            throw new InvalidFieldException(EVENTIN_MSG + decl.getName());

        Object o = fieldValueMap.get(index);
        VRMLFieldData ret_val;

        // If the value is a List then we haven't converted the implementation
        // over to a VRMLFieldData object yet. Do it now and replace the original
        // list with the FieldData instance for the next time it is retrieved.
        if(o instanceof List) {
            ret_val = new VRMLFieldData();
            fieldValueMap.put(index, ret_val);

            List l = (List)o;

            if(decl.getFieldType() == FieldConstants.SFNODE) {
                ret_val.dataType = VRMLFieldData.NODE_DATA;
                ret_val.nodeValue = (VRMLNode)l.get(0);
            } else {
                ret_val.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                ret_val.numElements = l.size();
                ret_val.nodeArrayValue = new VRMLNodeType[l.size()];
                l.toArray(ret_val.nodeArrayValue);
            }
        } else {
            ret_val = (VRMLFieldData)o;
        }

        return ret_val;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements and when trying to determine if the node
     * has been used in the right place. If it is unknown (eg not yet loaded
     * extern proto) then return -1.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return bodyGroup.getPrimaryType();
    }

    //----------------------------------------------------------
    // Local methods for general consumption.
    //----------------------------------------------------------

    /**
     * Add a nested proto or externproto declaration to this proto.
     *
     * @param proto The prototype declaration to add
     */
    public void addPrototypeDecl(VRMLNodeTemplate proto) {
        protos.add(proto);
    }

    /**
     * Fetch the list of all proto declarations in this proto. This includes
     * both externprotos and protos.
     */
    public List<VRMLNodeTemplate> getPrototypeDecls() {
        return protos;
    }

    /**
     * The route declaration to be registered with the node declaration
     * at this level. Nested routes inside nested proto declarations are
     * not held here.
     *
     * @param route The route representation to store
     */
    public void addRouteDecl(ProtoROUTE route) {
        routes.add(route);
    }

    /**
     * Get the set of the current route declarations that are available for
     * this definitions.
     *
     * @return The set of routes registered
     */
    public Set getRouteDecls() {
        return Collections.unmodifiableSet(routes);
    }

    /**
     * The IMPORT node proxy to be registered with the node declaration
     * at this level. Nested routes inside nested proto declarations are
     * not held here.
     *
     * @param name The local name this is imported as
     * @param node The proxy instance representing that node
     */
    public void addImportDecl(String name, ImportNodeProxy node) {
        imports.put(name, node);
    }

    /**
     * Return the internal mapping of the import proxy nodes mapped from their
     * name.
     *
     * @return A map keyed by the name string to their {@link ImportNodeProxy}
     */
    public Map getImportDecls() {
        return imports;
    }

    /**
     * Update the internal DEF map collection with the contents of this map. A
     * complete copy of the map is made.
     *
     * @param map The new map to copy
     */
    public void setDEFMap(Map map) {
        defs.putAll(map);
    }

    /**
     * Fetch the internal mapping of DEF names to nodes.
     *
     * @return A map of the name strings to nodes
     */
    public Map getDEFMap() {
        return defs;
    }

    /**
     * Get the grouping node that represents the body of the proto. This is
     * implicitly created by the proto but does not represent the abstract type
     * of the proto that would allow it to be used in a scenegraph.
     *
     * @return A grouping node suitable for adding content to during parsing
     */
    public VRMLGroupingNodeType getBodyGroup() {
        return bodyGroup;
    }

    /**
     * Register an IS setup between the incoming field and the destination node.
     *
     * @param declFieldName The name of the field we are copying in the proto
     *    declaration
     * @param destNode Reference to the node containing the field
     * @param destField Index of the recieving field
     * @throws InvalidFieldException Something is not valid in the fields defs
     * @throws InvalidFieldConnectionException The types of the field don't
     *    match or access doesn't match in VRML97 mode
     */
    public void addIS(String declFieldName, VRMLNodeType destNode, int destField)
        throws InvalidFieldException, InvalidFieldConnectionException {
        // check for a field of the given type.
        Integer index = (Integer)fieldIndexMap.get(declFieldName);

        if(index == null)
            throw new InvalidFieldException("Source for IS not known to " + nodeName,
                                            declFieldName);

        VRMLFieldDeclaration src_decl =
            (VRMLFieldDeclaration)fieldDeclList.get(index.intValue());

        VRMLFieldDeclaration dest_decl =
            destNode.getFieldDeclaration(destField);

        // Check this for existance too
        if(dest_decl == null) {
            throw new InvalidFieldException("Destination for IS not known.  destNode: "
                + destNode.getVRMLNodeName() + " fromField: " + declFieldName + " toIdx: " + destField);
        }

        if(src_decl.getFieldType() != dest_decl.getFieldType())
            throw new InvalidFieldConnectionException(
                "No match on IS data types. Source (" +
                src_decl.getName() +
                ") is " +
                src_decl.getFieldTypeString() +
                " and destination (" +
                dest_decl.getName() +
                ") is " +
                dest_decl.getFieldTypeString());

        // Now check the access types. These are defined in table 4.3 of 19775-1
        int dest_access = dest_decl.getAccessType();
        boolean match_ok = false;
        switch(src_decl.getAccessType()) {
            case FieldConstants.EXPOSEDFIELD:
                match_ok = dest_access == FieldConstants.EXPOSEDFIELD;
                break;

            case FieldConstants.FIELD:
                match_ok = (dest_access == FieldConstants.FIELD) ||
                           (dest_access == FieldConstants.EXPOSEDFIELD);
                break;

            case FieldConstants.EVENTIN:
                match_ok = (dest_access == FieldConstants.EXPOSEDFIELD) ||
                           (dest_access == FieldConstants.EVENTIN);
                break;

            case FieldConstants.EVENTOUT:
                match_ok = (dest_access == FieldConstants.EXPOSEDFIELD) ||
                           (dest_access == FieldConstants.EVENTOUT);
                break;
        }

        if(!match_ok)
            throw new InvalidFieldConnectionException(
                "IS access types are not compatible. Source (" +
                src_decl.getName() +
                ") is " +
                VRMLFieldDeclaration.toAccessTypeString(src_decl.getAccessType(),
                                                        isVrml97) +
                " and destination (" +
                dest_decl.getName() +
                ") is " +
                VRMLFieldDeclaration.toAccessTypeString(dest_access, isVrml97));


        // Now check the access type if we are VRML97 mode
        if(isVRML97()) {
            checkAccessMatch(src_decl, dest_decl);
        }

        // Just register the IS value in the map.
        ArrayList<ProtoFieldInfo> dest_list =
            (ArrayList<ProtoFieldInfo>)isMap.get(index);
        if(dest_list == null) {
            dest_list = new ArrayList<ProtoFieldInfo>(5); // start small!
            isMap.put(index, dest_list);
        }

        dest_list.add(new ProtoFieldInfo(destNode, destField));
    }

    /**
     * Get the IS mappings for this proto declaration. This should only be
     * used when building a new node instance so that the runtime instance
     * correctly maps the IS declaration to runtime nodes. The key of the
     * map is an Integer representation of the public field index. The value
     * is an instance of a {@link java.util.List} where the list contains
     * all the matching {@link ProtoFieldInfo} instances.
     *
     * @return A map containing the field index as the key and a list of output
     *    fields
     */
    public Map getISMaps() {
      return Collections.unmodifiableMap(isMap);
    }

    //----------------------------------------------------------
    // Private methods for local use
    //----------------------------------------------------------

    /**
     * Convenience method to check the field connection types in VRML 97 mode.
     * If they match, the method returns normally. If they don't, then an
     * exception is generated.
     *
     * @param src The source field declaration in the prototype
     * @param dest The destination field declaration in the node
     * @throws InvalidFieldConnectionException They don't match
     */
    private void checkAccessMatch(VRMLFieldDeclaration src,
                                  VRMLFieldDeclaration dest)
        throws InvalidFieldConnectionException {

        // This lookup table comes from VRML97 Part 1, Section 4, Table 4.4
        int dest_type = dest.getAccessType();
        switch(src.getAccessType()) {
            case FieldConstants.EXPOSEDFIELD:
                if(dest_type != FieldConstants.EXPOSEDFIELD)
                    throw new InvalidFieldConnectionException(
                        "Invalid IS access. Proto decl field is an " +
                        "exposedField and destination is not an exposedField");
                break;

            case FieldConstants.FIELD:
                if((dest_type == FieldConstants.EVENTIN) ||
                   (dest_type == FieldConstants.EVENTOUT))
                    throw new InvalidFieldConnectionException(
                        "Invalid IS access. Proto decl field is a field " +
                        " and destination is not field or exposedField");
                break;

            case FieldConstants.EVENTIN:
                if((dest_type == FieldConstants.FIELD) ||
                   (dest_type == FieldConstants.EVENTOUT))
                    throw new InvalidFieldConnectionException(
                        "Invalid IS access. Proto decl field is an eventIn "+
                        " and destination is a field or eventOut");
                break;

            case FieldConstants.EVENTOUT:
                if((dest_type == FieldConstants.FIELD) ||
                   (dest_type == FieldConstants.EVENTIN))
                    throw new InvalidFieldConnectionException(
                        "Invalid IS access. Proto decl field is an eventOut "+
                        " and destination is a field or eventIn");
                break;
        }
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.INT_DATA;
        data.intValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.INT_ARRAY_DATA;
        data.intArrayValue = value;

        // Determine the number of elements in the list. Dependent on the field
        // type.
        switch(decl.getFieldType()) {
            case FieldConstants.MFINT32:
                data.numElements = numValid;
                break;

            case FieldConstants.SFIMAGE:
                data.numElements = 1;
                break;

            case FieldConstants.MFIMAGE:
                // Need to walk the array and work out how many there are
                // because each image can be a different size. Find out size of
                // each image (read height x width int values + 1 for the
                // num_components value as well) and just increment until
                // the loop runs out of images to count.
                for(int i = 0; i < value.length; ) {
                    data.numElements++;
                    int offset = value[i] * value[i + 1] + 1;
                    i += offset;
                }

                break;

            default:
                System.out.println("PrototypeDecl setValue(int[]) unknown " +
                                   "field type: " + decl.getFieldType());
        }

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.BOOLEAN_DATA;
        data.booleanValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
        data.booleanArrayValue = value;
        data.numElements = numValid;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.FLOAT_DATA;
        data.floatValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
        data.floatArrayValue = value;

        // Determine the number of elements based on the field type
        switch(decl.getFieldType()) {
            case FieldConstants.MFFLOAT:
                data.numElements = numValid;
                break;

            case FieldConstants.SFVEC2F:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFROTATION:
                data.numElements = 1;
                break;

            case FieldConstants.MFVEC2F:
                data.numElements = numValid / 2;
                break;

            case FieldConstants.MFVEC3F:
            case FieldConstants.MFCOLOR:
                data.numElements = numValid / 3;
                break;

            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
                data.numElements = numValid / 4;
                break;

            default:
                System.out.println("PrototypeDecl setValue(float[]) unknown " +
                                   "field type: " + decl.getFieldTypeString());
        }

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException {
        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.LONG_DATA;
        data.longValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.LONG_ARRAY_DATA;
        data.longArrayValue = value;
        data.numElements = numValid;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.DOUBLE_DATA;
        data.doubleValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
        data.doubleArrayValue = value;

        switch(decl.getFieldType()) {
            case FieldConstants.MFDOUBLE:
            	data.numElements = numValid;
            	break;
            case FieldConstants.MFTIME:
                data.numElements = numValid;
                break;

            case FieldConstants.SFVEC2D:
            case FieldConstants.SFVEC3D:
                data.numElements = 1;
                break;

            case FieldConstants.MFVEC2D:
                data.numElements = numValid / 2;
                break;

            case FieldConstants.MFVEC3D:
                data.numElements = numValid / 3;
                break;

            default:
                System.out.println("PrototypeDecl setValue(double[]) unknown " +
                                   "field type: " + decl.getFieldTypeString());
        }

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.STRING_DATA;
        data.stringValue = value;

        fieldValueMap.put(index, data);
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        // find out if we have the node
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(NO_FIELD_MSG + index);

        VRMLFieldData data = new VRMLFieldData();
        data.dataType = VRMLFieldData.STRING_ARRAY_DATA;
        data.stringArrayValue = value;
        data.numElements = numValid;

        fieldValueMap.put(index, data);
    }
}
