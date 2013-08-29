/*****************************************************************************
 * Copyright North Dakota State University, 2003
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.util;

// External imports
import java.util.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

// Local imports
// None

/**
 * An extension of HashMap which stored weak references to the values
 * in the hash map.
 * <p>
 *
 * Like it says in the documentation for WeakHashMap, this class
 * breaks several immutability assumptions that are assumed for
 * HashMap, since values will be disappearing at random.
 *
 * @author Brad Vender
 * @version $Revision: 1.2 $
 */
public class WeakValueHashMap implements Map {

    /** Used for clearing out the weak references */
    private ReferenceQueue theReferenceQueue;

    /** The internal implementation */
    private HashMap internalHashMap;

    /** Construct a new map of default size */
    public WeakValueHashMap() {
        internalHashMap=new HashMap();
        theReferenceQueue=new ReferenceQueue();
    }

    /** Construct a new map with specified starting size
     * @param size
     */
    public WeakValueHashMap(int size) {
        internalHashMap=new HashMap(size);
        theReferenceQueue=new ReferenceQueue();
    }

    /** @see Map#clear */
    public void clear() {
        // Throw away the old reference queue for simplicity.
        theReferenceQueue=new ReferenceQueue();
        internalHashMap.clear();
    }

    /** @see Map#containsKey */
    public boolean containsKey(Object key) {
        return internalHashMap.containsKey(key);
    }

    /** @see Map#containsValue */
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    /** @see Map#entrySet */
    public Set entrySet() {
        throw new UnsupportedOperationException();
    }

    /** @see Object#equals */
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /** Check for and remove references from the map which have
     *  become cleared.
     **/
    protected void flushEmptyReferences() {
        KeyedWeakReference r;
        for (r=(KeyedWeakReference)(theReferenceQueue.poll());
                r!=null;
             r=(KeyedWeakReference)(theReferenceQueue.poll()) ) {
            internalHashMap.remove(r.key());
        }
    }

    /** @see Map#get
     * Weak references which have become clear are removed from the
     * internal hash map before performing this request.
     **/
    public Object get(Object key) {
        flushEmptyReferences();
        WeakReference ref=(WeakReference)(internalHashMap.get(key));
        if (ref!=null) {
            Object value=ref.get();
            if (value==null)
                internalHashMap.remove(key);
            return value;
        } else
            return null;
    }

    /** @see Object#hashCode */
    public int hashCode() {
        return super.hashCode();
    }

    /** @see Map#isEmpty */
    public boolean isEmpty() {
        return internalHashMap.isEmpty();
    }

    /** @see Map#keySet */
    public Set keySet() {
        return internalHashMap.keySet();
    }

    /** @see Map#put
     *  This method performs the necessary steps to check for cleared weak
     *  references, wrap incoming data in weak references,
     *  and unwrap the old value. */
    public Object put(Object key, Object value) {
        if (value==null || key==null)
            throw new IllegalArgumentException();
        flushEmptyReferences();
        KeyedWeakReference ref=new KeyedWeakReference(key,value,theReferenceQueue);
        KeyedWeakReference oldRef=(KeyedWeakReference)(internalHashMap.put(key,ref));
        if (oldRef!=null)
            return oldRef.get();
        else
            return null;
    }

    /** @see Map#putAll
     *  Not supported
     **/
    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    /** @see Map#remove */
    public Object remove(Object key) {
        KeyedWeakReference ref=(KeyedWeakReference)(internalHashMap.remove(key));
        // Its already removed, so no point in doing it again.
        if (ref!=null)
            return ref.get();
        else
            return null;
    }

    /** @see Map#size */
    public int size() {
        return internalHashMap.size();
    }

    /** @see Map#values */
    public Collection values() {
        flushEmptyReferences();
        Vector result=new Vector();
        Iterator i=internalHashMap.values().iterator();
        while (i.hasNext()) {
            WeakReference ref=(WeakReference)(i.next());
            Object val=ref.get();
            if (val!=null)
                result.add(val);
        }
        return result;
    }
}

/** Supplemental class for weak value hash map.  Links the value's weak reference
 *  to its key in the map so that the key can be removed when the value is released.
 */
class KeyedWeakReference extends WeakReference {

    /** The key in the containing Map which refers to this reference. */
    Object theKey;

    /** Construct a weak reference with a key value.
      * The reference is registered with ReferenceQueue aQueue and
      * contains value aValue. */
    KeyedWeakReference(Object key, Object aValue, ReferenceQueue aQueue) {
        super(aValue,aQueue);
        theKey=key;
    }

    /** Return the key that this reference contains. */
    Object key() { return theKey; }

}
