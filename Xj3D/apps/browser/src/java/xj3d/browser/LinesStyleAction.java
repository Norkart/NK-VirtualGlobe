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

// Standard library imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.media.opengl.GLCapabilities;

// Application specific imports
import org.xj3d.ui.awt.widgets.SwingStatusBar;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

/**
 * An action that can be used to change rendering styles to points
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class LinesStyleAction extends AbstractAction {
    /** The status bar */
    protected SwingStatusBar statusBar;

    /** The browser core */
    protected BrowserCore universe;

    /** Is rendering style active */
    private boolean active;

    /**
     * Create an instance of the action class.
     *
     * @param manager The surface manager
     */
    public LinesStyleAction(BrowserCore core, SwingStatusBar statusBar) {
        super("Lines");

        universe = core;
        this.statusBar = statusBar;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Wireframe style rendering");

    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        if (!active) {
            statusBar.setStatusText("Wireframe rendering mode enabled");
            universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
            active = true;
        } else {
            statusBar.setStatusText("Shaded rendering mode enabled");
            universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
            active = false;
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Sets this style to non active.  Used when Shaded mode is selected.
     */
    public void reset() {
        active = false;
    }
}
