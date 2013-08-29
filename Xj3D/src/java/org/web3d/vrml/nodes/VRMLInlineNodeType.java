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
package org.web3d.vrml.nodes;

// External imports
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * Denotes a node type that supports inlining content from external files.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface VRMLInlineNodeType
    extends VRMLSingleExternalNodeType,
            VRMLBoundedNodeType,
            VRMLExecutionSpace {

    /**
     * Get the parent execution space of this Inline.  This is for internal
     * usage, you cannot use this to walk back up the scenegraph because of
     * protos.
     *
     * @return The execution space of the loaded inline
     */
    public VRMLExecutionSpace getParentSpace();

    /**
     * Set the parent execution space of this Inline.  This is for internal
     * usage, you cannot use this to walk back up the scenegraph because of
     * protos.
     *
     * @param space The execution space of the loaded inline
     */
    public void setParentSpace(VRMLExecutionSpace space);

    /**
     * Set the mapping of import names to their proxy node implementations. The
     * mapping is from the exported name to the {@link ImportNodeProxy}
     * instance that it represents.
     *
     * @param imports The map of export names to proxy instances
     */
    public void setImportNodes(Map imports);
}
