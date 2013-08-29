/**
 * A test to show eventOut routing
 */
import java.util.Map;

import org.web3d.x3d.sai.*;


public class CreateNodeTest
    implements X3DScriptImplementation, X3DFieldEventListener {

    private SFNode geometryOutput;
    private SFTime inputField;

    private Browser browser;

    public CreateNodeTest() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        SFNode target = (SFNode)fields.get("target");
        X3DNode shape = target.getValue();
        geometryOutput = (SFNode)shape.getField("geometry");

        inputField = (SFTime)fields.get("touchInput");
        inputField.addX3DEventListener(this);
    }

    public void initialize() {
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

        X3DExecutionContext scene = browser.getExecutionContext();
        X3DNode box = scene.createNode("Box");
        SFVec3f box_size = (SFVec3f)box.getField("size");

        float[] new_size = { 0.5f, 3, 0.5f };
        box_size.setValue(new_size);
        box.realize();

        geometryOutput.setValue(box);
    }
}
