/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d;

// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRMainSceneBuilder;
import org.web3d.vrml.renderer.CRROUTE;
import org.web3d.vrml.renderer.CRVRMLScene;
import org.web3d.vrml.renderer.DefaultLocator;
import org.web3d.vrml.renderer.j3d.J3DProtoCreator;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.sav.SAVException;

/**
 * A Java3D scene builder implementation for reading VRML utf8 files and
 * building a Java3D scenegraph with them
 * <p>
 *
 * If the user asks for no behaviors, then we will still load nodes that
 * use behaviors, but will disable their use. For example, a LOD will still
 * need to have all of the geometry loaded, just not shown or activated
 * because the LOD's internal behavior is disabled.
 *
 * @author Justin Couch
 * @version $Revision: 1.38 $
 */
class J3DMainSceneBuilder extends CRMainSceneBuilder {

    /** The creator used to instantiate protos */
    private J3DExternProtoCreator externProtoCreator;

    /** The map of the override capability bit settings */
    private Map overrideCapBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredCapBitsMap;

    /** The map of the override capability bit settings */
    private Map overrideFreqBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredFreqBitsMap;

    /** Flag to say if we have any valid capability bits */
    private boolean haveCapBitsMaps;

    /**
     * Create a scene builder with the given node factory. The factory should
     * be producing nodes that conform to the {@link J3DVRMLNode} interface
     * as we expect some of the capabilities to be there. Using any other form
     * of factory is asking for errors. If the factory reference is null then
     * the default factory will be used.
     *
     * @param fac The factory instance to use.
     * @throws NullPointerException The factory reference is null
     */
    J3DMainSceneBuilder(VRMLNodeFactory fac) {
        super(fac);
    }

    /**
     * Reset the builder. This is used to make sure that the builder has been
     * reset after a parsing run just in case the last parsing run exited
     * abnormally and left us in an odd state. Sometimes this can prevent us
     * from parsing again. This method should be called just before the
     * <code>VRMLReader.parse()</code> method is called.
     * <p>
     * The flags set about what to load are <i>not</i> reset by this method.
     */
    public void reset() {
        super.reset();
        haveCapBitsMaps = false;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        overrideCapBitsMap = capBits;
        overrideFreqBitsMap = freqBits;

        if(capBits != null || freqBits != null)
            haveCapBitsMaps = true;
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        requiredCapBitsMap = capBits;
        requiredFreqBitsMap = freqBits;

        if(capBits != null || freqBits != null)
            haveCapBitsMaps = true;
    }

    //----------------------------------------------------------
    // Methods required by the ContentHandler interface.
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

