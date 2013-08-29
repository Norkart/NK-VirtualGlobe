/*****************************************************************************
 * Copyright North Dakota State University, 2008
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

/**
 * Test to make sure that the removeValue calls actually work.
 */

public class MFRemoveValueTest {
    private static ExternalBrowser b;
    private static X3DScene s;

    public static void main(String[] args) {
        b = SAITestFactory.getBrowser();
        SAIWaitForBrowserInit saiwaiter=new SAIWaitForBrowserInit(b);
        System.out.println("b: " + b);
    	String messyProto=
    		"#VRML V3.0 utf8\n"+
    		"PROFILE Interactive\n"+
    		"PROTO RemoveTest [\n"+
    		"  inputOutput MFBool bool [TRUE, FALSE, FALSE, TRUE, TRUE]\n"+
    		"  inputOutput MFColorRGBA colorRGBA [0 0 0 0 .1 .1 .1 .1 .2 .2 .2 .2]\n"+
    		"  inputOutput MFColor color [0 0 0 .1 .1 .1 .2 .2 .2]\n"+
    		"  inputOutput MFDouble double [1 2 3 4 5 6]\n"+
    		"  inputOutput MFFloat float [-100.0, -50.0, -25.0]\n"+
    		"  inputOutput MFInt32 int32 [0,1,2,3]\n"+
    		"  inputOutput MFNode nodes [Transform{} Group{} Transform{}]\n"+
    		"  inputOutput MFRotation rotation [1 0 0 .1, 0 1 0 .2, 0 0 1 .3]\n"+
    		"  inputOutput MFString string [\"a\" \"b\" \"c\" \"d\"]\n"+
    		"  inputOutput MFTime time [1 2 3 5 7 11]\n"+
    		"  inputOutput MFVec2d vec2d [0 1, 2 3, 4 5, 6 7, 8 9, 10 11]\n"+
    		"  inputOutput MFVec2d vec2d2 [0 1 2 3 4 5 6 7 8 9 10 11]\n"+
    		"  inputOutput MFVec2f vec2f [0 1, 2 3, 4 5, 6 7, 8 9, 10 11]\n"+
    		"  inputOutput MFVec3d vec3d [0 1 2, 3 4 5, 6 7 8, 9 10 11]\n"+
    		"  inputOutput MFVec3f vec3f [0 1 2, 3 4 5, 6 7 8, 9 10 11]\n"+
    		"] { Group {} }\n"+
    		"RemoveTest {}";
    	s=b.createX3DFromString(messyProto);
    	b.replaceWorld(s);
    	saiwaiter.waitForInit();
    	System.out.println("Done waiting.  Starting tests.");
    	X3DNode n=s.getRootNodes()[0];
    	
    	// bool 
    	MFBool boolField=(MFBool) n.getField("bool");
    	boolField.addX3DEventListener(new GenericSAIFieldListener("bool"));
    	boolField.removeValue(2);
    	
    	// colorRGBA
    	MFColorRGBA colorFieldRGBA=(MFColorRGBA) n.getField("colorRGBA");
    	colorFieldRGBA.addX3DEventListener(new GenericSAIFieldListener("colorRGBA"));
    	colorFieldRGBA.removeValue(0);
    	
    	// color
    	MFColor colorField=(MFColor) n.getField("color");
    	colorField.addX3DEventListener(new GenericSAIFieldListener("color"));
    	colorField.removeValue(0);
    	
    	// double
    	MFDouble doubleField=(MFDouble) n.getField("double");
    	doubleField.addX3DEventListener(new GenericSAIFieldListener("double"));
    	doubleField.removeValue(5);
    	
    	// float 
    	MFFloat floatField=(MFFloat) n.getField("float");
    	floatField.addX3DEventListener(new GenericSAIFieldListener("float"));
    	floatField.removeValue(1);
    	
    	// int32
    	MFInt32 intField=(MFInt32) n.getField("int32");
    	intField.addX3DEventListener(new GenericSAIFieldListener("int"));
    	intField.removeValue(3);

    	// node
    	MFNode nodes = (MFNode) n.getField("nodes");
    	nodes.addX3DEventListener(new GenericSAIFieldListener("nodes"));
    	nodes.removeValue(2);
    	
    	// rotation
    	MFRotation rotField = (MFRotation) n.getField("rotation");
    	rotField.addX3DEventListener(new GenericSAIFieldListener("rotation"));
    	rotField.removeValue(1);
    	
    	// string
    	MFString stringField = (MFString) n.getField("string");
    	stringField.addX3DEventListener(new GenericSAIFieldListener("string"));
    	stringField.removeValue(2);
    	
    	// time
    	MFTime timeField = (MFTime) n.getField("time");
    	timeField.addX3DEventListener(new GenericSAIFieldListener("time"));
    	timeField.removeValue(1);
    	
    	// vec2d
    	MFVec2d vec2d = (MFVec2d) n.getField("vec2d");
    	vec2d.addX3DEventListener(new GenericSAIFieldListener("vec2d"));
    	vec2d.removeValue(4);
    	
    	// vec2f
    	MFVec2f vec2f = (MFVec2f) n.getField("vec2f");
    	vec2f.addX3DEventListener(new GenericSAIFieldListener("vec2f"));
    	vec2f.removeValue(2);
    	
    	// vec3d
    	MFVec3d vec3d = (MFVec3d) n.getField("vec3d");
    	vec3d.addX3DEventListener(new GenericSAIFieldListener("vec3d"));
    	vec3d.removeValue(3);
    	
    	// vec3f
    	MFVec3f vec3f = (MFVec3f) n.getField("vec3f");
    	vec3f.addX3DEventListener(new GenericSAIFieldListener("vec3f"));
    	vec3f.removeValue(2);
    }
}
