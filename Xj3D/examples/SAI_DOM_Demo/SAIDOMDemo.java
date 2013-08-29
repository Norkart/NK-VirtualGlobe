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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.web3d.x3d.sai.BaseNode;
import org.web3d.x3d.sai.BaseNodeArray;
import org.web3d.x3d.sai.Boolean;
import org.web3d.x3d.sai.Float;
import org.web3d.x3d.sai.Rotation;
import org.web3d.x3d.sai.Time;
import org.web3d.x3d.sai.Vector3Float;
import org.web3d.x3d.sai.Core.Transform;
import org.web3d.x3d.sai.Core.Viewpoint;

// Application specific imports
import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;

/**
 * Demonstration of SAI and DOM interaction with an X3D scenegraph.
 * <p>
 *
 * The demo uses the custom parser built by our code. Then we fetch a
 * number of nodes from the scenegraph. We firstly print out their values
 * and then modify them using both the SAI and DOM interfaces and print
 * the resulting info as well to show the equivalence of the APIs
 */
public class SAIDOMDemo {

    /** Name of the file with the big list of DEFs in it we can get */
    private static final String DEF_FILE = "def_demo.xml";

    /** Name of the file with the X3D structure for DOM demonstrations */
    private static final String DOM_FILE = "dom_demo.xml";

    /** Factory class name for the SAI handling */
    private static final String ATTR_FACTORY =
        "org.web3d.x3d.jaxp.dom.sai.SAIAttributeFactory";

    /** Factory class name for the SAI handling */
    private static final String ELEMENT_FACTORY =
        "org.web3d.x3d.jaxp.dom.sai.SAIElementFactory";

    /** The document builder instance so that we can create documents */
    private DocumentBuilder builder;

