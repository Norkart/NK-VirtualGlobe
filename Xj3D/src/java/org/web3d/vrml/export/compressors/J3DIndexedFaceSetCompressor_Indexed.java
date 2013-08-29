/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export.compressors;

// External imports
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


import com.sun.j3d.utils.compression.CompressedGeometryFile;
import com.sun.j3d.utils.compression.CompressionStream;
import com.sun.j3d.utils.compression.GeometryCompressor;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.norender.nodes.geom3d.NRIndexedFaceSet;
import org.web3d.vrml.renderer.j3d.J3DNodeFactory;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * A node compressor for IndexedFaceSet nodes.
 *
 * This will use Java3D's geometry compression.  Please note these methods are
 * covered by Patents held by Sun Microsystems and are only allowed in X3D
 * implementations.
 *
 * @author Alan Hudson.
 * @version $Revision: 1.5 $
 */
public class J3DIndexedFaceSetCompressor_Indexed implements SceneGraphCompressor {
    private static final int STACK_START_SIZE = 3;

    /** The increment size of the stack if it get overflowed */
    protected static final int STACK_INCREMENT = 5;

    private static final int NODE_IFS = 0;
    private static final int NODE_COLOR = 1;
    private static final int NODE_COORDINATE = 2;
    private static final int NODE_NORMAL = 3;
    private static final int NODE_META = 4;
    private static final int NODE_TEX_COORD = 5;
    private static final int NODE_MULTI_TEX_COORD = 6;
    private static final int NODE_TEX_COORD_GENERATOR = 7;
    private static final int NODE_COLOR_RGBA = 8;

    private static final int FIELD_COORD_INDEX = 0;
    private static final int FIELD_POINT = 1;

    private DataOutputStream dos;
    private VRMLFieldReader fieldParser;
    private VRMLNodeType node;
    private static HashMap nodes;
    private static HashMap fields;

    private int currentFieldIndex;
    private int currentNodeLevel;

    /** Copy of the top node for efficiency purposes */
    protected VRMLNodeType currentNode;

    private int[] coordIndex;
    private float[] point;

    /** The Java3D format to use */
    private int format;

    static {
        nodes = new HashMap();
        nodes.put("IndexedFaceSet", new Integer(NODE_IFS));
        nodes.put("Color",new Integer(NODE_COLOR));
        nodes.put("ColorRGBA",new Integer(NODE_COLOR_RGBA));
        nodes.put("Coordinate",new Integer(NODE_COORDINATE));
        nodes.put("Normal",new Integer(NODE_NORMAL));
        nodes.put("Meta",new Integer(NODE_META));
        nodes.put("TextureCoordinate",new Integer(NODE_TEX_COORD));
        nodes.put("MultiTextureCoordinate",new Integer(NODE_MULTI_TEX_COORD));
        nodes.put("TextureCoordinateGenerator",new Integer(NODE_TEX_COORD_GENERATOR));

        fields = new HashMap();
        fields.put("coordIndex", new Integer(FIELD_COORD_INDEX));
        fields.put("point", new Integer(FIELD_POINT));
    }

    public J3DIndexedFaceSetCompressor_Indexed() {
    }

    /**
     * Reinitialize this class for a new instance.
     *
     * @param dos The output stream to use.
     * @param vfr The field parser to use.
     */
    public void reinit(DataOutputStream dos, VRMLFieldReader vfr) {
        this.dos = dos;
        fieldParser = vfr;
    }

    /**
     * Can this NodeCompressor support this compression method
     *
     * @param nodeNumber What node, constant defined by Web3D Consortium
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     */
    public boolean canSupport(int nodeNumber, int method) {
        // TODO: get a real value
        return true;
    }

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    public void setDocumentLocator(Locator loc) {
    }

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
    public void startDocument(String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentName)
        throws SAVException, VRMLException {
    }

