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
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsEnvironmentData;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsProfilingData;

/**
 * Handles the rendering for a single output device using quad-buffered stereo
 * techniques.
 * <p>
 * The code expects that everything is set up before each call of the display()
 * callback. It does not handle any recursive rendering requests as that is
 * assumed to have been sorted out before calling this renderer.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.12 $
 */
public class QuadBufferStereoProcessor extends BaseRenderingProcessor
    implements StereoRenderingProcessor
{
    /** The current eye separation to use */
    private float eyeSeparation;

    /**
     * Flag indicating if the buffers are avialable for implementing
     * stereo as required by this implementation.
     */
    private boolean stereoAvailability;

    /**
     * Construct handler for rendering objects to the main screen.
     *
     * @param context The context that this processor is working on
     */
    public QuadBufferStereoProcessor(GLContext context)
    {
        super(context);

        eyeSeparation = 0.005f;
        stereoAvailability = false;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseRenderingProcessor
    //---------------------------------------------------------------

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized or has changed; the GLContext has already been made
     * current when this method is called.
     */
    protected void init()
    {
        super.init();

        GL gl = glContext.getGL();

        byte[] params = new byte[1];
        gl.glGetBooleanv(GL.GL_STEREO, params, 0);

        stereoAvailability = (params[0] == GL.GL_TRUE);
    }

    /**
     * Called by the drawable to perform rendering by the client.
     *
     * @param profilingData The timing and load data
     */
    public void display(GraphicsProfilingData profilingData)
    {
        GL gl = glContext.getGL();

        if(gl == null)
            return;

        processRequestData(gl);

        if(terminate)
            return;

        // Draw the left eye first, then right
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glDrawBuffer(GL.GL_BACK_LEFT);

        if(terminate)
            return;

        render(gl, true, profilingData);

        if(terminate)
            return;

        // Draw the left eye first, then right
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glDrawBuffer(GL.GL_BACK_RIGHT);

        if(terminate)
            return;

        render(gl, false, profilingData);
    }

    //---------------------------------------------------------------
    // Methods defined by StereoSurface
    //---------------------------------------------------------------

    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    public boolean isStereoAvailable()
    {
        return stereoAvailability;
    }

    /**
     * Set the eye separation value when rendering stereo. The default value is
     * 0.33 for most applications. The absolute value of the separation is
     * always used. Ignored for this implementation.
     *
     * @param sep The amount of eye separation
     */
    public void setStereoEyeSeparation(float sep)
    {
        eyeSeparation = (sep < 0) ? -sep : sep;
    }

    /**
     * Get the current eye separation value - always returns 0.
     *
     * @return sep The amount of eye separation
     */
    public float getStereoEyeSeparation()
    {
        return eyeSeparation;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Perform the rendering loop for one of the two buffers, indicated by
     * the provided parameter.
     *
     * @param gl The gl context to draw with
     * @param left true if this is the left eye
     */
    private void render(GL gl, boolean left, GraphicsProfilingData profilingData)
    {
        // TODO:
        // May want to put some optimisations here for systems that can clear
        // both back buffers at once with a single call to
        // glDrawBuffer(GL_BACK) before the glClear();

        ObjectRenderable obj;
        ComponentRenderable comp;
        BufferStateRenderable buffer;
        GraphicsEnvironmentData data = null;
        int data_index = 0;
        int layer_data_index = 0;
        int mp_data_index = 0;
        int clear_buffer_bits = 0;
        boolean fog_active = false;
        boolean first_layer = true;
        ObjectRenderable current_fog = null;

        for(int i = 0; i < numRenderables && !terminate; i++)
        {
            switch(operationList[i])
            {
                case RenderOp.START_MULTIPASS:
                    data = environmentList[data_index];
                    mp_data_index = data_index;
                    data_index++;

                    // If this is not the first layer, render to one of the
                    // auxillary buffers and have that copy back to the main
                    // buffer when the layer is finished.
                    clear_buffer_bits = 0;
                    if(!first_layer)
                    {
                        gl.glDrawBuffer(GL.GL_AUX0);
                        gl.glReadBuffer(GL.GL_AUX0);

                        setupMultipassViewport(gl, data);
                    }
                    break;

                case RenderOp.STOP_MULTIPASS:
                    // If not the first layer, copy everything back and then
                    // reset the drawing and read layers back to the normal
                    // rendering setup.
                    if(!first_layer)
                    {
                        gl.glDrawBuffer(GL.GL_BACK);
                        gl.glRasterPos2i(data.viewport[GraphicsEnvironmentData.VIEW_X],
                                         data.viewport[GraphicsEnvironmentData.VIEW_Y]);

                        gl.glCopyPixels(0,
                                        0,
                                        data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                                        data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT],
                                        GL.GL_COLOR);
                        gl.glReadBuffer(GL.GL_BACK);
                    }
                    break;

                case RenderOp.START_MULTIPASS_PASS:
                    if(clear_buffer_bits != 0)
                        gl.glClear(clear_buffer_bits);

                    data = environmentList[data_index];
                    preMPPassEnvironmentDraw(gl, data);
                    break;

                case RenderOp.STOP_MULTIPASS_PASS:
                    data = environmentList[data_index];
                    data_index++;
                    postMPPassEnvironmentDraw(gl, data);
                    break;

                case RenderOp.START_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.setBufferState(gl);

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    break;

                case RenderOp.SET_BUFFER_CLEAR:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    else
                        clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.CHANGE_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.updateBufferState(gl);

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    else
                        clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.STOP_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.clearBufferState(gl);
                    clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.START_LAYER:
                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
                    layer_data_index = data_index;
                    data = environmentList[layer_data_index];

                    fog_active = data.fog != null;
                    current_fog = data.fog;

                    preLayerEnvironmentDraw(gl, data, left);
                    break;

                case RenderOp.STOP_LAYER:
                    data = environmentList[layer_data_index];

                    postLayerEnvironmentDraw(gl, data, profilingData);
                    fog_active = false;
                    first_layer = false;
                    data_index++;
                    layer_data_index = data_index;
                    break;

                case RenderOp.START_VIEWPORT:
                    data = environmentList[layer_data_index];
                    setupViewport(gl, data);
                    break;

                case RenderOp.STOP_VIEWPORT:
                    break;

                case RenderOp.START_RENDER:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_RENDER:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_GEOMETRY:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);
                    ((GeometryRenderable)renderableList[i].renderable).render(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_GEOMETRY_2D:
                    // load the matrix to render
                    gl.glRasterPos2f(renderableList[i].transform[3],
                                     renderableList[i].transform[7]);
                    gl.glPixelZoom(renderableList[i].transform[0],
                                   renderableList[i].transform[5]);
                    ((GeometryRenderable)renderableList[i].renderable).render(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_CUSTOM_GEOMETRY:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);
                    CustomGeometryRenderable gr =
                        (CustomGeometryRenderable)renderableList[i].renderable;
                    gr.render(gl, renderableList[i].instructions);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_CUSTOM:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);

                    CustomRenderable cr =
                        (CustomRenderable)renderableList[i].renderable;
                    cr.render(gl, renderableList[i].instructions);
                    gl.glPopMatrix();
                    break;

                case RenderOp.START_STATE:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_STATE:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);
                    break;

                case RenderOp.START_LIGHT:
                    // Get the next available light ID
                    if(lastLightIdx >= availableLights.length)
                        continue;

                    Integer l_id = availableLights[lastLightIdx++];
                    lightIdMap.put(renderableList[i].id, l_id);

                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);
                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, l_id);
                    gl.glPopMatrix();
                    break;

                case RenderOp.STOP_LIGHT:
                    if(lastLightIdx >= availableLights.length)
                        continue;

                    l_id = (Integer)lightIdMap.remove(renderableList[i].id);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.postRender(gl, l_id);
                    availableLights[--lastLightIdx] = l_id;
                    break;

                case RenderOp.START_CLIP_PLANE:
                    // Get the next available clip plane ID
                    if(lastClipIdx >= availableClips.length)
                        continue;

                    Integer c_id = availableClips[lastClipIdx++];
                    clipIdMap.put(renderableList[i].id, c_id);

                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixf(renderableList[i].transform, 0);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, c_id);
                    gl.glPopMatrix();
                    break;

                case RenderOp.STOP_CLIP_PLANE:
                    if(lastClipIdx >= availableClips.length)
                        continue;

                    c_id = (Integer)clipIdMap.remove(renderableList[i].id);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.postRender(gl, c_id);

                    availableClips[--lastClipIdx] = c_id;
                    break;

                case RenderOp.START_TRANSPARENT:
                    gl.glDepthMask(false);
                    gl.glEnable(GL.GL_BLEND);
                    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                    break;

                case RenderOp.STOP_TRANSPARENT:
                    gl.glDisable(GL.GL_BLEND);
                    gl.glDepthMask(true);
                    break;

                case RenderOp.START_ALPHATEST:
                    gl.glEnable(GL.GL_ALPHA_TEST);
                    gl.glAlphaFunc(GL.GL_GEQUAL, alphaCut);
                    break;
                    
                case RenderOp.STOP_ALPHATEST:
                    gl.glDisable(GL.GL_ALPHA_TEST);
                    break;

                case RenderOp.START_FOG:
                    if(!fog_active)
                    {
                        gl.glEnable(GL.GL_FOG);
                        fog_active = true;
                    }

                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_FOG:
                    if(current_fog != null)
                        current_fog.render(gl);
                    else
                    {
                        obj = (ObjectRenderable)renderableList[i].renderable;
                        obj.postRender(gl);
                        fog_active = false;
                        gl.glDisable(GL.GL_FOG);
                    }
                    break;

                case RenderOp.START_SHADER_PROGRAM:
                    ShaderComponentRenderable prog =
                        (ShaderComponentRenderable)renderableList[i].renderable;

                    if(!prog.isValid(gl))
                    {
                        currentShaderProgramId = INVALID_SHADER;
                        continue;
                    }

