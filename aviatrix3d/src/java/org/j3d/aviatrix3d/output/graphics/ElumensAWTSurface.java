/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.output.graphics;

// External imports
import javax.media.opengl.*;

import java.awt.Component;

// Local imports
// None

/**
 * A surface which uses the Elumens SPI library to output to
 * curved surfaces.
 * <p>
 *
 * This implementation is capable of handling multichannel dome support with
 * configurable number of channels. The number of channels used can be modified
 * on the fly, but it is costly to do so as the render has to be reinitialized
 * each time.
 * <p>
 *
 * Currently stereo is not supported on this device, though it may be at a
 * future time.
 *
 * @author Alan Hudson
 * @version $Revision: 3.8 $
 */
public class ElumensAWTSurface extends BaseAWTSurface
    implements ElumensOutputDevice
{
    /**
     * Static constructor loads the native libraries that we need to interface
     * to this library with.
     */
    static
    {
System.out.println("Elumens library loaded");
        try
        {
            System.loadLibrary("spiclops");
            System.loadLibrary("elumens");
        }
        catch(Exception e)
        {
            System.out.println("Unable to locate Elumens libraries");
            System.out.print("Searching path: ");
            System.out.println(java.lang.System.getProperty("java.library.path"));

            e.printStackTrace();
        }
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public ElumensAWTSurface(GLCapabilities caps)
    {
        this(caps, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public ElumensAWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser)
    {
        this(caps, chooser, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public ElumensAWTSurface(GLCapabilities caps, boolean lightweight)
    {
        this(caps, null, null, lightweight);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public ElumensAWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser,
                             boolean lightweight)
    {
        this(caps, chooser, null, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public ElumensAWTSurface(GLCapabilities caps, BaseSurface sharedWith)
    {
        this(caps, null, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public ElumensAWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser,
                             BaseSurface sharedWith)
    {
        this(caps, chooser, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public ElumensAWTSurface(GLCapabilities caps,
                             BaseSurface sharedWith,
                             boolean lightweight)
    {
        this(caps, null, sharedWith, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public ElumensAWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser,
                             BaseSurface sharedWith,
                             boolean lightweight)
    {
        super(sharedWith, lightweight);

        init(caps, chooser);
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    /**
     * Get the underlying object that this surface is rendered to. If it is a
     * screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     *
     * @return The drawable surface representation
     */
    public Object getSurfaceObject()
    {
        // Since we know that the canvas is GLJPanel or GLCanvas, we can just
        // return the raw drawable here for casting.
        return canvas;
    }

    //---------------------------------------------------------------
    // Methods defined by ElumensOutputDevice
    //---------------------------------------------------------------

    /**
     * Set the number of channels to display.  Calling this
     * will cause a reinitialization of renderer.
     *
     * @param channels The number of channels to render.
     */
    public void setNumberOfChannels(int channels)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setNumberOfChannels(channels);
    }

    /**
     * Set the channel lens position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelLensPosition(int channel, float x, float y, float z)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanLensPosition(channel,x,y,z);
    }

    /**
     * Set the channel eye position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelEyePosition(int channel, float x, float y, float z)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanEyePosition(channel,x,y,z);
    }

    /**
     * Set the screen orientation.  Allows the project to rotated in software
     * for different hardware setups.
     *
     * @param r The roll
     * @param p The pitch
     * @param v The yaw
     */
    public void setScreenOrientation(double r, double p, double v)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setScreenOrientation(r,p,v);
    }

    /**
     * Set the channel size in pixels.
     *
     * @param channel The ID of the channel(s) to affect
     * @param height The height in pixels
     * @param width The width in pixels
     */
    public void setChannelSize(int channel, int height, int width)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanSize(channel,height,width);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseAWTSurface
    //---------------------------------------------------------------

    /**
     * Attempt to create a new lightweight canvas renderer now. This will only
     * be called whenever the user has signalled that this is a lightweight
     * renderer and we do not yet have a canvasRenderer instance created. If
     * this fails, silently exit. We'll attempt to do this next frame.
     *
     * @return true if this creation succeeded
     */
    protected boolean createLightweightContext()
    {
        try
        {
            canvasContext = ((GLAutoDrawable)canvas).getContext();
        }
        catch(NullPointerException npe)
        {
            // This is unexpectedly thrown by the internals of the JOGL RI when
            // the surface has not yet been realised at the AWT level. Catch an
            // ignore, treating it as though context creation failed.
        }


        if(canvasContext != null)
        {
            ((GLJPanel)canvas).setAutoSwapBufferMode(false);
            canvasRenderer = new ElumensRenderingProcessor(canvasContext);
            return true;
        }

        return false;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Common internal initialisation for the constructors.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    private void init(GLCapabilities caps, GLCapabilitiesChooser chooser)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        if(lightweight)
        {
            canvas = new GLJPanel(caps, chooser, shared_context);

            // Don't fetch context here because the JOGL code doesn't
            // generate a valid context until the window has been drawn and
            // made visible the first time.
        }
        else
        {
            canvas = new GLCanvas(caps, chooser, shared_context, null);
            ((GLCanvas)canvas).setAutoSwapBufferMode(false);

            canvasContext = ((GLAutoDrawable)canvas).getContext();
            canvasRenderer = new ElumensRenderingProcessor(canvasContext);
        }

        ((Component)canvas).addComponentListener(resizer);
        ((Component)canvas).setIgnoreRepaint(true);

        init();
    }
}
