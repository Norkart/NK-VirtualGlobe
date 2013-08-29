/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  DataTree.java
 *
 * Created on 7. januar 2008, 14:16
 *
 */

package com.norkart.virtualglobe.gui;


import com.norkart.virtualglobe.components.DataTreeNode;
import com.norkart.virtualglobe.components.DataTreeNodeListener;
import com.norkart.virtualglobe.components.WorldComponent;
import com.norkart.virtualglobe.components.Universe;

import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
// import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import javax.swing.TransferHandler;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;

import java.util.Enumeration;

import java.io.IOException;

/**
 *
 * @author runaas
 */
public class DataTree extends JTree {
    
    class DataTreeCellRenderer extends JPanel implements TreeCellRenderer {
        TreeCellRenderer std_renderer;
        JCheckBox    check_box    = new JCheckBox();
        JRadioButton radio_button = new JRadioButton();
        
        /** Creates a new instance of TreeNodeRenderer */
        public DataTreeCellRenderer() {
            this(new DefaultTreeCellRenderer());
        }
        
        public DataTreeCellRenderer(TreeCellRenderer std_renderer) {
            this.std_renderer = std_renderer;
            
            setLayout(new BorderLayout());
            setOpaque(false);
            check_box.setOpaque(false);
            radio_button.setOpaque(false);
        }
        
        public Component getTreeCellRendererComponent(JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            Component std_cmp = std_renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (!(value instanceof DataTreeNode))
                return std_cmp;
            
            DataTreeNode n = (DataTreeNode)value;
            
            // Set correct box
            removeAll();
            
            // Set correct grayness.
            std_cmp.setEnabled(n.isEnabled());
            add(std_cmp,   BorderLayout.CENTER);
            switch (n.getMode()) {
                case CHECK:
                    check_box.setSelected(n.isSelected());
                    check_box.setEnabled(n.isEnabled());
                    add(check_box, BorderLayout.WEST);
                    break;
                case RADIO:
                    radio_button.setSelected(n.isSelected());
                    radio_button.setEnabled(n.isEnabled());
                    add(radio_button, BorderLayout.WEST);
                    break;
            }
            
            
            return this;
        }
    }
    
    class DataNodeMouseListener extends MouseAdapter {
        int hotspot = new JCheckBox().getPreferredSize().width;
        
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = getRowForLocation(x, y);
            TreePath  path = getPathForRow(row);
            //TreePath  path = tree.getSelectionPath();
            if (path == null)
                return;
            
            if(x > getPathBounds(path).x+hotspot)
                return;
            
            DataTreeNode node = (DataTreeNode)path.getLastPathComponent();
            if (!node.isEnabled())
                return;
            boolean isSelected = ! node.isSelected();
            switch (node.getMode()) {
                case CHECK:
                    node.select(isSelected);
                    break;
                case RADIO:
                    if (isSelected)
                        node.select(true);
                    break;
                case NONE:
                    return;
            }
            
                /*
                if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
                    if ( isSelected) {
                        expandPath(path);
                    } else {
                        collapsePath(path);
                    }
                }
                 */
            ((DefaultTreeModel) getModel()).nodeChanged(node);
            // I need revalidate if node is root.  but why?
            if (row == 0) {
                revalidate();
                repaint();
            }
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int row = getRowForLocation(e.getX(), e.getY());
                TreePath  path = getPathForRow(row);
                //TreePath  path = tree.getSelectionPath();
                if (path == null)
                    return;
                
