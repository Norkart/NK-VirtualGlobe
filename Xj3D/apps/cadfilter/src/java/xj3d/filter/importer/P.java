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
 * Data binding for Collada <p> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
class P {
    
    /** The content */
    String content;
    
    /** String array version of the content */
    private String[] p_indices;
    
    /**
     * Constructor
     * 
     * @param p_element The Element
     */
    P(Element p_element) {
        
        if (p_element == null) {
            throw new IllegalArgumentException( 
                "P: p_element must be non-null");
            
        } else if (!p_element.getTagName().equals("p")) {
            throw new IllegalArgumentException( 
                "P: p_element must be a <p> Element" );
        }
        content = p_element.getTextContent();
    }
    
    /**
     * Return indices for <triangles>.
     *
     * @param num_triangles The number of triangles
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getTrianglesIndices(int num_triangles, int initial_offset, int index_offset, boolean binary) {
        int num_indices = num_triangles * 3;
        String[] indices = new String[num_indices];
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int p_index = initial_offset;
        for (int i = 0; i < num_indices; i++) {
            indices[i] = p_indices[p_index];
            p_index += index_offset;
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <triangles> that will
     * be transformed to IndexedFaceSets.
     *
     * @param num_triangles The number of triangles
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getPolyIndices(int num_triangles, int initial_offset, int index_offset, boolean binary) {
        int num_indices = num_triangles * 4;
        String[] indices = new String[num_indices];
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int p_index = initial_offset;
        for (int i = 0; i < num_indices;) {
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = "-1";
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <trifans> that will
     * be transformed to IndexedFaceSets.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getPolyIndicesForTrifan(int initial_offset, int index_offset, boolean binary) {
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int num_triangles = (p_indices.length / index_offset) - 2;
        int num_indices = num_triangles * 4;
        String[] indices = new String[num_indices];
        int center = initial_offset;
        int v_index = initial_offset + index_offset;
        int index = 0;
        for (int i = 0; i < num_triangles; i++) {
            indices[index++] = p_indices[center];
            indices[index++] = p_indices[v_index];
            v_index += index_offset;
            indices[index++] = p_indices[v_index];
            indices[index++] = "-1";
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <tristrips> that will
     * be transformed to IndexedFaceSets.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getPolyIndicesForTristrip(int initial_offset, int index_offset, boolean binary) {
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int num_triangles = (p_indices.length / index_offset) - 2;
        int num_indices = num_triangles * 4;
        String[] indices = new String[num_indices];
        int p_index = initial_offset;
        int index = 0;
        boolean even = true;
        for (int i = 0; i < num_triangles; i++) {
            if (even) {
                indices[index++] = p_indices[p_index];
                p_index += index_offset;
                indices[index++] = p_indices[p_index];
                indices[index++] = p_indices[p_index+index_offset];
            } else {
                indices[index++] = p_indices[p_index+index_offset];
                indices[index++] = p_indices[p_index];
                p_index += index_offset;
                indices[index++] = p_indices[p_index+index_offset];
            }
            indices[index++] = "-1";
            even = !even;
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <polylist>.
     *
     * @param v The Vcount object, containg poly vertex information
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getPolyIndices(Vcount v, int initial_offset, int index_offset, boolean binary) {
        int num_polys = v.getNumPolys();
        int num_indices = v.getNumVertices() + num_polys;
        String[] indices = new String[num_indices];
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int[] num_vertices_per_poly = v.getVerticesPerPoly();
        int v_index = 0;
        int p_index = initial_offset;
        for (int i = 0; i < num_vertices_per_poly.length; i++) {
            int num_vertices = num_vertices_per_poly[i];
            for (int j = 0; j < num_vertices; j++) {
                indices[v_index++] = p_indices[p_index];
                p_index += index_offset;
            }
            indices[v_index++] = "-1";
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <lines>.
     *
     * @param num_lines The number of lines
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getLinesIndices(int num_lines, int initial_offset, int index_offset, boolean binary) {
        int num_indices = num_lines * 3;
        String[] indices = new String[num_indices];
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int p_index = initial_offset;
        for (int i = 0; i < num_indices;) {
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = "-1";
        }
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
    
    /**
     * Return indices for <linestrips>, <polygons>
     * <trifans> and <tristrips>.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @param binary true if a int array is required, false for a String array.
     * @return The array of indices, either an int array or a String array.
     */
    Object getIndices(int initial_offset, int index_offset, boolean binary) {
        if (p_indices == null) {
            p_indices = FieldValueHandler.split(content);
        }
        int num_indices = p_indices.length / index_offset;
        String[] indices = new String[num_indices + 1];
        int p_index = initial_offset;
        for (int i = 0; i < num_indices; i++) {
            indices[i] = p_indices[p_index];
            p_index += index_offset;
        }
        indices[num_indices] = "-1";
        if (binary) {
            return(FieldValueHandler.toInt(indices));
        } else {
            return(indices);
        }
    }
}
