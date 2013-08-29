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
import org.web3d.util.ErrorReporter;


/**
 * The handler that that the content loading thread pool will call to load
 * a specific request.
 * <p>
 *
 * Request handlers provide the implementation of the content type that will
 * perform content-specific loading. For example, script loading will be
 * different to texture loading, thus require different implementations of this
 * interface.
 * <p>
 *
 * <b>Implementation Requirements</b>
 * <p>
 *
 * As this class is being dumped into the middle of an explicitly multithreaded
 * environment, then the implementation must be able to handle that. Either a
 * new instance can be created for each load requested, or a single shared
 * instance can be used but must be re-entrant.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface LoadRequestHandler {

    /**
     * Process this load request now.
     *
     * @param reporter The errorReporter to send all messages to
     * @param url The list of URLs to load
     * @param loadList The list of LoadDetails objects to sent the fulfilled
     *    requests to
     */
    public void processLoadRequest(ErrorReporter reporter,
                                   String[] url,
                                   Vector loadList);

    /**
     * Notification to abort loading the current resource. If there is one
     * loading, it will terminate the procedure immediately and start fetching
     * the next available URI. This will only work if we are currently
     * processing a file. If we are not processing a file then this is
     * ignored.
     */
    public void abortCurrentFile();

    /**
     * Notification to shut down the load process entirely for this thread.
     * It probably means we are about to close down the whole system. If we
     * are held in the queue, blocked waiting for input, the caller should
     * call {@link org.web3d.util.BlockingQueue#purge()} on the queue
     * <i>after</i> calling this method. That will force the block to exit
     * and this thread to end.
     */
    public void shutdown();

}
