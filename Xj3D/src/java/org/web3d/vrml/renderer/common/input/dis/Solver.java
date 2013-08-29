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

import javax.vecmath.*;

/**
 * The differential equation solver service.
 *
 * @author Andrzej Kapolka
 */

public interface Solver {
    /**
     * Calculates and returns the values of the target variables after the specified time
     * interval.
     *
     * @param target the solver target, used to determine the rates of change
     * @param initialValues the initial values of the variables
     * @param timeInterval the time interval, in seconds
     * @param errorThreshold the maximum permissable error
     * @param results an array to hold the values of the variables at the end of the time
     * interval
     */
    public void solve(SolverTarget target, double[] initialValues, double timeInterval,
                      double errorThreshold, double[] results);
}