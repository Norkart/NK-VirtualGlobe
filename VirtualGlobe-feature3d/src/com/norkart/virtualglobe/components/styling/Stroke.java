/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Stroke.java
 *
 * Created on 7. juni 2007, 11:05
 *
 */

package com.norkart.virtualglobe.components.styling;

import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.Material;
import org.j3d.aviatrix3d.LineAttributes;

import org.w3c.dom.*;
import java.awt.Color;

/**
 *
 * @author runaas
 */
public class Stroke {
    Appearance app = new Appearance();
    
    /** Creates a new instance of Stroke */
    public Stroke() {
    }
    
    private void addParameter(String name, String value) {
        if (value != null)
            value = value.trim();
        if (name.equals("stroke")) {
            if (value != null && value.length() > 0) {
                Color c = Color.getColor(value);
                if (c == null) {
                    if (value.charAt(0) == '#')
                        value = value.substring(1);
                    int ci = Integer.parseInt(value, 16);
                    c = new Color(ci);
                }
                
                if (c != null) {
                    Material material = app.getMaterial();
                    if (material == null)
                        material = new Material();
                    material.setDiffuseColor(c.getRGBComponents(null));
                    material.setLightingEnabled(false);
                    // material.setColorMaterialEnabled(true);
                    // material.setSeparateBackfaceEnabled(true);
                    app.setMaterial(material);
                }
            }
        } else if (name.equals("stroke-width")) {
            float width = Float.parseFloat(value);
            LineAttributes l_att = app.getLineAttributes();
            if (l_att == null) l_att = new LineAttributes();
            l_att.setLineWidth(width);
            app.setLineAttributes(l_att);
        }
    }
    
    public void load(Element domElement) {
        NodeList ch_list = domElement.getElementsByTagName("OGLParameter");
        for (int i=0; i<ch_list.getLength(); ++i) {
            Element chEle = (Element)ch_list.item(i);
            addParameter(chEle.getAttribute("name"), chEle.getTextContent());
        }
    }
}
