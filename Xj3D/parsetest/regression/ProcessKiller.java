/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2006
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

 /**
  * A stone cold killer.  It destroy's a process after a specified
  * amount of time.
  *
  * @author Alan Hudson
  * @version
  */
 public class ProcessKiller extends Thread {
    /** The process to manage */
    private Process p;

    /** The amount of time to wait */
    private int wait;

    /** Should we exit this thread */
    private boolean exit;

    /** Was this process destroyed */
    private boolean destroyed;

    public ProcessKiller(Process p, int wait) {
        this.p = p;
        this.wait = wait;

        exit = false;
        destroyed = false;
    }

    public void run() {
        long startTime = System.currentTimeMillis();

        while(exit != true && System.currentTimeMillis() - startTime < wait) {
            try {
                Thread.sleep(500);
            } catch(Exception e) {}

            //System.out.println("Waiting...");
        }

        if (!exit) {
            System.out.println("***Destroying process");
            destroyed = true;
            p.destroy();
        }

        p = null;
    }

    /**
     * Exit this watcher.
     */
    public void exit() {
        exit = true;
    }

    /**
     * Was the process destroyed.
     *
     * @return Whether this process was destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }
}