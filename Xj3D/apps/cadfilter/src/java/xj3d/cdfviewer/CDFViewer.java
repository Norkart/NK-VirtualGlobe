/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.cdfviewer;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.accessibility.*;

import org.web3d.x3d.sai.*;

/**
 * A simple example of how to use SAI to load a scene and modify a value.
 *
 * @author Alan Hudson
 * @version
 */
public class CDFViewer extends JFrame implements MouseListener {

    JTree tree;

    /**
     * Constructor for the demo.
     */
    public CDFViewer(String filename) {
        super("CDF Viewer");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container content_pane = getContentPane();

        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);

        // Create an SAI component
        System.setProperty("x3d.sai.factory.class", "org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl");
        X3DComponent x3d_comp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        JComponent x3d_panel = (JComponent)x3d_comp.getImplementation();
        content_pane.add(x3d_panel, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3d_comp.getBrowser();

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("CADLayers");

        tree = new JTree(top) {
            public Insets getInsets() {
            return new Insets(5,5,5,5);
            }
        };

        JScrollPane treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(350,600));
        content_pane.add(treePane, BorderLayout.WEST);

        setSize(900,600);
        show();


        long startTime = System.currentTimeMillis();
        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { filename });
        System.out.println("creation time: " + (System.currentTimeMillis() - startTime));

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);
        System.out.println("replace time: " + (System.currentTimeMillis() - startTime));

        X3DNode roots[] = mainScene.getRootNodes();

        findLayers("CADLayer", roots, top);

        int len = top.getChildCount();

        if (len > 0) {
            TreeNode[] path = null;
            path = ((DefaultMutableTreeNode)top.getChildAt(0)).getPath();
            tree.scrollPathToVisible(new TreePath(path));
        }


        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);

        tree.addMouseListener(this);
    }

    //-------------------------------------------------------------------------
    //Methods for MouseListener
    //-------------------------------------------------------------------------
    public void mousePressed(MouseEvent e) {
         int selRow = tree.getRowForLocation(e.getX(), e.getY());

         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
         if(selRow != -1) {
             DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
             if(e.getClickCount() > 0) {
                 TreeValue node = (TreeValue) selectedNode.getUserObject();
                 node.flipVisible();
//                 System.out.println("node sel: " + node);


                 // Get parent, flip this childs visibility
                 int len = selPath.getPathCount();

                 Object obj = selPath.getPathComponent(len-2);

//                 System.out.println("parent obj: " + obj);

                 if (obj instanceof DefaultMutableTreeNode) {
                     DefaultMutableTreeNode parent = (DefaultMutableTreeNode) obj;

//                     System.out.println("parent user: " + parent.getUserObject());
                     obj = parent.getUserObject();

                     if (obj instanceof TreeValue) {
                         TreeValue parentTree = (TreeValue) parent.getUserObject();

//                         System.out.println("parent sel: " + parentTree);
                         int idx = parent.getIndex(selectedNode);

                         X3DNode x3dNode = parentTree.node;
                         MFBool visibleField = (MFBool) x3dNode.getField("visible");
                         boolean currVal = visibleField.get1Value(idx);

//                         System.out.println("Current value: " + currVal);

                         visibleField.set1Value(idx, !currVal);
                     } else {
                        // What to do with top level?
                     }
                 } else {
                    System.out.println("Close tree?");
                 }
             }
         }
     }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    //-------------------------------------------------------------------------
    //Local Methods
    //-------------------------------------------------------------------------

    /**
     * Walk the SG looking for nodes.  Places the result in layerNodes.
     *
     * @param nodeName The node to look for.
     * @param nodes The nodes to look at.
     */
    private void findLayers(String nodeName, X3DNode[] nodes, DefaultMutableTreeNode tree) {
        int len = nodes.length;
        int types[];

        X3DFieldDefinition[] decls;
        int dlen;
        X3DNode node;
        DefaultMutableTreeNode treeNode = null;
        SFString name;

        for(int i=0; i < len; i++) {
            node = nodes[i];

            if (node.getNodeName().equals(nodeName)) {
                name = (SFString) node.getField("name");
                treeNode = new DefaultMutableTreeNode(new TreeValue(name.getValue(), node));
                tree.add(treeNode);

                MFNode childrenField = (MFNode) node.getField("children");

                MFBool visibleField = (MFBool) node.getField("visible");
                int size = childrenField.getSize();
                boolean[] currVals = new boolean[size];
                for(int j=0; j < size; j++) {
                    currVals[j] = true;
                }

                visibleField.setValue(size,currVals);

            }

            if (treeNode == null)
                treeNode = tree;

            decls = node.getFieldDefinitions();
            dlen = decls.length;
            int ftype;
            int atype;
            MFNode mfnode;
            SFNode sfnode;

            X3DNode[] snodes;

            for(int j=0; j < dlen; j++) {
                ftype = decls[j].getFieldType();
                atype = decls[j].getAccessType();

                if ((atype == X3DFieldTypes.INPUT_OUTPUT || atype == X3DFieldTypes.INITIALIZE_ONLY)
                    &&  ftype == X3DFieldTypes.MFNODE) {
                    mfnode = (MFNode) node.getField(decls[j].getName());
                    snodes = new X3DNode[mfnode.getSize()];

                    mfnode.getValue(snodes);

                    findLayers(nodeName, snodes, treeNode);
                } else if (ftype == X3DFieldTypes.SFNODE) {
                    //X3DNode
                }
            }
        }
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("usage: CDFViewer <filename>");
            return;
        }

        CDFViewer viewer = new CDFViewer(args[0]);
    }
}


class TreeValue {
    public String name;
    public X3DNode node;
    private boolean visible;

    public TreeValue(String name, X3DNode node) {
        this.name = name;
        this.node = node;
        visible = true;
    }

    public void flipVisible() {
        visible = !visible;
    }

    public String toString() {
        String ret_val;

        if (visible)
            ret_val = "+ " + name;
        else
            ret_val = "- " + name;

        return ret_val;
    }

}

  /*
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import java.util.*;
 import java.io.*;
 import java.applet.*;
 import java.net.*;

 public class TreeDemo extends DemoModule {

     public static void main(String[] args) {
    TreeDemo demo = new TreeDemo(null);
    demo.mainImpl();
     }

     public TreeDemo(SwingSet2 swingset) {

    getDemoPanel().add(createTree(), BorderLayout.CENTER);
     }

     public JScrollPane createTree() {
         DefaultMutableTreeNode catagory = null ;
    DefaultMutableTreeNode artist = null;
    DefaultMutableTreeNode record = null;

        while(line != null) {
        // System.out.println("reading in: ->" + line + "<-");
        char linetype = line.charAt(0);
        switch(linetype) {
           case 'C':
             catagory = new DefaultMutableTreeNode(line.substring(2));
             top.add(catagory);
             break;
           case 'A':
             if(catagory != null) {
                 catagory.add(artist = new DefaultMutableTreeNode(line.substring(2)));
             }
             break;
           case 'R':
             if(artist != null) {
                 artist.add(record = new DefaultMutableTreeNode(line.substring(2)));
             }
             break;
           case 'S':
             if(record != null) {
                 record.add(new DefaultMutableTreeNode(line.substring(2)));
             }
             break;
           default:
             break;
        }
        line = reader.readLine();
        }
    } catch (IOException e) {
    }

    return new JScrollPane(tree);
     }

 }
}
*/