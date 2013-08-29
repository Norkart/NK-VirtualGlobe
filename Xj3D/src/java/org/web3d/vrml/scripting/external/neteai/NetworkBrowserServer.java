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

import java.net.ServerSocket;

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

/**
 * The server object which communicates directly with a running
 * rendering engine.
 */
public class NetworkBrowserServer implements ServerBrowserFactory {

    /** The thread which is performing the accept() call on the server
     *  socket and spawning new processing threads.
     */
    ServerProcess acceptingProcess;
        
    /** BrowserCore for fulfilling some calls */
    BrowserCore browserCore;
    
    /** VRML97CommonBrowser for fulfilling some calls */
    VRML97CommonBrowser vrmlBrowser;
    
    /** Basic constructor
     * @param socket Socket to accept connections on
     * @param browserCore BrowserCore to use
     * @param vrmlBrowser CommonBrowser to use
     * @param clock Clock for generating time stamps.
     */
    public NetworkBrowserServer(ServerSocket socket,
            BrowserCore browserCore,
            VRML97CommonBrowser vrmlBrowser,
            VRMLClock clock) {
        acceptingProcess=new ServerProcess(socket,this,clock);
        this.browserCore=browserCore;
        this.vrmlBrowser=vrmlBrowser;
        new Thread(acceptingProcess).start();
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.ServerBrowserFactory#createBrowserInstance()  */
    public ServerBrowser createBrowserInstance() {
        return new ServerBrowser(browserCore,vrmlBrowser);
    }
    
}