/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.input;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.geom.GeometryData;
import org.j3d.renderer.aviatrix3d.util.AVIntersectionUtils;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.util.MatrixUtils;

import org.j3d.renderer.aviatrix3d.geom.Text2D;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.ObjectArray;
import org.web3d.util.HashSet;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;
import org.xj3d.device.TrackerState;
import org.xj3d.device.ButtonModeConstants;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.vrml.nodes.VRMLCollidableNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

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
 * @author Justin Couch
 * @version $Revision: 1.48 $
 */
public class DefaultUserInputHandler
	implements OGLUserInputHandler, NavigationInfoChangeListener {
	
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
	private static final String DEFAULT_NAV_STRING = "EXAMINE";
	
	/** The default navigation constant */
	private static final int DEFAULT_NAV_INT = NavigationState.EXAMINE_STATE;
	
	/** The set of supported view types and their int values */
	private static final HashMap navigationTypes;
	
	/** The current state that we're in while processing events */
	private int currentState;
	
	/** The current state of each tracker */
	private int[] trackerState;
	
	/** The last state of each button */
	private boolean[] lastButtonState;
	
	/** Has ANY been selected as a NavMode */
	private boolean anyAllowed;
	
	/** Delegate to the j3d.org navigation handler for navigation */
	private NavigationProcessor navHandler;
	
	/** The current navigation info we are running with */
	private VRMLNavigationInfoNodeType currentNavInfo;
	
	// Class vars for performance so we don't need to keep re-allocating
	
	/** Representation of the direction that we pick in */
	private Vector3f mousePickDirection;
	
	/** Representation of the eye in the world */
	private Point3f mouseEyePosition;
	
	/** The eye position in the image plate (canvas) world coords */
	private Point3f mousePosition;
	
	/** The mouse position in local coordinates of the sensor */
	private Point3f mouseSensorPosition;
	
	/** The Matrix4f to read the view VWorld coordinates */
	private Matrix4f viewTransform;
	
	/** The current viewpoint used to set the picking */
	private SceneGraphPath viewPath;
	
	/** The branchgroup to do the picking against */
	private Group pickableWorld;
	
	/** The system clock */
	private VRMLClock clock;
	
	/**
	 * Flag indicating that we should do picking. We only pick if we have a
	 * world branchgroup, canvas and view transform set.
	 */
	private boolean doPicking;
	
	/** The class used to perform exact intersection */
	private AVIntersectionUtils iutils;
	
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
	
	/** The list of navigation state listener */
	private ArrayList navigationStateListeners;
	
	/** The list of sensorStatus listeners */
	private ArrayList sensorStatusListeners;
	
	/** The current world position this frame */
	private Vector3f currentPosition;
	
	/** Points that we use for working calculations (coord transforms) */
	private Point3f wkPoint;
	private Point3f pointTrans;
	private Vector3f normalTrans;
	
	/** A Transform3d that we use for working calculations*/
	private Matrix4f wkTrans;
	
	/** A vector3f that we use for working calculations */
	private Vector3f wkVec;
	
	/** Collection of the last touch sensors that were activated */
	private HashSet activeSensors;
	
	/** A temporary working variable for the active sensors */
	private HashSet workingSensors;
	
	/** The touch sensors active at this time */
	private ObjectArray activeTouchSensors;
	
	/** The drag sensors active at this time */
	private ObjectArray activeDragSensors;
	
	/** Working var for reading the VWorld transform of the sensors' group */
	private Matrix4f sensorTransform;
	
	/**
	 * The transform down to the parent group that the drag sensor is located
	 * in. Remains current while drag is in process.
	 */
	private Matrix4f dragParentTransform;
	
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
	private Vector3f diffVec;
	
	/** Flag to say if navigation handling should be disabled */
	private boolean navigationEnabled;
	
	/** Where did navigation start in 3D space */
	private float[] navStart;
	
	/** Temporary float arrays for working with */
	private float[] tmpVec1;
	private float[] tmpVec2;
	private float[] tmpVec3;
	
	/** The navigation mode active for a tracker */
	private int[] navMode;
	
	/** The list of navigation modes defined by the currently bound NavInfo */
	private int[] boundNavModes;
	
	/** The list of currently valid navigation types */
	private String[] navTypes;
	
	/** The number of valid elements in navTypes */
	private int numNavTypes;
	
	/**
	 * The currently active navigation mode index. This is the index into the
	 * current navTypes array of the mode that is selected based on the current
	 * user input method. This changes based on the tracker's button that is
	 * selected etc. There can be only one.
	 */
	private int currentNavTypeIndex;
	
	/** Pick request object for terrain */
	private PickRequest rayPicker;
	
	/** Utilities for doing matrix functions */
	private MatrixUtils matrixUtils;
	
	/** The previous navigation mode before a lookat */
	private int prevNavMode;
	
	/** The previous nav mode index */
	private int prevNavModeIdx;
	
	/** Should we test for pointing device interaction */
	private boolean testPointingDevices;
	
	/** Did the last tracker activate a pointing device sensor */
	private boolean trackerIntersect;
	
	/** Should we activate Pointing Device Sensors */
	private boolean activateSensors;
	
	/** Flag indicating that tracker wheel support should be enabled */
	private boolean wheelEnabled;
	
	/** Flag indicating that tracker wheel has been adjusted */
	private boolean wheelMoved;
	
	/** Index of the currently active navigation tracker */
	private int activeNavigationTracker;
	
	/** Flag indicating that the nav mode is track examine, 
	*  with the shift modifier active */
	private boolean trackExamineWithShiftMod;
	
	/** Flag indicating that a drag operation while in navigation mode is in progress */
	private boolean navigationDragInProgress;
	
	/** Flag indicating the last known state of the alt modifier key */
	private boolean localAltModifier;
	
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
		navigationTypes.put(Xj3DConstants.INSPECT_NAV_MODE,
			new Integer(Xj3DNavigationState.INSPECT_STATE));
		navigationTypes.put(Xj3DConstants.TRACK_EXAMINE_NAV_MODE,
			new Integer(Xj3DNavigationState.TRACK_EXAMINE_STATE));
		navigationTypes.put(Xj3DConstants.TRACK_PAN_NAV_MODE,
			new Integer(Xj3DNavigationState.TRACK_PAN_STATE));
		
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
		
		matrixUtils = new MatrixUtils();
		mousePickDirection = new Vector3f(0, 0, -1);
		mouseEyePosition = new Point3f();
		mousePosition = new Point3f();
		mouseSensorPosition = new Point3f();
		
		viewTransform = new Matrix4f();
		
		rayPicker = new PickRequest();
		rayPicker.pickType = PickRequest.FIND_GENERAL;
		rayPicker.pickGeometryType = PickRequest.PICK_RAY;
		rayPicker.pickSortType = PickRequest.SORT_ALL;
		rayPicker.generateVWorldMatrix = true;
		rayPicker.foundPaths = new ArrayList();
		
		navHandler = new NavigationProcessor();
		
		activeSensors = new HashSet();
		workingSensors = new HashSet();
		
		activeDragSensors = new ObjectArray();
		activeTouchSensors = new ObjectArray();
		
		dragParentTransform = new Matrix4f();
		
		sensorTransform = new Matrix4f();
		
		sensorHitPoint = new float[3];
		sensorHitNormal = new float[3];
		sensorHitTexCoord = new float[3];
		
		// Some arbitrary size. Are we likely to have more active than this?
		tmpSensors = new Object[10];
		
		hasTouchSensor = false;
		hasDragSensor = false;
		
		iutils = new AVIntersectionUtils();
		pointTrans = new Point3f();
		normalTrans = new Vector3f();
		wkPoint = new Point3f();
		wkTrans = new Matrix4f();
		wkVec = new Vector3f();
		currentPosition = new Vector3f();
		diffVec = new Vector3f();
		navStart = new float[3];
		tmpVec1 = new float[3];
		tmpVec2 = new float[3];
		tmpVec3 = new float[3];
		
		// TODO: Need to get the number of devices
		navMode = new int[16];
		for (int i = 0; i < navMode.length; i++) {
			navMode[i] = -1;
		}
		lastButtonState = new boolean[8*16];
		
		// Some arbitrary size.  Content max is unlimited.
		boundNavModes = new int[8];
		
		navTypes = new String[0];
		navigationStateListeners = new ArrayList(1);
		sensorStatusListeners = new ArrayList(1);
		
		// By default we get no notification so ANY is allowed
		anyAllowed = true;
		
		// no navigation active yet.
		activeNavigationTracker = -1;
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
		navTypes = newModes;
		numNavTypes = numValid;
		boolean state_found = false;
		boolean has_any = false;
		String type;
		int state = 0;
		
		Integer mode;
		fireNavigationListChanged(navTypes, numNavTypes);
		if (numNavTypes > 0) {
			if (navTypes[0].equals("ANY")) {
				setNavigationMode(DEFAULT_NAV_STRING);
			} else {
				setNavigationMode(navTypes[0]);
			}
		}
		
		wheelEnabled = false;
		anyAllowed = false;
		for (int i = 0; i < numNavTypes; i++) {
			type = navTypes[i];
			mode = (Integer) navigationTypes.get(type);
			if (mode != null) {
				int value = mode.intValue();
				boundNavModes[i] = value;
				if ( (value == NavigationState.WALK_STATE) ||
					(value == NavigationState.FLY_STATE) ) {
					wheelEnabled = true;
				}
			} else {
				if (type.equals("ANY")) {
					boundNavModes[i] = DEFAULT_NAV_INT;
					anyAllowed = true;
					wheelEnabled = true;
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
	// Methods defined by OGLUserInputHandler
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
	 * Set the world group that we are doing the picking on. This allows
	 * us to look for the picked items in the scene. A null value is used to
	 * clear the world and disable picking behaviours.
	 *
	 * @param scene The scene to pick against
	 */
	public void setPickableScene(Group scene) {
		pickableWorld = scene;
		
		doPicking = (pickableWorld != null) && (viewPath != null);
		
		navHandler.setWorldInfo(scene, scene);
	}
	
	/**
	 * Set the view and it's related transform group to use and the path to
	 * get there from the root of the scene. The transform group must allow
	 * for reading the local to Vworld coordinates so that we can accurately
	 * implement terrain following. A null value for the path is permitted.
	 * <p>
	 * This will also automatically set the center of rotation.
	 *
	 * @param vp The current viewpoint
	 * @param tg The transform just about the viewpoint used to move it
	 *    around in response to the UI device input
	 * @param path The path from the root to the transform to use
	 * @throws IllegalArgumentException The terminal node is not a viewpoint
	 */
	public void setViewInfo(OGLViewpointNodeType vp,
		TransformGroup tg,
		SceneGraphPath path)
		throws IllegalArgumentException {		
		navHandler.setViewInfo(vp, tg, path);
		
		if(vp != null)
			navHandler.setCenterOfRotation(vp.getCenterOfRotation(), null);
		
		viewPath = path;
		doPicking = (pickableWorld != null) && (viewPath != null);
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
		navHandler.setCollisionListener(l);
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
		if (currentNavInfo != null)
			currentNavInfo.removeNavigationChangedListener(this);
		
		currentNavInfo = navInfo;
		
		if(navInfo == null)
			return;
		
		navInfo.addNavigationChangedListener(this);
		
		// Set the speed values. Currently not handling dynamic changes in
		// speed values.
		float[] avatar = navInfo.getAvatarSize();
		float speed = navInfo.getSpeed();
		
		navHandler.setAvatarInfo(avatar[1], avatar[0], avatar[2]);
		navHandler.setNavigationSpeed(speed);
		
		navTypes = navInfo.getType();
		numNavTypes = navInfo.getNumTypes();
		
		boolean state_found = false;
		boolean has_any = false;
		String type;
		int state = 0;
		
		Integer mode;
		
		fireNavigationListChanged(navTypes,numNavTypes);
		
		if (numNavTypes > 0) {
			if (navTypes[0].equals("ANY")) {
				setNavigationMode(DEFAULT_NAV_STRING);
			} else {
				setNavigationMode(navTypes[0]);
			}
		}
		
		wheelEnabled = false;
		anyAllowed = false;
		for(int i = 0; i < numNavTypes; i++) {
			type = navTypes[i];
			mode = (Integer)navigationTypes.get(type);
			if (mode != null) {
				int value = mode.intValue();
				boundNavModes[i] = value;
				if ( (value == NavigationState.WALK_STATE) ||
					(value == NavigationState.FLY_STATE) ) {
					wheelEnabled = true;
				}
			}
			else {
				if (type.equals("ANY")) {
					boundNavModes[i] = DEFAULT_NAV_INT;
					anyAllowed = true;
					wheelEnabled = true;
				} else {
					System.out.println("Unknown navigation mode: " + type);
					boundNavModes[i] = NavigationState.NO_STATE;
				}
			}
		}
		
		navHandler.setNavigationState(boundNavModes[0]);
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
		checkSubMode( tracker, evt.altModifier );
		
		if (navMode[tracker] == -1) {
			setNavMode(tracker,evt);
		}
		trackerIntersect = false;
		
		boolean doNavigation = false;
		
		trackExamineWithShiftMod = evt.shiftModifier &&
			(navMode[tracker] == Xj3DNavigationState.TRACK_EXAMINE_STATE);
		
		if(!doPicking) {
			doNavigation = true;
		}
		else {
			SceneGraphPath path = null;
			
			// Look for a button that is picking, if none then don't pick
			boolean pickerFound = false;
			
			for(int i = 0; i < evt.numButtons; i++) {
				if (evt.pickingEnabled[i] && evt.buttonState[i]) {
					pickerFound = true;
					break;
				}
			}
			
			if (pickerFound && activateSensors && testPointingDevices) {
				path = doPick(evt);
			}
			if(path == null) {
				// Nothing picked so must be navigation
				doNavigation = true;
			} else {
				
				trackerIntersect = true;
				
				Object user_data;
				int num_nodes = path.getNodeCount();
				Node[] node_list = path.getNodes();
				SceneGraphObject node;
				boolean object_found = false;
				
				// track modes suppress touch and drag sensor handling when 
				// the shift modifier is active.
				// therefore, skip determining if a sensor node is affected
				boolean trackWithShiftMod = evt.shiftModifier &&
					((navMode[tracker] == Xj3DNavigationState.TRACK_EXAMINE_STATE) ||
					(navMode[tracker] == Xj3DNavigationState.TRACK_PAN_STATE));
				
				if(!trackWithShiftMod) {
					for(int i = num_nodes; --i >= 0;) {
						node = node_list[i];
						if(processNodePress(node)) {
							object_found = true;
							currentPickedPath = path;
							currentPickedNodeIndex = i;
							
							break;
						}
					}
				}
				if(!object_found) {
					// Did we find an object? If not, then let's navigate!
					doNavigation = true;
				} else {
					// process the node list
					if( hasTouchSensor ) {
						
						double time = clock.getTime();
						
						VRMLTouchSensorNodeType touch;
						int size = activeTouchSensors.size();
						for(int i = 0; i < size; i++) {
							
							touch = (VRMLTouchSensorNodeType)activeTouchSensors.get(i);
							touch.notifyPressed(
								1,
								time,
								sensorHitPoint,
								sensorHitNormal,
								sensorHitTexCoord);
							
							fireDeviceActivated(touch);
						}
					}
					
					if( hasDragSensor ) {
						
						calcSensorGroupVWorld(
							currentPickedPath,
							currentPickedNodeIndex,
							dragParentTransform);
						
						dragParentTransform.invert( );
						
						// convert the mouse coordinate from world space
						// to the local coordinates of the sensor. use
						// sensorHitNormal for the location parameter
						
						dragParentTransform.transform( mousePosition, wkPoint );
						
						sensorHitNormal[0] = (float)wkPoint.x;
						sensorHitNormal[1] = (float)wkPoint.y;
						sensorHitNormal[2] = (float)wkPoint.z;
						
						wkPoint.x = sensorHitPoint[0];
						wkPoint.y = sensorHitPoint[1];
						wkPoint.z = sensorHitPoint[2];
						
						dragParentTransform.transform( wkPoint, pointTrans );
						
						sensorHitPoint[0] = (float)pointTrans.x;
						sensorHitPoint[1] = (float)pointTrans.y;
						sensorHitPoint[2] = (float)pointTrans.z;
						
						VRMLDragSensorNodeType drag;
						int size = activeDragSensors.size();
						for(int i = 0; i < size; i++) {
							
							drag = (VRMLDragSensorNodeType)activeDragSensors.get(i);
							drag.notifySensorDragStart(
								sensorHitPoint,
								sensorHitNormal);
							
							fireDeviceActivated(drag);
						}
					}
				}
			}
		}
		if ( doNavigation ) {
			navStart[0] = evt.devicePos[0];
			navStart[1] = evt.devicePos[1];
			navStart[2] = evt.devicePos[2];
			setNavMode(tracker,evt);
			activeNavigationTracker = tracker;
			
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
	 * Process a tracker moved event.
	 *
	 * @param evt The event that caused the method to be called
	 */
	public void trackerMoved(int tracker, TrackerState evt) {
		
		checkSubMode( tracker, evt.altModifier );
		
		trackerIntersect = false;
		
		if(!doPicking)
			return;
		
		SceneGraphPath path = null;
		
		if (activateSensors && testPointingDevices) {
			path = doPick(evt);
		}
		
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
					
					if (sens instanceof VRMLPointingDeviceSensorNodeType)
						((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);
					
					fireDeviceNotOver(sens);
				}
				
				activeSensors.clear();
			}
			
			return;
		}
		
		trackerIntersect = true;
		
		int node_count = path.getNodeCount();
		
		Node[] node_list = path.getNodes();
		boolean sensor_found = false;
		VRMLLinkNodeType link;
		boolean ts_fired = false;
		
		for(int i = node_count - 1; i >= 0; i--) {
			Node n = node_list[i];
			
			OGLUserData u_data = (OGLUserData)n.getUserData();
			
			if(u_data == null)
				continue;
			
			if (u_data.linkReference != null) {
				link = u_data.linkReference;
				
				if(activeSensors.contains(link))
					activeSensors.remove(link);
				else {
					fireDeviceOver(link);
				}
				activeSensors.add(link);
				sensor_found = true;
			}
			
			if(u_data.sensors == null)
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
				
				if ( sens instanceof VRMLDragSensorNodeType ) {
					
					VRMLDragSensorNodeType drag = (VRMLDragSensorNodeType)sens;
					
					if ( drag.getEnabled( ) ) {
						workingSensors.add( drag );
						
						if ( activeSensors.contains( drag ) ) {
							activeSensors.remove( drag );
						} else {
							drag.setIsOver( true );
							fireDeviceOver( drag );
						}
						sensor_found = true;
					}
				}
				
				else if ( sens instanceof VRMLTouchSensorNodeType ) {
					
					VRMLTouchSensorNodeType touch = (VRMLTouchSensorNodeType)sens;
					
					if ( touch.getEnabled( ) ) {
						
						sensor_found = true;
						
						if( !ts_fired ) {
							// If it is active already, take if from this set and
							// place it in the working set. Only update the position
							// information.
							
							if( activeSensors.contains( touch ) ) {
								activeSensors.remove( touch );
							}
							else {
								touch.setIsOver( true );
								fireDeviceOver( touch );
							}
							touch.notifyHitChanged(
								sensorHitPoint,
								sensorHitNormal,
								sensorHitTexCoord );
							
							ts_fired = true;
							
							workingSensors.add( touch );
						}
					}
				}
			}
			
			// Any left in activeSensors are no longer active so
			// issue an isOver(false); This should also clear the
			// active set as a by-product.
			int size = activeSensors.size();
			
			if(size != 0) {
				// this will automatically resize if needed
				tmpSensors = (Object[])activeSensors.toArray(tmpSensors);
				
				for(int k = 0; k < size; k++) {
					sens = tmpSensors[k];
					
					if(sens instanceof VRMLPointingDeviceSensorNodeType) {
						((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);
					}
					fireDeviceNotOver(sens);
				}
				activeSensors.clear();
			}
			
			activeSensors.addAll(workingSensors);
			workingSensors.clear();
			
			// This insures only one sensor is activated outside a group
			// The sort mechanism might insure the correct X3D behavior from 20.2.1
			// but not sure
			
			if (sensor_found) {
				break;
			}
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
				
				if(sens instanceof VRMLPointingDeviceSensorNodeType) {
					((VRMLPointingDeviceSensorNodeType)sens).setIsOver(false);
				}
				fireDeviceNotOver(sens);
			}
			activeSensors.clear();
		}
	}
	
	/**
	 * Did the last tracker interaction intersect any active sensors.
	 *
	 * @return true if the tracker intersection an active sensor.
	 */
	public boolean trackerIntersected() {
		return trackerIntersect;
	}
	
	/**
	 * Sets whether this tracker is eligible to active a sensor.
	 *
	 * @param val Whether its eligible
	 */
	public void setActivateSensors(boolean val) {
		activateSensors = val;
	}
	
	/**
	 * Process a tracker press event. This may be used to start a touchtracker,
	 * start of a drag tracker or navigation
	 *
	 * @param tracker The id of the tracker that was released
	 * @param state The state of the tracker right now
	 */
	public void trackerDragged(int tracker, TrackerState state) {
		
		checkSubMode( tracker, state.altModifier );
		
		switch(currentState) {
		case NAVIGATION_STATE:
			navigationDragInProgress = true;
			if ( tracker == activeNavigationTracker ) {
				float x_dif = navStart[0] - state.devicePos[0];
				float y_dif = state.devicePos[1] - navStart[1];
				
				switch(navMode[tracker]) {
				case NavigationState.WALK_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = 0;
					tmpVec1[2] = y_dif;
					break;
				case NavigationState.PAN_STATE:
					tmpVec1[0] = -x_dif;
					tmpVec1[1] = -y_dif;
					tmpVec1[2] = 0;
					break;
				case NavigationState.TILT_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = -y_dif;
					tmpVec1[2] = 0;
					break;
				case NavigationState.FLY_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = 0;
					tmpVec1[2] = y_dif;
					break;
				case NavigationState.EXAMINE_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = y_dif;
					tmpVec1[2] = 0;
					break;
				case Xj3DNavigationState.INSPECT_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = 0;
					tmpVec1[2] = y_dif;
					break;
				case Xj3DNavigationState.TRACK_EXAMINE_STATE:
					if (state.altModifier) {
						// track pan mode
						tmpVec1[0] = x_dif;
						tmpVec1[1] = y_dif;
						tmpVec1[2] = 0;
					} 
					else if (state.ctrlModifier) {
						// y motion becomes zoom when ctrl is active
						tmpVec1[0] = 0;
						tmpVec1[1] = 0;
						tmpVec1[2] = y_dif;
					} else {
						tmpVec1[0] = -x_dif;
						tmpVec1[1] = -y_dif;
						tmpVec1[2] = 0;
					}
					// reset starting position
					navStart[0] = state.devicePos[0];
					navStart[1] = state.devicePos[1];
					navStart[2] = state.devicePos[2];
					break;
				case Xj3DNavigationState.TRACK_PAN_STATE:
					tmpVec1[0] = x_dif;
					tmpVec1[1] = y_dif;
					tmpVec1[2] = 0;
					// reset starting position
					navStart[0] = state.devicePos[0];
					navStart[1] = state.devicePos[1];
					navStart[2] = state.devicePos[2];
					break;
				}
				
				float scale = (float) Math.sqrt(tmpVec1[0] * tmpVec1[0] +
					tmpVec1[1] * tmpVec1[1] + tmpVec1[2] * tmpVec1[2]);
				
				if (scale != 0) {
					tmpVec1[0] /= scale;
					tmpVec1[1] /= scale;
					tmpVec1[2] /= scale;
				} else {
					tmpVec1[0] = 0;
					tmpVec1[1] = 1;
					tmpVec1[2] = 0;
				}
				
				navHandler.move(tmpVec1, scale);
			}
			break;
			
		case ANCHOR_STATE:
			// do nothing
			break;
			
		case TOUCH_STATE:
			
			workingSensors.clear( ); // paranoia, clearing just to be sure...
			
			// gather the active touch sensors to check
			// isOver status from the pick
			if ( hasTouchSensor ) {
				int size = activeTouchSensors.size( );
				for( int i = 0; i < size; i++ ) {
					workingSensors.add( activeTouchSensors.get( i ) );
				}
			}
			
			SceneGraphPath path = doPick(state);
			if ( path != null ) {
				
				Object sens;
				int node_count = path.getNodeCount( );
				Node[] node_list = path.getNodes( );
				for( int i = node_count - 1; i >= 0; i-- ) {
					
					Node n = node_list[i];
					OGLUserData u_data = (OGLUserData)n.getUserData();
					if ( ( u_data != null ) && ( u_data.sensors != null ) ) {
						
						for ( int j = 0; j < u_data.sensors.length; j++ ) {
							
							sens = u_data.sensors[j];
							if ( workingSensors.remove( sens ) ) {
								
								if ( sens instanceof VRMLTouchSensorNodeType ) {
									
									VRMLTouchSensorNodeType touch = (VRMLTouchSensorNodeType)sens;
									
									pointTrans.x = sensorHitPoint[0];
									pointTrans.y = sensorHitPoint[1];
									pointTrans.z = sensorHitPoint[2];
									
									calcHitPoint(path, i, pointTrans);
									
									sensorHitPoint[0] = (float)pointTrans.x;
									sensorHitPoint[1] = (float)pointTrans.y;
									sensorHitPoint[2] = (float)pointTrans.z;
									
									if ( !touch.getIsOver( ) ) {
										touch.setIsOver( true );
										activeSensors.add( touch );
									}
									// TODO: This is not a correct normal,
									// and - hitTexCoord has not been calculated.....
									touch.notifyHitChanged(
										sensorHitPoint,
										sensorHitNormal,
										sensorHitTexCoord );
								}
							}
						}
					}
				}
			}
			if ( hasDragSensor ) {
				
				mousePosition.x = state.worldPos[0];
				mousePosition.y = state.worldPos[1];
				mousePosition.z = state.worldPos[2];
				
				mousePickDirection.x = state.worldOri[0];
				mousePickDirection.y = state.worldOri[1];
				mousePickDirection.z = state.worldOri[2];
				
				dragParentTransform.transform( mousePosition, wkPoint );
				dragParentTransform.transform( mousePickDirection, diffVec );
				
				sensorHitPoint[0] = (float)wkPoint.x;
				sensorHitPoint[1] = (float)wkPoint.y;
				sensorHitPoint[2] = (float)wkPoint.z;
				
				sensorHitNormal[0] = (float)diffVec.x;
				sensorHitNormal[1] = (float)diffVec.y;
				sensorHitNormal[2] = (float)diffVec.z;
				
				VRMLDragSensorNodeType drag;
				int size = activeDragSensors.size( );
				for( int i = 0; i < size; i++ ) {
					
					drag = (VRMLDragSensorNodeType)activeDragSensors.get(i);
					drag.notifySensorDragChange(
						sensorHitPoint,
						sensorHitNormal );
				}
			}
			// send an isOver(false); to any left-over touch sensors
			int size = workingSensors.size( );
			if ( size != 0 ) {
				
				tmpSensors = (Object[])workingSensors.toArray(tmpSensors);
				
				for( int i = 0; i < size; i++ ) {
					
					VRMLPointingDeviceSensorNodeType sens =
						(VRMLPointingDeviceSensorNodeType)tmpSensors[i];
					
					if ( sens.getIsOver( ) ) {
						sens.setIsOver( false );
						activeSensors.remove( sens );
					}
				}
				workingSensors.clear( );
			}
			break;
		}
	}
	
	/**
	 * Process a tracker wheel event.
	 *
	 * @param tracker The id of the tracker calling this handler
	 * @param state The current state.
	 */
	public void trackerWheel(int tracker, TrackerState state) {
		if ( wheelEnabled && navTypes[currentNavTypeIndex].equals( Xj3DConstants.EXAMINE_NAV_MODE ) ) {
			tmpVec1[0] = 0;
			tmpVec1[1] = 0;
			tmpVec1[2] = state.wheelClicks;
			
			navHandler.move(tmpVec1, 1);
			wheelMoved = true;
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
		
		checkSubMode( tracker, state.altModifier );
		
		int i, size;
		
		switch(currentState) {
		case NAVIGATION_STATE:
			if ( tracker == activeNavigationTracker ) {
				navHandler.stopMove();
				
				if (navMode[tracker] == NavigationState.LOOKAT_STATE) {
					fireNavigationStateChanged(prevNavModeIdx);
					
					navHandler.setNavigationState(prevNavMode);
					boundNavModes[0] = prevNavMode;
				}
				if (trackExamineWithShiftMod && !navigationDragInProgress) {
					selectCenterOfRotation(state);
					trackExamineWithShiftMod = false;
				}
				currentState = NO_STATE;
				activeNavigationTracker = -1;
				navigationDragInProgress = false;
			}
			break;
			
		case ANCHOR_STATE:
			fireLinkActivated(currentLink);
			
			currentLink = null;
			
			currentState = NO_STATE;
			break;
			
		case TOUCH_STATE:
			size = activeTouchSensors.size();
			if(size != 0) {
				
				VRMLTouchSensorNodeType touch;
				double time = clock.getTime();
				///////////////////////////////////////////////////////////////////////////////
				// rem: Not sure what exactly this warning refers to. The BaseTouchSensor only
				// cares that the mouse has been released and the time, it does not process the
				// sensorHit* parameters. And as such - this seems OK....
				//
				//System.out.println("Warning: does not update released mouse correctly for " +
				//                   "TouchSensors. Must fix");
				///////////////////////////////////////////////////////////////////////////////
				for(i = 0; i < size; i++) {
					touch = (VRMLTouchSensorNodeType)activeTouchSensors.get(i);
					
					touch.notifyReleased(
						1,
						time,
						sensorHitPoint,
						sensorHitNormal,
						sensorHitTexCoord);
					
					if ( !touch.getIsOver( ) ) {
						fireDeviceNotOver(touch);
					}
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
					drag.notifySensorDragEnd(
						sensorHitPoint,
						sensorHitNormal);
					
					if ( !drag.getIsOver( ) ) {
						fireDeviceNotOver(drag);
					}
				}
			}
			
			currentPickedPath = null;
			currentPickedNodeIndex = -1;
			
			activeTouchSensors.clear();
			activeDragSensors.clear();
			
			hasDragSensor = false;
			hasTouchSensor = false;
			
			currentState = NO_STATE;
			
			break;
		}
		
	}
	
	/**
	 * Process a tracker click event. The click is used only on touch trackers
	 * and anchors. We treat it like a cross between a select and unselect.
	 */
	public void trackerClicked(int tracker, TrackerState state) {
		// rem: have never seen this method get called, click
		// does not seem to be a 'supported' event state.
		if (!doPicking) {
			return;
		}
		
		trackerPressed(tracker, state);
		trackerReleased(tracker, state);
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
	 * Process any navigation velocity.  Call every frame.
	 */
	public void processNavigation() {
		if( navigationEnabled ) {
			if ( currentState == NAVIGATION_STATE ) {
				navHandler.processNextFrame();
			}
			else if ( (currentState == NO_STATE) && wheelMoved ) {
				navHandler.processNextFrame();
				wheelMoved = false;
			}
		}
		navHandler.getPosition(currentPosition);
		
		currentNavInfo.setWorldLocation(currentPosition);
	}
	
	/**
	 * Set the desired navigation mode. The mode string is one of the
	 * spec-defined strings for the NavigationInfo node in the VRML/X3D
	 * specification.
	 *
	 * @param mode The requested mode.
	 * @return Whether the mode is valid.
	 */
	public boolean setNavigationMode(String mode) {
		boolean found = false;
		boolean found_any = false;
		int found_idx = 0;
		
		
		for(int i = 0; i < navTypes.length; i++) {
			if(navTypes[i].equals(mode)) {
				found = true;
				found_idx = i;
				break;
			} else if(navTypes[i].equals("ANY")) {
				found_any = true;
			}
		}
		
		if(!found && found_any) {
			// Add the nav mode to the list
			String[] new_types = new String[navTypes.length + 1];
			for(int i = 0; i < navTypes.length; i++) {
				new_types[i] = navTypes[i];
			}
			
			new_types[navTypes.length] = mode;
			
			fireNavigationListChanged(new_types,new_types.length);
			
			found_idx = navTypes.length;
			found = true;
			
			navTypes = new_types;
		}
		
		if(found) {
			fireNavigationStateChanged(found_idx);
			
			Integer typeConst;
			
			typeConst = (Integer)navigationTypes.get(mode);
			
			if(typeConst != null) {
				int val = typeConst.intValue();
				navHandler.setNavigationState(val);
				
				if (val == NavigationState.LOOKAT_STATE) {
					prevNavMode = boundNavModes[0];
				} else {
					prevNavModeIdx = found_idx;
				}
				
				boundNavModes[0] = val;
			} else if(found_any) {
				navHandler.setNavigationState(DEFAULT_NAV_INT);
			}
		}
		
		return found;
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
	 * Get the current user orientation.
	 *
	 * @param ori The orientation vector to fill in
	 */
	public void getOrientation(AxisAngle4f ori) {
		navHandler.getOrientation(ori);
	}
	
	/**
	 * Get the current user position.
	 *
	 * @param pos The position vector to fill in
	 */
	public void getPosition(Vector3f pos) {
		// Use the value calculated in processNavigation
		pos.x = currentPosition.x;
		pos.y = currentPosition.y;
		pos.z = currentPosition.z;
	}
	
	/**
	 * Set the world scale applied.  This will scale down navinfo parameters
	 * to fit into the world.
	 *
	 * @param scale The new world scale.
	 */
	public void setWorldScale(float scale) {
		navHandler.setWorldScale(scale);
	}
	
	/**
	 * The layer that contains this handler has just been made the active
	 * navigation layer, so send out to the navigation state listeners the
	 * current navigation state for this layer. This allows the UI to update
	 * based on the currently active layer.
	 */
	public void sendCurrentNavState() {
		// First send out the list of current nav states available, then
		// send out the selected one in that list.
		fireNavigationListChanged(navTypes, numNavTypes);
		fireNavigationStateChanged(currentNavTypeIndex);
	}
	
	/**
	 * Clear all the values, listeners etc, except for the clock. Returns the
	 * input handler back to being empty, with no state set.
	 */
	public void clear() {
		setWorldScale(1);
		setPickableScene(null);
		setViewInfo(null, null, null);
		
		navigationStateListeners.clear();
		sensorStatusListeners.clear();
		navHandler.clear();
		ArrayList pick_list = (ArrayList)rayPicker.foundPaths;
		if ( pick_list != null ) {
			pick_list.clear();
		}
		for(int i = 0; i < tmpSensors.length; i++) {
			tmpSensors[i] = null;
		}
	}
	
	//------------------------------------------------------------------------
	// Local Methods
	//------------------------------------------------------------------------
	
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
		
		rayPicker.origin[0] = state.worldPos[0];
		rayPicker.origin[1] = state.worldPos[1];
		rayPicker.origin[2] = state.worldPos[2];
		
		rayPicker.destination[0] = state.worldOri[0];
		rayPicker.destination[1] = state.worldOri[1];
		rayPicker.destination[2] = state.worldOri[2];
		
		// Find the bounding reps that intersect the mouse ray
		ArrayList pick_results = (ArrayList)rayPicker.foundPaths;
		
		pick_results.clear();
		
		pickableWorld.pickSingle(rayPicker);
		
		//System.out.println("origin: " + rayPicker.origin[0] + " " + rayPicker.origin[1] + " " + rayPicker.origin[2]);
		//System.out.println("dir: " + rayPicker.destination[0] + " " + rayPicker.destination[1] + " " + rayPicker.destination[2]);
		//System.out.println("pickCount: " + rayPicker.pickCount);
		if(rayPicker.pickCount == 0)
			return null;
		
		// Use exact geometry intersection to find the "one true"
		Geometry geom;
		double shortest_length = Float.POSITIVE_INFINITY;
		boolean valid_pick = false;
		int shortest = -1;
		pick_results = (ArrayList)rayPicker.foundPaths;
		
		for(int i = 0; i < rayPicker.pickCount; i++) {
			SceneGraphPath path = (SceneGraphPath)pick_results.get(i);
			Node end = path.getTerminalNode();
			
			if(end instanceof Shape3D) {
				path.getTransform(viewTransform);
				
				Shape3D i_shape = (Shape3D)path.getTerminalNode();
				geom = i_shape.getGeometry();
				
				if(geom == null)
					continue;
				
				OGLUserData u_data = (OGLUserData)geom.getUserData();
				
				boolean found = false;
				if((u_data != null) && (u_data.geometryData != null) &&
					(u_data.geometryData instanceof GeometryData))
				{
					GeometryData gd = (GeometryData)u_data.geometryData;
					
					found = iutils.rayUnknownGeometry(mouseEyePosition,
						mousePickDirection,
						0,
						gd,
						viewTransform,
						wkPoint,
						false);
				} else if(geom instanceof VertexGeometry) {
					found = iutils.rayUnknownGeometry(mouseEyePosition,
						mousePickDirection,
						0,
						(VertexGeometry)geom,
						viewTransform,
						wkPoint,
						false);
				} else if(geom instanceof Text2D) {
					// Call the intersection test directly as we know exactly
					// what it will return
					// Transform origin/direction to local coordinate space.
					tmpVec1[0] = viewTransform.m00 * rayPicker.origin[0] +
						viewTransform.m01 * rayPicker.origin[1] +
						viewTransform.m02 * rayPicker.origin[2] +
						viewTransform.m03;
					tmpVec1[1] = viewTransform.m10 * rayPicker.origin[0] +
						viewTransform.m11 * rayPicker.origin[1] +
						viewTransform.m12 * rayPicker.origin[2] +
						viewTransform.m13;
					tmpVec1[2] = viewTransform.m20 * rayPicker.origin[0] +
						viewTransform.m21 * rayPicker.origin[1] +
						viewTransform.m22 * rayPicker.origin[2] +
						viewTransform.m23;
					
					tmpVec2[0] = viewTransform.m00 * rayPicker.destination[0] +
						viewTransform.m01 * rayPicker.destination[1] +
						viewTransform.m02 * rayPicker.destination[2];
					tmpVec2[1] = viewTransform.m10 * rayPicker.destination[0] +
						viewTransform.m11 * rayPicker.destination[1] +
						viewTransform.m12 * rayPicker.destination[2];
					tmpVec2[2] = viewTransform.m20 * rayPicker.destination[0] +
						viewTransform.m21 * rayPicker.destination[1] +
						viewTransform.m22 * rayPicker.destination[2];
					
					found =
						geom.pickLineRay(tmpVec1, tmpVec2, true, tmpVec3, 0);
					
					if(found) {
						wkPoint.x = tmpVec3[0];
						wkPoint.y = tmpVec3[1];
						wkPoint.z = tmpVec3[2];
					}
					
				} else {
					// This will probably hit VolumeGeometry too
					System.out.println("Unhandled geometry type in picking: " + geom);
				}
				
				if(found) {
					diffVec.sub(mouseEyePosition, wkPoint);
					if(diffVec.lengthSquared() < shortest_length) {
						shortest = i;
						shortest_length = diffVec.lengthSquared();
						//System.out.println("***Initial set of sensorHitPoint(wc): " + wkPoint);
						sensorHitPoint[0] = (float)wkPoint.x;
						sensorHitPoint[1] = (float)wkPoint.y;
						sensorHitPoint[2] = (float)wkPoint.z;
						valid_pick = true;
					}
				}
			}
		}
		
		if(!valid_pick)
			return null;
		else
			return (SceneGraphPath)pick_results.get(shortest);
	}
	
	/**
	 * Convenience method to process the user data into a node. If the node
	 * is a sensor and not enabled it will be ignored.
	 *
	 * @param node The scenegraph object to process
	 * @return true if the node was successfully used for VRML purposes
	 */
	private boolean processNodePress(SceneGraphObject node) {
		Object ud = node.getUserData();
		
		if((ud == null) || !(ud instanceof OGLUserData))
			return false;
		
		boolean ret_val = false;
		double time = clock.getTime();
		OGLUserData user_data = (OGLUserData)ud;
		
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
				
				if( sensors[i].getEnabled( ) ) {
					
					if (sensors[i] instanceof VRMLTouchSensorNodeType) {
						touch = (VRMLTouchSensorNodeType)sensors[i];
						activeTouchSensors.add(touch);
						hasTouchSensor = true;
					} else {
						drag = (VRMLDragSensorNodeType)sensors[i];
						activeDragSensors.add(drag);
						hasDragSensor = true;
					}
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
	 * in the passed Matrix4f instance.
	 */
	private void calcSensorGroupVWorld(SceneGraphPath path,
		int sensorPos,
		Matrix4f transform) {
		
		transform.setIdentity();
		
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
		int num_nodes = path.getNodeCount();
		boolean has_transforms = false;
		
		for(int i = num_nodes - 1; (i >= sensorPos) && !has_transforms; i--) {
			Node n = path.getNode(i);
			
			OGLUserData u_data = (OGLUserData)n.getUserData();
			if (u_data != null)
				has_transforms = u_data.isTransform;
		}
		
		if(!has_transforms)
			return;
		
		// Ok, so we have to do some work now and convert the data back to
		// sensor coordinates.  first build the new path from the old one as
		// we need to get the vworld coordinates and that requires passing in
		// a SceneGraphPath because we'll have links/sharedGroups all over the
		// place.
		Node[] node_list = path.getNodes();
		
		for(int i = 0; i <= sensorPos; i++) {
			if(!(node_list[i] instanceof TransformGroup))
				continue;
			
			TransformGroup tg = (TransformGroup)node_list[i];
			tg.getTransform(wkTrans);
			transform.mul(wkTrans);
		}
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
	 * @param sensorPos The index of the sensor to use in that path
	 * @param worldPos The point to be transformed from world coordinates
	 */
	private void calcHitPoint(SceneGraphPath path,
		int sensorPos,
		Point3f worldPos) {
		
		calcSensorGroupVWorld(path, sensorPos, sensorTransform);
		
		matrixUtils.inverse(sensorTransform, sensorTransform);
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
					continue;
				case ButtonModeConstants.NAV2:
					navMode[tracker] = boundNavModes[1];
					continue;
				case ButtonModeConstants.NAV3:
					navMode[tracker] = boundNavModes[2];
					continue;
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

		if ((navMode[tracker] == Xj3DNavigationState.TRACK_EXAMINE_STATE) && localAltModifier) {
			navHandler.setNavigationState(Xj3DNavigationState.TRACK_PAN_STATE);
		} else {
			navHandler.setNavigationState(navMode[tracker]);
		}
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
	 * Fire a navigation state changed event. The index sent out is the
	 * index within the current navigation state list, that was sent out
	 * earlier.
	 *
	 * @param idx The index of the state in the current nav list
	 */
	private void fireNavigationStateChanged(int idx) {
		currentNavTypeIndex = idx;
		
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
	 *
	 * @param modes The list of new mode strings
	 * @param numModes number of valid items in that modes array
	 */
	private void fireNavigationListChanged(String[] modes, int numModes) {
		int size = navigationStateListeners.size();
		
		for(int i = 0; i < size; i++) {
			try {
				NavigationStateListener l =
					(NavigationStateListener)navigationStateListeners.get(i);
				
				l.navigationListChanged(modes,numModes);
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
		SceneGraphPath path = null;
		
		path = doPick(evt);
		
		if(path == null) {
			return;
		} else {
			
			Object user_data;
			int num_nodes = path.getNodeCount();
			Node[] node_list = path.getNodes();
			Node node;
			
			if (num_nodes < 1)
				return;
			
			node = node_list[num_nodes - 1];
			
			sensorTransform.setIdentity();
			
			for(int i = 0; i < num_nodes; i++) {
				if(!(node_list[i] instanceof TransformGroup))
					continue;
				
				TransformGroup tg = (TransformGroup)node_list[i];
				tg.getTransform(wkTrans);
				sensorTransform.mul(wkTrans);
			}
			
			BoundingVolume bounds = node.getBounds();
			float[] center = new float[3];
			
			bounds.getCenter(center);
			
			Point3f centerPnt = new Point3f(center);
			Point3f hitPnt = new Point3f(sensorHitPoint);
			
			sensorTransform.transform(centerPnt);
			
			Vector3f userPos = new Vector3f();
			navHandler.getPosition(userPos);
			Vector3f dir = new Vector3f();
			
			dir.sub(hitPnt, userPos);
			
			userPos.x = userPos.x + 0.5f * dir.x;
			userPos.y = userPos.y + 0.5f * dir.y;
			userPos.z = userPos.z + 0.5f * dir.z;
			
			center[0] = centerPnt.x;
			center[1] = centerPnt.y;
			center[2] = centerPnt.z;
			
			float[] lookPos = new float[] {userPos.x, userPos.y, userPos.z};
			
			navHandler.setCenterOfRotation(center, lookPos);
		}
	}
	
	/**
	 * Method used when selecting the center of rotation in the tracking
	 * examine navigation mode. Sets the picked point as the center
	 * and 'zooms' towards it.
	 */
	private void selectCenterOfRotation(TrackerState evt) {
		SceneGraphPath path = null;
		
		path = doPick(evt);
		
		if(path == null) {
			return;
		} else {
			
			Object user_data;
			int num_nodes = path.getNodeCount();
			Node[] node_list = path.getNodes();
			Node node;
			
			if (num_nodes < 1) {
				return;
			}
			
			Point3f hitPnt = new Point3f(sensorHitPoint);
			
			Vector3f userPos = new Vector3f();
			navHandler.getPosition(userPos);
			Vector3f dir = new Vector3f();
			
			dir.sub(hitPnt, userPos);
			
			userPos.x = userPos.x + 0.2f * dir.x;
			userPos.y = userPos.y + 0.2f * dir.y;
			userPos.z = userPos.z + 0.2f * dir.z;
			
			float[] lookPos = new float[] {userPos.x, userPos.y, userPos.z};
			
			navHandler.setCenterOfRotation(sensorHitPoint, lookPos, true);
		}
	}
	
	/**
	 * Check whether navigation is in tracking mode and configure the
	 * nav processor if a sub mode (pan) is necessary depending on the
	 * state of the modifier keys.
	 *
	 * @param tracker The index of the tracker
	 * @param altModifier The state of the alt key modifier
	 */
	private void checkSubMode( int tracker, boolean altModifier ) {
		if (altModifier && !localAltModifier) {
			if (navMode[tracker] == Xj3DNavigationState.TRACK_EXAMINE_STATE) {
				navHandler.setNavigationState(Xj3DNavigationState.TRACK_PAN_STATE);
				navHandler.startMove();
			}
		} else if (localAltModifier && !altModifier) {
			if (navMode[tracker] == Xj3DNavigationState.TRACK_EXAMINE_STATE) {
				navHandler.setNavigationState(Xj3DNavigationState.TRACK_EXAMINE_STATE);
				navHandler.startMove();
			}
		}
		localAltModifier = altModifier;
	}
}
