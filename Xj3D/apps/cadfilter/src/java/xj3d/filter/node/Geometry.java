/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports

// Local imports

/**
 * Base abstract impl wrapper for X3DGeometry nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class Geometry extends AbstractEncodable {

    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected Geometry(String name, String defName) {
        super(name, defName);
    }
}
