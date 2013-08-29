//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * GeoRSSFeed.java
 *
 * Created on 8. februar 2007, 13:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.av3d.AV3DPerspectiveCamera;
import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.virtualglobe.components.WorldComponent;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.components.DataTreeNode;
import com.norkart.virtualglobe.components.DataTreeNodeAdapter;
import com.norkart.geopos.Geometry;
import com.norkart.geopos.LineString;
import com.norkart.geopos.Point;
import com.norkart.geopos.Position;
import com.norkart.geopos.PositionList;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import org.w3c.dom.*;

import javax.vecmath.*;

import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.Material;
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.SwitchGroup;
import org.j3d.aviatrix3d.Appearance;
import org.j3d.renderer.aviatrix3d.geom.Sphere;
import org.j3d.renderer.aviatrix3d.geom.Cylinder;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.io.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;

import java.text.DateFormat;
import com.norkart.virtualglobe.util.SingletonDialog;
import com.norkart.virtualglobe.viewer.av3d.PickListener;
import com.norkart.virtualglobe.util.MyBrowserLauncher;
import com.norkart.virtualglobe.util.TableSorter;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.av3d.nodes.PointMarker;
import com.norkart.virtualglobe.viewer.av3d.nodes.GlobeSurfaceLine;
import com.norkart.virtualglobe.viewer.av3d.nodes.NavigatorElevationRescaleGroup;
import com.norkart.virtualglobe.components.DomLoadable.LoadException;
import com.norkart.virtualglobe.components.styling.FeatureTypeStyle;

/**
 *
 * @author runaas
 */
public class GeoRSSFeed extends WorldComponent {
    protected GlobeSurface globe = null;
    protected URL url;
    protected SyndFeed feed;
    protected Geometry geometry;
    protected Node         feedNode;
    protected Group        root_group = new Group();
    protected SwitchGroup  swGroup = new SwitchGroup();
    protected boolean is_enabled = true;
    // protected JCheckBox    cb_title = new JCheckBox();
    protected JLabel title_label = new JLabel();
    
    protected JTable    summaryTable = new JTable();
    protected TableSorter sorter;
    /*
    protected Appearance stem_app = new Appearance();
    protected Appearance head_app = new Appearance();
     */
    protected FeatureTypeStyle style = new FeatureTypeStyle();
    
