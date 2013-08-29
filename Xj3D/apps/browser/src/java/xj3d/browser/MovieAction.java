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

import javax.swing.Action;
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
public class MovieAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Is this the start or end action */
    private boolean start;

    /** The frame number */
    private int frame;

    /**
     * Create an instance of the action class.
     *
     */
    public MovieAction(boolean start, ErrorReporter console, OGLStandardBrowserCore core) {
        super("Single Frame");

        if (start) {
            putValue(Action.NAME, "Start Movie");
            putValue(SHORT_DESCRIPTION, "Start Movie Recording");
        } else {
            putValue(Action.NAME, "End Movie");
            putValue(SHORT_DESCRIPTION, "End Movie Recording");
        }

        this.start = start;
        this.console = console;
        this.core = core;
        frame = 0;
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

        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the
        // rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL.

        int p = width * height * 3; // Points to first byte (red) in each row.
        int q;                  // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int w3 = width*3;         // Number of bytes in each row

        for (int row = 0; row < height; row++) {
            p -= w3;
            q = p;
            for (int col = 0; col < width; col++) {
                int iR = pixelsRGB.get(q++);
                int iG = pixelsRGB.get(q++);
                int iB = pixelsRGB.get(q++);

                pixelInts[i++] = 0xFF000000
                             | ((iR & 0x000000FF) << 16)
                             | ((iG & 0x000000FF) << 8)
                             | (iB & 0x000000FF);
            }

        }

        BufferedImage bufferedImage =
               new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);

        try {
            File outputFile = new File("capture_" + String.format("%0,4d",frame) + ".png");
            ImageIO.write(bufferedImage, "PNG", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame++;
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

        // TODO: This will not work if they resize the window during the capture.

        if (start) {
            core.captureScreenStart(this, (int) size.getWidth(), (int) size.getHeight());
        } else {
            core.captureScreenEnd();
        }
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
