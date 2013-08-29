/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.util;

// Standard imports
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import org.ietf.uri.URIUtils;

// Application specific imports
import org.web3d.util.ObjectArray;

/**
 * A checker of URLs to update them and include a root URL if needed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class URLChecker {
	/**
	 * Check the given array of URLs for relative references and return a new 
	 * array with the resulting urls. If found, add the base URL to it to make 
	 * them all fully qualified. This will also set the urlRelativeCheck flag to
	 * true.
	 *
	 * @param worldURL the root URL to apply to the urls
	 * @param urls The array of URLs to check
	 * @param anchor true if we should apply extra checks for anchor URLs.
	 * @return An array of updated URLs.
	 */
	public static String[] checkURLs(String worldURL, String[] urls, boolean anchor) {
		
		int len = urls.length;
		String[] ret_val = new String[len];
		System.arraycopy( urls, 0, ret_val, 0, len );
		checkURLsInPlace(worldURL, ret_val, anchor);
		return( ret_val );
	}
	
	/**
	 * Check the given array of URLs for relative references - in place. If found,
	 * add the base URL to it to make them all fully qualified. This will also set
	 * the urlRelativeCheck flag to true.
	 *
	 * @param worldURL the root URL to apply to the urls
	 * @param urls The array of URLs to check and update if necessary.
	 * @param anchor true if we should apply extra checks for anchor URLs.
	 */
	public static void checkURLsInPlace(String worldURL, String[] urls, boolean anchor) {
		
		if (worldURL != null && worldURL.startsWith("file:")) {
			worldURL = worldURL.replace('\\', '/');
			if (worldURL.charAt(7) != '/') {
				// Change file:/ to file:///
				worldURL = "file:///" + worldURL.substring(6);
			}
			
			if (!worldURL.endsWith("/")) {
				worldURL = worldURL + "/";
			}
		}
		
		if(anchor) {
			String tmp;
			
			for(int i = 0; i < urls.length; i++) {
				tmp = urls[i];
				
				if((tmp.charAt(0) != '#') && (tmp.indexOf(':') == -1)) {
					urls[i] = buildURL(worldURL, tmp);
				}
			}
		} else {
			for(int i = 0; i < urls.length; i++) {
				if(urls[i].indexOf(':') == -1) {
					urls[i] = buildURL(worldURL, urls[i]);
				}
			}
		}
	}
	
	/**
	 * Join the world URL and the relative URL together to trim the relative
	 * path if it contains "../" and "./" entries.
	 *
	 * @param worldURL the root URL to apply to the urls
	 * @param url The relative URL to check
	 * @return The completed URL string
	 */
	private static String buildURL(String worldURL, String url) {
		
		if (url == null || url.length() < 1) {
			return worldURL;
		}
		
		char first_char = url.charAt(0);
		String ret_val = null;
		
		try {
			if(first_char == '.') {
				String scheme = URIUtils.getScheme(worldURL);
				String[] host_bits = URIUtils.getHostAndPortFromUrl(worldURL);
				
				StringBuffer buf = new StringBuffer(scheme);
				buf.append("://");
				
				if(host_bits != null) {
					if(host_bits[0] != null)
						buf.append(host_bits[0]);
					
					if(host_bits[1] != null) {
						buf.append(':');
						buf.append(host_bits[1]);
					}
				}
				
				String world_path = URIUtils.getPathFromUrlString(worldURL);
				StringTokenizer strtok = new StringTokenizer(world_path, "/");
				ObjectArray path_stack = new ObjectArray();
				
				while(strtok.hasMoreTokens()) {
					path_stack.add(strtok.nextToken());
				}
				
				// Tokenize the main string now.
				String[] path_bits = URIUtils.stripFile(url);
				if(path_bits[0] != null) {
					strtok = new StringTokenizer(path_bits[0], "/");
					while(strtok.hasMoreTokens()) {
						String str = strtok.nextToken();
						
						switch(str.length()) {
						case 2:
							if(str.charAt(0) != '.')
								path_stack.add(str);
							else {
								path_stack.remove(path_stack.size() - 1);
							}
							break;
							
						case 3:
							if((str.charAt(0) != '.') && (str.charAt(1) == '.')) {
								if(path_stack.size() == 0) {
									System.out.println("Invalid relative path " + url);
									return null;
								}
								path_stack.remove(path_stack.size() - 1);
							} else
								path_stack.add(str);
							
							break;
						default:
							path_stack.add(str);
						}
					}
				}
				
				// Now build all the bits back up again into a single path
				int num_items = path_stack.size();
				for(int i = 0; i < num_items; i++) {
					String dir = (String)path_stack.get(i);
					buf.append('/');
					buf.append(dir);
				}
				if(path_bits[1] != null) {
					buf.append('?');
					buf.append(path_bits[1]);
				}
				
				if(path_bits[2] != null) {
					buf.append('#');
					buf.append(path_bits[2]);
				}
				
				ret_val = buf.toString();
				
			} else if(first_char == '/') {
				// The URL is placing itself at the root of the source, so grab
				// that from the worldURL. This currently does not check for the
				// user using relative paths within the url string like this:
				//   /root/dir/../other/dir/something.jpg
				
				String scheme = URIUtils.getScheme(worldURL);
				String[] host_bits = URIUtils.getHostAndPortFromUrl(worldURL);
				
				StringBuffer buf = new StringBuffer(scheme);
				buf.append("://");
				
				if(host_bits != null) {
					if(host_bits[0] != null)
						buf.append(host_bits[0]);
					
					if(host_bits[1] != null) {
						buf.append(':');
						buf.append(host_bits[1]);
					}
				}
				
				// There may be an issue here in that we ignore the authority part,
				// like name and password that http and ftp urls may include.
				buf.append(url);
				ret_val = buf.toString();
			} else {
				int pos = worldURL.indexOf("://");
				if (pos < 0) {
					ret_val = "file:///" + worldURL + url;
				} else {
					ret_val = worldURL + url;
				}
			}
		} catch(MalformedURLException mue) {
			System.out.println("Danger! Malformed URL in checker");
			mue.printStackTrace();
		}
		
		return ret_val;
	}
}
