/**
 * A simple test class to indicate what is happening with the script.
 */
import vrml.node.Script;


public class LoadTest extends Script {
    public LoadTest() {
        System.out.println("Script constructed");
    }

    public void initialize() {
        System.out.println("Initialise called");
    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}