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
import java.util.Map;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface WorldLoaderManager {

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
     * Register a progress listener with the engine so progress can be tracked. Only
     * loadURL calls will issue progress, not createURL.
     *
     * @param listener The instance to use or null
     */
    public void setProgressListener(ProgressListener listener);

    /**
     * Queue a request for a loadURL call.
     *
     * @param urls List of urls to load
     * @param params The parameters to accompany the URLs
     */
    public void queueLoadURL(String[] urls, Map params);

    /**
     * Queue a request for a createX3D/VRMLFromURL call.
     *
     * @param urls List of urls to load
     * @param node The node to send the values to
     * @param field index of the field to write to
     * @param space The space the script is in for route adding
     */
    public void queueCreateURL(String[] urls,
                               VRMLNodeType node,
                               int field,
                               VRMLExecutionSpace space);
    /**
     * Fetch a world loader instance from the global pool to work on
     * loading of a world.
     *
     * @return A loader instance to use
     */
    public WorldLoader fetchLoader();

    /**
     * Release a currently used world loader back into the cache for others to
     * make use of.
     *
     * @param loader The instance to return
     */
    public void releaseLoader(WorldLoader loader);

    /**
     * Register the scene builder factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    public void registerBuilderFactory(int renderer,
                                       SceneBuilderFactory factory);

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @return The instance of the factory or null
     */
    public SceneBuilderFactory getBuilderFactory(int renderer);

    /**
     * Register the parser factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    public void registerParserFactory(int renderer, VRMLParserFactory factory);

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @return The instance of the factory or null
     */
    public VRMLParserFactory getParserFactory(int renderer);
    
    /**
     * Return the number of WorldLoaders that are currently allocated from
     * the pool and active loading.
     *
     * @return The number of WorldLoaders currently active.
     */
     public int getNumberLoadersActive( );
}
