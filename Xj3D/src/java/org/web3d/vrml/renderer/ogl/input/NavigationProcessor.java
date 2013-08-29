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

import javax.vecmath.*;

import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.HeightDataSource;

import org.j3d.geom.GeometryData;
import org.j3d.util.MatrixUtils;
import org.j3d.util.UserSupplementData;

import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.renderer.aviatrix3d.util.AVIntersectionUtils;

// Local imports
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;


/**
 * A listener and handler responsible for executing all navigation commands
 * from mice and keyboards to move a viewpoint around a scene.
 * <p>
 *
 * This class does not contain any direct event handling. Instead it assumes
 * that another class with either derive from it or delegate to it to do the
 * actual movement processing. This allows it to be used as a standard AWT
 * event listener.
 * <p>
 *
 * The class works like a standard VRML browser type navigation system. Press
 * the mouse button and drag to get the viewpoint moving. The more you drag
 * the button away from the start point, the faster the movement. The handler
 * does not recognize the use of the Shift key as a modifier to produce an
 * accelerated movement.
 * <p>
 *
 * This class will not change the cursor on the canvas in response to the
 * current mouse and navigation state. It will only notify the state change
 * listener. It is the responsibility of the listener to do this work.
 * <p>
 *
 * Separate states are allowed to be set for each button. Once one button is
 * pressed, all the other button presses are ignored. By default, all the
 * buttons start with no state set. The user will have to explicitly set
 * the state for each button to get them to work.
 * <p>
 *
 * <b>Terrain Following</b>
 * <p>
 *
 * When doing terrain following, the handler will project a ray from the
 * current viewpoint position to the ground position. It will then offset the
 * current position by the new position that we should be going to. If the
 * distance in the overall Y axis is less than the step height, the translation
 * will be allowed to proceed and the height adjusted to the new value. If it
 * is greater, then it will set the Z component to zero, allowing no forward
 * movement. Thus, if the next translation also has a sideways component, it
 * will simply shift sideways and not move forward.
 * <p>
 *
 * If there is no terrain under the current eye position, or the next eye
 * position, it will not change the Y axis value at all.
 * <P>
 *
 * If you do not wish to have terrain following for all modes, then pass a
 * <code>null</code> value for the terrain parameter to setWorldInfo().
 * <p>
 *
 *
 * <b>Collision Detection</b>
 * <p>
 *
 * Collision detection is based on using a fixed point representation of the
 * avatar - we do not have a volumetric body for it. A ray is cast in the
 * direction that we are looking that is the length of the avatarSize plus the
 * amount that we are due to move this next frame.
 * <p>
 *
 * If you do not wish to have collision detection for all modes, then pass a
 * <code>null</code> value for the collidables parameter to setWorldInfo().
 * <p>
 *
 * <b>Navigation Modes</b>
 * <p>
 *
 * <i>NONE<i>
 * <p>
 *
 * All navigation is disabled. We ignore any requests from mouse or
 * keyboard to move the viewpoint.
 * <p>
 *
 *
 * <i>EXAMINE</i>
 * <p>
 *
 * The viewpoint is moved around the center of rotation provided by
 * the user. There is no collision detection or terrain following in this mode.
 * <p>
 *
 * For movement input, direction is treated as the direction in the local
 * coordinates on the surface of a sphere formed around the rotation origin.
 * The scale represents the movement along the vector and then each component
 * defines the proportion used to move in that direction.
 * [0] is left/right, [1] is up/down, [2] is zoom in out where +Z is zoom out.
 * <p>
 *
 * <i>FLY</i>
 * <p>
 * The user moves through the scene that moves the eyepoint in forward,
 * reverse and side to side movements. There is collision detection, but no
 * terrain following.
 * <p>
 *
 * For movement input the direction controls the scale of the input to
 * translate and rotate about. [0] controls left/right rotation, [1]
 * controls pitch and [2] controls the amount of forward movement.
 * <p>
 *
 * <i>WALK</i>
 * <p>
 *
 * The user moves through the scene with left/right options and forward
 * reverse, but they are bound to the terrain and have collision detection.
 * <p>
 *
 * For movement input, the direction controls only two of the axes as gravity
 * is used to constrain in the local Y axis. [0] is the amount of rotation
 * left/right to perform each frame and [2] controls the forward/reverse
 * movement.
 * <p>
 *
 * <i>PAN</i>
 * <p>
 *
 * The camera moves in a sliding fashion along the given axis - the local
 * X or Z axis. There is not collision detection or terrain following.
 * <p>
 *
 * Move the viewpoint left/right/up/down while maintaining the current
 * viewing direction. [0] is used for left/right and [1] is used for
 * up/down. [2] is not used.
 * <p>
 *
 * <i>TILT</i>
 * <p>
 *
 * The camera rotates around the local axis in an up/down, left/right
 * fashion. It stays anchored in the one place and does not have terrain
 * following or collision detection.
 * <p>
 *
 * Movement input controls how the tilt is performed and is an absolute
 * value that controls total tilt. [0] controls rotation of the camera
 * in the X-Z plane, [1] controls rotation in the Y-Z plane.
 *
 * <b>TODO</b>
 * <p>
 * The collision vector does not move according to the direction that we are
 * travelling rather than the direction we are facing. Allows us to walk
 * backwards through objects when we shouldn't.
 * <p>
 * Implement Examine mode handling
 * <p>
 *
 * This code is a direct copy and modified version of the code in the j3d.org
 * library of the same name.
 *
 * @author Justin Couch
 * @version $Revision: 1.43 $
 */
public class NavigationProcessor implements NodeUpdateListener, OGLTransformNodeType {
	
	/** The avatar representation is a floating eyeball */
	public static final int AVATAR_POINT = 1;
	
	/** The avatar representation is a cylinder */
	public static final int AVATAR_CYLINDER = 2;
	
	/** The avatar representation is two shoulder rays */
	public static final int AVATAR_SHOULDERS = 3;
	
	/** The default height of the avatar */
	private static final float DEFAULT_AVATAR_HEIGHT = 1.8f;
	
	/*** The default size of the avatar for collisions */
	private static final float DEFAULT_AVATAR_SIZE = 0.25f;
	
	/** The default step height of the avatar to climb */
	private static final float DEFAULT_STEP_HEIGHT = 0.4f;
	
	/** Default time to orbit an object in examine mode */
	private static final float DEFAULT_ORBIT_TIME = 5;
	
	/** High-Side epsilon float = 0 */
	private static final double ZEROEPS = 0.000001;
	
	/** Fixed vector always pointing down -Y */
	private static final Vector3f Y_DOWN = new Vector3f(0, -1, 0);
	
	/** Fixed vector always pointing Y up for the examine mode */
	private static final Vector3f Y_UP = new Vector3f(0, 1, 0);
	
	/** Fixed vector always pointing along -Z */
	private static final Vector3f COLLISION_DIRECTION = new Vector3f(0, 0, -1);
	
	
	/** Intersection utilities used for terrain following */
	private AVIntersectionUtils terrainIntersect;
	
	/** Intersection utilities used for terrain following */
	private AVIntersectionUtils collideIntersect;
	
	/** Utilities for doing matrix functions */
	private MatrixUtils matrixUtils;
	
	/** The transform group above the view that is being moved each frame */
	private TransformGroup viewTg;
	
	/** The new transform group */
	private TransformGroup newViewTg;
	
	/** The current view point */
	private OGLViewpointNodeType currentViewpoint;
	
	/** The new viewpoint */
	private OGLViewpointNodeType newViewpoint;
	
	/** The transform that belongs to the view transform group */
	private Matrix4f viewTx;
	
	/** Path from the world root to the viewTg when in a shared graph */
	private SceneGraphPath viewPath;
	
	/** An observer for collision information */
	private CollisionListener collisionListener;
	
	/**
	 * The current navigation state either set from us or externally as
	 * the mouse if being dragged around. This is different to the state
	 * that a given mouse button will generate
	 */
	private int navigationState;
	
	/**
	 * Flag indicating that we are currently doing something and should
	 * ignore any current mouse presses.
	 */
	private boolean movementInProgress;
	
	/** The current movement speed in m/s in the local coordinate system */
	private float speed;
	
	/** Scaled version of the speed based on the last movement instruction */
	private float scaledSpeed;
	
	// Java3D stuff for terrain following and collision detection
	
	/** The branchgroup to do the terrain picking on */
	private Group terrain;
	
	/** The branchgroup to do collision picking on */
	private Group collidables;
	
	/** Pick request object for terrain */
	private PickRequest terrainPicker;
	
