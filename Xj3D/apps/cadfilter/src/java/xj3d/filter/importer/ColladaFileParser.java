/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import java.io.IOException;
import java.text.NumberFormat;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Local imports
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.StringArray;
import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.FieldValueHandler;
import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser that reads Collada files and generates an X3D stream
 * of events.
 *
 * @author Rex Melton
 * @version $Revision: 1.15 $
 */
public class ColladaFileParser implements NonWeb3DFileParser {
    // Should the scale be applied directly the coordinates and translations
    // Will not change MATRIX specified forms
    private boolean APPLY_SCALE = false;
    
    /** The 'supported' set of node content */
    private static final String[] INSTANCE = new String[] {
        ColladaStrings.INSTANCE_CAMERA,
        //ColladaStrings.INSTANCE_CONTROLLER,
        ColladaStrings.INSTANCE_GEOMETRY,
        //ColladaStrings.INSTANCE_LIGHT,
        ColladaStrings.INSTANCE_NODE,
        ColladaStrings.NODE,
    };
    
    /** The 'supported' set of node transforms */
    private static final String[] TRANSFORM = new String[] {
        ColladaStrings.TRANSLATE,
        ColladaStrings.ROTATE,
        ColladaStrings.SCALE,
        ColladaStrings.SKEW,
        ColladaStrings.MATRIX,
    };
    
    /** The 'supported' set of materials */
    private static final String[] MATERIAL = new String[] {
        ColladaStrings.BLINN,
        ColladaStrings.CONSTANT,
        ColladaStrings.LAMBERT,
        ColladaStrings.PHONG,
    };
    
    /** The DOM representation of the Collada File */
    private Document doc;
    
    /** The Document Element */
    private Element doc_element;
    
    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, rather than a StringContentHandler */
    private boolean handlerIsBinary;
    
    /** Binary Content Handler reference */
    private BinaryContentHandler bch;
    
    /** String Content Handler reference */
    private StringContentHandler sch;
    
    /** The url of the current document */
    private String documentURL;
    
    /** Reference to the registered content handler if we have one */
    private ContentHandler contentHandler;
    
    /** Reference to the registered route handler if we have one */
    private RouteHandler routeHandler;
    
    /** Reference to the registered script handler if we have one */
    private ScriptHandler scriptHandler;
    
    /** Reference to the registered proto handler if we have one */
    private ProtoHandler protoHandler;
    
    /** Reference to the registered error handler if we have one */
    private ErrorReporter errorHandler;
    
    /** Reference to our Locator instance to hand to users */
    private Locator locator;
    
    /** Scratch vecmath objects used to calculate Transform fields */
    private Matrix4f tmpMatrix0;
    private AxisAngle4f tmpAxisAngle;
    private Vector3f tmpVector;
    
    // Global scale for units conversion
    private float scale = 1.0f;
    
    /** The node IDs that have been DEF'ed */
    private ArrayList<String> nodeInstanceList;
    
    /** The source IDs that have been DEF'ed */
    private ArrayList<String> sourceInstanceList;
    
    /** The material IDs that have been DEF'ed */
    private ArrayList<String> materialInstanceList;
    
    /** The tranform field IDs that have been DEF'ed */
    private HashMap<String, String> defedFieldMap;
    
    /** Used to format numbers for printing in X3D fields */
    private NumberFormat numberFormater;
    
    /**
     * Constructor
     */
    public ColladaFileParser() {
        tmpMatrix0 = new Matrix4f();
        tmpAxisAngle = new AxisAngle4f();
        tmpVector = new Vector3f();
        
        nodeInstanceList = new ArrayList<String>();
        sourceInstanceList = new ArrayList<String>();
        materialInstanceList = new ArrayList<String>();
        defedFieldMap = new HashMap<String, String>();
        
        numberFormater = NumberFormat.getNumberInstance();
        numberFormater.setGroupingUsed(false);
    }
    
    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    public void initialize() {
        // Ignored for this implementation.
    }
    
