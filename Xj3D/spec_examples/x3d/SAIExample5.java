// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample5
    implements X3DScriptImplementation {

    /** Color Constant, RED */
    private static final float[] RED = new float[] {1.0f, 0, 0};

    /** A mapping for fieldName(String) to an X3DField object */
    private Map fields;

    /** A reference to the browser */
    private Browser browser;

    /** The field to place the generated nodes via createX3DFromString */
    private MFNode children;

    //----------------------------------------------------------
    // Methods from the X3DScriptImplementation interface.
    //----------------------------------------------------------
    /**
     * Set the browser instance to be used by this script implementation.
     *
     * @param browser The browser reference to keep
     */
    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    /**
     * Set the listing of fields that have been declared in the file for
     * this node. .
     *
     * @param The external view of ourselves, so you can add routes to yourself
     *    using the standard API calls
     * @param fields The mapping of field names to instances
     */
    public void setFields(X3DScriptNode externalView, Map fields) {
        this.fields = fields;
    }

    /**
     * Notification that the script has completed the setup and should go
     * about its own internal initialization.
     */
    public void initialize() {
        // Create nodes directly in the parent scene
        X3DScene scene = (X3DScene) browser.getExecutionContext();

        // Create protoInstance nodes

        // Get the proto declaration declared in the main scene
        X3DProtoDeclaration protoDecl = scene.getProto("ColoredSphere");

        // Create a new instance of this proto
        X3DProtoInstance instance = protoDecl.createInstance();

        // Get the color field and set it to red
        SFColor color = (SFColor) instance.getField("color");
        color.setValue(RED);

        // Add the created proto instance to the scene
        scene.addRootNode(instance);
    }

    /**
     * Notification that this script instance is no longer in use by the
     * scene graph and should now release all resources.
     */
    public void shutdown() {
    }

    /**
     * Notification that all the events in the current cascade have finished
     * processing.
     */
    public void eventsProcessed() {
    }
}
