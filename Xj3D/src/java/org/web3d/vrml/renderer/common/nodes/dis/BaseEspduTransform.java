/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.dis;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

import org.web3d.xmsf.dis.ProtocolDataUnitType;
import org.web3d.xmsf.dis.EntityStatePduType;
import org.web3d.xmsf.dis.Vector3Double;
import org.web3d.xmsf.dis.Vector3Float;
import org.web3d.xmsf.dis.EulerAnglesType;
import org.web3d.xmsf.dis.ArticulationParametersType;
import org.web3d.xmsf.dis.DeadReckoningParametersType;
import org.web3d.xmsf.dis.EntityIDType;
import org.web3d.xmsf.dis.EulerAnglesType;
import org.web3d.xmsf.dis.HeaderType;
import org.web3d.xmsf.dis.impl.EulerAnglesTypeImpl;
import org.web3d.xmsf.dis.impl.EntityIDTypeImpl;
import org.web3d.xmsf.dis.impl.Vector3DoubleImpl;
import org.web3d.xmsf.dis.impl.Vector3FloatImpl;
import org.web3d.xmsf.disutil.PduFactory;

import mil.navy.nps.dis.*;
import mil.navy.nps.disEnumerations.PduTypeField;
import mil.navy.nps.math.Quaternion;

import org.web3d.xmsf.dis.EntityType;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.j3d.util.ErrorReporter;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

/**
 * Common implementation of a EspuTransform node functionality.
 * <p>
 *
 * This base class does not automatically update the underlying transform
 * with each set() call. These calls only update the local field values,
 * but not the transform that would be used in the rendering code. To make
 * sure this is updated, call the {@link #updateMatrix()} method and then
 * use the updated matrix in your rendering code.
 *
 *
 *  Filter on: siteID, AppID, EntityID to know who to deliver a packet to
 *  Don't worry about dynamic changes to siteID,AppID,EntityID on first impl
 *
 *  Two prototype fields have been added:
 *     geoSystem and geoOrigin
 *
 *  Spec notes:
 *
 *  Why do we need read and writeInterval?
 *
 *  isActive feels weird.  Does it turn itself off, or just reflect network state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.33 $
 */
