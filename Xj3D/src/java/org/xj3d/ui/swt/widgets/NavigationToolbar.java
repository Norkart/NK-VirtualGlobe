/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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
import java.util.Properties;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Button;
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
import org.web3d.browser.Xj3DConstants;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * A toolbar for all navigation commands.
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
 * <li>Examine:  images/navigation/ButtonExamine.gif</li>
 * <li>Fly: images/navigation/ButtonFly.gif</li>
 * <li>Pan: images/navigation/ButtonPan.gif</li>
 * <li>Tilt: images/navigation/ButtonTilt.gif</li>
 * <li>Walk: images/navigation/ButtonWalk.gif</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class NavigationToolbar extends Composite
    implements SelectionListener, NavigationStateListener, Runnable {
    
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
    
    /** Default examine button text */
    private static final String DEFAULT_EXAMINE_TEXT = "Examine";
    
    /** Default fly button text */
    private static final String DEFAULT_FLY_TEXT = "Fly";
    
    /** Default pan button text */
    private static final String DEFAULT_PAN_TEXT = "Pan";
    
    /** Default tilt button text */
    private static final String DEFAULT_TILT_TEXT = "Tilt";
    
    /** Default walk button text */
    private static final String DEFAULT_WALK_TEXT = "Walk";
    
    /** Property index in buttonData */
    private static int PROPERTY_INDEX = 0;
    
    /** Image path index in buttonData */
    private static int IMAGE_INDEX = 1;
    
    /** Button text index in buttonData */
    private static int TEXT_INDEX = 2;
    
    /** Navigation mode identifier index in buttonData */
    private static int MODE_INDEX = 3;
    
    /** Property identifier - Image path - button text - navigation mode array */
    private static final String[][] buttonData = { 
        { FLY_BUTTON_PROPERTY, DEFAULT_FLY_BUTTON, 
            DEFAULT_FLY_TEXT, Xj3DConstants.FLY_NAV_MODE },
        { PAN_BUTTON_PROPERTY, DEFAULT_PAN_BUTTON, 
            DEFAULT_PAN_TEXT, Xj3DConstants.PAN_NAV_MODE },
        { TILT_BUTTON_PROPERTY, DEFAULT_TILT_BUTTON, 
            DEFAULT_TILT_TEXT, Xj3DConstants.TILT_NAV_MODE },
        { WALK_BUTTON_PROPERTY, DEFAULT_WALK_BUTTON, 
            DEFAULT_WALK_TEXT, Xj3DConstants.WALK_NAV_MODE },
        { EXAMINE_BUTTON_PROPERTY, DEFAULT_EXAMINE_BUTTON, 
            DEFAULT_EXAMINE_TEXT, Xj3DConstants.EXAMINE_NAV_MODE },
    };
    
    /** Condition setting indicating a button setting state transition to true */
    private static final int TRANSITION_TRUE = 1;
    
    /** Condition setting indicating no button setting state transition */
    private static final int TRANSITION_NONE = 0;
    
    /** Condition setting indicating a button setting state transition to false */
    private static final int TRANSITION_FALSE = -1;
    
    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;
    
    /** The last known navigation state list for updating nav mode */
    private String[] navigationModes;
    
    /** The array of navigation state buttons */
    private Button[] button;
    
    /** Display instance, used to put updates on the display thread */
    private Display display;
    
    /** The enabled states of the navigation buttons,
     *  sidepocketed for processing on the display thread */
    private int[] enabledTransition;
    
    /** The selection states of the navigation buttons,
     *  sidepocketed for processing on the display thread */
    private int[] selectionTransition;
    
    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;
    
    /**
     * Create a new horizontal navigation toolbar with an empty list of
     * viewpoints and disabled user selection of state.
     *
     * @param parent the SWT Composite widget that this will be added to
     * @param core The browser core implementation to send nav changes to
     * @param reporter The reporter instance to use or null
     */
    public NavigationToolbar( Composite parent, BrowserCore core, ErrorReporter reporter ) {
        this( parent, core, true, reporter);
    }
    
    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param parent the SWT Composite widget that this will be added to
     * @param core The browser core implementation to send nav changes to
     * @param horizontal True to lay out the buttons horizontally
     * @param reporter The reporter instance to use or null
     */
    public NavigationToolbar( 
        Composite parent,
        BrowserCore core,
        boolean horizontal,
        ErrorReporter reporter ) {
        this( parent, core, horizontal, null, reporter );
    }
    
    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     *
     * @param core The browser core implementation to send nav changes to
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public NavigationToolbar( 
        Composite parent,
        BrowserCore core,
        Properties skinProperties,
        ErrorReporter reporter ) {
        this( parent, core, true, skinProperties, reporter );
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
    public NavigationToolbar( 
        Composite parent,
        BrowserCore core,
        boolean horizontal,
        Properties skinProperties,
        ErrorReporter reporter ) {
        
        super( parent, SWT.NONE );
        
        browserCore = core;
        
        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;
        
        Properties skin = ( skinProperties == null ) ?
            DEFAULT_PROPERTIES : skinProperties;
        
        browserCore.addNavigationStateListener( this );
        
        int numButtons = buttonData.length;
        
        GridLayout layout = new GridLayout( );
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.makeColumnsEqualWidth = true;
        if( horizontal ) {
            layout.numColumns = numButtons;
        } else {
            layout.numColumns = 1;
        }
        super.setLayout( layout );
        
        button = new Button[numButtons];
        display = getDisplay( );
        for ( int i = 0; i < numButtons; i++ ) {
            String buttonText = buttonData[i][TEXT_INDEX];
            String img_name = skin.getProperty( 
                buttonData[i][PROPERTY_INDEX],
                buttonData[i][IMAGE_INDEX] );
            Image icon = ImageLoader.loadImage( display, img_name, reporter );
            Button b = new Button( this, SWT.TOGGLE );
            if( icon == null ) {
                b.setText( buttonText );
            } else {
                b.setImage( icon );
            }
            b.setToolTipText( buttonText );
            b.addSelectionListener( this );
            b.setEnabled( false );
            button[i] = b;
        }
        enabledTransition = new int[numButtons];
        selectionTransition = new int[numButtons];
    }
    
    //---------------------------------------------------------
    // Methods defined by Composite
    //---------------------------------------------------------
    
    /** Do nothing, we do our own layout. */
    public void setLayout( Layout layout ) {
    }
    
    //----------------------------------------------------------
    // Methods defined by NavigationStateListener
    //----------------------------------------------------------
    
    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged( int idx ) {
        // If only navigationStateChanged broadcast the
        // new nav state in numeric form...
        if( navigationModes != null ) {
            String newMode = navigationModes[idx];
            for ( int i = 0; i < button.length; i++ ) {
                if ( newMode.equalsIgnoreCase( buttonData[i][MODE_INDEX] ) ) {
                    selectionTransition[i] = TRANSITION_TRUE;
                } else {
                    selectionTransition[i] = TRANSITION_FALSE;
                }
            }
            display.asyncExec( this );
        }
    }
    
    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numModes The number of modes in the array
     */
    public void navigationListChanged( String[] modes, int numModes ) {
        if( navigationModes == null || navigationModes.length != numModes ) {
            navigationModes = new String[numModes];
        }
        System.arraycopy( modes, 0, navigationModes, 0, numModes );
        boolean found_any = false;
        
        for ( int i = 0; i < button.length; i++ ) {
            enabledTransition[i] = TRANSITION_FALSE;
        }
        
        String mode;
        for( int i = 0; i < numModes; i++ ) {
            if ( modes[i].equals( Xj3DConstants.ANY_NAV_MODE )) {
                found_any = true;
                break;
            }
            mode = modes[i];
            for ( int j = 0; j < button.length; j++ ) {
                if ( mode.equals( buttonData[j][MODE_INDEX] ) ) {
                    enabledTransition[j] = TRANSITION_TRUE;
                    break;
                }
            }
        }
        
        if ( found_any ) {
            for ( int i = 0; i < button.length; i++ ) {
                enabledTransition[i] = TRANSITION_TRUE;
            }
        }
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
        Button src = (Button)se.getSource( );
        String nav_mode = Xj3DConstants.NONE_NAV_MODE;
        
        for ( int i = 0; i < button.length; i++ ) {
            if ( src == button[i] ) {
                nav_mode = buttonData[i][MODE_INDEX];
            } else {
                button[i].setSelection( false );
            }
        }
        browserCore.setNavigationMode( nav_mode );
    }

    /**
     * Process the default selection generated from the user interface.
     * Ignored.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
    } 
    
    //------------------------------------------------------------------------
    // Methods defined by Runnable
    //------------------------------------------------------------------------
    
    /**
     * Process the enabled and selection state transitions of the buttons
     * that have been changed through the navigation state listener.
     */
    public void run( ) {
        for ( int i = 0; i < button.length; i++ ) {
            
            // process the enabled state transitions that have 
            // been queued, then reset the state transition to
            // 'do nothing'
            switch( enabledTransition[i] ) {
            case TRANSITION_FALSE:
                button[i].setEnabled( false );
                break;
            case TRANSITION_TRUE:
                button[i].setEnabled( true );
                break;
            }
            enabledTransition[i] = TRANSITION_NONE;
            
            // process the selection state transitions that have 
            // been queued, then reset the state transition to
            // 'do nothing'
            switch( selectionTransition[i] ) {
            case TRANSITION_FALSE:
                button[i].setSelection( false );
                break;
            case TRANSITION_TRUE:
                button[i].setSelection( true );
                break;
            }
            selectionTransition[i] = TRANSITION_NONE;
        }
    }
}
