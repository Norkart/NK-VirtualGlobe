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
import javax.swing.filechooser.*;
import java.io.File;
import java.util.HashSet;

// Application specific imports

/**
 * File filter for restricting files to types Xj3D knows about.
 *
 * @author Alan Hudson
 * @version
 */
public class Xj3DFileFilter extends FileFilter
{
    /** The valid extensions */
    private HashSet validExts;

    public Xj3DFileFilter() {
        validExts = new HashSet(4);

        validExts.add("x3d");
        validExts.add("x3dz");
        validExts.add("x3d.gz");
        validExts.add("x3dv");
        validExts.add("x3dvz");
        validExts.add("x3dv.gz");
        validExts.add("x3db");
        validExts.add("x3dbz");
        validExts.add("x3db.gz");
        validExts.add("wrl");
        validExts.add("wrz");
    }

    /**
     * Should we accept this file
     *
     * @param f The file to test
     * @return true if acceptable
     */
    public boolean accept(File f)
    {
        if (f.isDirectory())
            return true;

        String extension = null;

        String s = f.getName();

        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            extension = s.substring(i+1).toLowerCase();
        }


        if (extension != null)
        {
            if (validExts.contains(extension))
                return true;
            else
                return false;
        }

        return false;
    }

    // The description of this filter
    public String getDescription()
    {
        return "Just X3D/VRML Files";
    }
}
