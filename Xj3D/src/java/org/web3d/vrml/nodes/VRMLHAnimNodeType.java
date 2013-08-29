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
package org.web3d.vrml.nodes;

// External imports
import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimObject;

// Local imports
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * Denotes a node type that is part of the HAnim component.
 * <p>
 *
 * The Hanim component is internally implemented using the abstract HAnim
 * system from the <http://code.j3d.org/">j3d.org Code Repository</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLHAnimNodeType extends VRMLNodeType {

    /**
     * Set the current node factory to use. If this is set again, replace the
     * current implementation node with a new one from this new instance. This
     * may be needed at times when the user makes a change that forces the old
     * way to be incompatible and thus needing a different implementation.
     * <p>
     *
     * Nodes that have HAnim children, should recursively call this method on
     * the children.
     *
     * @param fac The new factory instance to use
     */
    public void setHAnimFactory(HAnimFactory fac);

    /**
     * Get the HAnim implementation node. Since the HAnim class instance is not
     * the same as the basic geometry instance of the particular rendering API, we
     * need to fetch this higher-level construct so that the scene graph can be
     * constructed.
     *
     * @return The HAnimObject instance for this node
     */
    public HAnimObject getHAnimObject();
}
