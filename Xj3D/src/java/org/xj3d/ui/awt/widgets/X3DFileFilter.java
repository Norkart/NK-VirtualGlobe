/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
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
import javax.swing.filechooser.*;

import java.io.File;
import java.util.HashSet;

// Local imports
// None

/**
 * File filter for restricting files to just X3D types.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class X3DFileFilter extends FileFilter {

    /** The valid extensions */
    private HashSet validExts;

    /**
     * Create a new file filter instance.
     */
    public X3DFileFilter() {
        validExts = new HashSet(6);

        validExts.add("x3d");
        validExts.add("x3dz");
        validExts.add("x3dv");
        validExts.add("x3dvz");
        validExts.add("x3db");
        validExts.add("gz");
    }

    /**
     * Should we accept this file
     *
     * @param f The file to test
     * @return true if acceptable
     */
    public boolean accept(File f) {
        if (f.isDirectory())
            return true;

        String extension = null;

        String s = f.getName();

        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
            extension = s.substring(i+1).toLowerCase();

        if (extension != null) {
            if (validExts.contains(extension))
                return true;
            else
                return false;
        }

        return false;
    }

    /**
     * The description of this filter
     */
    public String getDescription() {
        return "Just X3D Files";
    }
}
