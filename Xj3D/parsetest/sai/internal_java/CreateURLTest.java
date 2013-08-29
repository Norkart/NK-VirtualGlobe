/**
 * A test class to check on Browser.createX3DFromURL() functionality.
 *
 * Trigger the create process from an event.
 * There are two URLs defined - one VRML97 file and one X3D file. The VRML97
 * file should fail based on spec version, while the X3D one should succeed.
 */
import org.web3d.x3d.sai.*;

import java.util.Map;

public class CreateURLTest implements
    X3DScriptImplementation,
    X3DFieldEventListener {

    /** The string of nodes that we are going to create */
    private static final String[] URL_LIST = {
        "create_url_target.wrl",
        "create_url_target.x3dv"
    };

    private X3DField inputField;
    private Browser browser;

    public CreateURLTest() {
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
        System.out.println("About to call create method");

        X3DScene sc = browser.createX3DFromURL(URL_LIST);
        browser.replaceWorld(sc);
    }
 }
