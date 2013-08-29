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

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Data binding for Collada <vcount> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class Vcount {
    
    /** The content */
    String content;
    
    /** String array version of the content */
    private int[] num_vertices_per_poly;
    
    /**
     * Constructor
     * 
     * @param vcount_element The Element
     */
    Vcount(Element vcount_element) {
        
        if (vcount_element == null) {
            throw new IllegalArgumentException( 
                "Vcount: vcount_element must be non-null");
            
        } else if (!vcount_element.getTagName().equals("vcount")) {
            throw new IllegalArgumentException( 
                "Vcount: vcount_element must be a <vcount> Element" );
        }
        content = vcount_element.getTextContent();
    }
    
    /**
     * Return the array containing the number of vertices per polygon
     *
     * @return the array containing the number of vertices per polygon
     */
    int[] getVerticesPerPoly() {
        if (num_vertices_per_poly == null) {
            String[] s = FieldValueHandler.split(content);
            num_vertices_per_poly = FieldValueHandler.toInt(s);
        }
        return(num_vertices_per_poly);
    }
    
    /**
     * Return the number of polygons described by this
     *
     * @return the number of polygons described by this
     */
    int getNumPolys() {
        if (num_vertices_per_poly == null) {
            String[] s = FieldValueHandler.split(content);
            num_vertices_per_poly = FieldValueHandler.toInt(s);
        }
        return(num_vertices_per_poly.length);
    }
    
    /**
     * Return the total number of vertices described by this
     *
     * @return the total number of vertices described by this
     */
    int getNumVertices() {
        if (num_vertices_per_poly == null) {
            String[] s = FieldValueHandler.split(content);
            num_vertices_per_poly = FieldValueHandler.toInt(s);
        }
        int total = 0;
        for (int i = 0; i < num_vertices_per_poly.length; i++) {
            total += num_vertices_per_poly[i];
        }
        return(total);
    }
}
