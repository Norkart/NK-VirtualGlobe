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

package org.web3d.vrml.util;

// External imports - NONE
// Local imports - NONE

/**
 * The key code identifiers of the set of character and non-character
 * generating keys that are specifically identified by the X3D spec. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Xj3DKeyCode {
    
    //-----------------------------------------------------------------------
    // Action keys, non-character generating - codes specifically defined
    // by the X3D KeySensor spec
    
    /** The F1 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F1 = 1;
    
    /** The F2 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F2 = 2;
    
    /** The F3 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F3 = 3;
    
    /** The F4 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F4 = 4;
    
    /** The F5 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F5 = 5;
    
    /** The F6 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F6 = 6;
    
    /** The F7 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F7 = 7;
    
    /** The F8 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F8 = 8;
    
    /** The F9 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F9 = 9;
    
    /** The F10 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F10 = 10;
    
    /** The F11 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F11 = 11;
    
    /** The F12 key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_F12 = 12;
    
    /** The Home key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_HOME = 13;
    
    /** The End key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_END = 14;
    
    /** The Page Up key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_PGUP = 15;
    
    /** The Page Down key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_PGDN = 16;
    
    /** The Up Arrow key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_UP = 17;
    
    /** The Down Arrow key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_DOWN = 18;
    
    /** The Left Arrow key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_LEFT = 19;
    
    /** The Right Arrow key action code, defined by the X3D KeySensor spec */
    public static final int ACTION_KEY_RIGHT = 20;
    
    //-----------------------------------------------------------------------
    // Modifier keys - non-character generating - codes arbitrarily defined
    
    /** The modifier key code base identifier */
    public static final int MODIFIER_KEY_BASE = 0x100;
    
    /** The Alt key modifier code */
    public static final int MODIFIER_KEY_ALT = MODIFIER_KEY_BASE + 0;
    
    /** The Control key modifier code */
    public static final int MODIFIER_KEY_CONTROL = MODIFIER_KEY_BASE + 1;
    
    /** The Shift key modifier code */
    public static final int MODIFIER_KEY_SHIFT = MODIFIER_KEY_BASE + 2;
    
    //-----------------------------------------------------------------------
    // Character keys - character generating - codes arbitrarily defined
    
    /** The constant defining that the character is the Enter key */
    public static final int CHAR_KEY_ENTER = '\n';
    
    /** The constant defining that the character is the Backspace key */
    public static final int CHAR_KEY_BACKSPACE = '\b';
    
    //-----------------------------------------------------------------------
    // Null key code - arbitrarily defined
    
    /** The constant defining that no action or modifier code is associated
     *  with the key */
    public static final int KEY_CODE_UNDEFINED = 0xFFFF;
}

