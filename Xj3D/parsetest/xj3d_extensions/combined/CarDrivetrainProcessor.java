/**
 * Handler for the toy car model processing. Takes the output from the control device
 * and applies it to the physics model.
 */
import org.web3d.x3d.sai.*;

import java.util.Map;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

public class CarDrivetrainProcessor implements X3DFieldEventListener {

    /** Field for steering wheel input from the sensor */
    private SFFloat wheelDirectionInput;

    /** Field for accelerator pedal input */
    private SFFloat wheelAcceleratorInput;

    /** Has someone selected reverse gear? */
    private SFBool reverseSelectedInput;

    /** Input holding the last valid front wheel orientation vector */
    private SFVec3f frontWheelOrientation;

    /** Input holding the last valid rear wheel orientation vector */
    private SFVec3f rearWheelOrientation;

    /** Steering angle input from the joint */
    private SFFloat steeringAngleInput;

    /** Output to turn the front wheels */
    private SFFloat frontWheelMinStop;

    /** Output to turn the front wheels */
    private SFFloat frontWheelMaxStop;

    /** Steering velocity to send to the front wheel hinges */
    private SFFloat frontWheelSteerSpeed;

    /** Motor velocity applied to the rear wheel. */
    private SFFloat rearWheelSpeed;

    /** Motor torque applied to the front wheel. */
    private SFFloat frontWheelTorque;

    /** Motor torque applied to the rear wheel. */
    private SFFloat rearWheelTorque;

    /** Output direction for the axles on the front */
    private SFVec3f frontAxleAxis;

    /** Output direction for the axles on the rear */
    private SFVec3f rearAxleAxis;

    /** Field for the brake bias value */
    private SFFloat brakeBiasInput;

    /** Field for the engine bias value */
    private SFFloat engineBiasInput;

    /** Field for the drag coefficient value */
    private SFFloat dragCoefficientInput;

    /**
     * Output from the physics model indicating the car velocity
     * calculated from the last frame.
     */
    private SFVec3f carVelocity;

    /** Output of the calculated forces on the car - mainly air resistance */
    private MFVec3f outputForces;

    /** Amount of dead spot in the center of the steering */
    private float steeringDeadspot;

    /** The currently set amount of steering input */
    private float currentSteering;

    /** Maximum limit we'll let the user turn the wheel */
    private float steeringLimit;

    /**
     * Constant multiplication factor to adjust the control input value for
     * steering (in the range -1 to +1) to give the steering limits.
     */
    private float steeringCorrectionFactor;

    /** Maximum speed we'll let the user drive */
    private float speedLimit;

    /** Maximum amount of torque the engine could provide to each wheel */
    private float maxEngineTorque;

    /** Maximum amount of torque brakes can apply to each wheel */
    private float maxBrakingTorque;

    /** Bias of braking from the amount on the front versus back wheels */
    private float brakeBias;

    /** Bias of torque from the engine on the back versus front wheels */
    private float engineBias;

    /** Flag indicating if we're braking or accelerating right now */
    private boolean braking;

    /** Drag coeffficient currently set for the car */
    private float dragCoefficient;

    /** Working values get set here to pass to/from node fields */
    private float[] fieldValues;

    /**
     * Create a new car control script.
     */
    public CarDrivetrainProcessor() {
        fieldValues = new float[4];
        braking = false;
    }

    /**
     * Initialise everything in the script now. All the nodes are valid, so
     * let's go for it.
     */
    public void initialize(Map fields) {
        wheelDirectionInput = (SFFloat)fields.get("wheelDirectionInput");
        wheelDirectionInput.addX3DEventListener(this);

        wheelAcceleratorInput = (SFFloat)fields.get("wheelAcceleratorInput");
        wheelAcceleratorInput.addX3DEventListener(this);

        reverseSelectedInput = (SFBool)fields.get("reverseSelected");
        reverseSelectedInput.addX3DEventListener(this);

        steeringAngleInput = (SFFloat)fields.get("currentSteeringAngle");
        frontWheelSteerSpeed = (SFFloat)fields.get("steeringSpeed");

        carVelocity = (SFVec3f)fields.get("carVelocity");
        carVelocity.addX3DEventListener(this);

        outputForces = (MFVec3f)fields.get("frictionForces");

        SFFloat field = (SFFloat)fields.get("steeringDeadspot");
        steeringDeadspot = field.getValue();

        field = (SFFloat)fields.get("steeringLimitAngle");
        steeringLimit = field.getValue();
        steeringCorrectionFactor = 1 / steeringLimit;

        field = (SFFloat)fields.get("speedLimit");
        speedLimit = field.getValue();

        field = (SFFloat)fields.get("maxEngineTorque");
        maxEngineTorque = field.getValue();

        field = (SFFloat)fields.get("maxBrakingTorque");
        maxBrakingTorque = field.getValue();

        brakeBiasInput = (SFFloat)fields.get("brakeBias");
        brakeBiasInput.addX3DEventListener(this);
        brakeBias = brakeBiasInput.getValue();

        engineBiasInput = (SFFloat)fields.get("engineBias");
        engineBiasInput.addX3DEventListener(this);
        engineBias = engineBiasInput.getValue();

        dragCoefficientInput = (SFFloat)fields.get("dragCoefficient");
        dragCoefficientInput.addX3DEventListener(this);
        dragCoefficient = dragCoefficientInput.getValue();

        frontWheelMinStop = (SFFloat)fields.get("frontWheelMinStop");
        frontWheelMaxStop = (SFFloat)fields.get("frontWheelMaxStop");
        rearWheelSpeed = (SFFloat)fields.get("rearWheelSpeed");
        rearWheelTorque = (SFFloat)fields.get("rearWheelTorque");
        frontWheelTorque = (SFFloat)fields.get("frontWheelTorque");
        frontAxleAxis = (SFVec3f)fields.get("frontAxleAxis");
        rearAxleAxis = (SFVec3f)fields.get("rearAxleAxis");

        rearWheelOrientation = (SFVec3f)fields.get("rearWheelOrientation");
        frontWheelOrientation = (SFVec3f)fields.get("frontWheelOrientation");
    }

