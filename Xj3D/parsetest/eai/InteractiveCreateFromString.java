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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sun.awt.HorizBagLayout;
import vrml.eai.Browser;
import vrml.eai.Node;


/**
 *   An interactive program for use as a really primitive VRML browser.
 *   Modified from the original InteractiveLoadURL to use createVrmlFromString
 *   for testing.
 */

public class InteractiveCreateFromString {

  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    // Attach diagnostic 
    browser.addBrowserListener(new GenericBrowserListener());

    JFrame windowFrame=new JFrame();    
    windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    windowFrame.getContentPane().setLayout(new BorderLayout());
    JPanel p1=new JPanel();
    p1.setLayout(new BorderLayout());
    p1.add(new JLabel("VRML to create:"),BorderLayout.NORTH);
    String initGeom;
    if (args.length>0)
        initGeom=args[0];
    else
        initGeom="Group{}\n";

    JTextArea URL=new JTextArea(initGeom);
    JScrollPane pane=new JScrollPane(URL,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    p1.add(pane,BorderLayout.CENTER);
    JPanel p2=new JPanel();
    p2.setLayout(new GridLayout(1,2));
    JButton doIt=new JButton("create it");
    windowFrame.getContentPane().add(p1,BorderLayout.CENTER);
    p2.add(doIt);
    windowFrame.getContentPane().add(p2,BorderLayout.SOUTH);
    //windowFrame.getContentPane().add(doIt);
    CreateVrmlAction urlLoading=new CreateVrmlAction(browser,URL,windowFrame);
    doIt.addActionListener(urlLoading);
    windowFrame.pack();
    windowFrame.show();
  }

}

class CreateVrmlAction implements ActionListener {
    Browser theBrowser;

    JTextArea URL;

    JFrame windowFrame;

    CreateVrmlAction(Browser b, JTextArea jta, JFrame jf) {
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
    	Node nodes[]=null;
    	try {
           nodes=theBrowser.createVrmlFromString(URL.getText());
    	} catch (Exception err) {
    		err.printStackTrace(System.err);
    	}
        if (nodes!=null) {
        	windowFrame.setTitle("# of nodes: "+nodes.length);
        	for (int counter=0; counter<nodes.length; counter++) {
        		Node a=nodes[counter];
        		System.out.println("#"+counter+":"+((a==null)?"NULL":a.getType()));
        	}
        	theBrowser.replaceWorld(nodes);
        } else
			windowFrame.setTitle("NULL RESULT");
    }
}

