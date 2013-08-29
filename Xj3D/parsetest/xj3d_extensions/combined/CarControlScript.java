/**
 * Script for taking the output from the control device and applying it to
 * the physics model.
 */
import org.web3d.x3d.sai.*;

import java.util.Map;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

public class CarControlScript implements X3DPerFrameObserverScript {

    /** The browser reference for doing browsery stuff */
    private Browser browser;

    /** Holder of all the field values for this script */
    private Map fieldMap;

    /** Engine and drivetrain handler */
    private CarDrivetrainProcessor drivetrain;

    /** Collision system processor */
    private CarContactProcessor collider;

    /** Damping and limit processor */
    private CarDampingProcessor damper;

    /**
     * Create a new car control script.
     */
    public CarControlScript() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        fieldMap = fields;
    }

    /**
     * Initialize the script now.
     */
    public void initialize() {
        collider = new CarContactProcessor();
        drivetrain = new CarDrivetrainProcessor();
        damper = new CarDampingProcessor();

        collider.initialize(fieldMap);
        drivetrain.initialize(fieldMap);
        damper.initialize(fieldMap);
    }

    public void eventsProcessed() {
    }

    /**
     * Shut the script down now. Clean up any resources used.
     */
    public void shutdown() {
        collider.shutdown();
        drivetrain.shutdown();
        damper.shutdown();
    }

    //----------------------------------------------------------
    // Methods defined by X3DPerFrameObserverScript
    //----------------------------------------------------------

    /**
     * Call this every frame to process the body velocity and apply appropriate
     * damping to it.
     */
    public void prepareEvents() {
        damper.processDamping();
    }
}