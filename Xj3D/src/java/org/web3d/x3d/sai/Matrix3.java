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
 * Describes a 3x3 Matrix as required by the SAIMatrix abstract type.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class Matrix3 {

    public void set(int row, int column)
    {
    }

    public float get(int row, int column)
    {
        return 0;
    }

    public void setTransform(SFVec2f translation,
                             SFVec3f rotation,
                             SFVec2f scale,
                             SFVec3f scaleOrientation,
                             SFVec2f center)
    {
    }

    public void getTransform(SFVec2f translation,
                             SFVec3f rotation,
                             SFVec2f scale)
    {
    }

    public Matrix3 inverse()
    {
        return null;
    }

    public Matrix3 transpose()
    {
        return null;
    }

    public Matrix3 multiplyLeft(Matrix3 mat)
    {
        return null;
    }

    public Matrix3 multiplyRight(Matrix3 mat)
    {
        return null;
    }

    public Matrix3 multiplyRowVector(SFVec3f vec)
    {
        return null;
    }

    public Matrix3 multiplyColVector(SFVec3f vec)
    {
        return null;
    }
}
