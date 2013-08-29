/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Mark.java
 *
 * Created on 8. juni 2007, 10:47
 *
 */

package com.norkart.virtualglobe.components.styling;

import com.norkart.virtualglobe.components.GlobeSurface;
import org.j3d.aviatrix3d.Node;
import com.norkart.geopos.Geometry;
import org.w3c.dom.Element;

/**
 *
 * @author runaas
 */
public abstract class Mark {
    
    /** Creates a new instance of Mark */
    public Mark() {
    }
    
    public abstract Node createGraphics(Geometry geo, GlobeSurface globe_surface);
    abstract void load(Element domElement);
}
