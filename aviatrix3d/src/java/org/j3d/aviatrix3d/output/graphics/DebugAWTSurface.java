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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Local imports
// None

/**
 * Implementation of drawable surface with the key mapping defined to allow the
 * GL trace to be debugged for a single frame.
 * <p>
 *
 * To dump a set of GL trace, the 'd' key is mapped to dump the next frame.
 * <p>
 * This implementation of GraphicsOutputDevice renders to a normal GLCanvas instance
 * and provides pBuffer support as needed. Stereo support is not provided and
 * all associated methods always indicate negative returns on query about
 * support.
 *
 * @author Justin Couch
 * @version $Revision: 3.10 $
 */
public class DebugAWTSurface extends BaseAWTSurface
    implements KeyListener
{
    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public DebugAWTSurface(GLCapabilities caps)
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
    public DebugAWTSurface(GLCapabilities caps, GLCapabilitiesChooser chooser)
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
    public DebugAWTSurface(GLCapabilities caps, boolean lightweight)
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
    public DebugAWTSurface(GLCapabilities caps,
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
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public DebugAWTSurface(GLCapabilities caps, BaseSurface sharedSurface)
    {
        this(caps, null, sharedSurface, false);
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
     */
    public DebugAWTSurface(GLCapabilities caps,
                           GLCapabilitiesChooser chooser,
                           BaseSurface sharedSurface)
    {
        this(caps, chooser, sharedSurface, false);
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
     */
    public DebugAWTSurface(GLCapabilities caps,
                           BaseSurface sharedSurface,
                           boolean lightweight)
    {
        this(caps, null, sharedSurface, lightweight);
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
     */
    public DebugAWTSurface(GLCapabilities caps,
                           GLCapabilitiesChooser chooser,
                           BaseSurface sharedSurface,
                           boolean lightweight)
    {
        super(sharedSurface, lightweight);

        init(caps, chooser);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    /**
     * Notification of a key press event. When the 'd' key is pressed, dump
     * the next frame to stdout.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyPressed(KeyEvent evt)
    {
        if(evt.getKeyCode() == KeyEvent.VK_D)
            ((DebugRenderingProcessor)canvasRenderer).traceNextFrames(1);
    }

    /**
     * Notification of a key release event. Does nothing for this
     * implementation.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyReleased(KeyEvent evt)
    {
    }

    /**
     * Notification of a key type (press and release) event. This will any one
     * of the key value fields depending on the value.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyTyped(KeyEvent evt)
    {
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
            canvasContext.setSynchronized(true);
            canvasRenderer = new DebugRenderingProcessor(canvasContext);
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
            canvasContext.setSynchronized(true);
            canvasRenderer = new DebugRenderingProcessor(canvasContext);
        }

        ((Component)canvas).addKeyListener(this);
        ((Component)canvas).addComponentListener(resizer);
        ((Component)canvas).setIgnoreRepaint(true);

        init();
    }
}
