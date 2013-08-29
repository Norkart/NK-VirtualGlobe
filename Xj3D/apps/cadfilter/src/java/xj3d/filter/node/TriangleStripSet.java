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
 * Wrapper for an X3D TriangleStripSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class TriangleStripSet extends ComposedGeometry {
    
    /** Field value */
    public int[] stripCount;
    
    /** Number of strips in the array */
    public int num_strip;
    
    /**
     * Constructor
     */
    public TriangleStripSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TriangleStripSet(String defName) {
        super("TriangleStripSet", defName);
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler.
     */
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(name, defName);
                
                super.encode();
                
                if ( stripCount != null ) {
                    handler.startField("stripCount");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(stripCount, num_strip);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(stripCount, num_strip));
                        break;
                    }
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
        
        super.setValue(name, value);
        
        if (name.equals("stripCount")) {
            if (value instanceof String) {
                stripCount = fieldReader.MFInt32((String)value);
                num_strip = stripCount.length ;
            } else if (value instanceof String[]) {
                stripCount = fieldReader.MFInt32((String[])value);
                num_strip = stripCount.length;
            } else if (value instanceof int[]) {
                stripCount = (int[])value;
                num_strip = stripCount.length;
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
        
        super.setValue(name, value, len);
        
        if (name.equals("stripCount")) {
            if (value instanceof int[]) {
                stripCount = (int[])value;
                num_strip = len;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        TriangleStripSet tss = new TriangleStripSet();
        copy(tss);
        tss.num_strip = this.num_strip;
        tss.stripCount = new int[this.num_strip];
        System.arraycopy(this.stripCount, 0, tss.stripCount, 0, this.num_strip);
        return(tss);
    }
}
