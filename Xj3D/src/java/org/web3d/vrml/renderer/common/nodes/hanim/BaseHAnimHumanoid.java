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

package org.web3d.vrml.renderer.common.nodes.hanim;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimHumanoid;
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.geom.hanim.HAnimSite;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.FieldValidator;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation for the field handling of a HAnimHumanoid node.
 * <p>
 *
 * The node is defined as follows:
 * <pre>
 *  HAnimHumanoid : X3DChildNode, X3DBoundedObject {
 *    SFVec3f    [in,out] center           0 0 0    (-inf,inf)
 *    MFString   [in,out] info             []
 *    MFNode     [in,out] joints           []       [HAnimJoint]
 *    SFNode     [in,out] metadata         NULL     [X3DMetadataObject]
 *    SFString   [in,out] name             ""
 *    SFRotation [in,out] rotation         0 0 1 0  (-inf,inf)|[-1,1]
 *    SFVec3f    [in,out] scale            1 1 1    (0,inf)
 *    SFRotation [in,out] scaleOrientation 0 0 1 0  (-inf,inf)|[-1,1]
 *    MFNode     [in,out] segments         []       [HAnimSegment]
 *    MFNode     [in,out] sites            []       [HAnimSite]
 *    MFNode     [in,out] skeleton         []       [HAnimJoint]
 *    MFNode     [in,out] skin             []       [X3DChildNode]
 *    SFNode     [in,out] skinCoord        NULL     [X3DCoordinateNode]
 *    SFNode     [in,out] skinNormal       NULL     [X3DNormalNode]
 *    SFVec3f    [in,out] translation      0 0 0    (-inf,inf)
 *    SFString   [in,out] version          ""
 *    MFNode     [in,out] viewpoints       []       [Viewpoint]
 *    SFVec3f    []       bboxCenter       0 0 0    (-inf,inf)
 *    SFVec3f    []       bboxSize         -1 -1 -1 [0,inf) or -1 -1 -1
 *  }
 * </pre>
 *
 * This class does not pass the viewpoint field values along to the render
 * implementation node because that needs to be a renderer-specific object.
 * Derived classes need to handle this in the setHAnimFactory() call and
 * any time the viewpoints are set.
 *
 * @author Justin Couch
 * @version $Revision: 2.5 $
 */
