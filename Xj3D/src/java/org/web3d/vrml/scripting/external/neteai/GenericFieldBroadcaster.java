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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import org.web3d.util.IntHashMap;

import vrml.eai.event.VrmlEvent;
import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventOut;

/**
 * Simple implemetation of the FieldBroadcaster interface.
 * This broadcaster must use a seperate broadcasting thread because
 * ClientProcessingTask's thread can't be used for processing user
 * requests (it ends up blocking and waiting for itself).
 */
public class GenericFieldBroadcaster implements FieldBroadcaster {

    /** The to do list for the broadcast task */
    LinkedList toDoList;
    
    /** The table of user data objects for fields */
    IntHashMap userDataTable;
    
    /** Factory for producing eventOut's for the broadcasts */
    EAIFieldAndNodeFactory fieldFactory;
    
    /** Table of vectors of event listeners */
    IntHashMap listenerTable;
    
    /** Records the types of all know fields */
    IntHashMap typeTable;

    /** Make a new field broadcaster.
     * @param factory The factory for generating field buffers
     */
    GenericFieldBroadcaster(EAIFieldAndNodeFactory factory) {
        fieldFactory=factory;
        toDoList=new LinkedList();
        listenerTable=new IntHashMap();
        typeTable=new IntHashMap();
        userDataTable=new IntHashMap();
        new BroadcastTask(toDoList).start();
    }
    
    /** * @see org.web3d.vrml.scripting.external.neteai.FieldBroadcaster#generateFieldBroadcast(int, java.io.DataInputStream)  */
    public synchronized void generateFieldBroadcast(int fieldID, DataInputStream dis) throws IOException {
        //System.out.println("Broadcast for fieldID"+fieldID);
        int fieldType=((Integer)typeTable.get(fieldID)).intValue();
        double timestamp=dis.readDouble();
        EventOut field=fieldFactory.getStoredEventOut(fieldID,fieldType,dis);
        // Generate the broadcast.
        VrmlEvent event=new VrmlEvent(field,timestamp,userDataTable.get(fieldID));
        Vector listeners=(Vector) listenerTable.get(fieldID);
        if (listeners!=null) {
            Enumeration e=listeners.elements();
            while (e.hasMoreElements()) {
                VrmlEventListener listener=(VrmlEventListener) e.nextElement();
                synchronized (toDoList) {
                    toDoList.addLast(new ToDoEntry(event,listener));
                    toDoList.notify();
                }

            }
        } /* else {
            System.out.println("No listeners for field ID"+fieldID);
        }*/
        
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldBroadcaster#removeVrmlEventListener(int, vrml.eai.event.VrmlEventListener)  */
    public synchronized boolean removeVrmlEventListener(int fieldID, VrmlEventListener l) {
        Vector listeners=(Vector) listenerTable.get(fieldID);
        if (listeners==null)
            return false;
        else {
            listeners.remove(l);
            if (listeners.size()==0) {
                typeTable.remove(fieldID);
                return true;
            } else
                return false;
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldBroadcaster#addVrmlEventListener(int, vrml.eai.event.VrmlEventListener)  */
    public synchronized boolean addVrmlEventListener(int fieldID, int fieldType, VrmlEventListener l) {
        Vector listeners=(Vector) listenerTable.get(fieldID);
        if (listeners==null) {
            listeners=new Vector();
            listeners.add(l);
            listenerTable.put(fieldID,listeners);
            typeTable.put(fieldID,new Integer(fieldType));
            return true;
        } else {
            listeners.add(l);
            return false;
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldBroadcaster#getUserData(int)  */
    public Object getUserData(int fieldID) {
        return userDataTable.get(fieldID);
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldBroadcaster#setUserData(int, java.lang.Object)  */
    public void setUserData(int fieldID, Object data) {
        userDataTable.put(fieldID,data);
    }

    /** The work entries for the linked list.
     *  The listener list gets unpacked to one (event,listener)
     *  pair per entry.
     *  */
    static class ToDoEntry {
        VrmlEvent event;
        VrmlEventListener listener;
        ToDoEntry(VrmlEvent e, VrmlEventListener l) {
            event=e;
            listener=l;
        }
    }
    
    /** The simple broadcasting loop */
    static class BroadcastTask extends Thread {
        
        /** List to remove work elements from */
        LinkedList toDoList;
        
        /** Basic constructor.
         * @param workList Linked list to draw elements from
         */
        BroadcastTask(LinkedList workList) {
            toDoList=workList;
        }
        
        /** * @see java.lang.Thread#run()  */
        public void run() {
            while (true) {
                //System.out.println("Broadcaster in loop.");
                ToDoEntry entry;
                synchronized (toDoList) {
                    if (toDoList.size() == 0)
                        try {
                            toDoList.wait();
                        } catch (InterruptedException e) {
                            System.err.println("BroadcastTask interrupted.");
                            return;
                        }
                    entry = (ToDoEntry) toDoList.removeFirst();
                }
                entry.listener.eventOutChanged(entry.event);
            }
        }
        
    }
    
}
