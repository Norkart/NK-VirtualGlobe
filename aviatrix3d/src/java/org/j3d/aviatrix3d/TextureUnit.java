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
import java.util.HashMap;

import javax.vecmath.Matrix4f;

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.OffscreenCullable;
import org.j3d.aviatrix3d.rendering.TextureRenderable;

/**
 * Describes a texture stage and its associated texture and attributes.
 * <p>
 *
 * The external data passed to the ComponentRenderable calls shall be an
 * <code>Integer</code> instance that represents the GL texture unit of the
 * this instance is working with. If the value is -1, then only a single
 * texture is being used, so the class does not need to make use of the
 * <code>glActiveTexture()</code> call.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.43 $
 */
public class TextureUnit extends NodeComponent
    implements TextureRenderable
{
    /**
     * Global flag to know if we are capable of rendering multitextures.
     * This gets queried on the first time rendering is run and set
     * appropriately. After that, if this is set to false, then any
     * texture unit that has it's ID greater than 0 is just ignored.
     */
    private static boolean hasMultiTextureAPI;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete;

    /** The Texture for the unit. */
    private Texture texture;

    /** The GL type of the texture */
    private int textureType;

    /** The Texture Attributes for the unit. */
    private TextureAttributes tatts;

    /** The Texture Coordinate Generation for the unit. */
    private TexCoordGeneration coordGen;

    /** Flag to say the global state has changed */
    private boolean stateChanged;

    /** Flag to say we have a texture matrix worth pushing to GL */
    private boolean validMatrix;

    /** The current texture transform */
    private float[] texTransform;

    /**
     * Constructs a Texture Unit with default values.
     */
    public TextureUnit()
    {
        stateChanged = false;
        validMatrix = false;

        texTransform = new float[16];
        texTransform[0] = 1;
        texTransform[5] = 1;
        texTransform[10] = 1;
        texTransform[15] = 1;
    }

    /**
     * Construct a Texture Unit with the specified texture, attributes
     * and coordinate generation.
     *
     * @param t The texture instance to use
     * @param attrs Attributes used to control the visual appearance
     * @param tcg Automated texture coordinate generation, if needed
     */
    public TextureUnit(Texture t,
                       TextureAttributes attrs,
                       TexCoordGeneration tcg)
    {
        this();

        texture = t;

        if(texture != null)
            textureType = texture.getTextureType();

        tatts = attrs;
        coordGen = tcg;
        stateChanged = true;
    }

    //----------------------------------------------------------
    // Methods defined by ScenegraphObject
    //----------------------------------------------------------

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
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        if(texture != null)
            texture.checkForCyclicChild(parent);
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

        if(texture != null)
            texture .setUpdateHandler(updateHandler);

        if(tatts != null)
            tatts.setUpdateHandler(updateHandler);

        if(coordGen != null)
            coordGen.setUpdateHandler(updateHandler);
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

            if(texture != null)
                texture.setLive(state);

            if(tatts != null)
                tatts.setLive(state);

            if(coordGen != null)
                coordGen.setLive(state);
        }
    }

    //----------------------------------------------------------
    // Methods defined by TextureRenderable
    //----------------------------------------------------------

    /**
     * Check to see if the contained texture is an offscreen renderable such as
     * a pbuffer or multipass texture. If is is, then the XXXX method will return
     * the contained cullable.
     *
     * @return true if the texture contains an offscreen source
     */
    public boolean isOffscreenSource()
    {
        return texture instanceof OffscreenCullable;
    }

    /**
     * Fetch the offscreen texture source that this renderable holds on to. If this 
     * node does not contain an offscreen texture then return null.
     *
     * @return The Cullable instance for the offscreen, if available
     */
    public OffscreenCullable getOffscreenSource()
    {
        if(texture instanceof OffscreenCullable)
            return (OffscreenCullable)texture;
        else
            return null;
    }

    //----------------------------------------------------------
    // Methods defined by ComponentRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     * @param stageId The ID of the texture stage we're reading
     */
    public void render(GL gl, Object stageId)
    {
        if(!queryComplete)
        {
             hasMultiTextureAPI = gl.isFunctionAvailable("glActiveTexture");
             queryComplete = true;

             if(!hasMultiTextureAPI)
                System.out.println("Video card incapable of supporting " +
                                   "multitexture, Allowing single texture only");
        }

        if(stateChanged)
        {
            stateChanged = false;

            // If there is no texture, no point going on
            if(texture == null)
                return;

            if(hasMultiTextureAPI)
            {
                if(((Integer)stageId).intValue() >= 0)
                    gl.glActiveTexture(((Integer)stageId).intValue());
                else
                    gl.glActiveTexture(GL.GL_TEXTURE0);
            }
            else if(((Integer)stageId).intValue() > 0)
                return;

            // Setup TextureAttribute state
            if(tatts != null)
                tatts.render(gl);
            else
                gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                             GL.GL_TEXTURE_ENV_MODE,
                             GL.GL_REPLACE);

            if(validMatrix)
            {
                gl.glMatrixMode(GL.GL_TEXTURE);
                gl.glPushMatrix();

                gl.glMultMatrixf(texTransform, 0);
                gl.glMatrixMode(GL.GL_MODELVIEW);
            }

            // Setup Texture state
            texture.render(gl);

            gl.glEnable(textureType);

            // Setup TexCoordGeneration state
            if(coordGen != null)
                coordGen.render(gl);
        }
        else if(texture != null && texture.hasValidData())
        {
            if(hasMultiTextureAPI)
            {
                if(((Integer)stageId).intValue() >= 0)
                    gl.glActiveTexture(((Integer)stageId).intValue());
                else
                    gl.glActiveTexture(GL.GL_TEXTURE0);
            }
            else if(((Integer)stageId).intValue() > 0)
                return;

            gl.glEnable(textureType);

            if(tatts != null)
                tatts.render(gl);
            else
                gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                             GL.GL_TEXTURE_ENV_MODE,
                             GL.GL_REPLACE);

            if(validMatrix)
            {
                gl.glMatrixMode(GL.GL_TEXTURE);
                gl.glPushMatrix();
                gl.glMultMatrixf(texTransform, 0);
                gl.glMatrixMode(GL.GL_MODELVIEW);
            }

            if(coordGen != null)
                coordGen.render(gl);

            texture.render(gl);
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     * @param stageId The ID of the texture stage we're reading
     */
    public void postRender(GL gl, Object stageId)
    {
        if(texture != null)
        {
            if(hasMultiTextureAPI)
            {
                if(((Integer)stageId).intValue() >= 0)
                    gl.glActiveTexture(((Integer)stageId).intValue());
                else
                    gl.glActiveTexture(GL.GL_TEXTURE0);
            }
            else if(((Integer)stageId).intValue() > 0)
                return;


            if(tatts != null)
                tatts.postRender(gl);

            if(coordGen != null)
                coordGen.postRender(gl);

            if(validMatrix)
            {
                gl.glMatrixMode(GL.GL_TEXTURE);
                gl.glPopMatrix();
                gl.glMatrixMode(GL.GL_MODELVIEW);
            }

            texture.postRender(gl);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        TextureUnit tu = (TextureUnit)o;
        return compareTo(tu);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof TextureUnit))
            return false;
        else
            return equals((TextureUnit)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the texture for this stage. Passing a value of null will disable
     * this stage from being passed to the rendering APIs.
     *
     * @param tex The texture to be used or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setTexture(Texture tex)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(texture != null)
            texture.setLive(false);

        texture = tex;

        if(texture != null)
        {
            texture.setUpdateHandler(updateHandler);
            textureType = texture.getTextureType();
            texture.setLive(alive);
        }

        stateChanged = true;
    }

    /**
     * Get the currently set texture. If none is set, return null.
     *
     * @return The current texture instance or null
     */
    public Texture getTexture()
    {
        return texture;
    }

    /**
     * Set the texture attributes for this stage.
     *
     * @param attrs The texture attributes.  Null clears.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setTextureAttributes(TextureAttributes attrs)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(tatts != null)
            tatts.setLive(false);

        tatts = attrs;

        if(tatts != null)
        {
            tatts.setUpdateHandler(updateHandler);
            tatts.setLive(alive);
        }

        stateChanged = true;
    }

    /**
     * Get the currently set texture attributes. If none is set, return null.
     *
     * @return The current texture attributes instance or null
     */
    public TextureAttributes getTextureAttributes()
    {
        return tatts;
    }

    /**
     * Set the texture coordinate generation for this stage.
     *
     * @param tcg The texture coordinate generation.  Null clears.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setTexCoordGeneration(TexCoordGeneration tcg)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(coordGen != null)
            coordGen.setLive(false);

        coordGen = tcg;

        if(coordGen != null)
        {
            coordGen.setUpdateHandler(updateHandler);
            coordGen.setLive(alive);
        }

        stateChanged = true;
    }

    /**
     * Get the currently set texture. If none is set, return null.
     *
     * @return The current texture instance or null
     */
    public TexCoordGeneration getTexCoordGeneration()
    {
        return coordGen;
    }

    /**
     * Set the current texture transform matrix. A value of null will reset
     * the matrix back to the default identity matrix. A copy of this matrix
     * is made.
     *
     * @param mat The matrix to modify textures by, or null
     */
    public void setTextureTransform(Matrix4f mat)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(mat == null)
        {
            validMatrix = false;
            texTransform[0] = 1;
            texTransform[4] = 0;
            texTransform[8] = 0;
            texTransform[12] = 0;

            texTransform[1] = 0;
            texTransform[5] = 1;
            texTransform[9] = 0;
            texTransform[13] = 0;

            texTransform[2] = 0;
            texTransform[6] = 0;
            texTransform[10] = 1;
            texTransform[14] = 0;

            texTransform[3] = 0;
            texTransform[7] = 0;
            texTransform[11] = 0;
            texTransform[15] = 1;
        }
        else
        {
            validMatrix = true;

            // Transpose while copying.
            texTransform[0] = mat.m00;
            texTransform[4] = mat.m01;
            texTransform[8] = mat.m02;
            texTransform[12] = mat.m03;

            texTransform[1] = mat.m10;
            texTransform[5] = mat.m11;
            texTransform[9] = mat.m12;
            texTransform[13] = mat.m13;

            texTransform[2] = mat.m20;
            texTransform[6] = mat.m21;
            texTransform[10] = mat.m22;
            texTransform[14] = mat.m23;

            texTransform[3] = mat.m30;
            texTransform[7] = mat.m31;
            texTransform[11] = mat.m32;
            texTransform[15] = mat.m33;
        }
    }

    /**
     * Get the current local texture transformation maxtix. If no matrix is
     * currently set, it will set the value to an identity matrix.
     *
     * @param mat The matrix to copy the current values into
     */
    public void getTextureTransform(Matrix4f mat)
    {
        // Transpose while copying.
        mat.m00 = texTransform[0];
        mat.m01 = texTransform[4];
        mat.m02 = texTransform[8];
        mat.m03 = texTransform[12];

        mat.m10 = texTransform[1];
        mat.m11 = texTransform[5];
        mat.m12 = texTransform[9];
        mat.m13 = texTransform[13];

        mat.m20 = texTransform[2];
        mat.m21 = texTransform[6];
        mat.m22 = texTransform[10];
        mat.m23 = texTransform[14];

        mat.m30 = texTransform[3];
        mat.m31 = texTransform[7];
        mat.m32 = texTransform[11];
        mat.m33 = texTransform[15];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param tu The texture unit instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(TextureUnit tu)
    {
        if(tu == null)
            return 1;

        if(tu == this)
            return 0;

        if(texture != tu.texture)
        {
            if(texture == null)
               return -1;
            else if(tu.texture == null)
                return 1;

            int res = texture.compareTo(tu.texture);
            if(res != 0)
                return res;
        }

        // Same type of texture. Hmmmm.... let's now work on
        // the texture attributes.
        if(tatts != tu.tatts)
        {
            if(tatts == null)
                return -1;
            else if(tu.tatts == null)
                return 1;

            int res = tatts.compareTo(tu.tatts);
            if(res != 0)
                return res;
        }

        // Ah, well ok, Now let's try coordinate generation
        if(coordGen != tu.coordGen)
        {
            if(coordGen == null)
                return -1;
            else if(tu.coordGen == null)
                return 1;

            int res = coordGen.compareTo(tu.coordGen);
            if(res != 0)
                return res;
        }

        // Gah! Matrix time now as that's the only thing left
        if(validMatrix != tu.validMatrix)
            return validMatrix ? 1 : -1;

        for(int i = 0; i < 16; i++)
        {
            if(texTransform[i] != tu.texTransform[i])
                return texTransform[i] < tu.texTransform[i] ? -1 : 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tu The texture unit instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(TextureUnit tu)
    {
        if(tu == this)
            return true;

        if(tu == null)
            return false;

        if((texture != tu.texture) &&
           ((texture == null) || !texture.equals(tu.texture)))
            return false;

        if((tatts != tu.tatts) && (tatts == null || !tatts.equals(tu.tatts)))
            return false;

        if((coordGen != tu.coordGen) &&
           ((coordGen == null) || !coordGen.equals(tu.coordGen)))
            return false;

        if(validMatrix != tu.validMatrix)
            return false;

        for(int i = 0; i < 16; i++)
        {
            if(texTransform[i] != tu.texTransform[i])
                return false;
        }

        return true;
    }


    /**
     * Convenience method to check if this code has detected the prescense of
     * multitexture extensions. If none are found, this will return null.
     * However, one node instance has to have passed through the rendering
     * cycle for this to have detected it. A better option would be to make use
     * of the appropriate callbacks on the GraphicsOutputDevice APIs to detect
     * before you get to this point.
     *
     * @return true if multitexture is allowed
     */
    public boolean isMultiTextureAllowed()
    {
        return hasMultiTextureAPI;
    }
}
