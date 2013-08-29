/**
 * Handler for the toy car model processing. Takes the body velocity and
 * orientation and applies some damping on it - particularly to limit the
 * car from tipping over past the vertical.
 */
import org.web3d.x3d.sai.*;

import java.util.Map;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class CarDampingProcessor {

    /** Amount of damping to apply to linear velocities */
    private static final float LINEAR_DAMPING_FACTOR = 0.0001f;

    /** Amount of damping to apply to angular velocities */
    private static final float ANGULAR_DAMPING_FACTOR = 0.001f;

    /** Working values get set here to pass to/from node fields */
    private float[] fieldValues;

    /** The chassis object */
    private X3DNode chassisBody;

    /** Nodes for the rest of the car body */
    private X3DNode[] otherBodies;

    /** Orientation field for the chassis */
    private SFRotation chassisOrientation;

    /** Angular velocity field of the chassis */
    private SFVec3f chassisAngularVelocity;

    /** Total number of bodies */
    private int numBodies;

    /** Orientation field for each body */
    private SFVec3f[] linearVelocities;

    /** Orientation field for each body */
    private SFVec3f[] angularVelocities;

    /** Orientation field for each body */
    private MFVec3f[] forces;

    /** Orientation field for each body */
    private MFVec3f[] torques;

    /** Utility for deriving the friction direction */
    private AxisAngle4f orientation;

    /** Rotation matrix from axisAngle */
    private Matrix3f rotation;

    /** Vector to be transformed */
    private Vector3f upVector;

    /** Vector to be transformed */
    private Vector3f bodyVector;

    /**
     * Create a new car control script.
     */
    public CarDampingProcessor() {
        fieldValues = new float[4];
        orientation = new AxisAngle4f();
        rotation = new Matrix3f();
        upVector = new Vector3f(0, 1, 0);
        bodyVector = new Vector3f();
    }

    /**
     * Initialise everything in the script now. All the nodes are valid, so
     * let's go for it.
     */
    public void initialize(Map fields) {

        MFNode bodies_field = (MFNode)fields.get("bodies");
        numBodies = bodies_field.size();

        // Assume body[0] is the chassis reference
        otherBodies = new X3DNode[numBodies - 1];
        linearVelocities = new SFVec3f[numBodies];
        angularVelocities = new SFVec3f[numBodies];
        forces = new MFVec3f[numBodies];
        torques = new MFVec3f[numBodies];

        chassisBody = bodies_field.get1Value(0);
        linearVelocities[0] = (SFVec3f)chassisBody.getField("linearVelocity");
        angularVelocities[0] = (SFVec3f)chassisBody.getField("angularVelocity");
        forces[0] = (MFVec3f)chassisBody.getField("forces");
        torques[0] = (MFVec3f)chassisBody.getField("torques");

        for(int i = 1; i < numBodies; i++) {
            otherBodies[i - 1] = bodies_field.get1Value(i);
            linearVelocities[i] = (SFVec3f)otherBodies[i - 1].getField("linearVelocity");
            angularVelocities[i] = (SFVec3f)otherBodies[i - 1].getField("angularVelocity");
            forces[i] = (MFVec3f)otherBodies[i - 1].getField("forces");
            torques[i] = (MFVec3f)otherBodies[i - 1].getField("torques");
        }

        chassisOrientation = (SFRotation)chassisBody.getField("orientation");
        chassisAngularVelocity = (SFVec3f)chassisBody.getField("angularVelocity");
    }

    public void shutdown() {
    }

    public void processDamping() {
        // Fetch the velocity of each of the bodies and damp them.
        for(int i = 0; i < numBodies; i++) {
            linearVelocities[i].getValue(fieldValues);
            fieldValues[0] *= -1 * LINEAR_DAMPING_FACTOR;
            fieldValues[1] *= -1 * LINEAR_DAMPING_FACTOR;
            fieldValues[2] *= -1 * LINEAR_DAMPING_FACTOR;
            forces[i].setValue(1, fieldValues);

            angularVelocities[i].getValue(fieldValues);
            fieldValues[0] *= -1 * ANGULAR_DAMPING_FACTOR;
            fieldValues[1] *= -1 * ANGULAR_DAMPING_FACTOR;
            fieldValues[2] *= -1 * ANGULAR_DAMPING_FACTOR;
            torques[i].setValue(1, fieldValues);
        }

/*
        // Check on the main chassis orientation and limit it. We don't want
        // the vertical vector going past 45 degrees.
        chassisOrientation.getValue(fieldValues);

        orientation.set(fieldValues);
        rotation.set(orientation);
        bodyVector.set(0, 1, 0);
        rotation.transform(bodyVector);

        if(bodyVector.angle(upVector) > Math.PI / 3) {
            // Cross product to generate the axis to rotate around.
            bodyVector.cross(upVector, bodyVector);
            fieldValues[0] = bodyVector.x;
            fieldValues[1] = bodyVector.y;
            fieldValues[2] = bodyVector.z;
            fieldValues[3] = (float)(Math.PI / 3);

            chassisOrientation.setValue(fieldValues);
            fieldValues[0] = 0;
            fieldValues[1] = 0;
            fieldValues[2] = 0;
            chassisAngularVelocity.setValue(fieldValues);
        }
*/
    }
}