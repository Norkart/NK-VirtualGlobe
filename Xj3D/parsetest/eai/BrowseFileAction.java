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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import vrml.eai.Browser;

/** The BrowseFileAction is a siimple ActionListener which
 *  opens a JFileChooser and calls loadURL on a predesignated
 *  Browser instance if the selection is approved.
 */
class BrowseFileAction implements ActionListener {

    /** Shared chooser so that the last working directory
     *  is retained. */
    static JFileChooser chooser=new JFileChooser();

    /** The browser to send the event to */
	Browser theBrowser;

	/** The text area to change with new file name */
	JTextField textTarget;

	/**
	 * @param browser The browser to send the event to
	 */
	public BrowseFileAction(Browser browser, JTextField anEditor) {
		theBrowser=browser;
		textTarget=anEditor;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String urls[]=new String[1];
		//JFileChooser chooser=new JFileChooser();
		switch(chooser.showOpenDialog((Component)(event.getSource()))) {
			case JFileChooser.APPROVE_OPTION:
				try {
					urls[0]=chooser.getSelectedFile().toURL().toExternalForm();
					File f=chooser.getSelectedFile();
					String temp=f.getCanonicalPath();
					System.out.println(temp);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
				textTarget.setText(urls[0]);
				theBrowser.loadURL(urls,null);
				break;
			default:
				System.err.println("Selection not approved.");
		}	
	}
}	
