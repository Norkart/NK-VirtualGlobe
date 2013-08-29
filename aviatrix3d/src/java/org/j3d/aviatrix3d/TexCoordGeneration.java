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

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.ObjectRenderable;

/**
 * Describes a texture's automatic texture coordinate generation properties
 * per axis.
 * <p>
 *
 * This class allows texture coordinates to be specified for each axis of
 * an object separately. Only one instance of this class is needed per object
 * as all axes can be specified.
 * <p>
 *
 * Texture modes here directly correspond to the OpenGL constants of the same
 * type. Either are acceptable as parameters. All parameters can be set using
 * the {@link #setParameter(int,int,int,float[])} method. This takes 4
 * parameters, some of which are likely not to be used.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class TexCoordGeneration extends NodeComponent
    implements ObjectRenderable
{
    /** Message when an invalid coordinate is given */
    private static final String INVALID_COORD_MSG =
        "The coordinate specified is not one of S,T,R or Q";

    /** Message when the generation mode is invalid */
    private static final String INVALID_MODE_MSG =
        "The generation mode is invalid. Must be one of the MODE_x constants.";

    /** Message when the mode parameter type is invalid */
    private static final String INVALID_PARAM_MSG =
        "The mode's map parameter is invalid. Must be one of the MAP_x constants.";

    /** Generate coordinates for a texture's S coordinate */
    public static final int TEXTURE_S = GL.GL_S;

    /** Generate coordinates for a texture's T coordinate  */
    public static final int TEXTURE_T = GL.GL_T;

    /** Generate coordinates for a texture's R coordinate */
    public static final int TEXTURE_R = GL.GL_R;

    /** Generate coordinates for a texture's Q coordinate  */
    public static final int TEXTURE_Q = GL.GL_Q;

    /**
     * Coordinate reference plane is user defined. Additional information
     * in the form of extra parameters (The MAP_* values) will need to be
     * provided.
     */
    public static final int MODE_GENERIC = GL.GL_TEXTURE_GEN_MODE;

    /**
     * Generate coordinates for a reference plane that is relative to the
     * object for the given axis. No value needs to be specified for the param
     * or value arguments.
     */
    public static final int MODE_OBJECT_PLANE = GL.GL_OBJECT_PLANE;

    /**
     * Generate coordinates for a reference plane that is relative to the
     * user's eye position for the given axis. No value needs to be specified
     * for the param or value arguments.
     */
    public static final int MODE_EYE_PLANE = GL.GL_EYE_PLANE;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given coordinate relative to a plane specified in the object's
     * coordinate system.
     */
    public static final int MAP_OBJECT_LINEAR = GL.GL_OBJECT_LINEAR;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given coordinate relative to a plane specified in the user's eye
     * position coordinate system.
     */
    public static final int MAP_EYE_LINEAR = GL.GL_EYE_LINEAR;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given axis in a spherical shape for env mapping.
     */
    public static final int MAP_SPHERICAL = GL.GL_SPHERE_MAP;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates using the
     * normals at the vertex. Used mostly in cubic environment mapping.
     */
    public static final int MAP_NORMALS = GL.GL_NORMAL_MAP;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates using the
     * normals at the vertex. Used mostly in cubic environment mapping.
     */
    public static final int MAP_REFLECTIONS = GL.GL_REFLECTION_MAP;

    /** Modes that are needed to be passed to the system for tex gen */
    private static final int[] ENABLE_MODE =
    {
        GL.GL_TEXTURE_GEN_S,
        GL.GL_TEXTURE_GEN_T,
        GL.GL_TEXTURE_GEN_R,
        GL.GL_TEXTURE_GEN_Q,
    };

    /** Parameters used when TEXTURE_GEN or OBJECT_LINEAR used */
    private float[][] parameters;

    /** The texture coordiante effected */
    private int[] coordinate;

    /** The parameter mode use */
    private int[] modes;

    /** The mapping type to use */
    private int[] mapping;

    /** A mapping between glContext and displayListID(Integer) */
    private HashMap<GL, Integer> displayListMap;

    /** A mapping for displaylists that have been deleted */
    private HashMap<GL, Integer> deletedDisplayListMap;

    /**
     * Constructs a TexCoordGeneration with default values, which is
     * to say, do nothing.
     */
    public TexCoordGeneration()
    {
        parameters = new float[4][];
        coordinate = new int[4];
        modes = new int[4];
        mapping = new int[4];

        displayListMap = new HashMap<GL, Integer>();
        deletedDisplayListMap = new HashMap<GL, Integer>();
    }

    /**
     * Create automatic coordinate generation for one axis with
     * the given set of abilities.
     *
     * @throws IllegalArgumentException Invalid axis, mode or parameter
     */
    public TexCoordGeneration(int axis, int mode, int parameter, float[] value)
    {
        this();

        setParameter(axis, mode, parameter, value);
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
        // If we have changed state, then clear the old display lists
        if(deletedDisplayListMap.size() != 0)
        {
            Integer listName = deletedDisplayListMap.remove(gl);

            if(listName != null)
                gl.glDeleteLists(listName.intValue(), 1);
        }

        Integer listName = displayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE);

            //  State enable first
            for(int i = 0; i < 4; i++)
            {
                if(coordinate[i] == 0)
                    continue;

                if(modes[i] == MODE_GENERIC)
                {
                    gl.glTexGeni(coordinate[i], modes[i], mapping[i]);

                    if(mapping[i] == MAP_OBJECT_LINEAR)
                        gl.glTexGenfv(coordinate[i],
                                      MODE_OBJECT_PLANE,
                                      parameters[i],
                                      0);

                }
                else
                {
                    gl.glTexGeniv(coordinate[i], modes[i], new int[0], 0);
                }

                gl.glEnable(ENABLE_MODE[i]);
            }

            gl.glEndList();
            displayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        for(int i = 0; i < 4; i++)
        {
            if(coordinate[i] == 0)
                continue;

            gl.glDisable(ENABLE_MODE[i]);
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
        TexCoordGeneration tcg = (TexCoordGeneration)o;
        return compareTo(tcg);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Fetch the currently set mode value for the requested axis.
     *
     * @param axis One of the TEXTURE_x values
     * @return The current mode value (one of MODE_x)
     */
    public int getMode(int axis)
    {
        int ret_val = 0;

        switch(axis)
        {
            case TEXTURE_S:
                ret_val = modes[0];
                break;

            case TEXTURE_T:
                ret_val = modes[1];
                break;

            case TEXTURE_R:
                ret_val = modes[2];
                break;

            case TEXTURE_Q:
                ret_val = modes[3];
                break;
        }

        return ret_val;
    }

    /**
     * Clear the parameter settings for a specific axis. This will disable coordinate
     * generation on this axis.
     *
     * @param axis One of the TEXTURE_x values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void clearParameter(int axis)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        switch(axis)
        {
            case TEXTURE_S:
                clearActiveList();
                coordinate[0] = 0;
                break;

            case TEXTURE_T:
                clearActiveList();
                coordinate[1] = 0;
                break;

            case TEXTURE_R:
                clearActiveList();
                coordinate[2] = 0;
                break;

            case TEXTURE_Q:
                clearActiveList();
                coordinate[3] = 0;
                break;
        }
    }

    /**
     * Setup one of the axis parameters.
     *
     * @param axis One of the TEXTURE_x values
     * @param mode One of the MODE_x values
     * @param parameter One of the MAP_x values when the mode is set
     *    to MODE_GENERIC, otherwise ignored
     * @param value Optional values, dependent on the parameter type
     * @throws IllegalArgumentException The either the mode or parameter is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setParameter(int axis, int mode, int parameter, float[] value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        checkMode(mode);

        if(mode == MODE_GENERIC)
            checkParameter(parameter);

        switch(axis)
        {
            case TEXTURE_S:
                coordinate[0] = TEXTURE_S;
                modes[0] = mode;
                mapping[0] = parameter;

                if(value != null)
                {
                    if(parameters[0] == null)
                        parameters[0] = new float[4];

                    parameters[0][0] = value[0];
                    parameters[0][1] = value[1];
                    parameters[0][2] = value[2];
                    parameters[0][3] = value[3];
                }

                clearActiveList();
                break;

            case TEXTURE_T:
                coordinate[1] = TEXTURE_T;

                modes[1] = mode;
                mapping[1] = parameter;
                if(value != null)
                {
                    if(parameters[1] == null)
                        parameters[1] = new float[4];

                    parameters[1][0] = value[0];
                    parameters[1][1] = value[1];
                    parameters[1][2] = value[2];
                    parameters[1][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_R:
                coordinate[2] = TEXTURE_R;
                modes[2] = mode;
                mapping[2] = parameter;
                if(value != null)
                {
                    if(parameters[2] == null)
                        parameters[2] = new float[4];

                    parameters[2][0] = value[0];
                    parameters[2][1] = value[1];
                    parameters[2][2] = value[2];
                    parameters[2][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_Q:
                coordinate[3] = TEXTURE_Q;
                modes[3] = mode;
                mapping[3] = parameter;
                if(value != null)
                {
                    if(parameters[3] == null)
                        parameters[3] = new float[4];

                    parameters[3][0] = value[0];
                    parameters[3][1] = value[1];
                    parameters[3][2] = value[2];
                    parameters[3][3] = value[3];
                }
                clearActiveList();
                break;

            default:
                throw new IllegalArgumentException(INVALID_COORD_MSG);
        }
    }


    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param tcg The generator instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(TexCoordGeneration tcg)
    {
        if(tcg == null)
            return 1;

        if(tcg == this)
            return 0;

        for(int i = 0; i < 4; i++)
        {
            if(coordinate[i] != tcg.coordinate[i])
                return coordinate[i] < tcg.coordinate[i] ? -1 : 1;
            else if(coordinate[i] != 0)
            {
                if(mapping[i] != tcg.mapping[i])
                    return mapping[i] < tcg.mapping[i] ? -1 : 1;

                if(parameters[i] != tcg.parameters[i])
                {
                    if(parameters[i] == null)
                        return -1;
                    else if(tcg.parameters[i] == null)
                        return 1;

                    if(parameters[i][0] != tcg.parameters[i][0])
                        return parameters[i][0] < tcg.parameters[i][0] ? -1 : 1;

                    if(parameters[i][1] != tcg.parameters[i][1])
                        return parameters[i][1] < tcg.parameters[i][1] ? -1 : 1;

                    if(parameters[i][2] != tcg.parameters[i][2])
                        return parameters[i][2] < tcg.parameters[i][2] ? -1 : 1;

                    if(parameters[i][3] != tcg.parameters[i][3])
                        return parameters[i][3] < tcg.parameters[i][3] ? -1 : 1;
                }

                if(modes[i] != tcg.modes[i])
                    return modes[i] < tcg.modes[i] ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Check the validity of the mode argument. If invalid, throw the exception.
     *
     * @param mode One of the MODE_x values
     * @throws IllegalArgumentException Invalid mode type specified
     */
    private void checkMode(int mode)
        throws IllegalArgumentException
    {
        switch(mode)
        {
            case MODE_GENERIC:
            case MODE_OBJECT_PLANE:
            case MODE_EYE_PLANE:
                break;

            default:
                throw new IllegalArgumentException(INVALID_MODE_MSG);
        }
    }

    /**
     * Check the parameter value for validity. Assumes that it has been checked for
     * MODE_GENERIC first.
     *
     * @param parameter One of the MAP_x values when the mode is set
     *    to MODE_GENERIC, otherwise ignored
     * @throws IllegalArgumentException Invalid mode type specified
     */
    private void checkParameter(int parameter)
        throws IllegalArgumentException
    {
        switch(parameter)
        {
            case MAP_OBJECT_LINEAR:
            case MAP_EYE_LINEAR:
            case MAP_SPHERICAL:
            case MAP_NORMALS:
            case MAP_REFLECTIONS:
                break;

            default:
                throw new IllegalArgumentException(INVALID_PARAM_MSG);
        }
    }

    /**
     * Shift all the values from the current display list across to the deleted
     * list.
     */
    private void clearActiveList()
    {
        if(displayListMap.size() == 0)
            return;

        deletedDisplayListMap.putAll(displayListMap);
        displayListMap.clear();
    }
}
