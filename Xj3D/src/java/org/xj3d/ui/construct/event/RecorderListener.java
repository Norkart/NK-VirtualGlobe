/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.construct.event;

// External imports
import java.util.EventListener;

// Local imports
// None

/**
 * The listener interface for receiving events. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface RecorderListener extends EventListener {
    
    /**
     * Invoked when an RecorderEvent occurs.
     */
    public void recorderStatusChanged( RecorderEvent evt );
}

