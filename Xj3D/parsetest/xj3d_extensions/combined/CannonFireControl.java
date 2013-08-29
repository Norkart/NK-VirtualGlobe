/**
 * Script for indicating how to do fire control on the cannon.x3dv file
 */
import org.web3d.x3d.sai.*;

import java.util.Map;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

public class CannonFireControl
    implements X3DScriptImplementation,
               X3DFieldEventListener {

    private Browser browser;

    /** Input field for setting of the fire */
    private SFBool fireTrigger;

    /** Output field for sending the ball to the rendering group */
    private MFNode addCannonBall;

    /** Input field holding the elevation angle */
    private SFRotation cannonElevation;

    /** Input field holding the orientation */
    private SFRotation cannonOrientation;

    /** The output to set the ball's position */
    private SFVec3f ballPosition;

    /** The output to set the ball's initial velocity */
    private SFVec3f ballLinearVelocity;

    /** The output to set the ball's initial velocity */
    private SFVec3f ballAngularVelocity;

    /** Field to hold the force to impart on the ball */
    private SFFloat ballForce;

    public CannonFireControl() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        fireTrigger = (SFBool)fields.get("fireNow");
        fireTrigger.addX3DEventListener(this);

        addCannonBall = (MFNode)fields.get("addCannonBall");
        cannonOrientation = (SFRotation)fields.get("cannonOrientation");
        cannonElevation = (SFRotation)fields.get("cannonElevation");

        ballPosition = (SFVec3f)fields.get("ballPosition");
        ballLinearVelocity = (SFVec3f)fields.get("ballLinearVelocity");
        ballAngularVelocity = (SFVec3f)fields.get("ballAngularVelocity");
        ballForce = (SFFloat)fields.get("ballForce");
    }

    public void initialize() {
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    public void readableFieldChanged(X3DFieldEvent evt) {

        if(!fireTrigger.getValue())
            return;

        float[] rot = new float[4];
        AxisAngle4f aangle = new AxisAngle4f();
        Matrix4f elevation = new Matrix4f();
        elevation.setIdentity();
        Matrix4f orientation = new Matrix4f();
        orientation.setIdentity();

        cannonOrientation.getValue(rot);
        aangle.set(rot);
        orientation.set(aangle);

        cannonElevation.getValue(rot);
        aangle.set(rot);
        elevation.set(aangle);

        Matrix4f output = new Matrix4f();
        output.mul(orientation, elevation);

        float force = ballForce.getValue();
        rot[0] = 0;
        rot[1] = 0;
        rot[2] = 0;
        ballAngularVelocity.setValue(rot);

        rot[0] = -output.m02;
        rot[1] = -output.m12;
        rot[2] = -output.m22;
        ballPosition.setValue(rot);

        rot[0] = -output.m02 * force;
        rot[1] = -output.m12 * force;
        rot[2] = -output.m22 * force;
        ballLinearVelocity.setValue(rot);
    }
}