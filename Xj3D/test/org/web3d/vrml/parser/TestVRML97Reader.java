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

package org.web3d.vrml.parser;

// Standard imports
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Application specific imports
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * A test case to check the functionality of the VRML97Reader implementation.
 * <p>
 *
 */
public class TestVRML97Reader extends TestCase {

    /** List of empty world test cases. Files relative to classpath */
    private static final String[] EMPTY_WORLDS = {
        "parsetest/vrml97/empty1.wrl",
        "parsetest/vrml97/empty2.wrl",
        "parsetest/vrml97/empty3.wrl",
    };

    /** List of empty worlds with bad header formatting */
    private static final String[] HEADER_WORLDS = {
        "parsetest/vrml97/bad_header1.wrl",
        "parsetest/vrml97/bad_header2.wrl",
        "parsetest/vrml97/bad_header3.wrl",
        "parsetest/vrml97/bad_header4.wrl",
        "parsetest/vrml97/bad_header5.wrl",
    };

    /** List of worlds that contain a single node in different decl orders */
    private static final String[] SINGLE_WORLDS = {
        "parsetest/vrml97/single_node1.wrl",
        "parsetest/vrml97/single_node2.wrl",
        "parsetest/vrml97/single_node3.wrl",
        "parsetest/vrml97/single_node4.wrl",
    };

    /** List of worlds to test field values */
    private static final String[] FIELD_WORLDS = {
        "parsetest/vrml97/field1.wrl",
    };

    /** List of worlds that contain a SFNodes syntaxes */
    private static final String[] SFNODE_WORLDS = {
        "parsetest/vrml97/sfnode1.wrl",
        "parsetest/vrml97/sfnode2.wrl",
        "parsetest/vrml97/sfnode3.wrl",
        "parsetest/vrml97/sfnode4.wrl",
    };

    /** List of worlds that contain a MFNodes syntaxes */
    private static final String[] MFNODE_WORLDS = {
        "parsetest/vrml97/mfnode1.wrl",
        "parsetest/vrml97/mfnode2.wrl",
        "parsetest/vrml97/mfnode3.wrl",
        "parsetest/vrml97/mfnode4.wrl",
        "parsetest/vrml97/mfnode5.wrl",
    };

    /** List of worlds that contain DEF/USE combos */
    private static final String[] DEF_WORLDS = {
        "parsetest/vrml97/field2.wrl",
        "parsetest/vrml97/field3.wrl",
    };

    /** List of worlds that contain a Combo syntaxes */
    private static final String[] COMBO_WORLDS = {
        "parsetest/vrml97/combo1.wrl",
        "parsetest/vrml97/combo2.wrl",
        "parsetest/vrml97/combo3.wrl",
    };

    /** List of worlds that contain a PROTO syntaxes */
    private static final String[] PROTO_WORLDS = {
        "parsetest/vrml97/proto1.wrl",
        "parsetest/vrml97/proto2.wrl",
        "parsetest/vrml97/proto3.wrl",
        "parsetest/vrml97/proto4.wrl",
        "parsetest/vrml97/proto5.wrl",
        "parsetest/vrml97/proto6.wrl",
        "parsetest/vrml97/proto7.wrl",
        "parsetest/vrml97/proto8.wrl",
        "parsetest/vrml97/proto9.wrl",
    };

    /** List of worlds that contain a PROTO syntaxes */
    private static final String[] EXTERNPROTO_WORLDS = {
        "parsetest/vrml97/externproto1.wrl",
        "parsetest/vrml97/externproto2.wrl",
        "parsetest/vrml97/externproto3.wrl",
    };

    /** List of the worlds that contain scripts */
    private static final String[] SCRIPT_WORLDS = {
        "parsetest/vrml97/script1.wrl",
        "parsetest/vrml97/script2.wrl",
        "parsetest/vrml97/script3.wrl",
        "parsetest/vrml97/script4.wrl",
        "parsetest/vrml97/script5.wrl",
    };

