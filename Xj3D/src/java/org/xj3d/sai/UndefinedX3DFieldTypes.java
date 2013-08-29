/*****************************************************************************
 *                        Web3D.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

/**
 * Listing of type constants for X3D fields that have been included in revisions of the 
 * abstract spec (19775) that post date the java bindings spec. Presumably 
 * these type constants will be relocated to org.web3D.x3D.sai.X3DFieldTypes
 * when 19777 is updated. 
 * <p>
 * There has been no attempt to anticipate the constant assignment. For the 
 * purposes of this class, the hashcode of the node type string have been used.
 * It is therefore, highly unlikely that these constants will match those of
 * a later binding spec.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface UndefinedX3DFieldTypes {
    
	public static final int SFMATRIX3D = "SFMATRIX3D".hashCode( );
	public static final int MFMATRIX3D = "MFMATRIX3D".hashCode( );
	public static final int SFMATRIX3F = "SFMATRIX3F".hashCode( );
	public static final int MFMATRIX3F = "MFMATRIX3F".hashCode( );
	public static final int SFMATRIX4D = "SFMATRIX4D".hashCode( );
	public static final int MFMATRIX4D = "MFMATRIX4D".hashCode( );
	public static final int SFMATRIX4F = "SFMATRIX4F".hashCode( );
	public static final int MFMATRIX4F = "MFMATRIX4F".hashCode( );
	public static final int SFVEC4D = "SFVEC4D".hashCode( );
	public static final int MFVEC4D = "MFVEC4D".hashCode( );
	public static final int SFVEC4F = "SFVEC4F".hashCode( );
	public static final int MFVEC4F = "MFVEC4F".hashCode( );
}
