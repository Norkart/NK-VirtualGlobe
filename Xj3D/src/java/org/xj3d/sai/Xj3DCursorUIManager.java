/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2001-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * This allows an external application to replace the cursor logic
 * with their own.
 *
 * @author Alan Hudson
 */
public interface Xj3DCursorUIManager {
    /**
     * Add a cursor filter.  Null removes the filter.
     *
     * @param cf The filter
     */
    public void setCursorFilter(Xj3DCursorFilter cf);

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
