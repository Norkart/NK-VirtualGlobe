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
// JAXP packages

// Standard library imports
import java.io.InputStream;
import java.io.IOException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.web3d.x3d.sai.Vector3Float;
import org.web3d.x3d.sai.Rotation;
import org.web3d.x3d.sai.StringsUrl;
import org.web3d.x3d.sai.Color;
import org.web3d.x3d.sai.Strings;
import org.web3d.x3d.sai.meta;
import org.web3d.x3d.sai.Header;
import org.web3d.x3d.sai.Scene;
import org.web3d.x3d.sai.X3D;
import org.web3d.x3d.sai.Core.Viewpoint;
import org.web3d.x3d.sai.Core.Appearance;
import org.web3d.x3d.sai.Core.Material;
import org.web3d.x3d.sai.Core.ImageTexture;
import org.web3d.x3d.sai.Core.NavigationInfo;
import org.web3d.x3d.sai.Core.Transform;
import org.web3d.x3d.sai.Core.Shape;
import org.web3d.x3d.sai.Core.Group;

import org.web3d.x3d.sai.BaseLine.Sphere;
import org.web3d.x3d.sai.BaseLine.Text;

// Application specific imports
import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;

/**
 * Demonstration of SAI and DOM being used to create a scenegraph from scratch.
 * <p>
 *
 * The demo uses the custom parser built by our code. The only way to start
 * a scenegraph using the SAI APIs is to allow us to create a new root element
 * for the document using DOM apis. As we have our custom parser built, we can
 * then create an element and use the SAI to cast it to the appropriate value.
 */
public class SceneCreationDemo {

    /** Factory class name for the SAI handling */
    private static final String ATTR_FACTORY =
        "org.web3d.x3d.jaxp.dom.sai.SAIAttributeFactory";

    /** Factory class name for the SAI handling */
    private static final String ELEMENT_FACTORY =
        "org.web3d.x3d.jaxp.dom.sai.SAIElementFactory";

    /** The document that starts everything off */
    private Document document;

    /**
     * Create an instance of the demo class. Constructs an instance of the
     * document builder that we will use for the individual demos
     */
    public SceneCreationDemo() {
        // Set the system property that tells JAXP how to load our custom
        // parser. We can either to it like this in code or as a command
        // line property eg
        // java -Djavax.xml......=org.web3d.x3d..... SAIDOMDemo

        System.setProperty(X3DConstants.JAXP_FACTORY_PROPERTY,
                           X3DConstants.DOM_FACTORY_IMPL);


        // Now create an instance of our builder so that we could create new
        // documents or parse existing ones.
        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

            factory.setAttribute(X3DConstants.X3D_ELEMENT_FACTORY,
                                 ELEMENT_FACTORY);
            factory.setAttribute(X3DConstants.X3D_ATTRIBUTE_FACTORY,
                                 ATTR_FACTORY);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new X3DEntityResolver());
            builder.setErrorHandler(new X3DErrorHandler());

