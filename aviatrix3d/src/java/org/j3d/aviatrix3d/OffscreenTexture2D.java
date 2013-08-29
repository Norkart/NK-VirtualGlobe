/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLPbuffer;

// Local imports
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.OffscreenCullable;

/**
 * Describes the texture that is rendered to an offscreen buffer, using OpenGL
 * pBuffers.
 * <p>
 * Since the format information is provided as part of the GLCapabilities
 * instance, there is no need for it to be passed into the constructor like the
 * other texture types do.
 * <p>
 * This implementation is somewhat shaky right now as the documentation on how
 * to use the GLPbuffer class is very poor. The class may change drastically
 * once we really work out what is going on.
 * <p>
 * Since an offscreen rendering process has a completely different set of viewing
 * parameters to the main scene, we have to include almost everything here as
 * well. A complete viewing environment must be set up to deal with the texture
 * rendering.
 * <p>
 * For repainting updates, the logic is as-follows. On the first time this
 *
 * TODO:<br>
 * If the scene is set but without root geometry, then the root geometry added
 * later, then the update handler is not correctly dealt with. It never gets
 * set. To overcome this, make sure you set a root group node before setting the
 * scene, even if it is just a proxy value.
 *
 * @author Justin Couch
 * @version $Revision: 1.27 $
 */
