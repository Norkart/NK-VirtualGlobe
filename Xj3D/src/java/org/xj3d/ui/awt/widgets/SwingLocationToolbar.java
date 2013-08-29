/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.ietf.uri.*;
import org.ietf.uri.event.ProgressListener;

import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.util.FileHandler;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.impl.core.loading.FramerateThrottle;



/**
 * A swing panel that implements the capabilities of the URL/Location
 * toolbar.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class SwingLocationToolbar extends JPanel
    implements ActionListener, FileHandler, BrowserCoreListener {

    /** Empty skin definition for default */
    private static final Properties DEFAULT_SKIN = new Properties();

    /** Property in browser skin which determines 'Go' image */
    private static final String GO_BUTTON_PROPERTY = "GO.button";

    /** Property in browser skin which determines 'open' image */
    private static final String OPEN_BUTTON_PROPERTY = "OPEN.button";

    /** Property in browser skin which determines 'open' image */
    private static final String RELOAD_BUTTON_PROPERTY = "RELOAD.button";

    /** Default image to use for go button */
    private static final String DEFAULT_GO_BUTTON =
        "images/locationbar/goIcon32x32.gif";

    /** Default image to use for reload button */
    private static final String DEFAULT_OPEN_BUTTON =
        "images/locationbar/openIcon32x32.gif";

    /** Default image to use for reload button */
    private static final String DEFAULT_RELOAD_BUTTON =
        "images/locationbar/reloadIcon32x32.gif";

    /** The maximum number of history elements to keep */
    private static final int MAX_LOCATIONS = 5;

    /** The history property */
    private static final String HISTORY_PROPERTY = "History_";

    /** The current history file */
    private String historyFile;

    /** The list of stored locations */
    private Vector locations;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The textfield to read the values from */
    private JComboBox urlComboBox;

    /** The go button on the URl panel */
    private JButton locationGoButton;

    /** The open button on the URl panel */
    private JButton openButton;

    /** The reload button on the URl panel */
    private JButton reloadButton;

    /** The content directory to load content from.  NULL if none provided */
    private String contentDirectory;

    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;

    /** Should we ingore the actionPerformed for the comboBox */
    private boolean ignoreAction;

    /** Does the location bar contain an empty entry */
    private boolean locationEmpty;

    /** A world loader manager for loading worlds */
    private WorldLoaderManager loader;

    /** The current user_dir */
    private String userDir;

    /** The framerate throttle if in use */
    private FramerateThrottle throttle;

    /** The actions */
    private OpenAction openAction;
    private ReloadAction reloadAction;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param core The browser core implementation to send scene loads to
     * @param urlReadOnly true to make the location bar read only
     * @param showOpenButton true to put an open button with the URL location bar
     * @param showReloadButton true to put a reload button with the URL location bar
     * @param contentDir initial directory to load content from.  Must be a full path.
     * @param reporter The reporter instance to use or null
     */
    public SwingLocationToolbar(BrowserCore core,
                                WorldLoaderManager wlm,
                                boolean urlReadOnly,
                                boolean showOpenButton,
                                boolean showReloadButton,
                                String contentDir,
                                ErrorReporter reporter) {

        this(core,
             wlm,
             urlReadOnly,
             showOpenButton,
             showReloadButton,
             contentDir,
             null,
             reporter);
    }

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param core The browser core implementation to send scene loads to
     * @param urlReadOnly true to make the location bar read only
     * @param showOpenButton true to put an open button with the URL location bar
     * @param showReloadButton true to put a reload button with the URL location bar
     * @param contentDir initial directory to load content from.  Must be a full path.
     * @param reporter The reporter instance to use or null
     * @param skinProperties Customisation of the browser buttons etc
     */
    public SwingLocationToolbar(BrowserCore core,
                                WorldLoaderManager wlm,
                                boolean urlReadOnly,
                                boolean showOpenButton,
                                boolean showReloadButton,
                                String contentDir,
                                Properties skinProperties,
                                ErrorReporter reporter) {

        super(new BorderLayout());

        loader = wlm;
        browserCore = core;
        contentDirectory = contentDir;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(skinProperties == null)
            skinProperties = DEFAULT_SKIN;

        browserCore.addCoreListener(this);

        JLabel l1 = new JLabel(" Location: ");

        String img_name = skinProperties.getProperty(GO_BUTTON_PROPERTY,
                                                     DEFAULT_GO_BUTTON);
        ImageIcon icon = IconLoader.loadIcon(img_name, reporter);
        if(icon == null)
            locationGoButton = new JButton(" Go! ");
        else {
            locationGoButton = new JButton(icon);
        }

        locationGoButton.setEnabled(!urlReadOnly);
        locationGoButton.setMargin(new Insets(0,0,0,0));
        locationGoButton.setToolTipText("Go to the new location");

        locations = new Vector();

        userDir = (String)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    String ret_val = System.getProperty("user.dir");

                    return ret_val;
                }
            }
        );

        userDir += "\\";

        //clearLocations();
        locations.add(userDir);
        locationEmpty = true;
        loadLocations();

        urlComboBox = new JComboBox(locations);
        urlComboBox.setLightWeightPopupEnabled(false);
        urlComboBox.setEditable(true);
        urlComboBox.addActionListener(this);
        urlComboBox.requestFocus();
        urlComboBox.setEditable(!urlReadOnly);

        if(!urlReadOnly) {
            locationGoButton.addActionListener(this);
        }

        DynamicGridLayout dlg = new DynamicGridLayout(1,4,2,2);
        dlg.setColumnSize(0, DynamicGridLayout.MINIMUM);
        dlg.setColumnSize(1, DynamicGridLayout.MINIMUM);
        dlg.setColumnSize(3, DynamicGridLayout.MINIMUM);
        dlg.setRowSize(0, DynamicGridLayout.MINIMUM);
        JPanel p1 = new JPanel(dlg);

        JPanel p2 = new JPanel(new BorderLayout());
        JPanel p4;

        if(showOpenButton || showReloadButton) {

            int columns = 0;

            if(showOpenButton)
                columns++;

            if(showReloadButton)
                columns++;

            p4 = new JPanel(new GridLayout(1, columns));

            if(showOpenButton) {
                img_name = skinProperties.getProperty(OPEN_BUTTON_PROPERTY,
                                                      DEFAULT_OPEN_BUTTON);
                icon = IconLoader.loadIcon(img_name, reporter);

                if(icon == null)
                    openButton = new JButton("Open");
                else {
                    openButton = new JButton(icon);
                }

                openButton.setToolTipText("Open File");
                openButton.setMargin(new Insets(0,0,0,0));

                openAction = new OpenAction(this, this, contentDirectory);
                openButton.addActionListener(openAction);
                p4.add(openButton);
            }

            if (showReloadButton) {
                img_name = skinProperties.getProperty(RELOAD_BUTTON_PROPERTY,
                                                      DEFAULT_RELOAD_BUTTON);
                icon = IconLoader.loadIcon(img_name, reporter);

                if(icon == null)
                    reloadButton = new JButton("Reload");
                else {
                    reloadButton = new JButton(icon);
                }

                reloadButton.setToolTipText("Reload current location");
                reloadButton.setMargin(new Insets(0,0,0,0));

                reloadAction =
                    new ReloadAction(this, this, urlComboBox);

                reloadButton.addActionListener(reloadAction);
                p4.add(reloadButton);
            }

            Font font = l1.getFont().deriveFont(1.0f);

            JLabel spaceL1 = new JLabel(" ");
            JLabel spaceL2 = new JLabel(" ");
            JLabel spaceL3 = new JLabel(" ");
            JLabel spaceL4 = new JLabel(" ");
            spaceL1.setFont(font);
            spaceL2.setFont(font);
            spaceL3.setFont(font);
            spaceL4.setFont(font);

            DynamicGridLayout dlg2 = new DynamicGridLayout(3,2,0,0);
            dlg2.setColumnSize(0, DynamicGridLayout.MINIMUM);
            dlg2.setRowSize(1, DynamicGridLayout.MINIMUM);
            p2 = new JPanel(dlg2);

            p2.add(spaceL1);
            p2.add(spaceL2);
            p2.add(l1);
            p2.add(urlComboBox);
            p2.add(spaceL3);
            p2.add(spaceL4);

            if (showOpenButton)
                p1.add(openButton);

            if (showReloadButton)
                p1.add(reloadButton);

            p1.add(p2);
            p1.add(locationGoButton);
        }

        add(p1, BorderLayout.NORTH);
    }

    /**
     * Get the Open Action.
     *
     * @return Returns the action
     */
    public AbstractAction getOpenAction() {
        return openAction;
    }

    /**
     * Get the Reload Action.
     *
     * @return Returns the action
     */
    public AbstractAction getReloadAction() {
        return reloadAction;
    }

    //---------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
        String uri = scene.getLoadedURI();

        errorReporter.messageReport("Main scene: " + uri + " loaded.");
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed.
     */
    public void browserDisposed() {
    }

    //---------------------------------------------------------
    // Methods defined by FileHandler
    //---------------------------------------------------------

    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Change the panels content to the provided URL.
     *
     * @param url The URL to load.
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(String url) throws IOException {

        String url_str = null;

        // try a file first
        File f = new File(url);
        if(f.exists()) {
            if(f.isDirectory())
                errorReporter.errorReport("File is a directory", null);
            else {
                url_str = f.toURL().toExternalForm();
            }
        } else {
            // Try a URL
            URL url_obj = new URL(url);
            url_str = url_obj.toExternalForm();
        }

        if (throttle != null)
            throttle.startedLoading();

        loader.queueLoadURL(new String[] {url_str}, null);

        addLocation(url_str);
    }

    /**
     * Change the panels content to the provided URL.
     *
     * @param src The source representation to load
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(InputSource src) throws IOException {
        loadURL(src.getURL());
    }

    /**
     * Load the last locations into the location bar.
     */
    public void loadLocations() {
        Preferences prefs = Preferences.userNodeForPackage(SwingLocationToolbar.class);

        for(int i=0; i < MAX_LOCATIONS; i++) {
            String str = prefs.get(HISTORY_PROPERTY + i, null);

            if (str != null) {
                locations.add(str);
            } else
                break;
        }

    }

    //----------------------------------------------------------
    // Methods required by the ActionListener interface.
    //----------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {

        Object src = evt.getSource();

        File fil = null;

        if((src == urlComboBox && (ignoreAction || evt.getActionCommand().equals("comboBoxEdited")))) {
        } else {
            String location = (String) urlComboBox.getSelectedItem();

            // ignore empty selects
            if (location.length() < 1)
                return;

            try {
                loadURL(location);
            } catch(IOException ioe) {
                errorReporter.errorReport("Unable to load " + location, ioe);
            }
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set a progress listener for download progress.  Null will clear.
     *
     * @param listener The progress listener.
     */
    public void setProgressListener(ProgressListener listener) {
        loader.setProgressListener(listener);
    }

    /**
     * Set a Frame throttler.  Null is ok.
     */
    public void setThrottle(FramerateThrottle throttle) {
        this.throttle = throttle;
    }

    /**
     * Stores a new location in the bar.
     *
     * @param loc The location
     * @param max The maximum number to load
     * @param list The current locations
     */
    private void addLocation(String loc) {
        ignoreAction = true;

        if (locationEmpty) {
            urlComboBox.removeItem(userDir);
            locationEmpty = false;
        }

        urlComboBox.removeItem(loc);

        urlComboBox.insertItemAt(loc, 0);

        Object si = urlComboBox.getSelectedItem();

        urlComboBox.setSelectedIndex(0);

        ignoreAction = false;

        Preferences prefs = Preferences.userNodeForPackage(SwingLocationToolbar.class);

        Iterator itr = locations.iterator();
        int i = 0;

        while(itr.hasNext()) {
            String val = (String) itr.next();

            if (val.length() > 0) {
                prefs.remove(HISTORY_PROPERTY + i);
                i++;
            }
        }

        prefs.put(HISTORY_PROPERTY + "0", loc);

        itr = locations.iterator();
        i = 0;

        while(itr.hasNext()) {
            String val = (String) itr.next();

            if (val.length() > 0) {
                prefs.put(HISTORY_PROPERTY + i, val);
                i++;
            }
        }
    }

    /**
     * Clear all location information.
     */
    private void clearLocations() {
        Preferences prefs = Preferences.userNodeForPackage(SwingLocationToolbar.class);

        for(int i=0; i <  MAX_LOCATIONS * 2; i++) {
            prefs.remove(HISTORY_PROPERTY + i);
        }
    }
}
