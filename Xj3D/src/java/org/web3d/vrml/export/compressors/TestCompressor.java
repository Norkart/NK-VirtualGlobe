/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export.compressors;

import java.util.HashMap;

import javax.vecmath.Point3d;

import com.sun.j3d.utils.compression.CompressionStream;
import com.sun.j3d.utils.compression.GeometryCompressor;

import org.web3d.vrml.renderer.norender.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.J3DNodeFactory;
import org.web3d.vrml.renderer.norender.NRNodeFactory;
import javax.media.j3d.*;
import org.web3d.vrml.sav.*;

public class TestCompressor implements NodeCompressor, SceneGraphTraversalSimpleObserver {
    /** Traverser for printing proto's */
    private SceneGraphTraverser traverser;

    /** The J3D node factory to get default values from */
    protected J3DNodeFactory factory;

    /** The NR node factory */
    protected NRNodeFactory nrfactory;

    /** Mapping of declaration node to the J3D version */
    private HashMap nodeMap;

    /** The IFS */
    private J3DVRMLNode ifsNode;

    private boolean hasTexCoords;
    private boolean hasNormals;
    private boolean hasColors;

    private GeometryHeader x3dHeader;

    private GeometryStripArray ugeom;

    public TestCompressor() {
        factory = J3DNodeFactory.getJ3DNodeFactory();

        factory.setSpecVersion(3, 1);
        factory.setProfile("Immersive");

        nrfactory = NRNodeFactory.getNRNodeFactory();

        nrfactory.setSpecVersion(3, 1);
        nrfactory.setProfile("Immersive");

        nodeMap = new HashMap();
    }

    /**
     * Compress the given geometry.
     *
     * @param ifs The geometry to compress
     * @return An int array for the geometry
     */
    public int[] compress(VRMLNodeType ifs) {
        traverser = new SceneGraphTraverser();
        traverser.setObserver(this);

        traverser.traverseGraph(ifs);

        ifsNode.setupFinished();

        GeometryArray implGeom = (GeometryArray) ifsNode.getSceneGraphObject();

        if (implGeom == null) {
            System.out.println("***No J3D geometry returned!");
            System.out.println("ifs: " + ifs);
            return new int[0];
        }

        int format = GeometryArray.COORDINATES;

        if (hasNormals)
            format = format | GeometryArray.NORMALS;

        GeometryArray newGeom = null;

        if (implGeom instanceof IndexedGeometryArray) {
            System.out.println("Got indexed array");
            int cnt = ((IndexedGeometryArray)implGeom).getIndexCount();

            newGeom = new IndexedTriangleArray(implGeom.getVertexCount(),
                                     format, cnt);

            int[] indices = new int[cnt];
            ((IndexedGeometryArray)newGeom).setCoordinateIndices(0, indices);

        } else {
            newGeom = new TriangleArray(implGeom.getVertexCount(),
                                     format);
        }

        float[] coords = new float[implGeom.getVertexCount() * 3];
System.out.println("Vertices: " + implGeom.getVertexCount());
        implGeom.getCoordinates(0, coords);
        newGeom.setCoordinates(0, coords);
/*
for(int i=0; i < implGeom.getVertexCount(); i++) {
System.out.println(coords[i*3] + " " + coords[i*3+1] + " " + coords[i*3+2]);
}
*/
        System.out.println("ifs: " + ifsNode + " SG: " + ifsNode.getSceneGraphObject());
        Shape3D[] shapes = new Shape3D[1];
        shapes[0] = new Shape3D();


        if (hasTexCoords) {
            System.out.println("TextureCoordinates not supported yet");
        }

        if (hasNormals) {
            System.out.println("Has normals");
            float[] norms = new float[implGeom.getVertexCount() * 3];
            implGeom.getNormals(0, norms);
            newGeom.setNormals(0, norms);
        }

        if (hasColors) {
            System.out.println("Has colors");
            float[] colors = new float[implGeom.getVertexCount() * 3];
            implGeom.getColors(0, colors);
            newGeom.setColors(0, colors);
        }

        shapes[0].setGeometry(newGeom);
        CompressionStream cs = new CompressionStream(16,9,6,shapes);
        //CompressionStream cs = new CompressionStream(6,9,4,shapes);

        GeometryCompressor gc = new GeometryCompressor();
        CompressedGeometry cgeom = gc.compress(cs);

        CompressedGeometryHeader header = new CompressedGeometryHeader();
        cgeom.getCompressedGeometryHeader(header);

        GeometryHeader x3dHeader = createHeader(cs);

        int len = cgeom.getByteCount();
        byte[] bytes = new byte[len];
        cgeom.getCompressedGeometry(bytes);

        System.out.println("Len: " + len + " div 4: " + (len / 4.0f) + " total: " + (len / 4.0f + GeometryHeader.getSize()));

        int size = len / 4;
        int ghSize = GeometryHeader.getSize();

        int[] ret_val = new int[ghSize + size];
        int idx = ghSize;

        System.out.println("Header: " + x3dHeader);
        x3dHeader.encode(ret_val,0);

        for(int i=0; i < size; i++) {
            ret_val[idx++] = (bytes[i * 4] << 24) | ((bytes[i*4+1] & 0xFF) << 16) |
               ((bytes[i*4+2] & 0xFF) << 8) | (bytes[i*4+3] & 0xFF);
        }

        // TODO: Can we avoid changing this array over to ints?
        return ret_val;
    }

