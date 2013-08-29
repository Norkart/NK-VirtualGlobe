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
import org.j3d.geom.particle.ParticleSystem;

// Local imports
// None


/**
 * A node which specifies an emitter of particles for a particle system.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLParticleSystemNodeType
    extends VRMLShapeNodeType, VRMLBoundedNodeType {

    /**
     * Get the internal initialiser model that will be used for this particle
     * system implementation. This may not be available until after
     * setupFinished() has been called.
     *
     * @return The initialiser instance to use
     */
    public ParticleSystem getSystem();

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

    /**
     * Check to see if this node is currently active for evaluation.
     *
     * @return true if the node still can run this frame
     */
    public boolean isActive();

    /**
     * Manually set the particle system to inactive due to the behaviour of
     * the internals. The manager has decided that this node is no longer
     * needing to be run, so indicate that the activity level has changed.
     *
     * @param state true to set this as active, false for inActive
     */
    public void setActive(boolean state);

}
