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

package org.web3d.vrml.scripting.external.sai;

// External imports
// none

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.sai.Xj3DStatusListener;

/**
 * A class which implements efficient and thread-safe multi-cast event
 * dispatching for the events defined in this package.
 * <p>
 *
 * This class will manage an immutable structure consisting of a chain of
 * event listeners and will dispatch events to those listeners.  Because
 * the structure is immutable, it is safe to use this API to add/remove
 * listeners during the process of an event dispatch operation.
 * <p>
 *
 * An example of how this class could be used to implement a new
 * component which fires "action" events:
 *
 * <pre><code>
 * public myComponent extends Component {
 *   Xj3DStatusListener listener = null;
 *
 *   public void addNodeListener(Xj3DStatusListener l) {
 *     listener = StatusListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(Xj3DStatusListener l) {
 *     listener = StatusListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(Xj3DStatusEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class StatusListenerMulticaster
    implements Xj3DStatusListener {

    /** Error message when the user code barfs */
    private static final String STATUS_ERROR_MSG =
        "Error sending status message change updates: ";

    /** Error message when the user code barfs */
    private static final String FPS_ERROR_MSG =
        "Error sending framerate updates: ";

    /** Error message when the user code barfs */
    private static final String PROGRESS_ERROR_MSG =
        "Error sending progress update message: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending browser change event: ";

    /** The node listeners in use by this class */
    private final Xj3DStatusListener a;

    /** The node listeners in use by this class */
    private final Xj3DStatusListener b;

    /** Reporter instance for handing out errors */
    private static ErrorReporter errorReporter =
        DefaultErrorReporter.getDefaultReporter();

    /**
     * Creates an event multicaster instance which chains listener-a
     * with listener-b. Input parameters <code>a</code> and <code>b</code>
     * should not be <code>null</code>, though implementations may vary in
     * choosing whether or not to throw <code>NullPointerException</code>
     * in that case.
     * @param a listener-a
     * @param b listener-b
     */
    StatusListenerMulticaster(Xj3DStatusListener a, Xj3DStatusListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    Xj3DStatusListener remove(Xj3DStatusListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        Xj3DStatusListener a2 = removeInternal(a, oldl);
        Xj3DStatusListener b2 = removeInternal(b, oldl);

        if (a2 == a && b2 == b) {
            return this;  // it's not here
        }

        return addInternal(a2, b2);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    static void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Adds input-method-listener-a with input-method-listener-b and
     * returns the resulting multicast listener.
     * @param a input-method-listener-a
     * @param b input-method-listener-b
     */
    static Xj3DStatusListener add(Xj3DStatusListener a, Xj3DStatusListener b) {
        return (Xj3DStatusListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    static Xj3DStatusListener remove(Xj3DStatusListener l, Xj3DStatusListener oldl) {
        return (Xj3DStatusListener)removeInternal(l, oldl);
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DStatusListener
    //----------------------------------------------------------

    /**
     * Notification that a single line status message has changed to the new
     * string. A null string means to clear the currently displayed message.
     *
     * @param msg The new message string to display for the status
     */
    public void updateStatusMessage(String msg) {
        try {
            a.updateStatusMessage(msg);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(STATUS_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.updateStatusMessage(msg);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(STATUS_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that the calculated frames per second has changed to this
     * new value. It is expected that this is called frequently.
     */
    public void updateFramesPerSecond(float fps) {
        try {
            a.updateFramesPerSecond(fps);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(FPS_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.updateFramesPerSecond(fps);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(FPS_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification of a progress update. There may be several items in
     * progression at once (eg multithreaded texture and scripting loading)
     * so implementers should work appropriately for this situation. To keep
     * this aligned, each item that is reporting progress will have a unique
     * ID string (for this session) associated with it so you can keep track
     * of the multiples. Once 100% has been reached you can assume that the
     * tracking is complete for that object.
     *
     * @param id A unique ID string for the given item
     * @param msg A message to accompany the update
     * @param perc A percentage from 0-100 of the progress completion
     */
    public void progressUpdate(String id, String msg, float perc) {

        try {
            a.progressUpdate(id, msg, perc);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(PROGRESS_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.progressUpdate(id, msg, perc);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(PROGRESS_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     *     a new Xj3DStatusMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static Xj3DStatusListener addInternal(Xj3DStatusListener a,
                                                     Xj3DStatusListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new StatusListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of Xj3DStatusMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static Xj3DStatusListener removeInternal(Xj3DStatusListener l,
                                                        Xj3DStatusListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof StatusListenerMulticaster) {
            return ((StatusListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
