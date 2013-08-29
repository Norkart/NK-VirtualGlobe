/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.Iterator;

// Local imports
import org.web3d.x3d.sai.*;

import org.web3d.vrml.lang.NodeTemplateToInstanceCreator;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNodeTemplate;
import org.web3d.vrml.nodes.VRMLExternalNodeType;
import org.web3d.vrml.nodes.VRMLExternProtoDeclare;

/**
 * The representation of an EXTERNPROTO declaration.
 * <p>
 *
 * This is the representation of the declaration, not of a runtime node. For
 * this reason you cannot access the internals, nor can you work with the
 * individual field values. You can, however, perform basic introspection
 * tasks such as looking at the available field definitions and seeing the
 * basic node type.
 * <p>
 *
 * The implementation extends the basic proto declaration class to represent
 * externally represented information. While all the basic method calls are
 * supported, they do need to have thier behaivour modified somewhat to deal
 * with the external nature of this structure. The following modifications to
 * the behaviours are made:
 * <p>
 *
 * <i>createInstance()</i><br>
 * <i>getNodeType()</i><br>
 * If the instance has not loaded yet or failed to load, this will generate
 * an InvalidNodeException in addition to the normal reasons. A user should
 * check the load state first before trying to create an instance if they
 * wish to avoid this error.
 *
 * <p>
 * <i>getFieldDeclarations()</i><br>
 * When queried, this will return the definitions of the fields as declared
 * in the externproto, not the underlying proto definition.
 * <p>
 *
 * No access is provided to the underlying proto declaration that fulfills
 * this external representation. If the user wishes to access that, then
 * they may make use of the createVrmlFromURL feature of the browser to load
 * the individual file as needed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class SAIExternProtoDeclaration extends SAIProtoDeclaration
    implements X3DExternProtoDeclaration {

    /** The proto as an external declaration */
    private VRMLExternProtoDeclare externDecl;

    /**
     * Create a new instance of this class to represent the given decl.
     *
     * @param decl The declaration to copy
     * @param space The space this instance belongs to
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    SAIExternProtoDeclaration(VRMLExternProtoDeclare decl,
                              VRMLExecutionSpace space,
                              ReferenceQueue refQueue,
                              FieldFactory fac,
                              FieldAccessListener fal,
                              BaseNodeFactory bnf) {
        super(decl, space, refQueue, fac, fal, bnf);

        externDecl = decl;
    }

    /**
     * Check to see if the underlying definition has been loaded yet.
     * There are 4 load states - not loaded, in-progress, failed and
     * complete. If an externproto is never used in the containing scene
     * then the browser may never even attempt to load it. This is the
     * not-loaded state. A user may force the browser to load the defintion
     * through the loadNow() method later on.
     *
     * @return The current load state
     * @see X3DLoadStateTypes
     */
    public int getLoadState() {
        int ret_val = X3DLoadStateTypes.LOAD_NOT_STARTED;

        switch(externDecl.getLoadState()) {
            case VRMLExternalNodeType.NOT_LOADED:
                ret_val = X3DLoadStateTypes.LOAD_NOT_STARTED;
                break;

            case VRMLExternalNodeType.LOADING:
                ret_val = X3DLoadStateTypes.LOAD_IN_PROGRESS;
                break;

            case VRMLExternalNodeType.LOAD_FAILED:
                ret_val = X3DLoadStateTypes.LOAD_FAILED;
                break;

            case VRMLExternalNodeType.LOAD_COMPLETE:
                ret_val = X3DLoadStateTypes.LOAD_COMPLETED;
                break;
        }

        return ret_val;
    }

    /**
     * Ask the browser to load the defintion now. If the load state is
     * already in progress, failed or completed, then this request will be
     * ignored.
     */
    public void loadNow() {
System.out.println("SAIExternProtoDeclaration.loadNow() not implemented yet");
    }

    /**
     * Get the URLs used to describe this external prototype. If no URLs
     * are defined in the file, this will return null.
     *
     * @return The URLs used to describe this node's contents
     */
    public String[] getURLs() {
        return externDecl.getUrl();
    }
}
