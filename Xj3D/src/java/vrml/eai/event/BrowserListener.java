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
 * Listener interface for classes wishing to know about changes in the browser
 *
 * @version 1.0 7th March 1998
 */
public interface BrowserListener
{
    /**
     * Process an event that has occurred in the VRML browser.
     *
     * @param evt The event that caused this method to be called
     */
    public void browserChanged(BrowserEvent evt);
}
