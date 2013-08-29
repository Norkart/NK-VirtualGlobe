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
 * Data binding for Collada <param> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Param {
    
    /** name attribute */
    String name;
    
    /** is there a name attribute */
    boolean isNamed;
    
    /** the type, "float", "int", "boolean", etc.) */
    String type;
    
    /**
     * Constructor
     * 
     * @param param_element The Element
     */
    Param(Element param_element) {
        
        if (param_element == null) {
            throw new IllegalArgumentException( 
                "Param: param_element must be non-null");
            
        } else if (!param_element.getTagName().equals(ColladaStrings.PARAM)) {
            throw new IllegalArgumentException( 
                "Param: param_element must be a <param> Element" );
        }
        name = param_element.getAttribute(ColladaStrings.NAME);
        isNamed = !name.equals("");
        type = param_element.getAttribute(ColladaStrings.TYPE);
    }
}
