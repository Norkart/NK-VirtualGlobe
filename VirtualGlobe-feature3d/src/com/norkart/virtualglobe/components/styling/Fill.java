/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  Fill.java
 *
 * Created on 8. juni 2007, 11:47
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
public class Fill {
    Appearance app = new Appearance();
    
    /** Creates a new instance of Fill */
    public Fill() {
        Material material = new Material();
        material.setLightingEnabled(true);
        app.setMaterial(material);
    }
    
    private void addParameter(String name, String value) {
        if (value != null)
            value = value.trim();
        if (name.equals("fill")) {
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
                    material.setDiffuseColor(c.getRGBComponents(null));
                    app.setMaterial(material);
                }
            }
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
