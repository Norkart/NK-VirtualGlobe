/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.input;

// External imports
// none

// Local imports
// none

/**
 * A collection of navigation state information constants.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public interface Xj3DNavigationState {
	
    /** The navigation state is Inspect */
    public static int INSPECT_STATE = 10;
	
    /** The navigation state is a modified Examine */
    public static int TRACK_EXAMINE_STATE = 11;
	
    /** The navigation state is a modified Pan */
    public static int TRACK_PAN_STATE = 12;
}