    public class DTNodeListener extends DataTreeNodeAdapter {
        public void nodeSelected(DataTreeNode node) {
            if (node.isSelected() != is_enabled) {
                AV3DViewerManager mgr = (AV3DViewerManager)(ViewerManager.getInstance());
                is_enabled = node.isSelected();
                if (swGroup.isLive()) {
                    mgr.updateNode(swGroup, new NodeUpdateListener() {
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
    
    public class Entry implements ActionListener, HyperlinkListener, PickListener {
        private SyndEntry entry;
        private Geometry geometry;
        private Node  avNode;
        private Component gui;
        private Date date;
        
        public Entry(SyndEntry entry) {
            this.entry = entry;
            GeoRSSModule geoRSSModule = GeoRSSUtils.getGeoRSS(entry);
            if (geoRSSModule != null) {
                
                geometry = geoRSSModule.getGeometry();
                avNode = style.createGraphics(geometry, globe);
                 /*
                if (geometry instanceof Point) {
                    Point p = (Point)geometry;
                  */
                    /*
                    float stem_height = 1;//00000;
                    float stem_diameter = stem_height/20;
                    float sphere_diameter = stem_diameter*5;
                    Matrix4f mat = new Matrix4f();
                    mat.setIdentity();
                    mat.setColumn(3, 0, stem_height/2, 0, 1);
                    TransformGroup stem = new TransformGroup(mat);
                    stem.addChild(new Cylinder(stem_height, stem_diameter, stem_app));
                     
                    mat.setColumn(3, 0, stem_height, 0, 1);
                    TransformGroup head = new TransformGroup(mat);
                    head.addChild(new Sphere(sphere_diameter/2, head_app));
                     
                    GlobeNavigator navigator = GraphicsCore.getGraphics().getCamera(0).getNavigator();
                    NavigatorElevationRescaleGroup scaleGroup = new NavigatorElevationRescaleGroup(navigator, .1f);
                    scaleGroup.addChild(stem);
                    scaleGroup.addChild(head);
                     
                    avNode = new GlobeSurfaceGroup(globe.getSurface(),
                            Math.toRadians(p.getPosition().getLongitude()),
                            Math.toRadians(p.getPosition().getLatitude()),
                            0, GlobeSurfaceGroup.VERT_REF_TERRAIN, 0);
                    ((Group)avNode).addChild(scaleGroup);
                     */
                    /*
                    PointMarker pm = new PointMarker(globe.getSurface(),
                            Math.toRadians(p.getPosition().getLongitude()),
                            Math.toRadians(p.getPosition().getLatitude()));
                    pm.addPin(stem_app, head_app, GraphicsCore.getGraphics().getCamera(0).getNavigator(), .1f);
                     
                     getUniverse().getGraphics().addOriginUpdateListener(pm);
                     avNode = pm;
                     */
                
                /* Lag et objekt og sett på gruppen */
                    /*
                } else if (geometry instanceof LineString) {
                    LineString ls = (LineString)geometry;
                     
                    GlobeSurfaceLine gsl = new GlobeSurfaceLine(globe.getSurface(), ls);
                    Shape3D sh = new Shape3D();
                    sh.setGeometry(gsl);
                    getUniverse().getGraphics().addOriginUpdateListener(gsl);
                     
                    avNode = sh;
                }*/
                
                if (avNode != null) {
                    avNode.setUserData(this);
                    
                    if (root_group.isLive()) {
                        AV3DViewerManager mgr = (AV3DViewerManager)(ViewerManager.getInstance());
                        mgr.updateNode(root_group, new NodeUpdateListener() {
                            public void updateNodeBoundsChanges(Object o) {
                                Group g = (Group)o;
                                g.addChild(avNode);
                            }
                            public void updateNodeDataChanges(Object o) {}
                        }, AV3DViewerManager.UPDATE_BOUNDS);
                    } else
                        root_group.addChild(avNode);
                }
                /* else {
                 
                    printGeometry(geometry, entry.getTitle(),
                            entry.getDescription() == null ? "" : entry.getDescription().getValue(),
                            entry.getPublishedDate());
                } */
            }
            
            // DCModule dc = (DCModule)entry.getModule(DCModule.URI);
            if (date == null || (entry.getUpdatedDate() != null && date.before(entry.getUpdatedDate())))
                date = entry.getUpdatedDate();
            if (date == null || (entry.getPublishedDate() != null && date.before(entry.getPublishedDate())))
                date = entry.getPublishedDate();
            /*
            if (date == null) {
                if (dc != null)
                    date = dc.getDate();
            }*/
            if (date == null)
                date = new Date();
        }
        
        public void actionPerformed(ActionEvent ev) {
            Position p = ((Point)geometry).getPosition();
            GlobeNavigator navigator = ViewerManager.getInstance().getCamera(0).getNavigator();
            double dist = globe.getSurface().getEllipsoid().inverseGeodesic(
                    Math.toRadians(p.getLongitude()), Math.toRadians(p.getLatitude()), navigator.getLon(), navigator.getLat(), null).dist;
            dist += navigator.getTerrainHeight();
            dist *= .1;
            if (dist < 100)
                dist = 100;
            navigator.gotoLookat(Math.toRadians(p.getLongitude()), Math.toRadians(p.getLatitude()), dist);
        }
        
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                if (!MyBrowserLauncher.openURL(e.getURL()))
                    System.out.println("Supposed to open " + e.getURL() + ", but it didn't work");
        }
        
        
        private Component getGui() {
            if (gui != null)
                return gui;
            
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
            
            Box p = Box.createVerticalBox();
            p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), entry.getTitle()));
            p.setAlignmentX(Box.LEFT_ALIGNMENT);
            p.setPreferredSize(new Dimension(300, 200));
            
            // Flyto button
            if (geometry != null) {
                if (geometry instanceof Point) {
                    JButton flyButton = new JButton(settings.getResourceString("FLY_TO"));
                    flyButton.addActionListener(this);
                    flyButton.setAlignmentX(Box.LEFT_ALIGNMENT);
                    p.add(flyButton);
                }
            }
            
            // Description
            if (entry.getDescription() != null) {
                JTextPane desc = new JTextPane();
                desc.setEditable(false);
                if (entry.getDescription().getType() != null)
                    desc.setContentType(entry.getDescription().getType());
                desc.setText(entry.getDescription().getValue());
                desc.addHyperlinkListener(this);
                desc.setAlignmentX(Box.LEFT_ALIGNMENT);
                p.add(new JScrollPane(desc));
            }
            
            // Contents list
            List contentsList = entry.getContents();
            if (contentsList != null && !contentsList.isEmpty()) {
                Iterator it = contentsList.iterator();
                while (it.hasNext()) {
                    SyndContent content = (SyndContent)it.next();
                    JTextPane desc = new JTextPane();
                    desc.setEditable(false);
                    if (content.getType() != null)
                        desc.setContentType(content.getType());
                    desc.setText(content.getValue());
                    desc.addHyperlinkListener(this);
                    desc.setAlignmentX(Box.LEFT_ALIGNMENT);
                    p.add(new JScrollPane(desc));
                }
            }
            
            // URL
            {
                final String link = entry.getLink();
                try {
                    final URL url = new URL(link);
                    JButton lb = new JButton(link);
                    lb.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            MyBrowserLauncher.openURL(url);
                        }
                    });
                    p.add(lb);
                } catch (MalformedURLException ex) { }
            }
            /*
            List links = entry.getLinks();
            if (links != null && !links.isEmpty()) {
                Iterator it = links.iterator();
                while (it.hasNext()) {
                    final String link = ((SyndLink)it.next()).getHref();
                    try {
                        final URL url = new URL(link);
                        JButton lb = new JButton(link);
                        lb.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                MyBrowserLauncher.openURL(url);
                            }
                        });
                        p.add(lb);
                    } catch (MalformedURLException ex) { }
                }
            }
             */
            
