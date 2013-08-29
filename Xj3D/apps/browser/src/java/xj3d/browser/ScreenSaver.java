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
import org.xj3d.ui.awt.widgets.SwingConsoleWindow;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.ScreenCaptureListener;

/**
 * An action that takes a screen shot of the current content.
 *
 * Currently saves to user.dir/foo.png should add a file dialog box.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ScreenSaver {
    /**
     * Save a buffer to a filename
     *
     * @param buffer The screen capture
     */
     public void saveScreen(Buffer buffer, String filename, int width, int height) {
        ByteBuffer pixelsRGB = (ByteBuffer) buffer;

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
            File outputFile = new File(filename + ".png");
            ImageIO.write(bufferedImage, "PNG", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
