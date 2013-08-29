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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.texture.BasePixelTexture;

import org.web3d.vrml.renderer.ogl.nodes.OGLTextureNodeType;

/**
 * OpenGL implementation of a PixelTexture node.
 * <p>
 *
 * Given a SFImage this will produce an NIOBufferImage
 *
 * TODO:
 *      Needs more testing
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class OGLPixelTexture extends BasePixelTexture
	implements OGLTextureNodeType {
	
	/** Class vars for performance */
	private int texHeight;
	
	/** The height of the texture */
	private int texWidth;
	
	/** The AV3D Texture representation */
	private Texture texture;
	
	/**
	 * Construct a default instance of this node.
	 */
	public OGLPixelTexture() {
	}
	
	/**
	 * Construct a new instance of this node based on the details from the
	 * given node. If the node is not a Box node, an exception will be
	 * thrown.
	 *
	 * @param node The node to copy
	 * @throws IllegalArgumentException The node is not a Group node
	 */
	public OGLPixelTexture(VRMLNodeType node) {
		super(node);
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
	 * Get the Aviatrix3D scene graph object representation of this node. This will
	 * need to be cast to the appropriate parent type when being used.
	 *
	 * @return The Aviatrix3D representation.
	 */
	public SceneGraphObject getSceneGraphObject() {
		return texture;
	}
	
	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	/** Construct the texture from the image field data.
	 *  All calls while inSetup is true are ignored by this method.
	 */
	protected void processImageData() {
		if (vfImage == null) {
			return;
		}
	
		int width = vfImage[0];
		int height = vfImage[1];
		int components = vfImage[2];
		
		if((width*height) != (vfImageLen-3)) {
			throw new InvalidFieldValueException(
				"Incorrect number of pixels. Expecting "+
				(width*height)+" and got "+(vfImageLen-3)+"."
				);
		}
		
		ByteBuffer buffer = null;
		NIOBufferImageType format = null;
		implImage = null;
		
		switch(components) {
		case 0:
			// if you have no components, you see nothing.
			break;
			
		case 1:
			format = NIOBufferImageType.INTENSITY;
			break;
			
		case 2:
			format = NIOBufferImageType.INTENSITY_ALPHA;
			break;
			
		case 3:
			format = NIOBufferImageType.RGB;
			break;
			
		case 4:
			format = NIOBufferImageType.RGBA;
			break;
			
		default:
			throw new InvalidFieldValueException(
				"PixelTexture: Unsupported #components: " + components, null);
		}
		
		// TODO:
		// rem: the image may not be sized to power of 2 per side ?

		if ( format != null ) {
			int num_cmp = format.size;
			
			buffer = ByteBuffer.allocate( width * height * num_cmp );
			buffer.order( ByteOrder.nativeOrder( ) );
			
			switch ( num_cmp ) {
			case 4:
				for( int i = 3; i < vfImageLen; i++ ) {
					int tmp = vfImage[i];
					buffer.put((byte)((tmp >> 24) & 0xFF));
					buffer.put((byte)((tmp >> 16) & 0xFF));
					buffer.put((byte)((tmp >> 8) & 0xFF));
					buffer.put((byte)(tmp & 0xFF));
				}
				break;
				
			case 3:
				for( int i = 3; i < vfImageLen; i++ ) {
					int tmp = vfImage[i];
					buffer.put((byte)((tmp >> 16) & 0xFF));
					buffer.put((byte)((tmp >> 8) & 0xFF));
					buffer.put((byte)(tmp & 0xFF));
				}
				break;
				
			case 2:
				for( int i = 3; i < vfImageLen; i++ ) {
					int tmp = vfImage[i];
					buffer.put((byte)((tmp >> 8) & 0xFF));
					buffer.put((byte)(tmp & 0xFF));
				}
				break;
				
			case 1:
				for( int i = 3; i < vfImageLen; i++ ) {
					int tmp = vfImage[i];
					buffer.put((byte)(tmp & 0xFF));
				}
				break;
				
			default:
				// Shouldn't get here unless a new - unsupported type is created
				System.out.println( "Unhandled NIOBufferImageType: " + format );
			}
			
			implImage = new NIOBufferImage( width, height, format, buffer );
		}
		
		if(!inSetup) {
			fireTextureImageChanged(0, this, implImage, null);
		}
	}
}
