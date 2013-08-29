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

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.vecmath.Point3d;

import org.j3d.aviatrix3d.BoundingBox;
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.NodeUpdateListener;

import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;
import com.norkart.virtualglobe.viewer.ViewerManager;

import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DPerspectiveCamera;
import com.norkart.virtualglobe.viewer.av3d.nodes.NodeLoader;
import com.norkart.virtualglobe.viewer.av3d.nodes.AutoLoadNode;

import com.norkart.virtualglobe.components.FeatureSet;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Feature3DGroupExternal extends Feature3DGroup  implements
    NodeLoader,  NodeUpdateListener, OriginUpdateListener
{
  protected URL                  external_url;
  protected Feature3DGroupDirect external_child;

  protected double min_lon, min_lat, min_h, max_lon, max_lat, max_h;

  protected Point3d origin = new Point3d();
  protected BoundingVolume bounds = null;
  
  protected Ellipsoid ellps = null;

  public Feature3DGroupExternal(Feature3DGroup parent) {
    super(parent);
    ellps = this.featureSet.getGlobe().getSurface().getEllipsoid();
  }

  public Feature3DGroupExternal(FeatureSet featureSet) {
    super(featureSet);
    ellps = this.featureSet.getGlobe().getSurface().getEllipsoid();
  }

  public void load(Element domElement) throws LoadException {
    super.load(domElement);

    String bboxStr = domElement.getAttribute("bbox");
    boolean no_bounds = true;
    if (bboxStr != null && !bboxStr.equals("")) {
      String[] tokens = bboxStr.split(",");
      if (tokens.length == 6) {
        min_lon = Math.toRadians(Double.parseDouble(tokens[0]));
        min_lat = Math.toRadians(Double.parseDouble(tokens[1]));
        min_h   = Double.parseDouble(tokens[2]);
        max_lon = Math.toRadians(Double.parseDouble(tokens[3]));
        max_lat = Math.toRadians(Double.parseDouble(tokens[4]));
        max_h   = Double.parseDouble(tokens[5]);
        no_bounds = false;
      }
    }
    if (no_bounds) {
      throw new LoadException("Invalid boundingbox");
    }

    String urlStr = domElement.getAttribute("href");
    if (urlStr != null) {
      try {
        external_url = new URL(baseUrl, urlStr);
      }
      catch (MalformedURLException ex) {
        System.err.println("Unable to create URL: " + ex.getMessage());
      }
    }
    
    synchronized (this) {
        node = new AutoLoadNode(this);
        node.setUserData(this);
    }
    ViewerManager.getInstance().addOriginUpdateListener(this);

    //    requestLoad();
  }

  public Element save(Document doc) {
      Element ele = doc.createElement("feature3D-external");
      ele.setAttribute("href", external_url.toString());
      ele.setAttribute("bbox", 
              String.valueOf(Math.toDegrees(min_lon))+","+String.valueOf(Math.toDegrees(min_lat))+","+String.valueOf(min_h)+","+
              String.valueOf(Math.toDegrees(max_lon))+","+String.valueOf(Math.toDegrees(max_lat))+","+String.valueOf(max_h));
      return ele;
  }
  
  public void requestLoad() {
    if (external_child != null || external_url == null) return;
    external_child = new Feature3DGroupDirect(this);
    try {
      featureSet.getUniverse().getApplicationLoader().requestLoading(external_child, external_url, true);
    }
    catch (IOException ex) {
      System.err.println("Unable load from URL: " + external_url + " because of : " + ex.getMessage());
    }
  }

  public org.j3d.aviatrix3d.Node takeNode() {
    if (external_child == null)
      return null;
    org.j3d.aviatrix3d.Node ch = external_child.getNode();
    if (ch != null)
      external_child = null;
    return ch;
  }
  //-------------------------------------------------------
   // NodeUpdateListener methods
   //-------------------------------------------------------
   public void updateNodeBoundsChanges(java.lang.Object src) {
    // System.err.println("FeatureGroupExternal origin: " + origin);
   }

   public void	updateNodeDataChanges(java.lang.Object src) {}

   //-------------------------------------------------------
   // OriginUpdateListener methods
   //-------------------------------------------------------
   public void updateOrigin(Point3d origin) {
     this.origin.set(origin);
     bounds = null;
     if (node.isLive())
         ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(node, this, AV3DViewerManager.UPDATE_BOUNDS);
   }

   public boolean requestUpdateOrigin(Point3d origin) {
     return true;
   }

  public BoundingVolume getBounds() {
      if (bounds != null)
          return bounds;
      
    float min_x    = Float.MAX_VALUE;
    float min_y    = Float.MAX_VALUE;
    float min_z    = Float.MAX_VALUE;
    float max_x    = -Float.MAX_VALUE;
    float max_y    = -Float.MAX_VALUE;
    float max_z    = -Float.MAX_VALUE;
    Point3d p = new Point3d();
    

    ellps.toCartesian(min_lat, min_lon, min_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(min_lat, min_lon, max_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(min_lat, max_lon, min_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(min_lat, max_lon, max_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(max_lat, min_lon, min_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(max_lat, min_lon, max_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(max_lat, max_lon, min_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    ellps.toCartesian(max_lat, max_lon, max_h, p);
    p.sub(origin);
    if (p.x < min_x) min_x = (float)p.x;
    if (p.y < min_y) min_y = (float)p.y;
    if (p.z < min_z) min_z = (float)p.z;
    if (p.x > max_x) max_x = (float)p.x;
    if (p.y > max_y) max_y = (float)p.y;
    if (p.z > max_z) max_z = (float)p.z;

    BoundingBox bbox = new BoundingBox();
    bbox.setMinimum(min_x, min_y, min_z);
    bbox.setMaximum(max_x, max_y, max_z);
    bounds = bbox;
    return bbox;
  }
}