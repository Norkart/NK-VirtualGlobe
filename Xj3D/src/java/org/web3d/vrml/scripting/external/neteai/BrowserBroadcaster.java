/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import vrml.eai.Browser;
import vrml.eai.event.BrowserEvent;
import vrml.eai.event.BrowserListener;

/**
 * BrowserBroadcaster is responsible for broadcasting BrowserChanged messages on
 * the client side.
 */
public class BrowserBroadcaster {

    /** The browser these events are for */
    Browser theBrowser;

    /** Working thread blocks on this when idle */
    LinkedList toDoList;

    /** Listeners for BrwoserEvent events */
    Vector browserListeners;

    /**
     * Basic constructor. Starts one broadcast thread.
     * 
     * @param b
     *            The browser these events are for.
     */
    BrowserBroadcaster(Browser b) {
        theBrowser = b;
        browserListeners = new Vector();
        toDoList = new LinkedList();
        new BroadcastTask(toDoList).start();
    }

    /**
     * Register a browser listener and return true if that was the first browser
     * listener.
     * 
     * @param listener
     *            The listener to add.
     * @return Was that the first browser listener?
     */
    synchronized boolean addBrowserListener(BrowserListener listener) {
        if (!browserListeners.contains(listener))
            browserListeners.add(listener);
        return browserListeners.size() == 1;
    }

    /**
     * Remove a browser listener and returns true if there are no more browser
     * listeners.
     * 
     * @param listener
     *            The listener to remove
     * @return Are there no more browser listeners?
     */
    synchronized boolean removeBrowserListener(BrowserListener listener) {
        if (browserListeners.contains(listener))
            browserListeners.remove(listener);
        return browserListeners.size() == 0;
    }

    /** Send out a browserChanged broadcast */
    synchronized void queueBroadcast(int broadcastType) {
        synchronized (toDoList) {
            toDoList.add(new WorkEntry(new BrowserEvent(theBrowser,
                    broadcastType), (Vector) browserListeners.clone()));
            toDoList.notifyAll();
        }
    }

    /** The thread responsible for broadcasting the
     *  browser changed events.  Will wait() on the
     *  linked list if no work entries are available.
     */
    static class BroadcastTask extends Thread {

        /** Source of WorkEntry objects. */
        LinkedList toDoList;

        /**
         * @param workList The list to remove entries from.
         */
        BroadcastTask(LinkedList workList) {
            toDoList = workList;
        }

        /**
         * *
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            while (true) {
                System.out.println("Broadcaster in loop.");
                WorkEntry entry;
                synchronized (toDoList) {
                    while (toDoList.size() == 0)
                        try {
                            toDoList.wait();
                        } catch (InterruptedException e) {
                            System.err.println("BroadcastTask interrupted.");
                            return;
                        }
                    entry = (WorkEntry) toDoList.removeFirst();
                }
                Enumeration e = entry.getElements();
                while (e.hasMoreElements()) {
                    try {
                        ((BrowserListener) e.nextElement())
                                .browserChanged(entry.getEvent());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

    }

    /** Descriptor of a BrowserEvent and its receivers */
    static class WorkEntry {
        BrowserEvent event;

        Vector targets;

        /** Specify a new work entry
         * @param event The BrowserEvent to broadcast
         * @param targets Contains the BrowserListeners who will receive the event
         */
        WorkEntry(BrowserEvent event, Vector targets) {
            this.event = event;
            this.targets = targets;
        }

        /**
         * @return Enumeration of the BrowserListeners.
         */
        public Enumeration getElements() {
            return targets.elements();
        }

        public BrowserEvent getEvent() {
            return event;
        }

    }

}