    /**
     * A META declaration has been found in the code. There may be zero
     * or more meta declarations in the file, appearing just after the
     * component declaration. Each meta declaration has a key and value
     * strings. No information is to be implied from this. It is for extra
     * data only.
     *
     * @param key The value of the key string
     * @param value The value of the value string
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void metaDecl(String key, String value)
        throws SAVException, VRMLException {
    }

    /**
     * An IMPORT declaration has been found in the document. All three
     * parameters will always be provided, regardless of whether the AS keyword
     * has been used or not. The parser implementation will automatically set
     * the local import name as needed.
     *
     * @param inline The name of the inline DEF nodes
     * @param exported The exported name from the inlined file
     * @param imported The local name to use for the exported name
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void importDecl(String inline, String exported, String imported)
        throws SAVException, VRMLException {
    }

    /**
     * An EXPORT declaration has been found in the document. Both paramters
     * will always be provided regardless of whether the AS keyword has been
     * used. The parser implementation will automatically set the exported
     * name as needed.
     *
     * @param defName The DEF name of the nodes to be exported
     * @param exported The name to be exported as
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void exportDecl(String defName, String exported)
        throws SAVException, VRMLException {
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

System.out.println("SN: " + name );

        int nodeType = ((Integer)nodes.get(name)).intValue();
        currentNodeLevel++;

        switch(nodeType) {
            case NODE_IFS:
                break;
            case NODE_COORDINATE:
                format = format | GeometryArray.COORDINATES;
                break;
            case NODE_COLOR:
                format = format | GeometryArray.COLOR_3;
                break;
            case NODE_COLOR_RGBA:
                format = format | GeometryArray.COLOR_4;
                break;
            case NODE_NORMAL:
                format = format | GeometryArray.NORMALS;
                break;
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
            currentNodeLevel--;

        if (currentNodeLevel == 0) {
            int len = coordIndex.length;
            int indices[] = new int[(int)(len * 0.75f)];
            int idx = 0;

            for(int i=0; i < len / 4; i++) {
                indices[idx++] = coordIndex[i*4];
                indices[idx++] = coordIndex[i*4+1];
                indices[idx++] = coordIndex[i*4+2];
            }

            len = indices.length;
/*
            System.out.println("Indices:");
            for(int i=0; i < len; i++) {
                System.out.print(indices[i] + " ");
            }
            System.out.println();
*/

            Shape3D[] shapes = new Shape3D[1];

            if (false) {
                GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
System.out.println("Coordinates: " + point.length + " indices: " + len);

                gi.setCoordinates(point);
                gi.setCoordinateIndices(indices);

               Stripifier st = new Stripifier(Stripifier.COLLECT_STATS);
               st.stripify(gi);

               StripifierStats stats = st.getStripifierStats();
               System.out.println("Stats: " + stats);

               shapes[0] = new Shape3D();
               shapes[0].setGeometry(gi.getGeometryArray());
            } else {
                format = GeometryArray.COORDINATES;
System.out.println("Coordinates: " + (point.length /3) + " indices: " + len);
                IndexedTriangleArray ita = new IndexedTriangleArray(point.length / 3, format, len);
                ita.setCoordinates(0, point);
                ita.setCoordinateIndices(0, indices);

                // TODO: Need a way to determine this
                shapes[0] = new Shape3D();
                shapes[0].setGeometry(ita);
            }
    //            CompressionStream cs = new CompressionStream(16,9,6,shapes);
    //            CompressionStream cs = new CompressionStream(10,8,6,shapes);

            CompressionStream cs = new CompressionStream(16,9,6,shapes);
            GeometryCompressor gc = new GeometryCompressor();

/*
            CompressedGeometryFile gfc = new CompressedGeometryFile("out.j3d", true);
            gfc.clear();


            gc.compress(cs,gfc);
            } catch(Exception e) {
                e.printStackTrace();
            }
*/
            CompressedGeometry cgeom = gc.compress(cs);

            CompressedGeometryHeader header = new CompressedGeometryHeader();
            cgeom.getCompressedGeometryHeader(header);

            len = cgeom.getByteCount();
            byte[] bytes = new byte[len];
            cgeom.getCompressedGeometry(bytes);

            System.out.println("Len: " + len);

