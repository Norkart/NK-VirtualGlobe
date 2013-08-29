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
 * InvalidFieldException is thrown at the time getField() is executed and the field
 * name is invalid.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class InvalidFieldException extends IllegalArgumentException {
    public InvalidFieldException(){
	super();
    }

    public InvalidFieldException(String s){
	super(s);
    }
}
