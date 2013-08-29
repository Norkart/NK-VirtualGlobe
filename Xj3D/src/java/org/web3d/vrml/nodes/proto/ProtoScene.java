/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes.proto;

// External imports
// None

// Local imports
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.AbstractScene;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.SceneMetaData;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;

/**
 * Implementation of the {@link org.web3d.vrml.lang.BasicScene} interface that
 * is used withing protos.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class ProtoScene extends AbstractScene {

    /** Set for doing fast checks on the route existance */
    private HashSet routeSet;

    /**
     * Create an empty scene for the given spec version.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    public ProtoScene(int major, int minor) {
        super(major, minor);
        routeSet = new HashSet();
    }

    /**
     * Import the contents of the given scene into this scene. Used
     * to compose a scene from lower -level nested protos and in particular
     * the X3D SAI handling.
     *
     * @param sc The scene to import
     */
    public void mergeScene(BasicScene sc) {
        setNodeFactory(sc.getNodeFactory());

        setWorldRootURL(sc.getWorldRootURL());

        metaData = new SceneMetaData(sc.getMetaData());
    }

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
}
