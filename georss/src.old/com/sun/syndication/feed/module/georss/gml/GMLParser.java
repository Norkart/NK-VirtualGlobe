/*
 * Copyright 2006 Marc Wick, geonames.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.sun.syndication.feed.module.georss.gml;

import org.jdom.Element;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.SimpleModuleImpl;
import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
import com.sun.syndication.io.ModuleParser;

import com.sun.syndication.feed.module.georss.geometries.*;

/**
 * GMLParser is a parser for the GML georss format.
 *
 * @author Marc Wick
 * @version $Id: GMLParser.java,v 1.1 2006/04/03 18:46:19 marcwick Exp $
 *
 */
public class GMLParser implements ModuleParser {
    
        /*
         * (non-Javadoc)
         *
         * @see com.sun.syndication.io.ModuleParser#getNamespaceUri()
         */
    public String getNamespaceUri() {
        return GeoRSSModule.GEORSS_GEORSS_URI;
    }
    
        /*
         * (non-Javadoc)
         *
         * @see com.sun.syndication.io.ModuleParser#parse(org.jdom.Element)
         */
    public Module parse(Element element) {
        return parseGML(element);
    }
    
    private static PositionList parsePosList(Element element) {
        String coordinates = element.getText();
        String[] coord = coordinates.trim().split(" ");
        PositionList posList = new PositionList();
        for (int i=0; i<coord.length; i += 2) {
            posList.add(Double.parseDouble(coord[i+1]), Double.parseDouble(coord[i]));
        }
        return posList;
    }
    
    public static Module parseGML(Element element) {
        GeoRSSModule geoRSSModule = null;
        
        Element whereElement = element
                .getChild("where", GeoRSSModule.SIMPLE_NS);
        
        if (whereElement != null) {
            Element pointElement = whereElement.getChild("Point",
                    GeoRSSModule.GML_NS);
            Element lineStringElement = whereElement.getChild("LineString",
                    GeoRSSModule.GML_NS);
            Element polygonElement = whereElement.getChild("Polygon",
                    GeoRSSModule.GML_NS);
            if (pointElement != null) {
                Element posElement = pointElement.getChild("pos", GeoRSSModule.GML_NS);
                if (posElement != null) {
                    geoRSSModule = new GMLModuleImpl();
                    String coordinates = posElement.getText();
                    String[] coord = coordinates.trim().split(" ");
                    Position pos = new Position(Double.parseDouble(coord[1]), Double.parseDouble(coord[0]));
                    geoRSSModule.setGeometry(new Point(pos));
                }
            } else if (lineStringElement != null) {
                Element posListElement = lineStringElement.getChild("posList", GeoRSSModule.GML_NS);
                if (posListElement != null) {
                    geoRSSModule = new GMLModuleImpl();
                    geoRSSModule.setGeometry(new LineString(parsePosList(posListElement)));
                }
            } else if (polygonElement != null) {
                Polygon poly = null;
                Element exteriorElement = polygonElement.getChild("exterior", GeoRSSModule.GML_NS);
                if (exteriorElement != null) {
                    Element linearRingElement = exteriorElement.getChild("LinearRing", GeoRSSModule.GML_NS);
                    if (linearRingElement != null) {
                        Element posListElement = linearRingElement.getChild("posList", GeoRSSModule.GML_NS);
                        if (posListElement != null) {
                            if (poly == null)
                                poly = new Polygon();
                            poly.setExterior(new LinearRing(parsePosList(posListElement)));
                        }
                    }
                }
                // Should also handle interior rings
                if (poly != null) {
                    geoRSSModule = new GMLModuleImpl();
                    geoRSSModule.setGeometry(poly);
                }
            }
        }
        return geoRSSModule;
    }
    
}
