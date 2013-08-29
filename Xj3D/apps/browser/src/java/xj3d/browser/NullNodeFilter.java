/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

public class NullNodeFilter implements NodeFilter {

    /**
     * Whether the given node is accepted by this filter.
     *
     * @param node The node to filter
     * @return Whether its accepted
     */
    public boolean accept(VRMLNodeType node) {
        return true;
    }

    /**
     * The description of the filter.
     *
     * @return The description
     */
    public String getDescription() {
        return "No Filter";
    }

    public String toString() {
        return getDescription();
    }
}
