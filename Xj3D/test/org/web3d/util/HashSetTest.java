/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// Standard imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.List;
import java.util.ArrayList;


/**
 * Tests the implementation of the HashSet class.  Pretty basic stuff.
 * @author Rob Nielsen
 * @version $Revision: 1.1 $
 */
public class HashSetTest extends TestCase {

    public HashSetTest(String name)
    {
        super(name);
    }

    /**
     * Tests the toArray() method using a passed in array.
     * The result has to be sorted before comparing with the expected result.
     */
    public void testToArray()
    {
        HashSet hs=new HashSet();
        assertTrue("Add 1",hs.add("One"));
        assertTrue("Add 2",hs.add("Two"));
        assertTrue("Add 1 Again",!hs.add("One"));
        assertEquals("Size check",hs.size(),2);
        String[] arr=new String[2];
        hs.toArray(arr);
        java.util.Arrays.sort(arr);
        assertEquals(arr,new String[]{"One","Two"});
    }

    /**
     * Tests the toArray() method requiring a new array to be created.
     * The result has to be sorted before comparing with the expected result.
     */
    public void testToArrayTooSmall()
    {
        HashSet hs=new HashSet();
        assertTrue("Add 1",hs.add(new Integer(2701)));
        assertTrue("Add 2",hs.add(new Integer(353)));
        assertEquals("Size check",hs.size(),2);
        Integer[] arr=(Integer[])hs.toArray(new Integer[0]);
        java.util.Arrays.sort(arr);
        assertEquals(arr,new Integer[]{new Integer(353),new Integer(2701)});
    }

    /**
     * A simple stress test.  Adds 10000 objects and removes them all again,
     * making sure they are all added and removed correctly and the final size
     * is 0.
     */
    public void testLotsOfAddingAndRemoving()
    {
        int max=10000;
        HashSet hs=new HashSet();
        for(int i=0;i<max;i++)
        {
            assertTrue("Adding "+i,hs.add(""+i));
        }
        assertEquals(max,hs.size());
        assertTrue("Not empty",!hs.isEmpty());
        assertTrue(hs.contains("2701"));
        for(int i=0;i<max;i++)
        {
            assertTrue("Removing "+i,hs.remove(""+i));
        }
        assertEquals(0,hs.size());
        assertTrue(hs.isEmpty());
        assertEquals("[]",hs.toString());
    }

    /**
     * Tests the clear() method, confirming by trying to add and remove objects
     * that have been cleared.
     */
    public void testClear()
    {
        HashSet hs1=new HashSet();
        hs1.add("one");
        hs1.add("two");
        hs1.add("three");
        hs1.clear();
        assertTrue(hs1.isEmpty());
        assertTrue(!hs1.contains("one"));
        assertTrue(!hs1.remove("two"));
        assertTrue(hs1.add("three"));
    }

    /**
     * Tests nulls in various places
     */
    public void testNull()
    {
        HashSet hs=new HashSet();
        assertTrue(!hs.add(null));
        assertTrue(hs.isEmpty());
        assertTrue(!hs.remove(null));
        assertTrue(!hs.contains(null));

        try { hs.addAll((HashSet)null); fail(); } catch(NullPointerException e){};
        try { hs.removeAll((HashSet)null); fail(); } catch(NullPointerException e){};
        try { hs.addAll((java.util.Collection)null); fail(); } catch(NullPointerException e){};
        try { hs.removeAll((java.util.Collection)null); fail(); } catch(NullPointerException e){};
    }

    /**
     * Ensure that two objects are equal and have the same hashCode() when they
     * contain the same elements (even if added in a different order) and
     * that they aren't equal when an element is removed.  Note that hashCodes
     * don't have to be different for object that aren't equal.
     */
    public void testEqualsAndHashCode()
    {
        HashSet hs1=new HashSet();
        hs1.add("one");
        hs1.add("two");
        hs1.add("three");

        HashSet hs2=new HashSet();
        hs2.add("three");
        hs2.add("one");
        hs2.add("two");

        assertEquals(hs1.hashCode(),hs2.hashCode());
        assertTrue(hs1.equals(hs2));

        hs1.remove("two");

        assertTrue(!hs1.equals(hs2));
    }

