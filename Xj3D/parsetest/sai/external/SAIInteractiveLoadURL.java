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

import org.web3d.x3d.sai.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;



/**
 *   An interactive program for use as a really primitive X3D browser.
 */

public class SAIInteractiveLoadURL {

  public static void main(String[] args) {
  	// Make the brower
    HashMap requestedParameters=new HashMap();
    requestedParameters.put("Xj3D_ConsoleShown",Boolean.TRUE);
    requestedParameters.put("Xj3D_LocationShown",Boolean.FALSE);
	X3DComponent comp=BrowserFactory.createX3DComponent(requestedParameters);
	ExternalBrowser browser=comp.getBrowser();

	Frame f=new Frame();
	f.setLayout(new BorderLayout());
	f.setBackground(Color.blue);
	f.add((Component)comp, BorderLayout.CENTER);
	f.show();
	f.addWindowListener(new java.awt.event.WindowAdapter(){
		public void windowClosing(java.awt.event.WindowEvent e) {
			System.exit(0);
		}
	});
	f.setSize(400,400);

    // Attach diagnostic 
    browser.addBrowserListener(new GenericSAIBrowserListener());

    // Maka a simple set of controls
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
    JButton doIt=new JButton("Load from URL");
    windowFrame.getContentPane().setLayout(new FlowLayout());
    windowFrame.getContentPane().add(p1);
    p2.add(doIt);
    windowFrame.getContentPane().add(p2);
    //windowFrame.getContentPane().add(doIt);
    LoadSAIURLAction urlLoading=new LoadSAIURLAction(browser,URL,windowFrame);
    doIt.addActionListener(urlLoading);
    JButton newBrowser=new JButton("New Browser");
    ReplaceSAIBrowserAction replaceBrowser=new ReplaceSAIBrowserAction(urlLoading);
    newBrowser.addActionListener(replaceBrowser);
    //windowFrame.getContentPane().add(newBrowser);
    p2.add(newBrowser);
    JButton browseFile=new JButton("Open file...");
    browseFile.addActionListener(new BrowseSAIFileAction(browser,URL));
    p2.add(browseFile);
    windowFrame.pack();
    windowFrame.show();
  }

}

class BrowseSAIFileAction implements ActionListener {

    /** Static shared file chooser so that
     *  working directories are retained. */
	static JFileChooser chooser=new JFileChooser();    
    
	/** The browser to send the event to */
	ExternalBrowser theBrowser;

	/** The text area to change with new file name */
	JTextField textTarget;

	/**
	 * @param browser The browser to send the event to
	 */
	public BrowseSAIFileAction(ExternalBrowser browser, JTextField anEditor) {
		theBrowser=browser;
		textTarget=anEditor;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String urls[]=new String[1];
		switch(chooser.showOpenDialog((Component)(event.getSource()))) {
			case JFileChooser.APPROVE_OPTION:
				try {
					urls[0]=chooser.getSelectedFile().toURL().toExternalForm();
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return;
				}
				textTarget.setText(urls[0]);
				theBrowser.loadURL(urls,null);
				break;
			default:
				System.err.println("Selection not approved.");
		}	
	}
}	

class ReplaceSAIBrowserAction implements ActionListener {

    LoadSAIURLAction target;

    ReplaceSAIBrowserAction(LoadSAIURLAction lua) {
        target=lua;
    }

    public void actionPerformed(ActionEvent e) {
        ExternalBrowser newBrowser=SAITestFactory.getBrowser();
        target.setBrowser(newBrowser);
    }

}


class LoadSAIURLAction implements ActionListener {
    Browser theBrowser;

    JTextField URL;

    JFrame windowFrame;

    LoadSAIURLAction(Browser b, JTextField jta, JFrame jf) {
      theBrowser=b;
      URL=jta;
      windowFrame=jf;
    }

    /** Replace the targetted browser instance. */
    public void setBrowser(Browser newBrowser) {
        theBrowser=newBrowser;
    }

    /** Deal with the mouse click */
    public void actionPerformed(ActionEvent e) {
        String args[]=new String[1];
        args[0]=URL.getText();
        theBrowser.loadURL(args,null);
    }
}

