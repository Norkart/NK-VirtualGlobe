/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.geospatial;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoLOD;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * NoRender implementation of an GeoLOD
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRGeoLOD extends BaseGeoLOD
    implements NRVRMLNode {

    /**
     * Default constructor
     */
    public NRGeoLOD() {
        super();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRGeoLOD(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiExternalNodeType
    //----------------------------------------------------------

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This should
     * be one of the forms that the prefered class type call generates.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(int index, String mimetype, Object content)
        throws IllegalArgumentException {

        // do nothing for now.
    }
}
