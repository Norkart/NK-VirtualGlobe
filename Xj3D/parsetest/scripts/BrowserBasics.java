/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class BrowserBasics extends Script {
    public BrowserBasics() {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch browser");

        Browser browser = getBrowser();

        if(browser == null) {
            System.out.println("Null browser reference!");
            return;
        }

        System.out.println("World URL: " + browser.getWorldURL());
        System.out.println("Description: " + browser.getDescription());
        System.out.println("Name: " + browser.getName());
        System.out.println("Version: " + browser.getVersion());
        System.out.println("Speed: " + browser.getCurrentSpeed());
        System.out.println("Frame Rate: " + browser.getCurrentFrameRate());
    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}