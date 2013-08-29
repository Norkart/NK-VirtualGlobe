//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * IntegerArray.java
 *
 * Created on 11. september 2006, 10:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.globesurface;

import java.util.Random;

/**
 *
 * @author runaas
 */
public class IntegerArray {
    private static Random random = new Random();
    
    int [] array;
    int    size;
    int    rec_size;
    
    /** Creates a new instance of IntegerArray */
    public IntegerArray(int rec_size, int initial_size) {
        array = new int[initial_size*rec_size];
        size = 0;
        this.rec_size = rec_size;
    }
    
    public IntegerArray(int rec_size) {
        this(rec_size, 100);
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int size() {
        return size;
    }
    
    public void clear() {
        size = 0;
    }
    
    public void ensureCapacity(int max_size) {
        if (max_size*rec_size > array.length) {
            int [] tmp = array;
            array = new int[rec_size*max_size];
            System.arraycopy(tmp, 0, array, 0, size*rec_size);
        }
    }
    
    public void set(int ix, int [] value) {
        if (ix > size)
            throw new IndexOutOfBoundsException("Ix="+ix+" size="+size);
        if (ix == size) {
            ++size;
            if (size*rec_size >= array.length) {
                int [] tmp = array;
                array = new int[rec_size*(int)(size*1.5)];
                System.arraycopy(tmp, 0, array, 0, tmp.length);
            }
        }
        ix = ix*rec_size+(rec_size-1);
        for (int i=rec_size; --i >= 0; --ix)
            array[ix] = value[i];
    }
    
    public void set(int ix, int field, int value) {
        if (ix > size)
            throw new IndexOutOfBoundsException("Ix="+ix+" size="+size);
        if (field >= rec_size)
            throw new IndexOutOfBoundsException("Field="+field+" recsize="+rec_size);
        if (ix == size) {
            ++size;
            if (size*rec_size >= array.length) {
                int [] tmp = array;
                array = new int[rec_size*(int)(size*1.5)];
                System.arraycopy(tmp, 0, array, 0, tmp.length);
            }
        }
        array[ix*rec_size+field] = value;
    }
    
    public void get(int ix, int [] value) {
        if (ix >= size)
            throw new IndexOutOfBoundsException("Ix="+ix+" size="+size);
        
        ix = ix*rec_size+(rec_size-1);
        for (int i=rec_size; --i >= 0; --ix)
            value[i] = array[ix];
    }
    
    public int get(int ix, int field) {
        if (ix > size)
            throw new IndexOutOfBoundsException("Ix="+ix+" size="+size);
        if (field >= rec_size)
            throw new IndexOutOfBoundsException("Field="+field+" recsize="+rec_size);
        return array[ix*rec_size+field];
    }
    
    public void add(int [] value) {
        ++size;
        if (size*rec_size >= array.length) {
            int [] tmp = array;
            array = new int[rec_size*(int)(size*1.5)];
            System.arraycopy(tmp, 0, array, 0, tmp.length);
        }
        
        int ix = size-1;
        ix = ix*rec_size+(rec_size-1);
        for (int i=rec_size; --i >= 0; --ix)
            array[ix] = value[i];
    }
    
    public void remove(int ix) {
        if (ix < 0 || ix >= size)
            throw new IndexOutOfBoundsException("Ix="+ix+" size="+size);
        --size;
        if (ix < size)
            System.arraycopy(array, (ix+1)*rec_size, array, ix*rec_size, (size-ix)*rec_size);
    }
    
    public void removeRange(int fromIx, int toIx) {
        if (fromIx > toIx)
            throw new IllegalArgumentException("FromIx: " + fromIx + " larger than toIx: " + toIx);
        if (fromIx < 0)
            throw new IndexOutOfBoundsException("fromIx="+fromIx+" size="+size);
        if (toIx > size)
            throw new IndexOutOfBoundsException("toIx="+toIx+" size="+size);
        if (fromIx == toIx) return;
        if (toIx < size)
            System.arraycopy(array, toIx*rec_size, array, fromIx*rec_size, (size-toIx)*rec_size);
        size -= toIx - fromIx;
    }
    
    public void swap(int ix1, int ix2) {
        if (ix1 >= size)
            throw new IndexOutOfBoundsException("Ix1="+ix1+" size="+size);
        if (ix2 >= size)
            throw new IndexOutOfBoundsException("Ix1="+ix2+" size="+size);
        for (int k=rec_size; --k>=0;) {
            int tmp = array[ix1*rec_size+k];
            array[ix1*rec_size+k] = array[ix2*rec_size+k];
            array[ix2*rec_size+k] = tmp;
        }
        
    }
    
    public interface Comparator {
        int compare(int pos1, int [] v1, int pos2, int [] v2, int rec_size);
    }
    
    public void sort(Comparator comp) {
        int [] tmp = new int [rec_size];
        int [] pivot = new int[5];
        sort(0, size, comp, tmp, pivot);
        
        for (int i = 1; i < size; ++i) {
            int j = i - 1;
            get(i, tmp);
            while (j >= 0 && comp.compare(j, array, 0, tmp, rec_size) > 0) {
                for (int k=rec_size; --k>=0;)
                    array[(j + 1)*rec_size+k] = array[j*rec_size+k];
                --j;
            }
            set(j+1, tmp);
        }
        /*
        for (int i=0; i<size-1;++i)
            if (comp.compare(i, array, i+1, array, rec_size) > 0)
                System.err.println("Hælvete!");
         */
    }
    
    public void sort(int begin, int end, Comparator comp, int [] tmp, int [] pivot) {
        // If the array is small, use insertion sort
        if (end - begin < 7) {
            /*
            for (int i = begin + 1; i < end; ++i) {
                int j = i - 1;
                get(i, tmp);
                while (j >= begin && comp.compare(j, array, 0, tmp, rec_size) > 0) {
                    for (int k=rec_size; --k>=0;)
                        array[(j + 1)*rec_size+k] = array[j*rec_size+k];
                    --j;
                }
                set(j+1, tmp);
            }*/
            return;
        }
        
        // Quicksort
        // Find pivot
        int pivot_length = begin-end > 30 ? 5 : 3;
        for (int i=0; i < pivot_length; ++i)
            pivot[i]  = begin + random.nextInt(end-begin);
        for (int i=0; i<pivot_length; ++i) {
            for (int j=i+1; j < pivot_length; ++j) {
                if (comp.compare(pivot[j], array,  pivot[i], array, rec_size) < 0) {
                    int tmp_i = pivot[i];
                    pivot[i] = pivot[j];
                    pivot[j] = tmp_i;
                }
            }
        }
        get(pivot[pivot_length/2], tmp);
        
        // Partition
        int left = begin;
        int left_eq = begin;
        int right = end-1;
        int right_eq = end-1;
        int r;
        for (;;) {
            while (left <= right && (r = comp.compare(left, array, 0, tmp, rec_size)) <= 0) {
                if (r == 0) { swap(left_eq, left); ++left_eq; }
                ++left;
            }
            while (right >= left && (r = comp.compare(right, array, 0, tmp, rec_size)) >= 0) {
                if (r == 0) { swap(right_eq, right); --right_eq; }
                --right;
            }
            if (left > right) break;
            swap(left++, right--);
        }
        
        // Move equal elements to the middle
        int s;
        s = Math.min(left_eq-begin, left-left_eq);
        for (int i=0; i < s; ++i)
            swap(begin+i, left-s+i);
        
        s = Math.min(right_eq-right, end-1-right_eq);
        for (int i=0; i < s; ++i)
            swap(left+i, end-s+i);
        
        // Divide and conquer
        if ((s = left-left_eq) > 1)  sort(begin, begin+s, comp, tmp, pivot);
        if ((s = right_eq-right) > 1) sort(end-s, end, comp, tmp, pivot);
    }
}
