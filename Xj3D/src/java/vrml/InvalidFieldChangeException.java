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
 * An exception for fields being changed when they should be.
 * <p>
 * InvalidFieldChangeException may be thrown as a result of all sorts of illegal
 * field changes, for example:
 *      1.Adding a node from one World as the child of a node in another World.
 *      2.Creating a circularity in a scene graph.
 *      3.Setting an invalid string on enumerated fields, such as the fogType field
 *        of the Fog node.
 *      4.Calling the set1Value(), addValue() or delete() on a Field object obtained
 *        by the getEventIn() method.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class InvalidFieldChangeException extends IllegalArgumentException {
    public InvalidFieldChangeException() {
        super();
    }

    public InvalidFieldChangeException(String s) {
        super(s);
    }
}
