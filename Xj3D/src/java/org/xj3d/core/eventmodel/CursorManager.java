/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External Imports
// none

// Internal Imports
//none

/**
 * A manager of cursor changes.  These are based on internal actions in the
 * system like navigation state changes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface CursorManager {
    /**
     * Set the cursor cursor filter.  NULL will disable filtering
     *
     * @param cf The filter
     */
    public void setCursorFilter(CursorFilter cf);

    /**
     * Get the cursor cursor filter.  NULL means no filtering.
     *
     * @return The filter
     */
    public CursorFilter getCursorFilter();

    /**
     * Set the cursor to the currently specified image.  Normal changes
     * can still occur.  Monitor the cursorFilter for changes.
     *
     * @param url The image to use
     * @param x The center x coordinate
     * @param y The center y coordinate
     */
    public void setCursor(String url, int x, int y);
}
