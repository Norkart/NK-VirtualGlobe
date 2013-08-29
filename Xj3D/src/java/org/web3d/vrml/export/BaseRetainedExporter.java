/*****************************************************************************
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.regex.Pattern;

import java.text.NumberFormat;

import org.ietf.uri.*;

// Local imports
import org.web3d.util.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.nodes.proto.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.Xj3DConstants;
import org.xj3d.impl.core.loading.MemCacheLoadManager;
import org.xj3d.impl.core.loading.AbstractLoadManager;
import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

import org.web3d.parser.x3d.X3DFieldReader;

import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.CRProtoCreator;
import org.web3d.vrml.renderer.norender.NRSceneBuilderFactory;
import org.web3d.vrml.renderer.norender.NRNodeFactory;
import org.web3d.vrml.renderer.norender.NRProtoCreator;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.web3d.vrml.renderer.common.input.DefaultSensorManager;

import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;

/**
 * An exporter that brings the whole file into memory before
 * exporting.  This has same advantages and disadvantages.
 *
 * Advantages:
 *
 *    Ease of implementation.  Streamed writing has several difficulties.
 *
 * Disadvantages:
 *
 *   Comments will be lost
 *   Locations of import/exports/proto decls/routes might be changed.
 *   Upgrade logic must be in terminal files unless we want to track current node/field
 *
 * In the future we'd like to perform this across a SAI graph(ie
 * no SAV stream to sniff), this
 * block will document what problems that will entail:
 *
 *    The DEF table for .wrl files has duplicates, so getDEFNodes loses info
 *    ExternProto URL already has worldUrl added in, removeing worldRoot might not work
 *
 * TODO:
 *    Count Node num to help place ROUTE in the right place?
 *
 * @author Alan Hudson
 * @version $Revision: 1.29 $
 */
public abstract class BaseRetainedExporter extends Exporter implements BinaryContentHandler {

    /** Name of the property file holding the default container fields */
    protected static final String CONTAINER_PROPS_FILE = "XMLcontainerfields.properties";

    /** The stream to write results to */
    protected OutputStream oStream;

    /** The major version of the spec this file belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this file belongs to. */
    protected int minorVersion;

    /** The NR scene builder to create the scenegraph */
    protected SceneBuilder builder;

    /** The NR node factory to get default values from */
    protected VRMLNodeFactory nodeFactory;

    /** A cache of nodes with default values */
    protected HashMap defaultNodes;

    /** The current default valued node */
    protected VRMLNodeType defaultNode;

    /** Are we upgrading from VRML to X3D */
    protected boolean upgrading;

    /** The current DEF map, node to DEF name */
    protected Map currentDefMap;

    /** The current IS map */
    protected Map currentIsMap;

    /** The current proto definition */
    protected PrototypeDecl currentPrototypeDecl;

    /** The creator used to instantiate protos */
    protected CRProtoCreator protoCreator;

    /** The top scene */
    protected VRMLScene scene;

    /** The profile */
    protected String profile;

    /** Additional components to add, list of ComponentInfo */
    protected ArrayList componentList;

    /** Stack of def values */
    private SimpleStack defStack;

    /** The current def table */
    private HashMap currentDefTable;

    /** Stack of remap values */
    private SimpleStack defRemapStack;

    /** The current remap table */
    private HashMap currentDefRemapTable;

    /** Stack of def num values */
    private SimpleStack defNumStack;

    /** The current def num values */
    private HashMap currentDefNumTable;

    /** Mapping of EXTERNPROTO to url[] */
    protected HashMap epToUrl;

    /** The current extern proto name */
    private String currentEp;

    /** Upgrade patterns for scripts */
    protected Pattern[] scriptPatterns;

    /** The replacement values */
    protected String[] scriptReplacements;

    /** Should VRML97 optional nodes like h-Anim and GeoVRML be converted */
    protected boolean convertOldContent;

