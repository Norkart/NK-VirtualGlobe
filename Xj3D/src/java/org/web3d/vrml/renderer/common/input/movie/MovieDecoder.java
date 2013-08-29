/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.movie;

// External imports
import java.util.ArrayList;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.control.TrackControl;

// Local imports

/**
 * This class wraps an instance of the JMF Processor class.
 * It decodes a mpeg video stream and sends the audio and video
 * frames to the handlers specified at creation time.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.8 $
 */
public class MovieDecoder implements ControllerListener {

    public final int REALIZED =  0x0001;
    public final int STARTED  =  0x0002;
    public final int REWOUND  =  0x0004;
    public final int TIMESET  =  0x0008;
    public final int ALLSTATES=  0x000F;

    /*
     * Implementors Notes:
     *
     * The JMF mpeg decoding is not entirely bug-free, although it is
     * arguably better than the J3D Sound.  There are several issues that
     * have affected the code below:
     *
     * 1) Calls to the processor can be very time consuming.
     *    Calling processor.setMediaTime() can take several seconds.
     *    To minimize the amount of time we block the main thread, we
     *    queue requests which are serviced by a dedicated thread.
     *
     * 2) The processor is fussy about what state it is in.
     *    We get async callbacks from the processor when it changes
     *    state.  The control thread has some synchronization calls
     *    to allow it to wait for a given state before it calls a
     *    processor method.
     *
     * 3) The setMediaTime() call is buggy.  If you call
     *    stop(),setMediaTime(new Time(0)),start on a processor
     *    the call to setMediaTime will frequently never return,
     *    even if you wait for the stop() command to be acknowledged
     *    by an event to the ControllerListener.  It is much more
     *    reliable (but still occasionally locks) if you ffwd to
     *    the end of the stream, start the stream and let it end
     *    on its own accord, then rewind it.
     */

    //----------------------------------------------------------------------
    // Internal classes implement an asynchronous control queue
    //----------------------------------------------------------------------

    /**
     * This is the base class for actions performed on the processor.
     * Each subclass of Action defines an operation that can be
     * performed on the processor.  Actions are put into a queue and
     * processed sequentially.  This is necessary because calls made
     * to control the state of the processor are prone to long delays,
     * and also may cause callbacks to the handlers which can cause
     * deadlocks.
     */
    public interface Action {
        /**
         * Perform the specified action.  Called by the control
         * thread when this action is to be performed.
         */
        void run();
    }

    /**
     * This action starts the processor running.
     */
    public class StartAction implements Action {
        /**
         * Start the processor.
         */
        public void run()
        {
            //Debug.trace();
            //Debug.trace("----WAIT FOR REALIZED----");
            controlThread.waitForState(REALIZED);
            //Debug.trace("----START----");
            processor.start();
            //Debug.trace("----WAIT FOR STARTED----");
            controlThread.waitForState(STARTED);
        }
    }

    /**
     * This action class stops a running processor.
     */
    public class StopAction implements Action {
        /**
         * Stop the processor.
         */
        public void run()
        {
            //Debug.trace();
            processor.stop();
            controlThread.waitForState(0,STARTED);
        }
    }

