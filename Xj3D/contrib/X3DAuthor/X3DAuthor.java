//
//   X3DAuthor.java
//
//------------------------------------------------------------------------
//
//      Portions Copyright (c) 2000 SURVICE Engineering Company.
//      All Rights Reserved.
//      This file contains Original Code and/or Modifications of Original
//      Code as defined in and that are subject to the SURVICE Public
//      Source License (Version 1.3, dated March 12, 2002)
//
//      A copy of this license can be found in the doc directory
//------------------------------------------------------------------------
//
//      Developed by SURVICE Engineering Co. (www.survice.com)
//      April 2002
//
//      Authors:
//              Bob Parker
//------------------------------------------------------------------------
import java.io.*;
import java.util.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import java.net.URL;
import java.net.MalformedURLException;

// java3d stuff
import javax.media.j3d.TransformGroup;
import javax.media.j3d.GraphicsConfigTemplate3D;

import org.w3c.dom.*;

/*
import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URIResourceStreamFactory;
import org.ietf.uri.URI;

import org.web3d.vrml.sav.*;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.JavascriptResourceFactory;
import org.web3d.vrml.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.j3d.input.LinkSelectionListener;
import org.web3d.vrml.j3d.nodes.J3DViewpointNodeType;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.xj3d.core.eventmodel.ExecutionSpaceManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.scripting.SceneBuilder;
import org.web3d.vrml.scripting.SceneBuilderFactory;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;

import org.xj3d.core.loading.ExternalLoadManager;
import org.xj3d.core.loading.ScriptLoader;

import org.xj3d.impl.core.eventmodel.ListsRouterFactory;
import org.xj3d.impl.core.loading.MemCacheLoadManager;
import org.xj3d.impl.core.loading.DefaultLoadManager;
*/

