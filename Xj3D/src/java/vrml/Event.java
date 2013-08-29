/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package vrml;

/**
 * Java VRML97 script binding for event information
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class Event implements Cloneable {

    /** The name of the field that has changed */
    protected String name;

    /** Timestamp of when this field changed */
    protected double timestamp;

    /** value of the field */
    protected ConstField value;

    /**
     * Get the name of the field that generated this event
     *
     * @return the field name that has changed
     */
    public String getName() {
        return name;
    }

    /**
     * Get the timestamp for when this event occured. The value is a double
     * representing values in seconds
     *
     * @return The current timestamp of the event
     */
    public double getTimeStamp() {
        return timestamp;
    }

    /**
     * Get the value of the field that has just changed.
     *
     * @return A representation of the field value
     */
    public ConstField getValue() {
        return value;
    }

    /**
     * Clone the object to create an identical copy.
     *
     * @return A complete copy of the event.
     * @throws CloneNotSupportedException Cloning does not work for this class.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
