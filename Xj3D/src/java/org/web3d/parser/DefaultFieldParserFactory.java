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

package org.web3d.parser;

// Standard imports
// none

// Application specific imports
import org.web3d.parser.vrml97.VRML97FieldReader;
import org.web3d.parser.x3d.X3DFieldReader;
import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;

/**
 * Representation of a parser factory for field content.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class DefaultFieldParserFactory extends FieldParserFactory {

    /**
     * Construct a default instance of this factory.
     */
    public DefaultFieldParserFactory() {
    }

    /**
     * Request a new instance of a parser for fields of the given specification
     * version.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @return A new parser instance
     */
    public VRMLFieldReader newFieldParser(int major, int minor) {
        switch(major) {
            case 2:
                return new VRML97FieldReader();

            case 3:
                X3DFieldReader rdr = new X3DFieldReader();
                rdr.setCaseSensitive(false);
                return rdr;
            default:
System.out.println("Unknown field parser version requested " + major);
                return null;
        }
    }
}
