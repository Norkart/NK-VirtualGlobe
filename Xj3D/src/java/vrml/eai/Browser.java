/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL v2.1. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

import vrml.eai.event.BrowserListener;
import vrml.eai.field.InvalidEventInException;
import vrml.eai.field.InvalidEventOutException;

/**
 * Basic browser interface that represents the interface to the VRML browser
 * from any application. Individual VRML browser implementors are to extend this
 * interface and provide this functionality. The individual users will not see
 * anything but this interface.
 * <P>
 * A number of the methods in this applicationcan take strings representing URLs.
 * Relative URL strings contained in URL fields of nodes or these method
 * arguments are interpreted as follows:
 * <P>
 * Relative URLs are treated as per clause B.3.5 of the EAI Java Bindings
 * <P>
 *
 * @version 1.1 25 April 1998
 */
public interface Browser
{
  /**
   * Get the name of the browser. The name is an implementation specific
   * string representing the browser.
   *
   * @return The name of the browser or null if not supported
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public String getName()
    throws InvalidBrowserException;

  /**
   * Get the version of the browser. Returns an implementation specific
   * representation of the version number.
   *
   * @return The version of the browser or null if not supported
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public String getVersion()
    throws InvalidBrowserException;

  /**
   * Get the current velocity of the bound viewpoint in meters per second.
   * The velocity is defined in terms of the world values, not the local
   * coordinate system of the viewpoint.
   *
   * @return The velocity in m/s or 0.0 if not supported
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public float getCurrentSpeed()
    throws InvalidBrowserException;

  /**
   * Get the current frame rate of the browser in frames per second.
   *
   * @return The current frame rate or 0.0 if not supported
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public float getCurrentFrameRate()
    throws InvalidBrowserException;

  /**
   * Get the fully qualified URL of the currently loaded world. This returns
   * the entire URL including any possible arguments that might be associated
   * with a CGI call or similar mechanism. If the initial world is replaced
   * with <CODE>loadURL</CODE> then the string will reflect the new URL. If
   * <CODE>replaceWorld</CODE> is called then the URL still represents the
   * original world.
   *
   * @return A string of the URL or null if not supported.
   * @see #loadURL
   * @see #replaceWorld
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception URLUnavailableException The URL is not available because a
   *    world has not been loaded
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public String getWorldURL()
    throws InvalidBrowserException, URLUnavailableException;

  /**
   * Replace the current world with the given nodes. Replaces the entire
   * contents of the VRML world with the new nodes. Any node references that
   * belonged to the previous world are still valid but no longer form part of
   * the scene graph (unless it is these nodes passed to this method). The
   * URL of the world still represents the just unloaded world.
   * <P>
   * Calling this method causes a SHUTDOWN event followed by an INITIALIZED
   * event to be generated.
   *
   * @param nodes The list of nodes to use as the new root of the world
   * @exception IllegalArgumentException if the nodes are not valid VRML nodes
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void replaceWorld(Node[] nodes)
    throws IllegalArgumentException, InvalidBrowserException;

  /**
   * Load the URL as the new root of the scene. Replaces all the current
   * scene graph with the new world. A non-blocking call that will change the
   * contents at some time in the future.
   * <P>
   * Generates an immediate SHUTDOWN event and then when the new contents are
   * ready to be loaded, sends an INITIALIZED event.
   *
   * @param url The list of URLs in decreasing order of preference as defined
   *   in the VRML97 specification.
   * @param parameter The list of parameters to accompany the load call as
   *   defined in the Anchor node specification of VRML97
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception InvalidURLException All of the URLs passed to this method are
   *    bogus and cannot be translated to usable values
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void loadURL(String[] url, String[] parameter)
    throws InvalidBrowserException, InvalidURLException;

  /**
   * Set the description of the current world. If the world is operating as
   * part of a web browser then it shall attempt to set the title of the
   * window. If the browser is from a component then the result is dependent
   * on the implementation
   *
   * @param desc The description string to set.
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void setDescription(String desc)
    throws InvalidBrowserException;

  /**
   * Parse the given string and turn this into a list of VRML nodes. Method
   * is a blocking call that won't return until all of the top level nodes
   * defined in the string have been returned.
   * <P>
   * At the point that this method returns, external files such as textures,
   * sounds and inlines may not have been loaded.
   * <P>
   * The string may contain all legal VRML syntax. The VRML header line is not
   * required to be present in the string.
   *
   * @param vrmlString The string containing VRML string syntax
   * @return A list of the top level nodes in VRML representation as defined
   *    in the parameter
   * @exception InvalidVrmlException If the string does not contain legal
   *   VRML syntax or no node instantiations
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public Node[] createVrmlFromString(String vrmlString)
    throws InvalidBrowserException, InvalidVrmlException;

  /**
   * Create and load VRML from the given URL and place the returned values
   * as nodes into the given VRML node in the scene. The difference between
   * this and loadURL is that this method does not replace the entire scene
   * with the contents from the URL. Instead, it places the return values
   * as events in the nominated node and MFNode eventIn.
   *
   * @param url The list of URLs in decreasing order of preference as defined
   *   in the VRML97 specification.
   * @param node The destination node for the VRML code to be sent to.
   * @param eventIn The name of the MFNode eventIn to send the nodes to.
   * @exception InvalidNodeException The nominated destination node has been
   *   disposed of
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception InvalidURLException All of the URLs passed to this method are
   *    bogus and cannot be translated to usable values
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void createVrmlFromURL(String[] url, Node node, String eventIn)
    throws InvalidBrowserException, InvalidNodeException, InvalidURLException;

  /**
   * Get a DEF node by name. Nodes given DEF names in the root scene graph
   * are available to be retrieved by this method. DEFed nodes in Inlines,
   * createVrmlFromString and createVrmlFromURL are not available.
   *
   * @param name The name of the DEF node to retrieve
   * @return A reference to that node
   * @exception InvalidNodeException The named node does not exist or is not
   *    accessible.
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception URLUnavailableException The URL is not available because a
   *    world has not been loaded
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public Node getNode(String name)
      throws InvalidNodeException, InvalidBrowserException, URLUnavailableException;

  /**
   * Add a route between two nodes, from an eventOut to an eventIn. If the
   * ROUTE already exists, this method silently exits. It does not attempt
   * to add a second parallel ROUTE.
   *
   * @param fromNode The source node for the route
   * @param eventOut The eventOut source of the route
   * @param toNode The destination node of the route
   * @param eventIn The eventIn destination of the route
   * @exception InvalidEventOutException if the named eventOut does not exist
   * @exception InvalidEventInException if the named eventIn does not exist.
   * @exception InvalidNodeException The nominated destination or source node
   *   has been disposed of
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void addRoute(Node fromNode, String eventOut,
                       Node toNode,   String eventIn)
      throws InvalidBrowserException,
             InvalidEventOutException,
             InvalidEventInException,
             InvalidNodeException;

  /**
   * Delete a route between two nodes. If the route does not exist, the
   * method silently exits.
   *
   * @param fromNode The source node for the route
   * @param eventOut The eventOut source of the route
   * @param toNode The destination node of the route
   * @param eventIn The eventIn destination of the route
   * @exception InvalidEventOutException if the named eventOut does not exist
   * @exception InvalidEventInException if the named eventIn does not exist.
   * @exception InvalidNodeException The nominated destination or source node
   *   has been disposed of
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void deleteRoute(Node fromNode, String eventOut,
                          Node toNode,   String eventIn)
      throws InvalidBrowserException,
             InvalidEventOutException,
             InvalidEventInException,
             InvalidNodeException;

  /**
   * Lock the output from the external interface to the browser as the code
   * is about to begin a series of updates. No events will be passed to the
   * VRML world. They will be buffered pending release due to a subsequent
   * call to endUpdate.
   * <P>
   * This call is a nesting call which means subsequent calls to beginUpdate
   * are kept on a stack. No events will be released to the VRML browser
   * until as many endUpdates have been called as beginUpdate.
   *
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void beginUpdate()
      throws InvalidBrowserException;

  /**
   * Release the output of events from the external interface into the
   * VRML browser. All events posted to this point from the last time that
   * beginUpdate was called are released into the VRML browser for
   * processing at the next available oppourtunity.
   * <P>
   * This call is a nesting call which means subsequent calls to beginUpdate
   * are kept on a stack. No events will be released to the VRML browser
   * until as many endUpdates have been called as beginUpdate.
   * <P>
   * If no beginUpdate has been called before calling this method, it has
   * no effect.
   *
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void endUpdate()
      throws InvalidBrowserException;

  /**
   * Add a listener for browser events. Any changes in the browser will be
   * sent to this listener. The order of calling listeners is not guarenteed.
   * Checking is performed on whether the nominated listener is already
   * registered to ensure that multiple registration cannot take place.
   * Therefore it is possible to multiply register the one class
   * instance while only receiving one event.
   *
   * @param l The listener to add.
   * @exception NullPointerException If the provided listener reference is
   *     null
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void addBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

  /**
   * Remove a listener for browser events. After calling this method, the
   * listener will no longer recieve events from this browser instance. If the
   * listener passed as an argument is not currently registered, the method
   * will silently exit.
   *
   * @param l The listener to remove
   * @exception NullPointerException If the provided listener reference is
   *     null
   * @exception InvalidBrowserException The dispose method has been called on
   *    this browser reference.
   * @exception ConnectionException An error occurred in the connection to the
   *    browser.
   */
  public void removeBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

  /**
   * Dispose the resources that are used by this instance. Should be called
   * just prior to leaving the application.
   */
  public void dispose();
}
