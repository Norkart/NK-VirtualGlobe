/**
 * A test class to check on Browser.createVrmlFromString() functionality.
 *
 * Trigger the create process from an event.
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class CreateStringTest extends Script {

    /** The string of nodes that we are going to create */
    private static final String NODE_STRING =
        "Shape {" +
        "  appearance Appearance {" +
        "    material Material {" +
        "      diffuseColor 0 0 1" +
        "    }" +
        "    texture ImageTexture {" +
        "      url [\"vts.jpg\"]" +
        "    }" +
        "  }" +
        "  geometry Box {}" +
        "}";

    /** The string of nodes that we are going to create */
    private static final String NODE_STRING2 =
        "Inline { url [\"moving_box.wrl\"] }";

    private int ver;

    /** The field that holds the group we are writing values to */
    private MFNode txChildren;

    public CreateStringTest() {
        ver = 0;
    }

    public void initialize() {
        SFNode group = (SFNode)getField("target");
        Node node = (Node)group.getValue();
        txChildren = (MFNode)node.getEventIn("children");
    }

    public void processEvent(Event evt) {
        Browser browser = getBrowser();

        BaseNode[] shape=null;

        if (ver == 0) {
            ver = 1;
            shape = browser.createVrmlFromString(NODE_STRING);
        } else {
            ver = 0;
            shape = browser.createVrmlFromString(NODE_STRING2);
        }

        System.out.println("Created node is " + shape[0].getType());

        txChildren.clear();
        txChildren.addValue(shape[0]);
    }


    public void shutdown() {
        System.out.println("Shutdown called");
    }
}
