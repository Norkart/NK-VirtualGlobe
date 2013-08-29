/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
import java.io.IOException;
import java.io.DataOutputStream;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.norender.nodes.geom3d.NRIndexedFaceSet;

/**
 * A node compressor for IndexedFaceSet nodes.
 *
 * Index fields will be compressed using delta encoding and by removing -1 if the
 * faces are all the same size..
 *
 * This will use the Java3D compression utilities only if the Coordinate
 * node is not USED.
 *
 * @author Alan Hudson.
 * @version $Revision: 1.5 $
 */
//public class IndexedFaceSetCompressor implements SceneGraphCompressor {
public class IndexedFaceSetCompressor implements NodeCompressor {

    private DataOutputStream dos;
    private VRMLFieldReader fieldParser;
    private VRMLNodeType node;
    private int currentField;
    private int vfCcw;
    private int vfColorIndex;
    private int vfColorPerVertex;
    private int vfConvex;
    private int vfCoordIndex;
    private int vfCreaseAngle;
    private int vfNormalIndex;
    private int vfNormalPerVertex;
    private int vfSolid;
    private int vfTexCoordIndex;

    public IndexedFaceSetCompressor() {
        node = new NRIndexedFaceSet();
        vfCcw = node.getFieldIndex("ccw");
        vfColorIndex = node.getFieldIndex("colorIndex");
        vfColorPerVertex = node.getFieldIndex("colorPerVertex");
        vfConvex = node.getFieldIndex("convex");
        vfCoordIndex = node.getFieldIndex("coordIndex");
        vfCreaseAngle = node.getFieldIndex("creaseAngle");
        vfNormalIndex = node.getFieldIndex("normalIndex");
        vfNormalPerVertex = node.getFieldIndex("normalPerVertex");
        vfSolid = node.getFieldIndex("solid");
        vfTexCoordIndex = node.getFieldIndex("texCoordIndex");
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
        currentField = node.getFieldIndex(name);
        try {
            dos.writeByte(currentField);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
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
        try {
        if (currentField == vfCcw || currentField == vfColorPerVertex ||
            currentField == vfConvex || currentField == vfNormalPerVertex ||
            currentField == vfSolid) {

            boolean bval = fieldParser.SFBool(value);
            dos.writeBoolean(bval);
        } else if (currentField == vfCreaseAngle) {
            float fval = fieldParser.SFFloat(value);
            dos.writeFloat(fval);
        } else if (currentField == vfColorIndex || currentField == vfNormalIndex ||
                   currentField == vfTexCoordIndex) {

            int[] ival = fieldParser.MFInt32(value);
            // Scan for fixed sized polys of 3 or 4 vertices
            int cnt=0;
            int size=-1;
            boolean fixed=true;
            for(int i=0; i < ival.length; i++) {
                if (ival[i] == -1) {
                    if (size != -1 && cnt != size) {
                        fixed = false;
                        break;
                    }
                    size = cnt;
                    cnt = 0;
                } else {
                    cnt++;
                }
            }

            int lastMarker;
            if (ival[ival.length -1] == -1)
                lastMarker = 0;
            else
                lastMarker = -1;

            if (size == -1)
                size = cnt;
            // Check the last as it might not have a -1
            if (lastMarker != 0 && cnt != size) {
                fixed = false;
                System.out.println("last face not fixed:  cnt:" + cnt + " size: " + size);
            }

            if (fixed) {
                // Maybe convert to 2 bits(0-4?)
                dos.writeByte(size);
                int numElements = (int) Math.ceil(ival.length / (size + 1));
                int[] strippedVals = new int[numElements * size];
                int pos=0;
                for(int i=0; i < ival.length / size + lastMarker; i++) {
                    if (i % (size - 1) != 0) {
                        strippedVals[pos++] = ival[i];
                    }
                }
                CompressionTools.rangeCompressIntArray(dos,null,true,1,strippedVals);
            } else {
                System.out.println("not fixed IFS");
                fixed = false;
                dos.writeByte(0);
                CompressionTools.rangeCompressIntArray(dos,null,true,1,ival);
            }
        }
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {
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
}
