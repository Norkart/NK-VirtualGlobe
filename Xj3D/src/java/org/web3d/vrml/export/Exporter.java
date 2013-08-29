/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export;

// External imports
// None

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * Abstract representation of the ability to export content into some
 * external system - typically a file format.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class Exporter
    implements StringContentHandler,RouteHandler,ScriptHandler,ProtoHandler {

    /** The error reporter to use */
    protected ErrorReporter errorReporter;

    /** The major version of the spec this file belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this file belongs to. */
    protected int minorVersion;

    /**
     * Create a new exporter for the given spec version
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    public Exporter(int major, int minor, ErrorReporter reporter) {
        majorVersion = major;
        minorVersion = minor;

        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}
