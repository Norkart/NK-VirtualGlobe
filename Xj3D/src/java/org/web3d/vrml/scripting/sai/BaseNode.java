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

package org.web3d.vrml.scripting.sai;

// Standard imports
import java.lang.ref.SoftReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.x3d.sai.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.sai.X3DNodeTypeMapper;

/**
 * The base representation of any VRML node in the system whether built in or
 * a proto.
 * <p>
 *
 * May be used as a standalone node, or extended with the abstract data types
 * defined by higher levels of the SAI conformance.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class BaseNode implements X3DNode {

    /** Message for when a node has been disposed of */
    private static final String NODE_DISPOSED_MSG =
        "The node has been disposed. No valid handle exists";

    /**
     * Error message when an operation occurs at an invalid time in the
     * event model. See 19775 Part 2, 4.8.3.7 User code lifecycle for
     * more information.
     */
    private static final String INVALID_TIMING_ERR =
        "You are attempting to access a node when not permitted by the " +
        "specification. Please see 19775 Part 2, 4.8.3.7 User code lifecycle " +
        "for more information";

    /** This is the real node that this object represents */
    protected VRMLNodeType realNode;

    /**
     * Map containing the field name as key and a SoftReference instance
     * that points to the field object. The weak reference is used so that
     * fields can be removed automatically by Java if no longer referenced
     * by user code.
     */
    protected HashMap fieldRefMap;

    /** Reference queue used for keeping track of field object instances */
    protected ReferenceQueue fieldQueue;

    /** Flag for determining the read/write timing ability */
    protected boolean accessPermitted;

    /**
     * Set of fields that have ever been accessed from this node. Used
     * as a performance enhancer, at the cost of memory, for dealing with
     * fast setAccessValid() calls.
     */
    private HashSet accessedFieldsSet;
    private String[] accessedFieldsList;

    /** Factory used for field generation */
    private FieldFactory fieldFactory;

    /** Listener for dealing with the script wrapper for field access */
    private FieldAccessListener fieldAccessListener;

    /** The factory for node wrapper creation */
    private BaseNodeFactory baseNodeFactory;
    
    /** Listing of all the field defintions. Created on demand */
    private X3DFieldDefinition[] fieldList;

    /** The X3DNodeTypes array for this node */
    private int[] nodeType;
    
    /** Node type mapper */
    private X3DNodeTypeMapper typeMapper;

    /**
     * Create a new basic node implementation.
     *
     * @param node The source node this is wrapping
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public BaseNode(VRMLNodeType node,
                    ReferenceQueue refQueue,
                    FieldFactory fac,
                    FieldAccessListener fal,
                    BaseNodeFactory bnf ) {
        realNode = node;
        fieldQueue = refQueue;
        fieldFactory = fac;
        fieldAccessListener = fal;
        baseNodeFactory = bnf;

        accessedFieldsSet = new HashSet();
        fieldRefMap = new HashMap();

        accessedFieldsList = new String[1];
        accessPermitted = false;
    }

    /**
     * Set the Metadata object that belongs to this node. If the object
     * instance is null, then it clears the currently set node instance.
     *
     * @param node The new node instance to use
     */
    public void setMetadata(X3DMetadataObject node)
        throws InvalidNodeException {

        checkAccess();
    }

    /**
     * Get the metadata object associated with this node. If none is set, it
     * will return null.
     *
     * @return The metadata object instance or null
     */
    public X3DMetadataObject getMetadata()
        throws InvalidNodeException {

        checkAccess();

        return null;
    }

    /**
     * Get the type of this node. The string returned should be the name of
     * the VRML node or the name of the proto instance this node represents.
     *
     * @return The type of this node.
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public String getNodeName()
        throws InvalidNodeException {

        checkAccess();

        return realNode.getVRMLNodeName();
    }

    /**
     * Get the list of fields that this node contains. This will return one
     * definition for each field regardless of whether it is eventIn/Out,
     * exposedField or field access type.
     *
     * @return The definitions for all fields of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DFieldDefinition[] getFieldDefinitions()
        throws InvalidNodeException {

        checkAccess();

        if(fieldList == null) {
            ArrayList fields = new ArrayList();

            // TODO:
            // this is a real dodgy way of doing things for now. It assumes
            // that field indexes are sequential, with no holes. This won't
            // really work correctly for the general case - particularly
            // dynamic nodes like Protos and Scripts. Also, in nodes that
            // have two different indexes for the same field but different
            // names because of VRML97 v X3D, then it will double up those and
            // present the whole lot, which it shouldn't. The main issue is
            // that there should be a getAllFields() method on VRMLNode that
            // allows a caller to introspect all fields on a given node. We
            // don't have one right now. Should add.
            for(int i = 0; ; i++) {
                VRMLFieldDeclaration decl = realNode.getFieldDeclaration(i);
                if(decl == null)
                    break;

                fields.add(new SAIFieldDefinition(decl.getName(),
                                                  decl.getAccessType(),
                                                  decl.getFieldType()));
            }

            fieldList = new SAIFieldDefinition[fields.size()];
            fields.toArray(fieldList);
        }

        return fieldList;
    }

    /**
     * Notify this node that its setup stage is now complete. This will cause
     * all its fields to become non-writable, leaving only eventIns and
     * exposedFields writable. A user is not required to call this method as
     * it will be implicitly called immediately this node is added to any
     * other node. Any call after the first is ignored.
     *
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public void realize()
        throws InvalidNodeException {

        checkAccess();

        // If there are any outstanding field updates, either of this node
        // or child nodes, force it now. Once setupFinished() is called on
        // the realNode, you won't be able to send any updates to the node
        // at a later date.
        updateNodeAndChildren();
        realNode.setupFinished();
    }

    /**
     * Check to see if this node has completed its setup either by being
     * directly informed of it or through implicit measures (see the
     * specification for details).
     *
     * @return true if this node has completed the setup stage, false otherwise
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public boolean isRealized()
        throws InvalidNodeException {

        if(realNode == null)
            throw new InvalidNodeException(NODE_DISPOSED_MSG);

        return realNode.isSetupFinished();
    }

    /**
     * Get the basic categorisation type(s) of this node. The types values are
     * provided in the array of values. There is no specific order of the
     * returned types. It is expected that most node types, which only descend
     * from a single parent type would return an array of length 1.
     * The returned value(s) should be the most derived type applicable for
     * that node. For example, a Material node should return MaterialNodeType
     * value, not AppearanceChildNodeType value.
     *
     * @return The primary type(s) of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public int[] getNodeType()
        throws InvalidNodeException {

        checkAccess();

        if ( nodeType == null ) {
            X3DNodeTypeMapper typeMapper = X3DNodeTypeMapper.getInstance( );
            nodeType = typeMapper.getInterfaceTypes( realNode.getVRMLNodeName( ) );
        }
        return( nodeType );
    }

    /**
     * Get a field for this node.
     * <P>
     * If the basic field required is an exposedField you can use either the
     * standard name (such as <i>translation</i>) or you can use the <i>set_</i>
     * / <i>_changed</i> modifier (such as <i>set_translation</i>). If the field
     * asked for is of field access type then an object is returned that cannot
     * be read or written to. However, this allows the option for building
     * editor type applications that may permit reading and writing of field
     * access types when not running the VRML event model.
     *
     * @param name The name of the field that is required
     * @return A reference to the field requested.
     * @throws InvalidFieldException The named field does not exist for
     *   this node.
     * @throws InvalidNodeException The node has had it's resources disposed
     *   of
     */
    public X3DField getField(String name)
        throws InvalidFieldException, InvalidNodeException {

          // TODO: This is wrong sometimes
//        checkAccess();

        BaseField ret_val = null;

        SoftReference ref = (SoftReference)fieldRefMap.get(name);

        if(ref != null)
            ret_val = (BaseField)ref.get();

        if(ret_val == null) {
            // create a new field instance. Since this is a field of
            // a node, it can never be an internal node.
            ret_val = fieldFactory.createField(realNode,
                                               name,
                                               true,
                                               false,
                                               fieldQueue,
                                               baseNodeFactory);

//System.out.println("get field: " + name + " real_node: " + realNode + " fw: " + ret_val);

            if(ret_val == null) {
                throw new InvalidFieldException(
                    realNode.getVRMLNodeName() +
                    " does not have a field named " +
                    name);
            }

            ret_val.setFieldAccessListener(fieldAccessListener);
            ref = new SoftReference(ret_val, fieldQueue);
            fieldRefMap.put(name, ref);
            accessedFieldsSet.add(name);
        }

        ret_val.updateField();
        ret_val.setAccessValid(accessPermitted);

        int num_fields = accessedFieldsSet.size();

        if(accessedFieldsList.length < num_fields)
            accessedFieldsList = new String[num_fields + 1];

        accessedFieldsSet.toArray(accessedFieldsList);

        return ret_val;
    }

    /**
     * Dispose of this node's resources. This is used to indicate to the
     * browser that the java side of the application does not require the
     * resources represented by this node. The browser is now free to do
     * what it likes with the node.
     * <P>
     * This in no way implies that the browser is to remove this node from
     * the scene graph, only that the java code is no longer interested
     * in this particular node through this reference.
     * <P>
     * Once this method has been called, any further calls to methods of
     * this instance of the class is shall generate an InvalidNodeException.
     *
     * @throws InvalidNodeException The node is no longer valid and can't be
     *    disposed of again.
     */
    public void dispose()
        throws InvalidNodeException {

        checkAccess();

        realNode = null;
        fieldList = null;
        fieldRefMap = null;
        fieldQueue = null;
        fieldFactory = null;
        baseNodeFactory = null;
    }

    /**
     * Generate the hashcode for this object. In order to allow this node to
     * be used as a key lookup in a hashMap, we want to override this to make
     * sure that we compare against the real node that we're wrapping. In
     * doing so, we're maintaining the contract required by Java as the
     * equals() method compares for the same thing.
     *
     * @return The hash value for this object
     */
    public int hashCode() {
        return realNode.hashCode();
    }

    /**
     * Compare this node for equality to another. To do this, we want to see
     * if they're representing the same base internal node instance. If they
     * are (through using the reference comparison), then return true;
     *
     * @param obj The object to compare against
     * @return true if these represent the same internal object
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof BaseNode))
            return false;

        return ((BaseNode)obj).realNode == realNode;
    }

    /**
     * Generate a string version of this node's representation.
     *
     * @return A String representation of this node
     */
    public String toString()
        throws InvalidNodeException {

        StringBuffer buf = new StringBuffer(realNode.getVRMLNodeName());
        buf.append(" {");

        // Eventually we will iterate through all the fields here and print
        // them out.
        buf.append("\n}");

        return buf.toString();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Convenience method to check if the node may be accessed at this
     * point in time. If it is not, it will throw an exception appropriate
     * to the situation.
     *
     * @throws InvalidNodeException The node handle is no longer valid
     * @throws InvalidOperationTimingException Can't access the node
     *    at this point in time
     */
    protected void checkAccess() {
        if(realNode == null)
            throw new InvalidNodeException(NODE_DISPOSED_MSG);

        if(!accessPermitted)
            throw new InvalidOperationTimingException(INVALID_TIMING_ERR);
    }

    /**
     * Convenience method to fetch the reference to the local internal node
     * representation.
     *
     * @return The node this object encapsulates
     */
    VRMLNodeType getImplNode() {
        return realNode;
    }

    /**
     * Update all of the fields of this node.
     */
    void updateNodeAndChildren() {

        int num_fields = accessedFieldsSet.size();
        if(num_fields == 0)
            return;

        for(int i = 0; i < num_fields; i++) {
            String name = accessedFieldsList[i];
            SoftReference ref = (SoftReference)fieldRefMap.get(name);
            if(ref == null)
                continue;

            BaseField f = (BaseField)ref.get();

            if(f == null) {
                fieldRefMap.remove(name);
            } else {
                if(f instanceof NodeField)
                    ((NodeField)f).updateNodeAndChildren();
                else if(f.hasChanged())
                    f.updateNode();
            }
        }
    }

    /**
     * Update all of the fields of this node.
     */
    void updateFields() {

        int num_fields = accessedFieldsSet.size();
        if(num_fields == 0)
            return;

        for(int i = 0; i < num_fields; i++) {
            String name = accessedFieldsList[i];
            SoftReference ref = (SoftReference)fieldRefMap.get(name);
            if(ref == null)
                continue;

            BaseField f = (BaseField)ref.get();

            if(f == null) {
                fieldRefMap.remove(name);
            } else {
                if(f instanceof NodeField)
                    ((NodeField)f).updateFieldAndChildren();
                else
                    f.updateField();
            }
        }
    }

    /**
     * Chained method to control whether operations are valid on the fields
     * of this node instance right now. Works on only the fields that
     * still have valid internal references.
     *
     * @param valid True if access operations are now permitted.
     */
    void setAccessValid(boolean valid) {

        accessPermitted = valid;
        int num_fields = accessedFieldsSet.size();
        if(num_fields == 0)
            return;

        if(accessedFieldsList.length < num_fields) {
            accessedFieldsList = new String[num_fields + 1];
            accessedFieldsSet.toArray(accessedFieldsList);
        }

        for(int i = 0; i < num_fields; i++) {
            String name = accessedFieldsList[i];
            SoftReference ref = (SoftReference)fieldRefMap.get(name);
            if(ref == null)
                continue;

            BaseField f = (BaseField)ref.get();

            if(f == null) {
                fieldRefMap.remove(name);
            } else {
                f.setAccessValid(valid);
            }
        }
    }
}
