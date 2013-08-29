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

package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// None

/**
 * A listener used to provide node implementations that get pinged
 * once a frame so that they can do other offline processing that is required
 * pre or post event model evaluation.
 * <p>
 *
 * Note that there is a very similar interface to this one in
 * {@link org.web3d.browser.EventModelStatusListener}. Even though the methods
 * are similar, they form different purposes. This interface is meant for node
 * implementations and thus need to know about the internal part of the event
 * model. The other interface is used by external users looking into the
 * browser that need to synchronise with the event model.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLExternalSynchronizedNodeType extends VRMLNodeType {

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
