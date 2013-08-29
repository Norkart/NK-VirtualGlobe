/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

/**
 * A dark and evil class who is very impatient.  If it isn't told to stop
 * in time it will kill the whole system.
 *
 * Used with captureViewpoints in case the browser hangs.  We want the
 * browser to still exit.
 *
 * @author Alan Hudson
 */
public class DeathTimer extends Thread {
    /** Are we done */
    private boolean done;

    /** How long to wait till we die in milleseconds*/
    private int waitTime;

    public DeathTimer(int wait) {
        done = false;
        waitTime = wait;
    }

    public void run() {
        long startTime = System.currentTimeMillis();

        while (!done) {
            try {
                Thread.sleep(500);
            } catch(Exception e) {}

            if (System.currentTimeMillis() - startTime > waitTime) {
                System.out.println("Time exceeded, killing system");
                System.exit(-1);
            }
        }
    }

    public void exit() {
        done = true;
    }
}