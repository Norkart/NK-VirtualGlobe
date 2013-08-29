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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.io.ModuleGenerator;

import com.sun.syndication.feed.module.georss.geometries.*;

/**
 * GMLGenerator produces georss elements in georss GML format.
 * 
 * @author Marc Wick
 * @version $Id: GMLGenerator.java,v 1.2 2006/09/01 07:09:18 marcwick Exp $
 * 
 */
public class GMLGenerator implements ModuleGenerator {

	private static final Set NAMESPACES;

	static {
		Set nss = new HashSet();
		nss.add(GeoRSSModule.GML_NS);
		NAMESPACES = Collections.unmodifiableSet(nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.syndication.io.ModuleGenerator#getNamespaceUri()
	 */
	public String getNamespaceUri() {
		return GeoRSSModule.GEORSS_GML_URI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.syndication.io.ModuleGenerator#getNamespaces()
	 */
	public Set getNamespaces() {
		return NAMESPACES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.syndication.io.ModuleGenerator#generate(com.sun.syndication.feed.module.Module,
	 *      org.jdom.Element)
	 */
	public void generate(Module module, Element element) {
		// this is not necessary, it is done to avoid the namespace definition
		// in every item.
		Element root = element;
		while (root.getParent() != null && root.getParent() instanceof Element) {
			root = (Element) element.getParent();
		}
		root.addNamespaceDeclaration(GeoRSSModule.SIMPLE_NS);
		root.addNamespaceDeclaration(GeoRSSModule.GML_NS);

		Element whereElement= new Element("where", GeoRSSModule.SIMPLE_NS);
		element.addContent(whereElement);

		GeoRSSModule geoRSSModule = (GeoRSSModule) module;
                AbstractGeometry geometry = geoRSSModule.getGeometry();
        
                if (geometry instanceof Point) {
                    Position pos = ((Point)geometry).getPosition();
            
                    Element pointElement = new Element("Point", GeoRSSModule.GML_NS);
                    whereElement.addContent(pointElement);
                    
                    Element posElement = new Element("pos", GeoRSSModule.GML_NS);    
                
                    posElement.addContent(String.valueOf(pos.getLatitude()) + " "
				+ String.valueOf(pos.getLongitude()));
                    pointElement.addContent(posElement);
                }
	}
}
