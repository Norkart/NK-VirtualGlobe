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
 * InvalidVRMLSyntaxException is thrown at the time createVrmlFromString(), 
 * createVrmlFromURL() or loadURL() is executed and the vrml syntax is invalid. 
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class InvalidVRMLSyntaxException extends IllegalArgumentException {
	public InvalidVRMLSyntaxException() {
		super();
    }

    public InvalidVRMLSyntaxException(String s) {
        super(s);
    }
}
