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
 * Data binding for Collada <instance_material> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class InstanceMaterial {
    
    /** sid attribute */
    String sid;
    
    /** name attribute */
    String name;
    
    /** target attribute */
    String target;
    
    /** symbol attribute */
    String symbol;
    
    /**
     * Constructor
     * 
     * @param instance_material_element The Element
     */
    InstanceMaterial(Element instance_material_element) {
        
        if (instance_material_element == null) {
            throw new IllegalArgumentException( 
                "InstanceMaterial: instance_material_element must be non-null");
            
        } else if (!instance_material_element.getTagName().equals(ColladaStrings.INSTANCE_MATERIAL)) {
            throw new IllegalArgumentException( 
                "InstanceMaterial: instance_material_element must be a <instance_material> Element" );
        }
        sid = instance_material_element.getAttribute(ColladaStrings.SID);
        name = instance_material_element.getAttribute(ColladaStrings.NAME);
        target = instance_material_element.getAttribute(ColladaStrings.TARGET);
        symbol = instance_material_element.getAttribute(ColladaStrings.SYMBOL);
    }
}
