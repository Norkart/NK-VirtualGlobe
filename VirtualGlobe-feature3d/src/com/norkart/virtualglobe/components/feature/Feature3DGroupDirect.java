//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;

import com.norkart.virtualglobe.viewer.av3d.AV3DPerspectiveCamera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import org.w3c.dom.*;



import org.j3d.aviatrix3d.Group;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;

import com.norkart.virtualglobe.components.FeatureSet;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Feature3DGroupDirect extends Feature3DGroup {
    public final static int MODE_UNDEFINED = -1;
    public final static int MODE_GROUP = 0;
    public final static int MODE_LOD = 1;
    protected int mode = MODE_UNDEFINED;
    
    public Feature3DGroupDirect(Feature3DGroup parent) {
        super(parent);
    }
    
    public Feature3DGroupDirect(FeatureSet featureSet) {
        super(featureSet);
    }
    
    public void load(Element domElement) throws LoadException {
        if (domElement.getNodeName().equals("feature3D-group"))
            mode = MODE_GROUP;
        else if (domElement.getNodeName().equals("feature3D-lod"))
            mode = MODE_LOD;
        else
            throw new LoadException("Invalid element name");
        
        org.j3d.aviatrix3d.Node n = null;
        super.load(domElement);
        if (mode == MODE_GROUP) {
            n = new Group();
        } else if (mode == MODE_LOD) {
            n = new LODGroup(false);
        } else
            throw new IllegalStateException("Impossible mode");
        n.setUserData(this);
        
        // GraphicsCore core = GraphicsCore.getGraphics();
        // AVPerspectiveCamera camera = (AVPerspectiveCamera)core.getAviatrixCamera();
        
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            
            Feature3D f = null;
            if (chEle.getNodeName().equals("feature3D-point")) {
                f = new Feature3DPoint(this);
            } else if (chEle.getNodeName().equals("feature3D-group") ||
                    chEle.getNodeName().equals("feature3D-lod"))
                f = new Feature3DGroupDirect(this);
            else if (chEle.getNodeName().equals("feature3D-external")) {
                f = new Feature3DGroupExternal(this);
            }
            
            if (f == null) continue;
            f.load(chEle);
            
            if (mode == MODE_GROUP) {
                LODGroup lod = new LODGroup(false);
                lod.addChild(f.getNode());
                lod.setRange(0, f.getDetailSize());
                ((Group)n).addChild(lod);
            } else if (mode == MODE_LOD) {
                LODGroup lod = (LODGroup)n;
                lod.addChild(f.getNode());
                lod.setRange(lod.numChildren()-1, f.getDetailSize());
            } else
                throw new IllegalStateException("Impossible mode");
        }
        synchronized (this) {
            node = n;
        }
    }
    
    public Element save(Document doc) {
        Element ele = null;
        switch (mode) {
            case MODE_GROUP: {
                ele = doc.createElement("feature3D-group");
                Group g = (Group)node;
                for (int i = 0; i<g.numChildren(); ++i)
                    saveFeatureRecursive(g.getChild(i), ele, doc);
                break;
            }
            case MODE_LOD: {
                ele = doc.createElement("feature3D-lod");
                LODGroup g = (LODGroup)node;
                for (int i = 0; i<g.numChildren(); ++i)
                    saveFeatureRecursive(g.getChild(i), ele, doc);
                break;
            }
        }
        
        return ele;
    }
}
