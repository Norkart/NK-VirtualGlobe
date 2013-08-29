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

package org.web3d.vrml.renderer.norender.nodes.networking;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLInlineNodeType;
import org.web3d.vrml.renderer.common.nodes.networking.BaseInline;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * A node that can handle inlined content from other VRML worlds.
 * <p>
 *
 * This implementation does not care whether the source world came from a
 * UTF8 or XML encoded file.
 * <p>
 *
 * While the node is awaiting content to be downloaded, it will put a wireframe
 * box around the suggested bounds of the content. If no bounds are set then
 * a 1x1x1 box is placed at the local origin. If the URL given is null, then
 * the outline box will not be shown.
 * <p>
 * TODO:<br>
 * - Implement a scheme to allow the updating of the contents at runtime when
 *   the URL changes. It currently removes the old content, but does not
 *   inform any ContentLoadManager to fetch it's new values.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRInline extends BaseInline
    implements VRMLInlineNodeType, NRVRMLNode {

    /**
     * Create a new, default instance of this class.
     */
    public NRInline() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRInline(VRMLNodeType node) {
        super(node);
    }
}
