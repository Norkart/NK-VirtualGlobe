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
import org.web3d.vrml.sav.ContentHandler;

/**
 * Wrapper for the X3D Appearance node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Appearance extends AbstractEncodable {
    
    /** The Material node */
    private Material material;
    
    /**
     * Constructor
     */
    public Appearance() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Appearance(String defName) {
        super("Appearance", defName);
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
                
                if (material != null) {
                    handler.startField("material");
                    material.encode();
                    handler.endField();
                }
                
                //if ( texture != null ) {
                //}
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
        if (name.equals("material")) {
            if (value instanceof Material) {
                material = (Material)value;
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
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Set the Material node wrapper
     *
     * @param material The Material node wrapper
     */
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    /**
     * Get the Material node wrapper
     *
     * @return The Material node wrapper
     */
    public Material getMaterial() {
        return(material);
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        Appearance a = new Appearance();
        copy(a);
        if (this.material != null) {
            a.material = (Material)this.material.clone();
        }
        return(a);
    }
}