    /**
     * This action rewinds the processor and starts it playing.
     * It should be called on a stopped processor, and leaves
     * the processor in the stopped state.
     */
    public class RewindAction implements Action {
        /**
         * Rewinds a stopped processor.
         */
        public void run()
        {
            //Debug.trace(controlThread.stateString(state));

            //Debug.trace("----WAIT FOR REALIZED----");
            controlThread.waitForState(REALIZED);

            if ((state & REWOUND)>0) {
                //Debug.trace("----ALREADY REWOUND----");
                return;
            } else {
                //Debug.trace("----NOT REWOUND----"+controlThread.stateString(state));
            }

            videoRenderer.enableCallbacks(false);    // disable start/stop callbacks

            // We should be stopped.  Make certain.
            if ((state & STARTED)>0) {
                //Debug.trace("----STOP----");
                processor.stop();
            }

            // Wait until we are stopped
            //Debug.trace("----WAIT FOR STOP----");
            controlThread.waitForState(0,STARTED);

            // Clear the TIMESET state
            //Debug.trace("----CLEAR TIMESET----");
            controlThread.unsetState(TIMESET);

            // This is a workaround for a bug in the
            // JMF Processor implementation.  If we
            // stop, rewind to 0, play, then often
            // the call to setMediaTime will block and never
            // return.  If instead we stop, forward to end, play,
            // wait for the stream to end, then rewind to 0
            // then it works reliably.  Go figure.
            {
                Time duration = processor.getDuration();
                //Debug.trace("----GOTO END----");
                processor.setMediaTime(duration);      // goto end
                //Debug.trace("----WAIT FOR FFWD----");
                controlThread.waitForState(TIMESET);   // wait for ffwd
                //Debug.trace("----START----");
                processor.start();                     // play at end
                //Debug.trace("----WAIT FOR START----");
                controlThread.waitForState(STARTED);   // wait for play
                //Debug.trace("----WAIT FOR END----");
                controlThread.waitForState(0,STARTED); // wait for end
            }

            //Debug.trace("----REWIND----");
            processor.setMediaTime(new Time(0));       // rewind to start
            //Debug.trace("----WAIT FOR REWIND----");
            controlThread.waitForState(TIMESET);       // wait for rewind
            controlThread.setState(REWOUND);

            videoRenderer.enableCallbacks(true);       // enable start/stop callbacks
        }
    }

    /*
     * This action class sets the playback rate.
     * It should be called on a stopped processor.  It
     * will wait until the processor is realized.
     */
    public class SetRateAction implements Action {
        private float speed;
        /*
         * Constructor takes a new playout speed.
         */
        public SetRateAction(float newSpeed)
        {
            speed = newSpeed;
        }
        /**
         * Sets the playback rate on a stopped processor.
         */
        public void run()
        {
            //Debug.trace("----WAIT FOR REALIZED----");
            controlThread.waitForState(REALIZED);
            processor.setRate(speed);
        }
    }

    /**
     * This thread class consumes the action queue and
     * executes actions on the processor.
     */
    public class ControlThread extends Thread {

        /** queue of pending actions */
        private ArrayList jobQueue;

        private Object stateSemaphore;
        private static final int STATE_SEMAPHORE_TIMEOUT = 10000; // ten seconds

        /** thread exits when this thread is true */
        private boolean finish;


        /**
         * Constructor initializes the action queue and starts the thread.
         */
        public ControlThread()
        {
            //Debug.trace();
            jobQueue = new ArrayList();
            stateSemaphore = new Object();
            finish = false;
            this.start();
        }

        /**
         * Debugging code returns a string representation of a state field
         */
        public String stateString(int state)
        {
            String string = "[ ";
            if ((state & REALIZED)>0)
                string += "REALIZED ";
            if ((state & STARTED)>0)
                string += "STARTED ";
            if ((state & REWOUND)>0)
                string += "REWOUND ";
            if ((state & TIMESET)>0)
                string += "TIMESET ";
            string += "]";
            return string;
        }

        /**
         * Adds an action to the tail of the action queue, and
         * signals the thread.
         *
         * @param action - action to be performed
         */
        public void add(Action action)
        {
            //Debug.trace(action.toString());
            synchronized (jobQueue) {
                jobQueue.add(action);
                //Debug.trace("queue size="+jobQueue.size());
                jobQueue.notify();
            }
        }

        /**
         * Signals the control thread to shut down.  Pending actions
         * will be discarded.  This call returns immediately
         * without waiting for the thread to terminate.
         */
        public void finish()
        {
            //Debug.trace();
            synchronized (jobQueue) {
                finish = true;
                jobQueue.notify();
            }
        }

        /**
         * Entry point for the control thread.
         */
        public void run()
        {
            //Debug.trace();
            while (true) {
                //Debug.trace();
                Action action;
                synchronized (jobQueue) {
                    while (jobQueue.isEmpty()) {
                        if (finish) {
                            break;
                        } else {
                            try {
                                //Debug.trace();
                                jobQueue.wait();
                                //Debug.trace();
                            } catch (InterruptedException e) {
                                //Debug.trace(e.toString());
                            }
                        }
                    }
                    action = (Action)jobQueue.remove(0);
                }
                try {
                    //Debug.trace("Executing "+action.toString());
                    action.run();
                    //Debug.trace("Done executing "+action.toString());
                } catch (Exception e) {
                    //Debug.trace("Exception in action : "+e.toString());
                    e.printStackTrace();
                }
            }
        }