public class X3DAuthor extends JFrame
    implements DragGestureListener,
           DragSourceListener,
           DropTargetListener {

    protected Container contentPane;
    protected Document document;
    protected Document crDocument;
    protected Document schema;
    protected Hashtable schemaHash;
    protected String nodeTypes[];
    protected NodeAdapter documentRoot;
    protected NodeAdapter crDocumentRoot;
    protected File saveFile;
    protected File exportFile;
    protected File newFile;
    protected String exportFilePath;

    // used to quickly reconfigure the GUI to use or
    // not to use a built in viewer. It's best to leave
    // this false until Xj3D has matured.
    protected boolean builtInViewer = false;

    protected boolean cdataFlag = false;

    // tracks the number of rows  for the tableModel
    protected int tableRows = 2;

    // allowable attributes for the currently selected tree node
    protected Vector allowableAttrs = null;

    // attributes and values for the currently selected tree node
    protected NamedNodeMap currAttrs = null;

    // nodes associated with the tree
    protected NodeAdapter currNode;
    protected NodeAdapter editNode = null;
    protected TreePath currPath = null;

    // define edit types
    static public final short EDIT_NONE = 0;
    static public final short COPY = 1;
    static public final short CUT = 2;

    protected short editType = EDIT_NONE;

    // define import locals
    static public final short IMPORT_NONE = 0;
    static public final short SCENEGRAPH = 1;
    static public final short X3D_NODES = 2;
    static public final short GEOMETRY_REPOSITORY = 3;
    static public final short BEHAVIORS = 4;

    protected short importType = IMPORT_NONE;
    protected String baseDefName = "";
    protected DropTarget dropTarget = null;
    protected DragSource dragSource = null;
    protected DragSourceContext dragSourceContext = null;
    protected DragGestureRecognizer bListDragGesture = null;
    protected DragGestureRecognizer paletteDragGesture = null;
    protected DragGestureRecognizer crTreeDragGesture = null;
    protected DragGestureRecognizer treeDragGesture = null;

    // The following actions play in both menus and the toolbar.
    //    file menu actions
    protected SomeAction newAction;
    protected SomeAction openAction;
    protected SomeAction saveAction;
    protected SomeAction saveAsAction;
    protected SomeAction exitAction;
    //    edit menu actions
    protected SomeAction deleteAction;
    protected SomeAction cutAction;
    protected SomeAction copyAction;
    protected SomeAction pasteAction;
    protected SomeAction insertAction;

    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected JSplitPane content;
    protected JSplitPane contentR;
    protected JToolBar toolbar;
    protected JTabbedPane tabView1;
    protected JTree tree;
    protected JScrollPane treeView;
    protected JTable table;
    protected TableModel tableModel;
    protected JScrollPane tableView;
    protected JList palette;
    protected JScrollPane paletteView;
    protected JLabel status;
    protected JTextArea cdataText;
    protected JScrollPane cdataView;
    //    protected X3DViewer geoView;

    protected JPanel editView;
    protected JLabel editLabel;

    protected JTabbedPane tabView2;
    protected JTree crTree;                    // content repository tree
    protected JScrollPane crTreeView;
    protected JList bList;                     // behavior list
    protected JScrollPane bListView;

    protected JMenuBar menubar;
    protected JMenu fileMenu;
    protected JMenu editMenu;
    protected JMenu modeMenu;
    protected JMenu importMenu;
    protected JMenu exportMenu;
    protected JMenu toolMenu;
    protected JMenu helpMenu;

    // Controls the mode radiobuttons
    protected ButtonGroup modeGroup;
    protected short mode;

    protected JFileChooser fileChooser;

    // variable used for hack around JTree bug
    protected int rowHeight = 0;

    // java3d stuff
    protected GraphicsConfiguration gfxConfig;

    // keeps track of contentR's divider location
    int contentR_dloc;

    //XXX These are temporary. Eventually, behaviors
    //    will be loaded dynamically.
    static final String ROTATE = "Rotation";
    static final String XROTATE = "X Rotation";
    static final String YROTATE = "Y Rotation";
    static final String ZROTATE = "Z Rotation";
    static final String TRANSLATE = "Translation";
    static final String XTRANSLATE = "X Translation";
    static final String YTRANSLATE = "Y Translation";
    static final String ZTRANSLATE = "Z Translation";
    static final String SCALE = "Scale";
    static final String SWITCH = "Switch";
    static final String TRIGGER = "Trigger";
    static final String Behaviors[] = {ROTATE,
                       XROTATE,
                       YROTATE,
                       ZROTATE,
                       TRANSLATE,
                       XTRANSLATE,
                       YTRANSLATE,
                       ZTRANSLATE,
                       SCALE,
                       SWITCH,
                       TRIGGER};

    static final String LMSType = "LMSType";
    static final String IsTouched = "isTouched";
    static final String Enabled = "enabled";
    static final String Disabled = "disabled";
    static final String TouchTime = "touchTime";
    static final String DefaultFieldNames[] = {IsTouched,
                           Enabled,
                           Disabled,
                           TouchTime};
    static final String StringType = "String";
    static final String BoolType = "Boolean";
    static final String TimeType = "Time";
    static final String DefaultFieldTypes[] = {BoolType,
                           BoolType,
                           BoolType,
                           TimeType};
    static final String EventInHint = "eventIn";
    static final String EventOutHint = "eventOut";
    static final String DefaultFieldHints[] = {EventInHint,
                           EventOutHint,
                           EventOutHint,
                           EventOutHint};

    static final String TitlePrefix = "X3DAuthor: ";
    static final String X3D_SCHEMA = "X3dSchemaDraft.xsd";
    static final String X3D_TO_VRML97_XSL = "X3dToVrml97.xsl";
    static final String ContentRepository = "Repository.xml";
    static final String NewScene = "Repository/newScene.xml";
    static final short BUFSIZE = 1024;

    static final short XAXIS = 0;
    static final short YAXIS = 1;
    static final short ZAXIS = 2;

    public X3DAuthor() {
    super();

    saveFile = null;
    newFile = new File(NewScene);
    document = null;

    init();
    }

    public X3DAuthor(String fs) {
    super();

    saveFile = new File(fs);
    newFile = new File(NewScene);
    if (!saveFile.exists()) {
        // prompt user to create new file or exit
        if (JOptionPane.showConfirmDialog(this,
                          "Create " + fs + "?",
                          "Create File?",
                          JOptionPane.OK_CANCEL_OPTION) == 0)
        createDocumentFile(saveFile);
        else
        System.exit(1);
    }

    if ((document = XmlUtil.getDocument(saveFile)) == null) {
            System.err.println("Failed to get document for " + saveFile);
            System.exit(1);
    }

    this.setTitle(TitlePrefix + fs);
    init();
    }

    protected void init() {
    mode = NodeAdapter.BEGINNER_MODE;
    NodeAdapter.mode = mode;
    String dir;

    documentRoot = new NodeAdapter(document, NodeAdapter.SCENEGRAPH_TYPE);

    // Specify where to find the dtd for files created with this tool
    if ((dir = System.getProperty("user.dir")) == null)
        XmlUtil.Header2 = "<!DOCTYPE X3D PUBLIC \"http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd\"\n                     \"file:///c:/x3d-compact.dtd\">\n";
    else {
        dir = dir.replace('\\', '/');
        XmlUtil.Header2 = "<!DOCTYPE X3D PUBLIC \"http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd\"\n                     \"file:///" + dir + "/x3d-compact.dtd\">\n";
    }

    if ((exportFilePath = System.getProperty("user.home")) == null &&
        (exportFilePath = System.getProperty("user.dir")) == null)
        exportFilePath = "c:/_new_result.wrl";
    else {
        exportFilePath = exportFilePath.replace('\\', '/');
        exportFilePath += "/_new_result.wrl";
    }

    // utility file handles
    exportFile = new File(exportFilePath);

    File file = new File(ContentRepository);;
    if ((crDocument = XmlUtil.getDocument(file)) == null) {
            System.err.println("Failed to get document for " + file);
            System.exit(1);
    }
    crDocumentRoot = new NodeAdapter(crDocument, NodeAdapter.CONTENT_REPOSITORY_TYPE);

    //XXX This gets set in the tree listener
    //currNode = new NodeAdapter(documentRoot.dnode.getFirstChild());

    // XXX Please fix/eliminate this dependency!
    // For the moment, the X3D schema is expected to
    // be in the current directory.
    if ((schema = XmlUtil.getDocument(new File(X3D_SCHEMA))) == null) {
            System.err.println("Failed to get document for " + X3D_SCHEMA);
            System.exit(1);
    }

    schemaHash = XmlUtil.getXSDElemAttr(schema,
                        schema.getDocumentElement());

    //XXX find a better way to add in things like CDATA which are not elements
    // nodeTypes is being used to populate the list
    // of allowable X3D node types
    int i = 0;
    nodeTypes = new String[schemaHash.size() + 1];
    // preload non-element nodes
    nodeTypes[i++] = "CDATA";
    Enumeration keys = schemaHash.keys();
    for (; i < nodeTypes.length; ++i) {
        nodeTypes[i] = (String)(keys.nextElement());
        //Vector attrs = (Vector)(schemaHash.get(key));
    }
    Arrays.sort(nodeTypes);

    buildGui();
    tree.expandRow(0);
    tree.setSelectionRow(0);
    currPath = tree.getSelectionPath();

    // hack to hide problems using j3d with swing
    if (builtInViewer)
        tabView1.setSelectedIndex(1);

    tabView1.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            //System.out.println(e.toString());

            /*
            if (tabView1.getSelectedComponent() == geoView) {
            // blindly update the geometry
            if (builtInViewer)
                geoView.setJava3D(document, saveFile.getName());
            }
            */
        }
        }
    );

    initDragAndDrop();

        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width - 200, screenSize.height - 200);
        setLocation(0, 0);
        setVisible(true);
    contentR.setDividerLocation(0.8);
    content.setDividerLocation(0.8);
    }

    protected void buildGui() {
    // set look and feel
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        System.out.println("buildGUI: - set look and feel failed\n" +
                   e.toString());
    }

    // changes the global static default so all new popup menus are heavy
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);


    // pack components using GridBagLayout
    gridbag = new GridBagLayout();
    constraints = new GridBagConstraints();

    contentPane = getContentPane();
        addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
        }
    );

    // left side - tabView1 (contains treeView and geoView)
    buildTreeView();

    //  geoView = new X3DViewer();

    /*****
    String ts = "file:///c:/Xj3d/x3d/examples/browser/HelloWorld2.wrl";
    try {
        geoView.load(new InputSource(new URL(ts)));
    } catch(MalformedURLException mue) {
        System.out.println("Invalid URL: " + ts);
        System.exit(1);
    }
    */

    /*
    if (builtInViewer)
        geoView.setJava3D(document, saveFile.getName());
    */

    // create tabbed pane for tree and geometry
    tabView1 = new JTabbedPane();
    tabView1.addTab("Tree", treeView);
    /*
    if (builtInViewer)
        tabView1.addTab("Geometry", geoView);
    */

    // Node types
    palette = new JList(nodeTypes);
    palette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    palette.setSelectedIndex(0);
    paletteView = new JScrollPane(palette);

    // content repository
    buildCRTreeView();

    // behavior list
    bList = new JList(Behaviors);
    bList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    bList.setSelectedIndex(0);

    // create tabbed pane for the node palette,
    // the content repository and the behavior list
    tabView2 = new JTabbedPane();
    tabView2.addTab("X3D Nodes", paletteView);
    tabView2.addTab("Content", crTreeView);
    tabView2.addTab("Behaviors", bList);

    buildTableView();
    buildCDataView();

    editView = new JPanel(gridbag);
    editLabel = new JLabel("", SwingConstants.CENTER);
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 1;
    constraints.weighty = 0;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    editView.add(editLabel, constraints);

    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    editView.add(tableView, constraints);


    contentR = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                  tabView2, editView);

    contentR.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                       new PropertyChangeListener() {
                           public void propertyChange(PropertyChangeEvent e) {
                           contentR_dloc = contentR.getDividerLocation();
                           }
                       });

    // When resizing, the top/left component
    // gets all the extra space.
    contentR.setResizeWeight(1);

    content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 tabView1, contentR);

    // When resizing, the top/left component
    // gets all the extra space.
    content.setResizeWeight(1);

    // create Actions
    createActions();

    // create toolbar
    buildToolBar();

    // create menus
    buildMenuBar();

    // create status bar
    status = new JLabel("Status of X3DAuthor");

    contentPane.setLayout(gridbag);
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 1;
    constraints.weighty = 0;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    contentPane.add(toolbar, constraints);

    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    contentPane.add(content, constraints);

    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    contentPane.add(status, constraints);

    setJMenuBar(menubar);

    // create file chooser dialog initialized with user's current directory
    fileChooser = new JFileChooser(System.getProperty("user.dir"));
    }

    protected void initDragAndDrop() {
    dragSource = DragSource.getDefaultDragSource() ;

    // create drag gesture recognizers for each drag source
    bListDragGesture =
        dragSource.createDefaultDragGestureRecognizer(bList,
                              DnDConstants.ACTION_COPY,
                              this);
    paletteDragGesture =
        dragSource.createDefaultDragGestureRecognizer(palette,
                              DnDConstants.ACTION_COPY,
                              this);
    crTreeDragGesture =
        dragSource.createDefaultDragGestureRecognizer(crTree,
                              DnDConstants.ACTION_COPY,
                              this);
    treeDragGesture =
        dragSource.createDefaultDragGestureRecognizer(tree,
                              DnDConstants.ACTION_COPY,
                              this);

    // tree is the only drop target
    dropTarget = new DropTarget(tree, this);

        bList.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            importType = BEHAVIORS;
        }
    });

        palette.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            importType = X3D_NODES;
        }
    });

        crTree.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            importType = GEOMETRY_REPOSITORY;
        }
    });

        tree.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            importType = SCENEGRAPH;
        }
    });
    }

    // These play in both menus and the toolbar.
    //
    protected void createActions() {
    // File menu actions
    newAction = new SomeAction("New", new ImageIcon("images/new.gif"));
    openAction = new SomeAction("Open", new ImageIcon("images/open.gif"));
    saveAction = new SomeAction("Save", new ImageIcon("images/save.gif"));
    saveAsAction = new SomeAction("Save As", new ImageIcon("images/saveas.gif"));
    exitAction = new SomeAction("Exit");

    // Edit menu actions
    deleteAction = new SomeAction("Delete", new ImageIcon("images/delete.gif"));
    cutAction = new SomeAction("Cut", new ImageIcon("images/cut.gif"));
    copyAction = new SomeAction("Copy", new ImageIcon("images/copy.gif"));
    pasteAction = new SomeAction("Paste", new ImageIcon("images/paste.gif"));
    insertAction = new SomeAction("Insert", new ImageIcon("images/insertafter.gif"));
    }

    protected void importVrml97() {
    JOptionPane.showMessageDialog(this, "Import VRML97 not implemented yet!");
    }

    protected void exportVrml97() {
    if (document == null)
        return;

    XmlUtil.export(document, exportFile, X3D_TO_VRML97_XSL);
    }

    protected void exportToBrowser() {
    if (document == null)
        return;

    Runtime runtime = Runtime.getRuntime();
    exportVrml97();

    try {
        if (true) {
        runtime.exec("c:/Progra~1/Internet Explorer/IEXPLORE.EXE " +
                 "file:///" + exportFilePath);
        } else {
        //XXX these paths should not be hard coded
        runtime.exec("C:/Progra~1/Netscape/Communicator/Program/netscape.exe " +
                 "file:///" + exportFilePath);
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "exportToBrowser: " + e.toString());
    }
    }

    protected void openDocumentFile(File file) {
    Document tmpDoc;

    if (!file.exists()) {
        JOptionPane.showMessageDialog(this, file.getName() +
                      " doesn't exist");
        return;
    }

    if ((tmpDoc = XmlUtil.getDocument(file)) == null) {
        JOptionPane.showMessageDialog(this, "Failed to open " +
                      file.getName());
        return;
    }

    document = tmpDoc;
    documentRoot = new NodeAdapter(document, NodeAdapter.SCENEGRAPH_TYPE);
    saveFile = file;

    // update the tree
    tree.updateUI();
    tree.expandRow(0);
    tree.setSelectionRow(0);
    currPath = tree.getSelectionPath();

    this.setTitle(TitlePrefix + file.getName());
    }

    protected void saveDocumentFile(File file) {
    try {
        // use home grown writer
        if (true) {
        FileWriter fileWriter = new FileWriter(file);
        XmlUtil.documentWriter(document, fileWriter, "", "  ");
        fileWriter.close();
        } else {
        XmlUtil.transformWriter(document, file);
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "saveDocumentFile: " + e.toString());
    }
    }

    protected void createDocumentFile(File file) {
    FileReader reader;
    FileWriter writer;
    StringBuffer sbuf = new StringBuffer();
    char buf[] = new char[BUFSIZE];
    int n;

    try {
        // create new file
        if (!file.createNewFile()) {
        JOptionPane.showMessageDialog(this, file.getName() +
                          " already exists!");
        return;
        }

        // copy NewScene to file
        reader = new FileReader(newFile);
        writer = new FileWriter(file);

        while ((n = reader.read(buf, 0, BUFSIZE)) != -1) {
        sbuf.append(buf, 0, n);
        }
        writer.write(sbuf.toString(), 0, sbuf.length());
        writer.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "createDocumentFile: " + e.toString());
        return;
    }
    }

    protected void newHandler() {
    fileChooser.setDialogTitle("New File");
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();

        createDocumentFile(file);
        openDocumentFile(file);
    }
    }

    protected void openHandler() {
    fileChooser.setDialogTitle("Open File");

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        openDocumentFile(fileChooser.getSelectedFile());
    }

    protected void saveHandler() {
    if (document == null)
        return;

    saveDocumentFile(saveFile);
    }

    protected void saveAsHandler() {
    if (document == null)
        return;

    fileChooser.setDialogTitle("Save File As ...");

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        saveDocumentFile(fileChooser.getSelectedFile());
    }

    protected void exitHandler() {
    System.exit(0);
    }

    protected void deleteHandler() {
    if (document == null)
        return;

    if (currPath == null) {
        JOptionPane.showMessageDialog(this, "deleteHandler: currPath is null");
        return;
    }

    // delete node from the document
    currNode.dnode.getParentNode().removeChild(currNode.dnode);

    //XXX there is probably a more efficient way to
    // update the tree
    tree.updateUI();

    // set the selection to the parent
    tree.setSelectionPath(currPath.getParentPath());
    //tree.setLeadSelectionPath(currPath);
    //treeModel.nodeChanged(currNode);
    //tree.addSelectionPath(currPath.getParentPath());
    //tree.setLeadSelectionPath(currPath.getParentPath());
    //tree.getSelectionModel().resetRowSelection();
    //tree.getSelectionModel().setSelectionPath(currPath.getParentPath());
    //tree.setAnchorSelectionPath(currPath.getParentPath());
    }

    protected void cutHandler() {
    editNode = currNode;
    editType = CUT;
    }

    protected void copyHandler() {
    editNode = currNode;
    editType = COPY;
    }

    protected void pasteHandler() {
    if (document == null)
        return;

    if (editNode != null && editNode != currNode) {
        switch (editType) {
        case COPY:
            Node node = editNode.dnode.cloneNode(true);
            currNode.dnode.appendChild(node);
            tree.updateUI();

            // reset the selection
            tree.setSelectionPath(currPath);

            break;
        case CUT:
            editNode.dnode.getParentNode().removeChild(editNode.dnode);
            currNode.dnode.appendChild(editNode.dnode);
            tree.updateUI();

            // reset the selection
            tree.setSelectionPath(currPath);

            editNode = null;
            editType = EDIT_NONE;
            break;
        }
    }
    }

    protected void insertHandler() {
    if (document == null)
        return;

    /*
     * XXX Need to add code to validate the insert operation.
     *     In other words, can the current node type support
     *     children of this type?
     */

    String name = (String)palette.getSelectedValue();
    importX3DNode(name);
    }

    protected void aboutHandler() {
    JOptionPane.showMessageDialog(this, "Help/About not implemented yet!");
    }

    protected void buildToolBar() {
    JButton b;
    Action action;

    // Used to override the PLAF defaults
    Insets insets = new Insets(2, 2, 2, 2);

    toolbar = new JToolBar("My Tools");
    toolbar.setFloatable(false);
    toolbar.setBorderPainted(false);

    b = toolbar.add(newAction);
    b.setActionCommand((String)newAction.getValue(Action.NAME));
    b.setToolTipText((String)newAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(openAction);
    b.setActionCommand((String)openAction.getValue(Action.NAME));
    b.setToolTipText((String)openAction.getValue(Action.NAME));
    b.setMargin(insets);

    toolbar.addSeparator();

    b = toolbar.add(saveAction);
    b.setActionCommand((String)saveAction.getValue(Action.NAME));
    b.setToolTipText((String)saveAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(saveAsAction);
    b.setActionCommand((String)saveAsAction.getValue(Action.NAME));
    b.setToolTipText((String)saveAsAction.getValue(Action.NAME));
    b.setMargin(insets);

    toolbar.addSeparator();

    b = toolbar.add(deleteAction);
    b.setActionCommand((String)deleteAction.getValue(Action.NAME));
    b.setToolTipText((String)deleteAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(cutAction);
    b.setActionCommand((String)cutAction.getValue(Action.NAME));
    b.setToolTipText((String)cutAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(copyAction);
    b.setActionCommand((String)copyAction.getValue(Action.NAME));
    b.setToolTipText((String)copyAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(pasteAction);
    b.setActionCommand((String)pasteAction.getValue(Action.NAME));
    b.setToolTipText((String)pasteAction.getValue(Action.NAME));
    b.setMargin(insets);

    b = toolbar.add(insertAction);
    b.setActionCommand((String)insertAction.getValue(Action.NAME));
    b.setToolTipText((String)insertAction.getValue(Action.NAME));
    b.setMargin(insets);

    toolbar.addSeparator();

    b = toolbar.add((action = new AbstractAction("View geometry in a browser",
            new ImageIcon("images/box.gif")) {
                public void actionPerformed(ActionEvent e) {
                exportToBrowser();
                }
            }
    ));
    b.setActionCommand((String)action.getValue(Action.NAME));
    b.setToolTipText((String)action.getValue(Action.NAME));
    b.setMargin(insets);
    }

    protected void buildMenuBar() {
    JMenuItem item;

    menubar = new JMenuBar();
    fileMenu = new JMenu("File");
    editMenu = new JMenu("Edit");
    modeMenu = new JMenu("Mode");
    importMenu = new JMenu("Import");
    exportMenu = new JMenu("Export");
    toolMenu = new JMenu("Tools");
    helpMenu = new JMenu("Help");

    // populate the file menu
    item = fileMenu.add(newAction);
    item.setIcon(null);
    item = fileMenu.add(openAction);
    item.setIcon(null);
    fileMenu.addSeparator();
    item = fileMenu.add(saveAction);
    item.setIcon(null);
    item = fileMenu.add(saveAsAction);
    item.setIcon(null);
    fileMenu.addSeparator();
    item = fileMenu.add(exitAction);
    item.setIcon(null);

    // populate the edit menu
    item = editMenu.add(deleteAction);
    item.setIcon(null);
    item = editMenu.add(cutAction);
    item.setIcon(null);
    item = editMenu.add(copyAction);
    item.setIcon(null);
    item = editMenu.add(pasteAction);
    item.setIcon(null);
    editMenu.addSeparator();
    item = editMenu.add(insertAction);
    item.setIcon(null);

    // populate the mode menu
    modeGroup = new ButtonGroup();
    // Names used so far for this mode:
    //   Beginner, Limited, Standard
    item = modeMenu.add(new JRadioButtonMenuItem("Standard", true));
    modeGroup.add(item);
    item.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            mode = NodeAdapter.BEGINNER_MODE;
            NodeAdapter.mode = mode;
            tree.updateUI();

            // reset the selection
            tree.setSelectionPath(currPath);
        }
        }
    );

    item = modeMenu.add(new JRadioButtonMenuItem("Expert", false));
    modeGroup.add(item);
    item.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            mode = NodeAdapter.EXPERT_MODE;
            NodeAdapter.mode = mode;
            tree.updateUI();

            // reset the selection
            tree.setSelectionPath(currPath);
        }
        }
    );

    // populate the import menu
    item = importMenu.add("VRML97");
    item.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            importVrml97();
        }
        }
    );

    // populate the export menu
    item = exportMenu.add("VRML97");
    item.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            exportVrml97();
        }
        }
    );

    // populate the help menu
    item = helpMenu.add("About");
    item.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            aboutHandler();
        }
        }
    );

    // populate the tool menu

    // add menus to the menu bar
    menubar.add(fileMenu);
    menubar.add(editMenu);
    menubar.add(modeMenu);
    menubar.add(importMenu);
    menubar.add(exportMenu);
    menubar.add(toolMenu);

    // will eventually call this (i.e. when it's implemented)
    //menubar.setHelpMenu(helpMenu);
    menubar.add(helpMenu);
    }

    protected void buildCRTreeView() {
    crTree = new JTree(new DefaultTreeModel(crDocumentRoot) {
        public Object getRoot() {
            return crDocumentRoot;
        }
        });
    crTree.setRootVisible(false);
    crTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    crTreeView = new JScrollPane(crTree);
    }

    protected void buildTreeView() {
    tree = new JTree(new DefaultTreeModel(documentRoot) {
        public Object getRoot() {
            return documentRoot;
        }
        });
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    treeView = new JScrollPane(tree);

    //tree.setEditable(true);
    tree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            Node attr;
            TreePath tp = e.getNewLeadSelectionPath();
            //TreePath tp = e.getPath();

            //System.out.println("tree listener1");

            // resets the selection (i.e. an ancestor collapsed its tree)
            if (tp == null) {
            //System.out.println("tp was null");
            return;

            /*
            tp = e.getPath().getParentPath();

            System.out.println("fixing tp");

            tree.setLeadSelectionPath(tp);

            // setSelectionPath generates another listener event
            // which gets handled before the method returns.
            // In other words, this causes valueChanged to be
            // called recursively. Yikes!
            //tree.setSelectionPath(tp);


            System.out.println("tp was changed to ---> " + tp);

            return;
            */
            }


            currPath = tp;

            //System.out.println("currPath - " + currPath);

            currNode = (NodeAdapter)(tp.getLastPathComponent());
            currAttrs = currNode.dnode.getAttributes();
            allowableAttrs = (Vector)(schemaHash.get(currNode.dnode.getNodeName()));

            if (allowableAttrs == null) {
            if (currNode.dnode.getNodeType() == Node.CDATA_SECTION_NODE) {
                if (!cdataFlag) {
                // swap in cdataView
                //contentR.setBottomComponent(cdataView);
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.weightx = 1;
                constraints.weighty = 1;
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.fill = GridBagConstraints.BOTH;
                editView.remove(tableView);
                editView.add(cdataView, constraints);

                contentR.setDividerLocation(contentR_dloc);

                cdataFlag = true;
                }

                // update CDATA text
                cdataText.setText(currNode.dnode.getNodeValue());
                editLabel.setText("CDATA");
            } else {
                editLabel.setText(currNode.dnode.getNodeName());
                tableRows = 0;

                //XXX there is probably a more efficient way to do this
                table.updateUI();
            }
            } else {
            if (cdataFlag) {
                // swap in tableView
                //contentR.setBottomComponent(tableView);
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.weightx = 1;
                constraints.weighty = 1;
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.fill = GridBagConstraints.BOTH;
                editView.remove(cdataView);
                editView.add(tableView, constraints);

                contentR.setDividerLocation(contentR_dloc);

                cdataFlag = false;
            }

            editLabel.setText(currNode.dnode.getNodeName());
            tableRows = allowableAttrs.size();

            //XXX there is probably a more efficient way to do this
            table.updateUI();
            }
        }
        });

    /*
    tree.addTreeExpansionListener(new TreeExpansionListener() {
        public void treeCollapsed(TreeExpansionEvent e) {
            System.out.println("collapse - " +e.getPath());
        }

        public void treeExpanded(TreeExpansionEvent e) {
        }
        });
    */
    }

    protected void buildTableView() {
    tableModel = new AbstractTableModel() {
        public int getRowCount() {
            return tableRows;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int col) {
            switch (col) {
            case 0:
            return "Attribute";
            case 1:
            return "Value";
            default:
            return null;
            }
        }

        public Object getValueAt(int row, int col) {
            Node attr;

            if (allowableAttrs == null)
            return "";

            if (allowableAttrs.size() <= row)
            return "";

            // return attribute name
            if (col == 0)
            return allowableAttrs.get(row);

            // return attribute value
            if ((attr = currAttrs.getNamedItem((String)(allowableAttrs.get(row)))) != null) {
            return attr.getNodeValue();
            } else
            return "";
        };

        public boolean isCellEditable(int row, int col) {
            if (col == 0)
            return false;

            return true;
        }

        public void setValueAt(Object val, int row, int col) {
            Node attr;

            if (col == 0)
            return;

            if ((attr = currAttrs.getNamedItem((String)(allowableAttrs.get(row)))) != null) {
            // if val is "", remove the attribute
            if (val.equals(""))
                currAttrs.removeNamedItem((String)(allowableAttrs.get(row)));
            else
                attr.setNodeValue((String)val);
            } else {
            // add an attribute to the current node
            attr = document.createAttribute((String)(allowableAttrs.get(row)));
            attr.setNodeValue((String)val);
            currAttrs.setNamedItem(attr);
            }

            // update the tree
            //
            // this should work, but doesn't
            //tree.repaint();
            //
            // This hacks around the problem/bug in JTree.
            // Thanks to John Zukowski's Definitive Guide to
            // Swing for Java 2, Chapter 16, pg 634
            if (rowHeight <= 0)
            tree.setRowHeight(--rowHeight);
        }
    };

    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableView = new JScrollPane(table);
    }

    protected void buildCDataView() {
    cdataText = new JTextArea();
    cdataView = new JScrollPane(cdataText);

    cdataText.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
        }

        public void insertUpdate(DocumentEvent e) {
            currNode.dnode.setNodeValue(cdataText.getText());
        }

        public void removeUpdate(DocumentEvent e) {
            currNode.dnode.setNodeValue(cdataText.getText());
        }
    });
    }

    public class SomeAction extends AbstractAction {
    public SomeAction(String text) {
        this(text, null);
    }

    public SomeAction(String text, Icon icon) {
        super(text, icon);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("New")) {
        newHandler();
        return;
        }

        if (e.getActionCommand().equals("Open")) {
        openHandler();
        return;
        }

        if (e.getActionCommand().equals("Save")) {
        saveHandler();
        return;
        }

        if (e.getActionCommand().equals("Save As")) {
        saveAsHandler();
        return;
        }

        if (e.getActionCommand().equals("Exit")) {
        exitHandler();
        return;
        }

        if (e.getActionCommand().equals("Delete")) {
        deleteHandler();
        return;
        }

        if (e.getActionCommand().equals("Cut")) {
        cutHandler();
        return;
        }

        if (e.getActionCommand().equals("Copy")) {
        copyHandler();
        return;
        }

        if (e.getActionCommand().equals("Paste")) {
        pasteHandler();
        return;
        }

        if (e.getActionCommand().equals("Insert")) {
        insertHandler();
        return;
        }

        System.out.println(e.getActionCommand());
    }
    }

    //XXX This could eventually pop up a menu of choices
    //    to either cut/paste or copy/paste. For now,
    //    simply perform the cut/paste.
    protected boolean importSceneGraph(String name) {
    /*
    System.out.println("Import from SCENEGRAPH");
    System.out.println("Applying " +
               name +
               " to " +
               node.dnode.getNodeName() + "\n");
    */

    // For the moment assume cut/paste.
    editType = CUT;
    pasteHandler();
    editType = EDIT_NONE;

    return true;
    }

    protected boolean importX3DNode(String name) {
    if (name.equals("CDATA")) {
        CDATASection cdata = document.createCDATASection("javascript:\n");
        currNode.dnode.appendChild(cdata);
    } else {
        Element elem = document.createElement(name);
        currNode.dnode.appendChild(elem);
    }

    tree.updateUI();

    // reset the selection
    tree.setSelectionPath(currPath);

    return true;
    }

    protected boolean importGeometry(String geo) {
    NamedNodeMap nodeMap = editNode.dnode.getAttributes();
    Node node;
    Document importDoc;

    if (nodeMap == null)
        return false;

    if ((node = nodeMap.getNamedItem("file")) == null)
        return false;

    if ((importDoc = XmlUtil.getDocument(new File(node.getNodeValue()))) == null)
        return false;

    // import the scene node from importDoc into the scenegraph document
    XmlUtil.importSceneNode(importDoc, document, currNode.dnode);

    tree.updateUI();

    // reset the selection
    tree.setSelectionPath(currPath);

    return true;
    }

    protected void importBehavior(String behavior) {
    //  System.out.println("Import from BEHAVIORS");

    if (currNode.dnode.getNodeName().equals("Transform")) {
        if (behavior.equals(ROTATE)) {
        rotateBehavior();
        return;
        }

        if (behavior.equals(XROTATE)) {
        AxisRotateBehavior(XAXIS);
        return;
        }

        if (behavior.equals(YROTATE)) {
        AxisRotateBehavior(YAXIS);
        return;
        }

        if (behavior.equals(ZROTATE)) {
        AxisRotateBehavior(ZAXIS);
        return;
        }

        if (behavior.equals(TRANSLATE)) {
        translateBehavior();
        return;
        }

        if (behavior.equals(XTRANSLATE)) {
        AxisTranslateBehavior(XAXIS);
        return;
        }

        if (behavior.equals(YTRANSLATE)) {
        AxisTranslateBehavior(YAXIS);
        return;
        }

        if (behavior.equals(ZTRANSLATE)) {
        AxisTranslateBehavior(ZAXIS);
        return;
        }

        if (behavior.equals(SCALE)) {
        scaleBehavior();
        return;
        }

        if (behavior.equals(SWITCH)) {
        switchBehavior();
        return;
        }

        if (behavior.equals(TRIGGER)) {
        triggerBehavior();
        return;
        }
    }

    // XXX triggers and switches can be dropped on any
    //     kind of group node. Need to add support for
    //     every kind of group node.
    if (currNode.dnode.getNodeName().equals("Group")) {
        if (behavior.equals(SWITCH)) {
        switchBehavior();
        return;
        }

        if (behavior.equals(TRIGGER)) {
        triggerBehavior();
        return;
        }
    }
    }

    // This behavior can apply only to a Transform node's rotation field.
    //
    protected void rotateBehavior() {
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    AnimationDialog dialog = new AnimationDialog(this,
                             "Rotation Editor",
                             true,
                             name,
                             "0 0.5 1.0",
                             "1 0 0 0, 1 0 0 3.14159, 1 0 0 6.28318");
    dialog.setSize(new Dimension(300, 250));
    dialog.setEnabled(true);
    dialog.setCycleInterval("3");
    dialog.show();

    //XXX get info from Trigger
    if (dialog.getStatus() != AnimationDialog.OK)
        return;

    animationBehavior(dialog.getName(),
              "OrientationInterpolator",
              "_Orientation",
              "rotation",
              dialog.getKeys(),
              dialog.getValues(),
              dialog.getCycleInterval(),
              "0 0 0",
              dialog.getEnabled(),
              dialog.getLoop());
    }

    // This behavior can apply only to a Transform node's rotation field.
    //
    protected void AxisRotateBehavior(short axis) {
    String title;
    String keyValues;
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    switch (axis) {
    case XAXIS:
        title = "X Rotation Editor";
        keyValues = "1 0 0 0, 1 0 0 3.14159, 1 0 0 6.28318";
        break;
    case YAXIS:
        title = "Y Rotation Editor";
        keyValues = "0 1 0 0, 0 1 0 3.14159, 0 1 0 6.28318";
        break;
    case ZAXIS:
        title = "Z Rotation Editor";
        keyValues = "0 0 1 0, 0 0 1 3.14159, 0 0 1 6.28318";
        break;
    default:
        System.out.println("AxisRotateBehavior: bad axis - " + Short.toString(axis));
        return;
    }

    RotationDialog dialog = new RotationDialog(this,
                           title,
                           true,
                           name,
                           "10");
    dialog.setSize(new Dimension(250, 150));
    dialog.show();

    if (dialog.getStatus() != RotationDialog.OK)
        return;

    // calculate cycleInterval from RPM
    float rpm = Float.parseFloat(dialog.getRPM());
    double ci = 60.0 / rpm;

    animationBehavior(dialog.getName(),
              "OrientationInterpolator",
              "_Orientation",
              "rotation",
              "0 0.5 1",
              keyValues,
              Double.toString(ci),
              "0 0 0",
              true,
              true);
    }

    // This behavior can apply only to a Transform node's translation field.
    //
    protected void translateBehavior() {
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    AnimationDialog dialog = new AnimationDialog(this,
                             "Translation Editor",
                             true,
                             name,
                             "0 1",
                             "0 0 0, 1 0 0");
    dialog.setSize(new Dimension(300, 250));
    dialog.setEnabled(true);
    dialog.setCycleInterval("3");
    dialog.show();

    //XXX get info from Trigger
    if (dialog.getStatus() != AnimationDialog.OK)
        return;

    animationBehavior(dialog.getName(),
              "PositionInterpolator",
              "_Position",
              "translation",
              dialog.getKeys(),
              dialog.getValues(),
              dialog.getCycleInterval(),
              "0 0 0",
              dialog.getEnabled(),
              dialog.getLoop());
    }

    // This behavior can apply only to a Transform node's translation field.
    //
    protected void AxisTranslateBehavior(short axis) {
    String title;
    String keyValues;
    String name;
    Node node;

    // set dialog title
    switch (axis) {
    case XAXIS:
        title = "X Translation Editor";
        break;
    case YAXIS:
        title = "Y Translation Editor";
        break;
    case ZAXIS:
        title = "Z Translation Editor";
        break;
    default:
        System.out.println("AxisTranslateBehavior: bad axis - " + Short.toString(axis));
        return;
    }

    // set name
    if ((node = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = node.getNodeValue();
    }

    TranslationDialog dialog = new TranslationDialog(this,
                             title,
                             true,
                             name,
                             "10");
    dialog.setSize(new Dimension(250, 150));
    dialog.show();

    if (dialog.getStatus() != TranslationDialog.OK)
        return;

    // I believe this should never happen
    if ((node = currAttrs.getNamedItem("translation")) == null)
        return;

    String translation = node.getNodeValue();
    StringTokenizer tokenizer = new StringTokenizer(translation);
    int count = tokenizer.countTokens();

    //XXX need to pop up a dialog to warn user of bad input
    if (count != 3)
        return;

    // get all tokens
    //XXX need to test whether or not the input is valid (i.e. numeric)
    String sx = tokenizer.nextToken();
    String sy = tokenizer.nextToken();
    String sz = tokenizer.nextToken();

    // rebuild the key values with distance added in
    //XXX need to test whether or not the input is valid (i.e. numeric)
    float distance = Float.parseFloat(dialog.getDistance());
    switch (axis) {
    case XAXIS:
        float fx = Float.parseFloat(sx);
        keyValues = translation + ", " +
        Float.toString(fx + distance) + " " + sy + " " + sz;
        break;
    case YAXIS:
        float fy = Float.parseFloat(sy);
        keyValues = translation + ", " +
        sx + " " + Float.toString(fy + distance) + " " + sz;
        break;
    case ZAXIS:
        float fz = Float.parseFloat(sz);
        keyValues = translation + ", " +
        sx + " " + sy + " " + Float.toString(fz + distance);
        break;
    default:
        System.out.println("AxisTranslateBehavior: bad axis - " + Short.toString(axis));
        return;
    }

    animationBehavior(dialog.getName(),
              "PositionInterpolator",
              "_Position",
              "translation",
              "0 1",
              keyValues,
              "3",
              "0 0 0",
              true,
              false);
    }

    // This behavior can apply only to a Transform node's scale field.
    //
    protected void scaleBehavior() {
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    AnimationDialog dialog = new AnimationDialog(this,
                             "Scale Editor",
                             true,
                             name,
                             "0 0.5 1",
                             "1 1 1, 2 2 2, 1 1 1");
    dialog.setSize(new Dimension(300, 250));
    dialog.setEnabled(true);
    dialog.setCycleInterval("3");
    dialog.show();

    //XXX get info from Trigger
    if (dialog.getStatus() != AnimationDialog.OK)
        return;

    animationBehavior(dialog.getName(),
              "PositionInterpolator",
              "_Scale",
              "scale",
              dialog.getKeys(),
              dialog.getValues(),
              dialog.getCycleInterval(),
              "0 0 0",
              dialog.getEnabled(),
              dialog.getLoop());
    }

    //XXX The behaviors are temporarily hard coded. Eventually, these will
    //    be read in dynamically.
    //
    protected void animationBehavior(String baseName,
                     String interpolator,
                     String ilabel,
                     String field,
                     String key,
                     String keyValue,
                     String cycleInterval,
                     String value,
                     boolean enabled,
                     boolean loop) {
    Element elem;
    Node attr;
    NamedNodeMap attrs;

    if (baseName == null)
        baseName = "";

    // create TimeSensor
    elem = document.createElement("TimeSensor");
    currNode.dnode.appendChild(elem);

    // add attributes to the TimeSensor node
    attrs = elem.getAttributes();
    if (enabled)
        createAttribute(attrs, "enabled", "true");
    else
        createAttribute(attrs, "enabled", "false");

    createAttribute(attrs, "DEF", baseName + "_TS");
    createAttribute(attrs, "cycleInterval", cycleInterval);
    if (loop)
        createAttribute(attrs, "loop", "true");
    else
        createAttribute(attrs, "loop", "false");

    // create interpolator
    elem = document.createElement(interpolator);
    currNode.dnode.appendChild(elem);

    // add attributes to the interpolator node
    attrs = elem.getAttributes();
    createAttribute(attrs, "DEF", baseName + ilabel);
    createAttribute(attrs, "key", key);
    createAttribute(attrs, "keyValue", keyValue);
    //  createAttribute(attrs, "value", value);

    // create ROUTE from TimeSensor to interpolator
    currNode.dnode.appendChild(createRoute(baseName + "_TS",
                           "fraction_changed",
                           baseName + ilabel,
                           "set_fraction"));

    // create ROUTE from Interpolator to translation field of transform node
    // add toNode attribute
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        createAttribute(currAttrs, "DEF", baseName + "_XFORM");
        def = currAttrs.getNamedItem("DEF");
    }
    currNode.dnode.appendChild(createRoute(baseName + ilabel,
                           "value_changed",
                           def.getNodeValue(),
                           field));

    tree.updateUI();

    // reset the selection
    tree.setSelectionPath(currPath);
    }

    // Causes the "dropped on" node's children to become switches. When the mouse
    // is clicked on any of the child nodes an action is started/stopped.
    //
    protected void switchBehavior() {
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    SwitchDialog dialog = new SwitchDialog(this, "Switch Editor", true, document);
    dialog.setSize(new Dimension(400, 400));
    dialog.setName(name + "_Switch");
    dialog.show();

    //XXX get info from Trigger
    if (dialog.getStatus() != SwitchDialog.OK)
        return;

    processSwitchTargets(dialog);

    tree.updateUI();

    // reset the selection
    tree.setSelectionPath(currPath);

    }

    protected void processSwitchTargets(SwitchDialog dialog) {
    Element elem;
    Node attr;
    NamedNodeMap attrs;
    String baseName = dialog.getName();
    Enumeration targets = dialog.targetListModel.elements();
    String tsName = baseName + "_TS";
    String scriptName = baseName + "_Script";

    if (baseName == null)
        baseName = "";

    // create TouchSensor
    elem = document.createElement("TouchSensor");
    currNode.dnode.appendChild(elem);

    // add attributes to the TouchSensor node
    attrs = elem.getAttributes();

    // add DEF attribute
    createAttribute(attrs, "DEF", tsName);

    // create action script
    String cdata = "javascript:\n\nfunction isTouched(value, timestamp) {\n\tif (value) {\n\t\tif (enabled)\n\t\t\t\tenabled = false;\n\telse\n\t\tenabled = true;\n\t}\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              DefaultFieldNames,
                              DefaultFieldTypes,
                              DefaultFieldHints));

    // Create Routes

    // route from TouchSensor to the script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));

    // routes from script to target to enable/disable
    while (targets.hasMoreElements()) {
        String target = (String)targets.nextElement();
        currNode.dnode.appendChild(createRoute(scriptName, "enabled", target, "enabled"));
    }
    }

    protected void triggerBehavior() {
    String name;
    Node def;
    if ((def = currAttrs.getNamedItem("DEF")) == null) {
        name = "";
    } else {
        name = def.getNodeValue();
    }

    /*
    Vector targetDefs = XmlUtil.getDefs(document.getDocumentElement(),
                          TriggerDialog.sensors);
    if (targetDefs == null) {
        targetDefs = new Vector();
    }
    TriggerDialog dialog = new TriggerDialog(this, "Trigger Editor", true, targetDefs);
    */

    TriggerDialog dialog = new TriggerDialog(this, "Trigger Editor", true, document);
    dialog.setSize(new Dimension(600, 400));
    dialog.setName(name + "_Trigger");
    dialog.show();

    //XXX get info from Trigger
    if (dialog.getStatus() != TriggerDialog.OK)
        return;

    processTriggerActions(dialog);

    tree.updateUI();

    // reset the selection
    tree.setSelectionPath(currPath);
    }

    // XXX This should probably live in TriggerDialog
    protected void processTriggerActions(TriggerDialog dialog) {
    Element elem;
    Node attr;
    NamedNodeMap attrs;
    String baseName = dialog.getName();
    String tsName = baseName + "_TS";
    Enumeration actions = dialog.actionTable.keys();

    // create TouchSensor
    elem = document.createElement("TouchSensor");
    currNode.dnode.appendChild(elem);

    // add attributes to the TouchSensor node
    attrs = elem.getAttributes();

    // add DEF attribute
    createAttribute(attrs, "DEF", tsName);

    // add enabled attribute
    if (dialog.getEnabled())
        createAttribute(attrs, "enabled", "true");
    else
        createAttribute(attrs, "enabled", "false");

    // add route to turn off touchSensor
    if (dialog.getOneTime()) {
        createOneTimeTrigger(tsName,
                 "stop" + baseName + "_Script");
    }

    while (actions.hasMoreElements()) {
        String action = (String)actions.nextElement();
        String scriptName = baseName + "_" + action.replace(' ', '_') + "_Script";
        Enumeration targets = ((Vector)dialog.actionTable.get(action)).elements();

        //      System.out.println("ProcessTriggerActions: action - " + action);

        if (action.equals(TriggerDialog.EnabledAction)) {
        createEnabledAction(tsName, scriptName, targets, true);
        continue;
        }

        if (action.equals(TriggerDialog.DisabledAction)) {
        createEnabledAction(tsName, scriptName, targets, false);
        continue;
        }

        if (action.equals(TriggerDialog.TouchTimeAction)) {
        createTouchTimeAction(tsName, scriptName, targets);
        continue;
        }

        /*
        if (action.equals(TriggerDialog.LMSInitializeAction)) {
        createLMSInitializeAction(tsName, scriptName, targets);
        continue;
        }

        if (action.equals(TriggerDialog.LMSContinueAction)) {
        createLMSContinueAction(tsName, scriptName, targets);
        continue;
        }

        if (action.equals(TriggerDialog.LMSFinishAction)) {
        createLMSFinishAction(tsName, scriptName, targets);
        continue;
        }
        */

        if (action.equals(TriggerDialog.LMSInitializeAction)) {
        createLMSAction(tsName, scriptName, "Initialize", targets);
        continue;
        }

        if (action.equals(TriggerDialog.LMSContinueAction)) {
        createLMSAction(tsName, scriptName, "Continue", targets);
        continue;
        }

        if (action.equals(TriggerDialog.LMSFinishAction)) {
        createLMSAction(tsName, scriptName, "Finish", targets);
        continue;
        }
    }
    }

    protected void createOneTimeTrigger(String tsName,
                    String scriptName) {
    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tenabled = true;\n\tdisabled = false;\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              DefaultFieldNames,
                              DefaultFieldTypes,
                              DefaultFieldHints));

    // add route from TouchSensor to Script
    currNode.dnode.appendChild(createRoute(tsName,
                           "isActive",
                           scriptName,
                           "isTouched"));

    // add route from Script to TouchSensor
    currNode.dnode.appendChild(createRoute(scriptName,
                           "disabled",
                           tsName,
                           "enabled"));
    }

    protected void createEnabledAction(String tsName,
                       String scriptName,
                       Enumeration targets,
                       boolean enabled) {
    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tenabled = true;\n\tdisabled = false;\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              DefaultFieldNames,
                              DefaultFieldTypes,
                              DefaultFieldHints));

    // add route from TouchSensor to this action script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));

    while (targets.hasMoreElements()) {
        String target = (String)targets.nextElement();

        // add route from this script node to the target node
        if (enabled)
        currNode.dnode.appendChild(createRoute(scriptName,
                               "enabled",
                               target,
                               "enabled"));
        else
        currNode.dnode.appendChild(createRoute(scriptName,
                               "disabled",
                               target,
                               "enabled"));
    }
    }

    protected void createTouchTimeAction(String tsName,
                     String scriptName,
                     Enumeration targets) {
    //  System.out.println("createTouchTimeAction: enter");

    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tenabled = true;\n\tdisabled = false;\n\ttouchTime = timeStamp;\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              DefaultFieldNames,
                              DefaultFieldTypes,
                              DefaultFieldHints));

    // add route from TouchSensor to this action script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));

    while (targets.hasMoreElements()) {
        String target = (String)targets.nextElement();

        //      System.out.println("CreateTouchTimeAction: target - " + target);

        // add route from this script node to the target node
        currNode.dnode.appendChild(createRoute(scriptName,
                           "touchTime",
                           target,
                           "startTime"));
    }
    }

    protected void createLMSAction(String tsName,
                   String scriptName,
                   String type,
                   Enumeration targets) {
    final String fieldNames[] = {IsTouched,
                     LMSType};
    final String fieldTypes[] = {BoolType,
                     StringType};
    final String fieldHints[] = {EventInHint,
                     EventOutHint};

    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tLMSType = '" +
                   type + "';\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              fieldNames,
                              fieldTypes,
                              fieldHints));

    // add route from TouchSensor to this action script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));
    }

    /*
    protected void createLMSInitializeAction(String tsName,
                         String scriptName,
                         Enumeration targets) {
    final String fieldNames[] = {IsTouched,
                     LMSType};
    final String fieldTypes[] = {BoolType,
                     StringType};
    final String fieldHints[] = {EventInHint,
                     EventOutHint};

    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tLMSType = 'Initialize';\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              fieldNames,
                              fieldTypes,
                              fieldHints));

    // add route from TouchSensor to this action script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));
    }

    protected void createLMSFinishAction(String tsName,
                     String scriptName,
                     Enumeration targets) {
    final String fieldNames[] = {IsTouched,
                     LMSType};
    final String fieldTypes[] = {BoolType,
                     StringType};
    final String fieldHints[] = {EventInHint,
                     EventOutHint};

    // build javascript string
    String cdata = "javascript:\n\nfunction isTouched(value, timeStamp) {\n\tLMSType = 'Finish';\n}";
    currNode.dnode.appendChild(createActionScript(scriptName,
                              cdata,
                              fieldNames,
                              fieldTypes,
                              fieldHints));

    // add route from TouchSensor to this action script
    currNode.dnode.appendChild(createRoute(tsName, "isActive", scriptName, "isTouched"));
    }
    */

    // XXX need to pass in the fields and their types
    protected Element createActionScript(String name,
                     String cdata,
                     String fields[],
                     String types[],
                     String hints[]) {
    Element script;
    NamedNodeMap attrs;

    // create Script node
    script = document.createElement("Script");

    // add attributes to the Script node
    attrs = script.getAttributes();

    // add DEF attribute
    createAttribute(attrs, "DEF", name);

    // create fields
    /*
    script.appendChild(createField("isTouched", "Boolean", "eventIn"));
    script.appendChild(createField("enabled", "Boolean", "eventOut"));
    script.appendChild(createField("disabled", "Boolean", "eventOut"));
    script.appendChild(createField("touchTime", "Time", "eventOut"));
    */
    if (fields == null ||
        types == null ||
        hints == null ||
        fields.length != types.length ||
        fields.length != hints.length)
        return script;

    for (int i = 0; i < fields.length; ++i)
        script.appendChild(createField(fields[i], types[i], hints[i]));

    // create CDATA
    script.appendChild(createCDATA(cdata));

    return script;
    }

    protected Element createField(String name,
                  String type,
                  String vrml97Hint) {
    Element field;
    NamedNodeMap attrs;

    // create field node
    field = document.createElement("field");

    // add attributes to the field node
    attrs = field.getAttributes();

    createAttribute(attrs, "name", name);
    createAttribute(attrs, "type", type);
    createAttribute(attrs, "vrml97Hint", vrml97Hint);

    return field;
    }

    protected Element createRoute(String fromNode,
                  String fromField,
                  String toNode,
                  String toField) {
    Element route;
    NamedNodeMap attrs;

    route = document.createElement("ROUTE");

    // add attributes to the ROUTE node
    attrs = route.getAttributes();

    createAttribute(attrs, "fromNode", fromNode);
    createAttribute(attrs, "fromField", fromField);
    createAttribute(attrs, "toNode", toNode);
    createAttribute(attrs, "toField", toField);

    return route;
    }

    protected void createAttribute(NamedNodeMap attrs,
                String name,
                String value) {
    Node attr;

    // create attribute and set its value
    attr = document.createAttribute(name);
    attr.setNodeValue(value);

    // associate attr with attrs
    attrs.setNamedItem(attr);
    }

    protected CDATASection createCDATA(String data) {
    return document.createCDATASection(data);
    }

    // These methods implement the DragGestureListener interface.
    //
    // This is called when a drag gesture has been recognized
    // as the name suggests.
    //
    public void dragGestureRecognized(DragGestureEvent e) {
    Transferable t;

    switch (importType) {
    case SCENEGRAPH:
        if ((editNode = (NodeAdapter)tree.getLastSelectedPathComponent()) == null)
        return;

        t = new StringTransferable(editNode.dnode.getNodeName());
        break;
    case X3D_NODES:
        t = new StringTransferable((String)palette.getSelectedValue());
        break;
    case GEOMETRY_REPOSITORY:
        Object obj;
        if ((editNode = (NodeAdapter)crTree.getLastSelectedPathComponent()) == null)
        return;

        t = new StringTransferable(editNode.dnode.getNodeName());
        break;
    case BEHAVIORS:
        t = new StringTransferable((String)bList.getSelectedValue());
        break;
    default:
        return;
    }

    dragSource.startDrag(e, DragSource.DefaultCopyNoDrop, t, this);
    }

    // These methods implement the DragSourceListener interface.
    public void dragDropEnd(DragSourceDropEvent e) {
    //  System.out.println("dragDropEnd");
    }
    public void dragEnter(DragSourceDragEvent e) {
    //  System.out.println("dragEnter source");
    dragSourceContext = e.getDragSourceContext();
    }
    public void dragExit(DragSourceEvent e) {
    //  System.out.println("dragExit source");
    if (dragSourceContext != null)
        dragSourceContext.setCursor(DragSource.DefaultCopyNoDrop);
    dragSourceContext = null;
    }

    public void dragOver(DragSourceDragEvent e) {
    //  System.out.println("dragOver source");
    }

    public void dropActionChanged(DragSourceDragEvent e) {
    //  System.out.println("dropActionChanged source");
    }

    // These methods implement the DropTargetListener interface.
    public void dragEnter(DropTargetDragEvent e) {
    //  System.out.println("dragEnter target");
    }

    public void dragExit(DropTargetEvent e) {
    //  System.out.println("dragExit target");
    }

    // drag over the scenegraph tree
    public void dragOver(DropTargetDragEvent e) {
    Point loc = e.getLocation();
        TreePath tp = tree.getPathForLocation(loc.x, loc.y);

    if (tp == null) {
        if (dragSourceContext != null)
        dragSourceContext.setCursor(DragSource.DefaultCopyNoDrop);

        return;
    }

    // setSelectionPath causes the tree selection listener
    // to be called immediately. It causes currNode,
    //  currPath, etc. to be set.
    tree.setSelectionPath(tp);
    //  NodeAdapter node = (NodeAdapter)tp.getLastPathComponent();

    switch (importType) {
    case SCENEGRAPH:
    case X3D_NODES:
    case GEOMETRY_REPOSITORY:
        e.acceptDrag(DnDConstants.ACTION_COPY);
        if (dragSourceContext != null)
        dragSourceContext.setCursor(DragSource.DefaultCopyDrop);
        break;
    case BEHAVIORS:
        if (currNode.dnode.getNodeName().equals("Transform") ||
        currNode.dnode.getNodeName().equals("Group")) {
        e.acceptDrag(DnDConstants.ACTION_COPY);
        if (dragSourceContext != null)
            dragSourceContext.setCursor(DragSource.DefaultCopyDrop);

        break;
        }
    default:
        e.rejectDrag();
        if (dragSourceContext != null)
        dragSourceContext.setCursor(DragSource.DefaultCopyNoDrop);
    }
    }

    // drop on the scenegraph tree
    public void drop(DropTargetDropEvent e) {
    //  System.out.println("target drop");
    Transferable t = e.getTransferable();
    Object data = null;
    Point loc = e.getLocation();
        TreePath tp = tree.getPathForLocation(loc.x, loc.y);
    NodeAdapter node;

    if (tp == null) {
        e.rejectDrop();
        return;
    }

    // setSelectionPath causes the tree selection listener
    // to be called immediately. It causes currNode,
    //  currPath, etc. to be set.
    tree.setSelectionPath(tp);
    //XXX Can this ever be null?
    /*
    if ((node = (NodeAdapter)tp.getLastPathComponent()) == null) {
        e.rejectDrop();
        return;
    }
    */

    try {
        data = t.getTransferData(DataFlavor.stringFlavor);
    } catch (IOException ioe) {
        e.rejectDrop();
        return;
    } catch (UnsupportedFlavorException ufe) {
        e.rejectDrop();
        return;
    }

    if (data == null) {
        e.rejectDrop();
        return;
    }

    switch (importType) {
    case SCENEGRAPH:
        importSceneGraph(data.toString());
        break;
    case X3D_NODES:
        importX3DNode(data.toString());
        break;
    case GEOMETRY_REPOSITORY:
        importGeometry(data.toString());
        break;
    case BEHAVIORS:
        importBehavior(data.toString());
        break;
    default:
        e.rejectDrop();
    }
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    //  System.out.println("dropActionChanged target");
    }

    public static void main(String argv[]) {
    X3DAuthor author;

    switch (argv.length) {
    case 0:
        author = new X3DAuthor();
        break;
    case 1:
        author = new X3DAuthor(argv[0]);
        break;
    default:
        System.err.println("Usage: java X3DAuthor filename");
        System.exit(1);
    }
    }
}
