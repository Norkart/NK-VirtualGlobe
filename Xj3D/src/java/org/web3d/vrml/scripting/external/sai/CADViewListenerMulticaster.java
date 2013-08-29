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
import org.web3d.x3d.sai.X3DNode;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.sai.Xj3DCADViewListener;

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
 *   Xj3DCADViewListener listener = null;
 *
 *   public void addNodeListener(Xj3DCADViewListener l) {
 *     listener = CADViewListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(Xj3DCADViewListener l) {
 *     listener = CADViewListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(Xj3DCADViewEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class CADViewListenerMulticaster
    implements Xj3DCADViewListener {

    /** Error message when the user code barfs */
    private static final String LAYER_ADD_ERROR_MSG =
        "Error sending layer addtition update: ";

    /** Error message when the user code barfs */
    private static final String LAYER_REMOVE_ERROR_MSG =
        "Error sending layer removal update: ";

    /** Error message when the user code barfs */
    private static final String ASS_ADD_ERROR_MSG =
        "Error sending assembly addtition update: ";

    /** Error message when the user code barfs */
    private static final String ASS_REMOVE_ERROR_MSG =
        "Error sending assembly removal update: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending CAD View listener event: ";

    /** The node listeners in use by this class */
    private final Xj3DCADViewListener a;

    /** The node listeners in use by this class */
    private final Xj3DCADViewListener b;

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
    CADViewListenerMulticaster(Xj3DCADViewListener a, Xj3DCADViewListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    Xj3DCADViewListener remove(Xj3DCADViewListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        Xj3DCADViewListener a2 = removeInternal(a, oldl);
        Xj3DCADViewListener b2 = removeInternal(b, oldl);

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
    static Xj3DCADViewListener add(Xj3DCADViewListener a,
                                   Xj3DCADViewListener b) {
        return (Xj3DCADViewListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    static Xj3DCADViewListener remove(Xj3DCADViewListener l,
                                      Xj3DCADViewListener oldl) {
        return (Xj3DCADViewListener)removeInternal(l, oldl);
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DCADViewListener
    //----------------------------------------------------------

    /**
     * Notification that an assembly was added to the root of the world.
     *
     * @param assembly The node instance that was added
     */
    public void assemblyAdded(X3DNode assembly) {
        try {
            a.assemblyAdded(assembly);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(ASS_ADD_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.assemblyAdded(assembly);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(ASS_ADD_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that an assembly was removed from the root of the world.
     *
     * @param assembly The node instance that was removed
     */
    public void assemblyRemoved(X3DNode assembly) {
        try {
            a.assemblyRemoved(assembly);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(ASS_REMOVE_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.assemblyRemoved(assembly);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(ASS_REMOVE_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that a CADLayer was added to the root of the world.
     *
     * @param assembly The node instance that was added
     */
    public void layerAdded(X3DNode layer) {
        try {
            a.layerAdded(layer);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(LAYER_ADD_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.layerAdded(layer);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(LAYER_ADD_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that a CADLayer was added to the root of the world.
     *
     * @param assembly The node instance that was removed
     */
    public void layerRemoved(X3DNode layer) {
        try {
            a.layerRemoved(layer);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(LAYER_REMOVE_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.layerRemoved(layer);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(LAYER_REMOVE_ERROR_MSG + b,
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
     *     a new Xj3DCADViewMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static Xj3DCADViewListener addInternal(Xj3DCADViewListener a,
                                                     Xj3DCADViewListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new CADViewListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of Xj3DCADViewMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static Xj3DCADViewListener removeInternal(Xj3DCADViewListener l,
                                                        Xj3DCADViewListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof CADViewListenerMulticaster) {
            return ((CADViewListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
