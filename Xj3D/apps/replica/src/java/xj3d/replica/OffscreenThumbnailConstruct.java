/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

// Local imports
import org.web3d.util.ErrorReporter;

import org.xj3d.ui.awt.browser.ogl.OffscreenOGLConstruct;

/**
 * The customized browser Construct sub-class supporting the ThumbnailImager.
 * This Construct is built with the following modifications to the
 * 'standard' Construct.
 * <ul>
 * <li>The construct is built without support for UI devices,
 * such as Mouse or Keyboard.</li>
 * <li>The construct extends from OffscreenOGLConstruct to
 * function without a hardware graphics device.</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class OffscreenThumbnailConstruct extends OffscreenOGLConstruct
    implements ConfigGraphicsCapabilities {

    /** The logging identifer of this class */
    private static final String LOG_NAME = "OffscreenThumbnailConstruct";

    /**
     * Constructor
     *
     * @param reporter The error reporter
     */
    public OffscreenThumbnailConstruct( ErrorReporter reporter, int width, int height ) {
        super( reporter, width, height );
    }

    //----------------------------------------------------------
    // Methods defined by ConstructBuilder
    //----------------------------------------------------------

    /**
     * Override to build a UI 'device-less' browser.
     */
    public void buildAll( ) {
        buildRenderingCapabilities( );
        buildRenderingDevices( );
        //buildInterfaceDevices( );
        buildRenderer( );
        buildManagers( );
        buildNetworkCapabilities( );
    }

    /**
     * Set up the networking properties and objects needed to run the browser.
     *
     * @exception InvalidConfigurationException If a required class
     * class cannot be loaded.
     */
    public void buildNetworkCapabilities( ) {
        super.buildNetworkCapabilities( );
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>( ) {
                    public Object run( ) {
                        String prop = System.getProperty("java.content.handler.pkgs","" );
                        if( prop.indexOf( "vlc.net.content" ) == -1) {
                            System.setProperty( "java.content.handler.pkgs",
                                "vlc.net.content" );
                        }

                        return( null );
                    }
                } );
        } catch ( PrivilegedActionException pae ) {
            errorReporter.warningReport(
                LOG_NAME +": Exception setting System properties", pae );
        }
    }

    //----------------------------------------------------------
    // Methods defined by ConfigGraphicsCapabilities
    //----------------------------------------------------------

    /**
     * Set the Construct's parameters for building the graphics capabilities
     *
     * @param useMipMaps Should mipmaps be generated
     * @param doubleBuffered Should double (or single) buffering be used
     * @param antialiasSamples The antialias samples setting to be used.
     * A value of 1 or less disables antialiasing.
     * @param anisotropicDegree The anisotropic degree setting to be used.
     * A value of 1 or less disables anisotropic filtering.
     */
    public void setGraphicsCapabilitiesParameters(
        boolean useMipMaps,
        boolean doubleBuffered,
        int antialiasSamples,
        int anisotropicDegree ) {

        this.useMipMaps = useMipMaps;
        this.doubleBuffered = doubleBuffered;
        this.antialiasSamples = antialiasSamples;
        this.anisotropicDegree = anisotropicDegree;
    }
}