    /**
     * Tests the addAll method with another HashSet.
     */
    public void testAddAll()
    {
        HashSet hs1=new HashSet();
        hs1.add("one");
        hs1.add("two");
        hs1.add("three");

        HashSet hs2=new HashSet();
        hs2.add("two");
        hs2.add("three");
        hs2.add("four");

        assertTrue(hs1.addAll(hs2));
        assertEquals(4,hs1.size());
        assertEquals(3,hs2.size());
        assertTrue(hs1.contains("one"));
        assertTrue(hs1.contains("two"));
        assertTrue(hs1.contains("three"));
        assertTrue(hs1.contains("four"));

        assertTrue(!hs1.addAll(hs2));
    }

    /**
     * Tests the removeAll method with another HashSet.
     */
    public void testRemoveAll()
    {
        HashSet hs1=new HashSet();
        hs1.add("one");
        hs1.add("two");
        hs1.add("three");

        HashSet hs2=new HashSet();
        hs2.add("two");
        hs2.add("three");
        hs2.add("four");

        assertTrue(hs1.removeAll(hs2));
        assertEquals(1,hs1.size());
        assertEquals(3,hs2.size());
        assertTrue(hs1.contains("one"));
        assertTrue(!hs1.contains("two"));
        assertTrue(!hs1.contains("three"));

        assertTrue(!hs1.removeAll(hs2));
    }

    /**
     * Tests the removeAll method with the same object.
     * Works the same as clear()
     */
    public void testRemoveAllWithSelf()
    {
        HashSet hs=new HashSet();
        hs.add("one");
        hs.add("two");
        hs.add("three");
        hs.add("four");
        assertTrue(hs.removeAll(hs));
        assertTrue(hs.isEmpty());
    }

    /**
     * Tests the addAll method with a List containing duplicates.
     * The duplicates are discarded.
     */
    public void testAddAllCollection()
    {
        List l=new ArrayList();
        l.add("one");
        l.add("two");
        l.add("one");
        l.add("three");
        HashSet hs=new HashSet();
        assertTrue(hs.addAll(l));
        assertEquals(3,hs.size());
        assertEquals(4,l.size());
        assertTrue(!hs.addAll(l));
    }

    /**
     * Tests the removeAll method with a List containing duplicates.
     * The duplicates are ignored.
     */
    public void testRemoveAllCollection()
    {
        List l=new ArrayList();
        l.add("one");
        l.add("two");
        l.add("one");
        l.add("three");
        HashSet hs=new HashSet();
        hs.add("one");
        hs.add("four");
        assertTrue(hs.removeAll(l));
        assertEquals(1,hs.size());
        assertEquals("[four]",hs.toString());

        assertTrue(!hs.removeAll(l));
    }

    /**
     * This was the main problem with the old implementation.  hashCode()
     * is not unique and two objects with the same hashCode but not equal
     * to each other should be treated as individual objects.  For this test
     * the inner class A is used which always returns 1 for it's hashcode.
     */
    public void testWithDifferentObjectsWithSameHash()
    {
        A a1=new A(1);
        A a2=new A(2);
        A a3=new A(3);
        HashSet hs=new HashSet();
        assertTrue("Adding first",hs.add(a1));
        assertTrue("Adding second",hs.add(a2));
        assertTrue("Contains first",hs.contains(a1));
        assertTrue("Contains second",hs.contains(a2));
        assertTrue("Doesn't contain third",!hs.contains(a3));
        assertTrue("Removing first",hs.remove(a1));
        assertTrue("Doesn't contain first after remove",!hs.contains(a1));
        assertTrue("Contains second after remove",hs.contains(a2));
        assertTrue("Can't remove third",!hs.remove(a3));
    }

    /**
     * Compares two Object arrays
     */
    private void assertEquals(Object[] a1,Object[] a2)
    {
        assertEquals(a1==null,a2==null);
        if (a1==null)
          return;
        assertEquals(a1.length,a2.length);
        for(int i=0;i<a1.length;i++)
          assertEquals(a1[i],a2[i]);
    }

    public static Test suite()
    {
        return new TestSuite(HashSetTest.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

    /**
     * Inner class for testWithDifferentObjectsWithSameHash().  Has
     * a rather suboptimal hashCode() method.
     */
    class A
    {
        int a;

        public A(int a)
        {
            this.a=a;
        }

        public boolean equals(Object o)
        {
            return (o instanceof A) && ((A)o).a==a;
        }

        public int hashCode()
        {
            return 1;
        }
    }
}