    /** Protos/Extern Proto definitions to remove if convertOldContent is true */
    protected HashMap oldProtos;

    /** Fields to remap their types.  Node.fieldName -> new type */
    protected HashMap fieldRemap;

    /** Where there H-Anim nodes found */
    protected boolean hanimFound;

    /** H-Anim nodes */
    protected HashMap hanimNodes;

    /** Where there Geospatial nodes found */
    protected boolean geospatialFound;

    /** Geospatial nodes */
    protected HashMap geospatialNodes;

    /** A cache of IS map preprocessed versions */
    protected HashMap isCache;

    /** Property for container fields */
    protected Properties containerFields;

    /** The current encoding */
    protected String encodingTo;

    /** Should we strip whitespace */
    protected boolean stripWhitespace;

    /** Are we processing a document */
    protected boolean processingDocument;

    /** An X3DField reader for converting field types. */
    protected VRMLFieldReader fieldReader;

    /** The current resource connection if being used */
    protected ResourceConnection currentConnection;

    /** Did we issue a not found message */
    private boolean issuedNotFound;

    /** Start time used for optimization */
    protected long startTime;

    /** The number of significant digits when printing floats, -1 not to change */
    protected int sigDigits;

    /** The format string to pass in for float formating */
    protected NumberFormat floatFormat;


    /**
     * Create a new exporter for the given spec version
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param reporter The error reporter to use
     * @param sigDigits The number of significant digits for floats, -1 for unchanged
     */
    public BaseRetainedExporter(int major,
                                int minor,
                                ErrorReporter reporter,
                                int sigDigits) {

        super(major, minor, reporter);

        this.sigDigits = sigDigits;

        if (sigDigits > -1) {
            floatFormat = NumberFormat.getInstance(Locale.US);
            floatFormat.setMinimumFractionDigits(0);
            floatFormat.setMaximumFractionDigits(sigDigits);
        }

        NRSceneBuilderFactory builderFactory = new NRSceneBuilderFactory(false,
           true,true,true,true,true,true);

        builder = builderFactory.createBuilder();
        builder.setErrorReporter(reporter);

        nodeFactory = NRNodeFactory.getNRNodeFactory();
        nodeFactory.setErrorReporter(reporter);

        defaultNodes = new HashMap();
        componentList = new ArrayList();
        isCache = new HashMap();

        currentDefTable = new HashMap();
        currentDefRemapTable = new HashMap();
        currentDefNumTable = new HashMap();

        defStack = new SimpleStack();
        defRemapStack = new SimpleStack();
        defNumStack = new SimpleStack();

        epToUrl = new HashMap();

        initScriptPatterns();
        initOldProtos();

        stripWhitespace = false;
    }

    /**
     * Should old optional content like h-Anim and GeoVRML be converted
     * to native nodes.
     *
     * @param convert true to convert the protos
     */
    public void setConvertOldContent(boolean convert) {
        convertOldContent = convert;

        if (convert) {
            fieldReader = new X3DFieldReader();
        }
    }


    /**
     * Print the header.
     *
     * @param major The major version
     * @param minor The minor version
     */
    protected abstract void printHeader(int major, int minor);

    /**
     * Print the profile decl.
     *
     * @param profile The profile
     */
    protected abstract void printProfile(String profile);

    /**
     * Print the component decl
     *
     * @param comps The component list
     */
    protected abstract void printComponents(ComponentInfo[] comps);

    /**
     * Print the MetaData.
     *
     * @param meta The scene Metadata map
     */
    protected abstract void printMetaData(Map meta);

    /**
     * Print a proto declaration.
     *
     * @param proto The decl to print
     */
    protected abstract void printPrototypeDecl(PrototypeDecl proto);

    /**
     * Print a ROUTE statement.
     *
     * @param route The ROUTE to print
     * @param defMap The DEF map
     */
    protected abstract void printROUTE(ROUTE route, Map defMap);

    /**
     * Print Exports.
     *
     * @param exports A map of exports(name,AS).
     */
    protected abstract void printExports(Map exports);

