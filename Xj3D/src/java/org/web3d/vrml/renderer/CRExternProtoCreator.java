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

package org.web3d.vrml.renderer;

// Standard imports
import java.util.*;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoFieldInfo;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoScene;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.CRROUTE;

/**
 * A class that is used to create stub instances of extern protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * The implementation is designed to be derived by a concrete instance and
 * should never be created directly. This derived class should implement a
 * method
 * <pre>
 *   public <i>Renderer</i>ProtoInstance createInstanceExternalPrototypeDecl proto);
 * </pre>
 *
 * This method takes the declaration and creates the true proto instance, that
 * extends {@link org.web3d.vrml.renderer.CRProtoInstance}. This instance is
 * then passed to the protected <code>createInstance()</code> method of this
 * class, which then does all the hard work of building the correct instance.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class CRExternProtoCreator {

    /** Flag to say if the current proto instance is VRML97 capable */
    protected boolean isVRML97;

    /** The current world's root URL */
    protected String worldURL;

    /**
     * Create a new creator instance for the given world root URL
     *
     * @param worldURL the current world's root URL
     */
    protected CRExternProtoCreator(String worldURL) {

      this.worldURL = worldURL;
    }

    /**
     * Build a stub instance of the proto from the given description.
     *
     * @param proto The extern proto to stub from
     */
    protected void createInstance(CRProtoInstance proto) {

        ProtoScene scene = new ProtoScene(isVRML97 ? 2 : 3, 0);

        // All done now!
        proto.setContainedScene(scene);
    }
}
