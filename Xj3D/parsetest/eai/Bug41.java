import vrml.eai.*;
import vrml.eai.field.*;
import vrml.eai.event.*;

/** Test file for bugzilla.xj3d.org bug #41.
  * Testing addVrmlEventListener actuall producing results for a selection
  * of nodes including a Script node.
  */

public class Bug41 implements vrml.eai.event.VrmlEventListener,
    BrowserListener {

Browser browser;

public static void main(String[] args) {
    new Bug41(TestFactory.getBrowser()).run();
}

Bug41(Browser b) {
    browser=b;
}

public void run() {
    String s[]=new String[1];
    s[0]="http://vcell.ndsu.nodak.edu/~vender/web-start/Bug41.wrl";
    String s2[]=null;
    browser.addBrowserListener(new GenericBrowserListener());
    browser.addBrowserListener(this);
    System.out.println("Waiting for stabilization.");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        System.out.println("Interrupted.");
    }
    System.out.println("Loading the world.");
    browser.loadURL(s,s2);
}

public void doStuff() {
   //works
   Node aNode3 = browser.getNode("TouchCone");
   EventOut aNode3EO = aNode3.getEventOut("touchTime");
   aNode3EO.addVrmlEventListener(this);
   aNode3EO.addVrmlEventListener(new GenericFieldListener("TouchCone.touchTime"));
   aNode3EO.setUserData(new Integer(3));

   //does not work
   Node aNode2 = browser.getNode("MyProtoInstance");
   EventOut aNode2EO = aNode2.getEventOut("myExposedField");
   aNode2EO.addVrmlEventListener(this);
   aNode2EO.addVrmlEventListener(new GenericFieldListener("MyProtoInstance.myExposedField"));
   aNode2EO.setUserData(new Integer(2));

   //does not work
   Node aNode = browser.getNode("MyJavaScript");
   EventOut aNodeEO = aNode.getEventOut("myEventTime");
   aNodeEO.addVrmlEventListener(this);
   aNodeEO.addVrmlEventListener(new GenericFieldListener("MyJavaScript.myEventTime"));
   aNodeEO.setUserData(new Integer(3));
}
   
public void eventOutChanged(vrml.eai.event.VrmlEvent vrmlEvent) {
   System.out.println("Event!");
}

public void browserChanged(BrowserEvent evt) {
    if (evt.getID()==BrowserEvent.INITIALIZED) {
      System.out.println("Doing stuff.");
      doStuff();
    } else if (evt.getID()==BrowserEvent.URL_ERROR) {
      System.out.println("URL Error.");
    } else {
      System.out.println("Received an event of id:"+evt.getID());
    }
}


}
