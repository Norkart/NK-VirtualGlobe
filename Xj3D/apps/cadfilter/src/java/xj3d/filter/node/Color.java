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

import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D Color node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class Color extends AbstractEncodable {
    
    /** Field value */
    public float[] color;
    
    /** Number of coordinate values in the color array */
    public int num_color;
    
    /**
     * Constructor
     */
    public Color() {
        super("Color");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Color(String defName) {
        super("Color", defName);
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler.
     */
    public void encode() {
        
        if (handler != null) {
            handler.startNode(name, defName);
            
            if (color != null) {
                handler.startField("color");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(color, num_color*3);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(FieldValueHandler.toString(color, num_color*3));
                    break;
                }
            }
            handler.endNode();
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public void setValue(String name, Object value) {
        
        if (name.equals("color")) {
            if (value instanceof String) {
                color = fieldReader.MFColor((String)value);
                num_color = color.length / 3;
            } else if (value instanceof String[]) {
                color = fieldReader.MFColor((String[])value);
                num_color = color.length / 3;
            } else if (value instanceof float[]) {
                color = (float[])value;
                num_color = color.length / 3;
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
        
        if (name.equals("color")) {
            if (value instanceof float[]) {
                color = (float[])value;
                num_color = len / 3;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        Color c = new Color();
        copy(c);
        c.num_color = this.num_color;
        c.color = new float[this.num_color*3];
        System.arraycopy(this.color, 0, c.color, 0, this.num_color*3);
        return(c);
    }
}
