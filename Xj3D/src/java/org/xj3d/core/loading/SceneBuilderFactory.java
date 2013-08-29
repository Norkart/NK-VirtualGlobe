/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.loading;

// External imports
// None

// Local imports
// None

/**
 * Definition of a factory used to create new instances of the scene builder
 * on demand.
 * <p>
 *
 * The issue of VRML97 requirement is ignored as part of the interface, but
 * the constructor of the concrete implementation of this class should set it
 * as a flag.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SceneBuilderFactory {

    /**
     * Create a new scene builder instance.
     *
     * @return A fresh instance
     */
    public SceneBuilder createBuilder();
}

