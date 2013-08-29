/*
 * DataTreeNodeListener.java
 *
 * Created on 11. januar 2008, 10:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.components;

/**
 *
 * @author runaas
 */
public interface DataTreeNodeListener {
    public enum AcceptType {
        REJECT, ACCEPT_MOVE, ACCEPT_LINK
    }
    
    public void nodeSelected(DataTreeNode node);
    
    public void childrenChanged(DataTreeNode node);
    
    public AcceptType acceptAsChild(DataTreeNode parent, DataTreeNode child);
}
