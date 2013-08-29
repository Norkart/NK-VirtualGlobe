/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  LineStyle.java
 *
 * Created on 6. juni 2007, 15:46
 *
 */

package com.norkart.virtualglobe.components.styling;

import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.geopos.Geometry;
import com.norkart.geopos.LineString;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.nodes.GlobeSurfaceLine;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.Shape3D;

import org.w3c.dom.*;

/**
 *
 * @author runaas
 */
public class LineSymbolizer extends Symbolizer {
    private Stroke stroke;
    
    /** Creates a new instance of LineStyle */
    public LineSymbolizer() {
    }
    
    public Node createGraphics(Geometry geo, GlobeSurface globe_surface) {
        if (!(geo instanceof LineString))
            return null;
        
        LineString ls = (LineString)geo;
        GlobeSurfaceLine gsl = new GlobeSurfaceLine(globe_surface, ls, stroke==null?null:stroke.app);
        /*
        Shape3D sh = new Shape3D();
        sh.setGeometry(gsl);
        if (stroke != null)
            sh.setAppearance(stroke.app);
        */
        ViewerManager.getInstance().addOriginUpdateListener(gsl);
        
        return  gsl;
    }
    
    public void    load(Element domElement) throws LoadException {
        NodeList list = domElement.getElementsByTagName("Stroke");
        if (list.getLength() > 0) {
            stroke = new Stroke();
            stroke.load((Element)list.item(0));
        }
    }
    
    public Element save(Document doc) {
        return null;
    }
}
