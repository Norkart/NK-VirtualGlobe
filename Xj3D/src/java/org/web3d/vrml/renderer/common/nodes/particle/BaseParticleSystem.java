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

package org.web3d.vrml.renderer.common.nodes.particle;

// External imports
import org.j3d.geom.particle.*;

import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base renderer implementation of a particle system shape node.
 * <p>
 *
 * This implementation is renderer independent, but uses the common j3d.org
 * interfaces for dealing with the particle systems.
 *
 * @author Justin Couch
 * @version $Revision: 2.5 $
 */
public abstract class BaseParticleSystem extends AbstractNode
    implements VRMLParticleSystemNodeType{

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.BoundedNodeType };

    /** Index for the appearance field */
    protected static final int FIELD_APPEARANCE = LAST_NODE_INDEX + 1;

    /** Index for the geometry field */
    protected static final int FIELD_GEOMETRY = LAST_NODE_INDEX + 2;

    /** Index for the bbox_size field */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 3;

    /** Index for the bbox_center field */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 4;

    /** Index for the geometryType field */
    protected static final int FIELD_GEOMETRY_TYPE = LAST_NODE_INDEX + 5;

    /** Index for the enabled field */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 6;

    /** Index for the maxParticles field */
    protected static final int FIELD_MAX_PARTICLES = LAST_NODE_INDEX + 7;

    /** Index for the particleLifetime field */
    protected static final int FIELD_PARTICLE_LIFETIME = LAST_NODE_INDEX + 8;

    /** Index for the lifetimeVariation field */
    protected static final int FIELD_LIFETIME_VARIATION = LAST_NODE_INDEX + 9;

    /** Index for the emitter field */
    protected static final int FIELD_EMITTER = LAST_NODE_INDEX + 10;

    /** Index for the physics field */
    protected static final int FIELD_PHYSICS = LAST_NODE_INDEX + 11;

    /** Index for the colorRamp field */
    protected static final int FIELD_COLOR_RAMP = LAST_NODE_INDEX + 12;

    /** Index for the colorKey field */
    protected static final int FIELD_COLOR_KEY = LAST_NODE_INDEX + 13;

    /** Index for the colorKey field */
    protected static final int FIELD_IS_ACTIVE = LAST_NODE_INDEX + 14;

    /** Index for the particleSize field */
    protected static final int FIELD_PARTICLE_SIZE = LAST_NODE_INDEX + 15;

    /** Index for the createParticles field */
    protected static final int FIELD_CREATE_PARTICLES = LAST_NODE_INDEX + 16;

    /** Index for the texCoordRamp field */
    protected static final int FIELD_TEXCOORD_RAMP = LAST_NODE_INDEX + 17;

    /** Index for the texCoordKey field */
    protected static final int FIELD_TEXCOORD_KEY = LAST_NODE_INDEX + 18;

    /** The last field index used by this class */
    protected static final int LAST_PARTICLE_INDEX = FIELD_TEXCOORD_KEY;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_PARTICLE_INDEX + 1;


    /** Message for when the proto is not a Emitter */
    protected static final String EMITTER_PROTO_MSG =
        "Proto does not describe a Emitter object";

    /** Message for when the node in setValue() is not a Emitter */
    protected static final String EMITTER_NODE_MSG =
        "Node does not describe a Emitter object";

    /** Message for when the proto is not a Physics */
    protected static final String PHYSICS_PROTO_MSG =
        "Proto does not describe a Physics object";

    /** Message for when the node in setValue() is not a Physics */
    protected static final String PHYSICS_NODE_MSG =
        "Node does not describe a Physics object";

    /** Message for when the proto is not a Appearance */
    protected static final String APPEARANCE_PROTO_MSG =
        "Proto does not describe a Appearance object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String APPEARANCE_NODE_MSG =
        "Node does not describe a Appearance object";

    /** Message for when the proto is not a Color */
    protected static final String COLOR_PROTO_MSG =
        "Proto does not describe a Color object";

    /** Message for when the node in setValue() is not a Color */
    protected static final String COLOR_NODE_MSG =
        "Node does not describe a Color object";

    /** Message for when the proto is not a tex coords */
    protected static final String TEXCOORD_PROTO_MSG =
        "Proto does not describe a X3DTextureCoordinateNode object";

    /** Message for when the node in setValue() is not tex coords */
    protected static final String TEXCOORD_NODE_MSG =
        "Node does not describe a X3DTextureCoordinateNode object";

    /** Message when the maximum number of particles is negative */
    private static final String NEG_PARTICLES_ERR =
        "Value provided for maxParticles is negative.";

    /** Message when the lifetime variation is outside of [0,1] */
    private static final String VARIATION_RANGE_ERR =
        "Value of lifetimeVariation is outside the acceptable range [0,1]";

    /** Message when the particle lifetime is negative */
    private static final String NEG_LIFETIME_ERR =
        "Provided particleLifetime value was negative.";

    /** Message when someone tries to write to isActive */
    private static final String ACTIVE_WRITE_MSG =
        "Attempt to set the isActive outputOnly field";

    /** Message when the particle size is negative */
    private static final String NEG_SIZE_ERR =
        "Provided particleSize value contains a negative value:";

    /** Particle geometry type is Quads */
    protected static final int TYPE_QUADS = 1;

    /** Particle geometry type is Triangles */
    protected static final int TYPE_TRIS = 2;

    /** Particle geometry type is Lines */
    protected static final int TYPE_LINES = 3;

    /** Particle geometry type is Points */
    protected static final int TYPE_POINTS = 4;

    /** Particle geometry type is point sprites */
    protected static final int TYPE_SPRITES = 5;

    /** Particle geometry type is custom lvl 2 */
    protected static final int TYPE_CUSTOM = 6;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Mapping of geometry type strings to type integers */
    private static final HashMap geomTypeMap;

    // VRML Field declarations

    /** Proto version of the appearance */
    protected VRMLProtoInstance pAppearance;

    /** exposedField SFNode appearance NULL */
    protected VRMLAppearanceNodeType vfAppearance;

    /** Proto version of the Emitter */
    protected VRMLProtoInstance pEmitter;

    /** field SFNode emitter NULL */
    protected VRMLParticleEmitterNodeType vfEmitter;

    /** Proto version of the colorRamp */
    protected VRMLProtoInstance pColorRamp;

    /** field SFNode colorRamp NULL */
    protected VRMLColorNodeType vfColorRamp;

    /** Proto version of the texCoordRamp */
    protected VRMLProtoInstance pTexCoordRamp;

    /** field SFNode texCoordRamp NULL */
    protected VRMLTextureCoordinateNodeType vfTexCoordRamp;

    /** The physics nodes mixed proto and child nodes */
    protected VRMLNodeType[] vfPhysics;

    /** field SFVec3f bboxCenter 0, 0, 0 */
    protected float[] vfBboxCenter;

    /** field SFVec3f bboxSize [-1, -1, -1] */
    protected float[] vfBboxSize;

    /** field SFString geometryType "QUAD" */
    protected String vfGeometryType;

    /** exposedField SFBool enabled */
    protected boolean vfEnabled;

    /** exposedField SFBool createParticles */
    protected boolean vfCreateParticles;

    /** exposedField SFInt32 maxParticles */
    protected int vfMaxParticles;

    /** exposedField SFFloat particleLifetime */
    protected float vfParticleLifetime;

    /** exposedField SFFVec2f particleSize */
    protected float[] vfParticleSize;

    /** exposedField SFFloat lifetimeVariation */
    protected float vfLifetimeVariation;

    /** field MFFloat colorKey */
    protected float[] vfColorKey;

    /** Number of valid colorKey values */
    protected int numColorKey;

    /** field MFFloat texCoordKey */
    protected float[] vfTexCoordKey;

    /** Number of valid texCoordKey values */
    protected int numTexCoordKey;

    /** Value of the isActive outputOnly field */
    protected boolean vfIsActive;

    // Other vars

    /** Temporary list for holding all the physics objects */
    private ArrayList physicsNodes;

    /** The geometry type as a flag. Set during setupFinished */
    protected int geometryType;

    /** The particle system this node is made of */
    protected ParticleSystem particleSystem;

    /** Internal implementation of the emitter */
    protected ParticleInitializer emitter;

    /** Colour ramp handler for dealing with the colour keys changing */
    protected ColorRampFunction colorFunction;

    /** Particle function for limiting max particle life */
    protected MaxTimeParticleFunction timeFunction;

    //----------------------------------------------------------
    // Methods internal to NRShape
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_GEOMETRY,
            FIELD_APPEARANCE,
            FIELD_COLOR_RAMP,
            FIELD_EMITTER,
            FIELD_PHYSICS,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_APPEARANCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "appearance");
		//fieldDecl[FIELD_GEOMETRY] =
        //    new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
        //                             "SFNode",
        //                             "geometry");
        fieldDecl[FIELD_COLOR_RAMP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "colorRamp");
        fieldDecl[FIELD_COLOR_KEY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFFloat",
                                     "colorKey");
        fieldDecl[FIELD_EMITTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "emitter");
        fieldDecl[FIELD_PHYSICS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFNode",
                                     "physics");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_GEOMETRY_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "geometryType");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_MAX_PARTICLES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "maxParticles");
        fieldDecl[FIELD_PARTICLE_LIFETIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "particleLifetime");
        fieldDecl[FIELD_PARTICLE_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "particleSize");
        fieldDecl[FIELD_LIFETIME_VARIATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "lifetimeVariation");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_CREATE_PARTICLES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "createParticles");
        fieldDecl[FIELD_TEXCOORD_RAMP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "texCoordRamp");
        fieldDecl[FIELD_TEXCOORD_KEY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFFloat",
                                     "texCoordKey");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_APPEARANCE);
        fieldMap.put("appearance", idx);
        fieldMap.put("set_appearance", idx);
        fieldMap.put("appearance_changed", idx);

        idx = new Integer(FIELD_LIFETIME_VARIATION);
        fieldMap.put("lifetimeVariation", idx);
        fieldMap.put("set_lifetimeVariation", idx);
        fieldMap.put("lifetimeVariation_changed", idx);

        idx = new Integer(FIELD_MAX_PARTICLES);
        fieldMap.put("maxParticles", idx);
        fieldMap.put("set_maxParticles", idx);
        fieldMap.put("maxParticles_changed", idx);

        idx = new Integer(FIELD_PARTICLE_LIFETIME);
        fieldMap.put("particleLifetime", idx);
        fieldMap.put("set_particleLifetime", idx);
        fieldMap.put("particleLifetime_changed", idx);

        idx = new Integer(FIELD_PARTICLE_SIZE);
        fieldMap.put("particleSize", idx);
        fieldMap.put("set_particleSize", idx);
        fieldMap.put("particleSize_changed", idx);

        idx = new Integer(FIELD_CREATE_PARTICLES);
        fieldMap.put("createParticles", idx);
        fieldMap.put("set_createParticles", idx);
        fieldMap.put("createParticles_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("emitter", new Integer(FIELD_EMITTER));
        fieldMap.put("physics", new Integer(FIELD_PHYSICS));
        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));
        fieldMap.put("geometryType", new Integer(FIELD_GEOMETRY_TYPE));
        fieldMap.put("isActive", new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("colorRamp", new Integer(FIELD_COLOR_RAMP));
        fieldMap.put("colorKey", new Integer(FIELD_COLOR_KEY));
        fieldMap.put("texCoordRamp", new Integer(FIELD_TEXCOORD_RAMP));
        fieldMap.put("texCoordKey", new Integer(FIELD_TEXCOORD_KEY));


        geomTypeMap = new HashMap();
        geomTypeMap.put("QUAD", new Integer(TYPE_QUADS));
        geomTypeMap.put("TRIANGLE", new Integer(TYPE_TRIS));
        geomTypeMap.put("LINE", new Integer(TYPE_LINES));
        geomTypeMap.put("POINT", new Integer(TYPE_POINTS));
        geomTypeMap.put("SPRITE", new Integer(TYPE_SPRITES));
        geomTypeMap.put("GEOMETRY", new Integer(TYPE_CUSTOM));
    }

    /**
     * Construct a new default shape node implementation.
     */
    protected BaseParticleSystem() {
        super("ParticleSystem");

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};

        hasChanged = new boolean[LAST_PARTICLE_INDEX + 1];
        vfEnabled = true;
        vfGeometryType = "QUAD";
        geometryType = TYPE_QUADS;

        vfMaxParticles = 200;
        vfParticleLifetime = 5;
        vfLifetimeVariation = 0.25f;
        vfIsActive = false;
        vfCreateParticles = true;

        vfParticleSize = new float[] { 0.02f, 0.02f };

        numColorKey = 0;
        vfColorKey = FieldConstants.EMPTY_MFFLOAT;

        numTexCoordKey = 0;
        vfTexCoordKey = FieldConstants.EMPTY_MFFLOAT;

        timeFunction = new MaxTimeParticleFunction();

        physicsNodes = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    protected BaseParticleSystem(VRMLNodeType node) {
        this();

        checkNodeType(node);

        VRMLBoundedNodeType bn = (VRMLBoundedNodeType)node;

        setBboxSize(bn.getBboxSize());
        setBboxCenter(bn.getBboxCenter());

        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("geometryType");
            field = node.getFieldValue(index);
            vfGeometryType = field.stringValue;

            Integer geom = (Integer)geomTypeMap.get(vfGeometryType);
            geometryType = geom.intValue();

            index = node.getFieldIndex("maxParticles");
            field = node.getFieldValue(index);
            vfMaxParticles = field.intValue;

            index = node.getFieldIndex("particleLifetime");
            field = node.getFieldValue(index);
            vfParticleLifetime = field.floatValue;

            index = node.getFieldIndex("particleSize");
            field = node.getFieldValue(index);
            vfParticleSize[0] = field.floatArrayValue[0];
            vfParticleSize[1] = field.floatArrayValue[1];

            index = node.getFieldIndex("lifetimeVariation");
            field = node.getFieldValue(index);
            vfLifetimeVariation = field.floatValue;

            index = node.getFieldIndex("createParticles");
            field = node.getFieldValue(index);
            vfCreateParticles = field.booleanValue;

            index = node.getFieldIndex("colorKey");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfColorKey = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfColorKey,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("texCoordKey");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfTexCoordKey = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfTexCoordKey,
                                 0,
                                 field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLParticleSystemNodeType
    //-------------------------------------------------------------

    /**
     * Get the internal initialiser model that will be used for this particle
     * system implementation. This may not be available until after
     * setupFinished() has been called.
     *
     * @return The initialiser instance to use
     */
    public ParticleSystem getSystem() {
        return particleSystem;
    }

    /**
     * Set the enabled state of the Physics model.
     *
     * @param state true to enable the use of this model
     */
    public void setEnabled(boolean state) {
        vfEnabled = state;

        if(!inSetup) {
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);

            setActive(state);
        }
    }

    /**
     * Get the current enabled state of this model.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Check to see if this node is currently active for evaluation.
     *
     * @return true if the node still can run this frame
     */
    public boolean isActive() {
        return vfIsActive;
    }

    /**
     * Manually set the particle system to inactive due to the behaviour of
     * the internals. The manager has decided that this node is no longer
     * needing to be run, so indicate that the activity level has changed.
     *
     * @param state true to set this as active, false for inActive
     */
    public void setActive(boolean state) {

        if((state == vfIsActive) || (vfEmitter == null))
            return;

        vfIsActive = state;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }


    //-------------------------------------------------------------
    // Methods defined by VRMLShapeNodeType
    //-------------------------------------------------------------

    /**
     * Get node content for appearance.
     *
     * @return The current appearance
     */
    public VRMLNodeType getAppearance() {
        if(pAppearance != null)
            return pAppearance;
        else
            return vfAppearance;
    }

    /**
     * Set node content as replacement for appearance.
     *
     * @param newAppearance The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setAppearance(VRMLNodeType newAppearance)
        throws InvalidFieldValueException {

        VRMLAppearanceNodeType node;

        if (newAppearance instanceof VRMLProtoInstance) {
            node = (VRMLAppearanceNodeType)
                ((VRMLProtoInstance)newAppearance).getImplementationNode();
            pAppearance = (VRMLProtoInstance) newAppearance;
            if (!(node instanceof VRMLAppearanceNodeType)) {
                throw new InvalidFieldValueException(APPEARANCE_PROTO_MSG);
            }
        } else if (newAppearance != null &&
            (!(newAppearance instanceof VRMLAppearanceNodeType))) {
            throw new InvalidFieldValueException(APPEARANCE_NODE_MSG);
        } else {
            pAppearance = null;
            node = (VRMLAppearanceNodeType) newAppearance;
        }

        vfAppearance = (VRMLAppearanceNodeType)node;

        if (!inSetup) {
            hasChanged[FIELD_APPEARANCE] = true;
            fireFieldChanged(FIELD_APPEARANCE);
        }
    }

    /**
     * Get node content for <code>geometry</code>
     *
     * @return The current geoemtry field
     */
    public VRMLNodeType getGeometry() {
        return null;
    }

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param newGeomtry The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType newGeometry)
        throws InvalidFieldValueException {
    }

   //----------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //----------------------------------------------------------

    /**
     * Get the current value of the bboxCenter field. Default value is
     * <code>0 0 0</code>.
     *
     * @return Value of vfBboxCenter(SFVec3f)
     */
    public float[] getBboxCenter() {
        return vfBboxCenter;
    }

    /**
     * Get the value of the bboxSize field. Default value is
     * <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    public float[] getBboxSize() {
        return vfBboxSize;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     *
     * Assumes that the derived node has initialised the particleSystem
     * variable of this class before calling this method.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pAppearance != null)
            pAppearance.setupFinished();
        else if(vfAppearance != null)
            vfAppearance.setupFinished();

        if(pEmitter != null)
            pEmitter.setupFinished();
        else if(vfEmitter != null)
            vfEmitter.setupFinished();

        if(vfEmitter != null) {
            emitter = vfEmitter.getInitializer();
            particleSystem.setParticleInitializer(emitter);
            emitter.setMaxParticleCount(vfMaxParticles);
            emitter.setLifetimeVariation(vfLifetimeVariation);
            emitter.setParticleLifetime((int)(vfParticleLifetime * 1000));
            vfIsActive = true;
        }

        if(vfEmitter == null && pEmitter == null)
            vfIsActive = false;

        particleSystem.addParticleFunction(timeFunction);

        int num_physics_nodes = physicsNodes.size();
        vfPhysics = new VRMLNodeType[num_physics_nodes];

        for(int i = 0; i < num_physics_nodes; i++) {
            vfPhysics[i] = (VRMLNodeType)physicsNodes.get(i);
            vfPhysics[i].setupFinished();

            // find the physics function from this node.
            ParticleFunction func = findParticleFunction(vfPhysics[i]);
            particleSystem.addParticleFunction(func);
        }

        physicsNodes = null;

        particleSystem.addParticleFunction(new PhysicsFunction());
        particleSystem.enableParticleCreation(vfCreateParticles);

        if(vfColorRamp != null) {
            int num_comp = vfColorRamp.getNumColorComponents();
            boolean has_alpha = num_comp == 4;

            int index = vfColorRamp.getFieldIndex("color");
            VRMLFieldData field = vfColorRamp.getFieldValue(index);

            colorFunction = new ColorRampFunction(vfColorKey,
                                                  field.floatArrayValue,
                                                  field.numElements,
                                                  has_alpha);

            particleSystem.addParticleFunction(colorFunction);
        }

        // Finally, generate texture coordinates if we are given some
        if((vfTexCoordRamp != null) && (numTexCoordKey != 0)) {
            // Now check that there are enough coordinates in the texCoord
            // node, and then set the number of elements to the smaller number
            // of keys and values.
            int num_tex_coords = vfTexCoordRamp.getSize(0);
            float[] tex_coord  = new float[num_tex_coords];
            vfTexCoordRamp.getPoint(0, tex_coord);

            int num_tc_per_particle = 0;
            switch(geometryType) {
                case TYPE_QUADS:
                case TYPE_TRIS:
                    num_tc_per_particle = 8;  // 4 coord * 2 floats per tc
                    break;

                case TYPE_LINES:
                    num_tc_per_particle = 4;
                    break;

                // points have zero. NFI on sprites
            }

            // Find the minimum of the two lengths
            num_tex_coords /= num_tc_per_particle;

            int num_elements = num_tex_coords < numTexCoordKey ?
                               num_tex_coords :
                               numTexCoordKey;

            float[] time_keys = new float[num_elements];
            for(int i = 0; i < num_elements; i++)
                time_keys[i] = vfTexCoordKey[i] * 1000;

            particleSystem.setTexCoordFunction(time_keys,
                                               num_elements,
                                               tex_coord);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_PARTICLE_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ParticleSystemNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_APPEARANCE:
                fieldData.clear();
                if(pAppearance != null)
                    fieldData.nodeValue = pAppearance;
                else
                    fieldData.nodeValue = vfAppearance;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_EMITTER:
                fieldData.clear();
                if(pEmitter != null)
                    fieldData.nodeValue = pEmitter;
                else
                    fieldData.nodeValue = vfEmitter;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_COLOR_RAMP:
                fieldData.clear();
                if(pColorRamp != null)
                    fieldData.nodeValue = pColorRamp;
                else
                    fieldData.nodeValue = vfColorRamp;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_PHYSICS:
                fieldData.clear();
                fieldData.nodeArrayValue = vfPhysics;
                fieldData.numElements =
                    vfPhysics != null ? vfPhysics.length : 0;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_COLOR_KEY:
                fieldData.clear();
                fieldData.floatArrayValue = vfColorKey;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfColorKey.length;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_MAX_PARTICLES:
                fieldData.clear();
                fieldData.intValue = vfMaxParticles;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_PARTICLE_LIFETIME:
                fieldData.clear();
                fieldData.floatValue = vfParticleLifetime;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LIFETIME_VARIATION:
                fieldData.clear();
                fieldData.floatValue = vfLifetimeVariation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_GEOMETRY_TYPE:
                fieldData.clear();
                fieldData.stringValue = vfGeometryType;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_PARTICLE_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfParticleSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_CREATE_PARTICLES:
                fieldData.clear();
                fieldData.booleanValue = vfCreateParticles;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_APPEARANCE:
                    if(pAppearance != null)
                        destNode.setValue(destIndex, pAppearance);
                    else
                        destNode.setValue(destIndex, vfAppearance);
                    break;

                case FIELD_COLOR_RAMP:
                    if(pColorRamp != null)
                        destNode.setValue(destIndex, pColorRamp);
                    else
                        destNode.setValue(destIndex, vfColorRamp);
                    break;

                case FIELD_COLOR_KEY:
                    destNode.setValue(destIndex, vfColorKey, numColorKey);
                    break;

                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_MAX_PARTICLES:
                    destNode.setValue(destIndex, vfMaxParticles);
                    break;

                case FIELD_PARTICLE_LIFETIME:
                    destNode.setValue(destIndex, vfParticleLifetime);
                    break;

                case FIELD_LIFETIME_VARIATION:
                    destNode.setValue(destIndex, vfLifetimeVariation);
                    break;

                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_PARTICLE_SIZE:
                    destNode.setValue(destIndex, vfParticleSize, 2);
                    break;

                case FIELD_CREATE_PARTICLES:
                    destNode.setValue(destIndex, vfCreateParticles);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ENABLED:
                setEnabled(value);
                break;

            case FIELD_IS_ACTIVE:
                throw new InvalidFieldAccessException(ACTIVE_WRITE_MSG);

            case FIELD_CREATE_PARTICLES:
                setCreateParticles(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an int. This would be
     * used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MAX_PARTICLES:
                setMaxParticles(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an string. This would be
     * used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GEOMETRY_TYPE:
                if(!inSetup)
                    throw new InvalidFieldValueException(
                        INIT_ONLY_WRITE_MSG  + "geometryType");

                vfGeometryType = value;
                Integer geom = (Integer)geomTypeMap.get(vfGeometryType);
                geometryType = geom.intValue();
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would be
     * used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_LIFETIME_VARIATION:
                setLifetimeVariation(value);
                break;

            case FIELD_PARTICLE_LIFETIME:
                setParticleLifetime(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would be
     * used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;

            case FIELD_COLOR_KEY:
                setColorKey(value, numValid);
                break;

            case FIELD_TEXCOORD_KEY:
                setTexCoordKey(value, numValid);
                break;

            case FIELD_PARTICLE_SIZE:
                setParticleSize(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_APPEARANCE:
                setAppearance(node);
                break;

            case FIELD_COLOR_RAMP:
                setColorRamp(node);
                break;

            case FIELD_TEXCOORD_RAMP:
                setTexCoordRamp(node);
                break;

            case FIELD_EMITTER:
                if(!inSetup)
                    throw new InvalidFieldValueException(
                        INIT_ONLY_WRITE_MSG  + "emitter");

                setEmitter(child);
                break;

            case FIELD_PHYSICS:
                if(!inSetup)
                    throw new InvalidFieldValueException(
                        INIT_ONLY_WRITE_MSG  + "physics");

                addPhysics(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_PHYSICS:
                if(!inSetup)
                    throw new InvalidFieldValueException(
                        INIT_ONLY_WRITE_MSG  + "physics");

                for(int i = 0; i < numValid; i++)
                    addPhysics(children[i]);
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
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

        // check for all values being positive, increasing
        if((value[0] < 0) || (value[1] < 0))
            throw new InvalidFieldValueException(
                NEG_SIZE_ERR + value[0] + " " + value[1]);

        vfParticleSize[0] = value[0];
        vfParticleSize[1] = value[1];

        if(!inSetup) {
            hasChanged[FIELD_PARTICLE_SIZE] = true;
            fireFieldChanged(FIELD_PARTICLE_SIZE);
        }
    }

    /**
     * Set the colorKey field to the new value.
     *
     * @param value The new colour keys to use
     * @param numValid The number of valid values to copy from the array
     */
    private void setColorKey(float[] value, int numValid)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                INIT_ONLY_WRITE_MSG  + "colorKey");

        // check for all values being positive, increasing
        if(numValid > numColorKey || vfColorKey == null)
            vfColorKey = new float[numValid];

        numColorKey = numValid;

        if(value[0] < 0)
            throw new InvalidFieldValueException("colorKey[0] is < 0");

        vfColorKey[0] = value[0];

        for(int i = 1; i < numValid; i++) {
            if(value[i] <= value[i - 1])
                throw new InvalidFieldValueException(
                    "colorKey[" + i + "] is <= colorKey[" + (i-1) + "]");
            vfColorKey[i] = value[i];
        }
    }

    /**
     * Set the texCoordKey field to the new value.
     *
     * @param value The new colour keys to use
     * @param numValid The number of valid values to copy from the array
     */
    private void setTexCoordKey(float[] value, int numValid)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                INIT_ONLY_WRITE_MSG  + "texCoordKey");

        // check for all values being positive, increasing
        if(numValid > numTexCoordKey || vfTexCoordKey == null)
            vfTexCoordKey = new float[numValid];

        numTexCoordKey = numValid;

        if(value[0] < 0)
            throw new InvalidFieldValueException("texCoordKey[0] is < 0");

        vfTexCoordKey[0] = value[0];

        for(int i = 1; i < numValid; i++) {
            if(value[i] <= value[i - 1])
                throw new InvalidFieldValueException(
                    "texCoordKey[" + i + "] is <= texCoordKey[" + (i-1) + "]");
            vfTexCoordKey[i] = value[i];
        }
    }

    /**
     * Convenience method to set a new value the vfBboxCenter fields
     *
     * @param newBboxCenter The new center of the bounding box
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    after setup has finished
     */
    private void setBboxCenter(float[] newBboxCenter)
        throws InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                  "bboxCenter");

        vfBboxCenter[0] = newBboxCenter[0];
        vfBboxCenter[1] = newBboxCenter[1];
        vfBboxCenter[2] = newBboxCenter[2];
    }

    /**
     * Convenience method to set a new value for the vfBboxSize field.
     *
     * @param newBboxSize The new size for the bounding box
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    after setup has finished
     */
    private void setBboxSize(float[] newBboxSize)
        throws InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                  "bboxSize");

        vfBboxSize[0] = newBboxSize[0];
        vfBboxSize[1] = newBboxSize[1];
        vfBboxSize[2] = newBboxSize[2];
    }

    /**
     * Set the maxParticles field to a new value.
     *
     * @param value The new count to use
     * @throws InvalidFieldValueException The value is negative
     */
    private void setMaxParticles(int value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_PARTICLES_ERR);

        vfMaxParticles = value;

        if(!inSetup) {
            particleSystem.setMaxParticleCount(vfMaxParticles);
            hasChanged[FIELD_MAX_PARTICLES] = true;
            fireFieldChanged(FIELD_MAX_PARTICLES);
        }
    }

    /**
     * Set the new value for the particle lifetime. Time is in seconds.
     *
     * @param value The new lifetime value
     * @throws InvalidFieldValueException The
     */
    private void setParticleLifetime(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_LIFETIME_ERR);

        vfParticleLifetime = value;

        if(!inSetup) {
            // j3d.org wants time in milliseconds
            emitter.setParticleLifetime((int)(vfParticleLifetime * 1000));
            hasChanged[FIELD_PARTICLE_LIFETIME] = true;
            fireFieldChanged(FIELD_PARTICLE_LIFETIME);
        }
    }

    /**
     * Set the lifetimeVariation field to a new value.
     *
     * @param value The new variation to use
     * @throws InvalidFieldValueException The value outside [0,1]
     */
    private void setLifetimeVariation(float value)
        throws InvalidFieldValueException {

        if(value < 0 || value > 1)
            throw new InvalidFieldValueException(VARIATION_RANGE_ERR);

        vfLifetimeVariation = value;

        if(!inSetup) {
            emitter.setLifetimeVariation(value);
            hasChanged[FIELD_LIFETIME_VARIATION] = true;
            fireFieldChanged(FIELD_LIFETIME_VARIATION);
        }
    }

    /**
     * Set the createParticles field to a new value.
     *
     * @param value The new state to use
     */
    private void setCreateParticles(boolean value)
        throws InvalidFieldValueException {

        vfCreateParticles = value;

        if(!inSetup) {
            particleSystem.enableParticleCreation(value);
            hasChanged[FIELD_CREATE_PARTICLES] = true;
            fireFieldChanged(FIELD_CREATE_PARTICLES);
        }
    }

    /**
     * Set node content as replacement for colorRamp.
     *
     * @param ramp The new colorRamp
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setColorRamp(VRMLNodeType ramp)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                INIT_ONLY_WRITE_MSG  + "colorRamp");

        if (ramp instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)ramp).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLColorNodeType))
                throw new InvalidFieldValueException(COLOR_PROTO_MSG);

            vfColorRamp = (VRMLColorNodeType)impl;
            pColorRamp = (VRMLProtoInstance)ramp;
        } else if (ramp != null &&
            (!(ramp instanceof VRMLColorNodeType))) {
                throw new InvalidFieldValueException(COLOR_NODE_MSG);
        } else {
            pColorRamp = null;
            vfColorRamp = (VRMLColorNodeType)ramp;
        }
    }

    /**
     * Set node content as replacement for texCoordRamp.
     *
     * @param ramp The new texCoordRamp
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setTexCoordRamp(VRMLNodeType ramp)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                INIT_ONLY_WRITE_MSG  + "texCoordRamp");

        if (ramp instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)ramp).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLTextureCoordinateNodeType))
                throw new InvalidFieldValueException(TEXCOORD_PROTO_MSG);

            vfTexCoordRamp = (VRMLTextureCoordinateNodeType)impl;
            pTexCoordRamp = (VRMLProtoInstance)ramp;
        } else if (ramp != null &&
            (!(ramp instanceof VRMLTextureCoordinateNodeType))) {
                throw new InvalidFieldValueException(TEXCOORD_NODE_MSG);
        } else {
            pTexCoordRamp = null;
            vfTexCoordRamp = (VRMLTextureCoordinateNodeType)ramp;
        }
    }

    /**
     * Set content for the for emitter. Since the emitter is set once only, prior
     * to setupFinished, we can take a few shortcuts.
     *
     * @param emitter The new emitter
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setEmitter(VRMLNodeType emitter)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                INIT_ONLY_WRITE_MSG  + "emitter");

        if(emitter instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)emitter).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLParticleEmitterNodeType))
                throw new InvalidFieldValueException(EMITTER_PROTO_MSG);

            vfEmitter = (VRMLParticleEmitterNodeType)impl;
            pEmitter = (VRMLProtoInstance)emitter;
        } else if (emitter != null &&
            (!(emitter instanceof VRMLParticleEmitterNodeType))) {
            throw new InvalidFieldValueException(EMITTER_NODE_MSG);
        } else {
            pEmitter = null;
            vfEmitter = (VRMLParticleEmitterNodeType)emitter;
        }
    }

    /**
     * Check and add a physics node implementation.
     *
     * @param pn The physics node that needs to be checked for validity
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void addPhysics(VRMLNodeType pn)
        throws InvalidFieldValueException {

        if(pn instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)pn).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLParticlePhysicsModelNodeType))
                throw new InvalidFieldValueException(PHYSICS_PROTO_MSG);

        } else if (pn != null &&
            (!(pn instanceof VRMLParticlePhysicsModelNodeType))) {
            throw new InvalidFieldValueException(PHYSICS_NODE_MSG);
        }

        physicsNodes.add(pn);
    }

    /**
     * Dig down a physics node and go looking for the ParticleFunction instance
     * that it's wrapping.
     *
     * @param pn The physics node to check out
     */
    private ParticleFunction findParticleFunction(VRMLNodeType pn)
        throws InvalidFieldValueException {

        ParticleFunction ret_val = null;
        VRMLParticlePhysicsModelNodeType node = null;

        if(pn instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)pn).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl != null)
                node = (VRMLParticlePhysicsModelNodeType)impl;

        } else if(pn != null)
            node = (VRMLParticlePhysicsModelNodeType)pn;

        if(node != null)
            ret_val = node.getParticleFunction();

        return ret_val;
    }
}
