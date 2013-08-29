/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;

public class EventTest
    implements X3DScriptImplementation, X3DFieldEventListener {

    private X3DField inputField;
    private Browser browser;

    public EventTest() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        X3DField inputField = (X3DField)fields.get("touchInput");
        inputField.addX3DEventListener(this);
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch browser");
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
        System.out.println("Got event for " + evt.getSource());
    }
}