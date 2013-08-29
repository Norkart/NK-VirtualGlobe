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

// Application specific imports
// none

/**
 * Representation of a parser factory for field content.
 * <p>
 *
 * This is the representation of a basic VRML parser. An parser implementation
 * would extend this instance to provide a specific parser. The implementation
 * class is specified by defining a system property
 * <pre>
 *    org.web3d.vrml.parser.field.factory
 * </pre>
 * The value of this property is the fully qualified class name of that
 * implementation. When the <CODE>ngetFieldParserFactory()</CODE> method is
 * called it will read that property and create a new instance using
 * reflection. The implementation must have a public, zero argument constructor
 * in order to be loaded.
 * <p>
 *
 * Each time the <CODE>newVRMLParserFactory()</CODE> method is called, it will
 * re-read the property and create an instance of the class. This allows you
 * to create different parser instances for each call within the one JVM
 * instance. If no property is defined then the default implementation is used.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public abstract class FieldParserFactory {

    /** Property name for the factory instance */
    public static final String FACTORY_CLASS_PROP =
        "org.web3d.vrml.parser.field.factory";

    /** Name of the default factory to load */
    private static final String DEFAULT_FACTORY =
        "org.web3d.parser.DefaultFieldParserFactory";

    /** The common instance for everyone to use */
    private static FieldParserFactory instance;

    /**
     * Construct a default instance of this factory.
     */
    protected FieldParserFactory() {
    }

    /**
     * Create a new factory parser instance each time this method is called.
     * Looks up the sytem property and builds a new instance on demand.
     *
     * @return An instance of the factory
     * @throws FactoryConfigurationError The class could not be found for some
     *   reason or other startup error.
     */
    public static FieldParserFactory getFieldParserFactory()
        throws FactoryConfigurationError {

        if(instance == null) {
            String classname = (String)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        // privileged code goes here, for example:
                        return System.getProperty(FACTORY_CLASS_PROP,
                                                  DEFAULT_FACTORY);
                    }
                }
            );

            FieldParserFactory ret_val = null;

            try {
                Class cls = Class.forName(classname);
                Object fac = cls.newInstance();
                instance = (FieldParserFactory)fac;
            } catch(Exception e) {
                throw new FactoryConfigurationError(e,
                   "Couldn't create factory class " + classname);
            }
        }

        return instance;
    }

    /**
     * Request a new instance of a parser for fields of the given specification
     * version.
     *
     * @param major The major version number of the field to be parsed
     * @param minor The minor version number of the field to be parsed
     * @return A new parser instance
     */
    public abstract VRMLFieldReader newFieldParser(int major, int minor);
}
