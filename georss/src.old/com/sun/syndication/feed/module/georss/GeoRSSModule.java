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
package com.sun.syndication.feed.module.georss;

import org.jdom.Namespace;

import com.sun.syndication.feed.module.ModuleImpl;
import com.sun.syndication.feed.module.georss.geometries.AbstractGeometry;

/**
 * GeoRSSModule is the main georss interface defining the methods to produce and
 * consume georss elements.
 * 
 * @author Marc Wick
 * @version $Id: GeoRSSModule.java,v 1.5 2006/10/02 20:21:52 marcwick Exp $
 */
public abstract class GeoRSSModule extends ModuleImpl {
        protected AbstractGeometry geometry;
    
	/**
	 * namespace URI for georss simple: <i>"http://www.georss.org/georss"</i>
	 */
	public static final String GEORSS_GEORSS_URI = "http://www.georss.org/georss";

	/**
	 * namespace URI for w3c georss :
	 * <i>"http://www.w3.org/2003/01/geo/wgs84_pos#"</i>
	 */
	public static final String GEORSS_W3CGEO_URI = "http://www.w3.org/2003/01/geo/wgs84_pos#";

	/**
	 * namespace URI for GML georss : <i>"http://www.opengis.net/gml"</i>
	 */
	public static final String GEORSS_GML_URI = "http://www.opengis.net/gml";

	/**
	 * Namespace for georss simple :
	 * <i>xmlns:georss="http://www.georss.org/georss"</i>
	 */
	public static final Namespace SIMPLE_NS = Namespace.getNamespace("georss",
			GeoRSSModule.GEORSS_GEORSS_URI);

	/**
	 * 
	 * Namespace for w3c georss :
	 * <i>xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"</i>
	 */
	public static final Namespace W3CGEO_NS = Namespace.getNamespace("geo",
			GeoRSSModule.GEORSS_W3CGEO_URI);

	/**
	 * 
	 * Namespace for gml georss : <i>xmlns:gml="http://www.opengis.net/gml"</i>
	 */
	public static final Namespace GML_NS = Namespace.getNamespace("gml",
			GeoRSSModule.GEORSS_GML_URI);

        protected 	GeoRSSModule(java.lang.Class beanClass, java.lang.String uri) {
            super(beanClass, uri);
        }
        
	/**
	 * Set geometry of georss element
	 * 
	 * @param geometry
	 *            geometry
	 */
	public void setGeometry(AbstractGeometry geometry) {
            this.geometry = geometry;
        }

	
	/**
	 * returns the latitude
	 * 
	 * @return latitude
	 */
	public AbstractGeometry getGeometry() {
            return geometry;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.sun.syndication.feed.CopyFrom#copyFrom(java.lang.Object)
         */
        public void copyFrom(Object obj) {
            GeoRSSModule geoRSSModule = (GeoRSSModule) obj;
            geometry = geoRSSModule.getGeometry();
            try {
                geometry = (AbstractGeometry)geometry.clone();
            } 
            catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
}
