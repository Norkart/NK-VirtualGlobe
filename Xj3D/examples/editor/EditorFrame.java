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


// Standard library imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

// Application specific imports
import org.web3d.x3d.util.X3DBrowser;
import org.web3d.x3d.dom.swing.DOMTreeModel;
import org.web3d.x3d.dom.swing.DOMTreeNode;
import org.web3d.x3d.dom.swing.DOMTreeCellEditor;
import org.web3d.x3d.dom.swing.DOMTreeCellRenderer;

/**
 * The window frame that holds everything together.
 * <p>
 *
 * The frame builds a simple menubar across the top and places two parts
 * on screen - a DOMTree and the X3DBrowser to display the content.
 * There is one menu - file. It contains actions for a new document,
 * opening an existing document, exiting the application and some info about
 * who wrote it - me :)
 */
class EditorFrame extends JFrame
    implements FileOpenListener,
               NewDocListener,
               ExitListener,
               NodeCreationListener,
               TreeModelListener
{

    /** The base string name for the window title */
    private static final String BASE_TITLE =
        "SEX3D, The Simple Editor for X3D";

    /** The document builder instance used to create and parse XML */
    private DocumentBuilder builder;

    /** The document we are currently editing */
    private Document document;

    /** The currently rendered tree model representing the DOM */
    private DOMTreeModel treeModel;

    /** The current JTree that represents our DOM */
    private X3DTree domTree;

    /** The browser used to render the scenegraph with */
    private X3DBrowser browser;

    /** The home directory of this application when running */
    private final String cwd;

    /** Flag to indicate if this is the first time the window has been shown */
    private boolean firstShow = true;

    /**
     * Create a new editor frame instance that uses the given document builder
     * to managed the XML side.
     *
     * @param bldr The builder to create docs with
     */
    EditorFrame(DocumentBuilder bldr)
    {
        super(BASE_TITLE);

        cwd = System.getProperty("user.home");

        // Build the frame to our requirements.
        long evt_mask = WindowEvent.WINDOW_CLOSING |
                        WindowEvent.WINDOW_ICONIFIED |
                        WindowEvent.WINDOW_DEICONIFIED;
        enableEvents(evt_mask);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        setSize(800, 600);

        builder = bldr;

        // create a menubar and menus
        JMenuBar menu_bar = new JMenuBar();

        // File menu. Set up all the actions first
        FileOpenAction foa = new FileOpenAction(this);
        foa.setListener(this);

        NewDocAction nda = new NewDocAction();
        nda.setListener(this);

        ExitAction ea = new ExitAction();
        ea.setListener(this);

        JMenu file_menu = new JMenu("File");
        file_menu.add(foa);
        file_menu.add(nda);
        file_menu.addSeparator();
        file_menu.add(ea);

        menu_bar.add(file_menu);
        setJMenuBar(menu_bar);

        // Now, let's build the window contents!
        Container contents = getContentPane();

        document = builder.newDocument();
        treeModel = new DOMTreeModel(document);
        treeModel.addTreeModelListener(this);

        domTree = new X3DTree(treeModel);
        domTree.putClientProperty("JTree.lineStyle", "Angled");

        // Set these after creating an empty document because the default
        // model in the JTree above will cause class casting exceptions in
        // the renderer.
        domTree.setEditable(true);
        domTree.setShowsRootHandles(true);
        domTree.setExpandsSelectedPaths(true);
        domTree.setBorder(new EmptyBorder(3, 5, 3, 5));
        domTree.setCellRenderer(new DOMTreeCellRenderer());
        domTree.setCellEditor(new DOMTreeCellEditor());

        JScrollPane scroll = new JScrollPane(domTree);
        JSplitPane pane = new JSplitPane();
        pane.setDividerLocation(300);
        pane.setLeftComponent(scroll);

        browser = new X3DBrowser();
		browser.setSupportEvents(true);
        browser.setDocumentBuilder(builder);

        pane.setRightComponent(browser.getComponent());

        contents.add(pane, BorderLayout.CENTER);

        // Now the node selection panel on the left
        NodePanel nodes = new NodePanel();
        nodes.setNodeCreationListener(this);
        scroll = new JScrollPane(nodes);
        contents.add(scroll, BorderLayout.NORTH);
    }

    /**
     * Override the default window event processing mechanism to trap the
     * window closing event. All other events are sent through to the base
     * class for processing.
     *
     * @param evt The event that caused this class to be called
     */
    protected void processWindowEvent(WindowEvent evt)
    {
        int type = evt.getID();

        switch(type)
        {
            case WindowEvent.WINDOW_CLOSING:
                exitNow();
                break;

            // use these two events to start and stop the Java3D processing
            case WindowEvent.WINDOW_ICONIFIED:
                browser.stopRender();
                break;

            case WindowEvent.WINDOW_DEICONIFIED:
                browser.startRender();
                break;

        }

        super.processWindowEvent(evt);
    }

    /**
     * Once the window is visible, create the browser items.
     *
     * @param vis true if it should be shown
     */
    public void setVisible(boolean vis)
    {
        super.setVisible(vis);

        if(vis)
        {
            domTree.setEnabled(false);

            if(firstShow)
            {
                startNewDocument();
                firstShow = false;
            }
            else
                browser.setDocument(document, cwd);

            domTree.setEnabled(true);
        }
    }

    //------------------------------------------------------------
    // Methods for TreeSelectionListener
    //------------------------------------------------------------

    /**
     * Invoked after a node (or a set of siblings) has changed in some way.
     *
     * @param evt The event that caused this to be called
     */
    public void treeNodesChanged(TreeModelEvent evt)
    {
    }

    /**
     * Invoked after nodes have been inserted into the tree.
     *
     * @param evt The event that caused this to be called
     */
    public void treeNodesInserted(TreeModelEvent evt)
    {
        // Simple implmentation will automatically select the new node of the
        // root document that changed.
        TreePath parent_path = evt.getTreePath();

        // just get the first one and select that.
        Object[] children = evt.getChildren();
        TreePath select_path = parent_path.pathByAddingChild(children[0]);

        domTree.setSelectionPath(select_path);
    }

    /**
     * Invoked after nodes have been removed from the tree. Select the parent
     * object of the node that was removed.
     *
     * @param evt The event that caused this to be called
     */
    public void treeNodesRemoved(TreeModelEvent evt)
    {
        TreePath selected = evt.getTreePath();
        domTree.setSelectionPath(selected.getParentPath());
    }

    /**
     * Process an event where a large section of the tree structure has
     * changed - for example deleting a subtree area.
     *
     * @param evt The event that caused this to be called
     */
    public void treeStructureChanged(TreeModelEvent evt)
    {
        // Simple implmentation will automatically select the new node of the
        // root document that changed.
        TreePath path = evt.getTreePath();
        domTree.setSelectionPath(path);
    }

    //------------------------------------------------------------
    // Methods for FileOpenListener
    //------------------------------------------------------------

    /**
     * Request that the named file be opened by the application. A check has
     * been made to make sure that the file already exists before this method
     * is called.
     *
     * @param file The file to be opened
     */
    public void openFile(File file)
    {
        try
        {
            firstShow = false;
            document = builder.parse(file);
            treeModel = new DOMTreeModel(document);
            domTree.setModel(treeModel);
            treeModel.addTreeModelListener(this);

            browser.setDocument(document, file.getPath());

            // Now make the top node automatically selected
            Object root_node = treeModel.getRoot();
            TreePath root_path = new TreePath(root_node);
            domTree.setSelectionPath(root_path);
        }
        catch(IOException ioe)
        {
            System.err.println("Error reading the file " + file.getName());
            System.err.println(ioe);
        }
        catch(SAXException se)
        {
            System.err.println("Error parsing the file " + file.getName());
            System.err.println(se);
            if(se.getException() != null)
                se.getException().printStackTrace();
            else
                se.printStackTrace();
        }
    }

    //------------------------------------------------------------
    // Methods for NewDocListener
    //------------------------------------------------------------

    /**
     * Request to start a new document. Replaces the existing document with a
     * clean, empty document.
     */
    public void startNewDocument()
    {
        document = builder.newDocument();
        Node x3d = document.createElement("X3D");
        Node scene = document.createElement("Scene");

        document.appendChild(x3d);
        x3d.appendChild(scene);

        treeModel = new DOMTreeModel(document);
        domTree.setModel(treeModel);
        treeModel.addTreeModelListener(this);

        browser.setDocument(document, cwd);

        // Now make the top node automatically selected
        Object root_node = treeModel.getRoot();
        TreePath root_path = new TreePath(root_node);
        domTree.setSelectionPath(root_path);
    }

    //------------------------------------------------------------
    // Methods for ExitListener
    //------------------------------------------------------------

    /**
     * Request that the application be closed.
     */
    public void exitNow()
    {
        browser.stopRender();
        System.exit(0);
    }

    //------------------------------------------------------------
    // Methods for NodeCreationListener
    //------------------------------------------------------------

    /**
     * Request to create a new node. Add it as a child to the currently
     * selected node
     *
     * @param name The name of the node to create
     */
    public void createNode(String name)
    {
        TreePath sel_path = domTree.getSelectionPath();

        if(sel_path == null)
            return;

        DOMTreeNode tree_node = (DOMTreeNode)sel_path.getLastPathComponent();

        Node parent = tree_node.getNode();
        // hack fix because it's stuffed.
        if(name.equals("Color"))
            name = "ColorNode";
        Element el = document.createElement(name);

        if(el == null)
            System.out.println("Unknown element type " + name);
        else
            parent.appendChild(el);
    }
}
