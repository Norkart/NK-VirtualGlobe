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
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * An action that can be used to undo the last modification to
 * the model.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class HelpAction extends AbstractAction {

    private HelpBroker hb;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     * @param defaultDir The default directory to save files
     * @param parent The parent componet for the dialog
     * @param fmgr The filemanager
     */
    public HelpAction(boolean standAlone, Icon icon) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Help");
        }

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_F1,0);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));

        putValue(SHORT_DESCRIPTION, "Help");

        buildJavaHelp();
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener interface
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
      hb.setDisplayed(true);
      hb.setCurrentView("TOC");
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    private void buildJavaHelp()
    {
        File helpsetFile = new File("./doc/javaHelp","Xj3DHelpSet.hs");

        if (!helpsetFile.exists()){
            System.out.println("Could not find ./doc/javaHelp/Xj3DHelpSet.hs");
            return;
        }

        HelpSet hs = null;
        try {
          hs = new HelpSet(null, helpsetFile.toURL());
        }
        catch (Exception e) {
          e.printStackTrace();
          return;
        }
        hb = hs.createHelpBroker();
    }
}
