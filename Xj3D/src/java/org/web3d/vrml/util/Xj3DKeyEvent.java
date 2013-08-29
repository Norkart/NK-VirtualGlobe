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

// External imports
import java.util.EventObject;

// Local imports - NONE

/**
 * A key event object modeled on the awt and swt key event objects.
 * This class has been developed for use within the X3D KeySensor
 * and StringSensor node implementations to ensure independence from
 * a specific ui toolkit implementation. 
 * This class is limited in capability to delivering key characters
 * and identifying the few non-character generating keys that are 
 * specifically required by the X3D spec. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Xj3DKeyEvent extends EventObject {
    
    /** The constant defining a key pressed event */
    public static final int KEY_PRESSED = 'K'<<24|'P'<<18;
    
    /** The constant defining a key released event */
    public static final int KEY_RELEASED = 'K'<<24|'R'<<18;
    
    /** The constant defining that no character is associated with
     *  the key */
    public static final char CHAR_UNDEFINED = 0xFFFF;
    
    /** The id of the event, either KEY_PRESSED or KEY_RELEASED */
    private int eventID;
    
    /** The character associated with the key in this event, or
     *  the NO_CHARACTER constant if no character is associated with
     *  the key */
    private char character = CHAR_UNDEFINED;
    
    /** The identifier code associated with this key */
    private int keyCode = Xj3DKeyCode.KEY_CODE_UNDEFINED;
    
    /** Flag indicating that a character is associated with this key */
    private boolean isCharacter;
    
    /** Flag indicating that the character associated with this key
     *  is the Enter key */
    private boolean isEnter;
    
    /** Flag indicating that the character associated with this key
     *  is the Backspace key */
    private boolean isBackspace;
    
    /** Flag indicating that this key is a defined Action key */
    private boolean isAction;
    
    /** Flag indicating that this key is a defined Modifier key */
    private boolean isModifier;
    
    //-----------------------------------------------------------------------
    // Note - the min max ranges presume sequential definitions...
    
    /** The minimum modifier key code, used to bounds check in the constructor */
    private static final int MODIFIER_KEY_MIN = Xj3DKeyCode.MODIFIER_KEY_ALT;
    
    /** The maximum modifier key code, used to bounds check in the constructor */
    private static final int MODIFIER_KEY_MAX = Xj3DKeyCode.MODIFIER_KEY_SHIFT;
    
    /** The minimum action key code, used to bounds check in the constructor */
    private static final int ACTION_KEY_MIN = Xj3DKeyCode.ACTION_KEY_F1;
    
    /** The maximum action key code, used to bounds check in the constructor */
    private static final int ACTION_KEY_MAX = Xj3DKeyCode.ACTION_KEY_RIGHT;
    
    //-----------------------------------------------------------------------
    
    /** 
     * Constructor for a character associated event. If
     * the argument character equals the NO_CHARACTER constant,
     * the getKeyChar() method will return NO_CHARACTER and the 
     * isCharacter() method will return false. Otherwise,
     * the getKeyChar() method will return the argument 
     * character and the isCharacter() method will return true. 
     * @param src the object that was the source of the event
     * @param id the event id, either KEY_PRESSED or KEY_RELEASED.
     * @param c the character associated with the key.
     * @param code the identifier of the specific defined 
     * function of this character associated key event.
     */
    public Xj3DKeyEvent( Object src, int id, char c, int code ) {
        super( src );
        if ( (( id != KEY_PRESSED ) && ( id != KEY_RELEASED )) ) {
            throw new IllegalArgumentException( "Unknown Event ID" );
        }
        eventID = id;
        character = c;
        keyCode = code;
        if ( c != CHAR_UNDEFINED ) {
            isCharacter = true;
            if ( code == Xj3DKeyCode.CHAR_KEY_ENTER ) {
                isEnter = true;
            }
            else if ( code == Xj3DKeyCode.CHAR_KEY_BACKSPACE ) {
                isBackspace = true;   
            }
        }
        else {
            if ( ( code >= MODIFIER_KEY_MIN ) && ( code <= MODIFIER_KEY_MAX ) ) {
                isModifier = true;
            }
            else if ( ( code >= ACTION_KEY_MIN ) && ( code <= ACTION_KEY_MAX ) ) {
                isAction = true;
            }
        }
    }
    /** 
     * Return the event identifier.
     * @return the event identifier, either KEY_PRESSED or KEY_RELEASED.
     */
    public int getID( ) {
        return( eventID );
    }
    /**
     * Returns whether there is a character associated with the key
     * that generated this event.
     */
    public boolean isCharacter( ) {
        return( isCharacter );
    }
    /**
     * Returns whether the character associated with the key that
     * generated this event is the Enter key.
     */
    public boolean isEnterKey( ) {
        return( isEnter );
    }
    /**
     * Returns whether the character associated with the key that
     * generated this event is the Backspace key.
     */
    public boolean isBackspaceKey( ) {
        return( isBackspace );
    }
    /** 
     * Returns the character associated with the key that generated
     * this event. If no character is associated, then the NO_CHARACTER
     * constant is returned.
     */
    public char getKeyChar( ) {
        return( character );
    }
    /** 
     * Returns the key identifier code associated with the key that
     * generated this event. If no key identifier code is associated,
     * then the NO_KEY_CODE constant is returned.
     */
    public int getKeyCode( ) {
        return( keyCode );
    }
    /**
     * Returns whether the key that generated this event is a defined
     * action key.
     */
    public boolean isAction( ) {
        return( isAction );
    }
    /**
     * Returns whether the key that generated this event is a defined
     * modifer key.
     */
    public boolean isModifier( ) {
        return( isModifier );
    }
}

