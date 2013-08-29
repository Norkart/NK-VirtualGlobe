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

package org.web3d.vrml.renderer.j3d.nodes.particle;

// External imports
import javax.media.j3d.*;

import org.j3d.geom.particle.*;

import java.util.Map;

// Local imports
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.particle.BaseParticleSystem;

/**
 * Null renderer implementation of a particle system node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class J3DParticleSystem extends BaseParticleSystem
    implements J3DVRMLNode {

    /** The system manager for this particle system */
//    private J3DParticleSystem systemManager;

    /** Our local particle system */
//    private ByRefParticleSystem particleSystem;

    /** J3D impl */
    private Shape3D impl;

    /** J3D sharedGroup impl */
    private SharedGroup implSG;

    /** J3D BranchGroup impl */
    private BranchGroup implBG;

    /**
     * Construct a new default shape node implementation.
     */
    public J3DParticleSystem() {
        implBG = new BranchGroup();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    public J3DParticleSystem(VRMLNodeType node) {
        super(node);
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

        J3DAppearanceNodeType j3d_app = (J3DAppearanceNodeType)vfAppearance;

        if(vfAppearance != null) {
            impl.setAppearance(j3d_app.getAppearance());
        } else {
            impl.setAppearance(null);
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
        if(implSG != null)
            return implSG;
        else
            return implBG;
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
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notify this node that is has been DEFd. This method shall only be
     * called before setupFinished(). It is an error to call it any other
     * time. It is also guaranteed that this call will be made after
     * construction, but before any of the setValue()
     * methods have been called.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        super.setDEF();

        implSG = new SharedGroup();
        implSG.addChild(implBG);
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

/*
        switch(geometryType) {
            case TYPE_QUADS:
                particleSystem =
                    new QuadArrayByRefParticleSystem("foo",
                                                     vfMaxParticles,
                                                     initializer,
                                                     Collections.EMPTY_MAP);
                break;

            case TYPE_TRIS:
                particleSystem =
                    new TriangleArrayByRefParticleSystem("foo",
                                                         vfMaxParticles,
                                                         initializer,
                                                         Collections.EMPTY_MAP);
                break;

            case TYPE_LINES:
                particleSystem =
                    new LineArrayByRefParticleSystem("foo",
                                                     vfMaxParticles,
                                                     initializer,
                                                     Collections.EMPTY_MAP);
                break;

            case TYPE_POINTS:
                particleSystem =
                    new PointArrayByRefParticleSystem("foo",
                                                      vfMaxParticles,
                                                      initializer,
                                                      Collections.EMPTY_MAP);
                break;
        }

        // find all the physics function implementations and register them.
        for(int i = 0; i < vfPhysics.length; i++) {
            J3DPhysicsModelNodeType model = null;

            if(vfPhysics[i] instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)vfPhysics[i];
                VRMLNodeType impl = proto.getImplementationNode();

                model = (J3DPhysicsModelNodeType)impl;
            } else {
                model = (J3DPhysicsModelNodeType)vfPhysics[i];
            }

            ParticleFunction pf = model.getControlFunction();
            particleSystem.addParticleFunction(pf);
        }

        impl = (Shape3D)particleSystem.getNode();
// May want to fix this!
        impl.setPickable(false);
        impl.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        impl.setCapability(Shape3D.ALLOW_GEOMETRY_READ);

        if(vfAppearance != null) {
            J3DAppearanceNodeType j_app = (J3DAppearanceNodeType)vfAppearance;
            impl.setAppearance(j_app.getAppearance());
        }

        implBG.addChild(impl);
*/
    }
}
