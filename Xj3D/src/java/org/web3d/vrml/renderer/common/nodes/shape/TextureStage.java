/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.shape;

// External imports
// none

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.TextureConstants;

/**
 * Texture Stage information holder used by the Appearance node.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class TextureStage {
    // Common setup information

    /** Which stage number this belongs to. Allows for back referencing */
    public final int stageNumber;

    /** List of image(s) for this stage */
	public NIOBufferImage[] images;

    /** The texture transform for this stage. Object is renderer-specific */
    public Object transform;

    /** The texture coordinate generator for this stage. Object is renderer-specific */
    public Object texCoordGeneration;

    /** The colour of the boundary, if set. */
    public float[] boundaryColor;

    /** How many pixels wide to make the boundary */
    public int boundaryWidth;

    /** Boundary mode in the S coordinate direction */
    public int boundaryModeS;

    /** Boundary mode in the T coordinate direction */
    public int boundaryModeT;

    /** Minification filter setting */
    public int minFilter;

    /** Magnification filter setting */
    public int magFilter;

    /** Flag indicating whether we should generate mipmaps */
    public boolean generateMipMaps;

    public int anisotropicMode;
    public float anisotropicDegree;

    // Multitexture Setup Information
    public int mode;
    public int source;
    public int function;
    public float alpha;
    public float[] color;

    // 3D Texture setup information
    /** How deep is the texture in the 3rd dimension */
    public int depth;

    /** Boundary mode in the R coordinate direction */
    public int boundaryModeR;

    /**
     * Create a default instance of this class with the fields set to:
     *  <p>
     *  generateMipMaps: false <br>
     *  minFilter: TextureConstants.MINFILTER_NICEST <br>
     *  magFilter: TextureConstants.MAGFILTER_NICEST <br>
     *  boundaryModeS: TextureConstants.BM_WRAP <br>
     *  boundaryModeT: TextureConstants.BM_WRAP <br>
     *  boundaryModeR: TextureConstants.BM_WRAP <br>
     *  anisotropicMode: TextureConstants.ANISOTROPIC_MODE_NONE <br>
     *  anisotropicDegree: 1.0f <br>
     *
     * @param stage The number of the stage
     */
    public TextureStage(int stage) {
        stageNumber = stage;

        generateMipMaps = false;
        minFilter = TextureConstants.MINFILTER_NICEST;
        magFilter = TextureConstants.MAGFILTER_NICEST;
        boundaryModeS = TextureConstants.BM_WRAP;
        boundaryModeT = TextureConstants.BM_WRAP;
        boundaryModeR = TextureConstants.BM_WRAP;
        anisotropicMode = TextureConstants.ANISOTROPIC_MODE_NONE;
        anisotropicDegree = 1.0f;
        depth = 1;
        color = new float[3];
    }
}
