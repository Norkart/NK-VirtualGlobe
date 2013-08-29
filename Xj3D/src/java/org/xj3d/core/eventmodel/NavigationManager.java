/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
import java.util.List;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;

/**
 * An abstract representation of a class that would be responsible for
 * performing Viewpoint management.
 * <p>
 *
 * This interface represents a further abstracted view of viewpoint management
 * handling beyond the {@link org.web3d.browser.BrowserCore}. This gives you
 * all the handling that is normally seen at a user interface level. You should
 * use one or the other, but not both as implementations of this class will
 * also interact with BrowserCore.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface NavigationManager {

    /**
     * Set the current bound navigation info to an arbitrary instance. This
     * only works in the layer that the navigation info is a part of. If the
     * layer is not the active layer, you will probably not see any response
     * from this manager.
     *
     * @param nav The new current navigation info.
     */
    public void setNavigationInfo(VRMLNavigationInfoNodeType nav);

    /**
     * Get the current active navigation info node. If there isn't one
     * (eg there is no world loaded at all). Active is defined for the
     * current active navigation layer.
     *
     * @return The current active navigation info node or null
     */
    public VRMLNavigationInfoNodeType getNavigationInfo();

    /**
     * Get the list of active navigation modes for the current active
     * navigation layer. This is a list of all the available options, not
     * the single type that is currently being used right now. To find
     * out which one of these modes is active, use
     * {@link #getActiveNavgiationIndex()}. This is a read-only list.
     *
     * @return The list of all the available types
     */
    public List<String> getActiveNavigationTypes();

    /**
     * Set the active navigation index of the current list. If the index is -1
     * then all navigation is disabled. An index of out bounds for the current
     * list throws an exception.
     *
     * @param idx The index to set as the active type
     * @throws IllegalArgumentException The index is greater than the
     *    available list size
     */
    public void setActiveNavigationIndex(int idx)
        throws IllegalArgumentException;

    /**
     * Fetch the index into the navigation type array of the actual type
     * of navigation being used by the system. If no type is active or the
     * type list includes "NONE" then this will return -1.
     *
     * @return The index of the active type
     */
    public int getActiveNavgiationIndex();

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear();

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Gets the viewpoints for the currently active layer.
     *
     * @return A list of the viewpoint nodes
     */
    public List<VRMLNavigationInfoNodeType> getActiveNavInfos();

    /**
     * Add a listener for viewpoint status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addNavigationListener(NavigationStatusListener l);

    /**
     * Remove a listener for viewpoint status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeNavigationListener(NavigationStatusListener l);
}

