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

package org.web3d.vrml.lang;

// External imports
// None

// Local imports
// None

/**
 * Listing of type constants for nodes.
 * <p>
 * Each interface in the vrml.nodes area will have an entry.  These will be
 * used to make parsing faster by allowing the use of switch statements instead
 * of large if/else
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.35 $
 */
public interface TypeConstants {
    /** Convenience representation for no secondary node type */
    public static final int[] NO_SECONDARY_TYPE = new int[0];

    /** Indicator that there is no primary type set */
    public static final int NONE = -1;

    /**
     * The node is currently a proxy for an import statement that has not yet
     * been resolved. Once it has been resolved, we will know better and this
     * value will be replaced by that of the underlying node.
     */
    public static final int UNRESOLVED_IMPORT_PROXY = -2;

    public int AppearanceChildNodeType = 1;
    public int AppearanceNodeType = 2;
    public int AudioClipNodeType = 3;
    public int BackgroundNodeType = 4;
    public int BindableNodeType = 5;
    public int BoundedNodeType = 6;
    public int ChildNodeType = 7;
    public int CollidableNodeType = 8;
    public int ColorNodeType = 9;
    public int CoordinateNodeType = 11;
    public int DragSensorNodeType = 13;
    public int EnvironmentalSensorNodeType = 14;
    public int ExternalNodeType = 16;
    public int ExternProtoDeclare = 17;
    public int FogNodeType = 18;
    public int FontStyleNodeType = 19;
    public int GeometricPropertyNodeType = 20;
    public int GeometryNodeType = 21;
    public int GroupingNodeType = 22;
    public int InlineNodeType = 24;
    public int InterpolatorNodeType = 25;
    public int KeyDeviceSensorNodeType = 26;
    public int LightNodeType = 27;
    public int LinkNodeType = 28;
    public int MaterialNodeType = 29;
    public int MultiExternalNodeType = 30;
    public int NavigationInfoNodeType = 31;
    public int NodeType = 33;
    public int NormalNodeType = 34;
    public int ParametricGeometryNodeType = 35;
    public int PointingDeviceSensorNodeType = 36;
    public int ProtoDeclare = 37;
    public int ProtoInstance = 38;
    public int ScriptNodeType = 41;
    public int SensorNodeType = 42;
    public int ShapeNodeType = 43;
    public int SingleExternalNodeType = 44;
    public int SoundNodeType = 45;
    public int SurfaceMaterialNodeType = 46;
    public int Texture2DNodeType = 47;
    public int TextureCoordinateNodeType = 48;
    public int TextureNodeType = 50;
    public int TextureTransformNodeType = 51;
    public int TimeDependentNodeType = 52;
    public int TimeControlledNodeType = 53;
    public int ViewpointNodeType = 55;
    public int VisualMaterialNodeType = 56;
    public int WorldRootNodeType = 57;
    public int ComponentGeometryNodeType = 58;
    public int StaticNodeType = 59;
    public int SurfaceNodeType = 60;
    public int SurfaceChildNodeType = 61;
    public int SurfaceLayoutNodeType = 62;
    public int OverlayNodeType = 63;

    /** Primary type indicating the node emits particles for a particle system */
    public int ParticleEmitterNodeType = 64;

    /**
     * Primary type indicating the node controls the particles trajectory during
     * a running particle system.
     */
    public int ParticlePhysicsModelNodeType = 65;

    /** Primary type indicating the node is a complete particle system */
    public int ParticleSystemNodeType = 66;
    public int InfoNodeType = 67;
    public int SequencerNodeType = 68;
    public int Texture3DNodeType = 69;
    public int TouchSensorNodeType = 70;
    public int EnvironmentTextureNodeType = 71;
    public int MetadataObjectNodeType = 72;
    public int PickingSensorNodeType = 73;
    public int ExternalSynchronizedNodeType = 74;
    public int NetworkInterfaceNodeType = 75;
    public int ProductStructureChildNodeType = 76;
    public int HumanoidNodeType = 77;
    public int DeviceSensorNodeType = 78;
    public int DeviceManagerNodeType = 79;

    /** Secondary type indicating the node can be a target for picking */
    public int PickTargetNodeType = 80;

    /** Primary type for being a Joint in a rigid body physics system */
    public int RigidJointNodeType = 81;

    /** Primary type for being a single body for physics */
    public int RigidBodyNodeType = 82;

    /** Primary type for being a collection of bodies for physics */
    public int RigidBodyCollectionNodeType = 83;

    /** Primary type for nodes that can collide against each other */
    public int nBodyCollidableNodeType = 84;

    /** Primary type for being a collision space */
    public int nBodyCollisionSpaceNodeType = 85;

    /** Primary type for being a collection of collision spaces */
    public int nBodyCollisionCollectionNodeType = 86;

    /** Primary type for being a nbody collision sensor */
    public int nBodyCollisionSensorNodeType = 87;

    /** Secondard type for being dependent on viewer movements */
    public int ViewDependentNodeType = 88;

    /** Primary type for nodes that hold a single rendering layer */
    public int LayerNodeType = 89;

    /** Primary type for nodes that contain collections of layers */
    public int LayerSetNodeType = 90;

    /** Primary type for nodes that contain source for a shader program */
    public int ShaderProgramNodeType = 91;

    /** Primary type for nodes that are a viewport for a layer */
    public int ViewportNodeType = 92;

    /** Primary type for PointProperties node */
    public int PointPropertiesNodeType = 93;

    /** A secondary type for CADLayers */
    public int CADLayerNodeType = 94;

    /**
     * A secondary type for CADAssemblies, in addition to being a product
     * structure node.
     */
    public int CADAssemblyNodeType = 95;

    /** Primary type for annotation target nodes */
    public int AnnotationTargetType = 96;

    /** Primary type for annotation nodes that go into annotation targets */
    public int AnnotationType = 97;


    /**
     * The last identifier used by the internal representations. If you are
     * extending Xj3D with your own node types, then any identifiers you assign
     * should start with a number greater than this number.
     */
    public int LAST_NODE_TYPE_ID = 1000;
}
