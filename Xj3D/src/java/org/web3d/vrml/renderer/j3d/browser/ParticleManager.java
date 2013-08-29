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

package org.web3d.vrml.renderer.j3d.browser;

// Standard imports
import javax.media.j3d.WakeupOnElapsedFrames;

import org.j3d.geom.particle.ParticleSystemManager;

// Application specific imports
import org.web3d.vrml.renderer.j3d.nodes.J3DParticleSystemManager;

/**
 * Java3D implementation of a manager for particle systems in the current
 * scene.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ParticleManager extends ParticleSystemManager
    implements J3DParticleSystemManager {

    /**
     * Construct a new, default manager instance that wakes up every
     * frame.
     */
    ParticleManager() {
        super(new WakeupOnElapsedFrames(0), null);
    }

    /**
     * Construct a new, default manager instance that wakes up every
     * given number of frames.
     */
    ParticleManager(int frames) {
        super(new WakeupOnElapsedFrames(frames), null);
    }
}