// TODO: Optimise this to avoid the allocation. Use IntHashMap for lookup.
                    currentShaderProgramId = new Integer(prog.getProgramId(gl));
                    prog.render(gl);
                    break;

                case RenderOp.STOP_SHADER_PROGRAM:
                    if(currentShaderProgramId == INVALID_SHADER)
                        continue;

                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);

                    currentShaderProgramId = INVALID_SHADER;
                    break;

                case RenderOp.SET_SHADER_ARGS:
                    if(currentShaderProgramId == INVALID_SHADER)
                        continue;

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, currentShaderProgramId);
                    break;

                 case RenderOp.START_TEXTURE:
                    comp = (ComponentRenderable)renderableList[i].renderable;
                    Integer id = (Integer)(renderableList[i].instructions);
                    comp.render(gl, id);
                    break;

                case RenderOp.STOP_TEXTURE:
                    comp = (ComponentRenderable)renderableList[i].renderable;
                    id = (Integer)(renderableList[i].instructions);
                    comp.postRender(gl, id);
                    break;
           }
        }

        if(terminate)
            return;

        gl.glFlush();
    }

    /**
     * Setup the viewport environment to be drawn, but do not yet set up the
     * viewpoint and other per-layer-specific effects. If a viewport has
     * multiple layers, then each layer could potentially have a different
     * viewpoint etc.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void setupViewport(GL gl, GraphicsEnvironmentData data)
    {
        if(!data.useStereo)
        {
            super.setupViewport(gl, data);
        }
        else
        {
            gl.glViewport(data.viewport[GraphicsEnvironmentData.VIEW_X],
                          data.viewport[GraphicsEnvironmentData.VIEW_Y],
                          data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                          data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT]);
        }
    }

    /**
     * Setup the view environment data for drawing now.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     * @param left true if this is the left eye
     */
    protected void preLayerEnvironmentDraw(GL gl,
                                          GraphicsEnvironmentData data,
                                          boolean left)
    {
        if(!data.useStereo)
        {
            super.preLayerEnvironmentDraw(gl, data);
        }
        else
        {
            if(data.effectsProcessor != null)
                data.effectsProcessor.preDraw(gl, data.userData);

            if(data.background != null)
            {
                // If it is colour only, then don't bother with rest of the
                // the projection setup. It will be wasted.
                if(((BackgroundRenderable)data.background).is2D())
                {
                    data.background.render(gl);
                    data.background.postRender(gl);
                }
                else
                {
                    gl.glMatrixMode(GL.GL_PROJECTION);
                    gl.glPushMatrix();
                    gl.glLoadIdentity();

                    gl.glFrustum(data.backgroundFrustum[0],
                                 data.backgroundFrustum[1],
                                 data.backgroundFrustum[2],
                                 data.backgroundFrustum[3],
                                 data.backgroundFrustum[4],
                                 data.backgroundFrustum[5]);

                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

                    gl.glMatrixMode(GL.GL_MODELVIEW);
                    gl.glPushMatrix();

                    gl.glLoadMatrixf(data.backgroundTransform, 0);

                    gl.glDepthMask(false);
                    gl.glDisable(GL.GL_BLEND);
                    gl.glDisable(GL.GL_DEPTH_TEST);

                    data.background.render(gl);
                    data.background.postRender(gl);

                    gl.glDepthMask(true);
                    gl.glEnable(GL.GL_DEPTH_TEST);
                    gl.glEnable(GL.GL_BLEND);
                    gl.glPopMatrix();

                    gl.glMatrixMode(GL.GL_PROJECTION);
                    gl.glPopMatrix();
                    gl.glMatrixMode(GL.GL_MODELVIEW);
                }
            }

            renderViewpoint(gl, data, left);

            if(data.fog != null)
            {
                gl.glEnable(GL.GL_FOG);
                data.fog.render(gl);
            }
        }
    }

    /**
     * Render the viewpoint setup.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     * @param left true if this is the left eye
     */
    private void renderViewpoint(GL gl,
                                 GraphicsEnvironmentData data,
                                 boolean left)

    {
        updateProjectionMatrix(gl, data, left);

        gl.glLoadIdentity();

        if(left)
            gl.glTranslated(0, 0, -eyeSeparation);
        else
            gl.glTranslated(0, 0, eyeSeparation);

        data.viewpoint.render(gl);

        gl.glMultMatrixf(data.cameraTransform, 0);
    }

    /**
     * Update the projection matrix.
     *
     * @param gl The gl context to draw with
     * @param left true if this is the left eye
     */
    private void updateProjectionMatrix(GL gl,
                                        GraphicsEnvironmentData data,
                                        boolean left)
    {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        switch(data.viewProjectionType)
        {
            case ViewEnvironmentCullable.PERSPECTIVE_PROJECTION:
                if(left)
                {
                    gl.glFrustum(data.viewFrustum[0] - eyeSeparation,
                                 data.viewFrustum[1],
                                 data.viewFrustum[2],
                                 data.viewFrustum[3] - eyeSeparation,
                                 data.viewFrustum[4],
                                 data.viewFrustum[5]);
                }
                else
                {
                    gl.glFrustum(data.viewFrustum[0] + eyeSeparation,
                                 data.viewFrustum[1],
                                 data.viewFrustum[2],
                                 data.viewFrustum[3] + eyeSeparation,
                                 data.viewFrustum[4],
                                 data.viewFrustum[5]);
                }
                break;

            case ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION:
                // TODO:
                // EEK! Orthographic stereo projection. Ah.... doesn't sound
                // right, but not sure what to do about it here. Should we
                // change this and try to use the toe-in method for this style
                // or close our eyes and hope?

                // NOTE:
                // should this not be 0, 0 for the start, and perhaps
                // using the glViewport x and y settings? - JC

                gl.glOrtho(0,
                           data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                           0,
                           data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT],
                           data.viewFrustum[4],
                           data.viewFrustum[5]);
                break;

            default:
                System.out.println("unknown projection type");
        }

        gl.glMatrixMode(GL.GL_MODELVIEW);
    }
}