    /**
     * Print Imports.
     *
     * @param imports A map of imports(exported, String[] {def, as}.
     */
    protected abstract void printImports(Map imports);

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

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
        builder.endDocument();

        scene = builder.getScene();
        builder.releaseScene();

        if (convertOldContent) {
            if (hanimFound)
                componentList.add(new ComponentInfo("H-Anim",1));
            if (geospatialFound)
                componentList.add(new ComponentInfo("Geospatial",1));
        }


        SceneMetaData metadata = scene.getMetaData();

        printHeader(majorVersion,minorVersion);

        String profileName;

        if (upgrading)
            profileName = "Immersive";
        else
            profileName = metadata.getProfileName();

        printProfile(profileName);

        ComponentInfo[] baseComps = metadata.getComponents();

        int start = componentList.size();
        int baseSize = 0;

        if (baseComps != null)
            baseSize = baseComps.length;

        ComponentInfo[] comps = new ComponentInfo[baseSize + componentList.size()];
        componentList.toArray(comps);

        if (baseSize > 0) {
            for(int i=0; i < baseComps.length; i++) {
                comps[start + i] = baseComps[i];
            }
        }

        printComponents(comps);

        printMetaData(metadata.getMetaData());
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
        builder.setDocumentLocator(loc);

        if (fieldReader != null)
            fieldReader.setDocumentLocator(loc);
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

        if (processingDocument)
            return;

        startTime = System.currentTimeMillis();

        processingDocument = true;

        // Parse the number string looking for the version minor number.
        int dot_index = version.indexOf('.');
        String minor_num = version.substring(dot_index + 1);

        if (upgrading) {
            majorVersion = 3;
            minorVersion = 0;
        } else {
            majorVersion = 3;
            int mv = Integer.parseInt(minor_num);

            if (mv > minorVersion) {
                minorVersion = mv;
                errorReporter.messageReport("Content is of greater version than " +
                                      "requested version. Using content version");
            }
        }

        nodeFactory.setSpecVersion(majorVersion, minorVersion);

        if (version.equals("V2.0")) {
            upgrading = true;
            nodeFactory.setProfile("Immersive");
            profile = "Immersive";
            errorReporter.messageReport("Upgrading content from VRML to X3D");
        }

        builder.startDocument(uri,url,encoding,type,version,comment);

        protoCreator = new NRProtoCreator(nodeFactory,
                                           url,
                                           majorVersion,
                                           minorVersion);
        protoCreator.setErrorReporter(errorReporter);
        //protoCreator.setFrameStateManager(stateManager);

