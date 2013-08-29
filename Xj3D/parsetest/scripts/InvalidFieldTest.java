/**
 * A test to show invalid eventOut handling
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class InvalidFieldTest extends Script {
	SFVec3f location;

    public InvalidFieldTest() {
    }

    public void initialize() {
		try {
			location = (SFVec3f) getField("foobar");
			System.out.println("FAILED, no InvalidFieldException");
		}
		catch (InvalidFieldException e) {
			System.out.println("SUCCESS, generated InvalidFieldException");
		}

		try {
			location = (SFVec3f) getEventOut("foobar");
			System.out.println("FAILED, no InvalidEventOutException");
		}
		catch (InvalidEventOutException e) {
			System.out.println("SUCCESS, generated InvalidEventOutException");
		}

		try {
			location = (SFVec3f) getEventIn("foobar");
			System.out.println("FAILED, no InvalidEventInException");
		}
		catch (InvalidEventInException e) {
			System.out.println("SUCCESS, generated InvalidEventInException");
		}
    }

    public void shutdown() {
        System.out.println("Shutdown called");
    }
}
