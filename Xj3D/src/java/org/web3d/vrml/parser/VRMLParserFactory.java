/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.parser;

// Standard imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.util.HashSet;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.sav.SAVNotSupportedException;

/**
 * Representation of a parser factory for VRML content.
 * <p>
 *
 * This is the representation of a basic VRML parser. An parser implementation
 * would extend this instance to provide a specific parser. The implementation
 * class is specified by defining a system property
 * <pre>
 *    org.web3d.vrml.parser.file.factory
 * </pre>
 * The value of this property is the fully qualified class name of that
 * implementation. When the <CODE>newVRMLParserFactory()</CODE> method is
 * called it will read that property and create a new instance using
 * reflection. The implementation must have a public, zero argument constructor
 * in order to be loaded.
 * <p>
 * Each time the <CODE>newVRMLParserFactory()</CODE> method is called, it will
 * re-read the property and create an instance of the class. This allows you
 * to create different parser instances for each call within the one JVM
 * instance. If no property is defined then the default implementation is used.
 * <p>
 * All factories are required to support the feature name "VRML-utf8". The
 * version is the VRML specification version supported in the UTF8 encoding
 * (it is also possible the binary version may use this, but definitely not
 * XML encoding).
 * <p>
 *
 * The following a standard properties that may be required of all factories
 * and readers
 * <table>
 * <tr><th>Name</th><th>Values</th><th>Default</th><th>Description</th></tr>
 * <tr><td>Required-Version</td>
 *     <td>"2.0", "3.0"</td>
 *     <td><i>NULL</i></td>
 *     <td>When defined it says that the VRMLReader provided must only support
 *         the given version. If it is not supplied then the reader will do its
 *         best effort to adapt it's parsing to the version supplied in the
 *         stream's header
 *     </td>
 * </tr>
 * <tr><td>Required-Format</td>
 *     <td>"VRML", "X3D"</td>
 *     <td><i>NULL</i></td>
 *     <td>When defined it says to use this specific file format for the
 *         specification.
 *     </td>
 * </tr>
 * </table>
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public abstract class VRMLParserFactory {

    /** Property name for the factory instance */
    public static final String FACTORY_CLASS_PROP =
        "org.web3d.vrml.parser.file.factory";

    /** Feature name for the required VRML feature */
    public static final String VRML_FEATURE = "VRML-utf8";

    /** Feature name for the required VRML feature */
    public static final String X3D_FEATURE = "X3D-utf8";

    /** Property defining a required version */
    public static final String REQUIRE_VERSION_PROP = "Required-Version";

    /** Property defining the required file type */
    public static final String REQUIRE_FORMAT_PROP = "Required-Format";

    /** Property set defining all properties */
    private static HashSet validProperties;

    /** Name of the default factory to load */
    private static final String DEFAULT_FACTORY =
        "org.web3d.parser.DefaultVRMLParserFactory";

    /** The list of properties that have been set. Initially empty */
    protected Map propertyMap;

    /**
     * Construct a default instance of this factory.
     */
    protected VRMLParserFactory() {
        propertyMap = new HashMap();

        validProperties = new HashSet(2);
        validProperties.add(REQUIRE_VERSION_PROP);
        validProperties.add(REQUIRE_FORMAT_PROP);
    }

    /**
     * Create a new factory parser instance each time this method is called.
     * Looks up the sytem property and builds a new instance on demand.
     *
     * @return An instance of the factory
     * @throws FactoryConfigurationError The class could not be found for some
     *   reason or other startup error.
     */
    public static VRMLParserFactory newVRMLParserFactory()
        throws FactoryConfigurationError {

        String classname = (String)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // privileged code goes here, for example:
                    return System.getProperty(FACTORY_CLASS_PROP,
                                              DEFAULT_FACTORY);
                }
            }
        );

        VRMLParserFactory ret_val = null;

        try {
            Class cls = Class.forName(classname);
            Object fac = cls.newInstance();
            ret_val = (VRMLParserFactory)fac;
        } catch(Exception e) {
            throw new FactoryConfigurationError(e,
               "Couldn't create factory class " + classname);
        }

        return ret_val;
    }

    /**
     * Request a new instance of a VRMLReader to parse documents.
     *
     * @return A new reader instance
     */
    public abstract VRMLReader newVRMLReader();

    /**
     * Get the value of the named property. VRMLReaders are not required to
     * support any specific property names.
     *
     * @param prop The name of the property to get the value of
     * @return The value of the set property or null if not set
     * @throws SAVNotSupportedException The VRMLReader does not recognize
     *   or does not support this property name.
     */
    public Object getProperty(String prop)
        throws SAVNotSupportedException
    {
        if(!validProperties.contains(prop))
            throw new SAVNotSupportedException("Unknown property: " + prop);

        return propertyMap.get(prop);
    }

    /**
     * Set the value of the named property to the given value. VRMLReaders are
     * not required to support any specific property names. Using a value of
     * null will clear the currently set property value. Setting this value
     * here will ensure that the value is set in each instance of
     * {@link org.web3d.vrml.sav.VRMLReader} before it is returned to the user
     * after creation.
     *
     * @param name The name of the property to set
     * @param value The value of this property
     * @throws SAVNotSupportedException The VRMLReader does not recognize
     *   or does not support this property name.
     */
    public void setProperty(String name, Object value)
        throws SAVNotSupportedException
    {
        if(!validProperties.contains(name))
            throw new SAVNotSupportedException("Unknown property: " + name);

        propertyMap.put(name, value);
    }

    /**
     * Check to see if this implementation has the nominated feature. If there
     * is no version information passed, it will look for features of all
     * versions.
     *
     * @param feature The name of the feature requested
     * @param version The version of the feature required or null for none
     * @return true if the feature and version is supported
     */
    public abstract boolean hasFeature(String feature, String version);
}
