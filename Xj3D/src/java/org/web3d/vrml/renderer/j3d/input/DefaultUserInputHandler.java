/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.input;

// External imports
import javax.media.j3d.*;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.j3d.geom.GeometryData;
import org.j3d.renderer.java3d.navigation.NavigationProcessor;
import org.j3d.renderer.java3d.util.J3DIntersectionUtils;
import org.j3d.ui.navigation.NavigationState;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.ObjectArray;
import org.web3d.util.HashSet;
import org.xj3d.device.TrackerState;
import org.xj3d.device.ButtonModeConstants;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.vrml.nodes.VRMLCollidableNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;

/**
 * A complete handler for all user input information within a scene.
 * <p>
 *
 * The handler takes care of all the handling needed for sensors, anchors,
 * navigation and keyboard. However, it does not define a way of sourcing
 * those events as it assumes that a user will either delegate or extend this
 * class with more specific information such as an AWT listener or Java3D
 * behavior.
 * <p>
 *
 * The current key handling does not allow keyboard navigation of the world.
 * It passes all key events directly through to the current key sensor if one
 * is registered.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.46 $
 */
public class DefaultUserInputHandler
    implements J3DUserInputHandler,
               NavigationInfoChangeListener {

    /** The dimension (radius of the pick shape) */
    private static final double PICK_SIZE = 0.1;

    /** The internal state is nothing at the moment */
    private static final int NO_STATE = 0;

    /** The internal state is we're in navigation */
    private static final int NAVIGATION_STATE = 1;

    /** The internal state is we're on an anchor */
    private static final int ANCHOR_STATE = 2;

    /** The internal state is we're on a touch/drag sensor */
    private static final int TOUCH_STATE = 3;

    /** The default navigation mode for ANY */
    private static final String DEFAULT_NAV_STRING = "WALK";
    private static final int DEFAULT_NAV_INT = NavigationState.WALK_STATE;

    /** The set of supported view types and their int values */
    private static final HashMap navigationTypes;

    /** The current state that we're in while processing events */
    private int currentState;

    /** The current state of each tracker */
    private int[] trackerState;

    /** Has ANY been selected as a NavMode */
    private boolean anyAllowed;

    /** Delegate to the j3d.org navigation handler for navigation */
    private NavigationProcessor navHandler;

    /** The current navigation info we are running with */
    private VRMLNavigationInfoNodeType currentNavInfo;

    /** The current viewpoint we are running with */
    private VRMLViewpointNodeType currentViewpoint;

    /** The list of navigation state listener */
    private ArrayList navigationStateListeners;

    /** The list of sensorStatus listeners */
    private ArrayList sensorStatusListeners;

    // Class vars for performance so we don't need to keep re-allocating

    /** The shape we use for mouse picking */
    private PickRay mousePickShape;

    /** Representation of the direction that we pick in */
    private Vector3d mousePickDirection;

    /** Representation of the eye in the world */
    private Point3d mouseEyePosition;

    /** The eye position in the image plate (canvas) world coords */
    private Point3d mousePosition;

    /** The mouse position in local coordinates of the sensor */
    private Point3d mouseSensorPosition;

    /** The Transform3D to read the view VWorld coordinates */
    private Transform3D viewTransform;

    /** The transform group of the current viewpoint used to set the picking */
    private TransformGroup viewTg;

    /** The branchgroup to do the picking against */
    private BranchGroup pickableWorld;

    /** The system clock */
    private VRMLClock clock;

    /**
     * Flag indicating that we should do picking. We only pick if we have a
     * world branchgroup, canvas and view transform set.
     */
    private boolean doPicking;

    /** The class used to perform exact intersection */
    private J3DIntersectionUtils iutils;

    // Keepers of the nodes between calls to the mouse. Usually only one of
    // these are set at a time.

    /** The clicked anchor */
    private VRMLLinkNodeType currentLink;

    /**
     * The scene graph path of the node that is currently being processed
     * when the mouse button is down.
     */
    private SceneGraphPath currentPickedPath;

    /**
     * The index into the currentScenePickeddPath of the node that was
     * selected from the path and that contains the sensors.
     */
    private int currentPickedNodeIndex;

    /** Flag to say the point device list contains at least one touch sensor */
    private boolean hasTouchSensor;

    /** Flag to say the point device list contains at least one drag sensor */
    private boolean hasDragSensor;

    /** The current listener for link nodes being selected */
    private CollisionListener collisionListener;

    /** Points that we use for working calculations (coord transforms) */
    private Point3d wkPoint;
    private Point3d pointTrans;

    /** A Transform3d that we use for working calculations*/
    private Transform3D wkTrans;

    /** Collection of the last touch sensors that were activated */
    private HashSet activeSensors;

    /** A temporary working variable for the active sensors */
    private HashSet workingSensors;

    /** The touch sensors active at this time */
    private ObjectArray activeTouchSensors;

    /** The drag sensors active at this time */
    private ObjectArray activeDragSensors;

    /** Working var for reading the VWorld transform of the sensors' group */
    private Transform3D sensorTransform;

    /**
     * The transform down to the parent group that the drag sensor is located
     * in. Remains current while drag is in process.
     */
    private Transform3D dragParentTransform;

    /** Working var for when asking for the VWorld transform */
    private SceneGraphPath sensorPath;

    /** The current hit point in the sensor's coordinate system */
    private float[] sensorHitPoint;

    /** The current normal in the geometry coordinate system */
    private float[] sensorHitNormal;

    /**
     * The current texture coordinate in the geometry coordinate system.
     * The array will have 3 indexes so that it will cope with 3D textures
     * if and when VRML gets them. For the moment, the code only works with
     * 2D textures and the third index will always be zero.
     */
    private float[] sensorHitTexCoord;

    /** Temporary array for fetching the sensors */
    private Object[] tmpSensors;

    /**
     * Vector for doing difference calculations on the point we have and the
     * next while doing terrian following.
     */
    private Vector3d diffVec;

    /** Flag to say if navigation handling should be disabled */
    private boolean navigationEnabled;

    /** Where did navigation start in 3D space */
    private float[] navStart;

    private float[] tmpVec;

    /** The navigation mode active for a tracker */
    private int navMode[];

    /** The list of navigation modes defined by the currently bound NavInfo */
    private int boundNavModes[];

    /** The list of currently valid navigation types */
    private String[] nav_types;

    /** The number of navigation modes in nav_types */
    private int num_nav_types;

    /** The last state of each button */
    private boolean[] lastButtonState;

    /** The previous navigation mode before a lookat */
    private int prevNavMode;

    /** The previous nav mode index */
    private int prevNavModeIdx;

    /** Should we test for pointing device interaction */
    private boolean testPointingDevices;

    /**
     * Static constructor to build the supported navigation type maps.
     */
    static {
        navigationTypes = new HashMap();

        navigationTypes.put(Xj3DConstants.EXAMINE_NAV_MODE,
                            new Integer(NavigationState.EXAMINE_STATE));
        navigationTypes.put(Xj3DConstants.WALK_NAV_MODE,
                            new Integer(NavigationState.WALK_STATE));
        navigationTypes.put(Xj3DConstants.FLY_NAV_MODE,
                            new Integer(NavigationState.FLY_STATE));
        navigationTypes.put(Xj3DConstants.NONE_NAV_MODE,
                            new Integer(NavigationState.NO_STATE));
        navigationTypes.put(Xj3DConstants.LOOKAT_NAV_MODE,
                            new Integer(NavigationState.LOOKAT_STATE));
        navigationTypes.put(Xj3DConstants.PAN_NAV_MODE,
                            new Integer(NavigationState.PAN_STATE));
        navigationTypes.put(Xj3DConstants.TILT_NAV_MODE,
                            new Integer(NavigationState.TILT_STATE));

        // The ANY state is handled separately.
    }

    /**
     * Create a new instance of this class so that we can process events and
     * send them into the VRML scene. The initial state is set to none.
     */
    public DefaultUserInputHandler() {
        currentState = NO_STATE;
        doPicking = false;
        testPointingDevices = true;

        mousePickDirection = new Vector3d(0, 0, -1);
        mouseEyePosition = new Point3d();
        mousePosition = new Point3d();
        mouseSensorPosition = new Point3d();

        mousePickShape = new PickRay();
        viewTransform = new Transform3D();

        navHandler = new NavigationProcessor();

        activeSensors = new HashSet();
        workingSensors = new HashSet();

        activeDragSensors = new ObjectArray();
        activeTouchSensors = new ObjectArray();

        dragParentTransform = new Transform3D();

        sensorTransform = new Transform3D();
        sensorPath = new SceneGraphPath();

        sensorHitPoint = new float[3];
        sensorHitNormal = new float[3];
        sensorHitTexCoord = new float[3];

        // Some arbitrary size. Are we likely to have more active than this?
        tmpSensors = new Object[10];

        hasTouchSensor = false;
        hasDragSensor = false;

        iutils = new J3DIntersectionUtils();
        pointTrans = new Point3d();
        wkPoint = new Point3d();
        wkTrans = new Transform3D();
        diffVec = new Vector3d();
        navStart = new float[3];
        tmpVec = new float[3];

        // TODO: Need to get the number of devices
        navMode = new int[16];
        lastButtonState = new boolean[8*16];

        // Some arbitrary size.  Content max is unlimited.
        boundNavModes = new int[8];

        nav_types = new String[0];
        navigationStateListeners = new ArrayList(1);
        sensorStatusListeners = new ArrayList(1);

        // By default we get no notification so ANY is allowed
        anyAllowed = true;
    }

    //----------------------------------------------------------
    // Methods defined by NavigationInfoChangeLister
    //----------------------------------------------------------

    /**
     * Notification that the navigation modes allowed has changed
     * on the current NavigationInfo node.
     *
     * @param newModes The new allowed navigation modes
     * @param numValid number of valid modes in array
     */
    public void notifyNavigationModesChanged(String[] newModes, int numValid) {
        nav_types = newModes;
        num_nav_types = numValid;
        boolean state_found = false;
        boolean has_any = false;
        String type;
        int state = 0;

        Integer mode;
        fireNavigationListChanged(nav_types, num_nav_types);
        if (num_nav_types > 0) {
            if (nav_types[0].equals("ANY")) {
                setNavigationMode(DEFAULT_NAV_STRING);
            } else {
                setNavigationMode(nav_types[0]);
            }
        }

        anyAllowed = false;
        for (int i = 0; i < num_nav_types; i++) {
            type = nav_types[i];
            mode = (Integer) navigationTypes.get(type);
            if (mode != null) {
                boundNavModes[i] = mode.intValue();
            } else {
                if (type.equals("ANY")) {
                    boundNavModes[i] = DEFAULT_NAV_INT;
                    anyAllowed = true;
                } else {
                    System.out.println("Unknown navigation mode: " + type);
                    boundNavModes[i] = NavigationState.NO_STATE;
                }
            }
        }

        navHandler.setNavigationState(boundNavModes[0]);
    }

    /**
     * Notification that the avatar size has changed
     * on the current NavigationInfo node.
     *
     * @param size The size parameters for the avatar
     * @param numValid number of valid modes in array
     */
    public void notifyAvatarSizeChanged(float[] size, int numValid) {
        if (numValid!=3)
            System.err.println("Unsupported number of avatar dimensions");
        else
            navHandler.setAvatarInfo(size[1], size[0], size[2]);

    }

    /**
     * Notification that the navigation speed has changed on the
     * current NavigationInfo node.
     *
     * @param newSpeed The new navigation speed.
     */
    public void notifyNavigationSpeedChanged(float newSpeed) {
        navHandler.setNavigationSpeed(newSpeed);
    }

    /**
     * Notification that the visibility limit has been changed.
     *
     * @param distance The new distance value to use
     */
    public void notifyVisibilityLimitChanged(float distance) {
        // do nothing for this.
    }

    /**
     * Notification that headlight state has changed.
     *
     * @param enable true if the headlight should now be on
     */
    public void notifyHeadlightChanged(boolean enable) {
        // do nothing for this.
    }


    //----------------------------------------------------------
    // Methods defined by J3DUserInputHandler
    //----------------------------------------------------------

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled() {
        return navigationEnabled;
    }

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state) {
        navigationEnabled = state;
    }

    /**
     * Process a collection of mouse events. The events must be in
     * chronological order of reception so that we make sure we process mouse
     * positional information correctly
     *
     * @param events An array of event objects
     * @param valid The number of valid entries in the array
     */
