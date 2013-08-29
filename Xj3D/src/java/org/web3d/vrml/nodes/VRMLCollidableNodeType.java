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

// Standard Imports

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * A marker interface for describing a node that can handle collision
 * control and events.
 * <p>
 *
 * The purpose of this interface is to act as a common gateway between the
 * low-level detection routines and the VRML geometry. The methods here are to
 * be used for that purpose, not for external authoring APIs and scripting to
 * make state changes.
 * <p>
 *
 * At this stage the API is quite simple. However, for CollidableGeometry,
 * there will need to be a lot more low-level information provided. An example
 * of this is the precise coordinates of the intersection point.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLCollidableNodeType extends VRMLNodeType {

    /**
     * Notification that a collision has just taken place. This
     * collision should result in the appropriate event outs being
     * generated.
     */
    public void collisionDetected();
}
