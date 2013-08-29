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
import javax.media.j3d.*;

import java.awt.Color;
import java.awt.GraphicsConfiguration;

// Application specific imports
// none

/**
 * A single canvas that is dependent on another to provide all of the scene
 * graph information.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class VRMLDependentCanvas extends Canvas3D {

    /** The View that we use for everything */
    private View localView;

    /**
     * Construct an empty canvase that contains a single view
     * that is provided by the user..This constructor would be used when
     * you want to create the initial eye of a stereo pair.
     *
     * @param cfg The graphics configuration to use
     * @param view The view information to use for this class
     */
    public VRMLDependentCanvas(GraphicsConfiguration cfg, View view) {
        super(cfg);

        localView = view;
        view.addCanvas3D(this);
        setBackground(Color.black);
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    //----------------------------------------------------------
    // Internal convenience methods.
    //----------------------------------------------------------
}
