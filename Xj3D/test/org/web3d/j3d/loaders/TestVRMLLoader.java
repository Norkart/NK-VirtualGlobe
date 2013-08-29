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

package org.web3d.j3d.loaders;

// Standard imports
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Application specific imports
import org.web3d.vrml.lang.UnsupportedProfileException;

/**
 * A test case to check the functionality of the VRML97Reader implementation.
 * <p>
 *
 */
public class TestVRMLLoader extends TestCase {

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestVRMLLoader(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestVRMLLoader("simple"));

        return suite;
    }

    /**
     * Provide initialisation of this test instance.
     */
    public void setUp() {
    }

    /**
     * Test the parser based on empty worlds that only contain a header line
     */
    public void simple() {
        VRML97Loader vl = new VRML97Loader();
        try {
            vl.load("test.wrl");
        } catch(Exception e) {
            e.printStackTrace();
            fail("loader exception: " + e.getMessage());
        }
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

