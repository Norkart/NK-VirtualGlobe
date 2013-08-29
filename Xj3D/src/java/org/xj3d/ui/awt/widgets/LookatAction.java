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
 * An action that changes the navigation mode to LookAt.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class LookatAction extends AbstractAction {
    /** A BrowserCore instance to handle fitToWorld */
    private BrowserCore browserCore;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param core The browser core
     */
    public LookatAction(boolean standAlone, Icon icon, BrowserCore core) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Lookat");
        }

        browserCore = core;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        putValue(SHORT_DESCRIPTION, "Lookat Navigation");
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
        if(browserCore != null)
            browserCore.setNavigationMode(Xj3DConstants.LOOKAT_NAV_MODE);
    }
}
