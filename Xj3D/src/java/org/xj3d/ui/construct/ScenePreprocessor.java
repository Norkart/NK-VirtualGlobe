/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.construct;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLScene;

/**
 * Defines the requirements of a module that performs some modifications
 * to a VRMLScene instance before it is set to the browser Construct.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ScenePreprocessor {

	/** 
	 * Modify the VRMLScene
	 *
	 * @param scene The VRMLScene instance
	 * @param construct The browser construct
	 */
	public void preprocess( VRMLScene scene, Construct construct );
}
