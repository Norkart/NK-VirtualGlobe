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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Data binding for Collada <bind_material> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class BindMaterial {
    
    /** material instance objects */
    InstanceMaterial[] instance;
    
    /** material instance elements */
    Element[] element;
    
    /**
     * Constructor
     * 
     * @param bind_material_element The Element
     */
    BindMaterial(Element bind_material_element) {
        
        if (bind_material_element == null) {
            //throw new IllegalArgumentException( 
            //	"BindMaterial: bind_material_element must be non-null");
            instance = new InstanceMaterial[0];
            
        } else {
            if (!bind_material_element.getTagName().equals(ColladaStrings.BIND_MATERIAL)) {
                throw new IllegalArgumentException( 
                    "BindMaterial: bind_material_element must be a <sbind_material> Element" );
            }
            NodeList nl = bind_material_element.getElementsByTagName(ColladaStrings.TECHNIQUE_COMMON);
            Element technique_common_element = (Element)nl.item(0);
            nl = technique_common_element.getElementsByTagName(ColladaStrings.INSTANCE_MATERIAL);
            int num_instance_material = nl.getLength();
            instance = new InstanceMaterial[num_instance_material];
            element = new Element[num_instance_material];
            for (int i = 0; i < num_instance_material; i++) {
                Element instance_material_element = (Element)nl.item(i);
                element[i] = instance_material_element;
                instance[i] = new InstanceMaterial(instance_material_element);
            } 
        }
    }
    
    /**
     * Return the targeted <instance_material> Element for the requested symbol.
     *
     * @param symbol The material instance symbol to match
     * @return target The targeted <instance_material> Element, or null if no match is found.
     */
    Element getTarget(String symbol) {
        Element target = null;
        for (int i = 0; i < instance.length; i++) {
            if (instance[i].symbol.equals(symbol)) {
                target = element[i];
                break;
            }
        }
        return(target);
    }
}
