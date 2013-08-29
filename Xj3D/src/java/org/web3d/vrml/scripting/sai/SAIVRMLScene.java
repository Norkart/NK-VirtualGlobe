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

package org.web3d.vrml.scripting.sai;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLExternProtoDeclare;
import org.web3d.vrml.nodes.VRMLLayerSetNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoDeclare;
import org.web3d.vrml.nodes.VRMLScene;

/**
 * A representation of a complete scene in VRML.
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
 * @version $Revision: 1.4 $
 */
public class SAIVRMLScene extends AbstractScene implements VRMLScene {

    /** The root node of the scene */
    private VRMLLayerSetNodeType layerSet;

    /** Set for doing fast checks on the route existance */
    private HashSet routeSet;

    /** The map of all EXPORTs by name to DEF name (value) */
    private HashMap exportMap;

    /** A pointer to the first proto added to this scene */
    private VRMLProtoDeclare firstProto;

    /** Mapping of all PROTO declarations in this scene */
    private HashMap protos;

    /** Mapping of all EXTERNPROTO declarations in this scene */
    private HashMap externprotos;

    /**
     * Construct a new default instance of the scene for use by the SAI.
     *
     * @param data The meta data to be associated with this scene
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    public SAIVRMLScene(SceneMetaData data, int major, int minor) {
        super(major, minor);

        metaData = data;
        exportMap = new HashMap();
        protos = new HashMap();
        externprotos = new HashMap();
        routeSet = new HashSet();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLScene
    //----------------------------------------------------------

    /**
     * Get the first PROTO declared in this scene.  EXTERNPROTO's are not
     * included in this list. If no protos are declared it will return null.
     * If the proto is later removed then this will return null;
     *
     * @return The first proto declared or null
     */
    public VRMLProtoDeclare getFirstProto() {
        return firstProto;
    }

    /**
     * Get the first LayerSet declared in this scene. If there is no
     * LayerSet defined, return null.
     *
     * @return The first proto declared or null
     */
    public VRMLLayerSetNodeType getFirstLayerSet() {
        return layerSet;
    }

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto instances in this scene
     */
    public Map getProtos() {
        return protos;
    }

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public Map getExternProtos() {
        return externprotos;
    }

    /**
     * Get the list of exports from this file. The map is the exported name
     * to the node instance. If there are no nodes exported, the map will be
     * empty. Note that exported nodes is not a valid concept for VRML97. It
     * only exists for X3D V3.0 and above.
     *
     * @return A map of the exported nodes
     */
    public Map getExports() {
        return exportMap;
    }

    //----------------------------------------------------------
    // Methods defined by AbstractScene
    //----------------------------------------------------------

    //----------------------------------------------------------
    // Methods defined by AbstractScene
    //----------------------------------------------------------

    /**
     * Add a node to this scene. Override the base class method so that we
     * can track the first added layer set to this scene.
     *
     * @param node The node to add
     */
    public void addNode(VRMLNode node) {
        super.addNode(node);

        if(layerSet == null) {
            int p_type = node.getPrimaryType();
            if(p_type == TypeConstants.LayerSetNodeType)
                layerSet = (VRMLLayerSetNodeType)node;
        }
    }

    /**
     * Remove a node from this scene. Override the base class method so that we
     * can track the first layer set to this scene.
     *
     * @param node The node to add
     */
    public void removeNode(VRMLNode node) {
        super.removeNode(node);

        if(node == layerSet) {
            ArrayList sets = getByPrimaryType(TypeConstants.LayerSetNodeType);

            if(sets.size() == 0)
                layerSet = null;
            else
                layerSet = (VRMLLayerSetNodeType)sets.get(0);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add a new ROUTE instance to the internal list.
     *
     * @param node The new ROUTE instance to be added
     */
    public void addRoute(ROUTE node) {
        if((node != null) && !routeSet.contains(node)) {
            routeSet.add(node);
            routeList.add(node);
        }
    }

    /**
     * Remove a route instance from the internal list. If the list doesn't
     * know about this instance, it is quietly ignored.
     *
     * @param node The ROUTE to remove
     */
    public void removeRoute(ROUTE node) {
        routeList.remove(node);
        routeSet.remove(node);
    }

    /**
     * Add a new template to this scene. Each template is assumed to be
     * independent of the others. If a template contains further templates,
     * the nested instances should not be added to this scene.
     *
     * @param template The new template instance to use
     */
    public void addTemplate(VRMLNodeTemplate template) {
        if(template instanceof VRMLProtoDeclare)
            addProto((VRMLProtoDeclare)template);
        else
            addExternProto((VRMLExternProtoDeclare)template);
    }

    /**
     * Remove a template from this scene. The implementation shall silently
     * ignore a request to remove a template that does not exist in this scene.
     * If instances of this template already exist, they shall remain in the
     * scene.
     *
     * @param template The template instance to remove
     */
    public void removeTemplate(VRMLNodeTemplate template) {
        if(template instanceof VRMLProtoDeclare)
            removeProto((VRMLProtoDeclare)template);
        else
            removeExternProto((VRMLExternProtoDeclare)template);
    }

    /**
     * Add a new proto instance to the internal list.
     *
     * @param node The new proto instance to be added
     */
    public void addProto(VRMLProtoDeclare node) {
        if(node != null) {
            super.addTemplate(node);
            protos.put(node.getVRMLNodeName(), node);

            if(firstProto == null)
                firstProto = node;
        }
    }

    /**
     * Remove a proto instance from the internal list.
     *
     * @param node The new proto instance to be removed
     */
    public void removeProto(VRMLProtoDeclare node) {
        if(firstProto == node)
            firstProto = null;

        protos.remove(node.getVRMLNodeName());
        super.removeTemplate(node);
    }

    /**
     * Add a new EXTERNPROTO instance to the internal list.
     *
     * @param node The new EXTERNPROTO instance to be added
     */
    public void addExternProto(VRMLExternProtoDeclare node) {
        if(node != null) {
            super.addTemplate(node);
            externprotos.put(node.getVRMLNodeName(), node);
        }
    }

    /**
     * Remove an EXTERNPROTO instance from the internal list.
     *
     * @param node The EXTERNPROTO instance to be removed
     */
    public void removeExternProto(VRMLExternProtoDeclare node) {
        externprotos.remove(node.getVRMLNodeName());
        super.removeTemplate(node);
    }

    /**
     * Add a new export name to the map. If there is a Exported name of this type
     * already registered, it is replace with the incoming instance (as per
     * VRML specification.
     *
     * @param name The name of the node to be replaced
     * @param def The DEF name of the local node, may be null
     */
    public void addExportedNode(String name, String def) {
        if(name != null) {
            if(def != null)
                exportMap.put(name, def);
            else
                exportMap.put(name, name);
        }
    }

    /**
     * Remove a export name from the map.
     *
     * @param name The name of the node to be removed
     */
    public void removeExportedNode(String name) {
        exportMap.remove(name);
    }
}
