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
import vrml.eai.field.EventIn;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

/**
 * SimpleEAIFieldFactory is an implementation of EAIFieldFactory which
 * performs no mapping to reduce duplicate object creation.
 * <P>
 * setNodeFactory must be used after the constructor to properly
 * initialize this class.
 * @author Brad Vender
 */

class SimpleEAIFieldFactory implements EAIFieldFactory {
    /** The EAIEventAdapterFactory for the eventOut instances. */
    EAIEventAdapterFactory theEventAdapterFactory;

    /** The queue for posting events.  Field objects need to know this
      * information to post events, and this class creates field objects. */
    ExternalEventQueue theEventQueue;

    /** The VRMLNodeFactory instance used in mapping between VRMLNodeType 
      * and vrml.eai.Node.  Mainly for use in constructing the 
      * Event*NodeWrapper's */
    VRMLNodeFactory theNodeFactory;

    /** Basic constructor.  
      * @param anEventQueue The queue to post events to.
      */
    SimpleEAIFieldFactory(ExternalEventQueue anEventQueue) {
        theEventQueue=anEventQueue;
    }

    /** Produce an eventIn.
     *  @param vrmlNode The originating node
     *  @param eventName The eventIn name
     */
    public EventIn getEventIn(
        VRMLNodeType vrmlNode, String eventName
    ) throws vrml.eai.field.InvalidEventInException, 
    vrml.eai.InvalidNodeException {
        if (eventName==null)
            throw new IllegalArgumentException(
                "Null string not acceptable for eventIn name"
            );
        if (vrmlNode==null)
            throw new vrml.eai.InvalidNodeException();
        int fieldID=vrmlNode.getFieldIndex(eventName);
        if (fieldID==-1)
            throw new vrml.eai.field.InvalidEventInException(
                "EventIn "+eventName+" not found"
            );
        else {
            VRMLFieldDeclaration decl=vrmlNode.getFieldDeclaration(fieldID);
            /* Option here for storing references */
            EventIn result=null;
            if ( 
                (decl.getAccessType()==FieldConstants.EVENTIN) || 
                (decl.getAccessType()==FieldConstants.EXPOSEDFIELD)
            ) {
                switch (decl.getFieldType()) {
                    case FieldConstants.MFCOLOR:
                        result=new EventInMFColorWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFFLOAT:
                        result=new EventInMFFloatWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFINT32:
                        result=new EventInMFInt32Wrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFNODE:
                        result=new EventInMFNodeWrapper(
                            vrmlNode, fieldID, theNodeFactory, theEventQueue
                        );
                        break;
                    case FieldConstants.MFROTATION:
                        result=new EventInMFRotationWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFSTRING:
                        result=new EventInMFStringWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFTIME:
                        result=new EventInMFTimeWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFVEC2F:
                        result=new EventInMFVec2fWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.MFVEC3F:
                        result=new EventInMFVec3fWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFBOOL:
                        result=new EventInSFBoolWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFCOLOR:
                        result=new EventInSFColorWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFFLOAT:
                        result=new EventInSFFloatWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFIMAGE:
                        result=new EventInSFImageWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFINT32:
                        result=new EventInSFInt32Wrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFNODE:
                        result=new EventInSFNodeWrapper(
                            vrmlNode, fieldID, theNodeFactory, theEventQueue
                        );
                        break;
                    case FieldConstants.SFROTATION:
                        result=new EventInSFRotationWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFSTRING:
                        result=new EventInSFStringWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFTIME:
                        result=new EventInSFTimeWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFVEC2F:
                        result=new EventInSFVec2fWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    case FieldConstants.SFVEC3F:
                        result=new EventInSFVec3fWrapper(
                            vrmlNode, fieldID, theEventQueue
                        );
                        break;
                    default:
                        throw new vrml.eai.field.InvalidEventInException(
                            "Unknown event in type for "+eventName+
                            ".  Was "+decl.getFieldType()+"."
                        );
                }
                /* Option to store result here */
                return result;
            } else 
                throw new vrml.eai.field.InvalidEventInException(
                    "Event type incorrect for "+eventName+"."
                );
        }
    }

    /** Produce an asynchronous eventOut.
     *  These eventOut's respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param eventName The eventIn name
     */
    public EventOut getEventOut(VRMLNodeType vrmlNode, String eventName) 
    throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException {
        if (eventName==null)
            throw new IllegalArgumentException(
                "Null string is not an acceptable eventOut name"
            );
        if (vrmlNode==null)
            throw new vrml.eai.InvalidNodeException();
        int fieldID=vrmlNode.getFieldIndex(eventName);
        if (fieldID==-1)
            throw new vrml.eai.field.InvalidEventOutException(
                "EventOut "+eventName+" not found"
            );
        else {
            return getEventOut(vrmlNode,fieldID, eventName);
        }
    }

