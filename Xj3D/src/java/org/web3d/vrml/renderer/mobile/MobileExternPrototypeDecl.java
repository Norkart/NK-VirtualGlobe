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

package org.web3d.vrml.renderer.mobile;

// External imports
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.NodeTemplateToInstanceCreator;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;

/**
 * OpenGL-specific External Prototype Declaration.
 * <p>
 *
 * This node will be placed on the content loader.  Instances of this EP will
 * go into the scenegraph as stubs.  When this EP is loaded it will finish
 * creating those instances.
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public class MobileExternPrototypeDecl extends CRExternPrototypeDecl {

    /**
     * Create a place holder that represents the given extern proto
     * declaration.
     *
     * @param proto The prototype declaration to base this placeholder on
     * @param fac The factory to use for creating the instances
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public MobileExternPrototypeDecl(String name,
                                     VRMLNodeFactory fac,
                                  int majorVersion,
                                  int minorVersion,
                                  NodeTemplateToInstanceCreator creator) {

        super(name, fac, majorVersion, minorVersion, creator);
    }

    //--------------------------------------------------------------
    // Methods required by the VRMLSingleExternalNodeType interface.
    //--------------------------------------------------------------

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {

        if(!(content instanceof VRMLScene)) {
            throw new IllegalArgumentException(
                "Invalid content type for EXTERNPROTO. Not VRMLScene");
        }

        VRMLScene scene = (VRMLScene)content;
        Map protos = scene.getProtos();
        PrototypeDecl proto_def;
        if (uriRef == null)
            proto_def = (PrototypeDecl)scene.getFirstProto();
        else
            proto_def = (PrototypeDecl)scene.getNodeTemplate(uriRef);

        if(proto_def == null)
            throw new IllegalArgumentException(
                "No matching PROTO instances in " + scene.getLoadedURI() +
                " to match this EXTERNPROTO " + getVRMLNodeName());

        VRMLWorldRootNodeType root =
            (VRMLWorldRootNodeType)scene.getRootNode();

        setProtoDetails(proto_def);
        MobileProtoCreator creator =
            new MobileProtoCreator(nodeFactory,
                                   worldURL,
                                   isVrml97 ? 2 : 3,
                                   0);
        creator.setFrameStateManager(stateManager);
        creator.setErrorReporter(errorReporter);
        finishSetContent(proto_def, creator, root);
    }
}
