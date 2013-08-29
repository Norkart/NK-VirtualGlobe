/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Style.java
 *
 * Created on 6. juni 2007, 13:13
 *
 */

package com.norkart.virtualglobe.components.styling;

import com.norkart.virtualglobe.components.GlobeSurface;

import com.norkart.geopos.Geometry;
import com.norkart.virtualglobe.components.DomLoadable;
import org.j3d.aviatrix3d.Node;


/**
 *
 * @author runaas
 */
public abstract class Symbolizer implements DomLoadable {
    
    /** Creates a new instance of Style */
    public Symbolizer() {
    }
    
    public abstract Node createGraphics(Geometry geo, GlobeSurface globe_surface);
}
