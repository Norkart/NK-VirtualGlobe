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

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

// Local imports
// None

/**
 * A self-configured button implementation that can be used to open
 * the console window.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class ConsoleTool extends Composite implements SelectionListener {
    
    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    
    /** Property in browser skin which determines 'show console' image */
    private static final String CONSOLE_BUTTON_PROPERTY = "CONSOLE.button";
    
    /** Default image to use for 'show console' button */
    private static final String DEFAULT_CONSOLE_BUTTON =
        "images/navigation/ButtonConsole.gif";
    
    /** Console button text, if image is not found */
    private static final String CONSOLE_BUTTON_TEXT = "Console";
    
    /** Console button tool tip text */
    private static final String CONSOLE_BUTTON_TOOLTIP_TEXT =
        "Show browser console";
    
    /** THE button */
    private Button button;
    
    /** The object that will instantiate and show the console window */
    private ConsoleWindow console;
    
    /**
     * Create an instance of the button configured to show or 
     * hide the console window.
     *
     * @param parent - The SWT Composite widget that this will be added to
     * @param console - The console window object to control
     * @param skinProperties - The properties object specifying image names
     * @param buffer - The message buffer
     */
    public ConsoleTool( Composite parent, ConsoleWindow console, 
        Properties skinProperties, MessageBuffer buffer ) {
        
        super( parent, SWT.NONE );

		this.console = console;
		
        Properties skin = ( skinProperties == null ) ?
            DEFAULT_PROPERTIES : skinProperties;
        
        GridLayout layout = new GridLayout( );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.numColumns = 1;
        super.setLayout( layout );
        
        String img_name = skinProperties.getProperty( CONSOLE_BUTTON_PROPERTY,
            DEFAULT_CONSOLE_BUTTON );
        
        Display display = getDisplay( );
        Image icon = ImageLoader.loadImage( display, img_name, buffer );
        
        button = new Button( this, SWT.PUSH );
        if( icon == null ) {
            button.setText( CONSOLE_BUTTON_TEXT );
        } else {
            button.setImage( icon );
        }
        button.setToolTipText( CONSOLE_BUTTON_TOOLTIP_TEXT );
        button.addSelectionListener( this );
    }
    
    //---------------------------------------------------------
    // Methods overridden in Composite
    //---------------------------------------------------------
    
    /** Do nothing, we do our own layout */
    public void setLayout( Layout layout ) {
    }
    
    //----------------------------------------------------------
    // Methods defined by SelectionListener
    //----------------------------------------------------------
    
    /**
     * Process the selection event of the button being pressed. 
     * Will cause the console dialog to be created if it is not
     * already or bring it to the top if it exists.
     *
     * @param se The event that caused this method to be called
     */
    public void widgetSelected( SelectionEvent se ) {
        console.open( );
    }

    /**
     * Ignored, there is no default selection for a button.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
    }
}
