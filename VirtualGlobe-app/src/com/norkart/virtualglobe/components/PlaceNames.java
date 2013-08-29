/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PlaceNames.java
 *
 * Created on 16. mars 2007, 11:50
 *
 */

package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.components.*;
import com.norkart.virtualglobe.viewer.av3d.AV3DPerspectiveCamera;
import javax.xml.parsers.*;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.net.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;

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

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;
import org.j3d.renderer.aviatrix3d.nodes.Billboard;
import org.j3d.renderer.aviatrix3d.geom.Cylinder;
import org.j3d.renderer.aviatrix3d.geom.Text2D;

import javax.vecmath.*;

import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.av3d.PickListener;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;
import com.norkart.virtualglobe.viewer.ViewerManager;

// import com.norkart.VirtualGlobe.Util.MyBrowserLauncher;
// import com.norkart.VirtualGlobe.Util.SingletonDialog;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.av3d.nodes.*;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.util.MyBrowserLauncher;
import com.norkart.virtualglobe.util.SingletonDialog;

/**
 *
 * @author runaas
 */
public class PlaceNames extends WorldComponent  {
    private DocumentBuilder documentBuilder;
    
    protected String bbox_query_url_str   = "http://ws.geonames.org/wikipediaBoundingBox?srv=121&";
    protected String search_query_url_str = "http://ws.geonames.org/wikipediaSearch?";
    
    protected BttSurface surface = null;
    protected Ellipsoid ellps = null;
    
    protected ArrayList load_queue = new ArrayList();
    
    
    protected Loader loader;
    
    protected int max_pr_tile = 5;
    protected int max_pr_query = 20;
    protected float tile_tol_frac = 1f/2000;
    
    protected WeakHashMap loaded_features = new WeakHashMap();
    
    protected boolean is_enabled = true;
    protected JCheckBox    cb_title = new JCheckBox();
    protected JTextField   search_field = new JTextField();
    protected JButton      search_button = new JButton();
    
    protected SwitchGroup  swGroup = new SwitchGroup();
    protected Group        root_group   = new Group();
    protected Group        search_group = new Group();
    
    protected Appearance appearance = new Appearance();
    
    static String getText(org.w3c.dom.Node n) {
        StringBuffer str_buf = new StringBuffer();
        n.normalize();
        for (org.w3c.dom.Node ch = n.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch instanceof Text) {
                Text t = (Text)ch;
                str_buf.append(t.getData().trim());
            }
        }
        
