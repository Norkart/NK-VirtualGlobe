/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard imports
// none

// Application Specific Imports
import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.group.Transform;
import org.web3d.x3d.sai.shape.Shape;
import org.web3d.x3d.sai.geom3d.Box;

/**
 * A simple example of how to use the SAI.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class HelloWorldSAI {
    public static void main(String[] args) {
        X3DComponent component = BrowserFactory.createX3DComponent(null);
        Browser browser = component.getBrowser();
        X3DScene scene;

        ProfileInfo vrmlProfile=null;

        try {
            vrmlProfile = browser.getProfile("VRML97");
        } catch(NotSupportedException nse) {
            System.out.println("VRML97 Profile not supported");
            System.exit(-1);
        }

        scene = browser.createScene(vrmlProfile, null);

        // Here are two ways to modify the translation field

        X3DNode transform = scene.createNode("Transform");
        SFVec3f translation = (SFVec3f) transform.getField("translation");
        translation.setValue(new float[] {1,0,0});

        Transform transform2 = (Transform) scene.createNode("Transform");
        transform2.setTranslation(new float[] {0,3,0});

        Shape shape = (Shape) scene.createNode("Shape");
        Box box = (Box) scene.createNode("Box");
        box.setSize(new float[] {1,1,1});
        shape.setGeometry(box);

        ((X3DGroupingNode)transform).addChild(shape);
        // Implicit USE
        transform2.addChild(shape);

        scene.addRootNode(transform);
        scene.addRootNode(transform2);

    }
}