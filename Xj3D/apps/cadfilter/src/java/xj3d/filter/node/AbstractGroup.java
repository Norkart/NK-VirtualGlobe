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
import java.util.ArrayList;

// Local imports

/**
 * Base abstract impl wrapper for an X3D grouping nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class AbstractGroup extends AbstractEncodable {
    
    /** The root nodes */
    protected ArrayList<Encodable> children;
    
    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected AbstractGroup(String name, String defName) {
        super(name, defName);
        children = new ArrayList<Encodable>();
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler
     */
    public void encode() {
        
        if (handler != null) {
            for (int i = 0; i < children.size(); i++) {
                Encodable e = children.get(i);
                e.encode();
            }
        }
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Clear and set the children of this grouping node. 
     *
     * @param enc The Encodables to set as children of this grouping node.
     * A value of null just performs a clear.
     */
    public void setChildren(Encodable[] enc) {
        children.clear();
        if (enc != null) {
            for (int i = 0; i < enc.length; i++) {
                children.add(enc[i]);
            }
        }
    }
    
    /**
     * Return the children of this grouping node. 
     *
     * @return The Encodable children of this grouping node.
     */
    public Encodable[] getChildren() {
        return(children.toArray(new Encodable[children.size()]));
    }

    /**
     * Add children to this grouping node. 
     *
     * @param enc The Encodables to add to this grouping node.
     */
    public void addChildren(Encodable[] enc) {
        if (enc != null) {
            for (int i = 0; i < enc.length; i++) {
                children.add(enc[i]);
            }
        }
    }

    /**
     * Add a child to this grouping node. 
     *
     * @param enc An Encodable to add to this grouping node.
     */
    public void addChild(Encodable enc) {
        if (enc != null) {
            children.add(enc);
        }
    }
}
