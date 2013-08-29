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

package org.web3d.browser;

// External imports
// none

// Local imports
// none

/**
 * Listener for changes in profiling data.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface ProfilingListener {
    /**
     * The profiling data has changed.  This will happen at the end of each frame render.
     *
     * @param data The profiling data
     */
    public void profilingDataChanged(ProfilingInfo data);
}
