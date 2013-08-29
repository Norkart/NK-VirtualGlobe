/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.networking;

// External imports
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.networking.BaseInline;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

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
 * @version $Revision: 1.11 $
 */
public class OGLInline extends BaseInline
    implements OGLVRMLNode, NodeUpdateListener {

    /** The renderable scenegraph node */
    private SharedGroup implGroup;

    /**
     * Create a new, default instance of this class.
     */
    public OGLInline() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLInline(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
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

        implGroup = new SharedGroup();

        if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1) {
            float[] min = new float[3];
            min[0] = vfBboxCenter[0] - vfBboxSize[0] * 0.5f;
            min[1] = vfBboxCenter[1] - vfBboxSize[1] * 0.5f;
            min[2] = vfBboxCenter[2] - vfBboxSize[2] * 0.5f;

            float[] max = new float[3];
            max[0] = vfBboxCenter[0] + vfBboxSize[0] * 0.5f;
            max[1] = vfBboxCenter[1] + vfBboxSize[1] * 0.5f;
            max[2] = vfBboxCenter[2] + vfBboxSize[2] * 0.5f;

            BoundingBox bbox = new BoundingBox(min, max);
            implGroup.setBounds(bbox);
        }
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
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
    // Methods defined by FrameStateManagerListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the scene graph with the
     * loaded scene structure at the end of the frame to avoid issues with
     * multiple access to the scen graph.
     */
    public void allEventsComplete() {
        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
    }

    //----------------------------------------------------------
    // Methods defined by UpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        // The content has already been cleared with the setUrl call so this
        // just adds the new geometry in or removes the old geometry if
        // the load field was set to FALSE.
        if(scene == null)
            return;

        OGLVRMLNode root_node = (OGLVRMLNode)scene.getRootNode();
        Group grp = (Group)root_node.getSceneGraphObject();

        if(vfLoad)
            implGroup.addChild(grp);
        else {
            implGroup.removeChild(grp);
            scene = null;
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternalNodeType
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

        if (implGroup.isLive()) {
            stateManager.addEndOfThisFrameListener(this);
        } else {
            updateNodeBoundsChanges(null);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseInline
    //----------------------------------------------------------

    /**
     * Convenience method to handle the load field value. When value is
     * <code>true</code> replace the new URL with the old value by placing it
     * immediately on the load queue. If the value is <code>false</code> then
     * it should immediately remove the content. The derived classes should
     * make sure they override this method and handle the content removal.
     * The derived class should do all it's work before calling this method.
     *
     * @param value The new load field state
     */
    protected void setLoad(boolean value)
        throws InvalidFieldException
    {
        if(!inSetup && !value) {
            if (implGroup.isLive())
                implGroup.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGroup);
        }
        super.setLoad(value);
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
