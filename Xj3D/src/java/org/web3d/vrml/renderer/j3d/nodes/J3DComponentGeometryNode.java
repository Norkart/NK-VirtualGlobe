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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.Geometry;

// Application specific imports
import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;

/**
 * An abstract implementation of any form of componentized geometry.
 * <p>
 *
 * This implementation provides a number of the basic necessities when building
 * geometry information. This class does not define any extra fields over the
 * standard base node type.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class J3DComponentGeometryNode extends BaseComponentGeometryNode
    implements J3DGeometryNodeType {

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /**
     * Create a default instance of this class with an empty listener list
     * and the solid field set to true
     *
     * @param name The name of the type of node
     */
    public J3DComponentGeometryNode(String name) {
        super(name);

        listeners = new ArrayList();
    }

    //----------------------------------------------------------
    // Methods required by the J3DGeometryNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
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
    public void removeGeometryListener(J3DGeometryListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }


    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    //----------------------------------------------------------
    // Methods internal to J3DGeometryNode
    //----------------------------------------------------------

    /**
     * fire a geometry added event to the listeners.
     *
     * @param items The geometry items that have been added
     */
    protected void fireGeometryAdded(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryAdded(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry add message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry changed event to the listeners.
     *
     * @param items The geometry items that have changed or null for all
     */
    protected void fireGeometryChanged(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryChanged(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry removed event to the listeners.
     *
     * @param items The geometry items that have removed or null for all
     */
    protected void fireGeometryRemoved(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryRemoved(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry remove message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
