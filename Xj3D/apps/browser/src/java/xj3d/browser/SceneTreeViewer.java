/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External Imports
import java.util.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Cursor;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.*;
import javax.swing.tree.*;
import org.j3d.aviatrix3d.*;

// Local Imports
import org.web3d.util.SimpleStack;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.core.BaseMetadataString;
import org.web3d.vrml.renderer.common.nodes.networking.BaseInline;
import org.web3d.vrml.renderer.common.nodes.shape.BaseShape;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;
import org.web3d.vrml.renderer.common.nodes.BaseMetadataObjectNode;

/**
 * Provide a tree view of the currently loaded scene.
 * Display any metadata on the node
 *
 * @author Alan Hudson
 * @version
 */
public class SceneTreeViewer extends JPanel
    implements BrowserCoreListener, SceneGraphTraversalSimpleObserver,
               TreeSelectionListener, ActionListener, Runnable {

    /** How many time to flash a node when selected */
    private static final int NUM_FLASHES = 3;

    /** How long is each flash */
    private static final int FLASH_LENGTH = 100;

    /** The current scene */
    private VRMLScene scene;

    /** Scene traverser */
    private SceneGraphTraverser traverser;

    /** The JTree */
    private JTree tree;

    /** The tree model */
    private DefaultTreeModel treeModel;

    /** The top of the tree */
    private DefaultMutableTreeNode top;

    /** Hash of parent to tree node */
    private HashMap<VRMLNodeType, DefaultMutableTreeNode> treeMap;

    /** Stack of scenes */
    private SimpleStack sceneStack;

    /** Node Effects class for showing nodes */
    private NodeEffects nodeEffects;

    /** Transparent appearance used for hiding nodes */
    private Appearance altApp;

    /** The tree panel */
    private JPanel panel;

    /** The scroll pane */
    private JScrollPane scrollPane;

    /** The currently selected node */
    private DefaultMutableTreeNode currentNode;

    /** The currently hidden node */
    private Node hiddenNode;

    /** The node filter */
    private NodeFilter nodeFilter;

    /** Buttons */
    private JButton blinkButton;
    private JButton hideButton;
    private JButton showButton;
    private JButton centerButton;
    private JComboBox filterBox;

    /** Thread for updating tree */
    private Thread thread;

    /** Is the tree builder running */
    private boolean running;

    public SceneTreeViewer(OGLStandardBrowserCore core, NodeFilter initialFilter) {
        treeMap = new HashMap<VRMLNodeType, DefaultMutableTreeNode> (2000);
        sceneStack = new SimpleStack();
        nodeEffects = new NodeEffects();
        nodeFilter = initialFilter;
        running = false;

        traverser = new SceneGraphTraverser();
        traverser.setObserver(this);

        core.addCoreListener(this);

        JPanel buttonPanel = new JPanel(new GridLayout(1,4));
        blinkButton = new JButton("Blink");
        hideButton = new JButton("Hide");
        showButton = new JButton("Show Only");
        centerButton = new JButton("Center");
        blinkButton.addActionListener(this);
        hideButton.addActionListener(this);
        showButton.addActionListener(this);
        centerButton.addActionListener(this);

        buttonPanel.add(blinkButton);
        buttonPanel.add(hideButton);
        buttonPanel.add(showButton);
        //buttonPanel.add(centerButton);

        panel = new JPanel(new BorderLayout());
        scrollPane = new JScrollPane();
        JViewport view = scrollPane.getViewport();
        view.add(panel);

        JPanel filterPanel = new JPanel();
        JLabel filterLabel = new JLabel("Filter");
        NodeFilter[] filters = new NodeFilter[] {
           new NullNodeFilter(), new GeometryNodeFilter()
        };
        filterBox = new JComboBox(filters);
        filterBox.addActionListener(this);
        filterPanel.add(filterLabel);
        filterPanel.add(filterBox);

        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        // Generate an invisible alternate appearance
        altApp = new Appearance();
        Material mat = new Material();
        mat.setTransparency(0);
        altApp.setMaterial(mat);

        scene = core.getScene();

        if (scene != null)
            runBuildTree();

        show();
    }

    //----------------------------------------------------------
    // BrowserCoreListener methods
    //----------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
        this.scene = scene;
        runBuildTree();
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
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
    }

    //-------------------------------------------------------------------------
    // SceneGraphTraverserSimpleObserver methods
    //-------------------------------------------------------------------------

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {

        if (nodeFilter != null) {
            if (!nodeFilter.accept(child)) {
                return;
            }
        }

        DefaultMutableTreeNode c_node = null;

        BasicScene curr_scene = (BasicScene) sceneStack.peek();
        Map defNames = curr_scene.getDEFNodes();

        // TODO: Linear search which could be bad on some scenes
        Iterator<Map.Entry> nodes = defNames.entrySet().iterator();
        String defName = null;

        while(nodes.hasNext()) {
            Map.Entry entry = nodes.next();
            if (entry.getValue() == child) {
                defName = (String) entry.getKey();
            }
        }

        c_node = new DefaultMutableTreeNode();
        String label = null;

        if (used) {
            label = "USE " + defName;
        } else if (defName != null) {
            label = "DEF " + defName + " " + child.getVRMLNodeName();
        } else {
            label = child.getVRMLNodeName();
        }

        addName(c_node, child);
        //addMetadata(c_node, child);

        c_node.setUserObject(new STNodeWrapper(child, label));

        DefaultMutableTreeNode p_node = null;
        boolean new_scene = false;

        if (parent == null) {
            p_node = treeMap.get(child);

            new_scene = true;

            if (p_node == null && (child instanceof VRMLWorldRootNodeType)) {
                treeMap.put(child, top);

                return;
            } else {
                // ignore filtered out nodes
                return;
            }
/*
            if (p_node == null && (child instanceof VRMLWorldRootNodeType)) {
System.out.println("found world root");
                treeMap.put(child, top);

                return;
            } else {
System.out.println("Would of ignored: " + child);
                // ignore filtered out nodes
                //return;
            }
*/
        } else {
            p_node = treeMap.get(parent);

            if (p_node == null)
                System.out.println("Can't find mapping for: " + parent);
        }

        if (!new_scene) {
            p_node.add(c_node);
            treeMap.put(child, c_node);
        }

        if (child instanceof VRMLInlineNodeType) {
            BaseInline inline = (BaseInline) child;
            BasicScene scene = inline.getContainedScene();

            sceneStack.push(scene);
            VRMLWorldRootNodeType root = (VRMLWorldRootNodeType)
                    scene.getRootNode();

            treeMap.put(root, c_node);

            traverser = new SceneGraphTraverser();
            traverser.setObserver(this);
            traverser.traverseGraph(root);

            sceneStack.pop();
        }
    }

    //----------------------------------------------------------
    // TreeSelectionListener methods
    //----------------------------------------------------------
    public void valueChanged(TreeSelectionEvent e) {
        if (hiddenNode != null) {
            nodeEffects.restoreAlternateAppearance(hiddenNode);
            hiddenNode = null;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (node == null) return;

        currentNode = node;
    }

    //----------------------------------------------------------
    // Runnable methods
    //----------------------------------------------------------
    public void run() {
        running = true;
        buildTree();
        running = false;
    }

    //----------------------------------------------------------
    // ActionListener methods
    //----------------------------------------------------------

    /**
     * Handle local panel buttons.
     *
     * @param evt The action event
     */
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if (src == filterBox) {
            nodeFilter = (NodeFilter) filterBox.getSelectedItem();

            runBuildTree();
            return;
        }

        if (currentNode == null)
            return;

        Object ni = currentNode.getUserObject();
        VRMLNodeType vrml_node = null;

        if (ni instanceof STNodeWrapper) {
            STNodeWrapper nodeInfo = (STNodeWrapper) currentNode.getUserObject();
            vrml_node = nodeInfo.getVRMLNode();
        }

        boolean find_parent = false;
        OGLVRMLNode ogl_node = null;
        SceneGraphObject sgo = null;

        if (vrml_node == null) {
            find_parent = true;
        } else {
            ogl_node = (OGLVRMLNode) vrml_node;
            sgo = ogl_node.getSceneGraphObject();
        }

        if (sgo == null && !find_parent) {
            return;
        }

        if (find_parent || sgo instanceof NodeComponent) {
            // Go up till we find a Shape or reach the top
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
            VRMLNodeType shape_node = null;

            while(parent != null) {
                Object userObject = parent.getUserObject();
                VRMLNodeType node = null;

                if (userObject instanceof STNodeWrapper)
                    node = (VRMLNodeType) ((STNodeWrapper)parent.getUserObject()).getVRMLNode();

                if (node != null && ((node instanceof BaseShape) || (node instanceof BaseGroupingNode))) {
                    shape_node = node;
                    break;
                } else {
                    parent = (DefaultMutableTreeNode) parent.getParent();
                }
            }

            if (shape_node == null)
                return;

            ogl_node = (OGLVRMLNode) shape_node;
            sgo = ogl_node.getSceneGraphObject();
        }

        if (src == blinkButton) {
            // TODO: Sync to application update thread
            nodeEffects.blink((Node)sgo, NUM_FLASHES, FLASH_LENGTH);
        } else if (src == hideButton) {
            if (hiddenNode == null) {
                nodeEffects.setAlternateAppearance((Node)sgo, altApp);
                hiddenNode = (Node) sgo;

                hideButton.setText("Unhide");
            } else {
                nodeEffects.restoreAlternateAppearance(hiddenNode);
                hiddenNode = null;
                hideButton.setText("Hide");
            }
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    /**
     * Build the tree view from the current scene.
     */
    private void buildTree() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            currentNode = null;

            if (tree != null)
                panel.remove(tree);

            top = new DefaultMutableTreeNode("Root");
            DefaultTreeModel treeModel = new DefaultTreeModel(top);
            tree = new JTree(treeModel);
            tree.getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setLargeModel(true);
            //tree.setRootVisible(false);
            //tree.setShowsRootHandles(true);

            //Listen for when the selection changes.
            tree.addTreeSelectionListener(this);

            panel.add(tree, BorderLayout.CENTER);

            top.removeAllChildren();
            treeMap.clear();
            sceneStack.clear();
            traverser.reset();

            VRMLWorldRootNodeType root = (VRMLWorldRootNodeType)
                    scene.getRootNode();

            // TODO: I suspect we should do this on another thread

            sceneStack.push(scene);
            traverser.traverseGraph(root);
            sceneStack.pop();

            try {
                TreeNode first_node = top.getChildAt(0);
                tree.scrollPathToVisible(getTreePath(first_node));
            } catch(Exception e) {
                // ignore
            }
            treeModel.nodeStructureChanged(top);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Run the build tree method on its own thread.
     */
    public void runBuildTree() {
        if (running) {
            // busy wait till its down
            while(running) {
                try {
                    Thread.sleep(100);
                } catch(Exception e) {}
            }
        }

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Build the TreePath to use to open and scroll the JTree if needed
     *
     * @param treeNode the node to open
     */
    private TreePath getTreePath(TreeNode treeNode) {

        ArrayList<TreeNode> treePath = new ArrayList<TreeNode>();

        treePath.add(treeNode);

        while (treeNode.getParent() != null) {
            treeNode = treeNode.getParent();
            treePath.add(0, treeNode);
        }

        TreeNode[] path = new TreeNode[treePath.size()];
        treePath.toArray(path);

        return new TreePath(path);

    }

    /**
     * Display the name field if the node contains one.
     *
     * @param treeNode The tree node to add to
     * @param vrmlNode The VRML node
     */
    public void addName(DefaultMutableTreeNode treeNode, VRMLNodeType vrmlNode) {
        int idx = vrmlNode.getFieldIndex("name");

        if (idx == -1)
            return;

        String name = vrmlNode.getFieldValue(idx).stringValue;

        if (vrmlNode instanceof BaseMetadataString) {
            idx = vrmlNode.getFieldIndex("value");

            if (idx == -1)
                return;

            String[] values = vrmlNode.getFieldValue(idx).stringArrayValue;

            if (values.length > 0 && name != null)
                treeNode.insert(new DefaultMutableTreeNode(name + " = " + values[0]),0);
        } else {
            if (name != null) {
                treeNode.insert(new DefaultMutableTreeNode("Name: " + name),0);
            }
        }
    }

    /**
     * Display the metadata field if the node contains one.
     *
     * @param treeNode The tree node to add to
     * @param vrmlNode The VRML node
     */
    public void addMetadata(DefaultMutableTreeNode treeNode, VRMLNodeType vrmlNode) {
        int idx = vrmlNode.getFieldIndex("metadata");

        if (idx == -1)
            return;

        VRMLNodeType meta_node = (VRMLNodeType) vrmlNode.getFieldValue(idx).nodeValue;
        String label = null;

        if (meta_node instanceof BaseMetadataString) {
            String name = meta_node.getFieldValue(meta_node.getFieldIndex("name")).stringValue;
            String ref = meta_node.getFieldValue(meta_node.getFieldIndex("reference")).stringValue;
            String[] value = meta_node.getFieldValue(meta_node.getFieldIndex("value")).stringArrayValue;

            if (value.length > 0) {
                label = name + " = " + value[0];
            }
        }

        if (label != null)
            treeNode.add(new DefaultMutableTreeNode(label));
    }
}
