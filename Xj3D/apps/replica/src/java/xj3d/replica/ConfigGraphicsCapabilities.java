/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External imports
// None

// Local imports
// None 

/**
 * Defines the requirements for configuring the graphics capabilities
 * parameters of a Construct.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ConfigGraphicsCapabilities {
    
    /**
     * Set the Construct's parameters for building the graphics capabilities
     *
     * @param useMipMaps Should mipmaps be generated
     * @param doubleBuffered Should double (or single) buffering be used
     * @param antialiasSamples The antialias samples setting to be used.
     * A value of 1 or less disables antialiasing.
     * @param anisotropicDegree The anisotropic degree setting to be used.
     * A value of 1 or less disables anisotropic filtering.
     */
    public void setGraphicsCapabilitiesParameters( 
        boolean useMipMaps, 
        boolean doubleBuffered, 
        int antialiasSamples, 
        int anisotropicDegree );
}
