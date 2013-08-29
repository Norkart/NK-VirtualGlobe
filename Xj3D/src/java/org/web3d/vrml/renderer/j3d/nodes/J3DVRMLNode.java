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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of the basic VRMLNodeType within the Java 3D rendering system.
 * <p>
 *
 * Most of the methods are used to deal with the DEF/USE situation. DEF causes
 * some interesting problems compared to the J3D usage. In VRML you can DEF
 * any node, but in Java3D you cannot share all items in the scenegraph.
 * For example, the LOD cannot be shared across multiple parts of the same
 * scenegraph. So to deal with this we have introduced a set of methods to
 * allow a somewhat sane way of coping with the situation.
 * <p>
 * <b>Implementing DEF handling</b>
 * <p>
 * Implementing these methods changes depending on whether you are dealing
 * with branch or leaf nodes.
 * <p>
 * A branch node will always require the use of a shared group. You will know
 * this before actually receiving any node content. If this group is a
 * shared group, create the normal parent BranchGroup, then add as it's one
 * and only child the SharedGroup. Any children nodes get added to the
 * SharedGroup. Now, when someone asks for the representation, return the
 * SharedGroup instance. They are then responsible for adding the appropriate
 * {@link javax.media.j3d.Link} and parent.
 * <p>
 * Leaf nodes will need to also use SharedGroups. For example, a Shape3D will
 * need to insert a SharedGroup above itself when it has been DEFd. For nodes
 * like LOD, this shared instance will need to be a complete duplicate of
 * the children as J3D can't have shared LODs. (One possible approach is to
 * create a new LOD and then add shared groups for every child node of the LOD)
 * <p>
 * Node components will not need any extra information as Java3D allows shared
 * references to these (in fact encourages them for performance reasons).
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DVRMLNode extends VRMLNodeType {

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
    public void setCapabilityOverrideMap(Map capBits, Map freqBits);


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
    public void setCapabilityRequiredMap(Map capBits, Map freqBits);

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject();
}
