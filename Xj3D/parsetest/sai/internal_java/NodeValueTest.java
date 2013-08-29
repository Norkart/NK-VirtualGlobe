/**
 * A simple test class to indicate what is happening with the script.
 */
import java.util.Map;

import org.web3d.x3d.sai.*;

public class NodeValueTest implements X3DScriptImplementation {

    private Browser browser;

    private SFNode nodeField;

    public NodeValueTest() {
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        browser.println("Got browser");
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        nodeField = (SFNode)fields.get("testNodeField");
    }

    public void initialize() {
        browser.println("Initialise called. About to fetch fields");

        X3DNode node = nodeField.getValue();

        browser.println("Fetched node is " + node.getNodeName());

        X3DField bbox = node.getField("bboxSize");

        if(bbox.isReadable())
            browser.println("bbox is readable!");

        if(bbox.isWritable())
            browser.println("bbox is writable!");

        MFNode children = (MFNode)node.getField("children");
        MFNode set_children = (MFNode)node.getField("set_children");

        // check for equality of the above. It should be
        if(!children.equals(set_children))
            browser.println("Equality test failed");
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}