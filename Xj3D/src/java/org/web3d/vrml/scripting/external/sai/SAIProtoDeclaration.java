/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.Iterator;

// Local imports
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoDeclare;
import org.web3d.x3d.sai.InvalidNodeException;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DNodeTypes;
import org.web3d.x3d.sai.X3DProtoDeclaration;
import org.web3d.x3d.sai.X3DProtoInstance;

/**
 * Implementation wrapper of the X3DProtoDeclaration interface
 * <p>
 * Based to a significant amount on the ProtoDeclaration
 * implementation for the EcmaScript scripting.
 *
 * @author Brad Vender
 * @version $Revision: 1.6 $
 */
public class SAIProtoDeclaration implements X3DProtoDeclaration {

    /** Message after we've been disposed */
    private static final String DISPOSED_MSG =
        "This proto declaration has been disposed of";

    /** Used for wrapping the constructed proto instances */
    private SAINodeFactory nodeFactory;

    /** The underlying proto declaration */
    private VRMLProtoDeclare protoDecl;

    /** The parent scene this declaration belongs to */
    private BasicScene realScene;

    SAIProtoDeclaration(SAINodeFactory factory,
                        VRMLProtoDeclare protoDecl,
                        BasicScene scene) {
        nodeFactory = factory;
        this.protoDecl = protoDecl;
        realScene = scene;
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#getProtoName()
     */
    public String getProtoName() throws InvalidNodeException {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return protoDecl.getVRMLNodeName();
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#getNodeType()
     */
    public int[] getNodeType() throws InvalidNodeException {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return( new int[]{ X3DNodeTypes.X3DProtoInstance } );
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#getFieldDefinitions()
     */
    public X3DFieldDefinition[] getFieldDefinitions()
            throws InvalidNodeException {

        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        X3DFieldDefinition[] result =
            new X3DFieldDefinition[protoDecl.getFieldCount()];
        Iterator i = protoDecl.getAllFields().iterator();

        for(int counter = 0; i.hasNext(); counter++)
            result[counter] = new SAIFieldDefinition((VRMLFieldDeclaration) i.next());

        return result;
    }
    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#createInstance()
     */
    public X3DProtoInstance createInstance() throws InvalidNodeException {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        VRMLNodeType node =
            (VRMLNodeType)protoDecl.createNewInstance(realScene.getRootNode(),
                                                      false);

        return nodeFactory.getSAIProtoNode(node,this);
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#dispose()
     */
    public void dispose() throws InvalidNodeException {
         protoDecl = null;
         nodeFactory = null;
         realScene = null;
    }

    /** Get the underlying PROTO declaration for working with non-wrapper code */
    VRMLProtoDeclare getRealProtoDeclaration() {
        return protoDecl;
    }
}