                DataTreeNode node = (DataTreeNode)path.getLastPathComponent();
                JPopupMenu popup = node.getPopup();
                popup.show(DataTree.this, e.getX(), e.getY());
            }
        }
    }
    static
    {
        try { DATA_TREE_NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType); } catch (Exception ex) { ex.printStackTrace(); }
    }
    private static DataFlavor DATA_TREE_NODE_FLAVOR; // = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
    private static final DataFlavor flavors[] = {DATA_TREE_NODE_FLAVOR};
    
    private class DataTreeTransferable implements Transferable {
        DataTreeNode node;
        
        DataTreeTransferable(DataTreeNode node) {
            this.node = node;
        }
        
        public Object getTransferData(DataFlavor flavor) {
            if (isDataFlavorSupported(flavor)) {
                return node;
            }
            return null;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DATA_TREE_NODE_FLAVOR);
        }
    }
    
    private class DataTreeTransferHandler extends TransferHandler {
        
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }
        
        
        public void exportAsDrag( JComponent comp , InputEvent evt , int action ) {
            boolean abort = true;
            
            try{
                if( !( evt instanceof MouseEvent ) )
                    return;
                MouseEvent mouse = (MouseEvent) evt;
                
                if( !( comp instanceof JTree ) )
                    return;
                JTree tree = (JTree) comp;
                
                TreePath pathUnderMouse =
                        tree.getPathForLocation( mouse.getX() , mouse.getY() );
                /*
                if( pathUnderMouse == null || !canExportAsDrag( pathUnderMouse ) )
                    return;
                 */
                action &= getSourceActions( comp );
                abort = false;
            } finally {
                if( abort )
                    exportDone( comp , null , NONE );
                else {
                    super.exportAsDrag( comp , evt , action );
                }
            }
        }
        
        public Transferable createTransferable(JComponent comp) {
            if (comp instanceof DataTree) {
                DataTree tree = (DataTree)comp;
                Object o = tree.getSelectionPath().getLastPathComponent();
                if (o instanceof DataTreeNode)
                    return new DataTreeTransferable((DataTreeNode)o);
            }
            return null;
        }
        public boolean canImport(
                JComponent comp, DataFlavor flavor[]) {
            if (!(comp instanceof DataTree)) {
                return false;
            }
            for (int i=0, n=flavor.length; i<n; i++) {
                for (int j=0, m=flavors.length; j<m; j++) {
                    if (flavor[i].equals(flavors[j])) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public boolean importData(
                JComponent comp, Transferable t) {
            if (comp instanceof DataTree) {
                DataTree dt = (DataTree)comp;
                if (t.isDataFlavorSupported(flavors[0])) {
                    try {
                        DataTreeNode node = (DataTreeNode)t.getTransferData(flavors[0]);
                        TreePath sel_path = getSelectionPath();
                        for (Object o : sel_path.getPath())
                            if (node == o) return false;
                        
                        Object o = sel_path.getLastPathComponent();
                        if (!(o instanceof DataTreeNode))
                            return false;
                        
                        DataTreeNode parent_node = (DataTreeNode)o;
                        if (parent_node.acceptAsChild(node) != DataTreeNodeListener.AcceptType.ACCEPT_MOVE)
                            return false;
                        
                        DefaultTreeModel model = (DefaultTreeModel)getModel();
                        model.removeNodeFromParent(node);
                        model.insertNodeInto(node, parent_node, 0);
                        
                        return true;
                    } catch (UnsupportedFlavorException ignored) {
                    } catch (IOException ignored) {
                    }
                }
            }
            
            return false;
        }
    }
    
    private class DataTreeDropTargetListener extends DropTargetAdapter {
        public void	dragOver(DropTargetDragEvent dtde) {
            DataTreeNodeListener.AcceptType accept = DataTreeNodeListener.AcceptType.REJECT;
            try {
                Transferable trans = dtde.getTransferable();
                DataTreeNode drag_node = (DataTreeNode)trans.getTransferData(DATA_TREE_NODE_FLAVOR);
                TreePath drop_path = getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
                if (drop_path != null) {
                    DataTreeNode drop_node = (DataTreeNode)drop_path.getLastPathComponent();
                    if (drag_node != drop_node) {
                        DataTreeNodeListener.AcceptType child_accept = drop_node.acceptAsChild(drag_node);
                        if (child_accept != DataTreeNodeListener.AcceptType.REJECT)
                            accept = child_accept;
                        else if (drop_node.getParent() != null) {
                            accept = ((DataTreeNode)drop_node.getParent()).acceptAsChild(drag_node);
                        }
                    }
                }
                
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                switch (accept) {
                    case REJECT:
                        dtde.rejectDrag();
                        break;
                    case ACCEPT_MOVE:
                        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                        break;
                    case ACCEPT_LINK:
                        dtde.acceptDrag(DnDConstants.ACTION_LINK);
                        break;
                }
            }
        }
        
        public void	drop(DropTargetDropEvent dtde) {
            boolean ok = false;
            try {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                Transferable trans = dtde.getTransferable();
                DataTreeNode drag_node = (DataTreeNode)trans.getTransferData(DATA_TREE_NODE_FLAVOR);
                
                Point     point = dtde.getLocation();
                TreePath drop_path = getPathForLocation(point.x, point.y);
                if (drop_path == null)
                    return;
                
                for (Object o : drop_path.getPath()) {
                    if (drag_node == o)
                        return;
                }
                
                DefaultTreeModel model = (DefaultTreeModel)getModel();
                DataTreeNode drop_node = (DataTreeNode)drop_path.getLastPathComponent();
                Rectangle rect = getPathBounds(drop_path);
                DataTreeNodeListener.AcceptType child_accept = drop_node.acceptAsChild(drag_node);
                DataTreeNodeListener.AcceptType parent_accept = DataTreeNodeListener.AcceptType.REJECT;
                if (drop_node.getParent() != null)
                    parent_accept = ((DataTreeNode)drop_node.getParent()).acceptAsChild(drag_node);
                
                if (parent_accept != DataTreeNodeListener.AcceptType.REJECT && child_accept != DataTreeNodeListener.AcceptType.REJECT) {
                    if (point.x > rect.x + rect.width*3/4)
                        parent_accept = DataTreeNodeListener.AcceptType.REJECT;
                }
                
                if (drag_node == drop_node)
                    return;
                
                if (parent_accept != DataTreeNodeListener.AcceptType.REJECT) {
                    boolean insert_below = point.y > rect.y + rect.height/2;
                    DataTreeNode drop_parent_node = (DataTreeNode)drop_node.getParent();
                    DataTreeNode drag_parent_node = (DataTreeNode)drag_node.getParent();
                    if (drop_parent_node == drag_node)
                        return;
                    model.removeNodeFromParent(drag_node);
                    int ins_pos = drop_parent_node.getIndex(drop_node);
                    if (insert_below)
                        ++ins_pos;
                    model.insertNodeInto(drag_node, drop_parent_node, ins_pos);
                    if (drop_parent_node.getDataTreeNodeListener() != null)
                        drop_parent_node.getDataTreeNodeListener().childrenChanged(drop_parent_node);
                    if (drag_parent_node != drop_parent_node && drag_parent_node.getDataTreeNodeListener() != null)
                        drag_parent_node.getDataTreeNodeListener().childrenChanged(drag_parent_node);
                } else if (child_accept != DataTreeNodeListener.AcceptType.REJECT) {
                    // Insert into the drop node
                    
                    DataTreeNode drag_parent_node = (DataTreeNode)drag_node.getParent();
                    model.removeNodeFromParent(drag_node);
                    model.insertNodeInto(drag_node, drop_node, 0);
                    if (drop_node.getDataTreeNodeListener() != null)
                        drop_node.getDataTreeNodeListener().childrenChanged(drop_node);
                    if (drag_parent_node != drop_node && drag_parent_node.getDataTreeNodeListener() != null)
                        drag_parent_node.getDataTreeNodeListener().childrenChanged(drag_parent_node);
                } else
                    return;
                
                
                ok = true;
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                dtde.dropComplete(ok);
            }
        }
    }
    
    /*
    private class DataTreeTreeModelListener implements TreeModelListener {
        public void treeNodesInserted(TreeModelEvent e) {
            Object o = e.getTreePath().getLastPathComponent();
            if (o instanceof DataTreeNode) {
                DataTreeNode n = (DataTreeNode)o;
                if (n.listener != null)
                    n.listener.childrenChanged(n);
            }
        }
        public void treeNodesRemoved(TreeModelEvent e) {
            Object o = e.getTreePath().getLastPathComponent();
            if (o instanceof DataTreeNode) {
                DataTreeNode n = (DataTreeNode)o;
                if (n.listener != null)
                    n.listener.childrenChanged(n);
            }
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
        public void treeNodesChanged(TreeModelEvent e) {
        }
    }
     */
    /** Creates a new instance of DataTree */
    public DataTree(TreeModel model) {
        setModel(model);
        setRootVisible(false);
        setCellRenderer(new DataTreeCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addMouseListener(new DataNodeMouseListener());
        setDragEnabled(true);
        setTransferHandler(new DataTreeTransferHandler());
        setDropTarget(new DropTarget(this, new DataTreeDropTargetListener()));
        
        // getModel().addTreeModelListener(new DataTreeTreeModelListener());
    }
    
// If expand is true, expands all nodes in the tree.
// Otherwise, collapses all nodes in the tree.
    public void expandAll(boolean expand) {
        // Traverse tree from root
        Object root = getModel().getRoot();
        if (root != null)
            expandAll(new TreePath(root), expand);
    }
    
    public void expandAll(TreeNode root, boolean expand) {
        if (root != null)
            expandAll(new TreePath(((DataTreeNode)root).getPath()), expand);
    }
    
    private void expandAll(TreePath parent, boolean expand) {
        if (expand)
            expandPath(parent);
        
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
        
        // Expansion or collapse must be done bottom-up
        if (!expand)
            collapsePath(parent);
    }
    
    public String convertValueToText(Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        if (value instanceof DataTreeNode) {
            DataTreeNode node = (DataTreeNode)value;
            if (node.getTitle() != null)
                return node.getTitle();
            Object userObject = node.getUserObject();
            if (userObject instanceof WorldComponent)
                return ((WorldComponent)userObject).getTitle();
            if (userObject != null)
                return userObject.toString();
        }
        return value.toString();
    }
}
