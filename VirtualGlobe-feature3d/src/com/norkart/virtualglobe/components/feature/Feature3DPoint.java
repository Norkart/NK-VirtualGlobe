//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;

import org.w3c.dom.*;

import org.j3d.aviatrix3d.Group;

import com.norkart.virtualglobe.viewer.ViewerManager;

import com.norkart.virtualglobe.viewer.av3d.nodes.GlobeSurfaceGroup;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Feature3DPoint extends Feature3D {
  protected double lon, lat, h, az;
  protected GlobeSurfaceGroup surf_trans;
  protected int vert_ref;
  protected Graphic3D graphic;

  public Feature3DPoint(Feature3DGroup parent) {
    super(parent);
  }

  public void load(Element domElement) throws LoadException {
    if (!domElement.getNodeName().equals("feature3D-point"))
      throw new LoadException("Unknown node name");

    super.load(domElement);

    // Position
    String lonStr = domElement.getAttribute("lon");
    String latStr = domElement.getAttribute("lat");
    String hStr   = domElement.getAttribute("h");
    String vert_ref_str = domElement.getAttribute("vert-ref");
    String azStr  = domElement.getAttribute("azimuth");
    lon = Double.parseDouble(lonStr);
    lat = Double.parseDouble(latStr);
    h = 0;
    if (hStr != null && !hStr.equals(""))
      h = Double.parseDouble(hStr);
    vert_ref = GlobeSurfaceGroup.VERT_REF_TERRAIN;
    if (vert_ref_str == null);
    else if (vert_ref_str.equalsIgnoreCase("sea-level"))
      vert_ref = GlobeSurfaceGroup.VERT_REF_SEA;
    else if (vert_ref_str.equalsIgnoreCase("terrain"))
      vert_ref = GlobeSurfaceGroup.VERT_REF_TERRAIN;
    az = 0;
    if (azStr != null && !azStr.equals(""))
      az = Math.toRadians(Double.parseDouble(azStr));

    surf_trans = new GlobeSurfaceGroup(featureSet.getGlobe().getSurface(), Math.toRadians(lon), Math.toRadians(lat), h, vert_ref, az);
    ViewerManager.getInstance().addOriginUpdateListener(surf_trans);
    surf_trans.setUserData(this);

    for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
      if (!(ch instanceof Element)) continue;
      Element chEle = (Element)ch;

      if (chEle.getNodeName().equals("vrml-model")) {
        graphic = new Graphic3DXj3D(this);
      }
      else if (chEle.getNodeName().equals("billboard")) {
        graphic = new Graphic3DBillboard(this);
      }
      if (graphic != null) {
        graphic.load(chEle);
        break;
      }
    }
  }
  
  public Element save(Document doc) {
      Element ele = doc.createElement("feature3D-point");
      ele.setAttribute("lon", String.valueOf(lon));
      ele.setAttribute("lat", String.valueOf(lat));
      ele.setAttribute("h", String.valueOf(h));
      ele.setAttribute("azimuth", String.valueOf(Math.toDegrees(az)));
      if (vert_ref == GlobeSurfaceGroup.VERT_REF_SEA)
          ele.setAttribute("vert-ref", "sea-level");
      else if (vert_ref == GlobeSurfaceGroup.VERT_REF_TERRAIN)
          ele.setAttribute("vert-ref", "terrain");
      ele.appendChild(graphic.save(doc));
      return ele;
  }

  public org.j3d.aviatrix3d.Node getNode() {
    return surf_trans;
  }
}
