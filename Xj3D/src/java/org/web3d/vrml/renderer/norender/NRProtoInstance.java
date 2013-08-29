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

package org.web3d.vrml.renderer.norender;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * A concrete instance of a Prototype in the Null renderer realm.
 *<p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class NRProtoInstance extends CRProtoInstance
    implements NRVRMLNode {

    /**
     * Create an instance for the proto with the number of fields. To set the
     * values of these fields, use the normal setValue methods. The fields are
     * list does not care if it contains null values.
     *
     * @param name The node name of the proto
     * @param vrml97 true if this is a VRML97 issue proto
     * @param The fields that need to be set here
     * @param numBodyNodes The number of nodes in the body of the proto
     */
    NRProtoInstance(String name,
                     boolean vrml97,
                     VRMLFieldDeclaration[] fields,
                     int numBodyNodes) {

        super(name, vrml97, fields, numBodyNodes);
    }
}
