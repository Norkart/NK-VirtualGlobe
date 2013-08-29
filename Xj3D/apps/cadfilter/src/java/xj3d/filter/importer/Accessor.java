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

import org.w3c.dom.Element;

/**
 * Data binding for Collada <accessor> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Accessor {
    
    /** The number of times the source array is accessed */
    int count;
    
    /** Index of the first value in the source array */
    int offset;
    
    /** The source location */
    String source;
    
    /** The number of array values that form a coordinate */
    int stride;
    
    /** The array of parameter elements */
    Param[] param;
    
    /** The number of param elements */
    int num_params;
    
    /**
     * Constructor
     * 
     * @param accessor_element The Element
     */
    Accessor(Element accessor_element) {
        
        if (accessor_element == null) {
            throw new IllegalArgumentException( 
                "Accessor: accessor_element must be non-null");
            
        } else if (!accessor_element.getTagName().equals(ColladaStrings.ACCESSOR)) {
            throw new IllegalArgumentException( 
                "Accessor: accessor_element must be an <accessor> Element" );
        }
        
        String value = accessor_element.getAttribute(ColladaStrings.COUNT);
        count = Integer.parseInt(value);
        
        value = accessor_element.getAttribute(ColladaStrings.OFFSET);
        offset = (value.equals("")) ? 0 : Integer.parseInt(value);
        
        source = accessor_element.getAttribute(ColladaStrings.SOURCE);
        
        value = accessor_element.getAttribute(ColladaStrings.STRIDE);
        stride = (value.equals("")) ? 1 : Integer.parseInt(value);
        
        ArrayList<Element> param_element_list = 
            ImportUtils.getElements(accessor_element);
        num_params = param_element_list.size();
        param = new Param[num_params];
        for (int i = 0; i < num_params; i++) {
            param[i] = new Param(param_element_list.get(i));
        }
    }
}
