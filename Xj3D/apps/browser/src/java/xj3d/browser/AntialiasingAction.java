/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
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

// Standard library imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.media.opengl.GLCapabilities;

// Application specific imports
import org.xj3d.ui.awt.widgets.SwingStatusBar;

/**
 * An action that can be used to change antialiasing modes.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.2 $
 */
public class AntialiasingAction extends AbstractAction {
    /** Maximum Number of antialiasing samples */
    private int maxSamples = -1;

    /** Number of antialiasing samples */
    private int numSamples = 1;

    /** The surfaceManager */
    private SurfaceManager surfaceManager;

    /** The glCapabilities choosen */
    private GLCapabilities caps;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /**
     * Create an instance of the action class.
     *
     * @param manager The surface manager
     */
    public AntialiasingAction(SurfaceManager manager, SwingStatusBar statusBar) {
        super("");

        surfaceManager = manager;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Cycles the antialising");

        // Get the system maximum samples
        maxSamples =  maxSamples = MultisampleChooser.getMaximumNumSamples();
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        String val = evt.getActionCommand();

        if (val.equals("Disabled")) {
            numSamples = 1;
        } else if (val.equals("Cycle")) {
            cycleAntialiasing();
            return;
        } else {
            numSamples = Integer.parseInt(val);
        }

        changeSamples();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the maximum number of samples.
     *
     * @return the maximum number of samples
     */
    public int getMaximumNumberOfSamples() {
        return maxSamples;
    }

    /**
     * Set the desired samples.  This will be capped at the current system maximum.
     *
     * @param desired The desired number of samples
     */
    public void setDesiredSamples(int desired) {
        if (desired > maxSamples)
            numSamples = maxSamples;
        else
            numSamples = desired;

        changeSamples();
    }

    /**
     * Cycle through antialiasing options.
     *
     * @param p1 The panel the surface is in.
     */
    private void cycleAntialiasing() {
        numSamples = numSamples * 2;
        if (numSamples > maxSamples)
            numSamples = 1;

        changeSamples();
    }

    /**
     * Change to the current numSamples.
     */
    private void changeSamples() {
        if (numSamples == 1)
            statusBar.setStatusText("Antialiasing disabled");
        else
            statusBar.setStatusText("Antialiasing samples: " + numSamples + " out of max: " + maxSamples);

        caps = surfaceManager.getCapabilities();

        if (numSamples > 1)
            caps.setSampleBuffers(true);
        else
            caps.setSampleBuffers(false);
        caps.setNumSamples(numSamples);

        surfaceManager.resetSurface();
    }
}
