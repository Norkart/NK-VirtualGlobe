/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.x3d.dom.swing;

// External imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.w3c.dom.Node;

// Local imports
// None

/**
 * An implementation of the TreeCellRenderer interface to provided a renderer
 * for DOM specific capabilities.
 * <p>
 *
 * This cell renderer is very simple - it just displays a label with the text
 * name of the node type and any relvant information about it. It knows nothing
 * about X3D. If you want an X3D specific tree cell renderer, use the
 * {@link org.web3d.x3d.dom.swing.DOMTreeCellRenderer}
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class DOMTreeCellEditor
    implements TreeCellEditor, KeyListener {

    /** Default insets used by all panels */
    private Insets emptyBorder;

    /** Color for selected text */
    private Color textSelectColor;

    /** Color for unselected text */
    private Color textUnselectColor;

    /** Color of selected background */
    private Color bgSelectColor;

    /** Color of unselected background */
    private Color bgUnselectColor;

    /** Color of the selected border */
    private Color borderColor;

    /** The editor we are using to edit values with */
    private JTextField editor;

    /** The listeners used for event notification */
    private ArrayList listeners;

    /**
     * Create an instance of the tree cell editor.
     */
    public DOMTreeCellEditor() {
        listeners = new ArrayList(5);
        editor = new JTextField(10);
        editor.addKeyListener(this);

        emptyBorder = new Insets(0, 0, 0, 0);
        textSelectColor = UIManager.getColor("Tree.selectionForeground");
        textUnselectColor = UIManager.getColor("Tree.textForeground");
        bgSelectColor = UIManager.getColor("Tree.selectionBackground");
        bgUnselectColor = UIManager.getColor("Tree.textBackground");
        borderColor = UIManager.getColor("Tree.selectionBorderColor");
    }

    //------------------------------------------------------------
    // Methods for TreeCellEditor
    //------------------------------------------------------------

    /**
     * Request the renderer that suits the given value type and for the
     * given tree.
     *
     * @param tree The source tree this node comes from
     * @param value The DOMTreeNode to be rendered
     * @param selected True if the node is selected
     * @param expanded True if expanded
     * @param leaf True if this is a leaf node
     * @param row The row this node is on
     */
    public Component getTreeCellEditorComponent(JTree tree,
                                                Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row) {

        JComponent ret_val = null;
        Node node = ((DOMTreeNode)value).getNode();

        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                editor.setText(node.getNodeValue());
                editor.selectAll();
                JLabel label = new JLabel(node.getNodeName() + " ");
                JPanel panel = new JPanel();
                panel.add(label);
                panel.add(editor);

                label.setBorder(new EmptyBorder(emptyBorder));

                if(selected) {
                    label.setForeground(textSelectColor);
                    label.setBackground(bgSelectColor);
                    if(borderColor != null)
                        panel.setBorder(new LineBorder(borderColor));
                    else
                        panel.setBorder(new EmptyBorder(emptyBorder));
                } else {
                    label.setForeground(textUnselectColor);
                    label.setBackground(bgUnselectColor);
                    panel.setBorder(new EmptyBorder(emptyBorder));
                }

                ret_val = panel;
                break;

            case Node.TEXT_NODE:
            case Node.COMMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                editor.setText(node.getNodeValue());
                editor.selectAll();
                ret_val = editor;
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.ELEMENT_NODE:
                // do nothing....
        }

        return ret_val;
    }

    //------------------------------------------------------------
    // Methods for CellEditor
    //------------------------------------------------------------

    /**
     * Request the cell editor to cancel the current editing action. This is
     * ignored by this implementation.
     */
    public void cancelCellEditing() {
    }

    /**
     * Get the value of the last edited cell component. Returns a string
     * representation of the value.
     *
     * @return A string representing the value
     */
    public Object getCellEditorValue() {
        return editor.getText();
    }

    /**
     * Check to see if a cell is editable. We have to go through some hoops
     * here because we don't want all the cells to be editable. In particular,
     * only those that match the ones in the getComponent method above.
     *
     * @param evt The mouse event that caused this method to be called
     */
    public boolean isCellEditable(EventObject evt) {
        MouseEvent me = (MouseEvent)evt;
        if(me.getClickCount() < 2)
            return false;

        JTree src = (JTree)evt.getSource();

        TreePath path = src.getClosestPathForLocation(me.getX(), me.getY());
        DOMTreeNode tree_node = (DOMTreeNode)path.getLastPathComponent();
        Node node = tree_node.getNode();

        boolean ret_val = false;

        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE:
            case Node.COMMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                ret_val = true;
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.ELEMENT_NODE:
            default:
                ret_val = false;
        }

        return ret_val;
    }

    /**
     * Query to check if the cell should be selected when it is going to be
     * edited. All cells can be selected
     *
     * @param evt The mouse event selecting the cell
     * @return true;
     */
    public boolean shouldSelectCell(EventObject evt) {
        return true;
    }

    /**
     * Instruction to stop editing this cell. Ignored by this implementation.
     *
     * @return true
     */
    public boolean stopCellEditing() {
        return true;
    }

    /**
     * Add a cell editor listener. A listener instance can only be registered
     * once.
     *
     * @param l The listener to add.
     */
    public void addCellEditorListener(CellEditorListener l) {
        if((l != null) && !listeners.contains(l))
            listeners.add(l);
    }

    /**
     * Remove a cell editor listener. If the listener is not registered this
     * does nothing.
     *
     * @param l The listener to remove
     */
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
    }

    //------------------------------------------------------------
    // Methods for KeyListener
    //------------------------------------------------------------

    /**
     * Process a key event on the textfield. For this implementation we are
     * looking for either VK_ENTER or VK_ESCAPE to finish or cancel the
     * editing respectively.
     *
     * @param evt The event to be processed.
     */
    public void keyTyped(KeyEvent evt) {
    }

    /**
     * Process a key pressed event.
     */
    public void keyPressed(KeyEvent evt) {
    }

    /**
     * Process a key released event.
     */
    public void keyReleased(KeyEvent evt) {
        int code = evt.getKeyCode();

        if(code == KeyEvent.VK_ENTER) {
            fireFinishEditEvent();
        } else if(code == KeyEvent.VK_ESCAPE) {
            fireCancelEditEvent();
        }
    }

    //------------------------------------------------------------
    // Miscellaneous local methods
    //------------------------------------------------------------

    /**
     * Fire off a cell edit cancel event. Usually because the escape key has
     * been pressed.
     */
    private void fireCancelEditEvent() {
        Iterator itr = listeners.iterator();
        CellEditorListener l;
        ChangeEvent evt = new ChangeEvent(editor);

        while(itr.hasNext()) {
            l = (CellEditorListener)itr.next();
            try {
                l.editingCanceled(evt);
            } catch(Exception e) {
                System.err.println("Error sending cancel event: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire off a cell edit completed event. Usually because the user has
     * pressed the enter key.
     */
    private void fireFinishEditEvent() {
        Iterator itr = listeners.iterator();
        CellEditorListener l;
        ChangeEvent evt = new ChangeEvent(editor);

        while(itr.hasNext()) {
            l = (CellEditorListener)itr.next();
            try {
                l.editingStopped(evt);
            } catch(Exception e) {
                System.err.println("Error sending cancel event: " + e);
                e.printStackTrace();
            }
        }
    }
}
