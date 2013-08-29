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

package org.web3d.vrml.renderer.mobile.nodes.networking;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// Application specific imports
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.networking.BaseInline;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;

import org.web3d.vrml.renderer.mobile.sg.Group;
import org.web3d.vrml.renderer.mobile.sg.Node;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

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
 * @version $Revision: 1.3 $
 */
public class MobileInline extends BaseInline
    implements MobileVRMLNode {

    /** The renderable scenegraph node */
    private Group implGroup;

    /**
     * Create a new, default instance of this class.
     */
    public MobileInline() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public MobileInline(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the NRVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        inSetup = false;
        implGroup = new Group();
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLExternalNodeType interface.
    //----------------------------------------------------------

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

        super.setContent(mimetype, content);

        // Need to do something here!

    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Check the given list of URLs for relative references. If found, add the
     * base URL to it to make them all fully qualified. This will also set the
     * urlRelativeCheck flag to true.
     *
     * @param urls The list of URLs to check
     * @return The list of updated URLs.
     */
    private String[] checkURLs(String[] urls) {

        String[] ret_val = new String[urls.length];

        for(int i = 0; i < urls.length; i++) {
            if(urls[i].indexOf(':') == -1) {
                ret_val[i] = worldURL + urls[i];
            } else {
                ret_val[i] = urls[i];
            }
        }

        return ret_val;
    }
}
