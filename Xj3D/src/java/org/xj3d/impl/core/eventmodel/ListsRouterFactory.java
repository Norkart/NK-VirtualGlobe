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

package org.xj3d.impl.core.eventmodel;

// External imports
// None

// Local imports
import org.xj3d.core.eventmodel.Router;
import org.xj3d.core.eventmodel.RouterFactory;

/**
 * A factory interface for generating router instances.
 * <p>
 *
 * The factory is defined as an interface because we want to provide a flexible
 * system for defining who is to do the routing. While this package provides
 * a number of pre-packaged routers, a renderer might wish to provide its
 * own optimised system.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ListsRouterFactory implements RouterFactory {

    /**
     * Create a new Router instance for use by the caller.
     *
     * @return A new instance of the router object
     */
    public Router newRouter() {
        return new ListsRouter();
    }
}
