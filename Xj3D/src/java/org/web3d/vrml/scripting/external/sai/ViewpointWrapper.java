/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.xj3d.sai.Xj3DViewpoint;

/**
 * Wrapper interface for the internal viewpoint representation, to allow
 * abstraction of the user interface description of viewpoints from the
 * underlying node representation.
 * <p>
 *
 * This class deliberately does not give access to the SAI X3DNode that
 * represents the viewpoint.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ViewpointWrapper implements Xj3DViewpoint {

    /** The viewpoint node that we are wrapping */
    private VRMLViewpointNodeType realViewpoint;

    /**
     * Create a new wrapper for the given node instance
     */
    ViewpointWrapper(VRMLViewpointNodeType vp) {
        realViewpoint = vp;
    }

    //-------------------------------------------------------------------
    // Methods defined by Xj3DViewpoint
    //-------------------------------------------------------------------

    /**
     * Get the description String from this viewpoint. If there was no
     * description string provided in the source file, then this will
     * an empty, zero-length string as per the default value for SFString.
     *
     * @return A string containing the description
     */
    public String getDescription() {
        return realViewpoint.getDescription();
    }

    //-------------------------------------------------------------------
    // Methods defined by Object
    //-------------------------------------------------------------------

    /**
     * Compare this object for equality to another object.
     */
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ViewpointWrapper))
            return false;

        return ((ViewpointWrapper)obj).realViewpoint.equals(realViewpoint);
    }

    /**
     * Generate a hashcode for this object.
     *
     * @return The generated hash code
     */
    public int hashCode() {
        return realViewpoint.hashCode();
    }

    //-------------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------------

    /**
     * Fetch the underlying Xj3D representation of the viewpoint.
     *
     * @return The viewpoint instance
     */
    VRMLViewpointNodeType getRealViewpoint() {
        return realViewpoint;
    }
}
