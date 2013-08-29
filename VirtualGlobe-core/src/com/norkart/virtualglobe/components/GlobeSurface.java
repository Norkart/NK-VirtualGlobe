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
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.Camera;
import com.norkart.virtualglobe.viewer.GlobeSurfaceGraphics;
import java.net.*;
import java.io.*;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.w3c.dom.*;
import java.awt.*;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.vecmath.Point3d;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.virtualglobe.globesurface.ElevationSource;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class GlobeSurface extends WorldComponent  {
    private String elevationsUrlStr;
    private JLabel elevationUrlLabel = null;
    
    private BttSurface surface;
    private ElevationSource source;
    private GlobeSurfaceGraphics surface_graphics;
    
    // protected GeoRSSFeeds geoRSSFeeds;   
    
    public GlobeSurface(WorldComponent parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        parent.getUniverse().getDataTreeModel().insertNodeInto(node, parent.node, parent.node.getChildCount());
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        setTitle(settings.getResourceString("GLOBE_SURFACE_TITLE"));
        
        // Create GUI
        Border b = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createTitledBorder(b, settings.getResourceString("GLOBE_SURFACE_TITLE")));
        
        // URL label
        Box sub_box = Box.createHorizontalBox();
        sub_box.setBorder(b);
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(settings.getResourceString("ELEVATION_URL_LABEL"));
        sub_box.add(l);
        sub_box.add(Box.createHorizontalGlue());
        elevationUrlLabel = new JLabel(elevationsUrlStr);
        sub_box.add(elevationUrlLabel);
        panel.add(sub_box);
        
        // Wireframe
        sub_box = Box.createHorizontalBox();
        sub_box.setBorder(b);
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JCheckBox cb = new JCheckBox(settings.getResourceString("WIREFRAME_LABEL"));
        
        cb.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == cb) {
                    surface_graphics.setWireframe(cb.isSelected());
                }
            }
        });
        sub_box.add(cb);
        sub_box.add(Box.createHorizontalGlue());
        panel.add(sub_box);
        
        // Elevation exaggeration
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(b);
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Elevation Exaggeration slider and label
        final Object[] ma = { new Double(1) };
        final MessageFormat mf = new MessageFormat( settings.getResourceString("ELEVATION_MULT_LABEL"));
        final JLabel  elevExagLabel    = new JLabel(mf.format(ma));
        elevExagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider elevExagSlider   = new JSlider();
        elevExagSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        elevExagSlider.setOrientation(JSlider.HORIZONTAL);
        elevExagSlider.setMaximum(100);
        elevExagSlider.setMinimum(0);
        elevExagSlider.setValue(0);
        elevExagSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == elevExagSlider) {
                    float value    = elevExagSlider.getValue();
                    final double c = 1;
                    final double b = Math.log(19./4.)/Math.log(2.);
                    final double a = 4./Math.pow(50, b);
                    value = (float)(a*Math.pow(value, b) + c);
                    
                    ma[0] = new Double(value);
                    elevExagLabel.setText(mf.format(ma));
                    if (surface != null)
                        surface.setElevationScale(value);
                }
            }
        });
        elevExagSlider.setToolTipText(settings.getResourceString("ELEVATION_MULT_TIPS"));
        sub_box.add(elevExagSlider);
        sub_box.add(elevExagLabel);
        panel.add(sub_box);
        
        // Transparency
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(b);
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Elevation Exaggeration slider and label
        final Object[] trans_ma = { new Double(100) };
        final MessageFormat trans_mf = new MessageFormat( settings.getResourceString("TRANSPARENCY_LABEL"));
        final JLabel  transLabel    = new JLabel(trans_mf.format(trans_ma));
        transLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider transSlider   = new JSlider();
        transSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        transSlider.setOrientation(JSlider.HORIZONTAL);
        transSlider.setMaximum(100);
        transSlider.setMinimum(0);
        transSlider.setValue(100);
        transSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == transSlider) {
                    float value    = transSlider.getValue();
                    trans_ma[0] = new Double(value);
                    transLabel.setText(trans_mf.format(trans_ma));
                    if (surface_graphics != null)
                        surface_graphics.setTransparency(value/100f);
                }
            }
        });
        transSlider.setToolTipText(settings.getResourceString("TRANSPARENCY_TIPS"));
        sub_box.add(transSlider);
        sub_box.add(transLabel);
        panel.add(sub_box);
        
        node.getInfoPanel().add(panel);
    }
    
    synchronized public BttSurface getSurface() {
        while (surface == null) {
            try { wait(); } catch (InterruptedException ex) {}
        }
        return surface;
    }
    
    public String getTagName() {
        return "globe-surface";
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("globe-surface"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        
        // First load the surface:
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("elevations")) {
                elevationsUrlStr = chEle.getAttribute("href");
                if (elevationUrlLabel != null)
                    elevationUrlLabel.setText(elevationsUrlStr);
                Universe u = getUniverse();
                URL server_url = null;
                try {
                    server_url = new URL(getBaseUrl(), elevationsUrlStr);
                } catch (MalformedURLException ex) {
                    System.err.print("Råtten url " + elevationsUrlStr);
                }
                source = new ElevationSource(server_url, u.isCacheEnabled()?u.getCacheManager():null);
                // surface = new BttSurface(14745, source);
                synchronized (this) {
                    surface = new BttSurface(source);
                    notifyAll();
                }
                // surface = new BttSurface(1<<15, source);
                ViewerManager vm = ViewerManager.getInstance();
                surface_graphics = vm.addGlobeSurface(surface);
                // gc.addOriginUpdateListener(surface);
                for (int i=0; i<vm.getNumCamera(); i++) {
                    Camera c = vm.getCamera(i);
                    GlobeNavigator n = c.getNavigator();
                    if (n != null)
                        n.setGlobe(surface);
                }
            }
        }
        WorldComponentFactory.getInstance().loadChildren(this, domElement);
        // Then load all the other stuff
        /*
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("feature-set") && GraphicsCore.getGraphics().getAviatrixCamera() != null) {
                FeatureSet fs = new FeatureSet(this);
                fs.load(chEle);
            } else if (chEle.getNodeName().equals("simple-pyramid-coverage")) {
                SimplePyramidCoverage pc = new SimplePyramidCoverage(this);
                pc.load(chEle);
            } else if (chEle.getNodeName().equals("layered-pyramid-coverage")) {
                LayeredPyramidCoverage pc = new LayeredPyramidCoverage(this);
                pc.load(chEle);
            } else if (chEle.getNodeName().equals("georss-feed") && GraphicsCore.getGraphics().getAviatrixCamera() != null) {
                GeoRSSFeed geo_feed = new GeoRSSFeed(this);
                geo_feed.load(chEle);
                // }
            } else if (chEle.getNodeName().equals("place-names") && GraphicsCore.getGraphics().getAviatrixCamera() != null){
                PlaceNames place_names = new PlaceNames(this);
                place_names.load(chEle);
            }
        }
        */
        /*
        if (geoRSSFeeds == null && GraphicsCore.getGraphics().getAviatrixCamera() != null) {
            try {
                Class.forName("com.sun.syndication.feed.module.georss.GeoRSSModule");
                geoRSSFeeds=new GeoRSSFeeds(this);
            } catch (Throwable ex) { }
        }
         */
    }
    
    public Element save(Document doc) {
        Element ele = super.save(doc);
        
        Element elev_ele = doc.createElement("elevations");
        elev_ele.setAttribute("href", elevationsUrlStr);
        ele.appendChild(elev_ele);
        return ele;
    }
    
    void updateCache() {
        if (source != null) {
            Universe u = getUniverse();
            source.setCacheManager(u.isCacheEnabled()?u.getCacheManager():null);
        }
        super.updateCache();
    }
    
    public void clear() {
        // Close data source
        try {
            source.close();
        } catch (IOException ex) {
            System.err.print("Problems closing elevation source ");
            System.err.println(ex);
        }
        // Close texture loaders and 3D features
        super.clear();
        
        // Remove from graphics system
       
        ViewerManager vm = ViewerManager.getInstance();
        for (int i=0; i<vm.getNumCamera(); i++) {
            Camera c = vm.getCamera(i);
            GlobeNavigator n = c.getNavigator();
            if (n != null && n.getGlobe() == surface)
                    n.setGlobe(null);
        }
        // gc.removeOriginUpdateListener(surface);
        vm.removeGlobeSurface(surface);
        
        // Close surface structures
        surface.clear();
        source  = null;
        surface = null;
    }
}