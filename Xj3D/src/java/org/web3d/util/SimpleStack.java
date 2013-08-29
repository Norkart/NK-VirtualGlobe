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
 * A stack that has a minimal implementation and no syncrhonisation.
 * <P>
 *
 * This stack is designed to be used in a high-speed, single threaded
 * environment. It is directly backed by an array for fast access.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 * @see java.util.Stack
 */
public class SimpleStack {

    /** The initial size of the stack for element processing */
    private static final int STACK_START_SIZE = 20;

    /** The increment size of the stack if it get overflowed */
    private static final int STACK_INCREMENT = 5;

    /** The stack of child indexes used during processing */
    private Object[] stackContents;

    /** Counter to the top item in the stack array */
    private int topOfStack;

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <tt>20</tt> respectively.
     */
    public SimpleStack() {
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
    public SimpleStack(int initialCapacity) {
        stackContents = new Object[initialCapacity];
        topOfStack = -1;
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this stack object. Prints a string
     * in reverse order from top to bottom.
     *
     * @return A string representation of the stack
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("SimpleStack { size ");
        buf.append(topOfStack);
        buf.append("\n");

        for(int i = topOfStack; i >= 0; i--) {

            buf.append(' ');
            buf.append(i);
            buf.append(": ");
            if(stackContents[i] == null)
                buf.append("NULL");
            else {
                buf.append(stackContents[i].getClass());
                buf.append("#");
                buf.append(stackContents[i].hashCode());
            }

            buf.append("\n");

        }

        buf.append('}');
        return buf.toString();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
    public void push(Object val) {
        resizeStack();

        topOfStack++;
        stackContents[topOfStack] = val;
    }

    /**
     * Peek at the value on the top of the stack without removing it. If the
     * value pushed was null, then null is returned here.
     *
     * @return A reference to the object on the top of the stack
     * @throws EmptyStackException The stack is currently empty
     */
    public Object peek() throws EmptyStackException {
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
    public Object pop() {

        if(topOfStack == -1)
            throw new EmptyStackException();

        Object tmp = stackContents[topOfStack];
        stackContents[topOfStack] = null;

        topOfStack--;

        return tmp;
    }

    /**
     * Returns true if this stack contains an instance of the value. The checl
     * looks at both the reference comparison (==) and the equality using
     * <code>.equals()</code>. If the stack is currently empty this will always
     * return false. The search order is from the top of the stack towards the
     * bottom.
     *
     * @param value The value whose presence in this stack is to be tested.
     * @return true if this stack contains the value.
     */
    public boolean contains(Object value) {

        boolean ret_val = false;

        for(int i = topOfStack; i >= 0; i--) {
            if(stackContents[i] == value) {
                ret_val = true;
                break;
            }

            if(stackContents[i].equals(value)) {
                ret_val = true;
                break;
            }
        }

        return ret_val;
    }

    /**
     * Remove the given object from the stack if it exists. If it is not
     * in the stack, then ignore it quietly.
     *
     * @param obj The object to be removed
     */
    public void remove(Object obj) {

        int position = -1;

        for(int i = topOfStack; i >= 0; i--) {
            if(stackContents[i] == obj) {
                position = i;
                break;
            }

            if(stackContents[i].equals(obj)) {
                position = i;
                break;
            }
        }

        if(position != -1) {
            int top_dist = topOfStack - position;

            if(top_dist != 0)
            {
                System.arraycopy(stackContents,
                                 position + 1,
                                 stackContents,
                                 position,
                                 top_dist);
            }

            stackContents[topOfStack] = null;
            topOfStack--;
        }
    }

    /**
     * Clears this stack so that it contains no values.
     */
    public void clear() {
        for(int i = topOfStack; i >= 0; i--)
            stackContents[i] = null;

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

            Object[] tmp = new Object[new_size];

            System.arraycopy(stackContents, 0, tmp, 0, old_size);

            stackContents = tmp;
        }
    }
}
