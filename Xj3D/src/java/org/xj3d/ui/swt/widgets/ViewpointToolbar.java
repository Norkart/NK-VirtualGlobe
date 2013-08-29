/*****************************************************************************
 *                        Web3D.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.widgets;

// External imports
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

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
 * <li>Next:  images/navigation/ButtonForward.gif</li>
 * <li>Previous: images/navigation/ButtonBack.gif</li>
 * <li>Home: images/navigation/ButtonHome.gif</li>
 * <li>Fit: images/navigation/ButtonFit.gif</li>
 * <li>Look At: images/navigation/ButtonLookat.gif</li>
 * </ul>
 *
 * The toolbar always starts completely disabled. User code should not play
 * with the enabled state as we will do that based on the feedback from the
 * various status listeners...
 *
 * @author Justin Couch, Brad Vender, Rex Melton
 * @version $Revision: 1.8 $
 */
public class ViewpointToolbar extends Composite
    implements SelectionListener, ViewpointStatusListener, NavigationStateListener, Runnable {
    
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
    
    /** Home button text, if image is not found */
    private static final String HOME_BUTTON_TEXT = "Home";
    
    /** Fitworld button text, if image is not found */
    private static final String FITWORLD_BUTTON_TEXT = "Fit";
    
    /** Lookat button text, if image is not found */
    private static final String LOOKAT_BUTTON_TEXT = "Look At";
    
    /** Next viewpoint button text, if image is not found */
    private static final String NEXT_BUTTON_TEXT = "Next";
    
    /** Previous viewpoint button text, if image is not found */
    private static final String PREVIOUS_BUTTON_TEXT = "Previous";
    
    /** Home button tool tip text */
    private static final String HOME_BUTTON_TOOLTIP_TEXT =
        "Return to current Viewpoint";
    
    /** Fitworld button tool tip text */
    private static final String FITWORLD_BUTTON_TOOLTIP_TEXT =
        "Fit to World";
    
    /** Lookat button tool tip text */
    private static final String LOOKAT_BUTTON_TOOLTIP_TEXT =
        "Look At";
    
    /** Next viewpoint button tool tip text */
    private static final String NEXT_BUTTON_TOOLTIP_TEXT =
        "Next Viewpoint";
    
    /** Previous viewpoint button tool tip text */
    private static final String PREVIOUS_BUTTON_TOOLTIP_TEXT =
        "Previous Viewpoint";
    
    /** Viewpoint Combo tool tip text */
    private static final String VIEWPOINT_COMBO_TOOLTIP_TEXT =
        "Select a Viewpoint";
    
    /** Property index in buttonData */
    private static int PROPERTY_INDEX = 0;
    
    /** Image path index in buttonData */
    private static int IMAGE_INDEX = 1;
    
    /** Button text index in buttonData */
    private static int TEXT_INDEX = 2;
    
    /** Button text index in buttonData */
    private static int TOOLTIP_TEXT_INDEX = 3;
    
    /** Property identifier - Image path - button text -
    *  button tool tip text array */
    private static final String[][] buttonData = {
        { PREVIOUS_BUTTON_PROPERTY, DEFAULT_PREVIOUS_BUTTON,
            PREVIOUS_BUTTON_TEXT, PREVIOUS_BUTTON_TOOLTIP_TEXT },
        { NEXT_BUTTON_PROPERTY, DEFAULT_NEXT_BUTTON,
            NEXT_BUTTON_TEXT, NEXT_BUTTON_TOOLTIP_TEXT },
        { HOME_BUTTON_PROPERTY, DEFAULT_HOME_BUTTON,
            HOME_BUTTON_TEXT, HOME_BUTTON_TOOLTIP_TEXT },
        { LOOKAT_BUTTON_PROPERTY, DEFAULT_LOOKAT_BUTTON,
            LOOKAT_BUTTON_TEXT, LOOKAT_BUTTON_TOOLTIP_TEXT },
        { FITWORLD_BUTTON_PROPERTY, DEFAULT_FITWORLD_BUTTON,
            FITWORLD_BUTTON_TEXT, FITWORLD_BUTTON_TOOLTIP_TEXT },
    };
    
    /** Previous viewpoint button index in button[] */
    private static final int PREVIOUS_BUTTON_INDEX = 0;
    
    /** Next viewpoint button index in button[] */
    private static final int NEXT_BUTTON_INDEX = 1;
    
    /** Home button index in button[] */
    private static final int HOME_BUTTON_INDEX = 2;
    
    /** Lookat button index in button[] */
    private static final int LOOKAT_BUTTON_INDEX = 3;
    
    /** Fitworld button index in button[] */
    private static final int FITWORLD_BUTTON_INDEX = 4;
    
    /** The array of viewpoint buttons */
    private Button[] button;
    
    /** Display instance, used to get images and put updates on the display thread */
    private Display display;
    
    /** Flag indicating that the button enabled states should all be set
    *  on the display thread */
    private boolean setEnabledFlag;
    
    /** State that the button enables states should be set to on the display thread */
    private boolean enabled;
    
    /** Flag indicating that the look at button enabled state should be set
    *  on the display thread */
    private boolean setLookAtEnabledFlag;
    
    /** State that the look at button should be set to on the display thread */
    private boolean lookAtEnabled;
    
    /** Flag indicating that a 'special case' set of enabled states should be
    *  set for the widgets on the display thread */
    private boolean setPartialDisableFlag;
    
    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;
    
    /** The manager of viewpoints that we use to change them on the fly */
    private ViewpointManager vpManager;
    
    /** Combo box holding the list of viewpoint data */
    private Combo viewpointCombo;
    
    /** Class responsible for manipulating the viewpoint combo
    *  in the display thread */
    private ViewpointComboManager comboManager;
    
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
    
    /**
     * Create a new horizontal viewpoint toolbar with an empty list of
     * viewpoints.
     *
     * @param parent the SWT Composite widget that this will be added to
     * @param core The browser core
     * @param vpMgr The manager of viewpoint changes
     * @param reporter The reporter instance to use or null
     */
    public ViewpointToolbar(
        Composite parent,
        BrowserCore core,
        ViewpointManager vpMgr,
        ErrorReporter reporter) {
        this( parent, core, vpMgr, DEFAULT_PROPERTIES, reporter );
    }
    
    /**
     * Create a new horizontal viewpoint toolbar with an empty list of
     * viewpoints, but with non-default appearance.
     *
     * @param parent - The SWT Composite widget that this will be added to
     * @param core - The browser core
     * @param vpMgr - The manager of viewpoint changes
     * @param skinProperties - The properties object specifying image names
     * @param reporter - The reporter instance to use or null
     */
    public ViewpointToolbar(
        Composite parent,
        BrowserCore core,
        ViewpointManager vpMgr,
        Properties skinProperties,
        ErrorReporter reporter ) {
        
        super( parent, SWT.NONE );
        
        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;
        
        vpManager = vpMgr;
        browserCore = core;
        core.addNavigationStateListener( this );
        core.addViewpointStatusListener( this );
        
        viewpointsByLayer = new ArrayList( );
        defaultViewpoints = new ArrayList( );
        boundViewpoints = new ArrayList( );
        activeLayerId = -1;
        
        Properties skin = ( skinProperties == null ) ?
            DEFAULT_PROPERTIES : skinProperties;
        
        int numButtons = buttonData.length;
        
        GridLayout layout = new GridLayout( );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.numColumns = numButtons + 1;
        layout.makeColumnsEqualWidth = false;
        super.setLayout( layout );
        
        button = new Button[numButtons];
        display = getDisplay( );
        
        // install the 'previous' button
        String img_name = skin.getProperty(
            buttonData[PREVIOUS_BUTTON_INDEX][PROPERTY_INDEX],
            buttonData[PREVIOUS_BUTTON_INDEX][IMAGE_INDEX] );
        Image icon = ImageLoader.loadImage( display, img_name, reporter );
        Button b = new Button( this, SWT.PUSH );
        if( icon == null ) {
            b.setText( buttonData[PREVIOUS_BUTTON_INDEX][TEXT_INDEX] );
        } else {
            b.setImage( icon );
        }
        b.setToolTipText( buttonData[PREVIOUS_BUTTON_INDEX][TOOLTIP_TEXT_INDEX] );
        b.addSelectionListener( this );
        b.setEnabled( false );
        button[PREVIOUS_BUTTON_INDEX] = b;
        
        // install the viewpoint list combo box
        viewpointCombo = new Combo( this, SWT.DROP_DOWN | SWT.READ_ONLY );
        viewpointCombo.setVisibleItemCount( 10 );
        viewpointCombo.setToolTipText( VIEWPOINT_COMBO_TOOLTIP_TEXT );
        viewpointCombo.addSelectionListener( this );
        viewpointCombo.setEnabled( false );
        
        GridData gridData = new GridData( );
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        viewpointCombo.setLayoutData( gridData );
        
        comboManager = new ViewpointComboManager( display, viewpointCombo );
        
        // install the remainder of the buttons
        for ( int i = 1; i < numButtons; i++ ) {
            img_name = skin.getProperty(
                buttonData[i][PROPERTY_INDEX],
                buttonData[i][IMAGE_INDEX] );
            icon = ImageLoader.loadImage( display, img_name, reporter );
            b = new Button( this, SWT.PUSH );
            if( icon == null ) {
                b.setText( buttonData[i][TEXT_INDEX] );
            } else {
                b.setImage( icon );
            }
            b.setToolTipText( buttonData[i][TOOLTIP_TEXT_INDEX] );
            b.addSelectionListener( this );
            if ( i == NEXT_BUTTON_INDEX ) {
                b.setEnabled( false );
            }
            button[i] = b;
        }
    }
    
    //---------------------------------------------------------
    // Methods defined by Composite
    //---------------------------------------------------------
    
    /** Do nothing, we do our own layout */
    public void setLayout( Layout layout ) {
    }
    
    /**
     * Set the composite enabled or disabled. Overridden to make sure the
     * widgets are properly handled.
     *
     * @param enabled true if this composite is enabled
     */
    public synchronized void setEnabled( boolean enabled ) {
        this.enabled = enabled;
        setEnabledFlag = true;
        setPartialDisableFlag = false;
        display.asyncExec( this );
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
    public void navigationListChanged( String[] modes, int numModes ) {
        
        boolean found_any = false;
        
        String mode;
        lookAtEnabled = false;
        
        for ( int i = 0; i < numModes; i++ ) {
            if ( modes[i].equals( Xj3DConstants.ANY_NAV_MODE ) ) {
                lookAtEnabled = true;
                break;
            }
            mode = modes[i];
            
            if ( mode.equals( Xj3DConstants.LOOKAT_NAV_MODE ) ) {
                lookAtEnabled = true;
            }
        }
        setLookAtEnabledFlag = true;
        display.asyncExec( this );
    }
    
    //----------------------------------------------------------
    // Methods defined by SelectionListener
    //----------------------------------------------------------
    
    /**
     * Process the selection generated from the user interface.
     *
     * @param se The event that caused this method to be called
     */
    public void widgetSelected( SelectionEvent se ) {
        Object src = se.getSource( );
        
        // Don't select the VP here because the call flow will head into the
        // browser core, then to the bindable manager and from that, back to this
        // class that will then change the selected item.
        if ( src == button[NEXT_BUTTON_INDEX] ) {
            vpManager.nextViewpoint( );
        }
        else if ( src == button[PREVIOUS_BUTTON_INDEX] ) {
            vpManager.previousViewpoint( );
        }
        else if ( src == button[HOME_BUTTON_INDEX] ) {
            int index = viewpointCombo.getSelectionIndex( );
            VRMLViewpointNodeType node = comboManager.getNode( index );
            if( node != null ) {
                vpManager.setViewpoint( node );
            }
        }
        else if ( src == button[FITWORLD_BUTTON_INDEX] ) {
            if ( browserCore != null ) {
                browserCore.fitToWorld( false );
            }
        }
        else if( src == button[LOOKAT_BUTTON_INDEX] ) {
            if( browserCore != null ) {
                browserCore.setNavigationMode( Xj3DConstants.LOOKAT_NAV_MODE );
            }
        }
        else if ( src == viewpointCombo ) {
            int index = viewpointCombo.getSelectionIndex( );
            VRMLViewpointNodeType node = comboManager.getNode( index );
            
            // Don't attempt to rebind something that has already been bound.
            if( ( node != null ) && !node.getIsBound( ) ) {
                vpManager.setViewpoint( node );
            }
        }
    }
    
    /**
     * Process the default selection generated from the user interface.
     * Selection from the viewpoint combo box.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
        Object src = se.getSource( );
        if ( src == viewpointCombo ) {
            int index = viewpointCombo.getSelectionIndex( );
            VRMLViewpointNodeType node = comboManager.getNode( index );
            
            // Don't attempt to rebind something that has already been bound.
            if( ( node != null ) && !node.getIsBound( ) ) {
                vpManager.setViewpoint( node );
            }
        }
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
    public void viewpointLayerActive( int layerId ) {
        activeLayerId = layerId;
        
        if ( layerId == -1 ) {
            clearViewpoints( );
        } else {
            ArrayList l = (ArrayList)viewpointsByLayer.get( layerId );
            if( ( l == null ) || l.size( ) == 0 ) {
                comboManager.clear( );
                partialDisable( );
            } else {
                comboManager.clear( );
                
                // Don't put the default viewpoint into the global model.
                Object def_vp = defaultViewpoints.get( layerId );
                
                for( int i = 0; i < l.size( ); i++ ) {
                    Object vp = l.get( i );
                    
                    if (vp != def_vp ) {
                        comboManager.add( (VRMLViewpointNodeType)vp );
                    }
                }
                
                comboManager.select( (VRMLViewpointNodeType)boundViewpoints.get( layerId ) );
                
                // If we only have the default viewpoint, disable the dropdown
                if( l.size( ) == 1 ) {
                    partialDisable( );
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
    public void viewpointAdded(
        VRMLViewpointNodeType node,
        int layerId,
        boolean isDefault ) {
        if ( isDefault ) {
            defaultViewpoints.set( layerId, node );
            
            if ( boundViewpoints.get( layerId ) == null ) {
                boundViewpoints.set( layerId, node );
            }
        }
        
        ArrayList l = (ArrayList)viewpointsByLayer.get( layerId );
		boolean duplicate = l.contains(node);
		if ( !duplicate ) {
        	l.add( node );
		}
        
        if( layerId == activeLayerId ) {
            // If we have more than one viewpoint defined, remove the default
            // because we shouldn't be displaying it on the drop-down.
            if( isDefault ) {
                if( l.size( ) == 0 ) {
					if ( !duplicate ) {
						// being defensive, the default should never be duplicated
                    	comboManager.add( node );
					}
                    partialDisable( );
                }
            } else {
                // check to see if the default is in the list and if so, remove
                // it
                Object def_vp = defaultViewpoints.get( layerId );
                if( def_vp != null ) {
                    comboManager.remove( (VRMLViewpointNodeType)def_vp );
                }
                if ( !duplicate ) {
                	comboManager.add( node );
				}
                setEnabled( true );
            }
        }
    }
    
    /**
     * Invoked when a viewpoint has been removed
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is removed from
     */
    public void viewpointRemoved( VRMLViewpointNodeType node, int layerId ) {
        ArrayList l = (ArrayList)viewpointsByLayer.get( layerId );
        l.remove( node );
        
        if ( defaultViewpoints.get( layerId ) == node ) {
            defaultViewpoints.set( layerId, null );
        }
        if ( boundViewpoints.get( layerId ) == node ) {
            boundViewpoints.set( layerId, defaultViewpoints.get( layerId ) );
        }
        if ( layerId == activeLayerId ) {
            comboManager.remove( node );
        }
    }
    
    /**
     * Invoked when a viewpoint has been bound.
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is bound on
     */
    public void viewpointBound( VRMLViewpointNodeType node, int layerId ) {
        boundViewpoints.set( layerId, node );
        
        if ( layerId == activeLayerId ) {
            comboManager.select( node );
        }
    }
    
    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------
    
    /**
     * Process the enabling and disabling of the widgets on the
     * display thread.
     */
    public synchronized void run( ) {
        if ( setEnabledFlag ) {
            super.setEnabled( enabled );
            viewpointCombo.setEnabled( enabled );
            for ( int i = 0; i < button.length; i++ ) {
                button[i].setEnabled( enabled );
            }
            setEnabledFlag = false;
        }
        if ( setPartialDisableFlag ) {
            viewpointCombo.setEnabled( false );
            button[PREVIOUS_BUTTON_INDEX].setEnabled( false );
            button[NEXT_BUTTON_INDEX].setEnabled( false );
            button[HOME_BUTTON_INDEX].setEnabled( true );
            button[FITWORLD_BUTTON_INDEX].setEnabled( true );
            button[LOOKAT_BUTTON_INDEX].setEnabled( true );
            setPartialDisableFlag = false;
        }
        if ( setLookAtEnabledFlag ) {
            button[LOOKAT_BUTTON_INDEX].setEnabled( lookAtEnabled );
            setLookAtEnabledFlag = false;
        }
    }
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Clear the viewpoint list and disable self
     */
    public void clearViewpoints( ) {
        comboManager.clear( );
        setEnabled( false );
    }
    
    /**
     * Set the viewpoint dropdown list and prev/next buttons to a
     * disabled state. This is used when we only have the default viewpoint,
     * but still want to allow the home, fit and lookat capabilities.
     *
     * @param enabled true if this component is enabled
     */
    private synchronized void partialDisable( )
    {
        setPartialDisableFlag = true;
        setEnabledFlag = false;
        display.asyncExec( this );
    }
}
