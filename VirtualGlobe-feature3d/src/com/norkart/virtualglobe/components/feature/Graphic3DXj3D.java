//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;



import com.norkart.virtualglobe.viewer.av3d.nodes.AutoLoadNode;
import com.norkart.virtualglobe.viewer.av3d.nodes.Web3DAutoLoader;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import org.w3c.dom.*;

import java.net.URL;
import java.net.MalformedURLException;

import javax.vecmath.*;

import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.loader.AVModel;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Graphic3DXj3D extends Graphic3D implements NodeUpdateListener {
    // static protected Xj3DLoader xj3dLoader = null;
    
    static protected Web3DAutoLoader auto_loader = null;
    
    protected URL vrml_url = null;
    protected Group model_root;
    protected float [] min = {-100, -100, -100};
    protected float [] max = { 100,  100,  100};
    protected Matrix4f transform = null;
    
    public void updateNodeBoundsChanges(Object o) {
        Group n = (Group)o;
        n.addChild(model_root);
    }
    public void updateNodeDataChanges(Object o) {}
    
    /*
    private class Xj3DRequest extends Xj3DLoader.Xj3DRequest {
        public Xj3DRequest(URL url) {
            super(url);
        }
     
        public void setModel(AVModel model) {
            super.setModel(model);
            model_root = model.getModelRoot();
            if (feature.getNode().isLive()) {
                GraphicsCore core = GraphicsCore.getGraphics();
                AVPerspectiveCamera camera = (AVPerspectiveCamera)core.getAviatrixCamera();
                camera.updateNode(feature.getNode(), Graphic3DXj3D.this, AVPerspectiveCamera.UPDATE_BOUNDS);
            } else
                ((Group)feature.getNode()).addChild(model_root);
        }
    }
     
    protected Xj3DRequest xj3dRequest = null;
     */
    
    public Graphic3DXj3D(Feature3DPoint feature) {
        super(feature);
        
        if (auto_loader == null) {
            auto_loader = new Web3DAutoLoader(feature.getFeatureSet().getUniverse().getCacheManager());
            ((AV3DViewerManager)ViewerManager.getInstance()).addApplicationUpdateObserver(new ApplicationUpdateObserver() {
                public void	appShutdown() {
                    auto_loader.shutdown();
                }
                public void updateSceneGraph() {
                    auto_loader.executeModelBehavior();
                }
            });
        }
        /*
        if (xj3dLoader == null) {
         
            xj3dLoader = new Xj3DLoader(feature.featureSet.getUniverse().getCacheManager());
            camera.addApplicationUpdateObserver(xj3dLoader);
        }
         **/
    }
    
    
    public void load(Element domElement) throws LoadException {
        String vrml_url_str = domElement.getAttribute("href");
        
        try {
            vrml_url = new URL(feature.getBaseUrl(), vrml_url_str);
        } catch (MalformedURLException ex) {}
        if (vrml_url == null)
            return;
            /*
            xj3dRequest = new Xj3DRequest(vrml_url);
            xj3dLoader.requestLoad(xj3dRequest);
             */
        String transformStr = domElement.getAttribute("transform");
        if (transformStr != null && !transformStr.equals("")) {
            String[] tokens = transformStr.split(",");
            if (tokens.length == 12) {
                transform = new Matrix4f();
                transform.m00 = Float.parseFloat(tokens[0]);
                transform.m01 = Float.parseFloat(tokens[1]);
                transform.m02 = Float.parseFloat(tokens[2]);
                transform.m03 = Float.parseFloat(tokens[3]);
                transform.m10 = Float.parseFloat(tokens[4]);
                transform.m11 = Float.parseFloat(tokens[5]);
                transform.m12 = Float.parseFloat(tokens[6]);
                transform.m13 = Float.parseFloat(tokens[7]);
                transform.m20 = Float.parseFloat(tokens[8]);
                transform.m21 = Float.parseFloat(tokens[9]);
                transform.m22 = Float.parseFloat(tokens[10]);
                transform.m23 = Float.parseFloat(tokens[11]);
                transform.m30 = 0;
                transform.m31 = 0;
                transform.m32 = 0;
                transform.m33 = 1;
            }
        }
        
        
        String bboxStr = domElement.getAttribute("bbox");
        if (bboxStr != null && !bboxStr.equals("")) {
            String[] tokens = bboxStr.split(",");
            if (tokens.length == 6) {
                min[0] = Float.parseFloat(tokens[0]);
                min[1] = Float.parseFloat(tokens[1]);
                min[2] = Float.parseFloat(tokens[2]);
                max[0] = Float.parseFloat(tokens[3]);
                max[1] = Float.parseFloat(tokens[4]);
                max[2] = Float.parseFloat(tokens[5]);
            }
        }
        
        
        
        final AutoLoadNode al_node = new AutoLoadNode(auto_loader.createNodeLoader(vrml_url, new BoundingBox(min, max), transform));
        
        if (feature.getNode().isLive()) {
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(feature.getNode(), new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.addChild(al_node);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            ((Group)feature.getNode()).addChild(al_node);
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement("vrml-model");
        ele.setAttribute("href", vrml_url.toString());
        ele.setAttribute("bbox", String.valueOf(min[0])+","+String.valueOf(min[1])+","+String.valueOf(min[2])+","+
                String.valueOf(max[0])+","+String.valueOf(max[1])+","+String.valueOf(max[2]));
        if (transform != null) {
            String transformStr =
                    Float.toString(transform.m00) + "," +
                    Float.toString(transform.m01) + "," +
                    Float.toString(transform.m02) + "," +
                    Float.toString(transform.m03) + "," +
                    Float.toString(transform.m10) + "," +
                    Float.toString(transform.m11) + "," +
                    Float.toString(transform.m12) + "," +
                    Float.toString(transform.m13) + "," +
                    Float.toString(transform.m20) + "," +
                    Float.toString(transform.m21) + "," +
                    Float.toString(transform.m22) + "," +
                    Float.toString(transform.m23);
            ele.setAttribute("transform", transformStr);
        }
        
        return ele;
    }
}