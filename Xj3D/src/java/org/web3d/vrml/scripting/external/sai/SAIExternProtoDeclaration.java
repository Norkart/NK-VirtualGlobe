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
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLExternProtoDeclare;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.x3d.sai.InvalidNodeException;
import org.web3d.x3d.sai.X3DExternProtoDeclaration;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DProtoInstance;

import org.xj3d.sai.X3DNodeTypeMapper;

/**
 * Wrapper class for external PROTO declaration data.
 */
public class SAIExternProtoDeclaration
implements X3DExternProtoDeclaration {

    /** Message after we've been disposed */
    private static final String DISPOSED_MSG =
        "This proto declaration has been disposed of";

    /** Used for wrapping the constructed proto instances */
    private SAINodeFactory nodeFactory;

    /** The underlying proto declaration */
    private VRMLExternProtoDeclare protoDecl;

    /** The parent scene this declaration belongs to */
    private BasicScene realScene;

    /** Node type mapper */
    private X3DNodeTypeMapper typeMapper;

    public SAIExternProtoDeclaration(SAINodeFactory factory,
                                     VRMLExternProtoDeclare externProto,
                                     BasicScene scene) {
        nodeFactory = factory;
        protoDecl = externProto;
        realScene = scene;
    }

    /**
     * @see org.web3d.x3d.sai.X3DExternProtoDeclaration#getLoadState()
     */
    public int getLoadState() {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return protoDecl.getLoadState();
    }

    /**
     * @see org.web3d.x3d.sai.X3DExternProtoDeclaration#loadNow()
     */
    public void loadNow() {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        System.out.println("External SAI ExternProto.loadNow() Not yet implemented");
    }

    /**
     * @see org.web3d.x3d.sai.X3DExternProtoDeclaration#getURLs()
     */
    public String[] getURLs() {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        return protoDecl.getUrl();
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

        if ( typeMapper == null ) {
            typeMapper = X3DNodeTypeMapper.getInstance( );
        }
        return( typeMapper.getInterfaceTypes( protoDecl.getVRMLNodeName( ) ) );
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#getFieldDefinitions()
     */
    public X3DFieldDefinition[] getFieldDefinitions() throws InvalidNodeException {
        if(protoDecl == null)
            throw new InvalidNodeException(DISPOSED_MSG);

        X3DFieldDefinition result[]=new X3DFieldDefinition[protoDecl.getFieldCount()];
        Iterator i=protoDecl.getAllFields().iterator();
        int counter=0;
        while (i.hasNext()) {
            result[counter++]=new SAIFieldDefinition((VRMLFieldDeclaration) i.next());
        }
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

        if(node == null)
            return null;
        else
            return nodeFactory.getSAIProtoNode(node, this);
    }

    /**
     * @see org.web3d.x3d.sai.X3DProtoDeclaration#dispose()
     */
    public void dispose() throws InvalidNodeException {
         protoDecl = null;
         nodeFactory = null;
         realScene = null;
    }

}
