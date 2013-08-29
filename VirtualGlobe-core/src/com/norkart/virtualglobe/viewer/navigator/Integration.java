/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Integration.java
 *
 * Created on 7. mars 2008, 12:26
 *
 */

package com.norkart.virtualglobe.viewer.navigator;

/**
 *
 * @author runaas
 */
public class Integration {
    private static double recursive_asr(Func1 f, double a, double f_a, double b, double f_b, double c, double f_c, double eps, double sum, int maxdepth) {
        double ac = (a+c)/2;
        double f_ac = f.f(ac);
        double left = (c-a)/6 * (f_a + f_c + 4*f_ac);
        
        double cb = (c+b)/2;
        double f_cb = f.f(cb);
        double right = (b-c)/6 * (f_c + f_b + 4*f_cb);
        
        if (Math.abs(left + right - sum) <= 15*eps || --maxdepth <= 0)
            return left + right + (left + right - sum)/15;
        return recursive_asr(f, a, f_a, c, f_c, ac, f_ac, eps/2, left, maxdepth) + recursive_asr(f, c, f_c, b, f_b, cb, f_cb, eps/2, right, maxdepth);
    }
    
    /**
     * Adaptive Simpsons rule
     */
    public static double adaptiveSimpson(Func1 f, double a, double b, double eps, int maxdepth) {
        double c = (a+b)/2;
        double f_c = f.f(c);
        double f_a = f.f(a);
        double f_b = f.f(b);
        double sum = (b-a)/6 * (f_a + f_b + 4*f_c);
        
        return recursive_asr(f, a, f_a, b, f_b, c, f_c, eps, sum, maxdepth);
    }
      public static double adaptiveSimpson(Func1 f, double a, double b, double eps) {
          return adaptiveSimpson(f, a, b, eps, 10);
      }
    /*
     * Approximate the definite integral of f from a to b by Composite Simpson's rule, dividing the interval in n parts.
     */
    public static double simpson(Func1 f, double a, double b, int n) {
        double dx  = (b-a)/n;
        double sum = 0;
        
        for (int i = 0; i <= n; ++i) {
            double xk = a+i*dx;
            if (i==0 && i==n)
                sum += f.f(xk);
            else if (i%2 == 1)
                sum += 4*f.f(xk);
            else
                sum += 2*f.f(xk);
        }
        
        return (dx/3)*sum;
    }
}
