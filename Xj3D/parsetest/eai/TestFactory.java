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

import vrml.eai.Browser;
import vrml.eai.BrowserFactory;
import vrml.eai.Node;
import vrml.eai.VrmlComponent;

/** This class exists because I was getting very tired of copying and pasting
  * test code between the test cases when all I wanted was a Frame with
  * a Browser instance in it, and wanted the Frame set up in a standard
  * manner.
  */

public class TestFactory {

    /** Static initialization for good of code sanity. */
    static {
        BrowserFactory.setBrowserFactoryImpl(new org.web3d.browser.VRMLBrowserFactoryImpl());
    }

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

    /** Procuce a vrml.eai.Browser instance. */
    public static Browser getBrowser() {
        return TestFactory.getBrowser(false);
    }

    public static Browser getBrowser(boolean shouldQuit) {
        //BrowserFactory.setBrowserFactoryImpl(new org.web3d.vrml.scripting.external.J3DComponentBrowserFactory());
        VrmlComponent comp=BrowserFactory.createVrmlComponent(
                               new String[]{
                                   "xj3d.browser.ui.console=show"
                               }
                           );
        Browser browser=comp.getBrowser();

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

    /** Generate an arbitrary set of geometry for the test sets */
    public static Node[] getTestNodes(vrml.eai.Browser aBrowser, int number) {
        switch (number) {
        case 1:  return aBrowser.createVrmlFromString(geometryA);
        case 2:  return aBrowser.createVrmlFromString(geometryB);
        case 3:  return aBrowser.createVrmlFromString(geometryC);
        case 4:  return aBrowser.createVrmlFromString(geometryD);
        case 5:  return aBrowser.createVrmlFromString(geometryE);
        case 6:  return aBrowser.createVrmlFromString(geometryF);
        default:
            return null;
        }
    }

    /** Random nodes */
    static String geometryA=
        "Transform { translation 0 -2 0\n"+
        " children [	Shape { geometry Sphere	{}\n"+
        " appearance Appearance {\n"+
        "	material Material {\n"+
        " diffuseColor 1 0 0 } } } ] }\n"+
        "Transform { translation	0 0 0 children [ Shape {\n"+
        "geometry Box {} appearance Appearance { material Material {\n"+
        "diffuseColor 0 1 0 ambientIntensity 0.4 emissiveColor 0 0 1 } } } ]}";

    /** Example D.2 from VRML97 Spec */
    static String geometryB=
        "#VRML V2.0 utf8\n"+
        "DEF myColor ColorInterpolator { key [   0.0,    0.5,    1.0 ] keyValue   [ 1 0 0,  0 1 0,  0 0 1 ] }\n"+
        "DEF myClock TimeSensor { cycleInterval 10.0 loop TRUE }\n"+
        "Shape {  appearance Appearance { material DEF myMaterial Material { } } geometry Sphere { } }\n"+
        "ROUTE myClock.fraction_changed TO myColor.set_fraction"+
        "ROUTE myColor.value_changed TO myMaterial.set_diffuseColor";

    /** Example D.2 above stripped of comments and ROUTE statements and
     *  newlines. */
    static String geometryC=
        "ColorInterpolator { key [   0.0,    0.5,    1.0 ] keyValue   [ 1 0 0,  0 1 0,  0 0 1 ] } TimeSensor { cycleInterval 10.0 loop TRUE }  Shape {  appearance Appearance { material Material { } } geometry Sphere { } }";


    /** More random node assortments */
    static String geometryD=
        "Transform { children Group {} }\n"+
        "Group { children Shape {} }\n"+
        "Group { children TimeSensor {}}\n"+
        "Group { children ColorInterpolator {}}\n";

    /* Same as above, but with non-groupers at the top level. */
    static String geometryE=
        "Transform {}\n"+
        "Group {}\n"+
        //"ColorInterpolator {}\n"+
        "Shape {}\n";

    /** A PROTO node with every imaginable type of exposedField */
    static String geometryF=
        "PROTO fieldTest [\n"+
        "  exposedField SFBool SFBool TRUE\n"+
        "  exposedField SFColor SFColor 0 0 0\n"+
        "  exposedField MFColor MFColor []\n"+
        "  exposedField SFFloat SFFloat 0 0 0\n"+
        "  exposedField MFFloat MFFloat []\n"+
        "  exposedField SFImage SFImage 0 0 0\n"+
        "  exposedField SFInt32 SFInt32 0\n"+
        "  exposedField MFInt32 MFInt32 []\n"+
        "  exposedField SFNode SFNode NULL\n"+
        "  exposedField MFNode MFNode []\n"+
        "  exposedField SFRotation SFRotation 0 0 1 0\n"+
        "  exposedField MFRotation MFRotation []\n"+
        "  exposedField SFString SFString \"\"\n"+
        "  exposedField MFString MFString []\n"+
        "  exposedField SFTime SFTime 0\n"+
        "  exposedField MFTime MFTime []\n"+
        "  exposedField SFVec2f SFVec2f 0 0\n"+
        "  exposedField MFVec2f MFVec2f []\n"+
        "  exposedField SFVec3f SFVec3f 0 0 0\n"+
        "  exposedField MFVec3f MFVec3f []\n"+
        "] { Group {} } fieldTest {}\n";

    static String timerTest =
        "DEF TS TimeSensor {\n"+
        "  cycleInterval 5\n"+
        "  enabled TRUE\n"+
        "  loop TRUE\n"+
        "  startTime 0\n"+
        "  stopTime -1\n"+
        "}\n"+
        "DEF P PositionInterpolator {\n"+
        "  key [0 .5 1]\n"+
        "  keyValue [0.0 0.0 0.0 2.0 0.0 0.0 0.0 0.0 0.0]\n"+
        "}\n"+
        "DEF TR Transform {\n"+
        "  children Shape {\n"+
        "    appearance Appearance {\n"+
        "      material Material {\n"+
        "      }\n"+
        "    }\n"+
        "    geometry Sphere {}\n"+
        "  }\n"+
        "}\n"+
        "ROUTE TS.fraction_changed TO P.set_fraction\n"+
        "ROUTE P.value_changed TO TR.translation\n";

    static String proximityTest = "DEF TRANS Transform {\n"+
                                  "  translation 0 0 0\n"+
                                  "  children [\n"+
                                  "    Shape {\n"+
                                  "      geometry Sphere { radius 0.25 }\n"+
                                  "    }\n"+
                                  "  ]\n"+
                                  "}\n"+
                                  "DEF PS ProximitySensor {\n"+
                                  "  center 0 0 0\n"+
                                  "  size 5 5 5\n"+
                                  "}\n"+
                                  "DEF PI PositionInterpolator {\n"+
                                  "  key [0,1]\n"+
                                  "  keyValue [0 0 0, 1 1 0]\n"+
                                  "}\n"+
                                  "DEF TS TimeSensor {}\n"+
                                  "ROUTE PS.enterTime TO TS.startTime\n"+
                                  "ROUTE TS.fraction_changed TO PI.set_fraction\n"+
                                  "ROUTE PI.value_changed TO TRANS.set_translation";

}
