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
import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.Iterator;

// Local imports
import org.web3d.x3d.sai.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.lang.VRMLNodeTemplate;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

/**
 * The representation of a PROTO declaration.
 * <p>
 *
 * This is the representation of the declaration, not of a runtime node. For
 * this reason you cannot access the internals, nor can you work with the
 * individual field values. You can, however, perform basic introspection
 * tasks such as looking at the available field definitions and seeing the
 * basic node type.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
class SAIProtoDeclaration implements X3DProtoDeclaration  {

    /** Error message for when the declaration has been disposed of */
    private static final String DISPOSED_MSG =
        "This proto instance has been disposed of";

    /** The name of the proto */
    private final String name;

    /** The collection of fields for the proto */
    private final X3DFieldDefinition[] fields;

    /** Space that this proto belongs to as a parent */
    private VRMLExecutionSpace executionSpace;

    /** The template of the node itself */
    private VRMLNodeTemplate protoDecl;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** Factory used for field generation */
    private FieldFactory fieldFactory;

    /** Listener for dealing with the script wrapper for field access */
    private FieldAccessListener fieldAccessListener;

    /** The factory for node wrapper creation */
    private BaseNodeFactory baseNodeFactory;

    /**
     * Create a new instance of this class to represent the given decl.
     *
     * @param decl The declaration to copy
     * @param space The space this instance belongs to
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    SAIProtoDeclaration(VRMLNodeTemplate decl,
                        VRMLExecutionSpace space,
                        ReferenceQueue refQueue,
                        FieldFactory fac,
                        FieldAccessListener fal,
                        BaseNodeFactory bnf) {

        protoDecl = decl;
        executionSpace = space;
        fieldQueue = refQueue;
        fieldFactory = fac;
        fieldAccessListener = fal;
        baseNodeFactory = bnf;

        name = decl.getVRMLNodeName();

        List field_list = decl.getAllFields();
        Iterator itr = field_list.iterator();

        int idx = 0;
        fields = new X3DFieldDefinition[field_list.size()];

        while(itr.hasNext()) {
            VRMLFieldDeclaration f_decl = (VRMLFieldDeclaration)itr.next();

            // Need to validate that the constants are exactly the same for the
            // internal representation as the SAI-defined type.
            fields[idx] = new SAIFieldDefinition(f_decl.getName(),
                                                 f_decl.getAccessType(),
                                                 f_decl.getFieldType());
            idx++;
        }
    }

    //----------------------------------------------------------
    // Methods defined by X3DProtoDeclaration
    //----------------------------------------------------------

    /**
     * Get the type of this node. The string returned should be the name of
     * the VRML node or the name of the proto instance this node represents.
     *
     * @return The type of this node.
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public String getProtoName()
        throws InvalidNodeException {

        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return name;
    }

    /**
     * Fetch the type of this proto. The proto's type is defined by the first
     * child node in the body, in accordance with the X3D specification.
     * <p>
     * The types values are provided in the array of values. There is no
     * specific order of the returned types. It is expected that most node
     * types, which only descend from a single parent type would return an
     * array of length 1.
     *
     * @return The primary type(s) of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public int[] getNodeType()
        throws InvalidNodeException {

        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return( new int[]{ X3DNodeTypes.X3DProtoInstance } );
    }

    /**
     * Get the list of fields that this node contains. This will return one
     * definition for each field regardless of whether it is eventIn/Out,
     * exposedField or field access type.
     *
     * @return The definitions for all fields of this node
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DFieldDefinition[] getFieldDefinitions()
        throws InvalidNodeException {

        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return fields;
    }

    /**
     * Create an instance of this proto that may be used at runtime.
     *
     * @return An instance of this proto to work with
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DProtoInstance createInstance()
        throws InvalidNodeException {

        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        BasicScene scene = executionSpace.getContainedScene();
        VRMLNode root = scene.getRootNode();

        VRMLNodeType node =
            (VRMLNodeType)protoDecl.createNewInstance(root, false);


        if(node == null)
            return null;

        SAIProtoInstanceImpl instance =
            new SAIProtoInstanceImpl((VRMLProtoInstance)node,
                                     fieldQueue,
                                     fieldFactory,
                                     fieldAccessListener,
                                     baseNodeFactory);

        instance.setAccessValid(true);

        return instance;
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

        protoDecl = null;
        executionSpace = null;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Fetch the Xj3D internal representation of this proto declaration.
     *
     * @return The internal instance
     */
    VRMLNodeTemplate getInternalDecl() {
        return protoDecl;
    }
}
