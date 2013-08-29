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
 * A simple listener for requests to create a new node.
 * <p>
 *
 */
interface NodeCreationListener
{
    /**
     * Request to create a new node. Add it as a child to the currently
     * selected node
     *
     * @param name The name of the node to create
     */
    public void createNode(String name);
}
