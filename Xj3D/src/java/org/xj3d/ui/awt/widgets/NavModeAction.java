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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

/**
 * An action that changes to a specific navigation mode.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class NavModeAction extends AbstractAction {
    /** Is this standalone or in a menu */
    private boolean standAlone;

    /** A BrowserCore instance to handle fitToWorld */
    private BrowserCore browserCore;

    /** The mode */
    private String mode;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The Icon
     * @param modeString The mode string
     * @param accelKey The accelerator key
     * @param core The browser core
     */
    public NavModeAction(boolean standAlone, Icon icon, String modeString, KeyStroke accelKey, BrowserCore core) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, modeString);
        }

        this.standAlone = standAlone;
        this.mode = modeString;
        browserCore = core;

        putValue(ACCELERATOR_KEY, accelKey);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(SHORT_DESCRIPTION, modeString + " mode");
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
        browserCore.setNavigationMode(mode);
    }
}
