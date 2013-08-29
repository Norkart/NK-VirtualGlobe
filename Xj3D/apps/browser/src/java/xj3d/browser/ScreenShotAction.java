/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External imports
import java.io.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.media.opengl.GL;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.ScreenCaptureListener;

/**
 * An action that takes a screen shot of the current content.
 *
 * Currently saves to user.dir/foo.png should add a file dialog box.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class ScreenShotAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /**
     * Create an instance of the action class.
     *
     */
    public ScreenShotAction(ErrorReporter console, OGLStandardBrowserCore core) {
        super("Single Frame");

        this.console = console;
        this.core = core;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN,0);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Take a screen shot");
    }

    //----------------------------------------------------------
    // Methods required for ScreenCaptureListener
    //----------------------------------------------------------

    /**
     * Notification of a new screen capture.  This will be in openGL pixel order.
     *
     * @param buffer The screen capture
     */
     public void screenCaptured(Buffer buffer) {
        ByteBuffer pixelsRGB = (ByteBuffer) buffer;

        Component comp = (Component) surface.getSurfaceObject();
        Dimension size = comp.getSize();
        int width = (int) size.getWidth();
        int height = (int) size.getHeight();

        ScreenSaver saver = new ScreenSaver();
        saver.saveScreen(buffer, "capture", width, height);

        console.messageReport("Screen shot saved to capture.png");
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {

        Component comp = (Component) surface.getSurfaceObject();
        Dimension size = comp.getSize();

        core.captureScreenOnce(this, (int) size.getWidth(), (int) size.getHeight());
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    /**
     * Set the surface we are rendering on.
     */
    public void setSurface(GraphicsOutputDevice surface) {
        this.surface = surface;
    }
}
