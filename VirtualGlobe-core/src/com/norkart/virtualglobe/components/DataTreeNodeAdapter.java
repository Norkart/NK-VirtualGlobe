/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  DataTreeNodeAdapter.java
 *
 * Created on 17. januar 2008, 10:38
 *
 */

package com.norkart.virtualglobe.components;

/**
 *
 * @author runaas
 */
public class DataTreeNodeAdapter implements DataTreeNodeListener {
    
    public void nodeSelected(DataTreeNode node) {
        
    }
    
    public void childrenChanged(DataTreeNode node) {
        
    }
    
    public AcceptType acceptAsChild(DataTreeNode parent, DataTreeNode child) {
        return AcceptType.REJECT;
    }
    
}
