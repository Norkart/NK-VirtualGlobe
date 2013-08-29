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
 * Wrapper for an X3D TriangleFanSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class TriangleFanSet extends ComposedGeometry {
    
    /** Field value */
    public int[] fanCount;
    
    /** Number of fans in the array */
    public int num_fan;
    
    /**
     * Constructor
     */
    public TriangleFanSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TriangleFanSet(String defName) {
        super("TriangleFanSet", defName);
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
                
                if ( fanCount != null ) {
                    handler.startField("fanCount");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(fanCount, num_fan);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(fanCount, num_fan));
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
        
        if (name.equals("fanCount")) {
            if (value instanceof String) {
                fanCount = fieldReader.MFInt32((String)value);
                num_fan = fanCount.length ;
            } else if (value instanceof String[]) {
                fanCount = fieldReader.MFInt32((String[])value);
                num_fan = fanCount.length;
            } else if (value instanceof int[]) {
                fanCount = (int[])value;
                num_fan = fanCount.length;
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
        
        if (name.equals("fanCount")) {
            if (value instanceof int[]) {
                fanCount = (int[])value;
                num_fan = len;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        TriangleFanSet tfs = new TriangleFanSet();
        copy(tfs);
        tfs.num_fan = this.num_fan;
        tfs.fanCount = new int[this.num_fan];
        System.arraycopy(this.fanCount, 0, tfs.fanCount, 0, this.num_fan);
        return(tfs);
    }
}
