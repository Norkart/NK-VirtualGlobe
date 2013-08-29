/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
//------------------------------------------------------------

package org.web3d.x3d.jaxp.dom;

import org.w3c.dom.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A test case to check the functionality of the X3DDocument
 * implementation.
 * <p>
 * The document class is quite complex so this test has a lot of test
 * cases to implement to fully verify that everything is working.
 *
 */
public class TestX3DDocument extends TestCase {

    /** The listing of all the elements that we should be able to create */
    private static final String[] ELEMENT_LIST = {
	"Anchor", "Appearance", "AudioClip",
	"Background", "Box",
	"Collision", "Color", "ColorInterpolator", "Coordinate",
	"CoordinateInterpolator", "Cylinder", "CylinderSensor",
	"DirectionalLight",
	"ElevationGrid", "EventIn", "EventOut", "Extrusion",
	"Field", "Fog", "FontStyle",
	"Group",
	"ImageTexture", "IndexedFaceSet", "Inline",
	"Material", "MovieTexture",
	"NavigationInfo", "Normal", "NormalInterpolator",
	"OrientationInterpolator",
	"PlaneSensor", "PointLight", "PositionInterpolator",
	"Proto", "ProtoUse", "ProximitySensor",
	"Route",
	"ScalarInterpolator", "Scene", "Shape", "Sound",
	"Sphere", "SphereSensor", "SpotLight", "Switch",
	"Text", "TextureCoordinate", "TextureTransform",
	"TimeSensor", "TouchSensor", "Transform",
	"ViewPoint", "VisibilitySensor", "WorldInfo"
    };

    /** The document under test */
    private Document document;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestX3DDocument(String name) {
	super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new TestX3DDocument("testCreateElement"));
	suite.addTest(new TestX3DDocument("testCreateAttribute"));
	suite.addTest(new TestX3DDocument("testCreateEntity"));

	return suite;
    }

    /**
     * Setup the environment for this test.
     */
    protected void setUp() {
	document = new X3DDocument();
    }

    /**
     * Test that we can create an element of the named type. This is a generic
     * test and does not look at dealing with child elements or attributes. We
     * Loop through all of the elements that should be able to be created from
     * this document and make sure they can all be created.
     */
    public void testCreateElement() {
	Element el;

	for(int i = 0; i < ELEMENT_LIST.length; i++) {
	    el = document.createElement(ELEMENT_LIST[i]);
	    assertNotNull("Failed to create " + ELEMENT_LIST[i], el);
	    assertEquals("Name not correct for " + ELEMENT_LIST[i],
	                 el.getTagName(),
	                 ELEMENT_LIST[i]);

	    assertNull("Namespace not null", el.getNamespaceURI());
	}
    }

    /**
     * Test creating a standalone attribute. Make sure that all the right
     * flags are set when the attribute has been created.
     */
    public void testCreateAttribute() {
	Attr attr = document.createAttribute("Color");
	assertNotNull("Could not create Color attribute", attr);
	assertEquals("Name is not set", "Color", attr.getName());

	// Now test the parts of the attribute
	assertNull("Namespace not null", attr.getNamespaceURI());
	assertNull("Local name not null", attr.getLocalName());
	assertNull("Prefix not null", attr.getPrefix());
	assertEquals("Value is not empty", "", attr.getValue());

	// set a value and make sure it is the same when fetched
	attr.setValue("blah");
	assertEquals("Value not set", "blah", attr.getValue());
    }

    /**
     * Test creating the various entity types. They are all wrapped up in
     * this one test.
     */
    public void testCreateEntity() {
	EntityReference ent = document.createEntityReference("entity");
	assertNotNull("Could not create entity", ent);
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
	TestRunner.run(suite());
    }
}

