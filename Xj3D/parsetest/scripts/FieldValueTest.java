/**
 * A simple test class to indicate what is happening with the script.
 */
import vrml.field.*;

import vrml.Field;
import vrml.InvalidEventInException;
import vrml.node.Script;

public class FieldValueTest extends Script {
    public FieldValueTest() {
    }

    public void initialize() {
        System.out.println("Initialise called. About to fetch fields");

        try {
            Field i_field = getField("testIntField");

            if(!(i_field instanceof SFInt32)) {
                System.out.println("Incorrect field type " +
                                   i_field.getClass());
            } else {
                SFInt32 real_field = (SFInt32)i_field;
                int value = real_field.getValue();
                System.out.println("Field value is " + value);

                // now set the value and then read it back again
                real_field.setValue(value + 1);
                value = real_field.getValue();

                System.out.println("Updated value is " + value);
            }

        } catch(Exception e) {
            System.out.println("Error fetching field " +  e);
            e.printStackTrace();
        }

    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}