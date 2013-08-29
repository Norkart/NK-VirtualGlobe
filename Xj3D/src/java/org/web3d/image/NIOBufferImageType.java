/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.image;

// External imports
// none

// Local imports
// none

/**
 * Constant object identifiers of the image type contained by an 
 * <code>NIOBufferImage</code>.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class NIOBufferImageType {
	
	/** The INTENSITY type */
	public static final NIOBufferImageType INTENSITY = 
		new NIOBufferImageType( "INTENSITY", 1 );
	
	/** The INTENSITY_ALPHA type */
	public static final NIOBufferImageType INTENSITY_ALPHA = 
		new NIOBufferImageType( "INTENSITY_ALPHA", 2 );
	
	/** The RGB type */
	public static final NIOBufferImageType RGB = 
		new NIOBufferImageType( "RGB", 3 );
	
	/** The RGBA type */
	public static final NIOBufferImageType RGBA = 
		new NIOBufferImageType( "RGBA", 4 );
	
	/** The type identifer */
	public final String name;
	
	/** The buffer allocation per pixel */
	public final int size;
	
	/**
	 * Constructor
	 *
	 * @param name The type identifier
	 * @param size The buffer allocation per pixel of this type
	 */
	protected NIOBufferImageType ( String name, int size ) {
		this.name = name;
		this.size = size;
	}
	
	/**
	 * Return the type object that corresponds to the specified number
	 * of components per pixel. If unknown, then null is returned.
	 *
	 * @param cmp The number of components per pixel
	 * @return The type object
	 */
	public static NIOBufferImageType getType( int cmp ) {
		switch( cmp ) {
		case 1:
			return( INTENSITY );
		case 2:
			return( INTENSITY_ALPHA );
		case 3:
			return( RGB );
		case 4:
			return( RGBA );
		default:
			return( null );
		}
	}
	
	/**
	 * Return the String identifier of the type
	 *
	 * @return the String identifier of the type
	 */
	public String toString( ) {
		return( name );
	}
}
