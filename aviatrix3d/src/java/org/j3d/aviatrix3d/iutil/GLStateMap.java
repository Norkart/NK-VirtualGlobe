/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.iutil;

// External imports
import java.util.ArrayList;

// Local imports
// None

/**
 * A very hacked up HashMap variant that is built to maintain simple boolean
 * state values so that multiple contexts can individually track whether state
 * has changed and needs to be updated.
 * <p>
 *
 * When running in a multi-context environment, such as large-format displays,
 * CAVEs etc, each screen will have it's own GL context. Since each is
 * individually processed by the rendering pipeline, it's very possible that
 * objects appearing on one screen are not on another. When this happens and an
 * update is received to a scenegraph object instance, you will get a situation
 * where one context will flush the state change to create a new display list,
 * but the other context(s) will not, because they were never passed to their
 * respective local graphics pipe. This class manages the simple boolean state
 * that is needed to track whether these changes on a per-GL context have been
 * made or not.
 * <p>
 * This implementation is not thread-safe, so caution must be exercised about how
 * items are added and removed from the instance. However, since we're only
 * dealing with adding and removing new GL contexts, that should happen very,
 * very rarely. Any state change notifications are made in-place.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 * @see java.util.HashMap
 */
public class GLStateMap
{
    /** The hash table data. */
    private transient Entry[] table;

    /** The total number of entries in the hash table. */
    private transient int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     */
    private int threshold;

    /** The load factor for the state map. */
    private float loadFactor;

    /** Cache of the entry instances to prevent excessive object creation */
    private ArrayList<Entry> entryCache;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * table.
     */
    private static class Entry
    {
        int hash;
        boolean value;
        Entry next;

        /**
         * Create a new default entry with nothing set.
         */
        protected Entry()
        {
        }

        /**
         * Create a new entry with the given values.
         *
         * @param hash The code used to hash the object with
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected Entry(int hash, boolean value, Entry next)
        {
            this.hash = hash;
            this.value = value;
            this.next = next;
        }

        /**
         * Convenience method to set the entry with the given values.
         *
         * @param hash The code used to hash the object with
         * @param key The key used to enter this in the table
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected void set(int hash, boolean value, Entry next)
        {
            this.hash = hash;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * Constructs a new, empty state map with a default capacity and load
     * factor, which is <tt>20</tt> and <tt>0.75</tt> respectively.
     */
    public GLStateMap()
    {
        this(5, 0.75f);
    }

    /**
     * Constructs a new, empty state map with the specified initial
     * capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the state map.
     * @param loadFactor the load factor of the state map.
     * @throws IllegalArgumentException  if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    public GLStateMap(int initialCapacity, float loadFactor)
    {
        if(initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        if(loadFactor <= 0)
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);

        if(initialCapacity == 0)
            initialCapacity = 1;

        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);

        entryCache = new ArrayList<Entry>(initialCapacity);
    }

    /**
     * Returns the number of keys in this state map.
     *
     * @return  the number of keys in this state map.
     */
    public int size()
    {
        return count;
    }

    /**
     * Tests if this state map maps no keys to values.
     *
     * @return  <code>true</code> if this state map maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * Tests if the specified object is a key in this state map.
     *
     * @param  key  possible key.
     * @return <code>true</code> if and only if the specified object is a
     *    key in this state map, as determined by the <tt>equals</tt>
     *    method; <code>false</code> otherwise.
     */
    public boolean containsKey(Object key)
    {
        Entry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next)
        {
            if(e.hash == hash)
                return true;
        }
        return false;
    }

    /**
     * Returns the state to which the specified key is set.
     *
     * @param key a key in the state map.
     * @return True or false depending on the state set
     */
    public boolean getState(Object key)
    {
        Entry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next)
        {
            if(e.hash == hash)
                return e.value;
        }

        return false;
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this state map. The key cannot be
     * <code>null</code>.
     * <p>
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     *
     * @param key the state map key.
     * @param value  the value to associate with the key
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public void put(Object key, boolean value)
    {
        // Makes sure the key is not already in the state map.
        Entry[] tab = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next)
        {
            if(e.hash == hash)
            {
                e.value = value;
            }
        }

        if(count >= threshold)
        {
            // Rehash the table if the threshold is exceeded
            rehash();

            tab = table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
        Entry e = getNewEntry();
        e.set(hash, value, tab[index]);

        tab[index] = e;
        count++;
    }

    /**
     * Removes the key (and its corresponding value) from this
     * state map. This method does nothing if the key is not in the state map.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this state map,
     *          or <code>null</code> if the key did not have a mapping.
     */
    public boolean remove(Object key)
    {
        Entry[] tab = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index], prev = null ; e != null ; prev = e, e = e.next)
        {
            if(e.hash == hash)
            {
                if(prev != null)
                {
                    prev.next = e.next;
                }
                else
                {
                    tab[index] = e.next;
                }

                count--;
                releaseEntry(e);
                return e.value;
            }
        }

        return false;
    }

    /**
     * Set all the items to the given state.
     */
    public void setAll(boolean state)
    {
        if(count == 0)
            return;

        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            while(e != null)
            {
                e.value = state;
                e = e.next;
            }
        }
    }

    /**
     * Clears this state map so that it contains no keys.
     */
    public synchronized void clear()
    {
        if(count == 0)
            return;

        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            if(e == null)
                continue;

            while(e.next != null)
            {
                releaseEntry(e);

                Entry n = e.next;
                e.next = null;
                e = n;
            }

            tab[index] = null;
        }

        count = 0;
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * state map, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the state map exceeds this state map's capacity
     * and load factor.
     */
    private void rehash()
    {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        threshold = (int)(newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;)
        {
            for (Entry old = oldMap[i] ; old != null ; )
            {
                Entry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * Grab a new entry. Check the cache first to see if one is available. If
     * not, create a new instance.
     *
     * @return An instance of the Entry
     */
    private Entry getNewEntry()
    {
        Entry ret_val;

        int size = entryCache.size();
        if(size == 0)
            ret_val = new Entry();
        else
            ret_val = (Entry)entryCache.remove(size - 1);

        return ret_val;
    }

    /**
     * Release an entry back into the cache.
     *
     * @param e The entry to put into the cache
     */
    private void releaseEntry(Entry e)
    {
        entryCache.add(e);
    }
}
