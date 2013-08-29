/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.text;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Geometry;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.TriangleStripArray;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.nodes.VRMLFontStyleNodeType;
import org.web3d.vrml.renderer.common.nodes.text.BaseText;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DTextNodeType;

/**
 * Java3D implementation of a Text
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.19 $
 */
public class J3DText extends BaseText
    implements J3DTextNodeType, ImageComponent2D.Updater {

    /**
     * Coordinates for the square vertices. The coordinates are taken
     * from the default length and height values of a Text node.
     */
    private static final float[] DEFAULT_COORDS = {
        -0.0f, -0.5f, 0,
        0.9f, -0.5f, 0,
       -0.0f,  0.5f, 0,
        0.9f,  0.5f, 0,
    };


    /** TextureCoordinates for the square vertices */
    private static final float[] DEFAULT_TEXCOORDS = {
        0, 0,
        1, 0,
        0, 1,
        1, 1,
    };

    /** Reversed TextureCoordinates for the square vertices */
    private static final float[] DEFAULT_TEXCOORDS_REV = {
        1, 0,
        0, 0,
        1, 1,
        0, 1,
    };


    /** Normals for the square vertices */
    private static final float[] DEFAULT_NORMALS = {
        0, 0, 1,
        0, 0, 1,
        0, 0, 1,
        0, 0, 1,
    };

    /** Default strip listing */
    private static final int[] DEFAULT_STRIPS = { 4 };

    /** Java3D image component that contains the texture */
    private ImageComponent2D imgComponent;

    /** Java3D texture that represents the text */
    private Texture2D j3dTexture;

    /** The Geometry object that this uses in the scene */
    private TriangleStripArray j3dGeometry;

    /** Working var for the coordinates in use */
    private float[] coords;

    /** Working var for the texture coordinates in use */
    private float[] texCoords;

    /** Working var for the texture coordinates in use for textured text*/
    private float[] texCoords2;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /**
     * Construct a new default instance of this class.
     */
    public J3DText() {
        super(true);

        listeners = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DText(VRMLNodeType node) {
        super(node, true);

        listeners = new ArrayList();
    }

    //----------------------------------------------------------
    // Methods required by the J3DTextNodeType interface.
    //----------------------------------------------------------

    /**
     * Fetch the texture instance that this node is rendering to. If the
     * implementation uses a texture to render text, return it here. If
     * the implementation doesn't use textures, return null.
     *
     * @return The texture used or null
     */
    public Texture2D getTextTexture() {
        return j3dTexture;
    }

    //----------------------------------------------------------
    // Methods required by the J3DGeometryNodeType interface.
    //----------------------------------------------------------

    /**
     * Returns a J3D Geometry node.
     *
     * @return A Geometry node
     */
    public Geometry[] getGeometry() {
        Geometry[] geom = new Geometry[1];

        geom[0] = j3dGeometry;
        return geom;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }


    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dGeometry;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        createString();
    }

    //----------------------------------------------------------
    // Methods required by the ImageComponent.Updater interface.
    //----------------------------------------------------------

    public void updateData(ImageComponent2D imageComponent,
                           int x,
                           int y,
                           int width,
                           int height) {
        // Do nothing for now.
    }

    //----------------------------------------------------------
    // Methods internal to J3DText
    //----------------------------------------------------------

    /**
     * Update the text string to be drawn.
     *
     * @param sizeChanged true if the underlying texture size changed
     */
    protected void textUpdated(boolean sizeChanged) {
        if(inSetup)
            return;

        if(!sizeChanged) {
            imgComponent.updateData(this, 0, 0, imgWidth, imgHeight);
        }
        else {
            // build a new Texture object and notify the listeners
            createString();
            fireGeometryChanged(null);
        }
    }

    /**
     * Generate SG rep for the String field
     */
    private void createString() {
        // Setup the basic geometry Just a square. Scale it to the size of the
        // image for lines of text. The scale is 1.0 for the first line of text
        // and then 1.0 * spacing for each subsequent line. To work out the
        // length, we need the full texture width, so just scale the

        coords = new float[DEFAULT_COORDS.length];
        System.arraycopy(DEFAULT_COORDS, 0, coords, 0, DEFAULT_COORDS.length);

        float spacing = (vfFontStyle == null) ?
                        1.0f:
                        vfFontStyle.getSpacing();
        float geom_height = 1.0f + ((numString - 1) * (1.0f+spacing));
        float geom_width = ((float)usedPixelWidth) / (float)imgWidth;

        int i;


        for(i = 0; i < coords.length; ) {
            coords[i++] *= geom_width;
            coords[i++] *= geom_height;
            i++;    // do nothing for the z coord
        }


        // This code looks better but is still not right for sizing
        float x_scale;

        // What is the proper pixels/unit for the text texture?
        x_scale = (usedPixelWidth/30.0f);
        
/*        if (geom_width < 0.5)
           x_scale = 4;
        else if (geom_width < 0.75)
            x_scale = 2;
        else
            x_scale = 1;
*/
        // Should check to see if vfScale has useful information.
        boolean generateSize=true;
        if (generateSize) {
        	for(i = 0; i < coords.length; ) {
        		coords[i++] *= x_scale;
        		coords[i++] *= geom_height;
        		i++;    // do nothing for the z coord
        	}
        }
        
        
        // Now work on the texture coordinates. Since the image may use blank
        // space to resize the image to power of two, we set the texture
        // coordinates to always fill up the block with the valid part of the
        // image. The scale is just the used area divided by the total area

        float s_scale = usedPixelWidth / (float)imgWidth;
        float t_scale = usedPixelHeight / (float)imgHeight;

        texCoords = new float[DEFAULT_TEXCOORDS.length];
        texCoords2 = new float[DEFAULT_TEXCOORDS.length];
        if((vfFontStyle != null) && !vfFontStyle.isLeftToRight()) {
            System.arraycopy(DEFAULT_TEXCOORDS_REV,
                             0,
                             texCoords,
                             0,
                             DEFAULT_TEXCOORDS_REV.length);

            System.arraycopy(DEFAULT_TEXCOORDS,
                             0,
                             texCoords2,
                             0,
                             DEFAULT_TEXCOORDS_REV.length);
        } else {
            System.arraycopy(DEFAULT_TEXCOORDS,
                             0,
                             texCoords,
                             0,
                             DEFAULT_TEXCOORDS.length);

            System.arraycopy(DEFAULT_TEXCOORDS,
                             0,
                             texCoords2,
                             0,
                             DEFAULT_TEXCOORDS.length);
        }

        for(i = 0; i < texCoords.length; ) {
            texCoords[i++] *= s_scale;
            texCoords[i++] *= t_scale;
        }

        int format = TriangleStripArray.COORDINATES |
                     TriangleStripArray.NORMALS |
                     TriangleStripArray.TEXTURE_COORDINATE_2;

        int texMap[] = {0, 1, 1, 1, 1, 1, 1, 1};

        j3dGeometry = new TriangleStripArray(4, format, 2,texMap, DEFAULT_STRIPS);
        j3dGeometry.setCoordinates(0, coords);
        j3dGeometry.setTextureCoordinates(0, 0, texCoords);
        j3dGeometry.setTextureCoordinates(1, 0, texCoords2);

        j3dGeometry.setNormals(0, DEFAULT_NORMALS);
        j3dGeometry.setCapability(TriangleStripArray.ALLOW_COORDINATE_READ);
        j3dGeometry.setCapability(TriangleStripArray.ALLOW_FORMAT_READ);
        j3dGeometry.setCapability(TriangleStripArray.ALLOW_COUNT_READ);

        if(!isStatic) {
            j3dGeometry.setCapability(TriangleStripArray.ALLOW_COORDINATE_WRITE);
            j3dGeometry.setCapability(TriangleStripArray.ALLOW_TEXCOORD_WRITE);
        }

        // Need to create the texture again from scratch.
        imgComponent =
            new ImageComponent2D(ImageComponent2D.FORMAT_CHANNEL8,
                                 texturedImage,
                                 true,
                                 false);

        imgComponent.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);

        j3dTexture = new Texture2D(Texture2D.BASE_LEVEL,
                                   Texture.ALPHA,
                                   imgWidth,
                                   imgHeight);
        j3dTexture.setImage(0, imgComponent);
    }

    /**
     * fire a geometry changed event to the listeners.
     *
     * @param items The geometry items that have changed or null for all
     */
    private void fireGeometryChanged(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);

                l.geometryChanged(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
