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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Data binding for Collada <sampler> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Sampler {
    
    /** id attribute */
    String id;
    
    /** The array of input elements */
    Input[] input;
    
    /** The number of input elements */
    int num_inputs;
    
    /**
     * Constructor
     * 
     * @param sampler_element The Element
     */
    Sampler(Element sampler_element) {
        
        if (sampler_element == null) {
            throw new IllegalArgumentException( 
                "Sampler: sampler_element must be non-null");
            
        } else if (!sampler_element.getTagName().equals(ColladaStrings.SAMPLER)) {
            throw new IllegalArgumentException( 
                "Sampler: sampler_element must be an <sampler> Element" );
        }
        id = sampler_element.getAttribute(ColladaStrings.ID);
        
        NodeList input_list = sampler_element.getElementsByTagName(ColladaStrings.INPUT);
        input = Input.getInputs(input_list);
        num_inputs = input.length;
    }
    
    /**
     * Return a Map of Sampler objects contained in the NodeList,
     * key'ed by id.
     *
     * @param sampler_list A NodeList of <sampler> Elements
     * @return A Map of Sampler objects corresponding to the argument list
     */
    static Map<String, Sampler> getSamplerMap(NodeList sampler_list) {
        int num_samplers = sampler_list.getLength();
        HashMap<String, Sampler> map = new HashMap<String, Sampler>();
        for (int i = 0; i < num_samplers; i++) {
            Element sampler_element = (Element)sampler_list.item(i);
            Sampler sampler = new Sampler(sampler_element);
            map.put(sampler.id, sampler);
        }
        return(map);
    }
}
