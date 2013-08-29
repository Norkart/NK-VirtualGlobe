//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.components.DomLoadable;
import java.io.PrintStream;
import java.net.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TreeModelEvent;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.UUID;
import org.w3c.dom.*;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class WorldComponent implements DomLoadable {
    protected Vector<WorldComponent> children = new Vector();
    protected WorldComponent parent;
    private   String title = "";
    private   String title_attrib;
    private   String name = null;
    // protected JPanel uiComp = new JPanel(new BorderLayout());
    // private   JScrollPane scroll = new JScrollPane(uiComp);
    private   URL baseUrl;
    DataTreeNode node;
    
    
    
    public WorldComponent(URL baseUrl) {
        this.parent  = null;
        this.baseUrl = baseUrl;
    }
    
    public WorldComponent(WorldComponent parent) {
        this.parent = parent;
        parent.children.add(this);
        baseUrl = parent.baseUrl;
        
            /*
            int [] ch_ix_arr = new int [] {parent.getNumChildren()-1};
            Object [] ch_arr = new Object [] {this};
            Object[] path = parent.getPath();
            TreeModelEvent e = new TreeModelEvent(this, path, ch_ix_arr, ch_arr);
             */
        // parent.getUniverse().fireTreeNodesInserted(e);
        
    }
    
    public URL getBaseUrl() {
        return baseUrl;
    }
    
    public String getName() {
        return name;
    }
    
    public String getTitle() {
        return title;
    }
    
    abstract String getTagName();
    
    public void setTitle(String title) {
        this.title = title;
        getUniverse().getDataTreeModel().nodeChanged(node);
        /*
        if (parent != null) {
            int [] ch_ix_arr = new int [] {parent.getIndexOfChild(this)};
            Object [] ch_arr = new Object [] {this};
            Object[] path = parent.getPath();
            TreeModelEvent e = new TreeModelEvent(this, path, ch_ix_arr, ch_arr);
            parent.getUniverse().fireTreeNodesChanged(e);
        } else {
            TreeModelEvent e = new TreeModelEvent(this, new Object [] {this});
            getUniverse().fireTreeNodesChanged(e);
        }
         */
    }
    
    public Universe getUniverse() {
        return getParent().getUniverse();
    }
    
    public CacheManager getCacheManager() {
        return getUniverse().getCacheManager();
    }
    
    public WorldComponent getChild(int ix) {
        return children.elementAt(ix);
    }
    
    public WorldComponent getParent() {
        return parent;
    }
    
    public int getNumChildren() {
        return children.size();
    }
    
    
    void updateCache() {
        for (int i=0; i<children.size(); i++) {
            WorldComponent ch =(WorldComponent)children.elementAt(i);
            ch.updateCache();
        }
    }
    
    public void load(Element domElement) throws LoadException {
        if (parent != null)
            baseUrl = parent.getBaseUrl();
        String baseUrlStr = domElement.getAttribute("base-url");
        if (baseUrlStr != null && baseUrlStr.length() > 0) {
            try {
                if (baseUrl != null)
                    baseUrl = new URL(baseUrl, baseUrlStr);
                else
                    baseUrl = new URL(baseUrlStr);
            } catch (MalformedURLException ex) {
                System.err.println(ex);
            }
        }
        title_attrib = domElement.getAttribute("title");
        
        if (title_attrib != null && title_attrib.length() > 0) {
            if (title.length() > 0)
                title += ": ";
            setTitle(title + title_attrib);
            /*
            int [] ch_ix_arr = new int [] {parent.getIndexOfChild(this)};
            Object [] ch_arr = new Object [] {this};
            Object[] path = parent.getPath();
            Universe universe = parent.getUniverse();
            TreeModelEvent e = new TreeModelEvent(universe, path, ch_ix_arr, ch_arr);
            universe.fireTreeNodesChanged(e);
             */
            
        }
        
        name = domElement.getAttribute("name");
        Universe u = getUniverse();
        if (name == null)
            name = UUID.randomUUID().toString();
        
        u.loadedComponents.put(name, this);
        
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement(getTagName());
        
        if (title_attrib != null && title_attrib.length() > 0)
            ele.setAttribute("title", title_attrib);
        ele.setAttribute("base-url", baseUrl.toString());
        ele.setAttribute("name", getName());
        
        for (WorldComponent wc : children) {
            ele.appendChild(wc.save(doc));
        }
        
        return ele;
    }
    
    public void clear() {
        for (WorldComponent wc : children) {
            wc.clear();
            wc.parent = null;
        }
        children.clear();
        if (node != null)
            getUniverse().getDataTreeModel().removeNodeFromParent(node);
    }
    
    public DataTreeNode getDataTreeNode() {
        return node;
    }
}