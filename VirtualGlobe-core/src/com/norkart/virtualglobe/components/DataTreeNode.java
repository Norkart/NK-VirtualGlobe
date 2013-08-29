/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  DataTreeNode.java
 *
 * Created on 8. januar 2008, 10:47
 *
 */

package com.norkart.virtualglobe.components;

import java.awt.Component;
import java.awt.BorderLayout;

import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author runaas
 */
public class DataTreeNode extends DefaultMutableTreeNode {
    
    public enum Mode {
        NONE, CHECK, RADIO
    };
      
    protected String  title = null;
    protected Box    infoPanel = null;
    protected Box    editPanel = null;
    protected JPopupMenu popup = null;
    protected Mode    mode = Mode.NONE;
    protected boolean isEnabled  = true;
    protected boolean isSelected = true;
    
    protected DataTreeNodeListener listener = null;
    
    /** Creates a new instance of DataTreeNode */
    public DataTreeNode() {
        
    }
    
    public void setDataTreeNodeListener(DataTreeNodeListener listener) {
        this.listener = listener;
    }
    public DataTreeNodeListener getDataTreeNodeListener() {
        return listener;
    }
    
    public void select(boolean select) {
        if (select == isSelected || mode == Mode.NONE)
            return;
        
        if (mode == Mode.RADIO) {
            if (select) {
                Enumeration siblings = getParent().children();
                while (siblings.hasMoreElements()) {
                    Object o = siblings.nextElement();
                    if (!(o instanceof DataTreeNode) || o == this)
                        continue;
                    DataTreeNode sibling = (DataTreeNode)o;
                    if (sibling.mode == Mode.RADIO)
                        sibling.select(false);
                }
            }
        }
        isSelected = select;
        
        Enumeration children = children();
        while (children.hasMoreElements()) {
            Object o = children.nextElement();
            if (!(o instanceof DataTreeNode))
                continue;
            DataTreeNode ch = (DataTreeNode)o;
            ch.enable(select);
        }
        
        if (listener != null)
            listener.nodeSelected(this);
    }
    
    public boolean isSelected() {
        return isSelected && isEnabled;
    }
    
    protected void enable(boolean enable) {
        if (isEnabled == enable)
            return;
        
        if (isSelected || enable) {
            Enumeration children = children();
            while (children.hasMoreElements()) {
                Object o = children.nextElement();
                if (!(o instanceof DataTreeNode))
                    continue;
                DataTreeNode ch = (DataTreeNode)o;
                ch.enable(enable);
            }
        }
        
        isEnabled = enable;
        if (listener != null)
            listener.nodeSelected(this);
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
     public String getTitle( ) {
        return title;
    }
    
    
    public DataTreeNodeListener.AcceptType acceptAsChild(DataTreeNode child) {
        if (listener != null)
            return listener.acceptAsChild(this, child);
        return DataTreeNodeListener.AcceptType.REJECT;
    }
    
    public String toString() {
        return getClass().getName() + '@' + Integer.toHexString(hashCode());
    }
    
    public void clear() {
       if (infoPanel != null) {
            if (infoPanel.getParent() != null)
                infoPanel.getParent().remove(infoPanel);
            infoPanel.setVisible(false);
        }
       if (editPanel != null) {
            if (editPanel.getParent() != null)
                editPanel.getParent().remove(editPanel);
            editPanel.setVisible(false);
        }
        // Noe mere for å slette uiComp?
        infoPanel = null;
        editPanel = null;
    }
    public JComponent getInfoPanel() {
        if (infoPanel == null)
            infoPanel = Box.createVerticalBox(); // new JPanel(new BorderLayout());
        return infoPanel;
    }
    public JComponent getEditPanel() {
        if (editPanel == null)
            editPanel = Box.createVerticalBox(); // new JPanel(new BorderLayout());
        return editPanel;
    }
    
    public JPopupMenu getPopup() {
        if (popup == null)
            popup = new JPopupMenu();
        return popup;
    }
    
}
