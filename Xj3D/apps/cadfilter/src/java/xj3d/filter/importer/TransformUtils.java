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
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import org.w3c.dom.Element;

/**
 * Utility methods for handling TransformElements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
abstract class TransformUtils {
    
    /**
     * Return the data bound objects for the transform elements
     *
     * @param transform_list A list containing collada node transform elements
     * @return the data bound object for each transform element 
     */
    static TransformElement[] getTransformElements(ArrayList<Element> transform_list) {
        int num = transform_list.size();
        TransformElement[] te = new TransformElement[num];
        for (int i = 0; i < num; i++) {
            Element element = transform_list.get(i);
            String name = element.getTagName();
            if (name.equals(ColladaStrings.TRANSLATE)) {
                te[i] = new Translate(element);
            } else if (name.equals(ColladaStrings.ROTATE)) {
                te[i] = new Rotate(element);
            } else if (name.equals(ColladaStrings.SCALE)) {
                te[i] = new Scale(element);
            } else if (name.equals(ColladaStrings.SKEW)) {
                te[i] = new Skew(element);
            } else if (name.equals(ColladaStrings.MATRIX)) {
                te[i] = new Matrix(element);
            } else if (name.equals(ColladaStrings.LOOKAT)) {
                te[i] = new Lookat(element);
            }
        }
        return(te);
    }
    
    /**
     * Consolidated the array of transform elements into the argument matrix
     *
     * @param te The array of TransformElements 
     */
    static void getMatrix(TransformElement[] te, Matrix4f matrix) {
        Matrix4f m = new Matrix4f();
        matrix.setIdentity();
        for (int i = 0; i < te.length; i++) {
            te[i].getMatrix(m);
            matrix.mul(m);
        }
    }
}
