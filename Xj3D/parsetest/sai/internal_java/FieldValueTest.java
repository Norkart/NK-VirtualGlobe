/**
 * A simple test class to indicate what is happening with the script.
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;

public class FieldValueTest implements X3DScriptImplementation {
    private Browser browser;

    private SFInt32 field;

    public FieldValueTest() {
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        browser.println("Got browser");
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        field = (SFInt32)fields.get("testIntField");
    }

    public void initialize() {
        browser.println("Initialise called. About to work on field");

        int value = field.getValue();
        browser.println("Field value is " + value);

        // now set the value and then read it back again
        field.setValue(value + 1);
        value = field.getValue();

        System.out.println("Updated value is " + value);
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}