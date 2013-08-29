package xj3d.browser;

import javax.media.opengl.*;
import java.awt.*;

/**
 * A class for determinign the maximum antialiasing possible.
 *
 * NOTE: Why not pass in as the chooser for Aviaitrx and get then number then
 * instead of creating a seperate window.
 *
 * @author Alan Hudson
 * @version
 */
public class MultisampleChooser extends DefaultGLCapabilitiesChooser {
    /** Max samples field, modified by MultiSampleChooser. */
    private static int maxSamples = -1;

    public static int getMaximumNumSamples() {
        GLCapabilities caps = new GLCapabilities();
        GLCapabilitiesChooser chooser = new MultisampleChooser();
        caps.setSampleBuffers(true);

        Canvas canvas = new GLCanvas(caps, chooser, (GLContext) null, (GraphicsDevice) null);
        Frame frame = new Frame();
        frame.setUndecorated(true);
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

        return maxSamples;
    }

    public int chooseCapabilities(GLCapabilities desired,
        GLCapabilities[] available,
        int windowSystemRecommendedChoice) {

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
            int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
            if (!anyHaveSampleBuffers) {
                System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
            } else {
            if (!available[selection].getSampleBuffers()) {
                System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
            }
        }
        return selection;
    }
}