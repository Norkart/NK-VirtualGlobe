/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

/**
 * The solver target interface.
 *
 * @author Andrzej Kapolka
 */

public interface SolverTarget
{
    /**
     * Returns the rates of change corresponding to the given values and time.
     *
     * @param values the values at which to evaluate the rates of change
     * @param time the time at which to evaluate the rates of change
     * @param results an array to hold the calculated rates
     */
    public void getRatesOfChange(double[] values, double time, double[] results);
}
