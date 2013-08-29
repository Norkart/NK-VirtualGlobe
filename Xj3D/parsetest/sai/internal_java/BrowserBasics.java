/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;

public class BrowserBasics implements X3DScriptImplementation {
    private Browser browser;

    public BrowserBasics() {
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        browser.println("Got browser");
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch browser");

        if(browser == null) {
            System.out.println("Null browser reference!");
            return;
        }

        System.out.println("Description: " + browser.getDescription());
        System.out.println("Name: " + browser.getName());
        System.out.println("Version: " + browser.getVersion());
        System.out.println("Speed: " + browser.getCurrentSpeed());
        System.out.println("Frame Rate: " + browser.getCurrentFrameRate());
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}