/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.renderer.DefaultNodeFactory;

/**
 * Singleton implementation of the {@link org.web3d.vrml.lang.VRMLNodeFactory}
 * that produces Java 3D implementations of the nodes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class J3DNodeFactory extends DefaultNodeFactory {

    /** The global instance of the factory */
    private static J3DNodeFactory instance;


    /**
     * Private constructor to prevent direct instantiation.
     */
    private J3DNodeFactory() {
        super(JAVA3D_RENDERER);
    }

    /**
     * Get the current instance of this factory class. If an instance has
     * not been created yet, this will automatically create it
     *
     * @return The global instance of the factory
     */
    public static J3DNodeFactory getJ3DNodeFactory() {
        if(instance == null)
            instance = new J3DNodeFactory();

        return instance;
    }
}
