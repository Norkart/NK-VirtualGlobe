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

package org.xj3d.core.loading;

// External imports
// None

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.sav.StringContentHandler;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.RouteHandler;
import org.web3d.vrml.sav.ScriptHandler;

/**
 * Definition of a class that is used to build scenes from parsed content.
 * <p>
 *
 * The interface is used to define a complete system that can be passed to
 * a VRMLReader instance and create a complete scene graph so that it can
 * be used within scripts.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SceneBuilder
    extends StringContentHandler,
            BinaryContentHandler,
            ProtoHandler,
            RouteHandler,
            ScriptHandler {

    /**
     * Set the error handler instance used by this instance of the builder. The
     * handler is used to report errors at the higher level. A value of null
     * will clear the current instance and return to the default handling.
     *
     * @param reporter The handler instance to use or null to clear
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Reset the builder. This is used to make sure that the builder has been
     * reset after a parsing run just in case the last parsing run exited
     * abnormally and left us in an odd state. Sometimes this can prevent us
     * from parsing again. This method should be called just before the
     * {@link org.web3d.vrml.sav.VRMLReader#parse} method is called.
     */
    public void reset();

    /**
     * Set the frame state manager to use for the builder after
     * this point. Set a value of null it will clear the currently set items.
     *
     * @param fsm The state manager to use
     */
    public void setFrameStateManager(FrameStateManager fsm);

    /**
     * Change the builder to recognise only VRML97 content or allow any
     * version to be loaded.
     *
     * @param enabled true to restrict content to VRML97 only
     */
    public void allowVRML97Only(boolean enabled);

    /**
     * Get the scene that was last built by this scene handler. If none of the
     * methods have been called yet, this will return a null reference. The
     * scene instance returned by this builder will not have had any external
     * references resolved. Externprotos, scripts, Inlines and all other nodes
     * that reference part of their data as a URL will need to be loaded
     * separately.
     *
     * @return The last built scene
     */
    public VRMLScene getScene();
	
    /**
     * Release any references to the scene that was last built by this scene handler.
	 * This should be called by the loader after a reference to the scene has been 
	 * retrieved using the getScene() method.
     */
    public void releaseScene();
}
