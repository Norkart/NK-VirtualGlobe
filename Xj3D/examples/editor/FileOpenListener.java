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

/**
 * A simple listener for file open requests.
 * <p>
 *
 * The method in this listener only gets called when a file name it requested
 * to be opened.
 */
interface FileOpenListener
{
    /**
     * Request that the named file be opened by the application. A check has
     * been made to make sure that the file already exists before this method
     * is called.
     *
     * @param file The file to be opened
     */
    void openFile(File file);
}
