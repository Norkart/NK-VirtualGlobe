/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

/**
 * Implementation of a drawable surface that only renders to an offscreen
 * pbuffer.
 * <p>
 *
 * <b>Resizing information</b>
 *
 * Pbuffers cannot be resized, so all queries to add and remove resize
 * listeners are silently ignored. <i>However</i>, we do something a little
 * different. When the listener add request is received, we <i>immediately</i>
 * call the listener with the size of this surface. This allows applications
 * that depend on getting some resize information (eg using our utility
 * {@link org.j3d.aviatrix3d.pipeline.graphics.ViewportResizeManager} class)
 * to still behave consistently.
 * <p>
 * Beyond this initial call, no resize events will ever be generated. Listeners
 * are never kept by the implementation.
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public class PbufferSurface extends BaseSurface
{
    /** Error message when the resize listener class we called barfs */
    private static final String RESIZE_CALLBACK_ERR =
        "The GraphicsResizeListener we just called has tossed a wobbly";

    /** Error message when JOGL can't create Pbuffers for us */
    private static final String NOT_AVAILABLE_MSG =
        "The underlying platform cannot create pbuffers.";

    /** The width of this surface in pixels */
    private final int surfaceWidth;

    /** The height of this surface in pixels */
    private final int surfaceHeight;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps, int width, int height)
        throws PbufferUnavailableException
    {
        this(caps, null, null, width, height);
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
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          GLCapabilitiesChooser chooser,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        this(caps, chooser, null, width, height);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          BaseSurface sharedSurface,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        this(caps, null, sharedSurface, width, height);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          GLCapabilitiesChooser chooser,
                          BaseSurface sharedSurface,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        super(sharedSurface);

        surfaceWidth = width;
        surfaceHeight = height;

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

    /**
     * Add a resize listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    public void addGraphicsResizeListener(GraphicsResizeListener l)
    {
        try {
            l.graphicsDeviceResized(0, 0, surfaceWidth, surfaceHeight);
        } catch(Exception e) {
            errorReporter.errorReport(RESIZE_CALLBACK_ERR, e);
        }
    }

    /**
     * Remove a resize listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeGraphicsResizeListener(GraphicsResizeListener l)
    {
        // Ignored
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
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    private void init(GLCapabilities caps, GLCapabilitiesChooser chooser)
        throws PbufferUnavailableException
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLDrawableFactory factory = GLDrawableFactory.getFactory();

        if(!factory.canCreateGLPbuffer())
            throw new PbufferUnavailableException(NOT_AVAILABLE_MSG);

        canvas = factory.createGLPbuffer(caps,
                                         chooser,
                                         surfaceWidth,
                                         surfaceHeight,
                                         shared_context);

        canvasContext = ((GLAutoDrawable)canvas).getContext();
        canvasRenderer = new StandardRenderingProcessor(canvasContext);

        init();
    }
}
