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
import java.net.URL;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.widgets.Display;


// Local imports
import org.web3d.util.ErrorReporter;

/**
 * A convenience class that loads Images for Xj3D's internal uses
 * and provides a caching mechanism.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class ImageLoader {
	
	/** Message when we fail to find an icon */
	private static final String IMAGE_FAIL_MSG =
		"Unable to locate the required image file ";
	
	/** The default size of the map = roughly the number of default nodes */
	private static final int DEFAULT_SIZE = 70;
	
	/**
	 * A hashmap of the loaded image instances. The original implementation
	 * used a WeakHashMap. Since we are responsible for explicitly disposing
	 * of graphical resources, have changed this to HashMap to ensure that
	 * we retain references to dispose.
	 */
	private static HashMap loadedImages;
	
	/**
	 * Static initialiser to get all the bits set up as needed.
	 */
	static {
		loadedImages = new HashMap(DEFAULT_SIZE);
	}
	
	/**
	 * Initialize the cache of loaded images from the argument map.
	 *
	 * @param resourceMap - A map containing images keyed by their path.
	 */
	public static void initializeCache( Map resourceMap ) {
		if ( resourceMap != null ) {
			for ( Iterator i = resourceMap.keySet( ).iterator( ); i.hasNext( ); ) 
			{
				String key = (String)i.next( );
				Object obj = resourceMap.get( key );
				if ( obj instanceof Image ) {
					loadedImages.put( key, obj );
				}
			}
		}
	}
	
	/**
	 * Load an image for the named image file. Looks in the classpath for the
	 * image so the path provided must be fully qualified relative to the
	 * classpath.
	 *
	 * @param name The path name of the image to load the icon for. If not found,
	 * no image is loaded.
	 * @param reporter An error reporter to send error messages to
	 * @return An image for the named path.
	 */
	public static Image loadImage( Display display, String name, 
		ErrorReporter reporter ) {
		
		if( name == null ) {
			return( null );
		}
		
		// Check the map for an instance first
		Image ret_val = (Image)loadedImages.get( name );
		if ( ret_val == null ) {
			URL url = ClassLoader.getSystemResource( name );
			if( url != null ) {
				try {
					ret_val = new Image( display, url.openStream( ) );
				} catch ( Exception e ) {
					// could probably provide a more detailed message
					// message for the error reporter here.
				}
			}
			
			// Fallback for WebStart
			if ( ret_val == null ) {
				url = ImageLoader.class.getClassLoader( ).getResource( name );
				
				if ( url != null ) {
					try {
						ret_val = new Image( display, url.openStream( ) );
					} catch ( Exception e ) {
						// could probably provide a more detailed message
						// message for the error reporter here.
					}
				}
			}
			
			if ( ret_val == null ) {
				reporter.warningReport( IMAGE_FAIL_MSG + name, null );
			} else {
				loadedImages.put( name, ret_val );
			}
		}
		return( ret_val );
	}
	
	/** Explicitly dispose of all images in the cache */
	public static void dispose( ) {
		for ( Iterator i = loadedImages.values( ).iterator( ); i.hasNext( ); ) {
			final Image img = (Image)i.next( );
			img.dispose( );
		}
		loadedImages.clear( );
	}
}
