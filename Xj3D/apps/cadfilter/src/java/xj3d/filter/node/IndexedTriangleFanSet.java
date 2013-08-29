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
 * Wrapper for an X3D IndexedTriangleFanSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class IndexedTriangleFanSet extends ComposedGeometry {
    
    /** Field value */
    public int[] index;
    
    /** Number of indices in the index array */
    public int num_index;
    
    /**
     * Constructor
     */
    public IndexedTriangleFanSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public IndexedTriangleFanSet(String defName) {
        super("IndexedTriangleFanSet", defName);
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
                
                if (index != null) {
                    handler.startField("index");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(index, num_index);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(index, num_index));
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
        
        if (name.equals("index")) {
            if (value instanceof String) {
                index = fieldReader.MFInt32((String)value);
                num_index = index.length ;
            } else if (value instanceof String[]) {
                index = fieldReader.MFInt32((String[])value);
                num_index = index.length;
            } else if (value instanceof int[]) {
                index = (int[])value;
                num_index = index.length;
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
        
        if (name.equals("index")) {
            if (value instanceof int[]) {
                index = (int[])value;
                num_index = len;
            }
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        IndexedTriangleFanSet itfs = new IndexedTriangleFanSet();
        copy(itfs);
        itfs.num_index = this.num_index;
        itfs.index = new int[this.num_index];
        System.arraycopy(this.index, 0, itfs.index, 0, this.num_index);
        return(itfs);
    }
}
