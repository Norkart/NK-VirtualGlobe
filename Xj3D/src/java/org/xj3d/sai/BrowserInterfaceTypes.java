/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
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

// External Imports

// Local Imports
// None

/**
 * Enumeration of the types of browser implementations that we can create.
 * <p>
 *
 * This is used in conjunction with {@link BrowserConfig} when talking to
 * specific panel interfaces to work out what should be created.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public enum BrowserInterfaceTypes {
    HEAVYWEIGHT, PARTIAL_LIGHTWEIGHT, LIGHTWEIGHT, OFFSCREEN
}
