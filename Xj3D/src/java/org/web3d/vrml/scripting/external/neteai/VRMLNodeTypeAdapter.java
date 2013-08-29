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

import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

import vrml.eai.field.BaseField;
import vrml.eai.field.InvalidFieldException;

/**
 * VRMLNodeTypeAdapter is used on the server side
 * to map network field IDs to the field indices for
 * a particular VRMLNodeType instance.
 */
public class VRMLNodeTypeAdapter implements VRMLNodeListener {

    /** The party responsible for sending the change notification */
    FieldChangeTransmitter changeTransmitter;
    
    /** The actual node in the renderer */
    VRMLNodeType underlyingNode;
    
    /** ID generator to ensure that field ID's are not duplicated
     *  for this session. */
    IDGenerator globalIDGenerator;
    
    /** Mapping from network field ID's to the field index on the node */
    IntHashMap globalFieldIDToLocalFieldIDTable;
    
    /** Mapping from the field index on the node to the network field ID */
    IntHashMap localFieldIDToGlobalFieldIDTable;
    
    /** If a field has listeners, then it will have
     *  a non-null entry here.
     */
    IntHashMap listenerTable;
    
    /** Basic constructor.  Automatically registers as needed for
     *  field changed callbacks.
     * @param node The node to wrap.
     * @param generator ID generator used for field IDs.
     */
    VRMLNodeTypeAdapter(VRMLNodeType node, IDGenerator generator,
            FieldChangeTransmitter transmitter) {
        underlyingNode=node;
        globalIDGenerator=generator;
        globalFieldIDToLocalFieldIDTable=new IntHashMap();
        localFieldIDToGlobalFieldIDTable=new IntHashMap();
        listenerTable=new IntHashMap();
        changeTransmitter=transmitter;
        // Can't think of an easy way of doing this later, so
        // just register the node listener now and assume that
        // the fieldChanged broadcast is cheap.
        underlyingNode.addNodeListener(this);
    }

    /** Turn on field changed broadcasts for a field
     * @param globalFieldID The field to activate.
     */
    public void activateFieldListener(int globalFieldID) {
        Integer field=(Integer) globalFieldIDToLocalFieldIDTable.get(globalFieldID);
        if (field==null)
            throw new InvalidFieldException();
        else {
            int fieldID=field.intValue();
            listenerTable.put(fieldID,this);
        }
            
    }
    
    /** Deactivates listener by removing its entry from the listener table
     * @param globalFieldID The global ID to remove from broadcasts.
     */
    public void deactivateFieldListener(int globalFieldID) {
        Integer field=(Integer) globalFieldIDToLocalFieldIDTable.get(globalFieldID);
        if (field==null)
            throw new InvalidFieldException();
        else {
            int fieldID=field.intValue();
            listenerTable.remove(fieldID);
        }
    }
    
    /** Returns field type mapped to vrml.eai.field.FieldConstants
     * @param globalFieldID
     * @return
     */
    public int getFieldType(int globalFieldID) {
        Integer field=(Integer) globalFieldIDToLocalFieldIDTable.get(globalFieldID);
        if (field==null)
            throw new InvalidFieldException();
        else {
            int fieldID=field.intValue();
            switch (underlyingNode.getFieldDeclaration(fieldID).getFieldType()) {
            	case FieldConstants.MFCOLOR:
            	    return BaseField.MFColor;
            	case FieldConstants.MFFLOAT:
            	    return BaseField.MFFloat;
            	case FieldConstants.MFINT32:
            		return BaseField.MFInt32;
            	case FieldConstants.MFNODE:
            		return BaseField.MFNode;
            	case FieldConstants.MFROTATION:
            		return BaseField.MFRotation;
            	case FieldConstants.MFSTRING:
            		return BaseField.MFString;
            	case FieldConstants.MFTIME:
            		return BaseField.MFTime;
            	case FieldConstants.MFVEC2F:
            		return BaseField.MFVec2f;
            	case FieldConstants.MFVEC3F:
            		return BaseField.MFVec3f;
            	case FieldConstants.SFBOOL:
            		return BaseField.SFBool;
            	case FieldConstants.SFCOLOR:
            		return BaseField.SFColor;
            	case FieldConstants.SFFLOAT:
            		return BaseField.SFFloat;
            	case FieldConstants.SFIMAGE:
            		return BaseField.SFImage;
            	case FieldConstants.SFINT32:
            		return BaseField.SFInt32;
            	case FieldConstants.SFNODE:
            		return BaseField.SFNode;
            	case FieldConstants.SFROTATION:
            		return BaseField.SFRotation;
            	case FieldConstants.SFSTRING:
            		return BaseField.SFString;
            	case FieldConstants.SFTIME:
            		return BaseField.SFTime;
            	case FieldConstants.SFVEC2F:
            		return BaseField.SFVec2f;
            	case FieldConstants.SFVEC3F:
            		return BaseField.SFVec3f;
            	default:
            	    throw new InvalidFieldException("Field type not supported");
            }
        }
    }
    
