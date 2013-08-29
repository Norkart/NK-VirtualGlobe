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

package org.xj3d.loaders.j3d;

// External imports
import java.util.*;

import javax.media.j3d.*;
import com.sun.j3d.loaders.Scene;

// Local imports
import org.web3d.util.FloatArray;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DLightNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DViewpointNodeType;

/**
 * Java3D representation of a complete scene in VRML.
 * <p>
 *
 * The scene returns lists of nodes of the given type. This list contains
 * all of the nodes of that type in the order that they are declared in the
 * incoming stream. As the scene changes due to scripting and external
 * interactions, it will add new instances of these nodes to the end of the
 * list. If there is none of the given node types, the methods shall return
 * empty lists.
 * <p>
 * A requirement of the VRML specification is that if there is no Viewpoint
 * described in the file, the browser shall add an automatic one. As this scene
 * may be used in other applications than a VRML browser, we do not
 * automatically insert the viewpoint. If none is defined, the viewpoint list
 * returned will be empty and it is up to the loading application to add its
 * own as appropriate.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class J3DScene implements Scene {

    // Constants to make generation easier

    private static final Background[] EMPTY_BACKGROUND = new Background[0];
    private static final Behavior[] EMPTY_BEHAVIOR = new Behavior[0];
    private static final Fog[] EMPTY_FOG = new Fog[0];
    private static final Light[] EMPTY_LIGHT = new Light[0];
    private static final Sound[] EMPTY_SOUND = new Sound[0];
    private static final TransformGroup[] EMPTY_VIEW = new TransformGroup[0];

    /** The description of the VRML scene provided */
    private String description;

    /** The root node of the scene */
    private BranchGroup rootNode;

    /** The map of all DEFs by DEF name (key) to J3DVRMLNode (value) */
    private Hashtable defMap;

    /** List of all backgrounds as J3D SceneGraphObjects */
    private ArrayList backgrounds;

    /** ArrayList of all behaviors as J3D SceneGraphObjects */
    private ArrayList behaviors;

    /** ArrayList of all Fogs as J3D SceneGraphObjects */
    private ArrayList fogs;

    /** ArrayList of all FOVs as float[] */
    private float[] fovs;

    /** ArrayList of all lights as J3D SceneGraphObjects */
    private ArrayList lights;

    /** ArrayList of all sounds as J3D SceneGraphObjects */
    private ArrayList sounds;

    /** ArrayList of all views as J3D SceneGraphObjects */
    private ArrayList views;

    /**
     * Construct a new default instance of this class. Package private
     * because we only want the VRMLLoader and classes in this package to be
     * the originator of the instance. Others, can however, add to this.
     */
    J3DScene() {
        defMap = new Hashtable();
        backgrounds = new ArrayList(0);
        behaviors = new ArrayList(0);
        fogs = new ArrayList(0);
        sounds = new ArrayList(0);
        lights = new ArrayList(0);
        views = new ArrayList(0);
        fovs = new float[0];
    }

    /**
     * Set the VRML description information.
     *
     * @param ver A string representation of the description
     */
    public void setDescription(String ver) {
        description = ver;
    }

    /**
     * Get the VRML description information. This is the Description of the VRML
     * specification that was declared in the first line of the file.
     *
     * @return The description of this file
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method returns an array of all Background nodes defined in the
     * file. This listing will also include and Protos that are defined with
     * their primary node type as being Viewpoint.
     *
     * @return The list of backgrounds registered
     */
    public Background[] getBackgroundNodes()  {
        Background[] ret_val = null;

        if(backgrounds.size() != 0)
            ret_val = (Background[])backgrounds.toArray(EMPTY_BACKGROUND);

        return ret_val;
    }

    /**
     * This method returns a group containing all of the Behavior nodes in the
     * scene.
     *
     * @return The list of behaviors registered
     */
    public Behavior[] getBehaviorNodes()  {
        Behavior[] ret_val = null;

        if(behaviors.size() != 0)
            ret_val = (Behavior[])behaviors.toArray(EMPTY_BEHAVIOR);

        return ret_val;
    }

    /**
     * This method returns an array of all Fog nodes defined in the file.
     * This listing will also include and Protos that are defined with their
     * primary node type as being Fog.
     *
     * @return The list of fogs registered
     */
    public Fog[] getFogNodes()  {
        Fog[] ret_val = null;

        if(fogs.size() != 0)
            ret_val = (Fog[])fogs.toArray(EMPTY_FOG);

        return ret_val;
    }

    /**
     * This method returns an array of floats that contains the
     * horizontal field of view values for each corresponding entry in the
     * array of view groups returned by the method getViewGroups.
     *
     * @return The list of field of views registered
     */
    public float[] getHorizontalFOVs()  {
        float[] ret_val = null;

        if(fovs != null) {
            ret_val = new float[fovs.length];
            System.arraycopy(fovs, 0, ret_val, 0, fovs.length);
        }

        return ret_val;
    }

    /**
     * This method returns an array of all Lights defined in the file.
     * This listing will also include and Protos that are defined with their
     * primary node type as being LightNodeType.
     *
     * @return The list of lights registered
     */
    public Light[] getLightNodes() {
        Light[] ret_val = null;

        if(lights.size() != 0)
            ret_val = (Light[])lights.toArray(EMPTY_LIGHT);

        return ret_val;
    }

    /**
     * This method returns an array of all of the Sound nodes defined in the
     * file. This listing will also include and Protos that are defined with
     * their primary node type as being SoundNodeType.
     *
     * @return The list of sounds registered
     */
    public Sound[] getSoundNodes()  {
        Sound[] ret_val = null;

        if(sounds.size() != 0)
            ret_val = (Sound[])sounds.toArray(EMPTY_SOUND);

        return ret_val;
    }

    /**
     * This method returns an array of all View Groups defined in the file.
     * This listing will also include any Protos that are defined with their
     * primary node type as being Viewpoint.
     *
     * @return The list of viewpoints registered
     */
    public TransformGroup[] getViewGroups() {
        TransformGroup[] ret_val = null;

        if(views.size() != 0)
            ret_val = (TransformGroup[])views.toArray(EMPTY_VIEW);

        return ret_val;
    }

    /**
     * Get the node that forms the root of this scene.
     *
     * @return A reference to the root node of the scene
     */
    public BranchGroup getSceneGroup() {
        return rootNode;
    }

    /**
     * Get a list of the nodes that have been named with DEF in this scene.
     * The map is keyed by the DEF name string and the values are the
     * <code>SceneGraphObject</code> instances. If there are no nodes marked
     * with DEF then the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public Hashtable getNamedObjects() {
        return defMap;
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    /**
     * Add a Java3D Behavior to the system for later use
     *
     * @param bh The behavior instance to add
     */
    void addBehavior(Behavior bh) {
        if(bh != null)
            behaviors.add(bh);
    }

    /**
     * Fill the values of this scene with the given information, The set does
     * not set the root branch group, That is done separately.
     *
     * @param vrmlScene The description of the scene
     */
    void setValues(VRMLScene vrmlScene) {

        // Place all the DEF information into the Scene. Some VRML nodes do
        // not have a Java3D equivalent. For example interpolators and
        // TimeSensors will return null for getSceneGraphObject.
        Map vrml_map = vrmlScene.getDEFNodes();

        Set key_set = vrml_map.keySet();
        Iterator itr = key_set.iterator();
        J3DVRMLNode node;

        while(itr.hasNext()) {
            Object key = itr.next();
            node = (J3DVRMLNode)vrml_map.get(key);
            SceneGraphObject sgo = node.getSceneGraphObject();
            if(sgo != null)
                defMap.put(key, sgo);
        }

        int i;
        ArrayList list =
            vrmlScene.getBySecondaryType(TypeConstants.BindableNodeType);
        int size = list.size();
        FloatArray fov_array = new FloatArray();

        for(i = 0; i < size; i++) {
            node = (J3DVRMLNode)list.get(i);

            int p_type = node.getPrimaryType();

            switch(p_type) {
                case TypeConstants.BackgroundNodeType:
                    backgrounds.add(node.getSceneGraphObject());
                    break;

                case TypeConstants.FogNodeType:
                    fogs.add(node.getSceneGraphObject());
                    break;

                case TypeConstants.ViewpointNodeType:
                    // TODO: Need to add OrthViewpoint support
                    fov_array.add(((VRMLViewpointNodeType)node).getFieldOfView()[0]);
                    J3DViewpointNodeType jvn = (J3DViewpointNodeType)node;
                    views.add(jvn.getPlatformGroup());
                    break;
            }
        }

        list = vrmlScene.getByPrimaryType(TypeConstants.LightNodeType);
        size = list.size();

        lights.ensureCapacity(size * 2);

        for(i = 0; i < size; i++) {
            J3DLightNodeType lightNode = (J3DLightNodeType)list.get(i);
            Light[] lightList = lightNode.getLights();

            for(int j = 0; j < lightList.length; j++) {
                lights.add(lightList[j]);
            }
        }

        list = vrmlScene.getByPrimaryType(TypeConstants.LightNodeType);
        size = list.size();

        for(i = 0; i < size; i++) {
            node = (J3DVRMLNode)list.get(i);
            sounds.add(node.getSceneGraphObject());
        }

        fovs = fov_array.toArray();
    }

    /**
     * Set the root node separately to the rest.
     *
     * @param node The parent node to use for the root
     */
    void setRootNode(BranchGroup node) {
        rootNode = node;
    }
}