            try {

    System.out.println("bufferType: " + header.bufferType);
    System.out.println("bufferDataPresent: " + header.bufferDataPresent);
    System.out.println("size: " + header.size);
    System.out.println("start: " + header.start);
    System.out.println("lower: " + header.lowerBound);
    System.out.println("upper: " + header.upperBound);

                dos.writeInt(header.bufferType);
                dos.writeInt(header.bufferDataPresent);
                dos.writeInt(header.size);
                dos.writeInt(header.start);

                // TODO: Do we really need doubles here?
                dos.writeDouble(header.lowerBound.x);
                dos.writeDouble(header.lowerBound.y);
                dos.writeDouble(header.lowerBound.z);
                dos.writeDouble(header.upperBound.x);
                dos.writeDouble(header.upperBound.y);
                dos.writeDouble(header.upperBound.z);

                dos.write(bytes);
            } catch(IOException ioe) {
                ioe.printStackTrace();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {

        Integer field = (Integer) fields.get(name);
        int fieldIdx;

        if (field == null) {
            System.out.println("Unhandled field: " + name);
            currentFieldIndex = -1;
            return;
        }

        currentFieldIndex = field.intValue();
    }

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String value) throws SAVException, VRMLException {
        int type=0;

        switch(currentFieldIndex) {
            case FIELD_COORD_INDEX:
                type = FieldConstants.MFINT32;
                break;
            case FIELD_POINT:
                type = FieldConstants.MFVEC3F;
                break;
            default:
                return;
        }

        parseField(currentNode, currentFieldIndex, type , value);
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        int type=0;

        switch(currentFieldIndex) {
            case FIELD_COORD_INDEX:
                type = FieldConstants.MFINT32;
                break;
            case FIELD_POINT:
                type = FieldConstants.MFVEC3F;
                break;
            default:
                return;
        }

        parseField(currentNode, currentFieldIndex, type, values);
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
        System.out.println("Not handling USE in: " + this);
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
    }

    //----------------------------------------------------------
    // Methods required by the BinaryContentHandler interface.
    //----------------------------------------------------------

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

        currentNode.setValue(currentFieldIndex, value);
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

        currentNode.setValue(currentFieldIndex, value, len);
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

        currentNode.setValue(currentFieldIndex, value);
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

        currentNode.setValue(currentFieldIndex, value, len);
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

        currentNode.setValue(currentFieldIndex, value);
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

        currentNode.setValue(currentFieldIndex, value, len);
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

        currentNode.setValue(currentFieldIndex, value);
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

        currentNode.setValue(currentFieldIndex, value, len);
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

        currentNode.setValue(currentFieldIndex, value);
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

        currentNode.setValue(currentFieldIndex, value, len);
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

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Convenience method to parse a field string and set it in the destination
     * node.
     *
     * @param node The node to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLNodeType node,
                            int index,
                            int fieldType,
                            String value) {

        switch(fieldType) {
            case FieldConstants.SFINT32:
                node.setValue(index, fieldParser.SFInt32(value));
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFFLOAT:
                node.setValue(index, fieldParser.SFFloat(value));
                break;

            case FieldConstants.SFTIME:
                node.setValue(index, fieldParser.SFTime(value));
                break;

            case FieldConstants.SFDOUBLE:
                node.setValue(index, fieldParser.SFDouble(value));
                break;

            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFLONG:
                node.setValue(index, fieldParser.SFLong(value));
                break;

            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                node.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFBOOL:
                node.setValue(index, fieldParser.SFBool(value));
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                node.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFSTRING:
                node.setValue(index, fieldParser.SFString(value));
                break;

            case FieldConstants.MFSTRING:
                String[] s_val = fieldParser.MFString(value);
                node.setValue(index, s_val, s_val.length);
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                System.out.println("Unknown field type provided!" + fieldType);
        }
    }

    /**
     * Convenience method to parse a field from an array of strings and set
     * it in the destination node. Not all fields can be handled by this form.
     *
     * @param node The node to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLNodeType node,
                            int index,
                            int fieldType,
                            String[] value) {
        // If the length is zero, ignore the parse request
        if(value.length == 0)
            return;

        switch(fieldType) {
            case FieldConstants.SFINT32:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
            case FieldConstants.SFLONG:
            case FieldConstants.SFBOOL:
            case FieldConstants.SFSTRING:
                System.out.println("Field not parsable as String[]");
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                node.setValue(index, i_val, i_val.length);
                break;


            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                node.setValue(index, d_val, d_val.length);
                break;


            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                node.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                node.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFSTRING:
                String[] s_val = fieldParser.MFString(value);
                node.setValue(index, s_val, s_val.length);
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                System.out.println("Unknown field type provided!" + fieldType);
        }
    }
}
