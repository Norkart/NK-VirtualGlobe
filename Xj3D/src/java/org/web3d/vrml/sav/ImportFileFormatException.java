/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.sav;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.VRMLException;

/**
 * An exception that represents a parsing error during the import
 * of a file, usually due to an unsupported file type.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class ImportFileFormatException extends VRMLException {

    /**
     * Create an exception
     *
     * @param message - the detail message.
     */
    public ImportFileFormatException(String message) {
        super(message);
    }
}

