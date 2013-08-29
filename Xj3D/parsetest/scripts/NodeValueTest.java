/**
 * A simple test class to indicate what is happening with the script.
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class NodeValueTest extends Script {
    public NodeValueTest() {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch fields");

        try {
            Field field = getField("testNodeField");

            SFNode node_field = (SFNode)field;

            BaseNode value = node_field.getValue();

            System.out.println("Fetched node is " + value.getType());

            // Now attempt to fetch a field from it
            Node node = (Node)value;

            Field bbox = node.getExposedField("bboxSize");

            MFNode children = (MFNode)node.getExposedField("children");
            MFNode set_children = (MFNode)node.getEventIn("set_children");

        } catch(Exception e) {
            System.out.println("Error fetching field " +  e);
            e.printStackTrace();
        }

    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}