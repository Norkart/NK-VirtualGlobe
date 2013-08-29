/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2007
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
import java.util.ArrayList;

// Local imports

/**
 * Wrapper for an X3D Scene.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Scene extends AbstractEncodable {
    
    /** The root nodes */
    public ArrayList<Encodable> nodeList;
    
    /**
     * Constructor
     */
    public Scene(String name) {
        super("Scene");
        nodeList = new ArrayList<Encodable>();
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler
     */
    public void encode() {
        for (int i = 0; i < nodeList.size(); i++) {
            Encodable e = nodeList.get(i);
            e.encode();
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public void setValue(String name, Object value) {
        if (value instanceof Encodable) {
            nodeList.add((Encodable)value);
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
        if (value instanceof Encodable[]) {
            Encodable[] e = (Encodable[])value;
            for (int i = 0; i < e.length; i++) {
                nodeList.add(e[i]);
            }
        }
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Return the root nodes of the Scene
     *
     * @return the root nodes of the Scene
     */
    public Encodable[] getRootNodes() {
        return(nodeList.toArray(new Encodable[nodeList.size()]));
    }
}
