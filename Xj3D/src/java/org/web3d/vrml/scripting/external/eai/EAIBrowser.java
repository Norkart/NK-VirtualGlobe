/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.eai;

// Standard Java imports
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Map;

// VRML Specification imports
import vrml.eai.*;

import vrml.eai.event.BrowserEvent;
import vrml.eai.event.BrowserListener;
import vrml.eai.field.InvalidEventOutException;
import vrml.eai.field.InvalidEventInException;


// Application specific imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.InvalidNodeTypeException;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

/**
 * EAIBrowser implements the {@link vrml.eai.Browser} interface, largely by
 * translating and interfacing between the wrapper classes and the
 * implementation class represented by {org.web3d.vrml.scripting.CommonBrowser}.
 * <P>
 * To function correctly, EAIBrowser needs to be constructed using
 * CommonBrowser and BrowserCore instances.  The EAIBrowser then registers
 * as a listener so that BrowserCoreListener BrowserInitialized and
 * browserShutdown messages.  The BrowserCore instance is necessary mainly
 * for the global namespace and VRMLExecutionSpace information.
 *
 * @author Brad Vender
 * @version $Revision: 1.18 $
 */

public class EAIBrowser implements Browser, BrowserCoreListener {
    /** The event adapter factory.
      * The event adapter system is reachable through this object. */
    EAIEventAdapterFactory eventAdapterFactory;

    /** String for getBrowserVersion */
    static final String browserVersion="$Revision: 1.18 $";

    /** The list of browser listeners for browser events */
    Vector browserListeners;

    /** String for getBrowserName */
    static final String browserName="EAI Browser";

    /** The VRML code for the initial world state. */
    static final String INITIAL_WORLD=
        "#VRML V2.0 utf8\n"+
        "Group{}\n";

    /** The secondary implementation of the Browser*/
    BrowserCore theBrowserCore;

    /** The main implementation of the Browser */
    VRML97CommonBrowser theBrowserImpl;

    /** The reporting mechanism for messages. */
    ErrorReporter theErrorReporter;

    /** The queue to post events to.*/
    ExternalEventQueue theEventQueue;

    /** The VRMLNodeFactory for use in mapping between VRMLNodeType and
     *  vrml.eai.Node instances. */
    VRMLNodeFactory theNodeFactory;

    /** Construct an EAIBrowser for the given VrmlDisplayPanel
     *  @param browserCore The BrowserCore to use as the implementation.
     *  @param browserImpl The VRML97CommonBrowser to use.
     *  @param eventQueue The queue for sending events to the internals.
     *  @param reporter The ErrorReporter for messages and errors.
     */
    public EAIBrowser(BrowserCore browserCore,
                      VRML97CommonBrowser browserImpl,
                      ExternalEventQueue eventQueue,
                      ErrorReporter reporter) {

        if (browserCore==null)
            throw new IllegalArgumentException("Null BrowserCore");
        if (browserImpl==null)
            throw new IllegalArgumentException("Null CommonBrowser");

        theBrowserCore=browserCore;
        theBrowserImpl=browserImpl;
        theEventQueue=eventQueue;
        browserListeners=new Vector();
        browserCore.addCoreListener(this);
        // The null factory reference is corrected in the
        // NonMappingVRMLNodeFactory constructor.

        SimpleEAIFieldFactory fieldFactory =
            new SimpleEAIFieldFactory(theEventQueue);

        eventAdapterFactory=new BufferedMappingEAIEventAdapterFactory(2,browserCore.getVRMLClock());
        theNodeFactory=new NonMappingVRMLNodeFactory(fieldFactory);

        eventAdapterFactory.setFieldFactory(fieldFactory);

        fieldFactory.setNodeFactory(theNodeFactory);
        fieldFactory.setEAIEventAdapterFactory(eventAdapterFactory);

        if (reporter==null)
            theErrorReporter=DefaultErrorReporter.getDefaultReporter();
        else
            theErrorReporter=reporter;
    }

    //-------------------------------------------------------------------
    // Methods required by the vrml.eai.Browser interface
    //-------------------------------------------------------------------

    /** Returns the name of the Browser.
     *  @return The name of the Browser
     *  @see vrml.eai.Browser#getName
     */
    public String getName() throws vrml.eai.InvalidBrowserException {
        return browserName;
    }

    /** Returns the version string for this Browser.
     *  @return The version string for this Browser
     *  @see vrml.eai.Browser#getVersion
     */
    public String getVersion() throws InvalidBrowserException {
        return browserVersion;
    }

