/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// Standard imports
// none

// Application specific imports
// none

/**
 * Notification about where the VRML/X3D Event Model is.  This can be
 * used to make safer changes to the underlying rendering structures.  You must
 * know what you are doing at this level.  Remember that underlying structures
 * may change between releases.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface EventModelStatusListener {

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void preEventEvaluation();

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void postEventEvaluation();
}