    /**
     * Decompress the data into a geometry.
     *
     * @param data The compressed data
     */
    public void decompress(int[] data) {
System.out.println("decompress: size: " + data.length);
        int ghSize = GeometryHeader.getSize();

        x3dHeader = new GeometryHeader();
        x3dHeader.decode(data,0);

        System.out.println("Header: " + x3dHeader);

        CompressedGeometryHeader cgh = new CompressedGeometryHeader();
        cgh.bufferType = CompressedGeometryHeader.TRIANGLE_BUFFER;

        if (hasNormals)
            cgh.bufferDataPresent = CompressedGeometryHeader.NORMAL_IN_BUFFER;

        cgh.size = data.length * 4 - ghSize * 4;

        //cgh.lowerBound = new Point3d(dis2.readDouble(),dis2.readDouble(),dis2.readDouble());
        //cgh.upperBound = new Point3d(dis2.readDouble(),dis2.readDouble(),dis2.readDouble());

        byte[] cgBuffer = new byte[cgh.size];
        int len = data.length;
        int idx = 0;

        // Copy int[] to byte[]
        for(int i=ghSize; i < len; i++) {
            cgBuffer[idx++] = (byte) (data[i] >> 24);
            cgBuffer[idx++] = (byte) ((data[i] & 0x00FF0000) >> 16);
            cgBuffer[idx++] = (byte) ((data[i] & 0x0000FF00) >> 8);
            cgBuffer[idx++] = (byte) ((data[i] & 0x000000FF));
        }

        CompressedGeometry geom = new CompressedGeometry(cgh, cgBuffer) ;
        Shape3D shapes[] = geom.decompress();

        ugeom = (GeometryStripArray) shapes[0].getGeometry();

        System.out.println("geom: " + ugeom);

        VRMLNodeType tss = (VRMLNodeType) nrfactory.createVRMLNode("TriangleStripSet", false);
        int numStrips = ugeom.getNumStrips();
        int[] stripCounts = new int[numStrips];
        ugeom.getStripVertexCounts(stripCounts);
        float[] coords = new float[ugeom.getVertexCount() * 3];

        ugeom.getCoordinates(0,coords);

        System.out.println("Coords: " + coords.length);

        VRMLNodeType coordNode = (VRMLNodeType) nrfactory.createVRMLNode("Coordinate", false);
        idx = coordNode.getFieldIndex("point");
        coordNode.setValue(idx, coords, coords.length);
        idx = tss.getFieldIndex("coord");
        tss.setValue(idx, coordNode);

        if (hasNormals) {
            float[] norms = new float[coords.length];
            ugeom.getNormals(0, norms);
            VRMLNodeType normalNode = (VRMLNodeType) nrfactory.createVRMLNode("Normal", false);
            idx = normalNode.getFieldIndex("vector");
            normalNode.setValue(idx, norms, norms.length);
            idx = tss.getFieldIndex("normal");
            tss.setValue(idx, normalNode);
        }

        idx = tss.getFieldIndex("stripCount");
        tss.setValue(idx, stripCounts, stripCounts.length);

        System.out.println("Decompress data: " + ugeom);
        //return (VRMLNodeType) tss;
    }

    /**
     * Checks whether this compressors handles this data.  If not then
     * the data should be written to the stream.
     *
     * @param nodeName the node
     * @param fieldName the field
     */
    public boolean handleData(String nodeName, String fieldName) {
//System.out.println("Check: " + nodeName + " fieldName: " + fieldName);
        return true;
/*
        if (fieldName.equals("point"))
            return false;
        else
            return true;
*/
    }

