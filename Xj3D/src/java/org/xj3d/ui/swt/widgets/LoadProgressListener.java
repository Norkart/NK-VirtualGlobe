/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.widgets;

// External imports
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;

import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * An implementation of the URI progress listener for distributing
 * load progress messages to registered loggers and reporters.
 *
 * @author  Rex Melton
 * @version $Revision: 1.1 $
 */
public class LoadProgressListener implements ProgressListener, StatusReporter {
    
    /** Main file loading in progress message */
    private static final String MAIN_FILE_LOAD_MSG = 
        "Main file downloading";

    /** Main file loading complete message */
    private static final String MAIN_FILE_LOAD_COMPLETE_MSG = 
        "Main file complete";

    /** The reporter of current loading status */
    private StatusReporter statusRep;
    
    /** The logger of loading status messages */
    private ErrorReporter errorRep;
    
    /**
     * Create a new listener that puts information regarding the load process
     * into the status line and an error logger.
     *
     * @param statusRep - The reporter of current loading status. If null, the
     * status messages will be quietly dropped.
     * @param errorRep - The logger of loading status messages. If null, the logged
     * messages will be directed to the DefaultErrorReporter.
     */
    public LoadProgressListener( StatusReporter statusRep, ErrorReporter errorRep ) {

        this.statusRep = ( statusRep == null ) ? this : statusRep;
        
        this.errorRep = ( errorRep == null ) ? 
            DefaultErrorReporter.getDefaultReporter( ) : errorRep;
    }
    
    //---------------------------------------------------------------
    // Methods defined by ProgressListener
    //---------------------------------------------------------------
    
    /**
     * A connection to the resource has been established. At this point, no data
     * has yet been downloaded.
     *
     * @param evt The event that caused this method to be called.
     */
    public void connectionEstablished(ProgressEvent evt) {
        statusRep.setStatusText(evt.getMessage());
    }
    
    /**
     * The header information reading and handshaking is taking place. Reading
     * and intepreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     *
     * @param evt The event that caused this method to be called.
     */
    public void handshakeInProgress(ProgressEvent evt) {
        statusRep.setStatusText(evt.getMessage());
    }
    
    /**
     * The download has started.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadStarted(ProgressEvent evt) {
        statusRep.setStatusText(evt.getMessage());
    }
    
    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadUpdate( ProgressEvent evt ) {
        ResourceConnection conn = evt.getSource( );
        
        if ( conn != null ) {
            URI uri = conn.getURI( );
            
            StringBuffer buf = new StringBuffer( uri.toExternalForm( ) );
            buf.append( " (" );
            buf.append( evt.getValue( ) );
            buf.append( ")" );
            
            statusRep.setStatusText( buf.toString( ) );
        } else {
            // TODO: Right now we don't know the filename
            statusRep.setStatusText( MAIN_FILE_LOAD_MSG );
        }
    }
    
    /**
     * The download has ended.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadEnded( ProgressEvent evt ) {
        ResourceConnection conn = evt.getSource( );
        
        if ( conn != null ) {
            URI uri = conn.getURI( );
            String msg = uri.toExternalForm( ) + " complete.";
            statusRep.setStatusText( msg );
            errorRep.messageReport( msg );
        } else {
            statusRep.setStatusText( MAIN_FILE_LOAD_COMPLETE_MSG );
        }
    }
    
    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
        statusRep.setStatusText(evt.getMessage());
        errorRep.errorReport(evt.getMessage(), null);
    }
    
    
    //---------------------------------------------------------------
    // Methods defined by StatusReporter
    //---------------------------------------------------------------
    
    /** 
     * The default sink for status messages if no status reporter has
     * been established at instantiation.
     *
     * @param msg - The current status message
     */
    public void setStatusText( String msg ){
    }
}
