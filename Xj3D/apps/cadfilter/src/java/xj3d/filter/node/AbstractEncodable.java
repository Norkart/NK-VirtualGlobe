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
import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.sav.StringContentHandler;

/**
 * Common impl for encoding node representations in this package.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class AbstractEncodable implements Encodable {
    
    /** Content Handler Types */
    public static final int HANDLER_BINARY = 0;
    public static final int HANDLER_STRING = 1;
    public static final int HANDLER_NULL = 2;
    
    /** The name of this node */
    public final String name;
    
    /** The node's DEF name */
    public String defName;
    
    /** The node's USE name */
    public String useName;
    
    /** The field parser */
    protected VRMLFieldReader fieldReader;
    
    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, a StringContentHandler, or null */
    protected int handlerType;
    
    /** Content Handler reference */
    protected ContentHandler handler;
    
    /** Binary Content Handler reference */
    protected BinaryContentHandler bch;
    
    /** String Content Handler reference */
    protected StringContentHandler sch;
    
    /**
     * Constructor
     */
    protected AbstractEncodable(String name) {
        this.name = name;
        handlerType = HANDLER_NULL;
    }
    
    /**
     * Constructor
     */
    protected AbstractEncodable(String name, String defName) {
        this.name = name;
        this.defName = defName;
        handlerType = HANDLER_NULL;
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Return the name of the node
     *
     * @return the name of the node
     */
    public String getName() {
        return(name);
    }
    /**
     * Return the DEF name of the node
     *
     * @return the DEF name of the node
     */
    public String getDefName() {
        return(defName);
    }
    
    /**
     * Push the node contents to the ContentHandler
     */
    public abstract void encode();
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public abstract void setValue(String name, Object value);
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    public abstract void setValue(String name, Object value, int len);
    
    /**
     * Set the reader to use for parsing field values.
     *
     * @param fieldReader The reader
     */
    public void setFieldReader(VRMLFieldReader fieldReader) {
        this.fieldReader = fieldReader;
    }
    
    /**
     * Set the content handler.
     *
     * @param handler The ContentHandler instance to use
     */
    public void setContentHandler(ContentHandler handler) {
        
        this.handler = handler;
        if (handler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)handler;
            sch = null;
            handlerType = HANDLER_BINARY;
        } else if (handler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)handler;
            handlerType = HANDLER_STRING;
        } else {
            bch = null;
            sch = null;
            handlerType = HANDLER_NULL;
        }
    }
    
    /**
     * Create and return a copy of this object.
     * Must be overridden, by default returns null.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        return(null);
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Copy the working objects of this into the argument. Used
     * by subclasses to initialize a clone.
     * 
     * @param enc The encodable to initialize.
     */
    protected void copy(AbstractEncodable enc) {
        enc.fieldReader = this.fieldReader;
        enc.handlerType = this.handlerType;
        enc.handler = this.handler;
        enc.bch = this.bch;
        enc.sch = this.sch;
    }
}