public class BaseEspduTransform extends BaseGroupingNode
    implements VRMLNetworkInterfaceNodeType, VRMLDISNodeType,
               VRMLTimeDependentNodeType {

    /** Network modes, standalone */
    protected static final int MODE_STANDALONE = VRMLNetworkInterfaceNodeType.ROLE_INACTIVE;

    /** Network modes, writer */
    protected static final int MODE_WRITER = VRMLNetworkInterfaceNodeType.ROLE_WRITER;

    /** Network modes, reader */
    protected static final int MODE_READER = VRMLNetworkInterfaceNodeType.ROLE_READER;

    /** Protocol implemented */
    protected static final String PROTOCOL = "DIS";

    protected static final String DISPROTOCOL_HANDLER_PROP =
        "org.web3d.xj3d.disprotocol_handler.class";

    private static final int[] SECONDARY_TYPE = {
        TypeConstants.TimeDependentNodeType
    };

    /** Field Index */
    protected static final int FIELD_CENTER = LAST_GROUP_INDEX + 1;
    protected static final int FIELD_ROTATION = LAST_GROUP_INDEX + 2;
    protected static final int FIELD_SCALE = LAST_GROUP_INDEX + 3;
    protected static final int FIELD_SCALE_ORIENTATION = LAST_GROUP_INDEX + 4;
    protected static final int FIELD_TRANSLATION = LAST_GROUP_INDEX + 5;
    protected static final int FIELD_MARKING = LAST_GROUP_INDEX + 6;
    protected static final int FIELD_SITE_ID = LAST_GROUP_INDEX + 7;
    protected static final int FIELD_APPLICATION_ID = LAST_GROUP_INDEX + 8;
    protected static final int FIELD_ENTITY_ID = LAST_GROUP_INDEX + 9;
    protected static final int FIELD_READ_INTERVAL = LAST_GROUP_INDEX + 10;
    protected static final int FIELD_WRITE_INTERVAL = LAST_GROUP_INDEX + 11;
    protected static final int FIELD_NETWORK_MODE = LAST_GROUP_INDEX + 12;
    protected static final int FIELD_ADDRESS = LAST_GROUP_INDEX + 13;
    protected static final int FIELD_PORT = LAST_GROUP_INDEX + 14;
    protected static final int FIELD_ARTICULATION_PARAMETER_COUNT = LAST_GROUP_INDEX + 15;
    protected static final int FIELD_ARTICULATION_PARAMETER_ARRAY = LAST_GROUP_INDEX + 16;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED = LAST_GROUP_INDEX + 17;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED = LAST_GROUP_INDEX + 18;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED = LAST_GROUP_INDEX + 19;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED = LAST_GROUP_INDEX + 20;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED = LAST_GROUP_INDEX + 21;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED = LAST_GROUP_INDEX + 22;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED = LAST_GROUP_INDEX + 23;
    protected static final int FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED = LAST_GROUP_INDEX + 24;
    protected static final int FIELD_IS_ACTIVE = LAST_GROUP_INDEX + 25;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE0 = LAST_GROUP_INDEX + 26;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE1 = LAST_GROUP_INDEX + 27;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE2 = LAST_GROUP_INDEX + 28;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE3 = LAST_GROUP_INDEX + 29;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE4 = LAST_GROUP_INDEX + 30;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE5 = LAST_GROUP_INDEX + 31;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE6 = LAST_GROUP_INDEX + 32;
    protected static final int FIELD_SET_ARTICULATION_PARAMETER_VALUE7 = LAST_GROUP_INDEX + 33;
    protected static final int FIELD_TIMESTAMP = LAST_GROUP_INDEX + 34;
    protected static final int FIELD_DETONATION_RESULT = LAST_GROUP_INDEX + 35;
    protected static final int FIELD_DETONATION_LOCATION = LAST_GROUP_INDEX + 36;
    protected static final int FIELD_DETONATION_RELATIVE_LOCATION = LAST_GROUP_INDEX + 37;
    protected static final int FIELD_IS_DETONATED = LAST_GROUP_INDEX + 38;
    protected static final int FIELD_DETONATE_TIME = LAST_GROUP_INDEX + 39;
    protected static final int FIELD_EVENT_APPLICATION_ID = LAST_GROUP_INDEX + 40;
    protected static final int FIELD_EVENT_ENTITY_ID = LAST_GROUP_INDEX + 41;
    protected static final int FIELD_EVENT_SITE_ID = LAST_GROUP_INDEX + 42;
    protected static final int FIELD_FIRED_1 = LAST_GROUP_INDEX + 43;
    protected static final int FIELD_FIRED_2 = LAST_GROUP_INDEX + 44;
    protected static final int FIELD_FIRE_MISSION_INDEX = LAST_GROUP_INDEX + 45;
    protected static final int FIELD_FIRING_RANGE = LAST_GROUP_INDEX + 46;
    protected static final int FIELD_FIRING_RATE = LAST_GROUP_INDEX + 47;
    protected static final int FIELD_MUNITION_APPLICATION_ID = LAST_GROUP_INDEX + 48;
    protected static final int FIELD_MUNITION_END_POINT = LAST_GROUP_INDEX + 49;
    protected static final int FIELD_MUNITION_ENTITY_ID = LAST_GROUP_INDEX + 50;
    protected static final int FIELD_MUNITION_SITE_ID = LAST_GROUP_INDEX + 51;
    protected static final int FIELD_MUNITION_START_POINT = LAST_GROUP_INDEX + 52;
    protected static final int FIELD_FIRED_TIME = LAST_GROUP_INDEX + 53;
    protected static final int FIELD_GEO_SYSTEM = LAST_GROUP_INDEX + 54;
    protected static final int FIELD_GEO_ORIGIN = LAST_GROUP_INDEX + 55;
    protected static final int FIELD_ENTITY_CATEGORY = LAST_GROUP_INDEX + 56;
    protected static final int FIELD_ENTITY_DOMAIN = LAST_GROUP_INDEX + 57;
    protected static final int FIELD_ENTITY_EXTRA = LAST_GROUP_INDEX + 58;
    protected static final int FIELD_ENTITY_KIND = LAST_GROUP_INDEX + 59;
    protected static final int FIELD_ENTITY_SPECIFIC = LAST_GROUP_INDEX + 60;
    protected static final int FIELD_ENTITY_COUNTRY = LAST_GROUP_INDEX + 61;
    protected static final int FIELD_ENTITY_SUBCATEGORY = LAST_GROUP_INDEX + 62;
    protected static final int FIELD_APPEARANCE = LAST_GROUP_INDEX + 63;
    protected static final int FIELD_LINEAR_VELOCITY = LAST_GROUP_INDEX + 64;
    protected static final int FIELD_LINEAR_ACCELERATION = LAST_GROUP_INDEX + 65;
    protected static final int FIELD_FORCE_ID = LAST_GROUP_INDEX + 66;
    protected static final int FIELD_XMPP_PARAMS = LAST_GROUP_INDEX + 67;

    protected static final int FIELD_NOT_IMPL = LAST_GROUP_INDEX + 68;

    /** The last field index used by this class */
    protected static final int LAST_TRANSFORM_INDEX = FIELD_XMPP_PARAMS;

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TRANSFORM_INDEX + 1;

    /** Message for when the proto is not a GeoOrigin */
    private static final String GEO_ORIGIN_PROTO_MSG =
        "Proto does not describe a GeoOrigin object";

    /** Message for when the node in setValue() is not a GeoOrigin */
    private static final String GEO_ORIGIN_NODE_MSG =
        "Node does not describe a GeoOrigin object";

    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFVec3f center */
    protected float[] vfCenter;

    /** SFRotation rotation */
    protected float[] vfRotation;

    /** SFVec3f scale */
    protected float[] vfScale;

    /** SFRotation scaleOrientation */
    protected float[] vfScaleOrientation;

    /** SFVec3f translation */
    protected float[] vfTranslation;

    /** marking   SFString "" */
    protected String vfMarking;

    /** siteID SFInt32 0 */
    protected int vfSiteID;

    /** applicationID   SFInt32 0 */
    protected int vfApplicationID;

    /** entityID  SFInt32 0 */
    protected int vfEntityID;

    /** entityCategory  SFInt32 0 */
    protected int vfEntityCategory;

    /** appearance  SFInt32 0 */
    protected int vfAppearance;

    /** entityDomain  SFInt32 0 */
    protected int vfEntityDomain;

    /** entityExtra  SFInt32 0 */
    protected int vfEntityExtra;

    /** entityKind  SFInt32 0 */
    protected int vfEntityKind;

    /** entitySpecific  SFInt32 0 */
    protected int vfEntitySpecific;

    /** entitySubCategory  SFInt32 0 */
    protected int vfEntitySubCategory;

    /** entityCountry  SFInt32 0 */
    protected int vfEntityCountry;

    /** readInterval SFTime 0.1 */
    protected double vfReadInterval;

    /** writeInterval   SFTime 1 */
    protected double vfWriteInterval;

    /** networkMode  SFString "standAlone" */
    protected String vfNetworkMode;

    /** xmppParams MFString [] */
    protected String[] vfXMPPParams;

    /** networkMode string to int conversion, String to Integer */
    protected static HashMap networkModes;

    /** Current mode as integer constant */
    protected int currentMode;

    /** address   SFString "localhost" */
    protected String vfAddress;

    /** port   SFInt32 0 */
    protected int vfPort;

    /** eventApplicationID SFInt32 */
    protected int vfEventApplicationID;

    /** eventEntityID SFInt32 */
    protected int vfEventEntityID;

    /** eventSiteID SFInt32 */
    protected int vfEventSiteID;

    /** fired1 SFBool */
    protected boolean vfFired1;

    /** fired2 SFBool */
    protected boolean vfFired2;

    /** fireMissionIndex SFInt32 */
    protected int vfFireMissionIndex;

    /** firingRange SFFloat */
    protected float vfFiringRange;

    /** firingRate SFInt32 */
    protected int vfFiringRate;

    /** munitionApplicationID SFInt32 */
    protected int vfMunitionApplicationID;

    /** munitionEntityID SFInt32 */
    protected int vfMunitionEntityID;

    /** munitionSiteID SFInt32 */
    protected int vfMunitionSiteID;

    /** munitionEndPoint   SFVec3f 0 0 0 */
    protected float [] vfMunitionEndPoint;

    /** munitionStartPoint SFVec3f 0 0 0 */
    protected float [] vfMunitionStartPoint;

    /** firedTime SFTime */
    protected double vfFiredTime;

    /** detonationResult   SFBool */
    protected boolean vfIsDetonated;

    /** detonationResult   SFTime */
    protected double vfDetonateTime;

    /** detonationResult   SFInt32 0 */
    protected int vfDetonationResult;

    /** detonationLocation   SFVec3f 0 0 0 */
    protected float [] vfDetonationLocation;

    /** detonationRelativeLocation   SFVec3f 0 0 0 */
    protected float [] vfDetonationRelativeLocation;

    /** articulationParameterCount  SFInt32 0 */
    protected int vfArticulationParameterCount;

    /** articulationParameterArray  MFFloat [] */
    protected float[] vfArticulationParameterArray;

    /** Number of valid values in vfArticulationParameterArray */
    protected int numArticulationParameterArray;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue0;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue1;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue2;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue3;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue4;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue5;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue6;

    /**    articulationParameterValueChanged0_changed SFFloat */
    protected float vfArticulationParameterValue7;

    /** forceID  SFInt32 0 */
    protected int vfForceID;

    /** isActive SFBool */
    protected boolean vfIsActive;

    /** linearVelocity SFVec3f */
    protected float[] vfLinearVelocity;

    /** linearAcceleration SFVec3f */
    protected float[] vfLinearAcceleration;

    // Prototype geoSystem and geoOrigin fields, fodder for ammendment 2

    /** geoSystem MFString ["GD","WE"] */
    protected String [] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLLocalOriginNodeType vfGeoOrigin;

    /** timestamp SFTime */
    protected double vfTimestamp;

    /** Do we have new values to write */
    protected boolean needToWrite;

    /** When did we last write a value */
    protected double lastWrite;

    /** List of those who want to know about role changes, likely 1 */
    protected ArrayList roleListeners;

    /** Working variables for the computation */
    private Vector3f tempVec;
    private AxisAngle4f tempAxis;
    private Matrix4f tempMtx1;
    private Matrix4f tempMtx2;
    protected Matrix4f tmatrix;

    // Scratch matrixes for smoothing
    Matrix3d rotationMatrix;
    Matrix3d psiMat;
    Matrix3d thetaMat;
    Matrix3d phiMat;
    Quat4d rotationQuat;

    /** Scratch articulation parameter vals */
    private ArticulationParameter[] artVals;

    /** Scratch articulation parameter vals */
    private ArticulationParametersType[] artValsDX;

    /** The clock used to drive collide time eventOuts */
    protected VRMLClock vrmlClock;

    /** Should we ignore espdus.  Only allow 1 per frame */
    protected boolean ignoreEspdu;

    // Scratch orienation var  using NPS quats
    private float[] dRorientation;
    private Quaternion quaternion;

    // A scratch Espdu for writing values out */
    private EntityStatePdu espdu;
    private EntityID id;
    private EntityStatePduType espduDX;
    private EntityIDType idDX;
    private Vector3Double v3d;
    private Vector3Float v3f;
    private Vector3Float scrLinearVelo;
    private Vector3Float scrLinearAccel;
    private AxisAngle4d aa;
    private EulerAnglesType eat;

    // XMPP connection vars
    private String xmppUsername;
    private String xmppPassword;
    private String[] xmppAuthServer;
    private String xmppMucServer;
    private String xmppMucRoom;

    /**
     * Transformation used to make the coordinates to the local system. Does
     * not include the geoOrigin offset calcs.
     */
    private MathTransform geoTransform;

    /**
     * Flag to say if the translation geo coords need to be swapped before
     * conversion.
     */
    private boolean geoCoordSwap;

    /** The converted origin to subtract */
    private double[] origin;

    /** FieldDecl for not implemented fields */
    private static VRMLFieldDeclaration notimpl_decl;

    /** The simulation start time for calculating time stamps */
    private static long simStartTime;

    /** Are we using DISXML */
    private boolean useDX;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        simStartTime = System.currentTimeMillis();

        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA, FIELD_GEO_ORIGIN };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "center");
        fieldDecl[FIELD_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "rotation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "scale");
        fieldDecl[FIELD_SCALE_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "scaleOrientation");
        fieldDecl[FIELD_TRANSLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "translation");

        fieldDecl[FIELD_MARKING] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "marking");

        fieldDecl[FIELD_APPEARANCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "appearance");

        fieldDecl[FIELD_SITE_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "siteID");

        fieldDecl[FIELD_ENTITY_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityID");

        fieldDecl[FIELD_ENTITY_CATEGORY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityCategory");

        fieldDecl[FIELD_ENTITY_DOMAIN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityDomain");

        fieldDecl[FIELD_ENTITY_KIND] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityKind");

        fieldDecl[FIELD_ENTITY_SPECIFIC] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entitySpecific");

        fieldDecl[FIELD_ENTITY_SUBCATEGORY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entitySubCategory");

        fieldDecl[FIELD_ENTITY_COUNTRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityCountry");

        fieldDecl[FIELD_ENTITY_EXTRA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "entityExtra");

        fieldDecl[FIELD_EVENT_APPLICATION_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "eventApplicationID");

        fieldDecl[FIELD_EVENT_ENTITY_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "eventEntityID");

        fieldDecl[FIELD_EVENT_SITE_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "eventSiteID");

        fieldDecl[FIELD_FIRED_1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "fired1");

        fieldDecl[FIELD_FIRED_2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "fired2");

        fieldDecl[FIELD_FIRE_MISSION_INDEX] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "fireMissionIndex");

        fieldDecl[FIELD_FIRING_RANGE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "firingRange");

        fieldDecl[FIELD_MUNITION_APPLICATION_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "munitionApplicationID");

        fieldDecl[FIELD_MUNITION_ENTITY_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "munitionEntityID");

        fieldDecl[FIELD_MUNITION_SITE_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "munitionSiteID");

        fieldDecl[FIELD_MUNITION_END_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "munitionEndPoint");

        fieldDecl[FIELD_MUNITION_START_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "munitionStartPoint");

        fieldDecl[FIELD_FIRED_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "firedTime");

        fieldDecl[FIELD_FIRING_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "firingRate");

        fieldDecl[FIELD_DETONATION_LOCATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "detonationLocation");

        fieldDecl[FIELD_DETONATION_RELATIVE_LOCATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "detonationRelativeLocation");

        fieldDecl[FIELD_DETONATION_RESULT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "detonationResult");

        fieldDecl[FIELD_READ_INTERVAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "readInterval");

        fieldDecl[FIELD_WRITE_INTERVAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "writeInterval");

        fieldDecl[FIELD_NETWORK_MODE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "networkMode");

        fieldDecl[FIELD_ADDRESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "address");

        fieldDecl[FIELD_FORCE_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "forceID");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_COUNT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "articulationParameterCount");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_ARRAY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "articulationParameterArray");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue0_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue1_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue2_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue3_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue4_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue5_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue6_changed");

        fieldDecl[FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "articulationParameterValue7_changed");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE0] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue0");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE1] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue1");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE2] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue2");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE3] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue3");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE4] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue4");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE5] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue5");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE6] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue6");

        fieldDecl[FIELD_SET_ARTICULATION_PARAMETER_VALUE7] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_articulationParameterValue7");

        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_PORT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "port");

        fieldDecl[FIELD_APPLICATION_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "applicationID");

        fieldDecl[FIELD_DETONATE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "detonateTime");

        fieldDecl[FIELD_IS_DETONATED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isDetonated");

        fieldDecl[FIELD_TIMESTAMP] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "timestamp");

        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");

        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");

        fieldDecl[FIELD_XMPP_PARAMS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "xmppParams");

        fieldDecl[FIELD_LINEAR_VELOCITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "linearVelocity");

        fieldDecl[FIELD_LINEAR_ACCELERATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "linearAcceleration");

        fieldDecl[FIELD_XMPP_PARAMS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "xmppParams");

        notimpl_decl = new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "invalid");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_CENTER);
        fieldMap.put("center", idx);
        fieldMap.put("set_center", idx);
        fieldMap.put("center_changed", idx);

        idx = new Integer(FIELD_ROTATION);
        fieldMap.put("rotation", idx);
        fieldMap.put("set_rotation", idx);
        fieldMap.put("rotation_changed", idx);

        idx = new Integer(FIELD_SCALE);
        fieldMap.put("scale", idx);
        fieldMap.put("set_scale", idx);
        fieldMap.put("scale_changed", idx);

        idx = new Integer(FIELD_SCALE_ORIENTATION);
        fieldMap.put("scaleOrientation", idx);
        fieldMap.put("set_scaleOrientation", idx);
        fieldMap.put("scaleOrientation_changed", idx);

        idx = new Integer(FIELD_TRANSLATION);
        fieldMap.put("translation", idx);
        fieldMap.put("set_translation", idx);
        fieldMap.put("translation_changed", idx);

        idx = new Integer(FIELD_MARKING);
        fieldMap.put("marking", idx);
        fieldMap.put("set_marking", idx);
        fieldMap.put("marking_changed", idx);

        idx = new Integer(FIELD_APPEARANCE);
        fieldMap.put("appearance", idx);
        fieldMap.put("set_appearance", idx);
        fieldMap.put("appearance_changed", idx);

        idx = new Integer(FIELD_SITE_ID);
        fieldMap.put("siteID", idx);
        fieldMap.put("set_siteID", idx);
        fieldMap.put("siteID_changed", idx);

        idx = new Integer(FIELD_APPLICATION_ID);
        fieldMap.put("applicationID", idx);
        fieldMap.put("set_applicationID", idx);
        fieldMap.put("applicationID_changed", idx);

        idx = new Integer(FIELD_ENTITY_ID);
        fieldMap.put("entityID", idx);
        fieldMap.put("set_entityID", idx);
        fieldMap.put("entityID_changed", idx);

        idx = new Integer(FIELD_FORCE_ID);
        fieldMap.put("forceID", idx);
        fieldMap.put("set_forceID", idx);
        fieldMap.put("forceID_changed", idx);

        idx = new Integer(FIELD_ENTITY_CATEGORY);
        fieldMap.put("entityCategory", idx);
        fieldMap.put("set_entityCategory", idx);
        fieldMap.put("entityCategory_changed", idx);

        idx = new Integer(FIELD_ENTITY_COUNTRY);
        fieldMap.put("entityCountry", idx);
        fieldMap.put("set_entityCountry", idx);
        fieldMap.put("entityCountry_changed", idx);

        idx = new Integer(FIELD_ENTITY_DOMAIN);
        fieldMap.put("entityDomain", idx);
        fieldMap.put("set_entityDomain", idx);
        fieldMap.put("entityDomain_changed", idx);

        idx = new Integer(FIELD_ENTITY_EXTRA);
        fieldMap.put("entityExtra", idx);
        fieldMap.put("set_entityExtra", idx);
        fieldMap.put("entityExtra_changed", idx);

        idx = new Integer(FIELD_ENTITY_KIND);
        fieldMap.put("entityKind", idx);
        fieldMap.put("set_entityKind", idx);
        fieldMap.put("entityKind_changed", idx);

        idx = new Integer(FIELD_ENTITY_SPECIFIC);
        fieldMap.put("entitySpecific", idx);
        fieldMap.put("set_entitySpecific", idx);
        fieldMap.put("entitySpecific_changed", idx);

        idx = new Integer(FIELD_ENTITY_SUBCATEGORY);
        fieldMap.put("entitySubCategory", idx);
        fieldMap.put("set_entitySubCategory", idx);
        fieldMap.put("entitySubCategory_changed", idx);

        idx = new Integer(FIELD_READ_INTERVAL);
        fieldMap.put("readInterval", idx);
        fieldMap.put("set_readInterval", idx);
        fieldMap.put("readInterval_changed", idx);

        idx = new Integer(FIELD_WRITE_INTERVAL);
        fieldMap.put("writeInterval", idx);
        fieldMap.put("set_writeInterval", idx);
        fieldMap.put("writeInterval_changed", idx);

        idx = new Integer(FIELD_NETWORK_MODE);
        fieldMap.put("networkMode", idx);
        fieldMap.put("set_networkMode", idx);
        fieldMap.put("networkMode_changed", idx);

        idx = new Integer(FIELD_ADDRESS);
        fieldMap.put("address", idx);
        fieldMap.put("set_address", idx);
        fieldMap.put("address_changed", idx);

        idx = new Integer(FIELD_PORT);
        fieldMap.put("port", idx);
        fieldMap.put("set_port", idx);
        fieldMap.put("port_changed", idx);

        idx = new Integer(FIELD_EVENT_APPLICATION_ID);
        fieldMap.put("eventApplicationID", idx);
        fieldMap.put("set_eventApplicationID", idx);
        fieldMap.put("eventApplicationID_changed", idx);

        idx = new Integer(FIELD_EVENT_ENTITY_ID);
        fieldMap.put("eventEntityID", idx);
        fieldMap.put("set_eventEntityID", idx);
        fieldMap.put("eventEntityID_changed", idx);

        idx = new Integer(FIELD_FIRE_MISSION_INDEX);
        fieldMap.put("fireMissionIndex", idx);
        fieldMap.put("set_fireMissionIndex", idx);
        fieldMap.put("fireMissionIndex_changed", idx);

        idx = new Integer(FIELD_FIRING_RATE);
        fieldMap.put("firingRate", idx);
        fieldMap.put("set_firingRate", idx);
        fieldMap.put("firingRate_changed", idx);

        idx = new Integer(FIELD_MUNITION_APPLICATION_ID);
        fieldMap.put("munitionApplicationID", idx);
        fieldMap.put("set_munitionApplicationID", idx);
        fieldMap.put("munitionApplicationID_changed", idx);

        idx = new Integer(FIELD_MUNITION_ENTITY_ID);
        fieldMap.put("munitionEntityID", idx);
        fieldMap.put("set_munitionEntityID", idx);
        fieldMap.put("munitionEntityID_changed", idx);

        idx = new Integer(FIELD_MUNITION_SITE_ID);
        fieldMap.put("munitionSiteID", idx);
        fieldMap.put("set_munitionSiteID", idx);
        fieldMap.put("munitionSiteID_changed", idx);

        idx = new Integer(FIELD_MUNITION_END_POINT);
        fieldMap.put("munitionEndPoint", idx);
        fieldMap.put("set_munitionEndPoint", idx);
        fieldMap.put("munitionEndPoint_changed", idx);

        idx = new Integer(FIELD_MUNITION_START_POINT);
        fieldMap.put("munitionStartPoint", idx);
        fieldMap.put("set_munitionStartPoint", idx);
        fieldMap.put("munitionStartPoint_changed", idx);

        idx = new Integer(FIELD_FIRED_TIME);
        fieldMap.put("firedTime", idx);
        fieldMap.put("set_firedTime", idx);
        fieldMap.put("firedTime_changed", idx);

        idx = new Integer(FIELD_FIRED_1);
        fieldMap.put("fired1", idx);
        fieldMap.put("set_fired1", idx);
        fieldMap.put("fired1_changed", idx);

        idx = new Integer(FIELD_FIRED_2);
        fieldMap.put("fired2", idx);
        fieldMap.put("set_fired2", idx);
        fieldMap.put("fired2_changed", idx);

        idx = new Integer(FIELD_FIRING_RANGE);
        fieldMap.put("firingRange", idx);
        fieldMap.put("set_firingRange", idx);
        fieldMap.put("firingRange_changed", idx);

        idx = new Integer(FIELD_EVENT_SITE_ID);
        fieldMap.put("eventSiteID", idx);
        fieldMap.put("set_eventSiteID", idx);
        fieldMap.put("eventSiteID_changed", idx);

        idx = new Integer(FIELD_DETONATION_LOCATION);
        fieldMap.put("detonationLocation", idx);
        fieldMap.put("set_detonationLocation", idx);
        fieldMap.put("detonationLocation_changed", idx);

        idx = new Integer(FIELD_DETONATION_RELATIVE_LOCATION);
        fieldMap.put("detonationRelativeLocation", idx);
        fieldMap.put("set_detonationRelativeLocation", idx);
        fieldMap.put("detonationRelativeLocation_changed", idx);

        idx = new Integer(FIELD_DETONATION_RESULT);
        fieldMap.put("detonationResult", idx);
        fieldMap.put("set_detonationResult", idx);
        fieldMap.put("detonationResult_changed", idx);

        idx = new Integer(FIELD_IS_DETONATED);
        fieldMap.put("isDetonated", idx);
        fieldMap.put("isDetonated_changed", idx);

        idx = new Integer(FIELD_DETONATE_TIME);
        fieldMap.put("detonateTime", idx);
        fieldMap.put("detonateTime_changed", idx);

        idx = new Integer(FIELD_IS_ACTIVE);
        fieldMap.put("isActive", idx);
        fieldMap.put("isActive_changed", idx);

        idx = new Integer(FIELD_TIMESTAMP);
        fieldMap.put("timestamp", idx);
        fieldMap.put("timestamp_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_COUNT);
        fieldMap.put("articulationParameterCount", idx);
        fieldMap.put("set_articulationParameterCount", idx);
        fieldMap.put("articulationParameterCount_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_ARRAY);
        fieldMap.put("articulationParameterArray", idx);
        fieldMap.put("set_articulationParameterArray", idx);
        fieldMap.put("articulationParameterArray_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED);
        fieldMap.put("articulationParameterValue0_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED);
        fieldMap.put("articulationParameterValue1_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED);
        fieldMap.put("articulationParameterValue2_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED);
        fieldMap.put("articulationParameterValue3_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED);
        fieldMap.put("articulationParameterValue4_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED);
        fieldMap.put("articulationParameterValue5_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED);
        fieldMap.put("articulationParameterValue6_changed", idx);

        idx = new Integer(FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED);
        fieldMap.put("articulationParameterValue7_changed", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE0);
        fieldMap.put("set_articulationParameterValue0", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE1);
        fieldMap.put("set_articulationParameterValue1", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE2);
        fieldMap.put("set_articulationParameterValue2", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE3);
        fieldMap.put("set_articulationParameterValue3", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE4);
        fieldMap.put("set_articulationParameterValue4", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE5);
        fieldMap.put("set_articulationParameterValue5", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE6);
        fieldMap.put("set_articulationParameterValue6", idx);

        idx = new Integer(FIELD_SET_ARTICULATION_PARAMETER_VALUE7);
        fieldMap.put("set_articulationParameterValue7", idx);

        idx = new Integer(FIELD_GEO_SYSTEM);
        fieldMap.put("geoSystem", idx);

        idx = new Integer(FIELD_GEO_ORIGIN);
        fieldMap.put("geoOrigin", idx);

        idx = new Integer(FIELD_XMPP_PARAMS);
        fieldMap.put("xmppParams", idx);
        fieldMap.put("set_xmppParams", idx);
        fieldMap.put("xmppParams_changed", idx);

        idx = new Integer(FIELD_LINEAR_VELOCITY);
        fieldMap.put("linearVelocity", idx);
        fieldMap.put("set_linearVelocity", idx);
        fieldMap.put("linearVelocity_changed", idx);

        idx = new Integer(FIELD_LINEAR_ACCELERATION);
        fieldMap.put("linearAcceleration", idx);
        fieldMap.put("set_linearAcceleration", idx);
        fieldMap.put("linearAcceleration_changed", idx);

        networkModes = new HashMap(3);
        networkModes.put("standAlone", new Integer(MODE_STANDALONE));
        networkModes.put("networkWriter", new Integer(MODE_WRITER));
        networkModes.put("networkReader", new Integer(MODE_READER));
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseEspduTransform() {
        super("EspduTransform");

        hasChanged = new boolean[LAST_TRANSFORM_INDEX + 1];

        vfCenter = new float[] {0, 0, 0};
        vfRotation = new float[] {0, 0, 1, 0};
        vfScale = new float[] {1, 1, 1};
        vfScaleOrientation = new float[] {0, 0, 1, 0};
        vfTranslation = new float[] {0, 0, 0};
        vfEventApplicationID = 1;
        vfEntityID = 0;
        vfForceID = 0;
        vfAppearance = 1;
        vfEntityCategory = 0;
        vfEntityCountry = 0;
        vfEntityDomain = 0;
        vfEntityKind = 0;
        vfEntitySpecific = 0;
        vfEntityExtra = 0;
        vfEntitySubCategory = 0;
        vfEventSiteID = 0;
        vfFired1 = false;
        vfFired2 = false;
        vfFireMissionIndex = 0;
        vfFiringRange = 0;
        vfFiringRate = 0;
        vfMunitionApplicationID = 1;
        vfMunitionEntityID = 0;
        vfMunitionSiteID = 0;
        vfFiredTime = 0;
        vfMunitionStartPoint = new float[] {0, 0, 0};
        vfMunitionEndPoint = new float[] {0, 0, 0};
        vfDetonationLocation = new float[] {0, 0, 0};
        vfDetonationRelativeLocation = new float[] {0, 0, 0};
        vfMarking = "";
        vfSiteID = 0;
        vfApplicationID = 0;
        vfReadInterval = 0.1;
        vfWriteInterval = 1;
        vfNetworkMode = "standAlone";
        vfGeoSystem = new String[] {"GD", "WE"};
        vfXMPPParams = new String[] {};
        currentMode = MODE_STANDALONE;
        vfPort = 0;
        vfAddress = "";
        vfIsActive = false;
        vfIsDetonated = false;
        vfArticulationParameterCount = 0;
        vfArticulationParameterArray = FieldConstants.EMPTY_MFFLOAT;
        vfArticulationParameterValue0 = 0;
        vfArticulationParameterValue1 = 0;
        vfArticulationParameterValue2 = 0;
        vfArticulationParameterValue3 = 0;
        vfArticulationParameterValue4 = 0;
        vfArticulationParameterValue5 = 0;
        vfArticulationParameterValue6 = 0;
        vfArticulationParameterValue7 = 0;
        vfLinearVelocity = new float[] {0, 0, 0};
        vfLinearAcceleration = new float[] {0, 0, 0};

        artVals = new ArticulationParameter[0];
        artValsDX = new ArticulationParametersType[0];

        tmatrix = new Matrix4f();

        tempVec = new Vector3f();
        //tempAxis = new Vector4f();
        tempAxis = new AxisAngle4f();
        tempMtx1 = new Matrix4f();
        tempMtx2 = new Matrix4f();
        dRorientation = new float[3];
        quaternion = new Quaternion();
        roleListeners = new ArrayList(1);
        rotationMatrix = new Matrix3d();
        psiMat = new Matrix3d();
        thetaMat = new Matrix3d();
        phiMat = new Matrix3d();
        rotationQuat = new Quat4d();

        String prop = System.getProperty(DISPROTOCOL_HANDLER_PROP);

        aa = new AxisAngle4d();

        if (prop != null &&
            prop.equals("org.web3d.vrml.renderer.common.input.dis.DISXMLProtocolHandler")) {

            useDX = true;

            PduFactory pdf = new PduFactory();
            espduDX = pdf.getEntityStatePdu();
            v3d = new Vector3DoubleImpl();
            v3f = new Vector3FloatImpl();
            scrLinearVelo = new Vector3FloatImpl();
            scrLinearAccel = new Vector3FloatImpl();

            // TODO: Using ObjectFactory causes JAXB compile dependency
            eat = new EulerAnglesTypeImpl();
       } else {
            espdu = new EntityStatePdu();
       }
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseEspduTransform(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];
            vfCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("rotation");
            field = node.getFieldValue(index);

            vfRotation[0] = field.floatArrayValue[0];
            vfRotation[1] = field.floatArrayValue[1];
            vfRotation[2] = field.floatArrayValue[2];
            vfRotation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);

            vfScale[0] = field.floatArrayValue[0];
            vfScale[1] = field.floatArrayValue[1];
            vfScale[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("scaleOrientation");
            field = node.getFieldValue(index);

            vfScaleOrientation[0] = field.floatArrayValue[0];
            vfScaleOrientation[1] = field.floatArrayValue[1];
            vfScaleOrientation[2] = field.floatArrayValue[2];
            vfScaleOrientation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("translation");
            field = node.getFieldValue(index);

            vfTranslation[0] = field.floatArrayValue[0];
            vfTranslation[1] = field.floatArrayValue[1];
            vfTranslation[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("marking");
            field = node.getFieldValue(index);
            vfMarking = field.stringValue;

            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfGeoSystem, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("appearance");
            field = node.getFieldValue(index);
            vfAppearance = field.intValue;

            index = node.getFieldIndex("siteID");
            field = node.getFieldValue(index);
            vfSiteID = field.intValue;

            index = node.getFieldIndex("applicationID");
            field = node.getFieldValue(index);
            vfApplicationID = field.intValue;

            index = node.getFieldIndex("entityID");
            field = node.getFieldValue(index);
            vfEntityID = field.intValue;

            index = node.getFieldIndex("forceID");
            field = node.getFieldValue(index);
            vfForceID = field.intValue;

            index = node.getFieldIndex("entityCategory");
            field = node.getFieldValue(index);
            vfEntityCategory = field.intValue;

            index = node.getFieldIndex("entityDomain");
            field = node.getFieldValue(index);
            vfEntityDomain = field.intValue;

            index = node.getFieldIndex("entityExtra");
            field = node.getFieldValue(index);
            vfEntityExtra = field.intValue;

            index = node.getFieldIndex("entityKind");
            field = node.getFieldValue(index);
            vfEntityKind = field.intValue;

            index = node.getFieldIndex("entitySpecific");
            field = node.getFieldValue(index);
            vfEntitySpecific = field.intValue;

            index = node.getFieldIndex("entitySubCategory");
            field = node.getFieldValue(index);
            vfEntitySubCategory = field.intValue;

            index = node.getFieldIndex("entityCountry");
            field = node.getFieldValue(index);
            vfEntityCountry = field.intValue;

            index = node.getFieldIndex("eventApplicationID");
            field = node.getFieldValue(index);
            vfEventApplicationID = field.intValue;

            index = node.getFieldIndex("eventEntityID");
            field = node.getFieldValue(index);
            vfEventEntityID = field.intValue;

            index = node.getFieldIndex("eventSiteID");
            field = node.getFieldValue(index);
            vfEventSiteID = field.intValue;

            index = node.getFieldIndex("fireMissionIndex");
            field = node.getFieldValue(index);
            vfFireMissionIndex = field.intValue;

            index = node.getFieldIndex("firingRate");
            field = node.getFieldValue(index);
            vfFiringRate = field.intValue;

            index = node.getFieldIndex("munitionApplicationID");
            field = node.getFieldValue(index);
            vfMunitionApplicationID = field.intValue;

            index = node.getFieldIndex("munitionEntityID");
            field = node.getFieldValue(index);
            vfMunitionEntityID = field.intValue;

            index = node.getFieldIndex("munitionSiteID");
            field = node.getFieldValue(index);
            vfMunitionSiteID = field.intValue;

            index = node.getFieldIndex("munitionEndPoint");
            field = node.getFieldValue(index);

            vfMunitionEndPoint[0] = field.floatArrayValue[0];
            vfMunitionEndPoint[1] = field.floatArrayValue[1];
            vfMunitionEndPoint[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("munitionStartPoint");
            field = node.getFieldValue(index);

            vfMunitionStartPoint[0] = field.floatArrayValue[0];
            vfMunitionStartPoint[1] = field.floatArrayValue[1];
            vfMunitionStartPoint[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("firedTime");
            field = node.getFieldValue(index);
            vfFiredTime = field.doubleValue;

            index = node.getFieldIndex("firingRange");
            field = node.getFieldValue(index);
            vfFiringRange = field.floatValue;

            index = node.getFieldIndex("fired1");
            field = node.getFieldValue(index);
            vfFired1 = field.booleanValue;

            index = node.getFieldIndex("fired2");
            field = node.getFieldValue(index);
            vfFired2 = field.booleanValue;

            index = node.getFieldIndex("isDetonated");
            field = node.getFieldValue(index);
            vfIsDetonated = field.booleanValue;

            index = node.getFieldIndex("detonateTime");
            field = node.getFieldValue(index);
            vfDetonateTime = field.doubleValue;

            index = node.getFieldIndex("readInterval");
            field = node.getFieldValue(index);
            vfReadInterval = field.doubleValue;

            index = node.getFieldIndex("writeInterval");
            field = node.getFieldValue(index);
            vfWriteInterval = field.doubleValue;

            index = node.getFieldIndex("networkMode");
            field = node.getFieldValue(index);
            vfNetworkMode = field.stringValue;

            Integer nm = (Integer) networkModes.get(vfNetworkMode);
            if (nm == null) {
                throw new InvalidFieldValueException("Invalid networkMode: " + vfNetworkMode);
            }

            currentMode = nm.intValue();

            index = node.getFieldIndex("address");
            field = node.getFieldValue(index);
            vfAddress = field.stringValue;

            index = node.getFieldIndex("port");
            field = node.getFieldValue(index);
            vfPort = field.intValue;

            index = node.getFieldIndex("detonationResult");
            field = node.getFieldValue(index);
            vfDetonationResult = field.intValue;

            index = node.getFieldIndex("detonationLocation");
            field = node.getFieldValue(index);

            vfDetonationLocation[0] = field.floatArrayValue[0];
            vfDetonationLocation[1] = field.floatArrayValue[1];
            vfDetonationLocation[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("detonationRelativeLocation");
            field = node.getFieldValue(index);

            vfDetonationRelativeLocation[0] = field.floatArrayValue[0];
            vfDetonationRelativeLocation[1] = field.floatArrayValue[1];
            vfDetonationRelativeLocation[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("articulationParameterCount");
            field = node.getFieldValue(index);
            vfArticulationParameterCount = field.intValue;

            index = node.getFieldIndex("articulationParameterArray");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfArticulationParameterArray = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfArticulationParameterArray,
                                 0,
                                 field.numElements);
                numArticulationParameterArray = field.numElements;
            }

            index = node.getFieldIndex("xmppParams");
            field = node.getFieldValue(index);

            if (field.numElements != 0) {
                vfXMPPParams = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfXMPPParams, 0,
                    field.numElements);

                setXMPPParams(vfXMPPParams, field.numElements);
            }

            index = node.getFieldIndex("linearVelocity");
            field = node.getFieldValue(index);

            vfLinearVelocity[0] = field.floatArrayValue[0];
            vfLinearVelocity[1] = field.floatArrayValue[1];
            vfLinearVelocity[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("linearAcceleration");
            field = node.getFieldValue(index);

            vfLinearAcceleration[0] = field.floatArrayValue[0];
            vfLinearAcceleration[1] = field.floatArrayValue[1];
            vfLinearAcceleration[2] = field.floatArrayValue[2];

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        updateMatrix();
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.NetworkInterfaceNodeType;
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

        if(pGeoOrigin != null)
            pGeoOrigin.setupFinished();
        else if(vfGeoOrigin != null)
            vfGeoOrigin.setupFinished();

        if (vfGeoOrigin != null) {
            // Fetch the geo transform and shift the first set of points
            try {
                GTTransformUtils gtu = GTTransformUtils.getInstance();

                boolean[] swap = new boolean[1];

                geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
                geoCoordSwap = swap[0];

                origin = vfGeoOrigin.getConvertedCoordRef();

                // Only do this if we keep geoSystem.  Do we need one?
                // Ie will the on-wire value ever not be the speced DIS value?
    //            geoTransform.transform(vfPoint, 0, localCoords, 0, numPoint / 3);

            } catch(FactoryException fe) {
                errorReporter.errorReport(FACTORY_ERR_MSG, fe);
            }
        }

        regenID();
        inSetup = false;
    }

    //----------------------------------------------------------------
    // Methods defined by VRMLNetworkInterfaceNodeType
    //----------------------------------------------------------------

    /**
     * Get the protocol this node supports.
     *
     * @return The protocol.
     */
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Get the role of this node.
     *
     * @param The role, ROLE_*.
     */
    public int getRole() {
        return currentMode;
    }

    /**
     * Add a NetworkRoleListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    public void addNetworkRoleListener(NetworkRoleListener l) {
        if(!roleListeners.contains(l))
            roleListeners.add(l);
    }

    /**
     * Remove a NetworkRoleListener.
     *
     * @param l The listener
     */
    public void removeNetworkRoleListener(NetworkRoleListener l) {
        roleListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTimeDependentNodeType
    //----------------------------------------------------------

    /**
     * Set the clock that this time dependent node will be running with.
     * The clock provides all the information and listeners for keeping track
     * of time. Setting a value of null will ask the node to remove the clock
     * from it's use so that the node may be removed from the scene.
     *
     * @param clock The clock to use for this node
     */
    public void setVRMLClock(VRMLClock clock) {
        vrmlClock = clock;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTransform
    //----------------------------------------------------------


    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float[] rot)
        throws InvalidFieldValueException {

//System.out.println("SetRotation called");
        if(rot == null)
            throw new InvalidFieldValueException("Rotation value null");

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);

            needToWrite = true;
        }
    }

    /**
     * Get the current rotation component of the transform.
     *
     * @return The current rotation
     */
    public float[] getRotation() {
        return vfRotation;
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];
        vfTranslation[2] = tx[2];

        if (currentMode == MODE_READER && vfGeoOrigin != null) {
            vfTranslation[0] -= origin[0];
            vfTranslation[1] -= origin[1];
            vfTranslation[2] -= origin[2];
        }

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);

            needToWrite = true;
        }
    }

    /**
     * Get the current translation component of the transform.
     *
     * @return The current translation
     */
    public float[] getTranslation() {
        return vfTranslation;
    }

    /**
     * Set the scale component of the of transform. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    public void setScale(float[] scale)
        throws InvalidFieldValueException {

        if(scale == null)
            throw new InvalidFieldValueException("Scale value null");

        vfScale[0] = scale[0];
        vfScale[1] = scale[1];
        vfScale[2] = scale[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_SCALE] = true;
            fireFieldChanged(FIELD_SCALE);
        }
    }

    /**
     * Get the current scale component of the transform.
     *
     * @return The current scale
     */
    public float[] getScale() {
        return vfScale;
    }

    /**
     * Set the scale orientation component of the of transform. Setting a value
     * of null is an error
     *
     * @param so The new scale orientation component
     * @throws InvalidFieldValueException The scale orientation was null
     */
    public void setScaleOrientation(float[] so)
        throws InvalidFieldValueException {
        if(so == null)
            throw new InvalidFieldValueException("Scale Orientation value null");

        vfScaleOrientation[0] = so[0];
        vfScaleOrientation[1] = so[1];
        vfScaleOrientation[2] = so[2];
        vfScaleOrientation[3] = so[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_SCALE_ORIENTATION] = true;
            fireFieldChanged(FIELD_SCALE_ORIENTATION);
        }
    }

    /**
     * Get the current scale orientation component of the transform.
     *
     * @return The current scale orientation
     */
    public float[] getScaleOrientation() {
        return vfScaleOrientation;
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    public void setCenter(float[] center)
        throws InvalidFieldValueException {

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_CENTER] = true;
            fireFieldChanged(FIELD_CENTER);
        }
    }

    /**
     * Get the current center component of the transform.
     *
     * @return The current center
     */
    public float[] getCenter() {
        return vfCenter;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType interface.
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

        return (index == null) ? FIELD_NOT_IMPL : index.intValue();
    }

    /**
     * Get the secondary types of this node.  Replaces the instanceof mechanism
     * for use in switch statements. If there are no secondary types, it will
     * return a zero-length array.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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

        if (index == FIELD_NOT_IMPL)
            return notimpl_decl;

        if(index < 0  || index > LAST_TRANSFORM_INDEX)
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
            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if (pGeoOrigin != null)
                    fieldData.nodeValue = pGeoOrigin;
                else
                    fieldData.nodeValue = vfGeoOrigin;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_CENTER:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfCenter;
                break;

            case FIELD_ROTATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfRotation;
                break;

            case FIELD_SCALE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfScale;
                break;

            case FIELD_SCALE_ORIENTATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfScaleOrientation;
                break;

            case FIELD_TRANSLATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfTranslation;
                break;

            case FIELD_DETONATION_LOCATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfDetonationLocation;
                break;

            case FIELD_DETONATION_RELATIVE_LOCATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfDetonationRelativeLocation;
                break;

            case FIELD_MARKING:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfMarking;
                break;

            case FIELD_SITE_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfSiteID;
                break;

            case FIELD_ENTITY_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityID;
                break;

            case FIELD_FORCE_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfForceID;
                break;

            case FIELD_ENTITY_CATEGORY:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityCategory;
                break;

            case FIELD_APPEARANCE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfAppearance;
                break;

            case FIELD_ENTITY_COUNTRY:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityCountry;
                break;

            case FIELD_ENTITY_DOMAIN:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityDomain;
                break;

            case FIELD_ENTITY_EXTRA:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityExtra;
                break;

            case FIELD_ENTITY_KIND:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityKind;
                break;

            case FIELD_ENTITY_SPECIFIC:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntitySpecific;
                break;

            case FIELD_ENTITY_SUBCATEGORY:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntitySubCategory;
                break;

            case FIELD_READ_INTERVAL:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.doubleValue = vfReadInterval;
                break;

            case FIELD_WRITE_INTERVAL:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.doubleValue = vfWriteInterval;
                break;

            case FIELD_NETWORK_MODE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfNetworkMode;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValue = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
                break;

            case FIELD_APPLICATION_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfApplicationID;
                break;

            case FIELD_PORT:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfPort;
                break;

            case FIELD_DETONATION_RESULT:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfDetonationResult;
                break;

            case FIELD_ADDRESS:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfAddress;
                break;

            case FIELD_EVENT_APPLICATION_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEventApplicationID;
                break;

            case FIELD_EVENT_ENTITY_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEventEntityID;
                break;

            case FIELD_EVENT_SITE_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEventSiteID;
                break;

            case FIELD_FIRE_MISSION_INDEX:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfFireMissionIndex;
                break;

            case FIELD_MUNITION_APPLICATION_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfMunitionApplicationID;
                break;

            case FIELD_MUNITION_ENTITY_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfMunitionEntityID;
                break;

            case FIELD_MUNITION_SITE_ID:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfMunitionSiteID;
                break;

            case FIELD_MUNITION_END_POINT:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfMunitionEndPoint;
                break;

            case FIELD_MUNITION_START_POINT:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfMunitionStartPoint;
                break;

            case FIELD_FIRED_TIME:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.doubleValue = vfFiredTime;
                break;

            case FIELD_FIRED_1:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfFired1;
                break;

            case FIELD_FIRED_2:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfFired2;
                break;

            case FIELD_FIRING_RANGE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.floatValue = vfFiringRange;
                break;

            case FIELD_FIRING_RATE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfFiringRate;
                break;

            case FIELD_IS_DETONATED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfIsDetonated;
                break;

            case FIELD_DETONATE_TIME:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.doubleValue = vfDetonateTime;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfIsActive;
                break;

            case FIELD_TIMESTAMP:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                fieldData.doubleValue = vfTimestamp;
                break;

            case FIELD_ARTICULATION_PARAMETER_COUNT:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfArticulationParameterCount;
                break;

            case FIELD_ARTICULATION_PARAMETER_ARRAY:
                fieldData.floatArrayValue = vfArticulationParameterArray;
                fieldData.numElements = numArticulationParameterArray;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue0;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue1;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue2;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue3;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue4;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue5;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue6;
                break;

            case FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatValue = vfArticulationParameterValue7;
                break;

            case FIELD_XMPP_PARAMS:
                fieldData.clear();
                fieldData.stringArrayValue = vfXMPPParams;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfXMPPParams.length;
                break;

            case FIELD_LINEAR_VELOCITY:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfLinearVelocity;
                break;

            case FIELD_LINEAR_ACCELERATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfLinearAcceleration;
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
                case FIELD_CENTER :
                    destNode.setValue(destIndex, vfCenter, 3);
                    break;
                case FIELD_ROTATION :
                    destNode.setValue(destIndex, vfRotation, 4);
                    break;
                case FIELD_SCALE :
                    destNode.setValue(destIndex, vfScale, 3);
                    break;
                case FIELD_SCALE_ORIENTATION :
                    destNode.setValue(destIndex, vfScaleOrientation, 4);
                    break;
                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 3);
                    break;
                case FIELD_GEO_SYSTEM:
                    destNode.setValue(destIndex, vfGeoSystem, vfGeoSystem.length);
                    break;
                case FIELD_EVENT_APPLICATION_ID:
                    destNode.setValue(destIndex, vfEventApplicationID);
                    break;
                case FIELD_EVENT_ENTITY_ID:
                    destNode.setValue(destIndex, vfEventEntityID);
                    break;
                case FIELD_ENTITY_CATEGORY:
                    destNode.setValue(destIndex, vfEntityCategory);
                    break;
                case FIELD_ENTITY_DOMAIN:
                    destNode.setValue(destIndex, vfEntityDomain);
                    break;
                case FIELD_APPEARANCE:
                    destNode.setValue(destIndex, vfAppearance);
                    break;
                case FIELD_ENTITY_COUNTRY:
                    destNode.setValue(destIndex, vfEntityCountry);
                    break;
                case FIELD_ENTITY_SUBCATEGORY:
                    destNode.setValue(destIndex, vfEntitySubCategory);
                    break;
                case FIELD_ENTITY_SPECIFIC:
                    destNode.setValue(destIndex, vfEntitySpecific);
                    break;
                case FIELD_ENTITY_KIND:
                    destNode.setValue(destIndex, vfEntityKind);
                    break;
                case FIELD_ENTITY_EXTRA:
                    destNode.setValue(destIndex, vfEntityExtra);
                    break;
                case FIELD_EVENT_SITE_ID:
                    destNode.setValue(destIndex, vfEventSiteID);
                    break;
                case FIELD_FIRE_MISSION_INDEX:
                    destNode.setValue(destIndex, vfFireMissionIndex);
                    break;
                case FIELD_FIRING_RATE:
                    destNode.setValue(destIndex, vfFiringRate);
                    break;
                case FIELD_MUNITION_APPLICATION_ID:
                    destNode.setValue(destIndex, vfMunitionApplicationID);
                    break;
                case FIELD_MUNITION_SITE_ID:
                    destNode.setValue(destIndex, vfMunitionSiteID);
                    break;
                case FIELD_MUNITION_ENTITY_ID:
                    destNode.setValue(destIndex, vfMunitionEntityID);
                    break;
                case FIELD_MUNITION_START_POINT:
                    destNode.setValue(destIndex, vfMunitionStartPoint, 3);
                    break;
                case FIELD_MUNITION_END_POINT:
                    destNode.setValue(destIndex, vfMunitionEndPoint, 3);
                    break;
                case FIELD_FIRED_TIME:
                    destNode.setValue(destIndex, vfFiredTime);
                    break;
                case FIELD_FIRED_1:
                    destNode.setValue(destIndex, vfFired1);
                    break;
                case FIELD_FIRED_2:
                    destNode.setValue(destIndex, vfFired2);
                    break;
                case FIELD_FIRING_RANGE:
                    destNode.setValue(destIndex, vfFiringRange);
                    break;
                case FIELD_DETONATION_LOCATION:
                    destNode.setValue(destIndex, vfDetonationLocation, 3);
                    break;
                case FIELD_DETONATION_RELATIVE_LOCATION:
                    destNode.setValue(destIndex, vfDetonationRelativeLocation, 3);
                    break;
                case FIELD_MARKING:
                    destNode.setValue(destIndex, vfMarking);
                    break;
                case FIELD_SITE_ID:
                    destNode.setValue(destIndex, vfSiteID);
                    break;
                case FIELD_IS_DETONATED:
                    destNode.setValue(destIndex, vfIsDetonated);
                    break;
                case FIELD_DETONATE_TIME:
                    destNode.setValue(destIndex, vfDetonateTime);
                    break;
                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;
                case FIELD_TIMESTAMP:
                    destNode.setValue(destIndex, vfTimestamp);
                    break;
                case FIELD_ENTITY_ID:
                    destNode.setValue(destIndex, vfEntityID);
                    break;
                case FIELD_FORCE_ID:
                    destNode.setValue(destIndex, vfForceID);
                    break;
                case FIELD_READ_INTERVAL:
                    destNode.setValue(destIndex, vfReadInterval);
                    break;
                case FIELD_WRITE_INTERVAL:
                    destNode.setValue(destIndex, vfWriteInterval);
                    break;
                case FIELD_NETWORK_MODE:
                    destNode.setValue(destIndex, vfNetworkMode);
                    break;
                case FIELD_PORT:
                    destNode.setValue(destIndex, vfPort);
                    break;
                case FIELD_DETONATION_RESULT:
                    destNode.setValue(destIndex, vfDetonationResult);
                    break;
                case FIELD_ADDRESS:
                    destNode.setValue(destIndex, vfAddress);
                    break;
                case FIELD_ARTICULATION_PARAMETER_COUNT:
                    destNode.setValue(destIndex, vfArticulationParameterCount);
                    break;
                case FIELD_ARTICULATION_PARAMETER_ARRAY:
                    destNode.setValue(destIndex,
                                      vfArticulationParameterArray,
                                      numArticulationParameterArray);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue0);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue1);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue2);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue3);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue4);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue5);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue6);
                    break;
                case FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED:
                    destNode.setValue(destIndex, vfArticulationParameterValue7);
                    break;
                case FIELD_LINEAR_VELOCITY :
                    destNode.setValue(destIndex, vfLinearVelocity, 3);
                    break;
                case FIELD_LINEAR_ACCELERATION :
                    destNode.setValue(destIndex, vfLinearAcceleration, 3);
                    break;

                default: super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                "geoSystem");

                if(vfGeoSystem.length != numValid)
                    vfGeoSystem = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfGeoSystem[i] = value[i];
                break;
            case FIELD_XMPP_PARAMS:
                if(vfXMPPParams.length != numValid)
                    vfXMPPParams = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfXMPPParams[i] = value[i];

                setXMPPParams(value, numValid);
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
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_GEO_ORIGIN:
                setGeoOrigin(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as an integer.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FIRED_1:
                vfFired1 = value;

                if(!inSetup) {
                    hasChanged[FIELD_FIRED_1] = true;
                    fireFieldChanged(FIELD_FIRED_1);
                }
                break;
            case FIELD_FIRED_2:
                vfFired2 = value;

                if(!inSetup) {
                    hasChanged[FIELD_FIRED_2] = true;
                    fireFieldChanged(FIELD_FIRED_2);
                }
                break;
            case FIELD_IS_DETONATED:
                vfIsDetonated = value;

                if(!inSetup) {
                    hasChanged[FIELD_IS_DETONATED] = true;
                    fireFieldChanged(FIELD_IS_DETONATED);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an integer.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SITE_ID:
                vfSiteID = value;

                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_SITE_ID] = true;
                    fireFieldChanged(FIELD_SITE_ID);
                }
                break;
            case FIELD_APPLICATION_ID:
                vfApplicationID = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_APPLICATION_ID] = true;
                    fireFieldChanged(FIELD_APPLICATION_ID);
                }
                break;
            case FIELD_ENTITY_ID:
                vfEntityID = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_ID] = true;
                    fireFieldChanged(FIELD_ENTITY_ID);
                }
                break;
            case FIELD_FORCE_ID:
                vfForceID = value;
                if(!inSetup) {
                    hasChanged[FIELD_FORCE_ID] = true;
                    fireFieldChanged(FIELD_FORCE_ID);
                }
                break;
            case FIELD_ENTITY_CATEGORY:
                vfEntityCategory = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_CATEGORY] = true;
                    fireFieldChanged(FIELD_ENTITY_CATEGORY);
                }
                break;
            case FIELD_APPEARANCE:
                vfAppearance = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_APPEARANCE] = true;
                    fireFieldChanged(FIELD_APPEARANCE);
                }
                break;
            case FIELD_ENTITY_COUNTRY:
                vfEntityCountry = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_COUNTRY] = true;
                    fireFieldChanged(FIELD_ENTITY_COUNTRY);
                }
                break;
            case FIELD_ENTITY_SUBCATEGORY:
                vfEntitySubCategory = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_SUBCATEGORY] = true;
                    fireFieldChanged(FIELD_ENTITY_SUBCATEGORY);
                }
                break;
            case FIELD_ENTITY_DOMAIN:
                vfEntityDomain = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_DOMAIN] = true;
                    fireFieldChanged(FIELD_ENTITY_DOMAIN);
                }
                break;
            case FIELD_ENTITY_KIND:
                vfEntityKind = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_KIND] = true;
                    fireFieldChanged(FIELD_ENTITY_KIND);
                }
                break;
            case FIELD_ENTITY_EXTRA:
                vfEntityExtra = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_EXTRA] = true;
                    fireFieldChanged(FIELD_ENTITY_EXTRA);
                }
                break;
            case FIELD_ENTITY_SPECIFIC:
                vfEntitySpecific = value;
                if(!inSetup) {
                    regenID();
                    hasChanged[FIELD_ENTITY_SPECIFIC] = true;
                    fireFieldChanged(FIELD_ENTITY_SPECIFIC);
                }
                break;

            case FIELD_PORT:
                vfPort = value;
                if(!inSetup) {
                    hasChanged[FIELD_PORT] = true;
                    fireFieldChanged(FIELD_PORT);
                }
                break;
            case FIELD_EVENT_APPLICATION_ID:
                vfEventApplicationID = value;
                if(!inSetup) {
                    hasChanged[FIELD_EVENT_APPLICATION_ID] = true;
                    fireFieldChanged(FIELD_EVENT_APPLICATION_ID);
                }
                break;
            case FIELD_EVENT_ENTITY_ID:
                vfEventEntityID = value;
                if(!inSetup) {
                    hasChanged[FIELD_EVENT_ENTITY_ID] = true;
                    fireFieldChanged(FIELD_EVENT_ENTITY_ID);
                }
                break;
            case FIELD_EVENT_SITE_ID:
                vfEventSiteID = value;
                if(!inSetup) {
                    hasChanged[FIELD_EVENT_SITE_ID] = true;
                    fireFieldChanged(FIELD_EVENT_SITE_ID);
                }
                break;
            case FIELD_FIRE_MISSION_INDEX:
                vfFireMissionIndex = value;
                if(!inSetup) {
                    hasChanged[FIELD_FIRE_MISSION_INDEX] = true;
                    fireFieldChanged(FIELD_FIRE_MISSION_INDEX);
                }
                break;
            case FIELD_FIRING_RATE:
                vfFiringRate = value;
                if(!inSetup) {
                    hasChanged[FIELD_FIRING_RATE] = true;
                    fireFieldChanged(FIELD_FIRING_RATE);
                }
                break;
            case FIELD_MUNITION_APPLICATION_ID:
                vfMunitionApplicationID = value;
                if(!inSetup) {
                    hasChanged[FIELD_MUNITION_APPLICATION_ID] = true;
                    fireFieldChanged(FIELD_MUNITION_APPLICATION_ID);
                }
                break;
            case FIELD_MUNITION_ENTITY_ID:
                vfMunitionEntityID = value;
                if(!inSetup) {
                    hasChanged[FIELD_MUNITION_ENTITY_ID] = true;
                    fireFieldChanged(FIELD_MUNITION_ENTITY_ID);
                }
                break;
            case FIELD_MUNITION_SITE_ID:
                vfMunitionSiteID = value;
                if(!inSetup) {
                    hasChanged[FIELD_MUNITION_SITE_ID] = true;
                    fireFieldChanged(FIELD_MUNITION_SITE_ID);
                }
                break;
            case FIELD_DETONATION_RESULT:
                vfDetonationResult = value;
                if(!inSetup) {
                    hasChanged[FIELD_DETONATION_RESULT] = true;
                    fireFieldChanged(FIELD_DETONATION_RESULT);
                }
                break;
            case FIELD_ARTICULATION_PARAMETER_COUNT:
                // TODO: Need to fix this for DISXML code
                vfArticulationParameterCount = value;
                if (artVals.length < value) {
                    artVals = new ArticulationParameter[value];
                    vfArticulationParameterArray = new float[value];
                }

                for(int i=0; i < value; i++) {
                    artVals[i] = new ArticulationParameter();
                }
                numArticulationParameterArray = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_ARTICULATION_PARAMETER_COUNT] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_COUNT);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
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
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                setCenter(value);
                break;

            case FIELD_ROTATION:
                setRotation(value);
                break;

            case FIELD_SCALE:
                setScale(value);
                break;

            case FIELD_SCALE_ORIENTATION:
                setScaleOrientation(value);
                break;

            case FIELD_TRANSLATION:
                setTranslation(value);
                break;

            case FIELD_MUNITION_START_POINT:
                vfMunitionStartPoint[0] = value[0];
                vfMunitionStartPoint[1] = value[1];
                vfMunitionStartPoint[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_MUNITION_START_POINT] = true;
                    fireFieldChanged(FIELD_MUNITION_START_POINT);

                    needToWrite = true;
                }
                break;

            case FIELD_MUNITION_END_POINT:
                vfMunitionEndPoint[0] = value[0];
                vfMunitionEndPoint[1] = value[1];
                vfMunitionEndPoint[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_MUNITION_END_POINT] = true;
                    fireFieldChanged(FIELD_MUNITION_END_POINT);

                    needToWrite = true;
                }
                break;

            case FIELD_DETONATION_RELATIVE_LOCATION:
                vfDetonationRelativeLocation[0] = value[0];
                vfDetonationRelativeLocation[1] = value[1];
                vfDetonationRelativeLocation[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_DETONATION_RELATIVE_LOCATION] = true;
                    fireFieldChanged(FIELD_DETONATION_RELATIVE_LOCATION);

                    needToWrite = true;
                }
                break;

            case FIELD_DETONATION_LOCATION:
                vfDetonationLocation[0] = value[0];
                vfDetonationLocation[1] = value[1];
                vfDetonationLocation[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_DETONATION_LOCATION] = true;
                    fireFieldChanged(FIELD_DETONATION_LOCATION);

                    needToWrite = true;
                }
                break;

            case FIELD_ARTICULATION_PARAMETER_ARRAY:
                if(numValid > vfArticulationParameterArray.length)
                    vfArticulationParameterArray = new float[numValid];

                System.arraycopy(vfArticulationParameterArray,
                                 0,
                                 value,
                                 0,
                                 numValid);
                numArticulationParameterArray = numValid;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_ARTICULATION_PARAMETER_ARRAY] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_ARRAY);
                }
                break;
            case FIELD_LINEAR_VELOCITY:
                vfLinearVelocity[0] = value[0];
                vfLinearVelocity[1] = value[1];
                vfLinearVelocity[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_LINEAR_VELOCITY] = true;
                    fireFieldChanged(FIELD_LINEAR_VELOCITY);

                    needToWrite = true;
                }
                break;
            case FIELD_LINEAR_ACCELERATION:
                vfLinearAcceleration[0] = value[0];
                vfLinearAcceleration[1] = value[1];
                vfLinearAcceleration[2] = value[2];

                // Save recalcs during the setup phase
                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_LINEAR_ACCELERATION] = true;
                    fireFieldChanged(FIELD_LINEAR_ACCELERATION);

                    needToWrite = true;
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FIRING_RANGE:
                vfFiringRange = value;
                if (!inSetup) {
                  needToWrite = true;
                  hasChanged[FIELD_FIRING_RANGE] = true;
                  fireFieldChanged(FIELD_FIRING_RANGE);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE0:
                resizeAPA(1);
                vfArticulationParameterArray[0] = value;
                vfArticulationParameterValue0 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE1:
                resizeAPA(2);
                vfArticulationParameterArray[1] = value;
                vfArticulationParameterValue1 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE1] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE1);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE2:
                resizeAPA(3);
                vfArticulationParameterArray[2] = value;
                vfArticulationParameterValue2 = value;
                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE2] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE2);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE3:
                resizeAPA(4);

                vfArticulationParameterArray[3] = value;
                vfArticulationParameterValue3 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE3] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE3);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE4:
                resizeAPA(5);
                vfArticulationParameterArray[4] = value;
                vfArticulationParameterValue4 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE4] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE4);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE5:
                resizeAPA(6);
                vfArticulationParameterArray[5] = value;
                vfArticulationParameterValue5 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE5] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE5);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE6:
                resizeAPA(7);
                vfArticulationParameterArray[6] = value;
                vfArticulationParameterValue6 = value;

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE6] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE6);
                }
                break;
            case FIELD_SET_ARTICULATION_PARAMETER_VALUE7:
                resizeAPA(8);

                if(!inSetup) {
                    needToWrite = true;
                    hasChanged[FIELD_SET_ARTICULATION_PARAMETER_VALUE7] = true;
                    fireFieldChanged(FIELD_SET_ARTICULATION_PARAMETER_VALUE7);
                }
                break;
            default:
                super.setValue(index, value);
        }

    }

    /**
     * Set the value of the field at the given index as a time.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FIRED_TIME:
                vfFiredTime = value;
                if(!inSetup) {
                    hasChanged[FIELD_FIRED_TIME] = true;
                    fireFieldChanged(FIELD_FIRED_TIME);
                }
                break;
            case FIELD_DETONATE_TIME:
                vfDetonateTime = value;
                if(!inSetup) {
                    hasChanged[FIELD_DETONATE_TIME] = true;
                    fireFieldChanged(FIELD_DETONATE_TIME);
                }
                break;
            case FIELD_READ_INTERVAL:
                vfReadInterval = value;
                if(!inSetup) {
                    hasChanged[FIELD_READ_INTERVAL] = true;
                    fireFieldChanged(FIELD_READ_INTERVAL);
                }
                break;
            case FIELD_WRITE_INTERVAL:
                vfWriteInterval = value;

                if(!inSetup) {
                    hasChanged[FIELD_WRITE_INTERVAL] = true;
                    fireFieldChanged(FIELD_WRITE_INTERVAL);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MARKING:
                vfMarking = value;
                if(!inSetup) {
                    hasChanged[FIELD_MARKING] = true;
                    fireFieldChanged(FIELD_MARKING);
                }
                break;

            case FIELD_NETWORK_MODE:
                if (vfNetworkMode.equals(value))
                    return;

                vfNetworkMode = value;
                Integer nm = (Integer) networkModes.get(vfNetworkMode);
                if (nm == null) {
                    throw new InvalidFieldValueException("Invalid networkMode: " + value);
                }

                currentMode = nm.intValue();

                if(!inSetup) {
                    hasChanged[FIELD_NETWORK_MODE] = true;
                    fireFieldChanged(FIELD_NETWORK_MODE);

                    fireRoleChanged();
                }
                break;
            case FIELD_ADDRESS:
                vfAddress = value;
                if(!inSetup) {
                    hasChanged[FIELD_ADDRESS] = true;
                    fireFieldChanged(FIELD_ADDRESS);
                }
                break;

            case FIELD_NOT_IMPL:
                // ignore
                break;
            default:
                super.setValue(index, value);
        }

    }

    //----------------------------------------------------------
    // Methods for the VRMLDISNodeType
    //----------------------------------------------------------
    /**
     * Get the siteID specified for this node.
     *
     * @return the siteID.
     */
    public int getSiteID() {
        return vfSiteID;
    }

    /**
     * Get the applicationID specified for this node.
     *
     * @return the applicationID.
     */
    public int getAppID() {
        return vfApplicationID;
    }


    /**
     * Get the entityID specified for this node.
     *
     * @return the entityID.
     */
    public int getEntityID() {
        return vfEntityID;
    }


    /**
     * Get the network address to listen to.
     *
     * @param The address.
     */
    public String getAddress() {
        return vfAddress;
    }

    /**
     * Get the network port to listen to.
     *
     * @param The port.
     */
    public int getPort() {
        return vfPort;
    }

    /**
     * Get the chat room username.
     *
     * @return The username.  null if none provided
     */
    public String getUsername() {
        return xmppUsername;
    }

    /**
     * Get the chat room password.
     *
     * @return The password.  null if none provided
     */
    public String getPassword() {
        return xmppPassword;
    }

    /**
     * Get the chat room auth server.
     *
     * @return The auth server.  null if none provided
     */
    public String[] getAuthServer() {
        return xmppAuthServer;
    }

    /**
     * Get the chat room mucServer.
     *
     * @return The mucServer.  null if none provided
     */
    public String getMucServer() {
        return xmppMucServer;
    }

    /**
     * Get the chat room mucRoom.
     *
     * @return The mucRoom.  null if none provided
     */
    public String getMucRoom() {
        return xmppMucRoom;
    }

    /**
     * Set the isActive state for a DIS node.
     *
     * @param isActive Whether the node is active(traffic within 5 seconds).
     */
    public void setIsActive(boolean active) {
        vfIsActive = active;
        if (!inSetup) {
            hasChanged[FIELD_IS_ACTIVE] = true;
            fireFieldChanged(FIELD_IS_ACTIVE);
        }
    }

    /**
     * Does the this node have new information to write.  This only
     * accounts for local values, not required DIS heartbeart rules.
     *
     * @return TRUE if values have changed.
     */
    public boolean valuesToWrite() {
        if (!needToWrite || currentMode == MODE_READER)
            return false;

        double curr = vrmlClock.getTime();
        if (curr - lastWrite> vfWriteInterval) {
            return true;
        } else
            return false;
    }

    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnitType getStateDX() {
        //espduDX.reset();

        if (espduDX == null)
            return null;

        espduDX.setEntityID(idDX);

        for(int i=0; i < vfArticulationParameterCount; i++) {
            artVals[i].setParameterValue(vfArticulationParameterArray[i]);
            espdu.addArticulationParameter(artVals[i]);
        }

        v3d.setX(vfTranslation[0]);
        v3d.setY(vfTranslation[2]);
        v3d.setZ(-vfTranslation[1]);
        espduDX.setEntityLocation(v3d);

        aa.x = vfRotation[0];
        aa.y = vfRotation[1];
        aa.z = vfRotation[2];
        aa.angle = vfRotation[3];

        rotationQuat.set(aa);
        rotationQuat.normalize();

        set(rotationQuat);

        eat.setPhi((float) roll);
        eat.setTheta((float) pitch);
        eat.setPsi((float) -yaw);
        espduDX.setEntityOrientation(eat);

        scrLinearVelo.setX(vfLinearVelocity[0]);
        scrLinearVelo.setY(vfLinearVelocity[2]);
        scrLinearVelo.setZ(-vfLinearVelocity[1]);
        espduDX.setEntityLinearVelocity(scrLinearVelo);

        DeadReckoningParametersType dr = espduDX.getDeadReckoningParameters();
        scrLinearAccel.setX(vfLinearAcceleration[0]);
        scrLinearAccel.setY(vfLinearAcceleration[2]);
        scrLinearAccel.setZ(-vfLinearAcceleration[1]);
        dr.setEntityLinearAcceleration(scrLinearAccel);

        //espduDX.makeTimestampCurrent();
        HeaderType header = espduDX.getPduHeader();
        long currTime = System.currentTimeMillis();

        header.setTimestamp((int)(currTime - simStartTime));

        lastWrite = vrmlClock.getTime();
        needToWrite = false;

        return espduDX;
    }

    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnit getState() {
        espdu.reset();

        espdu.setEntityID(id);

        for(int i=0; i < vfArticulationParameterCount; i++) {
            artVals[i].setParameterValue(vfArticulationParameterArray[i]);
            espdu.addArticulationParameter(artVals[i]);
        }

        // location
        espdu.setEntityLocationX(vfTranslation[0]);
        espdu.setEntityLocationY(vfTranslation[2]);
        espdu.setEntityLocationZ(-vfTranslation[1]);

        aa.x = vfRotation[0];
        aa.y = vfRotation[1];
        aa.z = vfRotation[2];
        aa.angle = vfRotation[3];

        rotationQuat.set(aa);
        rotationQuat.normalize();


//System.out.println("sent vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);
//System.out.println("Sending: " + rotationQuat.x + " " + rotationQuat.y + " " + rotationQuat.z + " " + rotationQuat.w);
/*
        // orientation
        espdu.setEntityOrientationPhi((float)rotationQuat.x);
        espdu.setEntityOrientationTheta((float)rotationQuat.y);
        espdu.setEntityOrientationPsi((float)rotationQuat.z);
*/

        set(rotationQuat);
//System.out.println("roll: " + roll + " pitch: " + pitch + " yaw: " + yaw);
        espdu.setEntityOrientationPhi((float)roll);
        espdu.setEntityOrientationTheta((float)pitch);
        espdu.setEntityOrientationPsi((float)-yaw);

/*
System.out.println("sent vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);
        // Exactly replicate original NPS DIS code
        quaternion.setAxisAngle(vfRotation);
        // TODO: This generates garbage
        quaternion.getEulers(dRorientation);
System.out.println("phi: " + -dRorientation[0] + " theta: " + dRorientation[2] + " Psi: " + dRorientation[1]);

        // orientation
        espdu.setEntityOrientationPhi(-dRorientation[0]);   // h
        espdu.setEntityOrientationTheta(dRorientation[2]); // p
        espdu.setEntityOrientationPsi(dRorientation[1]);   // r
*/

/*

        // TODO: Hack to get around quat to axis angle conversion problem.
        // orientation
        espdu.setEntityOrientationPsi(vfRotation[0]);   // h
        espdu.setEntityOrientationTheta(vfRotation[1]); // p
        espdu.setEntityOrientationPhi(vfRotation[2]);   // r
        espdu.setEntityOrientationAngle(vfRotation[3]);   // r
*/

/*
        // velocity
        espdu.setEntityLinearVelocityX(pduValues[3]);
        espdu.setEntityLinearVelocityY(pduValues[4]);
        espdu.setEntityLinearVelocityZ(pduValues[5]);


        // angular velocity
        espdu.setEntityAngularVelocityY(pduValues[9]); // h
        espdu.setEntityAngularVelocityX(pduValues[10]); // p
        espdu.setEntityAngularVelocityZ(pduValues[11]); // r
*/


        // velocity
        espdu.setEntityLinearVelocityX(vfLinearVelocity[0]);
        espdu.setEntityLinearVelocityY(vfLinearVelocity[2]);
        espdu.setEntityLinearVelocityZ(-vfLinearVelocity[1]);

        // acceleration
        espdu.setEntityLinearAccelerationX(vfLinearAcceleration[0]);
        espdu.setEntityLinearAccelerationY(vfLinearAcceleration[2]);
        espdu.setEntityLinearAccelerationZ(-vfLinearAcceleration[1]);

        espdu.makeTimestampCurrent();

        lastWrite = vrmlClock.getTime();
        needToWrite = false;

        return espdu;
    }

    /**
     * Tell the DIS node that a packet arrived.  Used to update tiemstamp information.
     *
     */
    public void packetArrived(ProtocolDataUnit pdu) {
/*    Restore when timeDependant stuff is straightend out
        vfTimestamp = vrmlClock.getTime();

        hasChanged[FIELD_TIMESTAMP] = true;
        fireFieldChanged(FIELD_TIMESTAMP);
*/
        int type = pdu.getPduTypeValue();
        switch(type) {
            case PduTypeField.FIREFI:
            case PduTypeField.FIRE:
                FirePdu firepdu = (FirePdu) pdu;

                vfFired1 = true;
                hasChanged[FIELD_FIRED_1] = true;
                fireFieldChanged(FIELD_FIRED_1);

                vfFired2 = false;
                hasChanged[FIELD_FIRED_2] = true;
                fireFieldChanged(FIELD_FIRED_2);

                vfFiredTime = vrmlClock.getTime();
                hasChanged[FIELD_FIRED_TIME] = true;
                fireFieldChanged(FIELD_FIRED_TIME);

                vfEventApplicationID = firepdu.getEventID().getApplicationID().intValue();
                hasChanged[FIELD_EVENT_APPLICATION_ID] = true;
                fireFieldChanged(FIELD_EVENT_APPLICATION_ID);

                vfEventEntityID = firepdu.getEventID().getEventID().intValue();
                hasChanged[FIELD_EVENT_ENTITY_ID] = true;
                fireFieldChanged(FIELD_EVENT_ENTITY_ID);

                vfEventSiteID = firepdu.getEventID().getSiteID().intValue();
                hasChanged[FIELD_EVENT_SITE_ID] = true;
                fireFieldChanged(FIELD_EVENT_SITE_ID);

                vfMunitionApplicationID = firepdu.getMunitionID().getApplicationID().intValue();
                hasChanged[FIELD_MUNITION_APPLICATION_ID] = true;
                fireFieldChanged(FIELD_MUNITION_APPLICATION_ID);

                vfMunitionEntityID = firepdu.getMunitionID().getEntityID().intValue();
                hasChanged[FIELD_MUNITION_ENTITY_ID] = true;
                fireFieldChanged(FIELD_MUNITION_ENTITY_ID);

                vfMunitionSiteID = firepdu.getMunitionID().getSiteID().intValue();
                hasChanged[FIELD_MUNITION_SITE_ID] = true;
                fireFieldChanged(FIELD_MUNITION_SITE_ID);

                vfFireMissionIndex = firepdu.getFireMissionIndex().intValue();
                hasChanged[FIELD_FIRE_MISSION_INDEX] = true;
                fireFieldChanged(FIELD_FIRE_MISSION_INDEX);

                vfFiringRate = firepdu.getBurstDescriptor().getRate().intValue();
                hasChanged[FIELD_FIRING_RATE] = true;
                fireFieldChanged(FIELD_FIRING_RATE);

                vfFiringRange = firepdu.getRange();
                hasChanged[FIELD_FIRING_RANGE] = true;
                fireFieldChanged(FIELD_FIRING_RANGE);

                vfMunitionStartPoint[0] = (float)firepdu.getLocationInWorldCoordinate().getX();
                vfMunitionStartPoint[1] = -(float)firepdu.getLocationInWorldCoordinate().getZ();
                vfMunitionStartPoint[2] = (float)firepdu.getLocationInWorldCoordinate().getY();
                hasChanged[FIELD_MUNITION_START_POINT] = true;
                fireFieldChanged(FIELD_MUNITION_START_POINT);

                calculateMunitionEndPoint(firepdu.getVelocity());
                hasChanged[FIELD_MUNITION_END_POINT] = true;
                fireFieldChanged(FIELD_MUNITION_END_POINT);
                break;

            case PduTypeField.DETONATIONFI:
            case PduTypeField.DETONATION:
                DetonationPdu dtpdu = (DetonationPdu) pdu;

                vfIsDetonated = true;
                hasChanged[FIELD_IS_DETONATED] = true;
                fireFieldChanged(FIELD_IS_DETONATED);

                vfDetonateTime = vrmlClock.getTime();
                hasChanged[FIELD_DETONATE_TIME] = true;
                fireFieldChanged(FIELD_DETONATE_TIME);

                vfDetonationResult = dtpdu.getDetonationResult().intValue();
                hasChanged[FIELD_DETONATION_RESULT] = true;
                fireFieldChanged(FIELD_DETONATION_RESULT);

                vfDetonationRelativeLocation[0] = (float)dtpdu.getLocationInEntityCoordinates().getX();
                vfDetonationRelativeLocation[1] = -(float)dtpdu.getLocationInEntityCoordinates().getZ();
                vfDetonationRelativeLocation[2] = (float)dtpdu.getLocationInEntityCoordinates().getY();
                hasChanged[FIELD_DETONATION_RELATIVE_LOCATION] = true;
                fireFieldChanged(FIELD_DETONATION_RELATIVE_LOCATION);

                vfDetonationLocation[0] = (float)dtpdu.getLocationInWorldCoordinate().getX();
                vfDetonationLocation[1] = -(float)dtpdu.getLocationInWorldCoordinate().getZ();
                vfDetonationLocation[2] = (float)dtpdu.getLocationInWorldCoordinate().getY();
                hasChanged[FIELD_DETONATION_LOCATION] = true;
                fireFieldChanged(FIELD_DETONATION_LOCATION);
                break;
            case PduTypeField.ENTITYSTATE:
                EntityStatePdu espdu = (EntityStatePdu) pdu;

                if (!ignoreEspdu) {
//System.out.println("Time: " + System.currentTimeMillis());
//System.out.println("pos: " + espdu.getEntityLocationX() + " " + (-espdu.getEntityLocationZ()) + " " + espdu.getEntityLocationY());
//System.out.println("vel: " + espdu.getEntityLinearVelocityX() + " " + (-espdu.getEntityLinearVelocityZ()) + " " + espdu.getEntityLinearVelocityY());
//System.out.println("acc: " + espdu.getEntityLinearAccelerationX() + " " + espdu.getEntityLinearAccelerationY() + " " + espdu.getEntityLinearAccelerationZ());

                    vfTranslation[0] = (float)espdu.getEntityLocationX();
                    vfTranslation[1] = -(float)espdu.getEntityLocationZ();
                    vfTranslation[2] = (float)espdu.getEntityLocationY();

                    if (currentMode == MODE_READER && vfGeoOrigin != null) {
                        vfTranslation[0] -= origin[0];
                        vfTranslation[1] -= origin[1];
                        vfTranslation[2] -= origin[2];
                    }

                    hasChanged[FIELD_TRANSLATION] = true;
                    fireFieldChanged(FIELD_TRANSLATION);

//System.out.println("phi: " + espdu.getEntityOrientationPhi() + " theta: " + espdu.getEntityOrientationTheta() + " Psi: " + espdu.getEntityOrientationPsi());
/*
                    eulersToMatrix(
                        espdu.getEntityOrientationPsi(),
                        espdu.getEntityOrientationTheta(),
                        espdu.getEntityOrientationPhi(),
                        rotationMatrix
                    );

                    rotationQuat.set(rotationMatrix);
                    AxisAngle4f ta = new AxisAngle4f();
                    ta.set(rotationQuat);
                    ta.get(vfRotation);
*/
//System.out.println("recv vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);


//Replicate Original NPS code exactly
                    float roll   = espdu.getEntityOrientationPhi();
                    float pitch = espdu.getEntityOrientationTheta();
                    float yaw   = espdu.getEntityOrientationPsi();
//System.out.println("phi: " + roll + " theta: " + pitch + " Psi: " + yaw);
                    float[] eulers = new float[] {-yaw, roll, pitch};

                    quaternion.setEulers(eulers);
                    quaternion.getAxisAngle (vfRotation);

//System.out.println("recv vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);

                    hasChanged[FIELD_ROTATION] = true;
                    fireFieldChanged(FIELD_ROTATION);
                    stateManager.addEndOfThisFrameListener(this);

                    //ignoreEspdu = true;
                }

                vfEntityCategory = espdu.getEntityTypeCategory().intValue();
                hasChanged[FIELD_ENTITY_CATEGORY] = true;
                fireFieldChanged(FIELD_ENTITY_CATEGORY);

                vfEntityDomain = espdu.getEntityTypeDomain().intValue();
                hasChanged[FIELD_ENTITY_DOMAIN] = true;
                fireFieldChanged(FIELD_ENTITY_DOMAIN);

                vfEntityKind = espdu.getEntityTypeKind().intValue();
                hasChanged[FIELD_ENTITY_KIND] = true;
                fireFieldChanged(FIELD_ENTITY_KIND);

                vfForceID = espdu.getForceID().intValue();
                hasChanged[FIELD_FORCE_ID] = true;
                fireFieldChanged(FIELD_FORCE_ID);

                vfEntitySubCategory = espdu.getEntityTypeSubcategory().intValue();
                hasChanged[FIELD_ENTITY_SUBCATEGORY] = true;
                fireFieldChanged(FIELD_ENTITY_SUBCATEGORY);

                vfEntityExtra = espdu.getEntityTypeExtra().intValue();
                hasChanged[FIELD_ENTITY_EXTRA] = true;
                fireFieldChanged(FIELD_ENTITY_EXTRA);

                vfEntitySpecific = espdu.getEntityTypeSpecific().intValue();
                hasChanged[FIELD_ENTITY_SPECIFIC] = true;
                fireFieldChanged(FIELD_ENTITY_SPECIFIC);

                vfAppearance = espdu.getEntityAppearance().intValue();
                hasChanged[FIELD_APPEARANCE] = true;
                fireFieldChanged(FIELD_APPEARANCE);

                vfEntityCountry = espdu.getEntityTypeCountry().intValue();
                hasChanged[FIELD_ENTITY_COUNTRY] = true;
                fireFieldChanged(FIELD_ENTITY_COUNTRY);

                int articCount = espdu.articulationParameterCountValue();

                if (articCount > 0) {
                    vfArticulationParameterCount = articCount;
                    numArticulationParameterArray = articCount;
                    hasChanged[FIELD_ARTICULATION_PARAMETER_COUNT] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_COUNT);

                    if (vfArticulationParameterArray.length != articCount) {
                        vfArticulationParameterArray = new float[articCount];
                    }

                    if (artVals.length < articCount) {
                        artVals = new ArticulationParameter[articCount];
                    }

                    espdu.getArticulationParameters(artVals);

                    for(int i=0; i < articCount; i++) {
                        vfArticulationParameterArray[i] = (float)
                            artVals[i].getParameterValue();

                        switch(i) {
                            case 0:
                                vfArticulationParameterValue0 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED] = true;
                                break;
                            case 1:
                                vfArticulationParameterValue1 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED] = true;
                                break;
                            case 2:
                                vfArticulationParameterValue2 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED] = true;
                                break;
                            case 3:
                                vfArticulationParameterValue3 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED] = true;
                                break;
                            case 4:
                                vfArticulationParameterValue4 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED] = true;
                                break;
                            case 5:
                                vfArticulationParameterValue5 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED] = true;
                                break;
                            case 6:
                                vfArticulationParameterValue6 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED] = true;
                                break;
                            case 7:
                                vfArticulationParameterValue7 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED] = true;
                                break;
                        }
                    }

                    hasChanged[FIELD_ARTICULATION_PARAMETER_ARRAY] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_ARRAY);
                }
                break;
        }
    }

    /**
     * Tell the DIS node that a packet arrived.  Used to update tiemstamp information.
     *
     */
    public void packetArrived(ProtocolDataUnitType pdu) {
/*    Restore when timeDependant stuff is straightend out
        vfTimestamp = vrmlClock.getTime();

        hasChanged[FIELD_TIMESTAMP] = true;
        fireFieldChanged(FIELD_TIMESTAMP);
*/

        int type = pdu.getPduHeader().getPduType();
        switch(type) {
//            case PduTypeField.FIREFI:
//            case PduTypeField.FIRE:
              case 2:
                FirePdu firepdu = (FirePdu) pdu;

                vfFired1 = true;
                hasChanged[FIELD_FIRED_1] = true;
                fireFieldChanged(FIELD_FIRED_1);

                vfFired2 = false;
                hasChanged[FIELD_FIRED_2] = true;
                fireFieldChanged(FIELD_FIRED_2);

                vfFiredTime = vrmlClock.getTime();
                hasChanged[FIELD_FIRED_TIME] = true;
                fireFieldChanged(FIELD_FIRED_TIME);

                vfEventApplicationID = firepdu.getEventID().getApplicationID().intValue();
                hasChanged[FIELD_EVENT_APPLICATION_ID] = true;
                fireFieldChanged(FIELD_EVENT_APPLICATION_ID);

                vfEventEntityID = firepdu.getEventID().getEventID().intValue();
                hasChanged[FIELD_EVENT_ENTITY_ID] = true;
                fireFieldChanged(FIELD_EVENT_ENTITY_ID);

                vfEventSiteID = firepdu.getEventID().getSiteID().intValue();
                hasChanged[FIELD_EVENT_SITE_ID] = true;
                fireFieldChanged(FIELD_EVENT_SITE_ID);

                vfMunitionApplicationID = firepdu.getMunitionID().getApplicationID().intValue();
                hasChanged[FIELD_MUNITION_APPLICATION_ID] = true;
                fireFieldChanged(FIELD_MUNITION_APPLICATION_ID);

                vfMunitionEntityID = firepdu.getMunitionID().getEntityID().intValue();
                hasChanged[FIELD_MUNITION_ENTITY_ID] = true;
                fireFieldChanged(FIELD_MUNITION_ENTITY_ID);

                vfMunitionSiteID = firepdu.getMunitionID().getSiteID().intValue();
                hasChanged[FIELD_MUNITION_SITE_ID] = true;
                fireFieldChanged(FIELD_MUNITION_SITE_ID);

                vfFireMissionIndex = firepdu.getFireMissionIndex().intValue();
                hasChanged[FIELD_FIRE_MISSION_INDEX] = true;
                fireFieldChanged(FIELD_FIRE_MISSION_INDEX);

                vfFiringRate = firepdu.getBurstDescriptor().getRate().intValue();
                hasChanged[FIELD_FIRING_RATE] = true;
                fireFieldChanged(FIELD_FIRING_RATE);

                vfFiringRange = firepdu.getRange();
                hasChanged[FIELD_FIRING_RANGE] = true;
                fireFieldChanged(FIELD_FIRING_RANGE);

                vfMunitionStartPoint[0] = (float)firepdu.getLocationInWorldCoordinate().getX();
                vfMunitionStartPoint[1] = -(float)firepdu.getLocationInWorldCoordinate().getZ();
                vfMunitionStartPoint[2] = (float)firepdu.getLocationInWorldCoordinate().getY();
                hasChanged[FIELD_MUNITION_START_POINT] = true;
                fireFieldChanged(FIELD_MUNITION_START_POINT);

                calculateMunitionEndPoint(firepdu.getVelocity());
                hasChanged[FIELD_MUNITION_END_POINT] = true;
                fireFieldChanged(FIELD_MUNITION_END_POINT);
                break;

//            case PduTypeField.DETONATIONFI:
//            case PduTypeField.DETONATION:
            case 3:
                DetonationPdu dtpdu = (DetonationPdu) pdu;

                vfIsDetonated = true;
                hasChanged[FIELD_IS_DETONATED] = true;
                fireFieldChanged(FIELD_IS_DETONATED);

                vfDetonateTime = vrmlClock.getTime();
                hasChanged[FIELD_DETONATE_TIME] = true;
                fireFieldChanged(FIELD_DETONATE_TIME);

                vfDetonationResult = dtpdu.getDetonationResult().intValue();
                hasChanged[FIELD_DETONATION_RESULT] = true;
                fireFieldChanged(FIELD_DETONATION_RESULT);

                vfDetonationRelativeLocation[0] = (float)dtpdu.getLocationInEntityCoordinates().getX();
                vfDetonationRelativeLocation[1] = -(float)dtpdu.getLocationInEntityCoordinates().getZ();
                vfDetonationRelativeLocation[2] = (float)dtpdu.getLocationInEntityCoordinates().getY();
                hasChanged[FIELD_DETONATION_RELATIVE_LOCATION] = true;
                fireFieldChanged(FIELD_DETONATION_RELATIVE_LOCATION);

                vfDetonationLocation[0] = (float)dtpdu.getLocationInWorldCoordinate().getX();
                vfDetonationLocation[1] = -(float)dtpdu.getLocationInWorldCoordinate().getZ();
                vfDetonationLocation[2] = (float)dtpdu.getLocationInWorldCoordinate().getY();
                hasChanged[FIELD_DETONATION_LOCATION] = true;
                fireFieldChanged(FIELD_DETONATION_LOCATION);
                break;
//            case PduTypeField.ENTITYSTATE:
            case 1:
                EntityStatePduType espdu = (EntityStatePduType) pdu;

                if (!ignoreEspdu) {
//System.out.println("Time: " + System.currentTimeMillis());
//System.out.println("pos: " + espdu.getEntityLocationX() + " " + (-espdu.getEntityLocationZ()) + " " + espdu.getEntityLocationY());
//System.out.println("vel: " + espdu.getEntityLinearVelocityX() + " " + (-espdu.getEntityLinearVelocityZ()) + " " + espdu.getEntityLinearVelocityY());
//System.out.println("acc: " + espdu.getEntityLinearAccelerationX() + " " + espdu.getEntityLinearAccelerationY() + " " + espdu.getEntityLinearAccelerationZ());

                    Vector3Double location = espdu.getEntityLocation();

                    vfTranslation[0] = (float)location.getX();
                    vfTranslation[1] = -(float)location.getZ();
                    vfTranslation[2] = (float)location.getY();

//System.out.println("Location: " + vfTranslation[0] + " " + vfTranslation[1] + " " + vfTranslation[2]);
                    if (currentMode == MODE_READER && vfGeoOrigin != null) {
                        vfTranslation[0] -= origin[0];
                        vfTranslation[1] -= origin[1];
                        vfTranslation[2] -= origin[2];
                    }

                    hasChanged[FIELD_TRANSLATION] = true;
                    fireFieldChanged(FIELD_TRANSLATION);

/*
                    eulersToMatrix(
                        espdu.getEntityOrientationPsi(),
                        espdu.getEntityOrientationTheta(),
                        espdu.getEntityOrientationPhi(),
                        rotationMatrix
                    );

                    rotationQuat.set(rotationMatrix);
                    AxisAngle4f ta = new AxisAngle4f();
                    ta.set(rotationQuat);
                    ta.get(vfRotation);
*/
//System.out.println("recv vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);


                    EulerAnglesType eat = espdu.getEntityOrientation();

//Replicate Original NPS code exactly
                    float roll   = eat.getPhi();
                    float pitch = eat.getTheta();
                    float yaw   = eat.getPsi();
//System.out.println("phi: " + roll + " theta: " + pitch + " Psi: " + yaw);
                    float[] eulers = new float[] {-yaw, roll, pitch};

                    quaternion.setEulers(eulers);
                    quaternion.getAxisAngle (vfRotation);

//System.out.println("recv vfRot: " + vfRotation[0] + " " + vfRotation[1] + " " + vfRotation[2] + " " + vfRotation[3]);

                    hasChanged[FIELD_ROTATION] = true;
                    fireFieldChanged(FIELD_ROTATION);
                    stateManager.addEndOfThisFrameListener(this);

                    //ignoreEspdu = true;
                }

                vfForceID = espdu.getForceID();
                hasChanged[FIELD_FORCE_ID] = true;
                fireFieldChanged(FIELD_FORCE_ID);


                EntityType et = espdu.getEntity();

                vfEntityCategory = et.getCategory();
                hasChanged[FIELD_ENTITY_CATEGORY] = true;
                fireFieldChanged(FIELD_ENTITY_CATEGORY);

                vfEntityDomain = et.getDomain();
                hasChanged[FIELD_ENTITY_DOMAIN] = true;
                fireFieldChanged(FIELD_ENTITY_DOMAIN);

                vfEntityKind = et.getEntityKind();
                hasChanged[FIELD_ENTITY_KIND] = true;
                fireFieldChanged(FIELD_ENTITY_KIND);

                vfEntitySubCategory = et.getSubCategory();
                hasChanged[FIELD_ENTITY_SUBCATEGORY] = true;
                fireFieldChanged(FIELD_ENTITY_SUBCATEGORY);

                vfEntityExtra = et.getExtra();
                hasChanged[FIELD_ENTITY_EXTRA] = true;
                fireFieldChanged(FIELD_ENTITY_EXTRA);

                vfEntitySpecific = et.getSpecific();
                hasChanged[FIELD_ENTITY_SPECIFIC] = true;
                fireFieldChanged(FIELD_ENTITY_SPECIFIC);

                vfAppearance = espdu.getEntityAppearance();
                hasChanged[FIELD_APPEARANCE] = true;
                fireFieldChanged(FIELD_APPEARANCE);

                vfEntityCountry = et.getCountry();
                hasChanged[FIELD_ENTITY_COUNTRY] = true;
                fireFieldChanged(FIELD_ENTITY_COUNTRY);

                int articCount = espdu.getNumberOfArticulationParameters();

                if (articCount > 0) {
                    vfArticulationParameterCount = articCount;
                    numArticulationParameterArray = articCount;
                    hasChanged[FIELD_ARTICULATION_PARAMETER_COUNT] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_COUNT);

                    if (vfArticulationParameterArray.length != articCount) {
                        vfArticulationParameterArray = new float[articCount];
                    }

                    if (artValsDX.length < articCount) {
                        artValsDX = new ArticulationParametersType[articCount];
                    }

                    List alist = espdu.getArticulationParameters();
                    alist.toArray(artValsDX);

                    for(int i=0; i < articCount; i++) {
                        vfArticulationParameterArray[i] = (float)
                            artValsDX[i].getParameterValue();

                        switch(i) {
                            case 0:
                                vfArticulationParameterValue0 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE0_CHANGED] = true;
                                break;
                            case 1:
                                vfArticulationParameterValue1 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE1_CHANGED] = true;
                                break;
                            case 2:
                                vfArticulationParameterValue2 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE2_CHANGED] = true;
                                break;
                            case 3:
                                vfArticulationParameterValue3 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE3_CHANGED] = true;
                                break;
                            case 4:
                                vfArticulationParameterValue4 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE4_CHANGED] = true;
                                break;
                            case 5:
                                vfArticulationParameterValue5 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE5_CHANGED] = true;
                                break;
                            case 6:
                                vfArticulationParameterValue6 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE6_CHANGED] = true;
                                break;
                            case 7:
                                vfArticulationParameterValue7 =
                                    vfArticulationParameterArray[i];
                                hasChanged[FIELD_ARTICULATION_PARAMETER_VALUE7_CHANGED] = true;
                                break;
                        }
                    }

                    hasChanged[FIELD_ARTICULATION_PARAMETER_ARRAY] = true;
                    fireFieldChanged(FIELD_ARTICULATION_PARAMETER_ARRAY);
                }
                break;
        }
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------

    /**
     * Calculates vfMunitionEndPoint
     */
    private void calculateMunitionEndPoint(LinearVelocity lv) {
        double velocity;    // velocity in horizontal plane
        double psi;     // angle in horizontal plane
        double theta;       // angle in vertical plane

        velocity = Math.sqrt(lv.getX()*lv.getX() + lv.getY()*lv.getY());
        psi = Math.atan2(lv.getX(), lv.getY());
        theta = Math.atan2(-lv.getZ(), velocity);
        vfMunitionEndPoint[0] = vfMunitionStartPoint[0] + (float)(vfFiringRange * Math.cos(theta) * Math.sin(psi));
        vfMunitionEndPoint[1] = vfMunitionStartPoint[1] + (float)(vfFiringRange * Math.sin(theta));
        vfMunitionEndPoint[2] = vfMunitionStartPoint[2] + (float)(vfFiringRange * Math.cos(theta) * Math.cos(psi));
    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return True if they are equal within the given epsilon
     */
    private boolean floatEq(float val1, float val2) {
        float diff = val1 - val2;

        if(diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }

    /**
     * Calculate transforms needed to handle VRML semantics and place the
     * results in the matrix variable of this class.
     *  formula: T x C x R x SR x S x -SR x -C
     */
    protected void updateMatrix() {
//System.out.println("Final Pos: " + vfTranslation[0] + " " + vfTranslation[1] + " " + vfTranslation[2]);
        //System.out.println(this);
        tempVec.x = -vfCenter[0];
        tempVec.y = -vfCenter[1];
        tempVec.z = -vfCenter[2];

        tmatrix.setIdentity();
        tmatrix.setTranslation(tempVec);

        float scaleVal = 1.0f;

        if (floatEq(vfScale[0], vfScale[1]) &&
            floatEq(vfScale[0], vfScale[2])) {

            scaleVal = vfScale[0];
            tempMtx1.set(scaleVal);
            //System.out.println("S" + tempMtx1);

        } else {
            // non-uniform scale
            //System.out.println("Non Uniform Scale");
            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = -vfScaleOrientation[3];

            double tempAxisNormalizer =
                1 / Math.sqrt(tempAxis.x * tempAxis.x +
                              tempAxis.y * tempAxis.y +
                              tempAxis.z * tempAxis.z);

            tempAxis.x *= tempAxisNormalizer;
            tempAxis.y *= tempAxisNormalizer;
            tempAxis.z *= tempAxisNormalizer;

            tempMtx1.set(tempAxis);
            tempMtx2.mul(tempMtx1, tmatrix);

            // Set the scale by individually setting each element
            tempMtx1.setIdentity();
            tempMtx1.m00 = vfScale[0];
            tempMtx1.m11 = vfScale[1];
            tempMtx1.m22 = vfScale[2];

            tmatrix.mul(tempMtx1, tempMtx2);

            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = vfScaleOrientation[3];
            tempMtx1.set(tempAxis);
        }

        tempMtx2.mul(tempMtx1, tmatrix);

        //System.out.println("Sx-C" + tempMtx2);
        float magSq = vfRotation[0] * vfRotation[0] +
                      vfRotation[1] * vfRotation[1] +
                      vfRotation[2] * vfRotation[2];

        if(magSq < ZEROEPS) {
            tempAxis.x = 0;
            tempAxis.y = 0;
            tempAxis.z = 1;
            tempAxis.angle = 0;
        } else {
            if ((magSq > 1.01) || (magSq < 0.99)) {

                float mag = (float)(1 / Math.sqrt(magSq));
                tempAxis.x = vfRotation[0] * mag;
                tempAxis.y = vfRotation[1] * mag;
                tempAxis.z = vfRotation[2] * mag;
            } else {
                tempAxis.x = vfRotation[0];
                tempAxis.y = vfRotation[1];
                tempAxis.z = vfRotation[2];
            }

            tempAxis.angle = vfRotation[3];
        }

        tempMtx1.set(tempAxis);
        //System.out.println("R" + tempMtx1);

        tmatrix.mul(tempMtx1, tempMtx2);
        //System.out.println("RxSx-C" + matrix);

        tempVec.x = vfCenter[0];
        tempVec.y = vfCenter[1];
        tempVec.z = vfCenter[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);
        //System.out.println("C" + tempMtx1);

        tempMtx2.mul(tempMtx1, tmatrix);
        //System.out.println("CxRxSx-C" + tempMtx2);

        tempVec.x = vfTranslation[0];
        tempVec.y = vfTranslation[1];
        tempVec.z = vfTranslation[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);

        // TODO: Try reversing order of ops
        tmatrix.mul(tempMtx1, tempMtx2);
        //tmatrix.mul(tempMtx2, tempMtx1);
    }

    /**
     * Send a notification to the registered listeners the role has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     */
    protected void fireRoleChanged() {
        // Notify listeners of new value
        int num_listeners = roleListeners.size();
        NetworkRoleListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (NetworkRoleListener)roleListeners.get(i);
            ul.roleChanged(currentMode, this);
        }
    }

    /**
     * Regenerate the ID used for writing values to the network.
     */
    public void regenID() {
        id = new EntityID(vfSiteID, vfApplicationID, vfEntityID);

        if (useDX) {
            idDX = new EntityIDTypeImpl();

            idDX.setSite(vfSiteID);
            idDX.setApplication(vfApplicationID);
            idDX.setEntity(vfEntityID);
        }
    }

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeoOrigin(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                  "geoOrigin");

        VRMLNodeType node;
        VRMLNodeType old_node;

        if(pGeoOrigin != null)
            old_node = pGeoOrigin;
        else
            old_node = vfGeoOrigin;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLLocalOriginNodeType))
                throw new InvalidFieldValueException(GEO_ORIGIN_PROTO_MSG);

            node = impl;
            pGeoOrigin = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof VRMLLocalOriginNodeType)) {
            throw new InvalidFieldValueException(GEO_ORIGIN_NODE_MSG);
        } else {
            pGeoOrigin = null;
            node = (VRMLLocalOriginNodeType)geo;
        }

        vfGeoOrigin = (VRMLLocalOriginNodeType) node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }

    /**
     * Resize the articulation parameter array.
     *
     * @param size The new size
     */
    private void resizeAPA(int size) {
        if (artVals.length < size) {
            artVals = new ArticulationParameter[size];
            for(int i=0; i < artVals.length; i++) {
                artVals[i] = new ArticulationParameter();
            }

            float[] tmpVFA = new float[size];
            for(int i=0; i < vfArticulationParameterArray.length; i++) {
                tmpVFA[i] = vfArticulationParameterArray[i];
            }

            vfArticulationParameterArray = tmpVFA;

            if (vfArticulationParameterCount < size)
                vfArticulationParameterCount = size;
        }
    }

    /**
     * Set the XMPP connection parameters.
     *
     * @param value The params username, password, mucServer, mucRoom, authServers
     * @param numValid The number of valid params
     */
    private void setXMPPParams(String[] value, int numValid) {
        if (numValid < 5) {
            System.out.println("Invalid number of XMPP params.  Must be 5 or greater.");
            return;
        }

        // Return null for empty values
        for(int i=0; i < numValid; i++) {
            if (value[i].length() < 1)
                value[i] = null;
        }

        xmppUsername = value[0];
        xmppPassword = value[1];
        xmppMucServer = value[2];
        xmppMucRoom = value[3];
        int len = numValid - 4;

        xmppAuthServer = new String[len];
        for(int i=0; i < len; i++) {
            xmppAuthServer[i] = value[4+i];
        }
    }

    /**
     * Converts a set of Euler angles (phi, theta, psi)
     * to a rotation matrix.
     *
     * @param eulers the Euler angles to convert
     * @param rotMatrix a rotation matrix to hold the result
     */
    private void eulersToMatrix(double x, double y, double z, Matrix3d rotMatrix)
    {
        psiMat.setIdentity();
        psiMat.rotY(-z);

        thetaMat.rotZ(y);

        phiMat.rotX(x);

        rotMatrix.mul(phiMat,thetaMat);

        rotMatrix.mul(psiMat);
    }

    double yaw;
    double pitch;
    double roll;

    private void set(Quat4d q1) {
        double sqw = q1.w*q1.w;
        double sqx = q1.x*q1.x;
        double sqy = q1.y*q1.y;
        double sqz = q1.z*q1.z;
        double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
        double test = q1.x*q1.y + q1.z*q1.w;
        if (test > 0.499*unit) { // singularity at north pole
            yaw = 2 * Math.atan2(q1.x,q1.w);
            pitch = Math.PI/2;
            roll = 0;
            return;
        }
        if (test < -0.499*unit) { // singularity at south pole
            yaw = -2 * Math.atan2(q1.x,q1.w);
            pitch = -Math.PI/2;
            roll = 0;
            return;
        }
        yaw = Math.atan2(2*q1.y*q1.w-2*q1.x*q1.z , sqx - sqy - sqz + sqw);
        pitch = Math.asin(2*test/unit);
        roll = Math.atan2(2*q1.x*q1.w-2*q1.y*q1.z , -sqx + sqy - sqz + sqw);
    }
}
