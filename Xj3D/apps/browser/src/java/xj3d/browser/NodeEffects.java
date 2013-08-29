/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External Imports
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.swing.*;
import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraversalObserver;

// Local Imports
// none

/**
 * Visual Effects on a node.  Used to highlite or show a node and its
 * children.
 *
 * @author Alan Hudson
 */
public class NodeEffects implements Runnable, SceneGraphTraversalObserver, NodeUpdateListener {
    /** Map for storing nodes and the appearance value */
    private HashMap<Shape3D,Appearance> appearanceMap;

    /** Is the selection logic running? */
    private boolean running;

    /** Traverser for selection showing */
    private SceneGraphTraverser sgt;

    /** What mode are we in, set or restore appearance */
    private boolean setMode;

    /** The number of flashes to perform */
    private int totalFlashes;

    /** The current number of times the selection has been flashed */
    private int currentFlashes;

    /** The length of flashes */
    private int flashLength;

    /** The node being selected */
    private Node selectedNode;

    /** The current thread */
    private Thread thread;

    /** The current alternate Appearance */
    private Appearance altApp;

    public NodeEffects() {
        running = false;

        sgt = new SceneGraphTraverser();
        sgt.setObserver(this);
        appearanceMap = new HashMap<Shape3D,Appearance> (128);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphTraversalObserver
    //---------------------------------------------------------------

    /**
     * Notification of a scene graph object that has been traversed in the
     * scene.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param shared true if the object reference has already been traversed
     *    and this is beyond the first reference
     * @param depth The depth of traversal from the top of the tree.  Starts at 0 for top.
     */
    public void observedNode(SceneGraphObject parent,
                             SceneGraphObject child,
                             boolean shared,
                             int depth) {

        if (shared || !(child instanceof Shape3D))
            return;

        if (setMode) {
            // Make all nodes transparent, save state
            Shape3D shape = (Shape3D) child;
            Appearance app = shape.getAppearance();
            if (app == null)
                return;

            appearanceMap.put(shape, app);
            shape.dataChanged(this);
        } else {
            // Restore transparency
            Shape3D shape = (Shape3D) child;
            Appearance app = shape.getAppearance();
            if (app == null)
                return;

            if (app != altApp) {
                // Appearance changed midstream, do not restore
                return;
            }

            shape.dataChanged(this);
        }
    }

    //----------------------------------------------------------
    // UpdateListener interface methods
    //----------------------------------------------------------
    public void updateNodeBoundsChanges(Object src) {
        // ignored
    }

    public void updateNodeDataChanges(Object src) {
        Shape3D shape = (Shape3D) src;

        if (setMode) {
            shape.setAppearance(altApp);
        } else {
            // Clear later might be better but timing is hard to get right
            Appearance app = appearanceMap.remove(shape);
            shape.setAppearance(app);
        }
    }

    //----------------------------------------------------------
    // Runnable interface methods
    //----------------------------------------------------------

    /**
     * Flashes the node till the maximum is reached.
     */
    public void run() {
        running = true;

        while(currentFlashes < totalFlashes) {
            if (currentFlashes % 2 == 0) {
                setAlternateAppearance(selectedNode, altApp);
            } else {
                restoreAlternateAppearance(selectedNode);
            }

            try {
                Thread.sleep(flashLength);
            } catch(Exception e) {
                // ignore
            }

            currentFlashes++;
        }

        running = false;

        selectedNode = null;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set an alternate appearance to a node and its children.
     * This class has memory for one alternate appearance.  Multiple calls
     * to this method will overwrite the previous values.
     *
     * @param node The node
     * @param app The new appearance
     */
     public void setAlternateAppearance(Node n, Appearance app) {
        altApp = app;
        setMode = true;

        if (n instanceof Shape3D) {
            // Already at shape level
            n.dataChanged(this);
            return;
        }

        sgt.reset();
        sgt.traverseGraph(n);
     }

    /**
     * Restore the alternate appearance for a node.
     *
     * @param n The node
     */
    public void restoreAlternateAppearance(Node n) {
        setMode = false;

        if (n instanceof Shape3D) {
            // Already at shape level
            n.dataChanged(this);
            return;
        }

        sgt.reset();
        sgt.traverseGraph(n);
    }

    /**
     * Blink a node and its children for a certain amount of time.
     * Blinking is implemented with an alternate transparent appearance.
     *
     * @param node The node
     * @param numFlashes The number of times to blink the node.
     * @param flashLength The length in ms of a flash
     */
    public void blink(Node n, int numFlashes, int flashLength) {
        if (running)
            throw new IllegalStateException("NodeEffects in use");

        selectedNode = n;
        totalFlashes = numFlashes;
        this.flashLength = flashLength;

        totalFlashes = numFlashes * 2;
        currentFlashes = 0;

        // Generate an invisible alternate appearance
        Appearance altApp = new Appearance();
        Material mat = new Material();
        mat.setTransparency(0);
        altApp.setMaterial(mat);

        // Walk the SG and change everything to transparent
        setAlternateAppearance(selectedNode, altApp);

        thread = new Thread(this, "NodeEffects");
        thread.start();
    }
}