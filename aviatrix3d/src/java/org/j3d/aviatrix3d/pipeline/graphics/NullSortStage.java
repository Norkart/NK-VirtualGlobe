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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
import java.util.HashMap;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of the sort stage that does nothing.
 * <p>
 *
 * The sort stage just takes the given nodes and expands them into an array
 * renders and then immediately pops the node. No sorting on output is done.
 *
 * @author Justin Couch
 * @version $Revision: 3.8 $
 */
public class NullSortStage extends BaseSortStage
{
    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output.
     */
    public NullSortStage()
    {
        this(LIST_START_SIZE);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     */
    public NullSortStage(int numSurfaces)
    {
        super(numSurfaces);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSortStage
    //---------------------------------------------------------------

    /**
     * Sort a single set of nodes into the output details of a single layer of
     * a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param data The environment data used during sorting
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected int sortNodes(GraphicsCullOutputDetails[] nodes,
                            int numNodes,
                            GraphicsEnvironmentData data,
                            GraphicsInstructions instr,
                            int instrCount)
    {
        int idx = instrCount;
        int fog_id = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            // First drop all the lights into the queue.
            if(nodes[i].numLights != 0)
            {
                for(int j = 0; j < nodes[i].numLights; j++)
                {
                    VisualDetails ld = nodes[i].lights[j];
                    instr.renderList[idx].renderable = ld.getRenderable();

                    System.arraycopy(ld.getTransform(),
                                     0,
                                     instr.renderList[idx].transform,
                                     0,
                                     16);

                    instr.renderList[idx].id = lastGlobalId++;
                    instr.renderOps[idx] = RenderOp.START_LIGHT;
                    idx++;
                }
            }

            // Next drop all the clip planes into the queue.
            if(nodes[i].numClipPlanes != 0)
            {
                for(int j = 0; j < nodes[i].numClipPlanes; j++)
                {
                    VisualDetails cd = nodes[i].clipPlanes[j];
                    instr.renderList[idx].renderable = cd.getRenderable();

                    System.arraycopy(cd.getTransform(),
                                     0,
                                     instr.renderList[idx].transform,
                                     0,
                                     16);

                    instr.renderList[idx].id = lastGlobalId++;
                    instr.renderOps[idx] = RenderOp.START_CLIP_PLANE;
                    idx++;
                }
            }

            if(nodes[i].localFog != null)
            {
                fog_id = lastGlobalId++;
                instr.renderList[idx].id = fog_id;
                instr.renderList[idx].renderable = nodes[i].localFog;
                instr.renderOps[idx] = RenderOp.START_FOG;
                idx++;
            }

            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;
                Matrix4f tx = nodes[i].transform;

                if(!shape.is2D())
                {
                    Renderable r = shape.getAppearanceRenderable();

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.START_STATE;
                        idx++;
                    }

                    instr.renderList[idx].transform[0] = tx.m00;
                    instr.renderList[idx].transform[1] = tx.m10;
                    instr.renderList[idx].transform[2] = tx.m20;
                    instr.renderList[idx].transform[3] = tx.m30;

                    instr.renderList[idx].transform[4] = tx.m01;
                    instr.renderList[idx].transform[5] = tx.m11;
                    instr.renderList[idx].transform[6] = tx.m21;
                    instr.renderList[idx].transform[7] = tx.m31;

                    instr.renderList[idx].transform[8] = tx.m02;
                    instr.renderList[idx].transform[9] = tx.m12;
                    instr.renderList[idx].transform[10] = tx.m22;
                    instr.renderList[idx].transform[11] = tx.m32;

                    instr.renderList[idx].transform[12] = tx.m03;
                    instr.renderList[idx].transform[13] = tx.m13;
                    instr.renderList[idx].transform[14] = tx.m23;
                    instr.renderList[idx].transform[15] = tx.m33;

                    instr.renderList[idx].renderable = shape.getGeometryRenderable();
                    instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY;
                    idx++;

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }
                }
                else
                {
                    Renderable r = shape.getAppearanceRenderable();

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.START_RENDER;
                        idx++;
                    }

                    instr.renderList[idx].transform[0] = tx.m00;
                    instr.renderList[idx].transform[1] = 0;
                    instr.renderList[idx].transform[2] = 0;
                    instr.renderList[idx].transform[3] = tx.m30;

                    instr.renderList[idx].transform[4] = 0;
                    instr.renderList[idx].transform[5] = tx.m11;
                    instr.renderList[idx].transform[6] = 0;
                    instr.renderList[idx].transform[7] = tx.m31;

                    instr.renderList[idx].transform[8] = 0;
                    instr.renderList[idx].transform[9] = 0;
                    instr.renderList[idx].transform[10] = 0;
                    instr.renderList[idx].transform[11] = 0;

                    instr.renderList[idx].transform[12] = 0;
                    instr.renderList[idx].transform[13] = 0;
                    instr.renderList[idx].transform[14] = 0;
                    instr.renderList[idx].transform[15] = 0;

                    instr.renderList[idx].renderable = shape.getGeometryRenderable();
                    instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY_2D;
                    idx++;

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.STOP_RENDER;
                        idx++;
                    }
                }
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderList[idx].instructions = nodes[i].customData;

                Matrix4f tx = nodes[i].transform;

                instr.renderList[idx].transform[0] = tx.m00;
                instr.renderList[idx].transform[1] = tx.m10;
                instr.renderList[idx].transform[2] = tx.m20;
                instr.renderList[idx].transform[3] = tx.m30;

                instr.renderList[idx].transform[4] = tx.m01;
                instr.renderList[idx].transform[5] = tx.m11;
                instr.renderList[idx].transform[6] = tx.m21;
                instr.renderList[idx].transform[7] = tx.m31;

                instr.renderList[idx].transform[8] = tx.m02;
                instr.renderList[idx].transform[9] = tx.m12;
                instr.renderList[idx].transform[10] = tx.m22;
                instr.renderList[idx].transform[11] = tx.m32;

                instr.renderList[idx].transform[12] = tx.m03;
                instr.renderList[idx].transform[13] = tx.m13;
                instr.renderList[idx].transform[14] = tx.m23;
                instr.renderList[idx].transform[15] = tx.m33;

                instr.renderOps[idx] = RenderOp.RENDER_CUSTOM;
                idx++;
            }
            else
            {
                System.out.println("Non-shape node in sorter " + nodes[i].renderable);
            }

            if(nodes[i].localFog != null)
            {
                instr.renderList[idx].id = fog_id;
                instr.renderList[idx].renderable = nodes[i].localFog;
                instr.renderOps[idx] = RenderOp.STOP_FOG;
                idx++;
            }

            // Clean up the clip planess
            if(nodes[i].numClipPlanes != 0)
            {
                for(int j = nodes[i].numClipPlanes - 1; j >= 0; j--)
                {
                    VisualDetails cd = nodes[i].clipPlanes[j];
                    instr.renderList[idx].renderable = cd.getRenderable();

                    instr.renderList[idx].id = lastGlobalId - (1 + j);

                    // Don't need the transform for the clip stopping. Save
                    // CPU cycles by not copying it.

                    instr.renderOps[idx] = RenderOp.STOP_CLIP_PLANE;
                    idx++;
                }
            }

            // Clean up the lights
            if(nodes[i].numLights != 0)
            {
                for(int j = nodes[i].numLights - 1; j >= 0; j--)
                {
                    VisualDetails ld = nodes[i].lights[j];
                    instr.renderList[idx].renderable = ld.getRenderable();

                    instr.renderList[idx].id = lastGlobalId - (1 + j);

                    // Don't need the transform for the light stopping. Save
                    // CPU cycles by not copying it.

                    instr.renderOps[idx] = RenderOp.STOP_LIGHT;
                    idx++;
                }
            }
        }

        return idx;
    }

    /**
     * Sort a single set of 2D nodes into the output details of a single layer
     * of a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param data The environment data used during sorting
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected int sort2DNodes(GraphicsCullOutputDetails[] nodes,
                              int numNodes,
                              GraphicsEnvironmentData data,
                              GraphicsInstructions instr,
                              int instrCount)
    {
        int idx = instrCount;
        int fog_id = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;

                if(!shape.is2D())
                    continue;

                // For this simple one, just use the basic render command.
                // Something more complex would pull the shape apart and do
                // state/depth/transparency sorting in this section.
                Matrix4f tx = nodes[i].transform;

                instr.renderList[idx].transform[0] = tx.m00;
                instr.renderList[idx].transform[1] = tx.m10;
                instr.renderList[idx].transform[2] = tx.m20;
                instr.renderList[idx].transform[3] = tx.m30;

                instr.renderList[idx].transform[4] = tx.m01;
                instr.renderList[idx].transform[5] = tx.m11;
                instr.renderList[idx].transform[6] = tx.m21;
                instr.renderList[idx].transform[7] = tx.m31;

                instr.renderList[idx].transform[8] = tx.m02;
                instr.renderList[idx].transform[9] = tx.m12;
                instr.renderList[idx].transform[10] = tx.m22;
                instr.renderList[idx].transform[11] = tx.m32;

                instr.renderList[idx].transform[12] = tx.m03;
                instr.renderList[idx].transform[13] = tx.m13;
                instr.renderList[idx].transform[14] = tx.m23;
                instr.renderList[idx].transform[15] = tx.m33;

                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderOps[idx] = RenderOp.START_RENDER_2D;
                idx++;

                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderOps[idx] = RenderOp.STOP_RENDER_2D;
                idx++;
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderList[idx].instructions = nodes[i].customData;

                Matrix4f tx = nodes[i].transform;

                instr.renderList[idx].transform[0] = tx.m00;
                instr.renderList[idx].transform[1] = tx.m10;
                instr.renderList[idx].transform[2] = tx.m20;
                instr.renderList[idx].transform[3] = tx.m30;

                instr.renderList[idx].transform[4] = tx.m01;
                instr.renderList[idx].transform[5] = tx.m11;
                instr.renderList[idx].transform[6] = tx.m21;
                instr.renderList[idx].transform[7] = tx.m31;

                instr.renderList[idx].transform[8] = tx.m02;
                instr.renderList[idx].transform[9] = tx.m12;
                instr.renderList[idx].transform[10] = tx.m22;
                instr.renderList[idx].transform[11] = tx.m32;

                instr.renderList[idx].transform[12] = tx.m03;
                instr.renderList[idx].transform[13] = tx.m13;
                instr.renderList[idx].transform[14] = tx.m23;
                instr.renderList[idx].transform[15] = tx.m33;

                instr.renderOps[idx] = RenderOp.RENDER_CUSTOM;
                idx++;
            }
            else
            {
                System.out.println("Non-shape node in sorter " + nodes[i].renderable);
            }
        }

        return idx;
    }

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected int estimateInstructionSize(SceneRenderBucket scene)
    {
        int instr_count = 2 + scene.numNodes << 1;
        for(int m = 0; m < scene.numNodes; m++)
        {
            instr_count += (scene.nodes[m].numLights << 1) +
                           (scene.nodes[m].numClipPlanes << 1);
        }

        return instr_count;
    }

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected int estimateInstructionSize(MultipassRenderBucket scene)
    {
        int instr_count = 2;

        for(int i = 0; i < scene.mainScene.numPasses; i++)
        {
            // Start/stop pass commands + up to 4 buffer state start
            // and stop commands.
            instr_count += 10 + scene.mainScene.numNodes[i] << 1;

            for(int j = 0; j < scene.mainScene.numNodes[i]; j++)
            {
                instr_count += (scene.mainScene.nodes[i][j].numLights << 1) +
                               (scene.mainScene.nodes[i][j].numClipPlanes << 1);
            }
        }

        return instr_count;
    }
}
