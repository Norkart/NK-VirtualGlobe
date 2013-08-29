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

package org.xj3d.impl.core.loading;

// External imports
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Local imports
import org.web3d.vrml.nodes.*;
import org.xj3d.core.loading.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.Queue;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.util.NodeArray;


/**
 * Common base implementation of a load manager.
 * <p>
 *
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.web3d.vrml.nodes.loader.sort_order</code> A space separated list
 *    containing one or more of the words <code>scripts</code>,
 *    <code>inlines</code>, <code>textures</code>, <code>externprotos</code>,
 *    <code>audio</code> and <code>others</code>. The declaration order is the
 *    sort order for values to be fetched within a given scene. Any values not
 *    declared are placed at the end of the list in any arbitrary order.
 * </li>
 * <li><code>org.web3d.vrml.nodes.loader.cache.mem.size</code> The amount of
 *     memory in Kilobytes (integer value) to allocate to in-memory file
 *     caching. If the value is zero or less, no caching is performed.
 * </li>
 * </ul>
 *
 * <B>Note</B> Sort order is not implemented yet.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public abstract class AbstractLoadManager
    implements ContentLoadManager, LoadConstants, VRMLUrlListener {

    /** The thread pool holding all the goodies */
    private LoaderThreadPool loaderPool;

    /** The shared queue that all threads share */
    private ContentLoadQueue pending;

    /** The map of objects working in progress */
    private Map inProgress;

    /**
     * Create a new load manager initialised with the content loading threads
     * ready to work.
     */
    protected AbstractLoadManager() {
        loaderPool = LoaderThreadPool.getLoaderThreadPool();
        pending = loaderPool.getWaitingList();
        inProgress = loaderPool.getProgressMap();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        loaderPool.setErrorReporter(reporter);
    }

    /**
     * Queue for loading a single externproto implementation. Used as a
     * way of getting externprotos in to load from the scripting APIs and
     * their createProto or createVRMLFromString methods that have queued up
     * content to load but have no other way of pushing it into the system.
     *
     * @param proto The proto declaration that needs to be queued up
     */
    public synchronized void queueExternProtoLoad(VRMLExternProtoDeclare proto) {
        if(proto == null)
            return;

        String[] urls = proto.getUrl();

        if(urls == null || urls.length == 0)
            return;

        ayeShesDeadCapn();

        // Check to see if this is a full scene. If yes, then work on the
        // externprotos first so that our content may run as requested.
        FileCache cache = getCache();

        ContentLoadDetails details = new ContentLoadDetails();
        details.fieldIndex = -1;
        details.node = proto;

        LoadRequest lrq = (LoadRequest)inProgress.get(urls);

        if(lrq != null) {
            if(!lrq.loadList.contains(details))
                lrq.loadList.add(details);
        } else {
            ContentLoadHandler handler = new ContentLoadHandler(cache);
            pending.add(LoadConstants.SORT_PROTO,
                        urls,
                        handler,
                        details);
        }
    }

    /**
     * Start loading the contents of the given scene. If there are other scenes
     * already loading, this is placed in the queue behind them. If the scene
     * does not contain any external nodes or is null then it is not queued for
     * loading.
     *
     * @param scene The new scene to queue up
     */
    public void queueSceneLoad(BasicScene scene) {
        if(scene == null)
            return;

        ayeShesDeadCapn();

        // Check to see if this is a full scene. If yes, then work on the
        // externprotos first so that our content may run as requested.
        ContentLoadDetails details;
        FileCache cache = getCache();

        ArrayList proto_list = scene.getNodeTemplates();
        int size = proto_list.size();

        for(int i = 0; i < size; i++) {
            Object node = proto_list.get(i);
            if(!(node instanceof VRMLExternalNodeType))
                continue;

            String[] urls = ((VRMLSingleExternalNodeType)node).getUrl();

            if(urls == null || urls.length == 0)
                continue;

            details = new ContentLoadDetails();
            details.fieldIndex = -1;
            details.node = (VRMLExternalNodeType)node;

            LoadRequest lrq = (LoadRequest)inProgress.get(urls);

            if(lrq != null) {
                if(!lrq.loadList.contains(details))
                    lrq.loadList.add(details);
            } else {
                ContentLoadHandler handler = new ContentLoadHandler(cache);
                pending.add(LoadConstants.SORT_PROTO,
                            urls,
                            handler,
                            details);
            }
        }


        ArrayList node_list =
            scene.getBySecondaryType(TypeConstants.SingleExternalNodeType);

        // When sorting is implemented, it will go here
        // Collections.sort(node_list, ExternalSortComparator());
        int i;
        Object obj;
        size = node_list.size();

        for(i = 0; i < size; i++) {
            obj = node_list.get(i);

            // Scripts we ignore because they get punted to the script loader
            if(obj instanceof VRMLScriptNodeType)
                continue;

            String[] urls = ((VRMLSingleExternalNodeType)obj).getUrl();

            if(urls == null || urls.length == 0)
                continue;

            details = new ContentLoadDetails();
            details.fieldIndex = -1;
            details.node = (VRMLExternalNodeType)obj;

            LoadRequest lrq = (LoadRequest)inProgress.get(urls);

            if(lrq != null) {
                if(!lrq.loadList.contains(details))
                    lrq.loadList.add(details);
            } else {
                ContentLoadHandler handler = new ContentLoadHandler(cache);
                pending.add(findLoadConstant((VRMLNodeType)obj),
                            urls,
                            handler,
                            details);
            }

            // Express interest in Url changes
            ((VRMLExternalNodeType)obj).addUrlListener(this);
        }

        // Now the multi-externals, like Background
        node_list =
            scene.getBySecondaryType(TypeConstants.MultiExternalNodeType);

        size = node_list.size();

        for(i = 0; i < size; i++) {

            VRMLMultiExternalNodeType ext_node =
                (VRMLMultiExternalNodeType)node_list.get(i);

            int[] index_list = ext_node.getUrlFieldIndexes();

            for(int j = 0; j < index_list.length; j++) {
                String[] urls = ext_node.getUrl(index_list[j]);

                if(urls == null || urls.length == 0)
                    continue;

                details = new ContentLoadDetails();
                details.fieldIndex = index_list[j];
                details.node = ext_node;

                LoadRequest lrq = (LoadRequest)inProgress.get(urls);

                if(lrq != null) {
                    if(!lrq.loadList.contains(details))
                        lrq.loadList.add(details);
                } else {
                    ContentLoadHandler handler = new ContentLoadHandler(cache);
                    pending.add(findLoadConstant((VRMLNodeType)ext_node),
                                urls,
                                handler,
                                details);
                }
            }

            ext_node.addUrlListener(this);
        }
    }

    /**
     * Queue an arbitrary collection of nodes for loading.
     *
     * @param nodes Array of nodes to add
     */
    public void queueNodesLoad(NodeArray nodes) {
        int size = nodes.size();

        if(size == 0)
            return;

        VRMLNode node;
        ContentLoadDetails details;
        FileCache cache = getCache();

        ayeShesDeadCapn();

        for(int i = 0; i < size; i++) {
            node = nodes.get(i);

            // Scripts we ignore because they get punted to the script loader
            if(node instanceof VRMLScriptNodeType)
                continue;

            if(node instanceof VRMLSingleExternalNodeType) {
                VRMLSingleExternalNodeType ext_node =
                    (VRMLSingleExternalNodeType)node;

                String[] urls = ext_node.getUrl();

                if(urls == null || urls.length == 0)
                    continue;

                details = new ContentLoadDetails();
                details.fieldIndex = -1;
                details.node = ext_node;

                LoadRequest lrq = (LoadRequest)inProgress.get(urls);

                if(lrq != null) {
                    if(!lrq.loadList.contains(details))
                        lrq.loadList.add(details);
                } else {
                    ContentLoadHandler handler = new ContentLoadHandler(cache);
                    pending.add(findLoadConstant((VRMLNodeType)node),
                                urls,
                                handler,
                                details);
                }

                // Express interest in Url changes
                ext_node.addUrlListener(this);
            } else if(node instanceof VRMLMultiExternalNodeType) {
                VRMLMultiExternalNodeType ext_node =
                    (VRMLMultiExternalNodeType)node;

                int[] index_list = ext_node.getUrlFieldIndexes();

                for(int j = 0; j < index_list.length; j++) {
                    
                    String[] urls = ext_node.getUrl(index_list[j]);

                    if(urls == null || urls.length == 0) {
                        continue;
                    }
                    details = new ContentLoadDetails();
                    details.fieldIndex = index_list[j];
                    details.node = ext_node;
                
                    LoadRequest lrq = (LoadRequest)inProgress.get(urls);

                    if(lrq != null) {
                        if(!lrq.loadList.contains(details))
                            lrq.loadList.add(details);
                    } else {
                        ContentLoadHandler handler = new ContentLoadHandler(cache);
                        pending.add(findLoadConstant((VRMLNodeType)node),
                                    urls,
                                    handler,
                                    details);
                    }
                }

                ext_node.addUrlListener(this);
            }
        }
    }

    /**
     * Stop the named scene from loading. If there are any loads in progress
     * this action forces them to stop. If this is not a known scene then the
     * request is ignored.
     *
     * @param scene The scene to stop loading
     */
    public void stopSceneLoad(BasicScene scene) {

        if(scene == null)
            return;

        ArrayList node_list =
            scene.getBySecondaryType(TypeConstants.SingleExternalNodeType);
        int size = node_list.size();

        if(size == 0)
            return;

        // Remove items from the pending list and in progress map
        // We also need to tell the content loader to stop handling this item.
        for(int i = 0; i < size; i++) {
            Object node = node_list.get(i);

            VRMLSingleExternalNodeType ext_node =
                (VRMLSingleExternalNodeType)node;

            ContentLoadDetails details = new ContentLoadDetails();
            details.fieldIndex = -1;
            details.node = ext_node;

            String[] urls = ext_node.getUrl();

            if(inProgress.containsKey(urls)) {
                LoadRequest lrq = (LoadRequest)inProgress.get(urls);
                lrq.loadList.remove(details);
            } else
                pending.remove(urls, details);
        }

        node_list =
            scene.getBySecondaryType(TypeConstants.MultiExternalNodeType);
        size = node_list.size();

        if(size == 0)
            return;

        // Remove items from the pending list and in progress map
        // We also need to tell the content loader to stop handling this item.
        for(int i = 0; i < size; i++) {
            Object node = node_list.get(i);

            VRMLMultiExternalNodeType ext_node =
                (VRMLMultiExternalNodeType)node;

            int[] index_list = ext_node.getUrlFieldIndexes();

            for(int j = 0; j < index_list.length; j++) {
                ContentLoadDetails details = new ContentLoadDetails();
                details.fieldIndex = index_list[j];
                details.node = ext_node;

                String[] urls = ext_node.getUrl(index_list[j]);

                if(inProgress.containsKey(urls)) {
                    LoadRequest lrq = (LoadRequest)inProgress.get(urls);
                    lrq.loadList.remove(details);
                } else
                    pending.remove(urls, details);
            }
        }
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        pending.clear();

        loaderPool.clear();
    }

    //--------------------------------------------------------------
    // Methods defined by VRMLUrlListener
    //--------------------------------------------------------------

    /**
     * Notification that the Url content for this node has changed
     *
     * @param index The index of the field that has changed
     */
    public void urlChanged(VRMLNodeType node, int index) {

        FileCache cache = getCache();
        ContentLoadDetails details = new ContentLoadDetails();
        String[] urls = null;

        if(node instanceof VRMLSingleExternalNodeType) {

            VRMLSingleExternalNodeType ext_node =
                (VRMLSingleExternalNodeType)node;

            urls = ext_node.getUrl();

            if(urls == null || urls.length == 0)
                return;

            ext_node.setLoadState(VRMLExternalNodeType.NOT_LOADED);

            details.fieldIndex = -1;
            details.node = ext_node;

            ContentLoadHandler handler = new ContentLoadHandler(cache);
            pending.add(findLoadConstant(node),
                        urls,
                        handler,
                        details);
        } else {
            VRMLMultiExternalNodeType ext_node =
                (VRMLMultiExternalNodeType)node;

            urls = ext_node.getUrl(index);

            if(urls == null || urls.length == 0)
                return;

            ext_node.setLoadState(index, VRMLExternalNodeType.NOT_LOADED);

            details.fieldIndex = index;
            details.node = ext_node;

            ContentLoadHandler handler = new ContentLoadHandler(cache);
            pending.add(findLoadConstant(node),
                        urls,
                        handler,
                        details);
        }
    }

    /**
     * Get the number of items that need to be loaded.
     *
     * @return The number of items queued.
     */
    public int getNumberInProgress() {
        int ret_val = pending.size() + inProgress.size();

        return ret_val;
    }

    /**
     * Notification that the manager needs to shut down all the currently
     * running threads. Normally called when the application is exiting, so it
     * is not expected that the manager needs to act sanely after this method
     * has been called.
     */
    public void shutdown() {
        loaderPool.shutdown();
    }

    //--------------------------------------------------------------
    // Local Methods
    //--------------------------------------------------------------

    /**
     * Request to fetch the cache used by the derived type.
     *
     * @return The file cache instance
     */
    protected abstract FileCache getCache();

    /*
     * Restore lost scottish lasses who have strayed from their mates, or
     * restart dead threads.
     */
    private void ayeShesDeadCapn() {
        loaderPool.restartThreads();
    }

    /**
     * Return the given load type constant that represents the given external
     * node.
     *
     * @param node The node instance to test
     * @return One of the load constants
     */
    private String findLoadConstant(VRMLNodeType node) {

        switch(node.getPrimaryType()) {
            // Treat backgrounds as textures
            case TypeConstants.BackgroundNodeType:
            case TypeConstants.TextureNodeType:
                // May be a movietexture, so check the secondary type for
                // audio capabilities
                int[] other = node.getSecondaryType();

                if(other != null) {
                    for(int i = 0; i < other.length; i++) {
                        if(other[i] == TypeConstants.AudioClipNodeType)
                            return LoadConstants.SORT_MOVIE;
                    }
                }

                return LoadConstants.SORT_TEXTURE;

            case TypeConstants.ScriptNodeType:
                return LoadConstants.SORT_SCRIPT;

            case TypeConstants.AudioClipNodeType:
                return LoadConstants.SORT_AUDIO;

            case TypeConstants.ShaderProgramNodeType:
                return LoadConstants.SORT_SHADER;

            case TypeConstants.InlineNodeType:
                return LoadConstants.SORT_INLINE;

            case TypeConstants.ProtoInstance:
                return LoadConstants.SORT_PROTO;
        }

        return LoadConstants.SORT_OTHER;
    }
}
