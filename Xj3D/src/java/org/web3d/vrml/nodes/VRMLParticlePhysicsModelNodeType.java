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
package org.web3d.vrml.nodes;

// External imports
import org.j3d.geom.particle.ParticleFunction;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * A marker node type which specifies a physics rule used by particle systems.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLParticlePhysicsModelNodeType extends VRMLNodeType {

    /**
     * Return the particle function implementation that this model controls.
     * May not be available until after setupFinished() is called.
     *
     * @return The current function to control the system
     */
    public ParticleFunction getParticleFunction();

    /**
     * Set the enabled state of the Physics model.
     *
     * @param state true to enable the use of this model
     */
    public void setEnabled(boolean state);

    /**
     * Get the current enabled state of this model.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled();

}
