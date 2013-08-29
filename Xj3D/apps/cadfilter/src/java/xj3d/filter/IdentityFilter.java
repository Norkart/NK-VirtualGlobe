/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
// None

// Local imports
// None

/**
 * A filter which leaves the stream untouched.
 * <p>
 *
 * Effectively this implementation just turns the abstract class into a
 * concrete class without adding any additional implementation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class IdentityFilter extends AbstractFilter {

    /**
     * Create an instance of the filter.
     */
    public IdentityFilter() {
    }
}