        /**
         * Short cut for waitForState(wantState,wantState).
         * Waits until the state bit is set and ignores all
         * other bits.
         */
        public boolean waitForState(int wantState)
        {
            return waitForState(wantState, wantState);
        }

        /**
         * Waits until (state & stateMask) == wantState.
         * Callers must ensure that stateMask is a superset
         * of wantState or the call will never be matched.
         * Also, to work around some of the JMF bugs, we
         * time out if the event doesn't happen within
         * STATE_SEMAPHORE_TIMEOUT milliseconds.  This is
         * the minimum time it will wait.  The actual delay
         * may be longer.
         *
         * @param wantState - bit mask for bits you want set
         * @param stateMask - bit mask for bits you want to check
         * @return true if the state was matched, false if it timed out.
         */
        public boolean waitForState(int wantState, int stateMask)
        {
            /* Debug.trace("Waiting."+
                        " want="+stateString(wantState)+
                        " mask="+stateString(stateMask)+
                        " have="+stateString(state));
*/
            long t0 = System.currentTimeMillis();
            boolean done = false;
            boolean timeout = false;

            while (!done) {
                // synchronization here is important.  We must hold
                // the stateSemaphore lock while we do the comparison
                // to avoid a race condition.  It will be implicitly
                // released during the wait.
                synchronized (stateSemaphore) {
                    //Debug.trace("Checking if "+stateString(state & stateMask)+
                    //            " matches "+stateString(wantState));
                    if ((state & stateMask) == wantState) {
                        // check for successful completion
                        done = true;
                    } else {
                        // no match, so we wait for a state change
                        //Debug.trace("Waiting for state="+stateString(wantState));
                        try {
                            stateSemaphore.wait(STATE_SEMAPHORE_TIMEOUT);
                            // bail if we sit here too long
                        } catch (InterruptedException e) {
                                //Debug.trace(e.toString());
                        }
                        // we may have awaked because of timeout or because
                        // the state bits changed.  Check here for the case
                        // where the timeout passed and the condition isn't met.
                        long elapsed = System.currentTimeMillis() - t0;
                        if ((elapsed > STATE_SEMAPHORE_TIMEOUT) &&
                            ((state & stateMask) != wantState)) {
                            done = true;
                            timeout = true;
                        }
                    }
                }
            }
            /* Debug.trace("Done waiting."+
                        " want="+stateString(wantState)+
                        " mask="+stateString(stateMask)+
                        " have="+stateString(state));
*/
            return !timeout;
        }

        /**
         * Called to set one or more state bits in the controlThread state field.
         * When the state changes, the controlThread will be notified if it
         * is waiting for a state change.
         *
         * @param stateBit - the bit (or bits) to be set.
         */
        public void setState(int stateBit)
        {
            synchronized (stateSemaphore) {
                state = state | stateBit;
                stateSemaphore.notify();
            }
        }

        /**
         * Called to clear one or more state bits in the controlThread state field.
         * When the state changes, the controlThread will be notified if it
         * is waiting for a state change.
         *
         * @param stateBit - the bit (or bits) to be cleared.
         */
        public void unsetState(int stateBit)
        {
            synchronized (stateSemaphore) {
                state = state & (ALLSTATES - stateBit);
                stateSemaphore.notify();
            }
        }

    }

    /** processor instance */
    private Processor processor = null;

    /** video renderer */
    private VideoRenderer videoRenderer;

    /** video handler which will be passed to the video renderer */
    private VideoStreamHandler videoHandler;

    /** current processor state as indicated by event callbacks */
    private int state = 0;

    /** instance of the internal class which queues and executes actions */
    private ControlThread controlThread;


    /**
     * Constructor for the MovieDecoder.
     * Call init() after construction with a URL.
     *
     * @param videoStreamHandler - handler to be called with each video frame.
     */
    public MovieDecoder(VideoStreamHandler videoStreamHandler)
    {
        videoHandler = videoStreamHandler;
        controlThread = new ControlThread();
    }

