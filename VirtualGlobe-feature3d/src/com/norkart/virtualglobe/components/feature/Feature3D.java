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
import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.components.DomLoadable;
import com.norkart.virtualglobe.components.FeatureSet;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class Feature3D implements DomLoadable {
    protected FeatureSet featureSet;
    protected Feature3DGroup parent;
    protected String info;
    protected float detail_size = Float.MAX_VALUE;
    protected URL baseUrl;
    
    public Feature3D(Feature3DGroup parent) {
        this.parent = parent;
        this.featureSet = parent.featureSet;
        baseUrl = parent.baseUrl;
    }
    
    public Feature3D(FeatureSet featureSet) {
        parent = null;
        this.featureSet = featureSet;
        baseUrl = featureSet.getBaseUrl();
    }
    
    public FeatureSet getFeatureSet() {
        return featureSet;
    }
    
    public void load(Element domElement) throws LoadException {
        String detailStr = domElement.getAttribute("detail-size");
        if (detailStr != null && !detailStr.equals("")) {
            detail_size = Float.parseFloat(detailStr);
        }
        
        if (parent != null)
            baseUrl = parent.getBaseUrl();
        else
            baseUrl = featureSet.getBaseUrl();
        String baseUrlStr = domElement.getAttribute("base-url");
        if (baseUrlStr != null && baseUrlStr.length() > 0) {
            try {
                if (baseUrl != null)
                    baseUrl = new URL(baseUrl, baseUrlStr);
                else
                    baseUrl = new URL(baseUrlStr);
            } catch (MalformedURLException ex) {
                System.err.println(ex);
            }
        }
        
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("info")) {
                chEle.normalize();
                for (Node ch2 = chEle.getFirstChild(); ch2 != null; ch2 = ch2.getNextSibling()) {
                    if (ch2.getNodeType() == Node.CDATA_SECTION_NODE) {
                        info = ch2.getNodeValue();
                        break;
                    }
                    if (ch2.getNodeType() == Node.TEXT_NODE) {
                        if (info == null)
                            info = ch2.getNodeValue();
                        else
                            info += ch2.getNodeValue();
                    }
                }
            }
        }
    /*
    // Info URL
    String info_url_str = chEle.getAttribute("info-url");
    URL infoUrl = null;
    if (info_url_str != null && info_url_str.length() > 0) {
      try {
        infoUrl = new URL(info_url_str);
      }
      catch (MalformedURLException ex) {}
    }
     */
    }
    
    // public abstract Element save(Document doc);
    
    static public void saveFeatureRecursive(org.j3d.aviatrix3d.Node node, Element parent_ele, Document doc) {
        Object o = node.getUserData();
        if (o instanceof Feature3D) {
            Element ele = ((Feature3D)o).save(doc);
            if (ele != null)
                parent_ele.appendChild(ele);
        } else if (node instanceof org.j3d.aviatrix3d.BaseGroup) {
            org.j3d.aviatrix3d.BaseGroup g = (org.j3d.aviatrix3d.BaseGroup)node;
            for (int i = 0; i<g.numChildren(); ++i)
                saveFeatureRecursive(g.getChild(i), parent_ele, doc);
        }
        else if (node instanceof org.j3d.aviatrix3d.Group) {
            org.j3d.aviatrix3d.Group g = (org.j3d.aviatrix3d.Group)node;
            for (int i = 0; i<g.numChildren(); ++i)
                saveFeatureRecursive(g.getChild(i), parent_ele, doc);
        }
        else if (node instanceof org.j3d.aviatrix3d.SharedNode) {
            org.j3d.aviatrix3d.SharedNode g = (org.j3d.aviatrix3d.SharedNode)node;
            saveFeatureRecursive(g.getChild(), parent_ele, doc);
        }
    }
    
    
    public float getDetailSize() {
        return detail_size;
    }
    
    public String getInfo() {
        return info;
    }
    
    public URL getBaseUrl() {
        return baseUrl;
    }
    
    public CacheManager getCacheManager() {
        return featureSet.getCacheManager();
    }
    
    public abstract org.j3d.aviatrix3d.Node getNode();
}