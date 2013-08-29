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
 * File filter for restricting files to screen capture files
 *
 * @author Alan Hudson
 * @version
 */
public class PictureFilter implements FileFilter
{
    /** The valid extensions */
    private HashSet validExts;

    public PictureFilter() {
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
            return false;

        String s = f.getName();

        if (s.indexOf("_VP_") > -1 && s.endsWith("png")) {
            return true;
        }

        return false;
    }
}
