/**
 * Handler for the toy car model processing. Takes the output from the collision
 * detection system and applies it to the physics model.
 */
import org.web3d.x3d.sai.*;

import java.util.HashSet;
import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class CarContactProcessor implements X3DFieldEventListener {

    /**
     * The set of parameters we want to apply to ground contacts with
     * the wheels.
     */
    private static final String[] WHEEL_GROUND_PARAMS = {
        "BOUNCE", "SLIP-2", "USER-FRICTION"
    };

    /**
     * The set of parameters we want to apply to any other geometry contacts
     * with the wheels or chassis geom.
     */
    private static final String[] WHEEL_GEOM_PARAMS = {
        "BOUNCE", "ERROR_REDUCTION", "CONSTANT_FORCE"
    };

    /** Input field with the contacts from the collision system */
    private MFNode contactInput;

    /** Output field for sending contacts to the rigid body system */
    private MFNode contactOutput;

    /** Input holding the last valid front wheel orientation axleVector */
    private SFVec3f frontWheelOrientation;

    /** Input holding the last valid rear wheel orientation axleVector */
    private SFVec3f rearWheelOrientation;

    /** Input holding the current body rotation */
    private SFRotation bodyOrientation;

    /** Collection of everything that is terrain */
    private HashSet terrainObjects;

    /** Node representing front left wheel */
    private X3DNode frontLeftWheel;

    /** Node representing front right wheel */
    private X3DNode frontRightWheel;

    /** Node representing back left wheel */
    private X3DNode backLeftWheel;

    /** Node representing back right wheel */
    private X3DNode backRightWheel;

    /** Node representing the chassis */
    private X3DNode chassis;

    /** Working values get set here to pass to/from node fields */
    private float[] fieldValues;

    /** An array used to transfer contact lists */
    private X3DNode[] processedContacts;

    /** Utility for deriving the friction direction */
    private AxisAngle4f orientation;

    /** Rotation matrix from axisAngle */
    private Matrix3f rotation;

    /** Vector to be transformed */
    private Vector3f axleVector;

    /** Vector to be transformed */
    private Vector3f bodyVector;

    /**
     * Output from the physics model indicating the car velocity
     * calculated from the last frame.
     */
    private SFVec3f carVelocity;


    /**
     * Create a new car control script.
     */
    public CarContactProcessor() {
        terrainObjects = new HashSet();
        fieldValues = new float[4];
        processedContacts = new X3DNode[1024];
        orientation = new AxisAngle4f();
        rotation = new Matrix3f();
        axleVector = new Vector3f();
        bodyVector = new Vector3f();
    }

    /**
     * Initialise everything in the script now. All the nodes are valid, so
     * let's go for it.
     */
    public void initialize(Map fields) {

        contactInput = (MFNode)fields.get("collisionContacts");
        contactInput.addX3DEventListener(this);

        contactOutput = (MFNode)fields.get("correctedContacts");

        bodyOrientation = (SFRotation)fields.get("carOrientation");
        rearWheelOrientation = (SFVec3f)fields.get("rearWheelOrientation");
        frontWheelOrientation = (SFVec3f)fields.get("frontWheelOrientation");

        carVelocity = (SFVec3f)fields.get("carVelocity");

        MFNode terrain_field = (MFNode)fields.get("terrain");
        int num_terrain = terrain_field.size();

        for(int i = 0; i < num_terrain; i++)
            terrainObjects.add(terrain_field.get1Value(i));

        SFNode wheel = (SFNode)fields.get("frontLeftWheel");
        frontLeftWheel = wheel.getValue();

        wheel = (SFNode)fields.get("frontRightWheel");
        frontRightWheel = wheel.getValue();

        wheel = (SFNode)fields.get("backLeftWheel");
        backLeftWheel = wheel.getValue();

        wheel = (SFNode)fields.get("backRightWheel");
        backRightWheel = wheel.getValue();

        wheel = (SFNode)fields.get("chassis");
        chassis = wheel.getValue();
    }

    public void shutdown() {
        contactInput.removeX3DEventListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    /**
     * Process an incoming field event now. Since this is only for the contact
     * list, just head straight into processing that.
     */
    public void readableFieldChanged(X3DFieldEvent evt) {
        int num_contacts = contactInput.size();

        if(processedContacts.length < num_contacts)
            processedContacts = new X3DNode[num_contacts];

        int contact_count = 0;

//System.out.println("num contacts " + num_contacts);
        for(int i = 0; i < num_contacts; i++) {
            X3DNode contact = contactInput.get1Value(i);

            SFNode nf = (SFNode)contact.getField("geometry1");
            X3DNode geom1 = nf.getValue();

            nf = (SFNode)contact.getField("geometry2");
            X3DNode geom2 = nf.getValue();

            // Check to see if we have a contact with the ground. If we do,
            // then that's most likely a wheel, so we want to do some
            // processing on it to set the friction direction and slip-2 value.
            // If so, process and then immediately move onto the next contact.
            if(terrainObjects.contains(geom1) ||
               terrainObjects.contains(geom2)) {

                if(geom1.equals(chassis) || geom2.equals(chassis)) {
//System.out.println("geom hit the ground " + i + " of " + num_contacts);
                    continue;
                } else if(geom1.equals(frontLeftWheel) ||
                          geom1.equals(frontRightWheel) ||
                          geom2.equals(frontLeftWheel) ||
                          geom2.equals(frontRightWheel)) {

                    // Calculate the rotation matrix which describes the
                    // wheel relative to the car body.

                    SFVec3f f_dir =
                        (SFVec3f)contact.getField("frictionDirection");
                    SFVec2f slip = (SFVec2f)contact.getField("slipCoefficients");

                    carVelocity.getValue(fieldValues);

                    float x = fieldValues[0];
                    float y = fieldValues[1];
                    float z = fieldValues[2];
                    float speed = (float)Math.sqrt(x * x + y * y + z * z);

                    fieldValues[0] = 0;
                    fieldValues[1] = 0.004f * speed;
                    slip.setValue(fieldValues);

                    MFString params =
                        (MFString)contact.getField("appliedParameters");

                    params.setValue(WHEEL_GROUND_PARAMS.length,
                                    WHEEL_GROUND_PARAMS);

                    SFVec3f lin_v = null;
                    if(terrainObjects.contains(geom1))  {
                        SFNode val = (SFNode)contact.getField("body2");
                        X3DNode node = val.getValue();
                        lin_v = (SFVec3f)node.getField("linearVelocity");
                    } else {
                        SFNode val = (SFNode)contact.getField("body1");
                        X3DNode node = val.getValue();
                        lin_v = (SFVec3f)node.getField("linearVelocity");
                    }
                    lin_v.getValue(fieldValues);
                    f_dir.setValue(fieldValues);
                } else if(geom1.equals(backLeftWheel) ||
                          geom1.equals(backRightWheel) ||
                          geom2.equals(backLeftWheel) ||
                          geom2.equals(backRightWheel)) {

                    SFVec3f f_dir =
                        (SFVec3f)contact.getField("frictionDirection");
                    SFVec2f slip = (SFVec2f)contact.getField("slipCoefficients");

                    carVelocity.getValue(fieldValues);
                    f_dir.setValue(fieldValues);

                    float x = fieldValues[0];
                    float y = fieldValues[1];
                    float z = fieldValues[2];
                    float speed = (float)Math.sqrt(x * x + y * y + z * z);

                    fieldValues[0] = 0;
                    fieldValues[1] = 0.004f * speed;
                    slip.setValue(fieldValues);

                    MFString params =
                        (MFString)contact.getField("appliedParameters");

                    params.setValue(WHEEL_GROUND_PARAMS.length,
                                    WHEEL_GROUND_PARAMS);

                    SFVec3f lin_v = null;
                    if(terrainObjects.contains(geom1))  {
                        SFNode val = (SFNode)contact.getField("body2");
                        X3DNode node = val.getValue();
                        lin_v = (SFVec3f)node.getField("linearVelocity");
                    } else {
                        SFNode val = (SFNode)contact.getField("body1");
                        X3DNode node = val.getValue();
                        lin_v = (SFVec3f)node.getField("linearVelocity");
                    }
                    lin_v.getValue(fieldValues);
                    f_dir.setValue(fieldValues);
                }

                processedContacts[contact_count++] = contact;
                continue;
            }

            // So it wasn't the ground we hit, maybe it was an object. Now
            // check the chassis and wheels for collisions and see what they
            // hit. If they hit it, it'll be really soft, so make it bounce a
            // long way with the physics parameters.
            if(chassis.equals(geom1)) {
                SFFloat bounce = (SFFloat)contact.getField("bounce");
                bounce.setValue(0.95f);

                SFFloat b_speed = (SFFloat)contact.getField("minBounceSpeed");
                b_speed.setValue(0.2f);

                SFVec2f coeff = (SFVec2f)contact.getField("frictionCoefficients");
                fieldValues[0] = 0;
                fieldValues[1] = 0;
                coeff.setValue(fieldValues);

                processedContacts[contact_count++] = contact;
                continue;
            } else if(chassis.equals(geom2)) {
                SFFloat bounce = (SFFloat)contact.getField("bounce");
                bounce.setValue(0.1f);

                SFFloat b_speed = (SFFloat)contact.getField("minBounceSpeed");
                b_speed.setValue(0.2f);

                SFVec2f coeff = (SFVec2f)contact.getField("frictionCoefficients");
                fieldValues[0] = 0;
                fieldValues[1] = 0;
                coeff.setValue(fieldValues);

                processedContacts[contact_count++] = contact;
                continue;
            }

            if(geom2.equals(frontLeftWheel) ||
               geom2.equals(frontRightWheel) ||
               geom2.equals(backLeftWheel) ||
               geom2.equals(backRightWheel)) {

                // something hit us, make it _really_ bounce
                SFFloat bounce = (SFFloat)contact.getField("bounce");
                bounce.setValue(0.95f);

                SFFloat b_speed = (SFFloat)contact.getField("minBounceSpeed");
                b_speed.setValue(0.2f);

                SFVec2f coeff = (SFVec2f)contact.getField("frictionCoefficients");
                fieldValues[0] = 0;
                fieldValues[1] = 0;
                coeff.setValue(fieldValues);

                SFFloat erp = (SFFloat)contact.getField("softnessErrorCorrection");
                erp.setValue(0.00001f);

                SFFloat cfm = (SFFloat)contact.getField("softnessConstantForceMix");
                erp.setValue(1);

                MFString params = (MFString)contact.getField("appliedParameters");
                params.setValue(WHEEL_GEOM_PARAMS.length, WHEEL_GEOM_PARAMS);

                processedContacts[contact_count++] = contact;
            }
        }

        // Now write it all to the output
        if(contact_count != 0)
            contactOutput.setValue(contact_count, processedContacts);
    }
}