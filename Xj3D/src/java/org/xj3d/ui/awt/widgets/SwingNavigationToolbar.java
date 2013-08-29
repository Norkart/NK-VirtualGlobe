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
import  javax.swing.*;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Properties;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.Xj3DConstants;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * A toolbar for all navigation commands that is usable across any UI that
 * uses Swing.
 * <p>
 *
 * Creating an instance of this class will automatically register it as a
 * navigation state listener with the browser core. The end user
 * is not required to do this.
 * <p>
 *
 * <b>External Resources</b>
 * <p>
 * This toolbar uses images for the button icons rather than text. These are
 * the images used. The path is found relative to the classpath.
 *
 * <ul>
 * <li>Track:  images/navigation/ButtonTExamine.gif</li>
 * <li>Examine:  images/navigation/ButtonExamine.gif</li>
 * <li>Fly: images/navigation/ButtonFly.gif</li>
 * <li>Pan: images/navigation/ButtonPan.gif</li>
 * <li>Tilt: images/navigation/ButtonTilt.gif</li>
 * <li>Walk: images/navigation/ButtonWalk.gif</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class SwingNavigationToolbar extends JPanel
    implements NavigationStateListener {

    /** Empty skin properties definition for default */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** Property name for examine button image */
    private static final String EXAMINE_BUTTON_PROPERTY = "EXAMINE.button";

    /** Property name for fly button image */
    private static final String FLY_BUTTON_PROPERTY = "FLY.button";

    /** Property name for pan button image */
    private static final String PAN_BUTTON_PROPERTY = "PAN.button";

    /** Property name for tilt button image */
    private static final String TILT_BUTTON_PROPERTY = "TILT.button";

    /** Property name for walk button image */
    private static final String WALK_BUTTON_PROPERTY = "WALK.button";

    /** Property name for track button image */
    private static final String TRACK_BUTTON_PROPERTY = "TRACK.button";

    /** Default examine button image */
    private static final String DEFAULT_EXAMINE_BUTTON =
        "images/navigation/ButtonExamine.gif";

    /** Default fly button image */
    private static final String DEFAULT_FLY_BUTTON =
        "images/navigation/ButtonFly.gif";

    /** Default pan button image */
    private static final String DEFAULT_PAN_BUTTON =
        "images/navigation/ButtonPan.gif";

    /** Default tilt button image */
    private static final String DEFAULT_TILT_BUTTON =
        "images/navigation/ButtonTilt.gif";

    /** Default walk button image */
    private static final String DEFAULT_WALK_BUTTON =
        "images/navigation/ButtonWalk.gif";

    /** Default track button image */
    private static final String DEFAULT_TRACK_BUTTON =
        "images/navigation/ButtonTExamine.gif";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The last known navigation state list for updating nav mode */
    private String[] navigationModes;

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

    /** Button representing the track navigation state */
    private JToggleButton trackButton;

    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;

    /** The actions */
    private NavModeAction flyAction;
    private NavModeAction flyMenuAction;
    private NavModeAction walkAction;
    private NavModeAction walkMenuAction;
    private NavModeAction examineAction;
    private NavModeAction examineMenuAction;
    private NavModeAction panAction;
    private NavModeAction panMenuAction;
    private NavModeAction tiltAction;
    private NavModeAction tiltMenuAction;
    private NavModeAction trackAction;
    private NavModeAction trackMenuAction;


    /**
     * Create a new horizontal navigation toolbar with an empty list of
     * viewpoints and disabled user selection of state.
     *
     * @param core The browser core implementation to send nav changes to
     * @param reporter The reporter instance to use or null
     */
    public SwingNavigationToolbar(BrowserCore core, ErrorReporter reporter) {
        this(core, true, reporter);
    }

    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param core The browser core implementation to send nav changes to
     * @param horizontal True to lay out the buttons horizontally
     * @param reporter The reporter instance to use or null
     */
    public SwingNavigationToolbar(BrowserCore core,
                                  boolean horizontal,
                                  ErrorReporter reporter) {
        this(core, horizontal, null, reporter);
    }

    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param core The browser core implementation to send nav changes to
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public SwingNavigationToolbar(BrowserCore core,
                                  Properties skinProperties,
                                  ErrorReporter reporter) {
        this(core, true, skinProperties, reporter);
    }

    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param core The browser core implementation to send nav changes to
     * @param horizontal True to lay out the buttons horizontally
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public SwingNavigationToolbar(BrowserCore core,
                                  boolean horizontal,
                                  Properties skinProperties,
                                  ErrorReporter reporter) {
        browserCore = core;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;

        browserCore.addNavigationStateListener(this);

        if(horizontal)
            setLayout(new GridLayout(1, 5));
        else
            setLayout(new GridLayout(5, 1));

        navStateGroup = new ButtonGroup();

        String img_name = skinProperties.getProperty(FLY_BUTTON_PROPERTY,
                                                     DEFAULT_FLY_BUTTON);
        ImageIcon icon = IconLoader.loadIcon(img_name, reporter);

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                   KeyEvent.CTRL_MASK);

        flyAction = new NavModeAction(true, icon, "FLY", acc_key, browserCore);
        flyMenuAction = new NavModeAction(false, icon, "FLY", acc_key, browserCore);
        flyAction.setEnabled(false);
        flyMenuAction.setEnabled(false);

        flyButton = new JToggleButton(flyAction);

        flyButton.setMargin(new Insets(0,0,0,0));
        flyButton.setToolTipText("Fly");
        navStateGroup.add(flyButton);
        add(flyButton);

        img_name = skinProperties.getProperty(PAN_BUTTON_PROPERTY,
                                              DEFAULT_PAN_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);
        panAction = new NavModeAction(true, icon, "xj3d_PAN", null, browserCore);
        panMenuAction = new NavModeAction(false, icon, "xj3d_PAN", null, browserCore);
        panAction.setEnabled(false);
        panMenuAction.setEnabled(false);

        panButton = new JToggleButton(panAction);

        panButton.setMargin(new Insets(0,0,0,0));
        panButton.setToolTipText("Pan");
        panButton.setEnabled(false);
        navStateGroup.add(panButton);
        add(panButton);

        img_name = skinProperties.getProperty(TILT_BUTTON_PROPERTY,
                                              DEFAULT_TILT_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);
        tiltAction = new NavModeAction(true, icon, "xj3d_TILT", null, browserCore);
        tiltMenuAction = new NavModeAction(false, icon, "xj3d_TILT", null, browserCore);
        tiltAction.setEnabled(false);
        tiltMenuAction.setEnabled(false);

        tiltButton = new JToggleButton(tiltAction);

        tiltButton.setMargin(new Insets(0,0,0,0));
        tiltButton.setToolTipText("Tilt");
        tiltButton.setEnabled(false);
        navStateGroup.add(tiltButton);
        add(tiltButton);

        img_name = skinProperties.getProperty(WALK_BUTTON_PROPERTY,
                                              DEFAULT_WALK_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);

        acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                   KeyEvent.CTRL_MASK);

        walkAction = new NavModeAction(true, icon, "WALK", acc_key, browserCore);
        walkMenuAction = new NavModeAction(false, icon, "WALK", acc_key, browserCore);
        walkAction.setEnabled(false);
        walkMenuAction.setEnabled(false);

        walkButton = new JToggleButton(walkAction);

        walkButton.setMargin(new Insets(0,0,0,0));
        walkButton.setToolTipText("Walk");
        navStateGroup.add(walkButton);
        add(walkButton);

        img_name = skinProperties.getProperty(TRACK_BUTTON_PROPERTY,
                                              DEFAULT_TRACK_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);

        trackAction = new NavModeAction(true, icon, "xj3d_TRACK_EXAMINE", null, browserCore);
        trackMenuAction = new NavModeAction(false, icon, "xj3d_TRACK_EXAMINE", null, browserCore);
        trackAction.setEnabled(false);
        trackMenuAction.setEnabled(false);

        trackButton = new JToggleButton(trackAction);

        trackButton.setMargin(new Insets(0,0,0,0));
        trackButton.setToolTipText("Track");
        trackButton.setEnabled(false);
        navStateGroup.add(trackButton);
        add(trackButton);
		
        img_name = skinProperties.getProperty(EXAMINE_BUTTON_PROPERTY,
                                              DEFAULT_EXAMINE_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);
        acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                   KeyEvent.CTRL_MASK);

        examineAction = new NavModeAction(true, icon, "EXAMINE", acc_key, browserCore);
        examineMenuAction = new NavModeAction(false, icon, "EXAMINE", acc_key, browserCore);
        examineAction.setEnabled(false);
        examineMenuAction.setEnabled(false);

        examineButton = new JToggleButton(examineAction);

        examineButton.setMargin(new Insets(0,0,0,0));
        examineButton.setToolTipText("Examine");
        examineButton.setEnabled(false);
        navStateGroup.add(examineButton);
        add(examineButton);
    }

    //----------------------------------------------------------
    // Methods defined by NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx) {
        // If only navigationStateChanged broadcast the
        // new nav state in numeric form...
        if(navigationModes != null) {
           String newMode = navigationModes[idx];

           if (newMode.equalsIgnoreCase(Xj3DConstants.WALK_NAV_MODE))
               walkButton.setSelected(true);
           else if (newMode.equalsIgnoreCase(Xj3DConstants.FLY_NAV_MODE))
               flyButton.setSelected(true);
           else if (newMode.equalsIgnoreCase(Xj3DConstants.TILT_NAV_MODE))
               tiltButton.setSelected(true);
           else if (newMode.equalsIgnoreCase(Xj3DConstants.PAN_NAV_MODE))
               panButton.setSelected(true);
           else if (newMode.equalsIgnoreCase(Xj3DConstants.EXAMINE_NAV_MODE))
               examineButton.setSelected(true);
           else if (newMode.equalsIgnoreCase(Xj3DConstants.TRACK_EXAMINE_NAV_MODE))
               trackButton.setSelected(true);
       }
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numModes The number of modes in the array
     */
    public void navigationListChanged(String[] modes, int numModes) {
        if(navigationModes == null || navigationModes.length != numModes)
            navigationModes = new String[numModes];

        System.arraycopy(modes, 0, navigationModes, 0, numModes);
        boolean found_any = false;

        walkAction.setEnabled(false);
        walkMenuAction.setEnabled(false);
        flyAction.setEnabled(false);
        flyMenuAction.setEnabled(false);
        examineAction.setEnabled(false);
        examineMenuAction.setEnabled(false);
        panAction.setEnabled(false);
        panMenuAction.setEnabled(false);
        tiltAction.setEnabled(false);
        tiltMenuAction.setEnabled(false);
        trackAction.setEnabled(false);
        trackMenuAction.setEnabled(false);

        String mode;

        for(int i=0; i < numModes; i++) {
            if (modes[i].equals(Xj3DConstants.ANY_NAV_MODE)) {
                found_any = true;
                break;
            }
            mode = modes[i];

            if (mode.equals(Xj3DConstants.WALK_NAV_MODE)) {
                walkAction.setEnabled(true);
                walkMenuAction.setEnabled(true);
            } else if (mode.equals(Xj3DConstants.FLY_NAV_MODE)) {
                flyAction.setEnabled(true);
                flyMenuAction.setEnabled(true);
            } else if (mode.equals(Xj3DConstants.EXAMINE_NAV_MODE)) {
                examineAction.setEnabled(true);
                examineMenuAction.setEnabled(true);
            } else if (mode.equals(Xj3DConstants.PAN_NAV_MODE)) {
                panAction.setEnabled(true);
                panMenuAction.setEnabled(true);
            } else if (mode.equals(Xj3DConstants.TILT_NAV_MODE)) {
                tiltAction.setEnabled(true);
                tiltMenuAction.setEnabled(true);
			} else if (mode.equals(Xj3DConstants.TRACK_EXAMINE_NAV_MODE)) {
                trackAction.setEnabled(true);
                trackMenuAction.setEnabled(true);
            }
        }

        if (found_any) {
            walkAction.setEnabled(true);
            walkMenuAction.setEnabled(true);
            flyAction.setEnabled(true);
            flyMenuAction.setEnabled(true);
            examineAction.setEnabled(true);
            examineMenuAction.setEnabled(true);
            panAction.setEnabled(true);
            panMenuAction.setEnabled(true);
            tiltAction.setEnabled(true);
            tiltMenuAction.setEnabled(true);
            trackAction.setEnabled(true);
            trackMenuAction.setEnabled(true);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the Fly Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getFlyAction() {
        return flyMenuAction;
    }

    /**
     * Get the Walk Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getWalkAction() {
        return walkMenuAction;
    }

    /**
     * Get the Examine Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getExamineAction() {
        return examineMenuAction;
    }

    /**
     * Get the Pan Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getPanAction() {
        return panMenuAction;
    }

    /**
     * Get the Tilt Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getTiltAction() {
        return tiltMenuAction;
    }
    /**
     * Get the Track Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getTrackAction() {
        return trackMenuAction;
    }
}
