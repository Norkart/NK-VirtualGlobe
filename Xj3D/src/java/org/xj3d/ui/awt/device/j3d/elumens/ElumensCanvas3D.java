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

package org.xj3d.ui.awt.device.j3d.elumens;

// External imports
import java.util.ArrayList;

import javax.media.j3d.View;
import javax.media.j3d.Behavior;

import java.awt.GraphicsConfiguration;
import java.awt.Dimension;

// Local imports
import org.j3d.device.output.elumens.*;

import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.xj3d.core.eventmodel.EventModelEvaluator;

/**
 * A version of the standard VRMLBrowserCanvas class that works for Elumens Domes.
 * This code requires the elumens.dll created in the j3d.org area to run.
 * NOTE: the sevent flag on the constuctor is a work-around to a timing issue.
 *       in general seems that content works with this flag set true or false,
 *       but its a per-content thing.
 *
 * @author Alan Hudson
 * @verion $Revision: 1.2 $
 */
public class ElumensCanvas3D extends VRMLBrowserCanvas
    implements MouseCoordinateConverter {

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** The current wall clock time that we are sending to people */
    private long currentWallTime;

    /** The last clock tick. We don't bother sending if the diff is zero */
    private long lastWallTime;

    /** Listeners for SPI events */
    private ArrayList listeners;

    /** The Java wrapper to the native SPI library */
    private SPI spi;

    /** Is the SPI library initialized? */
    private boolean initialize = false;

    /** The format used to initialize the SPI library */
    private int spiFormat=0;

    /** How many channels to render */
    private int numChannels;

    /** Which wall are we rendering */
    private int wallCnt;

    /** A list of coordinateSources which need conversion by the SPI library */
    private ArrayList coordinateSources;

    /** The event model to run */
    private EventModelEvaluator eventModel;

    /** Should we run the event model.  Waits till the scene has been parsed before running */
    private boolean runEventModel;

    /** Run the event model on swap or preRender */
    private boolean sEvent;

    private int skip;
    private View view;

    /** The number of frames we have rendered */
    private long numFrames;

    /** The time the current frame started */
    private long startTime;

    /** The total time this run of AVGLEN frames has taken */
    private long totalTime;

    /** The total time for this program run */
    private long runTime;

    /** The lowest FPS recorded */
    private float lowestFPS=10000;

    /** The number of frames to average over */
    private final static int AVGLEN=10;

    private float near;
    private float far;

    static {
        System.loadLibrary("elumens");
    }

    /**
     * Create a new canvas given the graphics configuration
     * No timing information will be displayed
     *
     * @param gc The graphics configuration to use for the canvas
     */
    public ElumensCanvas3D(GraphicsConfiguration gc, View view, EventModelEvaluator eme, boolean sEvent, int numChannels) {
        super(gc,false, view);

        eventModel = eme;
        runEventModel = false;
        this.sEvent = sEvent;
        this.view = view;
        spi = new SPI();
        this.numChannels = numChannels;
        listeners = new ArrayList();
        coordinateSources = new ArrayList();

        near = 0.1f;
        far = 5000f;
    }

    /**
     * Add a listener for SPI events
     */
     public void addListener(ElumensEventListener eel) {
        if (eel != null)
            listeners.add(eel);
     }

    /**
     * Remove a listener for SPI events
     */
     public void removeListener(ElumensEventListener eel) {
        if (eel != null)
            listeners.remove(eel);
     }

    /**
     * Set the initialization paramaters for the SPI library.
     *
     * @format The format to use
     */
    public void initSPI(int format) {
        initialize = true;
        spiFormat = format;
    }

    /**
     * Get the SPI library used by this canvas.  Allows for direct manipulation
     * of its parameters.  Do not call rendering thread functions like init, begin
     * end, preRender, postRender.
     *
     * @return The SPI class
     */
    public SPI getSPI() {
        return spi;
    }

    /**
     * Setup the near and far clipping planes.
     */
    public void setNearFar(float near, float far) {
        this.near = near;
        this.far = far;

        if (initialize != true)
            spi.setNearFar(near,far);
    }

    /**
     * Process code before we render the image
     * Overrides the standard implementation to mark the start time
     */
     public void preRender() {
        if (skip > 0) {
            // Tell native code to skip swap
            spi.marker(2,0);
            return;
        }

        if (initialize == true) {
            boolean worked = spi.initialize(spiFormat, numChannels);
            if (!worked) {
                System.out.println("Failed to initialize SPI library");
                System.out.println("Possible Reasons:");
                System.out.println("    Another OpenGL application is running");
                System.out.println("    JAVA_PATH in elumens.bat is not correct");
                System.exit(0);
            }

            // TODO: Need to change this to a real value
            spi.setNearFar(near,far);

            // Notify Listeners of Initialize
            ElumensEventListener eel;
            for(int i=0; i < listeners.size(); i++) {
                eel = (ElumensEventListener) listeners.get(i);
                eel.initialized();
            }

            initialize = false;
            wallCnt = 0;
        }

//System.out.println("preRender: " + wallCnt);
        spi.marker(0,wallCnt);

        // TODO: Possible threading issue with coordinateSources?

        // Notify all converters that its ok to convert coordinates
        for(int i=0; i < coordinateSources.size(); i++) {
            MouseCoordinateSource mcs = (MouseCoordinateSource)
                coordinateSources.get(i);

            mcs.update();
        }

        coordinateSources.clear();
    }

    public void postRender() {
        if (skip > 0)
            return;

        spi.marker(1, wallCnt);
    }

    public void postSwap() {
        if (skip > 0) {
            skip--;
            return;
        }

        if (wallCnt == numChannels - 2) {
            numFrames++;

            float currFPS;

            long currTime = System.currentTimeMillis() - startTime;

            totalTime += currTime;
            runTime += currTime;
            if (numFrames % AVGLEN == 0) {
                currFPS = (1.0f / (totalTime / (AVGLEN * 1000.0f)));
                System.out.println("FPS:" + currFPS);

                if (currFPS < lowestFPS) lowestFPS = currFPS;
                totalTime = 0;
            }
        }

        wallCnt = (wallCnt + 1) % numChannels;

        if (runEventModel && wallCnt == 0) {
            currentWallTime = System.currentTimeMillis();

            if((currentWallTime - lastWallTime) <= 0)
                currentWallTime += TIME_FUDGE_FACTOR;

            lastWallTime = currentWallTime;

            startTime = System.currentTimeMillis();
            eventModel.evaluate(currentWallTime);

            skip=1;
        }
    }

    //------------------------------------------------------------------------
    // Methods for MouseCoordinateConverter methods
    //------------------------------------------------------------------------
    public void registerInterest(MouseCoordinateSource msc) {
        coordinateSources.add(msc);
    }

    /**
     * Warp the provided mouse coordinates into 3d space.
     * Always pass in a 3 component array with x,y holding the
     * mouse coordinate.  The array will be filled with an x,y,z
     * value.
     */
    public void warpMouseCoordinate(double[] coords) {
        Dimension size = getSize();

        spi.warpCoords((int)coords[0], (int)coords[1], 0, size.width, size.height, coords);
    }

    //------------------------------------------------------------------------
    // Methods for MultipassRenderer
    //------------------------------------------------------------------------

    public void enableEventModel(boolean reme) {
        runEventModel = reme;
    }
}
