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
 * Data binding for Collada <*_array> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class Array {
    
    /** identifiers of the array content type */
    static final int BOOL = 0;
    static final int FLOAT = 1;
    static final int INT = 2;
    static final int NAME = 3;
    static final int IDREF = 4;
    
    /** id attribute */
    String id;
    
    /** name attribute */
    String name;
    
    /** The content */
    String content;
    
    /** the number of values in the content */
    int count;
    
    /** the type, "float", "int", "boolean", etc. */
    String type;
    
    /** the type identifier */
    int type_identifier;
    
    /**
     * Constructor
     * 
     * @param array_element The Element
     */
    Array(Element array_element) {
        
        if (array_element == null) {
            throw new IllegalArgumentException( 
                "Array: array_element must be non-null");
            
        } else if (!array_element.getTagName().endsWith("_array")) {
            throw new IllegalArgumentException( 
                "Array: array_element must be an <*_array> Element" );
        }
        id = array_element.getAttribute(ColladaStrings.ID);
        name = array_element.getAttribute(ColladaStrings.NAME);
        content = array_element.getTextContent();
        
        String value = array_element.getAttribute(ColladaStrings.COUNT);
        count = Integer.parseInt(value);
        
        String tagName = array_element.getTagName();
        int idx = tagName.indexOf("_");
        type = tagName.substring(0, idx);
        
        if (type.equals("bool")) {
            type_identifier = BOOL;
        } else if (type.equals("float")) {
            type_identifier = FLOAT;
        } else if (type.equals("int")) {
            type_identifier = INT;
        } else if (type.equals("Name")) {
            type_identifier = NAME;
        } else if (type.equals("IDREF")) {
            type_identifier = IDREF;
        } else {
            // shouldn't happen since these are the valid known types.
            // will probably cause an exception eventually down the line if it does.
            type_identifier = -1;
        }
    }
}
