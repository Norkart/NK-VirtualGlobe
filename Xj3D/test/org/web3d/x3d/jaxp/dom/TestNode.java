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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.web3d.x3d.jaxp.dom.sai.element.BaseLine.BillboardElement;

/**
 * A test case to check the functionality of the basic Node implementation
 * encapsulated in the X3DNode class.
 * <p>
 *
 * The following tests are performed:
 * <ul>
 * <li>Making sure the basic node relationships work on an isolated node.
 * <li>Simple node returns the right values for nodeName, value etc
 * <li>Child nodes can be added and removed correctly
 * </ul>
 *
 * Testing does not check namespace or prefix handling currently
 * <p>
 * Currently this test does not check for event sending. Because JUnit does
 * not deal with asynchronous systems we have no way of really knowing if the
 * event has been sent or not. We can tell if it has arrived, but there is no
 * way to know that it didn't arrive, which is the error case we are looking
 * for.
 */
public class TestNode extends TestCase implements EventListener {

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestNode(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestNode("testBasicValues"));
        suite.addTest(new TestNode("testChildHandling"));

        return suite;
    }

    /**
     * Test the basic node values for a default node. These are name and
     * type information. We choose a node type that is not element or
     * attribute as these modify the default name and values that should be
     * returned.
     */
    public void testBasicValues() {
        Node node = new X3DNode(Node.ENTITY_NODE);

        assertEquals("Types don't match",
                     Node.ENTITY_NODE,
                     node.getNodeType());
        assertEquals("Name not set correctly", "Entity", node.getNodeName());

        assertNull("Value incorrectly set", node.getNodeValue());
        assertNull("Attributes incorrectly set", node.getAttributes());
        assertNull("Owner document is set", node.getOwnerDocument());
        assertNull("Prefix is set", node.getPrefix());
        assertNull("Has parent node", node.getParentNode());
        assertNull("Local name is set", node.getLocalName());
        assertNull("Next sibling set", node.getNextSibling());
        assertNull("Previous sibling set", node.getPreviousSibling());
    }

    /**
     * This test looks at the child handling of this node. With a simple node
     * we add and remove a number of children and check that everything works
     * fine and that we get the number of children. There is no checking for
     * events being set at this stage.
     */
    public void testChildHandling() {

        // Doesn't really matter here what we create
        X3DNode parent = new X3DNode(Node.ENTITY_NODE);
        NodePath parent_path = new NodePath(new EventTarget[] { parent });
        parent.setTargetPath(parent_path);

        NodeList kid_list = parent.getChildNodes();

        assertEquals("Initial kid list is not empty", 0, kid_list.getLength());

        // An array of children we create
        int num_kids = 5;
        X3DNode[] kids = new X3DNode[num_kids];

        for(int i = 0; i < num_kids; i++) {
            kids[i] = new X3DNode(Node.ENTITY_NODE);
            NodePath path = new NodePath(parent_path, kids[i]);
            kids[i].setTargetPath(path);
        }

        // Add a single kid and make sure both the first and last pointers
        // both point to it. NodeList is supposed to be live so we just check
        // it here to make sure.
        parent.appendChild(kids[0]);

        assertTrue("First child is not same", parent.getFirstChild() == kids[0]);
        assertTrue("Last child is not same", parent.getLastChild() == kids[0]);

        NodeList tmp_list = parent.getChildNodes();
        assertEquals("Single child list size wrong", 1, tmp_list.getLength());
        assertEquals("Child list not null", 1, kid_list.getLength());

        // Now add a bunch of them and check same
        for(int i = 1; i < num_kids; i++)
            parent.appendChild(kids[i]);

        assertTrue("First child in list wrong", parent.getFirstChild() == kids[0]);
        assertTrue("Last child in list wrong", parent.getLastChild() == kids[4]);
        assertEquals("Node list is wrong size", 5, kid_list.getLength());

        // Loop through each element in the kid list and make sure same. Use
        // the kid list here directly as the previous test made sure that the
        // ChildList was live with the source node.
        Node kid;
        for(int i = 0; i < num_kids; i++) {
            kid = kid_list.item(i);
            assertTrue("Child " + i + " reference wrong", kid == kids[i]);
            assertTrue("Child " + i + " parent wrong",
                   kid.getParentNode() == parent);
        }

        // Check the sibling handling by looking at the first kid and making
        // sure the next and previous of the list all match.
        kid = kid_list.item(0);
        assertNull("First child previous sibling not null",
                   kid.getPreviousSibling());
        assertNotNull("First child next sibling null", kid.getNextSibling());
        assertTrue("First child next sibling wrong",
               kid.getNextSibling() == kids[1]);

        // Check an arbitary one in the middle to make sure
        kid = kid_list.item(2);
        assertNotNull("Child 2 prev sibling null", kid.getPreviousSibling());
        assertNotNull("Child 2 next sibling null", kid.getNextSibling());

        assertTrue("Child 2 next sibling wrong", kid.getNextSibling() == kids[3]);
        assertTrue("Child 2 prev sibling wrong",
               kid.getPreviousSibling() == kids[1]);

        // Finally check the last sibling to make sure next is null
        kid = kid_list.item(4);
        assertNotNull("Last child previous sibling null",
                      kid.getPreviousSibling());
        assertNull("Last child next sibling not null", kid.getNextSibling());
        assertTrue("Last child prev sibling wrong",
               kid.getPreviousSibling() == kids[3]);
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
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

