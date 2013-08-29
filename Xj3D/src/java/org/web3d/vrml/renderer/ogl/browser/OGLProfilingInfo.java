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

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import org.j3d.aviatrix3d.rendering.ProfilingData;

// Local imports
import org.web3d.browser.AbstractProfilingInfo;

/**
 * Timing data for profiling the performance of different rendering stages.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class OGLProfilingInfo extends AbstractProfilingInfo {
	
	/**
	 * Constructor
	 */
	public OGLProfilingInfo(ProfilingData data) {
		this.sceneRenderTime = data.sceneRenderTime;
		this.sceneCullTime = data.sceneCullTime;
		this.sceneSortTime = data.sceneSortTime;
		this.sceneDrawTime = data.sceneDrawTime;
	}
}
