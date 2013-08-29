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

package org.xj3d.core.eventmodel;

// External imports
// none

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLViewpointNodeType;

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
 *   ViewpointStatusListener listener = null;
 *
 *   public void addNodeListener(ViewpointStatusListener l) {
 *     listener = ViewpointStatusListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(ViewpointStatusListener l) {
 *     listener = ViewpointStatusListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(ViewpointStatusEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class ViewpointStatusListenerMulticaster
    implements ViewpointStatusListener {

    /** Error message when the user code barfs */
    private static final String VP_REMOVE_ERROR_MSG =
        "Error sending viewpoint remove message: ";

    /** Error message when the user code barfs */
    private static final String VP_ADD_ERROR_MSG =
        "Error sending viewpoint add message: ";

    /** Error message when the user code barfs */
    private static final String VP_CHANGE_ERROR_MSG =
        "Error sending viewpoint list change message: ";

    /** Error message when the user code barfs */
    private static final String VP_SELECT_ERROR_MSG =
        "Error sending viewpoint change selection message: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending viewpoint event: ";

    /** The node listeners in use by this class */
    private final ViewpointStatusListener a;

    /** The node listeners in use by this class */
    private final ViewpointStatusListener b;

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
    ViewpointStatusListenerMulticaster(ViewpointStatusListener a,
                                  ViewpointStatusListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    ViewpointStatusListener remove(ViewpointStatusListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        ViewpointStatusListener a2 = removeInternal(a, oldl);
        ViewpointStatusListener b2 = removeInternal(b, oldl);

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
    public static void setErrorReporter(ErrorReporter reporter) {
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
    public static ViewpointStatusListener add(ViewpointStatusListener a,
                                              ViewpointStatusListener b) {
        return (ViewpointStatusListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    public static ViewpointStatusListener remove(ViewpointStatusListener l,
                                                 ViewpointStatusListener oldl) {
        return (ViewpointStatusListener)removeInternal(l, oldl);
    }

    //----------------------------------------------------------
    // Methods defined by ViewpointStatusListener
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
     * @param viewpoints An array of exactly the number of viewpoints in the list
     */
    public void availableViewpointsChanged(VRMLViewpointNodeType[] viewpoints) {
        try {
            a.availableViewpointsChanged(viewpoints);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(VP_CHANGE_ERROR_MSG + a,
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
                errorReporter.errorReport(VP_CHANGE_ERROR_MSG + b,
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
    public void selectedViewpointChanged(VRMLViewpointNodeType vp) {
        try {
            a.selectedViewpointChanged(vp);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(VP_SELECT_ERROR_MSG + a,
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
                errorReporter.errorReport(VP_SELECT_ERROR_MSG + b,
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
    public void viewpointAdded(VRMLViewpointNodeType vp) {
        try {
            a.viewpointAdded(vp);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(VP_ADD_ERROR_MSG + a,
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
                errorReporter.errorReport(VP_ADD_ERROR_MSG + b,
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
    public void viewpointRemoved(VRMLViewpointNodeType vp) {
        try {
            a.viewpointRemoved(vp);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(VP_REMOVE_ERROR_MSG + a,
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
                errorReporter.errorReport(VP_REMOVE_ERROR_MSG + b,
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
     *     a new ViewpointStatusMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static ViewpointStatusListener addInternal(ViewpointStatusListener a,
                                                     ViewpointStatusListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new ViewpointStatusListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of ViewpointStatusMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static ViewpointStatusListener removeInternal(ViewpointStatusListener l,
                                                          ViewpointStatusListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof ViewpointStatusListenerMulticaster) {
            return ((ViewpointStatusListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
