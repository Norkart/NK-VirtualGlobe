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

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * FieldChangeTransmitter allows the VRMLNodeType listeners to send their data
 * to the client.
 */
interface FieldChangeTransmitter {
    /**
     * Transmit a fieldChanged notification.
     * @param fieldID Network field ID for this field
     * @param fieldType Field type using vrml.eai.field.FieldTypes
     * @param node The node to get field value from
     * @param fieldIndex The field's index on the node
     */
    void transmitFieldChanged(int fieldID, VRMLNodeType node, int fieldIndex);
}