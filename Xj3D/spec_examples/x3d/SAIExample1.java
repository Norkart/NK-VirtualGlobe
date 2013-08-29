// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample1
    implements X3DScriptImplementation, X3DFieldEventListener {

    /** Color Constant, RED */
    private static final float[] RED = new float[] {1.0f, 0, 0};

    /** Color Constant, BLUE */
    private static final float[] BLUE = new float[] {0, 0, 1.0f};

    /** A mapping for fieldName(String) to an X3DField object */
    private Map fields;

    /** The isOver field */
    private SFBool isOver;

    /** The diffuseColor_changed field */
    private SFColor diffuseColor;

    //----------------------------------------------------------
    // Methods from the X3DScriptImplementation interface.
    //----------------------------------------------------------
    /**
     * Set the browser instance to be used by this script implementation.
     *
     * @param browser The browser reference to keep
     */
    public void setBrowser(Browser browser) {
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
        isOver = (SFBool) fields.get("isOver");
        diffuseColor = (SFColor) fields.get("diffuseColor_changed");

        // Listen to events on isOver
        isOver.addX3DEventListener(this);
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
        if (evt.getSource() == isOver) {
            if (isOver.getValue() == true)
                diffuseColor.setValue(RED);
            else
                diffuseColor.setValue(BLUE);
        } else {
            System.out.println("Unhandled event: " + evt);
        }
    }
}
