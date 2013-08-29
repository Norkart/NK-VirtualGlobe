/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Graphic.java
 *
 * Created on 7. juni 2007, 11:31
 *
 */

package com.norkart.virtualglobe.components.styling;


import com.norkart.geopos.*;

import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.nodes.PointMarker;

import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.Material;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.Color;

/**
 *
 * @author runaas
 */
public class PinMark extends Mark {
    private float size = -1;
    
    static protected Appearance stem_app = new Appearance();
    protected Fill head_fill = new Fill();
    
    static {
        Material stem_material = new Material();
        stem_material.setDiffuseColor(new float [] {1,1,1,1});
        stem_material.setLightingEnabled(true);
        stem_app.setMaterial(stem_material);
    }
    
    
    /** Creates a new instance of Graphic */
    public PinMark() {
    }
    /*
     public PinStyle(Color color, float size) {
        this.color = color;
        this.size = size;
     
     
     
        Material head_material = new Material();
        head_material.setDiffuseColor(color.getRGBComponents(null));
        head_material.setLightingEnabled(true);
        head_app.setMaterial(head_material);
    }
     
    public PinStyle(Color color) {
        this(color, -1);
    }
     
    public PinStyle() {
        this(Color.RED);
    }
     */
    
    public void load(Element domElement) {
        NodeList nl = domElement.getElementsByTagName("Fill");
        if (nl.getLength() > 0)
            head_fill.load((Element)nl.item(0));
        
        String size_str = domElement.getAttribute("size");
        try {
          size = Float.parseFloat(size_str);
        } catch (Exception ex) { }
    }
    
    public Node createGraphics(Geometry geo, GlobeSurface globe_surface) {
        Point p = (Point)geo;
        PointMarker pm = new PointMarker(globe_surface.getSurface(),
                Math.toRadians(p.getPosition().getLongitude()),
                Math.toRadians(p.getPosition().getLatitude()));
        if (size <= 0)
            pm.addPin(stem_app, head_fill.app, ViewerManager.getInstance().getCamera(0).getNavigator(), .1f);
        else
            pm.addPin(stem_app, size, size/20, head_fill.app, size/4);
        
        ViewerManager.getInstance().addOriginUpdateListener(pm);
        return pm;
    }
}
