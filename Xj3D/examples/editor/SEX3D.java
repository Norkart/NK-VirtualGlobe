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
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

// Application specific imports
import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;

/**
 * A Simple Editor for X3D (SEX3D) startup class.
 * <p>
 *
 * This class is responsible for initialising the editor. It creates the
 * various components and places them into a running state.
 * <p>
 * Usage information is:
 * <pre>
 *   java SEX3D [-help | [-default] [filename]]
 * </pre>
 * <p>
 * The application can exit with the following values:
 * <ul>
 * <li>0 - Successful, normal exit
 * <li>1 - Could not configure the required JAXP factory
 * <li>2 - The parser configuration failed
 * </ul>
 */
public class SEX3D
{
    // Argument strings

    /** The argument to use the default parser */
    private static final String USE_DEFAULT_ARG = "-default";

    /** The argument to show the usage information */
    private static final String HELP_ARG = "-help";

    /** The usage message */
    private static final String USAGE_MSG =
        "java SEX3D [-help | [-default] [filename]]\n" +
        "   -help     Show this help message\n" +
        "   -default  Use the default JAXP parser not X3D's custom parser";

    /** The frame containing the editor */
    private EditorFrame editor;

    /**
     * Create an instance of the demo class. Constructs an instance of the
     * document builder that we will use for the individual demos. If the flag
     * is set it will use JAXP's default parser rather than the X3D specific
     * custom parser.
     *
     * @param stdParser True if to use the default DOM parser
     */
    public SEX3D(boolean stdParser)
    {
        // Set the system property that tells JAXP how to load our custom
        // parser. We can either to it like this in code or as a command
        // line property eg
        // java -Djavax.xml......=org.web3d.x3d..... SAIDOMDemo
        if(!stdParser)
            System.setProperty(X3DConstants.JAXP_FACTORY_PROPERTY,
                               X3DConstants.DOM_FACTORY_IMPL);

        DocumentBuilder builder = null;

        // Now create an instance of our builder so that we could create new
        // documents or parse existing ones.
        try
        {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

            factory.setIgnoringElementContentWhitespace(true);

            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new X3DEntityResolver());
            builder.setErrorHandler(new X3DErrorHandler());
        }
        catch(FactoryConfigurationError fce)
        {
            System.err.println("Error configuring the factory. " + fce);
            System.exit(1);
        }
        catch(ParserConfigurationException pce)
        {
            System.err.println("Error configuring the parser. " + pce);
            System.exit(2);
        }

        editor = new EditorFrame(builder);
    }

    /**
     * Set the starter document to the given filename.
     *
     * @param file The name of the file to open initially
     */
    void useFile(File file)
    {
        editor.openFile(file);
    }

    /**
     * Display the editor on screen. It will now become visible with whatever
     * startup arrangements have been previously made.
     */
    void show()
    {
        editor.setVisible(true);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] argv) {

        boolean has_file = false;
        boolean use_default = false;

        File start_file = null;
        String arg;

        switch(argv.length)
        {
            case 1:
                arg = argv[0];
                if(arg.equals(USE_DEFAULT_ARG))
                    use_default = true;
                else if(arg.equals(HELP_ARG))
                {
                    System.out.println(USAGE_MSG);
                    return;
                }
                else
                {
                    // assume it is a file name
                    start_file = new File(argv[0]);
                    has_file = start_file.exists();
                }
                break;

            case 2:
                if(!argv[0].equals(USE_DEFAULT_ARG))
                {
                    System.out.println(USAGE_MSG);
                    return;
                }
                else
                    use_default = true;

                start_file = new File(argv[1]);
                has_file = start_file.exists();

        }

        // right, now start the application....
        SEX3D editor = new SEX3D(use_default);
        if(has_file)
            editor.useFile(start_file);

        editor.show();
    }
}