    /** Determine the local field ID for a field given its 
     * network field ID.
     * @param networkFieldID The network field ID of the field
     * @return The local field ID (field index)
     */
    int getLocalFieldID(int networkFieldID) {
        Integer i=(Integer) globalFieldIDToLocalFieldIDTable.get(networkFieldID);
        if (i==null)
            throw new InvalidFieldException();
        else
            return i.intValue();
    }
    
    /** Determines the network field ID for a field on this node.
     *  Multiple calls for the same field will return the same value
     *  if the field has not been released.
     * @param xj3dFieldID The field specified by field index on this node.
     * @return The field ID assigned by the ID generator
     */
    public int registerField(int xj3dFieldID) {
        int fieldID;
        Integer i=(Integer) localFieldIDToGlobalFieldIDTable.get(xj3dFieldID);
        if (i==null) {
            fieldID=globalIDGenerator.generateID();
            globalFieldIDToLocalFieldIDTable.put(fieldID,new Integer(xj3dFieldID));
            localFieldIDToGlobalFieldIDTable.put(xj3dFieldID,new Integer(fieldID));
        } else
            fieldID=i.intValue();
        return fieldID;
    }

    /** Determines the network field ID for an eventIn on this node.
     *  Multiple calls for the same field will return the same value
     *  if the field has not been released.  Throws an exception if the
     *  field is not an eventIn.
     * @param fieldName The name of the eventIn or exposedField.
     * @return The field ID assigned by the ID generator
     */
    public int registerEventIn(String fieldName) {
        int fieldID=underlyingNode.getFieldIndex(fieldName);
        if (fieldID==-1)
            throw new InvalidFieldException("Field not found");
        int accessType=underlyingNode.getFieldDeclaration(fieldID).getAccessType();
        if (accessType==FieldConstants.EVENTIN || accessType==FieldConstants.EXPOSEDFIELD)
            return registerField(fieldID);
        else
            throw new InvalidFieldException("Incorrect access type for field");
    }

    /** Determines the network field ID for an eventOut on this node.
     *  Multiple calls for the same field will return the same value
     *  if the field has not been released.  Throws an exception if the
     *  field is not an eventOut or exposedField.
     * @param fieldName The name of the eventIn
     * @return The field ID assigned by the ID generator
     */
    public int registerEventOut(String fieldName) {
        int fieldID=underlyingNode.getFieldIndex(fieldName);
        if (fieldID==-1)
            throw new InvalidFieldException("Field not found");
        int accessType=underlyingNode.getFieldDeclaration(fieldID).getAccessType();
        if (accessType==FieldConstants.EVENTOUT || accessType==FieldConstants.EXPOSEDFIELD)
            return registerField(fieldID);
        else
            throw new InvalidFieldException("Incorrect access type for field");
    }

    
    /** Dispose of a field ID.  Removed from listener table as well.
     * @param globalFieldID The field ID to dispose of
     */
    public void releaseField(int globalFieldID) {
        Integer localField=(Integer) globalFieldIDToLocalFieldIDTable.get(globalFieldID);
        if (localField==null)
            throw new InvalidFieldException();
        int localFieldID=localField.intValue();
        localFieldIDToGlobalFieldIDTable.remove(localFieldID);
        listenerTable.remove(localFieldID);
        globalIDGenerator.releaseKey(globalFieldID);
    }

    /** * @see org.web3d.vrml.nodes.VRMLNodeListener#fieldChanged(int)  */
    public void fieldChanged(int index) {
        if (listenerTable.get(index)==null)
            return;
        Integer field=(Integer) localFieldIDToGlobalFieldIDTable.get(index);
        if (field==null)
            // No listeners
            return;
        else {
            int globalFieldID=field.intValue();
            changeTransmitter.transmitFieldChanged(globalFieldID,underlyingNode,index);
        }
    }
    
}
