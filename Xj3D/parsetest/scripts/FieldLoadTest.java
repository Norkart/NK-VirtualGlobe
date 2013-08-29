/**
 * A simple test class to indicate what is happening with the script.
 */
import vrml.Field;
import vrml.InvalidEventInException;
import vrml.node.Script;

public class FieldLoadTest extends Script {
    public FieldLoadTest() {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch fields");

        try {
            Field int_field = getField("testIntField");
        } catch(Exception e) {
            System.out.println("Error fetching field");
        }

        try {
            Field node_field = getEventIn("testNodeInput");
        } catch(Exception e) {
            System.out.println("Error fetching eventIn");
        }

        try {
            Field vec_output = getEventOut("testVecOutput");
        } catch(Exception e) {
            System.out.println("Error fetching eventOut");
        }

        // This attempts to fetch an unknown field and therefor should barf.
        try {
            Field invalid = getEventIn("foobar");
            System.out.println("Oops. Didn't barf on a non-existant field");
        } catch(InvalidEventInException ieie) {
            // we're expecting this
        }
    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}