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
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;

import java.util.HashMap;
import java.util.Map;

import org.web3d.x3d.sai.*;

/** This class exists because I was getting very tired of copying and pasting
  * test code between the test cases when all I wanted was a Frame with
  * a Browser instance in it, and wanted the Frame set up in a standard
  * manner.
  */

public class SAITestFactory {
  /** Utility to compare arrays */
  public static boolean compareArray(float a[], float b[]) {
    try {
      int counter=0;
      for (;counter<a.length;counter++) {
        if (a[counter]!=b[counter])
          return false;
      }
      return true;
    } catch (ArrayIndexOutOfBoundsException aio) {
      return false;
    }
  }

  /** Utility to compare arrays */
  public static boolean compareArray(float a[][], float b[]) {
    return compareArray(b,a);
  }

  /** Utility to compare arrays */
  public static boolean compareArray(float a[], float b[][]) {
    try {
      if (a.length==b.length && b.length==0)
        return true;
      int innerMax=b[0].length;
      int outerMax=b.length;
      int flatCounter=0, innerCounter=0, outerCounter=0;
      for (;outerCounter<outerMax;outerCounter++) {
        for (;innerCounter<innerMax;innerCounter++) {
          if (a[flatCounter]!=b[outerCounter][innerCounter])
            return false;
          else
            flatCounter++;
        }
      }
      return true;
    } catch (ArrayIndexOutOfBoundsException aio) {
      return false;
    }
  }

  /** Make a new testing browser.
   * Closing this browser window will cause System.exit to be called
   * @return The browser in the window
   */
  public static ExternalBrowser getBrowser() {
  	return getBrowser(true);
  }

  /** Produce a testing browser with a specific set of parameters
   * @param theMap
   * @param shouldQuit
   * @return
   */
  public static ExternalBrowser getBrowser(Map suppliedParams,boolean shouldQuit) {
	X3DComponent comp=BrowserFactory.createX3DComponent(suppliedParams);
	ExternalBrowser browser=comp.getBrowser();

	Frame f=new Frame();
	f.setLayout(new BorderLayout());
	f.setBackground(Color.blue);
	f.add((Component)comp, BorderLayout.CENTER);
	f.show();
	if (!shouldQuit)
		f.addWindowListener(new java.awt.event.WindowAdapter(){
							/* Normal adapter to make dispose work. */
							public void windowClosing(java.awt.event.WindowEvent e) {
								e.getWindow().hide();
								e.getWindow().dispose();
							}
						});
	else
		f.addWindowListener(new java.awt.event.WindowAdapter(){
							public void windowClosing(java.awt.event.WindowEvent e) {
								System.exit(0);
							}
						});
	f.setSize(400,400);
	return browser;
  	
  }

  /** Procuce an ExternalBrowser instance. 
   *  @param shouldQuit Should closing the frame holding the browser call System.exit? */
  public static ExternalBrowser getBrowser(boolean shouldQuit) {
//    BrowserFactory.setBrowserFactoryImpl(new org.web3d.j3d.browser.X3DJ3DBrowserFactoryImpl());
    HashMap requestedParameters=new HashMap();
    requestedParameters.put("Xj3D_ConsoleShown",Boolean.TRUE);
    requestedParameters.put("Xj3D_LocationShown",Boolean.FALSE);
    return getBrowser(requestedParameters,shouldQuit);
  }

  /** Generate an arbitrary set of geometry for the test sets */
  public static X3DNode[] getTestNodes(ExternalBrowser aBrowser, int number) {
    switch (number) {
      default:
        return null;
    }
  }

}