/*
    public void processMouseEvents(MouseEvent[] events, int valid) {

        // So look through the list for the first mouse up. Use this to
        // basically stop whatever we are currently doing. Anything after
        // that is ignored. If we find no mouse up, find the first mouse
        // down for the button press. Check to see if there is a corresponding
        // up and decide on some behaviour. Finally, if it is none of these,
        // just take the last item in the array and use it as a mouse move or
        // drag.
        boolean answer_found = false;
        MouseEvent evt;

        if(valid > events.length)
            valid = events.length;

        for(int i = 0; i < valid && !answer_found; i++) {
            evt = events[i];

            switch(evt.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                    // Only interested in a press if nothing else
                    // currently active. Keep going to see if we got a mouse
                    // up to make it a "click" in this frame
                    if(currentState == NO_STATE) {
                        mousePressed(evt);
                        answer_found = true;
                    }
                    break;

                case MouseEvent.MOUSE_RELEASED:
                    mouseReleased(evt);
                    answer_found = true;
                    break;

                case MouseEvent.MOUSE_CLICKED:
                    if(currentState == NO_STATE) {
                        mouseClicked(evt);
                        answer_found = true;
                    }
                    break;

                default:
                    // do nothing, it was a move, enter or exit.
            }
        }

        if(valid != 0 && !answer_found) {
            // well, just grab the last event
            evt = events[valid - 1];
            switch(evt.getID()) {
                case MouseEvent.MOUSE_DRAGGED:
                    mouseDragged(evt);
                    break;

                case MouseEvent.MOUSE_MOVED:
                    mouseMoved(evt);
                    break;

                default:
                    // do nothing
            }
        }

        if(!navigationEnabled && (currentState == NAVIGATION_STATE))
            navHandler.processNextFrame();
    }
*/
    /**
     * Set the world branchgroup that we are doing the picking on. This allows
     * us to look for the picked items in the scene. A null value is used to
     * clear the world and disable picking behaviours.
     *
     * @param scene The scene to pick against
     */
    public void setPickableScene(BranchGroup scene) {
        pickableWorld = scene;

        doPicking = (pickableWorld != null) && (viewTg != null);

        navHandler.setWorldInfo(scene, scene);
    }

    /**
     * Set the view and it's related transform group to use. This view is what
     * we navigation around the scene with.
     *
     * @param view is the View object that we're modifying.
     * @param tg The transform group above the view object that should be used
     * @param path The path from the root to here, or null
     */
    public void setViewInfo(View view,
                            TransformGroup tg,
                            SceneGraphPath path) {
        viewTg = tg;
        navHandler.setViewInfo(view, tg, path);

        doPicking = (pickableWorld != null) && (viewTg != null);
    }

    /**
     * Change the currently set scene graph path for the world root to this new
     * path without changing the rest of the view setup. Null will clear the
     * current path set.
     *
     * @param path The new path to use for the viewpoint
     */
    public void setViewPath(SceneGraphPath path) {
        navHandler.setViewPath(path);
    }

    /**
     * Set the center of rotation explicitly to this place. Coordinates must
     * be in the coordinate space of the current view transform group. The
     * provided array must be of least length 3. Center of rotation is used
     * in examine mode.
     *
     * @param center The new center to use
     */
    public void setCenterOfRotation(float[] center) {
        navHandler.setCenterOfRotation(center, null);
    }

    /**
     * Set the clock we are going to operate from when generating events. A
     * null value will remove the clock.
     *
     * @param clk The new clock to use
     */
    public void setVRMLClock(VRMLClock clk) {
        clock = clk;
    }

    /**
     * Set the listener for collision notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setCollisionListener(CollisionListener l) {
        if(l == null)
            navHandler.setCollisionListener(null);
        else
            navHandler.setCollisionListener(new CollisionListenerAdapter(l));
    }

    /**
     * Set the viewpoint that is used for this scene.
     *
     * @param viewpoint The new viewpoint information.
     */
    public void setViewpoint(VRMLViewpointNodeType viewpoint) {
        currentViewpoint = viewpoint;
    }

    /**
     * Set the navigation info that is used for this scene. The canvas can
     * then use it to build any user interface interactions it desires. A
     * value of null will remove the info and is typically used when the
     * canvas is about to be removed from a universe.
     *
     * @param navInfo The new navigation information to be used
     */
    public void setNavigationInfo(VRMLNavigationInfoNodeType navInfo) {
        if (currentNavInfo!=null)
            currentNavInfo.setNavigationChangedListener(null);
        navInfo.setNavigationChangedListener(this);
        currentNavInfo = navInfo;
/*
        if(navInfo == null) {
            navHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                           NavigationState.NO_STATE);
            navHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                           NavigationState.NO_STATE);
            navHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                           NavigationState.NO_STATE);

            return;
        }
*/
        // Set the speed values. Currently not handling dynamic changes in
        // speed values.
        float[] avatar = navInfo.getAvatarSize();
        float speed = navInfo.getSpeed();

        navHandler.setAvatarInfo(avatar[1], avatar[0], avatar[2]);
        navHandler.setNavigationSpeed(speed);

        nav_types = navInfo.getType();
        num_nav_types = navInfo.getNumTypes();
        boolean state_found = false;
        boolean has_any = false;
        String type;
        int state = 0;

        Integer mode;

        fireNavigationListChanged(nav_types, num_nav_types);

        if (num_nav_types > 0) {
            if (nav_types[0].equals(Xj3DConstants.ANY_NAV_MODE)) {
                setNavigationMode(DEFAULT_NAV_STRING);
            } else {
                setNavigationMode(nav_types[0]);
            }
        }

        anyAllowed = false;
        for(int i = 0; i < num_nav_types; i++) {
            type = nav_types[i];
            mode = (Integer)navigationTypes.get(type);
            if (mode != null) {
                boundNavModes[i] = mode.intValue();
            }
            else {
                if (type.equals(Xj3DConstants.ANY_NAV_MODE)) {
                    boundNavModes[i] = DEFAULT_NAV_INT;
                    anyAllowed = true;
                } else {
                    System.out.println("Unknown navigation mode: " + type);
                    boundNavModes[i] = NavigationState.NO_STATE;
                }
            }
        }

        navHandler.setNavigationState(boundNavModes[0]);
    }

    /**
     * Process the buttons on a tracker.  No other state will be read.
     *
     * @param tracker The tracker generating the event.
     * @param state The current state.
     */
    public void trackerButton(int tracker, TrackerState state) {
        processButtons(tracker,state);
    }

    /**
     * Process a tracker press event. This may be used to start a TouchSensor
     * start of a drag sensor or navigation
     *
     * @param evt The event that caused the method to be called
     */
    public void trackerPressed(int tracker, TrackerState evt) {
        // If picking is enabled, do the whole thing, otherwise just do
        // navigation.

        if(doPicking) {
            SceneGraphPath path = null;

            // Look for a button that is picking, if none then don't pick
            boolean pickerFound = false;

            for(int i=0; i < evt.numButtons; i++) {
                if (evt.pickingEnabled[i] && evt.buttonState[i]) {
                    pickerFound = true;
                    break;
                }
            }

            if (pickerFound && testPointingDevices)
                path = doPick(evt);

            if(path == null) {
                // Nothing picked so must be navigation
                navStart[0] = evt.devicePos[0];
                navStart[1] = evt.devicePos[1];
                navStart[2] = evt.devicePos[2];

                setNavMode(tracker,evt);
                if (navMode[tracker] == NavigationState.LOOKAT_STATE) {
                    updateCenterOfRotation(evt);
                    currentState = NAVIGATION_STATE;
                    navHandler.startMove();
                } else if (navMode[tracker] != NavigationState.NO_STATE) {
                    currentState = NAVIGATION_STATE;
                    navHandler.startMove();
                } else {
                    currentState = NO_STATE;
                }
            } else {

                Object user_data;
                int num_nodes = path.nodeCount();
                SceneGraphObject node;
                boolean object_found = false;

                for(int i = num_nodes; --i >= 0; ) {
                    node = path.getNode(i);
                    if(processNodePress(node)) {
                        object_found = true;
                        currentPickedPath = path;
                        currentPickedNodeIndex = i;

                        break;
                    }
                }

                // Did we find an object? If not, then let's navigate!
                if(!object_found) {
                    navStart[0] = evt.devicePos[0];
                    navStart[1] = evt.devicePos[1];
                    navStart[2] = evt.devicePos[2];

                    setNavMode(tracker,evt);

                    if (navMode[tracker] == NavigationState.LOOKAT_STATE) {
                        updateCenterOfRotation(evt);
                        currentState = NAVIGATION_STATE;
                        navHandler.startMove();
                    } else if (navMode[tracker] != NavigationState.NO_STATE) {
                        currentState = NAVIGATION_STATE;
                        navHandler.startMove();
                    } else {
                        currentState = NO_STATE;
                    }
                } else {

                    // process the node list
                    int size = activeTouchSensors.size();

                    if(size != 0) {
                        VRMLTouchSensorNodeType touch;
                        double time = clock.getTime();

                        for(int i = 0; i < size; i++) {
                            touch = (VRMLTouchSensorNodeType)activeTouchSensors.get(i);
                            touch.notifyPressed(1,
                                                time,
                                                sensorHitPoint,
                                                sensorHitNormal,
                                                sensorHitTexCoord);
                            fireDeviceActivated(touch);
                        }
                    }

                    size = activeDragSensors.size();
                    if(size != 0) {

                        VRMLDragSensorNodeType drag;
                        calcSensorGroupVWorld(currentPickedPath,
                                              currentPickedNodeIndex,
                                              dragParentTransform);
                        dragParentTransform.invert();

                        // convert the mouse coordinate from world space
                        // to the local coordinates of the sensor. Since we're
                        // finished with the sensorHitNormal, let's just "borrow"
                        // that array to do the conversion to the local coordinate
                        // system here.
                        dragParentTransform.transform(mousePosition, wkPoint);

                        sensorHitNormal[0] = (float)wkPoint.x;
                        sensorHitNormal[1] = (float)wkPoint.y;
                        sensorHitNormal[2] = (float)wkPoint.z;

                        wkPoint.x = sensorHitPoint[0];
                        wkPoint.y = sensorHitPoint[1];
                        wkPoint.z = sensorHitPoint[2];

                        dragParentTransform.transform(wkPoint, pointTrans);

                        sensorHitPoint[0] = (float)pointTrans.x;
                        sensorHitPoint[1] = (float)pointTrans.y;
                        sensorHitPoint[2] = (float)pointTrans.z;

                        for(int i = 0; i < size; i++) {
                            drag = (VRMLDragSensorNodeType)activeDragSensors.get(i);
                            drag.notifySensorDragStart(sensorHitPoint,
                                                       sensorHitNormal);
                            fireDeviceActivated(drag);
                        }
                    }
                }
            }
        } else {
            navStart[0] = evt.devicePos[0];
            navStart[1] = evt.devicePos[1];
            navStart[2] = evt.devicePos[2];
            setNavMode(tracker,evt);

            if (navMode[tracker] == NavigationState.LOOKAT_STATE) {
                updateCenterOfRotation(evt);
                currentState = NAVIGATION_STATE;
                navHandler.startMove();
            } else if (navMode[tracker] != NavigationState.NO_STATE) {
                currentState = NAVIGATION_STATE;
                navHandler.startMove();
            } else {
                currentState = NO_STATE;
            }
        }
    }

    /**
     * Should pointing devices be tested for.
     *
     * @param enabled Test for intersection when true
     */
    public void setTestPointingDevices(boolean enabled) {
        testPointingDevices = enabled;
    }

    /**
     * Process a tracker moved event.
     *
     * @param evt The event that caused the method to be called
     */
    public void trackerMoved(int tracker, TrackerState evt) {

        if(!doPicking)
            return;

        SceneGraphPath path = null;

        if (testPointingDevices)
            path = doPick(evt);

        Object sens;

        // Clean up any not found sensors
        if(path == null) {
            int size = activeSensors.size();
            if(size != 0) {
                // this will automatically resize if needed
                tmpSensors =
                    (Object[])activeSensors.toArray(tmpSensors);

                for(int i = 0; i < size; i++) {
                    sens = tmpSensors[i];

                    if(sens instanceof VRMLPointingDeviceSensorNodeType)
                        ((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);

                    fireDeviceNotOver(sens);
                }

                activeSensors.clear();
            }

            return;
        }

        int node_count = path.nodeCount();
        boolean sensor_found = false;
        VRMLLinkNodeType link;
        boolean ts_fired = false;

        for(int i = node_count - 1; i >= 0; i--) {
            Node n = path.getNode(i);

            J3DUserData u_data = (J3DUserData)n.getUserData();


            if(u_data == null)
                continue;

            if (u_data.linkReference != null) {
                link = u_data.linkReference;

//                if (link.getEnable()) {
                    if(activeSensors.contains(link))
                        activeSensors.remove(link);
                    else {
                        fireDeviceOver(link);
                    }
                    activeSensors.add(link);
                    sensor_found = true;
//                }
            }

            if (u_data.sensors == null)
                continue;

            // If we have sensors look for a touch sensor. If we find one
            // then finish all the rest and break out of the loop
            // Transform the hit point, which was in world coordinates down
            // to the local coordinates of the sensor.
            pointTrans.x = sensorHitPoint[0];
            pointTrans.y = sensorHitPoint[1];
            pointTrans.z = sensorHitPoint[2];

            calcHitPoint(path, i, pointTrans);

            sensorHitPoint[0] = (float)pointTrans.x;
            sensorHitPoint[1] = (float)pointTrans.y;
            sensorHitPoint[2] = (float)pointTrans.z;

            for(int j = 0; j < u_data.sensors.length; j++) {

                sens = u_data.sensors[j];

                // Just add to list for selectable notification
                if (sens instanceof VRMLDragSensorNodeType) {
                    workingSensors.add(sens);

                    if(activeSensors.contains(sens))
                        activeSensors.remove(sens);
                    else {
                        ((VRMLDragSensorNodeType)sens).setIsOver(true);
                        fireDeviceOver(sens);
                    }

                    sensor_found = true;
                    continue;
                }

                if (!(sens instanceof VRMLTouchSensorNodeType) || !((VRMLTouchSensorNodeType)sens).getEnabled())
                    continue;

                sensor_found = true;

                if (!ts_fired) {
                    // If it is active already, take if from this set and
                    // place it in the working set. Only update the position
                    // information.
                    VRMLTouchSensorNodeType ts = (VRMLTouchSensorNodeType)sens;

                    if(activeSensors.contains(sens))
                        activeSensors.remove(sens);
                    else {
                        ts.setIsOver(true);

                        fireDeviceOver(ts);
                    }
                    ts.notifyHitChanged(sensorHitPoint,
                                        sensorHitNormal,
                                        sensorHitTexCoord);

                    ts_fired = true;

                    workingSensors.add(ts);
                }
            }

            // Any left in activeSensors are no longer active so
            // issue and isOver(false); This should also clear the
            // active set as a by-product.
            int size = activeSensors.size();
            if(size != 0) {
                // this will automatically resize if needed
                tmpSensors = (Object[])activeSensors.toArray(tmpSensors);

                for(int k = 0; k < size; k++) {
                    sens = tmpSensors[k];

                    if(sens instanceof VRMLPointingDeviceSensorNodeType)
                        ((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);

                    fireDeviceNotOver(sens);
                }

                activeSensors.clear();
            }

            activeSensors.addAll(workingSensors);
            workingSensors.clear();
        }

        // If we didn't find any sensors at all, but there could be some left
        // over that were active last frame. If so, they aren't active any
        // longer, so we should clear then out and set isOver to false.
        int size = activeSensors.size();
        if(!sensor_found && size != 0) {
            // this will automatically resize if needed

            tmpSensors = (Object[])activeSensors.toArray(tmpSensors);
            for(int i = 0; i < size; i++) {
                sens = tmpSensors[i];

                if(sens instanceof VRMLPointingDeviceSensorNodeType)
                    ((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);

                fireDeviceNotOver(sens);
            }

            activeSensors.clear();
        }
    }

    /**
     * Process a tracker press event. This may be used to start a touchtracker,
     * start of a drag tracker or navigation
     *
     * @param tracker The id of the tracker that was released
     * @param state The state of the tracker right now
     */
    public void trackerDragged(int tracker, TrackerState state) {
        float x_dif = navStart[0] - state.devicePos[0];
        float y_dif = state.devicePos[1] - navStart[1];

        switch(currentState) {
            case NAVIGATION_STATE:
                switch(navMode[tracker]) {
                    case NavigationState.WALK_STATE:
                        tmpVec[0] = x_dif;
                        tmpVec[1] = 0;
                        tmpVec[2] = y_dif;
                        break;
                    case NavigationState.PAN_STATE:
/*
                        tmpVec[0] = -x_dif;
                        tmpVec[1] = -y_dif;
                        tmpVec[2] = 0;

*/
                        tmpVec[0] = -x_dif;
                        tmpVec[1] = -y_dif;
                        tmpVec[2] = 0;

                        break;
                    case NavigationState.TILT_STATE:
                        tmpVec[0] = x_dif;
                        tmpVec[1] = -y_dif;
                        tmpVec[2] = 0;
                        break;
                    case NavigationState.FLY_STATE:
                        tmpVec[0] = x_dif;
                        tmpVec[1] = 0;
                        tmpVec[2] = y_dif;
                        break;
                    case NavigationState.EXAMINE_STATE:
                        tmpVec[0] = x_dif;
                        tmpVec[1] = 0;
                        tmpVec[2] = y_dif;
                        break;
                }

                float scale = (float) Math.sqrt(tmpVec[0] * tmpVec[0] +
                   tmpVec[1] * tmpVec[1] + tmpVec[2] * tmpVec[2]);

                if (scale != 0) {
                    tmpVec[0] /= scale;
                    tmpVec[1] /= scale;
                    tmpVec[2] /= scale;
                } else {
                    tmpVec[0] = 0;
                    tmpVec[1] = 1;
                    tmpVec[2] = 0;
                }

                navHandler.move(tmpVec, scale);
                break;

            case ANCHOR_STATE:
                // do nothing
                break;

            case TOUCH_STATE:
                // go through the list of active pointing device sensors and
                // tell them all about it.
                // Transform the hit point, which was in world coordinates down
                int size = activeTouchSensors.size();
                if(size != 0) {

                    VRMLTouchSensorNodeType touchy;

System.out.println("Warning: does not update dragged mouse correctly for " +
                   "TouchSensors. Must fix");

                    for(int i = 0; i < size; i++) {
                        touchy = (VRMLTouchSensorNodeType)activeTouchSensors.get(i);

                        touchy.notifyHitChanged(sensorHitPoint,
                                                sensorHitNormal,
                                                sensorHitTexCoord);
                    }
                }

                size = activeDragSensors.size();
                if(size != 0) {

                    VRMLDragSensorNodeType drag;

                    // convert the mouse coordinate from world space
                    // to the local coordinates of the sensor.
                    mouseEyePosition.x = state.worldPos[0];
                    mouseEyePosition.y = state.worldPos[1];
                    mouseEyePosition.z = state.worldPos[2];
                    mousePickDirection.x = state.worldOri[0];
                    mousePickDirection.y = state.worldOri[1];
                    mousePickDirection.z = state.worldOri[2];

                    mousePickShape.set(mouseEyePosition, mousePickDirection);

                    dragParentTransform.transform(mouseEyePosition, wkPoint);
                    dragParentTransform.transform(mousePickDirection, diffVec);

                    sensorHitPoint[0] = (float)wkPoint.x;
                    sensorHitPoint[1] = (float)wkPoint.y;
                    sensorHitPoint[2] = (float)wkPoint.z;

                    sensorHitNormal[0] = (float)diffVec.x;
                    sensorHitNormal[1] = (float)diffVec.y;
                    sensorHitNormal[2] = (float)diffVec.z;

                    for(int i = 0; i < size; i++) {
                        drag = (VRMLDragSensorNodeType)activeDragSensors.get(i);
                        drag.notifySensorDragChange(sensorHitPoint,
                                                    sensorHitNormal);
                    }
                }
        }

    }

    /**
     * Process a tracker press event. This may be used to start a touchtracker,
     * start of a drag tracker or navigation
     *
     * @param tracker The id of the tracker that was released
     * @param state The state of the tracker right now
     */
    public void trackerReleased(int tracker, TrackerState state) {

        int i, size;

        switch(currentState) {
            case NAVIGATION_STATE:
                navHandler.stopMove();

                if (navMode[tracker] == NavigationState.LOOKAT_STATE) {
                    fireNavigationStateChanged(prevNavModeIdx);

                    navHandler.setNavigationState(prevNavMode);
                    boundNavModes[0] = prevNavMode;
                }

                break;

            case ANCHOR_STATE:
                fireLinkActivated(currentLink);

                currentLink = null;
                break;

            case TOUCH_STATE:
                size = activeTouchSensors.size();
                if(size != 0) {

                    VRMLTouchSensorNodeType touch;
                    double time = clock.getTime();

System.out.println("Warning: does not update released mouse correctly for " +
                   "TouchSensors. Must fix");

                    for(i = 0; i < size; i++) {
                        touch = (VRMLTouchSensorNodeType)activeTouchSensors.get(i);

                        touch.notifyReleased(1,
                                             time,
                                             sensorHitPoint,
                                             sensorHitNormal,
                                             sensorHitTexCoord);
                    }
                }

                size = activeDragSensors.size();
                if(size != 0) {
                    VRMLDragSensorNodeType drag;

                    mousePosition.x = state.worldPos[0];
                    mousePosition.y = state.worldPos[1];
                    mousePosition.z = state.worldPos[2];
                    mousePickDirection.x = state.worldOri[0];
                    mousePickDirection.y = state.worldOri[1];
                    mousePickDirection.z = state.worldOri[2];

                    dragParentTransform.transform(mousePosition, wkPoint);
                    dragParentTransform.transform(mousePickDirection, diffVec);

                    sensorHitPoint[0] = (float)wkPoint.x;
                    sensorHitPoint[1] = (float)wkPoint.y;
                    sensorHitPoint[2] = (float)wkPoint.z;

                    sensorHitNormal[0] = (float)diffVec.x;
                    sensorHitNormal[1] = (float)diffVec.y;
                    sensorHitNormal[2] = (float)diffVec.z;

                    for(i = 0; i < size; i++) {
                        drag = (VRMLDragSensorNodeType)activeDragSensors.get(i);
                        drag.notifySensorDragEnd(sensorHitPoint,
                                                 sensorHitNormal);
                    }
                }

                currentPickedPath = null;
                currentPickedNodeIndex = -1;

                activeTouchSensors.clear();
                activeDragSensors.clear();

                hasDragSensor = false;
                hasTouchSensor = false;
                break;
        }

        currentState = NO_STATE;
    }

    /**
     * Process a tracker click event. The click is used only on touch trackers
     * and anchors. We treat it like a cross between a select and unselect.
     *
     * @param tracker The id of the tracker that was released
     * @param state The state of the tracker right now
     */
    public void trackerClicked(int tracker, TrackerState state) {
        if(!doPicking)
            return;

        trackerPressed(tracker, state);
        trackerReleased(tracker, state);

        currentState = NO_STATE;
    }

    /**
     * Process tracker orientation events.
     *
     * @param tracker Which tracker sourced the event
     * @param state The current state
     */
    public void trackerOrientation(int tracker, TrackerState state) {
        navHandler.orient(state.deviceOri);
    }

    /**
     * Process any navigation velocity.  Call every frame while a drag
     * is active.
     */
    public void processNavigation() {
        if(navigationEnabled && (currentState == NAVIGATION_STATE))
            navHandler.processNextFrame();
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addNavigationStateListener(NavigationStateListener l) {
        if((l != null) && !navigationStateListeners.contains(l))
            navigationStateListeners.add(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeNavigationStateListener(NavigationStateListener l) {
        navigationStateListeners.remove(l);
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addSensorStatusListener(SensorStatusListener l) {
        if((l != null) && !sensorStatusListeners.contains(l))
            sensorStatusListeners.add(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeSensorStatusListener(SensorStatusListener l) {
        sensorStatusListeners.remove(l);
    }

    /**
     * Get the current user position.
     *
     * @param pos The position vector to fill in
     */
    public void getPosition(Vector3f pos) {
        // TODO: unimplemented
    }

    /**
     * Get the current user orientation.
     *
     * @param ori The orientation vector to fill in
     */
    public void getOrientation(AxisAngle4f ori) {
        // TODO: unimplemented
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    public void setWorldScale(float scale) {
        // TODO: unimplemented
    }

    /**
     * Set the desired navigation mode.
     *
     * @param mode The requested mode.
     * @return Whether the mode is valid.
     */
    public boolean setNavigationMode(String mode) {
        boolean found = false;
        boolean found_any = false;

        int i=0;

        for(i=0; i < nav_types.length; i++) {
            if (nav_types[i].equals(mode)) {
                found = true;
                break;
            } else if (nav_types[i].equals("ANY")) {
                found_any = true;
            }
        }

        if (!found && found_any) {
            // Add the nav mode to the list
            String[] new_types = new String[num_nav_types + 1];
            for(i=0; i < num_nav_types; i++) {
                new_types[i] = nav_types[i];
            }

            new_types[i] = mode;
            fireNavigationListChanged(new_types,new_types.length);

            found = true;

            nav_types = new_types;
        }

        if (found) {
            fireNavigationStateChanged(i);

            Integer typeConst;

            typeConst = (Integer)navigationTypes.get(mode);

            if (typeConst != null) {
                int val = typeConst.intValue();
                navHandler.setNavigationState(val);

                if (val == NavigationState.LOOKAT_STATE) {
                    prevNavMode = boundNavModes[0];
                } else {
                    prevNavModeIdx = i;
                }

                boundNavModes[0] = val;

            } else if (found_any) {
                navHandler.setNavigationState(DEFAULT_NAV_INT);
            }
        }

        return found;
    }

    /**
     * Convenience method to set up picking for the current mouse position.
     * Picks the objects in the scene based on the mouse coordinates. The
     * final coordinates are left in the sensorHitPoint class variable
     * (in local coordinates).
     *
     * @param x The mouse X position in screen coordinates
     * @param y The mouse Y position in screen coordinates
     * @param canvas The canvas to work from
     * @return The closest picked object, Null if none
     */
    private SceneGraphPath doPick(TrackerState state) {

        mouseEyePosition.x = state.worldPos[0];
        mouseEyePosition.y = state.worldPos[1];
        mouseEyePosition.z = state.worldPos[2];
        mousePickDirection.x = state.worldOri[0];
        mousePickDirection.y = state.worldOri[1];
        mousePickDirection.z = state.worldOri[2];

//System.out.println("mouseEye: " + mouseEyePosition);
//System.out.println("mousePick: " + mousePickDirection);

        mousePickShape.set(mouseEyePosition, mousePickDirection);

        // Find the bounding reps that intersect the mouse ray
        SceneGraphPath[] path = pickableWorld.pickAllSorted(mousePickShape);

        if(path == null)
            return null;

//System.out.println("PickCount: " + path.length);
        // Use exact geometry intersection to find the "one true"
        Node node;
        Transform3D local_tx;
        Enumeration geom_list;
        GeometryArray geom;
        double shortest_length = -1;
        int shortest = -1;

        for(int i=0; i < path.length; i++) {
            node = path[i].getObject();

            if(node instanceof Shape3D) {
                local_tx = path[i].getTransform();

                try {
                    geom_list = ((Shape3D)node).getAllGeometries();
                } catch (CapabilityNotSetException cnse) {
                    // Ignore
                    continue;
                }

                while(geom_list.hasMoreElements()) {
                    geom = (GeometryArray)geom_list.nextElement();

                    J3DUserData u_data = (J3DUserData)geom.getUserData();

                    boolean found = false;
                    if((u_data != null) && (u_data.geometryData != null) &&
                       (u_data.geometryData instanceof GeometryData))
                    {
                        GeometryData gd = (GeometryData)u_data.geometryData;

                        found = iutils.rayUnknownGeometry(mouseEyePosition,
                                                          mousePickDirection,
                                                          0,
                                                          gd,
                                                          local_tx,
                                                          wkPoint,
                                                          false);
                    } else {
                        found = iutils.rayUnknownGeometry(mouseEyePosition,
                                                          mousePickDirection,
                                                          0,
                                                          geom,
                                                          local_tx,
                                                          wkPoint,
                                                          false);
                    }

                    if(found) {
                        diffVec.sub(mouseEyePosition, wkPoint);

                        if((shortest_length == -1) ||
                           (diffVec.lengthSquared() < shortest_length)) {
                            shortest = i;
                            shortest_length = diffVec.lengthSquared();
                            sensorHitPoint[0] = (float)wkPoint.x;
                            sensorHitPoint[1] = (float)wkPoint.y;
                            sensorHitPoint[2] = (float)wkPoint.z;
                        }
                    }
                }
            }
        }

        if(shortest == -1)
            return null;
        else
            return path[shortest];
    }

    /**
     * Convenience method to process the user data into a node. If the node
     * is a sensor and not enabled it will be ignored.
     *
     * @param data The scenegraph object to process
     * @return true if the node was successfully used for VRML purposes
     */
    private boolean processNodePress(SceneGraphObject node) {
        Object ud = node.getUserData();

        if((ud == null) || !(ud instanceof J3DUserData)) {
            return false;
        }

        boolean ret_val = false;
        double time = clock.getTime();
        J3DUserData user_data = (J3DUserData)ud;

        if(user_data.linkReference != null) {
            currentLink = user_data.linkReference;
            currentState = ANCHOR_STATE;
            ret_val = true;
        }

        if(user_data.sensors != null) {
            VRMLPointingDeviceSensorNodeType[] sensors = user_data.sensors;
            VRMLTouchSensorNodeType touch;
            VRMLDragSensorNodeType drag;

            for(int i = 0; i < sensors.length; i++) {

                boolean enabled = sensors[i].getEnabled();

                if(!enabled)
                    continue;

                if(sensors[i] instanceof VRMLTouchSensorNodeType) {
                    touch = (VRMLTouchSensorNodeType)sensors[i];
                    activeTouchSensors.add(touch);
                    hasTouchSensor = true;
                } else {
                    drag = (VRMLDragSensorNodeType)sensors[i];
                    activeDragSensors.add(drag);
                    hasDragSensor = true;
                }
            }

            if(hasTouchSensor || hasDragSensor) {
                currentState = TOUCH_STATE;
                ret_val = true;
            }
        }

        return ret_val;
    }

    /**
     * Convenience method to determine the current transformation down to
     * the sensor at the indicated position in the path. The result is left
     * in the passed Transform3D instance.
     */
    private void calcSensorGroupVWorld(SceneGraphPath path,
                                       int sensorPos,
                                       Transform3D transform) {

        // Optimisation: Frequently the VRML scene has the Shape and sensor
        // in the same group - eg
        //
        // children [
        //   Shape {}
        //   TouchSensor {}
        // ]
        //
        // This will save us one set of translations. We assume that the
        // Shape3D is not reported as part of the pick path and so if the
        // all the VRML nodes in the path between the base node and the
        // sensors do not contain transforms then don't bother with the second
        // transformation of the node to sensor coordinates, just pass the
        // values straight through.
        int num_nodes = path.nodeCount();
        boolean has_transforms = false;

        for(int i = num_nodes - 1; (i >= sensorPos) && !has_transforms; i--) {
            Node n = path.getNode(i);

            J3DUserData u_data = (J3DUserData)n.getUserData();
            if (u_data != null)
                has_transforms = u_data.isTransform;
        }

        if(!has_transforms) {
            transform.setIdentity();
            return;
        }

        // Ok, so we have to do some work now and convert the data back to
        // sensor coordinates.  first build the new path from the old one as
        // we need to get the vworld coordinates and that requires passing in
        // a SceneGraphPath because we'll have links/sharedGroups all over the
        // place.
        Node sensor_node = path.getNode(sensorPos);
        Node[] new_nodes = new Node[sensorPos];

        // Need -1 here because the sensorPos node now becomes the valid
        // object at the end of the transform.
        //
        // We also need a flag to deal with shared groups. The SceneGraphPath
        // variant of getLocalToVworld() can only be called when the path
        // contains Link object instances. Otherwise, we have to use the
        // shorter version without the path information! ARRRGGH.
        boolean has_links = false;

        for(int i = 0; i < sensorPos; i++) {
            Node n = path.getNode(i);
            if(n instanceof Link)
                has_links = true;

            new_nodes[i] = n;
        }

        // Determine if this touchSensor is in a transform as this is not accounted
        // for in the local_vworld
        boolean has_transform = false;
        Node n = path.getNode(sensorPos);

        if(n instanceof TransformGroup) {
            has_transform = true;
            ((TransformGroup)n).getTransform(wkTrans);
        }

        if(has_links) {
            sensorPath.setLocale(path.getLocale());
            sensorPath.setNodes(new_nodes);
            sensorPath.setObject(sensor_node);

            sensor_node.getLocalToVworld(sensorPath, transform);
        } else {
            sensor_node.getLocalToVworld(transform);
        }

        if(has_transform)
            transform.mul(transform, wkTrans);
    }

    /**
     * Convenience method to calculate the hit point on the geometry in the
     * sensor coordinate system. The results are left in the class global
     * vars sensorHitPoint. It already assumes that the hit point data is in
     * sensorHitPoint and just modifies it if needed. The texCoord and normals
     * are not modified IAW spec.
     *
     *
     * @param path The path to use as the calculation path
     * @param sensorPath The index of the sensor to use in that path
     * @param worldPos The point to be transformed from world coordinates
     */
    private void calcHitPoint(SceneGraphPath path,
                              int sensorPos,
                              Point3d worldPos) {


        calcSensorGroupVWorld(path, sensorPos, sensorTransform);

        sensorTransform.invert();
        sensorTransform.transform(worldPos);
    }

    /**
     * Set the navigation mode.
     */
    private void setNavMode(int tracker, TrackerState state) {
        int len;
        boolean found;

        for(int i=0; i < state.buttonState.length; i++) {
            if (state.buttonState[i]) {

                switch(state.buttonMode[i]) {
                    case ButtonModeConstants.NAV1:
                        navMode[tracker] = boundNavModes[0];
                        break;
                    case ButtonModeConstants.NAV2:
                        navMode[tracker] = boundNavModes[1];
                        break;
                    case ButtonModeConstants.NAV3:
                        navMode[tracker] = boundNavModes[2];
                        break;
                }
                if (!anyAllowed) {
                    len = boundNavModes.length;
                    found = false;

                    for(int j=0; j < len; j++) {
                        if (state.buttonMode[i] == ButtonModeConstants.WALK &&
                            boundNavModes[j] == NavigationState.WALK_STATE) {
                            found = true;
                            break;
                        }

                        if (state.buttonMode[i] == ButtonModeConstants.FLY &&
                            boundNavModes[j] == NavigationState.FLY_STATE) {
                            found = true;
                            break;
                        }

                        if (state.buttonMode[i] == ButtonModeConstants.PAN &&
                            boundNavModes[j] == NavigationState.PAN_STATE) {
                            found = true;
                            break;
                        }

                        if (state.buttonMode[i] == ButtonModeConstants.EXAMINE &&
                            boundNavModes[j] == NavigationState.EXAMINE_STATE) {
                            found = true;
                            break;
                        }

                    }

                    if (!found) {
                        navMode[tracker] = NavigationState.NO_STATE;
                        continue;
                    }
                }

                switch(state.buttonMode[i]) {
                    case ButtonModeConstants.WALK:
                        navMode[tracker] = NavigationState.WALK_STATE;
                        break;
                    case ButtonModeConstants.FLY:
                        navMode[tracker] = NavigationState.FLY_STATE;
                        break;
                    case ButtonModeConstants.PAN:
                        navMode[tracker] = NavigationState.PAN_STATE;
                        break;
                    case ButtonModeConstants.TILT:
                        navMode[tracker] = NavigationState.TILT_STATE;
                        break;
                    case ButtonModeConstants.NOTHING:
                        navMode[tracker] = NavigationState.NO_STATE;
                        break;
                }
                break;
            }
        }
        navHandler.setNavigationState(navMode[tracker]);
    }

    private void processButtons(int tracker, TrackerState state) {
        int pos;

        for(int i=0; i < state.buttonState.length; i++) {
            pos = tracker * 8 + i;
            if (state.buttonState[i] == true && state.buttonState[i] != lastButtonState[pos]) {

                switch(state.buttonMode[i]) {
                    case ButtonModeConstants.VIEWPOINT_NEXT:
                        System.out.println("GOTO Viewpoint next");
                        break;
                }

                lastButtonState[pos] = state.buttonState[i];
            }
        }
    }

    /**
     * Fire a navigation state changed event.
     */
    private void fireNavigationStateChanged(int idx) {
        int size = navigationStateListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                NavigationStateListener l =
                    (NavigationStateListener)navigationStateListeners.get(i);

                l.navigationStateChanged(idx);
            } catch(Exception e) {
                System.out.println("Error sending navigation state changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a navigation list changed event.
     * @param numTypes The number of navigation types
     * @param types The navigation types
     */
    private void fireNavigationListChanged(String[] types, int numTypes) {
        int size = navigationStateListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                NavigationStateListener l =
                    (NavigationStateListener)navigationStateListeners.get(i);

                l.navigationListChanged(types,numTypes);
            } catch(Exception e) {
                System.out.println("Error sending navigation list changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a deviceOver event.
     *
     * @param sensor The sensor
     */
    private void fireDeviceOver(Object sensor) {
        int type = 0;
        String desc;

        if (sensor instanceof VRMLTouchSensorNodeType) {
            type = SensorStatusListener.TYPE_TOUCH_SENSOR;
            desc = ((VRMLTouchSensorNodeType) sensor).getDescription();
        } else if (sensor instanceof VRMLDragSensorNodeType) {
            type = SensorStatusListener.TYPE_DRAG_SENSOR;
            desc = ((VRMLDragSensorNodeType) sensor).getDescription();
        } else if (sensor instanceof VRMLLinkNodeType) {
            type = SensorStatusListener.TYPE_ANCHOR;
            desc = ((VRMLLinkNodeType) sensor).getDescription();
        } else {
            System.out.println("Unhandled sensor type in fireDeviceOver: " + sensor);
            return;
        }

        int size = sensorStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                SensorStatusListener l =
                    (SensorStatusListener)sensorStatusListeners.get(i);

                l.deviceOver(type, desc);
            } catch(Exception e) {
                System.out.println("Error sending navigation list changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a deviceNotOver event.
     *
     * @param sensor The sensor
     */
    private void fireDeviceNotOver(Object sensor) {
        int type = 0;

        if (sensor instanceof VRMLTouchSensorNodeType) {
            type = SensorStatusListener.TYPE_TOUCH_SENSOR;
        } else if (sensor instanceof VRMLDragSensorNodeType) {
            type = SensorStatusListener.TYPE_DRAG_SENSOR;
        } else if (sensor instanceof VRMLLinkNodeType) {
            type = SensorStatusListener.TYPE_ANCHOR;
        } else {
            System.out.println("Unhandled sensor type in fireDeviceNotOver: " + sensor);
            return;
        }

        int size = sensorStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                SensorStatusListener l =
                    (SensorStatusListener)sensorStatusListeners.get(i);

                l.deviceNotOver(type);
            } catch(Exception e) {
                System.out.println("Error sending navigation list changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a deviceActivated event.
     *
     * @param sensor The sensor
     */
    private void fireDeviceActivated(Object sensor) {
        int type = 0;

        if (sensor instanceof VRMLTouchSensorNodeType) {
            type = SensorStatusListener.TYPE_TOUCH_SENSOR;
        } else if (sensor instanceof VRMLDragSensorNodeType) {
            type = SensorStatusListener.TYPE_DRAG_SENSOR;
        } else if (sensor instanceof VRMLLinkNodeType) {
            type = SensorStatusListener.TYPE_ANCHOR;
        } else {
            System.out.println("Unhandled sensor type in fireDeviceActivated: " + sensor);
            return;
        }

        int size = sensorStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                SensorStatusListener l =
                    (SensorStatusListener)sensorStatusListeners.get(i);

                l.deviceActivated(type);
            } catch(Exception e) {
                System.out.println("Error sending navigation list changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a linkActivated event.
     *
     * @param sensor The sensor
     */
    private void fireLinkActivated(VRMLLinkNodeType link) {
        String[] url = link.getUrl();

        int size = sensorStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                SensorStatusListener l =
                    (SensorStatusListener)sensorStatusListeners.get(i);

                l.linkActivated(url);
            } catch(Exception e) {
                System.out.println("Error sending navigation list changed " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the center of rotation based on the current click.
     *
     * evt The tracker state to use
     */
    private void updateCenterOfRotation(TrackerState evt) {

        mouseEyePosition.x = evt.worldPos[0];
        mouseEyePosition.y = evt.worldPos[1];
        mouseEyePosition.z = evt.worldPos[2];
        mousePickDirection.x = evt.worldOri[0];
        mousePickDirection.y = evt.worldOri[1];
        mousePickDirection.z = evt.worldOri[2];

//System.out.println("mouseEye: " + mouseEyePosition);
//System.out.println("mousePick: " + mousePickDirection);

        mousePickShape.set(mouseEyePosition, mousePickDirection);

        // Find the bounding reps that intersect the mouse ray
        SceneGraphPath[] path = pickableWorld.pickAllSorted(mousePickShape);

        if(path == null)
            return;

//System.out.println("PickCount: " + path.length);
        // Use exact geometry intersection to find the "one true"
        Node node;
        Transform3D local_tx;
        Enumeration geom_list;
        GeometryArray geom;
        double shortest_length = -1;
        int shortest = -1;

        for(int i=0; i < path.length; i++) {
            node = path[i].getObject();

            if(node instanceof Shape3D) {
                local_tx = path[i].getTransform();

                try {
                    geom_list = ((Shape3D)node).getAllGeometries();
                } catch (CapabilityNotSetException cnse) {
                    // Ignore
                    continue;
                }

                while(geom_list.hasMoreElements()) {
                    geom = (GeometryArray)geom_list.nextElement();

                    J3DUserData u_data = (J3DUserData)geom.getUserData();

                    boolean found = false;
                    if((u_data != null) && (u_data.geometryData != null) &&
                       (u_data.geometryData instanceof GeometryData))
                    {
                        GeometryData gd = (GeometryData)u_data.geometryData;

                        found = iutils.rayUnknownGeometry(mouseEyePosition,
                                                          mousePickDirection,
                                                          0,
                                                          gd,
                                                          local_tx,
                                                          wkPoint,
                                                          false);
                    } else {
                        found = iutils.rayUnknownGeometry(mouseEyePosition,
                                                          mousePickDirection,
                                                          0,
                                                          geom,
                                                          local_tx,
                                                          wkPoint,
                                                          false);
                    }

                    if(found) {
                        diffVec.sub(mouseEyePosition, wkPoint);

                        if((shortest_length == -1) ||
                           (diffVec.lengthSquared() < shortest_length)) {
                            shortest = i;
                            shortest_length = diffVec.lengthSquared();
                            sensorHitPoint[0] = (float)wkPoint.x;
                            sensorHitPoint[1] = (float)wkPoint.y;
                            sensorHitPoint[2] = (float)wkPoint.z;
                        }
                    }
                }
            }
        }

        if(shortest == -1)
            return;

        node = path[shortest].getObject();

        Transform3D transform = path[shortest].getTransform();

        Bounds bounds = node.getBounds();
        bounds.transform(transform);

        float[] center = new float[3];
        if (bounds instanceof BoundingBox) {
            Point3d lower = new Point3d();
            Point3d upper = new Point3d();
            BoundingBox bbox = (BoundingBox) bounds;

            bbox.getLower(lower);
            bbox.getUpper(upper);

            center[0] = (float) ((lower.x + upper.x) / 2);
            center[1] = (float) ((lower.y + upper.y) / 2);
            center[2] = (float) ((lower.z + upper.z) / 2);
        } else if (bounds instanceof BoundingSphere) {
            BoundingSphere bsphere = (BoundingSphere) bounds;

            Point3d c = new Point3d();
            bsphere.getCenter(c);
            center[0] = (float) c.x;
            center[1] = (float) c.y;
            center[2] = (float) c.z;
        } else {
            // Bah, can't find center just set to sensorHitPoint
            center[0] = sensorHitPoint[0];
            center[1] = sensorHitPoint[1];
            center[2] = sensorHitPoint[2];
        }

        Point3d centerPnt = new Point3d((double)center[0], (double) center[1], (double) center[2]);
        Point3d hitPnt = new Point3d((double)sensorHitPoint[0], (double)sensorHitPoint[1], (double)sensorHitPoint[2]);

        transform.transform(centerPnt);

        Vector3d userPos = new Vector3d();
        navHandler.getPosition(userPos);
        Vector3d dir = new Vector3d();

        dir.sub(hitPnt, userPos);

        userPos.x = userPos.x + 0.5 * dir.x;
        userPos.y = userPos.y + 0.5 * dir.y;
        userPos.z = userPos.z + 0.5 * dir.z;

        center[0] = (float) centerPnt.x;
        center[1] = (float) centerPnt.y;
        center[2] = (float) centerPnt.z;

        navHandler.setCenterOfRotation(center, new float[] {(float)userPos.x, (float)userPos.y, (float)userPos.z});
    }
}
