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

// Standard imports
import  javax.swing.*;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Application specific imports
import org.j3d.util.ImageLoader;
import org.j3d.ui.navigation.NavigationState;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.NavigationStateListener;

/**
 * A toolbar for all navigation commands.
 * <p>
 * This code is liberally borrowed from the j3d.org codebase and modified for
 * X3D semantics.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class NavigationToolbar extends JPanel
    implements ActionListener, NavigationStateListener {

    // Constants for images

    /** The name of the file for the pan cursor image */
    private static final String PAN_BUTTON = "images/navigation/ButtonPan.gif";

    /** The name of the file for the fly cursor image */
    private static final String FLY_BUTTON = "images/navigation/ButtonFly.gif";

    /** The name of the file for the fly cursor image */
    private static final String WALK_BUTTON = "images/navigation/ButtonWalk.gif";

    /** The name of the file for the fly cursor image */
    private static final String TILT_BUTTON = "images/navigation/ButtonTilt.gif";

    /** The name of the file for the fly cursor image */
    private static final String EXAMINE_BUTTON = "images/navigation/ButtonExamine.gif";

    // Local variables

    /** The current navigation state either set from us or externally */
    private int navigationState = NavigationState.WALK_STATE;

    /** The last known navigation state list for updating nav mode */
    private String navigationModes[];

    /** Button group holding the navigation state buttons */
    private ButtonGroup navStateGroup;

    /** Button representing the fly navigation state */
    private JToggleButton flyButton;

    /** Button representing the pan navigation state */
    private JToggleButton panButton;

    /** Button representing the tilt navigation state */
    private JToggleButton tiltButton;

    /** Button representing the walk navigation state */
    private JToggleButton walkButton;

    /** Button representing the examine navigation state */
    private JToggleButton examineButton;

    /** The last selected button */
    private JToggleButton lastButton;

    /** Flag to indicate if user state selection is allowed */
    private boolean allowUserSelect;

    /** The universe to register nav changes */
    private BrowserCore universe;

    /**
     * Create a new horizontal navigation toolbar with an empty list of
     * viewpoints and disabled user selection of state.
     */
    public NavigationToolbar(BrowserCore uni) {
        this(uni, true);
    }

    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param horizontal True to lay out the buttons horizontally
     */
    public NavigationToolbar(BrowserCore uni, boolean horizontal) {
        universe = uni;

        if(horizontal)
            setLayout(new GridLayout(1, 3));
        else
            setLayout(new GridLayout(3, 1));

        navStateGroup = new ButtonGroup();

        Icon icon = ImageLoader.loadIcon(FLY_BUTTON);

        flyButton = new JToggleButton(icon, false);
        flyButton.setMargin(new Insets(0,0,0,0));
        flyButton.setToolTipText("Fly");
        flyButton.addActionListener(this);
        navStateGroup.add(flyButton);
        add(flyButton);

        icon = ImageLoader.loadIcon(PAN_BUTTON);
        panButton = new JToggleButton(icon, false);
        panButton.setMargin(new Insets(0,0,0,0));
        panButton.setToolTipText("Pan");
        panButton.addActionListener(this);
        navStateGroup.add(panButton);
        add(panButton);

        icon = ImageLoader.loadIcon(TILT_BUTTON);
        tiltButton = new JToggleButton(icon, false);
        tiltButton.setMargin(new Insets(0,0,0,0));
        tiltButton.setToolTipText("Tilt");
        tiltButton.addActionListener(this);
        navStateGroup.add(tiltButton);
        add(tiltButton);

        icon = ImageLoader.loadIcon(WALK_BUTTON);
        walkButton = new JToggleButton(icon, false);
        walkButton.setMargin(new Insets(0,0,0,0));
        walkButton.setToolTipText("Walk");
        walkButton.addActionListener(this);
        navStateGroup.add(walkButton);
        add(walkButton);

        icon = ImageLoader.loadIcon(EXAMINE_BUTTON);
        examineButton = new JToggleButton(icon, false);
        examineButton.setMargin(new Insets(0,0,0,0));
        examineButton.setToolTipText("Examine");
        examineButton.addActionListener(this);
        navStateGroup.add(examineButton);
        add(examineButton);

        allowUserSelect = false;
        setEnabled(false);
    }

    //----------------------------------------------------------
    // Methods from the NavigationStateListener interface.
    //----------------------------------------------------------
    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx) {
        if (navigationModes != null) {
           String newMode = navigationModes[idx];

           if (newMode.equalsIgnoreCase("walk"))
               walkButton.setSelected(true);
           else if (newMode.equalsIgnoreCase("fly"))
               flyButton.setSelected(true);
           else if (newMode.equalsIgnoreCase("xj3d_tilt"))
               tiltButton.setSelected(true);
           else if (newMode.equalsIgnoreCase("xj3d_pan"))
               panButton.setSelected(true);
           else if (newMode.equalsIgnoreCase("examine"))
               examineButton.setSelected(true);
        }
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numModes The number of modes in the list
     */
    public void navigationListChanged(String[] modes, int numModes) {

        if (navigationModes == null || navigationModes.length != numModes)
            navigationModes = new String[numModes];

        System.arraycopy(modes,0,navigationModes,0,numModes);

        boolean found_any = false;

        walkButton.setEnabled(false);
        flyButton.setEnabled(false);
        examineButton.setEnabled(false);
        panButton.setEnabled(false);
        tiltButton.setEnabled(false);

        String mode;

        for(int i=0; i < numModes; i++) {
            if (modes[i].equals("ANY")) {
                found_any = true;
                break;
            }
            mode = modes[i];

            if (mode.equals("WALK"))
                walkButton.setEnabled(true);
            else if (mode.equals("FLY"))
                flyButton.setEnabled(true);
            else if (mode.equals("EXAMINE"))
                examineButton.setEnabled(true);
            else if (mode.equals("xj3d_PAN"))
                panButton.setEnabled(true);
            else if (mode.equals("xj3d_TILT"))
                tiltButton.setEnabled(true);
        }

        if (found_any) {
            walkButton.setEnabled(true);
            flyButton.setEnabled(true);
            examineButton.setEnabled(true);
            panButton.setEnabled(true);
            tiltButton.setEnabled(true);
        }
    }

    //----------------------------------------------------------
    // Local public methods
    //----------------------------------------------------------

    /**
     * Toggle whether the UI will allow the user to change the state selected
     * for navigation.
     *
     * @param allow True if the user can change the navigation state
     */
    public void setAllowUserStateChange(boolean allow) {
        allowUserSelect = allow;
        setEnabled(allowUserSelect);
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event on one of the buttons.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt) {
        if(!allowUserSelect)
            return;

        Object src = evt.getSource();

        if(src == flyButton) {
            navigationState = NavigationState.FLY_STATE;
            universe.setNavigationMode("FLY");
        }
        else if(src == panButton) {
            navigationState = NavigationState.PAN_STATE;
            universe.setNavigationMode("xj3d_PAN");
        }
        else if(src == tiltButton) {
            navigationState = NavigationState.TILT_STATE;
            universe.setNavigationMode("xj3d_TILT");
        }
        else if(src == walkButton) {
            navigationState = NavigationState.WALK_STATE;
            universe.setNavigationMode("WALK");
        }
        else if(src == examineButton) {
            navigationState = NavigationState.EXAMINE_STATE;
            universe.setNavigationMode("EXAMINE");
        }
    }

    //----------------------------------------------------------
    // Methods Overriding Component
    //----------------------------------------------------------

    /**
     * Set the panel enabled or disabled. Overridden to make sure the base
     * components are properly handled.
     *
     * @param enabled true if this component is enabled
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        examineButton.setEnabled(enabled);
        flyButton.setEnabled(enabled);
        walkButton.setEnabled(enabled);
        panButton.setEnabled(enabled);
    }
}