    /**
     * Initializes the decoder.  The file will be opened, decoding
     * will start, and the first frame of the movie will be sent to
     * the handlers.
     *
     * @param mediaFile - the URL of the media file to be loaded
     */
    public void init(String mediaFile)
    {
        try {
            java.net.URL url = new java.net.URL(mediaFile);
            processor = Manager.createProcessor(url);
            processor.addControllerListener(this);
            processor.configure();
        }
        catch (Exception e) {
            //Debug.trace("Movie player exception "+e);
        }
    }

    /**
     * Starts the processor running.  This call queues an asynchronous
     * request which may not be acted on immediately.
     */
    public void start()
    {
        controlThread.add(new StartAction());
    }

    /**
     * Stops the processor.  This call queues an asynchronous request
     * which may not be acted upon immediately
     */
    public void stop()
    {
        controlThread.add(new StopAction());
    }

    /**
     * Stops, Rewinds and Restarts the processor.  This call queues an
     * asynchronous request which may not be acted upon immediately
     */
    public void rewind()
    {
        controlThread.add(new RewindAction());
    }

    /**
     * Sets the speed (rate) of playout.  In theory negative
     * values are allowed, but may not be supported by the
     * player.
     * @param speed - playback rate scale factor.  1.0 is normal speed.
     */
    public void setRate(float speed)
    {
        controlThread.add(new SetRateAction(speed));
    }

    //----------------------------------------------------------------------
    // ControllerListener interface
    //----------------------------------------------------------------------

    /**
     * Callback for controller events.  This method is called by the
     * processor whenever state changes.
     *
     * @param event - describes the nature of the state change.
     */
    public synchronized void controllerUpdate(ControllerEvent event) {

        if (event instanceof ConfigureCompleteEvent) {
            //ConfigurationCompleteEvents come after we
            // call configure() and before we can call realize().
            // At this point we can query the controls to figure
            // out what tracks we have.
            //Debug.trace("ConfigureCompleteEvent");
            setHandlers();
            // set content descriptor to null
            // so that the processor can be used as a player
            processor.setContentDescriptor(null);
            // request transition to realized state
            processor.realize();

        } else if (event instanceof RealizeCompleteEvent) {
            // RealizeCompleteEvents come after we call realize() and
            // before we can call start() or do anything else interesting.
            //Debug.trace("RealizeCompleteEvent");
            controlThread.setState(REALIZED | REWOUND);

            // at this point we should be able to access duration
            // we convert it to the vrml convention (-1 for unknown)
            double vrmlDuration;
            Time duration = processor.getDuration();
            if (duration==Duration.DURATION_UNKNOWN ||
                duration==Duration.DURATION_UNBOUNDED) {
                vrmlDuration = -1.0;
            } else {
                vrmlDuration = duration.getSeconds();
            }
            videoHandler.videoStreamDuration(vrmlDuration);

            // finally we call prefetch will will send the first
            // frame to the video handler.
            processor.prefetch();

        } else if (event instanceof StartEvent) {
            controlThread.setState(STARTED);    // set STARTED
            controlThread.unsetState(REWOUND);  // clear REWOUND
        } else if (event instanceof StopEvent) {
            // includes EndOfMediaEvent, StopByRequestEvent
            controlThread.unsetState(STARTED);  //clear STARTED
        } else if (event instanceof MediaTimeSetEvent) {
            controlThread.setState(TIMESET);    // notify any waiting thread
        }
    }

    /**
     * Set appropriate handlers for the audio and video tracks.
     * We create our own handlers, which in turn will call the
     * handlers provided to us in the constructor of this class.
     */
    private void setHandlers()
    {
        TrackControl controls[] = processor.getTrackControls();
        int nControls = controls.length;
        int i;
        for (i=0;i<nControls;i++) {
            Format format = controls[i].getFormat();
            try {
                if (format instanceof VideoFormat) {
                    videoRenderer = new VideoRenderer(videoHandler);
                    controls[i].setRenderer(videoRenderer);
                } else if (format instanceof AudioFormat) {
                    controls[i].setRenderer(new AudioRenderer());
                } else {
                    System.err.println("Unknown track type");
                }
            } catch (UnsupportedPlugInException e) {
                System.err.println("Got exception "+e);
            }
        }
    }
}
