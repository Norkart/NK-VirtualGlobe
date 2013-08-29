/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.loading;

// External imports
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Local imports
import org.web3d.util.BlockingQueue;
import org.web3d.util.Queue;
import org.web3d.vrml.nodes.VRMLExternalNodeType;


/**
 * A test case to check the functionality of the ContentLoader implementation.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TestContentLoader extends TestCase {

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestContentLoader(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestContentLoader("testThreads"));

        return suite;
    }

    /**
     * Test the content loader in it's basic thread handling to make sure
     * it does not exit until we tell it to.
     */
    public void testThreads() {
        Map progress_map = new HashMap();
        BlockingQueue queue = new BlockingQueue();
        ThreadGroup tg = new ThreadGroup("TestContentLoader group");

        ContentLoader loader = new ContentLoader(tg, queue, progress_map);

        // now check the loader exists in the thread group
        assertEquals("Wrong number of active threads", 1, tg.activeCount());

        Thread[] thread_list = new Thread[1];
        tg.enumerate(thread_list);

        assertTrue("Loader thread is not active", thread_list[0].isAlive());

        // Now kill the thread and check that it is not active
        loader.shutdown();
        queue.purge();

        // Just yield to make sure this group is running
        Thread.yield();

        assertTrue("Loader thread is still active", !thread_list[0].isAlive());
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

