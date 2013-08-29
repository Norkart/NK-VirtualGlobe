import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;
/**
 * TextureDemo1
 * 
 * Create your very own animated texture by modifying it every frame.
 */
public class SAIPositionInterpolator {
	
	static X3DNode timer;
	static X3DNode trans;
	
	public static void main(String[] args) {
		// Step One: Create the browser component
		HashMap requestedParameters = new HashMap();
		requestedParameters.put("Xj3D_ConsoleShown", Boolean.TRUE);
		requestedParameters.put("Xj3D_LocationShown", Boolean.FALSE);
		X3DComponent comp = BrowserFactory
				.createX3DComponent(requestedParameters);
		ExternalBrowser browser = comp.getBrowser();
		Frame f = new Frame();
		f.setLayout(new BorderLayout());
		f.setBackground(Color.blue);
		f.add((Component) comp, BorderLayout.CENTER);
		f.show();
		f.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			}
		});
		f.setSize(400, 400);
		// Step Two: Initialize your scene
		browser.addBrowserListener(new GenericSAIBrowserListener());
		X3DScene s = browser.createX3DFromString(
				"PROFILE Interchange\n" 
				+ "Viewpoint {}\n"
				+ "TimeSensor { loop TRUE enabled TRUE }\n"
				+ "Transform { children Shape { geometry Box {} appearance Appearance { material Material { diffuseColor 1 0 0}} }}"
		);
		trans=s.getRootNodes()[2];
		timer=s.getRootNodes()[1];
	    timer.getField("time").addX3DEventListener(new SAIPositionInterpolatorOne(
	        (SFVec3f)trans.getField("translation"))
	    );
		browser.addBrowserListener(new BrowserListener() {
			public void browserChanged(BrowserEvent evt) {
				System.out.println("Finishing the stuff.");
//		        ((SFTime)timer.getField("startTime")).setValue(System.currentTimeMillis());
		        ((SFTime)timer.getField("startTime")).setValue(System.currentTimeMillis());

			}
		});
        browser.replaceWorld(s);
	}
}

/**
 * A simple demo recreating the functionality of an interplator
 * node by listening to a time field on a time sensor and
 * modifying a position field as appropriate.
 */
class SAIPositionInterpolatorOne implements X3DFieldEventListener {

	float startPosition[]=new float[]{-5,0,0};
	float endPosition[]=new float[]{5,0,0};
	
	SFVec3f destination;
	
	/** Buffer for setting position field. */
	float scratchData[]=new float[3];
	
	public SAIPositionInterpolatorOne(SFVec3f dest) {
		destination=dest;
	}
	/**
	 * Respond to the field changing. This demo depends on the fact that it can
	 * modify the field that it is receiving an event from. This is not always
	 * the case
	 */
	public void readableFieldChanged(X3DFieldEvent evt) {
		System.out.println("Time event "+evt.getTime());
		SFTime time=(SFTime)evt.getSource();
		scratchData[0]=(float)(evt.getTime()%5.0)-3.0f;
		System.out.println(scratchData[0]);
		scratchData[1]=0;
		scratchData[2]=0;
		destination.setValue(scratchData);
	}
}
