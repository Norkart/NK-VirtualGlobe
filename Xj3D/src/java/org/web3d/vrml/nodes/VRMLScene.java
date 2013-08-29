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

package org.web3d.vrml.nodes;

// External imports
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.SceneMetaData;

/**
 * Abstract representation of a complete scene in VRML.
 * <p>
 *
 * The scene returns lists of nodes of the given type. This list contains
 * all of the nodes of that type in the order that they are declared in the
 * incoming stream. As the scene changes due to scripting and external
 * interactions, it will add new instances of these nodes to the end of the
 * list. If there is none of the given node types, the methods shall return
 * empty lists.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public interface VRMLScene extends BasicScene {

    /**
     * Get the first PROTO declared in this scene.  EXTERNPROTO's are not
     * included in this list.  If no protos are declared it will return null.
     *
     * @return The first proto declared or null
     */
    public VRMLProtoDeclare getFirstProto();

    /**
     * Get the first LayerSet declared in this scene. If there is no
     * LayerSet defined, return null.
     *
     * @return The first proto declared or null
     */
    public VRMLLayerSetNodeType getFirstLayerSet();

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto declarations in this scene
     */
    public Map getProtos();

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public Map getExternProtos();

    /**
     * Get the list of exports from this file. The map is the exported name
     * to the node def name. To fetch the real node instance you then need to
     * make another call to getDEFNodes(). If there are no nodes exported, the
     * map will be empty. Note that exported nodes is not a valid concept for
     * VRML97. It only exists for X3D V3.0 and above.
     *
     * @return A map of the exported nodes
     */
    public Map getExports();
}
