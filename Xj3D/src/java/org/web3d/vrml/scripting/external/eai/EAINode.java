package org.web3d.vrml.scripting.external.eai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import vrml.eai.field.EventOut;
import vrml.eai.*;
import vrml.eai.field.EventIn;

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * EAINode is a simple implementation of the vrml.eai.Node class.
 * It serves simply as a wrapper around the VRMLNodeType implementation,
 * although that task does include producing wrappers around the various
 * EventIn and EventOut classes.
 * <P>
 * @author Brad Vender
 * @version 1.0
 */

public class EAINode extends Node {
    /** The VRMLNodeType instance that this node maps to */
    VRMLNodeType realNode;

    /** The EAIFieldFactory instance used to produce EventIn's and EventOut's.*/
    EAIFieldFactory theFieldFactory;

    /** The VRMLNodeFactory instance used in mapping between VRMLNodeType and
     *  vrml.eai.Node.  Mainly for use in constructing the 
     *  Event*NodeWrapper's */
    VRMLNodeFactory theNodeFactory;

    /** Basic constructor.
     * @param theRealNode The original VRMLNodeType instance
     * @param aNodeFactory The VRMLNodeFactory instance to use for mapping
              between VRMLNodeType and vrml.eai.Node.
     * @param aFieldFactory The EAIFieldFactory instance to use for making
              EventOut's and EventIn's.
     */
    EAINode(
        VRMLNodeType theRealNode, VRMLNodeFactory aNodeFactory,
        EAIFieldFactory aFieldFactory
    ) {
        if (theRealNode==null)
            throw new vrml.eai.InvalidNodeException();
        realNode=theRealNode;
        theNodeFactory=aNodeFactory;
        theFieldFactory=aFieldFactory;
    }

    /** @see vrml.eai.Node#getEventIn */
    public EventIn getEventIn(String eventName) 
    throws vrml.eai.field.InvalidEventInException, 
    vrml.eai.InvalidNodeException {
        if (realNode==null)
            throw new vrml.eai.InvalidNodeException();
        else
            return theFieldFactory.getEventIn(realNode, eventName);
    }

    /** @see vrml.eai.Node#getEventOut */
    public EventOut getEventOut(String eventName) 
    throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException {
        if (realNode==null)
            throw new vrml.eai.InvalidNodeException();
        else
            return theFieldFactory.getEventOut(realNode, eventName);
    }

    /** @see vrml.eai.Node#getType */
    public String getType() throws vrml.eai.InvalidNodeException {
        /* This should work for most of the nodes */
        if (realNode==null)
            throw new vrml.eai.InvalidNodeException();
        else
            return realNode.getVRMLNodeName();
    }

    /** Return the real VRMLNodeType object that we wrap around.  */
    VRMLNodeType getVRMLNode() {
        return realNode;
    }

    /** Two EAINode's are equal if they point to the same actual node. */
    public boolean equals(Object other) {
        if (other instanceof EAINode) {
            EAINode othernode=(EAINode)other;
            if (realNode!=null)
                return realNode.equals(othernode.realNode);
            else
                return othernode.realNode==null;
        } else 
            return super.equals(other);
    }

    /** @see vrml.eai.Node#dispose
     * Calling dispose gets rid of any resource allocated to this object, and
     * releases any references to actual VRML objects.  Calling any other
     * methods of this object after dispose (including this one) will result in
     * vrml.eai.InvalidNodeException being thrown.  The only exception is
     * equals, because that just seemed silly.
     * <P>
     */
    public void dispose() throws vrml.eai.InvalidNodeException {
        if (realNode==null)
            throw new vrml.eai.InvalidNodeException();
        else {
        }
        realNode=null;
    }

}

