/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

import vrml.eai.Node;

/**
 * The base representation of any VRML field. Provides a core set of services
 * that all field types can use for introspection of classes.
 *
 * @version 1.0 7th March 1998
 */
public abstract class BaseField
{
    //  single valued fields
    public static final int SFBool = 1;
    public static final int SFColor = 2;
    public static final int SFFloat = 3;
    public static final int SFImage = 4;
    public static final int SFInt32 = 5;
    public static final int SFNode = 6;
    public static final int SFRotation = 7;
    public static final int SFString = 8;
    public static final int SFTime = 9;
    public static final int SFVec2f = 10;
    public static final int SFVec3f = 11;

    // multi valued fields
    public static final int MFColor = 12;
    public static final int MFFloat = 13;
    public static final int MFInt32 = 14;
    public static final int MFNode = 15;
    public static final int MFRotation = 16;
    public static final int MFString = 17;
    public static final int MFTime = 18;
    public static final int MFVec2f = 19;
    public static final int MFVec3f = 20;

    protected static final int UNSET_FIELD = -1;

    /** The type of values that this field represents. */
    protected int fieldType = UNSET_FIELD;

    /**
     * Construct a new instance of a field.
     *
     * @param type The type of the field
     */
    protected BaseField(int type)
    {
        fieldType = type;
    }

    /**
     * Get the basic type of this field.
     *
     * @return The type as defined by one of the above values
     */
    public int getType()
    {
        return fieldType;
    }
}

