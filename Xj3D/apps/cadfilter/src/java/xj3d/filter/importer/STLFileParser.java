/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
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
import java.net.URL;
import java.text.NumberFormat;

import org.j3d.loaders.stl.STLFileReader;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.StringArray;
import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser implementation that reads STL files and generates an X3D stream
 * of events.
 * <p>
 *
 * @author Justin Couch
 * @version Grammar $Revision: 1.4 $
 */
public class STLFileParser implements NonWeb3DFileParser {

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

    /**
     * Create a new instance of this parser
     */
    public STLFileParser() {
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
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    public void parse(InputSource input)
        throws IOException, VRMLException {

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some of the STL parser to deal with this.
        URL url = new URL(input.getURL());

        STLFileReader reader = new STLFileReader(url);

        contentHandler.startDocument(input.getURL(),
                                     input.getBaseURL(),
                                     "utf8",
                                     "#X3D",
                                     "V3.0",
                                     "Auto converted STL file");

        contentHandler.profileDecl("Interchange");
        contentHandler.componentDecl("Rendering:3");

        generateTriSet(reader);

        contentHandler.endDocument();
        reader.close();
    }

    /**
     * Generate the coordinate and normal information for the TriangleSet node
     * based on that read from the STL file.
     */
    private void generateTriSet(STLFileReader rdr)
        throws IOException {

        int num_objects = rdr.getNumOfObjects();
        int[] num_tris = rdr.getNumOfFacets();
        String[] obj_names = rdr.getObjectNames();
        int max_tris = 0;

        for(int j = 0; j < num_objects; j++) {
            if(num_tris[j] > max_tris)
                max_tris = num_tris[j];
        }

        double[] in_normal = new double[3];
        double[][] in_coords = new double[3][3];

        // Tweak the objectNames into something that is acceptable to
        // use as a DEF name ID.

        for(int i = 0; i < num_objects; i++) {
            if(obj_names[i] != null) {
                obj_names[i] = obj_names[i].replace('.', '_');
            }
        }

        if(contentHandler instanceof BinaryContentHandler) {

            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;

            float[] out_coords = new float[max_tris * 3];
            float[] out_normals = new float[max_tris * 3];

            for(int i = 0; i < num_objects; i++) {
                int idx = 0;

                contentHandler.startNode("Shape", obj_names[i]);
                contentHandler.startField("geometry");
                contentHandler.startNode("TriangleSet", null);

                for(int j = 0; j < num_tris[i]; j++) {

                    rdr.getNextFacet(in_normal, in_coords);

                    out_normals[idx] = (float)in_normal[0];
                    out_normals[idx + 1] = (float)in_normal[1];
                    out_normals[idx + 2] = (float)in_normal[2];

                    out_normals[idx + 3] = (float)in_normal[0];
                    out_normals[idx + 4] = (float)in_normal[1];
                    out_normals[idx + 5] = (float)in_normal[2];

                    out_normals[idx + 6] = (float)in_normal[0];
                    out_normals[idx + 7] = (float)in_normal[1];
                    out_normals[idx + 8] = (float)in_normal[2];

                    out_coords[idx] = (float)in_coords[0][0];
                    out_coords[idx + 1] = (float)in_coords[0][1];
                    out_coords[idx + 2] = (float)in_coords[0][2];

                    out_coords[idx + 3] = (float)in_coords[1][0];
                    out_coords[idx + 4] = (float)in_coords[1][1];
                    out_coords[idx + 5] = (float)in_coords[1][2];

                    out_coords[idx + 6] = (float)in_coords[2][0];
                    out_coords[idx + 7] = (float)in_coords[2][1];
                    out_coords[idx + 8] = (float)in_coords[2][2];

                    idx += 9;
                }

                contentHandler.startField("normalPerVertex");
                bch.fieldValue(false);
                contentHandler.endField();

                contentHandler.startField("coord");
                contentHandler.startNode("Coordinate", null);
                contentHandler.startField("point");
                bch.fieldValue(out_coords, idx);
                contentHandler.endField();
                contentHandler.endNode();
                contentHandler.endField();

                contentHandler.startField("normal");
                contentHandler.startNode("Normal", null);
                contentHandler.startField("vector");
                bch.fieldValue(out_normals, idx);
                contentHandler.endField();
                contentHandler.endNode();
                contentHandler.endField();
            }

            contentHandler.endNode();
            contentHandler.endField();
            contentHandler.endNode();
        } else {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            StringArray out_coords = new StringArray();
            StringArray out_normals = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(5);

            for(int i = 0; i < num_objects; i++) {
                out_coords.clear();
                out_normals.clear();

                contentHandler.startNode("Shape", obj_names[i]);
                contentHandler.startField("geometry");
                contentHandler.startNode("TriangleSet", null);

                for(int j = 0; j < num_tris[i]; j++) {

                    rdr.getNextFacet(in_normal, in_coords);

                    String n1 = formatter.format(in_normal[0]);
                    String n2 = formatter.format(in_normal[1]);
                    String n3 = formatter.format(in_normal[2]);

                    out_normals.add(n1);
                    out_normals.add(n2);
                    out_normals.add(n3);

                    out_normals.add(n1);
                    out_normals.add(n2);
                    out_normals.add(n3);

                    out_normals.add(n1);
                    out_normals.add(n2);
                    out_normals.add(n3);

                    out_coords.add(formatter.format(in_coords[0][0]));
                    out_coords.add(formatter.format(in_coords[0][1]));
                    out_coords.add(formatter.format(in_coords[0][2]));

                    out_coords.add(formatter.format(in_coords[1][0]));
                    out_coords.add(formatter.format(in_coords[1][1]));
                    out_coords.add(formatter.format(in_coords[1][2]));

                    out_coords.add(formatter.format(in_coords[2][0]));
                    out_coords.add(formatter.format(in_coords[2][1]));
                    out_coords.add(formatter.format(in_coords[2][2]));
                }

                contentHandler.startField("coord");
                contentHandler.startNode("Coordinate", null);
                contentHandler.startField("point");
                sch.fieldValue(out_coords.toArray());
                contentHandler.endField();
                contentHandler.endNode();
                contentHandler.endField();

                contentHandler.startField("normal");
                contentHandler.startNode("Normal", null);
                contentHandler.startField("vector");
                sch.fieldValue(out_normals.toArray());
                contentHandler.endField();
                contentHandler.endNode();
                contentHandler.endField();

                contentHandler.endNode();
                contentHandler.endField();
                contentHandler.endNode();
            }
        }
    }
}
