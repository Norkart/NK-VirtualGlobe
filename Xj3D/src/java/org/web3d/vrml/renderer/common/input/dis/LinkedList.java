/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// Standard imports

// Application specific imports

/**
 * Custom single linked list.  We want to manage as many DIS nodes as possible,
 * so this linked list allows structured storage without method call overhead for traversal.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

class LinkedList {
    public ListEntry head;
    public ListEntry tail;

    public void add(ListEntry entry) {
        if (head == null) {
            head = entry;
            tail = entry;
        } else {
            tail.next = entry;
            tail = entry;
        }
    }

    public void remove(ListEntry entry, ListEntry last) {
        if (entry == head) {
            head = entry.next;
        } else {
            last.next = entry.next;
        }

        if (entry == tail) {
            tail = last;
        }
    }
}
