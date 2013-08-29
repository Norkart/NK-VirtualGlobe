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
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.awt.*;
import javax.media.opengl.*;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * A sample chooser for selecting the right number of multisamples.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class SampleChooser implements Runnable {

    /** Message when we can't find a matching format for the capabilities */
    private static final String NO_PIXEL_FORMATS_MSG =
        "WARNING: antialiasing will be disabled because none of the " +
        "available pixel formats had it to offer";

    /** Message the caller didn't request antialiasing */
    private static final String NO_AA_REQUEST_MSG =
        "WARNING: antialiasing will be disabled because the " +
        "DefaultGLCapabilitiesChooser didn't supply it";

    /** The number of samples we've discovered */
    private int maxSamples = -1;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Static class to find the number of samples available.  Returns
     * value by setting maxSamples field.
     */
    class MultisampleChooser extends DefaultGLCapabilitiesChooser {
        public int chooseCapabilities(GLCapabilities desired,
            GLCapabilities[] available,
            int recommended) {

                boolean anyHaveSampleBuffers = false;
                for (int i = 0; i < available.length; i++) {
                    GLCapabilities caps = available[i];
                    if (caps != null) {
                        if (caps.getNumSamples() > maxSamples)
                            maxSamples = caps.getNumSamples();
                        if (caps.getSampleBuffers())
                            anyHaveSampleBuffers = true;
                    }
                }
                int selection = super.chooseCapabilities(desired,
                                                         available,
                                                         recommended);
                if (!anyHaveSampleBuffers) {
                    errorReporter.messageReport(NO_PIXEL_FORMATS_MSG);
                } else {
                if (!available[selection].getSampleBuffers()) {
                    errorReporter.messageReport(NO_AA_REQUEST_MSG);
                }
            }
            return selection;
        }
    }

    /**
     * Construct a new, default instance of this class.
     */
    SampleChooser() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------

    /**
     * Run the capabilities of the class now.
     */
    public void run() {
       GLCapabilities caps = new GLCapabilities();
       GLCapabilitiesChooser chooser = new MultisampleChooser();
       caps.setSampleBuffers(true);

       Canvas canvas = new GLCanvas(caps, chooser, null, null);
       Frame frame = new Frame();
       canvas.setSize(16, 16);
       frame.add(canvas, BorderLayout.CENTER);
       frame.pack();
       frame.show();

       while(maxSamples < 0) {
           try {
               Thread.sleep(50);
           } catch(Exception e) {}
       }

       frame.hide();
       frame.dispose();
   }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Ask for the number of samples detected. This is only valid after having
     * been run. Otherwise, it will return a value of -1.
     *
     * @param a Positive value, or -1 if not run yet.
     */
    int getMaxSamples() {
        return maxSamples;
    }
}

