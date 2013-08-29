/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package web3d.install;

// Standard imports
import java.io.*;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Properties;
import java.util.StringTokenizer;

// Application specific imports

/**
 * An installer application that will automatically download and install
 * any set of JARs into your JRE/lib/ext directory.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Web3dInstaller extends Frame
    implements Runnable, WindowListener, ActionListener {

    /** Name of the property file that contains the file list */
    private static final String PROP_FILE = "web3d-install.properties";

    /** Property that describes the JAR files to grab */
    private static final String JAR_LIST = "jars";

    /** Property that describes the list of native DLLs to install */
    private static final String DLL_LIST = "libs";

    /** Size of the write buffer */
    private static final int BUFFER_SIZE = 4096;

    /** The JRE directory where DLLs are placed. */
    private String jreBinDir;

    /** The JRE directory where JARs are placed. */
    private String jreExtDir;

    /** The write buffer */
    private byte[] buffer;

    /** The area that we spew messages to */
    private TextArea messageLabel;

    /** The button allowing us to exit the application */
    private Button exitButton;

    /** Mutex for stopping the program from exiting too soon */
    private Object mutex;

    /**
     * Private constructor to prevent direct instantiation. The only entry
     * point is through the main() method.
     */
    public Web3dInstaller() {
        super("Web3d Installer");

        setBackground(SystemColor.menu);

        buffer = new byte[BUFFER_SIZE];

        messageLabel = new TextArea("Welcome to the Web3d Installer\n", 8, 80);
        exitButton = new Button("Exit");
        exitButton.setEnabled(false);
        exitButton.addActionListener(this);

        add(messageLabel, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);
        addWindowListener(this);

        pack();
        setLocation(100, 100);

        mutex = new Object();
    }

    /**
     * Grab the bytes for the named file from the JAR file and write it to the
     * local disk in the JRE directory. Switch to control whether the file is
     * native code or Java code as they end up in different places.
     *
     * @param file The name of the file to read/write
     * @param isNative true if this is a native file
     * @throws IOException I/O error writing or reading the file
     */
    private void writeFile(String file, boolean isNative) throws IOException {

        // Grab the input stream for the file to write
        Class cls = getClass();
        ClassLoader cl = cls.getClassLoader();

        InputStream is = cl.getSystemResourceAsStream(file);

        if(is == null) {
            updateText("Couldn't find requested file " + file);
            return;
        }

        BufferedInputStream bis = (is instanceof BufferedInputStream) ?
                                  (BufferedInputStream)is :
                                  new BufferedInputStream(is);


        String out_dir = isNative ? jreBinDir : jreExtDir;
        File out_file = new File(out_dir, file);

        updateText("Writing file " + file + " to " + out_file);

        if(out_file.exists()) {
            updateText("File already exists, replacing: " + file);
        }

        FileOutputStream fos = new FileOutputStream(out_file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int bytes_read;

        while((bytes_read = bis.read(buffer, 0, BUFFER_SIZE)) != -1)
            bos.write(buffer, 0, bytes_read);

        bos.flush();
        bos.close();
        bis.close();
    }

    public void startInstall() {

        setVisible(true);
        Thread th = new Thread(this);
        th.start();

        try {
            synchronized(mutex) {
                mutex.wait();
            }

            // Would you like fries with that?
            updateText("Goodbye. Have a nice day!");
            repaint();

            Thread.sleep(2000);
            setVisible(false);
            System.exit(1);

        } catch(InterruptedException ie) {
        }
    }

    private void updateText(String msg) {
        messageLabel.append(msg);
        messageLabel.append("\n");
    }

    private void exit() {
        exitButton.setEnabled(true);
    }

    //----------------------------------------------------------
    // Methods required by the Runnable interface.
    //----------------------------------------------------------

    /**
     * Run the privileged code block now. Does the action of installing everything.
     *
     * @throws IOException something happened when writing the files
     */
    public void run() {

        String jre_home = System.getProperty("java.home");
        jreBinDir = jre_home + "/bin/";
        jreExtDir = jre_home + "/lib/ext/";

        try {

            // Open the property file and read it in
            Class cls = getClass();
            ClassLoader cl = cls.getClassLoader();

            InputStream is = cl.getSystemResourceAsStream(PROP_FILE);

            if(is == null) {
                updateText("Couldn't find properties file");
                exit();
                return;
            }

            Properties props = new Properties();
            props.load(is);


            String file_list = props.getProperty(JAR_LIST);

            if(file_list != null) {
                StringTokenizer strtok = new StringTokenizer(file_list);

                while(strtok.hasMoreTokens()) {
                    String file = strtok.nextToken();

                    writeFile(file, false);
                }
            }

            file_list = props.getProperty(DLL_LIST);

            if(file_list != null) {
                StringTokenizer strtok = new StringTokenizer(file_list);

                while(strtok.hasMoreTokens()) {
                    String file = strtok.nextToken();

                    writeFile(file, true);
                }
            }
        } catch(IOException ioe) {
            updateText("I/O Error" + ioe.getMessage());
        }

        exit();
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener interface.
    //----------------------------------------------------------

    /**
     * Process an action event on the button. This says to show the big page
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        synchronized(mutex) {
            mutex.notify();
        }
    }

    //----------------------------------------------------------
    // Methods required by the WindowListener interface.
    //----------------------------------------------------------


    /**
     * Process a window open event - start the Java3D renderer on the canvas
     *
     * @param evt The event that caused this method to be called
     */
    public void windowActivated(WindowEvent evt)
    {
        updateText("Starting install");
    }

    /**
     * Process a window closed event. Do nothing.
     *
     * @param evt The event that caused this method to be called
     */
    public void windowClosed(WindowEvent evt) {
    }

    /**
     * Process a window wants to close event (the close button on the top
     * left is hit).
     *
     * @param evt The event that caused this method to be called
     */
    public void windowClosing(WindowEvent evt)
    {
        System.out.println("window closing");
        exit();
    }

    /**
     * Process a window loosing activation.
     *
     * @param evt The event that caused this method to be called
     */
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Process a window becoming de-iconified. Restart the renderer.
     *
     * @param evt The event that caused this method to be called
     */
    public void windowDeiconified(WindowEvent evt) {
    }

    /**
     * Process a window being iconified. Stop the rendered.
     *
     * @param evt The event that caused this method to be called
     */
    public void windowIconified(WindowEvent evt) {
    }

    /**
     * Process a window open event - start the Java3D renderer on the canvas
     *
     * @param evt The event that caused this method to be called
     */
    public void windowOpened(WindowEvent evt) {
    }


    /**
     * Main entry point for the installer. Used to create and install the
     * software.
     *
     * @param args The argument list
     */
    public static void main(String[] args) {

        Web3dInstaller installer = new Web3dInstaller();
        installer.startInstall();
    }
}