            gui = p;
            return p;
        }
        
        public void picked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            java.awt.Point p = e.getComponent().getLocationOnScreen();
            x += p.x;
            y += p.y;
            
            SingletonDialog.openDialog(getGui(), x, y);
        }
    }
    
    
    class GeoRSSTableModel extends AbstractTableModel {
        protected ArrayList entries = new ArrayList();
        private DateFormat df = DateFormat.getInstance();
        
        public void addEntry(Entry e) {
            entries.add(e);
            this.fireTableRowsInserted(entries.size()-1, entries.size()-1);
        }
        
        public int getRowCount() {
            return entries.size();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public Object getValueAt(int row, int column) {
            if (row < 0 || row >= entries.size())
                return null;
            GeoRSSFeed.Entry e = (GeoRSSFeed.Entry)entries.get(row);
            switch (column) {
                case 0:
                    return e.entry.getTitle();
                case 1: {
                    return df.format(e.date);
                }
                case 2: {
                    if (e.geometry != null)
                        return geometryToString(e.geometry);
                }
            }
            
            return null;
        }
        
        public String getColumnName(int col) {
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
            switch (col) {
                case 0:
                    return settings.getResourceString("TITLE");
                case 1:
                    return settings.getResourceString("DATE");
                case 2:
                    return settings.getResourceString("POSITION");
            }
            return "";
        }
    }
    GeoRSSTableModel tableModel = new GeoRSSTableModel();
    
    
    /** Creates a new instance of GeoRSSFeed */
    public GeoRSSFeed(WorldComponent parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        ((DefaultTreeModel)parent.getUniverse().getDataTreeModel()).insertNodeInto(node, parent.node, parent.node.getChildCount());
        
        node.setMode(DataTreeNode.Mode.CHECK);
        node.setDataTreeNodeListener(new DTNodeListener());
        ((DefaultTreeModel)getUniverse().getDataTreeModel()).nodeChanged(node);
        // Graphic stuff
        /*
        Material stem_material = new Material();
        stem_material.setDiffuseColor(new float [] {1,1,1,1});
        stem_material.setLightingEnabled(true);
        stem_app.setMaterial(stem_material);
         
        Material head_material = new Material();
        head_material.setDiffuseColor(new float [] {1,0,0,1});
        head_material.setLightingEnabled(true);
        head_app.setMaterial(head_material);
         */
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        while (parent != null) {
            if (globe == null && parent instanceof GlobeSurface)
                globe = (GlobeSurface)parent;
            parent = parent.getParent();
        }
        
        swGroup.addChild(root_group);
        swGroup.setActiveChild(is_enabled?0:-1);
        
        AV3DViewerManager mgr = (AV3DViewerManager)(ViewerManager.getInstance());
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
        
        
        
        // Create GUI
        Box panel = Box.createVerticalBox();
        
        /*
        Box feed_box = Box.createHorizontalBox();
        feed_box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), getTitle()));
        feed_box.setAlignmentX(Component.LEFT_ALIGNMENT);
         
        cb_title.setText(getTitle());
        cb_title.setSelected(is_enabled);
        cb_title.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        feed_box.add(cb_title);
        feed_box.add(Box.createHorizontalGlue());
        panel.add(feed_box);
         */
        
        title_label.setText(getTitle());
        panel.add(title_label);
        
        sorter = new TableSorter(tableModel);
        summaryTable.setModel(sorter);
        sorter.setTableHeader(summaryTable.getTableHeader());
        panel.add(new JScrollPane(summaryTable));
        summaryTable.addMouseListener(new MouseAdapter() {
            public void	mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2)
                    return;
                int sel_row = summaryTable.getSelectedRow();
                if (sel_row >= 0 && sel_row < tableModel.entries.size()) {
                    int x = e.getX();
                    int y = e.getY();
                    java.awt.Point p = e.getComponent().getLocationOnScreen();
                    x += p.x;
                    y += p.y;
                    
                    Entry en = (Entry)tableModel.entries.get(sorter.modelIndex(sel_row));
                    SingletonDialog.openDialog(en.getGui(), x, y);
                }
            }
        });
        
        node.getInfoPanel().add(panel);
    }
    
    public void clear() {
        AV3DViewerManager mgr = (AV3DViewerManager)(ViewerManager.getInstance());
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
        
        globe      = null;
        root_group = null;
    }
    
    public void setTitle(String title) {
        super.setTitle(title);
        title_label.setText(getTitle());
    }
    
    private String geometryToString(Geometry geometry) {
        StringBuffer str_buf = new StringBuffer();
        if (geometry instanceof Point) {
            Position pos = ((Point)geometry).getPosition();
            str_buf.append("pos[");
            str_buf.append(pos.getLatitude());
            str_buf.append(", ");
            str_buf.append(pos.getLongitude());
            str_buf.append("]");
        } else if (geometry instanceof LineString) {
            PositionList posList = ((LineString)geometry).getPositionList();
            str_buf.append("posList[");
            for (int i=0; i<posList.size(); ++i) {
                str_buf.append("[").append(posList.getLatitude(i)).append(", ").append(posList.getLongitude(i)).append("]");
            }
            str_buf.append("]");
        }
        return str_buf.toString();
    }
    
    public String getTagName() {
        return "georss-feed";
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("georss-feed"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        
        String urlStr = domElement.getAttribute("href");
        if (urlStr != null) {
            try {
                url = new URL(getBaseUrl(), urlStr);
            } catch (java.net.MalformedURLException ex) {
                System.err.println("Unable to create URL: " + ex.getMessage());
            }
        }
        
        for (org.w3c.dom.Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element))
                continue;
            Element chEle = (Element)ch;
            if (chEle.getTagName().equals("FeatureTypeStyle")) {
                style.load(chEle);
            }
        }
        
        new Thread() {
            public void run() {
                updateRSS();
            }
        }.start();
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement("georss-feed");
        ele.setAttribute("href", url.toString());
        return ele;
    }
    
    public void updateRSS() {
        
        SyndFeedInput input = new SyndFeedInput();
        try {
            feed = input.build(new XmlReader(url));
            // "http://www.geonames.org/recent-changes.xml")));
            
            // Get feed data
            
            List feedEntries = feed.getEntries();
            GeoRSSModule geoRSSModule = GeoRSSUtils.getGeoRSS(feed);
            if (geoRSSModule != null && geoRSSModule.getGeometry() != null) {
                geometry = geoRSSModule.getGeometry();
                // printGeometry(geometry, feed.getTitle(), feed.getDescription(), feed.getPublishedDate());
                feedNode = style.createGraphics(geometry, globe);
                   /*
                if (geometry instanceof LineString) {
                    LineString ls = (LineString)geometry;
                    
                    GlobeSurfaceLine gsl = new GlobeSurfaceLine(globe.getSurface(), ls);
                    Shape3D sh = new Shape3D();
                    sh.setGeometry(gsl);
                    getUniverse().getGraphics().addOriginUpdateListener(gsl);
                    feedNode = sh;
                }*/
                
                if (feedNode != null) {
                    feedNode.setUserData(this);
                    if (root_group.isLive()) {
                        AV3DViewerManager mgr = (AV3DViewerManager)(ViewerManager.getInstance());
                        mgr.updateNode(root_group, new NodeUpdateListener() {
                            public void updateNodeBoundsChanges(Object o) {
                                Group g = (Group)o;
                                g.addChild(feedNode);
                            }
                            public void updateNodeDataChanges(Object o) {}
                        }, AV3DViewerManager.UPDATE_BOUNDS);
                    } else
                        root_group.addChild(feedNode);
                }
            }
            
            // Get entry data
            Iterator it = feedEntries.iterator();
            while (it.hasNext()) {
                final Entry entry = new Entry((SyndEntry)it.next());
                tableModel.addEntry(entry);
                
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
