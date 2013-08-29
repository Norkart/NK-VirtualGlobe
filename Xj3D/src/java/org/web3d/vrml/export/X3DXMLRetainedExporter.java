/****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export;

// External imports
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.proto.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.vrml.export.compressors.*;

import org.web3d.util.IntHashMap;
import org.web3d.util.XMLTools;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.common.nodes.AbstractDynamicFieldNode;

/**
 * X3D XML exporter using a retained Scenegraph.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.30 $
 */
public class X3DXMLRetainedExporter extends X3DRetainedSAXExporter {
    /** The idenent String to replicate per level */
    private static String INDENT_STRING = "   ";

    /** An empty map to avoid null testing */
    private HashMap EMPTY_MAP = new HashMap();

    /** The current ident level */
    private int indent = 0;

    /** The current ident string */
    private String indentString;

    /** A mapping of ident to String */
    private IntHashMap indentMap;

    /** Temporary map during traversal for use references */
    private HashSet usedNodes;

    /** The printer */
    protected PrintWriter p;

    /** The passed in writer */
    protected Writer filterWriter;

    /** The current set of proto definitions */
    private HashSet protoDeclSet;

    /** Traverser for printing proto's */
    private SceneGraphTraverser traverser;

    /** The world root */
    private VRMLWorldRootNodeType root;

    /** Should we print the DOCTYPE */
    protected boolean printDocType;

    /** Should we use node compressors */
    protected boolean useNC = false;

    /** Should we ingore data, its handled by the compressor */
    private boolean ignoreData;

    /** Single level proto map  */
    protected HashMap protoMap;

    /** The current compressor.  Needs to change to a generic interface */
    private NodeCompressor currentCompressor;

    /** Switch between methods, should go away */
    private boolean compressedAttWay = false;

    /** The output stream */
    private OutputStream os;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     */
    public X3DXMLRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter) {

        super(major, minor, errorReporter, METHOD_STRINGS, 0);

        this.os = os;

        init();
    }

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param sigDigits The number of significant digits to use in printing floats
     */
    public X3DXMLRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int sigDigits) {

        super(major, minor, errorReporter, METHOD_STRINGS, 0, sigDigits);

        this.os = os;

        init();
    }

    /**
     *  Common initialization routine.
     */
    private void init() {
        encodingTo = ".x3d";
        printDocType = true;

        stripWhitespace = false;
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

        throws SAVException, VRMLException{

        super.startDocument(uri, url, encoding, type, version, comment);


        try {
            handler = new SAXPrinter(new OutputStreamWriter(os, "UTF8"), majorVersion, minorVersion, true);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
