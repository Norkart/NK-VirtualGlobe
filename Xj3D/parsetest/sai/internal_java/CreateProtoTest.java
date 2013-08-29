/**
 * A test to show eventOut routing
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;


public class CreateProtoTest
    implements X3DScriptImplementation, X3DFieldEventListener {

    private MFNode geometryOutput;
    private MFNode altOutput;
    private SFTime inputField;

    private Browser browser;

    public CreateProtoTest() {
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
        geometryOutput = (MFNode)shape.getField("addChildren");
        altOutput = (MFNode)fields.get("altOutput");

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

        X3DScene scene = (X3DScene)browser.getExecutionContext();
        X3DNode box = scene.createProto("MyBox");
        SFColor box_colour = (SFColor)box.getField("color");

        float[] new_colour = { 0, 1, 1 };
        box_colour.setValue(new_colour);
        box.realize();

        X3DNode[] value = { box };

        geometryOutput.setValue(1, value);
        altOutput.setValue(1, value);
    }
}
