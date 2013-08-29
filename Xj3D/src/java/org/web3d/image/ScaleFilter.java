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

package org.web3d.image;

// External imports
// none

// Local imports
// none

/**
 * Defines the requirements for a scaling filter for NIOBufferImage objects
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ScaleFilter {

    /**
     * Return an image scaled to the specified width and height
     *
     * @param width The width of the returned image
     * @param height The height of the returned image
     * @return The scaled image
     */
    public NIOBufferImage getScaledImage( int width, int height );
}
