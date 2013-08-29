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

package org.web3d.vrml.nodes.proto;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Convenience data holder to keep a node and field value together.
 * <p>
 * Principally used for IS mapping and is held as package only access.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class ProtoFieldInfo {

    /** Reference to the destination node */
    public final VRMLNodeType node;

    /** Index of the field in that node */
    public final int field;

    /**
     * Default constructor so that those that want to create an empty instance
     * can. Both fields are not set.
     */
    ProtoFieldInfo() {
        this(null, -1);
    }

    /**
     * Convenience constructor to create a class instance and assign the
     * values immediately in one action.
     *
     * @param n The node to use
     * @param f The field index
     */
    public ProtoFieldInfo(VRMLNodeType n, int f) {
        node = n;
        field = f;
    }

    /**
     * Return a String representation of this class.
     *
     * @return A string with details of this class
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("FieldInfo: node: ");
        buf.append(node);
        buf.append(" field index: ");
        buf.append(field);
        buf.append("\n Field details: ");
        buf.append(node.getFieldDeclaration(field));

        return buf.toString();
    }

    /**
     * Get the hash value for the class for use in data structures like
     * hash maps.
     *
     * @return A hash key uniquely identifying this class
     */
    public int hashCode() {
        return node.hashCode() + field;
    }

    /**
     * Check for equivalence between this class and another one.
     *
     * @param obj The class to check against
     * @return true If the two classes represent the same thing
     */
    public boolean equals(Object obj) {
        if(obj instanceof ProtoFieldInfo) {
            if(((ProtoFieldInfo)obj).node == node &&
               ((ProtoFieldInfo)obj).field == field) {
                return true;
            }
        }

        return false;
    }
}
