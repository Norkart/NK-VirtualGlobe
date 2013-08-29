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

package org.web3d.vrml.sav;

// Standard imports
// none

// Application specific imports
// none

/**
 * Exception indicating that a feature is not supported by the Simple API for
 * VRML.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SAVNotSupportedException extends SAVException {
    public SAVNotSupportedException() {
    }

    public SAVNotSupportedException(String msg) {
        super(msg);
    }
}
