/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.AppearanceAttributeRenderable;

/**
 * Describes attributes used for blending any drawing primitives.
 * <p>
 *
 *
 * The default blending mode is set up to mimic the defaults used by OpenGL.
 * However, this class should only be used when the <i>GL_ARB_imaging</i>
 * subset is available. This code automatically checks for it's existance and
 * will disable calling itself if it detects the lack of existance.
 * <p>
 *
 * <b>Note:</b> If you have semi-transparent objects that need blending, you
 * do <i>not</i> need to provide an instance of this class. Aviatrix3D will
 * internally handle the correct settings that you need, so there is no need
 * to provide your own. Only use this when you want to blend objects with
 * something different than the standard blending setup for non-transparent
 * and transparent objects as it will remove this object from the normal
 * transparency sorting processing and into a separate state.
 * <p>
 *
 * <b>Default Values</b>
 * <p>
 *
 * When intialised, this class follows the default OpenGL values for blending:
 * <pre>
 * Blend Equation:    EQ_FUNC_ADD
 * RGB Source Mode:   BLEND_SRC_COLOR
 * RGB Dest Mode:     BLEND_ONE_MINUS_SRC_COLOR
 * Alpha Source Mode: BLEND_SRC_ALPHA
 * Alpha Dest Mode:   BLEND_ONE_MINUS_SRC_ALPHA
 * Separated Blend:   false
 * Blend colour not set
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class BlendAttributes extends NodeComponent
    implements AppearanceAttributeRenderable
{
    /** Message when the saturate value is set to the destination method */
    private static final String DEST_ALPHA_MSG =
        "BLEND_SRC_ALPHA_SATURATE is not permitted as a destination mode";

    /** Generic message for an unknown blend factor type */
    private static final String INVALID_BLEND_MSG =
        "The provided blend factor ID is not recognised by the system.";

    /** Set the blend factor to zeros. */
    public static final int BLEND_ZERO = GL.GL_ZERO;

    /** Set the blend factor to ones. */
    public static final int BLEND_ONE = GL.GL_ONE;

    /** Set the blend factor to use the source object colour */
    public static final int BLEND_SRC_COLOR = GL.GL_SRC_COLOR;

    /** Set the blend factor to use one minus source object colour (1-src). */
    public static final int BLEND_ONE_MINUS_SRC_COLOR =
        GL.GL_ONE_MINUS_SRC_COLOR;

    /** Set the blend factor to use the destination object colour */
    public static final int BLEND_DEST_COLOR = GL.GL_DST_COLOR;

    /**
     * Set the blend factor to use one minus the destination object colour
     * (1-dest).
     */
    public static final int BLEND_ONE_MINUS_DEST_COLOR =
        GL.GL_ONE_MINUS_DST_COLOR;

    /** Set the blend factor to use the source object's alpha value */
    public static final int BLEND_SRC_ALPHA = GL.GL_SRC_ALPHA;

    /** Set the blend factor to use one minus source object alpha value (1-src). */
    public static final int BLEND_ONE_MINUS_SRC_ALPHA = GL.GL_ONE_MINUS_SRC_ALPHA;

    /** Set the blend factor to use the destination object's alpha value */
    public static final int BLEND_DEST_ALPHA = GL.GL_DST_ALPHA;

    /**
     * Set the blend factor to use one minus the destination object alpha value
     * (1-dest).
     */
    public static final int BLEND_ONE_MINUS_DEST_ALPHA =
        GL.GL_ONE_MINUS_DST_ALPHA;

    /**
     * Set the blend factor to use the provided constant colour. The constant
     * colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_CONSTANT_COLOR = GL.GL_CONSTANT_COLOR;

    /**
     * Set the blend factor to use one minus the constant colour (1-c). The
     * constant colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_ONE_MINUS_CONSTANT_COLOR =
        GL.GL_ONE_MINUS_CONSTANT_COLOR;

    /**
     * Set the blend factor to use the provided constant alpha value.The
     * constant colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_CONSTANT_ALPHA = GL.GL_CONSTANT_ALPHA;

    /**
     * Set the blend factor to use one minus the constant colour alpha value
     * (1-c). The constant colour value is provide through the setBlendColour()
     * method.
     */
    public static final int BLEND_ONE_MINUS_CONSTANT_ALPHA =
        GL.GL_ONE_MINUS_CONSTANT_ALPHA;

    /**
     * Set the blend function to saturage the colour value using the alpha
     * state. The RGB value is calculated using the min(Asrc, 1 - Adest). The
     * alpha value is treated as equivalent to GL_ONE.
     * Can only be used as the source blend factor. An error will be generated
     * if you try to use this for the destination factor.
     */
    public static final int BLEND_SRC_ALPHA_SATURATE = GL.GL_SRC_ALPHA_SATURATE;

    /** Set the blending equation to be C<sub>s</sub>S + C<sub>d</sub>D */
    public static final int EQ_FUNC_ADD = GL.GL_FUNC_ADD;

    /** Set the blending equation to be C<sub>s</sub>S - C<sub>d</sub>D */
    public static final int EQ_FUNC_SUBTRACT = GL.GL_FUNC_SUBTRACT;

    /** Set the blending equation to be C<sub>d</sub>D - C<sub>s</sub>S */
    public static final int EQ_FUNC_SUBTRACT_REVERSE = GL.GL_FUNC_REVERSE_SUBTRACT;

    /** Set the blending equation to be min(C<sub>d</sub>D, C<sub>s</sub>S) */
    public static final int EQ_FUNC_MIN = GL.GL_MIN;

    /** Set the blending equation to be max(C<sub>d</sub>D, C<sub>s</sub>S) */
    public static final int EQ_FUNC_MAX = GL.GL_MAX;


    /**
     * Global flag to know if we are capable of rendering multitextures.
     * This gets queried on the first time rendering is run and set
     * appropriately. After that, if this is set to false, then any
     * texture unit that has it's ID greater than 0 is just ignored.
     */
    private static boolean hasImaging = false;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete;


    /** The source mode for combined and RGB for the separate blending */
    private int rgbSourceMode;

    /** The destination mode for combined and RGB for the separate blending */
    private int rgbDestMode;

    /** Equation for combined and RGB for the separate blending */
    private int blendEquation;


    /** Source mode to use for alpha when separate blending */
    private int alphaSourceMode;

    /** Destination mode to use for alpha when separate blending */
    private int alphaDestMode;

    /** Flag indicating if separate RGB and Alpha functions should be set */
    private boolean useSeparatedBlend;

    /** The currently set blend colour. Null if not set */
    private float[] blendColor;

    /**
     * General flag indicating that the current setup requires the blend
     * colour to be passed through to OpenGL during the rendering.
     */
    private boolean needBlendColor;

    /** Has an attribute changed */
    private boolean stateChanged;

    /** Map of display contexts to maps */
    private HashMap<GL, Integer> displayListMap;

    /**
     * Constructs a attribute set with default values as specified above.
     */
    public BlendAttributes()
    {
        blendEquation = EQ_FUNC_ADD;
        rgbSourceMode = BLEND_SRC_COLOR;
        rgbDestMode = BLEND_ONE_MINUS_SRC_COLOR;

        alphaSourceMode = BLEND_SRC_ALPHA;
        alphaDestMode = BLEND_ONE_MINUS_SRC_ALPHA;

        useSeparatedBlend = false;
        needBlendColor = false;
        stateChanged = true;

        displayListMap = new HashMap<GL, Integer>(1);
    }

    //---------------------------------------------------------------
    // Methods defined by AppearanceAttributeRenderable
    //---------------------------------------------------------------

    /**
     * Get the type this visual attribute represents.
     *
     * @return One of the _ATTRIBUTE constants
     */
    public int getAttributeType()
    {
        return BLEND_ATTRIBUTE;
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
        if(!queryComplete)
        {
             hasImaging = gl.isExtensionAvailable("GL_ARB_imaging");
             queryComplete = true;

             if(!hasImaging)
                System.out.println("Video card incapable of supporting " +
                                   "OpenGL imaging subset. Blending disabled.");
        }

        if(!hasImaging)
            return;

        // If we have changed state, then clear the old display lists
        if(stateChanged)
        {
            synchronized(displayListMap)
            {
                displayListMap.clear();
                stateChanged = false;
            }
        }

        Integer listName = (Integer)displayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE);

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendEquation(blendEquation);

            if(needBlendColor)
                gl.glBlendColor(blendColor[0],
                                blendColor[1],
                                blendColor[2],
                                blendColor[3]);

            if(useSeparatedBlend)
            {
                gl.glBlendFuncSeparate(rgbSourceMode,
                                       rgbDestMode,
                                       alphaSourceMode,
                                       alphaDestMode);
            }
            else
            {
                gl.glBlendFunc(rgbSourceMode, rgbDestMode);
            }

            gl.glEndList();
            displayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
    }

    /**
     * Restore all openGL state to the given drawable. Does nothing as it
     * assumes the system will disable blending outside of this class.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(!hasImaging)
            return;

        gl.glDisable(GL.GL_BLEND);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        BlendAttributes ta = (BlendAttributes)o;
        return compareTo(ta);
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
        if(!(o instanceof BlendAttributes))
            return false;
        else
            return equals((BlendAttributes)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the source blend factor to use. Used for both non-separated blending
     * and the RGB component when separated.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSourceBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                throw new IllegalArgumentException(INVALID_BLEND_MSG);
        }

        if(rgbSourceMode != factor)
        {
            rgbSourceMode = factor;
            stateChanged = true;

            needBlendColor = (rgbSourceMode == BLEND_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_CONSTANT_ALPHA ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA);
        }
    }

    /**
     * Request the currently set source blend factor.
     *
     * @return One of the BLEND_* constant values
     */
    public int getSourceBlendFactor()
    {
        return rgbSourceMode;
    }

    /**
     * Set the destination blend factor to use. Used for both non-separated
     * blending and the RGB component when separated.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types or is SRC_ALPHA_SATURATED
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setDestinationBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                throw new IllegalArgumentException(INVALID_BLEND_MSG);
        }

        if(factor == BLEND_SRC_ALPHA_SATURATE)
            throw new IllegalArgumentException(DEST_ALPHA_MSG);

        if(rgbDestMode != factor)
        {
            rgbDestMode = factor;
            stateChanged = true;

            needBlendColor = (rgbSourceMode == BLEND_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_CONSTANT_ALPHA ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA);
        }
    }

    /**
     * Request the currently set destination blend factor.
     *
     * @return One of the BLEND_* constant values
     */
    public int getDestinationBlendFactor()
    {
        return rgbDestMode;
    }

    /**
     * Set the source blend factor to use. Only used for seperated blending
     * mode's alpha component. If non-separated mode is used, this setting is
     * ignored.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setAlphaSourceBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                throw new IllegalArgumentException(INVALID_BLEND_MSG);
        }

        if(alphaSourceMode != factor)
        {
            alphaSourceMode = factor;
            stateChanged = true;

            needBlendColor = (rgbSourceMode == BLEND_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_CONSTANT_ALPHA ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA);
        }
    }

    /**
     * Request the currently set source blend factor for the alpha component.
     *
     * @return One of the BLEND_* constant values
     */
    public int getAlphaSourceBlendFactor()
    {
        return rgbSourceMode;
    }

    /**
     * Set the destination blend factor to use. Only used for seperated blending
     * mode's alpha component. If non-separated mode is used, this setting is
     * ignored.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types or is SRC_ALPHA_SATURATED
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setAlphaDestinationBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                throw new IllegalArgumentException(INVALID_BLEND_MSG);
        }

        if(factor == BLEND_SRC_ALPHA_SATURATE)
            throw new IllegalArgumentException(DEST_ALPHA_MSG);

        if(alphaDestMode != factor)
        {
            alphaDestMode = factor;
            stateChanged = true;

            needBlendColor = (rgbSourceMode == BLEND_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbSourceMode == BLEND_CONSTANT_ALPHA ||
                              rgbSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              rgbDestMode == BLEND_CONSTANT_ALPHA ||
                              rgbDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaSourceMode == BLEND_CONSTANT_ALPHA ||
                              alphaSourceMode == BLEND_ONE_MINUS_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_COLOR ||
                              alphaDestMode == BLEND_CONSTANT_ALPHA ||
                              alphaDestMode == BLEND_ONE_MINUS_CONSTANT_ALPHA);
        }
    }

    /**
     * Request the currently set destination blend factor for the alpha
     * component.
     *
     * @return One of the BLEND_* constant values
     */
    public int getAlphaDestinationBlendFactor()
    {
        return alphaDestMode;
    }

    /**
     * Instruct the system whether to use separated RGB and Alpha blending
     * functions. By default this is not enabled, but may be turned on or off
     * using this method.
     *
     * @param state True to enable the use of separate RGB and alpha blending
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSeparatedBlendFactors(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(useSeparatedBlend != state)
        {
            useSeparatedBlend = state;
            stateChanged = true;
        }
    }

    /**
     * Check to see the current state of whether separated blending is used or
     * not.
     *
     * @return true if separated RGB and Alpha blending is used
     */
    public boolean getSeparatedBlendFactors()
    {
        return useSeparatedBlend;
    }

    /**
     * Set the blend colour to use for this texture. Blend is a 4-component
     * colour value.
     *
     * @param r The red component of the blend colour
     * @param g The green component of the blend colour
     * @param b The blue component of the blend colour
     * @param a The alpha component of the blend colour
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setBlendColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(blendColor == null)
            blendColor = new float[4];

        blendColor[0] = r;
        blendColor[1] = g;
        blendColor[2] = b;
        blendColor[3] = a;

        stateChanged = true;
    }

    /**
     * Get the current blend colour. If it hasn't been set previously, the
     * call is ignored.
     *
     * @param col An array to copy the colour in, in RGBA format
     */
    public void getBlendColor(float[] col)
    {
        if(blendColor == null)
            return;

        col[0] = blendColor[0];
        col[1] = blendColor[1];
        col[2] = blendColor[2];
        col[3] = blendColor[3];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ba The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(BlendAttributes ba)
    {
        if(ba == null)
            return 1;

        if(ba == this)
            return 0;

        if(useSeparatedBlend != ba.useSeparatedBlend)
            return useSeparatedBlend ? 1 : -1;

        if(blendEquation != ba.blendEquation)
            return blendEquation < ba.blendEquation ? 1 : -1;

        if(rgbSourceMode != ba.rgbSourceMode)
            return rgbSourceMode < ba.rgbSourceMode ? -1 : 1;

        if(rgbDestMode != ba.rgbDestMode)
            return rgbDestMode < ba.rgbDestMode ? -1 : 1;

        if(alphaSourceMode != ba.alphaSourceMode)
            return alphaSourceMode < ba.alphaSourceMode ? -1 : 1;

        if(alphaDestMode != ba.alphaDestMode)
            return alphaDestMode < ba.alphaDestMode ? -1 : 1;

        if(needBlendColor != ba.needBlendColor)
            return needBlendColor ? 1 : -1;
        else if(needBlendColor)
        {
            if(blendColor[0] < ba.blendColor[0])
                return -1;
            else if(blendColor[0] > ba.blendColor[0])
                return 1;

            if(blendColor[1] < ba.blendColor[1])
                return -1;
            else if(blendColor[1] > ba.blendColor[1])
                return 1;

            if(blendColor[2] < ba.blendColor[2])
                return -1;
            else if(blendColor[2] > ba.blendColor[2])
                return 1;

            if(blendColor[3] < ba.blendColor[3])
                return -1;
            else if(blendColor[3] > ba.blendColor[3])
                return 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ba The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(BlendAttributes ba)
    {
        if(ba == this)
            return true;

        if((ba == null) ||
           (useSeparatedBlend != ba.useSeparatedBlend) ||
           (blendEquation != ba.blendEquation) ||
           (rgbSourceMode != ba.rgbSourceMode) ||
           (rgbDestMode != ba.rgbDestMode) ||
           (alphaSourceMode != ba.alphaSourceMode) ||
           (alphaDestMode != ba.alphaDestMode) ||
           (needBlendColor != ba.needBlendColor))
            return false;

        if(needBlendColor)
        {
            if((blendColor[0] != ba.blendColor[0]) ||
               (blendColor[1] != ba.blendColor[1]) ||
               (blendColor[2] != ba.blendColor[2]) ||
               (blendColor[3] != ba.blendColor[3]))
                return false;
        }

        return true;
    }

    /**
     * Check to see if the code has detected the lack of the imaging subset.
     * This call will always return false until the first time an instance of
     * this class has been rendered. After that time, the real answer is known.
     *
     * @return true if the blending operations are allowed
     */
    public boolean isBlendingAvailable()
    {
        return hasImaging;
    }
}
