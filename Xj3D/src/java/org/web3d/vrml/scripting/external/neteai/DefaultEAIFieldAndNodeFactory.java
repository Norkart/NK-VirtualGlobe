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

package org.web3d.vrml.scripting.external.neteai;

import java.io.DataInputStream;
import java.io.IOException;

import org.web3d.util.IntHashMap;
import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.Node;
import vrml.eai.field.BaseField;
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;
import vrml.eai.field.InvalidFieldException;

/**
 * DefaultEAIFieldAndNodeFactory is a simple implementation of
 * EAIFieldAndNodeFactory.  It handles both the tasks of mapping
 * between NetEAINode instances and node network IDs and it also handles
 * constructing the field instances.
 */
public class DefaultEAIFieldAndNodeFactory implements EAIFieldAndNodeFactory {

    /** The event queue the eventIn fields feed into */
    NetworkEventQueue eventQueue;

    /** The request processor the fields need */
    FieldAndNodeRequestProcessor requestProcessor;

    /** The node network ID to node table */
    IntHashMap nodeTable;

    /** The field network ID to field table */
    IntHashMap fieldTable;

    /**
     * Basic constructor.  The arguments are passed as needed to
     * the field and node instances.
     * @param processor The object which handles field and node requests
     * @param externalEventQueue The queue for eventIn's to post to
     */
    public DefaultEAIFieldAndNodeFactory(FieldAndNodeRequestProcessor processor,
            NetworkEventQueue externalEventQueue) {
        nodeTable=new IntHashMap();
        fieldTable=new IntHashMap();
        requestProcessor=processor;
        eventQueue=externalEventQueue;
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EAIFieldAndNodeFactory#createNode(int, java.lang.String)  */
    public Node createNode(int nodeID) {
        Node result=(Node) nodeTable.get(nodeID);
        if (result==null){
            result=new NetEAINode(nodeID,requestProcessor);
            nodeTable.put(nodeID,result);
        }
        return result;
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EAIFieldAndNodeFactory#generateEventIn(int, int, java.lang.String)  */
    public EventIn generateEventIn(int fieldID, int fieldType) {
        switch (fieldType) {
            case BaseField.MFColor:
                return new EventInMFColorWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFFloat:
                return new EventInMFFloatWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFInt32:
                return new EventInMFInt32Wrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFNode:
                return new EventInMFNodeWrapper(fieldID,requestProcessor,this,eventQueue);
            case BaseField.MFRotation:
                return new EventInMFRotationWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFString:
                return new EventInMFStringWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFTime:
                return new EventInMFTimeWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFVec2f:
                return new EventInMFVec2fWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.MFVec3f:
                return new EventInMFVec3fWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFBool:
                return new EventInSFBoolWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFColor:
                return new EventInSFColorWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFFloat:
                return new EventInSFFloatWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFImage:
                return new EventInSFImageWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFInt32:
                return new EventInSFInt32Wrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFNode:
                return new EventInSFNodeWrapper(fieldID,requestProcessor,eventQueue,this);
            case BaseField.SFRotation:
                return new EventInSFRotationWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFString:
                return new EventInSFStringWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFTime:
                return new EventInSFTimeWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFVec2f:
                return new EventInSFVec2fWrapper(fieldID,requestProcessor,eventQueue);
            case BaseField.SFVec3f:
                return new EventInSFVec3fWrapper(fieldID,requestProcessor,eventQueue);
            default:
                throw new InvalidFieldException();
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EAIFieldAndNodeFactory#getEventOut(int, int, java.lang.String)  */
    public EventOut getEventOut(int fieldID, int fieldType) {
        switch (fieldType) {
        case BaseField.MFColor:
            return new EventOutMFColorWrapper(fieldID,requestProcessor);
        case BaseField.MFFloat:
            return new EventOutMFFloatWrapper(fieldID,requestProcessor);
        case BaseField.MFInt32:
            return new EventOutMFInt32Wrapper(fieldID,requestProcessor);
        case BaseField.MFNode:
            return new EventOutMFNodeWrapper(fieldID,requestProcessor,this);
        case BaseField.MFRotation:
            return new EventOutMFRotationWrapper(fieldID,requestProcessor);
        case BaseField.MFString:
            return new EventOutMFStringWrapper(fieldID,requestProcessor);
        case BaseField.MFTime:
            return new EventOutMFTimeWrapper(fieldID,requestProcessor);
        case BaseField.MFVec2f:
            return new EventOutMFVec2fWrapper(fieldID,requestProcessor);
        case BaseField.MFVec3f:
            return new EventOutMFVec3fWrapper(fieldID,requestProcessor);
        case BaseField.SFBool:
            return new EventOutSFBoolWrapper(fieldID,requestProcessor);
        case BaseField.SFColor:
            return new EventOutSFColorWrapper(fieldID,requestProcessor);
        case BaseField.SFFloat:
            return new EventOutSFFloatWrapper(fieldID,requestProcessor);
        case BaseField.SFImage:
            return new EventOutSFImageWrapper(fieldID,requestProcessor);
        case BaseField.SFNode:
            return new EventOutSFNodeWrapper(fieldID,requestProcessor,this);
        case BaseField.SFRotation:
            return new EventOutSFRotationWrapper(fieldID,requestProcessor);
        case BaseField.SFString:
            return new EventOutSFStringWrapper(fieldID,requestProcessor);
        case BaseField.SFTime:
            return new EventOutSFTimeWrapper(fieldID,requestProcessor);
        case BaseField.SFVec2f:
            return new EventOutSFVec2fWrapper(fieldID,requestProcessor);
        case BaseField.SFVec3f:
            return new EventOutSFVec3fWrapper(fieldID,requestProcessor);
        default:
            throw new InvalidFieldException();
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EAIFieldAndNodeFactory#getStoredEventOut(int, int, java.lang.String, java.io.DataInputStream)  */
    public EventOut getStoredEventOut(int fieldID, int fieldType, DataInputStream source) throws IOException {
        switch (fieldType) {
        case BaseField.MFColor:
            return new EventOutMFColorWrapper(fieldID,requestProcessor,source);
        case BaseField.MFFloat:
            return new EventOutMFFloatWrapper(fieldID,requestProcessor,source);
        case BaseField.MFInt32:
            return new EventOutMFInt32Wrapper(fieldID,requestProcessor,source);
        case BaseField.MFNode:
            return new EventOutMFNodeWrapper(fieldID,requestProcessor,this,source);
        case BaseField.MFRotation:
            return new EventOutMFRotationWrapper(fieldID,requestProcessor,source);
        case BaseField.MFString:
            return new EventOutMFStringWrapper(fieldID,requestProcessor,source);
        case BaseField.MFTime:
            return new EventOutMFTimeWrapper(fieldID,requestProcessor,source);
        case BaseField.MFVec2f:
            return new EventOutMFVec2fWrapper(fieldID,requestProcessor,source);
        case BaseField.MFVec3f:
            return new EventOutMFVec3fWrapper(fieldID,requestProcessor,source);
        case BaseField.SFBool:
            return new EventOutSFBoolWrapper(fieldID,requestProcessor,source);
        case BaseField.SFColor:
            return new EventOutSFColorWrapper(fieldID,requestProcessor,source);
        case BaseField.SFFloat:
            return new EventOutSFFloatWrapper(fieldID,requestProcessor,source);
        case BaseField.SFImage:
            return new EventOutSFImageWrapper(fieldID,requestProcessor,source);
        case BaseField.SFInt32:
            return new EventOutSFInt32Wrapper(fieldID,requestProcessor,source);
        case BaseField.SFNode:
            return new EventOutSFNodeWrapper(fieldID,requestProcessor,this,source);
        case BaseField.SFRotation:
            return new EventOutSFRotationWrapper(fieldID,requestProcessor,source);
        case BaseField.SFString:
            return new EventOutSFStringWrapper(fieldID,requestProcessor,source);
        case BaseField.SFTime:
            return new EventOutSFTimeWrapper(fieldID,requestProcessor,source);
        case BaseField.SFVec2f:
            return new EventOutSFVec2fWrapper(fieldID,requestProcessor,source);
        case BaseField.SFVec3f:
            return new EventOutSFVec3fWrapper(fieldID,requestProcessor,source);
        default:
            throw new InvalidFieldException();
        }

    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EAIFieldAndNodeFactory#getNodeID(vrml.eai.Node)  */
    public int getNodeID(Node node) {
        if (node instanceof NetEAINode)
            return ((NetEAINode)node).nodeID;
        else
            throw new RuntimeException("Incorrect node factory for node");
    }

}
