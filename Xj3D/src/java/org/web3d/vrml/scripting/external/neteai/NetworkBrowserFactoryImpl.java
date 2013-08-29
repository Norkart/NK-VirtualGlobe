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

import java.applet.Applet;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import vrml.eai.Browser;
import vrml.eai.BrowserFactoryImpl;
import vrml.eai.ConnectionException;
import vrml.eai.NoSuchBrowserException;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;

/**
 * NetworkBrowserFactoryImpl is the browser factory implementation responsible
 * for assembling the client side of the distributed browser.
 */
public class NetworkBrowserFactoryImpl implements BrowserFactoryImpl {

    /** * @see vrml.eai.BrowserFactoryImpl#createComponent(java.lang.String[])  */
    public VrmlComponent createComponent(String[] params)
            throws NotSupportedException {
        throw new NotSupportedException();
    }

    /** * @see vrml.eai.BrowserFactoryImpl#getBrowser(java.applet.Applet)  */
    public Browser getBrowser(Applet applet) throws NotSupportedException,
            NoSuchBrowserException, ConnectionException {
        throw new NotSupportedException();
    }

    /** * @see vrml.eai.BrowserFactoryImpl#getBrowser(java.applet.Applet, java.lang.String, int)  */
    public Browser getBrowser(Applet applet, String frameName, int index)
            throws NotSupportedException, NoSuchBrowserException,
            ConnectionException {
        throw new NotSupportedException();
    }

    /** * @see vrml.eai.BrowserFactoryImpl#getBrowser(java.net.InetAddress, int)  */
    public Browser getBrowser(InetAddress address, int port)
            throws NotSupportedException, NoSuchBrowserException,
            UnknownHostException, ConnectionException {
        try {
            return new NetworkBrowserClient(address,port);
        } catch (UnknownHostException uhe) {
            throw uhe;
        } catch (IOException ioe) {
            throw new ConnectionException();
        }
    }

}
