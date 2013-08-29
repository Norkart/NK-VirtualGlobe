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
import java.util.Iterator;
import java.util.List;

// Application specific imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;

/**
 * Utility class that traverses a live VRML scene graph.
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class SceneGraphTraverser {

    /** Flag to describe if we are currently in a traversal */
    private boolean inUse;

    /** Temporary map during traversal for use references */
    private HashSet usedNodes;

    /** The detailObs for the scene graph */
    private SceneGraphTraversalDetailObserver detailObs;

    /** The detailObs for the scene graph */
    private SceneGraphTraversalSimpleObserver simpleObs;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /**
     * Create a new traverser ready to go.
     */
    public SceneGraphTraverser() {
        inUse = false;
        usedNodes = new HashSet();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Set the detailObs to be used. If an detailObs is already set, it is
     * replaced by the new one. A value of null will clear the current
     * detailObs.
     *
     * @param obs The new detailObs reference to use
     */
    public void setObserver(SceneGraphTraversalDetailObserver obs) {
        simpleObs = null;
        detailObs = obs;
    }

    /**
     * Set the detailObs to be used. If an detailObs is already set, it is
     * replaced by the new one. A value of null will clear the current
     * detailObs.
     *
     * @param obs The new detailObs reference to use
     */
    public void setObserver(SceneGraphTraversalSimpleObserver obs) {
        detailObs = null;
        simpleObs = obs;
    }

    /**
     * Traverse the given scene graph now. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no detailObs is set, this method returns immediately. This method
     * is equivalent to calling <code>traverseGraph(null, source);</code>.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(VRMLNode source)
        throws IllegalStateException {

        traverseGraph(null, source);
    }

    /**
     * Traverse the given scene graph now with the option of providing an
     * explicit, parent reference. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no detailObs is set or the source is null, this method returns
     * immediately.
     * <p>
     * A explicit root may be provided for various reasons. The most common
     * would be for loading externprotos where the root of the traversed graph
     * is actually going to be in a separate file and scene graph structure
     * from where we are starting this traversal from.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(VRMLNode parent, VRMLNode source)
        throws IllegalStateException {

        if(inUse)
            throw new IllegalStateException("Currently traversing");

        if((detailObs == null) && (simpleObs == null) || (source == null))
            return;

        inUse = true;

        try {
            if(simpleObs != null)
                processSimpleNode((VRMLNodeType)parent, -1, (VRMLNodeType)source);
//                recurseSimpleSceneGraph((VRMLNodeType)source);
            else
                recurseDetailSceneGraph((VRMLNodeType)parent, -1, (VRMLNodeType)source);
        } finally {
            // this is for error recovery
            inUse = false;
        }
    }

    /**
     * Clear the use map.  This will not be cleared between traversal calls
     */
    public void reset() {
        usedNodes.clear();
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     */
    private void recurseSimpleSceneGraphChild(VRMLNodeType parent) {

        int[] fields = parent.getNodeFieldIndices();

        if(fields == null || fields.length == 0)
            return;

        VRMLFieldData value;

        // Special-case the most common form to avoid the for-loop setup
        // costs.
        if(fields.length == 1) {
            try {
                value = parent.getFieldValue(fields[0]);

                if(value.dataType == VRMLFieldData.NODE_ARRAY_DATA) {
                    for(int i = 0; i < value.numElements; i++) {
                        if(value.nodeArrayValue[i] == null)
                            continue;

                        processSimpleNode(parent,
                                          fields[0],
                                          (VRMLNodeType)value.nodeArrayValue[i]);
                    }
                } else if(value.nodeValue != null) {
                    processSimpleNode(parent,
                                      fields[0],
                                      (VRMLNodeType)value.nodeValue);
                }
            } catch(InvalidFieldException ife) {
                // Silently catch and continue on. This error will be
                // generated when our parser picks up field values that
                // are defined but not valid for this specification
                // version. When that happens, the getFieldValue() call
                // will generate this exception.
            }
        } else {
            for(int i = 0; i < fields.length; i++) {
                try {
                    value = parent.getFieldValue(fields[i]);
                } catch(InvalidFieldException ife) {
                    // Silently catch and continue on. This error will be
                    // generated when our parser picks up field values that
                    // are defined but not valid for this specification
                    // version. When that happens, the getFieldValue() call
                    // will generate this exception.
                    continue;
                }

                if(value.dataType == VRMLFieldData.NODE_ARRAY_DATA) {
                    for(int j = 0; j < value.numElements; j++) {
                        if(value.nodeArrayValue[j] == null)
                            continue;

                        processSimpleNode(parent,
                                          fields[i],
                                          (VRMLNodeType)value.nodeArrayValue[j]);
                    }
                } else if(value.nodeValue != null) {
                    processSimpleNode(parent,
                                      fields[i],
                                      (VRMLNodeType)value.nodeValue);
                }
            }
        }
    }

    /**
     * Process a single simple node with its callback
     */
    private void processSimpleNode(VRMLNodeType parent,
                                   int field,
                                   VRMLNodeType kid) {
        boolean use = usedNodes.contains(kid);
        if(!use)
            usedNodes.add(kid);

        try {
            simpleObs.observedNode(parent, kid, field, use);
        } catch(Exception e) {
            errorReporter.warningReport("Traversal error ", e);
        }

        // now recurse
        recurseSimpleSceneGraphChild(kid);
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     * @param index The index of the field in the parent the source came from
     * @param source The node to now recurse
     */
    private void recurseDetailSceneGraph(VRMLNodeType parent,
                                         int index,
                                         VRMLNodeType source) {

        int type = source.getPrimaryType();
        boolean use = usedNodes.contains(source);
        if(!use)
            usedNodes.add(source);

        VRMLNodeType kid;
        int field;

        switch(type) {
            case TypeConstants.GroupingNodeType:
                VRMLGroupingNodeType grp = (VRMLGroupingNodeType)source;

                try {
                    detailObs.groupingNode((VRMLGroupingNodeType)parent,
                                          grp,
                                          use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                VRMLNodeType[] children = grp.getChildren();
                field = grp.getFieldIndex("children");

                for(int i = 0; i < children.length; i++) {
                    recurseDetailSceneGraph(grp, field, children[i]);
                }

                break;

            case TypeConstants.ShapeNodeType:
                VRMLShapeNodeType shape = (VRMLShapeNodeType)source;

                try {
                    detailObs.shapeNode((VRMLGroupingNodeType)parent,
                                       shape,
                                       use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                kid = shape.getAppearance();
                if(kid != null)
                    recurseDetailSceneGraph(shape, -1, kid);

                kid = shape.getGeometry();
                if(kid != null)
                    recurseDetailSceneGraph(shape, -1, kid);
                break;

            case TypeConstants.AppearanceNodeType:
                VRMLAppearanceNodeType app = (VRMLAppearanceNodeType)source;

                try {
                    detailObs.appearanceNode((VRMLShapeNodeType)parent,
                                            app,
                                            use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                kid = app.getMaterial();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                kid = app.getTexture();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                kid = app.getTextureTransform();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                break;

            case TypeConstants.SoundNodeType:
                VRMLSoundNodeType sound = (VRMLSoundNodeType)source;

                try {
                    detailObs.soundNode((VRMLGroupingNodeType)parent,
                                       sound,
                                       use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                kid = sound.getSource();
                if(kid != null)
                    recurseDetailSceneGraph(sound, -1, kid);
                break;

            case TypeConstants.MaterialNodeType:
                try {
                    detailObs.materialNode((VRMLAppearanceNodeType)parent,
                                          (VRMLMaterialNodeType)source,
                                          use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.TextureNodeType:
                try {
                    detailObs.textureNode((VRMLAppearanceNodeType)parent,
                                         (VRMLTextureNodeType)source,
                                         use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.TextureTransformNodeType:
                try {
                    detailObs.textureTransformNode(
                        (VRMLAppearanceNodeType)parent,
                        (VRMLTextureTransformNodeType)source,
                        false
                    );
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.ComponentGeometryNodeType:
            case TypeConstants.GeometryNodeType:
                try {
                    detailObs.geometryNode(
                        (VRMLShapeNodeType)parent,
                        (VRMLGeometryNodeType)source,
                                           use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if (type == TypeConstants.ComponentGeometryNodeType) {
                    VRMLNodeType[] comps = ((VRMLComponentGeometryNodeType)
                        source).getComponents();
                    for(int i=0; i < comps.length; i++) {
                        recurseDetailSceneGraph(source, -1, comps[i]);
                    }

                }
                break;

            case TypeConstants.CoordinateNodeType:
            case TypeConstants.NormalNodeType:
            case TypeConstants.TextureCoordinateNodeType:
            case TypeConstants.ColorNodeType:
                try {
                    detailObs.geometricPropertyNode(
                        (VRMLComponentGeometryNodeType)parent,
                        (VRMLGeometricPropertyNodeType)source,
                                           use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.AudioClipNodeType:
                try {
                    detailObs.audioClipNode((VRMLSoundNodeType)parent,
                                           (VRMLAudioClipNodeType)source,
                                           use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.InterpolatorNodeType:
                try {
                    detailObs.interpolatorNode((VRMLGroupingNodeType)parent,
                                        (VRMLInterpolatorNodeType)source,
                                        use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.PointingDeviceSensorNodeType:
            case TypeConstants.DragSensorNodeType:
            case TypeConstants.KeyDeviceSensorNodeType:
            case TypeConstants.EnvironmentalSensorNodeType:
            case TypeConstants.DeviceSensorNodeType:
            case TypeConstants.SensorNodeType:
                try {
                    detailObs.sensorNode((VRMLGroupingNodeType)parent,
                                        (VRMLSensorNodeType)source,
                                        use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;


            case TypeConstants.InlineNodeType:
                try {
                    detailObs.inlineNode((VRMLGroupingNodeType)parent,
                                        (VRMLInlineNodeType)source,
                                        use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;


            case TypeConstants.LightNodeType:
                try {
                    detailObs.lightNode((VRMLGroupingNodeType)parent,
                                       (VRMLLightNodeType)source,
                                       use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.ViewpointNodeType:
            case TypeConstants.NavigationInfoNodeType:
            case TypeConstants.BackgroundNodeType:
            case TypeConstants.FogNodeType:
            case TypeConstants.BindableNodeType:
                try {
                    detailObs.bindableNode((VRMLGroupingNodeType)parent,
                                          (VRMLBindableNodeType)source,
                                          use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;

            case TypeConstants.ScriptNodeType:
                VRMLScriptNodeType script = (VRMLScriptNodeType)source;

                try {
                    detailObs.scriptNode((VRMLGroupingNodeType)parent,
                                        script,
                                        use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                // now let's look at all the fields for any SF/MFNode fields
                // and traverse those.
                List fields = script.getAllFields();
                processFieldList(script, fields);

                break;

            case TypeConstants.ProtoInstance:
                try {
                    detailObs.protoNode(parent,
                                       (VRMLProtoInstance)source,
                                       use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }

                if(use)
                    break;

                VRMLProtoInstance proto = (VRMLProtoInstance)source;
                fields = proto.getAllFields();
                processFieldList(proto, fields);
                break;

            default:
                try {
                    detailObs.miscellaneousNode(parent,
                                               index,
                                               source,
                                               use);
                } catch(Exception e) {
                    errorReporter.warningReport("Traversal error ", e);
                }
                break;
        }
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism.
     *
     * @param source The root of the current item to traverse
     */
    private void recurseDetailSceneGraphInternal(VRMLNodeType parent,
                                           int index,
                                           VRMLNodeType source) {
        if (source == null)
            return;

        int type = source.getPrimaryType();
        boolean use = usedNodes.contains(source);
        VRMLNodeType kid;

        if(!use)
            usedNodes.add(source);

        try {
            detailObs.customNode(parent, index, source, use);
        } catch(Exception e) {
            errorReporter.warningReport("Traversal error ", e);
        }

        if(use)
            return;

        switch(type) {
            case TypeConstants.GroupingNodeType:
                VRMLGroupingNodeType grp = (VRMLGroupingNodeType)source;

                VRMLNodeType[] children = grp.getChildren();
                int field = grp.getFieldIndex("children");

                for(int i = 0; i < children.length; i++) {
                    recurseDetailSceneGraph(grp, field, children[i]);
                }

                break;

            case TypeConstants.ShapeNodeType:
                VRMLShapeNodeType shape = (VRMLShapeNodeType)source;

                kid = shape.getAppearance();
                if(kid != null)
                    recurseDetailSceneGraph(shape, -1, kid);

                kid = shape.getGeometry();
                if(kid != null)
                    recurseDetailSceneGraph(shape, -1, kid);
                break;

            case TypeConstants.AppearanceNodeType:
                VRMLAppearanceNodeType app = (VRMLAppearanceNodeType)source;

                kid = app.getMaterial();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                kid = app.getTexture();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                kid = app.getTextureTransform();
                if(kid != null)
                    recurseDetailSceneGraph(app, -1, kid);

                break;

            case TypeConstants.SoundNodeType:
                VRMLSoundNodeType sound = (VRMLSoundNodeType)source;
                kid = sound.getSource();
                if(kid != null)
                    recurseDetailSceneGraph(sound, -1, kid);
                break;

            case TypeConstants.ScriptNodeType:
                VRMLScriptNodeType script = (VRMLScriptNodeType)source;

                // now let's look at all the fields for any SF/MFNode fields
                // and traverse those.
                List fields = script.getAllFields();
                processFieldList(script, fields);
                break;

            case TypeConstants.ProtoInstance:
                VRMLProtoInstance proto = (VRMLProtoInstance)source;
                fields = proto.getAllFields();
                processFieldList(proto, fields);
                break;

            // do nothing with these.
            case TypeConstants.MaterialNodeType:
            case TypeConstants.TextureNodeType:
            case TypeConstants.TextureTransformNodeType:
            case TypeConstants.GeometryNodeType:
            case TypeConstants.AudioClipNodeType:
            case TypeConstants.SensorNodeType:
            case TypeConstants.DeviceSensorNodeType:
            case TypeConstants.InlineNodeType:
            case TypeConstants.LightNodeType:
            case TypeConstants.BindableNodeType:
        }
    }

    /**
     * Process the fields of a proto or script node.
     *
     * @param fields The list of fields
     */
    private void processFieldList(VRMLNodeType parent, List fields) {

        Iterator itr = fields.iterator();

        while(itr.hasNext()) {
            VRMLFieldDeclaration decl =
                (VRMLFieldDeclaration)itr.next();

            int type = decl.getFieldType();

            if((type != FieldConstants.SFNODE) &&
               (type != FieldConstants.MFNODE))
                continue;

            int access = decl.getAccessType();

            if((access == FieldConstants.EVENTIN) ||
               (access == FieldConstants.EVENTOUT))
                    continue;

            int index = parent.getFieldIndex(decl.getName());
            VRMLFieldData data;

            try {
                data = parent.getFieldValue(index);
            } catch(InvalidFieldException ife) {
                errorReporter.warningReport("Traverser can't find field data", null);
                continue;
            }

            if (data == null) {
                // Catch for dealing with unloaded ExternProtos
                errorReporter.warningReport("Traverser can't find field data", null);
                continue;
            }

            if(data.dataType == VRMLFieldData.NODE_DATA) {

                recurseDetailSceneGraphInternal(parent,
                                  index,
                                  (VRMLNodeType)data.nodeValue);
            } else {
                for(int i = 0; i < data.numElements; i++) {
                    VRMLNodeType n = (VRMLNodeType)data.nodeArrayValue[i];
                    recurseDetailSceneGraphInternal(parent, index, n);
                }
            }
        }
    }
}
