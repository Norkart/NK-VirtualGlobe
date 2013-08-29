/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.hanim;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.SceneGraphObject;

import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.renderer.aviatrix3d.geom.hanim.AVHumanoid;
import org.j3d.renderer.aviatrix3d.geom.hanim.AVHumanoidPart;
import org.j3d.renderer.aviatrix3d.geom.hanim.AVShaderHAnimFactory;
import org.j3d.renderer.aviatrix3d.geom.hanim.AVSoftwareHAnimFactory;
import org.j3d.renderer.aviatrix3d.geom.hanim.SoftwareHumanoid;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.common.nodes.hanim.BaseHAnimHumanoid;

/**
 * OpenGL implementation of a HAnimHumanoid node.
 * <p>
 *
 * This implementation looks like the standard group node but
 *
 * @author Justin Couch
 * @version $Revision: 2.4 $
 */
public class OGLHAnimHumanoid extends BaseHAnimHumanoid
    implements OGLVRMLNode {

    /** Property for allowing the use of Hardware shaders or not */
    private static final String HARDWARE_RENDER_PROP =
        "org.web3d.vrml.renderer.ogl.nodes.hanim.allowHardwareHumanoid";

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** Message for when the geometry qualifies for hardware accelaration */
    private static final String HARDWARE_USED_MSG =
        "Skin nodes provided to the HAnimHumanoid allows the use of hardware " +
        "accelarated shader techniques";

    /** Message for when the geometry can only use software rendering */
    private static final String SOFTWARE_USED_MSG =
        "Skin nodes provided prevent the use hardware accelerated " +
        "rendering techniques";

    /**
     * Message issued the first time an instance of this node is created if
     * the use of hardware-accelerated shaders are provided.
     */
    private static final String USING_HARDWARE_MSG =
        "HAnimHumanoids are allowed to use the hardware-accelerated shader " +
        "implementation where possible";

    /**
     * The set of node names for geometry that we can use for hardware
     * accelerating the humanoid rendering.
     */
    private static HashSet hardwareCapableGeom;

    /** Will the user allow the use of hardware rendered shaders. */
    private static final boolean allowHardwareRender;

    /** Used to only issue the hardware message on first creation of node */
    private static boolean hardwarePropNotice;

    /** Flag to know if we're using hardware or software rendering */
    private boolean hardwareRendered;

    /**
     * Static constructor to initialise the hardware list.
     */
    static {
        hardwareCapableGeom = new HashSet();
        hardwareCapableGeom.add("PointSet");
        hardwareCapableGeom.add("LineSet");
        hardwareCapableGeom.add("IndexedLineSet");
        hardwareCapableGeom.add("TriangleSet");
        hardwareCapableGeom.add("TriangleStripSet");
        hardwareCapableGeom.add("TriangleFanSet");
        hardwareCapableGeom.add("IndexedTriangleSet");
        hardwareCapableGeom.add("IndexedTriangleStripSet");
        hardwareCapableGeom.add("IndexedTriangleFanSet");

        Boolean prop = (Boolean)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // privileged code goes here, for example:
                    boolean val = Boolean.getBoolean(HARDWARE_RENDER_PROP);
                    return Boolean.valueOf(val);
                }
            }
        );

        allowHardwareRender = prop.booleanValue();
        hardwarePropNotice = !allowHardwareRender;
    }


    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLHAnimHumanoid() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLHAnimHumanoid(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLHAnimHumanoidNodeType
    //----------------------------------------------------------

    /**
     * Notification that the event model is complete and skeleton should
     * perform all it's updates now.
     */
    public void updateMesh() {
        super.updateMesh();

        if(!hardwareRendered) {
            // copy the skin output values to the array.

            if(vfSkinCoord != null)
            {
                int num_points = hanimImpl.numSkinCoord();
                Object coords =
                    ((SoftwareHumanoid)hanimImpl).getUpdatedSkinCoords();

                if(coords instanceof float[])
                    vfSkinCoord.setPoint((float[])coords, num_points * 3);
            }

            if(vfSkinNormal != null)
            {
                int num_points = hanimImpl.numSkinNormal();
                Object coords =
                    ((SoftwareHumanoid)hanimImpl).getUpdatedSkinNormals();

                if(coords instanceof float[])
                    vfSkinNormal.setVector((float[])coords, num_points * 3);
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLHAnimNodeType
    //----------------------------------------------------------

    /**
     * Set the current node factory to use. If this is set again, replace the
     * current implementation node with a new one from this new instance. This
     * may be needed at times when the user makes a change that forces the old
     * way to be incompatible and thus needing a different implementation.
     *
     * @param fac The new factory instance to use
     */
    public void setHAnimFactory(HAnimFactory fac) {

        super.setHAnimFactory(fac);

        // Run through the list of children and add their scene graph
        // objects to this site using the renderer-specific objects.
        int num_vps = vfViewpoints.size();

        Node[] av_vp = new Node[num_vps];

        for(int i = 0; i < num_vps; i++) {
            OGLVRMLNode vp = (OGLVRMLNode)vfViewpoints.get(i);
            av_vp[i] = (Node)vp.getSceneGraphObject();
        }

        hanimImpl.setViewpoints(av_vp, num_vps);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // Determine which factory we should use and set it
        checkFactoryChoice();

        int num_skin = vfSkin.size();

        Node[] av_skin = new Node[num_skin];

        for(int i = 0; i < num_skin; i++) {
            OGLVRMLNode skin = (OGLVRMLNode)vfSkin.get(i);
            av_skin[i] = (Node)skin.getSceneGraphObject();
        }

        ((AVHumanoid)hanimImpl).setSkin(av_skin, num_skin);

        int num_vps = vfViewpoints.size();

        Node[] av_vp = new Node[num_vps];

        for(int i = 0; i < num_vps; i++) {
            OGLVRMLNode vp = (OGLVRMLNode)vfViewpoints.get(i);
            av_vp[i] = (Node)vp.getSceneGraphObject();
        }

        hanimImpl.setViewpoints(av_vp, num_vps);
    }

    /**
     * Handle notification that an ExternProto has resolved.
     *
     * @param index The field index that got loaded
     * @param node The owner of the node
     */
    public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node) {

        if(!(node instanceof VRMLChildNodeType) && !(node instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(BAD_PROTO_MSG);

        // TODO: This does not totally guard against notifications during setupFinished as
        // the base class sets inSetup finish true before J3D structures are complete

        if(inSetup)
            return;

        OGLVRMLNode kid = (OGLVRMLNode)node;

        // Make sure the child is finished first.
        kid.setupFinished();

        switch(index) {
            case FIELD_SKIN:
                int num_skin = vfSkin.size();

                Node[] av_skin = new Node[num_skin];

                for(int i = 0; i < num_skin; i++) {
                    OGLVRMLNode skin = (OGLVRMLNode)vfSkin.get(i);
                    av_skin[i] = (Node)skin.getSceneGraphObject();
                }

                ((AVHumanoid)hanimImpl).setSkin(av_skin, num_skin);
                break;

            case FIELD_VIEWPOINTS:
                int num_vps = vfViewpoints.size();

                Node[] av_vp = new Node[num_vps];

                for(int i = 0; i < num_vps; i++) {
                    OGLVRMLNode vp = (OGLVRMLNode)vfViewpoints.get(i);
                    av_vp[i] = (Node)vp.getSceneGraphObject();
                }

                hanimImpl.setViewpoints(av_vp, num_vps);
                break;

            case FIELD_SKELETON:
                int num_skels = vfSkeleton.size();

                HAnimObject[] av_skel = new HAnimObject[num_skels];

                for(int i = 0; i < num_skels; i++) {
                    VRMLHAnimNodeType skel = (VRMLHAnimNodeType)vfSkeleton.get(i);
                    av_skel[i] = skel.getHAnimObject();
                }

                hanimImpl.setSkeleton(av_skel, num_skels);
                break;

            default:
                // ignore everything else
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        super.setErrorReporter(reporter);

        if(!hardwarePropNotice && allowHardwareRender) {
            errorReporter.messageReport(USING_HARDWARE_MSG);
            hardwarePropNotice = true;
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return ((AVHumanoid)hanimImpl).getSceneGraphObject();
    }

    //----------------------------------------------------------
    // Methods defined by BaseHAnimHumanoid
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearSkin() {
        ((AVHumanoid)hanimImpl).setSkin(null, 0);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addSkinNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addSkinNode(node);

        if(inSetup)
            return;

        int num_skin = vfSkin.size();

        Node[] av_skin = new Node[num_skin];

        for(int i = 0; i < num_skin; i++) {
            OGLVRMLNode skin = (OGLVRMLNode)vfSkin.get(i);
            av_skin[i] = (Node)skin.getSceneGraphObject();
        }

        ((AVHumanoid)hanimImpl).setSkin(av_skin, num_skin);

        // Determine which factory we should use and set it
        checkFactoryChoice();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addViewpointNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addViewpointNode(node);

        if(inSetup)
            return;

        int num_vps = vfViewpoints.size();

        Node[] av_vp = new Node[num_vps];

        for(int i = 0; i < num_vps; i++) {
            OGLVRMLNode vp = (OGLVRMLNode)vfViewpoints.get(i);
            av_vp[i] = (Node)vp.getSceneGraphObject();
        }

        hanimImpl.setViewpoints(av_vp, num_vps);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Go through the list of skin objects provided and decide whether we
     * should make use of software or hardware implementation.
     */
    private void checkFactoryChoice() {
        int num_skins = vfSkin.size();
        hardwareRendered = true;

        for(int i = 0; i < num_skins; i++) {
            VRMLNodeType node = (VRMLNodeType)vfSkin.get(i);

            switch(node.getPrimaryType()) {
                case TypeConstants.ShapeNodeType:
                    VRMLShapeNodeType shape = (VRMLShapeNodeType)node;
                    VRMLNodeType geom = shape.getGeometry();

                    if(geom instanceof VRMLProtoInstance) {
                    } else {
                        String node_name = geom.getVRMLNodeName();
                        if(!hardwareCapableGeom.contains(node_name))
                            hardwareRendered = false;
                    }

                    break;

                case TypeConstants.ProtoInstance:
                    break;

                case TypeConstants.GroupingNodeType:
                    break;

                default:
                    // anything else is ignored
            }
        }

        // Correct the hardware render flag based on the global property
        hardwareRendered = hardwareRendered && allowHardwareRender;

        if(hardwareRendered) {
            setHAnimFactory(new AVShaderHAnimFactory());
            errorReporter.messageReport(HARDWARE_USED_MSG);
        } else {
            setHAnimFactory(new AVSoftwareHAnimFactory(true));

            if(allowHardwareRender)
                errorReporter.messageReport(SOFTWARE_USED_MSG);
        }
    }
}
