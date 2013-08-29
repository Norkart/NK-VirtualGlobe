/*****************************************************************************
 *
 *                            (c) Yumetech 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package web3d.install.applet;

// Standard imports
import java.awt.*;

import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.MalformedURLException;

import javax.media.j3d.Canvas3D;

import vrml.eai.BrowserFactory;
import vrml.eai.VrmlComponent;
import vrml.eai.Browser;

// Application specific imports
// none

/**
 * The applet that forms the basis of the Item viewer client application.
 * <p>
 *
 * <b>Configuration Parameters</b>
 * <p>
 * <ul>
 * <li><code>item</code>: The ID of the item to be loaded</li>
 * <li><code>model</code>: The type of model to load - book, letter etc.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class X3DBrowserApplet extends Applet
    implements ActionListener
{
    private Component canvas;

    /** The VRML EAI Browser instance */
    private Browser vrmlBrowser;

    /** Textfield containing the URL */
    private TextField urlText;

    /**
     * Create a new instance of this applet.
     */
    public X3DBrowserApplet()
    {
    }

    public void init()
    {
        setLayout(new BorderLayout());

        Button go = new Button("Go");
        go.addActionListener(this);

        urlText = new TextField();

        Panel p1 = new Panel(new BorderLayout());
        p1.add(go, BorderLayout.EAST);
        p1.add(urlText, BorderLayout.CENTER);

        add(p1, BorderLayout.NORTH);

        // Put together the parameters that we want to play with
        String[] browser_params = new String[]
        {
            "xj3d.browser.ui.type=awt",
            "xj3d.browser.ui.navbar.shown=false",
            "xj3d.browser.ui.urlbar.shown=true"
        };

        VrmlComponent comp =
            BrowserFactory.createVrmlComponent(browser_params);

        vrmlBrowser = comp.getBrowser();

        canvas = (Component)comp;

        add(canvas, BorderLayout.CENTER);
    }

    public void start()
    {
        // construct the URL and get the browser starting to load it
        URL doc_base = getDocumentBase();

        urlText.setText(doc_base.toExternalForm());
    }

    public void stop()
    {
    }

    public void destroy()
    {
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
        String[] url_list = { urlText.getText() };

        vrmlBrowser.loadURL(url_list, null);

    }
}
