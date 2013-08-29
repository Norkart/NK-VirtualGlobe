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
// None

/**
 * This allows an external application to replace the cursor logic
 * with their own.
 */
public interface Xj3DCursorFilter {
    /**
     * The internals of Xj3D have changed the cursor.  This method is
     * a control point for the application to decide on a different
     * cursor.  The cursor loaded will be changed to the returned
     * value.  These values will be cached internally to avoid
     * reloading images.
     *
     * @param cursor The new cursor to load
     * @return The cursor to use instead
     */
    public String cursorChanged(String cursor);
}
