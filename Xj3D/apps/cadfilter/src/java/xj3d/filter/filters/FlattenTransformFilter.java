/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.parser.DefaultFieldParserFactory;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.ScriptHandler;
import org.web3d.vrml.sav.SAVException;

import xj3d.filter.AbstractFilter;
import xj3d.filter.FieldValueHandler;

import xj3d.filter.node.*;

/**
 * Filter for removing transform hierarchies. The transformational
 * components of the Transforms are combined and the coordinates of
 * the children nodes are transformed to retain the information. The
 * end result is a file containg geometry (Shape nodes) without the 
 * grouping node. 
 * <p>
 * At present - IndexedTriangleSet, IndexedTriangleStripSet, 
 * IndexedTriangleFanSet, and Viewpoint nodes are transformed and 
 * output.
 *
 * @author Rex Melton
 * @version $Revision: 1.10 $
 */
public class FlattenTransformFilter extends AbstractFilter {
    
    /** A stack of node names */
    protected SimpleStack nodeStack;
    
    /** A stack of field names */
    protected SimpleStack fieldStack;
    
    /** A stack of node wrappers */
    protected SimpleStack encStack;
    
    /** A stack of parent node types */
    protected SimpleStack parentTypeStack;
    
    /** The field value handler */
    protected FieldValueHandler fieldHandler;
    
    /** Map of def'ed nodes keyed by DEF Name */
    protected HashMap<String, Encodable> defMap;
    
    /** Scene instance */
    protected Scene scene;
    
    /** Node wrapper factory */
    protected EncodableFactory factory;
    
    /** Scratch coordinate objects, used in transform calculations */
    private Vector3f translation;
    private Matrix3f rotation;
    private Vector3f child_translation;
    
    /** Flag indicating that an SFNode has ended and implicitly this
    *  means that the parent field of the node has ended as well */
    protected boolean fieldHasEndedImplicitly;
    
    /**
     * Create an instance of the filter.
     */
    public FlattenTransformFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        encStack = new SimpleStack();
        parentTypeStack = new SimpleStack();
        defMap = new HashMap<String, Encodable>();
        
