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

package org.web3d.vrml.scripting.browser;

// External imports
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.parser.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLExternProtoDeclare;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.nodes.VRMLInlineNodeType;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.sav.VRMLReader;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * The common parts of a browser implementation suitable for VRML97 scripting.
 * <P>
 *
 * This class is a full implementation because the external and scripting
 * interfaces will use it differently. External use can just subclass and
 * add the EAI Browser interface. For scripting, the need to extend the
 * Browser base class means that this class will need to be delegated to.
 * <p>
 *
 * The current implementation ignores any parameter values provided by the
 * world.
 *
 * @author Justin Couch
 * @version $Revision: 1.21 $
 */
public class VRML97CommonBrowser extends CommonBrowser {

    /** The VRML97 default header to add for strings. */
    private static final String DEFAULT_VRML_HEADER = "#VRML V2.0 utf8\n";

    /** Name of the profile used for VRML97 compatibility */
    private static final String VRML97_PROFILE_STRING = "vrml97";

    /** Node factory for generating the world root for replaceWorld */
    private VRMLNodeFactory nodeFactory;

    /**
     * Create a browser instance that represents the given universe details.
     * If the scene builder or factory is null, then it will find one from
     * the global pool defined for the renderer used by the browser core.
     *
     * @param browser The core representation of the browser
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader manager for doing async calls
     * @param fsm State manager for coordinating inter-frame processing
     * @param nodeFac Factory to create nodes with for this renderer
     * @throws IllegalArgumentException A paramter is null
     */
    public VRML97CommonBrowser(BrowserCore browser,
                               RouteManager rm,
                               FrameStateManager fsm,
                               WorldLoaderManager wlm,
                               VRMLNodeFactory nodeFac) {

        super(browser, rm, fsm, wlm);

        nodeFactory = nodeFac;
    }

    /**
     * Replace the current world with the given nodes. Replaces the entire
     * contents of the VRML world with the new nodes. Any node references that
     * belonged to the previous world are still valid but no longer form part of
     * the scene graph (unless it is these nodes passed to this method). The
     * URL of the world still represents the just unloaded world.
     * <P>
     * Calling this method causes a SHUTDOWN event followed by an INITIALIZED
     * event to be generated.
     *
     * @param nodes The list of nodes to use as the new root of the world
     */
    public void replaceWorld(VRMLNodeType[] nodes) {

        // Assuming that setScene will deal with the old scene in a nice way.
        // That leaves the scene graph traversal of the new nodes and
        // assembling them into a scene.

        // Create a new world root with an empty scene.
        // We do this to be renderer independent.

        VRMLWorldRootNodeType world =
            (VRMLWorldRootNodeType)nodeFactory.createVRMLNode("WorldRoot",
                                                              false);

        world.setErrorReporter(errorReporter);

        world.setChildren(nodes);
        world.setupFinished();

        // Copy the appropriate data into a mutable VRMLScene
        ReplaceWorldVRMLScene scene =
            new ReplaceWorldVRMLScene(2, 0, world, core.getWorldURL());

        WriteableSceneMetaData meta =
            new WriteableSceneMetaData("2.0", true, SceneMetaData.VRML_ENCODING);

        meta.setProfileName(VRML97_PROFILE_STRING);
        scene.setWorldRootURL(getWorldURL());
        scene.setNodeFactory(nodeFactory);

        // And copy the metadata over
        scene.setMetaData(meta);
        scene.setRootNode(world);
        world.setContainedScene(scene);

        // And now to fill the categories of that scene.
        SceneFillTraverser.processNodes(scene);

        // And finally set the new scene.
        core.setScene(scene, null);
    }

