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
import org.w3c.dom.Element;

/**
 * Data binding for Collada <lookat> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Lookat extends TransformElement {
    
    /**
     * Constructor
     * 
     * @param lookat_element The Element
     */
    Lookat(Element lookat_element) {
        super("lookat", lookat_element);
    }
}
