//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.util.ApplicationSettings;

import org.w3c.dom.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.tree.*;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class World  extends WorldComponent {
    protected Universe universe;
    private JLabel namelabel = new JLabel();
    
    public World(Universe universe) {
        super(universe.getUrl());
        this.universe = universe;
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        setTitle(settings.getResourceString("WORLD_TITLE"));
        namelabel.setText("URL: " + universe.getUrl().toString());
        
        node = new DataTreeNode();
        node.setUserObject(this);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getUniverse().getDataTreeModel().getRoot();
        getUniverse().getDataTreeModel().insertNodeInto(node, root, root.getChildCount());
        
        /*
        JPopupMenu popup = node.getPopup();
        
        JMenuItem new_viewpoint_mi = new JMenuItem(settings.getResourceString("NEW_VIEWPOINT_TITLE"));
        new_viewpoint_mi.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
                if (vs == null)
                    vs = new ViewpointSet(World.this);
           }
        });
        popup.add(new_viewpoint_mi);
        */
        node.getInfoPanel().add(namelabel);
    }
/*
  private void superClear() {
    super.clear();
  }
 */
    public void clear() {
        super.clear();
       /*
    new Thread() {
      public void run() {
        World.this.superClear();
      }
    }.start();
        */
    }
    
    public Universe getUniverse() {
        return universe;
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("world"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        WorldComponentFactory.getInstance().loadChildren(this, domElement);
        /*
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("globe-surface")) {
                GlobeSurface gs = new GlobeSurface(this);
                gs.load(chEle);
            } else if (chEle.getNodeName().equals("viewpoint-set")) {
                if (vs == null)
                    vs = new ViewpointSet(this);
                vs.load(chEle);
            } else if (chEle.getNodeName().equals("flypath-set")) {
                if (fs == null)
                    fs = new FlyPathSet(this);
                fs.load(chEle);
            }
        }
         */
    }
    
    public String getTagName() {
        return "world";
    }
}