/**
 * A test to show eventOut routing
 */
import java.util.Map;

import org.web3d.x3d.sai.*;


public class CreateEntities
    implements X3DScriptImplementation, X3DFieldEventListener {
    private static final int NUM_ENTITIES = 500;
    private static final boolean USE_POINTS = true;

    private MFNode pen;
    private Browser browser;

    public CreateEntities() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        SFNode target = (SFNode)fields.get("target");
        X3DNode group = target.getValue();
        pen = (MFNode)group.getField("children");
    }

    public void initialize() {
        X3DScene scene = (X3DScene)browser.getExecutionContext();
        X3DNode[] nodes = new X3DNode[NUM_ENTITIES];

        float[] blue = new float[] {0,0,1};
        float[] red = new float[] {1,0,0};
        float[] white = new float[] {1,1,1};

        for(int i=0; i < NUM_ENTITIES; i ++) {
            X3DNode espdu = scene.createNode("EspduTransform");
            SFString address = (SFString) espdu.getField("address");
            address.setValue("224.2.181.145");
            SFInt32 port = (SFInt32) espdu.getField("port");
            port.setValue(62040);
            SFInt32 siteID = (SFInt32) espdu.getField("siteID");
            siteID.setValue(0);
            SFInt32 appID = (SFInt32) espdu.getField("applicationID");
            appID.setValue(1);
            SFInt32 entityID = (SFInt32) espdu.getField("entityID");
            entityID.setValue(i);
            SFString networkMode = (SFString) espdu.getField("networkMode");
            networkMode.setValue("networkReader");

            X3DNode shape = scene.createNode("Shape");
            X3DNode app = scene.createNode("Appearance");
            X3DNode mat = scene.createNode("Material");
            SFColor color = (SFColor) mat.getField("diffuseColor");

            if (i < NUM_ENTITIES * 0.33) {
                color.setValue(red);

            } else if (i < NUM_ENTITIES * 0.67) {
                color.setValue(white);
            } else {
                color.setValue(blue);
            }

            SFNode matField = (SFNode) app.getField("material");
            matField.setValue(mat);

            if (USE_POINTS) {
                X3DNode pointSet = scene.createNode("PointSet");
                SFNode point_coord = (SFNode)pointSet.getField("coord");
                X3DNode coord = scene.createNode("Coordinate");
                MFVec3f coord_point = (MFVec3f) coord.getField("point");
                coord_point.setValue(1, new float[] {0,0,0});

                point_coord.setValue(coord);

                SFNode geom = (SFNode)shape.getField("geometry");
                geom.setValue(pointSet);

            } else {
                X3DNode box = scene.createNode("Box");
                SFVec3f box_size = (SFVec3f)box.getField("size");

                float[] new_size = { 1f, 1, 1f };
                box_size.setValue(new_size);
                box.realize();

                SFNode geom = (SFNode)shape.getField("geometry");
                geom.setValue(box);

            }

            SFNode appField = (SFNode)shape.getField("appearance");
//            appField.setValue(app);

            MFNode children = (MFNode)espdu.getField("children");
            children.setValue(1, new X3DNode[] {shape});

            nodes[i] = espdu;
        }

        pen.setValue(NUM_ENTITIES,nodes);
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    public void readableFieldChanged(X3DFieldEvent evt) {

    }
}
