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
 * Filter for combining multiple Shapes into a single Shape node.
 * <p>
 * The input to this filter is presumed to have been run through
 * the FlattenTransformFilter. Input must have the Shape nodes as
 * the root nodes of the Scene, otherwise they will be ignored.
 * At present, only IndexedTriangleSets are combined and output.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class CombineShapeFilter extends AbstractFilter {
    
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
    
    /** Geometry node wrapper converter */
    protected GeometryConverter converter;
    
    /** Flag indicating that an SFNode has ended and implicitly this
    *  means that the parent field of the node has ended as well */
    protected boolean fieldHasEndedImplicitly;
    
    /**
     * Create an instance of the filter.
     */
    public CombineShapeFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        encStack = new SimpleStack();
        parentTypeStack = new SimpleStack();
        defMap = new HashMap<String, Encodable>();
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
        converter = new GeometryConverter(factory);
        
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
        
        Encodable[] enc = scene.getRootNodes();
        combine(enc);
        
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
        
        Encodable use = defMap.get(defName);
        
        String fieldName = (String)fieldStack.pop();
        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            // don't bother cloning the node or settin it's USE name,
            // everything we're interested in (so far) is going to be
            // copied and re-arranged in the combine() operation.
            enc.setValue(fieldName, use);
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
     * Walk the nodes, combine the coordinates and indices of the Shapes,
     * then encode them into a single Shape.
     *
     * @param enc An array of nodes
     */
    private void combine(Encodable[] enc) {
        
        ArrayList<Shape> shapeList = new ArrayList<Shape>();
        for (int i = 0; i < enc.length; i++) {
            Encodable e = enc[i];
            if (e instanceof Shape) {
                Shape shape = (Shape)e;
                Geometry geometry = (Geometry)shape.getGeometry();
                if (geometry instanceof IndexedTriangleSet) {
                    shapeList.add(shape);
                } else if (geometry instanceof IndexedTriangleFanSet) {
                    shape.setGeometry(converter.toITS((IndexedTriangleFanSet)geometry));
                    shapeList.add(shape);
                } else if (geometry instanceof IndexedTriangleStripSet) {
                    shape.setGeometry(converter.toITS((IndexedTriangleStripSet)geometry));
                    shapeList.add(shape);
                }
            } else if (e instanceof Viewpoint) {
                Viewpoint viewpoint = (Viewpoint)e;
                viewpoint.encode();
            }
        }
        // get all the coordinates and indices
        int num_shapes = shapeList.size();
        if (num_shapes > 0) {
            float[][] point = new float[num_shapes][];
            int[] num_point = new int[num_shapes];
            int[][] index = new int[num_shapes][];
            int[] num_index = new int[num_shapes];
            int total_num_point = 0;
            int total_num_index = 0;
            for (int i = 0; i < num_shapes; i++) {
                Shape shape = shapeList.get(i);
                IndexedTriangleSet its = (IndexedTriangleSet)shape.getGeometry();
                Coordinate c = its.getCoordinate();
                point[i] = c.point;
                num_point[i] = c.num_point;
                total_num_point += num_point[i];
                index[i] = its.index;
                num_index[i] = its.num_index;
                total_num_index += num_index[i];
            }
            // put all the coordinates into a single array
            int idx = 0;
            float[] all_point = new float[total_num_point*3];
            for (int i = 0; i < num_shapes; i++) {
                int num_floats = num_point[i]*3;
                System.arraycopy(point[i], 0, all_point, idx, num_floats);
                idx += num_floats;
            }
            // adjust the indices to account for the concatenated coordinates 
            int offset = 0;
            for (int i = 1; i < num_shapes; i++) {
                offset += num_point[i-1];
                for (int j = 0; j < num_index[i]; j++) {
                    index[i][j] += offset;
                }
            }
            // put all the indices into a single array
            idx = 0;
            int[] all_index = new int[total_num_index];
            for (int i = 0; i < num_shapes; i++) {
                System.arraycopy(index[i], 0, all_index, idx, num_index[i]);
                idx += num_index[i];
            }
            //////////////////////////////////////////////////////////////////////
            // skip the re-indexing for now.....
            //CoordinateProcessor cp = new CoordinateProcessor(all_point);
            //if(cp.hasDuplicates()) {
            //    cp.processIndices(all_index);
            //	  total_num_point = cp.getNumCoords();
            //}
            //////////////////////////////////////////////////////////////////////
            // reuse the first shape (i.e. it's Appearance)
            Shape shape = shapeList.get(0);
            IndexedTriangleSet its = (IndexedTriangleSet)shape.getGeometry();
            its.index = all_index;
            its.num_index = total_num_index;
            Coordinate c = its.getCoordinate();
            c.point = all_point;
            c.num_point = total_num_point;
            its.setColor(null);
            its.setNormal(null);
            its.setTextureCoordinate(null);
            shape.encode();
        }
    }
}
