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

package org.xj3d.ui.swt.browser.ogl;

// External imports
import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;

import org.j3d.aviatrix3d.output.graphics.SimpleSWTSurface;

// Local imports
import org.web3d.util.ErrorReporter;

import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * An abstract sub-class of OGLConstruct that provides SWT UI capabilities
 * to the base Construct.
 *

 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class SWTOGLConstruct extends OGLConstruct {
    
    ///////////////////////////////////////////////////////////////////
    // Toolkit specific classes
    
    /** SWT Toolkit specific device factory */
    protected String SWT_UI_DEVICE_FACTORY = 
        "org.xj3d.ui.swt.device.SWTDeviceFactory";
    
    /** SWT Toolkit specific content handler factory */
    protected String SWT_CONTENT_HANDLER_FACTORY = 
        "org.xj3d.ui.swt.net.content.SWTContentHandlerFactory";
    
    ///////////////////////////////////////////////////////////////////
    
    /** The parent Composite of the rendering surface */
    protected Composite parent;
    
    /** The style attributes of the rendering surface */
    protected int style;
    
    /** 
     * Constructor 
     */
    protected SWTOGLConstruct( Composite parent, int style ) {
        this( parent, style, null );
    }
    
    /** 
     * Constructor 
     * 
     * @param reporter The error reporter
     */
    protected SWTOGLConstruct( Composite parent, int style, ErrorReporter reporter ) {
        super( reporter );
        this.parent = parent;
        this.style = style;
        UI_DEVICE_FACTORY = SWT_UI_DEVICE_FACTORY;
        CONTENT_HANDLER_FACTORY = SWT_CONTENT_HANDLER_FACTORY;
    }
    
    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------
    
    /**
     * Create the toolkit specific graphics rendering device
     */
    protected void buildGraphicsRenderingDevice( ) {
        
        graphicsDevice = new SimpleSWTSurface( 
            parent,
            style,
            glCapabilities,
            glCapabilitiesChooser,
            lightweightRenderer );
    }
}
