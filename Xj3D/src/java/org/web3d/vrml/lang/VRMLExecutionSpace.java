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
package org.web3d.vrml.lang;

// Standard imports
// none

// Application specific imports
// none

/**
 * The representation of a contained, separable execution space that a node
 * contains.
 * <p>
 *
 * This class is used to mark a node that contains a separated execution space
 * from the parent node. The two nodes that will use this are proto instances
 * and inlines. The idea is to mark the node instance as being special so that
 * we can treat the contained routes and other items as being important as well
 * as separable from the parent space.
 * <p>
 *
 * When the class instance that implements this node is created, this node is
 * used as a marker and a way of creating a separate execution and name space
 * for the nodes contained within it. When registering routes, this class
 * should be associated with the routes so that when the parent node is
 * removed from the active scene graph, all of the associated routes will be
 * removed with it. A proto or inline, once removed should not continue to
 * have the routes/scripts etc evaluated.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLExecutionSpace {

    /**
     * Get the contained scene graph that this instance has. This represents
     * everything about the internal scene that the node declaration wraps.
     * This is a real-time representation so that if it the nodes contains a
     * script that changes the internal representation then this instance will
     * be updated to reflect and changes made.
     *
     * @return The scene contained by this node instance
     */
    public BasicScene getContainedScene();
}
