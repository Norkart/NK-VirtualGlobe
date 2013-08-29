/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// Standard imports
import java.lang.ref.ReferenceQueue;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Internal listener for allowing a SAI field wrapper class to callback to the
 * containing script wrapper to modify the field access notification.
 * <p>
 *
 * This interface is to solve the problem of users directly adding field
 * listeners to external nodes other than the script's own fields. In this case
 * the nodes are generating their field event to the listener which is then
 * being directly translated into the X3DFieldEventListener callback. Users are
 * allowed to directly interact with other fields at this point, not just the
 * field that generated this event. To allow access to these other fields,
 * the field itself (represented by BaseField) has to tell "the system" that
 * access is currently valid. This is the callback that goes from that field
 * wrapper directly to the ScriptWrapper so that it can iterate through
 * everything and allow direct access.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface FieldAccessListener {

    /**
     * Notify that the child field now requires access to be valid or not valid
     * as the case may be from the various flags.
     *
     * @param state true if access should be currently valid.
     */
    public void childRequiresAccessStateChange(boolean state);

}
