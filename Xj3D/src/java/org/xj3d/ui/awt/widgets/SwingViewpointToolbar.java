/*****************************************************************************
 *                        Web3D.org Copyright (c) 2000 - 2007
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Properties;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.ViewpointStatusListener;
import org.web3d.browser.Xj3DConstants;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.xj3d.core.eventmodel.ViewpointManager;

/**
 * A toolbar for all viewpoint manipulation commands that offers convenient and
 * common code.
 * <p>
 *
 * Creating an instance of this class will automatically register it as
 * viewpoint and navigation state listeners with the browser core. The end user
 * is not required to do this.
 * <p>
 *
 * <b>External Resources</b>
 * <p>
 * This toolbar uses images for the button icons rather than text. These are
 * the images used. The path is found relative to the classpath.
 *
 * <ul>
 * <li>Forward:  images/navigation/ButtonForward.gif</li>
 * <li>Back: images/navigation/ButtonBack.gif</li>
 * <li>Home: images/navigation/ButtonHome.gif</li>
 * <li>Fit: images/navigation/ButtonFit.gif</li>
 * <li>Look At: images/navigation/ButtonLookat.gif</li>
 * </ul>
 *
 * The toolbar always starts completely disabled. User code should not play
 * with the enabled state as we will do that based on the feedback from the
 * various status listeners...
 *
 * The actions returned are suitable for menu usage.  Internally we have another
 * set of actions that have a different style.
 *
 * @author Justin Couch, Brad Vender
 * @version $Revision: 1.10 $
 */
