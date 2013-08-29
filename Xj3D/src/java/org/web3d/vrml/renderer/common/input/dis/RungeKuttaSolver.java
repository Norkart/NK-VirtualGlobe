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
 * A fourth order Runge-Kutta solver with adaptive step sizing, as described
 * here: <a href="http://www-2.cs.cmu.edu/~baraff/sigcourse/notesb.pdf">
 * http://www-2.cs.cmu.edu/~baraff/sigcourse/notesb.pdf</a>.
 *
 * @author Andrzej Kapolka, Alan Hudson
 * @version $Revision: 1.2 $
 */
public class RungeKuttaSolver implements Solver {

    // Scratch vars
    private double[] k1;
    private double[] k2;
    private double[] k3;
    private double[] k4;
    private double[] tmp;
    private double[] tmp2;

    private double[] wholeStepResults;
    private double[] halfStepResults;

    /**
     * Constructor.
     *
     * @param size The maximum number of variables.
     */
    public RungeKuttaSolver(int size) {
        k1 = new double[size];
        k2 = new double[size];
        k3 = new double[size];
        k4 = new double[size];
        tmp = new double[size];

        wholeStepResults = new double[size];
        halfStepResults = new double[size];
        tmp2 = new double[size];
    }

    /**
     * Sets the elements of the first array to the values of the elements
     * of the second array (<code>a1 = a2</code>).
     *
     * @param a1 the destination array
     * @param a2 the source array
     */
    private void arraySet(double[] a1, double[] a2)
    {
        System.arraycopy(a2,0,a1,0,a2.length);
    }

    /**
     * Scales the specified array by the given factor
     * (<code>a *= s</code>).
     *
     * @param a the array to scale
     * @param s the scale factor
     */
    private void arrayScale(double[] a, double s)
    {
        for(int i=0;i<a.length;i++)
        {
            a[i] *= s;
        }
    }

    /**
     * Adds the first array to the second array and stores the result in the third
     * array (<code>result = a1 + a2</code>).
     *
     * @param a1 the first array
     * @param a2 the second array
     * @param result an array to hold the result
     */
    private void arrayAdd(double[] a1, double[] a2, double[] result)
    {
        for(int i=0;i<a1.length;i++)
        {
            result[i] = a1[i] + a2[i];
        }
    }

    /**
     * Scales the first array by the specified scale factor, adds it to the second
     * array, and stores the result in the third array
     * (<code>result = a1*s + a2</code>).
     *
     * @param a1 the first array
     * @param s the scale factor
     * @param a2 the second array
     * @param result an array to hold the result
     */
    private void arrayScaleAdd(double[] a1, double s, double[] a2, double[] result)
    {
        for(int i=0;i<a1.length;i++)
        {
            result[i] = a1[i]*s + a2[i];
        }
    }

    /**
     * Computes and returns the maximum error between the elements of
     * the two specified arrays (which must be of the same size).
     *
     * @param a1 the first array to compare
     * @param a2 the second array to compare
     * @return the maximum error between the elements of the two arrays
     */
    private double arrayError(double[] a1, double[] a2)
    {
        double error, maxError = 0.0;

        for(int i=0;i<a1.length;i++)
        {
            error = Math.abs( a1[i] - a2[i] );

            if(error > maxError) maxError = error;
        }

        return maxError;
    }

    /**
     * Calculates the result of a single integration step.
     *
     * @param target the solver target, used to determine the rate of change
     * @param initialValue the initial value of the variables
     * @param timeInterval the time interval, in seconds
     * @param results an array to hold the values of the variables after the integration step
     */
    private void step(SolverTarget target, double[] initialValues, double timeInterval,
                      double[] results)
    {
        target.getRatesOfChange(initialValues,0.0,k1);
        arrayScale(k1,timeInterval);

        arrayScaleAdd(k1,0.5,initialValues,tmp);

        target.getRatesOfChange(tmp,timeInterval*0.5,k2);
        arrayScale(k2,timeInterval);

        arrayScaleAdd(k2,0.5,initialValues,tmp);

        target.getRatesOfChange(tmp,timeInterval*0.5,k3);
        arrayScale(k3,timeInterval);

        arrayAdd(initialValues,k3,tmp);

        target.getRatesOfChange(tmp,timeInterval,k4);
        arrayScale(k4,timeInterval);

        for(int i=0;i<initialValues.length;i++)
        {
            results[i] = initialValues[i] + k1[i]/6.0 + k2[i]/3.0 + k3[i]/3.0 + k4[i]/6.0;
        }
    }

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
                      double errorThreshold, double[] results)
    {
        arraySet(results,initialValues);

        double stepSize = timeInterval;

        while(timeInterval>0.0)
        {
            step(target,results,stepSize,wholeStepResults);

            step(target,results,stepSize*0.5,tmp2);
            step(target,tmp2,stepSize*0.5,halfStepResults);

            double error = arrayError(wholeStepResults,halfStepResults);

            if( error <= errorThreshold )
            {
                arraySet(results,halfStepResults);

                timeInterval -= stepSize;
            }

            stepSize = Math.min(
                stepSize * Math.pow(errorThreshold/error,1.0/5.0),
                timeInterval
            );
        }
    }
}
