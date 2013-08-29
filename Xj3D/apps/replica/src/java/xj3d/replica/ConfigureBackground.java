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

package xj3d.replica;

// External imports
import java.awt.Color;

// Local imports
import org.web3d.vrml.lang.VRMLNodeFactory;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;

import org.web3d.vrml.renderer.ogl.nodes.enveffects.OGLBackground;

import org.xj3d.ui.construct.Construct;
import org.xj3d.ui.construct.ScenePreprocessor;

/**
 * A ScenePreprocessor implementation that configures the background
 * skyColor to a predetermined RGB value. This module functions by
 * inserting a new Background node as the first node in the scene root.
 * It presumes that this node will therefore be the initial bound
 * Background node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class ConfigureBackground implements ScenePreprocessor {
	
	/** The background color */
	private Color color;
	
	/**
	 * Constructor
	 *
	 * @param color The background color to set.
	 */
	public ConfigureBackground( Color color ) {
		if ( color == null ) {
			this.color = Color.BLACK;
		} else {
			this.color = color;
		}
	}
	
	/** 
	 * Modify the VRMLScene
	 *
	 * @param scene The VRMLScene instance
	 * @param construct The browser construct
	 */
	public void preprocess( VRMLScene scene, Construct construct ) {
		
		VRMLNodeFactory factory = scene.getNodeFactory( );
		OGLBackground background = 
			(OGLBackground)factory.createVRMLNode( "Background", false );
		int index = background.getFieldIndex( "skyColor" );
		background.setValue( index, color.getRGBComponents( null ), 3 );
		index = background.getFieldIndex( "set_bind" );
		background.setValue( index, true );
		
		VRMLWorldRootNodeType root = (VRMLWorldRootNodeType)scene.getRootNode( );
		
		VRMLNodeType[] children = root.getChildren( );
		int num_children = children.length;
		VRMLNodeType[] new_children = new VRMLNodeType[num_children+1];
		new_children[0] = background;
		System.arraycopy( children, 0, new_children, 1, num_children );
		
		root.setChildren( new_children );
	}
}