        return str_buf.toString();
    }
    
    class NameFeatureTableModel extends AbstractTableModel {
        protected ArrayList features = new ArrayList();
        
        public void clear() {
            int sz = features.size();
            features.clear();
            if (sz > 0)
                this.fireTableRowsDeleted(0, sz-1);
        }
        
        public void add(NameFeature e) {
            features.add(e);
            this.fireTableRowsInserted(features.size()-1, features.size()-1);
        }
        
        public int getRowCount() {
            return features.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Object getValueAt(int row, int column) {
            if (row < 0 || row >= features.size())
                return null;
            NameFeature f = (NameFeature)features.get(row);
            switch (column) {
                case 0:
                    return f.title;
                case 1:
                    return f.feature;
            }
            
            return null;
        }
        
        public String getColumnName(int col) {
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
            switch (col) {
                case 0:
                    return settings.getResourceString("TITLE");
                case 1:
                    return settings.getResourceString("TYPE");
            }
            return "";
        }
    }
    NameFeatureTableModel tableModel = new NameFeatureTableModel();
    JTable searchTable = new JTable(tableModel);
    
    public class NameFeature implements PickListener, ActionListener {
        private String id;
        
        private double lat, lon;
        private String title;
        private String text;
        private String feature;
        private URL    url;
        private Component gui;
        private Node   graphics;
        
        public void load(Element e) {
            for (org.w3c.dom.Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (!(n instanceof Element)) continue;
                if (n.getNodeName().equals("wikipediaUrl")) {
                    id = getText(n);
                    try {
                        url = new URL(id);
                    } catch (Exception ex) {}
                } else if (n.getNodeName().equals("lat")) {
                    lat = Double.parseDouble(getText(n));
                } else if (n.getNodeName().equals("lng")) {
                    lon= Double.parseDouble(getText(n));
                } else if (n.getNodeName().equals("title")) {
                    title = getText(n);
                } else if (n.getNodeName().equals("summary")) {
                    text = getText(n);
                } else if (n.getNodeName().equals("feature")) {
                    feature = getText(n);
                }
            }
        }
        
        public Node getGraphics() {
            if (graphics != null)
                return graphics;
            PointMarker pm = new PointMarker(surface,
                    Math.toRadians(lon),
                    Math.toRadians(lat));
            pm.addTextBoard(title, appearance, ViewerManager.getInstance().getCamera(0).getNavigator(), .1f, true);
            ViewerManager.getInstance().addOriginUpdateListener(pm);
            pm.setUserData(this);
            return graphics = pm;
            /*
            float board_height = .3f;
            float stem_height  = 1f;
             
            GlobeSurfaceGroup surf_trans = new GlobeSurfaceGroup(surface, Math.toRadians(lon), Math.toRadians(lat),
                    0, GlobeSurfaceGroup.VERT_REF_TERRAIN, 0);
            getUniverse().getGraphics().addOriginUpdateListener(surf_trans);
            surf_trans.setUserData(this);
             
             
            Matrix4f stem_mx = new Matrix4f();
            stem_mx.setIdentity();
            stem_mx.setColumn(3, 0, stem_height/2, 0, 1);
            TransformGroup stemTrans = new TransformGroup(stem_mx);
            stemTrans.addChild(new Cylinder(stem_height, stem_height/40, appearance));
             
            String[] text_arr = title.split("\\\\n");
            Text2D t_node = new Text2D();
            t_node.setText(text_arr, text_arr.length);
            t_node.setHorizontalJustification(Text2D.JUSTIFY_MIDDLE);
            t_node.setSize(board_height);
            Shape3D s3d = new Shape3D();
            s3d.setGeometry(t_node);
             
            Matrix4f board1_mx = new Matrix4f();
            board1_mx.setIdentity();
            board1_mx.setColumn(3, 0, .5f*board_height*text_arr.length, 0, 1);
            TransformGroup board1Trans = new TransformGroup(board1_mx);
            board1Trans.addChild(s3d);
             
            Billboard bb =  new Billboard(true);
            bb.addChild(board1Trans);
             
            Matrix4f board2_mx = new Matrix4f();
            board2_mx.setIdentity();
            board2_mx.setColumn(3, 0, stem_height + .5f*board_height*text_arr.length, 0, 1);
            TransformGroup board2Trans = new TransformGroup(board2_mx);
            board2Trans.addChild(bb);
             
            com.norkart.VirtualGlobe.Util.Navigation.GlobeNavigator navigator = GraphicsCore.getGraphics().getCamera(0).getNavigator();
            NavigatorElevationRescaleGroup scaleGroup = new NavigatorElevationRescaleGroup(navigator, .1f);
            // scaleGroup.setBounds(new BoundingBox(new float[] {-1000f, -1000f, -1000f}, new float[] {1000f, 100000f, 1000f}));
            scaleGroup.addChild(board2Trans);
            scaleGroup.addChild(stemTrans);
             */
            /*
            LODGroup lod = new LODGroup(false);
            lod.addChild(scaleGroup);
            lod.setRange(0, board_height/2);
             */
            /*
            surf_trans.addChild(scaleGroup);
            SharedNode shared = new SharedNode();
            shared.setChild(surf_trans);
             
            return graphics = shared;
             */
        }
        
        private Component getGui() {
            if (gui != null)
                return gui;
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
            
            Box p = Box.createVerticalBox();
            p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title));
            p.setAlignmentX(Box.LEFT_ALIGNMENT);
            p.setPreferredSize(new Dimension(300, 200));
            
            Box bb = Box.createHorizontalBox();
            
            // Flyto button
            JButton flyButton = new JButton(settings.getResourceString("FLY_TO"));
            flyButton.addActionListener(this);
            flyButton.setAlignmentX(Box.LEFT_ALIGNMENT);
            bb.add(flyButton);
            
            bb.add(Box.createHorizontalGlue());
            
            // URL
            JButton lb = new JButton(settings.getResourceString("OPEN") + " Wikipedia");
            lb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    MyBrowserLauncher.openURL(url);
                }
            });
            bb.add(lb);
            
            p.add(bb);
            
            // Description
            JTextPane desc = new JTextPane();
            desc.setEditable(false);
            desc.setText(text);
            desc.setAlignmentX(Box.LEFT_ALIGNMENT);
            JScrollPane scroll = new JScrollPane(desc);
            scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), settings.getResourceString("TYPE") + ": " + feature));
            p.add(scroll);
            
            gui = p;
            return p;
        }
        
        public void actionPerformed(ActionEvent ev) {
            GlobeNavigator navigator = ViewerManager.getInstance().getCamera(0).getNavigator();
            double dist = surface.getEllipsoid().inverseGeodesic(
                    Math.toRadians(lon), Math.toRadians(lat), navigator.getLon(), navigator.getLat(), null).dist;
            dist += navigator.getTerrainHeight();
            dist *= .1;
            if (dist < 100)
                dist = 100;
            navigator.gotoLookat(Math.toRadians(lon), Math.toRadians(lat), dist);
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
    
    public class NameTile
            implements NodeLoader,  NodeUpdateListener, OriginUpdateListener {
        protected AutoLoadNode auto_node = null;
        protected Group child = null;
        protected boolean is_loading = false;
        
        protected double min_lon, min_lat, min_h, max_lon, max_lat, max_h;
        protected Point3d origin = new Point3d();
        protected BoundingVolume bounds = null;
        
        NameTile(double min_lon, double min_lat, double min_h, double max_lon, double max_lat, double max_h) {
            this.min_lon = min_lon;
            this.min_lat =  min_lat;
            this.min_h = min_h;
            this.max_lon =  max_lon;
            this.max_lat =  max_lat;
            this.max_h = max_h;
            
            ViewerManager.getInstance().addOriginUpdateListener(this);
        }
        //-------------------------------------------------------
        // NodeUpdateListener methods
        //-------------------------------------------------------
        public void updateNodeBoundsChanges(java.lang.Object src) {
            // System.err.println("FeatureGroupExternal origin: " + origin);
        }
        
        public void	updateNodeDataChanges(java.lang.Object src) {}
        
        //-------------------------------------------------------
        // OriginUpdateListener methods
        //-------------------------------------------------------
        public void updateOrigin(Point3d origin) {
            this.origin.set(origin);
            bounds = null;
            /*
            if (origin.x != 0 || origin.y != 0 || origin.z != 0)
                System.err.println("Set origin: " + origin);
             */
            if (auto_node != null && auto_node.isLive())
                ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(auto_node, this, AV3DViewerManager.UPDATE_BOUNDS);
        }
        
        public boolean requestUpdateOrigin(Point3d origin) {
            return true;
        }
        //--------------------------------------------------------
        // NodeLoader methods
        //--------------------------------------------------------
        public BoundingVolume getBounds() {
            if (bounds != null)
                return bounds;
            
            float min_x    = Float.MAX_VALUE;
            float min_y    = Float.MAX_VALUE;
            float min_z    = Float.MAX_VALUE;
            float max_x    = -Float.MAX_VALUE;
            float max_y    = -Float.MAX_VALUE;
            float max_z    = -Float.MAX_VALUE;
            Point3d p = new Point3d();
            
            
            ellps.toCartesian(min_lat, min_lon, min_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(min_lat, min_lon, max_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(min_lat, max_lon, min_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(min_lat, max_lon, max_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(max_lat, min_lon, min_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(max_lat, min_lon, max_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(max_lat, max_lon, min_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            ellps.toCartesian(max_lat, max_lon, max_h, p);
            p.sub(origin);
            if (p.x < min_x) min_x = (float)p.x;
            if (p.y < min_y) min_y = (float)p.y;
            if (p.z < min_z) min_z = (float)p.z;
            if (p.x > max_x) max_x = (float)p.x;
            if (p.y > max_y) max_y = (float)p.y;
            if (p.z > max_z) max_z = (float)p.z;
            
            BoundingBox bbox = new BoundingBox();
            bbox.setMinimum(min_x, min_y, min_z);
            bbox.setMaximum(max_x, max_y, max_z);
            bounds = bbox;
            return bbox;
        }
        
        public void           requestLoad() {
            synchronized (this) {
                if (child == null && !is_loading) {
                    is_loading = true;
                    synchronized (load_queue) {
                        load_queue.add(this);
                        load_queue.notify();
                    }
                }
            }
        }
        
        public Node           takeNode() {
            synchronized (this) {
                Node n = child;
                child = null;
                return n;
            }
        }
        
        public void load() {
            try {
                // Lag forespørsel
                String url_str = bbox_query_url_str +
                        "north="  + String.valueOf(Math.toDegrees(max_lat)) +
                        "&south=" + String.valueOf(Math.toDegrees(min_lat)) +
                        "&east="  + String.valueOf(Math.toDegrees(max_lon)) +
                        "&west="  + String.valueOf(Math.toDegrees(min_lon)) +
                        "&maxRows=" + String.valueOf(max_pr_tile);
                Document doc = null;
                synchronized (documentBuilder) {
                    doc = documentBuilder.parse(url_str);
                }
                // Generate graphics
                Group ch_group = new Group();
                // Load the document
                Element root_ele = doc.getDocumentElement();
                NodeList entries = root_ele.getElementsByTagName("entry");
                for (int i = 0; i < entries.getLength(); ++i) {
                    Element e = (Element)entries.item(i);
                    NameFeature feature = new NameFeature();
                    feature.load(e);
                    
                    if (!loaded_features.containsKey(feature.id)) {
                        // System.out.println(feature.title);
                        Iterator it = tableModel.features.iterator();
                        while (it.hasNext()) {
                            NameFeature f = (NameFeature)it.next();
                            if (f.id.equals(feature.id)) {
                                feature = f;
                                break;
                            }
                        }
                        loaded_features.put(feature.id, new WeakReference(feature));
                        ch_group.addChild(feature.getGraphics());
                    }
                }
                
                // Add child tiles
                double mid_lon = (max_lon+min_lon)/2;
                double mid_lat = (max_lat+min_lat)/2;
                
                ch_group.addChild(createTile(min_lon, min_lat, min_h, mid_lon, mid_lat, max_h));
                ch_group.addChild(createTile(min_lon, mid_lat, min_h, mid_lon, max_lat, max_h));
                ch_group.addChild(createTile(mid_lon, min_lat, min_h, max_lon, mid_lat, max_h));
                ch_group.addChild(createTile(mid_lon, mid_lat, min_h, max_lon, max_lat, max_h));
                synchronized (this) {
                    child = ch_group;
                    is_loading = false;
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    /** Creates a new instance of PlaceNames */
    public PlaceNames(WorldComponent parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        ((DefaultTreeModel)parent.getUniverse().getDataTreeModel()).insertNodeInto(node, parent.node, parent.node.getChildCount());
        
        
        setTitle("Place names from Wikipedia.org and geonames.org");
        
        while (parent != null) {
            if (surface == null && parent instanceof GlobeSurface)
                surface = ((GlobeSurface)parent).getSurface();
            parent = parent.getParent();
        }
        
        ellps = surface.getEllipsoid();
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {}
        
        Material material = new Material();
        material.setDiffuseColor(new float [] {1,1,1,1});
        material.setLightingEnabled(true);
        appearance.setMaterial(material);
        
        root_group.addChild(search_group);
        swGroup.addChild(root_group);
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
        
        // Create GUI
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        Box panel = Box.createVerticalBox();
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
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
        });
        feed_box.add(cb_title);
        feed_box.add(Box.createHorizontalGlue());
        
        panel.add(feed_box);
        
        ActionListener search_listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchName(search_field.getText());
            }
        };
        
        Box search_box = Box.createHorizontalBox();
        search_button.setText(settings.getResourceString("SEARCH"));
        search_box.add(search_field);
        search_box.add(search_button);
        search_field.addActionListener(search_listener);
        search_button.addActionListener(search_listener);
        
        panel.add(search_box);
        
        panel.add(new JScrollPane(searchTable));
        searchTable.addMouseListener(new MouseAdapter() {
            public void	mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2)
                    return;
                int sel_row = searchTable.getSelectedRow();
                if (sel_row >= 0 && sel_row < tableModel.features.size()) {
                    int x = e.getX();
                    int y = e.getY();
                    java.awt.Point p = e.getComponent().getLocationOnScreen();
                    x += p.x;
                    y += p.y;
                    
                    NameFeature en = (NameFeature)tableModel.features.get(sel_row);
                    SingletonDialog.openDialog(en.getGui(), x, y);
                }
            }
        });
        node.getInfoPanel().add(panel);
    }
    
    private void searchName(String name) {
        try {
            String url_str = search_query_url_str +
                    "q="  + URLEncoder.encode(name, "UTF-8") +
                    "&maxRows=" + String.valueOf(max_pr_query);
            
            Document doc = null;
            synchronized (documentBuilder) {
                doc = documentBuilder.parse(url_str);
            }
            
            // Load the document
            Element root_ele = doc.getDocumentElement();
            NodeList entries = root_ele.getElementsByTagName("entry");
            tableModel.clear();
            if (search_group.isLive()) {
                for (int i = 0; i < search_group.numChildren(); ++i) {
                    final Node fn = search_group.getChild(i);
                    ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(search_group, new NodeUpdateListener() {
                        public void updateNodeBoundsChanges(Object o) {
                            Group n = (Group)o;
                            n.removeChild(fn);
                        }
                        public void updateNodeDataChanges(Object o) {}
                    }, AV3DViewerManager.UPDATE_BOUNDS);
                }
            } else
                search_group.removeAllChildren();
            
            for (int i = 0; i < entries.getLength(); ++i) {
                Element e = (Element)entries.item(i);
                NameFeature feature = new NameFeature();
                feature.load(e);
                
                WeakReference ref = (WeakReference)loaded_features.get(feature.id);
                NameFeature f = ref == null ? null : (NameFeature)ref.get();
                if (f != null)
                    feature = f;
                final Node fn = feature.getGraphics();
                if (search_group.isLive()) {
                    ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(search_group, new NodeUpdateListener() {
                        public void updateNodeBoundsChanges(Object o) {
                            Group n = (Group)o;
                            n.addChild(fn);
                        }
                        public void updateNodeDataChanges(Object o) {}
                    }, AV3DViewerManager.UPDATE_BOUNDS);
                } else
                    search_group.addChild(fn);
                
                tableModel.add(feature);
            }
            
            
            
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    private Node createTile(double min_lon, double min_lat, double min_h, double max_lon, double max_lat, double max_h) {
        Ellipsoid ellps = surface.getEllipsoid();
        float tol = (float)ellps.inverseGeodesic(min_lat, min_lon, max_lat, max_lon, null).dist*tile_tol_frac;
        LODGroup lod = new LODGroup(false);
        NameTile tile = new NameTile(min_lon, min_lat, min_h, max_lon, max_lat, max_h);
        lod.addChild(tile.auto_node = new AutoLoadNode(tile));
        lod.setRange(0, tol);
        return lod;
    }
    
    public String getTagName() {
        return "place-names";
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("place-names"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        
        // Start loader thread
        if (loader != null)
            loader.stop();
        new Thread(loader = new Loader()).start();
        
        // Initialize fundamental set
        final Group ch_group = new Group();
        ch_group.addChild(createTile(Math.toRadians(-180), Math.toRadians(-90), 0,
                Math.toRadians(-90),    Math.toRadians( 0), 10000));
        ch_group.addChild(createTile(Math.toRadians(-180), Math.toRadians(0), 0,
                Math.toRadians(-90),    Math.toRadians( 90), 10000));
        ch_group.addChild(createTile(Math.toRadians(-90), Math.toRadians(-90), 0,
                Math.toRadians(0),    Math.toRadians( 0), 10000));
        ch_group.addChild(createTile(Math.toRadians(-90), Math.toRadians(0), 0,
                Math.toRadians(0),    Math.toRadians( 90), 10000));
        ch_group.addChild(createTile(Math.toRadians(0), Math.toRadians(-90), 0,
                Math.toRadians(90),    Math.toRadians( 0), 10000));
        ch_group.addChild(createTile(Math.toRadians(0), Math.toRadians(0), 0,
                Math.toRadians(90),    Math.toRadians( 90), 10000));
        ch_group.addChild(createTile(Math.toRadians(90), Math.toRadians(-90), 0,
                Math.toRadians(180),    Math.toRadians( 0), 10000));
        ch_group.addChild(createTile(Math.toRadians(90), Math.toRadians(0), 0,
                Math.toRadians(180),    Math.toRadians( 90), 10000));
         /*
        ch_group.addChild(createTile(Math.toRadians(5), Math.toRadians(58), 0,
                Math.toRadians(40),    Math.toRadians( 75), 10000));
          */
        
        if (root_group.isLive()) {
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(root_group, new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.addChild(ch_group);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            root_group.addChild(ch_group);
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement("place-names");
        return ele;
    }
    
    public void clear() {
        loader.stop();
        
        AV3DViewerManager mgr = (AV3DViewerManager)ViewerManager.getInstance();
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
        root_group = null;
        
        surface = null;
    }
    
    
    class Loader implements Runnable {
        private boolean go_on = true;
        
        public void stop() {
            synchronized(load_queue) {
                go_on = false;
                load_queue.notify();
            }
        }
        
        public void run() {
            while (go_on) {
                NameTile tile = null;
                synchronized(load_queue) {
                    while (go_on && tile == null) {
                        if (!load_queue.isEmpty())
                            tile = (NameTile)load_queue.remove(0);
                        else {
                            try {
                                load_queue.wait();
                            } catch (InterruptedException ex) {}
                        }
                    }
                }
                
                if (!go_on)
                    break;
                if (tile != null)
                    tile.load();
            }
        }
    }
}