    public void shutdown() {
        wheelDirectionInput.removeX3DEventListener(this);
        wheelAcceleratorInput.removeX3DEventListener(this);
        brakeBiasInput.removeX3DEventListener(this);
        engineBiasInput.removeX3DEventListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    public void readableFieldChanged(X3DFieldEvent evt) {
        Object src = evt.getSource();

        if(src == wheelDirectionInput) {
            float new_angle = wheelDirectionInput.getValue();

            if(Math.abs(new_angle) < steeringDeadspot)
                new_angle = 0;

            float actual = steeringAngleInput.getValue();
            float velocity = (new_angle - actual) * 0.8f;

            frontWheelMinStop.setValue(-steeringLimit);
            frontWheelMaxStop.setValue(steeringLimit);
            frontWheelSteerSpeed.setValue(velocity);
        } else if(src == wheelAcceleratorInput) {
            float accelerator_pos = wheelAcceleratorInput.getValue();

            if(accelerator_pos < 0) {
                // Braking so try to slow the car down
                float brake_output = -accelerator_pos * maxBrakingTorque;

                rearWheelTorque.setValue(brake_output * (1 - brakeBias));
                frontWheelTorque.setValue(brake_output * brakeBias);

                rearWheelSpeed.setValue(0);

                if(!braking) {
                    frontWheelOrientation.getValue(fieldValues);
                    fieldValues[0] *= -1;
                    fieldValues[1] *= -1;
                    fieldValues[2] *= -1;
                    frontAxleAxis.setValue(fieldValues);

                    rearWheelOrientation.getValue(fieldValues);
                    fieldValues[0] *= -1;
                    fieldValues[1] *= -1;
                    fieldValues[2] *= -1;
                    rearAxleAxis.setValue(fieldValues);
                    braking = true;
                }
            } else if(accelerator_pos > 0) {
                rearWheelSpeed.setValue(accelerator_pos * speedLimit);

                float eng_output = accelerator_pos * maxEngineTorque;
                rearWheelTorque.setValue(eng_output * engineBias);
                frontWheelTorque.setValue(eng_output * (1 - engineBias));

                if(braking) {
                    frontWheelOrientation.getValue(fieldValues);
                    fieldValues[0] *= -1;
                    fieldValues[1] *= -1;
                    fieldValues[2] *= -1;
                    frontAxleAxis.setValue(fieldValues);

                    rearWheelOrientation.getValue(fieldValues);
                    fieldValues[0] *= -1;
                    fieldValues[1] *= -1;
                    fieldValues[2] *= -1;
                    rearAxleAxis.setValue(fieldValues);
                    braking = false;
                }
            } else {
                // No pedal input, so coast to a stop if there is a air
                // resistance force being applied.
                rearWheelTorque.setValue(0);
                frontWheelTorque.setValue(0);
            }
        } else if(src == carVelocity) {
            // air resistance is proportional to the square of the speed and in
            // the equal and opposite direction.
            carVelocity.getValue(fieldValues);
            float x = fieldValues[0];
            float y = fieldValues[1];
            float z = fieldValues[2];
            float speed = (float)Math.sqrt(x * x + y * y + z * z);
            float drag = dragCoefficient * speed * speed;
            if(drag == 0) {
                fieldValues[0] = 0;
                fieldValues[1] = 0;
                fieldValues[2] = 0;
            } else {
                fieldValues[0] = -x / speed * drag;
                fieldValues[1] = -y / speed * drag;
                fieldValues[2] = -z / speed * drag;
            }
            outputForces.setValue(1, fieldValues);
        } else if(src == reverseSelectedInput) {
            // Sort of ignores the current rotation of the wheels. A better
            // model could be made here, but will do for now. Note that reverse
            // is a button that is held down in this model. If you let go of
            // the button, it will drive you in the initial direction again.
            frontWheelOrientation.getValue(fieldValues);
            fieldValues[0] *= -1;
            fieldValues[1] *= -1;
            fieldValues[2] *= -1;
            frontAxleAxis.setValue(fieldValues);

            rearWheelOrientation.getValue(fieldValues);
            fieldValues[0] *= -1;
            fieldValues[1] *= -1;
            fieldValues[2] *= -1;
            rearAxleAxis.setValue(fieldValues);
        } else if(src == brakeBiasInput) {
            brakeBias = brakeBiasInput.getValue();
        } else if(src == engineBiasInput) {
            engineBias = engineBiasInput.getValue();
        } else if(src == dragCoefficientInput) {
            dragCoefficient = dragCoefficientInput.getValue();
        }
    }
}