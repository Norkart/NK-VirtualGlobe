/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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
 * Listing of type constants for X3D nodes that either were omitted from the
 * java bindings spec (19777) or, have been included in revisions of the 
 * abstract spec (19775) that post date the java bindings spec. Presumably 
 * these type constants will be relocated to org.web3d.x3d.sai.X3DNodeTypes
 * when 19777 is updated. 
 * <p>
 * There has been no attempt to anticipate the constant assignment. For the 
 * purposes of this class, the hashcode of the node type string have been used.
 * It is therefore, highly unlikely that these constants will match those of
 * a later binding spec.
 * <p>
 * As with the X3DNodeTypes, these type constants only represent the node
 * classification types (abstract types), not the individual node type.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface UndefinedX3DNodeTypes {
    
	public static final int X3DChaserNode = "X3DChaserNode".hashCode( );
	public static final int X3DComposedGeometryNode = "X3DComposedGeometryNode".hashCode( );
	public static final int X3DDamperNode = "X3DDamperNode".hashCode( );
	public static final int X3DEnvironmentTextureNode = "X3DEnvironmentTextureNode".hashCode( );
	public static final int X3DFogObject = "X3DFogObject".hashCode( );
	public static final int X3DFollowerNode = "X3DFollowerNode".hashCode( );
	public static final int X3DLayerNode = "X3DLayerNode".hashCode( );
	public static final int X3DLayoutNode = "X3DLayoutNode".hashCode( );
	public static final int X3DNBodyCollidableNode = "X3DNBodyCollidableNode".hashCode( );
	public static final int X3DNBodyCollisionSpaceNode = "X3DNBodyCollisionSpaceNode".hashCode( );
	public static final int X3DNode = "X3DNode".hashCode( );
	public static final int X3DParticleEmitterNode = "X3DParticleEmitterNode".hashCode( );
	public static final int X3DParticlePhysicsModelNode = "X3DParticlePhysicsModelNode".hashCode( );
	public static final int X3DPickableObject = "X3DPickableObject".hashCode( );
	public static final int X3DPickingNode = "X3DPickingNode".hashCode( );
	public static final int X3DProductStructureChildNode = "X3DProductStructureChildNode".hashCode( );
	public static final int X3DProgrammableShaderObject = "X3DProgrammableShaderObject".hashCode( );
	public static final int X3DRigidJointNode = "X3DRigidJointNode".hashCode( );
	public static final int X3DShaderNode = "X3DShaderNode".hashCode( );
	public static final int X3DSoundNode = "X3DSoundNode".hashCode( );
	public static final int X3DTexture3DNode = "X3DTexture3DNode".hashCode( );
	public static final int X3DVertexAttributeNode = "X3DVertexAttributeNode".hashCode( );
	public static final int X3DViewpointNode = "X3DViewpointNode".hashCode( );
    public static final int X3DViewportNode = "X3DViewportNode".hashCode( );
}
