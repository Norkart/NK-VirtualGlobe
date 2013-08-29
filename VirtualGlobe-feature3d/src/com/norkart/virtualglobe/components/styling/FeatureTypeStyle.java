/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Styles.java
 *
 * Created on 6. juni 2007, 13:11
 *
 */

package com.norkart.virtualglobe.components.styling;

import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.geopos.Point;
import com.norkart.geopos.Geometry;

import com.norkart.virtualglobe.components.DomLoadable;
import com.norkart.virtualglobe.components.GlobeSurface;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

/**
 *
 * @author runaas
 */
public class FeatureTypeStyle implements DomLoadable {
    private ArrayList<Rule> rules = new ArrayList();
    
    private class Rule {
        private ArrayList<Symbolizer> symbolizers = new ArrayList();
        
        void load(Element domElement) throws  LoadException {
            for (org.w3c.dom.Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if (!(ch instanceof Element))
                    continue;
                Element chEle = (Element)ch;
                if (chEle.getTagName().equals("PointSymbolizer")) {
                    PointSymbolizer symbolizer = new PointSymbolizer();
                    symbolizer.load(chEle);
                    symbolizers.add(symbolizer);
                } else if (chEle.getTagName().equals("LineSymbolizer")) {
                    LineSymbolizer symbolizer = new LineSymbolizer();
                    symbolizer.load(chEle);
                    symbolizers.add(symbolizer);
                }
            }
            
        }
    }
    
    
    /** Creates a new instance of Styles */
    public FeatureTypeStyle() {
    }
    
    public void    load(Element domElement) throws LoadException {
        if (!domElement.getTagName().equals("FeatureTypeStyle"))
            throw new LoadException("Invalid element name");
        
        NodeList ruleElements = domElement.getElementsByTagName("Rule");
        for (int i = 0; i < ruleElements.getLength(); ++i) {
            Rule rule = new Rule();
            rule.load((Element)ruleElements.item(i));
            rules.add(rule);
        }
    }
    
    
    public Element save(Document doc) {
        return null;
    }
    
    public Node createGraphics(Geometry geo, GlobeSurface globe_surface) {
        Group group = null;
        for (Rule r : rules) {
            for (Symbolizer s : r.symbolizers) {
                Node n = s.createGraphics(geo, globe_surface);
                if (n != null) {
                    if (group == null)
                        group = new Group();
                    group.addChild(n);
                }
            }
        }
        return group;
    }
}
