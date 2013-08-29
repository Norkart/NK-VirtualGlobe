/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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
import java.util.*;

import javax.vecmath.*;

import java.text.NumberFormat;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.parser.DefaultFieldParserFactory;

import xj3d.filter.AbstractFilter;
import xj3d.filter.node.Coordinate;

/**
 * Generates normals if none are specified.  The only shape accepted at this time
 * is the IndexedTriangleSet
 * <p>
 *
 * This filter will take the average of all the normals of a point
 * as the normal provided.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class GenNormalsFilter extends AbstractFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The major version of the spec this file belongs to. */
    private int majorVersion;

    /** The minor version of the spec this file belongs to. */
    private int minorVersion;

    /** A stack of node names */
    private SimpleStack nodeStack;

    /** A stack of field names */
    private SimpleStack fieldStack;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** A stack of def names */
    private SimpleStack defStack;

    /** A mapping of defs to node names */
    private HashMap<String, String> defNodeMap;

    /** A mapping of def names to coordiante nodes */
    private HashMap<String, Coordinate> defCoordMap;

    /** A list of fields to pass though while in the ITS node */
    private HashSet<String> passThroughFields;

    /** Are we inside an IndexedTriangleSet */
    private boolean insideITS;

    private VRMLFieldReader fieldReader;

    /** Set the formating of numbers for output */
    private NumberFormat numberFormater;

    public GenNormalsFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        fieldValuesStack = new SimpleStack();
        defStack = new SimpleStack();
        defNodeMap = new HashMap<String, String>();
        defCoordMap = new HashMap<String, Coordinate>();
        insideITS = false;

        numberFormater = NumberFormat.getNumberInstance();
        numberFormater.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        numberFormater.setGroupingUsed(false);

        passThroughFields = new HashSet<String>();
        passThroughFields.add("ccw");
        passThroughFields.add("colorPerVertex");
        passThroughFields.add("normalPerVertex");
        passThroughFields.add("solid");

    }

    //----------------------------------------------------------
    // ContentHandler methods
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
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        contentHandler.startDocument(uri,url, encoding, type, version, comment);

        majorVersion = 3;
        minorVersion = 0;

        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            // Look at the 6th character to see the version number
            // ie "VRML V1.0" or "VRML V2.0"
            boolean is_20 = (version.charAt(1) == '2');

            if(is_20) {
                majorVersion = 2;
            }

        } else {
            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minorVersion = Integer.parseInt(minor_num);

        }

        DefaultFieldParserFactory fieldParserFactory = new DefaultFieldParserFactory();
        fieldReader = fieldParserFactory.newFieldParser(majorVersion, minorVersion);

    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endDocument() throws SAVException, VRMLException {

        contentHandler.endDocument();

        defNodeMap.clear();
        defCoordMap.clear();

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

//System.out.println("startNode " + name);

        if (defNodeMap.containsKey(defName)) {
            System.out.println("Duplicate defName: " + defName);
        }

        if (defName != null)
            defNodeMap.put(defName, name);

        if (name.equals("IndexedTriangleSet")) {
            insideITS = true;
            fieldValuesStack.push(new HashMap<String, Object>());
            defStack.push(defName);
            nodeStack.push(name);
            contentHandler.startNode("IndexedTriangleSet", defName);

            return;
        }


        defStack.push(defName);
        nodeStack.push(name);

        if (insideITS)
            return;

        contentHandler.startNode(name, defName);
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

        boolean useDef;

        String nodeName = (String) nodeStack.pop();
        String defName = (String) defStack.pop();
        //String fieldName = (String) fieldStack.peek();
        Coordinate coordNode = null;
        HashMap<String, Object> fieldValues;

//System.out.println("endNode " + nodeName);


        // Save the Coordinate data for later
        if ((defName != null) && nodeName.equals("Coordinate")) {

            fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();
            fieldValues.put("Coordinate.coord", defName);

            coordNode = new Coordinate(defName);
            coordNode.point = (float[])fieldValues.get("Coordinate.point");
            defCoordMap.put(defName, coordNode);

        }

        if (nodeName.equals("IndexedTriangleSet")) {
            insideITS = false;

            fieldValues = (HashMap<String, Object>) fieldValuesStack.pop();
            HashMap<Integer, Vector3f> normalMap = new HashMap<Integer, Vector3f>();

            // Issue all other ITS fields

            // process the index
            int[] inIndex = null;
            String[] outIndex = null;
            int maxIndex = 0;

            if (fieldValues.get("IndexedTriangleSet.index") != null) {

                inIndex = (int[])fieldValues.get("IndexedTriangleSet.index");

                int len = inIndex.length;
                outIndex = new String[len];
                for(int i=0; i < len; i++) {
                    outIndex[i] = String.valueOf(inIndex[i]);

//System.out.println("pre processing index " + inIndex[i]);


                    if (inIndex[i] > maxIndex) {
                        maxIndex = inIndex[i];
                    }

                }

            }

//System.out.println("max index " + maxIndex);

            // process the coords
            float[] inCoords = null;
            String[] outCoords = null;

            // get the coord def name
            String coordDefName = (String)fieldValues.get("Coordinate.coord");

            // try to get the data
            if (fieldValues.get("Coordinate.point") == null) {

                coordNode = defCoordMap.get(coordDefName);
                inCoords = coordNode.point;
                useDef = true;

            } else {

                inCoords = (float[])fieldValues.get("Coordinate.point");
                useDef = false;

            }

            if (inCoords != null) {

                int len = inCoords.length;
                outCoords = new String[len];
                for(int i=0; i < len; i++) {
                    outCoords[i] = numberFormater.format(inCoords[i]);
                }

            }

            // process the normals
            float[] inNormals = null;
            String[] outNormals = null;
            float[] floatNormals = null;

            if (fieldValues.get("Normal.vector") != null) {

                inNormals = (float[])fieldValues.get("Normal.vector");

                int len = inNormals.length;
                outNormals = new String[len];
                floatNormals = new float[len];
                for(int i=0; i < len; i++) {
                    outNormals[i] = numberFormater.format(inNormals[i]);
                    floatNormals[i] = inNormals[i];
                }

            } else {

                int c1, c2, c3;
                float x, y, z;
                int len = inIndex.length;

                normalMap.clear();

                // create the faces
                for(int i=0; i < len; i++) {

                    // get all 3 coordinates
                    c1 = inIndex[i++];
                    c2 = inIndex[i++];
                    c3 = inIndex[i];

//System.out.println("\nFace Index -> " + c1 + " " + c2 + " " + c3);

                    // create the vectors
                    x = inCoords[c1*3] - inCoords[c2*3];
                    y = inCoords[c1*3 + 1] - inCoords[c2*3 + 1];
                    z = inCoords[c1*3 + 2] - inCoords[c2*3 + 2];
                    Vector3f vec1 = new Vector3f(x, y, z);

                    x = inCoords[c3*3] - inCoords[c2*3];
                    y = inCoords[c3*3 + 1] - inCoords[c2*3 + 1];
                    z = inCoords[c3*3 + 2] - inCoords[c2*3 + 2];
                    Vector3f vec2 = new Vector3f(x, y, z);

                    // perform cross product to create the normal
                    Vector3f normal1 = new Vector3f();
                    Vector3f normal2;

                    normal1.cross(vec2, vec1);

                    if (normal1.x == 0 && normal1.y == 0 && normal1.z == 0) {
                        // Triangle is so small it has a 0 cross product, just use 0,0,1 as the normal

//System.out.println("Triangle is so small it has a 0 cross product");

                        normal1.z = 1;
                    } else {
                        normal1.normalize();
                    }

//System.out.println("Face Normal -> " + normal1.x + " " + normal1.y + " " + normal1.z);

                    // average normals for c1
                    if (normalMap.containsKey(c1)) {

                        normal2 = normalMap.get(c1);
                        normal2.add(normal1);
//System.out.println("Averaged Normal for " + c1 + " -> " + normal2.x + " " + normal2.y + " " + normal2.z);

                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
//System.out.println("Normal for " + c1 + " -> " + normal1.x + " " + normal1.y + " " + normal1.z);

                    }
                    normalMap.put(c1, normal2);


                    // average normals for c2
                    if (normalMap.containsKey(c2)) {

                        normal2 = normalMap.get(c2);
                        normal2.add(normal1);
//System.out.println("Averaged Normal for " + c2 + " -> " + normal2.x + " " + normal2.y + " " + normal2.z);

                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
//System.out.println("Normal for " + c2 + " -> " + normal1.x + " " + normal1.y + " " + normal1.z);

                    }
                    normalMap.put(c2, normal2);

                    // average normals for c3
                    if (normalMap.containsKey(c3)) {

                        normal2 = normalMap.get(c3);
                        normal2.add(normal1);
//System.out.println("Averaged Normal for " + c3 + " -> " + normal2.x + " " + normal2.y + " " + normal2.z);

                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
//System.out.println("Normal for " + c3 + " -> " + normal1.x + " " + normal1.y + " " + normal1.z);

                    }
                    normalMap.put(c3, normal2);

                }

                //len = inIndex.length;
                outNormals = new String[(maxIndex + 1) * 3];
                floatNormals = new float[(maxIndex + 1) * 3];

                for(int i=0; i <= maxIndex; i++) {

                    Vector3f normal = normalMap.get(i);

                    if (normal == null) {

                        outNormals[i*3] = "0";
                        outNormals[i*3 + 1] = "0";
                        outNormals[i*3 + 2] = "1";

                        // float form
                        floatNormals[i*3] = 0f;
                        floatNormals[i*3 + 1] = 0f;
                        floatNormals[i*3 + 2] = 1f;

                    } else {

                        normal.normalize();

                        // string form
                        outNormals[i*3] = numberFormater.format(normal.x);
                        outNormals[i*3 + 1] = numberFormater.format(normal.y);
                        outNormals[i*3 + 2] = numberFormater.format(normal.z);

                        // float form
                        floatNormals[i*3] = normal.x;
                        floatNormals[i*3 + 1] = normal.y;
                        floatNormals[i*3 + 2] = normal.z;

                    }

                }

            }

//System.out.println("    index count: " + outIndex.length);
//System.out.println("    normals count: " + outNormals.length);
//System.out.println("    coords count: " + outCoords.length);


            // add the index field
            contentHandler.startField("index");

            if(contentHandler instanceof StringContentHandler) {
                ((StringContentHandler)contentHandler).fieldValue(outIndex);
            } else if(contentHandler instanceof BinaryContentHandler) {
                ((BinaryContentHandler)contentHandler).fieldValue(inIndex, inIndex.length);
            }
            contentHandler.endField();

            // add the normal node
            contentHandler.startField("normal");
            contentHandler.startNode("Normal", null);
            contentHandler.startField("vector");

            if(contentHandler instanceof StringContentHandler) {
                ((StringContentHandler)contentHandler).fieldValue(outNormals);
            } else if(contentHandler instanceof BinaryContentHandler) {
                ((BinaryContentHandler)contentHandler).fieldValue(floatNormals, floatNormals.length);
            }
            contentHandler.endField();
            contentHandler.endNode();
            contentHandler.endField();

            // add the coord node
            contentHandler.startField("coord");
            if (useDef) {

                contentHandler.useDecl(coordDefName);

            } else {

                contentHandler.startNode("Coordinate", coordDefName);
                contentHandler.startField("point");
                if(contentHandler instanceof StringContentHandler) {
                    ((StringContentHandler)contentHandler).fieldValue(outCoords);
                } else if(contentHandler instanceof BinaryContentHandler) {
                    ((BinaryContentHandler)contentHandler).fieldValue(inCoords, inCoords.length);
                }
                contentHandler.endField();
                contentHandler.endNode();
                contentHandler.endField();
            }


        }

        if (insideITS)
            return;

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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {

//System.out.println("    startField " + name);

        fieldStack.push(name);
        if ((!insideITS) || (passThroughFields.contains(name)))
            contentHandler.startField(name);

    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void useDecl(String defName) throws SAVException, VRMLException {

        if (!insideITS)
            contentHandler.useDecl(defName);

        if (!fieldValuesStack.isEmpty()) {

            String nodeName = (String)defNodeMap.get(defName);
            String fieldName = (String) fieldStack.peek();
            String key = nodeName + "." + fieldName;

            HashMap<String, Object> fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();
            fieldValues.put(key, defName);

        }

        fieldStack.pop();

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
        String name = (String) fieldStack.pop();

//System.out.println("    endField " + name);

        if ((!insideITS) || (passThroughFields.contains(name)))
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

        if(contentHandler instanceof StringContentHandler) {

            String fieldName = (String) fieldStack.peek();
            String nodeName = (String) nodeStack.peek();

//System.out.println("        fieldValue " + nodeName + "." + fieldName);
//System.out.println("            insideITS " + insideITS);

            if (insideITS) {

                HashMap<String, Object> fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();

                if (nodeName.equals("IndexedTriangleSet") &&
                    fieldName.equals("index")) {

                    int[] index = fieldReader.MFInt32(value);
                    fieldValues.put(nodeName + "." + fieldName, index);

                } else if (nodeName.equals("Coordinate") &&
                    fieldName.equals("point")) {

                    float[] coords = fieldReader.MFVec3f(value);
                    fieldValues.put(nodeName + "." + fieldName, coords);

                } else if (nodeName.equals("Normal") &&
                    fieldName.equals("vector")) {

                    float[] normals = fieldReader.MFVec3f(value);
                    fieldValues.put(nodeName + "." + fieldName, normals);

                }

            } else {

//System.out.println("            value " + value);

                ((StringContentHandler)contentHandler).fieldValue(value);

            }

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

        if(contentHandler instanceof StringContentHandler) {

            if (insideITS) {

                // flatten the array
                StringBuilder value  = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    value.append(values[i]);
                    value.append(" ");
                }

                fieldValue(value.toString());

            } else {

                ((StringContentHandler)contentHandler).fieldValue(values);

            }

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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        if(contentHandler instanceof BinaryContentHandler) {

            String fieldName = (String) fieldStack.peek();
            String nodeName = (String) nodeStack.peek();

            if (nodeName.equals("IndexedTriangleSet") &&
                fieldName.equals("index")) {

                int[] index = new int[len];
                System.arraycopy(value, 0, index, 0, len);

                HashMap<String, Object> fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();
                fieldValues.put(nodeName + "." + fieldName, index);

            } else {

                ((BinaryContentHandler)contentHandler).fieldValue(value, len);

            }

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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value);

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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        if(contentHandler instanceof BinaryContentHandler) {

            String fieldName = (String) fieldStack.peek();
            String nodeName = (String) nodeStack.peek();

            if (nodeName.equals("Coordinate") &&
                    fieldName.equals("point")) {

                float[] coords = new float[len];
                System.arraycopy(value, 0, coords, 0, len);

                HashMap<String, Object> fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();
                fieldValues.put(nodeName + "." + fieldName, coords);

            } else if (nodeName.equals("Normal") &&
                    fieldName.equals("vector")) {

                float[] normals = new float[len];
                System.arraycopy(value, 0, normals, 0, len);

                HashMap<String, Object> fieldValues = (HashMap<String, Object>)fieldValuesStack.peek();
                fieldValues.put(nodeName + "." + fieldName, normals);

            } else {

                ((BinaryContentHandler)contentHandler).fieldValue(value, len);

            }

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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
    }

}
