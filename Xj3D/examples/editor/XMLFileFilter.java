/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A simple file filter for XML files.
 * <p>
 * The filter only accepts *.xml files
 */
class XMLFileFilter extends FileFilter
{
    /**
     * Check to see if the given file is acceptable for this filter.
     *
     * @param file The file to check
     * @return True if the file ends with .xml
     */
    public boolean accept(File file)
    {
        if(file.isDirectory())
            return true;

        String name = file.getName();
        int dot_pos = name.lastIndexOf('.');
        String ext = name.substring(dot_pos + 1);

        return ext.equalsIgnoreCase("xml");
    }

    /**
     * Return a description string of this filter that can be used in the UI.
     *
     * @return The current description string
     */
    public String getDescription()
    {
        return "XML files (*.xml)";
    }
}
