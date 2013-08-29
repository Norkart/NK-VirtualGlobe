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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



/**
 *   An interactive program for use as a really primitive X3D browser.
 */

public class SAIInteractiveCreateFromString {

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

    JFrame windowFrame=new JFrame();    
    windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    windowFrame.getContentPane().setLayout(new BorderLayout());
    JPanel p1=new JPanel();
    p1.setLayout(new BorderLayout());
    p1.add(new JLabel("String to create"),BorderLayout.NORTH);
    String initString;
    if (args.length>0)
        initString=args[0];
    else
        initString="PROFILE Interactive\nGroup{}";

    JTextArea creationString=new JTextArea(initString);
    JScrollPane pane=new JScrollPane(creationString,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    p1.add(pane,BorderLayout.CENTER);
    JPanel p2=new JPanel();
    p2.setLayout(new GridLayout(1,2));
    JButton doIt=new JButton("Create");
    windowFrame.getContentPane().add(p1,BorderLayout.CENTER);
    p2.add(doIt);
    windowFrame.getContentPane().add(p2,BorderLayout.SOUTH);
    //windowFrame.getContentPane().add(doIt);
    CreateSAIStringAction urlLoading=new CreateSAIStringAction(browser,creationString,windowFrame);
    doIt.addActionListener(urlLoading);
    JButton newBrowser=new JButton("New Browser");
    windowFrame.pack();
    windowFrame.show();
  }

}

/** Action listener to create a new scene from a string and then
 * set that scene as the current one.
 */
class CreateSAIStringAction implements ActionListener {
    Browser theBrowser;

    JTextArea URL;

    JFrame windowFrame;

    CreateSAIStringAction(Browser b, JTextArea jta, JFrame jf) {
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
    	String args=URL.getText();
    	X3DScene s = null;
    	try {
    	    s=theBrowser.createX3DFromString(args);
    	} catch (Exception er) {
    		er.printStackTrace(System.err);
    	}
    	if (s!=null) {
    		System.out.println("Number of root nodes:"+s.getRootNodes().length);
    		theBrowser.replaceWorld(s);
    	} else {
    		System.err.println("NULL Scene result");
    	}
/*    	String args[]=new String[1];
        args[0]=URL.getText();
        theBrowser.loadURL(args,null);
*/
    }
}

