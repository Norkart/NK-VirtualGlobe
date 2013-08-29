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

package org.xj3d.ui.swt.device;

// External imports 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

// Local imports 
import org.web3d.vrml.util.KeySensorDevice;
import org.web3d.vrml.util.KeySequence;
import org.web3d.vrml.util.Xj3DKeyCode;
import org.web3d.vrml.util.Xj3DKeyEvent;

/**
 * Implementation of a KeySensorDevice to gather and adapt the key events
 * for an SWT specific rendering component.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SWTKeySensorDevice implements KeySensorDevice, KeyListener {
    
    /** The array of character key codes - of interest. Arranged in a one
     *  dimensional array of swt, x3d pairs */
    private static int[] charKeyCode = {
        SWT.CR, Xj3DKeyCode.CHAR_KEY_ENTER,
        // There does not seem to be an SWT.BACKSPACE key code defined
        // Therefore this mapping is empirically derived.
        8, Xj3DKeyCode.CHAR_KEY_BACKSPACE,
    };
    
    /** The array of control key codes - of interest. Arranged in a one
     *  dimensional array of swt, x3d pairs */
    private static int[] controlKeyCode = {
        SWT.F1, Xj3DKeyCode.ACTION_KEY_F1,
        SWT.F2, Xj3DKeyCode.ACTION_KEY_F2,
        SWT.F3, Xj3DKeyCode.ACTION_KEY_F3,
        SWT.F4, Xj3DKeyCode.ACTION_KEY_F4,
        SWT.F5, Xj3DKeyCode.ACTION_KEY_F5,
        SWT.F6, Xj3DKeyCode.ACTION_KEY_F6,
        SWT.F7, Xj3DKeyCode.ACTION_KEY_F7,
        SWT.F8, Xj3DKeyCode.ACTION_KEY_F8,
        SWT.F9, Xj3DKeyCode.ACTION_KEY_F9,
        SWT.F10, Xj3DKeyCode.ACTION_KEY_F10,
        SWT.F11, Xj3DKeyCode.ACTION_KEY_F11,
        SWT.F12, Xj3DKeyCode.ACTION_KEY_F12,
        SWT.HOME, Xj3DKeyCode.ACTION_KEY_HOME,
        SWT.END, Xj3DKeyCode.ACTION_KEY_END,
        SWT.PAGE_UP, Xj3DKeyCode.ACTION_KEY_PGUP,
        SWT.PAGE_DOWN, Xj3DKeyCode.ACTION_KEY_PGDN,
        SWT.ARROW_UP, Xj3DKeyCode.ACTION_KEY_UP,
        SWT.ARROW_DOWN, Xj3DKeyCode.ACTION_KEY_DOWN,
        SWT.ARROW_LEFT, Xj3DKeyCode.ACTION_KEY_LEFT,
        SWT.ARROW_RIGHT, Xj3DKeyCode.ACTION_KEY_RIGHT,
        SWT.ALT, Xj3DKeyCode.MODIFIER_KEY_ALT,
        SWT.CONTROL, Xj3DKeyCode.MODIFIER_KEY_CONTROL,
        SWT.SHIFT, Xj3DKeyCode.MODIFIER_KEY_SHIFT,
    };
    
    /** The ordered list of key events once they have been adapted
     *  from swt to Xj3D format */
    private KeySequence keySequence;
    
    /**
     * Constructor
     */
    public SWTKeySensorDevice( ) {
        keySequence = new KeySequence( );
    }
    
    //------------------------------------------------------------------------
    // Methods for KeySensorDevice
    //------------------------------------------------------------------------
    
    /**
     * Return the ordered set of key events in the argument KeySequence
     * object that have occurred since the previous call to this method.
     *
     * @param seq - The KeySequence object to initialize with the set
     * of key events
     */
    public void getEvents( KeySequence seq ) {
        keySequence.transfer( seq );
    }
    
    //------------------------------------------------------------------------
    // Methods for KeyListener events
    //------------------------------------------------------------------------
    
    /**
     * Process a key press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyPressed( KeyEvent evt ) {
        int swtKeyCode = evt.keyCode;
        char character = evt.character;
        Object src = evt.widget;
        if ( character != 0 ) {
            // there is a character
            int x3dKeyCode = getCharKeyCode( swtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_PRESSED, 
                character, 
                x3dKeyCode ) );
        }
        else {
            // no character, must be a control key
            int x3dKeyCode = getControlKeyCode( swtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_PRESSED, 
                Xj3DKeyEvent.CHAR_UNDEFINED, 
                x3dKeyCode ) );
        }
    }
    
    /**
     * Process a key release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyReleased( KeyEvent evt ) {
        int swtKeyCode = evt.keyCode;
        char character = evt.character;
        Object src = evt.widget;
        if ( character != 0 ) {
            // there is a character
            int x3dKeyCode = getCharKeyCode( swtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_RELEASED, 
                character, 
                x3dKeyCode ) );
        }
        else {
            // no character, must be a control key
            int x3dKeyCode = getControlKeyCode( swtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_RELEASED, 
                Xj3DKeyEvent.CHAR_UNDEFINED, 
                x3dKeyCode ) );
        }
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
    /** 
     * Return the Xj3DKeyCode that cooresponds to the argument SWT 
     * key code. Note that for characters, the only key codes of
     * interest are enter and backspace. 
     *
     * @param swtKeyCode the SWT key code for a character key
     * @return the cooresponding Xj3DKeyCode if a match is found in
     * the defined set of character key codes, otherwise return
     * Xj3DKeyCode.KEY_CODE_UNDEFINED
     */
    private static int getCharKeyCode( int swtKeyCode ) {
        int x3dKeyCode = Xj3DKeyCode.KEY_CODE_UNDEFINED;
        for ( int i = charKeyCode.length - 2; i >= 0; i-=2 ) {
            if ( swtKeyCode == charKeyCode[i] ) {
                x3dKeyCode = charKeyCode[i+1];
                break;
            }
        }
        return( x3dKeyCode );
    }
    
    /** 
     * Return the Xj3DKeyCode that cooresponds to the argument SWT 
     * key code of a non-character key. 
     *
     * @param swtKeyCode the SWT key code for the control key
     * @return the cooresponding Xj3DKeyCode if a match is found in
     * the defined set of control key codes, otherwise return
     * Xj3DKeyCode.KEY_CODE_UNDEFINED
     */
    private static int getControlKeyCode( int swtKeyCode ) {
        int x3dKeyCode = Xj3DKeyCode.KEY_CODE_UNDEFINED;
        for ( int i = controlKeyCode.length - 2; i >= 0; i-=2 ) {
            if ( swtKeyCode == controlKeyCode[i] ) {
                x3dKeyCode = controlKeyCode[i+1];
                break;
            }
        }
        return( x3dKeyCode );
    }
}

