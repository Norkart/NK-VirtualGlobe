package org.web3d.vrml.scripting.external.sai;

/*******************************************************************************
 * Copyright North Dakota State University, 2001 Written By Bradley Vender
 * (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1 Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ******************************************************************************/

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.x3d.sai.*;

/**
 * SimpleSAIFieldFactory is an implementation of EAIFieldFactory which performs
 * no mapping to reduce duplicate object creation.
 * <P>
 * setNodeFactory must be used after the constructor to properly initialize
 * this class.
 *
 * @author Brad Vender
 */
class SimpleSAIFieldFactory implements SAIFieldFactory {

    /** The EAIEventAdapterFactory for the eventOut instances. */
    SAIEventAdapterFactory theEventAdapterFactory;

    /** The queue for posting events. */
    ExternalEventQueue theEventQueue;

    /**
     * The SAINodeFactory instance used in mapping between VRMLNodeType and
     * SAINode. Mainly for use in constructing the Event*NodeWrapper's
     */
    SAINodeFactory theNodeFactory;

    /**
     * Basic constructor.
     *
     * @param anEventQueue
     *            The event queue to which events are sent.
     */
    SimpleSAIFieldFactory(ExternalEventQueue anEventQueue) {
        theEventQueue = anEventQueue;
    }

    /**
     * Produce an asynchronous field. These fields respond with the current
     * field value when queried, as opposed to the field value when created.
     *
     * @param vrmlNode
     *            The originating node
     * @param eventName
     *            The eventIn name
     */
    public X3DField getField(VRMLNodeType vrmlNode, String eventName)
            throws InvalidFieldException, InvalidNodeException {
        if (eventName == null)
                throw new IllegalArgumentException(
                        "Null string is not an acceptable eventOut name");
        if (vrmlNode == null) throw new InvalidNodeException();
        int fieldID = vrmlNode.getFieldIndex(eventName);
        if (fieldID == -1)
            throw new InvalidFieldException("Field " + eventName + " not found");
        else {
            return getField(vrmlNode, fieldID, eventName);
        }
    }

    /**
     * Produce an asynchronous field. These fields respond with the current
     * field value when queried, as opposed to the field value when created.
     *
     * @param vrmlNode
     *            The originating node
     * @param fieldID
     *            The field ID
     * @param eventName
     *            The field name (for error reporting)
     */
    public X3DField getField(VRMLNodeType vrmlNode, int fieldID,
            String eventName) throws InvalidFieldException,
            vrml.eai.InvalidNodeException {
        VRMLFieldDeclaration decl = vrmlNode.getFieldDeclaration(fieldID);
        /* Option here for storing references */
        X3DField result = null;
        switch (decl.getFieldType()) {
        case FieldConstants.MFBOOL:
            result = new MFBoolWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFCOLOR:
            result = new MFColorWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFCOLORRGBA:
            result = new MFColorRGBAWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFDOUBLE:
            result = new MFDoubleWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFFLOAT:
            result = new MFFloatWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFIMAGE:
            result = new MFImageWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFINT32:
            result = new MFInt32Wrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFNODE:
            result = new MFNodeWrapper(vrmlNode, fieldID, theEventQueue,
                    theNodeFactory, theEventAdapterFactory);
            break;
        case FieldConstants.MFROTATION:
            result = new MFRotationWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFSTRING:
            result = new MFStringWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFTIME:
            result = new MFTimeWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFVEC2D:
            result = new MFVec2dWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFVEC2F:
            result = new MFVec2fWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFVEC3D:
            result = new MFVec3dWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.MFVEC3F:
            result = new MFVec3fWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFBOOL:
            result = new SFBoolWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFCOLOR:
            result = new SFColorWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFCOLORRGBA:
            result = new SFColorRGBAWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFDOUBLE:
            result = new SFDoubleWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFFLOAT:
            result = new SFFloatWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFIMAGE:
            result = new SFImageWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFINT32:
            result = new SFInt32Wrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFNODE:
            result = new SFNodeWrapper(vrmlNode, fieldID, theEventQueue,
                    theNodeFactory, theEventAdapterFactory);
            break;
        case FieldConstants.SFROTATION:
            result = new SFRotationWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFSTRING:
            result = new SFStringWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFTIME:
            result = new SFTimeWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFVEC2D:
            result = new SFVec2dWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFVEC2F:
            result = new SFVec2fWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFVEC3D:
            result = new SFVec3dWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        case FieldConstants.SFVEC3F:
            result = new SFVec3fWrapper(vrmlNode, fieldID, theEventQueue,
                    theEventAdapterFactory);
            break;
        default:
            throw new InvalidFieldException("Unknown event out type for "
                    + eventName + ".  Was " + decl.getFieldType());
        }
        /* Option to store reference here */
        return result;
    }