    /**
     * Fill in the data handled by the compressor.
     *
     * @param nodeName The nodeName
     * @param ch The handler to write to
     */
    public void fillData(String nodeName, BinaryContentHandler ch) {
        // TODO: Make hashmap

System.out.println("fillData: " + nodeName);
        if (nodeName.equals("TriangleStripSet")) {
            VRMLNodeType tss = (VRMLNodeType) nrfactory.createVRMLNode("TriangleStripSet", false);
            int numStrips = ugeom.getNumStrips();
            int[] stripCounts = new int[numStrips];
            ugeom.getStripVertexCounts(stripCounts);

            ch.startField("stripCount");
            ch.fieldValue(stripCounts, stripCounts.length);

            // TODO: This route is really slow, not sure why
System.out.println("StripCounts: " + stripCounts.length);
        } else if (nodeName.equals("IndexedFaceSet")) {
            VRMLNodeType tss = (VRMLNodeType) nrfactory.createVRMLNode("IndexedFaceSet", false);

            int numStrips = ugeom.getNumStrips();
            int[] stripCounts = new int[numStrips];
            ugeom.getStripVertexCounts(stripCounts);
            int numIndices = 0;

System.out.println("Number of Strips: " + stripCounts.length);
            for(int i=0; i < stripCounts.length; i++) {
                numIndices += 4 + (stripCounts[i] - 3) * 4;
            }

System.out.println("**Converting Strip to Flat: " + numIndices);
            // Its a flat array of triangles
            int[] indices = new int[numIndices];
            int idx = 0;
            int icnt = 0;

            for(int i=0; i < stripCounts.length; i++) {
                // Handle first 3 vertices
                indices[icnt++] = idx++;
                indices[icnt++] = idx++;
                indices[icnt++] = idx++;
                indices[icnt++] = -1;

                for(int j=0; j < stripCounts[i] - 3; j++) {
                    indices[icnt++] = idx - 2;
                    indices[icnt++] = idx - 1;
                    indices[icnt++] = idx++;
                    indices[icnt++] = -1;
                }
            }
/*
System.out.println("Indices: ");
for(int i=0; i < indices.length; i++) {
System.out.print(indices[i] + " ");
}
System.out.println();
*/
            ch.startField("coordIndex");
            ch.fieldValue(indices, indices.length);
System.out.println("coordIndex: " + indices.length);
        } else if (nodeName.equals("IndexedTriangleSet")) {
/*
            int numStrips = ugeom.getNumStrips();
            int[] stripCounts = new int[numStrips];
            ugeom.getStripVertexCounts(stripCounts);

            IndexedGeometryArray igeom = (IndexedGeometryArray) ugeom;
            int numIndices = igeom.getIndexCount();
            int[] origIndices = new int[numIndices];
            igeom.getCoordinateIndices(origIndices);

System.out.println("Number of Strips: " + stripCounts.length);
            for(int i=0; i < stripCounts.length; i++) {
                numIndices += 3 + (stripCounts[i] - 3) * 3;
            }

System.out.println("**Converting Strip to Flat: " + numIndices);
            // Its a flat array of triangles
            int[] indices = new int[numIndices];
            int idx = 0;
            int icnt = 0;

            for(int i=0; i < stripCounts.length; i++) {
                // Handle first 3 vertices
                indices[icnt++] = origIndices[idx++];
                indices[icnt++] = origIndices[idx++];
                indices[icnt++] = origIndices[idx++];

                for(int j=0; j < stripCounts[i] - 3; j++) {
                    indices[icnt++] = origIndices[idx - 2];
                    indices[icnt++] = origIndices[idx - 1];
                    indices[icnt++] = origIndices[idx++];
                }
            }
System.out.println("Indices: ");
for(int i=0; i < indices.length; i++) {
System.out.print(indices[i] + " ");
}
System.out.println();
            ch.startField("index");
            ch.fieldValue(indices, indices.length);
System.out.println("coordIndex: " + indices.length);
*/
        } else if (nodeName.equals("Coordinate")) {
            float[] coords = new float[ugeom.getVertexCount() * 3];
            ugeom.getCoordinates(0,coords);
System.out.println("coords: " + coords.length);
            float[] trans = x3dHeader.getBounds();
            float scale = x3dHeader.getScale();

            int len;

            // Apply translate and scale to return model to original coordinates
            len = ugeom.getVertexCount();

            for(int i=0; i < len; i++) {
                //System.out.println("Comp Coord: " + coords[i*3] + " " + coords[i*3+1] + " " + coords[i*3+2]);
                coords[i*3] = scale * (coords[i*3]) + (trans[0]);
                coords[i*3 + 1] = scale * (coords[i*3 + 1]) + (trans[1]);
                coords[i*3 + 2] = scale * (coords[i*3 + 2]) + (trans[2]);

                //System.out.println("Orig Coord: " + coords[i*3] + " " + coords[i*3+1] + " " + coords[i*3+2]);
            }

            ch.startField("point");
            ch.fieldValue(coords, coords.length);
        } else if (nodeName.equals("Normal")) {
            float[] norms = new float[ugeom.getVertexCount() * 3];
            ugeom.getNormals(0,norms);
System.out.println("normals: " + norms.length);
            ch.startField("vector");
            ch.fieldValue(norms, norms.length);
        }
    }

