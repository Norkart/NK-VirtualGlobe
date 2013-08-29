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
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

import java.awt.Component;
import java.applet.Applet;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * The factory class for obtaining references to browser instances.
 * <P>
 * An implementation independent representation of the class used to access
 * and create browsers. The model follows that used by java.net.Socket. A
 * setImpl method is provided for browser writers to provide the internal
 * implementations of the browser.
 * <P>
 * An alternative way of doing this is through a properties file. The class,
 * when it loads looks for the file vrml.properties in the class path. (For
 * more information on how this works read
 * <CODE>java.lang.ClassLoader.getSystemResourceAsStream()</CODE>). From this
 * file this class then uses the following properties:
 * <UL>
 * <LI><CODE>vrml.eai.factory.class</CODE>
 * </UL>
 *
 * The value of the factory class is then used as the name of the class to
 * load as the default browser implementation. This name must represent the
 * full package qualified name of the class. This class is then loaded using
 * the following method:
 *
 * <PRE>
 *  Class factory_class = Class.forName(factory_class_name);
 *  factory = (BrowserFactoryImpl)factory_class.newInstance();
 * </PRE>
 *
 * If a class cast exception is raised at the end, then an error is printed
 * but nothing is done about it. The result would be NullPointerExceptions
 * later in the code. Also, this may cause some security errors in some
 * web browsers.
 * <P>
 * To provide a custom implementation of the factory (which all
 * implementations must do) the user has the choice of the above two options
 * of either calling setImpl or by making sure that the vrml.properties
 * file appears in the classpath <I>before</I> the sample implementation
 * that comes with the classes from the VRMLC. If
 * <CODE>setBrowserFactoryImpl</CODE> has not been called at the time that
 * any of the other methods have been, then the class will attempt to load
 * the implementation defined in the properties file. Attempting to call the
 * set implementation method after this point shall result in a VrmlException
 * being generated. Otherwise, it shall use the set implementation.
 * <P>
 * If for some reason the properties file does not contain the property for
 * the name of the factory class or the properties file does not exist, then
 * the default class name is <CODE>vrml.eai.DefaultBrowserImpl</CODE>
 *
 * <p><b>Supported Parameters</b></p>
 * The X3D specification allows applications to specify parameters to change
 * how a browser operates.  You can also specify browser specifc options.
 * The following options are supported by Xj3D.
 * <p>
 * <table>
 * <tr><td><B>Param Name</B></td><td><B>Description</B></td><td><B>Type</B></td><td><B>Default</B></td><td><B>Legal Values</B></td></tr>
 * <tr><td>Antialiased</td><td>Whether to turn on antialiasing</td><td>Boolean</td><td>flase</td><td>true,false</td></tr>
 * <tr><td>TextureQuality</td><td>A quality metric for texturing.  High turns on all tricks like anisotropicFiltering</td><td>String</td><td>medium</td><td>low,medium,high</td></tr>
 * <tr><td>PrimitiveQuality</td><td>A quality metric for primitives.  Scales how many polygons to use for primitive</td><td>String</td><td>medium</td><td>low,medium,high</td></tr>
 * <tr><td>Xj3D_InterfaceType</td><td>Whether to use Swing or AWT</td><td>String</td><td>Swing</td><td>swing,awt,swing-lightweight</td></tr>
 * <tr><td>Xj3D_NavbarShown</td><td>Whether to show the navigation bar</td><td>Boolean</td><td>true</td><td>true,false</td></tr>
 * <tr><td>Xj3D_NavbarPosition</td><td>Where to position the navigation bar</td><td>String</td><td>Top</td><td>Top,Bottom</td></tr>
 * <tr><td>Xj3D_LocationShown</td><td>Whether the current location is shown</td><td>Boolean</td><td>true</td><td>true,false</td></tr>
 * <tr><td>Xj3D_LocationPosition</td><td>Where to position the location bar</td><td>String</td><td>Top</td><td>Top,Bottom</td></tr>
 * <tr><td>Xj3D_LocationReadOnly</td><td>Whether the location is read only</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_ShowConsole</td><td>Whether to show the console</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_OpenButtonShown</td><td>Whether to show a content Open button</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_ReloadButtonShown</td><td>Whether to show a content Reload button</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_StatusBarShown</td><td>Whether to show a status bar</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_FPSShown</td><td>Whether to show a Frames Per Second meter</td><td>Boolean</td><td>false</td><td>true,false</td></tr>
 * <tr><td>Xj3D_ContentDirectory</td><td>The initial directory to load content from</td><td>String</td><td>Current Directory</td><td>All</td></tr>
 * <tr><td>Xj3D_AntialiasingQuality</td><td>How many multisamples to use for antialiasing.  Must be enabled to matter.</td><td>String</td><td>low</td><td>low,medium,high</td></tr>
 * </table>
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class BrowserFactory
{
  /** The name of the properties file to read things from */
  private static final String PROPERTIES_FILE_NAME = "vrml.properties";

  /** Properties file location that is Xj3D-specific */
  private static final String XJ3D_PROPERTIES_FILE =
    "config/2.0/spec/" + PROPERTIES_FILE_NAME;

  /** The name of all the properties that are used by this this class */
  private static final String FACTORY_CLASS = "vrml.eai.factory.class";

  /** The default values of any properties */
  private static final String DEFAULT_FACTORY_CLASS = "vrml.eai.DefaultBrowserImpl";
    //"org.web3d.j3d.browser.J3DBrowserFactoryImpl";

  /** The reference to the factory implementation used */
  private static BrowserFactoryImpl factory = null;

  /** The list of properties needed by this class */
  private static Properties vrml_properties = null;

  /**
   * Static initialiser method. Used to load the system properties for
   * this class. If there are none then it sets up the default values
   * that are needed.
   * <P>
   * At this stage it does not load the factory class, just in case the
   * user may set something at a later date.
   */
  static
  {
    vrml_properties = new Properties();

    try
    {
      // fetch the properties file as a stream
      InputStream is = (InputStream)AccessController.doPrivileged(
        new PrivilegedAction()
        {
          public Object run()
          {
            // privileged code goes here, for example:
            return ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE_NAME);
          }
        }
      );

      // Fallback for WebStart
      if(is == null)
        is = (InputStream)BrowserFactory.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);

      // Now try the local Xj3D-specific area:
      // fetch the properties file as a stream
      if(is == null)
      {
        is = (InputStream)AccessController.doPrivileged(
          new PrivilegedAction()
          {
            public Object run()
            {
              // privileged code goes here, for example:
              return ClassLoader.getSystemResourceAsStream(XJ3D_PROPERTIES_FILE);
            }
          }
        );
      }

      // Fallback for WebStart
      if(is == null)
          is = (InputStream) BrowserFactory.class.getClassLoader().getResourceAsStream(XJ3D_PROPERTIES_FILE);

      // There is no properties file, then fill the properties list ourselves
      if(is == null)
      {
        vrml_properties.put(FACTORY_CLASS, DEFAULT_FACTORY_CLASS);
      }
      else
      {
        // from that stream load it into a properties table
        vrml_properties.load(is);
        is.close();
      }
    }
    catch(IOException ioe)
    {
      System.out.println(ioe);
    }
  }

  /**
   * Remove the constructor from public calling. Should never instantiate
   * this class.
   */
  private BrowserFactory()
  {
  }

  /**
   * set the factory implementation to use.
   */
  public static synchronized void setBrowserFactoryImpl(BrowserFactoryImpl fac)
  {
    if (factory != null)
      throw new VrmlException("factory already defined");

    // Check to see whether we can really set the factory needed.
    SecurityManager security = System.getSecurityManager();
    if (security != null)
      security.checkSetFactory();

    factory = fac;
  }

  /**
   * Create a VRML browser that can be used as an AWT component. The component
   * returned is guaranteed to be an instance of VrmlComponent.
   *
   * @param params Parameters to control the look and feel.
   * @return The component browser initialised to be empty.
   * @exception NotSupportedException The implementation does not support this
   *    type of VRML browser.
   * @see VrmlComponent
   */
  public static VrmlComponent createVrmlComponent(String[] params)
    throws NotSupportedException
  {
    if(factory == null)
      loadFactoryImpl();

    VrmlComponent comp = factory.createComponent(params);

    return comp;
  }

  /**
   * Get a browser from the given java applet reference as a base in the
   * current HTML page. Used when attempting to access a browser on the current
   * page as this applet and is the first browser on the page. Generically, the
   * same as calling getBrowser(applet, "", 0);
   *
   * @param applet The applet reference to use
   * @return A reference to the Browser implementation
   * @exception NotSupportedException The implementation does not support this
   *    type of VRML browser
   * @exception NoSuchBrowserException Could not locate a VRML browser on the
   *    same page as the applet.
   * @exception ConnectionException An error occurred during the connecting
   *    process
   */
  public static Browser getBrowser(Applet applet)
    throws NotSupportedException, NoSuchBrowserException, ConnectionException
  {
    if(factory == null)
      loadFactoryImpl();

    return factory.getBrowser(applet);
  }

  /**
   * Get a browser from the given java applet reference one some named page and
   * at some embbed location. Used when attempting to access a browser on
   * another HTML page within a multi-framed environment, or if there are a
   * number of VRML browser instances located on the same page.
   * <P>
   * If the frame name is a zero length string or null then it is assumed to be
   * located on the same HTML page as the applet. The index is the number of
   * the embbed VRML browser starting from the top of the page. If there are
   * other non-VRML plugins embedded in the page these are not taken into
   * account in calculating the embed index.
   *
   * @param applet The applet reference to use
   * @param frameName The name of the frame to look into for the browser
   * @param index The embed index of the VRML browser in the page
   * @return A reference to the Browser implementation
   * @exception NotSupportedException The implementation does not support this
   *    type of VRML browser.
   * @exception NoSuchBrowserException Could not locate a VRML browser on the
   *    same page as the applet.
   * @exception ConnectionException An error occurred during the connecting
   *    process
   */
  public static Browser getBrowser(Applet applet, String frameName, int index)
    throws NotSupportedException, NoSuchBrowserException, ConnectionException
  {
    if(factory == null)
      loadFactoryImpl();

    return factory.getBrowser(applet, frameName, index);
  }

  /**
   * Get a reference to a browser that is located on a remote machine. This
   * a server application to send scene updates to a number of client browsers
   * located on remote machines. If there are a number of browsers running on
   * a remote machine, they can be differentiated by the port number they are
   * listening on.
   * <P>
   * There is no default port number for VRML browsers.
   *
   * @param address The address of the machine to connect to
   * @param port The port number on that machine to connect to.
   * @return A reference to the Browser implementation
   * @exception NotSupportedException The implementation does not support this
   *    type of VRML browser.
   * @exception NoSuchBrowserException Could not locate a VRML browser on the
   *    same page as the applet.
   * @exception UnknownHostException Could not find the machine named in the
   *    address.
   * @exception ConnectionException An error occurred during the connecting
   *    process
   */
  public static Browser getBrowser(InetAddress address, int port)
    throws NotSupportedException,
           NoSuchBrowserException,
           UnknownHostException,
           ConnectionException
  {
    if(factory == null)
      loadFactoryImpl();

    return factory.getBrowser(address, port);
  }

  /**
   * Private method to load the resource file and use the appropriate class
   * defined in the properties file for dealing with the resource management
   * <P>
   * Assumes that the factory reference is currently null as it automatically
   * writes over the top of it.
   */
  private static void loadFactoryImpl()
  {
    try
    {
      // load the factory class
      String factory_class_name =
        (String)vrml_properties.getProperty(FACTORY_CLASS,
                                            DEFAULT_FACTORY_CLASS);

      Class factory_class = Class.forName(factory_class_name);
      factory = (BrowserFactoryImpl)factory_class.newInstance();
    }
    catch(ClassNotFoundException cnfe)
    {
      System.out.println("Unable to find vrml browser factory implementation\n" + cnfe);
    }
    catch(InstantiationException ie)
    {
      System.out.println("Error instantiating the vrml browser factory " + ie);
    }
    catch(IllegalAccessException iae)
    {
      System.out.println(iae);
    }
    catch(ClassCastException cce)
    {
      System.out.println("The nominated browser factory is not an instance of " +
                         "vrml.eai.BrowserFactoryImpl");
    }
  }
}





