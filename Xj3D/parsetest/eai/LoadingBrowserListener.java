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


import vrml.eai.Node;
import vrml.eai.event.BrowserListener;
import vrml.eai.event.BrowserEvent;
import vrml.eai.field.EventInMFNode;

/** A BrowserListener which deals with the common task of adding
 *  a group of nodes to a newly loaded scene's group node.
 *  In other words, upon receiving an INITIALIZED event, the listener
 *  does a getNode().getEventIn().setValue(). */
public class LoadingBrowserListener implements BrowserListener {

  String theTargetNode;

  String theTargetField;

  Node theNodes[];

  public LoadingBrowserListener(String aNode, String aField, Node[] someNodes) {
    theTargetNode=aNode;
    theTargetField=aField;
    theNodes=someNodes;
  }

  /** Process an event that has occurred in the VRML Browser.
   *  @see vrml.eai.event.BrowserListener
   */
  public void browserChanged(BrowserEvent event) {
    switch (event.getID()) {
      case BrowserEvent.INITIALIZED:
        
        System.out.println("Attempting to load nodes....");
        ((EventInMFNode)(event.getSource().getNode(theTargetNode).getEventIn(theTargetField))).setValue(theNodes);
        break;
    }
  }
}
