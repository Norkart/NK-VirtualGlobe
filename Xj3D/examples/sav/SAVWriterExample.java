// External Imports
import java.io.*;

// Local Imports
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.sav.*;

/**
 * An example of using the SAV interface to write out an X3D file.
 *
 * @author Alan Hudson
 * @version
 */
public class SAVWriterExample {
    public static final void main(String args[]) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

            Exporter writer;

            ErrorReporter console = new PlainTextErrorReporter();

            writer = new X3DClassicRetainedExporter(outputStream,3,0,console);

            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Interchange");

            writer.startNode("Transform", null);
            writer.startField("children");
                writer.startNode("Shape",null);
                   writer.startField("geometry");
                   writer.startNode("Box",null);
                      writer.startField("size");
                      writer.fieldValue("2 2 2");   // Does an implicit endField
                   writer.endNode();
                writer.endNode();
            writer.endField();  // Explicit required for MFNodes
            writer.endNode();
            writer.endDocument();

            outputStream.flush();
            System.out.println("File:\n" + outputStream.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