    /** Get any metadata the encoder would like to write to the file.
     *
     *  @return The metadata in X3D XML encoding
     */
    public String getEncoderMetadata() {
        return "<MetadataSet name='Stats'><MetadataString name='rate' value='15:1' /></MetadataSet>";
    }

    //-------------------------------------------------------------------------
    // SceneGraphTraverserSimpleObserver methods
    //-------------------------------------------------------------------------

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {


        if (child instanceof VRMLNormalNodeType)
            hasNormals = true;
        else if (child instanceof VRMLTextureNodeType)
            hasTexCoords = true;
        else if (child instanceof VRMLColorNodeType)
            hasColors = true;
        else if (child instanceof VRMLMetadataObjectNodeType) {
            // Ignore metadata, its handled elsewhere
            return;
        }

        VRMLNodeType out_kid;

        if(used) {
            System.out.println("USE not handled");
            out_kid = (VRMLNodeType)nodeMap.get(child);
        } else if(!(child instanceof VRMLProtoInstance)) {
            out_kid = (VRMLNodeType)factory.createVRMLNode(child, false);
            nodeMap.put(child, out_kid);
        } else {
            out_kid = child;
            nodeMap.put(child, out_kid);
        }

        if (ifsNode == null)
            ifsNode = (J3DVRMLNode) out_kid;

        if(parent == null)
            return;

        VRMLNodeType out_parent = (VRMLNodeType)nodeMap.get(parent);
        VRMLFieldDeclaration decl = parent.getFieldDeclaration(field);
        int idx = out_parent.getFieldIndex(decl.getName());

        try {
            out_parent.setValue(idx, out_kid);
        } catch(FieldException ife) {
            ife.printStackTrace();
        }
    }

    /**
     * Create the header needed to reconstitute the geometry.
     *
     * @return The header
     */
    private GeometryHeader createHeader(CompressionStream cs) {
        Point3d[] modelBounds = cs.getModelBounds();
        Point3d[] normalizedBounds = cs.getNormalizedBounds();

        System.out.println("ModelBounds:");
        for(int i=0; i < modelBounds.length; i++) {
            System.out.print(modelBounds[i] + " ");
        }
        System.out.println("\nNormalizedBounds:");
        for(int i=0; i < normalizedBounds.length; i++) {
            System.out.print(normalizedBounds[i] + " ");
        }
        System.out.println();

        // find axis with greatest range for accuracy

        int axis = -1;
        double largest = Double.MIN_VALUE;
        double range;

        range = modelBounds[1].x - modelBounds[0].x;
        if (range > largest) {
            largest = range;
            axis = 0;
        }

        range = modelBounds[1].y - modelBounds[0].y;
        if (range > largest) {
            largest = range;
            axis = 1;
        }
        range = modelBounds[1].z - modelBounds[0].z;
        if (range > largest) {
            largest = range;
            axis = 2;
        }

        double scale;

        if (axis == 0) {
            scale = largest / (normalizedBounds[1].x - normalizedBounds[0].x);
        } else if (axis == 1) {
            scale = largest / (normalizedBounds[1].y - normalizedBounds[0].y);
        } else {
            scale = largest / (normalizedBounds[1].z - normalizedBounds[0].z);
        }

        System.out.println("Scale: " + scale);

        float[] bounds = new float[3];
        bounds[0] = (float) (modelBounds[1].x + modelBounds[0].x) / 2.0f;
        bounds[1] = (float) (modelBounds[1].y + modelBounds[0].y) / 2.0f;
        bounds[2] = (float) (modelBounds[1].z + modelBounds[0].z) / 2.0f;

        GeometryHeader header = new GeometryHeader((float)scale, bounds,
           true, hasNormals, hasColors, hasTexCoords);

        return header;
    }
}