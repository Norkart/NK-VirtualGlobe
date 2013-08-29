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

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
import org.j3d.aviatrix3d.Texture;
import org.j3d.aviatrix3d.Texture2D;
import org.j3d.aviatrix3d.TextureAttributes;

// Application specific imports
import org.web3d.vrml.lang.TextureConstants;

/**
 * OpenGL mapping from TextureConstants to renderer specific constants.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLTextureConstConverter {
    /**
     * Convert mode constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertMode(int tc) {
        int m=0;
        switch(tc) {
            case TextureConstants.MODE_REPLACE:
                m = TextureAttributes.MODE_REPLACE;
            case TextureConstants.MODE_MODULATE:
                m = TextureAttributes.MODE_MODULATE;
                break;
            default:
                System.out.println("Unknown TextureConstants.Mode: " + tc);
        }

        return m;
    }

    /**
     * Convert source constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertSource(int tc) {
        return 0;
    }

    /**
     * Convert Bounadry Mode constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertBoundary(int tc) {
        int bm=0;

        switch(tc) {
            case TextureConstants.BM_WRAP:
                bm = Texture.BM_WRAP;
                break;
            case TextureConstants.BM_CLAMP:
                bm = Texture.BM_CLAMP;
                break;
            case TextureConstants.BM_CLAMP_EDGE:
                bm = Texture.BM_CLAMP_TO_EDGE;
                break;
            case TextureConstants.BM_CLAMP_BOUNDARY:
                bm = Texture.BM_CLAMP_TO_BOUNDARY;
                break;
            default:
                System.out.println("Unknown TextureConstants.boundaryMode: " + tc);
        }

        return bm;
    }

    /**
     * Convert MagFilter constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertMagFilter(int tc) {
        int mfm=0;

        switch(tc) {
            case TextureConstants.MAGFILTER_FASTEST:
                mfm = Texture.MAGFILTER_FASTEST;
                break;
            case TextureConstants.MAGFILTER_NICEST:
                mfm = Texture.MAGFILTER_NICEST;
                break;
            case TextureConstants.MAGFILTER_BASE_LEVEL_POINT:
                mfm = Texture.MAGFILTER_BASE_LEVEL_POINT;
                break;
            case TextureConstants.MAGFILTER_BASE_LEVEL_LINEAR:
                mfm = Texture.MAGFILTER_BASE_LEVEL_LINEAR;
                break;
/*
            case TextureConstants.MAGFILTER_LINEAR_DETAIL:
                mfm = Texture2D.LINEAR_DETAIL;
                break;
            case TextureConstants.MAGFILTER_LINEAR_DETAIL_RGB:
                mfm = Texture2D.LINEAR_DETAIL_RGB;
                break;
            case TextureConstants.MAGFILTER_LINEAR_DETAIL_ALPHA:
                mfm = Texture2D.LINEAR_DETAIL_ALPHA;
                break;
*/
            default:
               System.out.println("Unknown TextureConstants.MagFilter" + tc);
        }

        return mfm;
    }

    /**
     * Convert MinFilter constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertMinFilter(int tc) {
        int mfm=0;

        switch(tc) {
            case TextureConstants.MINFILTER_FASTEST:
                mfm = Texture.MINFILTER_FASTEST;
                break;
            case TextureConstants.MINFILTER_NICEST:
                mfm = Texture.MINFILTER_NICEST;
                break;
            case TextureConstants.MINFILTER_BASE_LEVEL_POINT:
                mfm = Texture.MINFILTER_BASE_LEVEL_POINT;
                break;
            case TextureConstants.MINFILTER_BASE_LEVEL_LINEAR:
                mfm = Texture.MINFILTER_BASE_LEVEL_LINEAR;
                break;
            case TextureConstants.MINFILTER_MULTI_LEVEL_POINT:
                mfm = Texture.MINFILTER_MULTI_LEVEL_POINT;
                break;
            case TextureConstants.MINFILTER_MULTI_LEVEL_LINEAR:
                mfm = Texture.MINFILTER_MULTI_LEVEL_LINEAR;
                break;
            default:
               System.out.println("Unknown TextureConstants.MinFilter" + tc);
        }

        return mfm;
    }

    /**
     * Convert Anisotropic Mode constants.
     *
     * @param tc The TextureConst to convert
     * @return The J3D specific value.
     */
    public static int convertAnisotropicMode(int tc) {
        int am=0;
        switch(tc) {
            case TextureConstants.ANISOTROPIC_MODE_SINGLE:
                am = Texture.ANISOTROPIC_MODE_SINGLE;
                break;
            case TextureConstants.ANISOTROPIC_MODE_NONE:
                am = Texture.ANISOTROPIC_MODE_NONE;
                break;
            default:
               System.out.println("Unknown TextureConstants.Anisotropic Mode: " + tc);
        }
        return am;
    }
}