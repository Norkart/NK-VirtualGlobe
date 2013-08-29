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

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserListener;

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
 *   BrowserListener listener = null;
 *
 *   public void addNodeListener(BrowserListener l) {
 *     listener = BrowserListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(BrowserListener l) {
 *     listener = BrowserListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(BrowserEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class BrowserListenerMulticaster
    implements BrowserListener {

    /** Error message when the user code barfs */
    private static final String CHANGE_ERROR_MSG =
        "Error sending emissive changed notification to: ";

    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending browser change event: ";

    /** The node listeners in use by this class */
    private final BrowserListener a;

    /** The node listeners in use by this class */
    private final BrowserListener b;

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
    BrowserListenerMulticaster(BrowserListener a, BrowserListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    BrowserListener remove(BrowserListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        BrowserListener a2 = removeInternal(a, oldl);
        BrowserListener b2 = removeInternal(b, oldl);

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
    static BrowserListener add(BrowserListener a, BrowserListener b) {
        return (BrowserListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    static BrowserListener remove(BrowserListener l, BrowserListener oldl) {
        return (BrowserListener)removeInternal(l, oldl);
    }

    //----------------------------------------------------------
    // Methods defined by BrowserListener
    //----------------------------------------------------------

    /**
     * Process an event that has occurred in the VRML browser.
     *
     * @param evt The event that caused this method to be called
     */
    public void browserChanged(BrowserEvent evt) {
        try {
            a.browserChanged(evt);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(CHANGE_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.browserChanged(evt);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(CHANGE_ERROR_MSG + b,
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
     *     a new BrowserMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static BrowserListener addInternal(BrowserListener a,
                                                     BrowserListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new BrowserListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of BrowserMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static BrowserListener removeInternal(BrowserListener l,
                                                        BrowserListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof BrowserListenerMulticaster) {
            return ((BrowserListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
