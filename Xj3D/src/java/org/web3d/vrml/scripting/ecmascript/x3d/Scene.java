/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.x3d;

// External imports
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.WriteableSceneMetaData;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.scripting.ecmascript.builtin.SFNode;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * Representation of the ECMAscript Scene host object from 19777-2 7.4.
 * <P>
 *
 * Due to the limitation of the locateFunction method, methods that are defined
 * in the base execution context typically cannot be found here because it
 * doesn't go searching base classes - just the top level class. That means we
 * need to redeclare the methods in this class. All they do is call the super
 * class methods of the same name.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class Scene extends X3DExecutionContext {

    /**
     * Error message for when we were expecting a SFNode instance and got
     * something else in the updateExportedNode method.
     */
    private static final String INVALID_EXPORT_UPDATE_MSG =
        "updateExportedNode() was expecting an object of type SFNode for the " +
        "second argument, but got ";

    /** Metadata relating to this scene. */
    private WriteableSceneMetaData metadata;

    /** map containing metadata info */
    private Map metaMap;

    /** map containing exports info */
    private Map exportMap;

    /** The full version of the scene variable in the base class. */
    private VRMLScene vrmlScene;

    static {
        // These are added over the base class ones
        functionNames.add("setMetaData");
        functionNames.add("getMetaData");
        functionNames.add("getExportedNode");
        functionNames.add("updateExportedNode");
        functionNames.add("removeExportedNode");
    }

    /**
     * Default version of the constructor for when someone is creating a new
     * instance of the Scene object.
     *
     * @param space The space to source information for this scene
     * @param rm A route manager for users creating/removing routes
     * @param profile The profile to use for this scene
     */
    public Scene(VRMLExecutionSpace space,
                 RouteManager rm,
                 FrameStateManager fsm,
                 ProfileInfo profile) {

        super(space, rm, fsm, profile);

        metadata = (WriteableSceneMetaData)scene.getMetaData();
        vrmlScene = (VRMLScene)scene;
    }

    //----------------------------------------------------------
    // Methods defined by X3DExecutionContext
    //----------------------------------------------------------

    /**
     * createNode() function defined by From table 7.6.
     */
    public SFNode jsFunction_createNode(String name) {
        return super.jsFunction_createNode(name);
    }

    /**
     * getNamedNode() function defined by From table 7.6.
     */
    public SFNode jsFunction_getNamedNode(String name) {
        return super.jsFunction_getNamedNode(name);
    }

    /**
     * updateNamedNode() function defined by From table 6.6.
     */
    public void jsFunction_updateNamedNode(String name, Scriptable node) {
        super.jsFunction_updateNamedNode(name, node);
    }

    /**
     * removeNamedNode() function defined by From table 6.6.
     */
    public void jsFunction_removeNamedNode(String name) {
        super.jsFunction_removeNamedNode(name);
    }

    /**
     * getImportedNode() function defined by From table 7.6.
     */
    public SFNode jsFunction_getImportedNode(String defName, String asName) {
        return super.jsFunction_getImportedNode(defName, asName);
    }

    /**
     * updateImportedNode() function defined by From table 6.6.
     */
    public void jsFunction_updateImportedNode(String defName, String asName) {
        super.jsFunction_updateImportedNode(defName, asName);
    }

    /**
     * removeImportedNode() function defined by From table 6.6.
     */
    public void jsFunction_removeImportedNode(String name) {
        super.jsFunction_removeImportedNode(name);
    }

    /**
     * addRoute() function defined by From table 7.6.
     */
    public Route jsFunction_addRoute(Scriptable fn,
                                     String fromField,
                                     Scriptable tn,
                                     String toField) {

        return super.jsFunction_addRoute(fn, fromField, tn, toField);
    }

    /**
     * deleteRoute() function defined by From table 7.6
     */
    public void jsFunction_deleteRoute(Scriptable route) {
        super.jsFunction_deleteRoute(route);
    }

    /**
     * createProto() function defined by From table 7.71.
     */
    public SFNode jsFunction_createProto(String name) {
        return super.jsFunction_createProto(name);
    }

    //----------------------------------------------------------
    // Local methods used by ECMAScript
    //----------------------------------------------------------

    /**
     * setMetaData() function defined by From table 7.8.
     */
    public void jsFunction_setMetaData(String name, String value) {
        if(value == null)
            metadata.removeMetaData(name);
        else
            metadata.addMetaData(name, value);
    }

    /**
     * getMetaData() function defined by From table 7.8.
     */
    public String jsFunction_getMetaData(String name) {
        if(metaMap == null)
            metaMap = metadata.getMetaData();

        return (String)metaMap.get(name);
    }

    /**
     * getExportedNode() function defined by From table 7.8.
     */
    public SFNode jsFunction_getExportedNode(String name) {
        if(exportMap == null)
            exportMap = vrmlScene.getExports();

        String def_name = (String)exportMap.get(name);

        SFNode ret_val = null;

        if(def_name != null)
            ret_val = jsFunction_getNamedNode(name);

        return ret_val;
    }

    /**
     * updateExportedNode() function defined by From table 7.8.
     */
    public void jsFunction_updateExportedNode(String name, Scriptable n) {

        if(!(n instanceof SFNode))
            Context.reportRuntimeError(INVALID_EXPORT_UPDATE_MSG + n.getClass());

        if(exportMap == null)
            exportMap = vrmlScene.getExports();

errorReporter.messageReport("Scene.updateExportedNode() not implemented yet");
    }

    /**
     * getExportedNode() function defined by From table 7.8.
     */
    public void jsFunction_removeExportedNode(String name) {
        if(exportMap == null)
            exportMap = vrmlScene.getExports();

        // Just remove the export name from the global list.
        exportMap.remove(name);
    }
}
