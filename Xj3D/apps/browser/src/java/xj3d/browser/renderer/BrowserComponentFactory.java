/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser.renderer;

// External imports
import java.awt.Canvas;
import java.awt.Frame;
import javax.swing.JLabel;

import java.lang.reflect.Constructor;

// Local imports
import org.web3d.browser.BrowserCore;
import org.xj3d.core.eventmodel.PickingManager;
import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.SensorManager;
import org.xj3d.core.loading.SceneBuilderFactory;

/**
 * A factory for getting browser components.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class BrowserComponentFactory {
    public static final int JAVA3D_RENDERER = 0;
    public static final int OPENGL_RENDERER = 1;
    public static final int ELUMENS_RENDERER = 2;

    /** The number of parameters on a BrowserComponent constructor */
    private static final int NUM_PARAMS = 7;

    /**
     * Get a browser component.
     *
     * @param type The renderer type, defined in BrowserCore.
     */
    public static BrowserComponent getComponent(int type, Frame parent, JLabel statusLabel, boolean stereo,
        int fullscreen, int desiredSamples, int numZBits, int numCpus) {

        Object[] constParams = new Object[NUM_PARAMS];

        constParams[0] = parent;
        constParams[1] = statusLabel;
        constParams[2] = new Boolean(stereo);
        constParams[3] = new Integer(fullscreen);
        constParams[4] = new Integer(desiredSamples);
        constParams[5] = new Integer(numZBits);
        constParams[6] = new Integer(numCpus);

        Class[] paramTypes;
        Class devClass;
        Constructor[] consts;

        try {
            switch(type) {
                case JAVA3D_RENDERER:
                    devClass = Class.forName("xj3d.browser.renderer.java3d.J3DBrowserComponent");
                    consts = devClass.getConstructors();

                    for(int i=0; i < consts.length; i++) {
                        paramTypes = consts[i].getParameterTypes();

                        if (paramTypes.length == NUM_PARAMS) {
                            return (BrowserComponent) consts[i].newInstance(constParams);
                        }
                    }
                    break;
               case OPENGL_RENDERER:
                    devClass = Class.forName("xj3d.browser.renderer.ogl.OGLBrowserComponent");
                    consts = devClass.getConstructors();

                    for(int i=0; i < consts.length; i++) {
                        paramTypes = consts[i].getParameterTypes();

                        if (paramTypes.length == NUM_PARAMS) {
                            return (BrowserComponent) consts[i].newInstance(constParams);
                        }
                    }
                    break;
               case ELUMENS_RENDERER:
                    devClass = Class.forName("xj3d.browser.renderer.ogl.OGLBrowserComponent");
                    consts = devClass.getConstructors();

                    for(int i=0; i < consts.length; i++) {
                        paramTypes = consts[i].getParameterTypes();

                        if (paramTypes.length == NUM_PARAMS) {
                            BrowserComponent bc = (BrowserComponent) consts[i].newInstance(constParams);
                            return bc;
                        }
                    }
                    break;

               default:
                   System.out.println("Unsupported renderer in BrowserComponentFactory");
           }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
