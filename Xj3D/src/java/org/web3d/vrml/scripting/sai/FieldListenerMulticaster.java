/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;

/**
 * A class which implements efficient and thread-safe multi-cast event
 * dispatching for the events defined in this package.
 * <P>
 *
 * This class will manage an immutable structure consisting of a chain of
 * event listeners and will dispatch events to those listeners.  Because
 * the structure is immutable, it is safe to use this API to add/remove
 * listeners during the process of an event dispatch operation.
 * <P>
 *
 * An example of how this class could be used to implement a new
 * component which fires "action" events:
 *
 * <PRE><CODE>
 * public myComponent extends Component {
 *   X3DFieldEventListener nodeListener = null;
 *
 *   public void addFieldListener(X3DFieldEventListener l) {
 *     nodeListener = FieldListenerMulticaster.add(nodeListener, l);
 *   }
 *
 *   public void removeFieldListener(X3DFieldEventListener l) {
 *     nodeListener = FieldListenerMulticaster.remove(nodeListener, l);
 *   }
 *
 *   public void fireFieldChanged(int index) {
 *     if(nodeListener != null) {
 *       nodeListener.fieldChanged(index);
 *   }
 * }
 * </CODE></PRE>
 *
 * @author  Justin Couch
 * @version 0.7 (27 August 1999)
 */
public class FieldListenerMulticaster implements X3DFieldEventListener {

    /** The node listeners in use by this class */
    final X3DFieldEventListener a, b;

    /**
     * Creates an event multicaster instance which chains listener-a
     * with listener-b. Input parameters <code>a</code> and <code>b</code>
     * should not be <code>null</code>, though implementations may vary in
     * choosing whether or not to throw <code>NullPointerException</code>
     * in that case.
     * @param a listener-a
     * @param b listener-b
     */
    public FieldListenerMulticaster(X3DFieldEventListener a, X3DFieldEventListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    public X3DFieldEventListener remove(X3DFieldEventListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        X3DFieldEventListener a2 = removeInternal(a, oldl);
        X3DFieldEventListener b2 = removeInternal(b, oldl);

        if (a2 == a && b2 == b) {
            return this;  // it's not here
        }

        return addInternal(a2, b2);
    }

    /**
     * Adds input-method-listener-a with input-method-listener-b and
     * returns the resulting multicast listener.
     * @param a input-method-listener-a
     * @param b input-method-listener-b
     */
    public static X3DFieldEventListener add(X3DFieldEventListener a,
                                            X3DFieldEventListener b) {
        return (X3DFieldEventListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    public static X3DFieldEventListener remove(X3DFieldEventListener l,
                                               X3DFieldEventListener oldl) {
        return (X3DFieldEventListener)removeInternal(l, oldl);
    }

    /**
     * Send a field change notification.
     *
     * @param evt An event describing what happened
     */
    public void readableFieldChanged(X3DFieldEvent evt) {
        try {
            a.readableFieldChanged(evt);
        } catch(Throwable th) {
            System.out.println("Error sending connection event to: " + a);
            th.printStackTrace();
        }

        try {
            b.readableFieldChanged(evt);
        } catch(Throwable th) {
            System.out.println("Error sending connection event to: " + b);
            th.printStackTrace();
        }
    }

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     * a new FieldListenerMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static X3DFieldEventListener addInternal(X3DFieldEventListener a,
                                                     X3DFieldEventListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new FieldListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of FieldListenerMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static X3DFieldEventListener removeInternal(X3DFieldEventListener l,
                                 X3DFieldEventListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof FieldListenerMulticaster) {
            return ((FieldListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
