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

package org.web3d.x3d.jaxp;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A test case to check the functionality of the EntityResolver
 * implementation.
 * <p>
 * The test case passes a number of different URIs to the entity
 * resolver to see if an InputSource is returned correctly or not.
 *
 */
public class TestEntityResolver extends TestCase {

    /** The list of valid URI types to test */
    private static final String[] VALID_URI = {
	"x3d.dtd",
	"x3d-compact.dtd",
	"urn:web3d:x3d:/x3d.dtd",
	"urn:web3d:x3d:/x3d-compact.dtd",
	"http://www.web3d.org/Specifications/X3D/x3d.dtd",
	"http://www.web3d.org/Specifications/X3D/x3d-compact.dtd"
    };

    /** A list of invalid URIs to test */
    private static final String[] INVALID_URI = {
	"test.dtd",
	"test-x3d.dtd",
	"urn:web3d:x3d:x3d.dtd",
	"urn:web3d:x3d:x3d-compact.dtd",
	"http://www.web3d.org/Specifications/X3D/test.dtd"
    };

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestEntityResolver(String name) {
	super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new TestEntityResolver("testValid"));
	suite.addTest(new TestEntityResolver("testInvalid"));

	return suite;
    }

    /**
     * Test the resolver with a number of valid DTD URIs. These should all
     * return an instance of InputSource in order to pass.
     */
    public void testValid() {
	EntityResolver resolver = new X3DEntityResolver();

	for(int i = 0; i < VALID_URI.length; i++) {
	    try {
		InputSource is = resolver.resolveEntity(null, VALID_URI[i]);
		assertNotNull("Input was; " + VALID_URI[i], is);
	    } catch(SAXException se) {
		fail(se.getMessage());
	    } catch(IOException ioe) {
		fail(ioe.getMessage());
	    }
	}
    }

    /**
     * Test the resolver with a number of valid DTD URIs. These should all
     * return an instance of InputSource in order to pass.
     */
    public void testInvalid() {
	EntityResolver resolver = new X3DEntityResolver();

	for(int i = 0; i < INVALID_URI.length; i++) {
	    try {
		InputSource is = resolver.resolveEntity(null, INVALID_URI[i]);
		assertNull("Input was; " + INVALID_URI[i], is);
	    } catch(SAXException se) {
		fail(se.getMessage());
	    } catch(IOException ioe) {
		fail(ioe.getMessage());
	    }
	}
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
	TestRunner.run(suite());
    }
}

