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

package org.web3d.vrml.renderer.ogl.nodes.shape;

// External imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.picking.PickableObject;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.renderer.common.nodes.shape.BaseShape;


/**
 * OGL implementation of a shape node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.28 $
 */
public class OGLShape extends BaseShape
    implements OGLVRMLNode,
               OGLPickableTargetNodeType,
               NodeUpdateListener,
               MaterialColorListener {

    /** Shared node */
    private SharedNode implSN;

    /** OGL impl of the shape */
    private Shape3D impl;

    // Impl Note:
    // There are several fields in the geometry node that we need to deal with
    // that end up being handled by the appearance. If there is no appearance
    // node, then we need to do something about that. What we do is create an
    // internal proxy appearance structure to handle the solid and ccw field
    // values. lightingEnabled does nothing if we have no appearance.

    /** The proxy appearance to use if there is no appearance set. */
    private Appearance proxyAppearance;

    /** The proxy poly attribs to use if there is no appearance set. */
    private PolygonAttributes proxyPolyAttr;

    /** Was a geometry Extern proto loaded */
    private boolean geomExternLoaded;

    /** Was an appearance Extern proto loaded */
    private boolean appExternLoaded;

    /**
     * Used when the appearance or geometry changed in this last frame. We need
     * to go register a material listener in the events callback so that the
     * emissive values make it through to the geometry.
     */
    private boolean checkMaterialListener;

    /**
     * Construct a new default shape node implementation.
     */
    public OGLShape() {
        checkMaterialListener = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    public OGLShape(VRMLNodeType node) {
        super(node);
        checkMaterialListener = false;
    }

    //----------------------------------------------------------
    // Methods defined by OGLPickableTargetNodeType
    //----------------------------------------------------------

    /**
     * Set the flag convertor. Ignored for this node.
     *
     * @param conv The convertor instance to use, or null
     */
    public void setTypeConvertor(OGLPickingFlagConvertor conv) {
        // ignored for this node.
    }

    /**
     * Fetch the object that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public PickableObject getPickableObject() {
        return impl;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        Geometry g = null;

        if(vfGeometry != null) {
            OGLGeometryNodeType o_g =
                (OGLGeometryNodeType)vfGeometry;

            g = o_g.getGeometry();
        }

        impl.setGeometry(g);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {

        if(src == impl) {
            Appearance a = null;

            if(vfAppearance != null) {
                OGLAppearanceNodeType o_a =
                    (OGLAppearanceNodeType)vfAppearance;

                a = o_a.getAppearance();
            } else {
                a = proxyAppearance;
            }
            impl.setAppearance(a);

        } else if(src == proxyPolyAttr) {
            boolean solid = true;
            boolean ccw = true;

            if(vfGeometry != null) {
                solid = vfGeometry.isSolid();
                ccw = vfGeometry.isCCW();
            }

            if(solid) {
                proxyPolyAttr.setCulledFace(PolygonAttributes.CULL_BACK);
                proxyPolyAttr.setTwoSidedLighting(false);
            } else {
                proxyPolyAttr.setCulledFace(PolygonAttributes.CULL_NONE);
                proxyPolyAttr.setTwoSidedLighting(true);
            }

            proxyPolyAttr.setCCW(ccw);
        }
    }

    //----------------------------------------------------------
    // Methods defined by MaterialColorListener
    //----------------------------------------------------------

    /**
     * The emissiveColor value has changed.
     *
     * @param color The new color value to use
     */
    public void emissiveColorChanged(float[] color) {
        if(vfGeometry != null) {
            ((VRMLComponentGeometryNodeType)vfGeometry).setUnlitColor(color);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        // Add in the new listener if the appearance is changing. This will
        // force a new emissive color value to be sent back to the node almost
        // right away.
        if(checkMaterialListener && (vfAppearance != null) &&
           (vfGeometry instanceof VRMLComponentGeometryNodeType) &&
           ((VRMLComponentGeometryNodeType)vfGeometry).requiresUnlitColor()) {

           vfAppearance.addMaterialColorListener(this);
           checkMaterialListener = false;
       }

        if (geomExternLoaded) {
            if(impl.isLive()) {
                impl.boundsChanged(this);
            } else {
                updateNodeBoundsChanges(impl);
            }
        }

        if (appExternLoaded) {
            if(impl.isLive()) {
                impl.dataChanged(this);
            } else {
                updateNodeDataChanges(impl);
            }
        }
    }

    //----------------------------------------------------------
    // Methods overriding BaseShape.
    //----------------------------------------------------------

    /**
     * Set node content as replacement for the appearance field.
     *
     * @param newAppearance The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setAppearance(VRMLNodeType newAppearance)
        throws InvalidFieldValueException {

        if(!(newAppearance instanceof OGLAppearanceNodeType) &&
           !(newAppearance instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(APPEARANCE_NODE_MSG);

        // Remove the old listener if the appearance is changing
        if((vfAppearance != null) &&
           (vfGeometry instanceof VRMLComponentGeometryNodeType) &&
           ((VRMLComponentGeometryNodeType)vfGeometry).requiresUnlitColor())
           vfAppearance.removeMaterialColorListener(this);

        super.setAppearance(newAppearance);

        checkForProxy();

        if(impl != null) {
            if(impl.isLive())
                impl.dataChanged(this);
            else
                updateNodeDataChanges(impl);
        }

        if(!inSetup) {
            checkMaterialListener = true;
            stateManager.addEndOfThisFrameListener(this);
        }
    }

    /**
     * Set node content as replacement for geometry field.
     *
     * @param newGeomtry The new value for geometry.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType newGeometry)
        throws InvalidFieldValueException {

        if(newGeometry != null && !(newGeometry instanceof OGLGeometryNodeType) &&
           !(newGeometry instanceof VRMLProtoInstance)) {

            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);
        }

        // Remove the old listener if the appearance is changing
        if((vfAppearance != null) &&
           (vfGeometry instanceof VRMLComponentGeometryNodeType) &&
           ((VRMLComponentGeometryNodeType)vfGeometry).requiresUnlitColor())
           vfAppearance.removeMaterialColorListener(this);

        super.setGeometry(newGeometry);

        if(impl != null) {
            if(impl.isLive())
                impl.boundsChanged(this);
            else
                updateNodeBoundsChanges(impl);
        }

        if(!inSetup) {
            checkMaterialListener = true;
            stateManager.addEndOfThisFrameListener(this);
        }
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        boolean ok = false;
        int type;

        switch(index) {
            case FIELD_GEOMETRY:
                type = node.getPrimaryType();

                switch(type) {
                    case TypeConstants.GeometryNodeType:
                    case TypeConstants.ComponentGeometryNodeType:
                        vfGeometry = (OGLGeometryNodeType)node;
                        ok = true;
                        break;

                    case TypeConstants.ProtoInstance:
                        int[] stypes = node.getSecondaryType();
                        for(int i=0; i < stypes.length; i++) {
                            if (stypes[i] == TypeConstants.GeometryNodeType ||
                                stypes[i] == TypeConstants.ComponentGeometryNodeType) {
                                ok = true;
                                break;
                            }
                        }

                        pGeometry = (VRMLProtoInstance) node;
                        vfGeometry = (VRMLGeometryNodeType)
                            ((VRMLProtoInstance)node).getImplementationNode();
                        break;
                }

                if(!ok)
                    throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

                if(impl.isLive()) {
                    geomExternLoaded = true;
                } else
                    updateNodeBoundsChanges(impl);

                checkMaterialListener = true;
                stateManager.addEndOfThisFrameListener(this);
                break;

            case FIELD_APPEARANCE:
                type = node.getPrimaryType();

                switch(type) {
                    case TypeConstants.AppearanceNodeType:
                        ok = true;
                        break;

                    case TypeConstants.ProtoInstance:
                        int[] stypes = node.getSecondaryType();
                        for(int i=0; i < stypes.length; i++) {
                            if (stypes[i] == TypeConstants.AppearanceNodeType) {
                                ok=true;
                                break;
                            }
                        }

                        pAppearance = (VRMLProtoInstance) node;
                        vfAppearance = (VRMLAppearanceNodeType)
                            ((VRMLProtoInstance)node).getImplementationNode();

                        break;
                }

                if(!ok)
                    throw new InvalidFieldValueException(APPEARANCE_PROTO_MSG);

                if(impl.isLive()) {
                    appExternLoaded = true;
                } else
                    updateNodeBoundsChanges(this);

                checkMaterialListener = true;
                stateManager.addEndOfThisFrameListener(this);
                break;

            default:
                System.out.println("OGLShape: Unknown field for notifyExternProtoLoaded");
        }
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implSN;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        impl = new Shape3D();

        if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1) {
            float[] min = new float[3];
            min[0] = vfBboxCenter[0] - vfBboxSize[0] / 2;
            min[1] = vfBboxCenter[1] - vfBboxSize[1] / 2;
            min[2] = vfBboxCenter[2] - vfBboxSize[2] / 2;

            float[] max = new float[3];
            max[0] = vfBboxCenter[0] + vfBboxSize[0] / 2;
            max[1] = vfBboxCenter[1] + vfBboxSize[1] / 2;
            max[2] = vfBboxCenter[2] + vfBboxSize[2] / 2;

            BoundingBox bbox = new BoundingBox(min, max);
            impl.setBounds(bbox);
        }

        implSN = new SharedNode();
        implSN.setChild(impl);

        createGeometry();
        createAppearance();

        // Add in the new listener if there is an appearance. This will
        // force a new emissive color value to be sent back to the node almost
        // right away.
        if((vfAppearance != null) &&
           (vfGeometry instanceof VRMLComponentGeometryNodeType) &&
           ((VRMLComponentGeometryNodeType)vfGeometry).requiresUnlitColor()) {

           vfAppearance.addMaterialColorListener(this);
           checkMaterialListener = false;
       }
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createAppearance() {
        OGLAppearanceNodeType o_app = (OGLAppearanceNodeType)vfAppearance;

        if(o_app == null) {
            checkForProxy();
            impl.setAppearance(proxyAppearance);
            return;
        }

        if(vfGeometry != null) {
            OGLGeometryNodeType geom = (OGLGeometryNodeType)vfGeometry;

            int texGenCount = geom.getNumSets();
            String mode;

            for(int i=0; i < texGenCount; i++) {
                mode = geom.getTexCoordGenMode(i);

                if (mode != null)
                    o_app.setTexCoordGenMode(i, mode);
            }
        }

        impl.setAppearance(o_app.getAppearance());
    }

    /**
     * Create the render specific structures for the geometry portion
     * of the node.
     */
    private void createGeometry() {
        OGLGeometryNodeType geom = (OGLGeometryNodeType)vfGeometry;

        if(geom == null) {
            impl.setGeometry(null);
            return;
        }

        // Fetch all of the geometry and build the shape with it.
        Geometry o_geom = geom.getGeometry();

        if(o_geom != null)
            impl.setGeometry(o_geom);

/*
        // TODO: This is already in createAppearance, why is it here twice?
        if(vfAppearance != null) {
            OGLAppearanceNodeType app = (OGLAppearanceNodeType)vfAppearance;

            int texGenCount = geom.getNumSets();

            for(int i=0; i < texGenCount; i++)
                app.setTexCoordGenMode(i, geom.getTexCoordGenMode(i));
        }
*/
    }

    /**
     * Check and create proxy appearance information if needed.
     */
    private void checkForProxy() {
        if(vfAppearance == null) {
            proxyPolyAttr = new PolygonAttributes();

            proxyAppearance = new Appearance();
            proxyAppearance.setPolygonAttributes(proxyPolyAttr);

            boolean solid = true;
            boolean ccw = true;

            if(vfGeometry != null) {
                solid = vfGeometry.isSolid();
                ccw = vfGeometry.isCCW();
            }

            if(solid) {
                proxyPolyAttr.setCulledFace(PolygonAttributes.CULL_BACK);
                proxyPolyAttr.setTwoSidedLighting(false);
            } else {
                proxyPolyAttr.setCulledFace(PolygonAttributes.CULL_NONE);
                proxyPolyAttr.setTwoSidedLighting(true);
            }

            proxyPolyAttr.setCCW(ccw);
        }
    }
}
