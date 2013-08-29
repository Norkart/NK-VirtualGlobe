/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript;

// Standard imports
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Function;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

// Application specific imports

/**
 * The global object for ECMAScript.  Used to implement the print function.
 *
 * @author Alan Hudson
 */
public class Global extends ScriptableObject {
    /** Class that represents the external reporter */
    private static ErrorReporter errorReporter;

    /**
      * Return name of this class, the global object.
      *
      * This method must be implemented in all concrete classes
      * extending ScriptableObject.
      *
      * @see org.mozilla.javascript.Scriptable#getClassName
      */
     public String getClassName() {
         return "global";
     }

    /**
      * Print the string values of its arguments.
      *
      * This method is defined as a JavaScript function.
      * Note that its arguments are of the "varargs" form, which
      * allows it to handle an arbitrary number of arguments
      * supplied to the JavaScript function.
      *
      */
     public static void print(Context cx, Scriptable thisObj,
                              Object[] args, Function funObj) {

        if (errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        StringBuffer sb = new StringBuffer();

         for (int i=0; i < args.length; i++) {
             if (i > 0)
                 sb.append(" ");

             // Convert the arbitrary JavaScript value into a string form.
             String s = Context.toString(args[i]);

             sb.append(s);
         }

        if(errorReporter != null)
            errorReporter.messageReport(sb.toString());
        else
            System.out.println(sb.toString());
     }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}
