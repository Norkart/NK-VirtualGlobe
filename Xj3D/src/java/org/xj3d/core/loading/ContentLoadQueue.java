/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;

// Local imports
// None


/**
 * Customised queue implementation specifically designed to handle the needs
 * of X3D/VRML external content loading by compressing multi requests for the
 * same URL into a single structure.
 * <p>
 *
 * The queue sorts the incoming requests based on priority. The priority can
 * be defined based on the value of the system property
 * <code>org.xj3d.core.loading.sort.order</code>. See the package documentation
 * for details on the value of this property. This can be changed at runtime
 * and have the queue resorted by calling the {@link #requestResort()} method
 * called on this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class ContentLoadQueue {

    /** Message when the URL to be loaded is either null or zero length */
    private static final String NULL_URL_MSG =
        "The URL provided to the content load queue did not contain anything.";

    /** Message when the type constant is null */
    private static final String NULL_TYPE_MSG =
        "The node type constant was null. A valid type is needed.";

    /** A flag to indicate the class is currently undergoing a purge */
    private boolean purging;

    /** Count of threads waiting in getNext, used to ensure that a
     *  purge completes before resetting the purge flag */
    private int numberOfWaitingThreads = 0;
    
    /** List of LoadDetails to be loaded for this URL. */
    private TreeSet loadQueue;

    /** The priority sorter for the loading queue. */
    private LoadPriorityComparator sorter;

    /**
     * The current set of URLs that are in the queue mapped to their object
     * representation in LoadRequest.
     */
    private HashMap detailsToRequestMap;

    /**
     * Create a new instance of this class. Package private to prevent direct
     * instantiation.
     */
    ContentLoadQueue() {
        sorter = new LoadPriorityComparator();
        loadQueue = new TreeSet(sorter);
        detailsToRequestMap = new HashMap();
        purging = false;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add the given load request onto the queue. If the URL is already sitting
     * on the queue, the details will be inserted into the existing load
     * request so that they may all be serviced at once.
     *
     * @param type The node type classification. One of the defined constants in
     *    {@link LoadConstants}.
     * @param url The urls of the item to load
     * @param handler The class that will process the load request once it is
     *    pulled grabbed from the queue for processing
     * @param details Detail set for what should be loaded
     * @throws IllegalArgumentException Either the URL list was null/zero length,
     *   or the type was null
     */
    public synchronized void add(String type,
                                 String[] url,
                                 LoadRequestHandler handler,
                                 LoadDetails details) {

        if((url == null) || (url.length == 0))
            throw new IllegalArgumentException(NULL_URL_MSG);

        LoadRequest req = new LoadRequest();
        req.url = url;
        req.handler = handler;
        req.type = type;

        synchronized(loadQueue) {
            if (loadQueue.contains(req)) {
                SortedSet sset = loadQueue.tailSet(req);

                req = (LoadRequest) sset.first();

                req.loadList.add(details);
            } else {
                req.loadList.add(details);
                loadQueue.add(req);
            }

            detailsToRequestMap.put(details, req);
        }

        notify();
    }

    /**
     * Get the next item from the queue. Block until an item is available.
     *
     * @return The next item on the queue
     */
    public synchronized LoadRequest getNext() {
        LoadRequest o = null;
        
        while(!purging && o == null) {
            while(!purging && loadQueue.isEmpty()) {
                numberOfWaitingThreads++;
                try {
                    synchronized(this) {
                        wait();
                    }
                } catch(InterruptedException e) {
                }
                numberOfWaitingThreads--;
            }

            synchronized(loadQueue) {
                if(!loadQueue.isEmpty()) {
                    o = (LoadRequest)loadQueue.first();
                    loadQueue.remove(o);
					
					int num_details = o.loadList.size();
					for (int i = 0; i < num_details; i++) {
						detailsToRequestMap.remove(o.loadList.get(i));
					}
                }
            }
        }
        
        if ( purging && ( numberOfWaitingThreads == 0 ) ) {
            purging = false;
        }

        return o;
    }

    /**
     * Return the size of the queue.
     *
     * @return size of queue.
     */
    public synchronized int size() {
        return loadQueue.size();
    }

    /**
     * Remove all elements from queue. Also unblock those who are waiting for
     * items in the queue. They leave the getNext() method with null.
     */
    public synchronized void purge() {
        loadQueue.clear();
        detailsToRequestMap.clear();
        purging = true;
        notifyAll();
    }

    /**
     * Clear the queue of items. If there are users of the class that are
     * blocked while waiting for elements in the queue, they remain so.
     */
    public synchronized void clear() {
        loadQueue.clear();
        detailsToRequestMap.clear();
    }

    /**
     * Remove the given item from the queue.
     *
     * @param url The url of the object to be removed
     * @param details The instance of the detail to be removed from the URL
     */
    public void remove(String[] url, LoadDetails details) {
        synchronized(loadQueue) {
            LoadRequest req = (LoadRequest)detailsToRequestMap.get(details);

            if(req == null || !req.loadList.contains(details))
                return;

            // remove from the load queue first so that we make sure we get
            // it before the content loader snaffles it. Then go back and grab
            // it from the hashmap.
            if(req.loadList.size() == 1) {
                loadQueue.remove(req);
            } else {
                req.loadList.remove(details);
            }

            detailsToRequestMap.remove(details);
        }
    }

    /**
     * If the sorting priority system property has changed, call this method to
     * reload the property and force a resorting of the queue. This will not
     * effect items that are currently being processed, only items that are
     * waiting to be processed.
     */
    public void requestResort() {
        sorter.updatePriorities();
        TreeSet new_queue = new TreeSet(sorter);
        new_queue.addAll(loadQueue);

        loadQueue = new_queue;
    }
}
