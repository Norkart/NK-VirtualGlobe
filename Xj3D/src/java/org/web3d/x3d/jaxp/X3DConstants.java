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

package org.web3d.x3d.jaxp;

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A collection of useful constants for setting up X3D specific capabilities
 * with the JAXP DOM parser.
 * <p>
 * Allows the use of property information without needing to import specific
 * instances of the factory.
 */
public interface X3DConstants {

    /** The formal DTD name for XML documents conforming to the X3D spec */
    public static final String DTD_NAME = "X3D";

    /** The formal system identifier for the specification V3.0 */
    public static final String GENERAL_SYSTEM_ID_3_0 =
        "http://www.web3d.org/specifications/x3d-3.0.dtd";

    /** The formal public Identifier for the specification V3.0 */
    public static final String GENERAL_PUBLIC_ID_3_0 =
        "ISO//Web3D//DTD X3D 3.0//EN";

    /** The formal public Identifier for the Interchange profile */
    public static final String INTERCHANGE_PUBLIC_ID_3_0 =
        "ISO//Web3D//DTD X3D 3.0 Interchange//EN";

    /** The formal public Identifier for the Interactive profile */
    public static final String INTERACTIVE_PUBLIC_ID_3_0 =
        "ISO//Web3D//DTD X3D 3.0 Interactive//EN";

    /** The formal public Identifier for the Immersive profile */
    public static final String IMMERSIVE_PUBLIC_ID_3_0 =
        "ISO//Web3D//DTD X3D 3.0 Immersive//EN";

    /** The formal public Identifier for the Full profile */
    public static final String FULL_PUBLIC_ID_3_0 =
        "ISO//Web3D//DTD X3D 3.0 Full//EN";


    /** The formal system identifier for the specification V3.1 */
    public static final String GENERAL_SYSTEM_ID_3_1 =
        "http://www.web3d.org/specifications/x3d-3.1.dtd";

    /** The formal public Identifier for the specification V3.1 */
    public static final String GENERAL_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1//EN";

    /** The formal public Identifier for the Interchange profile */
    public static final String INTERCHANGE_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1 Interchange//EN";

    /** The formal public Identifier for the Interactive profile */
    public static final String INTERACTIVE_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1 Interactive//EN";

    /** The formal public Identifier for the Immersive profile */
    public static final String IMMERSIVE_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1 Immersive//EN";

    /** The formal public Identifier for the Interchange profile */
    public static final String CAD_INTERCHANGE_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1 CADInterchange//EN";

    /** The formal public Identifier for the Full profile */
    public static final String FULL_PUBLIC_ID_3_1 =
        "ISO//Web3D//DTD X3D 3.1 Full//EN";


    /** The formal system identifier for the specification V3.2 */
    public static final String GENERAL_SYSTEM_ID_3_2 =
        "http://www.web3d.org/specifications/x3d-3.2.dtd";

    /** The formal public Identifier for the specification V3.2 */
    public static final String GENERAL_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2//EN";

    /** The formal public Identifier for the Interchange profile */
    public static final String INTERCHANGE_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2 Interchange//EN";

    /** The formal public Identifier for the Interactive profile */
    public static final String INTERACTIVE_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2 Interactive//EN";

    /** The formal public Identifier for the Immersive profile */
    public static final String IMMERSIVE_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2 Immersive//EN";

    /** The formal public Identifier for the Interchange profile */
    public static final String CAD_INTERCHANGE_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2 CADInterchange//EN";

    /** The formal public Identifier for the Full profile */
    public static final String FULL_PUBLIC_ID_3_2 =
        "ISO//Web3D//DTD X3D 3.2 Full//EN";



    /** The old, transitional public Identifier for the specification */
    public static final String OLD_PUBLIC_ID =
         "http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd";

    /** The old, transitional system Identifier for the specification */
    public static final String OLD_SYSTEM_ID =
         "/www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd";

    /** The new, transitional public Identifier for the specification */
    public static final String TRANS_PUBLIC_ID =
          "http://www.web3d.org/specifications/x3d-3.0.dtd";

    /** The new, transitional system Identifier for the specification */
    public static final String TRANS_SYSTEM_ID =
         "file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd";

    /** Schema definition for X3D 3.0 */
    public static final String SCHEMA_ID_3_0 =
        "http://www.web3d.org/specifications/x3d-3.0.xsd";

    /** Schema definition for X3D 3.1 */
    public static final String SCHEMA_ID_3_1 =
        "http://www.web3d.org/specifications/x3d-3.1.xsd";

    /** Schema definition for X3D 3.2 */
    public static final String SCHEMA_ID_3_2 =
        "http://www.web3d.org/specifications/x3d-3.2.xsd";

    /**
     * The URI that is used for determining if a namespace definition is the
     * X3D namespace or something else. The value is
     * <code>http://www.web3d.org/specifications/x3d-namespace</code>.
     * <b>Note</b>: This is an arbitrary value picked by Xj3D for the moment.
     * The real value(s) is yet to be determined by the X3D specification
     * process. This probably can and will change at somepoint, so don't get
     * too hooked on it for now.
     */
    public static final String X3D_NAMESPACE_URI =
        "http://www.web3d.org/specifications/x3d-namespace";

}
