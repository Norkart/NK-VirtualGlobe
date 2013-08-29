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

package org.web3d.net.resolve;

// Standard library imports
import org.ietf.uri.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

// Application specific imports
import org.web3d.util.HashSet;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * A URN resolver to allow the integration of URNs that use the
 * <code>web3d</code> Namespace ID.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class Web3DURNResolver implements URNResolverService {

    /** The namespace we can resolve */
    private static final String WEB3D_NID = "web3d";

    /** The resource factory we use to fetch resource connections for */
    private URIResourceFactory resourceFactory;

    /** The mapping of prefix names to their directories */
    private HashMap spaceMap;

    /** Set containing the list of valid IDs */
    private static HashSet validNIDs;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Static constructor to set up the valid NID information
     */
    static {
        validNIDs = new HashSet();
        validNIDs.add(WEB3D_NID);
    }

    /**
     * Create an instance of the resolver. This will not be able to resolve
     * anything until the appropriate directories are set.
     */
    public Web3DURNResolver() {
        spaceMap = new HashMap();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods required by URNResolverService
    //----------------------------------------------------------

    /**
     * Check to see what NIDs can be resolved by this service implementation.
     *
     * @param nid The NID to check
     * @return true if we can resolve it
     */
    public boolean canResolve(String nid) {
        return validNIDs.contains(nid);
    }

    /**
     * Decode this URN using the given service type in the singular.
     *
     * @param urn The urn that was requested to be resolved
     * @param service The service type of the resolution
     * @throws UnsupportedServiceException We don't handle this service
     */
    public Object decode(URN urn, int service)
        throws UnsupportedServiceException {

        if(!((service == I2L) || (service == I2R)))
            throw new UnsupportedServiceException("Don't support this");

        String nss = urn.getNSS();

        Object ret_val = null;

        // Defined as urn:web3d:sub-space:/blah....
        int colon_index = nss.indexOf(':');
        String prefix = nss.substring(0, colon_index);

        // fetch this from the map
        String base_path = (String)spaceMap.get(prefix);

        if(base_path != null) {
            String path = nss.substring(colon_index + 1);
            String full_path = base_path + path;
            ret_val = setupResource(full_path, service);
        }

        return ret_val;
    }

    /**
     * Decode this URN using the given service type that gives a list of
     * return values.
     *
     * @param urn The urn that was requested to be resolved
     * @param service The service type of the resolution
     * @throws UnsupportedServiceException We don't handle this service
     */
    public Object[] decodeList(URN urn, int service)
        throws UnsupportedServiceException {

        if(!((service == I2Ls) || (service == I2Rs)))
            throw new UnsupportedServiceException("Don't support this");

        // we know that these files only ever resolve to a single place on disk
        // so just call the single version.
        Object single_val = decode(urn, (service == I2Ls) ? I2L : I2R);
        Object[] ret_val = null;

        if(single_val != null) {
            if(service == I2Rs) {
                ResourceConnection[] conns = new ResourceConnection[1];
                conns[0] = (ResourceConnection)single_val;
                ret_val = conns;
            } else {
                URL[] urls = new URL[1];
                urls[0] = (URL)single_val;
                ret_val = urls;
            }
        }

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods required by URIResolverService
    //----------------------------------------------------------

    /**
     * Check to see if the service type is one that we can resolve given the
     * namespace ID.
     *
     * @param type The service type to check if it works
     * @return true if the service type is supported by this implementation
     */
    public boolean checkService(int type) {
        boolean ret_val;

        switch(type) {
            case I2L:
            case I2Ls:
            case I2R:
            case I2Rs:
                ret_val = true;

            default:
                ret_val = false;
        }

        return ret_val;
    }

    /**
     * Initialise any internal information. Ignored in this implementation
     */
    public void init() {
    }

    /**
     * Set the resource factory to be used to fetch streams for. Should never
     * be called by end-user code. Is internal to the URI library.
     *
     * @param fac The factory to be used
     */
    public void setResourceFactory(URIResourceFactory fac) {
        resourceFactory = fac;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the directory used to locate a particular subspace of the
     * Web3D URN namespace ID. A check is made to make sure the passed string
     * represents a valid directory. If it does not, an exception is thrown.
     * If null is passed as the directory, it removes the current mapping.
     *
     * @param prefix The subspace prefix to use
     * @param directory The directory that GeoVRML is installed in
     * @throws IllegalArgumentException The directory is not valid
     */
    public void registerPrefixLocation(String prefix, String directory)
        throws IllegalArgumentException {

        if(prefix == null)
            throw new IllegalArgumentException("Subspace prefix is null");

        if(directory == null)
             spaceMap.remove(prefix);
        else {
            checkDir(directory);

            // set the full dir as the one with slashes the right way. This is to
            // deal with windoze machines.
            String path = directory.replace('\\', '/');

            if(!path.endsWith("/"))
                path += '/';

            spaceMap.put(prefix, path);
        }
    }

    /**
     * Check to see if the directory string represents a real directory. If
     * it does, the method returns normally. If it does not, and exception is
     * thrown.
     *
     * @param directory The directory to check on
     * @throws IllegalArgumentException The directory is not valid
     */
    private void checkDir(String dir) throws IllegalArgumentException {
        File check_this = new File(dir);

        if(!check_this.exists())
            throw new IllegalArgumentException("Directory does not exist");

        if(!check_this.isDirectory())
            throw new IllegalArgumentException("Gave a file, need directory");
    }

    /**
     * Convenience method to set up a URL or Resource connection
     *
     * @param path The full path on the local system
     * @param service The service to to create the resource for
     * @return The object for it
     */
    private Object setupResource(String path, int service) {
        Object ret_val = null;

        if(service == I2L) {
            try {
                ret_val = new URL(URIConstants.FILE_SCHEME,
                                  null,
                                  -1,
                                  path);
            } catch(MalformedURLException mue) {
                // should never happen!
            }
        } else {
            try {
                ret_val =
                    resourceFactory.requestResource(URIConstants.FILE_SCHEME,
                                                    null,
                                                    -1,
                                                    path);
            } catch(IOException ioe) {
                errorReporter.warningReport(
                    "I/O Error attempting to fetch the resource from the URN" +
                    " resolver.", ioe);
            }
        }

        return ret_val;
    }
}
