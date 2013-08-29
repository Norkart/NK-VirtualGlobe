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

package org.web3d.util;

// Standard imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Application specific imports

/**
 * A test case to check the functionality of the ArrayUtils implementation.
 * <p>
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class TestArrayUtils extends TestCase {
    // Test Arrays

    float[] a1 = {0,1,2,3,4,5,6,7,8,9,10,11};
    float[][] a12 = {{0,1}, {2,3},{4,5},{6,7},{8,9},{10,11}}; 
    float[][] a13 = {{0,1,2},{3,4,5},{6,7,8},{9,10,11}};    
    float[][] a14 = {{0,1,2,3},{4,5,6,7},{8,9,10,11}};  
    double[] aD1 = {0,1,2,3,4,5,6,7,8,9,10,11};
    double[][] aD14 = {{0,1,2,3},{4,5,6,7},{8,9,10,11}};  

    // Scratch vars
    int num;
    float[] flattened;
    float[][] raised;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestArrayUtils(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestArrayUtils("test2"));
        suite.addTest(new TestArrayUtils("test3"));
        suite.addTest(new TestArrayUtils("test4"));
        suite.addTest(new TestArrayUtils("testN"));
        suite.addTest(new TestArrayUtils("testDouble"));

        return suite;
    }

    /**
     * Provide initialisation of this test instance.
     */
    public void setUp() {
    }

    /**
     * Test the routines using size 2 vectors.
     */
    public void test2() {
        num=2;
        raised = new float[a1.length / num][num];
        ArrayUtils.raise2(a1, a1.length / num, raised);

        for(int i=0; i < a1.length / num; i++) {
            for(int j=0; j < num; j++) {
                assertTrue("Invalid at: " + i + "," + j + " " + raised[i][j],
                    a12[i][j] == raised[i][j]);
            }
        }

        flattened = new float[a1.length];

        ArrayUtils.flatten2(a12, a1.length / num, flattened);
        for(int i=0; i < a1.length; i++) {
            assertTrue("Invalid at : " + i + " " + flattened[i], 
                a1[i] == flattened[i]);
        }
    }

    /**
     * Test the routines using size 3 vectors.
     */
    public void test3() {
        num=3;
        raised = new float[a1.length / num][num];
        ArrayUtils.raise3(a1, a1.length / num, raised);

        for(int i=0; i < a1.length / num; i++) {
            for(int j=0; j < num; j++) {
                assertTrue("Invalid at: " + i + "," + j + " " + raised[i][j],
                    a13[i][j] == raised[i][j]);
            }
        }

        flattened = new float[a1.length];

        ArrayUtils.flatten3(a13, a1.length / num, flattened);
        for(int i=0; i < a1.length; i++) {
            assertTrue("Invalid at : " + i + " " + flattened[i], 
                a1[i] == flattened[i]);
        }
    }

    /**
     * Test the routines using size 4 vectors.
     */
    public void test4() {
        num=4;
        raised = new float[a1.length / num][num];
        ArrayUtils.raise4(a1, a1.length / num, raised);

        for(int i=0; i < a1.length / num; i++) {
            for(int j=0; j < num; j++) {
                assertTrue("Invalid at: " + i + "," + j + " " + raised[i][j],
                    a14[i][j] == raised[i][j]);
            }
        }

        flattened = new float[a1.length];

        ArrayUtils.flatten4(a14, a1.length / num, flattened);
        for(int i=0; i < a1.length; i++) {
            assertTrue("Invalid at : " + i + " " + flattened[i], 
                a1[i] == flattened[i]);
        }
    }

    /**
     * Test the routines using size N vectors.
     */
    public void testN() {
        num=4;
        raised = new float[a1.length / num][num];
        ArrayUtils.raiseN(a1, a1.length / num, num, raised);

        for(int i=0; i < a1.length / num; i++) {
            for(int j=0; j < num; j++) {
                assertTrue("Invalid at: " + i + "," + j + " " + raised[i][j],
                    a14[i][j] == raised[i][j]);
            }
        }

        flattened = new float[a1.length];

        ArrayUtils.flattenN(a14, a1.length / num, num, flattened);
        for(int i=0; i < a1.length; i++) {
            assertTrue("Invalid at : " + i + " " + flattened[i], 
                a1[i] == flattened[i]);
        }
    }

    /**
     * Test the routines using size N vectors.
     */
    public void testDouble() {
        double[] flattenedD;
        double[][] raisedD;

        num=4;
        raisedD = new double[aD1.length / num][num];
        ArrayUtils.raise4(aD1, aD1.length / num, raisedD);

        for(int i=0; i < aD1.length / num; i++) {
            for(int j=0; j < num; j++) {
                assertTrue("Invalid at: " + i + "," + j + " " + raisedD[i][j],
                    aD14[i][j] == raisedD[i][j]);
            }
        }

        flattenedD = new double[aD1.length];

        ArrayUtils.flattenN(aD14, aD1.length / num, num, flattenedD);
        for(int i=0; i < aD1.length; i++) {
            assertTrue("Invalid at : " + i + " " + flattenedD[i], 
                aD1[i] == flattenedD[i]);
        }
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

