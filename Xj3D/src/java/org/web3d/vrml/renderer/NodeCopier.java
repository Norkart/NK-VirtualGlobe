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

package org.web3d.vrml.renderer;

// Standard imports
import java.util.*;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.proto.ProtoScene;

/**
 * A class that is used to create real instances of protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * We have a small conundrum to deal with - if the proto definition contains
 * SF/MFNode fields, we don't know whether the values should be also generated
 * as real runtime nodes too. Maybe the usage of this node will provide values
 * that are dealt with after this class has finished. Other times, these defaul
 * values must be used. For this implementation, we have gone with the
 * safety-first approach: Always parse the definition of any SF or MFNode field
 * and turn those into runtime instances. Although this may create extra
 * garbage, there seems to be no nice way of dealing with this issue without a
 * completely different architecture for the library.
 * <p>
 *
 * If, during the traversal processing we discover that the traversed node is
 * an instance of a J3D implementation node, we do not process it any further.
 * The basic assumption here is that the node is a USE of a j3D structure.
 * <p>
 *
 * Note:
 * Under the current implementation, EXTERNPROTOs are not yet catered for.
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class NodeCopier implements SceneGraphTraversalSimpleObserver {

    /** The factory that is used to generate the runtime version */
    private VRMLNodeFactory factory;

    /** The scene as we are building it up during traversal */
    private ProtoScene scene;

    /** Mapping of declaration node to the J3D version */
    private HashMap nodeMap;

    /** The source root node. Kept so we can deal with it at setupFinished */
    private VRMLNodeType srcNode;

    /** Parent execution space, for passing along to scripts, inlines etc */
    private VRMLExecutionSpace parentSpace;

    /** The current world's root URL */
    private String worldURL;

    /** The major version of the spec this file belongs to. */
    private int majorVersion;

    /** The minor version of the spec this file belongs to. */
    private int minorVersion;

    /** Whether this node is static */
    private boolean staticNodes;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create a new copier that will use the given classes for generating the
     * copied values.
     *
     * @param fac The factory to use (Must generate VRMLNodeType instances)
     * @param worldURL The current world's root URL
     */
    NodeCopier(VRMLNodeFactory fac, String worldURL) {

        if(fac == null)
            throw new NullPointerException("No node factory supplied");

        factory = fac;

        nodeMap = new HashMap();
        this.worldURL = worldURL;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Process the contents of the given field. The scene provided is used to
     * contain references to any nodes such as scripts and protos that will
     * need to be processed.
     *
     * @param src The node representing the root of the nodes to copy
     * @param scn The scene to add node details to
     * @param space The execution space that holds the node instance
     * @param major The major version number of the spec to output
     * @param minor The minor version number of the spec to output
     * @param staticNode Whether this node is will be modified
     * @return The copied root node tree
     */
    VRMLNodeType copyNode(VRMLNodeType src,
                          ProtoScene scn,
                          VRMLExecutionSpace space,
                          int major,
                          int minor,
                          boolean staticNodes) {

        if(scn == null)
            throw new NullPointerException("No scene supplied");

        if(space == null)
            throw new NullPointerException("No execution space supplied");

        scene = scn;
        srcNode = src;
        parentSpace = space;
        majorVersion = major;
        minorVersion = minor;
        this.staticNodes = staticNodes;

        VRMLNodeType root = (VRMLNodeType)factory.createVRMLNode(src,
                                                                 staticNodes);
        nodeMap.put(src, root);

        return root;
    }

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {
        VRMLNodeType out_kid;

        if(used) {
            out_kid = (VRMLNodeType)nodeMap.get(child);
            if (!out_kid.isDEF())
                out_kid.setDEF();
        } else if(!(child instanceof VRMLProtoInstance)) {
            out_kid = (VRMLNodeType)factory.createVRMLNode(child,
                                                           staticNodes);
            nodeMap.put(child, out_kid);

            if(out_kid instanceof VRMLInlineNodeType)
                ((VRMLInlineNodeType)out_kid).setParentSpace(parentSpace);
            else if(out_kid instanceof VRMLScriptNodeType)
                ((VRMLScriptNodeType)out_kid).setExecutionSpace(parentSpace);

            if(out_kid instanceof VRMLExternalNodeType)
                ((VRMLExternalNodeType)out_kid).setWorldUrl(worldURL);
        } else {
            scene.addNode(child);
            out_kid = child;
            nodeMap.put(child, out_kid);
        }

        if(parent == null)
            return;

        VRMLNodeType out_parent = (VRMLNodeType)nodeMap.get(parent);
        VRMLFieldDeclaration decl = parent.getFieldDeclaration(field);
        int idx = out_parent.getFieldIndex(decl.getName());

        try {
            out_parent.setValue(idx, out_kid);
        } catch(FieldException ife) {
            errorReporter.warningReport("NodeCopier error", ife);
        }
    }
}