    /**
     * Produce an stored field. These fields respond with the value of the
     * field at the time of creation, rather than the current field value. Note
     * that this method is mainly for use by the event propogation system,
     * since it uses the underlying fieldID's rather than the String
     * fieldNames.
     *
     * @param vrmlNode
     *            The originating node
     * @param fieldID
     *            The field ID
     * @param eventName
     *            The field name (for error reporting)
     * @param isInput
     *            Is this an input or output buffer
     */
    public X3DField getStoredField(VRMLNodeType vrmlNode, int fieldID,
            String eventName, boolean isInput) throws InvalidFieldException,
            InvalidNodeException {
        if (vrmlNode == null) throw new InvalidNodeException();
        if (fieldID == -1)
            throw new InvalidFieldException("EventOut " + eventName
                    + " not found");
        else {
            VRMLFieldDeclaration decl = vrmlNode.getFieldDeclaration(fieldID);
            // Option here for storing references
            X3DField result = null;
            switch (decl.getFieldType()) {
            case FieldConstants.MFBOOL:
                result = new MFBoolWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFCOLOR:
                result = new MFColorWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFCOLORRGBA:
                result = new MFColorRGBAWrapper(vrmlNode, fieldID,
                        theEventQueue, theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFDOUBLE:
                result = new MFDoubleWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFFLOAT:
                result = new MFFloatWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFIMAGE:
                result = new MFImageWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFINT32:
                result = new MFInt32Wrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFNODE:
                result = new MFNodeWrapper(vrmlNode, fieldID, theEventQueue,
                        theNodeFactory, theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFROTATION:
                result = new MFRotationWrapper(vrmlNode, fieldID,
                        theEventQueue, theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFSTRING:
                result = new MFStringWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFTIME:
                result = new MFTimeWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFVEC2D:
                result = new MFVec2dWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFVEC2F:
                result = new MFVec2fWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFVEC3D:
                result = new MFVec3dWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.MFVEC3F:
                result = new MFVec3fWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFBOOL:
                result = new SFBoolWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFCOLOR:
                result = new SFColorWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFFLOAT:
                result = new SFFloatWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFIMAGE:
                result = new SFImageWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFINT32:
                result = new SFInt32Wrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFNODE:
                result = new SFNodeWrapper(vrmlNode, fieldID, theEventQueue,
                        theNodeFactory, theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFROTATION:
                result = new SFRotationWrapper(vrmlNode, fieldID,
                        theEventQueue, theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFSTRING:
                result = new SFStringWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFTIME:
                result = new SFTimeWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFVEC2F:
                result = new SFVec2fWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFVEC3F:
                result = new SFVec3fWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            case FieldConstants.SFVEC3D:
                result = new SFVec3dWrapper(vrmlNode, fieldID, theEventQueue,
                        theEventAdapterFactory, isInput);
                break;
            default:
                throw new InvalidFieldException("Unknown event out type for "
                        + eventName + ".  Was " + decl.getFieldType());
            }
            //Option to store reference here
            return result;
        }
    }

    /** Set the SAIEventAdapterFactory for new EventOut instances.
     * @param aFactory The new event adapter factory
     * */
    void setSAIEventAdapterFactory(SAIEventAdapterFactory aFactory) {
        theEventAdapterFactory = aFactory;
    }

    /**
     * Change the NodeFactory for new Event This is to avoid a
     * chicken-and-the-egg problem with the VRMLNodeFactory and EAIFieldFactory
     * constructors where both constructors want the other instance.
     * @param aFactory The new node factory
     */
    void setNodeFactory(SAINodeFactory aFactory) {
        theNodeFactory = aFactory;
    }
}
