/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.util.Arrays;

import javax.vecmath.Point3f;

// Local imports
import org.web3d.util.IntArray;
import org.web3d.util.IntHashMap;

/**
 * Package level class that will compact a coordinate array to 
 * eliminate redundant coordinate points and reconfigure an
 * associated index array to account for the changed coordinate
 * array. Used with the ReindexFilter.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class CoordinateProcessor {
    
    /** epsilon for comparing coordinate points */
    private static final float EPSILON = 0.0001f;
    
    /** Point data from a Coordinate node */
    private float[] coord_point;
    
    /** The number of valid (non-redundant) coordinate points in the array */
    private int num_coords;
    
    /** Flag indicating that the original array had duplicates */
    private boolean has_duplicates;
    
    /** Array of indices into the coord_point array that have been 
    * determined to contain redundant coordinates */
    private IntArray dup_indices;
    
    /** Ordered version of dup_indices */
    private int[] dup;
    
    /** 
     * Map of coord_point indices from original array. 
     * key = index of initial occurance of a coordinate that is repeated.
     * value = IntArray, containing subsequent indices of the equivalent coordinate
     */
    private IntHashMap indexMap;
    
    /**
     * Default Constructor
     */
    CoordinateProcessor(float[] coord_point) {
        
        Point3f p0 = new Point3f();
        Point3f p1 = new Point3f();
        
        dup_indices = new IntArray();
        indexMap = new IntHashMap();
        
        this.coord_point = coord_point;
        
        num_coords = coord_point.length/3;
        
        // walk through the coord_point array and extract the indices of the
        // coordinates that are redundant (dup_indices) and a map keyed by
        // initial occurance of the coordinate in the array of the redundant
        // coordinate indices.
        int i = 0;
        for (int idx = 0; idx < num_coords; idx++) {
            if (!isDuplicate(idx)) {
                i = idx*3;
                p0.set(coord_point[i], coord_point[i+1], coord_point[i+2]);
                
                for (int next = idx+1; next < num_coords; next++) {
                    if (!isDuplicate(next)) {
                        i = next*3;
                        p1.set(coord_point[i], coord_point[i+1], coord_point[i+2]);
                        if (p0.epsilonEquals(p1, EPSILON)) {
                            dup_indices.add(next);
                            // aggregate the indices of redundant coordinates from the
                            // coord_point array into an IntArray - stored in a map keyed
                            // by the index of the first occurance of that coordinate.
                            // the redundant indices (in the index array) will eventually 
                            // be replaced by the key
                            IntArray dup_set = (IntArray)indexMap.get(idx);
                            if (dup_set == null) {
                                dup_set = new IntArray();
                                indexMap.put(idx, dup_set);
                            }
                            dup_set.add(next);
                        }
                    }
                }
            }
        }
        has_duplicates = (dup_indices.size() > 0);
        if (has_duplicates) {
            // there were duplicates 
            dup = dup_indices.toArray();
            // get a sequential ordering of the duplicate indices
            Arrays.sort(dup);
            int coord_idx = 0;
            int dup_idx = 0;
            // compact the coordinate array to overwrite redundant points
            for (i = 0; i < num_coords; i++) {
                if ((dup_idx < dup.length) && (i == dup[dup_idx])) {
                    dup_idx++;
                } else {
                    if (i != coord_idx) {
                        int dst_coord_idx = coord_idx*3;
                        int src_coord_idx = i*3;
                        coord_point[dst_coord_idx] = coord_point[src_coord_idx];
                        coord_point[dst_coord_idx+1] = coord_point[src_coord_idx+1];
                        coord_point[dst_coord_idx+2] = coord_point[src_coord_idx+2];
                    }
                    coord_idx++;
                }
            }
            num_coords -= dup.length;
        }
    }
    
    /**
     * Return whether the coordinate array has been compacted
     *
     * @return whether the coordinate array has been compacted
     */
    boolean hasDuplicates() {
        return(has_duplicates);
    }
    
    /**
     * Return the number of non-redundant coordinate points in the array
     *
     * @return the number of non-redundant coordinate points in the array
     */
    int getNumCoords() {
        return(num_coords);
    }
    
    /**
     * Return the ordered array of duplicate indices 
     *
     * @return the ordered array of duplicate indices 
     */
    int[] getDuplicateIndices() {
        return(dup);
    }
    
    /**
     * Return the duplicate index mapping
     *
     * @return the duplicate index mapping
     */
    IntHashMap getMap() {
        return(indexMap);
    }
    
    /**
     * Rebuild the indices in the argument array to coorespond to the 
     * compacted coordinate point array.
     *
     * @param index An array of indices into the coordinate point array
     * @return true if the array has been modified, false if it remains unchanged
     */
    boolean processIndices(int[] index) {
        if (hasDuplicates()) {
            // there were duplicates 
            
            // the set of indices that will be used to replace indices for
            // redundant coordinate points
            int[] key_idx = indexMap.keySet();
            
            // the redundant indices that will be replaced
            int[][] redundant_idx = new int[key_idx.length][];
            
            for (int i = 0; i < key_idx.length; i++) {
                redundant_idx[i] = ((IntArray)indexMap.get(key_idx[i])).toArray();
            }
            // walk through the index array
            for (int i = 0; i < index.length; i++) {
                // replace redundant values with their keyed value
                int index_value = index[i];
                for (int j = 0; j < key_idx.length; j++) {
                    int replacement_value = key_idx[j];
                    if (index_value != replacement_value) {
                        boolean replaced = false;
                        int[] red = redundant_idx[j];
                        for (int k = 0; k < red.length; k++) {
                            if (index_value == red[k]) {
                                index_value = replacement_value;
                                replaced = true;
                                break;
                            }
                        }
                        if (replaced) {
                            break;
                        }
                    }
                }
                // recalculate indices that may have changed due to the removal
                // of redundant coordinates from the coord_point array
                int n = 0;
                for ( ; n < dup.length; n++) {
                    if (index_value < dup[n]) {
                        break;
                    }
                }
                index[i] = index_value - n;
            }
            return(true);
        } else {
            return(false);
        }
    }
    
    /**
     * Return whether the argument index is contained in the duplicate 
     * index array.
     *
     * @param index The index to search the duplicate index array for.
     * @return true if the index is contained in the array, false if not.
     */
    private boolean isDuplicate(int index) {
        boolean isDuplicate = false;
        int num = dup_indices.size();
        for (int i = 0; i < num; i++) {
            if (index == dup_indices.get(i)) {
                isDuplicate = true;
                break;
            }
        }
        return(isDuplicate);
    }
}
