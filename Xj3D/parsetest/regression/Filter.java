/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard library imports
import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

// Application specific imports

/**
 * File filter for restricting files to types Xj3D knows about.
 *
 * @author Alan Hudson
 * @version
 */
public class Filter implements FileFilter
{
    /** The valid extensions */
    private HashSet validExts;

    public Filter() {
        validExts = new HashSet(4);

        validExts.add("x3d");
        validExts.add("x3dv");
        validExts.add("x3db");
        validExts.add("wrl");
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
