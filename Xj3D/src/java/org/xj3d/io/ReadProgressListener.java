/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.io;

// External imports
// None

// Local imports
// None

/**
 * Listens for updates to read status on a stream.
 * <p>
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface ReadProgressListener {

    /**
     * Notification of where the stream is at.  The value is
     * dependent on the type, absolute or relative.
     *
     * @param value The new value
     */
    public void progressUpdate(long value);

    /**
     * The stream has closed.
     */
    public void streamClosed();
}
