/**
 * A simple test class to indicate what is happening with the script.
 */
import org.web3d.x3d.sai.*;

import java.util.Map;

public class LoadTest implements X3DScriptImplementation {

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
    }

    public void initialize() {
        System.out.println("Got initialize");
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}