    /**
     * Create an instance of the demo class. Constructs an instance of the
     * document builder that we will use for the individual demos
     */
    public SAIDOMDemo() {

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

            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new X3DEntityResolver());
            builder.setErrorHandler(new X3DErrorHandler());
        } catch(FactoryConfigurationError fce) {
            System.err.println("Error configuring the factory. " + fce);
        } catch(ParserConfigurationException pce) {
            System.err.println("Error configuring the parser. " + pce);
        }
    }

    /**
     * Parse a document and fetch the DEF nodes from it.
     */
    public void demoDEFs() {
        Document doc = findXMLFile(DEF_FILE);

        if(doc == null)
            return;

        // There are 7 DEFs to find: NAV_INFO, BOX_SHAPE, BOX_GEOMETRY,
        // BOX_MATERIAL, SPHERE_SHAPE, SPHERE_TRANSLATION, SPHERE_GEOMETRY
        Element nav_info = doc.getElementById("NAV_INFO");
        Element box_shape = doc.getElementById("BOX_SHAPE");
        Element box_geom = doc.getElementById("BOX_GEOMETRY");
        Element box_mat = doc.getElementById("BOX_MATERIAL");
        Element sphere_shape = doc.getElementById("SPHERE_SHAPE");
        Element sphere_tx = doc.getElementById("SPHERE_TRANSLATION");
        Element sphere_geom = doc.getElementById("SPHERE_GEOMETRY");

        System.out.println("Demonstrating DEF locating capabilities");
        System.out.println("Navigation Info element = " + nav_info.getTagName());
        System.out.println("Box shape element = " + box_shape.getTagName());
        System.out.println("Box geometry element = " + box_geom.getTagName());
        System.out.println("Box material element = " + box_mat.getTagName());
        System.out.println("Sphere shape element = " + sphere_shape.getTagName());
        System.out.println("Sphere transform element = " + sphere_tx.getTagName());
        System.out.println("Sphere geometry element = " + sphere_geom.getTagName());
    }

    /**
     * This demo shows the accessing of nodes using the DOM interface. We start
     * by grabbing a node that has been DEF'd. With this, we look at a couple
     * of fields and print them out. We can then change the value and print
     * that out too.
     */
    public void demoDOM() {
        Document doc = findXMLFile(DOM_FILE);

        if(doc == null)
            return;

        System.out.println();
        System.out.println("Demonstrating DOM capabilities");

        Element transform = doc.getElementById("TX");

        // Now fetch the viewpoint by looking for the child node that has a
        // tag name of "Viewpoint". We could also use the normal getChildNodes
        // method to find the viewpoint such as:
        //
        // NodeList vp_list = transform.getChildNodes();
        NodeList vp_list = transform.getElementsByTagName("Viewpoint");

        System.out.print("The transform has ");
        System.out.print(vp_list.getLength());
        System.out.println(" viewpoints");

        Element viewpoint = (Element)vp_list.item(0);

        // The field values before we do anything. Of these, only the use
        // instance should be null. All have default values except for
        // description, which we've set in the file
        Attr fov = viewpoint.getAttributeNode("fieldOfView");
        Attr jump = viewpoint.getAttributeNode("jump");
        Attr orient = viewpoint.getAttributeNode("orientation");
        Attr position = viewpoint.getAttributeNode("position");
        Attr desc = viewpoint.getAttributeNode("description");
        Attr bind = viewpoint.getAttributeNode("bind");
        Attr b_time = viewpoint.getAttributeNode("bindTime");
        Attr bound = viewpoint.getAttributeNode("isBound");
        Attr def = viewpoint.getAttributeNode("DEF");
        Attr use = viewpoint.getAttributeNode("USE");

        System.out.println("Viewpoint attributes");
        System.out.println("  fov        : " + fov.getValue());
        System.out.println("  jump       : " + jump.getValue());
        System.out.println("  orientation: " + orient.getValue());
        System.out.println("  position   : " + position.getValue());
        System.out.println("  description: " + desc.getValue());
        System.out.println("  bind       : " + bind.getValue());
        System.out.println("  bindTime   : " + b_time.getValue());
        System.out.println("  isBound    : " + bound.getValue());
        System.out.println("  DEF        : " + def.getValue());
        System.out.println("  USE        : " +
                           ((use == null) ? "not defined" : use.getValue()));

        // Set some of the values in the element now. Both directly and
        // indirectly.

        viewpoint.setAttribute("jump", "false");
        viewpoint.setAttribute("position", "10 10 0");
        viewpoint.setAttribute("description", "Programmatic description");

        bound.setValue("true");
        b_time.setValue("1000");

        System.out.println();
        System.out.println("Viewpoint attributes after changes");
        System.out.println("  fov        : " + fov.getValue());
        System.out.println("  jump       : " + jump.getValue());
        System.out.println("  orientation: " + orient.getValue());
        System.out.println("  position   : " + position.getValue());
        System.out.println("  description: " + desc.getValue());
        System.out.println("  bind       : " + bind.getValue());
        System.out.println("  bindTime   : " + b_time.getValue());
        System.out.println("  isBound    : " + bound.getValue());
        System.out.println("  DEF        : " + def.getValue());
        System.out.println("  USE        : " +
                           ((use == null) ? "not defined" : use.getValue()));
    }

    /**
     * Demonstrate the use of the SAI classes. As the SAI has no way of
     * fetching a document directly, we start by using DOM to fetch a node,
     * casting it to the appropriate SAI type. From there we use the SAI
     * methods to get and set the nodes.
     */
    public void demoSAI() {
        Document doc = findXMLFile(DOM_FILE);

        if(doc == null)
            return;

        System.out.println();
        System.out.println("Demonstrating DOM capabilities");

        Transform transform = (Transform)doc.getElementById("TX");

        // Now fetch the children by looking for the child type that matches
        // a Viewpoint as an object from the list of child nodes.
        BaseNodeArray node_list = transform.getChildren();
        int size = node_list.getSize();
        Viewpoint viewpoint = null;

        for(int i = 0; i < size; i++) {
            BaseNode node = node_list.get1Node(i);
            if(node instanceof Viewpoint) {
                viewpoint = (Viewpoint)node;
                break;
            }
        }

        Float fov = viewpoint.getFieldOfView();
        Boolean jump = viewpoint.getJump();
        Rotation orient = viewpoint.getOrientation();
        Vector3Float position = viewpoint.getPosition();
        org.web3d.x3d.sai.String desc = viewpoint.getDescription();
        Boolean bind = viewpoint.getBind();
        Time b_time = viewpoint.getBindTime();
        Boolean bound = viewpoint.getIsBound();
        org.web3d.x3d.sai.String def = viewpoint.getDEF();
        org.web3d.x3d.sai.String use = viewpoint.getUSE();

        // Those that are commented out here are because the appropriate
        // accessor methods had not made it through to the SAI at the time.
        System.out.println("Viewpoint attributes from SAI");
        //System.out.println("  fov        : " + fov.getValue());
        System.out.println("  jump       : " + jump.booleanValue());
        //System.out.println("  orientation: " + orient.getValue());
        System.out.println("  position   : " +
                           position.getX() + " " +
                           position.getY() + " " +
                           position.getZ());
        System.out.println("  description: " + desc.getValue());
        System.out.println("  bind       : " + bind.booleanValue());
        // System.out.println("  bindTime   : " + b_time.getValue());
        System.out.println("  isBound    : " + bound.booleanValue());
        System.out.println("  DEF        : " + def.getValue());
        System.out.println("  USE        : " +
                           ((use == null) ? "not defined" : use.getValue()));

        jump.setValue(false);
        position.setValue(10, 10, 0);

        desc.setValue("Programmatic description");

        bound.setValue(true);
        //b_time.setValue("1000");

        System.out.println();
        System.out.println("Viewpoint attributes after changes");
        //System.out.println("  fov        : " + fov.getValue());
        System.out.println("  jump       : " + jump.booleanValue());
        //System.out.println("  orientation: " + orient.getValue());
        System.out.println("  position   : " +
                           position.getX() + " " +
                           position.getY() + " " +
                           position.getZ());
        System.out.println("  description: " + desc.getValue());
        System.out.println("  bind       : " + bind.booleanValue());
        // System.out.println("  bindTime   : " + b_time.getValue());
        System.out.println("  isBound    : " + bound.booleanValue());
        System.out.println("  DEF        : " + def.getValue());
        System.out.println("  USE        : " +
                           ((use == null) ? "not defined" : use.getValue()));
    }

    /**
     * Convenience method used to find an XML file and return an input stream
     * to it. The file is located in the classpath.
     *
     * @param name The name of the file to find
     * @return A DOM document version of the file
     */
    private Document findXMLFile(String name) {
        Document ret_val = null;

        InputStream is = null;

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        is = cl.getSystemResourceAsStream(name);

        try {
            ret_val = builder.parse(is);
        } catch(IOException ioe) {
            System.err.println("Error reading the file " + DEF_FILE);
            System.err.println(ioe);
        } catch(SAXException se) {
            System.err.println("Error parsing the file " + DEF_FILE);
            System.err.println(se);
            if(se.getException() != null)
                se.getException().printStackTrace();
            else
                se.printStackTrace();
        }

        return ret_val;
    }

    /**
     * Create an instance of this class and run it.
     */
    public static void main(String[] argv) {
        SAIDOMDemo demo = new SAIDOMDemo();
        demo.demoDEFs();
        demo.demoDOM();
        demo.demoSAI();
    }
}
