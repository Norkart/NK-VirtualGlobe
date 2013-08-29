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
package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * An abstract representation of the a node that can be used directly at the
 * root of the scene graph.
 * <p>
 *
 * Only X3DChildNode and LayerSet nodes can appear at the root level of a
 * scene graph. This is used as a marker for that capability.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLWorldRootChildNodeType extends VRMLNodeType {
}
