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

package org.xj3d.ui.awt.browser.ogl;

// External imports
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;

// Local imports
import org.web3d.util.ErrorReporter;

import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * An abstract sub-class of OGLConstruct that provides AWT UI capabilities
 * to the base Construct.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class AWTOGLConstruct extends OGLConstruct {
    
    ///////////////////////////////////////////////////////////////////
    // Toolkit specific classes
    
    /** AWT Toolkit specific device factory */
    protected String AWT_UI_DEVICE_FACTORY = 
        "org.xj3d.ui.awt.device.AWTDeviceFactory";
    
    /** AWT Toolkit specific content handler factory */
    protected String AWT_CONTENT_HANDLER_FACTORY = 
        "org.xj3d.ui.awt.net.content.AWTContentHandlerFactory";
    
    ///////////////////////////////////////////////////////////////////
    
    /** 
     * Constructor 
     */
    protected AWTOGLConstruct( ) {
        this( null );
    }
    
    /** 
     * Constructor 
     * 
     * @param reporter The error reporter
     */
    protected AWTOGLConstruct( ErrorReporter reporter ) {
        super( reporter );
        UI_DEVICE_FACTORY = AWT_UI_DEVICE_FACTORY;
        CONTENT_HANDLER_FACTORY = AWT_CONTENT_HANDLER_FACTORY;
    }
    
    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------
    
    /**
     * Create the toolkit specific graphics rendering device
     */
    protected void buildGraphicsRenderingDevice( ) {
        
        graphicsDevice = new SimpleAWTSurface( 
            glCapabilities,
            glCapabilitiesChooser,
            lightweightRenderer );
    }
}
