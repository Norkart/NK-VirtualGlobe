/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

import java.io.InputStream;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.web3d.x3d.sai.X3DNodeTypes;

import org.xml.sax.SAXException;

/**
 * A utility class for handling abstract node type functions, Including:
 * <ul>
 * <li>Associating abstract node type names with their enumerated type constants
 * and vice versa.</li>
 * <li>Obtaining the inherited abstract node types of both abstract nodes and
 * 'real' nodes.</li>
 * <li>Producing the set of enumerated type constants suitable for return from
 * the X3DNode.getNodeType() method.</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class X3DNodeTypeMapper {	
// AKA - The abominable node type mapper. Scourge of all who pass within.

    /** The config file for initializing the inheritance map */
    private static final String NODE_MAP_FILENAME = "config/3.2/nodemap.xml";
    
    /** The instance */
    private static X3DNodeTypeMapper instance;
    
    /** Node inheritance map, key = (String)nodeName, value = String[]{ parentNodeNames } */
    private Map<String,String[]> imap;
    
    /** Abstract type map, key = (String)typeName, value = (Integer)constant */
    private Map<String,Integer> typeMap;
    
    /** Protected Constructor */
    protected X3DNodeTypeMapper( ) {
        initializeIMap( );
        initializeTypeMap( );
    }
    
    /** 
     * Return the instance of the X3DNodeTypeMapper
     *
     * @return the instance of the X3DNodeTypeMapper
     */
    public static X3DNodeTypeMapper getInstance( ) {
        if ( instance == null ) {
            instance = new X3DNodeTypeMapper( );
        }
        return( instance );
    }
    
    /**
     * Return the array of interface names for the named argument node
     *
     * @param node_name The node for which to determine the interfaces
     * @return The array of interface names. If the named node does not inherit
     * from any known interfaces, an empty String array is returned. If the named
     * node is unknown, null is returned.
     */
    public String[] getInterfaces( String node_name ) {
        return( imap.get( node_name ) );
    }
    
    /**
     * Return the array of abstract node types for the named argument node
     *
     * @param node_name The node for which to determine the interfaces
     * @return The array of abstract node types. If the named node does not inherit
     * from any known interfaces, an empty array is returned. If the named
     * node is unknown, an array containing a single value of -1 is returned.
     */
    public int[] getInterfaceTypes( String node_name ) {
        int[] type = null;
        String[] absTypeName = imap.get( node_name );
        if ( absTypeName != null ) {
            int num = absTypeName.length;
            type = new int[num];
            for ( int i = 0; i < num; i++ ) {
                String typeName = absTypeName[i];
                int absType = getType( typeName );
                if ( absType == -1 ) {
                    System.out.println( "Warning: NodeTypeMap: "+ typeName +" is of unknown X3DNodeType" );
                }
                type[i] = absType;
            }
        }
        if ( type == null ) {
            type = new int[]{ -1 };
        }
        return( type );
    }
    
    /**
     * Return an unmodifiable interface map. The map key is the named node.
     * The map value is an array of interface names.
     *
     * @return The interface map.
     */
    public Map<String,String[]> getInterfaceMap( ) {
        return( imap );
    }
    
    /** 
     * Return the abstract node type name that cooresponds to the 
     * X3DNodeType constant.
     *
     * @param type The X3DNodeType constant
     * @return The abstract node type name. If the constant does not
     * coorespond to a known type, null is returned.
     */
    public String getTypeName( int type ) {
        String typeName = null;
        for ( Iterator<Map.Entry<String,Integer>> i = typeMap.entrySet( ).iterator( ); i.hasNext( ); ) {
            Map.Entry<String,Integer> e = i.next( );
            if ( type == e.getValue( ).intValue( ) ) {
                typeName = e.getKey( );
                break;
            }
        }
        return( typeName );
    }
    
    /** 
     * Return the X3DNodeType constant that cooresponds to the 
     * abstract node type name.
     *
     * @param typeName The abstract node type name.
     * @return the X3DNodeType constant that cooresponds to the 
     * named abstract node type. If the named node type is unknown,
     * -1 is returned.
     */
    public int getType( String typeName ) {
        int type = -1;
        Integer typeVal = typeMap.get( typeName );
        if ( typeVal != null ) {
            type = typeVal.intValue( );
        }
        return( type );
    }
    
    /**
     * Retrieve the configuration file containing the node inheritance 
     * data and initialize the local map with it.
     */
    private void initializeIMap( ) {
        
        InputStream is = AccessController.doPrivileged(
            new PrivilegedAction<InputStream>( ) {
                public InputStream run( ) {
                    return ClassLoader.getSystemResourceAsStream( NODE_MAP_FILENAME );
                }
            }
            );
        
        // Fallback mechanism for WebStart
        if( is == null ) {
            ClassLoader cl = X3DNodeTypeMapper.class.getClassLoader( );
            is = (InputStream)cl.getResourceAsStream( NODE_MAP_FILENAME );
        }
        
        Document nodemap_doc = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance( );
            fac.setValidating( false );
            fac.setNamespaceAware( false );
            DocumentBuilder builder = fac.newDocumentBuilder( );
            nodemap_doc = builder.parse( is );
        } catch( FactoryConfigurationError fce ) {
            //System.out.println( "X3DNodeTypeMapper: FactoryConfigurationError: "+ fce.getMessage( ) );
        } catch( ParserConfigurationException pce ) {
            //System.out.println( "X3DNodeTypeMapper: ParserConfigurationException: "+ pce.getMessage( ) );
        } catch( SAXException se ) {
            //System.out.println( "X3DNodeTypeMapper: SAXException: "+ se.getMessage( ) );
        } catch( IOException ioe ) {
            //System.out.println( "X3DNodeTypeMapper: IOException: "+ ioe.getMessage( ) );
        }
        
        if ( nodemap_doc == null ) {
            System.out.println( "X3DNodeTypeMapper: abstract type inheritance mapping is unavailable" );
            imap = Collections.unmodifiableMap(new HashMap<String,String[]>(0));
        } else {
            Element root_element = nodemap_doc.getDocumentElement( );
            
            NodeList node_list = root_element.getElementsByTagName( "node" );
            int num_nodes = node_list.getLength( );
            Map<String,String[]> intf_map = new LinkedHashMap<String,String[]>( num_nodes );
            
            for ( int i = 0; i < num_nodes; i++ ) {
                Element node_element = (Element)node_list.item( i );
                String node_name = node_element.getAttribute( "name" );
                NodeList parent_list = node_element.getElementsByTagName( "parent" );
                int num_parents = parent_list.getLength( );
                String[] parent = new String[num_parents];
                for ( int j = 0; j < num_parents; j++ ) {
                    Element parent_element = (Element)parent_list.item( j );
                    parent[j] = parent_element.getAttribute( "name" );
                }
                intf_map.put( node_name, parent );
            }
            imap = Collections.unmodifiableMap(intf_map);
        }
    }
    
    /**
     * Setup the map of type names to integer constant values. 
     * This is done dynamically rather than statically as this
     * is only needed when Java SAI is being used.
     */
    private void initializeTypeMap( ) {
        typeMap = new HashMap<String,Integer>( );
        
        typeMap.put( "X3DBoundedObject", new Integer( X3DNodeTypes.X3DBoundedObject ) );
        typeMap.put( "X3DMetadataObject", new Integer( X3DNodeTypes.X3DMetadataObject ) );
        typeMap.put( "X3DUrlObject", new Integer( X3DNodeTypes.X3DUrlObject ) );
        typeMap.put( "X3DAppearanceNode", new Integer( X3DNodeTypes.X3DAppearanceNode ) );
        typeMap.put( "X3DAppearanceChildNode", new Integer( X3DNodeTypes.X3DAppearanceChildNode ) );
        typeMap.put( "X3DMaterialNode", new Integer( X3DNodeTypes.X3DMaterialNode ) );
        typeMap.put( "X3DTextureNode", new Integer( X3DNodeTypes.X3DTextureNode ) );
        typeMap.put( "X3DTexture2DNode", new Integer( X3DNodeTypes.X3DTexture2DNode ) );
        typeMap.put( "X3DTextureTransformNode", new Integer( X3DNodeTypes.X3DTextureTransformNode ) );
        typeMap.put( "X3DTextureTransform2DNode", new Integer( X3DNodeTypes.X3DTextureTransform2DNode ) );
        typeMap.put( "X3DGeometryNode", new Integer( X3DNodeTypes.X3DGeometryNode ) );
        typeMap.put( "X3DGeometricPropertyNode", new Integer( X3DNodeTypes.X3DGeometricPropertyNode ) );
        typeMap.put( "X3DParametricGeometryNode", new Integer( X3DNodeTypes.X3DParametricGeometryNode ) );
        typeMap.put( "X3DNurbsSurfaceGeometryNode", new Integer( X3DNodeTypes.X3DNurbsSurfaceGeometryNode ) );
        typeMap.put( "X3DColorNode", new Integer( X3DNodeTypes.X3DColorNode ) );
        typeMap.put( "X3DCoordinateNode", new Integer( X3DNodeTypes.X3DCoordinateNode ) );
        typeMap.put( "X3DNormalNode", new Integer( X3DNodeTypes.X3DNormalNode ) );
        typeMap.put( "X3DTextureCoordinateNode", new Integer( X3DNodeTypes.X3DTextureCoordinateNode ) );
        typeMap.put( "X3DFontStyleNode", new Integer( X3DNodeTypes.X3DFontStyleNode ) );
        typeMap.put( "X3DProtoInstance", new Integer( X3DNodeTypes.X3DProtoInstance ) );
        typeMap.put( "X3DChildNode", new Integer( X3DNodeTypes.X3DChildNode ) );
        typeMap.put( "X3DBindableNode", new Integer( X3DNodeTypes.X3DBindableNode ) );
        typeMap.put( "X3DBackgroundNode", new Integer( X3DNodeTypes.X3DBackgroundNode ) );
        typeMap.put( "X3DGroupingNode", new Integer( X3DNodeTypes.X3DGroupingNode ) );
        typeMap.put( "X3DShapeNode", new Integer( X3DNodeTypes.X3DShapeNode ) );
        typeMap.put( "X3DInterpolatorNode", new Integer( X3DNodeTypes.X3DInterpolatorNode ) );
        typeMap.put( "X3DLightNode", new Integer( X3DNodeTypes.X3DLightNode ) );
        typeMap.put( "X3DScriptNode", new Integer( X3DNodeTypes.X3DScriptNode ) );
        typeMap.put( "X3DSensorNode", new Integer( X3DNodeTypes.X3DSensorNode ) );
        typeMap.put( "X3DEnvironmentalSensorNode", new Integer( X3DNodeTypes.X3DEnvironmentalSensorNode ) );
        typeMap.put( "X3DKeyDeviceSensorNode", new Integer( X3DNodeTypes.X3DKeyDeviceSensorNode ) );
        typeMap.put( "X3DNetworkSensorNode", new Integer( X3DNodeTypes.X3DNetworkSensorNode ) );
        typeMap.put( "X3DPointingDeviceSensorNode", new Integer( X3DNodeTypes.X3DPointingDeviceSensorNode ) );
        typeMap.put( "X3DDragSensorNode", new Integer( X3DNodeTypes.X3DDragSensorNode ) );
        typeMap.put( "X3DTouchSensorNode", new Integer( X3DNodeTypes.X3DTouchSensorNode ) );
        typeMap.put( "X3DSequencerNode", new Integer( X3DNodeTypes.X3DSequencerNode ) );
        typeMap.put( "X3DTimeDependentNode", new Integer( X3DNodeTypes.X3DTimeDependentNode ) );
        typeMap.put( "X3DSoundSourceNode", new Integer( X3DNodeTypes.X3DSoundSourceNode ) );
        typeMap.put( "X3DTriggerNode", new Integer( X3DNodeTypes.X3DTriggerNode ) );
        typeMap.put( "X3DInfoNode", new Integer( X3DNodeTypes.X3DInfoNode ) );
        typeMap.put( "X3DNurbsControlCurveNode", new Integer( X3DNodeTypes.X3DNurbsControlCurveNode ) );
        
        // begin of 'undefined', but known types
        
        typeMap.put( "X3DChaserNode", new Integer( UndefinedX3DNodeTypes.X3DChaserNode ) );
        typeMap.put( "X3DComposedGeometryNode", new Integer( UndefinedX3DNodeTypes.X3DComposedGeometryNode ) );
        typeMap.put( "X3DDamperNode", new Integer( UndefinedX3DNodeTypes.X3DDamperNode ) );
        typeMap.put( "X3DEnvironmentTextureNode", new Integer( UndefinedX3DNodeTypes.X3DEnvironmentTextureNode ) );
        typeMap.put( "X3DFogObject", new Integer( UndefinedX3DNodeTypes.X3DFogObject ) );
        typeMap.put( "X3DFollowerNode", new Integer( UndefinedX3DNodeTypes.X3DFollowerNode ) );
        typeMap.put( "X3DLayerNode", new Integer( UndefinedX3DNodeTypes.X3DLayerNode ) );
        typeMap.put( "X3DLayoutNode", new Integer( UndefinedX3DNodeTypes.X3DLayoutNode ) );
        typeMap.put( "X3DNBodyCollidableNode", new Integer( UndefinedX3DNodeTypes.X3DNBodyCollidableNode ) );
        typeMap.put( "X3DNBodyCollisionSpaceNode", new Integer( UndefinedX3DNodeTypes.X3DNBodyCollisionSpaceNode ) );
        typeMap.put( "X3DNode", new Integer( UndefinedX3DNodeTypes.X3DNode ) );
        typeMap.put( "X3DParticleEmitterNode", new Integer( UndefinedX3DNodeTypes.X3DParticleEmitterNode ) );
        typeMap.put( "X3DParticlePhysicsModelNode", new Integer( UndefinedX3DNodeTypes.X3DParticlePhysicsModelNode ) );
        typeMap.put( "X3DPickableObject", new Integer( UndefinedX3DNodeTypes.X3DPickableObject ) );
        typeMap.put( "X3DPickingNode", new Integer( UndefinedX3DNodeTypes.X3DPickingNode ) );
        typeMap.put( "X3DProductStructureChildNode", new Integer( UndefinedX3DNodeTypes.X3DProductStructureChildNode ) );
        typeMap.put( "X3DProgrammableShaderObject", new Integer( UndefinedX3DNodeTypes.X3DProgrammableShaderObject ) );
        typeMap.put( "X3DRigidJointNode", new Integer( UndefinedX3DNodeTypes.X3DRigidJointNode ) );
        typeMap.put( "X3DShaderNode", new Integer( UndefinedX3DNodeTypes.X3DShaderNode ) );
        typeMap.put( "X3DSoundNode", new Integer( UndefinedX3DNodeTypes.X3DSoundNode ) );
        typeMap.put( "X3DTexture3DNode", new Integer( UndefinedX3DNodeTypes.X3DTexture3DNode ) );
        typeMap.put( "X3DVertexAttributeNode", new Integer( UndefinedX3DNodeTypes.X3DVertexAttributeNode ) );
        typeMap.put( "X3DViewpointNode", new Integer( UndefinedX3DNodeTypes.X3DViewpointNode ) );
        typeMap.put( "X3DViewportNode", new Integer( UndefinedX3DNodeTypes.X3DViewportNode ) );
    }
    
    /**
     * Test routine to echo out the nodes and inherited types in "spec" format.
     * Will complain if an unknown abstract type is encountered.
     */
    public static void main( String[] arg ) {
        X3DNodeTypeMapper ntm = X3DNodeTypeMapper.getInstance( );
        
        Map<String,String[]> map = ntm.getInterfaceMap( );
        for ( Iterator<Map.Entry<String,String[]>> i = map.entrySet( ).iterator( ); i.hasNext( ); ) {
            Map.Entry<String,String[]> e = i.next( );
            String base = e.getKey( );
            String[] sub = e.getValue( );
            StringBuffer sb = new StringBuffer( base +" : " );
            for ( int j = 0; j < sub.length; j++ ) {
                sb.append( sub[j] );
                if ( j < sub.length-1 ) {
                    sb.append( ", " );
                }
            }
            System.out.println( sb.toString( ) );
            ntm.getInterfaceTypes( base );
        }
    }
}
