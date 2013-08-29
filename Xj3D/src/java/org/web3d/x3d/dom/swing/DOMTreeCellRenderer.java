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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.w3c.dom.Node;

// Local imports
import org.web3d.util.ShortHashMap;
import org.xj3d.ui.awt.widgets.IconLoader;

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
 * @version $Revision: 1.6 $
 */
public class DOMTreeCellRenderer extends JLabel
    implements TreeCellRenderer {

    /** Flag indicating this instance has been selected */
    private boolean selected;

    /** Flag indicating this instance has focus currently */
    private boolean focused;

    /** Flag indicating if icon should have a border around it */
    private boolean iconFocusBorder;

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

    /** The mapping of node types (shorts) to strings */
    private static final ShortHashMap nodeNameMap;

    /**
     * Static initialisation of the class. Sets up the node type to node name
     * string mapping that is used each time this class is instantiated.
     */
    static {
        nodeNameMap = new ShortHashMap();

        nodeNameMap.put(Node.ATTRIBUTE_NODE, "Attribute");
        nodeNameMap.put(Node.CDATA_SECTION_NODE, "CDATA Section");
        nodeNameMap.put(Node.COMMENT_NODE, "Comment");
        nodeNameMap.put(Node.DOCUMENT_FRAGMENT_NODE, "Document Fragment");
        nodeNameMap.put(Node.DOCUMENT_NODE, "Document");
        nodeNameMap.put(Node.DOCUMENT_TYPE_NODE, "Document Type");
        nodeNameMap.put(Node.ELEMENT_NODE, "Element");
        nodeNameMap.put(Node.ENTITY_NODE, "Entity");
        nodeNameMap.put(Node.ENTITY_REFERENCE_NODE, "Entity Reference");
        nodeNameMap.put(Node.NOTATION_NODE, "Notation");
        nodeNameMap.put(Node.PROCESSING_INSTRUCTION_NODE,
                        "Processing Instruction");
        nodeNameMap.put(Node.TEXT_NODE, "Text");
    }

    /**
     * Create a new instance of this renderer. Initialises a lot of
     * values.
     */
    public DOMTreeCellRenderer() {
        textSelectColor = UIManager.getColor("Tree.selectionForeground");
        textUnselectColor = UIManager.getColor("Tree.textForeground");
        bgSelectColor = UIManager.getColor("Tree.selectionBackground");
        bgUnselectColor = UIManager.getColor("Tree.textBackground");
        borderColor = UIManager.getColor("Tree.selectionBorderColor");

        Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
        iconFocusBorder = (value != null && ((Boolean)value).booleanValue());
    }

    /**
     * Request the renderer that suits the given value type and for the
     * given tree.
     *
     * @param tree The source tree this node comes from
     * @param value The DOMTreeNode to be rendered
     * @param selected True if the node is selected
     * @param expanded True if expanded
     * @param row The row this node is on
     * @param hasFocus True if the node currently has focus
     */
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {

        this.selected = selected;
        this.focused = hasFocus;

        Node node = ((DOMTreeNode)value).getNode();
        String name = (String)nodeNameMap.get(node.getNodeType());
        setIcon(null);

        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                setText(node.getNodeName() + "=" + node.getNodeValue());
                break;

            case Node.TEXT_NODE:
            case Node.COMMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                StringBuffer txt = new StringBuffer(name);
                txt.append(" \"");
                txt.append(node.getNodeValue());
                txt.append('\"');
                setText(txt.toString());
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
                // I'd really like to put the name of the document here.
                setText(name);
                break;

            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                setText(node.getNodeName());
                break;

            case Node.ELEMENT_NODE:
                String nn = node.getNodeName();
                setText(nn);

                // Check to see if we have an icon too.
                Icon icon = IconLoader.loadIcon(nn, null);
                if(icon != null)
                    setIcon(icon);
        }

        setComponentOrientation(tree.getComponentOrientation());

        return this;
    }

    /**
     * Override the base class to properly set the painting. Code mostly
     * stolen from DefaultTreeCellRenderer without all the extra crap.
     *
     * @param g The graphics context to paint with
     */
    public void paint(Graphics g) {
        int imageOffset = -1;
        int width = getWidth();
        int height = getHeight();

        if(focused) {
            if(iconFocusBorder) {
                imageOffset = 0;
            } else if(imageOffset == -1) {
                imageOffset = getLabelStart();
            }
        }

        if(selected) {
            g.setColor(bgSelectColor);
            if(getComponentOrientation().isLeftToRight()) {
                g.fillRect(imageOffset, 0, width - 1 - imageOffset, height);
            } else {
                g.fillRect(0, 0, width - 1 - imageOffset, height);
            }

            g.setColor(textSelectColor);
            setForeground(textSelectColor);
        } else {
            g.setColor(textSelectColor);
            setForeground(textSelectColor);
        }

        if(focused) {
            g.setColor(borderColor);
            if(getComponentOrientation().isLeftToRight()) {
                g.drawRect(imageOffset,
                           0,
                           width - 1 - imageOffset,
                           height - 1);
            } else {
                g.drawRect(0, 0, width - 1 - imageOffset, height - 1);
            }

        }
        super.paint(g);
    }

    /**
     * Convenience method to determine where the text should start if there is
     * an icon provided.
     *
     * @return number of pixels offset
     */
    private int getLabelStart() {
        Icon currentI = getIcon();
        int ret_val = 0;
        if(currentI != null && getText() != null) {
            ret_val = currentI.getIconWidth() +
                      Math.max(0, getIconTextGap() - 1);
        }

        return ret_val;
    }
}
