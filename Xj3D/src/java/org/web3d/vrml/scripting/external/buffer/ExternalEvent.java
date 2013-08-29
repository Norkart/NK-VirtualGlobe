package org.web3d.vrml.scripting.external.buffer;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

/** A very simple event class for storage and queueing purposes.
  * This class doesn't impose any requirements beyond the doEvent method. 
  * <P>
  * External events from both the EAI and SAI systems implement this interface
  * to get into the queuing system.
  * */
public interface ExternalEvent {

    /** Perform the event actions. */
    void doEvent();
    
    /** Is the event conglomerating or non-conglomerating.  Conglomerating
      * events such as the set1Value calls build up in the event buffer.
      */
    boolean isConglomerating();
}

