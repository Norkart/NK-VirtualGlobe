// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample4
    implements X3DScriptImplementation, X3DFieldEventListener {

    /** A mapping for fieldName(String) to an X3DField object */
    private Map fields;

    /** A reference to the browser */
    private Browser browser;

    /** inputOnly touchTime */
    private SFTime touchTime;

    /** initializeOnly selfRef */
    private X3DScriptNode selfRef;

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
        selfRef = externalView;
    }

    /**
     * Notification that the script has completed the setup and should go
     * about its own internal initialization.
     */
    public void initialize() {
        touchTime = (SFTime) fields.get("touchTime");

        // Listen to events on touchTime
        touchTime.addX3DEventListener(this);

        // Create nodes directly in the parent scene
        X3DScene scene = (X3DScene) browser.getExecutionContext();

        X3DShapeNode shape = (X3DShapeNode) scene.createNode("Shape");
        X3DGeometryNode box = (X3DGeometryNode) scene.createNode("Box");
        X3DNode touchSensor = scene.createNode("TouchSensor");

        shape.setGeometry(box);

        // Create a Group to hold the nodes
        X3DGroupingNode group = (X3DGroupingNode) scene.createNode("Group");

        // Add the shape and sensor to the group
        group.addChild(shape);
        group.addChild(touchSensor);

        // Add the nodes to the scene
        scene.addRootNode(group);

        // Get a handle to the toplevel execution context
        scene.addRoute(touchSensor,"touchTime", selfRef, "touchTime");
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

    //----------------------------------------------------------
    // Methods from the X3DFieldEventListener interface.
    //----------------------------------------------------------

    /**
     * Handle field changes.
     *
     * @param evt The field event
     */
    public void readableFieldChanged(X3DFieldEvent evt) {
        if (evt.getSource() == touchTime) {
            System.out.println("Poke!");
        } else {
            System.out.println("Unhandled event: " + evt);
        }
    }
}
