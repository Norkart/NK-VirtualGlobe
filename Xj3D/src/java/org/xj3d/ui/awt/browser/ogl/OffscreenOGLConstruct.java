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
import org.j3d.aviatrix3d.output.graphics.PbufferSurface;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * An abstract sub-class of AWTOGLConstruct that provides offscreen rendering
 * capabilities to the base Construct.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class OffscreenOGLConstruct extends AWTOGLConstruct {

	/** The width of the rendering surface */
	protected int width;
	
	/** The height of the rendering surface */
	protected int height;
	
    /**
     * Constructor
     */
    protected OffscreenOGLConstruct() {
        this(null, 0, 0);
    }

    /**
     * Constructor
     *
     * @param reporter The error reporter
     */
    protected OffscreenOGLConstruct(ErrorReporter reporter, int width, int height) {
        super(reporter);
        UI_DEVICE_FACTORY = null;
		this.width = width;
		this.height = height;
    }

    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------

    /**
     * Create the toolkit specific graphics rendering device
     */
    protected void buildGraphicsRenderingDevice() {

        graphicsDevice =
            new PbufferSurface(glCapabilities, width, height);
    }
	
    /**
     * Create the audio rendering device
     */
    protected void buildAudioRenderingDevice( ) {
    }
}
