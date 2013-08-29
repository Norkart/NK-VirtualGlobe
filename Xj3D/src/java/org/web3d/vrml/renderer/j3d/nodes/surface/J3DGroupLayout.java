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

package org.web3d.vrml.renderer.j3d.nodes.surface;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

import org.j3d.renderer.java3d.overlay.OverlayManager;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DOverlayStructureNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DOverlayItemNodeType;
import org.web3d.vrml.renderer.common.nodes.surface.BaseGroupLayout;

/**
 * Java3D renderer implementation of a GroupLayout node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class J3DGroupLayout extends BaseGroupLayout
    implements J3DOverlayStructureNodeType {

    /** The manager for the overlay information */
    private OverlayManager overlayManager;

    /**
     * Construct a new default Overlay object
     */
    public J3DGroupLayout() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DGroupLayout(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the J3DOverlayStructureNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the overlay manager to use.
     *
     * @param mgr The overlay manager instance to use
     */
    public void setOverlayManager(OverlayManager mgr) {
        overlayManager = mgr;

        J3DOverlayStructureNodeType stn;
        J3DOverlayItemNodeType item;

        int size = (vfChildren == null) ? 0 : vfChildren.length;

        for(int i = 0; i < size; i++) {
            if(vfChildren[i] instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)vfChildren[i];
                VRMLNodeType impl = proto.getImplementationNode();

                if(impl instanceof J3DOverlayStructureNodeType) {
                    stn = (J3DOverlayStructureNodeType)impl;
                    stn.setOverlayManager(overlayManager);
                } else {
                    item = (J3DOverlayItemNodeType)impl;

                    overlayManager.addOverlay(item.getOverlay());
                }
            } else if(vfChildren[i] instanceof J3DOverlayStructureNodeType) {
                stn = (J3DOverlayStructureNodeType)vfChildren[i];
                stn.setOverlayManager(overlayManager);
            } else {
                item = (J3DOverlayItemNodeType)vfChildren[i];

                overlayManager.addOverlay(item.getOverlay());
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
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
    }

    //----------------------------------------------------------
    // Methods required by the VRMLSurfaceLayoutNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the drawable content of this node to the surface. If value is set
     * to null, then it clears all the renderable list and nothing is show.
     * The nodes provided must be {@link VRMLSurfaceChildNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param kids The list of new children to render
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    public void setChildren(VRMLNodeType[] kids)
        throws InvalidFieldValueException {

        VRMLNodeType[] old_kids = vfChildren;

        super.setChildren(kids);

        if(inSetup)
            return;

        J3DOverlayStructureNodeType stn;
        J3DOverlayItemNodeType item;

        int size = (old_kids == null) ? 0 : old_kids.length;

        for(int i = 0; i < size; i++) {
            if(old_kids[i] instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)old_kids[i];
                VRMLNodeType impl = proto.getImplementationNode();

                if(impl instanceof J3DOverlayStructureNodeType) {
                    stn = (J3DOverlayStructureNodeType)impl;
                    stn.setOverlayManager(null);
                } else {
                    item = (J3DOverlayItemNodeType)impl;
                    overlayManager.removeOverlay(item.getOverlay());
                }
            } else if(old_kids[i] instanceof J3DOverlayStructureNodeType) {
                stn = (J3DOverlayStructureNodeType)old_kids[i];
                stn.setOverlayManager(null);
            } else {
                item = (J3DOverlayItemNodeType)old_kids[i];
                overlayManager.removeOverlay(item.getOverlay());
            }
        }

        size = (vfChildren == null) ? 0 : vfChildren.length;

        for(int i = 0; i < size; i++) {
            if(vfChildren[i] instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)vfChildren[i];
                VRMLNodeType impl = proto.getImplementationNode();

                if(impl instanceof J3DOverlayStructureNodeType) {
                    stn = (J3DOverlayStructureNodeType)impl;
                    stn.setOverlayManager(overlayManager);
                } else {
                    item = (J3DOverlayItemNodeType)impl;
                    overlayManager.addOverlay(item.getOverlay());
                }
            } else if(vfChildren[i] instanceof J3DOverlayStructureNodeType) {
                stn = (J3DOverlayStructureNodeType)vfChildren[i];
                stn.setOverlayManager(overlayManager);
            } else {
                item = (J3DOverlayItemNodeType)vfChildren[i];
                overlayManager.addOverlay(item.getOverlay());
            }
        }
    }
}