    /**
     * Set the base URL of the document that is about to be parsed. Users
     * should always call this to make sure we have correct behaviour for the
     * ContentHandler's <code>startDocument()</code> call.
     * <p>
     * The URL is cleared at the end of each document run. Therefore it is
     * imperative that it get's called each time you use the parser.
     *
     * @param url The document url to set
     */
    public void setDocumentUrl(String url) {
        documentURL = url;
    }
    
    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * {@link #Scene()} but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    public Locator getDocumentLocator() {
        return locator;
    }
    
    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    public void setContentHandler(ContentHandler ch) {
        contentHandler = ch;
        if (contentHandler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)contentHandler;
            sch = null;
            handlerIsBinary = true;
        } else if (contentHandler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)contentHandler;
            handlerIsBinary = false;
        }
        // otherwise - we don't know how to deal with the content handler
    }
    
    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
    }
    
    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
    }
    
    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
    }
    
    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorHandler(ErrorHandler eh) {
        errorHandler = eh;
        
        if(eh != null)
            eh.setDocumentLocator(getDocumentLocator());
    }
    
    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh) {
        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorHandler = eh;
    }
    
    /**
     * Parse the input now.
     *
     * @param input The stream to read from
     * @throws IOException An I/O error while reading the stream
     * @throws ImportFileFormatException A parsing error occurred in the file
     */
    public void parse(InputSource input)
        throws IOException, ImportFileFormatException {
        
        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some to deal with this.
        URL url = new URL(input.getURL());
        
        doc = ImportUtils.getDocument(url);
        doc_element = doc.getDocumentElement();
        String docTagName = doc_element.getTagName();
        if (!docTagName.equals(ColladaStrings.COLLADA)) {
            throw new ImportFileFormatException("ColladaFileParser: "+ url +" is not a Collada file");
        }
        
        contentHandler.startDocument(input.getURL(),
            input.getBaseURL(),
            "utf8",
            "#X3D",
            "V3.2",
            "Collada file conversion");
        
        contentHandler.profileDecl("Interchange");
        
        contentHandler.startNode("Transform", "COLLADA_UNITS");
        
        // process asset information that affects the scene
        NodeList nl = doc_element.getElementsByTagName(ColladaStrings.ASSET);
        Element asset_element = (Element)nl.item(0);
        nl = asset_element.getElementsByTagName(ColladaStrings.UNIT);
        if (nl.getLength() > 0) {
            // process the global scale setting
            Element unit_element = (Element)nl.item(0);
            String meter = unit_element.getAttribute(ColladaStrings.METER);
            try {
                scale = Float.parseFloat(meter);
            } catch (NumberFormatException nfe) {
                //
            }
            if (scale != 1 && !APPLY_SCALE) {
                contentHandler.startField("scale");
                if (handlerIsBinary) {
                    bch.fieldValue(new float[]{ scale, scale, scale }, 3);
                } else {
                    String scale_string = Float.toString(scale);
                    sch.fieldValue(scale_string +" "+ scale_string +" "+ scale_string);
                }
            }
        }
        nl = asset_element.getElementsByTagName(ColladaStrings.UP_AXIS);
        if (nl.getLength() > 0) {
            // process the global orientation
            Element axis_element = (Element)nl.item(0);
            String up_axis = axis_element.getTextContent();
            if (up_axis.equals(ColladaStrings.X_UP)) {
                
                contentHandler.startField("rotation");
                if (handlerIsBinary) {
                    bch.fieldValue(new float[]{ 0, 0, 1, 1.570796f }, 4);
                } else {
                    sch.fieldValue("0 0 1 1.570796");
                }
                
            } else if (up_axis.equals(ColladaStrings.Z_UP)) {
                
                contentHandler.startField("rotation");
                if (handlerIsBinary) {
                    bch.fieldValue(new float[]{ -1, 0, 0, 1.570796f }, 4);
                } else {
                    sch.fieldValue("-1 0 0 1.570796");
                }
                
            } // else - do nothing if Y_UP
        }
        
        contentHandler.startField("children");
        
        // get the nodes from the scene and process them.
        ArrayList<Element> rootNodes = getColladaNodesFromScene();
        if (rootNodes != null) {
            for (int i = 0; i < rootNodes.size(); i++) {
                processNode((Element)rootNodes.get(i));
            }
        }
        
        contentHandler.endField();  // children
        contentHandler.endNode();   // Transform
        
        ////////////////////////////////////////////////////////////////////////////////
        ArrayList<Element> animation_lib_list =
            ImportUtils.getElementsByTagName(doc_element, ColladaStrings.LIBRARY_ANIMATIONS);
        if (animation_lib_list.size() > 0) {
            Element animation_lib_element = animation_lib_list.get(0);
            
            ArrayList<Element> animation_element_list =
                ImportUtils.getElementsByTagName(animation_lib_element, ColladaStrings.ANIMATION);
            for (int i = 0; i < animation_element_list.size(); i++) {
                processAnimation(animation_element_list.get(i));
            }
        }
        ////////////////////////////////////////////////////////////////////////////////
        contentHandler.endDocument();
        
    }
    
    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------
    
    /**
     * Return the Collada Nodes from the Scene in the argument Document.
     *
     * @return the Collada Nodes from the Document, or null if they could not
     * be found for any reason.
     */
    private ArrayList<Element> getColladaNodesFromScene() {
        ArrayList<Element> root_nodes = null;
        String doc_tag = doc_element.getTagName();
        if (!doc_tag.equals(ColladaStrings.COLLADA)) {
            // not a Collada file
            return(null);
        }
        NodeList nl = doc_element.getElementsByTagName(ColladaStrings.SCENE);
        Element scene_element = null;
        if (nl.getLength() > 0) {
            scene_element = (Element)nl.item(0);
        }
        if (scene_element != null) {
            Element ivs_element = null;
            nl = scene_element.getElementsByTagName(ColladaStrings.INSTANCE_VISUAL_SCENE);
            if (nl.getLength() > 0) {
                ivs_element = (Element)nl.item(0);
            }
            if (ivs_element != null) {
                String vs_url = ivs_element.getAttribute(ColladaStrings.URL);
                String vs_url_id = getElementId(vs_url);
                // find the visual scene, identified by it's url
                Element vs_element =
                    getResourceElement(ColladaStrings.VISUAL_SCENE, vs_url_id);
                if (vs_element != null) {
                    root_nodes = ImportUtils.getElementsByTagName(vs_element, ColladaStrings.NODE);
                }
            }
        }
        return(root_nodes);
    }
    
    /**
     * Create the X3D for the argument Collada Node
     *
     * @param node The Collada node element
     */
    private void processNode(Element node) {
        
        ArrayList<Element> instance_list = ImportUtils.getElements(node);
        cull(instance_list, INSTANCE);
        int num_instance = instance_list.size();
        if (num_instance >= 0) {
            
            String id = ImportUtils.getAttribute(node, ColladaStrings.ID);
            nodeInstanceList.add(id);
            
            // the node contains supported instance_*, or node(s), get the
            // the transform elements and set up the grouping node(s).
            ArrayList<Element> transform_list = ImportUtils.getElements(node);
            cull(transform_list, TRANSFORM);
            
            contentHandler.startNode("Transform", id);
            contentHandler.startField("children");
            if ( transform_list.size() != 0 ) {
                processTransformElements(transform_list, id);
            }
            
            // add each of the instance elements
            for (int i = 0; i < num_instance; i++) {
                Element element = (Element)instance_list.get(i);
                String tagName = element.getTagName();
                if (tagName.equals(ColladaStrings.INSTANCE_CAMERA)) {
                    processCameraInstance(element);
                }
                if (tagName.equals(ColladaStrings.INSTANCE_GEOMETRY)) {
                    processGeometryInstance(element);
                }
                if (tagName.equals(ColladaStrings.INSTANCE_NODE)) {
                    processNodeInstance(element);
                }
                if (tagName.equals(ColladaStrings.NODE)) {
                    processNode(element);
                }
            }
            
            for (int i = 0; i < transform_list.size(); i++) {
                // terminate the transform element hierarchy
                contentHandler.endField();  // children
                contentHandler.endNode();   // Transform
            }
            
            // fini, terminate the grouping node
            contentHandler.endField();  // children
            contentHandler.endNode();   // Transform
        }
    }
    
    /**
     * Add a node for the specified instance
     *
     * @param node A Collada node_instance
     */
    private void processNodeInstance(Element node) {
        String url = node.getAttribute(ColladaStrings.URL);
        String url_id = getElementId(url);
        if (nodeInstanceList.contains(url_id)) {
            // an instance of this node already exists, USE it
            contentHandler.startNode("Transform", null);
            contentHandler.useDecl(url_id);
            contentHandler.endNode();   // Transform
        } else {
            // otherwise, find the node resource, identified by it's url
            // and DEF it
            Element node_element =
                getResourceElement(ColladaStrings.NODE, url_id);
            if (node_element != null) {
                processNode(node_element);
            }
        }
    }
    
    /**
     * Create the X3D Transform node's transformational fields.
     *
     * @param transform_list The list of transformational elements from the Collada node.
     */
    private void processTransformElements(ArrayList<Element> transform_list, String node_id) {
        int num_transforms = transform_list.size();
        if (num_transforms != 0) {
            TransformElement[] te = TransformUtils.getTransformElements(transform_list);
            for (int i = 0; i < num_transforms; i++) {
                TransformElement t = te[i];
                String defName = t.sid;
                String fieldName = t.x3d_field_name;
                if ((fieldName != null) && (defName != null)) {
                    defName = node_id +"/"+ defName;
                    defedFieldMap.put(defName, fieldName);
                }
                contentHandler.startNode("Transform", defName);
                
                if (fieldName != null) {
                    if (fieldName.equals("translation") && APPLY_SCALE) {
                        if (handlerIsBinary) {
                            t.value[0] =  t.value[0] * scale;
                            t.value[1] =  t.value[1] * scale;
                            t.value[2] =  t.value[2] * scale;
                        } else {
                            StringBuffer sb = new StringBuffer();
                            for(int j=0; j < t.value.length; j++) {
                                sb.append(numberFormater.format(t.value[j]));
                                sb.append(" ");
                            }
                            
                            t.content = new String[] {sb.toString()};
                        }
                    }
                    
                    // for transform elements that have a direct x3d match,
                    // fill in the field value
                    contentHandler.startField(fieldName);
                    if (handlerIsBinary) {
                        bch.fieldValue(t.value, t.value.length);
                    } else {
                        sch.fieldValue(t.content);
                    }
                    
                } else {
                    // for transform elements that don't have a direct x3d match
                    // i.e. matrix, skew, lookat - extract values from their matrix
                    //////////////////////////////////////////////////////////////////////////////
                    // extract the rotation
                    t.getMatrix(tmpMatrix0);
                    tmpAxisAngle.set(tmpMatrix0);
                    if (tmpAxisAngle.angle != 0) {
                        contentHandler.startField("rotation");
                        float[] value = new float[4];
                        tmpAxisAngle.get(value);
                        if (handlerIsBinary) {
                            bch.fieldValue(value, 4);
                        } else {
                            sch.fieldValue(value[0] +" "+ value[1] +" "+ value[2] +" "+ value[3]);
                        }
                    }
                    //////////////////////////////////////////////////////////////////////////////
                    // extract the translation
                    tmpMatrix0.get(tmpVector);
                    float[] value = new float[3];
                    tmpVector.get(value);
                    contentHandler.startField("translation");
                    if (handlerIsBinary) {
                        bch.fieldValue(value, 3);
                    } else {
                        sch.fieldValue(value[0] +" "+ value[1] +" "+ value[2]);
                    }
                    //////////////////////////////////////////////////////////////////////////////
                }
                contentHandler.startField("children");
            }
        }
    }
    
    /**
     * Add Shape nodes for the specified geometry to the content handler
     *
     * @param node A Collada geometry_instance
     */
    private void processGeometryInstance(Element node) {
        
        Element bind_material_element = null;
        NodeList nl = node.getElementsByTagName(ColladaStrings.BIND_MATERIAL);
        if (nl.getLength() > 0) {
            bind_material_element = (Element)nl.item(0);
        }
        BindMaterial materialSource = new BindMaterial(bind_material_element);
        
        String url = node.getAttribute(ColladaStrings.URL);
        String url_id = getElementId(url);
        
        // find the geometry resource, identified by it's url
        Element geometry_element =
            getResourceElement(ColladaStrings.GEOMETRY, url_id);
        if (geometry_element != null) {
            nl = geometry_element.getElementsByTagName(ColladaStrings.MESH);
            if (nl.getLength() > 0) {
                // there may only be one mesh per geometry.
                Element mesh_element = (Element)nl.item(0);
                NodeList source_element_list = mesh_element.getElementsByTagName(ColladaStrings.SOURCE);
                Map<String, Source> sourceMap = Source.getSourceMap(source_element_list);
                NodeList vertices_element_list = mesh_element.getElementsByTagName(ColladaStrings.VERTICES);
                Element vertices_element = (Element)vertices_element_list.item(0);
                
                NodeList triangle_list =
                    mesh_element.getElementsByTagName(ColladaStrings.TRIANGLES);
                if (triangle_list.getLength() > 0) {
                    processTriangles(triangle_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList trifans_list =
                    mesh_element.getElementsByTagName(ColladaStrings.TRIFANS);
                if (trifans_list.getLength() > 0) {
                    processTrifans(trifans_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList tristrips_list =
                    mesh_element.getElementsByTagName(ColladaStrings.TRISTRIPS);
                if (tristrips_list.getLength() > 0) {
                    processTristrips(tristrips_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList lines_list =
                    mesh_element.getElementsByTagName(ColladaStrings.LINES);
                if (lines_list.getLength() > 0) {
                    processLines(lines_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList linestrips_list =
                    mesh_element.getElementsByTagName(ColladaStrings.LINESTRIPS);
                if (linestrips_list.getLength() > 0) {
                    processLinestrips(linestrips_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList polylist_list =
                    mesh_element.getElementsByTagName(ColladaStrings.POLYLIST);
                if (polylist_list.getLength() > 0) {
                    processPolylist(polylist_list, sourceMap, vertices_element, materialSource);
                }
                
                NodeList polygons_list =
                    mesh_element.getElementsByTagName(ColladaStrings.POLYGONS);
                if (polygons_list.getLength() > 0) {
                    processPolygons(polygons_list, sourceMap, vertices_element, materialSource);
                }
            }
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-triangles
     *
     * @param triangle_list A NodeList containing the triangles Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTriangles(NodeList triangle_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < triangle_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element triangle_element = (Element)triangle_list.item(i);
            int num_triangles = Integer.parseInt(
                triangle_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                triangle_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            boolean need_another_index = false;
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            
            normal_input = null;
            
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            texCoord_input = null;
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            
            NodeList p_list = triangle_element.getElementsByTagName(ColladaStrings.P);
            Element p_element = (Element)p_list.item(0);
            P indexer = new P(p_element);
            
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            
            if (need_another_index) {
                // there is not a single index - switch to an IndexedFaceSet
                
                Object v_index = null;
                if (handlerIsBinary) {
                    int[] vertex_indices = (int[])indexer.getPolyIndices(
                        num_triangles, vertex_offset, num_offsets, true);
                    v_index = vertex_indices;
                } else {
                    String[] vertex_indices = (String[])indexer.getPolyIndices(
                        num_triangles, vertex_offset, num_offsets, false);
                    v_index = vertex_indices;
                }
                
                Object n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int[] normal_indices = (int[])indexer.getPolyIndices(
                                num_triangles, normal_offset, num_offsets, true);
                            n_index = normal_indices;
                        } else {
                            String[] normal_indices = (String[])indexer.getPolyIndices(
                                num_triangles, normal_offset, num_offsets, false);
                            n_index = normal_indices;
                        }
                    }
                }
                
                Object tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int[] texCoord_indices = (int[])indexer.getPolyIndices(
                                num_triangles, texCoord_offset, num_offsets, true);
                            tc_index = texCoord_indices;
                        } else {
                            String[] texCoord_indices = (String[])indexer.getPolyIndices(
                                num_triangles, texCoord_offset, num_offsets, false);
                            tc_index = texCoord_indices;
                        }
                    }
                }
                
                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index);
                
            } else {
                contentHandler.startField("geometry");
                contentHandler.startNode("IndexedTriangleSet", null);
                contentHandler.startField("index");
                if (handlerIsBinary) {
                    int[] vertex_indices = (int[])indexer.getTrianglesIndices(
                        num_triangles, vertex_offset, num_offsets, true);
                    bch.fieldValue(vertex_indices, vertex_indices.length);
                } else {
                    String[] vertex_indices = (String[])indexer.getTrianglesIndices(
                        num_triangles, vertex_offset, num_offsets, false);
                    sch.fieldValue(vertex_indices);
                }
                
                buildCoordField(vertexSource);
                
                if ( normalSource != null ) {
                    buildNormalField(normalSource);
                }
                
                if ( texCoordSource != null ) {
                    buildTexCoordField(texCoordSource);
                }
                
                contentHandler.endNode();   // IndexedTriangleSet
                contentHandler.endField();  // geometry
            }
            
            String material = triangle_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-trifans
     *
     * @param trifans_list A NodeList containing the trifans Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTrifans(NodeList trifans_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < trifans_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element trifans_element = (Element)trifans_list.item(i);
            int num_trifans = Integer.parseInt(
                trifans_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                trifans_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            boolean need_another_index = false;
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            
            NodeList p_list = trifans_element.getElementsByTagName(ColladaStrings.P);
            P[] indexer = new P[num_trifans];
            for (int j = 0; j < num_trifans; j++) {
                Element p_element = (Element)p_list.item(j);
                indexer[j] = new P(p_element);
            }
            
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            ///////////////////////////////////////////////////////////////////////////
            // xj3d's indexed trifan handling seems problematic with some test cases
            // forcing to index face sets for now.
            //if (need_another_index) {
            if (true) {
                ///////////////////////////////////////////////////////////////////////////
                // there is not a single index - switch to an IndexedFaceSet
                
                Object v_index = null;
                if (handlerIsBinary) {
                    int num_indices = 0;
                    int[][] poly_indices = new int[num_trifans][];
                    for (int j = 0; j < num_trifans; j++) {
                        poly_indices[j] = (int[])indexer[j].getPolyIndicesForTrifan(
                            vertex_offset, num_offsets, true);
                        num_indices += poly_indices[j].length;
                    }
                    v_index = FieldValueHandler.flatten(poly_indices, num_indices);
                } else {
                    int num_indices = 0;
                    String[][] poly_indices = new String[num_trifans][];
                    for (int j = 0; j < num_trifans; j++) {
                        poly_indices[j] = (String[])indexer[j].getPolyIndicesForTrifan(
                            vertex_offset, num_offsets, false);
                        num_indices += poly_indices[j].length;
                    }
                    v_index = FieldValueHandler.flatten(poly_indices, num_indices);
                }
                
                Object n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int num_indices = 0;
                            int[][] poly_indices = new int[num_trifans][];
                            for (int j = 0; j < num_trifans; j++) {
                                poly_indices[j] = (int[])indexer[j].getPolyIndicesForTrifan(
                                    normal_offset, num_offsets, true);
                                num_indices += poly_indices[j].length;
                            }
                            n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        } else {
                            int num_indices = 0;
                            String[][] poly_indices = new String[num_trifans][];
                            for (int j = 0; j < num_trifans; j++) {
                                poly_indices[j] = (String[])indexer[j].getPolyIndicesForTrifan(
                                    normal_offset, num_offsets, false);
                                num_indices += poly_indices[j].length;
                            }
                            n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        }
                    }
                }
                
                Object tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int num_indices = 0;
                            int[][] poly_indices = new int[num_trifans][];
                            for (int j = 0; j < num_trifans; j++) {
                                poly_indices[j] = (int[])indexer[j].getPolyIndicesForTrifan(
                                    texCoord_offset, num_offsets, true);
                                num_indices += poly_indices[j].length;
                            }
                            tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        } else {
                            int num_indices = 0;
                            String[][] poly_indices = new String[num_trifans][];
                            for (int j = 0; j < num_trifans; j++) {
                                poly_indices[j] = (String[])indexer[j].getPolyIndicesForTrifan(
                                    texCoord_offset, num_offsets, false);
                                num_indices += poly_indices[j].length;
                            }
                            tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        }
                    }
                }
                
                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index);
                
            } else {
                contentHandler.startField("geometry");
                contentHandler.startNode("IndexedTriangleFanSet", null);
                contentHandler.startField("index");
                if (handlerIsBinary) {
                    int num_indices = 0;
                    int[][] trifans_indices = new int[num_trifans][];
                    for (int j = 0; j < num_trifans; j++) {
                        trifans_indices[j] = (int[])indexer[j].getIndices(
                            vertex_offset, num_offsets, true);
                        num_indices += trifans_indices[j].length;
                    }
                    int[] vertex_indices = FieldValueHandler.flatten(trifans_indices, num_indices);
                    bch.fieldValue(vertex_indices, vertex_indices.length);
                } else {
                    int num_indices = 0;
                    String[][] trifans_indices = new String[num_trifans][];
                    for (int j = 0; j < num_trifans; j++) {
                        trifans_indices[j] = (String[])indexer[j].getIndices(
                            vertex_offset, num_offsets, false);
                        num_indices += trifans_indices[j].length;
                    }
                    String[] vertex_indices = FieldValueHandler.flatten(trifans_indices, num_indices);
                    sch.fieldValue(vertex_indices);
                }
                
                buildCoordField(vertexSource);
                
                if ( normalSource != null ) {
                    buildNormalField(normalSource);
                }
                
                if ( texCoordSource != null ) {
                    buildTexCoordField(texCoordSource);
                }
                
                contentHandler.endNode();   // IndexedTriangleFanSet
                contentHandler.endField();  // geometry
            }
            
            String material = trifans_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-tristrips
     *
     * @param tristrips_list A NodeList containing the tristrips Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTristrips(NodeList tristrips_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < tristrips_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element tristrips_element = (Element)tristrips_list.item(i);
            int num_tristrips = Integer.parseInt(
                tristrips_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                tristrips_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            boolean need_another_index = false;
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            
            NodeList p_list = tristrips_element.getElementsByTagName(ColladaStrings.P);
            P[] indexer = new P[num_tristrips];
            for (int j = 0; j < num_tristrips; j++) {
                Element p_element = (Element)p_list.item(j);
                indexer[j] = new P(p_element);
            }
            
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            
            if (need_another_index) {
                // there is not a single index - switch to an IndexedFaceSet
                
                Object v_index = null;
                if (handlerIsBinary) {
                    int num_indices = 0;
                    int[][] poly_indices = new int[num_tristrips][];
                    for (int j = 0; j < num_tristrips; j++) {
                        poly_indices[j] = (int[])indexer[j].getPolyIndicesForTristrip(
                            vertex_offset, num_offsets, true);
                        num_indices += poly_indices[j].length;
                    }
                    v_index = FieldValueHandler.flatten(poly_indices, num_indices);
                } else {
                    int num_indices = 0;
                    String[][] poly_indices = new String[num_tristrips][];
                    for (int j = 0; j < num_tristrips; j++) {
                        poly_indices[j] = (String[])indexer[j].getPolyIndicesForTristrip(
                            vertex_offset, num_offsets, false);
                        num_indices += poly_indices[j].length;
                    }
                    v_index = FieldValueHandler.flatten(poly_indices, num_indices);
                }
                
                Object n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int num_indices = 0;
                            int[][] poly_indices = new int[num_tristrips][];
                            for (int j = 0; j < num_tristrips; j++) {
                                poly_indices[j] = (int[])indexer[j].getPolyIndicesForTristrip(
                                    normal_offset, num_offsets, true);
                                num_indices += poly_indices[j].length;
                            }
                            n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        } else {
                            int num_indices = 0;
                            String[][] poly_indices = new String[num_tristrips][];
                            for (int j = 0; j < num_tristrips; j++) {
                                poly_indices[j] = (String[])indexer[j].getPolyIndicesForTristrip(
                                    normal_offset, num_offsets, false);
                                num_indices += poly_indices[j].length;
                            }
                            n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        }
                    }
                }
                
                Object tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        if (handlerIsBinary) {
                            int num_indices = 0;
                            int[][] poly_indices = new int[num_tristrips][];
                            for (int j = 0; j < num_tristrips; j++) {
                                poly_indices[j] = (int[])indexer[j].getPolyIndicesForTristrip(
                                    texCoord_offset, num_offsets, true);
                                num_indices += poly_indices[j].length;
                            }
                            tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        } else {
                            int num_indices = 0;
                            String[][] poly_indices = new String[num_tristrips][];
                            for (int j = 0; j < num_tristrips; j++) {
                                poly_indices[j] = (String[])indexer[j].getPolyIndicesForTristrip(
                                    texCoord_offset, num_offsets, false);
                                num_indices += poly_indices[j].length;
                            }
                            tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                        }
                    }
                }
                
                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index);
                
            } else {
                contentHandler.startField("geometry");
                contentHandler.startNode("IndexedTriangleStripSet", null);
                contentHandler.startField("index");
                if (handlerIsBinary) {
                    int num_indices = 0;
                    int[][] tristrips_indices = new int[num_tristrips][];
                    for (int j = 0; j < num_tristrips; j++) {
                        tristrips_indices[j] = (int[])indexer[j].getIndices(
                            vertex_offset, num_offsets, true);
                        num_indices += tristrips_indices[j].length;
                    }
                    int[] vertex_indices = FieldValueHandler.flatten(tristrips_indices, num_indices);
                    bch.fieldValue(vertex_indices, vertex_indices.length);
                } else {
                    int num_indices = 0;
                    String[][] tristrips_indices = new String[num_tristrips][];
                    for (int j = 0; j < num_tristrips; j++) {
                        tristrips_indices[j] = (String[])indexer[j].getIndices(
                            vertex_offset, num_offsets, false);
                        num_indices += tristrips_indices[j].length;
                    }
                    String[] vertex_indices = FieldValueHandler.flatten(tristrips_indices, num_indices);
                    sch.fieldValue(vertex_indices);
                }
                
                buildCoordField(vertexSource);
                
                if ( normalSource != null ) {
                    buildNormalField(normalSource);
                }
                
                if ( texCoordSource != null ) {
                    buildTexCoordField(texCoordSource);
                }
                
                contentHandler.endNode();   // IndexedTriangleStripSet
                contentHandler.endField();  // geometry
            }
            
            String material = tristrips_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-polylist
     *
     * @param polylist_list A NodeList containing the polylist Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processPolylist(NodeList polylist_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < polylist_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element polylist_element = (Element)polylist_list.item(i);
            int num_polys = Integer.parseInt(
                polylist_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                polylist_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] p_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(p_input);
            Input vertex_input = Input.getInput(p_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            NodeList p_list =
                polylist_element.getElementsByTagName(ColladaStrings.P);
            Element p_element = (Element)p_list.item(0);
            P indexer = new P(p_element);
            
            NodeList vcount_list =
                polylist_element.getElementsByTagName(ColladaStrings.VCOUNT);
            Element vcount_element = (Element)vcount_list.item(0);
            Vcount vertex_data = new Vcount(vcount_element);
            
            Object v_index = null;
            if (handlerIsBinary) {
                int[] vertex_indices = (int[])indexer.getPolyIndices(
                    vertex_data, vertex_offset, num_offsets, true);
                v_index = vertex_indices;
            } else {
                String[] vertex_indices = (String[])indexer.getPolyIndices(
                    vertex_data, vertex_offset, num_offsets, false);
                v_index = vertex_indices;
            }
            
            // get the coordinate points
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            
            Source normalSource = null;
            Object n_index = null;
            
            Input normal_input = Input.getInput(p_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
                
                int normal_offset = normal_input.offset;
                if (normal_offset != vertex_offset) {
                    // normals have indices distinct from the vertices
                    if (handlerIsBinary) {
                        int[] normal_indices = (int[])indexer.getPolyIndices(
                            vertex_data, normal_offset, num_offsets, true);
                        n_index = normal_indices;
                    } else {
                        String[] normal_indices = (String[])indexer.getPolyIndices(
                            vertex_data, normal_offset, num_offsets, false);
                        n_index = normal_indices;
                    }
                }
            }
            
            Source texCoordSource = null;
            Object tc_index = null;
            
            Input texCoord_input = Input.getInput(p_input, ColladaStrings.TEXCOORD);
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
                
                int texCoord_offset = texCoord_input.offset;
                if (texCoord_offset != vertex_offset) {
                    // texCoords have indices distinct from the vertices
                    if (handlerIsBinary) {
                        int[] texCoord_indices = (int[])indexer.getPolyIndices(
                            vertex_data, texCoord_offset, num_offsets, true);
                        tc_index = texCoord_indices;
                    } else {
                        String[] texCoord_indices = (String[])indexer.getPolyIndices(
                            vertex_data, texCoord_offset, num_offsets, false);
                        tc_index = texCoord_indices;
                    }
                }
            }
            
            buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index);
            
            String material = polylist_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Create a Shape node's geometry field, using an IndexedFaceSet node
     *
     * @param vertexSource The Source object containing vertex coordinates.
     * @param vertex_indices An array containing the vertex coordinate indices.
     * @param normalSource The Source object containing noormal coordinates.
     * If null the normal and normalIndex field are not created.
     * @param normal_indices An array containing normal coordinate indices.
     * If null the normalIndex field is not created.
     * @param texCoordSource The Source object containing texture coordinates.
     * If null the texCoord and texCoordIndex field are not created.
     * @param texCoord_indices An array containing texture coordinate indices.
     * If null the texCoordIndex field is not created.
     */
    private void buildIFS(
        Source vertexSource, Object vertex_indices,
        Source normalSource, Object normal_indices,
        Source texCoordSource, Object texCoord_indices) {
        
        contentHandler.startField("geometry");
        contentHandler.startNode("IndexedFaceSet", null);
        
        buildCoordField(vertexSource);
        
        contentHandler.startField("coordIndex");
        if (handlerIsBinary) {
            int[] indices = (int[])vertex_indices;
            bch.fieldValue(indices, indices.length);
        } else {
            String[] indices = (String[])vertex_indices;
            sch.fieldValue(indices);
        }
        
        if (normalSource != null) {
            
            buildNormalField(normalSource);
            
            if (normal_indices != null) {
                // normals have indices distinct from the vertices
                contentHandler.startField("normalIndex");
                if (handlerIsBinary) {
                    int[] indices = (int[])normal_indices;
                    bch.fieldValue(indices, indices.length);
                } else {
                    String[] indices = (String[])normal_indices;
                    sch.fieldValue(indices);
                }
            }
        }
        
        if (texCoordSource != null) {
            
            buildTexCoordField(texCoordSource);
            
            if (texCoord_indices != null) {
                // texCoords have indices distinct from the vertices
                contentHandler.startField("texCoordIndex");
                if (handlerIsBinary) {
                    int[] indices = (int[])texCoord_indices;
                    bch.fieldValue(indices, indices.length);
                } else {
                    String[] indices = (String[])texCoord_indices;
                    sch.fieldValue(indices);
                }
            }
        }
        
        contentHandler.endNode();   // IndexedFaceSet
        contentHandler.endField();  // geometry
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-polygons
     *
     * @param polygons_list A NodeList containing the polylist Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processPolygons(NodeList polygons_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < polygons_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element polygons_element = (Element)polygons_list.item(i);
            int num_polys = Integer.parseInt(
                polygons_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                polygons_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] p_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(p_input);
            Input vertex_input = Input.getInput(p_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            NodeList p_list = polygons_element.getElementsByTagName(ColladaStrings.P);
            P[] indexer = new P[num_polys];
            for (int j = 0; j < num_polys; j++) {
                Element p_element = (Element)p_list.item(j);
                indexer[j] = new P(p_element);
            }
            
            Object v_index = null;
            if (handlerIsBinary) {
                int num_indices = 0;
                int[][] poly_indices = new int[num_polys][];
                for (int j = 0; j < num_polys; j++) {
                    poly_indices[j] = (int[])indexer[j].getIndices(
                        vertex_offset, num_offsets, true);
                    num_indices += poly_indices[j].length;
                }
                v_index = FieldValueHandler.flatten(poly_indices, num_indices);
            } else {
                int num_indices = 0;
                String[][] poly_indices = new String[num_polys][];
                for (int j = 0; j < num_polys; j++) {
                    poly_indices[j] = (String[])indexer[j].getIndices(
                        vertex_offset, num_offsets, false);
                    num_indices += poly_indices[j].length;
                }
                v_index = FieldValueHandler.flatten(poly_indices, num_indices);
            }
            
            // get the coordinate points
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            
            Source normalSource = null;
            Object n_index = null;
            
            Input normal_input = Input.getInput(p_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
                
                int normal_offset = normal_input.offset;
                if (normal_offset != vertex_offset) {
                    // normals have indices distinct from the vertices
                    if (handlerIsBinary) {
                        int num_indices = 0;
                        int[][] poly_indices = new int[num_polys][];
                        for (int j = 0; j < num_polys; j++) {
                            poly_indices[j] = (int[])indexer[j].getIndices(
                                normal_offset, num_offsets, true);
                            num_indices += poly_indices[j].length;
                        }
                        n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    } else {
                        int num_indices = 0;
                        String[][] poly_indices = new String[num_polys][];
                        for (int j = 0; j < num_polys; j++) {
                            poly_indices[j] = (String[])indexer[j].getIndices(
                                normal_offset, num_offsets, false);
                            num_indices += poly_indices[j].length;
                        }
                        n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }
            }
            
            Source texCoordSource = null;
            Object tc_index = null;
            
            Input texCoord_input = Input.getInput(p_input, ColladaStrings.TEXCOORD);
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
                
                int texCoord_offset = texCoord_input.offset;
                if (texCoord_offset != vertex_offset) {
                    // texCoords have indices distinct from the vertices
                    if (handlerIsBinary) {
                        int num_indices = 0;
                        int[][] poly_indices = new int[num_polys][];
                        for (int j = 0; j < num_polys; j++) {
                            poly_indices[j] = (int[])indexer[j].getIndices(
                                texCoord_offset, num_offsets, true);
                            num_indices += poly_indices[j].length;
                        }
                        tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    } else {
                        int num_indices = 0;
                        String[][] poly_indices = new String[num_polys][];
                        for (int j = 0; j < num_polys; j++) {
                            poly_indices[j] = (String[])indexer[j].getIndices(
                                texCoord_offset, num_offsets, false);
                            num_indices += poly_indices[j].length;
                        }
                        tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }
            }
            
            buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index);
            
            String material = polygons_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-lines
     *
     * @param lines_list A NodeList containing the lines Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processLines(NodeList lines_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < lines_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element lines_element = (Element)lines_list.item(i);
            int num_lines = Integer.parseInt(
                lines_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                lines_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(input);
            Input vertex_input = Input.getInput(input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            NodeList p_list = lines_element.getElementsByTagName(ColladaStrings.P);
            Element p_element = (Element)p_list.item(0);
            P indexer = new P(p_element);
            
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coordIndex");
            if (handlerIsBinary) {
                int[] vertex_indices = (int[])indexer.getLinesIndices(
                    num_lines, vertex_offset, num_offsets, true);
                bch.fieldValue(vertex_indices, vertex_indices.length);
            } else {
                String[] vertex_indices = (String[])indexer.getLinesIndices(
                    num_lines, vertex_offset, num_offsets, false);
                sch.fieldValue(vertex_indices);
            }
            
            // get the coordinate points
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            String position_source_id = getElementId(position_input.source);
            Source verticesSource = sourceMap.get(position_source_id);
            buildCoordField(verticesSource);
            
            contentHandler.endNode();   // IndexedLineSet
            contentHandler.endField();  // geometry
            
            String material = lines_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Add Shape nodes cooresponding to the mesh-linestrips
     *
     * @param linestrips_list A NodeList containing the linestrips Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processLinestrips(NodeList linestrips_list, Map<String, Source> sourceMap,
        Element vertices_element, BindMaterial bindMaterial) {
        
        for (int i = 0; i < linestrips_list.getLength(); i++) {
            contentHandler.startNode("Shape", null);
            Element linestrips_element = (Element)linestrips_list.item(i);
            int num_linestrips = Integer.parseInt(
                linestrips_element.getAttribute(ColladaStrings.COUNT));
            
            NodeList input_list =
                linestrips_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(input);
            Input vertex_input = Input.getInput(input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            
            NodeList p_list = linestrips_element.getElementsByTagName(ColladaStrings.P);
            P[] indexer = new P[num_linestrips];
            for (int j = 0; j < num_linestrips; j++) {
                Element p_element = (Element)p_list.item(j);
                indexer[j] = new P(p_element);
            }
            
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coordIndex");
            if (handlerIsBinary) {
                int num_indices = 0;
                int[][] strip_indices = new int[num_linestrips][];
                for (int j = 0; j < num_linestrips; j++) {
                    strip_indices[j] = (int[])indexer[j].getIndices(
                        vertex_offset, num_offsets, true);
                    num_indices += strip_indices[j].length;
                }
                int[] vertex_indices = FieldValueHandler.flatten(strip_indices, num_indices);
                bch.fieldValue(vertex_indices, vertex_indices.length);
            } else {
                int num_indices = 0;
                String[][] strip_indices = new String[num_linestrips][];
                for (int j = 0; j < num_linestrips; j++) {
                    strip_indices[j] = (String[])indexer[j].getIndices(
                        vertex_offset, num_offsets, false);
                    num_indices += strip_indices[j].length;
                }
                String[] vertex_indices = FieldValueHandler.flatten(strip_indices, num_indices);
                sch.fieldValue(vertex_indices);
            }
            
            // get the coordinate points
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT);
            
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            
            String position_source_id = getElementId(position_input.source);
            Source verticesSource = sourceMap.get(position_source_id);
            buildCoordField(verticesSource);
            
            contentHandler.endNode();   // IndexedLineSet
            contentHandler.endField();  // geometry
            
            String material = linestrips_element.getAttribute(ColladaStrings.MATERIAL);
            Element material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            
            contentHandler.endNode();   // Shape
        }
    }
    
    /**
     * Create the coord field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildCoordField(Source source) {
        String id = source.id;
        contentHandler.startField("coord");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("Coordinate", id);
            contentHandler.startField("point");
            if (handlerIsBinary) {
                float[] source_data = (float[])source.getSourceData(true);
                
                if (APPLY_SCALE) {
                    for(int i=0; i < source_data.length; i++) {
                        source_data[i] = source_data[i] * scale;
                    }
                }
                bch.fieldValue(source_data, source_data.length);
            } else {
                if (APPLY_SCALE) {
                    float[] source_data = (float[])source.getSourceData(true);
                    for(int i=0; i < source_data.length; i++) {
                        source_data[i] = source_data[i] * scale;
                    }
                    
                    StringBuffer sb = new StringBuffer();
                    for(int i=0; i < source_data.length; i++) {
                        sb.append(numberFormater.format(source_data[i]));
                        sb.append(" ");
                    }
                    sch.fieldValue(sb.toString());
                } else {
                    String source_data = (String)source.getSourceData(false);
                    sch.fieldValue(source_data);
                }
            }
            contentHandler.endNode();   // Coordinate
            contentHandler.endField();  // coord
        }
    }
    
    /**
     * Create the normal field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildNormalField(Source source) {
        String id = source.id;
        contentHandler.startField("normal");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("Normal", id);
            contentHandler.startField("vector");
            if (handlerIsBinary) {
                float[] source_data = (float[])source.getSourceData(true);
                bch.fieldValue(source_data, source_data.length);
            } else {
                String source_data = (String)source.getSourceData(false);
                sch.fieldValue(source_data);
            }
            contentHandler.endNode();   // Normal
            contentHandler.endField();  // normal
        }
    }
    
    /**
     * Create the texCoord field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildTexCoordField(Source source) {
        String id = source.id;
        contentHandler.startField("texCoord");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("TextureCoordinate", id);
            contentHandler.startField("point");
            if (handlerIsBinary) {
                float[] source_data = (float[])source.getSourceData(true);
                bch.fieldValue(source_data, source_data.length);
            } else {
                String source_data = (String)source.getSourceData(false);
                sch.fieldValue(source_data);
            }
            contentHandler.endNode();   // TextureCoordinate
            contentHandler.endField();  // texCcoord
        }
    }
    
    /**
     * Add an Appearance node for the specified material to the content handler
     *
     * @param node A Collada material_instance
     */
    private void processMaterialInstance(Element node) {
        
        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);
        
        if (node != null) {
            String url = node.getAttribute(ColladaStrings.TARGET);
            String material_url_id = getElementId(url);
            // find the material resource, identified by it's url
            Element material_element =
                getResourceElement(ColladaStrings.MATERIAL, material_url_id);
            if (material_element != null) {
                NodeList nl =
                    material_element.getElementsByTagName(ColladaStrings.INSTANCE_EFFECT);
                Element instance_effect_element = (Element)nl.item(0);
                url = instance_effect_element.getAttribute(ColladaStrings.URL);
                String effect_url_id = getElementId(url);
                Element effect_element =
                    getResourceElement(ColladaStrings.EFFECT, effect_url_id);
                if (effect_element != null) {
                    nl = effect_element.getElementsByTagName(ColladaStrings.PROFILE_COMMON);
                    if (nl.getLength() > 0) {
                        Element profile_common_element = (Element)nl.item(0);
                        nl = profile_common_element.getElementsByTagName(ColladaStrings.TECHNIQUE);
                        Element technique_element = (Element)nl.item(0);
                        ArrayList<Element> material_list = ImportUtils.getElements(technique_element);
                        cull(material_list, MATERIAL);
                        if (material_list.size() > 0) {
                            contentHandler.startField("material");
                            if (materialInstanceList.contains(effect_url_id)) {
                                contentHandler.useDecl(effect_url_id);
                            } else {
                                contentHandler.startNode("Material", effect_url_id);
                                // there should be only one and only one
                                Element material_type_element = material_list.get(0);
                                String id = material_type_element.getTagName();
                                if ( id.equals( ColladaStrings.BLINN ) ) {
                                    processMaterialFields(material_type_element);
                                    
                                } else if ( id.equals( ColladaStrings.CONSTANT ) ) {
                                    processMaterialFields(material_type_element);
                                    
                                } else if ( id.equals( ColladaStrings.LAMBERT ) ) {
                                    processMaterialFields(material_type_element);
                                    
                                } else if ( id.equals( ColladaStrings.PHONG ) ) {
                                    processMaterialFields(material_type_element);
                                    
                                } else {
                                    // this should be an error, there should be one....
                                }
                                contentHandler.endNode();   // Material
                                materialInstanceList.add(effect_url_id);
                                contentHandler.endField();  // material
                            }
                        }
                    }
                }
            }
        } else {
            // a 'default' Appearance
            contentHandler.startField("material");
            contentHandler.startNode("Material", null);
            contentHandler.startField("diffuseColor");
            if (handlerIsBinary) {
                float[] content = new float[]{ 0.7f, 0.7f, 0.7f };
                bch.fieldValue(content, content.length);
            } else {
                sch.fieldValue("0.7 0.7 0.7");
            }
            contentHandler.endNode();   // Material
            contentHandler.endField();  // material
        }
        contentHandler.endNode();   // Appearance
        contentHandler.endField();  // appearance
    }
    
    /**
     * Translate the fields of the Collada 'material' to X3D
     *
     * @param e The Collada material type element.
     */
    private void processMaterialFields(Element e) {
        NodeList nl = e.getElementsByTagName(ColladaStrings.EMISSION);
        if (nl.getLength( ) > 0) {
            Element emission_element = (Element)nl.item(0);
            if (containsColor(emission_element)) {
                contentHandler.startField("emissiveColor");
                processColor(emission_element);
            }
        }
        nl = e.getElementsByTagName(ColladaStrings.DIFFUSE);
        if (nl.getLength( ) > 0) {
            Element diffuse_element = (Element)nl.item(0);
            if (containsColor(diffuse_element)) {
                contentHandler.startField("diffuseColor");
                processColor(diffuse_element);
            }
        }
        nl = e.getElementsByTagName(ColladaStrings.SPECULAR);
        if (nl.getLength( ) > 0) {
            Element specular_element = (Element)nl.item(0);
            if (containsColor(specular_element)) {
                contentHandler.startField("specularColor");
                processColor(specular_element);
            }
        }
        nl = e.getElementsByTagName(ColladaStrings.SHININESS);
        if (nl.getLength( ) > 0) {
            Element shininess_element = (Element)nl.item(0);
            contentHandler.startField("shininess");
            nl = shininess_element.getElementsByTagName(ColladaStrings.FLOAT);
            Element float_element = (Element)nl.item(0);
            String float_content = float_element.getTextContent();
            float value = Float.parseFloat(float_content);
            // note, special handling for the shininess value
            if ( value > 1 ) {
                value /= 128;
            }
            if (handlerIsBinary) {
                bch.fieldValue(value);
            } else {
                sch.fieldValue(Float.toString(value));
            }
        }
    }
    
    /**
     * Test that the Collada element contains a 'color' element
     *
     * @param e A Collada element
     * @return true if the argument Collada element contains a color element,
     * false otherwise.
     */
    private boolean containsColor(Element e) {
        NodeList nl = e.getElementsByTagName(ColladaStrings.COLOR);
        return( nl.getLength( ) > 0 );
    }
    
    /**
     * Translate an element that contains a Collada 'color' element to an X3D SFColor field
     *
     * @param e A Collada element of the type 'common_color_or_texture_type'
     */
    private void processColor(Element e) {
        NodeList nl = e.getElementsByTagName(ColladaStrings.COLOR);
        Element color_element = (Element)nl.item(0);
        String color_content = color_element.getTextContent();
        // note, dropping the alpha value....
        float[] fcontent = FieldValueHandler.toFloat(color_content);
        if (handlerIsBinary) {
            float[] content = new float[]{ fcontent[0], fcontent[1], fcontent[2] };
            bch.fieldValue(content, content.length);
        } else {
            sch.fieldValue(fcontent[0]+" "+fcontent[1]+" "+fcontent[2]);
        }
    }
    
    /**
     * Translate an element that contains a Collada 'float' element to an X3D SFFloat field
     *
     * @param e A Collada element of the type 'common_float_or_param_type'
     */
    private void processFloat(Element e) {
        NodeList nl = e.getElementsByTagName(ColladaStrings.FLOAT);
        Element float_element = (Element)nl.item(0);
        String float_content = float_element.getTextContent();
        if (handlerIsBinary) {
            float[] fcontent = FieldValueHandler.toFloat(float_content);
            bch.fieldValue(fcontent[0]);
        } else {
            sch.fieldValue(float_content);
        }
    }
    
    /**
     * Add a Viewpoint node for the specified camera to the content handler
     *
     * @param node A Collada camera_instance
     */
    private void processCameraInstance(Element node) {
        
        String url = node.getAttribute(ColladaStrings.URL);
        String camera_url_id = getElementId(url);
        
        // find the camera resource, identified by it's url
        Element camera_element =
            getResourceElement(ColladaStrings.CAMERA, camera_url_id);
        if (camera_element != null) {
            String name = camera_element.getAttribute(ColladaStrings.NAME);
            NodeList nl = camera_element.getElementsByTagName(ColladaStrings.OPTICS);
            Element optics_element = (Element)nl.item(0);
            nl = optics_element.getElementsByTagName(ColladaStrings.TECHNIQUE_COMMON);
            Element technique_common_element = (Element)nl.item(0);
            nl = technique_common_element.getElementsByTagName(ColladaStrings.PERSPECTIVE);
            if (nl.getLength() > 0) {
                // for now we only deal with perspective
                Element perspective_element = (Element)nl.item(0);
                contentHandler.startNode("Viewpoint", camera_url_id);
                contentHandler.startField("description");
                if (handlerIsBinary) {
                    bch.fieldValue(name);
                } else {
                    sch.fieldValue(name);
                }
                contentHandler.startField("position");
                if (handlerIsBinary) {
                    bch.fieldValue(new float[]{ 0, 0, 0 }, 3);
                } else {
                    sch.fieldValue("0 0 0");
                }
                
                contentHandler.endNode(); // Viewpoint
            } else {
                nl = technique_common_element.getElementsByTagName(ColladaStrings.ORTHOGRAPHIC);
                if (nl.getLength() > 0) {
                    // don't know how to deal with orthographic yet
                }
            }
        }
    }
    
    /**
     * Return the id string of a library element
     *
     * @param url The url String from which to extract the id
     * @return The id String, or null if we're baffled by the url......
     */
    private String getElementId(String url) {
        String id = null;
        if (url.startsWith("#")) {
            // a local url, trim off the identifer
            id = url.substring(1);
        } else {
            // don't know what to do with non local urls yet
        }
        return(id);
    }
    
    /**
     * Return the name attribute of the argument element, or
     * null if it does not exist.
     *
     * @param element The element to search for a name attribute
     * @return The name String, or null if it does not exist.
     */
    private String getName(Element element) {
        String name = element.getAttribute(ColladaStrings.NAME);
        if (name.equals("")) {
            name = null;
        }
        return(name);
    }
    
    /**
     * Remove any Elements from the argument list that are not named
     * in the argument tagName array.
     *
     * @param list A list of Elements to inspect
     * @param children An array of element tags to search for
     */
    private void cull(ArrayList<Element> list, String[] children) {
        for (int i = list.size()-1; i >= 0; i-- ) {
            Element e = list.get(i);
            String name = e.getTagName();
            boolean supported = false;
            for (int j = 0; j < children.length; j++) {
                if (name.equals(children[j])) {
                    supported = validate(e);
                    break;
                }
            }
            if ( !supported ) {
                list.remove(i);
            }
        }
    }
    
    /**
     * Called when an element is on the initial supported list to 'look deeper'
     * into the instance elements to determine if we can REALLY support it.
     *
     * @param element The element
     * @return true if the element is REALLY supported, false otherwise.
     */
    private boolean validate(Element element) {
        boolean isSupported = true;
        String tagName = element.getTagName();
        if (tagName.equals(ColladaStrings.INSTANCE_GEOMETRY)) {
            String url = element.getAttribute(ColladaStrings.URL);
            String url_id = getElementId(url);
            // find the geometry resource, identified by it's url
            Element geometry_element =
                getResourceElement(ColladaStrings.GEOMETRY, url_id);
            if (geometry_element != null) {
                NodeList nl = geometry_element.getElementsByTagName(ColladaStrings.MESH);
                if (!(nl.getLength() > 0)) {
                    // only mesh geometry supported so far
                    isSupported = false;
                }
            } else {
                // the resource could not be found
                isSupported = false;
            }
        } else if (tagName.equals(ColladaStrings.INSTANCE_CAMERA)) {
            String url = element.getAttribute(ColladaStrings.URL);
            String url_id = getElementId(url);
            // find the camera resource, identified by it's url
            Element camera_element =
                getResourceElement(ColladaStrings.CAMERA, url_id);
            if (camera_element != null) {
                NodeList nl = camera_element.getElementsByTagName(ColladaStrings.OPTICS);
                Element optics_element = (Element)nl.item(0);
                nl = optics_element.getElementsByTagName(ColladaStrings.TECHNIQUE_COMMON);
                Element technique_common_element = (Element)nl.item(0);
                nl = technique_common_element.getElementsByTagName(ColladaStrings.PERSPECTIVE);
                if (nl.getLength() <= 0) {
                    // for now we only deal with perspective
                    // this would be orthographic....
                    isSupported = false;
                }
            } else {
                // the resource could not be found
                isSupported = false;
            }
        }
        return(isSupported);
    }
    
    /**
     * Search the libraries of the document of the argument resource type for the
     * Element that matches the argument id.
     * <p>
     * For example, for the resource type "geometry", the "library_geometries"
     * Elements will be searched for a "geometry" Element with the specified id
     * attribute.
     *
     * @param resource The resource type
     * @param id The identifier of the resource
     * @return The resource Element, or null if the specified element was not found.
     */
    private Element getResourceElement(String resource, String id) {
        Element resource_element = null;
        String library = ColladaStrings.getLibraryTagName(resource);
        NodeList library_nodelist = doc_element.getElementsByTagName(library);
        for (int i = 0; i < library_nodelist.getLength(); i++) {
            Element library_element = (Element)library_nodelist.item(i);
            NodeList resource_nodelist =
                library_element.getElementsByTagName(resource);
            for (int j = 0; j < resource_nodelist.getLength(); j++) {
                Element tmp_element = (Element)resource_nodelist.item(j);
                String resource_id = tmp_element.getAttribute(ColladaStrings.ID);
                if (id.equals(resource_id)) {
                    resource_element = tmp_element;
                    break;
                }
            }
            if (resource_element != null) {
                break;
            }
        }
        return(resource_element);
    }
    
    /**
     * Add interpolator capabilities to the content handler
     *
     * @param animation_element A Collada <animation> element
     */
    private void processAnimation(Element animation_element) {
        
        NodeList sampler_element_list =
            animation_element.getElementsByTagName(ColladaStrings.SAMPLER);
        Map<String, Sampler> samplerMap = Sampler.getSamplerMap(sampler_element_list);
        
        NodeList source_element_list =
            animation_element.getElementsByTagName(ColladaStrings.SOURCE);
        Map<String, Source> sourceMap = Source.getSourceMap(source_element_list);
        
        NodeList channel_element_list =
            animation_element.getElementsByTagName(ColladaStrings.CHANNEL);
        
        Channel[] channel = Channel.getChannels(channel_element_list);
        
        for (int i = 0; i < channel.length; i++) {
            String target_def_id = channel[i].target;
            if (defedFieldMap.containsKey(target_def_id)) {
                
                String target_def_field = defedFieldMap.get(target_def_id);
                
                String sampler_id = getElementId(channel[i].source);
                Sampler sampler = samplerMap.get(sampler_id);
                
                Input input = Input.getInput(sampler.input, ColladaStrings.INPUT_SEMANTIC);
                String input_source_id = getElementId(input.source);
                Source input_source = sourceMap.get(input_source_id);
                
                float[] input_data = (float[])input_source.getSourceData(true);
                float begin_time = input_data[0];
                float end_time = input_data[input_data.length - 1];
                float cycle_interval = end_time - begin_time;
                
                // convert the input data array to be the interpolator keys
                for (int j = 0; j < input_data.length; j++) {
                    float key = input_data[j] / cycle_interval;
                    input_data[j] = key;
                }
                
                Input output = Input.getInput(sampler.input, ColladaStrings.OUTPUT);
                String output_source_id = getElementId(output.source);
                Source output_source = sourceMap.get(output_source_id);
                
                Input interp = Input.getInput(sampler.input, ColladaStrings.INTERPOLATION);
                String interp_source_id = getElementId(interp.source);
                //Source interp_source = sourceMap.get(interp_source_id);
                
                ////////////////////////////////////////////////////////////////////////
                // TODO: the DEF id on this interpolator needs more thought....
                // may not be unique?
                contentHandler.startNode("PositionInterpolator", interp_source_id);
                ////////////////////////////////////////////////////////////////////////
                contentHandler.startField("key");
                if (handlerIsBinary) {
                    bch.fieldValue(input_data, input_data.length);
                } else {
                    String[] keys = FieldValueHandler.toString(input_data);
                    sch.fieldValue(keys);
                }
                
                contentHandler.startField("keyValue");
                if (handlerIsBinary) {
                    float[] keyValue_data = (float[])output_source.getSourceData(true);
                    bch.fieldValue(keyValue_data, keyValue_data.length);
                } else {
                    String[] keyValue_data = (String[])output_source.getSourceData(true);
                    sch.fieldValue(keyValue_data);
                }
                contentHandler.endNode();   // PositionInterpolator
                
                ////////////////////////////////////////////////////////////////////////
                // TODO: the DEF id on this time sensor needs more thought....
                // may not be unique?
                contentHandler.startNode("TimeSensor", input_source_id);
                ////////////////////////////////////////////////////////////////////////
                contentHandler.startField("loop");
                if (handlerIsBinary) {
                    bch.fieldValue(true);
                } else {
                    sch.fieldValue(Boolean.TRUE.toString());
                }
                
                contentHandler.startField("cycleInterval");
                if (handlerIsBinary) {
                    bch.fieldValue((double)cycle_interval);
                } else {
                    sch.fieldValue(Float.toString(cycle_interval));
                }
                contentHandler.endNode();   // TimeSensor
                
                routeHandler.routeDecl(
                    input_source_id, "fraction_changed",
                    interp_source_id, "set_fraction");
                
                routeHandler.routeDecl(
                    interp_source_id, "value_changed",
                    target_def_id, target_def_field +"_changed");
            }
        }
    }
}
