/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// Standard imports
import java.util.EventListener;

// Application specific imports
// None

/**
 * A listener for events on X3D fields
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface X3DFieldEventListener extends EventListener {

    /**
     * Process an event that has occurred on a node's event output.
     *
     * @param evt The event that caused this method to be called
     */
    public void readableFieldChanged(X3DFieldEvent evt);
}
