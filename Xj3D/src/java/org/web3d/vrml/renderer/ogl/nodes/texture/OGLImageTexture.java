/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.texture;

// External imports
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.texture.BaseImageTexture;

/**
 * OGL implementation of a ImageTexture node.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 2.5 $
 */
public class OGLImageTexture extends BaseImageTexture
	implements OGLTextureNodeType {
	
	/** Reference to the created AV3D texture */
	private Texture texture;
	
	/**
	 * Construct a default instance of this node.
	 */
	public OGLImageTexture() {	
	}
	
	/**
	 * Construct a new instance of this node based on the details from the
	 * given node. If the node is not the same type, an exception will be
	 * thrown.
	 *
	 * @param node The node to copy
	 * @throws IllegalArgumentException Incorrect Node Type
	 */
	public OGLImageTexture(VRMLNodeType node) {
		super(node);
	}
	
	//--------------------------------------------------------------
	// Methods defined by VRMLSingleExternalNodeType
	//--------------------------------------------------------------
	
	/**
	 * Set the URL to a new value. If the value is null, it removes the old
	 * contents (if set) and treats it as though there is no content.
	 *
	 * @param url The list of urls to set or null
	 */
	public void setUrl(String[] newURL, int numValid) {
		super.setUrl(newURL, numValid);
		
		if(numValid == 0 && !inSetup)
			fireTextureImageChanged(0,this,null,null);
	}
	
	
	/**
	 * Set the content of this node to the given object. The object is then
	 * cast by the internal representation to the form it needs. This assumes
	 * at least some amount of intelligence on the part of the caller, but
	 * we also know that we should not pass something dumb to it when we can
	 * check what sort of content types it likes to handle. We assume the
	 * loader thread is operating in the same context as the one that created
	 * the node in the first place and thus knows the general types of items
	 * to pass through.
	 *
	 * @param mimetype The mime type of this object if known
	 * @param content The content of the object
	 * @throws IllegalArguementException The content object is not supported
	 */
	public void setContent(String mimetype, Object content)
		throws IllegalArgumentException {
 
		if (content == null)
			return;
		
		if (content instanceof NIOBufferImage) { 
			implImage = (NIOBufferImage)content;
		} else {
			System.out.println("Unknown content type: " + content + " URL: " + loadedURI);
			// Notify listeners of new impl.
			fireTextureImageChanged(0, this, null, null);
			return;
		}
		
		fireTextureImageChanged(0, this, implImage, loadedURI);
		
		// Clear after notification
		implImage = null;
		
		loadState = LOAD_COMPLETE;
		fireContentStateChanged();
	}
	//----------------------------------------------------------
	// Methods defined by VRMLTextureNodeType
	//----------------------------------------------------------
	
	/**
	 * Get a string for cacheing this object.  Null means do not cache this
	 * texture.
	 *
	 * @param stage The stage number,  0 for all single stage textures.
	 * @return A string to use in lookups.  Typically the url loaded.
	 */
	public String getCacheString(int stage) {
		return loadedURI;
	}
	
	//----------------------------------------------------------
	// Methods defined by OGLTextureNodeType
	//----------------------------------------------------------
	
	/**
	 * Set the Aviatrix3D texture representation back into the node
	 * implementation.
	 *
	 * @param index The index of the texture (for multitexture)
	 * @param tex The texture object to set
	 */
	public void setTexture(int index, Texture tex) {
		texture = tex;
	}
	
	//----------------------------------------------------------
	// Methods defined by OGLVRMLNode
	//----------------------------------------------------------
	
	/**
	 * Get the Java3D scene graph object representation of this node. This will
	 * need to be cast to the appropriate parent type when being used.
	 *
	 * @return The J3D representation.
	 */
	public SceneGraphObject getSceneGraphObject() {
		return texture;
	}
	
	//----------------------------------------------------------
	// Methods defined by VRMLNodeType
	//----------------------------------------------------------
	
	/**
	 * Set the value of the field at the given index as an array of strings.
	 * This would be used to set MFString field types.
	 *
	 * @param index The index of destination field to set
	 * @param value The new value to use for the node
	 * @param numValid The number of valid values to copy from the array
	 * @throws InvalidFieldException The field index is not know
	 */
	public void setValue(int index, String[] value, int numValid)
		throws InvalidFieldException, InvalidFieldValueException {
		
		super.setValue(index, value, numValid);
		
		if (numValid == 0)
			fireTextureImageChanged(0, this, null, null);
	}
}
