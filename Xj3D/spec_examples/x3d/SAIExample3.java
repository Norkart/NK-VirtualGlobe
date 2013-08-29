// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample3
    implements X3DPerFrameObserverScript {

    /** When did the last frame start */
    private long lastStartTime;

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
    }

    /**
     * Notification that the script has completed the setup and should go
     * about its own internal initialization.
     */
    public void initialize() {
        lastStartTime = System.currentTimeMillis();
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
    // Methods from the X3DPerFrameObserver interface.
    //----------------------------------------------------------

    /**
     * Start of frame notification.
     */
    public void prepareEvents() {
        float frameTime = (System.currentTimeMillis() - lastStartTime) / 1000f;
        lastStartTime = System.currentTimeMillis();

        float fps = 1.0f / frameTime;
        System.out.println("FPS: " + fps);
    }
}
