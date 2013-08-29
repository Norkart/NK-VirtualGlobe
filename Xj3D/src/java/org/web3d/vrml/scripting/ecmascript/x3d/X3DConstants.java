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

package org.web3d.vrml.scripting.ecmascript.x3d;

// Standard imports
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.FieldConstants;

import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * X3DConstants miscellaneous object.
 * <P>
 *
 * All properties are fixed, read-only.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class X3DConstants extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashMap propertyValues;

    static {
        propertyValues = new HashMap();
        propertyValues.put("INITIALIZED_EVENT", new Integer(0));
        propertyValues.put("SHUTDOWN_EVENT", new Integer(1));
        propertyValues.put("CONNECTION_ERROR", new Integer(2));
        propertyValues.put("INITIALIZED_ERROR", new Integer(3));
        propertyValues.put("NOT_STARTED_STATE", new Integer(4));
        propertyValues.put("IN_PROGRESS_STATE", new Integer(5));
        propertyValues.put("COMPLETE_STATE", new Integer(6));
        propertyValues.put("FAILED_STATE", new Integer(7));
        propertyValues.put("SFBool", new Integer(FieldConstants.SFBOOL));
        propertyValues.put("MFBool", new Integer(FieldConstants.MFBOOL));
        propertyValues.put("MFInt32", new Integer(FieldConstants.MFINT32));
        propertyValues.put("SFInt32", new Integer(FieldConstants.SFINT32));
        propertyValues.put("SFFloat", new Integer(FieldConstants.SFFLOAT));
        propertyValues.put("MFFloat", new Integer(FieldConstants.MFFLOAT));
        propertyValues.put("SFDouble", new Integer(FieldConstants.SFDOUBLE));
        propertyValues.put("MFDouble", new Integer(FieldConstants.MFDOUBLE));
        propertyValues.put("SFTime", new Integer(FieldConstants.SFTIME));
        propertyValues.put("MFTime", new Integer(FieldConstants.MFTIME));
        propertyValues.put("SFNode", new Integer(FieldConstants.SFNODE));
        propertyValues.put("MFNode", new Integer(FieldConstants.MFNODE));
        propertyValues.put("SFVec2f", new Integer(FieldConstants.SFVEC2F));
        propertyValues.put("MFVec2f", new Integer(FieldConstants.MFVEC2F));
        propertyValues.put("SFVec3f", new Integer(FieldConstants.SFVEC3F));
        propertyValues.put("MFVec3f", new Integer(FieldConstants.MFVEC3F));
        propertyValues.put("SFVec3d", new Integer(FieldConstants.SFVEC3D));
        propertyValues.put("MFVec3d", new Integer(FieldConstants.MFVEC3D));
        propertyValues.put("SFRotation", new Integer(FieldConstants.SFROTATION));
        propertyValues.put("MFRotation", new Integer(FieldConstants.MFROTATION));
        propertyValues.put("SFColor", new Integer(FieldConstants.SFCOLOR));
        propertyValues.put("MFColor", new Integer(FieldConstants.MFCOLOR));
        propertyValues.put("SFColorRGBA", new Integer(FieldConstants.SFCOLORRGBA));
        propertyValues.put("MFColorRGBA", new Integer(FieldConstants.MFCOLORRGBA));
        propertyValues.put("SFImage", new Integer(FieldConstants.SFIMAGE));
        propertyValues.put("MFImage", new Integer(FieldConstants.MFIMAGE));
        propertyValues.put("SFString", new Integer(FieldConstants.SFSTRING));
        propertyValues.put("MFString", new Integer(FieldConstants.MFSTRING));
        propertyValues.put("inputOutput", new Integer(FieldConstants.EXPOSEDFIELD));
        propertyValues.put("initializeOnly", new Integer(FieldConstants.FIELD));
        propertyValues.put("inputOnly", new Integer(FieldConstants.EVENTIN));
        propertyValues.put("outputOnly", new Integer(FieldConstants.EVENTOUT));
        propertyValues.put("X3DBoundedObject", new Integer(TypeConstants.BoundedNodeType));
        propertyValues.put("X3DUrlObject", new Integer(TypeConstants.ExternalNodeType));
        propertyValues.put("X3DAppearanceNode", new Integer(TypeConstants.AppearanceNodeType));
        propertyValues.put("X3DAppearanceChildNode", new Integer(TypeConstants.AppearanceChildNodeType));
        propertyValues.put("X3DMaterialNode", new Integer(TypeConstants.MaterialNodeType));
        propertyValues.put("X3DTextureNode", new Integer(TypeConstants.TextureNodeType));
        propertyValues.put("X3DTexture2DNode", new Integer(TypeConstants.Texture2DNodeType));
        propertyValues.put("X3DTexture3DNode", new Integer(TypeConstants.Texture3DNodeType));
        propertyValues.put("X3DTextureTransformNode", new Integer(TypeConstants.TextureTransformNodeType));
        propertyValues.put("X3DGeometryNode", new Integer(TypeConstants.GeometryNodeType));
        propertyValues.put("X3DParametricGeometryNode", new Integer(TypeConstants.ParametricGeometryNodeType));
        propertyValues.put("X3DGeometricPropertyNode", new Integer(TypeConstants.GeometricPropertyNodeType));
        propertyValues.put("X3DColorNode", new Integer(TypeConstants.ColorNodeType));
        propertyValues.put("X3DCoordinateNode", new Integer(TypeConstants.CoordinateNodeType));
        propertyValues.put("X3DNormalNode", new Integer(TypeConstants.NormalNodeType));
        propertyValues.put("X3DTextureCoordinateNode", new Integer(TypeConstants.TextureCoordinateNodeType));
        propertyValues.put("X3DFontStyleNode", new Integer(TypeConstants.FontStyleNodeType));
        propertyValues.put("X3DProtoInstance", new Integer(TypeConstants.ProtoInstance));
        propertyValues.put("X3DChildNode", new Integer(TypeConstants.ChildNodeType));
        propertyValues.put("X3DBindableNode", new Integer(TypeConstants.BindableNodeType));
        propertyValues.put("X3DBackgroundNode", new Integer(TypeConstants.BackgroundNodeType));
        propertyValues.put("X3DGroupingNode", new Integer(TypeConstants.GroupingNodeType));
        propertyValues.put("X3DShapeNode", new Integer(TypeConstants.ShapeNodeType));
        propertyValues.put("X3DInterpolatorNode", new Integer(TypeConstants.InterpolatorNodeType));
        propertyValues.put("X3DLightNode", new Integer(TypeConstants.LightNodeType));
        propertyValues.put("X3DScriptNode", new Integer(TypeConstants.ScriptNodeType));
        propertyValues.put("X3DSensorNode", new Integer(TypeConstants.SensorNodeType));
        propertyValues.put("X3DDeviceSensorNode", new Integer(TypeConstants.DeviceSensorNodeType));
        propertyValues.put("X3DEnvironmentalSensorNode", new Integer(TypeConstants.EnvironmentalSensorNodeType));
        propertyValues.put("X3DKeyDeviceSensorNode", new Integer(TypeConstants.KeyDeviceSensorNodeType));
        propertyValues.put("X3DNetworkSensorNode", new Integer(TypeConstants.LAST_NODE_TYPE_ID + 1));
        propertyValues.put("X3DPointingDeviceSensorNode", new Integer(TypeConstants.PointingDeviceSensorNodeType));
        propertyValues.put("X3DDragSensorNode", new Integer(TypeConstants.DragSensorNodeType));
        propertyValues.put("X3DTouchSensorNode", new Integer(TypeConstants.TouchSensorNodeType));
        propertyValues.put("X3DSequencerNode", new Integer(TypeConstants.LAST_NODE_TYPE_ID + 2));
        propertyValues.put("X3DTimeDependentNode", new Integer(TypeConstants.TimeDependentNodeType));
        propertyValues.put("X3DSoundNode", new Integer(TypeConstants.SoundNodeType));
        propertyValues.put("X3DTriggerNode", new Integer(TypeConstants.LAST_NODE_TYPE_ID + 3));
        propertyValues.put("X3DInfoNode", new Integer(TypeConstants.InfoNodeType));
    }

    /**
     * Construct an instance of this class.
     */
    public X3DConstants() {
        super("X3DConstants");
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        return propertyValues.containsKey(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = propertyValues.get(name);

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }
}
