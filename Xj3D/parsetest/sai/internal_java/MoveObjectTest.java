/**
 * A test to show eventOut routing
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;


public class MoveObjectTest
    implements X3DScriptImplementation, X3DFieldEventListener {

    private SFVec3f location;
    private SFRotation rotation;
    private SFTime inputField;
    private boolean flip = true;

    private float[] homeLocation = {0, 0, 0};
    private float[] homeRotation = {0, 0, 1, 0};

    private float[] otherLocation = {0, 2, 0};
    private float[] otherRotation = {0, 1, 0, 0.5f};

    public MoveObjectTest() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        location = (SFVec3f)fields.get("location");
        rotation = (SFRotation)fields.get("orientation");

        inputField = (SFTime)fields.get("pulse");
        inputField.addX3DEventListener(this);
    }

    public void initialize() {
        // Use this to offset the direction at start
        float[] loc = { 1, 0, 0 };
        float[] rot = { 0, 1, 0, 0.25f };

        location.setValue(loc);
        rotation.setValue(rot);
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
        inputField.removeX3DEventListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    public void readableFieldChanged(X3DFieldEvent evt) {
        if(flip) {
            flip = false;
            location.setValue(otherLocation);
            rotation.setValue(otherRotation);
        }
        else {
            flip = true;
            location.setValue(homeLocation);
            rotation.setValue(homeRotation);
        }
    }
}