	/** Pick request object for collision detection */
	private PickRequest collidePicker;
	
	/** The local down direction for the terrain picking */
	private Vector3f downVector;
	
	/** The vector along which we do collision detection */
	private Vector3f collisionVector;
	
	/** Movement direction vector */
	private Vector3f movementDirection;
	
	/** Orientation of the user's gaze */
	private Vector3f lookDirection;
	
	/** Last frames look Direction */
	private Vector3f lastLookDirection;
	
	/** The scaled height of the avatar above the terrain */
	private float avatarHeight;
	
	/** The scaled size of the avatar for collision detection */
	private float avatarSize;
	
	/** The scaled step height of the avatar to allow stair climbing */
	private float avatarStep;
	
	/** The original height of the avatar above the terrain */
	private float origAvatarHeight;
	
	/** The original size of the avatar for collision detection */
	private float origAvatarSize;
	
	/** The original step height of the avatar to allow stair climbing */
	private float origAvatarStep;
	
	/** The world scale */
	private float worldScale;
	
	/** Difference between the avatar height and the step height */
	private float lastTerrainHeight;
	
	/** Vector used to read the location value from a Matrix4f */
	private Vector3f locationVector;
	
	/** Point3D used to represent the location for the picker setup */
	private Point3f locationPoint;
	
	/** Point 3D use to calculate the end point for collisions per frame */
	private Point3f locationEndPoint;
	
	/** Point 3D use to calculate the knee points for collisions per frame */
	private Point3f kneePoint;
	
	/** A point that we use for working calculations (coord transforms) */
	private Point3f wkPoint;
	
	/**
	 * Vector for doing difference calculations on the point we have and the next
	 * while doing terrian following.
	 */
	private Vector3f diffVec;
	
	/** The current position in world coordinates */
	private Vector3f currentPosVec;
	
	/** The intersection point that we really collided with */
	private Point3f intersectionPoint;
	
	// Vars for doing examine mode
	
	/** Center of rotation for examine and look at modes */
	private Point3f centerOfRotation;
	
	/** Time it takes to do a single orbit around the object */
	private float orbitTime;
	
	/** Current angle in the rotation. Always relative to the +X axis. */
	private float lastAngle;
	
	/** current radius of the user from the center of rotation */
	private float rotationRadius;
	
	// The variables from here down are working variables during the drag
	// process. We declare them as class scope so that we don't generate
	// garbage for every mouse movement. The idea is we just re-use these
	// rather than create and destroy each time.
	
	/** The translation amount set by the last change in movement value */
	private Vector3f dragTranslationAmt;
	
	/** A working value for the current frame's translation of the eye */
	private Vector3f oneFrameTranslation;
	
	/** A working value for the current frame's rotation of the eye */
	private Matrix4f oneFrameRotation;
	
	/** The current translation total from the start of the movement */
	private Vector3f viewTranslation;
	
	/** The current viewpoint location in world coordinates */
	private Matrix4f worldEyeTransform;
	
	/** Used to fetch the v-world transformation from picks */
	private Matrix4f vwTransform;
	
	/** The amount to move the view in mouse coords up/down */
	private float inputRotationY;
	
	/** The amount to move the view in mouse coords left/right */
	private float inputRotationX;
	
	/** Flag to indicate that we should be doing collisions this time */
	private boolean allowCollisions;
	
	/** Flag to indicate that we should do terrain following this time */
	private boolean allowTerrainFollowing;
	
	/** Used to correct the rotations */
	private float angle;
	
	/** Calculations for frame duration timing */
	private long startFrameDurationCalc;
	
	/**
	 * Working variable that is used to track the duration it took to render
	 * the last frame so we can compensate for this frame.
	 */
	private long frameDuration;
	
	/** Temp placeholder of the object that has just been collided with */
	private SceneGraphPath collidedObject;
	
	/** The avatar representation to use */
	private int avatarRep;
	
	/** Scratch matrix for view pos updates */
	private Matrix4f tmpMatrix;
	
	/** Scratch vector for view pos updates */
	private Vector3f tmpVector;
	
	///////////////////////////////////////////////////////////////////////////
	// Examine mode scratch objects
	
	/** The up vector of the view matrix */
	private Vector3f upVector;
	
	/** The horizontal vector of the view matrix */
	private Vector3f rightVector;
	
	/** The rotation about the up vector */
	private AxisAngle4f vrtRot;
	
	/** The rotation about the horizontal vector */
	private AxisAngle4f hrzRot;
	
	/** Scratch matrix for calculating the total rotation */
	private Matrix3f rotMat1;
	
	/** Scratch matrix for calculating the total rotation */
	private Matrix3f rotMat2;
	
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Create a new mouse handler with no view information set. This
	 * handler will not do anything until the view transform
	 * references have been set and the navigation modes for at least one
	 * mouse button.
	 */
	public NavigationProcessor() {
		navigationState = NavigationState.NO_STATE;
		movementInProgress = false;
		
		terrainIntersect = new AVIntersectionUtils();
		collideIntersect = new AVIntersectionUtils();
		matrixUtils = new MatrixUtils();
		
		viewTg = new TransformGroup();
		OGLUserData data = new OGLUserData();
		viewTg.setUserData(data);
		data.owner = this;
		
		viewTx = new Matrix4f();
		tmpMatrix = new Matrix4f();
		
		vwTransform = new Matrix4f();
		worldEyeTransform = new Matrix4f();
		downVector = new Vector3f();
		
		terrainPicker = new PickRequest();
		terrainPicker.pickType = PickRequest.FIND_COLLIDABLES;
		terrainPicker.pickGeometryType = PickRequest.PICK_RAY;
		terrainPicker.pickSortType = PickRequest.SORT_ALL;
		terrainPicker.generateVWorldMatrix = true;
		terrainPicker.foundPaths = new ArrayList();
		
		collidePicker = new PickRequest();
		collidePicker.pickType = PickRequest.FIND_COLLIDABLES;
		collidePicker.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
		collidePicker.pickSortType = PickRequest.SORT_ALL;
		collidePicker.generateVWorldMatrix = true;
		collidePicker.foundPaths = new ArrayList();
		
		movementDirection = new Vector3f();
		lookDirection = new Vector3f();
		lastLookDirection = new Vector3f();
		lastLookDirection.x = 0;
		lastLookDirection.y = 0;
		lastLookDirection.z = 0;
		
		centerOfRotation = new Point3f();
		collisionVector = new Vector3f();
		intersectionPoint = new Point3f();
		wkPoint = new Point3f();
		diffVec = new Vector3f();
		currentPosVec = new Vector3f();
		
		avatarRep = AVATAR_POINT;
		
		locationVector = new Vector3f();
		locationPoint = new Point3f();
		locationEndPoint = new Point3f();
		kneePoint = new Point3f();
		
		dragTranslationAmt = new Vector3f();
		oneFrameTranslation = new Vector3f();
		oneFrameRotation = new Matrix4f();
		viewTranslation = new Vector3f();
		inputRotationY = 0;
		inputRotationX = 0;
		
		allowCollisions = false;
		allowTerrainFollowing = false;
		
		orbitTime = DEFAULT_ORBIT_TIME;
		avatarHeight = DEFAULT_AVATAR_HEIGHT;
		avatarSize = DEFAULT_AVATAR_SIZE;
		avatarStep = DEFAULT_STEP_HEIGHT;
		origAvatarHeight = avatarHeight;
		origAvatarSize = avatarSize;
		origAvatarStep = avatarStep;
		worldScale = 1;
		lastTerrainHeight = 0;
		speed = 0;
		
		centerOfRotation = new Point3f(0, 0, 0);
		
		tmpVector = new Vector3f( );
		
		upVector = new Vector3f( );
		rightVector = new Vector3f( );
		vrtRot = new AxisAngle4f( );
		hrzRot = new AxisAngle4f( );
		rotMat1 = new Matrix3f( );
		rotMat2 = new Matrix3f( );
	}
	
	/**
	 * Set the center of rotation explicitly to this place. Coordinates must
	 * be in the coordinate space of the current view transform group. The
	 * provided array must be of least length 3. Center of rotation is used
	 * in examine mode.
	 *
	 * @param center The new center to use
	 * @param lookPos The position to look from.  NULL is the current user position.
	 */
	public void setCenterOfRotation(float[] center, float[] lookPos) {
		setCenterOfRotation(center, lookPos, false);
	}
	
