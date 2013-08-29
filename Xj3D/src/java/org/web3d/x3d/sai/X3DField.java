/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Base representation of an X3D field type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface X3DField {
    /**
     * Get the definition of this field.
     *
     * @return The field definition to use
     */
    public X3DFieldDefinition getDefinition();

    /**
     * Check to see if this field is readable. This may return two different
     * sets of values depending on the use. If this field is the field of a
     * script that has been passed to a script implementation, it will return
     * true if the field is an eventIn, exposedField or field and false for an
     * eventOut. If it is a field of any other circumstance (ie an external
     * application querying a node or a script querying another node it has a
     * reference to) it shall return true for eventOuts, exposedFields and
     * false for eventIn or field.
     *
     * @return true if the values of this field are readable
     * @throws InvalidFieldException The underlying node this field came from
     *    has been disposed of
     */
    public boolean isReadable();

    /**
     * Check to see if this field is writable. This may return two different
     * sets of values depending on the use. If this field is the field of a
     * script that has been passed to a script implementation, it will return
     * true if the field is an eventOut, exposedField or field and false for an
     * eventIn. If it is a field of any other circumstance (ie an external
     * application querying a node or a script querying another node it has a
     * reference to) it shall return true for eventIns, exposedFields and
     * false for eventOut or field.
     *
     * @return true if the values of this field are readable
     * @throws InvalidFieldException The underlying node this field came from
     *    has been disposed of
     */
    public boolean isWritable();

    /**
     * Add a listener for changes in this field. This works for listening to
     * changes in a readable field. A future extension to the specification,
     * or a browser-specific extension, may allow for listeners to be added
     * to writable nodes as well.
     * <p>
     * A listener instance cannot have multiple simulatenous registerations.
     * If the listener instance is currently registered, this request shall
     * be silently ignored.
     *
     * @param l The listener to add
     */
    public void addX3DEventListener(X3DFieldEventListener l);

    /**
     * Remove a listener for changes in the readable field. If the listener is
     * not currently registered, this request shall be silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeX3DEventListener(X3DFieldEventListener l);

    /**
     * Associate user data with this field. Whenever an field is generated
     * on this field, this data will be available with the Event through
     * its getData method.
     *
     * @param data The data to associate with this eventOut instance
     */
    public void setUserData(Object data);

    /**
     * Get the user data that is associated with this field.
     *
     * @return The user data, if any, associated with this field
     */
    public Object getUserData();
}