        translation = new Vector3f();
        rotation = new Matrix3f();
        child_translation = new Vector3f();
    }
    
    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------
    
    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startDocument(String uri,
        String url,
        String encoding,
        String type,
        String version,
        String comment)
        throws SAVException, VRMLException {
        
        // this is a crude way to get revision numbers.....
        int separator_index = version.indexOf(".");
        int majorVersion = Integer.parseInt(version.substring(separator_index-1, separator_index)); 
        int minorVersion = Integer.parseInt(version.substring(separator_index+1, separator_index+2)); 
        
        DefaultFieldParserFactory fac = new DefaultFieldParserFactory();
        VRMLFieldReader fieldReader = fac.newFieldParser(majorVersion, minorVersion);
        
        factory = new EncodableFactory(contentHandler, fieldReader);
        scene = (Scene)factory.getEncodable("Scene", null);
        encStack.push(scene);
        
        fieldHandler = new FieldValueHandler(version, contentHandler);
        contentHandler.startDocument(uri, url, encoding, type, version, comment);
    }
    
    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endDocument() throws SAVException, VRMLException {
        
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        
        Encodable[] enc = scene.getRootNodes();
        flatten(enc, matrix);
        
        contentHandler.endDocument();
    }
    
    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {
        
        Encodable enc = factory.getEncodable(name, defName);
        
        String fieldName = null;
        if (!fieldStack.isEmpty() && !nodeStack.isEmpty()) {
            String nodeName = (String)nodeStack.peek();
            fieldName = (String)fieldStack.peek();
            parentTypeStack.push(fieldHandler.getFieldType(nodeName, fieldName));
        }
        Encodable parent = (Encodable)encStack.peek();
        if (parent != null) {
            parent.setValue(fieldName, enc);
        }
        encStack.push(enc);
        
        if (defName != null) {
            defMap.put(defName, enc);
        }
        nodeStack.push(name);
    }
    
    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endNode() throws SAVException, VRMLException {
        
        String name = (String)nodeStack.pop();
        Encodable enc = (Encodable)encStack.pop();
        
        if (!parentTypeStack.isEmpty()) {
            int fieldType = ((Integer)parentTypeStack.pop()).intValue();
            if (fieldType == FieldConstants.SFNODE) {
                fieldHasEndedImplicitly = true;
                String fieldName = (String)fieldStack.pop();
            }
        }
    }
    
    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startField(String name) throws SAVException, VRMLException {
        
        fieldStack.push(name);
    }
    
    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void useDecl(String defName) throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        
        Encodable use = defMap.get(defName);
        Encodable enc = (Encodable)encStack.peek();
        if ((enc != null) && (use != null)) {
            Encodable dup = use.clone();
            enc.setValue(fieldName, dup);
        }
    }
    
    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endField() throws SAVException, VRMLException {

        if (!parentTypeStack.isEmpty()) {
            int fieldType = ((Integer)parentTypeStack.peek()).intValue();
            if ((fieldType == FieldConstants.MFNODE) && !fieldHasEndedImplicitly) {
                String fieldName = (String)fieldStack.pop();
                parentTypeStack.pop();
            }
        }
        fieldHasEndedImplicitly = false;
    }
    
    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------
    
    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void fieldValue(String value) throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, values);
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------
    
    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(fieldName, value, len);
        }
    }
    
    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------
    
    /**
     * Walk the nodes, transform the coordinates and normals of Shapes
     * and the position and orientation of Viewpoints, then encode them.
     *
     * @param enc An array of nodes
     * @param matrix The matrix to use to transform the coordinates.
     */
    private void flatten(Encodable[] enc, Matrix4f matrix) {
        
        for (int i = 0; i < enc.length; i++) {
            Encodable e = enc[i];
            if (e instanceof Shape) {
                Shape shape = (Shape)e;
                ComposedGeometry geometry = (ComposedGeometry)shape.getGeometry();
                if (geometry != null) {
                    float[] point = null;
                    Coordinate coord = geometry.getCoordinate();
                    if (coord != null) {
                        point = coord.point;
                        if (point != null) {
                            int num_point = coord.num_point;
                            float[] point_xfrm = new float[num_point*3];
                            Point3f p = new Point3f();
                            int idx = 0;
                            for (int j = 0; j < num_point; j++) {
                                p.x = point[idx]; 
                                p.y = point[idx+1]; 
                                p.z = point[idx+2];
                                matrix.transform(p);
                                point_xfrm[idx] = p.x;
                                point_xfrm[idx+1] = p.y;
                                point_xfrm[idx+2] = p.z;
                                idx += 3;
                            }
                            coord.point = point_xfrm;
                        }
                    }
                    
                    float[] vector = null;
                    Normal normal = geometry.getNormal();
                    if (normal != null) {
                        vector = normal.vector;
                        if (vector != null) {
                            int num_vector = normal.num_vector;
                            float[] vector_xfrm = new float[num_vector*3];
                            Vector3f v = new Vector3f();
                            int idx = 0;
                            for (int j = 0; j < num_vector; j++) {
                                v.x = vector[idx]; 
                                v.y = vector[idx+1]; 
                                v.z = vector[idx+2];
                                matrix.transform(v);
                                vector_xfrm[idx] = v.x;
                                vector_xfrm[idx+1] = v.y;
                                vector_xfrm[idx+2] = v.z;
                                idx += 3;
                            }
                            normal.vector = vector_xfrm;
                        }
                    }
                    
                    shape.encode();
                    
                    // restore the original data in case it is reused
                    if (coord != null) {
                        coord.point = point;
                    }
                    if (normal != null) {
                        normal.vector = vector;
                    }
                } else {
                    // no geometry, nothing to transform
                    shape.encode();
                }
            } else if (e instanceof Group) {
                Group group = (Group)e;
                Encodable[] children = group.getChildren();
                flatten(children, matrix);
                
            } else if (e instanceof Transform) {
                Transform transform = (Transform)e;
                Encodable[] children = transform.getChildren();
                ////////////////////////////////////////////////////////
                // there's gotta be a better way......
                Matrix4f child_matrix = transform.getMatrix();
                
                //Vector3f translation = new Vector3f();
                matrix.get(translation);
                
                //Matrix3f rotation = new Matrix3f();
                matrix.get(rotation);
                
                float scale = matrix.getScale();
                
                //Vector3f child_translation = new Vector3f();
                child_matrix.get(child_translation);
                
                rotation.transform(child_translation);
                
                child_translation.scale(scale);
                
                translation.add(child_translation);
                
                child_matrix.mul(matrix, child_matrix);
                child_matrix.setTranslation(translation);
                ////////////////////////////////////////////////////////
                flatten(children, child_matrix);
                
            } else if (e instanceof Viewpoint) {
                Viewpoint viewpoint = (Viewpoint)e;
                
                // transform the viewpoint position
                if (viewpoint.position == null) {
                    viewpoint.position = new float[]{0, 0, 10};
                }
                float[] p = viewpoint.position;
                Point3f pos = new Point3f();
                pos.set(p[0], p[1], p[2]);
                matrix.transform(pos);
                pos.get(p);
                
                // transform the viewpoint orientation
                if (viewpoint.orientation == null) {
                    viewpoint.orientation = new float[]{0, 0, 1, 0};
                }
                float[] o = viewpoint.orientation;
                Quat4f quat = new Quat4f();
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(o[0], o[1], o[2], o[3]);
                quat.set(rot);
                matrix.transform(quat);
                rot.set(quat);
                rot.get(o);
                
                viewpoint.encode();
            }
        }
    }
}
