/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

// External imports
import org.j3d.aviatrix3d.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.j3d.aviatrix3d.management.DisplayCollection;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraversalObserver;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * An action that prints information about the scene.
 *
 * Currently prints to the console.  Might be better as a Dialog.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class SceneInfoAction extends AbstractAction
   implements SceneGraphTraversalObserver {

    /** The display collection */
    private DisplayCollection displayManager;

    /** The console to print information to */
    private ErrorReporter console;

    /** The number of Shapes */
    private int numShapes;

    /** The maximum scenegraph depth */
    private int maxSceneGraphDepth;

    /** The root bounds */
    private BoundingVolume bounds;

    /**
     * Create an instance of the action class.
     *
     */
    public SceneInfoAction(ErrorReporter console, DisplayCollection displayManager) {
        super("Scene Info");

        this.console = console;
        this.displayManager = displayManager;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
        putValue(SHORT_DESCRIPTION, "View Scene Information");
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
                             int depth)
    {
        if (depth > maxSceneGraphDepth)
            maxSceneGraphDepth = depth;

        if (parent == null) {
            bounds = ((Node)child).getBounds();
            return;
        }
        if (child == null) {
        } else {
            if (child instanceof Shape3D) {
                numShapes++;
            }
        }
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        Layer[] layers = new Layer[displayManager.numLayers()];
        displayManager.getLayers(layers);
        SimpleScene currentScene;
        SimpleViewport viewport;
        SimpleLayer layer;

        System.gc();

        console.messageReport("Scene Information:");

        for(int i=0; i < layers.length; i++) {
            console.messageReport("   Layer: " + i);
            if (!(layers[i] instanceof SimpleLayer)) {
                console.warningReport("      tScene printer can only handle SimpleLayers currently", null);
                continue;
            }

            layer = (SimpleLayer) layers[i];

            if (!(layer.getViewport() instanceof SimpleViewport)) {
                console.warningReport("      Scene printer can only handle SimpleViewports currently", null);
                continue;
            }

            viewport = (SimpleViewport) layer.getViewport();

            if (!(viewport.getScene() instanceof SimpleScene)) {
                console.warningReport("      Scene printer can only handle SimpleScenes currently", null);
                return;
            }

            currentScene = (SimpleScene) viewport.getScene();
            Group root = currentScene.getRenderedGeometry();

            maxSceneGraphDepth = 0;
            numShapes = 0;

            SceneGraphTraverser sgt = new SceneGraphTraverser();
            sgt.setObserver(this);
            sgt.traverseGraph(root);

            float xsize, ysize, zsize = 0;
            float[] size = new float[3];

            if (bounds instanceof BoundingBox) {
                ((BoundingBox)bounds).getSize(size);
            }

            console.messageReport("      number of Shapes: " + numShapes);
            console.messageReport("      scenegraph depth: " + maxSceneGraphDepth);
            console.messageReport("      bounds: " + bounds);
            console.messageReport("      size: " + (size[0]*2) + " " + (size[1]*2) + " " + (size[2]*2));

            long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            console.messageReport("      memory used: " + mem);


        }
    }
}
