/**
 * A simple test class to indicate what is happening with the script.
 */
import java.util.Map;

import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;

public class FieldLoadTest implements X3DScriptImplementation {
    private Browser browser;

    public FieldLoadTest() {
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        browser.println("Got browser");
    }

    public void setFields(X3DScriptNode externalView, Map fields) {

        System.out.println("Initialise called. About to fetch fields");

        X3DField int_field = (X3DField)fields.get("testIntField");

        if(int_field == null)
            browser.println("Didn't find testIntField");
        else {
            if(!int_field.isWritable())
                browser.println("testIntField not writable");

            if(!int_field.isReadable())
                browser.println("testIntField not readable");
        }

        X3DField color_field = (X3DField)fields.get("testColorInputOutput");

        if(color_field == null)
            browser.println("Didn't find testColorInputOutput");
        else {
            if(!color_field.isWritable())
                browser.println("testColorInputOutput not writable");

            if(!color_field.isReadable())
                browser.println("testColorInputOutput not readable");
        }

        X3DField node_field = (X3DField)fields.get("testNodeInput");

        if(node_field == null)
            browser.println("Didn't find testNodeInput");
        else {
            if(node_field.isWritable())
                browser.println("testNodeInput is writable");

            if(!node_field.isReadable())
                browser.println("testNodeInput not readable");
        }

        X3DField vec_output = (X3DField)fields.get("testVecOutput");

        if(vec_output == null)
            browser.println("Didn't find testVecOutput");
        else {
            if(!vec_output.isWritable())
                browser.println("testVecOutput not writable");

            if(vec_output.isReadable())
                browser.println("testVecOutput is readable");
        }

        // This attempts to fetch an unknown field and therefor should barf.
        X3DField invalid = (X3DField)fields.get("foobar");

        if(invalid != null)
            browser.println("Oops. Didn't barf on a non-existant field");
    }

    public void initialize() {
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }
}