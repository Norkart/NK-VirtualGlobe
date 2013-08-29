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
package org.web3d.x3d.dom.j3d;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

//import org.web3d.vrml.j3d.J3DScene;

import java.awt.*;

/**
 * A test case to check the functionality of the DOMtoJ3D
 * implementation.
 * <p>
 * The test case tests a bunch of xml files and looks at the created J3D
 *
 */
public class TestDOMtoJ3D extends TestCase {
    /** File name of a simple test document */
    private static final String BASIC_DOC_FILE = "empty.xml";

    /**
     * A list of test files to test the parsing. These increase in
     * complexity as you go down the list.
     */
    private static final String[] TEST_DOCS = {
        "group.xml",
        "shape.xml"
    };

    /** File name of a test document with every node and every field */
    private static final String EVENTS_DOC_FILE = "events.xml";

    static boolean valError;
    static String valErrorMsg;

    private Document doc;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestDOMtoJ3D(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestDOMtoJ3D("testBasic"));
        suite.addTest(new TestDOMtoJ3D("testDocuments"));
        suite.addTest(new TestDOMtoJ3D("testEvents"));
        return suite;
    }

    /**
     * Initialize DOM parsing, J3D and browser
     *
     * @param filename The X3D file to test
     */
    private void init(String filename) {
    }

    /**
     * Test the basic initialization, parsing and rendering of a simple file
     */
    public void testBasic() {
/*
        J3DScene sceneNode;

        init(BASIC_DOC_FILE);
        try {
            DOMtoJ3D domtoj3d = new DOMtoJ3D();
            sceneNode = domtoj3d.convertDoc(doc);
        } catch (DOMtoJ3DException e) {
            fail(e.getMessage());
            return;
        }

        assertTrue(valErrorMsg,!valError);
        assertNotNull("Empty scenegraph", sceneNode);
*/
    }

    /**
     * Test a series of increasingly difficult documentes.  We loop through
     * a list of test documents and convert each one.  We check that the
     * DOMtoJ3D process creates no exceptions and that the scenegraphs root
     * node is created.
     */
    public void testDocuments() {
/*
    Scene sceneNode=null;

    for(int i=0; i < TEST_DOCS.length; i++) {
        valError=false;
        init(TEST_DOCS[i]);

        try {
            DOMtoJ3D domtoj3d = new DOMtoJ3D(browser);
            sceneNode = domtoj3d.convertDoc(doc);
            } catch (DOMtoJ3DException e) {
            fail(e.getMessage());
        }

        assertTrue(valErrorMsg,!valError);
        assertNotNull("Empty scenegraph on: " + TEST_DOCS[i], sceneNode);
    }
*/
    }

    /**
     * Test that DOM level 2 events are being handled correctly
     *
     */
    public void testEvents() {
/*
    Scene sceneNode;

    init(EVENTS_DOC_FILE);
    try {
        DOMtoJ3D domtoj3d = new DOMtoJ3D(browser);
        sceneNode = domtoj3d.convertDoc(doc);
        } catch (DOMtoJ3DException e) {
        fail(e.getMessage());
        return;
    }

    assertTrue(valErrorMsg,!valError);
    assertNotNull("Empty scenegraph", sceneNode);
*/
/*
    Element node1 = sceneNode.getElementById("Appearance1");
    Element node2 = sceneNode.getElementById("Appearance2");
*/
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();

            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
        valError=true;
        valErrorMsg = "Warning: " + getParseExceptionInfo(spe);
        }

        public void error(SAXParseException spe) throws SAXException {
        valError=true;
            valErrorMsg = "Error: " + getParseExceptionInfo(spe);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
        valError=true;
            valErrorMsg = "Fatal Error: " + getParseExceptionInfo(spe);
        }
    }
    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());

        // Force Java3D to exit
        System.exit(0);
    }

    // Internal convenience method

    /**
     * Create an InputSource that will present the contents of the named file.
     * The file name should be relative and will be found in the classpath.
     *
     * @param file The name of the file to fetch
     * @return The input source to that file
     */
    private InputSource findFile(String file) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        InputSource ret_val = null;
        InputStream is = cl.getSystemResourceAsStream(file);

        if(is != null)
            ret_val = new InputSource(is);

        return ret_val;
    }
}

