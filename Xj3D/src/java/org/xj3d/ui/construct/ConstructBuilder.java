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

package org.xj3d.ui.construct;

// External imports
// None

// Local imports
// None

/**
 * Defines the functional steps for building the infrastructure of 
 * an Xj3D browser instance.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ConstructBuilder {
    
    /**
     * Build a 'default' configuration.
     */
    public void buildAll( );
    
    /**
     * Create the rendering capabilities
     */
    public void buildRenderingCapabilities( );
    
    /**
     * Create the rendering devices
     */
    public void buildRenderingDevices( );
    
    /**
     * Create the access to the user interface devices
     */
    public void buildInterfaceDevices( );
    
    /**
     * Create the rendering pipelines and render manager
     */
    public void buildRenderer( );
    
    /**
     * Create the xj3d managers
     */
    public void buildManagers( );
    
    /**
     * Create the xj3d scripting engines
     */
    public void buildScriptEngines( );
    
    /**
     * Create the networking properties and objects needed to load content.
     */
    public void buildNetworkCapabilities( );
}
