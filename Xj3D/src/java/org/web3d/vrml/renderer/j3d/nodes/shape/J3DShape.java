/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.shape;

// External imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector4f;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.renderer.common.nodes.shape.BaseShape;

/**
 * Java3D implementation of a shape node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.23 $
 */
public class J3DShape extends BaseShape
    implements J3DVRMLNode,
               J3DGeometryListener,
               J3DAppearanceListener,
               J3DPickableTargetNodeType {

    /** J3D impl */
    private Shape3D impl;

    /** J3D sharedGroup impl */
    private SharedGroup implSG;

    /** J3D BranchGroup impl */
    private BranchGroup implBG;

    /** Counter for the number of geometry items in use */
    private int geomCount;

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

    /**
     * Construct a new default shape node implementation.
     */
    public J3DShape() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    public J3DShape(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by J3DAppearanceListener interface.
    //-------------------------------------------------------------
    /**
     * Invoked when the underlying Java3D Appearance object is changed
     * @param app The new appearance object
     */
    public void appearanceChanged(Appearance app) {
        J3DAppearanceNodeType j3d_app = (J3DAppearanceNodeType)vfAppearance;

        // This does not use createAppearance to avoid an infinite loop with alphaTextures

        if(vfAppearance != null) {
            if(vfGeometry != null) {
                J3DGeometryNodeType geom = (J3DGeometryNodeType)vfGeometry;

                int texGenCount = ((J3DGeometryNodeType)vfGeometry).getNumSets();

                for(int i=0; i < texGenCount; i++)
                    j3d_app.setTexCoordGenMode(i, ((J3DGeometryNodeType)vfGeometry).getTexCoordGenMode(i));
            }

            j3d_app.addAppearanceListener(this);

            impl.setAppearance(j3d_app.getAppearance());
        } else {
            impl.setAppearance(null);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryListener interface.
    //-------------------------------------------------------------

    /**
     * A piece of geometry has been added to the list. The index is the
     * position in the array that the geometry has been added if you were to
     * fetch the geometry now. This allows new items to be inserted into the
     * array. The array will never be null.
     *
     * @param items The geometry items that have been added
     */
    public void geometryAdded(int[] items) {

        J3DGeometryNodeType geom = (J3DGeometryNodeType)vfGeometry;
        Geometry[] new_geom = geom.getGeometry();

        for(int i = 0; i < items.length; i++) {
            impl.insertGeometry(new_geom[items[i]], items[i]);
        }

        geomCount = new_geom.length;
    }

    /**
     * Invoked when a single geometry item has changed. If the value is
     * null, that indicates that all geometry should be reloaded. Otherwise
     * the array lists the index values of all items of geometry that have
     * changed.
     *
     * @param items The geometry items that have changed or null for all
     */
    public void geometryChanged(int[] items) {
        J3DGeometryNodeType geom = (J3DGeometryNodeType)vfGeometry;
        Geometry[] new_geom = geom.getGeometry();

        int size = (items == null) ? new_geom.length : items.length;

        if (items == null) {
            if (new_geom == null)
                impl.setGeometry(null);
            else {
                impl.setGeometry(new_geom[0]);
            }
        }
        else {
            for(int i = 0; i < new_geom.length; i++) {
                impl.setGeometry(new_geom[items[i]], items[i]);
            }
        }

        J3DAppearanceNodeType app = (J3DAppearanceNodeType)vfAppearance;

        if(app != null) {
            // Treat Text-geom notifys as Appearance notifies
            if (vfGeometry instanceof VRMLTextNodeType)
                app.setAlphaTexture(((J3DTextNodeType)vfGeometry).getTextTexture());
        }

        geomCount = new_geom.length;
    }

    /**
     * Invoked when one or more pieces of geometry have changed. The items list
     * the index values of the geometry before they were removed from the
     * array. At the point that this method is called, the geometry will have
     * already been removed from the array. If the array is null, that means
     * all geometry is to be removed.
     *
     * @param items The geometry items that have removed or null for all
     */
    public void geometryRemoved(int[] items) {
        int i;

        // Always remove geomety in the reverse order. If you do it in
        // incremental order, the index values change each time you delete a
        // node and so things end up being out.
        if(items == null) {
            for(i = geomCount; --i > 0; ) {
                impl.removeGeometry(i);
            }
        } else {
            for(i = items.length; --i > 0; ) {
                impl.removeGeometry(items[i]);
            }
        }
    }

    //-------------------------------------------------------------
    // Methods overriding BaseShape
    //-------------------------------------------------------------

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param app The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setAppearance(VRMLNodeType app)
        throws InvalidFieldValueException {

        super.setAppearance(app);

        if(inSetup)
            return;

        checkForProxy();

        createAppearance();
    }

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param geom The new value for geometry.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType geom)
        throws InvalidFieldValueException {

        J3DGeometryNodeType old_geom = (J3DGeometryNodeType)vfGeometry;

        super.setGeometry(geom);

        if(inSetup)
            return;

        if(old_geom != null) {
            // remove the old geometry list
            for(int i = geomCount; --i > 0; ) {
                impl.removeGeometry(i);
            }

            old_geom.removeGeometryListener(this);
        }

        createGeometry();
    }

    //----------------------------------------------------------
    // Methods defined by J3DPickableTargetNodeType
    //----------------------------------------------------------

    /**
     * Fetch the group that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public Group getPickableGroup() {
        return implBG;
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own animation engine.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            if(capBits.containsKey(Shape3D.class)) {
                bits = (int[])capBits.get(Shape3D.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        impl.clearCapability(bits[i]);
                } else if(!isStatic) {
                    // unset the cap bits that would have been set in setVersion()
                    impl.clearCapability(Shape3D.ALLOW_GEOMETRY_READ);
                    impl.clearCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
                    impl.clearCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                    impl.clearCapability(Shape3D.ALLOW_BOUNDS_READ);
                }
            }

            if(capBits.containsKey(BranchGroup.class)) {
                bits = (int[])capBits.get(BranchGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implBG.clearCapability(bits[i]);
                } else if(!isStatic) {
                    implBG.clearCapability(BranchGroup.ALLOW_DETACH);
                }
            }

            if(capBits.containsKey(SharedGroup.class)) {
                bits = (int[])capBits.get(SharedGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implSG.clearCapability(bits[i]);
                }
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        if(freqBits.containsKey(Shape3D.class)) {
            bits = (int[])freqBits.get(Shape3D.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    impl.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                impl.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_READ);
                impl.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_WRITE);
                impl.clearCapabilityIsFrequent(Shape3D.ALLOW_APPEARANCE_WRITE);
                impl.clearCapabilityIsFrequent(Shape3D.ALLOW_BOUNDS_READ);
            }
        }

        if(freqBits.containsKey(BranchGroup.class)) {
            bits = (int[])freqBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implBG.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implBG.clearCapabilityIsFrequent(BranchGroup.ALLOW_DETACH);
            }
        }

        if(freqBits.containsKey(SharedGroup.class)) {
            bits = (int[])freqBits.get(SharedGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implSG.clearCapabilityIsFrequent(bits[i]);
            }
        }
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(Shape3D.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    impl.setCapability(bits[i]);
            }

            bits = (int[])capBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implBG.setCapability(bits[i]);
            }

            bits = (int[])capBits.get(SharedGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implSG.setCapability(bits[i]);
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(Shape3D.class);

        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                impl.setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(BranchGroup.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implBG.setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(SharedGroup.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implSG.setCapabilityIsFrequent(bits[i]);
        }
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implSG;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        // Appearance is late loaded so it must be enabled even for static
        impl.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        impl.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        impl.setCapability(Shape3D.ALLOW_BOUNDS_READ);
        impl.clearCapabilityIsFrequent(Shape3D.ALLOW_BOUNDS_READ);

        if(isStatic)
            return;

        impl.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

        implBG.setCapability(BranchGroup.ALLOW_DETACH);
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if (inSetup)
            return;

        boolean ok=false;
        int type;

        switch(index) {
            case FIELD_GEOMETRY:
                type = node.getPrimaryType();

                switch(type) {
                    case TypeConstants.GeometryNodeType:
                    case TypeConstants.ComponentGeometryNodeType:
                        vfGeometry = (VRMLGeometryNodeType) node;
                        ok=true;
                        break;
                    case TypeConstants.ProtoInstance:
                        int[] stypes = node.getSecondaryType();
                        for(int i=0; i < stypes.length; i++) {
                            if (stypes[i] == TypeConstants.GeometryNodeType ||
                                stypes[i] == TypeConstants.ComponentGeometryNodeType) {
                                ok=true;
                                break;
                            }
                        }

                        pGeometry = (VRMLProtoInstance) node;
                        vfGeometry = (VRMLGeometryNodeType)
                            ((VRMLProtoInstance)node).getImplementationNode();
                        break;
                }

                if (!ok)
                    throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

                createGeometry();

                break;
            case FIELD_APPEARANCE:
                type = node.getPrimaryType();

                switch(type) {
                    case TypeConstants.AppearanceNodeType:
                        ok=true;
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

                if (!ok) {
                    throw new InvalidFieldValueException(APPEARANCE_PROTO_MSG);
                }
                createAppearance();
                break;
            default:
                System.out.println("J3DShape: Unknown field for notifyExternProtoLoaded");
        }
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

        createGeometry();
        createAppearance();

        implBG.setCapability(BranchGroup.ENABLE_PICK_REPORTING);

    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Convenience method to do common initialisation on construction.
     */
    private void init() {
        impl = new Shape3D();
        impl.setPickable(true);

        implBG = new BranchGroup();
        implBG.addChild(impl);

        implSG = new SharedGroup();
        implSG.addChild(implBG);

        geomCount = 0;
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createAppearance() {
        J3DAppearanceNodeType j3d_app = (J3DAppearanceNodeType)vfAppearance;

        if (j3d_app != null)
            j3d_app.addAppearanceListener(this);
        else {
            checkForProxy();
            impl.setAppearance(proxyAppearance);
            return;
        }

        if(vfGeometry != null) {
            J3DGeometryNodeType geom = (J3DGeometryNodeType)vfGeometry;

            int texGenCount = ((J3DGeometryNodeType)vfGeometry).getNumSets();

            for(int i=0; i < texGenCount; i++)
                j3d_app.setTexCoordGenMode(i, ((J3DGeometryNodeType)vfGeometry).getTexCoordGenMode(i));
        }

        j3d_app.addAppearanceListener(this);

        impl.setAppearance(j3d_app.getAppearance());

        if(vfGeometry instanceof J3DTextNodeType) {
            J3DTextNodeType text = (J3DTextNodeType)vfGeometry;
            j3d_app.setAlphaTexture(text.getTextTexture());
        } else if (vfGeometry instanceof J3DTerrainSource) {
            ((J3DTerrainSource)vfGeometry).setAppearance(j3d_app);
        }
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createGeometry() {
        J3DGeometryNodeType geom = (J3DGeometryNodeType)vfGeometry;

        if(geom != null)
            geom.addGeometryListener(this);
        else {
            impl.setGeometry(null);
            return;
        }

        // Fetch all of the geometry and build the shape with it.
        Geometry[] j3d_geom = geom.getGeometry();

        if (j3d_geom != null) {
            for(int i = 0; i < j3d_geom.length; i++) {
                impl.setGeometry(j3d_geom[i],i);
            }

            geomCount = j3d_geom.length;
        } else {
            geomCount = 0;
        }

        if(vfAppearance != null) {
            J3DAppearanceNodeType app = (J3DAppearanceNodeType)vfAppearance;

            int texGenCount = geom.getNumSets();

            for(int i=0; i < texGenCount; i++)
                app.setTexCoordGenMode(i, geom.getTexCoordGenMode(i));
        }
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

            if(!solid) {
                if(ccw) {
                    proxyPolyAttr.setCullFace(PolygonAttributes.CULL_BACK);
                    proxyPolyAttr.setBackFaceNormalFlip(false);
                } else {
                    proxyPolyAttr.setCullFace(PolygonAttributes.CULL_FRONT);
                    proxyPolyAttr.setBackFaceNormalFlip(true);
                }
            } else {
                proxyPolyAttr.setCullFace(PolygonAttributes.CULL_NONE);
                proxyPolyAttr.setBackFaceNormalFlip(true);
            }
        }
    }
}
