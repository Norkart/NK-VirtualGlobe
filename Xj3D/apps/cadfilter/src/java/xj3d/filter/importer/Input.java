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
import java.util.Arrays;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Data binding for the Collada <input> element.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class Input {
    
    /** offset attribute */
    int offset;
    
    /** semantic attribute */
    String semantic;
    
    /** source attribute */
    String source;
    
    /** set attribute */
    int set;
    
    /**
     * Constructor
     * 
     * @param input_element The Element
     */
    Input(Element input_element) {
        
        if (input_element == null) {
            throw new IllegalArgumentException( 
                "Input: input_element must be non-null");
            
        } else if (!input_element.getTagName().equals(ColladaStrings.INPUT)) {
            throw new IllegalArgumentException( 
                "Input: input_element must be an <input> Element" );
        }
        String value = input_element.getAttribute(ColladaStrings.OFFSET);
        offset = (value.equals("")) ? 0 : Integer.parseInt(value);
        
        semantic = input_element.getAttribute(ColladaStrings.SEMANTIC);
        
        source = input_element.getAttribute(ColladaStrings.SOURCE);
        
        value = input_element.getAttribute(ColladaStrings.SET);
        set = (value.equals("")) ? -1 : Integer.parseInt(value);
    }
    
    /**
     * Return the number of unique offsets contained in the array of
     * Input objects.
     *
     * @param input An array of Input objects.
     * @return The number of unique offsets.
     */
    static int getNumberOfOffsets(Input[] input) {
        int num_offsets = 0;
        int num_inputs = input.length;
        if ( num_inputs > 1 ) {
            int max_index = 0;
            for (int i = 0; i < input.length; i++) {
                if (input[i].offset > max_index) {
                    max_index = input[i].offset;
                }
            }
            num_offsets = max_index + 1;
        } else {
            num_offsets = num_inputs;
        }
        return(num_offsets);
    }
    
    /**
     * Return the Input object from the array that matches the
     * requested semantic attribute
     *
     * @param input An array of Input objects.
     * @param semantic The semantic attribute to match
     * @return The requested Input object, or null if it could not be found.
     */
    static Input getInput(Input[] input, String semantic) {
        Input rval = null;
        for (int i = 0; i < input.length; i++) {
            if (input[i].semantic.equals(semantic)) {
                rval = input[i];
                break;
            }
        }
        return(rval);
    }
    
    /**
     * Return the set of Input objects contained in the NodeList
     *
     * @param input_list A NodeList of <input> Elements
     * @return The array of Input objects corresponding to the argument list
     */
    static Input[] getInputs(NodeList input_list) {
        int num_inputs = input_list.getLength();
        Input[] input = new Input[num_inputs];
        for (int i = 0; i < num_inputs; i++) {
            Element input_element = (Element)input_list.item(i);
            input[i] = new Input(input_element);
        }
        return(input);
    }
}
