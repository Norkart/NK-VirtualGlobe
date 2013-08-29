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

package xj3d.filter.node;

// External imports

// Local imports

/**
 * Utility class for converting geometry nodes to different representations
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class GeometryConverter {

    /** Node wrapper factory */
    protected EncodableFactory factory;
    
    /**
     * Constructor
     *
     * @param factory The node wrapper factory
     */
    public GeometryConverter(EncodableFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Convert an ITFS to an ITS
     *
     * @param itfs The IndexedTriangleFanSet to convert
     * @return The cooresponding IndexedTriangleSet
     */
    public IndexedTriangleSet toITS(IndexedTriangleFanSet itfs) {
        int[] index = itfs.index;
        int num_index = itfs.num_index;
        int num_fans = 0;
        int num_delimiters = 0;
        int last_index = num_index - 1;
        for (int i = 0; i < num_index; i++) {
            if (index[i] == -1) {
                num_fans++;
                num_delimiters++;
            }
            if ((i == last_index) && (index[i] != -1)) {
                num_fans++;
            }
        }
        // magic to determine the number of triangles
        int num_triangles = (num_index - num_delimiters) - (2 * num_fans);
        int num_its_index = num_triangles * 3;
        int[] its_index = new int[num_its_index];
        
        int center = 0;
        int fan_idx = 1;
        int idx = 0;
        for (int i = 0; i < num_triangles; i++) {
            its_index[idx++] = index[center];
            its_index[idx++] = index[fan_idx++];
            its_index[idx++] = index[fan_idx];
            int next = fan_idx + 1;
            if ((next != num_index) && (index[next] == -1)) {
                // at the end of a fan, reset the working itfs indices
                center = next + 1;
                fan_idx = next + 2;
            }
        }
        // clone the itfs into the its, replace the index
        IndexedTriangleSet its = 
            (IndexedTriangleSet)factory.getEncodable("IndexedTriangleSet", null);
        itfs.copy(its);
        its.index = its_index;
        its.num_index = num_its_index;
        
        return(its);
    }
    
    /**
     * Convert an ITSS to an ITS
     *
     * @param itss The IndexedTriangleStripSet to convert
     * @return The cooresponding IndexedTriangleSet
     */
    public IndexedTriangleSet toITS(IndexedTriangleStripSet itss) {
        int[] index = itss.index;
        int num_index = itss.num_index;
        int num_strips = 0;
        int num_delimiters = 0;
        int last_index = num_index - 1;
        for (int i = 0; i < num_index; i++) {
            if (index[i] == -1) {
                num_strips++;
                num_delimiters++;
            }
            if ((i == last_index) && (index[i] != -1)) {
                num_strips++;
            }
        }
        // magic to determine the number of triangles
        int num_triangles = (num_index - num_delimiters) - (2 * num_strips);
        int num_its_index = num_triangles * 3;
        int[] its_index = new int[num_its_index];
        
        int idx = 0;
        int strip_idx = 0;
        boolean even = true;
        for (int i = 0; i < num_triangles; i++) {
            if (even) {
                its_index[idx++] = index[strip_idx++];
                its_index[idx++] = index[strip_idx];
                its_index[idx++] = index[strip_idx+1];
            } else {
                its_index[idx++] = index[strip_idx+1];
                its_index[idx++] = index[strip_idx++];
                its_index[idx++] = index[strip_idx+1];
            }
            int next = strip_idx + 2;
            if ((next != num_index) && (index[next] == -1)) {
                // at the end of a strip, reset the working itss indices
                strip_idx += 3;
                even = true;
            } else {
                even = !even;
            }
        }
        // clone the itss into the its, replace the index
        IndexedTriangleSet its = 
            (IndexedTriangleSet)factory.getEncodable("IndexedTriangleSet", null);
        itss.copy(its);
        its.index = its_index;
        its.num_index = num_its_index;
        
        return(its);
    }
    
    /**
     * Convert a TS to an ITS
     *
     * @param ts The TriangleSet to convert
     * @return The cooresponding IndexedTriangleSet
     */
    public IndexedTriangleSet toITS(TriangleSet ts) {
        int num_index = ts.getCoordinate().num_point;
        int[] index = new int[num_index];
        for (int i = 0; i < num_index; i++) {
            index[i] = i;
        }
        // clone the ts into the its, replace the index
        IndexedTriangleSet its = 
            (IndexedTriangleSet)factory.getEncodable("IndexedTriangleSet", null);
        ts.copy(its);
        its.index = index;
        its.num_index = num_index;
        
        return(its);
    }
    
    /**
     * Convert a TSS to an ITSS
     *
     * @param tss The TriangleStripSet to convert
     * @return The cooresponding IndexedTriangleStripSet
     */
    public IndexedTriangleStripSet toITSS(TriangleStripSet tss) {
        
        int num_strip = tss.num_strip;
        int num_index = num_strip; // each strip is terminated with a -1
        for (int i = 0; i < num_strip; i++) {
            num_index += tss.stripCount[i];
        }
        int[] index = new int[num_index];
        int idx = 0;
        int coord_idx = 0;
        for (int i = 0; i < num_strip; i++) {
            int strip_length = tss.stripCount[i];
            for (int j = 0; j < strip_length; j++) {
                index[idx++] = coord_idx++;
            }
            index[idx++] = -1;
        }
        // clone the ts into the its, replace the index
        IndexedTriangleStripSet itss = 
            (IndexedTriangleStripSet)factory.getEncodable("IndexedTriangleStripSet", null);
        tss.copy(itss);
        itss.index = index;
        itss.num_index = num_index;
        
        return(itss);
    }
    
    /**
     * Convert a TFS to an ITFS
     *
     * @param tfs The TriangleFanSet to convert
     * @return The cooresponding IndexedTriangleFanSet
     */
    public IndexedTriangleFanSet toITFS(TriangleFanSet tfs) {
        
        int num_fan = tfs.num_fan;
        int num_index = num_fan; // each strip is terminated with a -1
        for (int i = 0; i < num_fan; i++) {
            num_index += tfs.fanCount[i];
        }
        int[] index = new int[num_index];
        int idx = 0;
        int coord_idx = 0;
        for (int i = 0; i < num_fan; i++) {
            int fan_length = tfs.fanCount[i];
            for (int j = 0; j < fan_length; j++) {
                index[idx++] = coord_idx++;
            }
            index[idx++] = -1;
        }
        // clone the ts into the its, replace the index
        IndexedTriangleFanSet itfs = 
            (IndexedTriangleFanSet)factory.getEncodable("IndexedTriangleFanSet", null);
        tfs.copy(itfs);
        itfs.index = index;
        itfs.num_index = num_index;
        
        return(itfs);
    }
    
    /**
     * Convert the LS to an ILS
     *
     * @param ls The LineSet to convert
     * @return The cooresponding IndexedLineSet
     */
    /*
    private IndexedLineSet toILS(LineSet ls) {
            int num_line = ls.num_vertex;
            int num_index = num_line; // each line is terminated with a -1
            for (int i = 0; i < num_line; i++) {
                num_index += ls.vertexCount[i];
            }
            int[] index = new int[num_index];
            int idx = 0;
            int coord_idx = 0;
            for (int i = 0; i < num_line; i++) {
                int line_length = ls.vertexCount[i];
                for (int j = 0; j < line_length; j++) {
                    index[idx++] = coord_idx++;
                }
                index[idx++] = -1;
            }
            // clone the ls into the ils, replace the index
            IndexedLineSet ils = 
                (IndexedLineSet)factory.getEncodable("IndexedLineSet", null);
            ils.colorPerVertex = ls.colorPerVertex;
            ils.setCoordinate(ls.getCoordinate());
            ils.setColor(ls.getColor());
            ils.coordIndex = index;
            ils.num_coordIndex = num_index;
            ils.colorIndex = index;
            ils.num_colorIndex = num_index;
            
            return(ils);
        }
        */
}
