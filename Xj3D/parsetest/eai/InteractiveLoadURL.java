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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import vrml.eai.Browser;


/**
 *   An interactive program for use as a really primitive VRML browser.
 */

public class InteractiveLoadURL {

    
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    // Attach diagnostic 
    browser.addBrowserListener(new GenericBrowserListener());

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
    LoadURLAction urlLoading=new LoadURLAction(browser,URL,windowFrame);
    doIt.addActionListener(urlLoading);
    JButton newBrowser=new JButton("New Browser");
    ReplaceBrowserAction replaceBrowser=new ReplaceBrowserAction(urlLoading);
    newBrowser.addActionListener(replaceBrowser);
    //windowFrame.getContentPane().add(newBrowser);
    p2.add(newBrowser);
    JButton browseFile=new JButton("Open file...");
    browseFile.addActionListener(new BrowseFileAction(browser,URL));
    p2.add(browseFile);
    windowFrame.pack();
    windowFrame.show();
  }

}


class ReplaceBrowserAction implements ActionListener {

    LoadURLAction target;

    ReplaceBrowserAction(LoadURLAction lua) {
        target=lua;
    }

    public void actionPerformed(ActionEvent e) {
        Browser newBrowser=TestFactory.getBrowser();
        target.setBrowser(newBrowser);
    }

}
