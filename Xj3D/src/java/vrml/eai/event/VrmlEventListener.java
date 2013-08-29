/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.event;

/**
 * A listener for events on VRML fields
 *
 * @version 1.1 25 April 1998
 */
public interface VrmlEventListener
{
    /**
     * Process an event that has occurred on a node's eventOut
     *
     * @param evt The event that caused this method to be called
     */
    public void eventOutChanged(VrmlEvent evt);

}



