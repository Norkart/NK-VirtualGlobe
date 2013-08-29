/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

/**
 * Listing of type constants for textures.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface TextureConstants {
    // Texture stage modes
    public int MODE_MODULATE = 0;
    public int MODE_REPLACE = 1;
    public int MODE_MODULATE_2X = 2;
    public int MODE_MODULATE_4X = 3;
    public int MODE_ADD = 4;
    public int MODE_ADD_SIGNED = 5;
    public int MODE_ADD_SIGNED_2X = 6;
    public int MODE_SUBTRACT = 7;
    public int MODE_ADD_SMOOTH = 8;
    public int MODE_BLEND_DIFFUSE_ALPHA = 9;
    public int MODE_BLEND_TEXTURE_ALPHA = 10;
    public int MODE_BLEND_FACTOR_ALPHA = 11;
    public int MODE_BLEND_CURRENT_ALPHA = 12;
    public int MODE_MODULATE_ALPHA_ADD_COLOR = 13;
    public int MODE_MODULATE_INVCOLOR_ADD_ALPHA = 14;
    public int MODE_OFF = 15;
    public int MODE_SELECT_ARG1 = 16;
    public int MODE_SELECT_ARG2 = 17;
    public int MODE_DOTPRODUCT3 = 18;

    // Texture Types
    public int TYPE_SINGLE_2D = 0;
    public int TYPE_SINGLE_3D = 1;
    public int TYPE_MULTI = 2;
    public int TYPE_CUBIC_ENVIRONMAP = 3;
    public int TYPE_PBUFFER = 4;

    // Source Types
    public int SRC_COMBINE_PREVIOUS = 0;
    public int SRC_DIFFUSE = 1;
    public int SRC_SPECULAR = 2;
    public int SRC_FACTOR = 3;

    // Function Types
    public int FUNC_NONE = 0;
    public int FUNC_COMPLEMENT = 1;
    public int FUNC_ALPHA_REPLICATE = 2;

    // Boundary Modes
    public int BM_WRAP = 0;
    public int BM_CLAMP = 1;
    public int BM_CLAMP_EDGE = 2;
    public int BM_CLAMP_BOUNDARY = 3;

    // Magnification Filter Techniques
    public int MAGFILTER_FASTEST = 0;
    public int MAGFILTER_NICEST = 1;
    public int MAGFILTER_BASE_LEVEL_POINT = 2;
    public int MAGFILTER_BASE_LEVEL_LINEAR = 3;
    public int MAGFILTER_LINEAR_DETAIL = 4;
    public int MAGFILTER_LINEAR_DETAIL_RGB = 5;
    public int MAGFILTER_LINEAR_DETAIL_ALPHA = 6;

    // Minification Filter Techniques
    public int MINFILTER_FASTEST = 0;
    public int MINFILTER_NICEST = 1;
    public int MINFILTER_BASE_LEVEL_POINT = 2;
    public int MINFILTER_BASE_LEVEL_LINEAR = 3;
    public int MINFILTER_MULTI_LEVEL_POINT = 4;
    public int MINFILTER_MULTI_LEVEL_LINEAR = 5;

    // Anistropic Mode
    public int ANISOTROPIC_MODE_NONE = 0;
    public int ANISOTROPIC_MODE_SINGLE = 1;
}
