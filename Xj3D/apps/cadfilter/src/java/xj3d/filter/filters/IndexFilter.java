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
import java.util.HashMap;

// Local imports
import org.web3d.parser.DefaultFieldParserFactory;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.ScriptHandler;
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.parser.VRMLFieldReader;

import xj3d.filter.FieldValueHandler;

import xj3d.filter.node.*;

/**
 * Filter for creating indexed versions of non-indexed geometry. This filter
 * transforms TriangleSet, TriangleStripSet, TriangleFanSet and LineSet nodes
 * to IndexedTriangleSet, IndexedTriangleStripSet, IndexedTriangleFanSet, and 
 * IndexedLineSet nodes respectively.
 *
 * @author Rex Melton
 * @version $Revision: 1.8 $
 */
public class IndexFilter extends TypeConversionFilter {
    
    /** The set of nodes that require translation to indexed form */
    private static final String[] NODE_SRC = new String[] {
        "TriangleFanSet", 
        "TriangleSet", 
        "TriangleStripSet", 
        //"LineSet",
    };
    
    /** The set of indexed nodes that will be produced */
    private static final String[] NODE_DST = new String[] {
        "IndexedTriangleFanSet", 
        "IndexedTriangleSet", 
        "IndexedTriangleStripSet", 
        //"IndexedLineSet", 
    };
    
    /** Indices into the NODE_* arrays, by type */
    private static final int TRIANGLE_FAN = 0;
    private static final int TRIANGLE = 1;
    private static final int TRIANGLE_STRIP = 2;
    //private static final int LINE = 3;
    
    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;
    
    /** Index into the NODE_* arrays for the node type that is being translated */
    private int intercept_index;
    
    /** A stack of node wrappers */
    protected SimpleStack encStack;
    
    /** Node wrapper factory */
    protected EncodableFactory factory;
    
    /** Geometry node wrapper converter */
    protected GeometryConverter converter;
    
    /** The node that is being intercepted */
    protected Encodable node;
    
    /** Map of def'ed nodes keyed by DEF Name */
    protected HashMap<String, Encodable> defMap;
    
    /**
     * Default Constructor
     */
    public IndexFilter() {
        
        intercept = false;
        intercept_index = -1;
        
        encStack = new SimpleStack();
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
        
        fieldHandler = new FieldValueHandler(version, contentHandler);
        contentHandler.startDocument(uri, url, encoding, type, version, comment);
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {
        
        if (protoList.contains(name)) {
            protoStack.push(name);
            isProtoInstance = true;
        }
        String fieldName = null;
        if (!fieldStack.isEmpty() && !nodeStack.isEmpty()) {
            String nodeName = (String)nodeStack.peek();
            fieldName = (String)fieldStack.peek();
            parentTypeStack.push(fieldHandler.getFieldType(nodeName, fieldName));
        }
        if (!intercept) {
            // check if this is a node that must be translated
            for (int i = 0; i < NODE_SRC.length; i++) {
                if (name.equals(NODE_SRC[i])) {
                    intercept = true;
                    intercept_index = i;
                    node = factory.getEncodable(name, defName);
                    encStack.push(node);
                    if (defName != null) {
                        defMap.put(defName, node);
                    }
                }
            }
        } else {
            Encodable enc = factory.getEncodable(name, defName);
            Encodable parent = (Encodable)encStack.peek();
            if (parent != null) {
                parent.setValue(fieldName, enc);
            }
            encStack.push(enc);
            if (defName != null) {
                defMap.put(defName, enc);
            }
        }
        nodeStack.push(name);
        if (!intercept) {
            contentHandler.startNode(name, defName);
        }
    }
    
    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode() throws SAVException, VRMLException {
        
        String nodeName = (String)nodeStack.pop();
        if (!protoStack.isEmpty()) {
            String protoName = (String)protoStack.peek();
            if (nodeName.equals(protoName)) {
                protoStack.pop();
                if (protoStack.isEmpty()) {
                    isProtoInstance = false;
                }
            }
        }
        if (!parentTypeStack.isEmpty()) {
            int fieldType = ((Integer)parentTypeStack.pop()).intValue();
            if (fieldType == FieldConstants.SFNODE) {
                fieldHasEndedImplicitly = true;
                String fieldName = (String)fieldStack.pop();
            }
        }
        if (intercept) {
            Encodable enc = (Encodable)encStack.pop();
            if (nodeName.equals(NODE_SRC[intercept_index])) {
                convert();
                intercept = false;
                intercept_index = -1;
                node = null;
            }
        } else {
            contentHandler.endNode();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {
        
        fieldStack.push(name);
        
        if (!intercept) {
            contentHandler.startField(name);
        }
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
        
        if (intercept) {
            /////////////////////////////////////////////////////////
            // only nodes that we are intercepting are cached in the 
            // defMap, if a use is declared while intercepting for a
            // node that we have not cached - then the USE will not be
            // declared in the output file
            Encodable use = defMap.get(defName);
            /////////////////////////////////////////////////////////
            Encodable enc = (Encodable)encStack.peek();
            if ((enc != null) && (use != null)) {
                Encodable dup = use.clone();
                ((AbstractEncodable)dup).useName = defName;
                enc.setValue(fieldName, dup);
            }
        } else {
            contentHandler.useDecl(defName);
        }
    }
    
    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
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
        if (!intercept) {
            contentHandler.endField();
        }
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
    public void fieldValue(String value)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        
        if (isProtoInstance) {
            super.fieldValue(value);
            
        } else if (intercept) {
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
        } else {
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value);
        }
    }
    
    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values)
        throws SAVException, VRMLException {
        
        String fieldName = (String)fieldStack.pop();
        
        if (isProtoInstance) {
            super.fieldValue(values);
            
        } else if (intercept) {
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, values);
            }
        } else {
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, values);
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------
    
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
        
        if (isProtoInstance) {
            super.fieldValue(value, len);
            
        } else if (intercept) {
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        } else {
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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
        
        if (isProtoInstance) {
            super.fieldValue(value);
            
        } else if (intercept) {
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
        } else {
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        if (isProtoInstance) {
            super.fieldValue(value, len);
            
        } else if (intercept) {
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        } else {
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
        }
    }
    
    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------
    
    
    /**
     * Convert and encode the node.
     */
    private void convert() {
        
        switch(intercept_index) {
        case TRIANGLE_FAN:
            TriangleFanSet tfs = (TriangleFanSet)node;
            IndexedTriangleFanSet itfs = converter.toITFS(tfs);
            itfs.encode();
            break;
            
        case TRIANGLE:
            TriangleSet ts = (TriangleSet)node;
            IndexedTriangleSet its = converter.toITS(ts);
            its.encode();
            break;
            
        case TRIANGLE_STRIP:
            TriangleStripSet tss = (TriangleStripSet)node;
            IndexedTriangleStripSet itss = converter.toITSS(tss);
            itss.encode();
            break;
            /*
            case LINE:
        
            LineSet tss = (LineSet)node;
            IndexedLineSet itss = converter.toILS(ls);
            ils.encode();
            */
            //break;
        }
    }
}
