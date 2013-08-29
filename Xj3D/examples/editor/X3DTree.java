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

// Standard imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Application specific imports
import org.web3d.x3d.dom.swing.DOMTreeModel;
import org.web3d.x3d.dom.swing.DOMTreeNode;

/**
 * A customised version of JTree that supports mouse events and popup menus.
 * <p>
 * This tree is designed solely for X3D DOM documents. To the normal tree it
 * adds popup menu handling. The menu allows you to create new attributes and
 * also to set/remove the DEF/USE name of the objects.
 */
class X3DTree extends JTree
    implements MouseListener, ActionListener
{
    /** The popup menu to show on screen */
    private JPopupMenu editMenu;

    /** The remove a node menu item */
    private JMenuItem removeNodeItem;

    /** The add attribute menu item */
    private JMenuItem addItem;

    /** The remove attribute menu item */
    private JMenuItem removeItem;

    /** The set DEF/USE menu item */
    private JMenuItem defItem;

    /** The attribute panel for attribute information */
    private AttributeDialog attribDialog;

    /** The element that was last selected for the popup */
    private Element selectedElement;

    /**
     * Create a new tree using the given DOMTreeModel.
     *
     * @param model The tree model to use
     */
    X3DTree(DOMTreeModel model)
    {
        super(model);

        // Create the popup menu to be shown on screen
        editMenu = new JPopupMenu();

        removeNodeItem = new JMenuItem("Remove This Node");
        removeNodeItem.addActionListener(this);

        addItem = new JMenuItem("Add Attribute");
        addItem.addActionListener(this);

        removeItem = new JMenuItem("Remove Attribute");
        removeItem.addActionListener(this);

        defItem = new JMenuItem("Set DEF/USE");
        defItem.addActionListener(this);

        editMenu.add(removeNodeItem);
        editMenu.addSeparator();
        editMenu.add(addItem);
        editMenu.add(removeItem);
        editMenu.addSeparator();
        editMenu.add(defItem);

        addMouseListener(this);
    }

    //------------------------------------------------------------
    // Methods for ActionListener
    //------------------------------------------------------------

    /**
     * Process an action event from one of the popup menu items. This usually
     * means showing a dialog box and doing something with the input
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();

        if(src == addItem)
        {
            if(attribDialog == null)
            {
                Component comp = this.getParent();
                while((!(comp instanceof JFrame)) && (comp != null))
                    comp = comp.getParent();

                attribDialog = new AttributeDialog((JFrame)comp);
            }

            attribDialog.show();

            if(!attribDialog.isCanceled())
            {
                String name = attribDialog.getAttributeName();
                String value = attribDialog.getAttributeValue();
                selectedElement.setAttribute(name, value);
            }
        }
        else if(src == removeItem)
        {
System.out.println("remove attribute request");
        }
        else if(src == defItem)
        {
System.out.println("DEF/USE request");
        }
        else if(src == removeNodeItem)
        {
            Node parent = selectedElement.getParentNode();
            parent.removeChild(selectedElement);
        }

        // Set back to null to enable GC if needed.
        selectedElement = null;
    }

    //------------------------------------------------------------
    // Methods for MouseListener
    //------------------------------------------------------------

    /**
     * Process a mouse click event. Not used in this implementation.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt)
    {
    }

    /**
     * Process a mouse entering event. Not used in this implementation.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
    }

    /**
     * Process a mouse exiting event. Not used in this implementation.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt)
    {
    }

    /**
     * Process a mouse press event. If this is a popup trigger on this
     * platform, show the mouse.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        maybeShowPopup(evt);
    }

    /**
     * Process a mouse released event. If this is a popup trigger on this
     * platform, show the mouse.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        maybeShowPopup(evt);
    }

    /**
     * Decide whether we should show a popup menu based on this mouse event.
     * We only show the menu when it is over the right type of node - an
     * Element.
     *
     * @param evt The event to decide whether we should use a menu or not
     */
    private void maybeShowPopup(MouseEvent evt)
    {
        if(!evt.isPopupTrigger())
            return;

        // Find out where we are on screen - what node we are over.
        int x = evt.getX();
        int y = evt.getY();

        TreePath path = getClosestPathForLocation(x, y);
        DOMTreeNode tree_node = (DOMTreeNode)path.getLastPathComponent();
        Node node = tree_node.getNode();

        switch(node.getNodeType())
        {
            case Node.ELEMENT_NODE:
                selectedElement = (Element)node;
                editMenu.show(evt.getComponent(), x, y);
                break;
            default:
                // do nothing for now.
        }
    }
}
