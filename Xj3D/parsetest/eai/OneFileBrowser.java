/*****************************************************************************
 * Copyright North Dakota State University, 2001
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
import java.awt.Component;

import javax.swing.JFrame;

import vrml.eai.Browser;
import vrml.eai.BrowserFactory;
import vrml.eai.VrmlComponent;


/**
 * A simple application for just loading a file and viewing it.
 */

public class OneFileBrowser {

  public static void main(String[] args) {
  	VrmlComponent component=BrowserFactory.createVrmlComponent(null);
    Browser browser=component.getBrowser();

    // Attach diagnostic 
    browser.addBrowserListener(new GenericBrowserListener());

    JFrame windowFrame=new JFrame();    
    windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    windowFrame.getContentPane().add((Component) component,BorderLayout.CENTER);
    windowFrame.setSize(400,400);
    windowFrame.show();
    browser.loadURL(args,null);
  }

}