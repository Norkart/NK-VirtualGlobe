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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import vrml.eai.Browser;
import vrml.eai.BrowserFactory;
import vrml.eai.ConnectionException;
import vrml.eai.NoSuchBrowserException;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;


/**
 *   An interactive program for use as a really primitive VRML browser.
 *   Modified to use the network browser code so that I can test the
 *   functionality.  In a real test this would be split in two and
 *   running on seperate machines.
 */

public class NetworkInteractiveLoadURL {

    
  public static void main(String[] args) {
    VrmlComponent comp=BrowserFactory.createVrmlComponent(
                           new String[]{
                               "Xj3D_ServerPort=4023"
                           }
                       );

    Frame f=new Frame();
    f.setLayout(new BorderLayout());
    f.setBackground(Color.blue);
    f.add((Component)comp, BorderLayout.CENTER);
    f.show();
    f.addWindowListener(new WindowAdapter(){
                            public void windowClosing(java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
    f.setSize(400,400);
    Browser serverBrowser=comp.getBrowser();

    // Attach diagnostic 
    serverBrowser.addBrowserListener(new GenericBrowserListener());
    
    
    JFrame windowFrame=new JFrame();    
    windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    windowFrame.getContentPane().setLayout(new GridLayout(2,1));
    JPanel p1=new JPanel();
    p1.setLayout(new GridLayout(2,1));
    p1.add(new JLabel("URL of world to load"));
    String initURL;
    if (args.length>0)
        initURL=args[0];
    else
        initURL="http://vcell.ndsu.nodak.edu/client/WRL/BlueBall.wrl";

    JTextField URL=new JTextField(initURL);
    p1.add(URL);
    JPanel p2=new JPanel();
    p2.setLayout(new GridLayout(1,2));
    JButton doIt=new JButton("Load URL");
    windowFrame.getContentPane().setLayout(new FlowLayout());
    windowFrame.getContentPane().add(p1);
    p2.add(doIt);
    windowFrame.getContentPane().add(p2);
    //windowFrame.getContentPane().add(doIt);
    
    Browser clientBrowser;
    try {
        clientBrowser = BrowserFactory.getBrowser(InetAddress.getLocalHost(),4023);
    } catch (NotSupportedException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (NoSuchBrowserException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (ConnectionException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (UnknownHostException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    }


    clientBrowser.addBrowserListener(new GenericBrowserListener("Client side"));
    LoadURLAction urlLoading=new LoadURLAction(clientBrowser,URL,windowFrame);
    doIt.addActionListener(urlLoading);
    JButton browseFile=new JButton("Open file...");
    browseFile.addActionListener(new BrowseFileAction(clientBrowser,URL));
    p2.add(browseFile);
    windowFrame.pack();
    windowFrame.show();
  }

}