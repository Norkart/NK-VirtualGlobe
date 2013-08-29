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
 * InvalidExposedFieldException is thrown at the time getExposedField() is executed
 * and the exposedField name is invalid.  
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class InvalidExposedFieldException extends IllegalArgumentException {
    public InvalidExposedFieldException() {
		super();
    }

    public InvalidExposedFieldException(String s) {
		super(s);
    }
}
