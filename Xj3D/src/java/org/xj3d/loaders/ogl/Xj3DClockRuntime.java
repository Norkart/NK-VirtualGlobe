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

package org.xj3d.loaders.ogl;

// External imports
import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.EventModelStatusListener;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.vrml.renderer.ogl.browser.PerFrameManager;

import org.xj3d.core.eventmodel.EventModelEvaluator;

/**
 * Implementation of the
 * {@link org.j3d.renderer.aviatrix3d.loaders.AVRuntimeComponent} that
 * can be used to drive the X3D/VRML scene graph clock.
 * <p>
 *
 * This clock allows the end user to play with the definition of "time"
 * in the scene graph by speeding up or slowing down the standard system clock.
 * The intial time is set to the time that this class instance is created.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class Xj3DClockRuntime extends PerFrameManager
    implements AVRuntimeComponent {

    /** The clock scale factor */
    private double clockScale;

    /**
     * Create a new instance of the clock runtime component.
     *
     * @param core The browser representation to send events to
     * @param eme The evaluator to use
     */
    public Xj3DClockRuntime(EventModelEvaluator eme, BrowserCore core) {
        super(eme, core, System.currentTimeMillis());

        clockScale = 1;
    }

    //---------------------------------------------------------------
    // Methods defined by AVRuntimeComponent
    //---------------------------------------------------------------

    /**
     * Execute the behaviour of the runtime component now.
     */
    public void executeModelBehavior() {
        long time = (long)(System.currentTimeMillis() * clockScale);

        clockTick(time);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the clock's scale factor to a new value. By default, the value is
     * 1.0. There's no limits on the scale, meaning zero and negative values
     * can be used.
     *
     * @param scale The new scale factor to use
     */
    public void setClockScaleFactor(float scale) {
        clockScale = (double) scale;
    }

    /**
     * Get the currently set clock scale factor.
     *
     * @return The current scale value
     */
    public float getClockScaleFactor() {
        return (float) clockScale;
    }
}
