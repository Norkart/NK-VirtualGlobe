/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PointStyle.java
 *
 * Created on 6. juni 2007, 15:46
 *
 */

package com.norkart.virtualglobe.components.styling;

import java.util.ArrayList;

import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.geopos.Geometry;
import com.norkart.geopos.Point;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.Group;

import org.w3c.dom.*;

/**
 *
 * @author runaas
 */
public class PointSymbolizer extends Symbolizer {
    private ArrayList<Graphic> graphics = new ArrayList();
    
    private class Graphic {
        Mark mark;
        
        
        void load(Element domElement) {
            for (org.w3c.dom.Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if (!(ch instanceof Element))
                    continue;
                Element chEle = (Element)ch;
                if (chEle.getTagName().equals("PinMark")) {
                    mark = new PinMark();
                    mark.load(chEle);
                    break;
                }
            }
            if (mark == null)
                mark = new PinMark();
        }
    }
    
    /** Creates a new instance of PointStyle */
    public PointSymbolizer() {
    }
    
    public void    load(Element domElement) throws LoadException {
        NodeList gElements = domElement.getElementsByTagName("Graphic");
        for (int i = 0; i < gElements.getLength(); ++i) {
            Graphic g = new Graphic();
            g.load((Element)gElements.item(i));
            graphics.add(g);
        }
    }
    
    public Element save(Document doc) {
        return null;
    }
    public Node createGraphics(Geometry geo, GlobeSurface globe_surface) {
         if (!(geo instanceof Point))
            return null;
        
        Group group = null;
        for (Graphic g : graphics) {
            Node n = g.mark.createGraphics(geo, globe_surface);
            if (n != null) {
                if (group == null)
                    group = new Group();
                group.addChild(n);
            }
        }
        return group;
    }
}
