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
 *****************************************************************************/

package xj3d.replica;

/**
 * Wrapper class for chosing the Replica operational mode. The
 * mode selection determines the application class to use.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Replica {

	/** The logging identifer of this class */
	private static final String LOG_NAME = "Replica";
	
	/** Thumbnail image mode identifier */
	public static final String THUMBNAIL = "thumbnail";
	
	/** Sequenced image recording mode identifier */
	public static final String SEQUENCE = "sequence";

	/** Help mode identifier */
	public static final String HELP = "help";

	/** Usage message with command line options */
	private static final String USAGE =
		//"0---------1---------2---------3---------4---------5---------6---------7---------8"+
		//"012345678901234567890123456789012345678901234567890123456789012345678901234567890"+
		"Usage: "+ LOG_NAME +" [options] \n" +
		"  -help                  Print out this message to the stdout. If a -mode argument \n" +
		"                         preceeds a -help argument, the -help argument is passed \n" +
		"                         on to the operational application class. \n" +
		"  -mode id               The identifier of the mode of operation. \n" +
		"                         ["+ THUMBNAIL +"|"+ SEQUENCE +"] \n";
	
	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	/**
	 * Entry point. For a full list of valid arguments,
	 * invoke with the -help argument. If a mode argument preceeds
	 * the a help request, the help request is passed on to the
	 * operational application class.
	 *
	 * @param arg The list of arguments
	 */
	public static void main( String[] arg ) {
		
		String mode = parseArgs( arg );
		
		if ( mode.equals( HELP ) ) {
			System.out.println( USAGE );
			
		} else if ( mode.equals( THUMBNAIL ) ) {
			ThumbnailImager imager = new ThumbnailImager( arg );
			
		} else if ( mode.equals( SEQUENCE ) ) {
			SceneRecorder recorder = new SceneRecorder( arg );
			
		} else if ( mode == null ) {
			System.out.println( LOG_NAME +": Mode of operation not specified" );
			
		} else {
			System.out.println( LOG_NAME +": Unknown mode of operation: "+ mode );
		}
	}

	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	/**
	 * Parse the command line arguments, extract and return the
	 * mode of operation identifier.
	 *
	 * @param arg The command line arguments
	 * @return The mode of operation identifier or null if one is not found.
	 */
	private static String parseArgs( String[] arg ) {
		
		String mode = null;
		
		//////////////////////////////////////////////////////////////////////
		// parse the arguments, sort out a help request
		
		for ( int i = 0; i < arg.length; i++ ) {
			String argument = arg[i];
			if ( argument.startsWith( "-" ) ) {
				try {
					if ( argument.equals( "-mode" ) ) {
						mode = arg[i+1];
						break;
					} else if ( argument.equals( "-help" ) ) {
						mode = HELP;
						break;
					}
				} catch ( Exception e ) {
					// this would be an IndexOutOfBounds - should arrange to log it
				}
			}
		}
		return( mode );
	}
}