    /** @see vrml.eai.Browser#getCurrentSpeed */
    public float getCurrentSpeed() throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            return theBrowserImpl.getCurrentSpeed();
    }

    /** @see vrml.eai.Browser#getCurrentFrameRate */
    public float getCurrentFrameRate() throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            return theBrowserImpl.getCurrentFrameRate();
    }

    /** @see vrml.eai.Browser#getWorldURL */
    public String getWorldURL()
        throws InvalidBrowserException, URLUnavailableException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            return theBrowserImpl.getWorldURL();
    }

    /** @see vrml.eai.Browser#replaceWorld */
    public void replaceWorld(Node[] nodes)
        throws IllegalArgumentException, InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            /* Translate from vrml.eai.Node[] to VRMLNodeType[] and the call
             theBrowserImpl.replaceWorld().  Event broadcast and translation
             occurs elsewhere in response to that call. */
            VRMLNodeType vrmlNodes[]=null;
            if (nodes!=null) {
                vrmlNodes=new VRMLNodeType[nodes.length];
                int counter;
                for (counter=0; counter<nodes.length; counter++)
                    vrmlNodes[counter]=
                        theNodeFactory.getVRMLNode(nodes[counter]);
            }
            theEventQueue.postReplaceWorld(theBrowserImpl, vrmlNodes);
        }
    }

    /** @see vrml.eai.Browser#loadURL */
    public void loadURL(String[] urls, String[] params)
        throws InvalidBrowserException, InvalidURLException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            theEventQueue.postLoadURL(theBrowserImpl, urls,params);
    }

    /** @see vrml.eai.Browser#setDescription */
    public void setDescription(String newDescription)
        throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            theBrowserImpl.setDescription(newDescription);
    }

    /** @see vrml.eai.Browser#createVrmlFromString */
    public Node[] createVrmlFromString(String string)
        throws InvalidBrowserException, InvalidVrmlException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            Node result[]=null;
            /* Just translation of CommonBrowser's results to what we need */
            try {
                VRMLNodeType children[]=
                    theBrowserImpl.createVrmlFromString(string,null);
                if (children!=null) {
                    result=new Node[children.length];
                    int counter=0;
                    for (counter=0;counter<result.length; counter++)
                        result[counter]=
                            theNodeFactory.getEAINode(children[counter]);
                }
            } catch (VRMLParseException p) {
                throw new InvalidVrmlException(p.getMessage());
            } catch (InvalidNodeTypeException v) {
                //throw new RuntimeException(v.getMessage());
                throw new InvalidVrmlException(v.getMessage());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
            return result;
        }
    }

    /** @see vrml.eai.Browser#createVrmlFromURL */
    public void createVrmlFromURL(String[] urls,
                                  Node targetNode,
                                  String eventInName)
        throws InvalidBrowserException,
               InvalidNodeException,
               InvalidURLException,
               InvalidEventInException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            if (targetNode==null)
                throw new vrml.eai.InvalidNodeException("Node cannot be null.");
            VRMLNodeType actualNode=theNodeFactory.getVRMLNode(targetNode);
            if (actualNode==null)
                throw new vrml.eai.InvalidNodeException("Node was disposed.");
            theEventQueue.postCreateVrmlFromURL(
                theBrowserImpl, urls,actualNode,eventInName
            );
        }
    }

    /** @see vrml.eai.Browser#getNode */
    public Node getNode(String nodeName)
        throws InvalidNodeException,
               InvalidBrowserException,
               URLUnavailableException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            Map map = theBrowserCore.getDEFMappings();
            System.out.println("DEF Map Size: " + map.size());

            VRMLNodeType who=(VRMLNodeType)(
                theBrowserCore.getDEFMappings().get(nodeName)
            );
            if (who!=null)
                return theNodeFactory.getEAINode(who);
            else
                throw new vrml.eai.InvalidNodeException(nodeName+" not found");
        }
    }

    /** @see vrml.eai.Browser#addRoute */
    public void addRoute(Node fromNode,
                         String fromEventName,
                         Node toNode,
                         String toEventName)
        throws InvalidBrowserException,
               InvalidEventOutException,
               InvalidEventInException,
               InvalidNodeException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            VRMLNodeType actualFrom=theNodeFactory.getVRMLNode(fromNode);
            VRMLNodeType actualTo=theNodeFactory.getVRMLNode(toNode);
            theEventQueue.postAddRoute(
                theBrowserImpl, theBrowserCore, actualFrom,fromEventName,
                actualTo,toEventName
            );
        }
    }

    /** @see vrml.eai.Browser#deleteRoute */
    public void deleteRoute(Node fromNode,
                            String fromEventName,
                            Node toNode,
                            String toEventName)

        throws InvalidBrowserException,
               InvalidEventOutException,
               InvalidEventInException,
               InvalidNodeException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            VRMLNodeType actualFrom=theNodeFactory.getVRMLNode(fromNode);
            VRMLNodeType actualTo=theNodeFactory.getVRMLNode(toNode);
            theEventQueue.postRemoveRoute(
                theBrowserImpl, theBrowserCore, actualFrom,
                fromEventName,actualTo,toEventName
            );
        }
    }

    /** @see vrml.eai.Browser#beginUpdate */
    public void beginUpdate() throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            theEventQueue.beginUpdate();
    }

    /** @see vrml.eai.Browser#endUpdate */
    public void endUpdate() throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            theEventQueue.endUpdate();
    }

    /**
     * addBrowserListener adds the specified listener to the set of listeners
     * for this browser.
     * @param theListener The listener to add to the list of listeners for
                          this browser
     */
    public void addBrowserListener(BrowserListener theListener)
        throws InvalidBrowserException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            if ((theListener!=null)&&(!browserListeners.contains(theListener)))
                browserListeners.addElement(theListener);
        }
    }

    /**
     * removeBrowserListener removes the specified listener from the set of
     * listeners for this browser.
     * @param listener The listener to remove from the list.
     */
    public void removeBrowserListener(BrowserListener listener)
        throws InvalidBrowserException {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else
            browserListeners.removeElement(listener);
    }

    //---------------------------------------------------------
    // Methods required by the BrowserCoreListener interface
    //---------------------------------------------------------

    /** @see org.web3d.browser.BrowserCoreListener#browserInitialized */
    public void browserInitialized(VRMLScene newScene) {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.INITIALIZED));
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        theErrorReporter.messageReport("Received urlLoadFailed message:"+msg);
        broadcastEvent(new BrowserEvent(this, BrowserEvent.URL_ERROR));
    }

    /** @see org.web3d.browser.BrowserCoreListener#browserShutdown */
    public void browserShutdown() {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.SHUTDOWN));
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
    }

    //---------------------------------------------------------
    // Internal implementation methods
    //---------------------------------------------------------

    /**
     * Internal convenience routine for sending events to all listeners.
     * Not very efficient, but faithful to the wording of the spec.
     */
    void broadcastEvent(BrowserEvent e) {
        Enumeration en=browserListeners.elements();
        while (en.hasMoreElements()) {
            BrowserListener b=(BrowserListener)(en.nextElement());
            createNotifierThread(new EventNotifier(b,e)).start();
        }
    }

    /**
     * Internal convenience method for constructing new threads.
     * Will be removed when thread pool added later.
     */
    Thread createNotifierThread(Runnable r) {
        return (Thread)(
            AccessController.doPrivileged(
                new CreateThreadAction(r)
            )
        );
    }

    /**
     * Clean up and get rid of this browser.  When this method is called,
     * the event queue will be processed, the browser will shut down,
     * and any subsequent calls to browser methods (except getName() and
     * getVersion()) will result in InvalidBrowserException's being generated.
     * <P>
     * Since this version does not support sharing between clients, shutdown
     * also initiates the termination of the Xj3D to EAI event adapter
     * system.
     */
    public void dispose() {
        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            endUpdate();

            // Clear the current world
            try {
                theBrowserImpl.replaceWorld(
                    theBrowserImpl.createVrmlFromString(
                        INITIAL_WORLD, theBrowserCore.getWorldExecutionSpace()
                    )
                );
            } catch (Exception e) {
                theErrorReporter.fatalErrorReport("Error initializing system.",e);
            }

            theBrowserImpl=null;
            theBrowserCore=null;
            eventAdapterFactory.shutdown();
            do_shutdown();
        }
    }

    /** Perform the browser shutdown process */
    void do_shutdown() {
        broadcastEvent(new BrowserEvent(this,BrowserEvent.SHUTDOWN));
    }


    /** Initialize the world to a known and default state.
      * The initialization is done so that the event model and other
      * internals of the system will be functioning, and to prevent special
      * cases in the various event buffering routines.  Since the event
      * buffering routines are assumed to not be functioning, this method
      * calls down to the VRML97CommonBrowser directly. */
    public void initializeWorld() {
        // Alan: This does bad things, including generating multiple browser.INITIALIZED events
/*
        try {
            theBrowserImpl.replaceWorld(
                theBrowserImpl.createVrmlFromString(
                    INITIAL_WORLD, theBrowserCore.getWorldExecutionSpace()
                )
            );
        } catch (Exception e) {
            theErrorReporter.fatalErrorReport("Error initializing system.",e);
        }
*/
    }

    /**
     * EventNotifier is a simple runnable used to prevent event listeners from
     * slowing down the notification process, as per the spec.
     * An anonymous inner class was not practical for this purpose. */
    private class EventNotifier implements Runnable {

        /** Basic constructor.
          * @param aListener The BrowserListener to notify.
          * @param anEvent The event to notify of.
          */
        EventNotifier(BrowserListener aListener, BrowserEvent anEvent) {
            theListener=aListener;
            theEvent=anEvent;
        }

        /** The listener to notify */
        private BrowserListener theListener;

        /** The event to send */
        private BrowserEvent theEvent;

        /** Notify the listener.
          * @see java.lang.Thread#run */
        public void run() {
            theListener.browserChanged(theEvent);
        }
    }

    /** Convenience class for the security actions.
      * Just creates arbitary threads using the passed in Runnable's.
      *  */
    class CreateThreadAction implements PrivilegedAction {
        /** The runnable for the thread. */
        Runnable r;

        /** Create a new instance which will make a thread using
          * aRunnable as its Runnable instance.
          * @param aRunnable The runnable to use.
          */
        CreateThreadAction(Runnable aRunnable) {
            r=aRunnable;
        }

        /** Make the thread. */
        public Object run() {
            return new Thread(r);
        }
    }
}
