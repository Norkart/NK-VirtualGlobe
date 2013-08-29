/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Listing of constants relating to fields types.
 * <p>
 *
 * These constants will be returned by the various methods of
 * {@link X3DFieldDefinition}.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public interface X3DFieldTypes {

    /** The field access type is exposedField */
    public int INPUT_ONLY = 1;

    /** The field access type is eventIn */
    public int INITIALIZE_ONLY = 2;

    /** The field access type is eventOut */
    public int INPUT_OUTPUT = 3;

    /** The field access type is field */
    public int OUTPUT_ONLY = 4;


    /** The field type is SFBOOL */
    public int SFBOOL = 1;

    /** The field type is MFBOOL */
    public int MFBOOL = 2;

    /** The field type is MFInt32 */
    public int MFINT32 = 3;

    /** The field type is SFInt32 */
    public int SFINT32 = 4;

    /** The field type is SFLong */
    public int SFLONG = 5;

    /** The field type is MFLong */
    public int MFLONG = 6;

    /** The field type is SFFloat */
    public int SFFLOAT = 7;

    /** The field type is MFFloat */
    public int MFFLOAT = 8;

    /** The field type is SFDouble */
    public int SFDOUBLE = 9;

    /** The field type is MFDouble */
    public int MFDOUBLE = 10;

    /** The field type is SFTime */
    public int SFTIME = 11;

    /** The field type is MFTime */
    public int MFTIME = 12;

    /** The field type is SFNode */
    public int SFNODE = 13;

    /** The field type is MFNode */
    public int MFNODE = 14;

    /** The field type is SFVec2f */
    public int SFVEC2F = 15;

    /** The field type is MFVec2f */
    public int MFVEC2F = 16;

    /** The field type is SFVec2d */
    public int SFVEC2D = 17;

    /** The field type is MFVec2d */
    public int MFVEC2D = 18;

    /** The field type is SFVec3f */
    public int SFVEC3F = 19;

    /** The field type is MFVec3f */
    public int MFVEC3F = 20;

    /** The field type is SFVec3d */
    public int SFVEC3D = 21;

    /** The field type is MFVec3d */
    public int MFVEC3D = 22;

    /** The field type is MFRotation */
    public int MFROTATION = 23;

    /** The field type is SFRotation */
    public int SFROTATION = 24;

    /** The field type is MFColor */
    public int MFCOLOR = 25;

    /** The field type is SFColor */
    public int SFCOLOR = 26;

    /** The field type is MFColorRGBA */
    public int MFCOLORRGBA = 27;

    /** The field type is SFColorRGBA */
    public int SFCOLORRGBA = 28;

    /** The field type is SFImage */
    public int SFIMAGE = 29;

    /** The field type is MFImage */
    public int MFIMAGE = 30;

    /** The field type is SFString */
    public int SFSTRING = 31;

    /** The field type is MFString */
    public int MFSTRING = 32;
}
