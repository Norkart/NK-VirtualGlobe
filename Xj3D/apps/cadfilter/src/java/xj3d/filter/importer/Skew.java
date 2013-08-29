/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.w3c.dom.Element;

/**
 * Data binding for Collada <skew> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Skew extends TransformElement {
    
    /**
     * Constructor
     * 
     * @param skew_element The Element
     */
    Skew(Element skew_element) {
        super("skew", skew_element);
    }
    
    /**
     * Return the value of this transform element in the argument Matrix
     * 
     * @return the value of this transform element in the argument Matrix
     */
    void getMatrix(Matrix4f matrix) {
        /////////////////////////////////////////////////////////////////////////////////////
        // adapted from:
        // http://csourcesearch.net/package/aqsis/1.0.0/aqsis-1.0.0/libaqsistypes/matrix.cpp
        /////////////////////////////////////////////////////////////////////////////////////
        float angle = value[0];
        // Normalize the two vectors, and construct a third perpendicular
        Vector3f d1 = new Vector3f(value[1], value[2], value[3]);
        d1.normalize();
        Vector3f d2 = new Vector3f(value[4], value[5], value[6]);
        d2.normalize();
        
        float d1d2dot = d1.dot(d2);
        float axisangle = (float)Math.acos(d1d2dot);
        if (angle >= axisangle || angle <= (axisangle - Math.PI)) {
            // Skewed past the axes
            matrix.setIdentity();
            
        } else {
            Vector3f right = new Vector3f();
            right.cross(d1, d2);
            right.normalize();
            
            // d1ortho will be perpendicular to <d2> and <right> and can be
            // used to construct a rotation matrix
            Vector3f d1ortho = new Vector3f();
            d1ortho.cross(d2, right);
            
            // 1) Rotate to a space where the skew operation is in a major plane.
            // 2) Bend the y axis towards the z axis causing a skew.
            // 3) Rotate back.
            Matrix4f rot = new Matrix4f( 
                right.x, d1ortho.x, d2.x, 0,
                right.y, d1ortho.y, d2.y, 0,
                right.z, d1ortho.z, d2.z, 0,
                0, 0, 0, 1);
            
            float par = d1d2dot;               // Amount of d1 parallel to d2
            float perp = (float)Math.sqrt(1 - par * par);   // Amount perpendicular
            float s = (float)Math.tan(angle + Math.acos(perp)) * perp - par;
            Matrix4f skew = new Matrix4f( 
                1, 0, 0, 0,
                0, 1, s, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
            // Note the Inverse of a rotation matrix is it's Transpose.
            matrix.mulTransposeLeft(rot, skew);
            matrix.mul(rot);
        }
    }
}
