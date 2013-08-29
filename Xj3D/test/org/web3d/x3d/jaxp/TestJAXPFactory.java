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

import javax.xml.parsers.*;

import org.w3c.dom.*;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A test case to check the functionality of the various parser capabilities
 * within JAXP using both standard and our custom implementations.
 * <p>
 * The test starts by making sure we can fetch the standard factory and
 * making sure that we can create both DOM and SAX parsers
 * <p>
 * The second test is to make sure that we can create one of our custom
 * parser instances. If that succeeds we then create a few small items
 * and check that the right object types are return.
 * <p>
 * The final test works on a series of test documents of increasing complexity
 * to make sure the parser does not barf. We don't really check for the right
 * structures, more that we can parse correct files without trouble.
 */
public class TestJAXPFactory extends TestCase {

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

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestJAXPFactory(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestJAXPFactory("testStdParser"));
        suite.addTest(new TestJAXPFactory("testCustomParser"));
        suite.addTest(new TestJAXPFactory("testDocuments"));

        return suite;
    }

    /**
     * Setup the environment for this test.
     */
    protected void setUp() {
    }

    /**
     * Check to make sure that we can create the standard parsers from JAXP.
     * We look for both DOM and SAX parsers.
     */
    public void testStdParser() {
        // SAX parser first
        boolean namespace = false;
        boolean validating = false;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            namespace = parser.isNamespaceAware();
            validating = parser.isValidating();

            XMLReader reader = parser.getXMLReader();
            assertNotNull("SAX XMLReader not created", reader);

            // Right, this worked so now create a parser with the
            // opposite options and check we can create that.
            factory.setNamespaceAware(!namespace);
            factory.setValidating(!validating);

            parser = factory.newSAXParser();
            reader = parser.getXMLReader();
            assertNotNull("SAX XMLReader with options not created", reader);

            assertTrue("Validation options different",
                   validating != parser.isValidating());

            assertTrue("Namespace options different",
                   namespace != parser.isNamespaceAware());

        } catch(FactoryConfigurationError fce) {
            fail("SAX: " + fce.getMessage());
        } catch(ParserConfigurationException pcd) {
            fail("SAX: " + pcd.getMessage());
        } catch(SAXException se) {
            fail("SAX: " + se.getMessage());
        }

        // Now do the same thing with DOM.
        try {
            DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            namespace = builder.isNamespaceAware();
            validating = builder.isValidating();

            Document doc = builder.newDocument();
            assertNotNull("DOM document not created", doc);

            // Right, this worked so now create a parser with the
            // opposite options and check we can create that.
            factory.setNamespaceAware(!namespace);
            factory.setValidating(!validating);

            builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            assertNotNull("DOM document with options not created", doc);

            assertTrue("Validation options different",
                   validating != builder.isValidating());

            assertTrue("Namespace options different",
                   namespace != builder.isNamespaceAware());

        } catch(FactoryConfigurationError fce) {
            fail("DOM: " + fce.getMessage());
        } catch(ParserConfigurationException pcd) {
            fail("DOM: " + pcd.getMessage());
        }
    }

    /**
     * Test that we can create a custom instance of our parser. If it can
     * create a factory of the correct type and then produce the right
     * DocumentBuilder instance we then look to the document and other objects
     * that could be created at the top level.
     */
    public void testCustomParser() {
        DocumentBuilder builder = null;

        // first set the system property for our class
        System.setProperty(X3DConstants.JAXP_FACTORY_PROPERTY,
                           X3DConstants.DOM_FACTORY_IMPL);

        try {
            Class cls = Class.forName(X3DConstants.DOM_FACTORY_IMPL);
            cls.newInstance();
        } catch (IllegalAccessException iae) {
            fail("Failed to access factory class");
        } catch (InstantiationException ie) {
            fail("Failed to create factory class");
        } catch(ClassNotFoundException cnfe) {
            fail("Failed to find factory class");
        }

        try{
            DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

            assertTrue("Custom factory not used",
                   factory instanceof X3DDocumentBuilderFactory);

            builder = factory.newDocumentBuilder();

            assertTrue("Custom builder not used ",
                   builder instanceof X3DDocumentBuilder);

        } catch(FactoryConfigurationError fce) {
            // System.out.println(fce.getException().getMessage());
            // fce.printStackTrace();
            fail(fce.getException().getMessage());
        } catch(ParserConfigurationException pcd) {
            // System.out.println(pcd.getMessage());
            // pcd.printStackTrace();
            fail(pcd.getMessage());
        }

        // So we can create our custom factory. Now let's see how we go
        // creating a simple document based on the factory.
        String classname;
        Document doc = builder.newDocument();
        classname = doc.getClass().toString();
        assertNotNull("No document returned", doc);
        assertTrue("Custom document not used " + classname,
               classname.equals("org.web3d.x3d.jaxp.dom.X3DDocument"));

        // Test a few of the basic items
        DocumentType doctype = doc.getDoctype();
        classname = doctype.getClass().toString();
        assertNotNull("DocType is null", doctype);
        assertTrue("Custom DocType not used",
               classname.equals("org.web3d.x3d.jaxp.dom.X3DDocumentType"));

        Element root = doc.getDocumentElement();
        assertNull("Root element is not null", root);

        DOMImplementation impl = doc.getImplementation();
        classname = impl.getClass().toString();
        assertNotNull("Implementation is null", impl);
        assertTrue("Custom DOMImpl not used",
               classname.equals("org.web3d.x3d.jaxp.dom.X3DImplementation"));
    }

    /**
     * Test the custom parser on a series of documents. We loop through the
     * list of test documents and build each one. We check that the parser
     * doesn't throw any exceptions and that a document object is returned.
     * We also check for the basic root node etc.
     */
    public void testDocuments() {
        // first set the system property for our class
        System.setProperty(X3DConstants.JAXP_FACTORY_PROPERTY,
                           X3DConstants.DOM_FACTORY_IMPL);

        DocumentBuilder builder = null;

        try{
            DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

            builder = factory.newDocumentBuilder();

            builder.setEntityResolver(new X3DEntityResolver());
        } catch(FactoryConfigurationError fce) {
            fail(fce.getException().getMessage());
        } catch(ParserConfigurationException pcd) {
            fail(pcd.getMessage());
        }

        // First try parsing the empty document so that we can check just
        // basic parsing works
        InputSource source = findFile(BASIC_DOC_FILE);
        assertNotNull("Couldn't find basic file", source);

        try {
            Document doc = builder.parse(source);
        } catch(IOException ioe) {
            fail("Error with basic file stream");
        } catch(SAXException se) {
            Exception ex = se.getException();
            String msg = null;

            if(ex == null) {
            msg = se.getMessage();
                se.printStackTrace();
            } else {
            msg = ex.getMessage();
                ex.printStackTrace();
            }

            fail("Basic file: " + msg);
        }
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

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

