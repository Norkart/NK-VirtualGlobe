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

/**
 * FieldToVRMLNodeTypeMapper is used to map field IDs to
 * the appropriate VRMLNodeTypeAdapter instance that the field belongs to.
 * The VRMLNodeTypeAdapter contains the reference to the VRMLNodeType
 * instance.
 */
public class FieldToVRMLNodeTypeMapper {

    /** Mapping from network field IDs to
     *  VRMLNodeTypeAdapter instances */
    IntHashMap fieldIDToAdapterTable;
    
    /**
     * Default Constructor
     */
    public FieldToVRMLNodeTypeMapper() {
        fieldIDToAdapterTable=new IntHashMap();
    }
    
    /**
     * Determine the network field ID for a given
     * field on the VRMLNodeTypeAdapter.  Ends up asking
     * the VRMLNodeTypeAdapter to do the work but stores
     * the information for the reverse retrieval in findNode.
     * @param adapter The adapter containing the field.
     * @param xj3dFieldID The field index on the local node.
     * @return The network field ID.
     */
    int registerField(VRMLNodeTypeAdapter adapter, int xj3dFieldID) {
        int fieldID=adapter.registerField(xj3dFieldID);
        fieldIDToAdapterTable.put(fieldID,adapter);
        return fieldID;
    }
    
    /** Determine the network field ID for a given eventIn by name.
     *  Ends up asking the VRMLNodeTypeAdapter to do the work but
     * stores the information for the reverse retieval in findNode.
     * @param adapter The adapter containing the field
     * @param fieldName The name of the field
     * @return The network field ID
     */
    int registerEventIn(VRMLNodeTypeAdapter adapter, String fieldName) {
        int fieldID=adapter.registerEventIn(fieldName);
        fieldIDToAdapterTable.put(fieldID,adapter);
        return fieldID;
    }
    
    /** Determine the network field ID for a given eventOut by name.
     *  Ends up asking the VRMLNodeTypeAdapter to do the work but
     * stores the information for the reverse retieval in findNode.
     * @param adapter The adapter containing the field
     * @param fieldName The name of the field
     * @return The network field ID
     */
    int registerEventOut(VRMLNodeTypeAdapter adapter, String fieldName) {
        int fieldID=adapter.registerEventOut(fieldName);
        fieldIDToAdapterTable.put(fieldID,adapter);
        return fieldID;
    }
    
    /**
     * Given a fieldID locate the adapter it resides on.
     * @param fieldID The network field ID for the field
     * @return The adapter instance for the real node.
     */
    VRMLNodeTypeAdapter findNode(int fieldID) {
        return (VRMLNodeTypeAdapter) fieldIDToAdapterTable.get(fieldID);
    }
    
}
