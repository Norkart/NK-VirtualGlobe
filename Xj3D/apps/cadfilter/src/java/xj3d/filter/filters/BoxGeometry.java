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
// None

// Local imports
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;


/**
 * Geometry holder that represents the X3D Box node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class BoxGeometry extends TriangulationGeometry  {

    /** The size attributes of the box in all 3 dimensions */
    private float[] size;

    /** Indicator if this cone is solid or not */
    private boolean solid;

    /**
     * Construct a default instance of this box.
     */
    BoxGeometry() {
        size = new float[3];
        size[0] = 2;
        size[1] = 2;
        size[2] = 2;

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
        size[0] = 2;
        size[1] = 2;
        size[2] = 2;
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
        if(name.equals("size")) {
            float[] s = null;

            if(value instanceof String) {
                s = fieldReader.SFVec3f((String)value);
            } else if(value instanceof String[]) {
                s = fieldReader.SFVec3f((String[])value);
            } else if(value instanceof float[]) {
                s = (float[])value;
            }

            if(s != null) {
                size[0] = s[0];
                size[1] = s[1];
                size[2] = s[2];
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

        // Set up the coordinate array and indices here
        float x = size[0] * 0.5f;
        float y = size[1] * 0.5f;
        float z = size[2] * 0.5f;

        float[] coords = {
            x,  y,  z,
           -x,  y,  z,
           -x, -y,  z,
            x, -y,  z,
            x,  y, -z,
           -x,  y, -z,
           -x, -y, -z,
            x, -y, -z,
        };

        int[] indices = {
            0, 1, 2,
            0, 2, 3,
            7, 0, 3,
            7, 4, 0,
            1, 5, 6,
            1, 6, 2,
            4, 5, 1,
            4, 1, 0,
            3, 2, 6,
            3, 6, 7,
            7, 6, 5,
            7, 5, 4
        };

        ch.startNode("IndexedTriangleSet", null);
        ch.startField("coord");
        ch.startNode("Coordinate", null );
        ch.startField("point");

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(coords, coords.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < coords.length; i++) {
                buf.append(coords[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endField(); // point
        ch.endNode();  // Coordinate
        ch.endField(); // coord

        ch.startField("index");

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(indices, indices.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < indices.length; i++) {
                buf.append(indices[i]);
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