public abstract class BaseHAnimHumanoid extends AbstractNode
    implements VRMLChildNodeType, VRMLBoundedNodeType, VRMLHAnimHumanoidNodeType {

    /** Field Index for the field: center */
    protected static final int FIELD_CENTER = LAST_NODE_INDEX + 1;

    /** Field Index for the field: rotation */
    protected static final int FIELD_ROTATION = LAST_NODE_INDEX + 2;

    /** Field Index for the field: scale */
    protected static final int FIELD_SCALE = LAST_NODE_INDEX + 3;

    /** Field Index for the field: scaleOrientation */
    protected static final int FIELD_SCALE_ORIENTATION = LAST_NODE_INDEX + 4;

    /** Field Index for the field: translation */
    protected static final int FIELD_TRANSLATION = LAST_NODE_INDEX + 5;

    /** Field Index for the field: name */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 6;

    /** Field Index for the field: info */
    protected static final int FIELD_INFO = LAST_NODE_INDEX + 7;

    /** Field Index for the field: joints */
    protected static final int FIELD_JOINTS = LAST_NODE_INDEX + 8;

    /** Field Index for the field: segments */
    protected static final int FIELD_SEGMENTS = LAST_NODE_INDEX + 9;

    /** Field Index for the field: sites */
    protected static final int FIELD_SITES = LAST_NODE_INDEX + 10;

    /** Field Index for the field: skeleton */
    protected static final int FIELD_SKELETON = LAST_NODE_INDEX + 11;

    /** Field Index for the field: skin */
    protected static final int FIELD_SKIN = LAST_NODE_INDEX + 12;

    /** Field Index for the field: skinCoord */
    protected static final int FIELD_SKIN_COORD = LAST_NODE_INDEX + 13;

    /** Field Index for the field: skinNormal */
    protected static final int FIELD_SKIN_NORMAL = LAST_NODE_INDEX + 14;

    /** Field Index for the field: version */
    protected static final int FIELD_VERSION = LAST_NODE_INDEX + 15;

    /** Field Index for the field: viewpoints */
    protected static final int FIELD_VIEWPOINTS = LAST_NODE_INDEX + 16;

    /** Index of the Bounding box center bboxCenter field */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 17;

    /** Index of the Bounding box size bboxSize field */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 18;

    /** The last field index used by this class */
    protected static final int LAST_HUMANOID_INDEX = FIELD_BBOX_SIZE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_HUMANOID_INDEX + 1;

    /** Message for when writing to an initialiseOnly field */
    protected static final String INITONLY_FIELD_MSG =
        "Writing to initialize only field was attempted";

    /** Message for when the proto is not a CoordinateNodeType */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a X3DCoordinateNode object";

    /** Message for when the node in setValue() is not a CoordinateNodeType */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a X3DCoordinateNode object";

    /** Message for when the proto is not a NormalNodeType */
    protected static final String NORMAL_PROTO_MSG =
        "Proto does not describe a X3DNormalNode object";

    /** Message for when the node in setValue() is not a NormalNodeType */
    protected static final String NORMAL_NODE_MSG =
        "Node does not describe a X3DNormalNode object";


    /** Message for when the proto is not a HAnimJoint instance */
    protected static final String JOINT_PROTO_MSG =
        "Proto does not describe a HAnimJoint object";

    /** Message for when the node is not a HAnimJoint instance */
    protected static final String JOINT_NODE_MSG =
        "Node does not describe a HAnimJoint object";

    /** Message for when the proto is not a HAnimSegment instance */
    protected static final String SEGMENT_PROTO_MSG =
        "Proto does not describe a HAnimSegment object";

    /** Message for when the node is not a HAnimSegment instance */
    protected static final String SEGMENT_NODE_MSG =
        "Node does not describe a HAnimSegment object";

    /** Message for when the proto is not a HAnimSite instance */
    protected static final String SITE_PROTO_MSG =
        "Proto does not describe a HAnimSite object";

    /** Message for when the node is not a HAnimSite instance */
    protected static final String SITE_NODE_MSG =
        "Node does not describe a HAnimSite object";

    /** Message for when the proto is not a X3DChildNode instance */
    protected static final String CHILD_PROTO_MSG =
        "Proto does not describe a X3DChildNode object";

    /** Message for when the node is not a X3DChildNode instance */
    protected static final String CHILD_NODE_MSG =
        "Node does not describe a X3DChildNode object";

    /** Message for when the proto is not a Viewpoint instance */
    protected static final String VIEWPOINT_PROTO_MSG =
        "Proto does not describe a Viewpoint object";

    /** Message for when the node is not a Viewpoint instance */
    protected static final String VIEWPOINT_NODE_MSG =
        "Node does not describe a Viewpoint object";


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

    /** The name of this humanoid */
    protected String vfName;

    /** exposedField SFNode skinCoord */
    protected VRMLCoordinateNodeType vfSkinCoord;

    /** proto version of skinCoord */
    protected VRMLProtoInstance pSkinCoord;

    /** exposedField SFNode skinNormal */
    protected VRMLNormalNodeType vfSkinNormal;

    /** proto version of skinNormal */
    protected VRMLProtoInstance pSkinNormal;

    /** field SFVec3f bboxCenter */
    protected float[] vfBboxCenter;

    /** field SFVec3f bboxSize */
    protected float[] vfBboxSize;

    /** The version of the humanoid model */
    protected String vfVersion;

    /** Additional information attached to the model */
    protected String[] vfInfo;

    /** Number of valid values in vfInfo */
    protected int numInfo;

    /** exposedField MFNode joints */
    protected ArrayList vfJoints;

    /** exposedField MFNode segments */
    protected ArrayList vfSegments;

    /** exposedField MFNode sites */
    protected ArrayList vfSites;

    /** exposedField MFNode skeleton */
    protected ArrayList vfSkeleton;

    /** exposedField MFNode skin */
    protected ArrayList vfSkin;

    /** exposedField MFNode viewpoints */
    protected ArrayList vfViewpoints;

    /** Temp array for fetching node lists from ArrayLists */
    protected VRMLNodeType[] nodeTmp;

    /** Counter for the number of sharing references this has */
    protected int shareCount;

    /** The generic internal representation of the node */
    protected HAnimHumanoid hanimImpl;

    /** Factory used to generate the implementation node */
    protected HAnimFactory hanimFactory;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] {
            FIELD_JOINTS,
            FIELD_SEGMENTS,
            FIELD_SITES,
            FIELD_SKELETON,
            FIELD_SKIN,
            FIELD_SKIN_COORD,
            FIELD_SKIN_NORMAL,
            FIELD_VIEWPOINTS,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
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
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");
        fieldDecl[FIELD_INFO] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "info");
        fieldDecl[FIELD_JOINTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "joints");
        fieldDecl[FIELD_SEGMENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "segments");
        fieldDecl[FIELD_SITES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "sites");
        fieldDecl[FIELD_SKELETON] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "skeleton");
        fieldDecl[FIELD_SKIN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "skin");
        fieldDecl[FIELD_SKIN_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "skinCoord");
        fieldDecl[FIELD_SKIN_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "skinNormal");
        fieldDecl[FIELD_VERSION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "version");
        fieldDecl[FIELD_VIEWPOINTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "viewpoints");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

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

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);

        idx = new Integer(FIELD_INFO);
        fieldMap.put("info", idx);
        fieldMap.put("set_info", idx);
        fieldMap.put("info_changed", idx);

        idx = new Integer(FIELD_JOINTS);
        fieldMap.put("joints", idx);
        fieldMap.put("set_joints", idx);
        fieldMap.put("joints_changed", idx);

        idx = new Integer(FIELD_SEGMENTS);
        fieldMap.put("segments", idx);
        fieldMap.put("set_segments", idx);
        fieldMap.put("segments_changed", idx);

        idx = new Integer(FIELD_SITES);
        fieldMap.put("sites", idx);
        fieldMap.put("set_sites", idx);
        fieldMap.put("sites_changed", idx);

        idx = new Integer(FIELD_SKELETON);
        fieldMap.put("skeleton", idx);
        fieldMap.put("set_skeleton", idx);
        fieldMap.put("skeleton_changed", idx);

        idx = new Integer(FIELD_SKIN);
        fieldMap.put("skin", idx);
        fieldMap.put("set_skin", idx);
        fieldMap.put("skin_changed", idx);

        idx = new Integer(FIELD_SKIN_COORD);
        fieldMap.put("skinCoord", idx);
        fieldMap.put("set_skinCoord", idx);
        fieldMap.put("skinCoord_changed", idx);

        idx = new Integer(FIELD_SKIN_NORMAL);
        fieldMap.put("skinNormal", idx);
        fieldMap.put("set_skinNormal", idx);
        fieldMap.put("skinNormal_changed", idx);

        idx = new Integer(FIELD_VERSION);
        fieldMap.put("version", idx);
        fieldMap.put("set_version", idx);
        fieldMap.put("version_changed", idx);

        idx = new Integer(FIELD_VIEWPOINTS);
        fieldMap.put("viewpoints", idx);
        fieldMap.put("set_viewpoints", idx);
        fieldMap.put("viewpoints_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseHAnimHumanoid() {
        super("HAnimJoint");

        hasChanged = new boolean[LAST_HUMANOID_INDEX + 1];

        vfCenter = new float[] {0, 0, 0};
        vfRotation = new float[] {0, 0, 1, 0};
        vfScale = new float[] {1, 1, 1};
        vfScaleOrientation = new float[] {0, 0, 1, 0};
        vfTranslation = new float[] {0, 0, 0};

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};

        vfInfo = FieldConstants.EMPTY_MFSTRING;

        vfJoints = new ArrayList();
        vfSegments = new ArrayList();
        vfSites = new ArrayList();
        vfSkeleton = new ArrayList();
        vfSkin = new ArrayList();
        vfViewpoints = new ArrayList();

        shareCount = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseHAnimHumanoid(VRMLNodeType node) {
        this();

        checkNodeType(node);

        VRMLBoundedNodeType b_node = (VRMLBoundedNodeType)node;
        float[] val = b_node.getBboxCenter();

        vfBboxCenter[0] = val[0];
        vfBboxCenter[1] = val[1];
        vfBboxCenter[2] = val[2];

        val = b_node.getBboxSize();

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];

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

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;

            index = node.getFieldIndex("version");
            field = node.getFieldValue(index);
            vfVersion = field.stringValue;

            index = node.getFieldIndex("info");
            field = node.getFieldValue(index);

            if(vfInfo.length < field.numElements)
                vfInfo = new String[field.numElements];

            System.arraycopy(field.stringArrayValue,
                             0,
                             vfInfo,
                             0,
                             field.numElements);
            numInfo = field.numElements;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLHAnimHumanoidNodeType
    //----------------------------------------------------------

    /**
     * Notification that the event model is complete and skeleton should
     * perform all it's updates now.
     */
    public void updateMesh() {
        hanimImpl.updateSkeleton();
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
        hanimFactory = fac;

        // Loop through all the skeleton and set their factory first.
        int num_skeleton = vfSkeleton.size();
        for(int i = 0; i < num_skeleton; i++)
        {
            VRMLHAnimNodeType skel = (VRMLHAnimNodeType)vfSkeleton.get(i);
            skel.setHAnimFactory(fac);
        }

        int num_sites = vfSites.size();
        for(int i = 0; i < num_sites; i++)
        {
            VRMLHAnimNodeType site = (VRMLHAnimNodeType)vfSites.get(i);
            site.setHAnimFactory(fac);
        }

        hanimImpl = fac.createHumanoid();
        hanimImpl.setErrorReporter(errorReporter);
        hanimImpl.setName(vfName);
        hanimImpl.setCenter(vfCenter);
        hanimImpl.setScale(vfScale);
        hanimImpl.setRotation(vfRotation);
        hanimImpl.setTranslation(vfTranslation);
        hanimImpl.setScaleOrientation(vfScaleOrientation);
        hanimImpl.setBboxCenter(vfBboxCenter);
        hanimImpl.setBboxSize(vfBboxSize);
        hanimImpl.setVersion(vfVersion);
        hanimImpl.setInfo(vfInfo, numInfo);

        if(vfSkinCoord != null)
        {
            int num_point = vfSkinCoord.getNumPoints();
            float[] tmp = vfSkinCoord.getPointRef();
            hanimImpl.setSkinCoord(tmp, num_point / 3);
        }

        if(vfSkinNormal != null)
        {
            int num_norms = vfSkinNormal.getNumNormals();
            float[] tmp = vfSkinNormal.getVectorRef();
            hanimImpl.setSkinNormal(tmp, num_norms / 3);
        }

        HAnimObject[] skel_list = new HAnimObject[num_skeleton];

        for(int i = 0; i < num_skeleton; i++)
        {
            VRMLHAnimNodeType skel = (VRMLHAnimNodeType)vfSkeleton.get(i);
            skel_list[i] = (HAnimObject)skel.getHAnimObject();
        }

        hanimImpl.setSkeleton(skel_list, num_skeleton);

        HAnimSite[] site_list = new HAnimSite[num_sites];

        for(int i = 0; i < num_sites; i++)
        {
            VRMLHAnimNodeType site = (VRMLHAnimNodeType)vfSkeleton.get(i);
            site_list[i] = (HAnimSite)site.getHAnimObject();
        }

        hanimImpl.setSites(site_list, num_sites);

        // The viewpoint field needs to be handled by the derived class.

        // Don't both passing the segment or joint list along. Doesn't do
        // anything useful.
    }

    /**
     * Get the HAnim implementation node. Since the HAnim class instance is not
     * the same as the basic geometry instance of the particular rendering API, we
     * need to fetch this higher-level construct so that the scene graph can be
     * constructed.
     *
     * @return The HAnimObject instance for this node
     */
    public HAnimObject getHAnimObject() {
        return hanimImpl;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished. This
     * will call setupFinished() on the child nodes.
     *
     * Derived classes that do not like this behaviour should override this
     * method or ensure that the implGroup has a parent before this method
     * is called.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pSkinCoord != null)
            pSkinCoord.setupFinished();
        else if(vfSkinCoord != null)
            vfSkinCoord.setupFinished();

        if(pSkinNormal != null)
            pSkinNormal.setupFinished();
        else if(vfSkinNormal != null)
            vfSkinNormal.setupFinished();

        int num_kids = vfJoints.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfJoints.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }

        num_kids = vfSegments.size();

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfSegments.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }

        num_kids = vfSites.size();

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfSites.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }

        num_kids = vfSkeleton.size();

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfSkeleton.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }

        num_kids = vfSkin.size();

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfSkin.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }

        num_kids = vfViewpoints.size();

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfViewpoints.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

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
        if(index < 0  || index > LAST_HUMANOID_INDEX)
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
        return TypeConstants.HumanoidNodeType;
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

        fieldData.clear();

        switch(index) {
            case FIELD_CENTER:
                fieldData.floatArrayValue = vfCenter;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_ROTATION:
                fieldData.floatArrayValue = vfRotation;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValue = vfScale;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SCALE_ORIENTATION:
                fieldData.floatArrayValue = vfScaleOrientation;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_TRANSLATION:
                fieldData.floatArrayValue = vfTranslation;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_NAME:
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_VERSION:
                fieldData.stringValue = vfVersion;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_INFO:
                fieldData.stringArrayValue = vfInfo;
                fieldData.numElements = numInfo;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.floatArrayValue = vfBboxSize;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.floatArrayValue = vfBboxCenter;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SKIN_COORD:
                if(pSkinCoord != null)
                    fieldData.nodeValue = pSkinCoord;
                else
                    fieldData.nodeValue = vfSkinCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SKIN_NORMAL:
                if(pSkinNormal != null)
                    fieldData.nodeValue = pSkinNormal;
                else
                    fieldData.nodeValue = vfSkinNormal;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_JOINTS:
                fieldData.numElements = vfJoints.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfJoints.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_SEGMENTS:
                fieldData.numElements = vfSegments.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfSegments.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_SITES:
                fieldData.numElements = vfSites.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfSites.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_SKELETON:
                fieldData.numElements = vfSkeleton.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfSkeleton.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_SKIN:
                fieldData.numElements = vfSkin.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfSkin.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_VIEWPOINTS:
                fieldData.numElements = vfViewpoints.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfViewpoints.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
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
                case FIELD_CENTER:
                    destNode.setValue(destIndex, vfCenter, 3);
                    break;

                case FIELD_ROTATION:
                    destNode.setValue(destIndex, vfRotation, 4);
                    break;

                case FIELD_SCALE:
                    destNode.setValue(destIndex, vfScale, 3);
                    break;

                case FIELD_SCALE_ORIENTATION:
                    destNode.setValue(destIndex, vfScaleOrientation, 4);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 3);
                    break;

                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                case FIELD_SKIN_COORD:
                    if(pSkinCoord != null)
                        destNode.setValue(destIndex, pSkinCoord);
                    else
                        destNode.setValue(destIndex, vfSkinCoord);
                    break;

                case FIELD_SKIN_NORMAL:
                    if(pSkinNormal != null)
                        destNode.setValue(destIndex, pSkinNormal);
                    else
                        destNode.setValue(destIndex, vfSkinNormal);
                    break;

                case FIELD_JOINTS:
                    int size = vfJoints.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfJoints.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_SEGMENTS:
                    size = vfSegments.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfSegments.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_SITES:
                    size = vfSites.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfSites.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_SKELETON:
                    size = vfSkeleton.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfSkeleton.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_SKIN:
                    size = vfSkin.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfSkin.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_VIEWPOINTS:
                    size = vfViewpoints.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfViewpoints.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseHAnimHumanoid.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
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
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

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

            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_NAME:
                vfName = value;

                if(!inSetup) {
                    hanimImpl.setName(vfName);
                    hasChanged[FIELD_NAME] = true;
                    fireFieldChanged(FIELD_NAME);
                }
                break;

            case FIELD_VERSION:
                vfVersion = value;

                if(!inSetup) {
                    hasChanged[FIELD_VERSION] = true;
                    fireFieldChanged(FIELD_VERSION);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_INFO:
                if(vfInfo.length < numValid)
                    vfInfo = new String[numValid];

                System.arraycopy(value, 0, vfInfo, 0, numValid);
                numInfo = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_INFO] = true;
                    fireFieldChanged(FIELD_INFO);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }


    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_JOINTS:
                if(!inSetup)
                    clearJoints();

                if(child != null)
                    addJointNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_JOINTS] = true;
                    fireFieldChanged(FIELD_JOINTS);
                }
                break;

            case FIELD_SEGMENTS:
                if(!inSetup)
                    clearSegments();

                if(child != null)
                    addSegmentNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_SEGMENTS] = true;
                    fireFieldChanged(FIELD_SEGMENTS);
                }
                break;

            case FIELD_SITES:
                if(!inSetup)
                    clearSites();

                if(child != null)
                    addSiteNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_SITES] = true;
                    fireFieldChanged(FIELD_SITES);
                }
                break;

            case FIELD_SKELETON:
                if(!inSetup)
                    clearSkeleton();

                if(child != null)
                    addSkeletonNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_SKELETON] = true;
                    fireFieldChanged(FIELD_SKELETON);
                }
                break;

            case FIELD_SKIN:
                if(!inSetup)
                    clearSkin();

                if(child != null)
                    addSkinNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_SKIN] = true;
                    fireFieldChanged(FIELD_SKIN);
                }
                break;

            case FIELD_VIEWPOINTS:
                if(!inSetup)
                    clearViewpoints();

                if(child != null)
                    addViewpointNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_VIEWPOINTS] = true;
                    fireFieldChanged(FIELD_VIEWPOINTS);
                }
                break;

            case FIELD_SKIN_COORD:
                setSkinCoord(child);
                break;

            case FIELD_SKIN_NORMAL:
                setSkinNormal(child);
                break;

            default:
                super.setValue(index, child);
        }

    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
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
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_JOINTS:
                if(!inSetup)
                    clearJoints();

                for(int i = 0; i < numValid; i++)
                    addJointNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_JOINTS] = true;
                    fireFieldChanged(FIELD_JOINTS);
                }
                break;

            case FIELD_SEGMENTS:
                if(!inSetup)
                    clearSegments();

                for(int i = 0; i < numValid; i++)
                    addSegmentNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_SEGMENTS] = true;
                    fireFieldChanged(FIELD_SEGMENTS);
                }
                break;

            case FIELD_SITES:
                if(!inSetup)
                    clearSites();

                for(int i = 0; i < numValid; i++)
                    addSiteNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_SITES] = true;
                    fireFieldChanged(FIELD_SITES);
                }
                break;

            case FIELD_SKELETON:
                if(!inSetup)
                    clearSkeleton();

                for(int i = 0; i < numValid; i++)
                    addSkeletonNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_SKELETON] = true;
                    fireFieldChanged(FIELD_SKELETON);
                }
                break;

            case FIELD_SKIN:
                if(!inSetup)
                    clearSkin();

                for(int i = 0; i < numValid; i++)
                    addSkinNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_SKIN] = true;
                    fireFieldChanged(FIELD_SKIN);
                }
                break;

            case FIELD_VIEWPOINTS:
                if(!inSetup)
                    clearViewpoints();

                for(int i = 0; i < numValid; i++)
                    addViewpointNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_VIEWPOINTS] = true;
                    fireFieldChanged(FIELD_VIEWPOINTS);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>bboxCenter</b>
     * default value is <code>0 0 0</code>.
     *
     * @return Value of bboxCenter(SFVec3f)
     */
    public float[] getBboxCenter () {
        return vfBboxCenter;
    }

    /**
     * Accessor method to get current value of field <b>bboxSize</b>
     * default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    public float[] getBboxSize () {
        return vfBboxSize;
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    public boolean isShared() {
        return (shareCount > 1);
    }

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    public void setShared(boolean used) {

        if(used)
            shareCount++;
        else
            shareCount--;

        Object kid;

        int num_kids = vfSkeleton.size();

        for(int i = 0; i < num_kids; i++) {
            kid = vfSkeleton.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(used);
        }

        num_kids = vfSkin.size();

        for(int i = 0; i < num_kids; i++) {
            kid = vfSkin.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(used);
        }
    }

    /**
     * Set the rotation component of the joint. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    protected void setRotation(float[] rot)
        throws InvalidFieldValueException {

        if(rot == null)
            throw new InvalidFieldValueException("Rotation value null");

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);
        }
    }

    /**
     * Set the translation component of the joint. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    protected void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];
        vfTranslation[2] = tx[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);
        }
    }

    /**
     * Set the scale component of the joint. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    protected void setScale(float[] scale)
        throws InvalidFieldValueException {

        if(scale == null)
            throw new InvalidFieldValueException("Scale value null");

        vfScale[0] = scale[0];
        vfScale[1] = scale[1];
        vfScale[2] = scale[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_SCALE] = true;
            fireFieldChanged(FIELD_SCALE);
        }
    }

    /**
     * Set the scale orientation component of the joint. Setting a value
     * of null is an error
     *
     * @param so The new scale orientation component
     * @throws InvalidFieldValueException The scale orientation was null
     */
    protected void setScaleOrientation(float[] so)
        throws InvalidFieldValueException {
        if(so == null)
            throw new InvalidFieldValueException("Scale Orientation value null");

        vfScaleOrientation[0] = so[0];
        vfScaleOrientation[1] = so[1];
        vfScaleOrientation[2] = so[2];
        vfScaleOrientation[3] = so[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_SCALE_ORIENTATION] = true;
            fireFieldChanged(FIELD_SCALE_ORIENTATION);
        }
    }

    /**
     * Set the center component of the joint. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    protected void setCenter(float[] center)
        throws InvalidFieldValueException {

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_CENTER] = true;
            fireFieldChanged(FIELD_CENTER);
        }
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box center to set
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    private void setBboxCenter(float[] val)
        throws InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INITONLY_FIELD_MSG);

        vfBboxCenter[0] = val[0];
        vfBboxCenter[1] = val[1];
        vfBboxCenter[2] = val[2];
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box size to set
     * @throws InvalidFieldValueException The bounds is not valid
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    private void setBboxSize(float[] val)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INITONLY_FIELD_MSG);

        FieldValidator.checkBBoxSize(getVRMLNodeName(),val);

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];
    }

    /**
     * Set node content as replacement for the skinCoord field.
     *
     * @param coord The new coordinate node.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setSkinCoord(VRMLNodeType coord)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pSkinCoord != null)
            old_node = pSkinCoord;
        else
            old_node = vfSkinCoord;

        if(coord instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)coord).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLCoordinateNodeType))
                throw new InvalidFieldValueException(COORD_PROTO_MSG);

            pSkinCoord = (VRMLProtoInstance)coord;
            vfSkinCoord = (VRMLCoordinateNodeType)impl;

        } else if((coord != null) &&
                  !(coord instanceof VRMLCoordinateNodeType)) {
            throw new InvalidFieldValueException(COORD_NODE_MSG);
        } else {
            pSkinCoord = null;
            vfSkinCoord = (VRMLCoordinateNodeType)coord;
        }

        if(coord != null)
            updateRefs(coord, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(coord != null)
                stateManager.registerAddedNode(coord);

            hasChanged[FIELD_SKIN_COORD] = true;
            fireFieldChanged(FIELD_SKIN_COORD);
        }
    }

    /**
     * Set node content as replacement for the skinNormal field.
     *
     * @param normal The new normal node.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setSkinNormal(VRMLNodeType normal)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pSkinCoord != null)
            old_node = pSkinNormal;
        else
            old_node = vfSkinNormal;

        if(normal instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)normal).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNormalNodeType))
                throw new InvalidFieldValueException(NORMAL_PROTO_MSG);

            pSkinNormal = (VRMLProtoInstance)normal;
            vfSkinNormal = (VRMLNormalNodeType)impl;

        } else if((normal != null) &&
                  !(normal instanceof VRMLNormalNodeType)) {
            throw new InvalidFieldValueException(NORMAL_NODE_MSG);
        } else {
            pSkinNormal = null;
            vfSkinNormal = (VRMLNormalNodeType)normal;
        }

        if(normal != null)
            updateRefs(normal, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(normal != null)
                stateManager.registerAddedNode(normal);

            hasChanged[FIELD_SKIN_NORMAL] = true;
            fireFieldChanged(FIELD_SKIN_NORMAL);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearJoints() {
        int num_kids = vfJoints.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfJoints.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfJoints.clear();

        hanimImpl.setJoints(null, 0);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addJointNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseHAnimJoint))
                throw new InvalidFieldValueException(JOINT_PROTO_MSG);

        } else if(node != null && !(node instanceof BaseHAnimJoint)) {
            throw new InvalidFieldValueException(JOINT_NODE_MSG);
        }

        vfJoints.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearSegments() {
        int num_kids = vfSegments.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfSegments.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfSegments.clear();

        hanimImpl.setSegments(null, 0);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addSegmentNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseHAnimSegment))
                throw new InvalidFieldValueException(SEGMENT_PROTO_MSG);

        } else if(node != null && !(node instanceof BaseHAnimSegment)) {
            throw new InvalidFieldValueException(SEGMENT_NODE_MSG);
        }

        vfSegments.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearSites() {
        int num_kids = vfSites.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfSites.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfSites.clear();

        hanimImpl.setSites(null, 0);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addSiteNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseHAnimSite))
                throw new InvalidFieldValueException(SITE_PROTO_MSG);

        } else if(node != null && !(node instanceof BaseHAnimSite)) {
            throw new InvalidFieldValueException(SITE_NODE_MSG);
        }

        vfSites.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearSkeleton() {
        int num_kids = vfSkeleton.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfSkeleton.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfSkeleton.clear();

        hanimImpl.setSkeleton(null, 0);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addSkeletonNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseHAnimJoint))
                throw new InvalidFieldValueException(JOINT_PROTO_MSG);

        } else if(node != null && !(node instanceof BaseHAnimJoint)) {
            throw new InvalidFieldValueException(JOINT_NODE_MSG);
        }

        vfSkeleton.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearSkin() {
        int num_kids = vfSkin.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfSkin.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfSkin.clear();
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

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLChildNodeType))
                throw new InvalidFieldValueException(CHILD_PROTO_MSG);

        } else if(node != null && !(node instanceof VRMLChildNodeType)) {
            throw new InvalidFieldValueException(CHILD_NODE_MSG);
        }

        vfSkin.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearViewpoints() {
        int num_kids = vfViewpoints.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfViewpoints.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfViewpoints.clear();

        hanimImpl.setViewpoints(null, 0);
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

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            // Note that these checks would also allow through a GeoViewpoint
            // node, which is technically not correct according to the spec.
            if((impl != null) && !(impl instanceof VRMLViewpointNodeType))
                throw new InvalidFieldValueException(VIEWPOINT_PROTO_MSG);

        } else if(node != null && !(node instanceof VRMLViewpointNodeType)) {
            throw new InvalidFieldValueException(VIEWPOINT_NODE_MSG);
        }

        vfViewpoints.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }
}
