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

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;

/**
 * An abstract implementation of any bindable node.
 * <p>
 *
 * The implementation treats the time and bound states independently. It is
 * assumed that the browser environment displaying the world will take care
 * of the stack and bind time information setting.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.13 $
 */
public abstract class BaseBindableNode extends AbstractNode
    implements VRMLBindableNodeType, VRMLTimeDependentNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.BindableNodeType, TypeConstants.TimeDependentNodeType };

    /** Index of the set_bind eventIn */
    protected static final int FIELD_BIND = LAST_NODE_INDEX + 1;

    /** Index of the bindTime field */
    protected static final int FIELD_BIND_TIME = LAST_NODE_INDEX + 2;

    /** Index of the isBind eventOut */
    protected static final int FIELD_IS_BOUND = LAST_NODE_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_BINDABLE_INDEX = FIELD_IS_BOUND;

    /** SFTime bindTime */
    protected double vfBindTime;

    /** SFBool isBound */
    protected boolean vfIsBound;

    /** Flag indicating if this node is already on the stack */
    protected boolean isOnStack;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /** VRMLClock used for bindTime events */
    private VRMLClock vrmlClock;

    /**
     * Flag indicating that we have a bind call pending once we have a clock
     * and at least one listener. When bindables are created and added during
     * external scripting, and implicitly realised, the set_bind call comes in
     * before the node gets added to running scene or the bindable node
     * manager. This makes sure that we send the updates through once we have
     * all the right info.
     */
    private boolean notifyPending;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseBindableNode(String name) {
        super(name);

        listeners = new ArrayList();
        vfIsBound = false;
        notifyPending = false;
    }

    /**
     * Set the fields of the binadble node that has the fields set
     * based on the fields of the passed in node. This directly copies the
     * bind state, so could cause some interesting problems. Not sure what
     * we should do with this currently.
     *
     * @param node The bindable node to copy info from
     */
    protected void copy(VRMLBindableNodeType node) {
        vfBindTime = node.getBindTime();
        vfIsBound = node.getIsBound();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTimeDependentNodeType interface.
    //----------------------------------------------------------
    /**
     * Set the clock that this time dependent node will be running with.
     * The clock provides all the information and listeners for keeping track
     * of time. Setting a value of null will ask the node to remove the clock
     * from it's use so that the node may be removed from the scene.
     *
     * @param clock The clock to use for this node
     */
    public void setVRMLClock(VRMLClock clock) {
        vrmlClock = clock;

        if(notifyPending) {
            vfBindTime = vrmlClock.getTime();

            if(!inSetup) {
                hasChanged[FIELD_BIND_TIME] = true;
                fireFieldChanged(FIELD_BIND_TIME);

                if(listeners.size() != 0)
                    fireIsBoundChanged(vfIsBound);
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLBindableNodeType interface.
    //----------------------------------------------------------

    /**
     * Notify the bindable node that it is on the stack, or not on the
     * stack, as the case may be and that it should send bind events as
     * appropriate
     *
     * @param onStack true if this node is now on the stack
     */
    public void setOnStack(boolean onStack) {
        isOnStack = onStack;
    }

    /**
     * Set the bind field of this node. This will cause the node to be moved
     * within the stack according to the properties.
     *
     * @param enable True if this node is to be bound
     * @param notify true if this should notify the listeners
     * @param time The time that this was sent
     */
    public void setBind(boolean enable, boolean notify, double time) {
        if(!enable && !isOnStack)
            return;

        vfIsBound = enable;
        vfBindTime = time;

        if(!inSetup) {
            if(notify)
                fireIsBoundChanged(vfIsBound);

            hasChanged[FIELD_IS_BOUND] = true;
            hasChanged[FIELD_BIND_TIME] = true;

            fireFieldChanged(FIELD_IS_BOUND);
            fireFieldChanged(FIELD_BIND_TIME);
        }
    }

    /**
     * Get the current isBound state of the node.
     *
     * @return the current binding state
     */
    public boolean getIsBound() {
        return vfIsBound;
    }

    /**
     * Set the bindTime field of this node. This has no effect on the bind
     * information.
     *
     * @return The bound time value last set
     */
    public double getBindTime() {
        return vfBindTime;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addBindableNodeListener(VRMLBindableNodeListener l) {
        if((l == null) || listeners.contains(l))
            return;

        boolean send_notify = (listeners.size() == 0) && notifyPending;

        listeners.add(l);

        if(send_notify)
            fireIsBoundChanged(vfIsBound);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeBindableNodeListener(VRMLBindableNodeListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_IS_BOUND:
                fieldData.clear();
                fieldData.booleanValue = vfIsBound;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BIND_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfBindTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {
        try {
            switch(srcIndex) {
                case FIELD_BIND_TIME:
                    destNode.setValue(destIndex, vfBindTime);
                    break;

                case FIELD_IS_BOUND:
                    destNode.setValue(destIndex, vfIsBound);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field type bind.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BIND:
                if(vrmlClock != null)
                    setBind(value, true, vrmlClock.getTime());
                else {
                    notifyPending = true;
                    setBind(value, true, 0);
                }

                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Methods internal to VRMLBindableNodeType
    //----------------------------------------------------------

    /**
     * Send the bindable listeners the an event to say we have just become
     * the active node.
     *
     * @param isActive true if this node is becoming active
     */
    protected void fireIsBoundChanged(boolean isActive) {
        int size = listeners.size();
        VRMLBindableNodeListener l;

        if(size == 0)
            return;

        for(int i = 0; i < size; i++) {
            try {
                l = (VRMLBindableNodeListener)listeners.get(i);
                l.nodeIsBound(this, isActive);
            } catch(Exception e) {
                System.out.println("Error sending node bound message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }

        notifyPending = false;
    }
}
