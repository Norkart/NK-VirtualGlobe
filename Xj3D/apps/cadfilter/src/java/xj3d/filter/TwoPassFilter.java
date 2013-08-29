/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2007
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
 * Marker for a filter which requires two passes.
 * <p>
 *
 * The calling code will pass the data through twice during its processing,
 * assuming that the document will be fully traversed before returning for
 * the second pass. The mechanisms for how this two pass is implemented
 * are not defined. Do not make any assumptions about it.
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public abstract class TwoPassFilter extends AbstractFilter {
    /**
     * Notification that the start of the first pass is beginning.
     */
    public abstract void startFirstPass();

    /**
     * Notification that the second of the first pass is beginning.
     */
    public abstract void startSecondPass();

}
