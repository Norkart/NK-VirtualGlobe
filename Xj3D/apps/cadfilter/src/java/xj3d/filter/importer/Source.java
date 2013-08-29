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

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Data binding for Collada <source> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
class Source {
    
    /** id attribute */
    String id;
    
    /** name attribute */
    String name;
    
    /** array object */
    Array array;
    
    /** accessor object */
    Accessor accessor;
    
    /**
     * Constructor
     * 
     * @param source_element The Element
     */
    Source(Element source_element) {
        
        if (source_element == null) {
            throw new IllegalArgumentException( 
                "Source: source_element must be non-null");
            
        } else if (!source_element.getTagName().equals(ColladaStrings.SOURCE)) {
            throw new IllegalArgumentException( 
                "Source: source_element must be a <source> Element" );
        }
        id = source_element.getAttribute(ColladaStrings.ID);
        name = source_element.getAttribute(ColladaStrings.NAME);
        
        ArrayList<Element> children = ImportUtils.getElements(source_element);
        
        // extract the content we'll use, ignore <asset> & <technique>
        for (int i = 0; i < children.size(); i++) {
            Element element = children.get(i);
            String tagName = element.getTagName();
            if (tagName.endsWith("_array")) {
                // array element is required
                array = new Array(element);
                
            } else if (tagName.equals(ColladaStrings.TECHNIQUE_COMMON)) {
                // if technique common is present, accessor is required
                Element accessor_element = ImportUtils.getFirstElement(element);
                
                String accessor_source = 
                    accessor_element.getAttribute(ColladaStrings.SOURCE);
                if (accessor_source.startsWith("#")) {
                    accessor_source = accessor_source.substring(1);
                }
                if (!accessor_source.equals(array.id)) {
                    // don't know how to source coordinates that are not contained
                    // in the local array.....
                    throw new IllegalArgumentException( 
                        "Source: <accessor> source attribute "+ 
                        accessor_source +" cannot be accessed");
                }
                accessor = new Accessor(accessor_element);
            }
        }
    }
    
    /**
     * Return the data from the argument source element in the order and form specified.
     *
     * @param binary true if a primative array is required, false for a String.
     * @return The source data, either a primative or String array 
     */
    Object getSourceData(boolean binary) {

        // if the accessor is null, this will throw an NPE....
        int num_params = accessor.num_params;
        boolean[] valid = new boolean[num_params];
        int num_valid = 0;
        for (int i = 0; i < num_params; i++) {
            valid[i] = accessor.param[i].isNamed;
            if (valid[i]) {
                num_valid++;
            }
        }
        
        Object rval = null;
        if ((num_valid == num_params) && (accessor.offset == 0) && (accessor.stride == num_params)) {
            // the array content is good to go
            if (binary) {
                switch (array.type_identifier) {
                case Array.FLOAT:
                    rval = FieldValueHandler.toFloat(array.content);
                    break;
                case Array.INT:
                    rval = FieldValueHandler.toInt(array.content);
                    break;
                case Array.NAME:
                    rval = FieldValueHandler.split(array.content);
                    break;
                case Array.IDREF:
                    rval = FieldValueHandler.split(array.content);
                    break;
                case Array.BOOL:
                    // TODO, FieldValueHandler method(s) for boolean
                    break;
                }	
            } else {
                rval = FieldValueHandler.split(array.content);
            }
        } else {
            // the array content needs some processing
            String[] src = FieldValueHandler.split(array.content);
            int src_index = accessor.offset;
            String[] dst = new String[accessor.count*num_valid];
            int dst_index = 0;
            for (int i = 0; i < accessor.count; i++) {
                for (int j = 0; j < num_params; j++) {
                    if (valid[j]) {
                        dst[dst_index++] = src[src_index + j];
                    }
                }
                src_index += accessor.stride;
            }
            if (binary) {
                switch (array.type_identifier) {
                case Array.FLOAT:
                    rval = FieldValueHandler.toFloat(dst);
                    break;
                case Array.INT:
                    rval = FieldValueHandler.toInt(dst);
                    break;
                case Array.NAME:
                    rval = dst;
                    break;
                case Array.IDREF:
                    rval = dst;
                    break;
                case Array.BOOL:
                    // TODO, FieldValueHandler method(s) for boolean
                    break;
                }	
            } else {
                rval = dst;
            }
        }
        return(rval);
    }
    
    /**
     * Return a Map of Source objects contained in the NodeList,
     * key'ed by id.
     *
     * @param source_list A NodeList of <source> Elements
     * @return A Map of Source objects corresponding to the argument list
     */
    static Map<String, Source> getSourceMap(NodeList source_list) {
        int num_sources = source_list.getLength();
        HashMap<String, Source> map = new HashMap<String, Source>();
        for (int i = 0; i < num_sources; i++) {
            Element source_element = (Element)source_list.item(i);
            Source source = new Source(source_element);
            map.put(source.id, source);
        }
        return(map);
    }
}
