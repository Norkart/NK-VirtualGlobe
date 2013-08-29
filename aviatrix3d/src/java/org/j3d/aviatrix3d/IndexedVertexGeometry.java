/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2006
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
import java.nio.*;

import java.util.HashMap;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * Base class that defines indexed geometry types.
 * <p>
 *
 * The implementation assumes a single index is used to describe all the
 * information needed for rendering. An index will grab the same index from
 * vertex, normal, colour and texture arrays.
 *
 * <h3>Setting geometry</h3>
 *
 * <p>Part of the optimisation we make is to only copy into the underlying
 * structures the exact number of coordinates, normals etc that are needed.
 * To know this number, we need to know how many coordinates exist before
 * attempting to set anything else. When constructing, or updating, geometry,
 * you should always make sure that you first set the vertex list, then the
 * sizing information for the strip or fan counts, and then set normals as
 * needed. </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public abstract class IndexedVertexGeometry extends VertexGeometry
{
    /** Error message indicating the max index > numCoords */
    private static final String MAX_INDEX_MSG =
        "The maximum index value provided is greater than the number of " +
        "coordinates provided to this geometry object.";

    /**
     * The indices defining the geometry types. A reference to the
     * user-provided array. The current values are kept in the accompanying
     * IntBuffer instance.
     */
    protected int[] indices;

    /** The number of valid values to read from the array */
    protected int numIndices;

    /** Buffer holding the current index list */
    protected IntBuffer indexBuffer;

    /** Map of VBO IDs */
    protected HashMap<GL, Integer> vboElementIdMap;

    /**
     * Constructs an instance.
     */
    protected IndexedVertexGeometry()
    {
        this(false, VBO_HINT_STATIC);
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*
     */
    protected IndexedVertexGeometry(boolean useVbo, int vboHint)
    {
        super(useVbo, vboHint);

        if (useVbo)
            vboElementIdMap = new HashMap<GL, Integer>();
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. Returns true if the defined number of coordinates and indices
     * are both non-zero.
     *
     * @return true when the geometry is visible
     */
    protected boolean isVisible()
    {
        return super.isVisible() && numIndices != 0;
    }

    //----------------------------------------------------------
    // Methods defined by VertexGeometry
    //----------------------------------------------------------

    protected void setVertexStateVBO(GL gl)
    {
        Integer vbo_id = vboElementIdMap.get(gl);

        if(vbo_id == null)
        {
            int[] vbo_id_tmp = new int[1];
            gl.glGenBuffers(1, vbo_id_tmp, 0);

            vboElementIdMap.put(gl, new Integer(vbo_id_tmp[0]));

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo_id_tmp[0]);

            // Set the flag so that we update later in the method
            dataChanged.put(gl, true);
        }

        super.setVertexStateVBO(gl);
    }

    /**
     * Fill VBOs with vertex buffer data. The
     * VBO must be bound and allocated with glBufferData before the method is called.
     * This method is called by <code>setVertexStateVBO</code>, and should not be called
     * other places. <p>
     * Must be overridden in subclasses that has vertex data in addition to what
     * is in <code>VertexGeometry</code>. See <code>TriangleArray</code> for
     * examples.
     */
     protected int fillBufferData(GL gl)
     {
        int offset = super.fillBufferData(gl);

        Integer vbo_id = vboElementIdMap.get(gl);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo_id.intValue());
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, numIndices * 4, (Buffer)null, vboHint);

        gl.glBufferSubData(GL.GL_ELEMENT_ARRAY_BUFFER, 0, numIndices * 4, indexBuffer);

        return numIndices;
    }

    //---------------------------------------------------------------
    // Methods defined by DeletableRenderable
    //---------------------------------------------------------------

    /**
     * Cleanup the object now for the given GL context.
     *
     * @param gl The gl context to draw with
     */
    public void cleanup(GL gl)
    {
        super.cleanup(gl);

        if(useVbo)
        {
            Integer vbo_id = (Integer)vboElementIdMap.get(gl);
            if(vbo_id != null)
            {
                int[] vbo_id_tmp = { vbo_id.intValue() };
                gl.glDeleteBuffers(1, vbo_id_tmp, 0);
                vboElementIdMap.remove(gl);
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the number of valid indexs to use. A check is performed to make
     * sure that the number of vertices high enough to support the total
     * of all the index counts so make sure to call setVertex() with the
     * required array length before calling this method. Each index must be
     * a minumum length of three.
     *
     * @param indexList The array of indices to set
     * @param num The number of valid items to read from the array
     * @throws IllegalArgumentException Invalid total index count or
     *   individual index count < 3
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setIndices(int[] indexList, int num)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // Find the max index defined and set that as the required count.
        int max_index = 0;
        for(int i = 0; i < num; i++)
        {
            if(indexList[i] > max_index)
                max_index = indexList[i];
        }

        if(max_index + 1> numCoords)
            throw new IllegalArgumentException(MAX_INDEX_MSG);

        numRequiredCoords = max_index + 1;
        numIndices = num;

        indices = indexList;

        if(indexBuffer == null || numIndices > indexBuffer.capacity())
            indexBuffer = createBuffer(numIndices);
        else
            indexBuffer.clear();

        indexBuffer.put(indexList, 0, numIndices);
        indexBuffer.rewind();

        // TODO: Need to handle realloc of VBO
    }

    /**
     * Get the number of valid indexs that are defined for this geometry.
     *
     * @return a positive number
     */
    public int getValidIndexCount()
    {
        return numIndices;
    }

    /**
     * Get the sizes of the valid indexs. The passed array must be big enough
     * to contain all the indexs.
     *
     * @param values An array to copy the index values into
     */
    public void getIndices(int[] values)
    {
        System.arraycopy(indices, 0, values, 0, numIndices);
    }

    /**
     * Set whether Vertex Buffer Objects are used.  This will only apply on
     * graphics hardware that supports VBO's.
     *
     * @param enabled Should VBO's be used.
     */
    public void setVBOEnabled(boolean enabled) throws InvalidWriteTimingException
    {
        super.setVBOEnabled(enabled);

        if (vboQueryComplete && vboAvailable == false)
            return;

        if (enabled)
        {
            if (vboElementIdMap == null)
                vboElementIdMap = new HashMap<GL, Integer>();
        }
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    private IntBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        IntBuffer ret_val = buf.asIntBuffer();

        return ret_val;
    }
}
