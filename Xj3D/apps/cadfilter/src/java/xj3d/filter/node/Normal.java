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
 * Wrapper for the X3D Normal node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class Normal extends AbstractEncodable {
    
    /** Field value */
    public float[] vector;
    
    /** Number of coordinate values in the vector array */
    public int num_vector;
    
    /**
     * Constructor
     */
    public Normal() {
        super("Normal");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Normal(String defName) {
        super("Normal", defName);
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
            
            if (vector != null) {
                handler.startField("vector");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(vector, num_vector*3);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(FieldValueHandler.toString(vector, num_vector*3));
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
        
        if (name.equals("vector")) {
            if (value instanceof String) {
                vector = fieldReader.MFVec3f((String)value);
                num_vector = vector.length / 3;
            } else if (value instanceof String[]) {
                vector = fieldReader.MFVec3f((String[])value);
                num_vector = vector.length / 3;
            } else if (value instanceof float[]) {
                vector = (float[])value;
                num_vector = vector.length / 3;
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
        
        if (name.equals("vector")) {
            if (value instanceof float[]) {
                vector = (float[])value;
                num_vector = len / 3;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        Normal n = new Normal();
        copy(n);
        n.num_vector = this.num_vector;
        n.vector = new float[this.num_vector*3];
        System.arraycopy(this.vector, 0, n.vector, 0, this.num_vector*3);
        return(n);
    }
}