        throws SAVException, VRMLException {

        super.startDocument(uri,url, encoding, type, version, comment);

        J3DVRMLNode root = (J3DVRMLNode)currentNode;

        if(haveCapBitsMaps) {
            root.setCapabilityOverrideMap(overrideCapBitsMap,
                                          overrideFreqBitsMap);
            root.setCapabilityRequiredMap(requiredCapBitsMap,
                                          requiredFreqBitsMap);
        }
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        // If one of our parent nodes is something we don't understand then we
        // need to ignore it. We do this as a first thing and don't even
        // attempt to process this child node. If we did, we would run into all
        // sorts of problems, such as having nested ignores. Basically, once we
        // start to ignore it, _everything_ is ignored until we get back to the
        // same level and we start processing the next sibling node.

        if(ignoreNodeCounter > 0) {
            // Don't increment unless we are already ignoring nodes
            ignoreNodeCounter++;

            if(defName != null) {
                defMap.remove(defName);
                ignoreDefSet.add(defName);
            }

            return;
        } else if(badFieldName) {
            ignoreNodeCounter = 1;
            return;
        }

        if(staticDepthCounter > 0)
            staticDepthCounter++;

        J3DVRMLNode node = null;

        // Let's try local protos then extern protos to try to find one before
        // hitting the factory. If all these fail, then mark this as being an
        // ignorable node and exit. Not sure if this is currently spec
        // compliant as to proto v externproto asking first

        if(protoMap.containsKey(name) || externProtoMap.containsKey(name)) {
            PrototypeDecl proto_def = null;

            proto_def = (PrototypeDecl)protoMap.get(name);

            if(proto_def == null) {
                CRExternPrototypeDecl ex_proto =
                    (CRExternPrototypeDecl)externProtoMap.get(name);

                proto_def = (PrototypeDecl) ex_proto.getProtoDetails();
                // This will normally be null unless its in the cache

                if(proto_def == null) {
                    if(externProtoCreator == null)
                        externProtoCreator =
                            new J3DExternProtoCreator(worldURL);

                    node = externProtoCreator.createInstance(ex_proto);
                    node.setVersion(majorVersion, minorVersion, inStatic);
                    ex_proto.addInstance(nodeStack[topOfStack],
                                         currentFieldIndex,
                                         node);
                }
            }

            if(proto_def != null) {
                if(protoCreator == null) {
                    protoCreator = new J3DProtoCreator(nodeFactory,
                                                       worldURL,
                                                       majorVersion,
                                                       minorVersion);
                    protoCreator.setFrameStateManager(stateManager);
                    protoCreator.setErrorReporter(errorReporter);
                }

                node = (J3DVRMLNode)protoCreator.newInstance(proto_def,
                                                             root,
                                                             majorVersion,
                                                             minorVersion,
                                                             inStatic);
            }
        } else {
            node = (J3DVRMLNode)nodeFactory.createVRMLNode(name,
                                                           inStatic);
        }

        node.setErrorReporter(errorReporter);

        // Check to see if this is on the ignore list. If it is, we just null
        // the reference and treat it like we didn't know the node exists. We
        // Still register it as a DEF so that when we come to the USE
        // declaration we can ignore it there without generating an error. It
        // also means removing the declaration from the list of real decls as
        // the DEF/USE spec says that re-defining the name changes all uses
        // after that point to the new definition.
        VRMLProtoInstance proto = null;
        VRMLNode impl = null;

        if(node != null) {
            Class cls = node.getClass();
            boolean dont_load = false;

            if(dontLoadTypes.contains(cls)) {
               dont_load = true;
            } else if(node instanceof VRMLProtoInstance) {
                // check the proto's type for matches
                proto = (VRMLProtoInstance)node;
                impl = proto.getImplementationNode();

                if(impl != null && dontLoadTypes.contains(impl.getClass()))
                    dont_load = true;
            }

            if(dont_load) {
                ignoreNodeCounter = 1;

                if(defName != null) {
                    defMap.remove(defName);
                    ignoreDefSet.add(defName);

                    if(inStatic)
                        staticNodeSet.add(defName);
                }

                return;
            }
         } else {
            // how do we deal with nodes that are not known? Will need to
            // generate an error here somewhere.
            ignoreNodeCounter = 1;

            if(defName != null) {
                defMap.remove(defName);
                ignoreDefSet.add(defName);

                if(inStatic)
                    staticNodeSet.add(defName);
            }

            return;
        }

        if(haveCapBitsMaps) {
            node.setCapabilityOverrideMap(overrideCapBitsMap,
                                          overrideFreqBitsMap);
            node.setCapabilityRequiredMap(requiredCapBitsMap,
                                          requiredFreqBitsMap);
        }


        // If we are at a root node of the file then the only valid types are
        // child node types. Make a check and ignore it.
        if((topOfStack == 0) && !(node instanceof VRMLChildNodeType)) {

            // Check to see if it's a non-proto by walking the stack
            while(impl instanceof VRMLProtoInstance) {

                impl = ((VRMLProtoInstance)impl).getImplementationNode();
            }

            if(impl != null && !(impl instanceof VRMLChildNodeType)) {
                ignoreNodeCounter = 1;
                throw new VRMLException("The root of a scene graph is required " +
                                        "to be a ChildNodeType. This node is " +
                                        "not: " + node.getVRMLNodeName());
            }
        }

        if(defName != null) {
            if((majorVersion > 2) && defMap.containsKey(defName)) {
                // JC: This should really terminate completely the parsing,
                // but something upstream is not doing that. So, for now just
                // set it up to ignore.
                ignoreNodeCounter = 1;
                throw new VRMLException("DEF name \"" + defName +
                                         "\" already declared in this scope. " +
                                         "X3D requires unique DEF names per file");
            }

            node.setDEF();
            ignoreDefSet.remove(defName);
            defMap.put(defName, node);
            scene.addDEFNode(defName, (J3DVRMLNode)node);

            if(inStatic)
                staticNodeSet.add(defName);
        }

        // OK, so it all checks out, let's pop it onto the stack
        node.setFrameStateManager(stateManager);
        nodeStack[topOfStack].setValue(currentFieldIndex, node);
        topOfStack++;
        nodeStack[topOfStack] = node;
        currentNode = node;

        // don't assign the childIndex here because we don't do that until
        // we get the field call. It is possible that this is an empty node
        // declaration so the next thing we get will be the request to end
        // node. The child index will never have been used. Besides, at this
        // point, we can't work out what the next child field that will be
        // read will be.

        // register the node with the scene in the various catergories it could
        // be in. THis is using a big pile of instanceofs as a node may
        // implement one or more of these interfaces. It would be nice if we
        // could make this into something a little faster....
        int[] s_types = node.getSecondaryType();

        for(int i = 0; i < s_types.length; i++) {
            switch(s_types[i]) {
                case TypeConstants.ExternalNodeType:
                case TypeConstants.SingleExternalNodeType:
                case TypeConstants.MultiExternalNodeType:
                    VRMLExternalNodeType ext_node = (VRMLExternalNodeType)node;
                    ext_node.setWorldUrl(worldURL);
                    break;

                case TypeConstants.LinkNodeType:
                    VRMLLinkNodeType l_node = (VRMLLinkNodeType)node;
                    l_node.setWorldUrl(worldURL);
                    break;
            }
        }

        scene.addNode(node);

        switch(node.getPrimaryType()) {

            case TypeConstants.StaticNodeType:
                if(defName != null)
                    staticNodeSet.add(defName);

                staticDepthCounter = 1;
                inStatic = true;
                break;

            case TypeConstants.ScriptNodeType:
                ((VRMLScriptNodeType)node).setExecutionSpace(root);
                break;

            case TypeConstants.InlineNodeType:
                ((VRMLInlineNodeType)node).setParentSpace(root);
                break;
        }
    }

    //----------------------------------------------------------
    // Methods required by CRMainSceneBuilder
    //----------------------------------------------------------

    /**
     * Generate a protoCreator instance now because one has not been
     * set yet.
     */
    protected void generateProtoCreator() {
        protoCreator = new J3DProtoCreator(nodeFactory,
                                           worldURL,
                                           majorVersion,
                                           minorVersion);
        protoCreator.setFrameStateManager(stateManager);
        protoCreator.setErrorReporter(errorReporter);
    }
}
