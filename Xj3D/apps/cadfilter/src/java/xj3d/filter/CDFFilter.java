/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
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
import java.io.*;
import java.util.*;

import org.ietf.uri.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

// Local imports

import org.web3d.vrml.export.*;

import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;

import org.web3d.util.ErrorReporter;
import org.web3d.browser.BrowserCore;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.JavascriptResourceFactory;

import org.web3d.parser.DefaultVRMLParserFactory;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;

import xj3d.filter.exporter.STLFileExporter;

/**
 * General X3D filter driver. Takes an input file or URL, filters
 * it and writes it out to a file.
 *
 * @author Alan Hudson
 * @version $Revision: 1.31 $
 */
public class CDFFilter {

    /** Error message when class can't be found */
    private static final String CREATE_MSG =
        "New node instantiation exception";

    /** Error message for constructor having non-public access type */
    private static final String ACCESS_MSG =
        "New now IllegalAccess exception";

    /** Error message for Parsing format error */
    private static final String PARSER_VRMLEXCEPTION_MSG =
        "File format error encountered";

    /** Error message for Parsing IO error */
    private static final String PARSER_IOEXCEPTION_MSG =
        "IO error encountered";

    /** Exit code for general error condition */
    public static final int ERROR = -1;

    /** Exit code for success condition */
    public static final int SUCCESS = 0;

    /** Exit code for input file not found condition */
    public static final int INPUT_FILE_NOT_FOUND = 1;

    /** Exit code for invalid input file format condition */
    public static final int INVALID_INPUT_FILE_FORMAT = 2;

    /** Exit code for output file write error condition */
    public static final int OUTPUT_FILE_ERROR = 5;

    /** Exit code for invalid filter argument condition */
    public static final int INVALID_FILTER_ARGUMENTS = 6;

    /** Exit code for invalid filter specified condition */
    public static final int INVALID_FILTER_SPECIFIED = 7;

    // Default Largest acceptable error for float quantization
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    /** List of known filter class names, mapped from a short name */
    private HashMap<String, String> filters;

    /** The compression method to use for binary */
    private int compressionMethod;

    /** The float lossy param */
    private float quantizeParam;

    /** Should we upgrade old protos to X3D native */
    private boolean upgradeContent;

    /** Output for sending messages to the outside world */
    private ErrorReporter console;

    /**
     * Create an instance of the demo class.
     */
    public CDFFilter() {
        setupProperties();
        initFilters();

        ParserNameMap content_map = new ParserNameMap();
        content_map.registerType("stl", "model/x-stl");
        content_map.registerType("dae", "application/xml");
        content_map.registerType("DAE", "application/xml");

        URI.setFileNameMap(content_map);
    }

    /**
     * Set the compression method to use for binary compression.
     *
     * @param method The compression method, defined in X3DBinarySerializer
     */
    public void setCompressionMethod(int method) {
        compressionMethod = method;
    }

    /**
     * Set the maximum desired quantization loss when using lossy compression.
     *
     * @param max The maximum loss
     */
    public void setQuantizationParam(float max) {
        quantizeParam = max;
    }

