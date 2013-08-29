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

package org.web3d.vrml.scripting.external.sai;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.x3d.sai.*;

import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.xj3d.sai.X3DNodeTypeMapper;

/**
 * SAINode is a simple implementation of the vrml.eai.Node class.
 * It serves simply as a wrapper around the VRMLNodeType implementation,
 * although that task does include producing wrappers around the various
 * EventIn and EventOut classes.
 *
 * @author Brad Vender
 * @version 1.0
 */
public class SAINode implements X3DNode {
    
    /** The VRMLNodeType instance that this node maps to */
    private VRMLNodeType realNode;

    /** Event queue to deal with node realization */
    private ExternalEventQueue theExternalEventQueue;

    /** The SAIFieldFactory instance used to produce EventIn's and EventOut's.*/
    private SAIFieldFactory theFieldFactory;

    /** The SAINodeFactory instance used in mapping between VRMLNodeType and
     *  X3DNode.  Mainly for use in constructing the Event*NodeWrapper's */
    private SAINodeFactory theNodeFactory;

    private X3DFieldDefinition[] fieldList;

    /** Node type mapper */
    private X3DNodeTypeMapper typeMapper;

    /** 
     * Basic constructor.
     * @param theRealNode The original VRMLNodeType instance
     * @param aNodeFactory The SAINodeFactory instance to use for mapping
              between VRMLNodeType and vrml.eai.Node.
     * @param aFieldFactory The SAIFieldFactory instance to use for making
              EventOut's and EventIn's.
     * @param queue The event processing queue
     */
    public SAINode(VRMLNodeType theRealNode, SAINodeFactory aNodeFactory,
      SAIFieldFactory aFieldFactory, ExternalEventQueue queue) {
      if (theRealNode==null)
          throw new InvalidNodeException();
      theExternalEventQueue=queue;
      realNode=theRealNode;
      theNodeFactory=aNodeFactory;
      theFieldFactory=aFieldFactory;
    }

    /**
     * Set the Metadata object that belongs to this node. If the object
     * instance is null, then it clears the currently set node instance.
     *
     * @param node The new node instance to use
     */
    public void setMetadata(X3DMetadataObject node) {
    	if (realNode==null)
    		throw new InvalidNodeException();
    	/* No read/write check because metadata is [in,out] */
    	SAINode realValue=(SAINode) node;
    	realNode.setMetadataObject(theNodeFactory.getVRMLNode(realValue));
    }

    /**
     * Get the metadata object associated with this node. If none is set, it
     * will return null.
     *
     * @return The metadata object instance or null
     */
    public X3DMetadataObject getMetadata() {
    	if (realNode==null)
    		throw new InvalidNodeException();
    	/* No read/write check because metadata is [in,out] */
    	return (X3DMetadataObject) theNodeFactory.getSAINode(realNode.getMetadataObject());
    }


    public X3DFieldDefinition[] getFieldDefinitions()
        throws InvalidNodeException
    {
        if (fieldList != null)
            return fieldList;

        if (realNode==null)
            throw new InvalidNodeException();

        ArrayList fields = new ArrayList();

        if (realNode instanceof VRMLProtoInstance) {
            List nodeFields = ((VRMLProtoInstance)realNode).getAllFields();

            Iterator itr = nodeFields.iterator();
            VRMLFieldDeclaration decl;

            while(itr.hasNext()) {
                decl = (VRMLFieldDeclaration) itr.next();
                fields.add(new SAIFieldDefinition(decl));
            }

            fieldList = new SAIFieldDefinition[fields.size()];
            fields.toArray(fieldList);
        } else {
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

            int len = realNode.getNumFields();

            for(int i = 0; i < len; i++) {
                VRMLFieldDeclaration decl = realNode.getFieldDeclaration(i);

                if(decl == null)
                    continue;

                fields.add(new SAIFieldDefinition(decl));
            }

            fieldList = new SAIFieldDefinition[fields.size()];
            fields.toArray(fieldList);
        }

        return fieldList;
    }

    public X3DField getField(String name)
        throws InvalidFieldException, InvalidNodeException
    {
        if (realNode==null)
            throw new InvalidNodeException();
        else
            return theFieldFactory.getField(realNode,name);
    }

    public String getNodeName() throws InvalidNodeException {
        if (realNode==null)
            throw new InvalidNodeException();

        return realNode.getVRMLNodeName();
    }

    public int[] getNodeType() throws InvalidNodeException {
        if (realNode==null)
            throw new InvalidNodeException();

        if ( typeMapper == null ) {
            typeMapper = X3DNodeTypeMapper.getInstance( );
        }
        return( typeMapper.getInterfaceTypes( realNode.getVRMLNodeName( ) ) );
    }

    /**
     * Return the real VRMLNodeType object that we wrap around.
     */
    VRMLNodeType getVRMLNode() {
      return realNode;
    }

    /** Two SAINode's are equal if they point to the same actual node. */
    public boolean equals(Object other) {
        if (other instanceof SAINode) {
            SAINode othernode=(SAINode)other;
        if (realNode!=null)
            return realNode.equals(othernode.realNode);
        else
            return othernode.realNode==null;
        } else return super.equals(other);
    }

    /**
     * Returns a hash code value for the object.
     * This method is supported for the benefit of hashtables such as those provided
     * by java.util.Hashtable.
     */
    public int hashCode() {
        if (realNode == null)
            return 0;

        return realNode.hashCode();
    }

    /** @see vrml.eai.Node#dispose
     * Calling dispose gets rid of any resource allocated to this object, and
     * releases any references to actual VRML objects.  Calling any other
     * methods of this object after dispose (including this one) will result in
     * vrml.eai.InvalidNodeException being thrown.  The only exception is
     * equals, because that just seemed silly.
     */
    public void dispose() throws InvalidNodeException {
        if (realNode==null)
            throw new InvalidNodeException();
        realNode=null;
    }

    public boolean isRealized() {
        if (realNode==null)
            throw new InvalidNodeException();
        else
            return theExternalEventQueue.isNodeRealized(realNode);
    }

    public void realize() {
        if (realNode==null)
            throw new InvalidNodeException();
        else
            theExternalEventQueue.postRealizeNode(realNode);
    }
}
