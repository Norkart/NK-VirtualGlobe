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
import java.util.List;

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
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.lang.TypeConstants;
import org.xj3d.core.eventmodel.ViewpointManager;

/**
 * An action that takes a screen shot of all the top-level viewpoints
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class CaptureViewpointsAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The current name */
    private String currentName;

    /** The current unknown count */
    private int unknownCount;

    /** The manager of viewpoints that we use to change them on the fly */
    private ViewpointManager vpManager;

    /** Has the capture happended */
    private boolean captured;

    /** The basename */
    private String basename;

    /**
     * Create an instance of the action class.
     *
     */
    public CaptureViewpointsAction(ErrorReporter console,
        OGLStandardBrowserCore core, ViewpointManager vpMgr) {
        super("Capture Viewpoints");

        vpManager = vpMgr;
        this.console = console;
        this.core = core;

        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN,0);

        //putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Capture Viewpoints");
    }

    /**
     * Set the basename to use on file output
     *
     * @param basename The basename
     */
    public void setBasename(String basename) {
        this.basename = basename;
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
        saver.saveScreen(buffer, currentName, width, height);

        console.messageReport("Screen shot saved to:" + currentName + ".png");
        captured = true;
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

        List vp_list = vpManager.getActiveViewpoints();

        VRMLViewpointNodeType vp;

        int len = vp_list.size();

        if (len == 0) {
            currentName = basename + "_VP_Unnamed_1";
            capture(size);
            return;
        }

        for(int i=0; i < vp_list.size(); i++) {
            vp = (VRMLViewpointNodeType) vp_list.get(i);

            if(!vp.getIsBound()) {
//                System.out.println("Binding to: " + vp);
                vpManager.setViewpoint(vp);
            }
            String desc = vp.getDescription().replace(" ", "_");
            desc = desc.replace("\\","_");
            desc = desc.replace("/", "_");

            currentName = basename + "._VP_" + desc;

            if (currentName == null) {
                unknownCount++;
                currentName = basename + "_VP_Unnamed_" + unknownCount;
            }

            try {
                Thread.sleep(500);
            } catch(Exception e) {}

            capture(size);
        }

    }

    /**
     * Capture the current screen.
     */
    private void capture(Dimension size) {
        captured = false;
        core.captureScreenOnce(this, (int) size.getWidth(), (int) size.getHeight());

        while(!captured) {
            try {
                Thread.sleep(100);
            } catch(Exception e) {}
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
