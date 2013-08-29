/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.util;

// External imports
import java.util.EventListener;

// Local imports - NONE

/**
 * A key event listener interface modeled on the awt and swt key listener
 * interface. This interface has been developed for use within the KeySensor
 * and StringSensor node implementations to ensure independence from a
 * specific ui toolkit implementation. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Xj3DKeyListener extends EventListener {
    
    /** Invoked when a key has been pressed. */
    public void keyPressed( Xj3DKeyEvent e );
    
    /** Invoked when a key has been released. */
    public void keyReleased( Xj3DKeyEvent e );
}

