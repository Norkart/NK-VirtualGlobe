import java.util.*;
import java.awt.*;
import java.applet.*;
import vrml.BaseNode;
import vrml.external.Node;
import vrml.external.Browser;
import vrml.external.field.*;

import netscape.javascript.JSObject;

import org.adl.lms.client.APIAdapterApplet;

public class ScriptApplet extends Applet {

    Browser browser;
    Label label = null;
    APIAdapterApplet apiApplet = null;
    JSObject jsRoot = null;
    Timer timer;


    final String API_ADAPTER_NAME = "APIAdapter";
    final String DoContinueArgs[] = {"completed"};

    public void init() {
	super.init();

	timer = new Timer();
	label = new Label("This is the Java Applet.");

	// Paint something to the applet so that you can see something
	add(label);
    }

    public void start() {
	System.out.println("Enter start");

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
	    System.out.println("Leave start: get node");

	    Node node = browser.getNode("StartMeUp_LMSInitialize_Script");

	    System.out.println("Leave start: get EventOut");

	    //XXX This is cosmoplayer specific.
	    node.getEventOut("LMSType").advise(new GridObserver(), null);

	    // This is the EAI compliant way of registering events.
	    // node.getEventOut("touchChoice").addVrmlEventListener(VrmlEventListener);
	} catch (Exception e) {
	    System.out.println("ScriptApplet - " + e.toString());
	}

	System.out.println("Leave start");
    }

    public void changeLabel(String s) {
	label.setText(s);
    }

    public void doLMSInitialize() {
	System.out.println("Enter ScriptApplet's doLMSInitialize");
	changeLabel("Enter ScriptApplet's doLMSInitialize");

        if (jsRoot == null) {
	    System.out.println("Exit ScriptApplet's doLMSInitialize, jsRoot is null");
	    return;
	}

	String cs[] = {"completed"};
	String empty[] = {""};
	//	jsRoot.call("doLMSInitialize", null);
	jsRoot.call("doContinue", cs);

	System.out.println("Exit ScriptApplet's doLMSInitialize");
	changeLabel("Exit ScriptApplet's doLMSInitialize");
    }

    public void doContinue() {
	System.out.println("Enter ScriptApplet's doContinue");

        if (jsRoot == null) {
	    System.out.println("Exit ScriptApplet's doContinue, jsRoot is null");
	    return;
	}

	String cs[] = {"completed"};
	String empty[] = {""};
	jsRoot.call("doContinue", cs);
	//	jsRoot.call("nextSCO", empty);

	System.out.println("Exit ScriptApplet's doContinue");
    }

    public class GridObserver implements EventOutObserver {
	public void callback(EventOut value, 
			     double   timeStamp, 
			     Object   data) {
	    //	    int touchChoice = ((EventOutSFInt32)value).getValue();

	    System.out.println("Enter GridObserver");
	    String type = ((EventOutSFString)value).getValue();
	    changeLabel("type - " + type);

	    //	    if (touchChoice == 0) {
		changeLabel("calling LMSInitialize");

		timer.schedule(new TimerTask() {
			public void run() {
			    doLMSInitialize();
			}
		    }, 10);
		//	    }

	    System.out.println("Leave GridObserver");
	}
    }
}
