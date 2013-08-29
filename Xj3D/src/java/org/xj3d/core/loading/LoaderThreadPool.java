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

package org.xj3d.core.loading;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.List;

// Local imports
import org.web3d.util.BlockingQueue;
import org.web3d.util.ErrorReporter;
import org.web3d.util.Queue;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLNodeListener;

/**
 * A local singleton that manages all of the threads used to load content.
 * <p>
 *
 * The singleton loads a number of threads that must use a common map and
 * queue instance for all users. To organise and queue inputs to this class
 * we must first fetch the instance that is running and then query it for
 * the map and queue. Once you have access to these, you are free to use them
 * directly to add or remove items from the general pool.
 * <p>
 * The number of threads can be controlled at startup of the application by
 * setting a system property. If the property defines a value less than or
 * equal to zero it is ignored and the default number of threads are loaded
 * - 5.
 * <p>
 *
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.xj3d.core.loading.threads</code> The number of
 *    concurrent threads to be started to do loading.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class LoaderThreadPool {

    /** Property describing the number of threads to start */
    private static final String THREAD_COUNT_PROP =
        "org.xj3d.core.loading.threads";

    /** The default number of threads to start if none are defined */
    private static int DEFAULT_THREAD_COUNT;

    /** The shared queue that all threads share */
    private ContentLoadQueue pending;

    /** The map of objects working in progress */
    private Map inProgress;

    /** List of all the threads we're handling */
    private ContentLoader[] loaders;

    /** The shared singleton instance of this class */
    private static LoaderThreadPool threadPool;

    /** The shared thread group to assign all the threads to */
    private static ThreadGroup threadGroup;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LoaderThreadPool() {
        pending = new ContentLoadQueue();
        inProgress = Collections.synchronizedMap( new HashMap() );

        if(threadGroup == null)
            threadGroup = new ThreadGroup("Xj3D Content Loaders");

        // fetch the system property defining the values
        Integer prop = (Integer)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    int num_processors = Runtime.getRuntime().availableProcessors();

                    DEFAULT_THREAD_COUNT = (num_processors == 1) ? 1 : (num_processors - 1);

                    // privileged code goes here, for example:
                    return Integer.getInteger(THREAD_COUNT_PROP,
                        DEFAULT_THREAD_COUNT);
                }
            }
            );

        int size = prop.intValue();
        if(size <= 0)
            size = DEFAULT_THREAD_COUNT;

        if (size != DEFAULT_THREAD_COUNT)
            System.out.println(THREAD_COUNT_PROP + " set to: " + size);
        loaders = new ContentLoader[size];

        for(int i = size; --i >= 0;) {
            loaders[i] = new ContentLoader(threadGroup, pending, inProgress);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Fetch the singleton instance of this class. If there is not an instance
     * running, one is created.
     *
     * @return The shared instance of this class
     */
    public static LoaderThreadPool getLoaderThreadPool() {
        if(threadPool == null)
            threadPool = new LoaderThreadPool();

        return threadPool;
    }

    /**
     * Set the global thread group that we want these content handlers to
     * run under. This can only be set once and must be before the first
     * instance has been created.
     *
     * @param tg The thread group to use
     * @throws IllegalStateException Too late or it has been set already
     */
    public static void setThreadGroup(ThreadGroup tg)
        throws IllegalStateException {

        if(threadGroup != null)
            throw new IllegalStateException("Cannot assign thread group now");

        threadGroup = tg;
    }

    /**
     * Get the thread group that these objects belong to. If one has not been
     * set yet then this will return null.
     *
     * @return The ThreadGroup holding these objects
     */
    public static ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        for(int i = 0; i < loaders.length; i++)
            loaders[i].setErrorReporter(reporter);
    }

    /**
     * Get the working queue of items yet to be processed. The queue contains
     * the instances of LoadRequest objects that are to be processed.
     *
     * @return The working queue of objects
     */
    public ContentLoadQueue getWaitingList() {
        return pending;
    }

    /**
     * Get the working map of in-progess loading. The map contains the current
     * external node instance as the key and the LoadRequest that is handling
     * it as the value.
     *
     * @return The working map of items
     */
    public Map getProgressMap() {
        return inProgress;
    }

    /**
     * Force a clearing of the files currently being loaded by the content
     * loaders. Used when a new world is about to be loaded and the existing
     * pending loads need to be terminated.
     */
    public void clear() {
        pending.purge();

        for(int i = loaders.length; --i >=0; )
            loaders[i].abortCurrentFile();
    }

    /**
     * Force a shutdown of the entire thread pool. All threads will be
     * forcefully terminated
     */
    public synchronized void shutdown() {
        // this will be called multiple times at shutdown
        if ( threadPool != null ) {
            for( int i = loaders.length; --i >= 0; ) {
                // inform the loader threads to exit
                loaders[i].shutdown();
                loaders[i] = null;
            }
            // clear the load queue and force a release of any waiting threads
            pending.purge();
        }
        threadPool = null;
    }

    /**
     * Ensure all threads are running.
     */
    public void restartThreads() {
        int size = loaders.length;
        for(int i = size; --i >= 0;) {
            if(!loaders[i].isAlive())
                loaders[i] = new ContentLoader(threadGroup,
                pending,
                inProgress);
        }
    }
}
