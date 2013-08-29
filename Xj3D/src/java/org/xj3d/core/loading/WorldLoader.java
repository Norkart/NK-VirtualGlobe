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

package org.xj3d.core.loading;

// External imports
import java.io.IOException;

// Local imports
import org.web3d.browser.BrowserCore;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface WorldLoader {

    /**
     * Request to load the world immediately. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core, InputSource source)
        throws IOException, VRMLParseException;

    /**
     * Request to load the world immediately. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @param ignoreHeader false if it should look for the file header to
     *        check version information
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core,
                             InputSource source,
                             boolean ignoreHeader)
        throws IOException, VRMLParseException;

    /**
     * Request to load the world immediately and constrain that loading to
     * using a specific specification version. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     * <p>
     *
     * A major version of 0 means to ignore the required version and just
     * load whatever can be found.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @param ignoreHeader false if it should look for the file header to
     *        check version information
     * @param majorVersion Require the given major version
     * @param minorVersion Require the given minor version
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core,
                             InputSource source,
                             boolean ignoreHeader,
                             int majorVersion,
                             int minorVersion)
        throws IOException, VRMLParseException;
    
    
    /** Shutdown the loader, release any resources */
    public void shutdown( );
}
