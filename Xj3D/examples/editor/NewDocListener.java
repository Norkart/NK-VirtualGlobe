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

/**
 * A simple listener for new document requests.
 * <p>
 *
 */
interface NewDocListener
{
    /**
     * Request to start a new document. Replaces the existing document with a
     * clean, empty document.
     */
    public void startNewDocument();
}
