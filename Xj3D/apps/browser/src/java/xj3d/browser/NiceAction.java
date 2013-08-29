/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Application specific imports
import org.xj3d.impl.core.loading.FramerateThrottle;
import org.xj3d.ui.awt.widgets.SwingStatusBar;

/**
 * An action that can be used to select nice rendering.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class NiceAction extends AbstractAction {

    /** The framerate throttle */
    private FramerateThrottle throttle;

    /** Are we currently clamping */
    private boolean nice;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /**
     * Create an instance of the action class.
     *
     * @param throttle The throttle
     * @param initial The initial value
     */
    public NiceAction(FramerateThrottle throttle, SwingStatusBar statusBar, boolean initial) {
        super("Limit framerate");

        this.nice = initial;
        this.throttle = throttle;
        this.statusBar = statusBar;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        putValue(SHORT_DESCRIPTION, "Toggle clamped rendering speed");
    }

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        if (!nice) {
            statusBar.setStatusText("Frame rate limiting enabled.");
            throttle.setMinimumNoLoading(20);
            nice = true;
        } else {
            statusBar.setStatusText("Frame rate limiting disabled.");
            throttle.setMinimumNoLoading(0);
            nice = false;
        }
    }
}
