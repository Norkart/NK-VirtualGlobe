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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * The global test case collector for this package.
 * <p>
 *
 * Collects together all of the test cases in this package and runs them
 * in the specified order.
 */
public class TestAll extends TestCase {

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestAll(String name) {
    super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestNode.class);
    suite.addTestSuite(TestAttribute.class);
    suite.addTestSuite(TestElement.class);
    suite.addTestSuite(TestX3DDocument.class);

    return suite;
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
    TestRunner.run(suite());
    }
}

