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
 * InvalidEventInException is thrown at the time getEventIn() is executed and the 
 * eventIn name is invalid. 
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class InvalidEventInException extends IllegalArgumentException {
    public InvalidEventInException() {
		super();
    }

    public InvalidEventInException(String s) {
		super(s);
    }
}