    /** The reader to use in each test case */
    private VRML97Reader reader;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestVRML97Reader(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestVRML97Reader("testEmptyWorld"));
        suite.addTest(new TestVRML97Reader("testBadHeaders"));
        suite.addTest(new TestVRML97Reader("testFields"));
        suite.addTest(new TestVRML97Reader("testSingleNode"));
        suite.addTest(new TestVRML97Reader("testSFNodes"));
        suite.addTest(new TestVRML97Reader("testMFNodes"));
        suite.addTest(new TestVRML97Reader("testDEFs"));
        suite.addTest(new TestVRML97Reader("testComboNodes"));
        suite.addTest(new TestVRML97Reader("testProtos"));
        suite.addTest(new TestVRML97Reader("testExternprotos"));
        suite.addTest(new TestVRML97Reader("testScripts"));

        return suite;
    }

    /**
     * Provide initialisation of this test instance.
     */
    public void setUp() {
        reader = new VRML97Reader();
    }

    /**
     * Test the parser based on empty worlds that only contain a header line
     */
    public void testEmptyWorld() {
        testGoodFiles(EMPTY_WORLDS);
    }

    /**
     * Test the parser based on empty worlds with invalid a header lines
     */
    public void testBadHeaders() {
        testBadFiles(HEADER_WORLDS);
    }

    /**
     * Test the parser based on worlds that only contain a field values and
     * no SF or MFNode declarations.
     */
    public void testFields() {
        testGoodFiles(FIELD_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain a single node declaration.
     */
    public void testSingleNode() {
        testGoodFiles(SINGLE_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain a single node declaration.
     */
    public void testSFNodes() {
        testGoodFiles(SFNODE_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain a single node declaration.
     */
    public void testMFNodes() {
        testGoodFiles(MFNODE_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain a single node declaration.
     */
    public void testDEFs() {
        testGoodFiles(DEF_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain a single node declaration.
     */
    public void testComboNodes() {
        testGoodFiles(COMBO_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain protos
     */
    public void testProtos() {
        testGoodFiles(PROTO_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain externprotos
     */
    public void testExternprotos() {
        testGoodFiles(EXTERNPROTO_WORLDS);
    }

    /**
     * Test the parser based on worlds that contain scripts
     */
    public void testScripts() {
        testGoodFiles(SCRIPT_WORLDS);
    }

    /**
     * Internal convenience method to test good files that should always
     * parse.
     *
     * @param fileList The array of files to test
     */
    private void testGoodFiles(String[] fileList) {
        assertNotNull("Null file list to test", fileList);
        assertTrue("Empty file list to test", fileList.length > 0);

        for(int i = 0; i < fileList.length; i++) {
            URL url = ClassLoader.getSystemResource(fileList[i]);

            assertNotNull("Could not find test file: " + fileList[i], url);
            InputSource is = new InputSource(url);

            try {
                reader.parse(is);
            } catch(Exception e) {
                e.printStackTrace();
                fail("Failed for world: " +
                     fileList[i] +
                     "\n" +
                     e.getClass());
            }
        }
    }

    /**
     * Internal convenience method to test bad files that should always
     * generate parsing exceptions.
     *
     * @param fileList The array of files to test
     */
    private void testBadFiles(String[] fileList) {
        assertNotNull("Null bad file list to test", fileList);
        assertTrue("No bad world files to test", fileList.length > 0);

        for(int i = 0; i < fileList.length; i++) {
            URL url = ClassLoader.getSystemResource(fileList[i]);

            assertNotNull("Could not find test file: " + fileList[i], url);
            InputSource is = new InputSource(url);

            try {
                reader.parse(is);
                fail("Failed for world: " + fileList[i]);
            } catch(VRMLParseException vpe) {
                // we are supposed to get this exception
            } catch(Exception e) {
                // We are not supposed to get this one!
                fail("Failed for world: " +
                     fileList[i] +
                     "\n" +
                     e.getMessage());
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

