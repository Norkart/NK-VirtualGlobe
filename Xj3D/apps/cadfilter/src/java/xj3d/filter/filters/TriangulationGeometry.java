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

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.parser.VRMLFieldReader;

import xj3d.filter.AbstractFilter;


/**
 * Base representation of per-geometry object data for the triangulation
 * class.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
abstract class TriangulationGeometry  {

    /** The field parser suitable for the current content */
    protected VRMLFieldReader fieldReader;

    /** Error handler for reporting oddities */
    protected ErrorReporter errorReporter;

    /**
     * Set the error handler instance used for this  instance.
     *
     * @param reporter The error reporter instance to use
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;
    }

    /**
     * Set the field reader instance to use for parsing fields here.
     * The reader corresponds to the spec version of the read file.
     *
     * @param reader The reader instance to use
     */
    void setFieldReader(VRMLFieldReader reader) {
        fieldReader = reader;
    }

    /**
     * Set the arguments available to the system. This can be used to
     * modify the behaviour of the output. For example, it can tell it
     * to generate a smooth surface regardless or only non-indexed geometry.
     * The default implementation does nothing, so override if you need to
     * keep track of the arguments.
     *
     * @param args The list of arguments to use. Zero length if none provided
     */
    void setArguments(String[] args) {
    }

    /**
     * Clear the currently stored values and return to the defaults for
     * this geometry type.
     */
    abstract void reset();

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
    abstract void addFieldValue(String name, Object value);

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
    abstract void addFieldValue(String name, Object value, int len);

    /**
     * The geometry definition is now finished so take the given field
     * values and generate the triangle output.
     *
     * @param ch The content handler instance to write to
     * @param sh The script handler instance to write to
     * @param ph The proto handler instance to write to
     * @param rh The route handler instance to write to
     */
    abstract void generateOutput(ContentHandler ch,
                                 ScriptHandler sh,
                                 ProtoHandler ph,
                                 RouteHandler rh);
}
