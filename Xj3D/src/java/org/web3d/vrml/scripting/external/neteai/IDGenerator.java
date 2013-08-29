/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import org.web3d.util.IntHashMap;

/**
 * Simple utility for generating unique IDs.
 */
public class IDGenerator {

    /** starting point for ID generator */
    int nextKey;
    
    /** Used as set to record currently allocated IDs */
    IntHashMap usedKeys;
    
    IDGenerator() {
        nextKey=0;
        usedKeys=new IntHashMap();
    }
    
    /** Free a previous allocated ID
     * @param oldKey The recycled ID
     */
    synchronized void releaseKey(int oldKey) {
        usedKeys.remove(oldKey);
    }
    
    /** Allocate a new ID
     * @return The newly reserved ID
     */
    synchronized int generateID() {
        nextKey++;
        while (usedKeys.containsKey(nextKey))
            nextKey++;
        usedKeys.put(nextKey,this);
        return nextKey;
    }
    
}
