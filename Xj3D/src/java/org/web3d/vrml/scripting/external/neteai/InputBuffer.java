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

import java.io.DataOutputStream;

/**
 * Abstraction of part of the EventIn*Wrapper buffer logic
 * to prevent having twenty copies of the setFieldValue method.
 * Instead the field transmitter uses this interface as a callback
 * during the setFieldValue call.
 */
public interface InputBuffer {

    /** Write out the appropriate network representation
     *  of the EventIn*Wrapper's field value.
     * @param stream The stream to write data to.
     */
    void transmitFieldValue(DataOutputStream stream);
    
}
