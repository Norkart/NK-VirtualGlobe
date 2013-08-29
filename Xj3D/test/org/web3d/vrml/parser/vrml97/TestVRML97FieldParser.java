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

package org.web3d.vrml.parser.vrml97;

// Standard imports
import java.net.URL;
import java.io.StringReader;
import java.io.InputStream;

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
public class TestVRML97FieldParser extends TestCase {

    /** Test strings for SFInt32 */
    private static final String[] SFINT = {
        "1", "-3", "-0", "23435302"
    };

    /** Correct values for SFInt32 */
    private static final int[] ANS_SFINT = {1, -3, 0, 23435302};

    /** Test strings for SFFloat */
    private static final String[] SFFLOAT = {
        "1", "1.5"
    };

    /** Correct values for SFFloat */
    private static final float[] ANS_SFFLOAT = {1f,1.5f};

    /** Test strings for SFBool */
    private static final String[] SFBOOL = {
        "TRUE", "FALSE",
    };

    /** Correct values for SFBool */
    private static final boolean[] ANS_SFBOOL = {true, false};

    /** Test strings for SFVec3f */
    private static final String[] SFVEC3F = {
        "1 2 3" ,
        "1.0 2.0 3.5"
    };

    /** Correct values for SFVec3f */
    private static final float[][] ANS_SFVEC3F = {
        {1f,2f,3f},
        {1f,2f,3.5f}
    };

    /** Test strings for MFFloat */
    private static final String[] MFFLOAT = {
        "1 2 3" ,
        "1.0 2.0 3.5",
        "[1.0 2.0 3.5 ]",
        "1.0 3.5 0 -1 3.14"
    };

    /** Correct values for MFFloat */
    private static final float[][] ANS_MFFLOAT = {
        {1f,2f,3f},
        {1f,2f,3.5f},
        {1f,2f,3.5f},
        {1f,3.5f,0, -1,3.14f}
    };

    /** The parser instance we are testing */
    private VRML97FieldParser fieldParser;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestVRML97FieldParser(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestVRML97FieldParser("testSFInt32"));
        suite.addTest(new TestVRML97FieldParser("testSFBool"));
        suite.addTest(new TestVRML97FieldParser("testSFFloat"));
        suite.addTest(new TestVRML97FieldParser("testSFVec3f"));
        suite.addTest(new TestVRML97FieldParser("testMFFloat"));

        return suite;
    }

    /**
     * Provide initialisation of this test instance.
     */
    public void setUp() {
        fieldParser = new VRML97FieldParser(new StringReader(""));
    }

    /**
     * Test parsing of SFBool strings
     */
    public void testSFBool() {
         boolean vf;

        for (int i=0; i < SFBOOL.length; i++) {
            StringReader input = new StringReader(SFBOOL[i]);

            fieldParser.ReInit(input);

            try {
                vf = fieldParser.SFBool();

                assertEquals("Incorrect Boolean ", ANS_SFBOOL[i], vf);
            } catch(ParseException pe) {
                fail(pe.getMessage());
            }
        }
    }

    /**
     * Test parsing of SFInt32 strings
     */
    public void testSFInt32() {
        int vf;

        for (int i=0; i < SFINT.length; i++) {
            StringReader input = new StringReader(SFINT[i]);

            fieldParser.ReInit(input);

            try {
                vf = fieldParser.SFInt32();

                assertEquals("Incorrect SFInt32 ", ANS_SFINT[i], vf);
            } catch(ParseException pe) {
                fail(pe.getMessage());
            }
        }
    }

    /**
     * Test parsing of SFFloat strings
     */
    public void testSFFloat() {
        float vf;

        for (int i=0; i < SFFLOAT.length; i++) {
            StringReader input = new StringReader(SFFLOAT[i]);

            fieldParser.ReInit(input);

            try {
                vf = fieldParser.SFFloat();

                assertEquals("Incorrect SFFloat ", ANS_SFFLOAT[i], vf, 0);
            } catch(ParseException pe) {
                fail(pe.getMessage());
            }
        }
    }

    /**
     * Test parsing of SFVec3F strings
     */
    public void testSFVec3f() {
        float vf[];

        for (int i=0; i < SFVEC3F.length; i++) {
            StringReader input = new StringReader(SFVEC3F[i]);

            fieldParser.ReInit(input);

            try {
                vf = fieldParser.SFVec3f();

                assertEquals("Wrong return size", 3, vf.length);
                assertEquals("Incorrect X component", ANS_SFVEC3F[i][0], vf[0], 0);
                assertEquals("Incorrect Y component", ANS_SFVEC3F[i][1], vf[1], 0);
                assertEquals("Incorrect Z component", ANS_SFVEC3F[i][2], vf[2], 0);

            } catch(ParseException pe) {
                fail(pe.getMessage());
            }
        }
    }

    /**
     * Test parsing of MFFloat strings
     */
    public void testMFFloat() {
        float vf[];

        for (int i=0; i < MFFLOAT.length; i++) {
            StringReader input = new StringReader(MFFLOAT[i]);

            fieldParser.ReInit(input);

            try {
                vf = fieldParser.MFFloat();

                assertEquals("Wrong return size",
                             ANS_MFFLOAT[i].length,
                             vf.length);

                for(int j = 0; j < ANS_MFFLOAT[i].length; j++)
                    assertEquals("Incorrect component item " + j,
                                 ANS_MFFLOAT[i][j],
                                 vf[j],
                                 0);
            } catch(ParseException pe) {
                fail(pe.getMessage());
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

