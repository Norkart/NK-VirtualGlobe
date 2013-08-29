/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
import java.util.ArrayList;
import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimObject;

/**
 * Implementation of the joint object that does in-place software evaluation of
 * the skin mesh updates and is optimised for speed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SoftwareSpeedJoint extends SoftwareJoint
{
    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_MSG =
        "Conflicting joint optimisation types";

    /**
     * Create a new, default instance of the site.
     */
    SoftwareSpeedJoint()
    {
    }

    //----------------------------------------------------------
    // Methods defined by HAnimJoint
    //----------------------------------------------------------

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setChildren(HAnimObject[] kids, int numValid)
    {
        for(int i = 0; i < numChildren; i++)
        {
            if(!(kids[i] instanceof SoftwareSpeedJoint))
                throw new IllegalArgumentException(WRONG_TYPE_MSG);
        }

        super.setChildren(kids, numValid);
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    public void addChild(HAnimObject kid)
    {
        if(!(kid instanceof SoftwareSpeedJoint))
            throw new IllegalArgumentException(WRONG_TYPE_MSG);

        super.addChild(kid);
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. This should not be callable by the general public. Derived
     * classes may override this method, but should call it as well to ensure
     * the internal matrices are correctly updated.
     *
     * @param parentTransform The transformation into global coordinates of
     *   the parent of this joint
     * @param parentChanged Flag to indicate that the parent transformation
     *   matrix has changed or is still the same as last call
     */
    protected void updateSkeleton(Matrix4f parentTransform,
                                  boolean parentChanged)
    {
        super.updateSkeleton(parentTransform, parentChanged);

        // now update the weighted vertices using the global matrix
        if(outputCoords == null)
            return;

        // if either of the normal items are dodgy, ignore
        if(numSourceNormals == 0)
        {
            float[] c_buf = (float[])outputCoords;

            for(int i = 0; i < numSkinCoord; i++)
            {
                int index = skinCoordIndex[i];
                if(!dirtyCoordinates[index])
                    continue;

                float x = sourceCoords[index * 3];
                float y = sourceCoords[index * 3 + 1];
                float z = sourceCoords[index * 3 + 2];

                float out_x = globalMatrix.m00 * x + globalMatrix.m01 * y +
                              globalMatrix.m02 * z + globalMatrix.m03;
                float out_y = globalMatrix.m10 * x + globalMatrix.m11 * y +
                              globalMatrix.m12 * z + globalMatrix.m13;
                float out_z = globalMatrix.m20 * x + globalMatrix.m21 * y +
                              globalMatrix.m22 * z + globalMatrix.m23;

                c_buf[index * 3] += out_x * skinCoordWeight[i];
                c_buf[index * 3 + 1] += out_y * skinCoordWeight[i];
                c_buf[index * 3 + 2] += out_z * skinCoordWeight[i];
            }
        }
        else
        {
            float[] c_buf = (float[])outputCoords;
            float[] n_buf = (float[])outputNormals;

            for(int i = 0; i < numSkinCoord; i++)
            {
                int index = skinCoordIndex[i];
                if(!dirtyCoordinates[index])
                    continue;

                float cx = sourceCoords[index * 3];
                float cy = sourceCoords[index * 3 + 1];
                float cz = sourceCoords[index * 3 + 2];

                float nx = sourceNormals[index * 3];
                float ny = sourceNormals[index * 3 + 1];
                float nz = sourceNormals[index * 3 + 2];

                float out_cx = globalMatrix.m00 * cx + globalMatrix.m01 * cy +
                               globalMatrix.m02 * cz + globalMatrix.m03;
                float out_cy = globalMatrix.m10 * cx + globalMatrix.m11 * cy +
                               globalMatrix.m12 * cz + globalMatrix.m13;
                float out_cz = globalMatrix.m20 * cx + globalMatrix.m21 * cy +
                               globalMatrix.m22 * cz + globalMatrix.m23;

                float out_nx = globalMatrix.m00 * nx + globalMatrix.m01 * ny +
                               globalMatrix.m02 * nz;
                float out_ny = globalMatrix.m10 * nx + globalMatrix.m11 * ny +
                               globalMatrix.m12 * nz;
                float out_nz = globalMatrix.m20 * nx + globalMatrix.m21 * ny +
                               globalMatrix.m22 * nz;

                c_buf[index * 3] += out_cx * skinCoordWeight[i];
                c_buf[index * 3 + 1] += out_cy * skinCoordWeight[i];
                c_buf[index * 3 + 2] += out_cz * skinCoordWeight[i];

                n_buf[index * 3] += out_nx * skinCoordWeight[i];
                n_buf[index * 3 + 1] += out_ny * skinCoordWeight[i];
                n_buf[index * 3 + 2] += out_nz * skinCoordWeight[i];
            }
        }

        dirty = false;
    }
}
