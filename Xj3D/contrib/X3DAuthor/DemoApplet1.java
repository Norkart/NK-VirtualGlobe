import java.util.*;
import java.awt.*;
import java.applet.*;

import vrml.BaseNode;
import vrml.external.Node;
import vrml.external.Browser;
import vrml.external.field.*;

import netscape.javascript.JSObject;

import org.adl.lms.client.APIAdapterApplet;

public class DemoApplet1 extends Applet {

    Browser browser;
    TextArea status;
    GridBagConstraints constraints;
    APIAdapterApplet apiApplet = null;
    JSObject jsRoot = null;
    Timer timer;


    final String API_ADAPTER_NAME = "APIAdapter";
    final String DoContinueArgs[] = {"completed"};

    public void init() {
	super.init();

	timer = new Timer();

	/*
	// pack components using GridBagLayout
	setLayout(new GridBagLayout());
	constraints = new GridBagConstraints();

	status = new TextArea();

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.BOTH;
	add(status, constraints);
	*/
    }

    public void start() {
	//	System.out.println("Enter start");

	jsRoot = JSObject.getWindow(this);

	for (int count = 0; count < 10; count++) {
	    // get the Browser
	    browser = Browser.getBrowser(this);

	    if (browser != null)
		break;

	    try {
		Thread.sleep(200);
	    } catch (InterruptedException ignored) {}
	}

	// Test if we really got a browser and print a message to the Java Console
	if (browser == null) {
	    System.out.println("FATAL ERROR! no browser :( ");
	    return;
	}


	try {
	    // This is the EAI compliant way of registering events.
	    // node.getEventOut("LMSType").addVrmlEventListener(VrmlEventListener);

	    System.out.println("Registering callbacks with the VRML world.");

	    Node node;

	    // LMS script node
	    node = browser.getNode("VC_Gasket_Trigger_LMSMarkSCOComplete_Script");

	    //XXX This is cosmoplayer specific.
	    node.getEventOut("LMSType").advise(new DemoObserver(), "SCO 01");
	} catch (Exception e) {
	    System.out.println("DemoApplet1 - " + e.toString());
	}
    }

    public void changeLabel(String s) {
	status.append(s + "\n");
    }

    public void doLMSInitialize() {
	System.out.println("Enter DemoApplet1's doLMSInitialize");
	//	changeLabel("Enter DemoApplet1's doLMSInitialize");

        if (jsRoot == null) {
	    System.out.println("Exit DemoApplet1's doLMSInitialize, jsRoot is null");
	    return;
	}

	String cs[] = {"completed"};
	String empty[] = {""};
	jsRoot.call("doLMSInitialize", null);

	System.out.println("Exit DemoApplet1's doLMSInitialize");
	//	changeLabel("Exit DemoApplet1's doLMSInitialize");
    }

    public void doContinue(String task) {
        if (jsRoot == null) {
	    System.out.println("Exit DemoApplet1's doContinue, jsRoot is null");
	    return;
	}

	String cs[] = {"completed"};
	jsRoot.call("doContinue", cs);

	System.out.println("Notifying LMS that " + task + " is complete.");
	//	changeLabel("Notifying LMS that task is complete.");
    }

    public class DemoObserver implements EventOutObserver {
	public void callback(EventOut value, 
			     double   timeStamp, 
			     final Object   data) {
	    //	    int touchChoice = ((EventOutSFInt32)value).getValue();

	    //	    System.out.println("Enter DemoObserver");
	    String type = ((EventOutSFString)value).getValue();
	    //	    changeLabel("type - " + type);

	    //	    if (touchChoice == 0) {
	    //		changeLabel("calling doContinue");

		timer.schedule(new TimerTask() {
			public void run() {
			    //			    doLMSInitialize();
			    doContinue((String)data);
			}
		    }, 10);
		//	    }

	    System.out.println("Leave DemoObserver");
	}
    }
}
