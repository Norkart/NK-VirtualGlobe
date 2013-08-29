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
import org.eclipse.swt.SWT;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

// Local imports
// none

/**
 * A window that can act as console for error messages from the application.
 * <p>
 *
 * The window will print error messages for all the error levels and only
 * throw an exception for the fatalError.
 *
 * @author Rex Melton
 * @version $Revision: 1.8 $
 */
public class ConsoleWindow implements SelectionListener, ShellListener, DisposeListener, Runnable {
    
    /** The console's title */
    private static final String CONSOLE_TITLE = "Xj3D Console";
    
    /** The clear button label */
    private static final String CLEAR_BUTTON_LABEL = "Clear";
    
    /** Tooltip used on the clear button */
    private static final String CLEAR_TOOLTIP =
        "Clear the contents of the console";
    
    /** The copy button label */
    private static final String COPY_BUTTON_LABEL = "Copy";
    
    /** Tooltip used on the copy button */
    private static final String COPY_TOOLTIP =
        "Copy the contents of the window to the clipboard";
    
    /** The version message prefix text */
    private static final String VERSION_MESSAGE_PREFIX = "Xj3D Version: ";
    
    /** The text widget to display the messages in */
    private Text text;
    
    /** The buffer for messages that will be displayed */
    private MessageBuffer messageBuffer;
    
    /** Clear button to remove the current contents of the message buffer */
    private Button clearButton;
    
    /** Copy button to paste the current contents of the message buffer 
     *  to the clipboard. */
    private Button copyButton;
    
    /** Reference to the Display object */
    private Display display;
    
    /** Our parent Shell object */
    private Shell parentShell;
    
    /** This console window instance */
    private Shell dialog;
    
    /** The clipboard instance */
    private Clipboard clipboard;
    
    /** Flag indicating the operation to perform on the dialog
     *  on the display thread - open or close */
    private boolean doOpen;
    
    /**
     * Create an instance of the console window.
     */
    public ConsoleWindow( Shell parent, MessageBuffer buffer ) {
        if ( parent == null ) {
            throw new IllegalArgumentException( "ConsoleWindow must have a valid parent" );
        }
        if ( buffer == null ) {
            throw new IllegalArgumentException( "ConsoleWindow must have a valid MessageBuffer" );
        }
        messageBuffer = buffer;
        parentShell = parent;
        parentShell.addShellListener( this );
        display = parent.getDisplay( );
        doOpen = true;
    }
    
    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------
    
    /**
     * Display thread routine to open or close the console.
     */
    public void run( ) {
        if ( doOpen ) {
            if ( dialog == null ) {
                dialog = new Shell( display, SWT.SHELL_TRIM|SWT.MODELESS|SWT.RESIZE );
                dialog.setText( CONSOLE_TITLE );
                dialog.setLayout( new FormLayout( ) );
                dialog.addDisposeListener( this );
                
                clearButton = new Button( dialog, SWT.PUSH );
                clearButton.setText( CLEAR_BUTTON_LABEL );
                clearButton.setToolTipText( CLEAR_TOOLTIP );
                clearButton.addSelectionListener( this );
                FormData formData = new FormData( );
                formData.left = new FormAttachment( 50, -5 );
                formData.bottom = new FormAttachment( 100, -5 );
                clearButton.setLayoutData( formData );
                
                copyButton = new Button( dialog, SWT.PUSH );
                copyButton.setText( COPY_BUTTON_LABEL );
                copyButton.setToolTipText( COPY_TOOLTIP );
                copyButton.addSelectionListener( this );
                formData = new FormData( );
                formData.right = new FormAttachment( clearButton, -5 );
                formData.bottom = new FormAttachment( 100, -5 );
                copyButton.setLayoutData( formData );
                
                text = new Text( dialog, SWT.MULTI|SWT.WRAP|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL );
                text.setEditable( false );
                formData = new FormData( );
                formData.top = new FormAttachment( 0, 5 );
                formData.bottom = new FormAttachment( clearButton, -5 );
                formData.left = new FormAttachment( 0, 5 );
                formData.right = new FormAttachment( 100, -5 );
                text.setLayoutData( formData );
                
                messageBuffer.setTextWidget( text );
                messageBuffer.run( );
                
                dialog.setSize( 600, 400 );
                dialog.setLocation( 80, 80 );
            }
            dialog.open( );
        } else {
            if ( ( dialog != null ) && !dialog.isDisposed( ) ) {
                dialog.close( );
                dialog = null;
            }
        }
    }
    
    //----------------------------------------------------------
    // Methods defined by SelectionListener
    //----------------------------------------------------------
    
    /**
     * Process the action generated from the user interface.
     *
     * @param se - The event that caused this method to be called
     */
    public void widgetSelected( SelectionEvent se ) {
        Object src = se.getSource( );
        if ( src == clearButton ) {
            messageBuffer.clear( );
            text.setText( "" );
        }
        else if ( src == copyButton ) {
            if ( clipboard == null ) {
                clipboard = new Clipboard( display );
            }
            String textData = text.getText( );
            if ( textData.length( ) > 0 ) {
                TextTransfer textTransfer = TextTransfer.getInstance();
                clipboard.setContents( new Object[]{ textData }, new Transfer[]{ textTransfer } );
            }
        }
    }
    
    /**
     * Ignored
     */
    public void widgetDefaultSelected( SelectionEvent se ) {
    } 
    
    //---------------------------------------------------------------
    // Methods defined by ShellListener
    //---------------------------------------------------------------
    
    /** Ignored */
    public void shellActivated( ShellEvent evt ) {
    }
    
    /**
     * When our parent shell closes - so do we.
     */
    public void shellClosed( ShellEvent evt ) {
        if ( ( dialog != null ) && !dialog.isDisposed( ) ) {
            dialog.close( );
            dialog = null;
        }
    }
    
    /** Ignored */
    public void shellDeactivated( ShellEvent evt ) {
    }
    
    /** Ignored */
    public void shellDeiconified( ShellEvent evt ) {
    }
    
    /** Ignored */
    public void shellIconified( ShellEvent evt ) {
    }
    
    //----------------------------------------------------------
    // Methods defined by DisposeListener
    //----------------------------------------------------------
    
    /**
     * We're being disposed of. Remove our Text widget from
     * the MessageBuffer
     */
    public void widgetDisposed( DisposeEvent evt ) {
		if ( !parentShell.isDisposed( ) ) {
        	parentShell.removeShellListener( this );
		}
        messageBuffer.setTextWidget( null );
        dialog = null;
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /** Open the console window */
    public void open( ) {
        doOpen = true;
        display.asyncExec( this );
    }
    
    /** Close the console window */
    public void close( ) {
        doOpen = false;
        display.asyncExec( this );
    }
}
