/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
// None

/**
 * Wrapper interface for the internal viewpoint representation, to allow
 * abstraction of the user interface description of viewpoints from the
 * underlying node representation.
 * <p>
 *
 * This class deliberately does not give access to the SAI X3DNode that
 * represents the viewpoint.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Xj3DViewpoint {

    /**
     * Get the description String from this viewpoint. If there was no
     * description string provided in the source file, then this will
     * an empty, zero-length string as per the default value for SFString.
     *
     * @return A string containing the description
     */
    public String getDescription();
}
