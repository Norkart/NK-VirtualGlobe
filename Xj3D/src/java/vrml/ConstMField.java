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
 * Java binding for Constant multiple value fields
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public abstract class ConstMField extends ConstField {

    /** The number of elements registered in this class */
    protected int numElements;

    /**
     * Get the number of elements in the current field
     *
     * @return The number of elements
     */
    public int getSize() {
        return numElements;
    }
}
