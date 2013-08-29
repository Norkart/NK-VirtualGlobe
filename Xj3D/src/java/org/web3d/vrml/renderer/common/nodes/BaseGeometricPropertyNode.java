/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeComponentListener;
import org.web3d.vrml.nodes.VRMLGeometricPropertyNodeType;

/**
 * An abstract implementation of any form geometric property node type.
 * <p>
 *
 * This implementation provides a number of the basic necessities when building
 * node information. This class does not define any extra fields over
 * the standard base node type.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseGeometricPropertyNode extends AbstractNode
    implements VRMLGeometricPropertyNodeType {

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /**
     * Create a default instance of this class with an empty listener list
     * and the solid field set to false.
     *
     * @param name The name of the type of node
     */
    public BaseGeometricPropertyNode(String name) {
        super(name);

        // Usually only 1 listener
        listeners = new ArrayList(1);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLComponentNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addComponentListener(VRMLNodeComponentListener l) {
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeComponentListener(VRMLNodeComponentListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    //----------------------------------------------------------
    // Public methods
    //----------------------------------------------------------

    /**
     * Fire a  event coordinate change event to the listeners. When
     * calling the listeners, it is assumed this node has changed.
     *
     * @param index The field index that has changed
     */
    protected void fireComponentChanged(int index) {
        int size = listeners.size();
        VRMLNodeComponentListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (VRMLNodeComponentListener)listeners.get(i);
                l.fieldChanged(this, index);
            } catch(Exception e) {
                System.out.println("Error sending component changed message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
