/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.input;

// External imports
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.image.RenderedImage;
import java.nio.Buffer;

import org.j3d.terrain.Landscape;
import org.j3d.terrain.TerrainData;
import org.j3d.util.frustum.ViewFrustum;
import org.j3d.util.interpolator.ColorInterpolator;

import org.j3d.renderer.java3d.terrain.AppearanceGenerator;
import org.j3d.renderer.java3d.terrain.J3DLandscape;
import org.j3d.renderer.java3d.terrain.roam.J3DSplitMergeLandscape;
import org.j3d.renderer.java3d.util.J3DViewFrustum;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.util.ObjectArray;

/**
 * A manager for terrain updates.
 *
 * Use only the first sector for now.  Dynamic updates will replace this one.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class J3DTerrainManager implements AppearanceGenerator,
    J3DAppearanceListener, VRMLTextureListener {

    /** The canvas for frustum calculations */
    private Canvas3D canvas;

    /** The source for terrain data */
    private J3DTerrainSource source;

    /** The underlying terrain data representation */
    private StaticTerrainDataSource terrainData;

    /** The view frustum for the ground canvas */
    private J3DViewFrustum viewFrustum;

    /** The landscape we are navigating around */
    private J3DSplitMergeLandscape landscape;

    /** Branchgroup from the landscape itself */
    private BranchGroup landscapeGroup;

    /** Local holder of the terrain data */
    private BranchGroup terrainHolder;

    private TransformGroup terrainTransform;

    /** The color interpolator for doing height interpolations with */
    private ColorInterpolator heightRamp;

    /** Global polygon attributes to use */
    private PolygonAttributes polyAttr;

    /** Global material instance to use */
    private Material material;

    /** The texture to use */
    private Texture texture;

    /** The current origin */
    private double[] geoOrigin;

    double[] spacing;
    int xSize;
    int zSize;
    double[] data;
    float[][][] tcs;

    public J3DTerrainManager(Canvas3D canvas) {
        this.canvas = canvas;

        viewFrustum = new J3DViewFrustum(canvas);
        terrainHolder = new BranchGroup();
        terrainTransform = new TransformGroup();
        terrainHolder.addChild(terrainTransform);
        terrainTransform.setCapability(Group.ALLOW_CHILDREN_WRITE);
        terrainTransform.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        terrainTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        heightRamp = new ColorInterpolator(ColorInterpolator.HSV_SPACE);
        heightRamp.addRGBKeyFrame(-20,  0,    0,    1,     0);
        heightRamp.addRGBKeyFrame(0,    0,    0.7f, 0.95f, 0);
        heightRamp.addRGBKeyFrame(4,    1,    1,    0,     0);
        heightRamp.addRGBKeyFrame(8,   0,    0.6f, 0,     0);
        heightRamp.addRGBKeyFrame(12,  0,    1,    0,     0);
        heightRamp.addRGBKeyFrame(16, 0.6f, 0.7f, 0,     0);

        // Now set up the material and appearance handling for the generator
        material = new Material();
        material.setLightingEnable(true);

        polyAttr = new PolygonAttributes();
        polyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
        polyAttr.setBackFaceNormalFlip(true);
        //polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        polyAttr.setPolygonMode(PolygonAttributes.POLYGON_FILL);
    }

    //----------------------------------------------------------
    // Methods required by J3DAppearanceListener
    //----------------------------------------------------------

    /**
     * Invoked when the underlying Java3D Appearance object is changed
     * @param app The new appearance object
     */
    public void appearanceChanged(Appearance app) {
        System.out.println("new appearance: " + app);

    }

    //----------------------------------------------------------
    // Methods required by J3DTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when a texture has changed
     *
     * @param tex The new texture impl
     * @param alpha Does this texture have an alpha channel
     * @param attrs The texture attributes
     */
    public void textureImplChanged(VRMLTextureNodeType node,
                                   Texture[] tex,
                                   boolean[] alpha,
                                   TextureAttributes[] attrs) {

System.out.println("Texture arrived: " + node);
        newTexture(node);
    }

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    RenderedImage image,
                                    String url) {
System.out.println("J3D TerrainManager need to update to latest texture handling");
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    RenderedImage[] image,
                                    String[] url) {
System.out.println("J3D TerrainManager need to update to latest texture handling");
    }

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image as a data buffer for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    Buffer image,
                                    String url) {
        // Not implemented yet
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images as data buffers for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    Buffer[] image,
                                    String[] url) {
        // Not implemented yet
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  3 Component color.
     */
    public void textureParamsChanged(int idx,
                                     int mode,
                                     int source,
                                     int function,
                                     float alpha,
                                     float[] color) {
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @param len The number of items that have changed in each array
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3 component colors.
     */
    public void textureParamsChanged(int len,
                                     int mode[],
                                     int[] source,
                                     int[] function,
                                     float alpha,
                                     float[] color) {
    }

    //----------------------------------------------------------
    // Methods required by AppearanceGenerator
    //----------------------------------------------------------

    /**
     * Create a new appearance instance. We set them all up with different
     * appearance instances, but share the material information.
     *
     * @return The new appearance instance to use
     */
    public Appearance createAppearance()
    {
        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setPolygonAttributes(polyAttr);

        return app;
    }


    public BranchGroup getTerrainGroup() {
        return terrainHolder;
    }

    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Transform3D transform) {
        if (landscape != null)
            landscape.viewerPositionUpdated(transform);
    }

    /**
     * Notification that sectors have been added for terrain management.
     *
     * @param sectors The sectors added
     */
    public void sectorsAdded(ObjectArray sectors) {
        if (sectors.size() >= 1) {
            source = (J3DTerrainSource) sectors.get(0);

            J3DAppearanceNodeType app = source.getAppearance();

            // TODO: Assume the first tile is origin for now
            geoOrigin = source.getGeoOrigin();
            spacing = source.getSpacing();;
            tcs = source.getTextureCoords();

            if (app != null) {
                app.addAppearanceListener(this);
                VRMLNodeType tex = app.getTexture();

                newTexture(tex);
            }

            double[][] sources = new double[sectors.size()][1];
            int[] xDims = new int[sectors.size()];
            int[] yDims = new int[sectors.size()];
            J3DTerrainSource src;

            // TODO: Need a real number for this
            int srcCols = 4;

            if (sectors.size() < 4)
                srcCols = sectors.size();

            int srcRows = sectors.size() / srcCols;

            for(int i=0; i < sectors.size(); i++) {
                src = (J3DTerrainSource) sectors.get(i);
                sources[i] = src.getHeight();
                xDims[i] = src.getXSize();
                yDims[i] = src.getZSize();
            }

            xSize = 64 * srcCols;
            zSize = 64 * srcRows;

            int src1Width = source.getXSize();
            int src1Height = source.getZSize();
            int wPower2 = 64;
            int hPower2 = 64;
            float wFactor;
            float hFactor;

            wFactor = ((float)src1Width) / wPower2;
            hFactor = ((float)src1Height) / hPower2;

            src1Width = wPower2;
            src1Height = hPower2;

            int finalWidth = srcCols * src1Width;

            data = new double[srcCols * src1Width*src1Height * srcRows];

            /** Source x,y */
            int sx;
            int sy;
            int firstCol;
            int wIdx;
            int hIdx;

        for(int r=0; r < srcRows; r++) {
            for(int i=0; i < src1Height; i++) {
                for(int n=0; n < srcCols; n++) {
                    sx = r*srcCols + n;

                    wFactor = ((float)xDims[n]) / wPower2;
                    hFactor = ((float)yDims[n]) / hPower2;

                    if (n == 0)
                        firstCol = 0;
                    else
//                       firstCol = 1;
                       firstCol = 0;

                        for(int j=firstCol; j < src1Width+firstCol; j++) {
                            wIdx = (int)Math.floor(j*wFactor);
                            hIdx = (int)Math.floor(i*hFactor);

                            if (wIdx > 64)
                                wIdx = 64;

                            if (hIdx > 64)
                                hIdx = 64;

//System.out.println("r: " + r + " i: " + i + " fw: " + finalWidth + " j: " + j + " n: " + n + " src1Width: " + src1Width + " fc: " + -firstCol + " data idx: " +  (r*(finalWidth*src1Height) + (i * finalWidth) + j + (n*src1Width) - firstCol) + " hIdx + " + hIdx + " wIdx: " + wIdx);
//System.out.println(" src idx: " + ((int)Math.floor(i*hFactor) * src1Width + (int)Math.floor(j*wFactor)));

//                            data[r*(finalWidth*src1Height) + (i * finalWidth) + j + (n*src1Width) - firstCol] = sources[sx][wIdx * src1Width + hIdx];

                            // TODO: Makes all zero
                            data[r*(finalWidth*src1Height) + (i * finalWidth) + j + (n*src1Width) - firstCol] = 0;
                        }
                }
            }
        }
        }
    }

    /**
     * Notification that sectors have been removed from terrain management.
     *
     * @param sectors The sectors removed
     */
    public void sectorsRemoved(ObjectArray sectors) {
    }

    private void newTexture(VRMLNodeType tex) {
/*
        if (tex != null) {
            // TODO: Need to handle proto instances
            J3DTextureNodeType j3d_tex = (J3DTextureNodeType) tex;
            j3d_tex.addTextureListener(this);

            Texture[] texs = j3d_tex.getTextures();

            if (texs != null && texs.length > 0) {
                texture = texs[0];

                buildRoam();
            }
        }
*/
    }

    private void buildRoam() {

        // Must be a power of two
        int patchSize;
        if (xSize >= 127)
            patchSize = 64;
        else
            patchSize = 2;

/*
        // Hardcode to make space a known area
        spacing[0] = 32.0f / patchSize;
        spacing[1] = 32.0f / patchSize;
*/

        terrainData = new StaticTerrainDataSource(data,
                                                  false,
                                                  spacing,
                                                  xSize,
                                                  zSize,
                                                  patchSize,
                                                  patchSize);

        terrainData.tcMap = tcs;

        float[] coord = new float[3];
        float[] tex = new float[2];

        terrainData.getCoordinateWithTexture(coord, tex, 6,3,0,0);
        terrainData.getCoordinateWithTexture(coord, tex, 5,0,0,0);

        //terrainData.setColorInterpolator(heightRamp);
        //if(texture != null)
        //    terrainData.setTexture(texture);

        // TODO: Assume we are square
        landscape = new J3DSplitMergeLandscape(viewFrustum, terrainData,patchSize+1);
        landscape.setAppearanceGenerator(this);

        landscapeGroup = landscape.getSceneGraphObject();
        landscapeGroup.setCapability(BranchGroup.ALLOW_DETACH);

        viewFrustum.viewingPlatformMoved();

        Matrix3f mtx = new Matrix3f();
        Vector3f orient = new Vector3f(0, 0, -1);
        Vector3f pos = new Vector3f();

        Transform3D angle = new Transform3D();

        angle.get(mtx, pos);
        mtx.transform(orient);

        landscape.initialize(pos, orient);

        // Assume the first tile is the origin for now.


        System.out.println("geoOrigin: " + geoOrigin[0] + " 0 " + geoOrigin[1]);

        Transform3D trans = new Transform3D();
        Vector3d vec = new Vector3d();
        vec.set(geoOrigin[0], 0, geoOrigin[1]);
        trans.set(vec);
        terrainTransform.setTransform(trans);
        terrainTransform.removeAllChildren();
        terrainTransform.addChild(landscapeGroup);
    }
}