        hanimFound = false;
        geospatialFound = false;
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
        throws SAVException, VRMLException{

        builder.profileDecl(profileName);
        nodeFactory.setProfile(profileName);
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
        throws SAVException, VRMLException{

        builder.componentDecl(componentName);

        int level;
        String compName;
        int pos = componentName.indexOf(":");

        if (pos < 0)
            throw new VRMLException("Invalid component decl , no level");

        compName = componentName.substring(0,pos);
        level = Integer.parseInt(componentName.substring(pos+1));

        nodeFactory.addComponent(compName, level);
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
        throws SAVException, VRMLException{

        builder.metaDecl(key, value);
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
        throws SAVException, VRMLException{

        builder.importDecl(inline, exported, imported);
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
        throws SAVException, VRMLException{

        builder.exportDecl(defName, exported);
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
        throws SAVException, VRMLException{

        if (defName != null) {
            if (currentDefTable.get(defName) != null) {
                Integer num = (Integer) currentDefNumTable.get(defName);

                if (num != null)
                    num = new Integer(num.intValue() + 1);
                else
                    num = new Integer(1);

                currentDefNumTable.put(defName, num);
                String newName = defName + "_DUP" + num.intValue();
                errorReporter.messageReport("Duplicate DEF detected, renamed to: " + newName);
                currentDefRemapTable.put(defName, newName);
                defName = newName;
            } else
                currentDefTable.put(defName, name);
        }

        if (convertOldContent) {
            if (hanimNodes.get(name) != null) {
                hanimFound = true;
            }
            if (geospatialNodes.get(name) != null) {
                geospatialFound = true;
            }
        }

        builder.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode()
        throws SAVException, VRMLException{
        builder.endNode();
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
    public void startField(String name)
        throws SAVException, VRMLException{
        builder.startField(name);
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
    public void fieldValue(String value)
        throws SAVException, VRMLException{

        builder.fieldValue(value);
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
    public void fieldValue(String[] values)
        throws SAVException, VRMLException{

        builder.fieldValue(values);
    }

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

        builder.fieldValue(value);
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

        builder.fieldValue(value, len);
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

        builder.fieldValue(value);
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

        builder.fieldValue(value, len);
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

        builder.fieldValue(value);
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

        builder.fieldValue(value, len);
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

        builder.fieldValue(value);
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

        builder.fieldValue(value,len);
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

        builder.fieldValue(value);
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

        builder.fieldValue(value,len);
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

        builder.fieldValue(value);
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
    public void useDecl(String defName)
        throws SAVException, VRMLException{

        String newName = (String) currentDefRemapTable.get(defName);
        if (newName != null)
            defName = newName;

        builder.useDecl(defName);
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
        builder.endField();
    }

    //-----------------------------------------------------------------------
    //Methods for interface RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNode The name of the DEF of the source node
     * @param srcField The name of the field to route values from
     * @param destNode The name of the DEF of the destination node
     * @param destField The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void routeDecl(String srcNode,
                          String srcField,
                          String destNode,
                          String destField)
        throws SAVException, VRMLException{

        builder.routeDecl(srcNode, srcField, destNode, destField);
    }

    //----------------------------------------------------------
    // ScriptHandler methods
    //----------------------------------------------------------

    /**
     * Notification of the start of a script declaration. All calls between
     * now and the corresponding {@link #endScriptDecl} call belong to this
     * script node. This method will be called <I>after</I> the ContentHandler
     * <CODE>startNode()</CODE> method call. All DEF information is contained
     * in that method call and this just signifies the start of script
     * processing so that we know to treat the field parsing callbacks a
     * little differently.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startScriptDecl() throws SAVException, VRMLException {
        builder.startScriptDecl();
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endScriptDecl() throws SAVException, VRMLException {
        builder.endScriptDecl();
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     * <p>
     * If the current parsing is in a proto and the field "value" is defined
     * with an IS statement then the value returned here is null. There will
     * be a subsequent call to the ProtoHandlers <CODE>protoIsDecl()</CODE>
     * method with the name of the field included.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException{

        builder.scriptFieldDecl(access, type, name, value);
    }

    //----------------------------------------------------------
    // ProtoHandler methods
    //----------------------------------------------------------
    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startProtoDecl(String name)
        throws SAVException, VRMLException{

        builder.startProtoDecl(name);
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #startProtoBody()} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endProtoDecl()
        throws SAVException, VRMLException{

        builder.endProtoDecl();
    }

    /**
     * Notification of a proto's field declaration. This is used for both
     * external and ordinary protos. Externprotos don't allow the declaration
     * of a value for the field. In this case, the parameter value will be
     * null.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException{

        builder.protoFieldDecl(access, type, name, value);
    }

    /**
     * Notification of a field value uses an IS statement.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void protoIsDecl(String fieldName)
        throws SAVException, VRMLException{

        builder.protoIsDecl(fieldName);
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startProtoBody() throws SAVException, VRMLException{

        builder.startProtoBody();
        defStack.push(currentDefTable);
        defRemapStack.push(currentDefRemapTable);
        defNumStack.push(currentDefNumTable);

        currentDefTable = new HashMap();
        currentDefRemapTable = new HashMap();
        currentDefNumTable = new HashMap();
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endProtoBody() throws SAVException, VRMLException{
        builder.endProtoBody();

        currentDefTable = (HashMap) defStack.pop();
        currentDefRemapTable = (HashMap) defRemapStack.pop();
        currentDefNumTable = (HashMap) defNumStack.pop();
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startExternProtoDecl(String name)
        throws SAVException, VRMLException{

        builder.startExternProtoDecl(name);
        currentEp = name;
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #externProtoURI} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endExternProtoDecl()
        throws SAVException, VRMLException{

        builder.endExternProtoDecl();
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list as an array of URI strings. The calling application is required to
     * interpet the incoming string. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        builder.externProtoURI(values);

        epToUrl.put(currentEp, values);
    }

    /**
     * Find the IS relationship for a node.
     *
     * @param node The current node
     * @param idx The current field
     * @param isMap The current IS map
     */
    protected String findIS(VRMLNodeType node, int idx, Map isMap) {
        if (isMap == null)
            return null;

        Map.Entry[] map = (Map.Entry[]) isCache.get(isMap);
        ProtoFieldInfo pfi;
        List list;

        if (map == null) {
            map = new Map.Entry[isMap.size()];

            isMap.entrySet().toArray(map);

            isCache.put(isMap, map);
        }

        // TODO: Could change this to a hash lookup

        for(int i=0; i < map.length; i++) {
            list = (List) map[i].getValue();

            for(int j=0; j < list.size(); j++) {
                pfi = (ProtoFieldInfo) list.get(j);
                if (pfi.node == node && pfi.field == idx) {
                    VRMLFieldDeclaration decl = currentPrototypeDecl.getFieldDeclaration(((Integer)map[i].getKey()).intValue());
                    return decl.getName();
                }
            }
        }

        return null;
    }

    /**
     * Utility to print IS mapping for debugging purposes.
     *
     * @param isMap The map to print
     */
    protected void printIS(Map isMap) {
        if (isMap == null)
            return;

        List list;
        Map.Entry[] map = new Map.Entry[isMap.size()];
        ProtoFieldInfo pfi;

        isMap.entrySet().toArray(map);
        for(int i=0; i < map.length; i++) {
            list = (List) map[i].getValue();

            for(int j=0; j < list.size(); j++) {
                pfi = (ProtoFieldInfo) list.get(j);

                errorReporter.messageReport("   n: " + pfi.node + " field: " + pfi.field);
            }
        }
    }

    /**
     * Utility to print DEF mapping for debugging purposes.
     *
     * @param defMap The map to print
     */
    protected void printDefMap(Map defMap) {
        Map.Entry[] entries;

        entries = new Map.Entry[defMap.size()];
        defMap.entrySet().toArray(entries);

        errorReporter.messageReport("DEF Map:");
        for(int i=0; i < entries.length; i++) {
            errorReporter.messageReport("   " + entries[i].getKey() + " --> " + entries[i].getValue());
        }
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    protected String getPublicId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_PUBLIC_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_PUBLIC_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_PUBLIC_ID_3_2;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    protected String getSystemId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_SYSTEM_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_SYSTEM_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_SYSTEM_ID_3_2;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }

    //-------------------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------------------

    /**
     * Reverse a maps key/value mapping.
     */
    protected void reverseMap(Map in, Map out) {
        Map.Entry entry;
        Set entrySet = in.entrySet();
        Iterator itr = entrySet.iterator();
        VRMLNode node;
        String defName;

        while(itr.hasNext()) {
            entry = (Map.Entry) itr.next();
            node = (VRMLNode) entry.getValue();
            defName = (String) entry.getKey();

            out.put(entry.getValue(), defName);
        }
    }

    /**
     * Load the properties files for the given spec version.
     *
     * @param majorVersion The spec major version
     * @param minorVersion The spec minor version
     */
    protected void loadContainerProperties(final int majorVersion, final int minorVersion) {

        if (containerFields == null) {
            containerFields = new Properties();
        }

        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    String filename = "config/" + majorVersion + "." + minorVersion + "/" +
                                                         CONTAINER_PROPS_FILE;

                    try {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        InputStream is =
                            cl.getSystemResourceAsStream(filename);

                        // WebStart fallback
                        if(is == null) {
                            cl = BaseRetainedExporter.class.getClassLoader();
                            is = cl.getResourceAsStream(filename);
                        }

                        if(is != null)
                            containerFields.load(is);
                        else
                            errorReporter.messageReport("Couldn't find container properties file: " +
                                                  filename);

                    } catch(IOException ioe) {
                        errorReporter.errorReport("Error reading container properties file", ioe);
                    }
                    return null;
                }
            }
        );
    }

    /**
     * Initialize script patterns for upgrading VRML to X3D.
     *
     */
    private void initScriptPatterns() {
        scriptPatterns = new Pattern[7];
        scriptReplacements = new String[7];

        int i = 0;

        scriptPatterns[i] = Pattern.compile("Browser.addRoute");
        scriptReplacements[i++] = "Browser.currentScene.addRoute";
        scriptPatterns[i] = Pattern.compile("Browser.deleteRoute");
        scriptReplacements[i++] = "Browser.currentScene.deleteRoute";
        scriptPatterns[i] = Pattern.compile("Browser.getName\\p{Punct}\\p{Punct}");
        scriptReplacements[i++] = "Browser.name";
        scriptPatterns[i] = Pattern.compile("Browser.getVersion\\p{Punct}\\p{Punct}");
        scriptReplacements[i++] = "Browser.version";
        scriptPatterns[i] = Pattern.compile("Browser.getCurrentSpeed\\p{Punct}\\p{Punct}");
        scriptReplacements[i++] = "Browser.currentSpeed";
        scriptPatterns[i] = Pattern.compile("Browser.getCurrentFrameRate\\p{Punct}\\p{Punct}");
        scriptReplacements[i++] = "Browser.currentFrameRate";
        scriptPatterns[i] = Pattern.compile("Browser.getWorldURL\\p{Punct}\\p{Punct}");
        scriptReplacements[i++] = "Browser.currentScene.worldURL";
    }

    /**
     * Initialize old proto's to remove when upgrading content from VRML97.
     */
    private void initOldProtos() {
        fieldRemap = new HashMap();

        hanimNodes = new HashMap();

        hanimNodes.put("Humanoid","HAnimHumanoid");
        hanimNodes.put("Joint","HAnimJoint");
        hanimNodes.put("Segment", "HAnimSegment");
        hanimNodes.put("Site", "HAnimSite");
        hanimNodes.put("Displacer", "HAnimDisplacer");

        geospatialNodes = new HashMap();

        Integer DOUBLE_DATA = new Integer(VRMLFieldData.DOUBLE_DATA);
        Integer DOUBLE_ARRAY_DATA = new Integer(VRMLFieldData.DOUBLE_ARRAY_DATA);

        geospatialNodes.put("GeoOrigin","GeoOrigin");
        fieldRemap.put("GeoOrigin.geoCoords",DOUBLE_ARRAY_DATA);
        geospatialNodes.put("GeoViewpoint","GeoViewpoint");
        fieldRemap.put("GeoViewpoint.position",DOUBLE_ARRAY_DATA);
        geospatialNodes.put("GeoLocation", "GeoLocation");
        fieldRemap.put("GeoLocation.geoCoords",DOUBLE_ARRAY_DATA);
        geospatialNodes.put("GeoLOD", "GeoLOD");
        geospatialNodes.put("GeoTouchSensor", "GeoTouchSensor");
        geospatialNodes.put("GeoElevationGrid", "GeoElevationGrid");
        fieldRemap.put("GeoElevationGrid.creaseAngle",DOUBLE_DATA);
        fieldRemap.put("GeoElevationGrid.xSpacing",DOUBLE_DATA);
        fieldRemap.put("GeoElevationGrid.zSpacing",DOUBLE_DATA);
        fieldRemap.put("GeoElevationGrid.geoGridOrigin",DOUBLE_ARRAY_DATA);
        geospatialNodes.put("GeoMetadata", "GeoMetadata");
        geospatialNodes.put("GeoCoordinate", "GeoCoordinate");
        fieldRemap.put("GeoCoordinate.point",DOUBLE_ARRAY_DATA);
        geospatialNodes.put("GeoPositionInterpolator", "GeoPositionInterpolator");
        fieldRemap.put("GeoPositionInterpolator.keyValue",DOUBLE_ARRAY_DATA);

        oldProtos = new HashMap(hanimNodes.size() + geospatialNodes.size());
        oldProtos.putAll(hanimNodes);
        oldProtos.putAll(geospatialNodes);
    }

    /**
     * Convert a field data from one type to another.
     *
     * @param newType The new type
     * @param data The data to convert in place
     * @param decl The field decl
     */
    protected void convertFieldData(int newType, VRMLFieldData data, VRMLFieldDeclaration decl) {
        // For now this will just handle from strings to doubles

        int len;

        switch(newType) {
            case VRMLFieldData.DOUBLE_DATA:
                switch(data.dataType) {
                    case VRMLFieldData.STRING_DATA:
                        data.doubleValue = fieldReader.SFDouble(data.stringValue);
                        break;
                    case VRMLFieldData.FLOAT_DATA:
                        data.doubleValue = (double) data.floatValue;
                        break;
                    case VRMLFieldData.FLOAT_ARRAY_DATA:
                        len = data.floatArrayValue.length;
                        data.doubleArrayValue = new double[len];

                        for(int i=0; i < len; i++) {
                            data.doubleArrayValue[i] = (double) data.floatArrayValue[i];
                        }
                        break;
                    default:
                        StringBuffer buf =
                            new StringBuffer("Unhandled type conversion in convertFieldData:");
                        buf.append(decl);
                        buf.append("\noriginal type: ");
                        buf.append(data.dataType);

                        errorReporter.errorReport(buf.toString(), null);

                        return;
                }

                data.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                switch(data.dataType) {
                    case VRMLFieldData.STRING_DATA:
                        data.doubleArrayValue = fieldReader.MFDouble(data.stringValue);
                        break;
                    case VRMLFieldData.STRING_ARRAY_DATA:
                        data.doubleArrayValue = MFDouble(data.stringArrayValue);
                        break;
                    case VRMLFieldData.FLOAT_ARRAY_DATA:
                        len = data.floatArrayValue.length;
                        data.doubleArrayValue = new double[len];

                        for(int i=0; i < len; i++) {
                            data.doubleArrayValue[i] = (double) data.floatArrayValue[i];
                        }
                        break;
                    default:
                        StringBuffer buf =
                            new StringBuffer("Unhandled type conversion in convertFieldData:");
                        buf.append(decl);
                        buf.append("\noriginal type: ");
                        buf.append(data.dataType);

                        errorReporter.errorReport(buf.toString(), null);
                        return;
                }

                data.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            default:
                StringBuffer buf =
                    new StringBuffer("Unhandled type conversion in convertFieldData:");
                buf.append(decl);

                errorReporter.errorReport(buf.toString(), null);

        }
    }

    /**
     * Parse an MFDouble value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    private double[] MFDouble(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        int final_size = 0;
        double[][] ret_val = new double[size][];
        int i = 0;

        for(i = 0; i < size; i++) {
            ret_val[i] = fieldReader.MFDouble(value[i]);

            final_size += ret_val[i].length;
        }

        double[] final_ret = new double[final_size];
        int pos = 0;
        int len;

        for(i=0; i < size; i++) {
            len = ret_val[i].length;

            System.arraycopy(ret_val[i], 0, final_ret, pos, len);
            pos += len;
        }

        return final_ret;
    }
}
