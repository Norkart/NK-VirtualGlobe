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

package org.web3d.vrml.renderer;

// Standard imports
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoFieldInfo;
import org.web3d.vrml.nodes.proto.ProtoScene;

/**
 * A class that is used to create real instances of protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * We have a small conundrum to deal with - if the proto definition contains
 * SF/MFNode fields, we don't know whether the values should be also generated
 * as real runtime nodes too. Maybe the usage of this node will provide values
 * that are dealt with after this class has finished. Other times, these defaul
 * values must be used. For this implementation, we have gone with the
 * safety-first approach: Always parse the definition of any SF or MFNode field
 * and turn those into runtime instances. Although this may create extra
 * garbage, there seems to be no nice way of dealing with this issue without a
 * completely different architecture for the library.
 * <p>
 *
 * Note:
 * Under the current implementation, EXTERNPROTOs are not yet catered for.
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class ProtoCopier implements SceneGraphTraversalSimpleObserver {

    /** The factory that is used to generate the runtime version */
    private VRMLNodeFactory factory;

    /** The scene as we are building it up during traversal */
    private ProtoScene scene;

    /** Frame state manager for this event model instance */
    private FrameStateManager stateManager;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Mapping of declaration node to the CR version */
    private Map nodeMap;

    /** The proto instance we're building up */
    private CRProtoInstance protoInstance;

    /** The current world's root URL */
    private String worldURL;

    /** The major version of the spec this file belongs to. */
    private int majorVersion;

    /** The minor version of the spec this file belongs to. */
    private int minorVersion;

    /** Whether this node is static */
    private boolean staticNodes;

    /**
     * Create a new copier that uses the given world URL and node factory.
     *
     * @param fac The factory to use (Must generate VRMLNodeType instances)
     */
    public ProtoCopier(VRMLNodeFactory fac, String worldURL) {

        if(fac == null)
            throw new NullPointerException("No node factory supplied");

        factory = fac;
        this.worldURL = worldURL;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the frame state manager to use for the builder after
     * this point. Set a value of null it will clear the currently set items.
     *
     * @param fsm The state manager to use
     */
    public void setFrameStateManager(FrameStateManager fsm) {
        stateManager = fsm;
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
     * Build an instance of the proto body from the given description.
     *
     * @param src The node representing the proto to add root nodes to
     * @param scn The scene to add node details to
     * @param nodeMap The map to store node references in
     * @param version The version of VRML/X3D to use, 2.0 or 3.0
     * @param staticNode Whether this node is will be modified
     * @return The copied root node tree
     */
    void copyNode(CRProtoInstance src,
                  ProtoScene scn,
                  Map nodeMap,
                  int major,
                  int minor,
                  boolean staticNodes) {

        if(scn == null)
            throw new NullPointerException("No scene supplied");

        scene = scn;
        protoInstance = src;
        majorVersion = major;
        minorVersion = minor;
        this.nodeMap = nodeMap;
        this.staticNodes = staticNodes;
    }

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {
        VRMLNodeType out_kid;

        if(used) {
            out_kid = (VRMLNodeType)nodeMap.get(child);
            if (!out_kid.isDEF())
                out_kid.setDEF();
        } else if(!(child instanceof VRMLProtoInstance)) {
            out_kid = (VRMLNodeType)factory.createVRMLNode(child,
                                                           staticNodes);

            if(out_kid == null) {
                errorReporter.warningReport("ProtoCopier is unable to make " +
                                            "a copy of " +
                                            child.getVRMLNodeName(), null);
                return;
            }

            out_kid.setFrameStateManager(stateManager);
            scene.addNode(out_kid);
            nodeMap.put(child, out_kid);

            if(out_kid instanceof VRMLInlineNodeType)
                ((VRMLInlineNodeType)out_kid).setParentSpace(protoInstance);
            else if(out_kid instanceof VRMLScriptNodeType)
                ((VRMLScriptNodeType)out_kid).setExecutionSpace(protoInstance);

            if(out_kid instanceof VRMLExternalNodeType)
                ((VRMLExternalNodeType)out_kid).setWorldUrl(worldURL);
        } else {
            // The proto instance has already been copied before it got here,
            // so just apply directly.
            scene.addNode(child);
            out_kid = child;
            out_kid.setFrameStateManager(stateManager);
            nodeMap.put(child, out_kid);
        }

        if((parent == null) || (field == -1)) {
            protoInstance.addBodyChild(out_kid);
        } else {
            VRMLNodeType out_parent = (VRMLNodeType)nodeMap.get(parent);
            VRMLFieldDeclaration decl = parent.getFieldDeclaration(field);

            int idx = out_parent.getFieldIndex(decl.getName());

            try {
                out_parent.setValue(idx, out_kid);
            } catch(FieldException ife) {
                errorReporter.warningReport(
                    "ProtoCopier error setting copied node! Parent:" +
                    parent.getClass() + " child: " + child.getClass() +
                   " field " + field, ife);
            }
        }
    }

    //----------------------------------------------------------
    // Public convenience  methods
    //----------------------------------------------------------

    /**
     * Notification of a completed proto instance that needs to be registered
     * with its parent node.
     *
     * @param parent The parent node of this node
     * @param proto The proto node that has been found
     */
    public void protoNode(VRMLNodeType parent, CRProtoInstance proto) {

        if(parent == null) {
            protoInstance.addBodyChild(proto);
            return;
        }

        scene.addNode(proto);


        VRMLNodeType out_parent = (VRMLNodeType)nodeMap.get(parent);
        int parent_type = parent.getPrimaryType();
        int child_type = proto.getImplementationNode().getPrimaryType();
        int child_index;

        switch(parent_type) {
            case TypeConstants.GroupingNodeType:
                // Not entirely sure this will work. If setupFinished has been
                // called on the parent group then this will replace the
                // current children rather than add to it. May need an addChild
                // method in groupingNodeType.
                ((VRMLGroupingNodeType)out_parent).addChild(proto);
                break;

            case TypeConstants.ShapeNodeType:
                if(child_type == TypeConstants.AppearanceNodeType)
                    child_index = out_parent.getFieldIndex("appearance");
                else
                    child_index = out_parent.getFieldIndex("geometry");

                try {
                    ((VRMLShapeNodeType)out_parent).setValue(child_index,
                                                             proto);
                } catch(FieldException ife) {
                    // should never have this
                }
                break;

            case TypeConstants.AppearanceNodeType:
                switch(child_type) {
                    case TypeConstants.MaterialNodeType:
                        child_index = out_parent.getFieldIndex("material");
                        break;
                    case TypeConstants.TextureNodeType:
                        child_index = out_parent.getFieldIndex("texture");
                        break;
                    case TypeConstants.TextureTransformNodeType:
                        child_index =
                            out_parent.getFieldIndex("textureTransform");
                        break;
                    default:
                        child_index = -1;
                }

                try {
                    ((VRMLAppearanceNodeType)out_parent).setValue(child_index,
                                                                  proto);
                } catch(FieldException ife) {
                    // should never have this
                }
                break;

            case TypeConstants.SoundNodeType:
                child_index = out_parent.getFieldIndex("source");

                try {
                    ((VRMLSoundNodeType)out_parent).setValue(child_index,
                                                             proto);
                } catch(FieldException ife) {
                    // should never have this
                }
                break;

            // The following cases we do nothing with
            case TypeConstants.MaterialNodeType:
            case TypeConstants.TextureNodeType:
            case TypeConstants.TextureTransformNodeType:
            case TypeConstants.GeometryNodeType:
            case TypeConstants.AudioClipNodeType:
            case TypeConstants.DeviceSensorNodeType:
            case TypeConstants.SensorNodeType:
            case TypeConstants.InlineNodeType:
            case TypeConstants.LightNodeType:
            case TypeConstants.BindableNodeType:
                break;

            case TypeConstants.ScriptNodeType:
System.out.println("ProtoCopier can't handle script fields containing protos. Must fix");
        }
    }
}