	/**
	 * Set the center of rotation explicitly to this place. Coordinates must
	 * be in the coordinate space of the current view transform group. The
	 * provided array must be of least length 3. Center of rotation is used
	 * in examine mode.
	 *
	 * @param center The new center to use
	 * @param lookPos The position to look from.  NULL is the current user position.
	 * @param preserve true to preserve the current up vector, false to reset
	 * the up vector to Y-UP.
	 */
	public void setCenterOfRotation(float[] center, float[] lookPos, boolean preserve) {
		centerOfRotation.x = center[0];
		centerOfRotation.y = center[1];
		centerOfRotation.z = center[2];
		
		if(navigationState == NavigationState.EXAMINE_STATE ||
			navigationState == NavigationState.LOOKAT_STATE ||
			navigationState == Xj3DNavigationState.INSPECT_STATE ||
			navigationState == Xj3DNavigationState.TRACK_EXAMINE_STATE) {
			
			if (lookPos == null) {
				if (currentViewpoint != null) {
					Matrix4f vMatrix = currentViewpoint.getViewTransform();
					vMatrix.get(viewTranslation);
					locationPoint.set(viewTranslation);
				} else {
					locationPoint.x = 0;
					locationPoint.y = 0;
					locationPoint.z = 10;
				}
			} else {
				locationPoint.x = lookPos[0];
				locationPoint.y = lookPos[1];
				locationPoint.z = lookPos[2];
			}
			
			double x = locationPoint.x - centerOfRotation.x;
			double z = locationPoint.z - centerOfRotation.z;
			
			rotationRadius = (float)Math.sqrt(x * x + z * z);
			lastAngle = (float)Math.atan2(z, x);
			
			
			if (preserve) {
				////////////////////////////////////////////////////////////////////
				// rem: tweak - so the up vector remains the same rather
				// than clamped to the y axis.
				viewTg.getTransform(viewTx);
				
				upVector.x = viewTx.m01;
				upVector.y = viewTx.m11;
				upVector.z = viewTx.m21;
				upVector.normalize( );
				matrixUtils.lookAt(locationPoint, centerOfRotation, upVector, viewTx);
				////////////////////////////////////////////////////////////////////
			} else {
				matrixUtils.lookAt(locationPoint, centerOfRotation, Y_UP, viewTx);
			}

			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null)
				currentViewpoint.setNavigationTransform(viewTx);
			
			if (viewTg.isLive())
				viewTg.boundsChanged(this);
			else
				updateNodeBoundsChanges(viewTg);
		}
	}
	
	/**
	 * Set the view and it's related transform group to use and the path to
	 * get there from the root of the scene. The transform group must allow
	 * for reading the local to Vworld coordinates so that we can accurately
	 * implement terrain following. A null value for the path is permitted.
	 *
	 * @param vp The current viewpoint
	 * @param tg The transform just about the viewpoint used to move it
	 *    around in response to the UI device input
	 * @param path The path from the root to the transform to use
	 */
	public void setViewInfo(OGLViewpointNodeType vp,
		TransformGroup tg,
		SceneGraphPath path)
		throws IllegalArgumentException {
		
		viewPath = path;
		
		if (newViewTg == null) {
			viewTg = tg;
			currentViewpoint = vp;
		}
		
		newViewTg = tg;
		newViewpoint = vp;
		
		// TODO:
		// Adjust the step height to the values from the scaled down vector
		// component so that it relates to the world coordinate system.
	}
	
	/**
	 * Set the branchgroups to use for terrain and collision information. The
	 * two are treated separately for the different processes. The caller may
	 * choose to make them the same reference, but the code internally treats
	 * them separately.
	 * <p>
	 *
	 * <b>Note</b> For picking purposes, the code currently assumes that both
	 * groups do not have any parent transforms. That is, their world origin
	 * is the same as the transform group presented in the view information.
	 *
	 * @param terrainGroup  The geometry to use as terrain for following
	 * @param worldGroup The geometry to use for collisions
	 */
	public void setWorldInfo(Group terrainGroup, Group worldGroup) {
		terrain = terrainGroup;
		collidables = worldGroup;
	}
	
	/**
	 * Set the information about the avatar that is used for collisions and
	 * navigation information.
	 *
	 * @param height The heigth of the avatar above the terrain
	 * @param size The distance between the avatar and collidable objects
	 * @param stepHeight The height that an avatar can step over
	 */
	public void setAvatarInfo(float height, float size, float stepHeight) {
		avatarHeight = height * worldScale;
		avatarSize = size * worldScale;
		avatarStep = stepHeight * worldScale;
		
		origAvatarHeight = avatarHeight;
		origAvatarSize = avatarSize;
		origAvatarStep = avatarStep;
	}
	
	/**
	 * Set the navigation speed to the new value. The speed must be a
	 * non-negative number.
	 *
	 * @param newSpeed The new speed value to use
	 * @throws IllegalArgumentException The value was negative
	 */
	public void setNavigationSpeed(float newSpeed) {
		if(newSpeed < 0)
			throw new IllegalArgumentException("Negative speed value");

		speed = newSpeed;
	}
	
	/**
	 * Set the time it takes in seconds to make one 360 degree rotation of the
	 * center position when in examine mode. Speed to the new value. The time
	 * must be a non-negative number.
	 *
	 * @param time The time value to use
	 * @throws IllegalArgumentException The value was <= 0
	 */
	public void setOrbitTime(float time) {
		if(time <= 0)
			throw new IllegalArgumentException("Orbit time <= 0");
		
		orbitTime = time;
	}
	
	
	/**
	 * Set the ability to use a given state within the handler for a
	 * specific mouse button (up to 3). This allows the caller to control
	 * exactly what states are allowed to be used and with which buttons.
	 * Note that it is quite legal to set all three buttons to the same
	 * navigation state
	 *
	 * @param state The navigation state to use for that button
	 */
	public void setNavigationState(int state) {
		navigationState = state;
	}
	
	/**
	 * Set the listener for collision notifications. By setting
	 * a value of null it will clear the currently set instance
	 *
	 * @param l The listener to use for updates
	 */
	public void setCollisionListener(CollisionListener l) {
		collisionListener = l;
	}
	
	/**
	 * Callback to ask the listener what navigation state it thinks it is
	 * in.
	 *
	 * @return The state that the listener thinks it is in
	 */
	public int getNavigationState()
	{
		return navigationState;
	}
	
	/**
	 * Call to update the user position now based on the difference in time
	 * between the last call and this call. This is to be called when the user
	 * wishes to manually control the navigation process rather than relying on
	 * the inbuilt timer. The user should not be using both mechanisms at the
	 * same time although the code takes no steps to enforce this. If you do
	 * try to do manual updates while also having the automated system, the
	 * results are undefined.
	 */
	public void processNextFrame() {
		frameDuration = System.currentTimeMillis() - startFrameDurationCalc;
		if(frameDuration == 0)
			frameDuration = 1;
		
		processClockTick();
	}
	
	/**
	 * Start the user moving in the current direction. Initialises all the
	 * internal timers but does not actually start the movement.
	 */
	public void startMove() {
		if(movementInProgress || (viewTg == null) ||
			(navigationState == NavigationState.NO_STATE) ||
			navigationState == NavigationState.LOOKAT_STATE)
			return;
		
		viewTg.getTransform(viewTx);
		viewTx.get(viewTranslation);
		
		inputRotationY = 0;
		inputRotationX = 0;
		
		oneFrameRotation.setIdentity();
		dragTranslationAmt.scale(0);
		
		//start the frame duration calculation
		startFrameDurationCalc = System.currentTimeMillis();
		
		switch(navigationState) {
		case NavigationState.FLY_STATE:
			allowCollisions = (collidables != null);
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.PAN_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.TILT_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.WALK_STATE:
			allowCollisions = (collidables != null);
			allowTerrainFollowing = (terrain != null);
			break;
			
		case NavigationState.EXAMINE_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.INSPECT_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.TRACK_EXAMINE_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.TRACK_PAN_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.NO_STATE:
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
		}
		
		if((navigationState == NavigationState.WALK_STATE) &&
			allowTerrainFollowing) {
			setInitialTerrainHeight();
		}
		
		if(navigationState == Xj3DNavigationState.INSPECT_STATE) {
			// If in navigation state, need to determine the original angle of the
			// user. Always relative to the +X axis.
			double x = viewTranslation.x - centerOfRotation.x;
			double z = viewTranslation.z - centerOfRotation.z;
			
			rotationRadius = (float)Math.sqrt(x * x + z * z);
			lastAngle = (float)Math.atan2(z, x);
			
			locationPoint.set(viewTranslation);
			
			matrixUtils.lookAt(locationPoint, centerOfRotation, Y_UP, viewTx);
			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null)
				currentViewpoint.setNavigationTransform(viewTx);
			
			if (viewTg.isLive())
				viewTg.boundsChanged(this);
			else
				updateNodeBoundsChanges(viewTg);
		}
	}
	
	/**
	 * Halt the current movement being processed. Any move input after this
	 * will be ignored and the user will stop moving.
	 */
	public void stopMove() {
		movementInProgress = false;
		allowCollisions = false;
		allowTerrainFollowing = false;
		
		viewTx.setIdentity();
		
		inputRotationY = 0;
		inputRotationX = 0;
		
		oneFrameRotation.setIdentity();
		dragTranslationAmt.scale(0);
		
		if(viewTg != null)
			viewTg.getTransform(viewTx);
	}
	
	/**
	 * Update the user movement to be going in this absolute direction. Scale
	 * gives a proportion of the set speed value to move in that direction.
	 * The interpretation of the 3 components of the vector are described in
	 * the class header documentation.
	 *
	 * @param direction The new direction to move the user
	 * @param scale fractional value of the set speed to use [0, inf)
	 */
	public void move(float[] direction, float scale) {
		if(viewTg == null)
			return;

		// Don't move when speed = 0
		if(speed == 0)
			scale = 0;

		scaledSpeed = speed * scale * worldScale;
		switch(navigationState) {
		case NavigationState.FLY_STATE:
			//  Translate on Z:
			dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);
			
			//  Rotate around Y:
			inputRotationY = direction[0] * scale;
			inputRotationX = direction[1] * scaledSpeed;
			
			allowCollisions = (collidables != null);
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.PAN_STATE:
			//  Translate on X,Y:
			dragTranslationAmt.set(direction[0] * scaledSpeed,
				direction[1] * scaledSpeed,
				direction[2] * scaledSpeed);
			
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.TILT_STATE:
			//  Rotate arround X,Y:
			inputRotationX = direction[1] * scale;
			inputRotationY = direction[0] * scale;
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.WALK_STATE:
			//  Translate on Z only
			dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);
			
			//  Rotate around Y:
			inputRotationY = direction[0] * scale;
			
			allowCollisions = (collidables != null);
			allowTerrainFollowing = (terrain != null);
			break;
			
		case NavigationState.EXAMINE_STATE:
			//dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);
			dragTranslationAmt.set(0, 0, direction[2]);
			inputRotationY = direction[0] * scale;
			inputRotationX = direction[1] * scale;
			
			// do nothing
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.INSPECT_STATE:
			//  Translate on Z only
			dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);
			inputRotationY = direction[0] * scale;
			inputRotationX = direction[1] * scale;
			
			// do nothing
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.TRACK_EXAMINE_STATE:
			// TODO: hard coded multiplier for scaled speed
			// will probably not be 'correct' for all content.
			// need to calculate a better value?
			dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed * 16);
			inputRotationY = direction[0] * scale;
			inputRotationX = direction[1] * scale;
			
			// do nothing
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case Xj3DNavigationState.TRACK_PAN_STATE:
			// TODO: hard coded multiplier for scaled speed
			// will probably not be 'correct' for all content.
			// need to calculate a better value?
			dragTranslationAmt.set(
				direction[0] * scaledSpeed * 16,
				direction[1] * scaledSpeed * 16,
				0);
			
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
			
		case NavigationState.NO_STATE:
			// do nothing
			allowCollisions = false;
			allowTerrainFollowing = false;
			break;
		}
	}
	
	/**
	 * Orient the viewer direction to this position, but do not change their
	 * movement direction. The direction is a normalised 3D vector relative to
	 * the current movement. 0,0,0 means always look where the movement is
	 * taking you.
	 *
	 * @param direction 3D vector where the user should be looking
	 */
	public void orient(float[] direction) {
		if(viewTg == null)
			return;
		
		lookDirection.x = direction[0];
		lookDirection.y = direction[1];
		lookDirection.z = direction[2];
		
		viewTg.getTransform(viewTx);
		viewTx.get(viewTranslation);
		
		inputRotationY = 0;
		inputRotationX = 0;
		
		oneFrameRotation.setIdentity();
		dragTranslationAmt.scale(0);
		
		frameDuration = 0;
		processClockTick();
	}
	
	//----------------------------------------------------------
	// Methods from OGLTransformNodeType
	//----------------------------------------------------------
	/**
	 * Get the transform matrix for this node.  A reference is ok as
	 * the users of this method will not modify the matrix.
	 *
	 * @return The matrix.
	 */
	public Matrix4f getTransform() {
		return viewTx;
	}
	
	//----------------------------------------------------------
	// Methods defined by NodeUpdateListener
	//----------------------------------------------------------
	
	/**
	 * Notification that its safe to update the node now with any operations
	 * that could potentially effect the node's bounds.
	 *
	 * @param src The node or Node Component that is to be updated.
	 */
	public void updateNodeBoundsChanges(Object src) {
		// Ignore old changes to viewTg
		if (src == viewTg) {
			viewTg.setTransform(viewTx);
		}
	}
	
	/**
	 * Notification that its safe to update the node now with any operations
	 * that only change the node's properties, but do not change the bounds.
	 *
	 * @param src The node or Node Component that is to be updated.
	 */
	public void updateNodeDataChanges(Object src) {
	}
	
	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	/**
	 * Get the current user orientation.
	 *
	 * @param ori The orientation vector to fill in
	 */
	public void getOrientation(AxisAngle4f ori) {
		Quat4f quat = new Quat4f();
		
		if (viewPath != null) {
			getLocalToVworld();
			viewTg.getTransform(tmpMatrix);
			worldEyeTransform.mul(tmpMatrix);
			
			worldEyeTransform.get(quat);
			ori.set(quat);
		} else {
			ori.x = 0;
			ori.y = 1;
			ori.z = 0;
			ori.angle = 0;
		}
	}
	
	/**
	 * Get the current user position in world coordinates.
	 *
	 * @param pos The position vector to fill in
	 */
	public void getPosition(Vector3f pos) {
		
		// TODO: This is getting called at least twice per frame which is expensive.
		
		// Implementation note.  We require this method to be called each
		// frame.  Otherwise movements of the VP by non-navigation will
		// make the value of currentPosVec wrong.  This is needed for
		// several pieces of code like near/far clip planes and elevation
		// change.
		
		if (newViewTg != null && newViewTg != viewTg) {
			viewTg = newViewTg;
			currentViewpoint = newViewpoint;
			
			viewTg.getTransform(viewTx);
			viewTx.get(viewTranslation);
			
			if (currentViewpoint != null)
				currentViewpoint.setNavigationTransform(viewTx);
			
			if (viewTg.isLive())
				viewTg.boundsChanged(this);
			else
				updateNodeBoundsChanges(viewTg);
		}
		
		if (viewPath != null) {
			getLocalToVworld();
			viewTg.getTransform(tmpMatrix);
			worldEyeTransform.mul(tmpMatrix);
			
			worldEyeTransform.get(currentPosVec);
		} else {
			currentPosVec.x = 0;
			currentPosVec.y = 0;
			currentPosVec.z = 10;
		}
		
		pos.x = currentPosVec.x;
		pos.y = currentPosVec.y;
		pos.z = currentPosVec.z;
	}
	
	/**
	 * Set the world scale applied.  This will scale down navinfo parameters
	 * to fit into the world.
	 *
	 * @param scale The new world scale.
	 */
	public void setWorldScale(float scale) {
		worldScale = scale;
		
		avatarHeight = origAvatarHeight * worldScale;
		avatarSize = origAvatarSize * worldScale;
		avatarStep = origAvatarStep * worldScale;
	}
	
	/**
	 * Clear references that might be held.
	 */
	public void clear() {
		collidedObject = null;
		ArrayList pick_list = (ArrayList)terrainPicker.foundPaths;
		if ( pick_list != null ) {
			pick_list.clear();
		}
		pick_list = (ArrayList)collidePicker.foundPaths;
		if ( pick_list != null ) {
			pick_list.clear();
		}
	}
	
	//----------------------------------------------------------
	// Internal convenience methods
	//----------------------------------------------------------
	
	/**
	 * Internal processing method for the per-frame update behaviour. Assumes
	 * that the frameDuration has been calculated before calling this method.
	 */
	private void processClockTick() {
		/*
		if (newViewTg != viewTg) {
                    viewTg = newViewTg;
                    viewTg.getTransform(viewTx);
                    viewTx.get(viewTranslation);

                    viewTg.boundsChanged(this);
                }
        */
		
		if (navigationState == NavigationState.EXAMINE_STATE) {
			processExamineMotion();
			processExamineRadialMotion( );
		}
		else if ( navigationState == Xj3DNavigationState.INSPECT_STATE ) {
			processInspectMotion();
		}
		else if ( navigationState == Xj3DNavigationState.TRACK_EXAMINE_STATE ) {
			processTrackExamineMotion();
			processTrackExamineRadialMotion( );
		}
		else if ( navigationState == Xj3DNavigationState.TRACK_PAN_STATE ) {
			processTrackPanMotion();
		}
		else if (navigationState == NavigationState.LOOKAT_STATE) {
			return;
		}
		else {
			processDefaultMotion();
		}
	}
	
	/**
	 * Motion behaviour processing for the examine mode.
	 */
	private void processExamineRadialMotion( ) {
		
		// Check to see if the radius is changing at all. If so, recalculate
		// for this frame
		
		if(dragTranslationAmt.z != 0) {
			
			float x, y, z;
			viewTg.getTransform(viewTx);
			viewTx.get(locationVector);
			
			x = locationVector.x - centerOfRotation.x;
			y = locationVector.y - centerOfRotation.y;
			z = locationVector.z - centerOfRotation.z;
			
			// the radial motion amount (dragTranslationAmt.z) is a number of
			// wheel clicks. we transform that into a fraction to be added
			// to the current radius
			double radial_fraction = dragTranslationAmt.z/10;
			tmpVector.set( x, y, z );
			tmpVector.normalize( );
			
			rotationRadius = (float)(Math.sqrt(x * x + y * y + z * z) * (1 + radial_fraction));
			
			locationVector.x = centerOfRotation.x + rotationRadius * tmpVector.x;
			locationVector.y = centerOfRotation.y + rotationRadius * tmpVector.y;
			locationVector.z = centerOfRotation.z + rotationRadius * tmpVector.z;
			
			viewTx.setTranslation( locationVector );
			
			if (currentViewpoint != null) {
				currentViewpoint.setNavigationTransform(viewTx);
			}
			if (viewTg.isLive()) {
				viewTg.boundsChanged(this);
			} else {
				updateNodeBoundsChanges(viewTg);
			}
			dragTranslationAmt.z = 0;
		}
		
		startFrameDurationCalc = System.currentTimeMillis();
	}
	
	/**
	 * Motion behaviour processing for the examine mode.
	 */
	private void processTrackExamineRadialMotion( ) {
		
		// Check to see if the radius is changing at all. If so, recalculate
		// for this frame
		
		if(dragTranslationAmt.z != 0) {
			
			float x, y, z;
			viewTg.getTransform(viewTx);
			viewTx.get(locationVector);
			
			x = locationVector.x - centerOfRotation.x;
			y = locationVector.y - centerOfRotation.y;
			z = locationVector.z - centerOfRotation.z;
			
			tmpVector.set( x, y, z );
			tmpVector.normalize( );
			
			rotationRadius = (float)(Math.sqrt(x * x + y * y + z * z) + dragTranslationAmt.z);
			
			locationVector.x = centerOfRotation.x + rotationRadius * tmpVector.x;
			locationVector.y = centerOfRotation.y + rotationRadius * tmpVector.y;
			locationVector.z = centerOfRotation.z + rotationRadius * tmpVector.z;
			
			viewTx.setTranslation( locationVector );
			
			if (currentViewpoint != null) {
				currentViewpoint.setNavigationTransform(viewTx);
			}
			if (viewTg.isLive()) {
				viewTg.boundsChanged(this);
			} else {
				updateNodeBoundsChanges(viewTg);
			}
			dragTranslationAmt.z = 0;
		}
	}
	
	/**
	 * Motion behaviour processing for the examine mode.
	 */
	private void processTrackExamineMotion() {
		
		if( ( inputRotationY != 0 ) || ( inputRotationX != 0 ) ) {
			
			double theta_Y = inputRotationY * Math.PI;
			double theta_X = inputRotationX * Math.PI;
			
			viewTg.getTransform(viewTx);
			viewTx.get(locationVector);
			
			upVector.x = viewTx.m01;
			upVector.y = viewTx.m11;
			upVector.z = viewTx.m21;
			upVector.normalize( );
			
			vrtRot.set( upVector, (float)-theta_Y );
			rotMat1.set( vrtRot );
			
			rightVector.x = viewTx.m00;
			rightVector.y = viewTx.m10;
			rightVector.z = viewTx.m20;
			rightVector.normalize( );
			
			hrzRot.set( rightVector, (float)theta_X );
			rotMat2.set( hrzRot );
			
			rotMat1.mul( rotMat2 );
			
			locationVector.x -= centerOfRotation.x;
			locationVector.y -= centerOfRotation.y;
			locationVector.z -= centerOfRotation.z;
			
			rotMat1.transform( locationVector );
			
			locationPoint.x = locationVector.x + centerOfRotation.x;
			locationPoint.y = locationVector.y + centerOfRotation.y;
			locationPoint.z = locationVector.z + centerOfRotation.z;
			
			matrixUtils.lookAt(locationPoint, centerOfRotation, upVector, viewTx);
			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null) {
				currentViewpoint.setNavigationTransform(viewTx);
			}
			if (viewTg.isLive()) {
				viewTg.boundsChanged(this);
			} else {
				updateNodeBoundsChanges(viewTg);
			}
			inputRotationX = 0;
			inputRotationY = 0;
		}
	}
	
	/**
	 * Motion behaviour processing for the pan mode.
	 */
	private void processTrackPanMotion() {
		
		if ((dragTranslationAmt.x != 0) || (dragTranslationAmt.y != 0)) {
			
			viewTg.getTransform(viewTx);
			viewTx.get(locationVector);
			
			upVector.x = viewTx.m01;
			upVector.y = viewTx.m11;
			upVector.z = viewTx.m21;
			upVector.normalize( );
			
			tmpVector.set( upVector );
			
			tmpVector.scale( dragTranslationAmt.y );
			locationVector.add( tmpVector );
			
			rightVector.x = viewTx.m00;
			rightVector.y = viewTx.m10;
			rightVector.z = viewTx.m20;
			rightVector.normalize( );
			
			rightVector.scale( dragTranslationAmt.x );
			locationVector.add( rightVector );
			
			centerOfRotation.add( tmpVector );
			centerOfRotation.add( rightVector );
			
			locationPoint.set( locationVector );
			
			matrixUtils.lookAt(locationPoint, centerOfRotation, upVector, viewTx);
			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null) {
				currentViewpoint.setNavigationTransform(viewTx);
			}
			if (viewTg.isLive()) {
				viewTg.boundsChanged(this);
			} else {
				updateNodeBoundsChanges(viewTg);
			}
			dragTranslationAmt.set( 0, 0, 0 );
		}
	}
	
	/**
	 * Motion behaviour processing for the examine mode.
	 */
	private void processExamineMotion() {
		
		if( ( inputRotationY != 0 ) || ( inputRotationX != 0 ) ) {
			
			double theta_Y = inputRotationY * Math.PI * 2 *
				frameDuration / (orbitTime * 1000);
			double theta_X = inputRotationX * Math.PI * 2 *
				frameDuration / (orbitTime * 1000);
			
			viewTg.getTransform(viewTx);
			viewTx.get(locationVector);
			
			upVector.x = viewTx.m01;
			upVector.y = viewTx.m11;
			upVector.z = viewTx.m21;
			upVector.normalize( );
			
			vrtRot.set( upVector, (float)-theta_Y );
			rotMat1.set( vrtRot );
			
			rightVector.x = viewTx.m00;
			rightVector.y = viewTx.m10;
			rightVector.z = viewTx.m20;
			rightVector.normalize( );
			
			hrzRot.set( rightVector, (float)theta_X );
			rotMat2.set( hrzRot );
			
			rotMat1.mul( rotMat2 );
			
			locationVector.x -= centerOfRotation.x;
			locationVector.y -= centerOfRotation.y;
			locationVector.z -= centerOfRotation.z;
			
			rotMat1.transform( locationVector );
			
			locationPoint.x = locationVector.x + centerOfRotation.x;
			locationPoint.y = locationVector.y + centerOfRotation.y;
			locationPoint.z = locationVector.z + centerOfRotation.z;
			
			matrixUtils.lookAt(locationPoint, centerOfRotation, upVector, viewTx);
			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null)
				currentViewpoint.setNavigationTransform(viewTx);
			
			if (viewTg.isLive())
				viewTg.boundsChanged(this);
			else
				updateNodeBoundsChanges(viewTg);
		}
		
		startFrameDurationCalc = System.currentTimeMillis();
	}
	
	/**
	 * Motion behaviour processing for the inspect mode (formerly the examine mode).
	 */
	private void processInspectMotion() {
		boolean matrix_changed = false;
		double total_theta = lastAngle;
		float x, y, z;
		
		// Check to see if the radius is changing at all. If so, recalculate
		// for this frame
		viewTg.getTransform(viewTx);
		viewTx.get(locationVector);
		
		if(dragTranslationAmt.z != 0) {
			double motionDelay = 0.0005 * frameDuration;
			
			x = locationVector.x - centerOfRotation.x;
			z = locationVector.z - centerOfRotation.z;
			
			//            double local_speed = dragTranslationAmt.z * scaledSpeed;
			double local_speed = dragTranslationAmt.z;
			
			rotationRadius = (float)(Math.sqrt(x * x + z * z) + local_speed);
			
			matrix_changed = true;
		}
		
		if(inputRotationY != 0) {
			// how much of a circle did we take this time? Frame duration in
			// ms but orbit time in seconds. Speed is proportional to the
			// amount of width draging
			//            double local_speed = inputRotationY * scaledSpeed;
			double local_speed = inputRotationY;
			double theta_inc = local_speed * Math.PI * 2 *
				frameDuration / (orbitTime * 1000);
			
			total_theta += theta_inc;
			
			if(total_theta > Math.PI * 2)
				total_theta -= Math.PI * 2;
			
			lastAngle = (float)total_theta;
			
			matrix_changed = true;
		}
		
		if(matrix_changed) {
			x = (float)(rotationRadius * Math.cos(total_theta));
			z = (float)(rotationRadius * Math.sin(total_theta));
			
			// just set the x and z position based on the angle. The Y position
			// remains unchanged. This is so you can elevate and orbit looking down.
			// Also means we don't have to worry about divide by zero as the user
			// goes over the poles.
			
			locationPoint.x = centerOfRotation.x + x;
			locationPoint.y = locationVector.y;
			locationPoint.z = centerOfRotation.z + z;
			
			matrixUtils.lookAt(locationPoint, centerOfRotation, Y_UP, viewTx);
			matrixUtils.inverse(viewTx, viewTx);
			
			if (currentViewpoint != null)
				currentViewpoint.setNavigationTransform(viewTx);
			
			if (viewTg.isLive())
				viewTg.boundsChanged(this);
			else
				updateNodeBoundsChanges(viewTg);
		}
		
		startFrameDurationCalc = System.currentTimeMillis();
	}
	
	/**
	 * Normal motion behaviour processing for anything that is not special
	 * cased.
	 */
	private void processDefaultMotion() {
		float motionDelay = 0.005f * frameDuration;
		
		viewTg.getTransform(viewTx);
		
		// Remove last lookDirection
		if (!(lastLookDirection.x == 0 && lastLookDirection.y == 0
			&& lastLookDirection.z == 0)) {
			matrixUtils.setEuler(lastLookDirection, oneFrameRotation);
			viewTx.mul(oneFrameRotation);
		}
		
		matrixUtils.rotateX(inputRotationX * motionDelay, oneFrameRotation);
		viewTx.mul(oneFrameRotation);
		
		//  RotateY:
		matrixUtils.rotateY(inputRotationY * motionDelay, oneFrameRotation);
		viewTx.mul(oneFrameRotation);
		
		//  Translation:
		oneFrameTranslation.set(dragTranslationAmt);
		oneFrameTranslation.scale(motionDelay);
		
		viewTx.transform(oneFrameTranslation);
		
		boolean collision = false;
		
		// If we allow collisions, adjust it for the collision amount
		if(allowCollisions)
			collision = !checkCollisions();
		
		if(allowTerrainFollowing && !collision)
			collision = !checkTerrainFollowing();
		
		if(collision) {
			if(collisionListener != null)
				collisionListener.avatarCollision(collidedObject);
			
			collidedObject = null;
			
			// Z doesn't always stop the user, must clear all
			oneFrameTranslation.z = 0;
			
			oneFrameTranslation.x = 0;
			oneFrameTranslation.y = 0;
			
		}
		
		// Now set the translation amounts that have been adjusted by any
		// collisions.
		viewTranslation.add(oneFrameTranslation);
		viewTx.setTranslation(viewTranslation);
		
		if (!(lookDirection.x == 0 && lookDirection.y == 0 &&
			lookDirection.z == 0)) {
			
			matrixUtils.setEuler(lookDirection, oneFrameRotation);
			lastLookDirection.x = -lookDirection.x;
			lastLookDirection.y = -lookDirection.y;
			lastLookDirection.z = -lookDirection.z;
			viewTx.mul(oneFrameRotation);
		}
		
		if (currentViewpoint != null)
			currentViewpoint.setNavigationTransform(viewTx);
		
		if (viewTg.isLive())
			viewTg.boundsChanged(this);
		else
			updateNodeBoundsChanges(viewTg);
		
		startFrameDurationCalc = System.currentTimeMillis();
	}
	
	/**
	 * Check the terrain following component of the translation for the next
	 * frame. Adjusts the oneFrameTranslation amount depending on the terrain
	 * and step height we encounter at this next location.
	 *
	 * @return true if the terrain following has successfully been applied
	 *   false means a collision.
	 */
	private boolean checkTerrainFollowing() {
		boolean ret_val = true;
		
		// Avoid this calc if already done
		if (!allowCollisions) {
			getLocalToVworld();
			worldEyeTransform.mul(viewTx);
		}
		
		worldEyeTransform.get(locationVector);
		worldEyeTransform.transform(Y_DOWN, downVector);
		
		locationPoint.add(locationVector, oneFrameTranslation);
		
		terrainPicker.origin[0] = (float)(locationVector.x + oneFrameTranslation.x);
		terrainPicker.origin[1] = (float)(locationVector.y + oneFrameTranslation.y);
		terrainPicker.origin[2] = (float)(locationVector.z + oneFrameTranslation.z);
		
		terrainPicker.destination[0] = (float)downVector.x;
		terrainPicker.destination[1] = (float)downVector.y;
		terrainPicker.destination[2] = (float)downVector.z;
		
		ArrayList pick_results = (ArrayList)terrainPicker.foundPaths;
		
		terrain.pickSingle(terrainPicker);
		
		// if there is no ground below us, do nothing.
		if(terrainPicker.pickCount == 0)
			return ret_val;
		
		double shortest_length = Double.POSITIVE_INFINITY;
		boolean found = false;
		
		for(int i = 0; i < terrainPicker.pickCount; i++) {
			
			// Firstly, check the path to see if this is eligible for picking
			// Look at the picked item first, then do a depth traversal of the
			// path from the root down to the the node.
			SceneGraphPath path = (SceneGraphPath)pick_results.get(i);
			
			Node end = path.getTerminalNode();
			Object user_data = end.getUserData();
			
			if(user_data instanceof UserSupplementData &&
				!((UserSupplementData)user_data).isTerrain)
				continue;
			
			int num_path_items = path.getNodeCount();
			Node[] node_list = path.getNodes();
			boolean not_eligible = false;
			
			for(int j = 0; j < num_path_items && !not_eligible; j++) {
				user_data = node_list[j].getUserData();
				
				if(user_data instanceof UserSupplementData)
					not_eligible = !((UserSupplementData)user_data).isTerrain;
			}
			
			if(not_eligible)
				continue;
			
			// So this is ok, let's look at the object to see whether we
			// intersect with the actual geometry.
			path.getTransform(vwTransform);
			
			vwTransform.get(locationVector);
			
			Shape3D i_shape = (Shape3D)path.getTerminalNode();
			
			// Get the user data, if the user data contains a height data
			// source use that to determine the terrain, otherwise pass it
			// through to the geometry intersection handling. Inside that
			// Also check to see what geometry is being used
			user_data = i_shape.getUserData();
			HeightDataSource hds = null;
			GeometryData geom_data = null;
			
			if(user_data instanceof UserSupplementData) {
				UserSupplementData usd = (UserSupplementData)user_data;
				
				if(usd.geometryData instanceof HeightDataSource)
					hds = (HeightDataSource)usd.geometryData;
				else if(usd.geometryData instanceof GeometryData)
					geom_data = (GeometryData)usd.geometryData;
			}
			else if(user_data instanceof HeightDataSource) {
				hds = (HeightDataSource)user_data;
			} else if(user_data instanceof GeometryData)
				geom_data = (GeometryData)user_data;
			
			if(hds != null) {
				intersectionPoint.x = locationVector.x;
				intersectionPoint.y = locationVector.y;
				intersectionPoint.z = hds.getHeight((float)locationVector.x,
					(float)locationVector.y);
			} else {
				// Do we have geometry data to play with at the shape level?
				// If so, use that in preference to going down to the
				// individual geometry arrays of the shape.
				if(geom_data != null) {
					if(terrainIntersect.rayUnknownGeometry(locationPoint,
						downVector,
						0,
						geom_data,
						vwTransform,
						wkPoint,
						false))
					{
						diffVec.sub(locationPoint, wkPoint);
						
						if((shortest_length == -1) ||
							(diffVec.lengthSquared() < shortest_length)) {
							shortest_length = diffVec.lengthSquared();
							intersectionPoint.set(wkPoint);
							collidedObject = path;
						}
					}
				} else {
					Object g = i_shape.getGeometry();
					
					// TODO: Ignore Text for now
					if(g instanceof VertexGeometry)
					{
						VertexGeometry geom = (VertexGeometry)g;
						
						boolean intersect =
							terrainIntersect.rayUnknownGeometry(locationPoint,
							downVector,
							0,
							geom,
							vwTransform,
							wkPoint,
							false);
						
						if(intersect) {
							diffVec.sub(locationPoint, wkPoint);
							float d = diffVec.lengthSquared();
							
							if(d < shortest_length) {
								shortest_length = d;
								intersectionPoint.set(wkPoint);
								collidedObject = path;
								found = true;
							}
						}
					}
				}
			}
		}
		// No intersection!!!! How did that happen? Well, just exit and
		// pretend there was nothing below us
		if(!found)
			return true;
		
		// Is the difference in world Y values greater than the step height?
		// If so, then jump the viewpoint to the new terrain height plus the
		// avatar height above ground. Handles both rising and descending
		// terrain. If the difference is greater than the step height, we set
		// the translation to nothing in the Z direction.
		float terrain_step = (intersectionPoint.y - lastTerrainHeight) * worldScale;
		float height_above_terrain = locationPoint.y - intersectionPoint.y;
		
		// Do we need to adjust the height? If so the check if the height is a
		// step that is too high or not
		if(!floatEq(height_above_terrain - avatarHeight, 0)) {
			if(floatEq(terrain_step, 0)) {
				// Flat surface. Check to see the avatar height is correct
				oneFrameTranslation.y = avatarHeight - height_above_terrain;
				ret_val = true;
			} else if(terrain_step < avatarStep) {
				oneFrameTranslation.y = terrain_step;
				ret_val = true;
			} else {
				// prevent it. Set the transform to 0.
				ret_val = false;
				
				// Don't let lastTerrainHeight get set
				return ret_val;
			}
		}
		lastTerrainHeight = (float)intersectionPoint.y;
		
		return ret_val;
	}
	
	/**
	 * Check the collision detection component of the translation for the next
	 * frame. Basically test for a collision within the given distance that
	 * would be travelled next frame. If nothing is picked then no collision
	 * will occur. If it does find something then obviously a collision will
	 * occurr so you do return a flag to say so.
	 *
	 * @param prefetch True if viewpoint info has already been fetched for
	 *    this frame
	 * @return true if the no collisions detected, false means a collision.
	 */
	private boolean checkCollisions() {
		boolean ret_val = true;
		
		// TODO: This is called 2 times a frame with terrain following, can we stop?
		getLocalToVworld();
		worldEyeTransform.mul(viewTx);
		
		// Where are we now?
		worldEyeTransform.get(locationVector);
		locationPoint.set(locationVector);
		
		/*
		// Where are we going to be soon?
                worldEyeTransform.transform(COLLISION_DIRECTION, collisionVector);

                collisionVector.scale(avatarSize);

                locationEndPoint.add(locationVector, collisionVector);
                locationEndPoint.add(oneFrameTranslation);
        */
		
		// TODO: Look in the direction of movement.  Still not sure this is right
		
		collisionVector.x = oneFrameTranslation.x;
		collisionVector.y = oneFrameTranslation.y;
		collisionVector.z = oneFrameTranslation.z;
		
		if(collisionVector.lengthSquared() > 0)
			collisionVector.normalize();
		
		collisionVector.scale(avatarSize);
		//        collisionVector.scale(0.75f);
		
		locationEndPoint.add(locationVector, collisionVector);
		locationEndPoint.add(oneFrameTranslation);
		
		int num_found = 0;
		
		// We need to transform the end point to the direction that we are
		// currently travelling. At the moment, this always points forward
		// in the same direction as the viewpoint.
		switch(avatarRep) {
		case AVATAR_POINT:
			//System.out.println("Pick: " + locationEndPoint + "-" + locationPoint);
			collidePicker.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
			collidePicker.origin[0] = (float)locationEndPoint.x;
			collidePicker.origin[1] = (float)locationEndPoint.y;
			collidePicker.origin[2] = (float)locationEndPoint.z;
			
			collidePicker.destination[0] = (float)locationPoint.x;
			collidePicker.destination[1] = (float)locationPoint.y;
			collidePicker.destination[2] = (float)locationPoint.z;
			
			collidables.pickSingle(collidePicker);
			break;
			
		case AVATAR_CYLINDER:
			collidePicker.pickGeometryType = PickRequest.PICK_CYLINDER_SEGMENT;
			
			kneePoint.x = locationEndPoint.x;
			
			// Not good for stepHeights > avatarSize
			kneePoint.y = locationEndPoint.y - avatarHeight + avatarStep;
			kneePoint.z = locationEndPoint.z;
			
			collidePicker.origin[0] = (float)locationEndPoint.x;
			collidePicker.origin[1] = (float)locationEndPoint.y;
			collidePicker.origin[2] = (float)locationEndPoint.z;
			
			collidePicker.destination[0] = (float)kneePoint.x;
			collidePicker.destination[1] = (float)kneePoint.y;
			collidePicker.destination[2] = (float)kneePoint.z;
			
			collidePicker.additionalData = avatarSize;
			
			collidables.pickSingle(collidePicker);
			break;
			
		case AVATAR_SHOULDERS:
			
			collidePicker.pickGeometryType = PickRequest.PICK_CYLINDER_SEGMENT;
			
			float center = locationPoint.x;
			
			locationPoint.x -= avatarSize;
			// A small shoulder cone
			kneePoint.x = center + avatarSize;
			
			kneePoint.y = locationEndPoint.y - avatarSize / 2;
			kneePoint.z = locationEndPoint.z;
			
			collidePicker.origin[0] = (float)locationEndPoint.x;
			collidePicker.origin[1] = (float)locationEndPoint.y;
			collidePicker.origin[2] = (float)locationEndPoint.z;
			
			collidePicker.destination[0] = (float)kneePoint.x;
			collidePicker.destination[1] = (float)kneePoint.y;
			collidePicker.destination[2] = (float)kneePoint.z;
			
			collidePicker.additionalData = avatarSize;
			
			collidables.pickSingle(collidePicker);
			break;
		}
		
		// if there is no ground below us, do nothing.
		if(collidePicker.pickCount == 0)
			return ret_val;
		
		boolean real_collision = false;
		float length = (float)collisionVector.length();
		ArrayList pick_results = (ArrayList)collidePicker.foundPaths;
		
		for(int i = 0; (i < collidePicker.pickCount) && !real_collision; i++) {
			// Firstly, check the path to see if this is eligible for picking
			// Look at the picked item first, then do a depth traversal of the
			// path from the root down to the the node.
			SceneGraphPath path = (SceneGraphPath)pick_results.get(i);
			
			Node end = path.getTerminalNode();
			Object user_data = end.getUserData();
			
			if(user_data instanceof UserSupplementData &&
				!((UserSupplementData)user_data).collidable)
				continue;
			
			int num_path_items = path.getNodeCount();
			Node[] node_list = path.getNodes();
			boolean not_eligible = false;
			
			for(int j = 0; j < num_path_items && !not_eligible; j++) {
				user_data = node_list[j].getUserData();
				
				if(user_data instanceof UserSupplementData)
					not_eligible = !((UserSupplementData)user_data).collidable;
			}
			
			if(not_eligible)
				continue;
			
			// OK, so we collided on the bounds, lets check on the geometry
			// directly to see if we had a real collision. Java3D just gives
			// us the collision based on the bounding box intersection. We
			// might actually have just walked through something like an
			// archway.
			path.getTransform(vwTransform);
			
			Shape3D i_shape = (Shape3D)path.getTerminalNode();
			Object g = i_shape.getGeometry();
			
			// TODO: Ignore Text and other geometry for now
			if(g instanceof VertexGeometry)
			{
				VertexGeometry geom = (VertexGeometry)g;
				GeometryData geom_data = null;
				
				user_data = geom.getUserData();
				geom_data = null;
				
				if(user_data instanceof UserSupplementData) {
					UserSupplementData usd = (UserSupplementData)user_data;
					
					if(usd.geometryData instanceof GeometryData)
						geom_data = (GeometryData)usd.geometryData;
				} else if(user_data instanceof GeometryData)
					geom_data = (GeometryData)user_data;
				
				// Ah, finally. This is where we do the intersection
				// testing against either the raw geometry or the object.
				if(geom_data != null) {
					real_collision =
						terrainIntersect.rayUnknownGeometry(locationPoint,
						collisionVector,
						length,
						geom_data,
						vwTransform,
						wkPoint,
						true);
					
					/*
					if(real_collision)
                                            System.out.println("head collided:  dir: " + collisionVector + " cpnt: " + wkPoint);
                    */
					
					// Fake CylinderSegment test with a second ray at kneePnt
					if (!real_collision && avatarRep == AVATAR_CYLINDER) {
						real_collision =
							terrainIntersect.rayUnknownGeometry(kneePoint,
							collisionVector,
							length,
							geom_data,
							vwTransform,
							wkPoint,
							true);
						if(real_collision)
							System.out.println("knee collided");
					}
					
					if (!real_collision && avatarRep == AVATAR_SHOULDERS) {
						// Test second shoulder
						real_collision =
							terrainIntersect.rayUnknownGeometry(kneePoint,
							collisionVector,
							length,
							geom_data,
							vwTransform,
							wkPoint,
							true);
						if (real_collision)
							System.out.println("right shoulder collided");
					}
				} else {
					real_collision =
						terrainIntersect.rayUnknownGeometry(locationPoint,
						collisionVector,
						length,
						geom,
						vwTransform,
						wkPoint,
						true);
					/*
					if(real_collision)
                                                System.out.println("head collided");
                    */
					
					// Fake CylinderSegment test with a second ray at kneePnt
					if (!real_collision && avatarRep == AVATAR_CYLINDER) {
						real_collision =
							terrainIntersect.rayUnknownGeometry(kneePoint,
							collisionVector,
							length,
							geom,
							vwTransform,
							wkPoint,
							true);
						if (real_collision)
							System.out.println("knee collided");
					}
					
					if (!real_collision && avatarRep == AVATAR_SHOULDERS) {
						// Test second shoulder
						real_collision =
							terrainIntersect.rayUnknownGeometry(kneePoint,
							collisionVector,
							length,
							geom,
							vwTransform,
							wkPoint,
							true);
						if (real_collision)
							System.out.println("right shoulder collided");
					}
					
				}
				
				ret_val = !real_collision;
				
				if(real_collision)
					collidedObject = path;
			}
			/*
			else
                        {
            Process text and volume geometry here
                        }
            */
		}
		pick_results.clear();
		
		return ret_val;
	}
	
	/**
	 * Check the terrain height at the current position. This is done when
	 * we first start moving a viewpoint with a mouse press.
	 *
	 * @return true if the terrain following has successfully been applied
	 *   false means a collision.
	 */
	private void setInitialTerrainHeight() {
		if(terrain == null)
			return;
		
		getLocalToVworld();
		worldEyeTransform.mul(viewTx);
		
		worldEyeTransform.get(locationVector);
		worldEyeTransform.transform(Y_DOWN, downVector);
		
		terrainPicker.origin[0] = (float)(locationVector.x);
		terrainPicker.origin[1] = (float)(locationVector.y);
		terrainPicker.origin[2] = (float)(locationVector.z);
		
		terrainPicker.destination[0] = (float)downVector.x;
		terrainPicker.destination[1] = (float)downVector.y;
		terrainPicker.destination[2] = (float)downVector.z;
		
		locationPoint.set(locationVector);
		
		terrain.pickSingle(terrainPicker);
		
		// if there is no ground below us, do nothing.
		if(terrainPicker.pickCount == 0)
			return;
		
		double shortest_length = Double.POSITIVE_INFINITY;
		boolean found = false;
		
		ArrayList pick_results = (ArrayList)terrainPicker.foundPaths;
		
		for(int i = 0; i < terrainPicker.pickCount; i++) {
			// Firstly, check the path to see if this is eligible for picking.
			SceneGraphPath path = (SceneGraphPath)pick_results.get(i);
			
			Node end = path.getTerminalNode();
			Object user_data = end.getUserData();
			
			if(user_data instanceof UserSupplementData &&
				!((UserSupplementData)user_data).isTerrain)
				continue;
			
			int num_path_items = path.getNodeCount();
			Node[] node_list = path.getNodes();
			boolean not_eligible = false;
			
			for(int j = 0; j < num_path_items && !not_eligible; j++) {
				user_data = node_list[j].getUserData();
				
				if(user_data instanceof UserSupplementData)
					not_eligible = !((UserSupplementData)user_data).isTerrain;
			}
			
			if(not_eligible)
				continue;
			
			
			path.getTransform(vwTransform);
			vwTransform.get(locationVector);
			
			Shape3D i_shape = (Shape3D)path.getTerminalNode();
			
			// Get the user data, if the user data contains a height data
			// source use that to determine the terrain, otherwise pass it
			// through to the geometry intersection handling. Inside that
			// Also check to see what geometry is being used
			user_data = i_shape.getUserData();
			HeightDataSource hds = null;
			GeometryData geom_data = null;
			
			if(user_data instanceof UserSupplementData) {
				UserSupplementData usd = (UserSupplementData)user_data;
				
				if(usd.geometryData instanceof HeightDataSource)
					hds = (HeightDataSource)usd.geometryData;
				else if(usd.geometryData instanceof GeometryData)
					geom_data = (GeometryData)usd.geometryData;
			} else if(user_data instanceof HeightDataSource) {
				hds = (HeightDataSource)user_data;
			} else if(user_data instanceof GeometryData)
				geom_data = (GeometryData)user_data;
			
			if(hds != null) {
				intersectionPoint.x = locationVector.x;
				intersectionPoint.y = locationVector.y;
				intersectionPoint.z = hds.getHeight(locationVector.x,
					locationVector.y);
			} else {
				Object g = i_shape.getGeometry();
				
				// TODO: Ignore Text for now
				if(g instanceof VertexGeometry)
				{
					VertexGeometry geom = (VertexGeometry)g;
					
					boolean intersect =
						terrainIntersect.rayUnknownGeometry(locationPoint,
						downVector,
						0,
						geom,
						vwTransform,
						wkPoint,
						false);
					
					if(intersect) {
						found = true;
						diffVec.sub(locationPoint, wkPoint);
						
						float d = diffVec.lengthSquared();
						if(d < shortest_length) {
							shortest_length = d;
							intersectionPoint.set(wkPoint);
						}
					}
				}
				/*
				else
                                {
                do text and volume processing here
                                }
                */
			}
		}
		
		
		// No intersection!!!! How did that happen? Well, just exit and
		// pretend there was nothing below us
		if(found) {
			lastTerrainHeight = (float)intersectionPoint.y;
		}
	}
	
	/**
	 * Compares to floats to determine if they are equal or very close
	 *
	 * @param val1 The first value to compare
	 * @param val2 The second value to compare
	 * @return True if they are equal within the given epsilon
	 */
	private boolean floatEq(double val1, double val2) {
		double diff = val1 - val2;
		
		if(diff < 0)
			diff *= -1;
		
		return (diff < ZEROEPS);
	}
	
	/**
	 * Convenience method to walk the viewPath and calculate the root to
	 * virtual world coordinate location of the viewpoint.
	 */
	private void getLocalToVworld() {
		int num_nodes = viewPath.getNodeCount();
		Node[] nodes = viewPath.getNodes();
		worldEyeTransform.setIdentity();
		
		// use vwTransform for fetching the tx. It is only a temp var anyway
		for(int i = 0; i < num_nodes - 1; i++) {
			if(!(nodes[i] instanceof TransformGroup))
				continue;
			
			TransformGroup tg = (TransformGroup)nodes[i];
			tg.getTransform(vwTransform);
			worldEyeTransform.mul(vwTransform);
		}
	}
}
