/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import javax.swing.Icon;
import javax.swing.Action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;

// Local imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.xj3d.core.eventmodel.ViewpointManager;

/**
 * An action that moves to the Home viewpoint on the main layer.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class HomeViewpointAction extends AbstractAction {
    /** The manager of viewpoints that we use to change them on the fly */
    private ViewpointManager vpManager;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param manager The viewpoint manager
     */
    public HomeViewpointAction(boolean standAlone, Icon icon, ViewpointManager manager) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Reset");
        }

        vpManager = manager;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        putValue(SHORT_DESCRIPTION, "Reset the current viewpoint");
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
        vpManager.resetViewpoint();
    }
}
