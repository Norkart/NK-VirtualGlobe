import java.util.Map;

import org.web3d.x3d.sai.*;

/**
 * Processes new DIS entites and creates geometry to represent them.
 *
 * @author Alan Hudson
 */


public class EntityProcessor
    implements X3DScriptImplementation, X3DFieldEventListener {

    private Browser browser;
    MFNode addedEntities;
    private X3DNode[] nodes;

    public EntityProcessor() {
        nodes = new X3DNode[1];
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        addedEntities = (MFNode)fields.get("addedEntities");
        addedEntities.addX3DEventListener(this);
    }

    public void initialize() {
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
        addedEntities.removeX3DEventListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    public void readableFieldChanged(X3DFieldEvent evt) {
        int len = addedEntities.size();
        if (nodes.length < len) {
            nodes = new X3DNode[len];
        }

        addedEntities.getValue(nodes);

        for(int i=0; i < len; i++) {
            SFInt32 entityID = (SFInt32) nodes[i].getField("entityID");
    System.out.println("Creating entity for: " + entityID.getValue());
            X3DScene scene = (X3DScene)browser.getExecutionContext();
            X3DNode shape = scene.createNode("Shape");
            SFNode geometry = (SFNode) shape.getField("geometry");
            X3DNode box = scene.createNode("Box");
            SFVec3f box_size = (SFVec3f)box.getField("size");

            float[] new_size = { 1f, 1, 1f };
            box_size.setValue(new_size);
            box.realize();

            geometry.setValue(box);
            shape.realize();

            MFNode children = (MFNode) nodes[i].getField("children");
            children.setValue(1, new X3DNode[] { shape });
        }
    }
}
