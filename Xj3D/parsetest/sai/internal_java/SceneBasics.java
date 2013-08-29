/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import java.util.Map;

import org.web3d.x3d.sai.*;

public class SceneBasics implements X3DScriptImplementation {
    private Browser browser;

    public SceneBasics() {
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        browser.println("Got browser");
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
    }

    public void initialize() {
        browser.println("Initialise called. About to fetch scene");

        X3DScene scene = (X3DScene)browser.getExecutionContext();

        // World URL
        browser.println("World URL: " + scene.getWorldURL());

        // Get the component and profiles listed in this scene
        ProfileInfo pi = scene.getProfile();
        browser.println("Profile: " + pi);

        ComponentInfo[] ci = scene.getComponents();

        if(ci == null)
            browser.println("No components declared");
        else {
            for(int i = 0; i < ci.length; i++)
                browser.println("Component: " + ci[i]);
        }

        // Root node names
        X3DNode[] root_nodes = scene.getRootNodes();

        for(int i = 0; i < root_nodes.length; i++)
            browser.println("Root node: " + root_nodes[i].getNodeName());

        X3DRoute[] routes = scene.getRoutes();

        if(routes == null)
            browser.println("No routes declared");
        else {
            for(int i = 0; i < routes.length; i++)
                browser.println("Route: " + routes[i]);
        }
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}