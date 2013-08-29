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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A test case to check the functionality of the X3DElement implementation.
 * <p>
 *
 * The test case aims to check only element specific functionality. For example
 * adding and removing child elements is actually a function of the Node class
 * and therefore not tested here. We check for:
 *
 * <ul>
 * <li>Basic values like tagName, nodeName and nodeValue are correctly set
 * <li>attributes can be added and removed using strings
 * <li>attributes as nodes can be added and removed
 * <li>Finding existing attributes is possible
 * </ul>
 *
 * Testing does not look at namespace handling currently.
 * <p>
 * When testing attributes, we only check that we can set the values to new
 * values and not what instances get returned. Any testing to make sure
 * attributes are set or not is taken care of in the TestAttribute tester.
 */
public class TestElement extends TestCase {

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestElement(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestElement("testBasicValues"));
        suite.addTest(new TestElement("testAttributeBasics"));
        suite.addTest(new TestElement("testAttributeSet"));

        return suite;
    }

    /**
     * Test for the basic values being correctly set. We look at the values
     * that override the default handling of the node base class
     */
    public void testBasicValues() {
        // Create a default element. This will make sure that the tagName and
        // node name are still the default values of "Element".
        X3DElement el = new X3DElement();

        assertEquals("Default tag name wrong", "Element", el.getTagName());
        assertEquals("Default node name wrong", "Element", el.getNodeName());
        assertEquals("Node type wrong", Node.ELEMENT_NODE, el.getNodeType());
        assertNull("Node value not null", el.getNodeValue());
        assertNotNull("Attributes table null", el.getAttributes());
    }

    /**
     * Check the basic attribute handling. See if a bogus attribute exists,
     * add one and check it exists and then remove it and check it doesn't
     * exist.
     */
    public void testAttributeBasics() {
        X3DElement el = new X3DElement();
        NodePath path = new NodePath(new EventTarget[] { el });
        el.setTargetPath(path);
        el.setAttributeFactory(new TesterAttributeFactory());

        assertTrue("Bogus attribute exists", !el.hasAttribute("test"));

        el.setAttribute("test", "hello");
        assertTrue("test Attribute not found", el.hasAttribute("test"));

        // now remove it
        el.removeAttribute("test");
        assertTrue("test attribute not removed", !el.hasAttribute("test"));
    }

    /**
     * Test changing the attribute values using the string methods. Makes
     * sure that the attribute instance is correctly set.
     */
    public void testAttributeSet() {
        X3DElement el = new X3DElement();
        NodePath path = new NodePath(new EventTarget[] { el });
        el.setTargetPath(path);
        el.setAttributeFactory(new TesterAttributeFactory());

        el.setAttribute("test", "hello");
        assertTrue("test Attribute not found", el.hasAttribute("test"));
        assertEquals("test attribute wrong initial value",
                     "hello",
                     el.getAttribute("test"));

        el.setAttribute("test", "goodbye");
        assertTrue("test Attribute removed after set", el.hasAttribute("test"));
        assertEquals("test attribute wrong after set",
                     "goodbye",
                     el.getAttribute("test"));

        // now do Namespace eqivalent tests of the above
        el.setAttributeNS("ns", "test", "hello");
        assertTrue("test NS Attr not found", el.hasAttributeNS("ns", "test"));
        assertEquals("test NS attribute wrong initial value",
                     "hello",
                     el.getAttributeNS("ns", "test"));

        el.setAttributeNS("ns", "test", "goodbye");
        assertTrue("test NS Attribute removed after set",
               el.hasAttributeNS("ns", "test"));
        assertEquals("test NS attribute wrong after set",
                     "goodbye",
                     el.getAttributeNS("ns", "test"));
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

