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

// External imports
import org.j3d.aviatrix3d.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.io.File;
import java.awt.Container;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

/**
 * An action that displays a tree view of the scene.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class SceneTreeAction extends AbstractAction {
    /** The console to print information to */
    private ErrorReporter console;

    /** The brower core */
    private OGLStandardBrowserCore core;

    /** The content pane for the frame */
    private Container mainPane;

    /** The position to place the viewer.  Layout constant */
    private String position;

    /** The tree viewer */
    private SceneTreeViewer sceneTree;

    /**
     * Create an instance of the action class.
     *
     */
    public SceneTreeAction(ErrorReporter console, OGLStandardBrowserCore core, Container pane, String position) {
        super("Scene Tree");

        this.console = console;
        this.core = core;
        this.mainPane = pane;
        this.position = position;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        putValue(SHORT_DESCRIPTION, "View Scene Tree");
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        if (sceneTree == null) {
            sceneTree = new SceneTreeViewer(core, new NullNodeFilter());
            mainPane.add(sceneTree, position);
        } else {
            mainPane.remove(sceneTree);
            sceneTree = null;
        }
    }
}
