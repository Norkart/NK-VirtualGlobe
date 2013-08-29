/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

/**
 * NetworkStartupListener is an interface
 * for detecting the end of the network initialization
 * code.
 */
public interface NetworkStartupListener {

    /** Notificatation that network initialization
     *  is completed and request writing can proceed.
     */
    void notifyNetworkInitializationComplete();
    
}
