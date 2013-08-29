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

package org.xj3d.loaders.ogl;

// External imports
import java.util.*;

import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;

// Local imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;

/**
 * Representation of a loaded VRML97 or X3D model.
 * <p>
 *
 * If the user requested a raw model, the object returned from
 * {@link #getRawModel()} will be an instance of
 * {@link org.web3d.vrml.nodes.VRMLScene} representing the root of the
 * scene graph.
 * <p>
 *
 * There will be only a single AVRuntimeComponent returned from this model -
 * an instance of {@link Xj3DClockRuntime}.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Xj3DModel implements AVModel {

    /** The group node representing the root of the scene. */
    private Group modelRoot;

    /** The object mesh, if requested */
    private VRMLScene realScene;

    /** Mapping of the names to object instances */
    private HashMap namedObjects;

    /** Mapping of objects to their externally named resources */
    private HashMap externalObjects;

    /** List of layers encountered in the file */
    private ArrayList layers;

    /** List of lights encountered in the file */
    private ArrayList lights;

    /** List of viewpoints encountered in the file */
    private ArrayList viewpoints;

    /** List of backgrounds encountered in the file */
    private ArrayList backgrounds;

    /** List of fogs encountered in the file */
    private ArrayList fogs;

    /** List of keyframe animations encountered in the file */
    private ArrayList runtimes;

    /**
     * Create a new model instance and prepare it for work
     *
     * @param scene The internal format, if requested. May be null
     */
    Xj3DModel(VRMLScene scene) {

        realScene = scene;

        OGLVRMLNode v_node = (OGLVRMLNode)scene.getRootNode();
        modelRoot = (Group)v_node.getSceneGraphObject();

        namedObjects = new HashMap();
        externalObjects = new HashMap();
        layers = new ArrayList();
        lights = new ArrayList();
        viewpoints = new ArrayList();
        backgrounds = new ArrayList();
        fogs = new ArrayList();
        runtimes = new ArrayList();
    }

    //---------------------------------------------------------------
    // Methods defined by AVModel
    //---------------------------------------------------------------

    /**
     * Get the root of the scene graph structure that represents this model.
     *
     * @return The grouping node that represents the root of the scene graph
     */
    public Group getModelRoot() {
        return modelRoot;
    }

    /**
     * Get the raw model representation of the scene as defined by a
     * loader-specific set of classes. If the loader was instructed to discard
     * this information, the method returns null.
     *
     * @return An implementation-specific object that represents the raw model
     *    format structure or null
     */
    public Object getRawModel() {
        return realScene;
    }

    /**
     * Get a mapping of any internally named objects to their corresponding
     * scene graph structure. The key of the map is the name defined in the
     * file format, and the value is the aviatrix3D scene graph structure
     * that they map to. The exact mapping that each makes is dependent on
     * the loader implementation.
     *
     * @return A map of strings to SceneGraphObject instances
     */
    public Map getNamedObjects() {
        return namedObjects;
    }

    /**
     * Get the listing of the external resources declared as being needed by
     * this file. External resources are keyed by the object to their
     * provided file name string or strings from the file. The value string(s)
     * will be exactly as declared in the file. It is expected the user
     * application will resolve to fully qualified path names to read the rest
     * of the files required.
     *
     * @return a map of the objects to their requested file name(s)
     */
    public Map getExternallyDefinedFiles() {
        return externalObjects;
    }

    /**
     * Get the list of viewpoints that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Viewpoint} instances
     * corresponding to each viewpoint declared in the file. If a file does
     * not declare any viewpoints, or the loader was requested not to load
     * viewpoints, this returns an empty list.
     *
     * @return A list of the viewpoint instances declared in the file
     */
    public List getViewpoints() {
        return viewpoints;
    }

    /**
     * Get the list of backgrounds that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Background} instances
     * corresponding to each background declared in the file. If a file does
     * not declare any backgrounds, or the loader was requested not to load
     * backgrounds, this returns an empty list.
     *
     * @return A list of the background instances declared in the file
     */
    public List getBackgrounds() {
        return backgrounds;
    }

    /**
     * Get the list of fogs that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Fog} instances
     * corresponding to each fog declared in the file. If a file does
     * not declare any fogs, or the loader was requested not to load
     * fogs, this returns an empty list.
     *
     * @return A list of the fog instances declared in the file
     */
    public List getFogs() {
        return fogs;
    }

    /**
     * Get the list of layers that are contained in the file. The list will
     * contain the {@link org.j3d.aviatrix3d.Layer} instances corresponding
     * to each layer declared in the file. If a file does not declare any
     * layers, or the loader was requested not to load layers, this returns
     * an empty list. Unlike the other methods, this method will guarantee to
     * return the layer instances in the order of front to rear.
     * <p>
     * If this model contains loaded layers, it will not return a root object.
     *
     * @return A list of the layer instances declared in the file
     * @since Aviatrix3D 2.0
     */
    public List getLayers() {
        return layers;
    }

    /**
     * Get the list of lights that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Light} instances
     * corresponding to each light declared in the file. If a file does
     * not declare any lights, or the loader was requested not to load
     * lights, this returns an empty list.
     *
     * @return A list of the light instances declared in the file
     */
    public List getLights() {
        return lights;
    }

    /**
     * Get the list of runtime components that are contained in the file. The
     * list will contain the {@link RuntimeComponent} instances used for
     * controlling animation or any other runtime capabilities inherent to
     * the file format.If a file does not declare any runtime capabilities, or
     * the loader was requested not to load runtimes, this returns an empty
     * list.
     *
     * @return A list of the RuntimeComponent instances declared in the file
     */
    public List getRuntimeComponents() {
        return runtimes;
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Add a runtime component to the list for this model. Will control one
     * of the keyframed actions like lights, mesh, camera etc.
     *
     * @param rt The runtime instance to add
     */
    void addRuntimeComponent(AVRuntimeComponent rt) {
        runtimes.add(rt);
    }

    /**
     * Fill the values of this scene with the given information, The set does
     * not set the root branch group, That is done separately.
     *
     * @param keepScene flag to know whether to keep the scene model
     *    around now or it should be discarded at the end of this call
     */
    void setValues(boolean keepScene) {

        // Place all the DEF information into the Scene. Some VRML nodes do
        // not have a Java3D equivalent. For example interpolators and
        // TimeSensors will return null for getSceneGraphObject.
        Map vrml_map = realScene.getDEFNodes();

        Set key_set = vrml_map.keySet();
        Iterator itr = key_set.iterator();
        OGLVRMLNode node;

        while(itr.hasNext()) {
            Object key = itr.next();
            node = (OGLVRMLNode)vrml_map.get(key);
            SceneGraphObject sgo = node.getSceneGraphObject();
            if(sgo != null)
                namedObjects.put(key, sgo);
        }

        int i;
        ArrayList list =
            realScene.getBySecondaryType(TypeConstants.BindableNodeType);
        int size = list.size();

        for(i = 0; i < size; i++) {
            node = (OGLVRMLNode)list.get(i);

            int p_type = node.getPrimaryType();

            switch(p_type) {
                case TypeConstants.BackgroundNodeType:
                    backgrounds.add(node.getSceneGraphObject());
                    break;

                case TypeConstants.FogNodeType:
                    fogs.add(node.getSceneGraphObject());
                    break;

                case TypeConstants.ViewpointNodeType:
                    OGLViewpointNodeType jvn = (OGLViewpointNodeType)node;
                    viewpoints.add(jvn.getPlatformGroup());
                    break;
            }
        }

        list = realScene.getByPrimaryType(TypeConstants.LightNodeType);
        size = list.size();

        lights.ensureCapacity(size);

        for(i = 0; i < size; i++) {
            node = (OGLVRMLNode)list.get(i);
            lights.add(node.getSceneGraphObject());
        }

        list = realScene.getByPrimaryType(TypeConstants.LayerNodeType);
        size = list.size();

        layers.ensureCapacity(size);

        for(i = 0; i < size; i++) {
            node = (OGLVRMLNode)list.get(i);
            layers.add(node.getSceneGraphObject());
        }

        list = realScene.getBySecondaryType(TypeConstants.SingleExternalNodeType);
        size = list.size();

        for(i = 0; i < size; i++) {
            node = (OGLVRMLNode)list.get(i);

            if(node instanceof VRMLSingleExternalNodeType) {

                String[] filenames =
                    ((VRMLSingleExternalNodeType)node).getUrl();
                externalObjects.put(node.getSceneGraphObject(), filenames);
            } else
System.out.println("Aviatrix3D loader not handling multi-external node types");
        }

        if(!keepScene)
            realScene = null;
    }
}