    /** Produce an asynchronous eventOut.
     *  These eventOut's respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
    */
    public EventOut getEventOut(
        VRMLNodeType vrmlNode, int fieldID, String eventName
    ) throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException {
        VRMLFieldDeclaration decl=vrmlNode.getFieldDeclaration(fieldID);
        /* Option here for storing references */
        EventOut result=null;
        if ( 
            (decl.getAccessType()==FieldConstants.EVENTOUT) || 
            (decl.getAccessType()==FieldConstants.EXPOSEDFIELD)
        ) {
            switch (decl.getFieldType()) {
                case FieldConstants.MFCOLOR:
                    result=new EventOutMFColorWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFFLOAT:
                    result=new EventOutMFFloatWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFINT32:
                    result=new EventOutMFInt32Wrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFNODE:
                    result=new EventOutMFNodeWrapper(
                        vrmlNode,fieldID,theNodeFactory,
                        theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFROTATION:
                    result=new EventOutMFRotationWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFSTRING:
                    result=new EventOutMFStringWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFTIME:
                    result=new EventOutMFTimeWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFVEC2F:
                    result=new EventOutMFVec2fWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.MFVEC3F:
                    result=new EventOutMFVec3fWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFBOOL:
                    result=new EventOutSFBoolWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFCOLOR:
                    result=new EventOutSFColorWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFFLOAT:
                    result=new EventOutSFFloatWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFIMAGE:
                    result=new EventOutSFImageWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFINT32:
                    result=new EventOutSFInt32Wrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFNODE:
                    result=new EventOutSFNodeWrapper(
                        vrmlNode,fieldID, theNodeFactory,
                        theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFROTATION:
                    result=new EventOutSFRotationWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFSTRING:
                    result=new EventOutSFStringWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFTIME:
                    result=new EventOutSFTimeWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFVEC2F:
                    result=new EventOutSFVec2fWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                case FieldConstants.SFVEC3F:
                    result=new EventOutSFVec3fWrapper(
                        vrmlNode,fieldID,theEventAdapterFactory,false
                    );
                    break;
                default:
                    throw new vrml.eai.field.InvalidEventOutException(
                        "Unknown event out type for "+eventName+".  Was "
                        +decl.getFieldType()
                    );
            }
            /* Option to store reference here */
            return result;
        } else 
            throw new vrml.eai.field.InvalidEventOutException(
                "Event type is incorrect for "+eventName+"."
            );
    }

    /** Produce an stored eventOut.
     *  These eventOut's respond with the value of the field at the time
     *  of creation, rather than the current field value.
     *  Note that this method is mainly for use by the event
     *  propogation system, since it uses the underlying fieldID's rather
     *  than the String fieldNames.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
     */
    public EventOut getStoredEventOut(
        VRMLNodeType vrmlNode, int fieldID, String eventName
    ) throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException {
        if (vrmlNode==null)
            throw new vrml.eai.InvalidNodeException();
        if (fieldID==-1)
            throw new vrml.eai.field.InvalidEventOutException(
                "EventOut "+eventName+" not found"
            );
        else {
            VRMLFieldDeclaration decl=vrmlNode.getFieldDeclaration(fieldID);
            // Option here for storing references 
            EventOut result=null;
            if ( 
                (decl.getAccessType()==FieldConstants.EVENTOUT) || 
                (decl.getAccessType()==FieldConstants.EXPOSEDFIELD)
            ) {
                switch (decl.getFieldType()) {
                    case FieldConstants.MFCOLOR:
                        result=new EventOutMFColorWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFFLOAT:
                        result=new EventOutMFFloatWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFINT32:
                        result=new EventOutMFInt32Wrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFNODE:
                        result=new EventOutMFNodeWrapper(
                            vrmlNode,fieldID,theNodeFactory,
                            theEventAdapterFactory, true
                        );
                        break;
                    case FieldConstants.MFROTATION:
                        result=new EventOutMFRotationWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFSTRING:
                        result=new EventOutMFStringWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFTIME:
                        result=new EventOutMFTimeWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFVEC2F:
                        result=new EventOutMFVec2fWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.MFVEC3F:
                        result=new EventOutMFVec3fWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFBOOL:
                        result=new EventOutSFBoolWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFCOLOR:
                        result=new EventOutSFColorWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFFLOAT:
                        result=new EventOutSFFloatWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFIMAGE:
                        result=new EventOutSFImageWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFINT32:
                        result=new EventOutSFInt32Wrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFNODE:
                        result=new EventOutSFNodeWrapper(
                            vrmlNode,fieldID, theNodeFactory,
                            theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFROTATION:
                        result=new EventOutSFRotationWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFSTRING:
                        result=new EventOutSFStringWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFTIME:
                        result=new EventOutSFTimeWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true 
                        ); 
                        break;
                    case FieldConstants.SFVEC2F:
                        result=new EventOutSFVec2fWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    case FieldConstants.SFVEC3F:
                        result=new EventOutSFVec3fWrapper(
                            vrmlNode,fieldID,theEventAdapterFactory,true
                        );
                        break;
                    default:
                        throw new vrml.eai.field.InvalidEventOutException(
                            "Unknown event out type for "+eventName+".  Was "
                            +decl.getFieldType()
                        );
                }
                // Option to store reference here 
                return result;
            } else 
                throw new vrml.eai.field.InvalidEventOutException(
                    "Event type is incorrect for "+eventName+"."
                );
        }
    }

    /** Set the EAIEventAdapterFactory for new EventOut instances. */
    void setEAIEventAdapterFactory(EAIEventAdapterFactory aFactory) {
        theEventAdapterFactory=aFactory;
    }

    /** Change the NodeFactory for new Event*Node instances.
      * This is to avoid a chicken-and-the-egg problem with the VRMLNodeFactory
      * and EAIFieldFactory constructors where both constructors want
      * the other instance. */
    void setNodeFactory(VRMLNodeFactory aFactory) {
        theNodeFactory=aFactory;
    }

}
