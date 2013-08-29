/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
// none

// Local imports
// none

/**
 * An observer interface to look at the structure of the VRML runtime scene
 * graph presented by the {@link org.web3d.vrml.nodes.SceneGraphTraverser}.
 * <p>
 *
 * As this interface is an observer of the traversal process, it may perform
 * many tasks, such as building an equivalent API. However, it should not
 * attempt to make calls back to this class. For speed reasons, only one
 * observer can be registered at a time. We feel this is the most common way
 * that the class will be used (usually to generate alternate scene graphs)
 * and the overheads of dealing with loops for multiple observers is not
 * worth it.
 * <p>
 *
 * The observer will report the top of a use hierarchy. If the traverser, in
 * it's internal references, detects a reference re-use that is indicative of
 * a DEF/USE situation then the flag passed with each method call will be set
 * true. After reporting the USE, the traverser will not descend that part of
 * the scene graph any further.
 * <p>
 *
 * When reporting the parent node, if the root is the root node of the VRML
 * file or the body of a Proto, the parent reference will be null - regardless
 * of the type of node.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SceneGraphTraversalDetailObserver {

    /**
     * Notification of a grouping node.
     *
     * @param parent The parent group of this node
     * @param group The grouping node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void groupingNode(VRMLGroupingNodeType parent,
                             VRMLGroupingNodeType group,
                             boolean used);

    /**
     * Notification of an inline node.
     *
     * @param parent The parent group of this node
     * @param inline The inline node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void inlineNode(VRMLGroupingNodeType parent,
                           VRMLInlineNodeType inline,
                           boolean used);

    /**
     * Notification of a light node.
     *
     * @param parent The parent group of this node
     * @param light The light node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void lightNode(VRMLGroupingNodeType parent,
                          VRMLLightNodeType light,
                          boolean used);

    /**
     * Notification of a bindable node.
     *
     * @param parent The parent group of this node
     * @param bindable The bindable node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void bindableNode(VRMLGroupingNodeType parent,
                             VRMLBindableNodeType bindable,
                             boolean used);

    /**
     * Notification of a shape node.
     *
     * @param parent The parent group of this node
     * @param shape The shape node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void shapeNode(VRMLGroupingNodeType parent,
                          VRMLShapeNodeType shape,
                          boolean used);

    /**
     * Notification of an appearance node.
     *
     * @param parent The parent shape node of this node
     * @param appearance The appearance node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void appearanceNode(VRMLShapeNodeType parent,
                               VRMLAppearanceNodeType appearance,
                               boolean used);

    /**
     * Notification of a Material node.
     *
     * @param parent The parent appearance of this node
     * @param material The material node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void materialNode(VRMLAppearanceNodeType parent,
                             VRMLMaterialNodeType material,
                             boolean used);

    /**
     * Notification of a texture node. If the node is a movie texture node
     * it will be reported here as well as the audioclip notification when
     * the child of a sound node.
     *
     * @param parent The parent appearance of this node
     * @param texture The texture node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void textureNode(VRMLAppearanceNodeType parent,
                            VRMLTextureNodeType texture,
                            boolean used);

    /**
     * Notification of a TextureTransform node.
     *
     * @param parent The parent appearance of this node
     * @param transform The texture transform node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void textureTransformNode(VRMLAppearanceNodeType parent,
                                     VRMLTextureTransformNodeType transform,
                                     boolean used);

    /**
     * Notification of a geometry node.
     *
     * @param parent The parent shape node of this node
     * @param geometry The geometry node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void geometryNode(VRMLShapeNodeType parent,
                             VRMLGeometryNodeType geometry,
                             boolean used);

    /**
     * Notification of a geometric property node.
     *
     * @param parent The parent component geometry  of this node
     * @param geoprop The geometric property node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void geometricPropertyNode(VRMLComponentGeometryNodeType parent,
                                     VRMLGeometricPropertyNodeType geoprop,
                                     boolean used);
    /**
     * Notification of a sound node.
     *
     * @param parent The parent group of this node
     * @param sound The sound node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void soundNode(VRMLGroupingNodeType parent,
                          VRMLSoundNodeType sound,
                          boolean used);

    /**
     * Notification of an AudioClip based node. This may be a movie texture
     * or ordinary audio clip node.
     *
     * @param parent The parent sound node of this node
     * @param clip The audioclip node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void audioClipNode(VRMLSoundNodeType parent,
                              VRMLAudioClipNodeType clip,
                              boolean used);

    /**
     * Notification of any form of sensor node. Anchors are not considered to
     * be an anchor node.
     *
     * @param parent The parent group of this node
     * @param sensor The sensor node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void sensorNode(VRMLGroupingNodeType parent,
                           VRMLSensorNodeType sensor,
                           boolean used);

    /**
     * Notification of any form of interpolator node
     *
     * @param parent The parent group of this node
     * @param interp The interpolator node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void interpolatorNode(VRMLGroupingNodeType parent,
                           VRMLInterpolatorNodeType interp,
                           boolean used);

    /**
     * Notification of a script node.
     *
     * @param parent The parent group of this node
     * @param script The script node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void scriptNode(VRMLGroupingNodeType parent,
                           VRMLScriptNodeType script,
                           boolean used);

    /**
     * Notification of a proto instance.
     *
     * @param parent The parent node of this node
     * @param proto The proto node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void protoNode(VRMLNodeType parent,
                          VRMLProtoInstance proto,
                          boolean used);

    /**
     * Notification of a custom node that defines its own children types.
     * A typical custom node is a proto or script.
     *
     * @param parent The parent node of this node
     * @param fieldIndex The index of the field that the child belongs to
     * @param child The child node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void customNode(VRMLNodeType parent,
                           int fieldIndex,
                           VRMLNodeType child,
                           boolean used);

    /**
     * Notification of an unknown or miscellaneous node type that does not
     * fit into any of the other categories. For example WorldInfo.
     *
     * @param parent The parent node of this node
     * @param fieldIndex The index of the field that the child belongs to
     * @param node The node that has been found
     * @param used true if the node reference is actually a USE
     */
    public void miscellaneousNode(VRMLNodeType parent,
                                  int fieldIndex,
                                  VRMLNodeType node,
                                  boolean used);
}
