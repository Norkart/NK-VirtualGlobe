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

package xj3d.filter;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.parser.DefaultFieldParserFactory;

/**
 * Converts a IndexedFaceSet to a TriangleSet.
 * <p>
 *
 * Right now this code assumes the IFS is all triangles.  Later versions will
 * actually triangulate the code for you.  Must have trailing -1 on coordIndex
 * Doesn't handle USE coordinates.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class IFSToTSFilter extends AbstractFilter {

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

    /** A list of current defnames and URL's.  Non ImageTextures will be null. */
    private HashSet defNames;

    /** A mapping of urls to field values */
    private HashMap urlMap;

    /** A mapping of urls to def names */
    private HashMap urlDefMap;

    /** Are we inside an IndexedFaceSet */
    private boolean insideIFS;

    private VRMLFieldReader fieldReader;

    /**
     * Create a new default instance of this filter
     */
    public IFSToTSFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        fieldValuesStack = new SimpleStack();
        defStack = new SimpleStack();
        defNames = new HashSet();
        urlMap = new HashMap();
        urlDefMap = new HashMap();
        insideIFS = false;

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

        defNames.clear();
        urlMap.clear();
        urlDefMap.clear();
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
        if (defNames.contains(defName)) {
            System.out.println("Duplicate defName: " + defName);
        }

        if (name.equals("IndexedFaceSet")) {
            insideIFS = true;
            fieldValuesStack.push(new HashMap());
            if (defName != null)
                defNames.add(defName);

            defStack.push(defName);
            nodeStack.push(name);
            contentHandler.startNode("TriangleSet", defName);

            return;
        }

        if (defName != null)
            defNames.add(defName);

        defStack.push(defName);
        nodeStack.push(name);

        if (insideIFS)
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
        String nodeName = (String) nodeStack.pop();
        String defName = (String) defStack.pop();
        HashMap fieldValues;

        if (nodeName.equals("IndexedFaceSet")) {
            insideIFS = false;
            fieldValues = (HashMap) fieldValuesStack.pop();

            // Issue all other ITS fields

            int[] coordIndex = null;
            if (fieldValues.get("IndexedFaceSet.coordIndex") != null) {
                coordIndex = fieldReader.MFInt32((String)fieldValues.get("IndexedFaceSet.coordIndex"));
            }
            float[] coord = null;
            if (fieldValues.get("Coordinate.point") != null) {
                coord = fieldReader.MFVec3f((String)fieldValues.get("Coordinate.point"));
            }

            if (coord == null || coordIndex == null) {
                contentHandler.endNode();
                return;
            }

            int len = coordIndex.length;
            int idx = 0;

            int indices = (int) (len / 4f * 3);
            String coords[] = new String[indices * 3];

            if (len % 4 != 0)
                System.out.println("coordIndex not a multiple of 4");

            int cidx;

            for(int i=0; i < len; i++) {
                cidx = coordIndex[i];
                if (cidx == -1)
                    continue;

                coords[idx++] = String.valueOf(coord[cidx*3]);
                coords[idx++] = String.valueOf(coord[cidx*3+1]);
                coords[idx++] = String.valueOf(coord[cidx*3+2]);
            }

            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate",null);
            contentHandler.startField("point");

            if(contentHandler instanceof StringContentHandler) {
                ((StringContentHandler)contentHandler).fieldValue(coords);
            } else if(contentHandler instanceof BinaryContentHandler) {
System.out.println("IFSToTSFilter not hanlding binary coordinate data");
//                ((BinaryContentHandler)contentHandler).fieldValue(url, url.length);
            }
            contentHandler.endNode();

        }

        if (insideIFS)
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
        fieldStack.push(name);
        if (!insideIFS)
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
        if (!insideIFS)
            contentHandler.useDecl(defName);

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
        if (!insideIFS)
            contentHandler.endField();

        String name = (String) fieldStack.pop();
    }

    //-----------------------------------------------------------------------
    //Methods for interface RouteHandler
    //-----------------------------------------------------------------------

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
    public void fieldValue(String[] values)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        // flatten the array
        StringBuilder value  = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            value.append(values[i]);
            value.append(" ");
        }

        if (insideIFS) {
            HashMap fieldValues = (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
            if(contentHandler instanceof StringContentHandler) {
                ((StringContentHandler)contentHandler).fieldValue(values);
            } else if(contentHandler instanceof BinaryContentHandler) {
System.out.println("IFSToTSFilter not hanlding binary coordinate data");
//                ((BinaryContentHandler)contentHandler).fieldValue(url, url.length);
            }
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
        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideIFS) {
            HashMap fieldValues = (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
            if(contentHandler instanceof StringContentHandler) {
                ((StringContentHandler)contentHandler).fieldValue(value);
            } else if(contentHandler instanceof BinaryContentHandler) {
System.out.println("IFSToTSFilter not hanlding binary coordinate data");
//                ((BinaryContentHandler)contentHandler).fieldValue(url, url.length);
            }
        }
    }
}
