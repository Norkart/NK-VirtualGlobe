/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
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

// External imports
import javax.vecmath.*;

// Local imports
// None

/**
 * A variable for solving rotation convergence.
 *
 * @author Andrzej Kapolka, Alan Hudson
 * @version $Revision: 1.4 $
 */
public class Quat4dVariable implements SolverTarget {
    /**
     * The current value of the variable.
     */
    private Quat4d value;

    /**
     * The current value of the variable's derivatives.
     */
    private Vector3d[] derivatives;

    /**
     * The reference time at which the values were valid.
     */
    private long referenceTime;

    /**
     * A solver for convergence.
     */
    private Solver solver;

    // Scratch vars
    double[] state;
    double[] results;
    Quat4d q1;
    Quat4d q2;

    /**
     * Constructor and instance of this variable
     *
     * @param order the order of the converger
     * @param pSolver The solver to use, or null to generate one locally
     */
    public Quat4dVariable(int order, Solver pSolver) {
        value = new Quat4d();

        derivatives = new Vector3d[order];

        for(int i=0;i<order;i++) {
            derivatives[i] = new Vector3d();
        }

        state = new double[4+order*3];
        results = new double[4+order*3];

        if (pSolver != null) {
            this.solver = pSolver;
        } else {
            solver = new RungeKuttaSolver(state.length);
        }
    }

    /**
     * Sets the state of this variable.
     *
     * @param pValue the value of the variable
     * @param pDerivatives the value of the variable's derivatives
     * @param pReferenceTime the reference time at which the values were valid
     */
    public void set(Quat4d pValue, Vector3d[] pDerivatives, long pReferenceTime)    {
        value.set(pValue);
        referenceTime = pReferenceTime;

        for(int i=0;i<derivatives.length;i++) {
            if(i < pDerivatives.length) {
                derivatives[i].set(pDerivatives[i]);
            }
            else {
                derivatives[i].set(0.0,0.0,0.0);
            }
        }
    }

    /**
     * Sets the state of this variable to that of another variable.
     *
     * @param pVariable the variable to copy
     */
    public void set(Quat4dVariable pVariable)
    {
        value.set(pVariable.value);
        referenceTime = pVariable.referenceTime;

        for(int i=0;i<derivatives.length;i++)
        {
            derivatives[i].set(pVariable.derivatives[i]);
        }
    }

    /**
     * Sets the state of this variable to an interpolated state between the
     * two specified variables.
     *
     * @param v1 the starting variable
     * @param v2 the ending variable
     * @param alpha the interpolation parameter
     * @param newReferenceTime the new reference time
     */
    public void interpolate(Quat4dVariable v1, Quat4dVariable v2, double alpha, long newReferenceTime)
    {
        if(newReferenceTime > referenceTime)
        {
            v1.makeCurrent(newReferenceTime);
            v2.makeCurrent(newReferenceTime);

            value.interpolate(v1.value, v2.value, alpha);

            for(int i=0;i<derivatives.length;i++)
            {
                derivatives[i].interpolate(v1.derivatives[i], v2.derivatives[i], alpha);
            }

            referenceTime = newReferenceTime;
        }
    }

    /**
     * Makes the values of this variable current by integrating over time.
     *
     * @param newReferenceTime the new reference time
     */
    public void makeCurrent(long newReferenceTime)
    {
        if(newReferenceTime > referenceTime)
        {
            double interval = (newReferenceTime-referenceTime)/1000.0;

            encodeState();

            solver.solve(this, state, interval, 0.001, results);

            decodeState(results);

            referenceTime = newReferenceTime;
        }
    }

    /**
     * Encodes the state of this variable (its value and the values of its
     * derivatives) into a format suitable for use by the differential
     * equation solver.
     */
    public void encodeState()
    {
        state[0] = value.x;
        state[1] = value.y;
        state[2] = value.z;
        state[3] = value.w;

        for(int i=4,j=0;i<state.length;i+=3,j++)
        {
            state[i] = derivatives[j].x;
            state[i+1] = derivatives[j].y;
            state[i+2] = derivatives[j].z;
        }
    }

    /**
     * Decodes the state of this variable from the format used by
     * the differential equation solver.
     *
     * @param state the state vector to decode
     */
    public void decodeState(double[] state)
    {
        value.x = state[0];
        value.y = state[1];
        value.z = state[2];
        value.w = state[3];

        value.normalize();

        for(int i=4,j=0;i<state.length;i+=3,j++)
        {
            derivatives[j].set(
                state[i],
                state[i+1],
                state[i+2]
            );
        }
    }

    /**
     * Retrieves the current value of this variable.
     *
     * @param result a <code>Quat4d</code> to hold the result
     */
    public void getValue(Quat4d result)
    {
        result.set(value);
    }

    /**
     * Retrieves the values of the derivatives of this variable.
     *
     * @param result an array to hold the result
     */
    public void getDerivatives(Vector3d[] result)
    {
        for(int i=0;i<result.length;i++)
        {
            if(i < derivatives.length)
            {
                result[i].set(derivatives[i]);
            }
            else
            {
                result[i].set(0.0,0.0,0.0);
            }
        }
    }

    /**
     * Returns the rates of change corresponding to the given values and time.
     *
     * @param values the values at which to evaluate the rates of change
     * @param time the time at which to evaluate the rates of change
     * @param results an array to hold the calculated rates
     */
    public void getRatesOfChange(double[] values, double time, double[] results)
    {
        int i;
        if (q1 == null) {
            q1 = new Quat4d();
            q2 = new Quat4d();
        }

        q1.x = values[0];
        q1.y = values[1];
        q1.z = values[2];
        q1.w = values[3];

        q2.x = values[4];
        q2.y = values[5];
        q2.z = values[6];
        q2.w = 0.0;

        q2.mul(q1);
        q2.scale(0.5);

        results[0] = q2.x;
        results[1] = q2.y;
        results[2] = q2.z;
        results[3] = q2.w;

        for(i=4;i<(values.length-3);i+=3)
        {
            results[i] = values[i+3];
            results[i+1] = values[i+4];
            results[i+2] = values[i+5];
        }

        results[i] = 0.0;
        results[i+1] = 0.0;
        results[i+2] = 0.0;
    }
}

