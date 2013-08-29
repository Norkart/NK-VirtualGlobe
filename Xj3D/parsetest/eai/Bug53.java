import vrml.eai.*;
import vrml.eai.field.*;
import vrml.eai.event.*;

/** Test case for Bug #53 from bugzilla.xj3d.org.
  * Testing whether a PROTO with an eventIn and eventOut IS'd
  * gets the whole event circuit.
  */

public class Bug53 implements vrml.eai.event.VrmlEventListener,
    BrowserListener {

Browser browser;

Node nodes[];

public static void main(String[] args) {
    new Bug53(TestFactory.getBrowser()).run();
}

Bug53(Browser b) {
    browser=b;
}

public void run() {
    String s="Group {}";
    browser.addBrowserListener(this);
    browser.addBrowserListener(new GenericBrowserListener());
    nodes=browser.createVrmlFromString(s);
    System.out.println("Nodes:"+nodes.length);
    System.out.println(nodes[0]);
    browser.replaceWorld(nodes);
}

public void doStuff() {
    String s[]=new String[1];
    s[0]="http://vcell.ndsu.nodak.edu/~vender/web-start/xj3d-RotationSocket.wrl";
    // Register as listener to trigger second layer's addition
    nodes[0].getEventOut("children_changed").addVrmlEventListener(this);
    // Register general diagnostic listener
    nodes[0].getEventOut("children_changed").addVrmlEventListener(new GenericFieldListener("createVrmlFromURL at nodes[0].children probably caused this"));
    browser.createVrmlFromURL(s,nodes[0],"set_children");
}
   
public void eventOutChanged(vrml.eai.event.VrmlEvent vrmlEvent) {
    System.out.println("----------------------------------");
    System.out.println("+++++ +++ Adding the BlueBall +++ ");
    System.out.println("----------------------------------");
    Node n=((EventOutMFNode)(nodes[0].getEventOut("children"))).getValue()[0];
    n.getEventOut("geometry_changed").addVrmlEventListener(new GenericFieldListener("createVrmlFromURL at geometry_changed probably sent me"));
    String s[]=new String[1];
    s[0]="http://vcell.ndsu.nodak.edu/client/WRL/BlueBall.wrl";
    browser.createVrmlFromURL(s,n,"set_geometry");
}

public void browserChanged(BrowserEvent evt) {
    if (evt.getID()==BrowserEvent.INITIALIZED) {
      System.out.println("Doing stuff.");
      doStuff();
    } else if (evt.getID()==BrowserEvent.URL_ERROR) {
      System.out.println("URL Error.");
    } else if (evt.getID()==BrowserEvent.SHUTDOWN) {
      System.out.println("Shutting down.");
    } else {
      System.out.println("Received an event of id:"+evt.getID());
    }
}


}