            document = builder.newDocument();

        } catch(FactoryConfigurationError fce) {
            System.err.println("Error configuring the factory. " + fce);
        } catch(ParserConfigurationException pce) {
            System.err.println("Error configuring the parser. " + pce);
        }
    }

    /**
     * This demo shows the accessing of nodes using the DOM interface.
     */
    public void demoDOM() {
        Element x3d = document.createElement("X3D");
        document.appendChild(x3d);

        Element header = document.createElement("Header");
        x3d.appendChild(header);

        // create a bunch of meta tags
        Element meta = document.createElement("meta");
        meta.setAttribute("name", "filename");
        meta.setAttribute("content", "HelloWorld.xml");
        header.appendChild(meta);

        meta = document.createElement("meta");
        meta.setAttribute("name", "description");
        meta.setAttribute("content", "Demo created from DOM code");
        header.appendChild(meta);

        meta = document.createElement("meta");
        meta.setAttribute("name", "author");
        meta.setAttribute("content", "Justin Couch");
        header.appendChild(meta);

        // Now on with the scene proper
        Element scene = document.createElement("Scene");
        x3d.appendChild(scene);

        Comment comment = document.createComment(
            "Example scene to illustrate DOM document creation"
        );
        scene.appendChild(comment);

        Element group = document.createElement("Group");
        scene.appendChild(group);

        Element viewpoint = document.createElement("Viewpoint");
        viewpoint.setAttribute("description", "Hello world!");
        viewpoint.setAttribute("orientation", "0 1 0 1.57");
        viewpoint.setAttribute("position", "6 -1 0");
        group.appendChild(viewpoint);

        Element nav = document.createElement("NavigationInfo");
        nav.setAttribute("type", "\"EXAMINE\" \"ANY\"");
        group.appendChild(nav);

        Element shape1 = document.createElement("Shape");
        group.appendChild(shape1);

        Element sphere = document.createElement("Sphere");
        sphere.setAttribute("DEF", "S");
        shape1.appendChild(sphere);

        Element app1 = document.createElement("Appearance");
        shape1.appendChild(app1);

        Element texture = document.createElement("ImageTexture");
        texture.setAttribute("url", "\"earth-topo.png\" " +
            "\"earth-topo-small.gif\" " +
            "\"http://www.web3D.org/TaskGroups/x3d/translation/examples/earth-topo.png\" " +
            "\"http://www.web3D.org/TaskGroups/x3d/translation/examples/earth-topo-small.gif\"");
        app1.appendChild(texture);

        Element tx = document.createElement("Transform");
        tx.setAttribute("rotation", "0 1 0 1.57");
        tx.setAttribute("translation", "0 -2 1.25");
        group.appendChild(tx);

        Element shape2 = document.createElement("Shape");
        tx.appendChild(shape2);

        Element text = document.createElement("Text");
        text.setAttribute("string", "\"Hello\" \"world!\"");
        shape2.appendChild(text);

        Element app2 = document.createElement("Appearance");
        shape2.appendChild(app2);

        Element material = document.createElement("Material");
        material.setAttribute("diffuseColor", "0.1 0.5 1");
        app2.appendChild(material);
    }

    /**
     * Demonstrate the use of the SAI classes. As the SAI has no way of
     * fetching a document directly, we start by using DOM to fetch a node,
     * casting it to the appropriate SAI type. From there we use the SAI
     * methods to get and set the nodes.
     */
    public void demoSAI() {
        X3D x3d = (X3D)document.createElement("X3D");
        document.appendChild(x3d);

        Header header = (Header)document.createElement("Header");
        x3d.setHeader(header);

        // create a bunch of meta tags
        meta me = (meta)document.createElement("meta");
        org.web3d.x3d.sai.String name = me.getName();
        name.setValue("filename");
        me.setName(name);
        org.web3d.x3d.sai.String content = me.getContent();
        content.setValue("HelloWorld.xml");
        me.setContent(content);

        header.appendMeta(me);

        me = (meta)document.createElement("meta");
        name = me.getName();
        name.setValue("description");
        me.setName(name);
        content = me.getContent();
        content.setValue("Demo created from SAI code");

        header.appendMeta(me);

        me = (meta)document.createElement("meta");
        name = me.getName();
        name.setValue("author");
        me.setName(name);
        content = me.getContent();
        content.setValue("Justin Couch");
        header.appendMeta(me);

        // Now on with the scene proper
        Scene scene = (Scene)document.createElement("Scene");
        x3d.setScene(scene);

        // SAI has no comment equivalents

        Group group = (Group)document.createElement("Group");
        scene.appendChild(group);

        Viewpoint viewpoint = (Viewpoint)document.createElement("Viewpoint");
        org.web3d.x3d.sai.String desc = viewpoint.getDescription();
        desc.setValue("Hello world!");
        viewpoint.setDescription(desc);

        Rotation rot1 = viewpoint.getOrientation();
        rot1.setValue(0, 1, 0, 1.57f);
        viewpoint.setOrientation(rot1);

        Vector3Float pos = viewpoint.getPosition();
        pos.setValue(6, -1, 0);
        viewpoint.setPosition(pos);
        group.appendChildren(viewpoint);

        NavigationInfo nav = (NavigationInfo)document.createElement("NavigationInfo");
        Strings type = nav.getType();
        String[] type_str = {"EXAMINE",  "ANY"};
        type.setValue(type_str);
        group.appendChildren(nav);

        Shape shape1 = (Shape)document.createElement("Shape");
        group.appendChildren(shape1);

        Sphere sphere = (Sphere)document.createElement("Sphere");
        org.web3d.x3d.sai.String def = sphere.getDEF();
        def.setValue("S");
        sphere.setDEF(def);
        shape1.setGeometry(sphere);

        Appearance app1 = (Appearance)document.createElement("Appearance");
        shape1.setAppearance(app1);

        ImageTexture texture = (ImageTexture)document.createElement("ImageTexture");
        StringsUrl url = texture.getUrl();
        String[] url_str = {
            "earth-topo.png",
            "earth-topo-small.gif",
            "http://www.web3D.org/TaskGroups/x3d/translation/examples/earth-topo.png",
            "http://www.web3D.org/TaskGroups/x3d/translation/examples/earth-topo-small.gif"
        };

        url.setValue(url_str);
        app1.setTexture(texture);

        Transform tx = (Transform)document.createElement("Transform");

        Rotation rot2 = tx.getRotation();
        rot2.setValue(0, 1, 0, 1.57f);
        tx.setRotation(rot2);

        Vector3Float trans = tx.getTranslation();
        trans.setValue(0, -2, 1.25f);
        tx.setTranslation(trans);

        group.appendChildren(tx);

        Shape shape2 = (Shape)document.createElement("Shape");
        tx.appendChildren(shape2);

        Text text = (Text)document.createElement("Text");
        org.web3d.x3d.sai.String str = text.getString();
        str.setValue("\"Hello\" \"world!\"");
        text.setString(str);

        shape2.setGeometry(text);

        Appearance app2 = (Appearance)document.createElement("Appearance");
        shape2.setAppearance(app2);

        Material material = (Material)document.createElement("Material");
        Color col = material.getDiffuseColor();
        col.setValue(0.1f, 0.5f, 1);
        material.setDiffuseColor(col);


        app2.setMaterial(material);
    }

    /**
     * Create an instance of this class and run it.
     */
    public static void main(String[] argv) {
        SceneCreationDemo demo = new SceneCreationDemo();
        demo.demoDOM();
        demo.demoSAI();
    }
}
