/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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

// External imports
import java.util.EmptyStackException;

// Local imports
// none

/**
 * A stack that is based on int values.
 * <P>
 *
 * This stack is designed to be used in a high-speed, single threaded
 * environment. It is directly backed by an array for fast access. Similar to
 * the SimpleStack, but some methods not provided as they don't make sense.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 * @see java.util.Stack
 */
public class IntStack {

    /** The initial size of the stack for element processing */
    private static final int STACK_START_SIZE = 20;

    /** The increment size of the stack if it get overflowed */
    private static final int STACK_INCREMENT = 5;

    /** The stack of child indexes used during processing */
    private int[] stackContents;

    /** Counter to the top item in the stack array */
    private int topOfStack;

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <tt>20</tt> respectively.
     */
    public IntStack() {
        this(STACK_START_SIZE);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param  initialCapacity the initial capacity of the hashtable.
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public IntStack(int initialCapacity) {
        stackContents = new int[initialCapacity];
        topOfStack = -1;
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */
    public int size() {
        return topOfStack + 1;
    }

    /**
     * Tests if this stack maps no values.
     *
     * @return  <code>true</code> if this stack has no values
     */
    public boolean isEmpty() {
        return topOfStack == -1;
    }

    /**
     * Push a new value onto the top of the stack. The value may be any legal
     * reference to an object including null.
     *
     * @param val The new value for the stack.
     */
    public void push(int val) {
        resizeStack();

        stackContents[++topOfStack] = val;
    }

    /**
     * Peek at the value on the top of the stack without removing it. If the
     * value pushed was null, then null is returned here.
     *
     * @return A reference to the object on the top of the stack
     * @throws EmptyStackException The stack is currently empty
     */
    public int peek() throws EmptyStackException {
        if(topOfStack == -1)
            throw new EmptyStackException();

        return stackContents[topOfStack];
    }

    /**
     * Pop the value from the top of the stack. If the last value in the stack
     * was null then this will return null.
     *
     * @return The top object on the stack
     */
    public int pop() {

        if(topOfStack == -1)
            throw new EmptyStackException();

        int tmp = stackContents[topOfStack];

        topOfStack--;

        return tmp;
    }

    /**
     * Clears this stack so that it contains no values.
     */
    public void clear() {
        topOfStack = -1;
    }

    /**
     * Resize the stack if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeStack() {

        int old_size = stackContents.length;

        if((topOfStack + 1) == old_size) {

            int new_size = old_size + STACK_INCREMENT;

            int[] tmp = new int[new_size];

            System.arraycopy(stackContents, 0, tmp, 0, old_size);

            stackContents = tmp;
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("IntStack: size: ");
        buff.append(topOfStack+1);
        buff.append(" vals: ");
        for(int i=0; i <= topOfStack; i++) {
            buff.append(stackContents[i]);
            buff.append(" ");
        }

        return buff.toString();
    }
}
