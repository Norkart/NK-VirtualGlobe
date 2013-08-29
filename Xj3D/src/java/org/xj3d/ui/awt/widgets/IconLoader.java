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

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * A convenience class that loads Icons and images for Xj3D's internal uses
 * and provides caching mechanisms.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class IconLoader {
    /** Message when we fail to find an icon */
    private static final String IMAGE_FAIL_MSG =
        "Unable to locate the required image file ";

    /** The default size of the map = roughly the number of default nodes */
    private static final int DEFAULT_SIZE = 70;

    /**
     * A hashmap of the loaded Icon instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static WeakHashMap loadedIcons;

    /**
     * A hashmap of the loaded image instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static WeakHashMap loadedImages;

    /**
     * Static initialiser to get all the bits set up as needed.
     */
    static {
        loadedIcons = new WeakHashMap(DEFAULT_SIZE);
        loadedImages = new WeakHashMap(DEFAULT_SIZE);
    }

    /**
     * Load an image for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath.
     *
     * @param name The path name to load the icon for. If not found,
     * no image is loaded.
     * @param reporter An error reporter to send error messages to
     * @return An image for the named path.
     */
    public static Image loadImage(String name, ErrorReporter reporter) {

        // Check the map for an instance first
        Image ret_val = (Image)loadedImages.get(name);

        if(name == null)
            return null;

        URL url = ClassLoader.getSystemResource(name);
        Toolkit tk = Toolkit.getDefaultToolkit();

        if(url != null)
            ret_val = tk.createImage(url);

        // Fallback for WebStart
        if(ret_val == null) {
            url = IconLoader.class.getClassLoader().getResource(name);

            if (url != null)
                ret_val = tk.createImage(url);

        }

        if(ret_val == null)
            reporter.warningReport(IMAGE_FAIL_MSG + name, null);
        else
            loadedImages.put(name, ret_val);

        return ret_val;
    }

    /**
     * Load an icon for the named node type. Looks in the classpath for
     * the image. The path must be qualified relative to the classpath.
     *
     * @param name The name of the node to load the icon for. If not found,
     *   no image is loaded.
     * @param reporter An error reporter to send error messages to
     * @return An icon for the named type.
     */
    public static ImageIcon loadIcon(String name, ErrorReporter reporter) {
        // Check the map for an instance first
        ImageIcon ret_val = (ImageIcon)loadedIcons.get(name);

        if(ret_val == null) {
            Image img = loadImage(name, reporter);

            if(img != null) {
                ret_val = new ImageIcon(img, name);

                loadedIcons.put(name, ret_val);
            }
        }

        return ret_val;
    }
}
