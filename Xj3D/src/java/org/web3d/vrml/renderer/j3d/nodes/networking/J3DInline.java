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

package org.web3d.vrml.renderer.j3d.nodes.networking;

// External imports
import javax.media.j3d.*;

import java.util.Map;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.util.ObjectArray;

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLInlineNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.networking.BaseInline;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;


/**
 * A node that can handle inlined content from other VRML worlds.
 * <p>
 *
 * This implementation does not care whether the source world came from a
 * UTF8 or XML encoded file, so long as the content is a
 * {@link org.web3d.vrml.j3d.J3DVRMLScene}.
 * <p>
 *
 * While the node is awaiting content to be downloaded, it will put a wireframe
 * box around the suggested bounds of the content. If no bounds are set then
 * a 1x1x1 box is placed at the local origin. If the URL given is null, then
 * the outline box will not be shown.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class J3DInline extends BaseInline
    implements J3DVRMLNode,
               J3DParentPathRequestHandler,
               J3DPathAwareNodeType {

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    /** J3D Implementation that we put the content in */
    private BranchGroup j3dContentImpl;

    /** j3D node returned to the user of this node */
    private SharedGroup j3dImpl;

    /** Shared J3D box outline */
    private static SharedGroup outlineBoxImpl;

    /** Link hooked to the box */
    private BranchGroup boxGroup;


    /**
     * Static constructor builds the outline geometry needed to represent the
     * data. Gets scaled according to bounds info for individual nodes.
     */
    static {
        createOutlineGeometry();
    }

    /**
     * Create a new, default instance of this class.
     */
    public J3DInline() {
        super();

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DInline(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods defined by J3DParentPathRequestHandler
    //----------------------------------------------------------

    /**
     * Check to see if the parent path to the root of the scene graph has
     * changed in structure and the scene graph path needs to be regenerated.
     * This is a query only and if this level has not changed then the parent
     * level above should be automatically requested until the root of the
     * live scene graph is reached.
     *
     * @return true if this or a parent of this path has changed
     */
    public boolean hasParentPathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the world to this node.
     * If this node's SceneGraphObject is represented by a SharedGroup, then
     * the last item in the given path will be the Link node that is attached
     * to this object.
     *
     * @param requestingChild A reference to the child that's making the request
     * @return The list of locales and nodes in the path down to this node or null
     */
    public ObjectArray getParentPath(J3DVRMLNode requestingChild) {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        // Note that, although there is a Link node on the wireframe box,
        // that does not form part of this path from the inlined file to
        // the root of the scene graph.
        ObjectArray p_path = parentPathHandler.getParentPath(this);

        return p_path;
    }

    //----------------------------------------------------------
    // Methods defined by J3DPathAwareNodeType
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
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
        // The content has already been cleared with the setUrl call so this
        // just adds the new geometry in.
        J3DVRMLNode root_node = (J3DVRMLNode)scene.getRootNode();

        if(root_node instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)root_node).addParentPathListener(this);

        BranchGroup bg = (BranchGroup)root_node.getSceneGraphObject();

        boxGroup.detach();
        j3dContentImpl.addChild(bg);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLInlineNodeType
    //----------------------------------------------------------

    /**
     * Set the URL to a new value. If the value is null, it removes the old
     * contents (if set) and treats it as though there is no content.
     *
     * @param url The list of urls to set or null
     * @param numValid number of valid items to use from the size array
     */
    public void setUrl(String[] url, int numValid) {

        super.setUrl(url, numValid);

        if(!inSetup) {
            J3DVRMLNode root_node = (J3DVRMLNode)scene.getRootNode();

            if(root_node instanceof J3DPathAwareNodeType)
                ((J3DPathAwareNodeType)root_node).removeParentPathListener(this);

            // remove the old content because we are replacing it with the
            // new content. Change it for null rather than remove completely.
            // If we remove it then we would need to enable
            // ALLOW_CHILDREN_EXTEND.
            j3dContentImpl.setChild(boxGroup, 0);
        }
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dImpl;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
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
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // Build the bits over the top of the default box.
        // So that we can remove it we need
        // BG -> TG -> Link -> Outline SharedGroup
        // as the child of our normal branchgroup.

        Link link = new Link(outlineBoxImpl);

        // Transform to place the box in the right spot
        Transform3D t3d = new Transform3D();
        Vector3f tx = new Vector3f(vfBboxCenter);
        Vector3d scale;

        // check for default bbox value. -ve values are bad for scale...
        if(vfBboxSize[0] != -1)
            scale = new Vector3d(vfBboxSize[0], vfBboxSize[1], vfBboxSize[2]);
        else
            scale = new Vector3d(1, 1, 1);

        t3d.setTranslation(tx);
        t3d.setScale(scale);

        TransformGroup tg = new TransformGroup(t3d);

        tg.addChild(link);

        boxGroup = new BranchGroup();
        boxGroup.setCapability(BranchGroup.ALLOW_DETACH);
        boxGroup.setPickable(false);
        boxGroup.addChild(tg);

        j3dContentImpl.addChild(boxGroup);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        j3dContentImpl.setCapability(Group.ALLOW_CHILDREN_WRITE);
        j3dContentImpl.setCapability(Group.ALLOW_CHILDREN_EXTEND);
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

        stateManager.addEndOfThisFrameListener(this);
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
        // This sets back the old wireframe box. In the case where we are
        // clearing the contents of the URL through setting the load field to
        // false, this may not be desired.
        if(!inSetup && !value)
            j3dContentImpl.setChild(boxGroup, 0);

        super.setLoad(value);
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Common initialisation method regardless of constructor called.
     */
    private void init() {
        allParentPaths = new ObjectArray();
        j3dContentImpl = new BranchGroup();

        j3dImpl = new SharedGroup();
        j3dImpl.addChild(j3dContentImpl);
    }

    /**
     * Convenience method to create all of the shared geometry that is used to
     * outline where our content would go before we actually get content. This
     * is a 1x1x1 wireframe bounding box centered on the local origin. The
     * top of this collection is a SharedGroup as it is possible that a world
     * contains many Inlines and this geometry is shared between them all for
     * performance reasons.
     */
    private static void createOutlineGeometry() {
        // TODO: Shouldn't this have a branchGroup at the top for detach?
        outlineBoxImpl = new SharedGroup();

        // Create the shape and geometry. There is no need for appearance as
        // we are going to create this as an unlit, white box.
        Shape3D shape = new Shape3D();

        // setup the local user data to be not part of the collision
        // system.
        J3DUserData user_data = new J3DUserData();
        user_data.collidable = false;
        user_data.isTerrain = false;
        shape.setUserData(user_data);

        float[] coords = {
            1, 1, 1,  -1, 1, 1,  -1, -1, 1,  1, -1, 1,
            1, 1, -1,  -1, 1, -1,  -1, -1, -1,  1, -1, -1,
        };

        int[] indexes = {
            0, 1, 2, 3,
            4, 5, 6, 7,
            0, 3, 7, 4,
            5, 6, 2, 1,
            5, 1, 0, 4,
            3, 7, 6, 2
        };

        int[] strips = { 4, 4, 4, 4, 4, 4 };

        int format = GeometryArray.COORDINATES;

        IndexedLineStripArray array =
            new IndexedLineStripArray(8, format, 24, strips);

        array.setCoordinates(0, coords);
        array.setCoordinateIndices(0, indexes);

        shape.addGeometry(array);
        outlineBoxImpl.addChild(shape);
    }
}
