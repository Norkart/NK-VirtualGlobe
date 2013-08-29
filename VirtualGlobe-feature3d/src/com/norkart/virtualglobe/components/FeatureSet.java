//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

// import com.sintef.VirtualGlobe.Graphics.AV.Nodes.*;
import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.components.feature.Feature3D;
import com.norkart.virtualglobe.components.feature.Feature3DGroup;
import com.norkart.virtualglobe.components.feature.Feature3DGroupDirect;
import com.norkart.virtualglobe.components.feature.Feature3DGroupExternal;
// import com.norkart.VirtualGlobe.WorldComponents.gui.*;

import org.w3c.dom.*;
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SwitchGroup;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultTreeModel;

import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class FeatureSet extends WorldComponent {
    protected GlobeSurface globe = null;
    protected Group        group = new Group();
    protected SwitchGroup  swGroup = new SwitchGroup();
    protected boolean is_enabled = true;
    // protected JCheckBox    cb_title = new JCheckBox();
    
    public class DTNodeListener extends DataTreeNodeAdapter {
        public void nodeSelected(DataTreeNode node) {
            if (node.isSelected() != is_enabled) {
                is_enabled = node.isSelected();
                if (swGroup.isLive()) {
                    ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(swGroup, new NodeUpdateListener() {
                        public void updateNodeBoundsChanges(Object o) {
                            if (o == swGroup)
                                swGroup.setActiveChild(is_enabled?0:-1);
                        }
                        public void updateNodeDataChanges(Object o) {}
                    }, AV3DViewerManager.UPDATE_BOUNDS);
                } else
                    swGroup.setActiveChild(is_enabled?0:-1);
            }
        }
    }
    
    public FeatureSet(WorldComponent parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        getUniverse().getDataTreeModel().insertNodeInto(node, parent.node, parent.node.getChildCount());
        
        node.setMode(DataTreeNode.Mode.CHECK);
        node.setDataTreeNodeListener(new DTNodeListener());
        getUniverse().getDataTreeModel().nodeChanged(node);
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        while (parent != null) {
            if (globe == null && parent instanceof GlobeSurface)
                globe = (GlobeSurface)parent;
            parent = parent.getParent();
        }
        
        swGroup.addChild(group);
        swGroup.setActiveChild(is_enabled?0:-1);
        
       
        final AV3DViewerManager mgr = (AV3DViewerManager)ViewerManager.getInstance();
        if (mgr.getFeatureRoot().isLive()) {
            mgr.updateNode(mgr.getFeatureRoot(), new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.addChild(swGroup);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            mgr.getFeatureRoot().addChild(swGroup);
        
        setTitle(settings.getResourceString("FEATURE_SET_TITLE"));
        
        // Create GUI
        /*
        Border b = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createTitledBorder(b, settings.getResourceString("FEATURE_SET_TITLE")));
         
         
        // Wireframe
        Box sub_box = Box.createHorizontalBox();
        sub_box.setBorder(b);
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
         
        cb_title.setText(getTitle());
        cb_title.setSelected(is_enabled);
        cb_title.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == cb_title && cb_title.isSelected() != is_enabled) {
                    is_enabled = cb_title.isSelected();
                    if (swGroup.isLive()) {
                        camera.updateNode(swGroup, new NodeUpdateListener() {
                            public void updateNodeBoundsChanges(Object o) {
                                if (o == swGroup)
                                    swGroup.setActiveChild(is_enabled?0:-1);
                            }
                            public void updateNodeDataChanges(Object o) {}
                        }, AVPerspectiveCamera.UPDATE_BOUNDS);
                    } else
                        swGroup.setActiveChild(is_enabled?0:-1);
                }
            }
        });
        sub_box.add(cb_title);
        sub_box.add(Box.createHorizontalGlue());
        panel.add(sub_box);
        uiComp.add(panel);
         */
    }
    
    public void setTitle(String title) {
        super.setTitle(title);
        // cb_title.setText(getTitle());
    }
    
    public String getTagName() {
        return "feature-set";
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("feature-set"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        
        // cb_title.setText(getTitle());
        
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            
            String eleName = chEle.getNodeName();
            if (eleName.equals("feature3D-group") ||
                    eleName.equals("feature3D-lod") ||
                    eleName.equals("feature3D-external")) {
                Feature3DGroup f3Dg = null;
                if (eleName.equals("feature3D-external"))
                    f3Dg = new Feature3DGroupExternal(this);
                else
                    f3Dg = new Feature3DGroupDirect(this);
                f3Dg.load(chEle);
                
                final LODGroup lod = new LODGroup(false);
                lod.addChild(f3Dg.getNode());
                lod.setRange(0, f3Dg.getDetailSize());
                if (group.isLive()) {
                    ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(group, new NodeUpdateListener() {
                        public void updateNodeBoundsChanges(Object o) {
                            Group g = (Group)o;
                            g.addChild(lod);
                        }
                        public void updateNodeDataChanges(Object o) {}
                    }, AV3DViewerManager.UPDATE_BOUNDS);
                } else
                    group.addChild(lod);
            }
        }
    }
    
    public Element save(Document doc) {
        Element ele = super.save(doc);
        
        Feature3D.saveFeatureRecursive(group, ele, doc);
        
        return ele;
    }
    
    public void clear() {
        final AV3DViewerManager mgr = (AV3DViewerManager)ViewerManager.getInstance();
        if (mgr.getFeatureRoot().isLive()) {
            mgr.updateNode(mgr.getFeatureRoot(), new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.removeChild(swGroup);
                    swGroup = null;
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else {
            mgr.getFeatureRoot().removeChild(swGroup);
            swGroup = null;
        }
        
        globe   = null;
        group   = null;
    }
    
    public GlobeSurface getGlobe() {
        return globe;
    }
    
    
}