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

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * Marker interface that defines a node that has it's rendered state generated
 * relative to the viewpoint, possibly every frame.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLViewDependentNodeType extends VRMLNodeType {
}
