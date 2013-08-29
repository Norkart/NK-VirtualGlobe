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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.web3d.x3d.jaxp.dom.sai.element.BaseLine.BillboardElement;
import org.web3d.x3d.jaxp.dom.sai.SAIAttributeFactory;

/**
 * A test case to check the functionality of the DOM Attribute implementation.
 * <p>
 * The test case looks at how the attribute:
 * <ul>
 * <li>Ensures setting new values works
 * <li>The owner is correctly handled before and after adding to elements
 * <li>That the attribute holds the correct parent and sibling values
 * <li>Cloning sets the appropriate fields to the right values
 * <li>Sending events results in the right event information coming
 * </ul>
 */
public class TestAttribute extends TestCase implements EventListener {

    /** The factory needed by the parent elements for testing */
    private AttributeFactory attribFactory;

    /**
     * Flag used in the event processing tests. true if this is the test for
     * The attribute value changing.
     */
    private boolean changeEvent;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestAttribute(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestAttribute("testValue"));
        suite.addTest(new TestAttribute("testOwner"));
        suite.addTest(new TestAttribute("testRelationships"));
        suite.addTest(new TestAttribute("testClone"));
        suite.addTest(new TestAttribute("testChangeValueEvent"));
        suite.addTest(new TestAttribute("testSameValueEvent"));

        return suite;
    }

    /**
     * Initialise the class for this round of testing.
     */
    public void setUp() {
        attribFactory = new SAIAttributeFactory();
    }

    /**
     * Test setting various values in the attribute. We look at a couple
     * of different combos to make sure it all works.
     */
    public void testValue() {
        X3DAttr attr = new X3DAttr("foo", "a");
        NodePath path = new NodePath(new EventTarget[] { attr });
        attr.setTargetPath(path);

        assertEquals("Original Value wrong", "a", attr.getValue());

        attr.setValue("b");
        assertEquals("Updated value wrong", "b", attr.getValue());
    }

    /**
     * Test that the attribute is correctly handling it's owner
     * relationship with the element.
     */
    public void testOwner() {
        // first create a standalone attribute
        Attr unowned = new X3DAttr("unowned", "");

        assertNull("Unowned is owned", unowned.getOwnerElement());

        X3DElement el = createElement();
        NodePath path = new NodePath(new EventTarget[] { el });
        el.setTargetPath(path);

        Attr owned = el.getAttributeNode("axisOfRotation");

        assertNotNull("Could not find axis attr", owned);
        assertNotNull("Owned has no owner", owned.getOwnerElement());
        assertTrue("Not the same owner (old)", owned.getOwnerElement() == el);

        // Now, set the unowned attribute into our element and check
        // the owner to match.
        el.setAttributeNode(unowned);
        Attr attr = el.getAttributeNode("unowned");

        assertNotNull("Couldn't find 'unowned'", attr);
        assertTrue("Not the same owner (new)", attr.getOwnerElement() == el);

        // Test now adding an attribute not using the xNode methods.
        el.setAttribute("bboxCenter", "1 1 1");
        attr = el.getAttributeNode("bboxCenter");

        assertNotNull("Couldn't find 'bboxCenter'", attr);
        assertTrue("Not the same owner (bboxCenter)", attr.getOwnerElement() == el);
    }

    /**
     * Test to make sure that the parent/sibling relationships are
     * created properly. To do this, we create an element from a
     * and fetch it's attributes. These should not have parent or
     * siblings set (the methods should return null)
     */
    public void testRelationships() {
        X3DElement el = createElement();

        Attr attr = el.getAttributeNode("axisOfRotation");

        // right, now let's test.
        assertNotNull("Could not find axis attr", attr);
        assertNull("Parent incorrectly set", attr.getParentNode());
        assertNull("Next sibling incorrect", attr.getNextSibling());
        assertNull("Previous sibling incorrect", attr.getPreviousSibling());
    }

    /**
     * Test the cloning behaviour of an attribute. When we clone it, it
     * should set the specified flag.
     */
    public void testClone() {
        Attr orig = new X3DAttr("foo", "");

        Attr copy = (Attr)orig.cloneNode(false);

        assertTrue("Specified flag incorrect (shallow)", copy.getSpecified());

        copy = (Attr)orig.cloneNode(true);
        assertTrue("Specified flag incorrect (deep)", copy.getSpecified());
    }

    /**
     * Test that we get an event when we set the value to something different.
     * The one problem this test will have is if the event implementation is
     * threaded and not synchronous with this set method. If threaded, it is
     * possible that this test will exit before the event has been propogated
     * back to this class instance.
     */
    public void testChangeValueEvent() {
        X3DAttr attr = new X3DAttr("foo", "a");
        NodePath path = new NodePath(new EventTarget[] { attr });
        attr.setTargetPath(path);

        attr.addEventListener(DOMEventNames.ATTR_MODIFIED_EVENT, this, false);

        changeEvent = true;

        attr.setValue("b");
    }

    /**
     * Test that we do not get an event when we set the attribute value to
     * the same value.
     */
    public void testSameValueEvent() {
        X3DAttr attr = new X3DAttr("foo", "a");
        NodePath path = new NodePath(new EventTarget[] { attr });
        attr.setTargetPath(path);

        attr.addEventListener(DOMEventNames.ATTR_MODIFIED_EVENT, this, false);

        changeEvent = false;

        attr.setValue("a");
    }

    // Local utility methods

    /**
     * Convenience method to create a new element. The element created is a
     * BillboardElement.
     *
     * @return A new element instance
     */
    private X3DElement createElement() {
        ProfileInfo profile = new ProfileInfo(null);
        X3DElement el = new BillboardElement(profile);

        EventTarget[] path = { el };
        NodePath nodePath = new NodePath(path);

        el.setTargetPath(nodePath);
        el.setAttributeFactory(attribFactory);

        return el;
    }

    // Method from the event listener

    /**
     * Process the incoming event. This is a generic implementation used
     * across all of the methods. The behaviour depends on the flag set during
     * the test.
     *
     * @param evt The event to process
     */
    public void handleEvent(Event evt) {
        if(!changeEvent)
            fail("Event received when it should not be");

        if(!(evt instanceof MutationEvent))
            fail("Not a mutation event");

        MutationEvent me = (MutationEvent)evt;

        assertEquals("Attribute name doesn't match", "foo", me.getAttrName());
        assertEquals("Change type doesn't match",
                     MutationEvent.MODIFICATION,
                     me.getAttrChange());

        assertTrue("Attr is should not be cancelable", !evt.getCancelable());
        assertTrue("Attr should bubble", evt.getBubbles());

        String old_val = me.getPrevValue();
        String new_val = me.getNewValue();

        assertEquals("Previous value not correct", "a", old_val);
        assertEquals("New value not correct", "b", new_val);
        assertTrue("Attr values not set correctly", !old_val.equals(new_val));
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