public class SwingViewpointToolbar extends JPanel
    implements ItemListener,
               ViewpointStatusListener,
               NavigationStateListener {

    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** Default for the file for the walk cursor image */
    private static final String DEFAULT_HOME_BUTTON =
        "images/navigation/ButtonHome.gif";

    /** Default for the file for the fit world image */
    private static final String DEFAULT_FITWORLD_BUTTON =
        "images/navigation/ButtonFit.gif";

    /** Default for the file for the lookat image */
    private static final String DEFAULT_LOOKAT_BUTTON =
        "images/navigation/ButtonLookat.gif";

    /** Default for the next viewpoint button image */
    private static final String DEFAULT_NEXT_BUTTON =
        "images/navigation/ButtonForward.gif";

    /** Default for the file for the previous viewpoint button image */
    private static final String DEFAULT_PREVIOUS_BUTTON =
        "images/navigation/ButtonBack.gif";

    /** Property for overriding home button image */
    private static final String HOME_BUTTON_PROPERTY = "HOME.button";

    /** Property for overriding fitworld button image */
    private static final String FITWORLD_BUTTON_PROPERTY = "FITWORLD.button";

    /** Property for overriding lookat button image */
    private static final String LOOKAT_BUTTON_PROPERTY = "LOOKAT.button";

    /** Property for overriding next viewpoint button image */
    private static final String NEXT_BUTTON_PROPERTY = "NEXTVIEW.button";

    /** Property for overriding previous viewpoint button image */
    private static final String PREVIOUS_BUTTON_PROPERTY = "PREVIOUSVIEW.button";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The manager of viewpoints that we use to change them on the fly */
    private ViewpointManager vpManager;

    /** Combo box holding the list of viewpoint data */
    private JComboBox viewpoints;

    /** The model used by the combo box to handle viewpoint data */
    private DefaultComboBoxModel viewpointModel;

    /** Button representing a move to the next viewpoint */
    private JButton nextViewpoint;

    /** Button representing a move to the next viewpoint */
    private JButton prevViewpoint;

    /** Button representing a move to straighten up a viewpoint (lost user) */
    private JButton homeViewpoint;

    /** Button representing a move to show the whole world */
    private JButton fitworldViewpoint;

    /** Button representing a seek operation */
    private JButton lookatViewpoint;

    /**
     * Viewpoint nodes indexed by the layer they are in. The array contains
     * another ArrayList in each index, or null if that layer is no longer
     * valid. Each of these nested arrays contains a list of the Viewpoint
     * nodes in that layer.
     */
    private ArrayList viewpointsByLayer;

    /** Was there an initial viewpoint in the scene */
    private boolean noInitialVPS;

    /**
     * The default viewpoint for each layer ID. Contains a list of
     * VRMLViewpointNodeType instances.
     */
    private ArrayList defaultViewpoints;

    /**
     * The bound viewpoint for each layer ID. Contains a list of
     * VRMLViewpointNodeType instances.
     */
    private ArrayList boundViewpoints;

    /** The currently active layer ID */
    private int activeLayerId;

    /** A BrowserCore instance to handle fitToWorld */
    private BrowserCore browserCore;

    /** The actions to return */
    private NextViewpointAction nextViewpointMenuAction;
    private PreviousViewpointAction prevViewpointMenuAction;
    private HomeViewpointAction homeViewpointMenuAction;
    private LookatAction lookatMenuAction;
    private FitWorldAction fitWorldMenuAction;

    /** The local actions */
    private NextViewpointAction nextViewpointAction;
    private PreviousViewpointAction prevViewpointAction;
    private HomeViewpointAction homeViewpointAction;
    private LookatAction lookatAction;
    private FitWorldAction fitWorldAction;

    /**
     * Create a new horizontal viewpoint toolbar with an empty list of
     * viewpoints.
     *
     * @param core The browser core
     * @param vpMgr The manager of viewpoint changes
     * @param reporter The reporter instance to use or null
     */
    public SwingViewpointToolbar(BrowserCore core,
                                 ViewpointManager vpMgr,
                                 ErrorReporter reporter) {
        this(core, vpMgr, DEFAULT_PROPERTIES, reporter);
    }

    /**
     * Create a new horizontal viewpoint toolbar with an empty list of
     * viewpoints, but with non-default appearance.
     *
     * @param core The browser core
     * @param vpMgr The manager of viewpoint changes
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public SwingViewpointToolbar(BrowserCore core,
                                 ViewpointManager vpMgr,
                                 Properties skinProperties,
                                 ErrorReporter reporter) {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        vpManager = vpMgr;
        browserCore = core;
        core.addNavigationStateListener(this);
        core.addViewpointStatusListener(this);

        viewpointsByLayer = new ArrayList();
        defaultViewpoints = new ArrayList();
        boundViewpoints = new ArrayList();
        activeLayerId = -1;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;

        viewpointModel = new DefaultComboBoxModel();

        viewpoints = new JComboBox(viewpointModel);
        viewpoints.setRenderer(new ViewpointCellRenderer());
        viewpoints.setEditable(false);
        viewpoints.setLightWeightPopupEnabled(false);
        //viewpoints.setMaximumRowCount(10);
        viewpoints.setMinimumSize(new Dimension(60,10)); // yuck!
        viewpoints.setToolTipText("Select a Viewpoint");
        viewpoints.addItemListener(this);

        String img_name = skinProperties.getProperty(NEXT_BUTTON_PROPERTY,
                                                     DEFAULT_NEXT_BUTTON);

        ImageIcon icon = IconLoader.loadIcon(img_name, reporter);

        nextViewpointAction = new NextViewpointAction(true, icon, vpManager);
        nextViewpointMenuAction = new NextViewpointAction(false, icon, vpManager);

//        int VERT_POS = SwingConstants.BOTTOM;
//        int HORIZ_POS = SwingConstants.CENTER;

        int VERT_POS = SwingConstants.CENTER;
        int HORIZ_POS = SwingConstants.TRAILING;

        nextViewpoint = new JButton(nextViewpointAction);

        nextViewpoint.setMargin(new Insets(0,0,0,0));
        nextViewpoint.setToolTipText("Next Viewpoint");
        nextViewpoint.setVerticalTextPosition(VERT_POS);
        nextViewpoint.setHorizontalTextPosition(HORIZ_POS);

        img_name = skinProperties.getProperty(PREVIOUS_BUTTON_PROPERTY,
                                              DEFAULT_PREVIOUS_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);
        prevViewpointAction = new PreviousViewpointAction(true, icon, vpManager);
        prevViewpointMenuAction = new PreviousViewpointAction(false, icon, vpManager);

        prevViewpoint = new JButton(prevViewpointAction);
        prevViewpoint.setMargin(new Insets(0,0,0,0));
        prevViewpoint.setToolTipText("Previous Viewpoint");
        prevViewpoint.setVerticalTextPosition(VERT_POS);
        prevViewpoint.setHorizontalTextPosition(HORIZ_POS);

        img_name = skinProperties.getProperty(HOME_BUTTON_PROPERTY,
                                              DEFAULT_HOME_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);

        homeViewpointAction = new HomeViewpointAction(true, icon, vpManager);
        homeViewpointMenuAction = new HomeViewpointAction(false, icon, vpManager);

        homeViewpoint = new JButton(homeViewpointAction);
        homeViewpoint.setMargin(new Insets(0,0,0,0));
        homeViewpoint.setToolTipText("Return to current Viewpoint");

        img_name = skinProperties.getProperty(LOOKAT_BUTTON_PROPERTY,
                                              DEFAULT_LOOKAT_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);

        lookatAction = new LookatAction(true, icon, browserCore);
        lookatMenuAction = new LookatAction(false, icon, browserCore);

        lookatViewpoint = new JButton(lookatAction);
        lookatViewpoint.setMargin(new Insets(0,0,0,0));
        lookatViewpoint.setToolTipText("LookAt");

        img_name = skinProperties.getProperty(FITWORLD_BUTTON_PROPERTY,
                                              DEFAULT_FITWORLD_BUTTON);
        icon = IconLoader.loadIcon(img_name, reporter);
        fitWorldAction = new FitWorldAction(true, icon, browserCore);
        fitWorldMenuAction = new FitWorldAction(false, icon, browserCore);

        fitworldViewpoint = new JButton(fitWorldAction);

        fitworldViewpoint.setMargin(new Insets(0,0,0,0));
        fitworldViewpoint.setToolTipText("Fit to World");

        JPanel p1 = new JPanel(new GridLayout(1, 3));
        p1.add(nextViewpoint);
        p1.add(homeViewpoint);
        p1.add(lookatViewpoint);
        p1.add(fitworldViewpoint);

        setLayout(new BorderLayout());

        add(prevViewpoint, BorderLayout.WEST);
        add(viewpoints, BorderLayout.CENTER);
        add(p1, BorderLayout.EAST);

        // We start ourselves disabled and only enable based on content.
        setEnabled(false);
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
        // ignore
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numModes The number of modes in the list
     */
    public void navigationListChanged(String[] modes, int numModes) {

        boolean found_any = false;

        String mode;
        lookatAction.setEnabled(false);

        for(int i = 0; i < numModes; i++) {
            if(modes[i].equals("ANY")) {
                found_any = true;
                break;
            }
            mode = modes[i];

            if(mode.equals("LOOKAT"))
                lookatAction.setEnabled(true);
        }

        if(found_any)
            lookatAction.setEnabled(true);
    }

    //----------------------------------------------------------
    // Methods defined by ItemListener
    //----------------------------------------------------------

    /**
     * Listen for item changing events in the comboBox selection
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() != ItemEvent.SELECTED)
            return;

        VRMLViewpointNodeType node =
            (VRMLViewpointNodeType)evt.getItem();

        // Don't attempt to rebind something that has already been bound.
        if(!node.getIsBound())
            vpManager.setViewpoint(node);
    }

    //----------------------------------------------------------
    // Methods defined by Component
    //----------------------------------------------------------

    /**
     * Set the panel enabled or disabled. Overridden to make sure the base
     * components are properly handled.
     *
     * @param enabled true if this component is enabled
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        viewpoints.setEnabled(enabled);
        nextViewpointAction.setEnabled(enabled);
        nextViewpointMenuAction.setEnabled(enabled);
        prevViewpointAction.setEnabled(enabled);
        prevViewpointMenuAction.setEnabled(enabled);
        homeViewpointAction.setEnabled(enabled);
        homeViewpointMenuAction.setEnabled(enabled);
        fitWorldAction.setEnabled(enabled);
        fitWorldMenuAction.setEnabled(enabled);
        lookatAction.setEnabled(enabled);
        lookatMenuAction.setEnabled(enabled);
    }

    //----------------------------------------------------------------
    // Methods defined by ViewpointStatusListener
    //----------------------------------------------------------------

    /**
     * Notification of the addition of a valid layer ID to the current
     * list. This layer is currently empty of viewpoints. Calls to
     * {@link #viewpointAdded} will subsequently follow with all the viewpoints
     * listed in this layer. It can be assumed that the layer is not currently
     * the active layer. A separate notificaion is available for that.
     *
     * @param layerId The ID of the layer to be added
     */
    public void viewpointLayerAdded(int layerId) {
        if(layerId >= viewpointsByLayer.size()) {
            for(int i = viewpointsByLayer.size() - 1; i <= layerId; i++) {
                viewpointsByLayer.add(null);
                defaultViewpoints.add(null);
                boundViewpoints.add(null);
            }
        }

        viewpointsByLayer.set(layerId, new ArrayList());
    }

    /**
     * Notification that a Layer ID is no longer valid. Any viewpoints that
     * have been made available for that layer should now be removed from the
     * layer.
     *
     * @param layerId The ID of the layer to be added
     */
    public void viewpointLayerRemoved(int layerId) {
        viewpointsByLayer.set(layerId, null);
        defaultViewpoints.set(layerId, null);
        boundViewpoints.set(layerId, null);
    }

    /**
     * The given layer is now made the active layer. If there is a viewpoint
     * list being maintained per-layer then the UI can perform some sort of
     * highlighting to indicate this. Viewpoints in other layers are still
     * allowed to be bound by the user interface. If there was a previously
     * active layer, ignore it.
     * <p>
     * The code will guarantee that if the active layer is removed, then this
     * method will be called first to set a different valid layer, before
     * removing that layer ID.
     * <p>
     *
     * If a value of -1 is provided, that means no layers are active and that
     * we currently have a completely clear browser with no world loaded. The
     * UI should act appropriately.
     *
     * @param layerId The ID of the layer to be made current or -1
     */
    public void viewpointLayerActive(int layerId) {
        activeLayerId = layerId;

        if(layerId == -1) {
            clearViewpoints();
        } else {
            ArrayList l = (ArrayList)viewpointsByLayer.get(layerId);
            if((l == null) || l.size() == 0) {
                viewpointModel.removeAllElements();
                partialDisable();
            } else {
                viewpointModel.removeAllElements();

                // Don't put the default viewpoint into the global model.
                Object def_vp = defaultViewpoints.get(layerId);

                for(int i = 0; i < l.size(); i++) {
                    Object vp = l.get(i);

                    if(vp != def_vp)
                        viewpointModel.addElement(vp);
                }

                viewpointModel.setSelectedItem(boundViewpoints.get(layerId));

                // If we only have the default viewpoint, disable the dropdown
                if(l.size() == 1) {
                    partialDisable();
                }
            }
        }
    }

    /**
     * Invoked when a viewpoint has been added
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is added to
     * @param isDefault Is the node a default
     */
    public void viewpointAdded(VRMLViewpointNodeType node,
                               int layerId,
                               boolean isDefault) {
                                
//System.out.println( "viewpointAdded: "+ node.getDescription( ) +" : "+ isDefault );
        if(isDefault) {
            defaultViewpoints.set(layerId, node);

            if(boundViewpoints.get(layerId) == null)
                boundViewpoints.set(layerId, node);
        }

        ArrayList l = (ArrayList)viewpointsByLayer.get(layerId);
		boolean duplicate = l.contains(node);
		if (!duplicate) {
        	l.add(node);
		}

        if(layerId == activeLayerId) {
            // If we have more than one viewpoint defined, remove the default
            // because we shouldn't be displaying it on the drop-down.
            if(isDefault) {
                if(l.size() == 0) {
//System.out.println( "adding default: "+ node );
					if (!duplicate) {
						// being defensive, the default should never be duplicated
                    	viewpointModel.addElement(node);
					}
                    partialDisable();
                }
            } else {
                // check to see if the default is in the list and if so, remove
                // it
                Object def_vp = defaultViewpoints.get(layerId);
                if(def_vp != null) {
//System.out.println( "removing default: "+ def_vp );
                    viewpointModel.removeElement(def_vp);
                }
				if (!duplicate) {
                	viewpointModel.addElement(node);
				}
                setEnabled(true);
            }
        }
    }

    /**
     * Invoked when a viewpoint has been removed
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is removed from
     */
    public void viewpointRemoved(VRMLViewpointNodeType node, int layerId) {
        ArrayList l = (ArrayList)viewpointsByLayer.get(layerId);
        l.remove(node);

        if(defaultViewpoints.get(layerId) == node)
            defaultViewpoints.set(layerId, null);

        if(boundViewpoints.get(layerId) == node)
            boundViewpoints.set(layerId, defaultViewpoints.get(layerId));

        if(layerId == activeLayerId)
            viewpointModel.removeElement(node);
    }

    /**
     * Invoked when a viewpoint has been bound.
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is bound on
     */
    public void viewpointBound(final VRMLViewpointNodeType node, int layerId) {
        boundViewpoints.set(layerId, node);

        if(layerId == activeLayerId) {
            //viewpointModel.setSelectedItem(node);
            EventQueue.invokeLater( new Runnable( ) {
                    public void run( ) {
                        viewpoints.setSelectedItem( node );
                    }
                } );
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the Next Viewpoint Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getNextViewpointAction() {
        return nextViewpointMenuAction;
    }

    /**
     * Get the Previous Viewpoint Action.
     *
     * @return Returns the action
     */
    public AbstractAction getPreviousViewpointAction() {
        return prevViewpointMenuAction;
    }

    /**
     * Get the Home Viewpoint Action.
     *
     * @return Returns the action
     */
    public AbstractAction getHomeViewpointAction() {
        return homeViewpointMenuAction;
    }

    /**
     * Get the Lookat Action.
     *
     * @return Returns the action
     */
    public AbstractAction getLookatAction() {
        return lookatMenuAction;
    }

    /**
     * Get the FitWorld Action.
     *
     * @return Returns the action
     */
    public AbstractAction getFitWorldAction() {
        return fitWorldMenuAction;
    }

    /**
     * Clear the viewpoint list and disable self
     */
    public void clearViewpoints() {
        viewpointModel.removeAllElements();

        setEnabled(false);
    }

    /**
     * Set the just the viewpoint dropdown list and prev/next buttons to a
     * disabled state. This is used when we only have the default viewpoint,
     * but still want to allow the home, fit and lookat capabilities.
     *
     * @param enabled true if this component is enabled
     */
    private void partialDisable()
    {
        viewpoints.setEnabled(false);
        nextViewpointAction.setEnabled(false);
        nextViewpointMenuAction.setEnabled(false);
        prevViewpointAction.setEnabled(false);
        prevViewpointMenuAction.setEnabled(false);
        homeViewpointAction.setEnabled(true);
        homeViewpointMenuAction.setEnabled(true);
        fitWorldAction.setEnabled(true);
        fitWorldMenuAction.setEnabled(true);
        lookatAction.setEnabled(true);
        lookatMenuAction.setEnabled(true);
    }
}
