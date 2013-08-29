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
 * Wrapper for the X3D TextureCoordinate node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class TextureCoordinate extends AbstractEncodable {
    
    /** Field value */
    public float[] point;
    
    /** Number of coordinate values in the point array */
    public int num_point;
    
    /**
     * Constructor
     */
    public TextureCoordinate() {
        super("TextureCoordinate");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TextureCoordinate(String defName) {
        super("TextureCoordinate", defName);
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
            
            if (point != null) {
                handler.startField("point");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(point, num_point*2);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(FieldValueHandler.toString(point, num_point*2));
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
        
        if (name.equals("point")) {
            if (value instanceof String) {
                point = fieldReader.MFVec2f((String)value);
                num_point = point.length / 2;
            } else if (value instanceof String[]) {
                point = fieldReader.MFVec2f((String[])value);
                num_point = point.length / 2;
            } else if (value instanceof float[]) {
                point = (float[])value;
                num_point = point.length / 2;
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
        
        if (name.equals("point")) {
            if (value instanceof float[]) {
                point = (float[])value;
                num_point = len / 2;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        TextureCoordinate tc = new TextureCoordinate();
        copy(tc);
        tc.num_point = this.num_point;
        tc.point = new float[this.num_point*2];
        System.arraycopy(this.point, 0, tc.point, 0, this.num_point*2);
        return(tc);
    }
}
