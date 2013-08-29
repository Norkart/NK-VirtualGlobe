/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2005 - 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.vrml.scripting.external.sai;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.x3d.sai.X3DNodeTypes;

/** This is the horrible node type mapper used for mapping Xj3D's internal node
 *  type numbers to X3D's node type numbers.  Behold and tremble.
 *  
 * @author Bradley Vender
 *
 */
public class HorribleNodeTypeMapper {

    /** Convert from Xj3D's internal node type constants to X3D's
     *  node type constants.
     * @param xj3dTypeNumber
     * @return The X3D type constant
     */
    static final int getX3DTypeNumber(int xj3dTypeNumber) {
        switch (xj3dTypeNumber) {
            /** Note that these are organized alphabetically
             *  according to what the X3DNodeTypes constant is */
            case TypeConstants.AppearanceNodeType:
                return X3DNodeTypes.X3DAppearanceNode;
            case TypeConstants.BackgroundNodeType:
                return X3DNodeTypes.X3DBackgroundNode;
            case TypeConstants.BindableNodeType:
                return X3DNodeTypes.X3DBindableNode;
            case TypeConstants.BoundedNodeType:
                return X3DNodeTypes.X3DBoundedObject;
            case TypeConstants.ChildNodeType:
                return X3DNodeTypes.X3DChildNode;
            case TypeConstants.ColorNodeType:
                return X3DNodeTypes.X3DColorNode;
            case TypeConstants.CoordinateNodeType:
                return X3DNodeTypes.X3DCoordinateNode;
            case TypeConstants.DragSensorNodeType:
                return X3DNodeTypes.X3DDragSensorNode;
            case TypeConstants.EnvironmentalSensorNodeType:
                return X3DNodeTypes.X3DEnvironmentalSensorNode;
            case TypeConstants.FontStyleNodeType:
                return X3DNodeTypes.X3DFontStyleNode;
            case TypeConstants.GeometricPropertyNodeType:
                return X3DNodeTypes.X3DGeometricPropertyNode;
            case TypeConstants.GeometryNodeType:
                return X3DNodeTypes.X3DGeometryNode;
            case TypeConstants.GroupingNodeType:
                return X3DNodeTypes.X3DGroupingNode;
            case TypeConstants.InfoNodeType:
                return X3DNodeTypes.X3DInfoNode;
            case TypeConstants.InterpolatorNodeType:
                return X3DNodeTypes.X3DInterpolatorNode;
            case TypeConstants.KeyDeviceSensorNodeType:
                return X3DNodeTypes.X3DKeyDeviceSensorNode;
            case TypeConstants.LightNodeType:
                return X3DNodeTypes.X3DLightNode;
            case TypeConstants.MaterialNodeType:
                return X3DNodeTypes.X3DMaterialNode;
            case TypeConstants.NetworkInterfaceNodeType:
                return X3DNodeTypes.X3DNetworkSensorNode;
            case TypeConstants.NormalNodeType:
                return X3DNodeTypes.X3DNormalNode;
            case TypeConstants.ParametricGeometryNodeType:
                return X3DNodeTypes.X3DParametricGeometryNode;
            case TypeConstants.PointingDeviceSensorNodeType:
                return X3DNodeTypes.X3DPointingDeviceSensorNode;
            case TypeConstants.ProtoInstance:
                return X3DNodeTypes.X3DProtoInstance;
            case TypeConstants.ScriptNodeType:
                return X3DNodeTypes.X3DScriptNode;
            case TypeConstants.SensorNodeType:
                return X3DNodeTypes.X3DSensorNode;
            case TypeConstants.SequencerNodeType:
                return X3DNodeTypes.X3DSequencerNode;
            case TypeConstants.ShapeNodeType:
                return X3DNodeTypes.X3DShapeNode;
            case TypeConstants.SoundNodeType:
                return X3DNodeTypes.X3DSoundSourceNode;
            //case TypeConstants.Text:
            //	return X3DNodeTypes.X3DTextNode;
            case TypeConstants.TextureCoordinateNodeType:
                return X3DNodeTypes.X3DTextureCoordinateNode;
            case TypeConstants.TextureNodeType:
                return X3DNodeTypes.X3DTextureNode;
            case TypeConstants.TextureTransformNodeType:
                return X3DNodeTypes.X3DTextureTransformNode;
            //case TypeConstants.TextureTransform2DNode:
            //	return X3DNodeTypes.X3DTextureTransform2DNode;
            case TypeConstants.TimeDependentNodeType:
                return X3DNodeTypes.X3DTimeDependentNode;
            case TypeConstants.TouchSensorNodeType:
                return X3DNodeTypes.X3DTouchSensorNode;
            //case TypeConstants.TriggerNodeType:
            //	return X3DNodeTypes.X3DTriggerNode;
            //???
            //	return X3DNodeTypes.X3DURLObject;
            case TypeConstants.AppearanceChildNodeType:
                return X3DNodeTypes.X3DAppearanceChildNode;
            default:
                return -1;
                //throw new RuntimeException("Node type not mapped yet.");

        }
    }
    
}
