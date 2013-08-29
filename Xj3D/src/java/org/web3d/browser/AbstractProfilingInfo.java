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
 * Timing data for profiling the performance of different rendering stages.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class AbstractProfilingInfo implements ProfilingInfo {
	
    /** The total time to render the scene in nanoseconds */
    public long sceneRenderTime;

    /** The time spent in the cull stage in nanoseconds */
    public long sceneCullTime;

    /** The time spent in the sort stage in nanoseconds */
    public long sceneSortTime;

    /** The time spent in the draw stage in nanoseconds */
    public long sceneDrawTime;
	
    /** Return the total time to render the scene in nanoseconds */
    public long getSceneRenderTime() {
		return(sceneRenderTime);
	}
    /** Return the time spent in the cull stage in nanoseconds */
    public long getSceneCullTime() {
		return(sceneCullTime);
	}
    /** Return the time spent in the sort stage in nanoseconds */
    public long getSceneSortTime() {
		return(sceneSortTime);
	}
    /** Return the time spent in the draw stage in nanoseconds */
    public long getSceneDrawTime() {
		return(sceneDrawTime);
	}
}
