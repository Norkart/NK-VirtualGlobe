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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.web3d.vrml.nodes.VRMLClock;

/**
 * ServerProcess is the task which accepts incoming network connections
 * and spawns the appropriate processing threads.
 */
public class ServerProcess implements Runnable {

    /** The clock for generating time stamps */
    VRMLClock timeClock;
    
    /** Factory for generating ServerBrowser instances */
    ServerBrowserFactory serverBrowserFactory;
    
	/** Create a ServerProcess which will accept incoming connections
	 * on a specified ServerSocket
	 * @param socket The ServerSocket to accept on.
	 */
	public ServerProcess(ServerSocket socket, ServerBrowserFactory browserFactory, VRMLClock clock) {
		serverConnection=socket;
		serverBrowserFactory=browserFactory;
		timeClock=clock;
	}
	
	/** The connection on which to listen */
	ServerSocket serverConnection;
	
	/** * @see java.lang.Runnable#run()  */
	public void run() {
		while (true) {
			try {
				Socket client=serverConnection.accept();
				ServerProcessingTask processor=new ServerProcessingTask(client, serverBrowserFactory.createBrowserInstance(), timeClock);
				new Thread(processor).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}

