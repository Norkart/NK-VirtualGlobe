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

package org.web3d.vrml.renderer.j3d.input;

// Standard imports
// none

// Application specific imports
// none

/**
 * A simple internal interface for keeping track of each behavior ID that can
 * be used to trigger a sub-system.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
interface BehaviorIDConstants {

    /** The VRML clock trigger */
    int VRML_CLOCK_ID = 10;

    /** Routing is required now */
    int ROUTE_REQUIRED_ID = 15;

    /** The user input sensor trigger */
    int SENSOR_INPUT_ID = 20;
}
