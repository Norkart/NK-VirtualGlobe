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
import java.util.Vector;

// Local imports
// None


/**
 * A data holder for items that are inserted into a load queue.
 * <p>
 *
 * Because we want to use this object to check in the various lists and maps
 * for the URL being existing, the equality and hashing values are based on
 * the value of the URL only. Anything else is ignored.
 * <p>
 *
 * The data holder contains a reference to the cache to use. This is because
 * we might have different caching regimes set up by using different load
 * managers in the same VM instance. This allows each user to specify
 * themselves what sort of caching they want done without restricting them
 * to a first come, first set cache mechanism.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class LoadRequest {

    /**
     * The URL list that is to be loaded. List is in order as per the normal
     * VRML specification definition.
     */
    public String[] url;

    /** List of LoadDetails to be loaded for this URL. */
    public Vector loadList;

    /**
     * The type that describes the sort of node that this request comes from.
     * Used for priority sorting the request queue. Should be one of the
     * {@link LoadConstants} types.
     */
    public String type;

    /**
     * The class that the thread is to run when it grabs this request object
     * from the load queue.
     */
    public LoadRequestHandler handler;

    /**
     * Create a new instance of this class.
     */
    public LoadRequest() {
        loadList = new Vector();
    }

    /**
     * Check to see if this is the same as another instance.
     *
     * @param o The object to be compared against
     * @return true if these represent the same object
     */
    public boolean equals(Object o) {
        if(!(o instanceof LoadRequest))
            return false;

        LoadRequest lr = (LoadRequest)o;

        if(lr.url == url)
            return true;

        if((lr.url.length != url.length) ||
           !lr.type.equals(type))
            return false;

        for(int i = 0; i < url.length; i++) {
            if(!url.equals(lr.url))
                return false;
        }

        return true;
    }

    /**
     * Return the hashcode of this object - which is just the hashcode of the
     * underlying URL. If the URL is null, return 0.
     *
     * @return The hash value from this
     */
    public int hashCode() {
        return url == null ? 0 : url.hashCode();
    }
}