    /**
     * Parse the given string and turn this into a list of VRML nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <P>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <P>
     * The string may contain all legal VRML syntax. The VRML header line is not
     * required to be present in the string.
     *
     * @param vrmlSyntax The string containing VRML string syntax
     * @param parentSpace The parent space for these nodes or null for top-level
     * @return A list of the top level nodes in VRML representation as defined
     *    in the parameter
     * @throws VRMLException General error during processing
     * @throws VRMLParseException If the string does not contain legal
     *    VRML syntax or no node instantiations
     */
    public VRMLNodeType[] createVrmlFromString(String vrmlSyntax,
                                               VRMLExecutionSpace parentSpace)
        throws VRMLException, VRMLParseException, IOException {

        VRMLNodeType[] ret_val = null;

        String workingString;

        if(!(vrmlSyntax.startsWith(DEFAULT_VRML_HEADER)))
            workingString = DEFAULT_VRML_HEADER + vrmlSyntax;
        else
            workingString = vrmlSyntax;

        StringReader reader = new StringReader(workingString);
        InputSource is = new InputSource(core.getWorldURL(), reader);

        WorldLoader ldr = loaderManager.fetchLoader();
        VRMLScene scene = ldr.loadNow(core, is, false, 2, 0);

        loaderManager.releaseLoader(ldr);

        VRMLNodeType root = (VRMLNodeType)scene.getRootNode();

        if(root.getPrimaryType() == TypeConstants.WorldRootNodeType) {
            VRMLWorldRootNodeType world = (VRMLWorldRootNodeType)root;

            // Get the children nodes and then force the world root to delete
            // them. This is because if we leave the nodes as part of the world
            // root, they have a Java3D parent. If we try to add them later on
            // to part of the live scene graph, they will generate multiple
            // parent exceptions. This avoids that problem.
            VRMLNodeType[] children = world.getChildren();
            world.setChildren((VRMLNodeType)null);

            ret_val = children;

            routeManager.addSpace(world);

            // Rip through and setParentSpace on all inlines
            ArrayList nodes = scene.getByPrimaryType(TypeConstants.InlineNodeType);
            int size = nodes.size();
            VRMLInlineNodeType inline;

            for(int i = 0; i < size; i++) {
                inline = (VRMLInlineNodeType)nodes.get(i);
                inline.setParentSpace(parentSpace);
            }

            // Look for any externprotos and queue them up
            ArrayList proto_list = scene.getNodeTemplates();
            size = proto_list.size();

            for(int i = 0; i < size; i++) {
                Object node = proto_list.get(i);
                if(node instanceof VRMLExternProtoDeclare) {
                    VRMLExternProtoDeclare proto = (VRMLExternProtoDeclare)node;
                    stateManager.registerAddedExternProto(proto);
                }
            }
        } else {
            // How could we get in here?
            errorReporter.messageReport("Unhandled root node type in VRML97CommonBrowser.");
            ret_val = new VRMLNodeType[1];
            ret_val[0] = root;
        }

        return ret_val;
    }

    /**
     * Create and load VRML from the given URL and place the returned values
     * as nodes into the given VRML node in the scene. The difference between
     * this and loadURL is that this method does not replace the entire scene
     * with the contents from the URL. Instead, it places the return values
     * as events in the nominated node and MFNode eventIn.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97 specification.
     * @param node The destination node for the VRML code to be sent to.
     * @param eventIn The name of the MFNode eventIn to send the nodes to.
     * @throws InvalidFieldException the eventIn or node is not a valid
     *   destination for the URL information
     */
    public void createVrmlFromURL(String[] url, VRMLNodeType node,
        String eventIn) throws InvalidFieldException {

        int field = node.getFieldIndex(eventIn);
        VRMLFieldDeclaration decl = node.getFieldDeclaration(field);

        // check for an eventIn handling
        int access = decl.getAccessType();
        if((access == FieldConstants.EVENTOUT) ||
           (access == FieldConstants.FIELD))
           throw new InvalidFieldException("The field is not an eventIn");

        // Check for MFNode field to set nodes in
        int field_type = decl.getFieldType();
        if(field_type != FieldConstants.MFNODE)
            throw new InvalidFieldException("Field is not an MFNode");

        loaderManager.queueCreateURL(url,
                                     node,
                                     field,
                                     core.getWorldExecutionSpace());

    }
}
