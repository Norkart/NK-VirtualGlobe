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
 * Wrapper for the X3D Shape node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Shape extends AbstractEncodable {
    
    /** The geometry node */
    private Geometry geometry;
    
    /** The appearance node */
    private Appearance appearance;
    
    /**
     * Constructor
     */
    public Shape() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Shape(String defName) {
        super("Shape", defName);
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
                
                if (appearance != null) {
                    handler.startField("appearance");
                    appearance.encode();
                    handler.endField();
                }
                
                if (geometry != null) {
                    handler.startField("geometry");
                    geometry.encode();
                    handler.endField();
                }
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
        if (name.equals("appearance")) {
            if (value instanceof Appearance) {
                appearance = (Appearance)value;
            }
        } else if (name.equals("geometry")) {
            if (value instanceof Geometry) {
                geometry = (Geometry)value;
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
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        Shape s = new Shape();
        copy(s);
        if (this.geometry != null) {
            s.geometry = (Geometry)this.geometry.clone();
        }
        if (this.appearance != null) {
            s.appearance = (Appearance)this.appearance.clone();
        }
        return(s);
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Set the Geometry node wrapper
     *
     * @param geometry The Geometry node wrapper
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
    /**
     * Get the Geometry node wrapper
     *
     * @return The Geometry node wrapper
     */
    public Geometry getGeometry() {
        return(geometry);
    }
    
    /**
     * Set the Appearance node wrapper
     *
     * @param appearance The Appearance node wrapper
     */
    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }
    
    /**
     * Get the Appearance node wrapper
     *
     * @return The Appearance node wrapper
     */
    public Appearance getAppearance() {
        return(appearance);
    }
}