public class OffscreenTexture2D extends Texture
    implements PBufferTextureSource, OffscreenCullable
{
    /** The height of the main texture. */
    private int height;

    /** The boundary mode S value */
    private int boundaryModeT;

    /** The Scene Graph renderableObjects */
    private Layer[] layers;

    /** The number of valid layers to render */
    private int numLayers;

    /** The current clear colour */
    private float[] clearColor;

    /** Flag for the per-frame repaint setup */
    private boolean repaintNeeded;

    /** Capabilities setup for this renderer */
    private final GLCapabilities capabilities;

    /** Maps the GL context to an already created PBuffer */
    private HashMap displayListMap;

    /**
     * Constructs an offscreen texture that fits the given setup. All values
     * must be valid and non-negative.
     */
    public OffscreenTexture2D(GLCapabilities caps,
                              int width,
                              int height)
    {
        super(GL.GL_TEXTURE_2D);

        if(caps == null)
            throw new IllegalArgumentException("Capabilities must be provided");

        capabilities = caps;
        numSources = 0;
        this.height = height;
        this.width = width;

        clearColor = new float[4];
        boundaryModeT = BM_CLAMP;
        displayListMap = new HashMap();
        layers = new Layer[0];
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenCullable
    //---------------------------------------------------------------

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public LayerCullable getCullableLayer(int layerIndex)
    {
        if(layerIndex < 0 || layerIndex >= numLayers)
            return null;

        if(layers[layerIndex] == null)
            return null;

        // TODO:
        // When we go to full internal cullable representations then this should
        // pass in the correct buffer ID that has been passed down from on high.
        return layers[layerIndex].getCullable(0);
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numCullableChildren()
    {
        return numLayers;
    }

    //---------------------------------------------------------------
    // Methods defined by Texture
    //---------------------------------------------------------------

    /**
     * Set the images for this texture, overridden to provide an empty
     * implementation as this is handled by the pBuffer directly.
     *
     * @param mipMapMode Flag stating the type of texture mode to use
     * @param format Image format to use for grayscale images
     * @param texSources The source data to use, single for base level
     * @param num The valid number of images to use from the array
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSources(int mipMapMode,
                           int format,
                           TextureSource[] texSources,
                           int num)
        throws InvalidWriteTimingException
    {
    }

    //---------------------------------------------------------------
    // Methods defined by TextureSource
    //---------------------------------------------------------------

    /**
     * Get the format for this texture. As this is a single pBuffer texture,
     * there is only ever one level, so the argument is ignored.
     *
     * @param level The mipmap level to get the format for
     * @return The format.
     */
    public int getFormat(int level)
    {
        return format;
    }

    /**
     * Get the number of levels for the mipmapping in this source.
     *
     * @return The number of levels.
     */
    public int getNumLevels()
    {
        return 1;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        GLPbuffer pbuffer = (GLPbuffer)displayListMap.get(gl);

        if(pbuffer == null)
            return;

        if(!stateChanged.getState(gl))
        {
// JC: Moving to the JSR code, the isInitialised() method is now gone.
// Do we always assume that the pbuffer is initialised by now?
//            if(pbuffer.isInitialized())
            pbuffer.bindTexture();

            return;
        }
        else
        {
            stateChanged.put(gl, false);

            pbuffer.bindTexture();

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_S,
                               boundaryModeS);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_T,
                               boundaryModeT);
            int mode = 0;
            switch(magFilter)
            {
                case MAGFILTER_FASTEST:
                case MAGFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;

                case MAGFILTER_NICEST:
                case MAGFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                    break;

                default:
                    System.out.println("Unknown mode in MagFilter: " + magFilter);
            }

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MAG_FILTER,
                               mode);

            switch(minFilter)
            {
                case MINFILTER_FASTEST:
                case MINFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;

                case MINFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                    break;

                case MINFILTER_MULTI_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR_MIPMAP_LINEAR;
                    break;

                case MINFILTER_MULTI_LEVEL_POINT:
                    mode = GL.GL_NEAREST_MIPMAP_NEAREST;
                    break;

                case MINFILTER_NICEST:
                    mode = (numSources > 1) ?
                           GL.GL_LINEAR_MIPMAP_LINEAR :
                           GL.GL_LINEAR;
                    break;

                default:
                    System.out.println("Unknown mode in MinFilter: " + minFilter);
            }

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if(anisotropicMode != ANISOTROPIC_MODE_NONE)
            {

                // float[] val = new float[1];
                //gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, val);
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            if(priority >= 0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_PRIORITY,
                                   priority);
            }

            if(borderColor != null)
            {
                gl.glTexParameterfv(GL.GL_TEXTURE_2D,
                                    GL.GL_TEXTURE_BORDER_COLOR,
                                    borderColor,
                                    0);
            }

            if(format == FORMAT_DEPTH_COMPONENT)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_DEPTH_TEXTURE_MODE,
                                   depthComponentMode);

                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_COMPARE_MODE,
                                   compareMode);

                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_COMPARE_FUNC,
                                   compareFunction);
            }
        }
    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        GLPbuffer pbuffer = (GLPbuffer)displayListMap.get(gl);

        if(pbuffer != null)
            pbuffer.releaseTexture();
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < numLayers; i++)
        {
            if(layers[i] != null)
                layers[i].checkForCyclicChild(parent);
        }
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            super.setLive(state);

            for(int i = 0; i < numLayers; i++)
            {
                if(layers[i] != null)
                    layers[i].setLive(state);
            }
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i = 0; i < numLayers; i++)
        {
            if(layers[i] != null)
                layers[i].setUpdateHandler(updateHandler);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by PBufferTextureSource
    //---------------------------------------------------------------

    /**
     * Get the requested buffer setup that describes this offscreen texture.
     *
     * @return The defined capabilities setup for the texture
     */
    public GLCapabilities getGLSetup()
    {
        return capabilities;
    }

    /**
     * Get the currently registered pBuffer for the given key object. If there
     * is no buffer registered for the current context, return null.
     *
     * @param obj The key used to register the buffer with
     * @return buffer The buffer instance to use here.
     */
    public GLPbuffer getBuffer(Object obj)
    {
        return (GLPbuffer)displayListMap.get(obj);
    }

    /**
     * Register a pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     * @param buffer The buffer instance to use here.
     */
    public void registerBuffer(Object obj, GLPbuffer buffer)
    {
        displayListMap.put(obj, buffer);
    }

    /**
     * Remove an already registered pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     */
    public void unregisterBuffer(Object obj)
    {
        displayListMap.remove(obj);
    }

    /**
     * Set the collection of geometry that should be rendered to this
     * texture. The geometry is, in effect, a completely separate rendarable
     * space, with it's own culling and sorting pass. In addition, a check
     * is made to make sure that no cyclic scene graph structures are created,
     * as this can create really major headachesfor nested surface rendering.
     * A null value will clear the current geometry and result in only
     * rendering the background, if set. if not set, then whatever the default
     * colour is, is used (typically black).
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     * @throws CyclicSceneGraphStructureException Equal parent and child
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setLayers(Layer[] layers, int numLayers)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Make sure things are valid first
        for(int i = 0; i < numLayers; i++)
            layers[i].checkForCyclicChild(this);

        int cur_size = this.layers.length;

        // clean up the old list first
        for(int i = 0; i < cur_size; i++)
        {
            if(alive)
                this.layers[i].setLive(false);
            this.layers[i].setUpdateHandler(null);
        }

        if(cur_size < numLayers)
            this.layers = new Layer[numLayers];

        if(numLayers != 0)
            System.arraycopy(layers, 0, this.layers, 0, numLayers);

        for(int i = numLayers; i < cur_size; i++)
            layers[i] = null;

        this.numLayers = numLayers;

        // clean up the old list first
        for(int i = 0; i < numLayers; i++)
        {
            this.layers[i].setLive(alive);
            this.layers[i].setUpdateHandler(updateHandler);
        }
    }

    /**
     * Get the number of layers that are currently set. If no layers are set,
     * or a scene is set, this will return zero.
     *
     * @return a value greater than or equal to zero
     */
    public int numLayers()
    {
        return numLayers;
    }

    /**
     * Fetch the current layers that are set. The values will be copied into
     * the user-provided array. That array must be at least
     * {@link #numLayers()} in length. If not, this method does nothing (the
     * provided array will be unchanged).
     *
     * @param layers An array to copy the values into
     * @throws IllegalArgumentException The array provided is too small or null
     */
    public void getLayers(Layer[] layers)
        throws IllegalArgumentException
    {
        if((layers == null) || (layers.length < numLayers))
            throw new IllegalArgumentException("Array provided is too small");

        System.arraycopy(layers, 0, this.layers, 0, numLayers);
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenTextureSource
    //---------------------------------------------------------------

    /**
     * Get the current state of the repainting enabled flag.
     *
     * @return true when the texture requires re-drawing
     */
    public boolean isRepaintRequired()
    {
        return repaintNeeded;
    }

    /**
     * Get the height of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param col An array of at least length 4 to copy values into
     */
    public void getClearColor(float[] col)
    {
        col[0] = clearColor[0];
        col[1] = clearColor[1];
        col[2] = clearColor[2];
        col[3] = clearColor[3];
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set this texture as requiring a repaint for the next frame. If no
     * repaint is required, reset this to null at the point where no
     * repainting is required. The internal flag is a user-defined state,
     * so For the first frame at least, this should be set to true so that
     * the initial paint can be performed (assuming data is present, of
     * course).
     *
     * @param enable true to have this repaint the next frame
     */
    public void setRepaintRequired(boolean enable)
    {
        repaintNeeded = enable;
    }

    /**
     * Set the boundary handling for the T parameter.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setBoundaryModeT(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        boundaryModeT = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current boundary handling for the S parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeT()
    {
        return boundaryModeT;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setClearColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
        clearColor[3] = a;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param tex The texture instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Texture tex)
    {
        int res = super.compareTo(tex);
        if(res != 0)
            return res;

        OffscreenTexture2D o2d = (OffscreenTexture2D)tex;

        if(numLayers != o2d.numLayers)
            return numLayers < o2d.numLayers ? -1 : 1;

        for(int i = 0; i < numLayers; i++)
        {
            res = layers[i].compareTo(o2d.layers[i]);
            if(res != 0)
                return res;
        }

        if(repaintNeeded != o2d.repaintNeeded)
            return repaintNeeded ? 1 : -1;

        if(height != o2d.height)
            return height < o2d.height ? -1 : 1;

        if(boundaryModeS != o2d.boundaryModeS)
            return boundaryModeS < o2d.boundaryModeS ? -1 : 1;

        if(boundaryModeT != o2d.boundaryModeT)
            return boundaryModeT < o2d.boundaryModeT ? -1 : 1;

        if(clearColor[0] != o2d.clearColor[0])
            return clearColor[0] < o2d.clearColor[0] ? -1 : 1;

        if(clearColor[1] != o2d.clearColor[1])
            return clearColor[1] < o2d.clearColor[1] ? -1 : 1;

        if(clearColor[2] != o2d.clearColor[2])
            return clearColor[2] < o2d.clearColor[2] ? -1 : 1;

        if(clearColor[3] != o2d.clearColor[3])
            return clearColor[3] < o2d.clearColor[3] ? -1 : 1;


        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tex The texture instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Texture tex)
    {
        if(!super.equals(tex))
            return false;

        if(!(tex instanceof OffscreenTexture2D))
            return false;

        OffscreenTexture2D o2d = (OffscreenTexture2D)tex;

        if((height != o2d.height) ||
           (boundaryModeS != o2d.boundaryModeS) ||
           (boundaryModeT != o2d.boundaryModeT) ||
           (numLayers != o2d.numLayers) ||
           (repaintNeeded != o2d.repaintNeeded) ||
           (clearColor[0] != o2d.clearColor[0]) ||
           (clearColor[1] != o2d.clearColor[1]) ||
           (clearColor[2] != o2d.clearColor[2]) ||
           (clearColor[3] != o2d.clearColor[3]))
            return false;

        // so the number of layers is the same, check to see if one of them
        // is not equal to the others.
        for(int i = 0; i < numLayers; i++)
        {
            if(!layers[i].equals(o2d.layers[i]))
                return false;
        }

        return true;
    }
}
