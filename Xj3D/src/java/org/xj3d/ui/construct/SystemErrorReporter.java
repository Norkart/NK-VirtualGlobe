/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.construct;

// External imports
import java.util.Date;

// Local imports
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.ErrorReporter;

/**
 * An implementation of the ErrorReporter interface that writes all reports
 * to System.out and can be configured to limit the report types that will
 * be produced.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SystemErrorReporter implements ErrorReporter {
	
    /** Messaging strings */
    private static final String MESSAGE = "Message: ";
    private static final String WARNING = "Warning: ";
    private static final String ERROR = "Error: ";
    private static final String FATAL_ERROR = "Fatal Error: ";
    
	/** Should messages be logged */
	private boolean logMessage = true;
	
	/** Should warnings be logged */
	private boolean logWarning = true;
	
	/** Should errors be logged */
	private boolean logError = true;
	
	/** Should fatal errors be logged */
	private boolean logFatalError = true;
	
	/**
	 * Creates a new, default instance of the reporter
	 */
	public SystemErrorReporter() {
		this(true, true, true, true);
	}
	
	/**
	 * Constructor
	 *
	 * @param logMessage Set whether messages should be logged
	 * @param logWarning Set whether warnings should be logged
	 * @param logError Set whether errors should be logged
	 * @param logFatalError Set whether fatal errors should be logged
	 */
	public SystemErrorReporter(  
		boolean logMessage,
		boolean logWarning,
		boolean logError,
		boolean logFatalError) {
		
		setReportingLevels(logMessage, logWarning, logError, logFatalError);
		
		messageReport(new Date().toString());
		messageReport("Xj3D Version: " + Xj3DConstants.VERSION + "\n");
	}
	
	/**
	 * Notification of an partial message from the system. When being written
	 * out to a display device, a partial message does not have a line
	 * termination character appended to it, allowing for further text to
	 * appended on that same line.
	 *
	 * @param msg The text of the message to be displayed
	 */
	public void partialReport(String msg) {
		System.out.print(msg);
	}
	
	/**
	 * Notification of an informational message from the system.
	 *
	 * @param msg The text of the message to be displayed
	 */
	public void messageReport(String msg) {
		if (logMessage) {
			System.out.print(MESSAGE);
			System.out.println(msg);
		}
	}
	
	/**
	 * Notification of a warning in the way the system is currently operating.
	 * This is a non-fatal, non-serious error.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e The exception that caused this warning. May be null
	 */
	public void warningReport(String msg, Exception e) {
		if (logWarning) {
			System.out.print(WARNING);
			System.out.println(msg);
			
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Notification of a recoverable error.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e The exception that caused this warning. May be null
	 */
	public void errorReport(String msg, Exception e) {
		if (logError) {
			System.out.print(ERROR);
			System.out.println(msg);
			
			if(e != null) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Notification of a non-recoverable error that halts the entire system.
	 * After you receive this report the runtime system will no longer
	 * function.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e The exception that caused this warning. May be null
	 */
	public void fatalErrorReport(String msg, Exception e) {
		if (logFatalError) {
			System.out.print(FATAL_ERROR);
			System.out.println(msg);
			
			if(e != null) {
				e.printStackTrace();
			}
		}
	}
	
	//---------------------------------------------------------------
	// Local Methods
	//---------------------------------------------------------------
	
	/** 
	 * Configure the reporting levels
	 *
	 * @param logMessage Set whether messages should be logged
	 * @param logWarning Set whether warnings should be logged
	 * @param logError Set whether errors should be logged
	 * @param logFatalError Set whether fatal errors should be logged
	 */
	public void setReportingLevels( 
		boolean logMessage,
		boolean logWarning,
		boolean logError,
		boolean logFatalError ) {
		
		this.logMessage = logMessage;
		this.logWarning = logWarning;
		this.logError = logError;
		this.logFatalError = logFatalError;
	}
}
