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

import org.xj3d.sai.Xj3DNavigationUIListener;
import org.xj3d.sai.Xj3DViewpoint;

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
 *   Xj3DNavigationUIListener listener = null;
 *
 *   public void addNodeListener(Xj3DNavigationUIListener l) {
 *     listener = NavigationListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(Xj3DNavigationUIListener l) {
 *     listener = NavigationListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(Xj3DNavigationUIEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class NavigationListenerMulticaster
    implements Xj3DNavigationUIListener {

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
        "Unknown error sending navigation state change event: ";

    /** The node listeners in use by this class */
    private final Xj3DNavigationUIListener a;

    /** The node listeners in use by this class */
    private final Xj3DNavigationUIListener b;

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
    NavigationListenerMulticaster(Xj3DNavigationUIListener a,
                                  Xj3DNavigationUIListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    Xj3DNavigationUIListener remove(Xj3DNavigationUIListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        Xj3DNavigationUIListener a2 = removeInternal(a, oldl);
        Xj3DNavigationUIListener b2 = removeInternal(b, oldl);

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
    static Xj3DNavigationUIListener add(Xj3DNavigationUIListener a,
                                        Xj3DNavigationUIListener b) {
        return (Xj3DNavigationUIListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    static Xj3DNavigationUIListener remove(Xj3DNavigationUIListener l,
                                           Xj3DNavigationUIListener oldl) {
        return (Xj3DNavigationUIListener)removeInternal(l, oldl);
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DNavigationUIListener
    //----------------------------------------------------------

    /**
     * The list of active viewpoints have been changed to this new list. A new
     * list can be given due to either a new world being loaded, or the active
     * navigation layer has been changed. This method is not called if a single
     * viewpoint is added or removed from the scene.
     * <p>
     *
     * If the scene contains no viewpoints at all (except the default bindable),
     * then this will be called with a null parameter.
     * <p>
     *
     * On scene or navigation layer change, it is guaranteed that this method
     * will be called before the notification of the actual bound viewpoint.
     *
     * @param An array of exactly the number of viewpoints in the list
     */
    public void availableViewpointsChanged(Xj3DViewpoint[] viewpoints) {
        try {
            a.availableViewpointsChanged(viewpoints);
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
            b.availableViewpointsChanged(viewpoints);
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
     * Notification that the selected viewpoint has been changed to this
     * new instance. There are many different reasons this could happen -
     * new node bound, world changed or even active navigation layer has
     * changed and this is the default in that new layer.
     * <p>
     *
     * If the file contains no viewpoints or the default viewpoint is
     * bound (due to unbinding all real viewpoints on the stack) then this
     * will be called with a null parameter.
     * <p>
     *
     * It is guaranteed that this will always contain something from the
     * currently active viewpoint list. If all change, that callback will be
     * called before this one, to ensure consistency.
     *
     * @param vp The viewpoint instance that is now selected or null
     */
    public void selectedViewpointChanged(Xj3DViewpoint vp) {
        try {
            a.selectedViewpointChanged(vp);
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
            b.selectedViewpointChanged(vp);
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
     * Notification that this viewpoint has been appended to the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointAdded(Xj3DViewpoint vp) {
        try {
            a.viewpointAdded(vp);
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
            b.viewpointAdded(vp);
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
     * Notification that this viewpoint has been removed from the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointRemoved(Xj3DViewpoint vp) {
        try {
            a.viewpointRemoved(vp);
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
            b.viewpointRemoved(vp);
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
     * The list of available navigation states has changed to this new list.
     * The list of states corresponds to the X3D-specification defined list
     * and any additional browser-specific state names. If a state is not
     * listed in this array, then the ability to activate it should be
     * disabled (eg greying out the button that represents it). If no states
     * are currently defined, this will contain the default string "NONE",
     * which corresponds to disabling all user selection of navigation and
     * even the ability to navigate.
     *
     * @param states An array of the available state strings.
     */
    public void availableNavigationStatesChanged(String[] states) {
        try {
            a.availableNavigationStatesChanged(states);
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
            b.availableNavigationStatesChanged(states);
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
     * Notification that the currently selected navigation state has changed
     * to this new value. Selection may be due to the UI interaction,
     * courtesy of a node being bound or the active navigation layer has
     * changed.
     *
     * @param state The name of the state that is now the active state
     */
    public void selectedNavigationStateChanged(String state) {
        try {
            a.selectedNavigationStateChanged(state);
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
            b.selectedNavigationStateChanged(state);
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

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     *     a new Xj3DNavigationUIMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static Xj3DNavigationUIListener addInternal(Xj3DNavigationUIListener a,
                                                     Xj3DNavigationUIListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new NavigationListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of Xj3DNavigationUIMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static Xj3DNavigationUIListener removeInternal(Xj3DNavigationUIListener l,
                                                        Xj3DNavigationUIListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof NavigationListenerMulticaster) {
            return ((NavigationListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
