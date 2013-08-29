/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import org.j3d.aviatrix3d.ViewEnvironment;

// Local imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Internal data holder used by viewpoint resize manager.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class ViewpointData {
    /** The Viewpoint */
    VRMLViewpointNodeType viewpoint;

    /** The AV3D viewEnvironment instance that is being managed */
    ViewEnvironment viewEnvironment;
}
