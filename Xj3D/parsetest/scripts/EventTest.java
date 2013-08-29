/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class EventTest extends Script {
    public EventTest() {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch browser");
    }

    public void processEvent(Event evt) {
        System.out.println("Got event for " + evt.getName());
    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}