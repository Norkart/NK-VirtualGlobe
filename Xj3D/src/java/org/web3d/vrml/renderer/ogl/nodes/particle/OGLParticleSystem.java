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

package org.web3d.vrml.renderer.ogl.nodes.particle;

// External imports
import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.geom.particle.*;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.vrml.renderer.common.nodes.particle.BaseParticleSystem;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLAppearanceNodeType;

/**
 * OpenGL implementation of a ParticleSystem.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class OGLParticleSystem extends BaseParticleSystem
    implements OGLVRMLNode, NodeUpdateListener {

    /** The Aviatrix3D representation of a shape object */
    private Shape3D implShape;

    /** The global shared node we return to the scene graph */
    private SharedNode implNode;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public OGLParticleSystem() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLParticleSystem(VRMLNodeType node) {
        super(node);
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        Appearance a = null;

        if(vfAppearance != null) {
            OGLAppearanceNodeType o_a =
                (OGLAppearanceNodeType)vfAppearance;

            a = o_a.getAppearance();
        }

        implShape.setAppearance(a);
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
        return implNode;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        // Create the Aviatrix3D particle system before calling the base class
        // because it expects that to happen.

        // The number of colours comes from the colour ramp
        int num_colors = 3;
        if(vfColorRamp != null)
            num_colors = vfColorRamp.getNumColorComponents();

        switch(geometryType) {
            case TYPE_QUADS:
                particleSystem = new QuadParticleSystem(null,
                                                        vfMaxParticles,
                                                        num_colors);

                ((QuadParticleSystem)particleSystem).setParticleSize(vfParticleSize[0],
                                                                     vfParticleSize[1]);
                break;

            case TYPE_TRIS:
                particleSystem =
                    new TriangleFanParticleSystem(null,
                                                  vfMaxParticles,
                                                  num_colors);
                ((TriangleFanParticleSystem)particleSystem).setParticleSize(vfParticleSize[0],
                                                                            vfParticleSize[1]);
                break;

            case TYPE_LINES:
//                particleSystem = new LineParticleSystem(null,
//                                                        vfMaxParticles,
//                                                        num_colors);
                break;

            case TYPE_POINTS:
                particleSystem = new PointParticleSystem(null,
                                                        vfMaxParticles,
                                                        num_colors);
                break;

            case TYPE_SPRITES:
System.out.println("OpenGL does not support point sprite particles yet");
                break;

            case TYPE_CUSTOM:
System.out.println("OpenGL does not support custom geometry particles yet");
                break;

            default:
System.out.println("unknown particle system index " + geometryType);
                // don't do anything
        }

        implShape = new Shape3D();
        implShape.setGeometry(((AVParticleSystem)particleSystem).getNode());

        implNode = new SharedNode();
        implNode.setChild(implShape);

        super.setupFinished();

        if(vfAppearance != null) {
            OGLAppearanceNodeType o_app = (OGLAppearanceNodeType)vfAppearance;
            o_app.setSolid(false);
            o_app.setLightingEnabled(true);
            o_app.setCCW(true);

            Appearance app = o_app.getAppearance();
            implShape.setAppearance(app);
        }

        // Check on bounding box info and see if it's worth setting an explicit
        // bounds to the shape.
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
            implShape.setBounds(bbox);
        }
    }

    /**
     * Set node content as replacement for <code>appearance</code>.
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

        super.setAppearance(newAppearance);

        if(implShape != null) {
            if (implShape.isLive())
                implShape.dataChanged(this);
            else
                updateNodeDataChanges(implShape);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseParticleSystem
    //----------------------------------------------------------

    /**
     * Set the size of the particle to a new value. This should be overridden
     * by renderer-specific classes for telling the particle system about it.
     *
     * @param value The new 2D size to use
     * @throws InvalidFieldValueException The value is negative
     */
    protected void setParticleSize(float[] value)
        throws InvalidFieldValueException {

        super.setParticleSize(value);

        if(inSetup)
            return;

        switch(geometryType) {
            case TYPE_QUADS:
                ((QuadParticleSystem)particleSystem).setParticleSize(value[0],
                                                                     value[1]);
                break;

            case TYPE_TRIS:
                ((TriangleFanParticleSystem)particleSystem).setParticleSize(value[0],
                                                                            value[1]);
                break;

            case TYPE_LINES:
//                particleSystem = new LineParticleSystem(null,
//                                                        vfMaxParticles,
//                                                        num_colors);
                break;

            case TYPE_POINTS:
                // ignored for this version. Perhaps we may want it to influence
                // the point size?
                break;

            case TYPE_SPRITES:
                break;

            case TYPE_CUSTOM:
                break;

            default:
                // don't do anything
        }
    }
}
