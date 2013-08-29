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

/**
 * Marker interface to say that the implementing class is interested in knowing
 * about per-frame updates and the pre event cascade processing steps.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface X3DPerFrameObserverScript extends X3DScriptImplementation {
    /**
     * Notification that the script is in the prepareEvents section of the
     * event model evaluation.
     */
    public void prepareEvents();
}