    /**
     * Should old VRML PROTO's like h-anim, geovrml be upgraded to X3D native
     * nodes.
     *
     * @param upgrade TRUE to upgrade content.
     */
    public void setUpgradeContent(boolean upgrade) {
        upgradeContent = upgrade;
    }

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param filters The identifier of the filter type.
     * @param url The URL to open.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, URL url, String out, String[] fargs) {
        return load(filters, url, null, out, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file The file to load.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, File file, String out, String[] fargs) {
        return load(filters, null, file, out, fargs);
    }

    /**
     * Print out the filters available.
     *
     */
    public void printFilters() {

        Set ks = filters.keySet();
        Iterator itr = ks.iterator();

        System.out.println("Available filters:");
        while(itr.hasNext()) {
            System.out.println("   " + itr.next());
        }

    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Initialize the filters.
     *
     */
    private void initFilters() {

        filters = new HashMap();
        filters.put("Identity", "xj3d.filter.IdentityFilter");
        filters.put("DEFUSEImageTexture", "xj3d.filter.DEFUSEImageTextureFilter");
        filters.put("IFSToTS", "xj3d.filter.IFSToTSFilter");
        filters.put("IFSToITS", "xj3d.filter.IFSToITSFilter");
        filters.put("Debug", "xj3d.filter.filters.DebugFilter");
        filters.put("GenNormals", "xj3d.filter.filters.GenNormalsFilter");
        filters.put("CombineShapes", "xj3d.filter.filters.CombineShapeFilter");
        filters.put("ShortenDEF", "xj3d.filter.ShortenDEFFilter");
        filters.put("MinProfile", "xj3d.filter.MinimizeProfileFilter");
        filters.put("AbsScale", "xj3d.filter.AbsScaleFilter");
        filters.put("FlattenTransform", "xj3d.filter.filters.FlattenTransformFilter");
        filters.put("Index", "xj3d.filter.filters.IndexFilter");
        filters.put("ReIndex", "xj3d.filter.filters.ReindexFilter");
        filters.put("Triangulation", "xj3d.filter.filters.TriangulationFilter");
        filters.put("TriangleCountInfo", "xj3d.filter.TriangleCountInfoFilter");
        filters.put("ModifyViewpoint", "xj3d.filter.ModifyViewpointFilter");

    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param filter The identifier of the filter type.
     * @param url The URL to open, or null if the input is specified by the file argument.
     * @param inFile The file to load, or null if the input is specified by the url argument.
     * @param out The output filename.
     * @param filter_args The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private int load(String[] filterNames,
                     URL url,
                     File inFile,
                     String out,
                     String[] filterArgs) {

        int log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;

        // find the log level. Should always be the first argument, if supplied.
        if(filterArgs.length > 2) {
            if(filterArgs[0].equals("-loglevel")) {
                String lvl = filterArgs[1];
                if(lvl.equals("ALL"))
                    log_level = FilterErrorReporter.PRINT_ALL;
                else if(lvl.equals("WARNINGS"))
                    log_level = FilterErrorReporter.PRINT_WARNINGS;
                else if(lvl.equals("ERRORS"))
                    log_level = FilterErrorReporter.PRINT_ERRORS;
                else if(lvl.equals("FATAL"))
                    log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;
                else if(lvl.equals("NONE"))
                    log_level = FilterErrorReporter.PRINT_NONE;
            }
        }

        console = new FilterErrorReporter(log_level);

        AbstractFilter previous_filter = null;
        AbstractFilter last_filter = null;
        AbstractFilter first_filter = null;

        for(int i = 0; i < filterNames.length; i++) {
            String filter_name = filters.get(filterNames[i]);

            // If not one of the stock ones, try seeing if it is a class name
            // and load it directly as one.

            if (filter_name == null) {
                filter_name = filterNames[i];
            }

            AbstractFilter filter = loadFilterClass(filter_name);

            if (filter == null) {
                return INVALID_FILTER_SPECIFIED;
            }

            if(filter instanceof TwoPassFilter) {
                filter = new TwoPassFilterWrapper((TwoPassFilter)filter);
            }

            try {
                filter.setArguments(filterArgs);
            } catch (IllegalArgumentException iae) {
                return INVALID_FILTER_ARGUMENTS;
            }

// Do we have one of these yet?
//            filter.setLocator();
            filter.setErrorReporter(console);

            if(i != 0) {
                previous_filter.setContentHandler(filter);
                previous_filter.setScriptHandler(filter);
                previous_filter.setProtoHandler(filter);
                previous_filter.setRouteHandler(filter);
            } else {
                first_filter = filter;
            }

            previous_filter = filter;
            last_filter = filter;
        }

        InputSource is = null;
        if (url != null) {
            is = new InputSource(url);
        } else if (inFile != null) {
            is = new InputSource(inFile);
        }

        File tmpOutFile = null;
        File outFile = new File(out);
        if ((inFile != null) && (inFile.equals(outFile))) {
            // the input and output files are the same, arrange
            // for the output to be written to a tmp file
            tmpOutFile = new File(outFile.getParentFile(), "tmp_"+outFile.getName());
        }

        FileOutputStream fos = null;
        OutputStream outputStream = null;

        try {
            if (tmpOutFile != null) {
                fos = new FileOutputStream(tmpOutFile);
            } else {
                fos = new FileOutputStream(outFile);
            }
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return OUTPUT_FILE_ERROR;
        }

        int idxDot = out.lastIndexOf(".");
        if (idxDot < 0) {
            console.messageReport("Unknown destination file type");
            return OUTPUT_FILE_ERROR;
        }

        String encoding = out.substring(idxDot+1);
        boolean gzipCompression = false;

        // Forcing gzip compression? Let's go find the next index of the
        // period to find out the raw encoding to be used.
        if (encoding.equals("gz")) {
            gzipCompression = true;
            String baseFilename = out.substring(0, idxDot-1);

            idxDot = baseFilename.lastIndexOf(".");
            encoding = baseFilename.substring(idxDot+1);

        } else if (encoding.equals("x3dvz") || encoding.equals(".x3dz")) {
            gzipCompression = true;
        }

        console.messageReport("Encoding: " + encoding);

        // If gzip is needed, change the output stream to wrap it in a
        // compressed version.
        if (gzipCompression) {
            try {
                outputStream = new GZIPOutputStream(fos);
            } catch (IOException ioe) {
                console.warningReport("Unable to create GZIP output", ioe);
                outputStream = fos;
            }
        } else {
            outputStream = fos;
        }


        // JC:
        // Fixed output for the spec version. Not so good. We should have some
        // sort of flags that allow us to specify which spec version we want as
        // output and then let the stream handle it.

        Exporter writer = null;

        if (encoding.equals("x3db")) {
            writer = new X3DBinaryRetainedDirectExporter(outputStream,3,1, console, compressionMethod, quantizeParam);
            ((X3DBinaryRetainedDirectExporter)writer).setConvertOldContent(upgradeContent);
        } else if (encoding.equals("x3dv")) {
            writer = new X3DClassicRetainedExporter(outputStream,3,1,console);
            ((X3DClassicRetainedExporter)writer).setConvertOldContent(upgradeContent);
        } else if (encoding.equals("x3d")) {
            writer = new X3DXMLRetainedExporter(outputStream,3,1,console);
            ((X3DXMLRetainedExporter)writer).setConvertOldContent(upgradeContent);
        } else if (encoding.equals("stl")) {
            writer = new STLFileExporter(outputStream,3,1,console);
        } else {
            console.fatalErrorReport("Unknown destination encoding", null);
            return OUTPUT_FILE_ERROR;
        }

        last_filter.setContentHandler(writer);
        last_filter.setRouteHandler(writer);
        last_filter.setScriptHandler(writer);
        last_filter.setProtoHandler(writer);

        FileParserReader reader = new FileParserReader();
        reader.registerImporter("model/x-stl", "xj3d.filter.importer.STLFileParser");
        reader.registerImporter("application/xml", "xj3d.filter.importer.ColladaFileParser");

        reader.setContentHandler(first_filter);
        reader.setRouteHandler(first_filter);
        reader.setScriptHandler(first_filter);
        reader.setProtoHandler(first_filter);

        try {
            reader.parse(is);
        } catch(Exception e) {
            if(e instanceof FilterProcessingException) {
                FilterProcessingException fpe = (FilterProcessingException)e;

                console.fatalErrorReport("Filter Error for " +
                                         fpe.getFilterName(), null);
                return fpe.getErrorCode();
            } else if(e instanceof VRMLException){
                console.fatalErrorReport(PARSER_VRMLEXCEPTION_MSG, e);
                return INVALID_INPUT_FILE_FORMAT;
            } else if (e instanceof IOException) {
                console.fatalErrorReport(PARSER_IOEXCEPTION_MSG, e);
                return ERROR;
            } else {
                // something unexpected.....
                e.printStackTrace();
                return ERROR;
            }
        }

        try {
            // clean up...
            is.close();
            outputStream.flush();
            outputStream.close();
        } catch ( IOException ioe ) {
        }

        if (tmpOutFile != null) {
            // the presence of a tmpOutFile means that what we really
            // want is to replace the input file with the output
            if (!inFile.delete()) {
                console.fatalErrorReport("Could not delete original input file " +
                                         inFile, null);
            } else if (!tmpOutFile.renameTo(outFile)) {
                console.fatalErrorReport("Could not rename tmp output file " +
                                         tmpOutFile, null);
            }
        }

        return SUCCESS;
    }

    /**
     * Load a filter class from the given class name. If the filter cannot be
     * loaded, null is returned.
     *
     * @param classname The fully qualified name of the class needed
     * @return The filter loaded up from the class name
     */
    private AbstractFilter loadFilterClass(String classname) {
        AbstractFilter ret_val = null;

        try {
            Class cls = Class.forName(classname);
            ret_val = (AbstractFilter)cls.newInstance();
        } catch (ClassNotFoundException cnfe) {
            // ignore
            console.errorReport(CREATE_MSG, cnfe);
        } catch(InstantiationException ie) {
            console.errorReport(CREATE_MSG, ie);
        } catch(IllegalAccessException iae) {
            console.errorReport(ACCESS_MSG, iae);
        }

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties() {
        System.setProperty("uri.content.handler.pkgs",
                           "vlc.net.content");

        System.setProperty("uri.protocol.handler.pkgs",
                           "vlc.net.protocol");

        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof JavascriptResourceFactory)) {
            res_fac = new JavascriptResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
/*
        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
            c_fac = new VRMLContentHandlerFactory(core, loader, c_fac);
            URI.setContentHandlerFactory(c_fac);
        }
*/
        FileNameMap fn_map = URI.getFileNameMap();
        if(!(fn_map instanceof VRMLFileNameMap)) {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }
    }

    /**
     * Print out usage information
     *
     * @param filterer The filter instance to use
     */
    private static void printUsage(CDFFilter filterer) {
        System.out.print("CDFFilter - usage:  filter [filters] ");
        System.out.println(" input output [-loglevel type] [filter_args]");
        System.out.print("  -loglevel type [ALL|WARNINGS|ERRORS|FATAL|NONE]");
        System.out.println(" The minimum level that logs should be written at");
        System.out.println();
        System.out.println("Available built in filters:");

        filterer.printFilters();
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param args The list of arguments for this application.
     */
    public static void main(String[] args) {
        CDFFilter filterer = new CDFFilter();

        int method = X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY;
        float quantizeParam = PARAM_FLOAT_LOSSY;
        boolean upgradeContent = false;

        String filename = null;
        String outfile = null;
        String[] filters = null;

        String[] filter_args = null;

        int num_args = args.length;
        if (num_args < 3) {
            printUsage(filterer);

            System.exit(0);

        } else {
            if (num_args > 3) {
                // Work through the list of arguments looking for the first one
                // that starts with a - The first two items before that are
                int filter_count = 0;
                for(int i = 0; i < args.length; i++) {
                    if(args[i].charAt(0) == '-')
                        break;

                    filter_count++;
                }

                filters = new String[filter_count - 2];

                filename = args[filter_count - 2];
                outfile = args[filter_count - 1];

                System.arraycopy(args, 0, filters, 0, filter_count - 2);
                int num_filter_args = num_args - filter_count;

                filter_args = new String[num_filter_args];
                System.arraycopy(args,
                                 filter_count,
                                 filter_args,
                                 0,
                                 num_filter_args);

            } else {
                filters = new String[1];
                filters[0] = args[0];
                filename = args[1];
                outfile = args[2];
                filter_args = new String[0];
            }
        }

        filterer.setCompressionMethod(method);
        filterer.setQuantizationParam(quantizeParam);
        filterer.setUpgradeContent(upgradeContent);

        int status;
        File fil = new File(filename);
        if (fil.exists()) {
            status = filterer.filter(filters, fil, outfile, filter_args);
        } else {
            try {
                URL url = new URL(filename);
                status = filterer.filter(filters, url, outfile, filter_args);
            } catch(MalformedURLException mfe) {
                System.out.println("Malformed URL: " + filename);
                status = INPUT_FILE_NOT_FOUND;
            }
        }

        System.exit(status);
    }

}
