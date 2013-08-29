/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.CylinderGenerator;

// Local imports
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;


/**
 * Geometry holder that represents the X3D Cylinder node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class CylinderGeometry extends TriangulationGeometry  {

    /** The radius of the base of the cone */
    private float radius;

    /** The height of the cone */
    private float height;

    /** Indicator if this cone is solid or not */
    private boolean solid;

    /**
     * Construct a default instance of this box.
     */
    CylinderGeometry() {
        radius = 2;
        height = 2;
        solid = true;
    }

    //----------------------------------------------------------
    // Methods defined by TriangulationGeometry
    //----------------------------------------------------------

    /**
     * Clear the currently stored values and return to the defaults for
     * this geometry type.
     */
    void reset() {
        radius = 2;
        height = 2;
        solid = true;
    }

    /**
     * Add a new field value to the geometry. The form of the value is
     * not defined and is up to the implementing class to interpret it
     * according to the needed fields. Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     */
    void addFieldValue(String name, Object value) {
        if(name.equals("radius")) {
            if(value instanceof String) {
               radius = fieldReader.SFFloat((String)value);
            } else if(value instanceof float[]) {
                radius = ((Float)value).floatValue();
            }
        } else if(name.equals("height")) {
            if(value instanceof String) {
               height = fieldReader.SFFloat((String)value);
            } else if(value instanceof float[]) {
                height = ((Float)value).floatValue();
            }
        } else if(name.equals("solid")) {
            if(value instanceof String) {
               solid = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                solid = ((Boolean)value).booleanValue();
            }
        }
    }

    /**
     * Add a new field value to the geometry using array data. The
     * form of the value is  not defined and is up to the implementing
     * class to interpret it according to the needed fields.  The
     * array length is the number of valid items in the passed array.
     * <p>
     * Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     * @param len The length of the valid data in the array
     */
    void addFieldValue(String name, Object value, int len) {
    }

    /**
     * The geometry definition is now finished so take the given field
     * values and generate the triangle output.
     *
     * @param ch The content handler instance to write to
     * @param sh The script handler instance to write to
     * @param ph The proto handler instance to write to
     * @param rh The route handler instance to write to
     */
    void generateOutput(ContentHandler ch,
                        ScriptHandler sh,
                        ProtoHandler ph,
                        RouteHandler rh) {
		// Set up the coordinate array and indices.
        CylinderGenerator generator = new CylinderGenerator(height, radius);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        ch.startNode("IndexedTriangleSet", null);
        ch.startField("coord");
        ch.startNode("Coordinate", null );
        ch.startField("point");

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(data.coordinates, data.coordinates.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < data.coordinates.length; i++) {
                buf.append(data.coordinates[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endField(); // point
        ch.endNode();  // Coordinate
        ch.endField(); // coord

        ch.startField("index");

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(data.indexes, data.indexes.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < data.indexes.length; i++) {
                buf.append(data.indexes[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endField(); // index

        if(!solid) {
            ch.startField("solid");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(solid);
            } else if(ch instanceof StringContentHandler) {

                ((StringContentHandler)ch).fieldValue("FALSE");
            }

            ch.endField(); // solid
        }

        ch.endNode();  // IndexedTriangleSet
    }
}
