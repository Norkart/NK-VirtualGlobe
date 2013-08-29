/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard library imports
import java.io.*;

// Application specific imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.parser.VRMLFieldReader;

import javax.media.j3d.*;

import com.sun.j3d.utils.compression.*;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3d;

/**
 * A node compressor for J3DIndexedFaceSet nodes.  Implements the compression
 * method devised by the Java3D team.
 *
 * IPR Warning: This method has patents on it(Sun Microsystems).  We are trying to secure a
 * royality free license for X3D implementations.
 *
 *
 * @author Alan Hudson.
 * @version $Revision: 1.2 $
 */
public class J3DIndexedFaceSetDecompressor  {
    /**
     * Can this NodeCompressor support this compression method
     *
     * @param nodeNumber What node, constant defined by Web3D Consortium
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     */
    public boolean canSupport(int nodeNumber, int method) {
        // TODO: get a real value
        return true;
    }

    public void decompress(InputStream dis, BinaryContentHandler ch) throws IOException {
        CompressedGeometryHeader cgh = new CompressedGeometryHeader();

        // TODO: Why is this necessary?
        System.out.println("tag?: " + dis.read() + " and: " + dis.read());
//        dis.read();
//        dis.read();

        cgh.bufferType = readInt(dis);
        cgh.bufferDataPresent = readInt(dis);
        cgh.size = readInt(dis);
        cgh.start = readInt(dis);

        DataInputStream dis2 = new DataInputStream(dis);
        cgh.lowerBound = new Point3d(dis2.readDouble(),dis2.readDouble(),dis2.readDouble());
        cgh.upperBound = new Point3d(dis2.readDouble(),dis2.readDouble(),dis2.readDouble());

System.out.println("bufferType: " + cgh.bufferType);
System.out.println("bufferDataPresent: " + cgh.bufferDataPresent);
System.out.println("size: " + cgh.size);
System.out.println("start: " + cgh.start);
System.out.println("lower: " + cgh.lowerBound);
System.out.println("upper: " + cgh.upperBound);

        byte[] cgBuffer = new byte[cgh.size];
        dis.read(cgBuffer, 0, cgh.size);

        CompressedGeometry geom = new CompressedGeometry(cgh, cgBuffer) ;
        Shape3D shapes[] = geom.decompress();

        ch.startNode("IndexedFaceSet",null);
        GeometryArray ugeom = (GeometryArray) shapes[0].getGeometry();
System.out.println("geom: " + ugeom);

        int vtx_count = ugeom.getVertexCount();
        int tri_count = vtx_count / 3;

        float[] buff = new float[vtx_count];

        ugeom.getCoordinates(0, buff);

/*
        System.out.println("Coords:  num: " + ugeom.getVertexCount() + " tri_count: " + tri_count);
        for(int i=0; i < buff.length; i++) {
            System.out.print(buff[i] + " ");
        }
        System.out.println();
*/
        ch.startField("coord");
           ch.startNode("Coordinate","");
           ch.startField("point");
              ch.fieldValue(buff, buff.length);
           ch.endField();
           ch.endNode();
        ch.endField();

        ch.startField("coordIndex");
        int[] index = new int[(tri_count / 3) * 4];
        int idx=0;
        for(int i=0; i < tri_count / 3; i++) {
            index[i*4] = idx++;
            index[i*4+1] = idx++;
            index[i*4+2] = idx++;
            index[i*4+3] = -1;
        }
/*
        System.out.println("CoordIndex:");
        for(int i=0; i < index.length; i++) {
            System.out.print(index[i] + " ");
        }
        System.out.println();
*/
        ch.fieldValue(index, index.length);
        // TODO: Do not call endNode, handled by outer stuff, do we like this?
    }

    private int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

}
