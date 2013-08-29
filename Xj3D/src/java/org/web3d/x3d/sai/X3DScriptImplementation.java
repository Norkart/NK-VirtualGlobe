/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// External imports
import java.util.Map;

// Local imports
// None

/**
 * Marker interface to say that the implementing class is allowed to be
 * executed as a script within the X3D scene graph.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface X3DScriptImplementation
{
    /**
     * Set the browser instance to be used by this script implementation.
     * This shall be called by the browser code to give the script a handle
     * to the browser's interface. It shall be only called once in the
     * lifetime of a script implementation before the initialize() method
     * is called.
     *
     * @param browser The browser reference to keep
     */
    public void setBrowser(Browser browser);

    /**
     * Set the listing of fields that have been declared in the file for
     * this node. The map contains field name string to field object instances.
     * If no fields are available, then this method shall still be called, but
     * with an empty map.
     *
     * @param externalView The external view of ourselves, so you can add routes to yourself
     *    using the standard API calls
     * @param fields The mapping of field names to instances
     */
    public void setFields(X3DScriptNode externalView, Map fields);

    /**
     * Notificatoin that the script has completed the setup and should go
     * about its own internal initialisation. This shall be called after the
     * {@link #setBrowser(Browser)} and {@link #setFields(X3DScriptNode, Map)}
     * methods have been called.
     */
    public void initialize();

    /**
     * Notification that this script instance is no longer in use by the
     * scene graph and should now release all resources.
     */
    public void shutdown();

    /**
     * Notification that all the events in the current cascade have finished
     * processing.
     */
    public void eventsProcessed();
}
