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

package org.xj3d.ui.awt.device;

// External imports - NONE
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.web3d.vrml.util.KeySensorDevice;
import org.web3d.vrml.util.KeySequence;
import org.web3d.vrml.util.Xj3DKeyCode;
import org.web3d.vrml.util.Xj3DKeyEvent;

// Local imports - NONE


/**
 * Implementation of a KeySensorDevice to gather and adapt the key events
 * for an AWT specific rendering component.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class AWTKeySensorDevice implements KeySensorDevice, KeyListener {
    
    /** The array of character key codes - of interest. Arranged in a one
     *  dimensional array of awt, x3d pairs */
    private static int[] charKeyCode = {
        KeyEvent.VK_ENTER, Xj3DKeyCode.CHAR_KEY_ENTER,
        KeyEvent.VK_BACK_SPACE, Xj3DKeyCode.CHAR_KEY_BACKSPACE,
    };
    
    /** The array of control key codes - of interest. Arranged in a one
     *  dimensional array of awt, x3d pairs */
    private static int[] controlKeyCode = {
        KeyEvent.VK_F1, Xj3DKeyCode.ACTION_KEY_F1,
        KeyEvent.VK_F2, Xj3DKeyCode.ACTION_KEY_F2,
        KeyEvent.VK_F3, Xj3DKeyCode.ACTION_KEY_F3,
        KeyEvent.VK_F4, Xj3DKeyCode.ACTION_KEY_F4,
        KeyEvent.VK_F5, Xj3DKeyCode.ACTION_KEY_F5,
        KeyEvent.VK_F6, Xj3DKeyCode.ACTION_KEY_F6,
        KeyEvent.VK_F7, Xj3DKeyCode.ACTION_KEY_F7,
        KeyEvent.VK_F8, Xj3DKeyCode.ACTION_KEY_F8,
        KeyEvent.VK_F9, Xj3DKeyCode.ACTION_KEY_F9,
        KeyEvent.VK_F10, Xj3DKeyCode.ACTION_KEY_F10,
        KeyEvent.VK_F11, Xj3DKeyCode.ACTION_KEY_F11,
        KeyEvent.VK_F12, Xj3DKeyCode.ACTION_KEY_F12,
        KeyEvent.VK_HOME, Xj3DKeyCode.ACTION_KEY_HOME,
        KeyEvent.VK_END, Xj3DKeyCode.ACTION_KEY_END,
        KeyEvent.VK_PAGE_UP, Xj3DKeyCode.ACTION_KEY_PGUP,
        KeyEvent.VK_PAGE_DOWN, Xj3DKeyCode.ACTION_KEY_PGDN,
        KeyEvent.VK_UP, Xj3DKeyCode.ACTION_KEY_UP,
        KeyEvent.VK_DOWN, Xj3DKeyCode.ACTION_KEY_DOWN,
        KeyEvent.VK_LEFT, Xj3DKeyCode.ACTION_KEY_LEFT,
        KeyEvent.VK_RIGHT, Xj3DKeyCode.ACTION_KEY_RIGHT,
        KeyEvent.VK_ALT, Xj3DKeyCode.MODIFIER_KEY_ALT,
        KeyEvent.VK_CONTROL, Xj3DKeyCode.MODIFIER_KEY_CONTROL,
        KeyEvent.VK_SHIFT, Xj3DKeyCode.MODIFIER_KEY_SHIFT,
    };
    
    /** The ordered list of key events once they have been adapted
     *  from awt to Xj3D format */
    private KeySequence keySequence;
    
    /**
     * Constructor
     */
    public AWTKeySensorDevice( ) {
        keySequence = new KeySequence( );
    }
    
    //------------------------------------------------------------------------
    // Methods for KeyDevice
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
        int awtKeyCode = evt.getKeyCode( );
        char character = evt.getKeyChar( );
        Object src = evt.getSource( );
        if ( character != KeyEvent.CHAR_UNDEFINED ) {
            // there is a character
            int x3dKeyCode = getCharKeyCode( awtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_PRESSED, 
                character, 
                x3dKeyCode ) );
        }
        else {
            // no character, must be a control key
            int x3dKeyCode = getControlKeyCode( awtKeyCode );
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
        int awtKeyCode = evt.getKeyCode( );
        char character = evt.getKeyChar( );
        Object src = evt.getSource( );
        if ( character != KeyEvent.CHAR_UNDEFINED ) {
            // there is a character
            int x3dKeyCode = getCharKeyCode( awtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_RELEASED, 
                character, 
                x3dKeyCode ) );
        }
        else {
            // no character, must be a control key
            int x3dKeyCode = getControlKeyCode( awtKeyCode );
            keySequence.add( new Xj3DKeyEvent( 
                src,
                Xj3DKeyEvent.KEY_RELEASED, 
                Xj3DKeyEvent.CHAR_UNDEFINED, 
                x3dKeyCode ) );
        }
    }
    
    /**
     * Process a key typed event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyTyped( KeyEvent evt ) {
    }
    
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
    /** 
     * Return the Xj3DKeyCode that cooresponds to the argument AWT 
     * key code. Note that for characters, the only key codes of
     * interest are enter and backspace. 
     *
     * @param awtKeyCode the AWT key code for a character key
     * @return the cooresponding Xj3DKeyCode if a match is found in
     * the defined set of character key codes, otherwise return
     * Xj3DKeyCode.KEY_CODE_UNDEFINED
     */
    private static int getCharKeyCode( int awtKeyCode ) {
        int x3dKeyCode = Xj3DKeyCode.KEY_CODE_UNDEFINED;
        for ( int i = charKeyCode.length - 2; i >= 0; i-=2 ) {
            if ( awtKeyCode == charKeyCode[i] ) {
                x3dKeyCode = charKeyCode[i+1];
                break;
            }
        }
        return( x3dKeyCode );
    }
    
    /** 
     * Return the Xj3DKeyCode that cooresponds to the argument AWT 
     * key code of a non-character key. 
     *
     * @param awtKeyCode the AWT key code for the control key
     * @return the cooresponding Xj3DKeyCode if a match is found in
     * the defined set of control key codes, otherwise return
     * Xj3DKeyCode.KEY_CODE_UNDEFINED
     */
    private static int getControlKeyCode( int awtKeyCode ) {
        int x3dKeyCode = Xj3DKeyCode.KEY_CODE_UNDEFINED;
        for ( int i = controlKeyCode.length - 2; i >= 0; i-=2 ) {
            if ( awtKeyCode == controlKeyCode[i] ) {
                x3dKeyCode = controlKeyCode[i+1];
                break;
            }
        }
        return( x3dKeyCode );
    }
}

