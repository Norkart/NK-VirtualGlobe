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

package org.xj3d.ui.swt.widgets;

// External imports
import java.util.EventListener;

// Local imports
// None

/**
 * Defines the requirements for an alarm listener.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface AlarmListener extends EventListener {
    
    /** Invoked when an alarm has expired. */
    public void alarmAction( AlarmEvent ae );
}

