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

import org.web3d.vrml.sav.ContentHandler;

/**
 * Primary interface for encoding node representations in this package.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public interface Encodable {
    
    /**
     * Return the name of the node
     *
     * @return the name of the node
     */
    public String getName();
    
    /**
     * Return the DEF name of the node
     *
     * @return the DEF name of the node
     */
    public String getDefName();
    
    /**
     * Push the node contents to the ContentHandler
     */
    public void encode();
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public void setValue(String name, Object value);

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    public void setValue(String name, Object value, int len);

    /**
     * Set the content handler.
     *
     * @param handler The ContentHandler instance to use
     */
    public void setContentHandler(ContentHandler handler);
    
    /**
     * Set the reader to use for parsing field values.
     *
     * @param fieldReader The reader
     */
    public void setFieldReader(VRMLFieldReader fieldReader);
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone();
}
