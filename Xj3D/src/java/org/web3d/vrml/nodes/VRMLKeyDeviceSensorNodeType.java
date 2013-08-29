/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// Standard imports - NONE

// Application specific imports
import org.web3d.vrml.util.Xj3DKeyListener;

/**
 * A sensor that takes it's input from a keyboard device.
 * <p>
 *
 * These sensors handle an active status that determines when they should
 * receive key information from the user interface.
 * <p>
 * The key mappings passed through to the sensor are implemented in a ui
 * toolkit independent way through the Xj3DKeyListener/Xj3DKeyEvent objects.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public interface VRMLKeyDeviceSensorNodeType
    extends VRMLSensorNodeType, Xj3DKeyListener {

    /**
     * See if this key device sensor requires only the last key sent or all of
     * them. This is used to determine what is sent to the implementation. For
     * example, a StringSensor would want all the keys, but the KeySensor only
     * wants the last one.
     *
     * @return true if this only requires the last key event
     */
    public boolean requiresLastEventOnly();
}
