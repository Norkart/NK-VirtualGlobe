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

// Local imports
import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.ScriptHandler;
import org.web3d.vrml.sav.SAVException;

import xj3d.filter.AbstractFilter;
import xj3d.filter.FieldValueHandler;

/**
 * A filter which leaves field values unchanged, but converts the data 
 * representation from String to binary or vice-versa as determined
 * by the content handler's type.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class TypeConversionFilter extends AbstractFilter {
    
    /** A stack of node names */
    protected SimpleStack nodeStack;
    
    /** A stack of field names */
    protected SimpleStack fieldStack;
    
    /** A stack of parent node types */
    protected SimpleStack parentTypeStack;
    
    /** The field value handler */
    protected FieldValueHandler fieldHandler;
    
    /** A stack of proto instance names */
    protected SimpleStack protoStack;
    
    /** List of Proto and externProto declation names */
    protected ArrayList protoList;
    
    /** Flag indicating that we're inside a proto instance */
    protected boolean isProtoInstance;
    
    /** Flag indicating that an SFNode has ended and implicitly this
    *  means that the parent field of the node has ended as well */
    protected boolean fieldHasEndedImplicitly;
    
    /**
     * Create an instance of the filter.
     */
    public TypeConversionFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        protoStack = new SimpleStack();
        parentTypeStack = new SimpleStack();
        protoList = new ArrayList();
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {
        
        if (protoList.contains(name)) {
            protoStack.push(name);
            isProtoInstance = true;
        }
        if (!fieldStack.isEmpty() && !nodeStack.isEmpty()) {
            String nodeName = (String)nodeStack.peek();
            String fieldName = (String)fieldStack.peek();
            parentTypeStack.push(fieldHandler.getFieldType(nodeName, fieldName));
        }
        nodeStack.push(name);
        contentHandler.startNode(name, defName);
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
        contentHandler.endNode();
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
        contentHandler.startField(name);
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
        
        String name = (String)fieldStack.pop();
        contentHandler.useDecl(defName);
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
        contentHandler.endField(); 
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(values);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, values);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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
        
        String nodeName = (String)nodeStack.peek();
        String fieldName = (String)fieldStack.pop();
        
        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else {
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
        }
    }
    
    //---------------------------------------------------------------
    // Methods defined by ProtoHandler
    //---------------------------------------------------------------
    
    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void startProtoDecl(String name) throws SAVException, VRMLException {
        protoList.add(name);
        super.startProtoDecl(name);
    }
    
    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        protoList.add(name);
        super.startExternProtoDecl(name);
    }
}
