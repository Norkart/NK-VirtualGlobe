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

package org.web3d.vrml.renderer.j3d.input;

// Standard imports
import java.util.Enumeration;
import javax.media.j3d.*;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.j3d.nodes.J3DAreaListener;

/**
 * A behavior which detects the entry/exit of the view platform with a specified
 * bounding volume
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class J3DProximityBehavior extends Behavior {
    WakeupOnViewPlatformEntry woven;
    WakeupOnViewPlatformExit wovex;
    WakeupCriterion[] conditions;
    WakeupOr wor;
    Bounds bounds;
    J3DAreaListener owner;

    public J3DProximityBehavior(J3DAreaListener owner, Bounds bounds) {
        this.bounds = bounds;
        this.owner = owner;
    }

    public void initialize() {
        setSchedulingBounds(bounds);
        wovex = new WakeupOnViewPlatformExit(bounds);
        woven = new WakeupOnViewPlatformEntry(bounds);
        conditions = new WakeupCriterion[2];
        conditions[0] = wovex;
        conditions[1] = woven;
        wor = new WakeupOr(conditions);
        wakeupOn(wor);
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
        setSchedulingBounds(bounds);
    }

    public void processStimulus(Enumeration ofElements) {
        WakeupCriterion wakeup;

        while(ofElements.hasMoreElements()) {
            wakeup = (WakeupCriterion)ofElements.nextElement();
            if (wakeup instanceof WakeupOnViewPlatformExit)  {
                owner.areaExit();
                postId(BehaviorIDConstants.ROUTE_REQUIRED_ID);
                wakeupOn(new WakeupOnViewPlatformEntry(bounds));
            }
            else if (wakeup instanceof WakeupOnViewPlatformEntry) {
                owner.areaEntry();
                postId(BehaviorIDConstants.ROUTE_REQUIRED_ID);
                wakeupOn(new WakeupOnViewPlatformExit(bounds));
            }
        }
    }
}
