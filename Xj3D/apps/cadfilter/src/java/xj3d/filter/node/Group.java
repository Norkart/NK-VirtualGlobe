/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports

// Local imports

/**
 * Wrapper for an X3D Group node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Group extends AbstractGroup {
    
    /**
     * Constructor
     */
    public Group() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Group(String defName) {
        super("Group", defName);
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler
     */
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(name, defName);
                super.encode();
                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public void setValue(String name, Object value) {
        if (name.equals("children")) {
            if (value instanceof Encodable) {
                children.add((Encodable)value);
            }
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    public void setValue(String name, Object value, int len) {
        if (name.equals("children")) {
            if (value instanceof Encodable[]) {
                Encodable[] e = (Encodable[])value;
                for (int i = 0; i < len; i++) {
                    children.add(e[i]);
                }
            }
        }
    }
}
