//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;


import com.norkart.virtualglobe.viewer.av3d.nodes.PointMarker;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.ViewerManager;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.io.PrintStream;
import java.net.URL;
import java.net.MalformedURLException;

import org.w3c.dom.*;

import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.geom.*;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;
import org.j3d.renderer.aviatrix3d.nodes.Billboard;

import javax.vecmath.Matrix4f;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Graphic3DBillboard extends Graphic3D {
    protected URL imageUrl;
    protected String text;
    protected float stem_height, board_height;
    
    static protected Appearance stem_app = new Appearance();
    
    static {
        Material stem_material = new Material();
        stem_material.setDiffuseColor(new float [] {1,1,1,1});
        stem_material.setLightingEnabled(true);
        stem_app.setMaterial(stem_material);
    }
    
    public Graphic3DBillboard(Feature3DPoint feature) {
        super(feature);
    }
    
    public void load(Element domElement) throws LoadException {
        String board_h_str = domElement.getAttribute("board-height");
        String stem_h_str  = domElement.getAttribute("stem-height");
        stem_height  = Float.parseFloat(stem_h_str);
        board_height =  Float.parseFloat(board_h_str);
        
        text = domElement.getAttribute("text");
        
        String image_url_str = domElement.getAttribute("image");
        if (image_url_str != null && image_url_str.length() > 0) {
            try {
                imageUrl = new URL(feature.getBaseUrl(), image_url_str);
            } catch (MalformedURLException ex) {}
        }
        
        Group group = new Group();
        // Billboard bb =  new Billboard(false);
        final LODGroup lod = new LODGroup(false);
        lod.addChild(group);
        lod.setRange(0, board_height/2);
        
    /*
    Matrix4f stem_mx = new Matrix4f();
    stem_mx.setIdentity();
    stem_mx.setColumn(3, 0, stem_height/2, 0, 1);
    TransformGroup stemTrans = new TransformGroup(stem_mx);
    stemTrans.addChild(new Cylinder(stem_height, stem_height/20));
    bb.addChild(stemTrans);
     */
        group.addChild(PointMarker.createStem(stem_app, stem_height, stem_height/40));
        if (imageUrl != null) {
            try {
                BufferedImage img = javax.imageio.ImageIO.read(imageUrl);
                group.addChild(PointMarker.createBoard(PointMarker.createTexture(img, board_height), board_height, stem_height, false));
            } catch (java.io.IOException ex) { System.err.println("Image read error " + ex);}
        /*
      Texture2D texture = null;
      int width = -1, height = -1;
      int h = 1, w = 1;
      try {
        BufferedImage img = javax.imageio.ImageIO.read(imageUrl);
         
        int comp_format = TextureComponent.FORMAT_RGBA;
        int tex_format  = Texture.FORMAT_RGBA;
        int img_format  = BufferedImage.TYPE_4BYTE_ABGR;
        switch(img.getType()) {
          case BufferedImage.TYPE_3BYTE_BGR:
          case BufferedImage.TYPE_INT_RGB:
            // System.out.println("TD RGB");
            comp_format = TextureComponent.FORMAT_RGB;
            tex_format  = Texture.FORMAT_RGB;
            img_format  = BufferedImage.TYPE_3BYTE_BGR;
            break;
         
          case BufferedImage.TYPE_4BYTE_ABGR:
          case BufferedImage.TYPE_INT_ARGB:
          case BufferedImage.TYPE_CUSTOM:
            // System.out.println("TD RGBA");
            comp_format = TextureComponent.FORMAT_RGBA;
            tex_format  = Texture.FORMAT_RGBA;
            img_format  = BufferedImage.TYPE_4BYTE_ABGR;
            break;
        }
         
        width  = img.getWidth();
        height = img.getHeight();
        while (h < height) { h <<= 1; }
        while (w < width)  { w <<= 1; }
        if (h != height || w != width) {
          BufferedImage new_img = new BufferedImage(w, h, img_format);
          ((Graphics2D)new_img.getGraphics()).drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
          img = new_img;
        }
         
        ImageTextureComponent2D img_comp = new ImageTextureComponent2D(comp_format, img);
        texture = new Texture2D();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                           tex_format,
                           new TextureComponent[] { img_comp },
                           1);
      }
      catch (java.io.IOException ex) { System.err.println("Image read error " + ex);}
      float board_width = board_height * width / height;
      TriangleStripArray tsa = new TriangleStripArray();
      tsa.setVertices(TriangleStripArray.COORDINATE_2, new float [] {
        -board_width/2, stem_height,
        board_width/2, stem_height,
            -board_width/2, stem_height + board_height,
        board_width/2, stem_height + board_height
      });
        tsa.setStripCount(new int [] {4}, 1);
        float fh = ((float)height)/h;
        float fw = ((float)width)/w;
        tsa.setTextureCoordinates(new int [] {TriangleStripArray.TEXTURE_COORDINATE_2}, new float [][] {{
          0,  1-fh,
          fw, 1-fh,
          0,  1,
          fw, 1
        }}, 1);
         
         
        TextureUnit tu = new TextureUnit();
        tu.setTexture(texture);
         
        Appearance app = new Appearance();
        app.setTextureUnits(new TextureUnit [] {tu}, 1);
        Shape3D s3d = new Shape3D();
        s3d.setGeometry(tsa);
        s3d.setAppearance(app);
         
        bb.addChild(s3d);
         */
        } else if (text != null) {
            String[] text_arr = text.split("\\\\n");
            
            group.addChild(PointMarker.createBoard(PointMarker.createText(text_arr, null, board_height), text_arr.length*board_height, stem_height, false));
            /*
            Text2D t_node = new Text2D();
            t_node.setText(text_arr, text_arr.length);
            t_node.setHorizontalJustification(Text2D.JUSTIFY_MIDDLE);
            t_node.setSize(board_height);
            Shape3D s3d = new Shape3D();
            s3d.setGeometry(t_node);
             
            Matrix4f board_mx = new Matrix4f();
            board_mx.setIdentity();
            board_mx.setColumn(3, 0, stem_height + board_height*text_arr.length, 0, 1);
            TransformGroup boardTrans = new TransformGroup(board_mx);
            boardTrans.addChild(s3d);
            bb.addChild(boardTrans);
             */
        }
        
        if (feature.getNode().isLive()) {
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(feature.getNode(), new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.addChild(lod);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            ((Group)feature.getNode()).addChild(lod);
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement("billboard");
        ele.setAttribute("board-height", String.valueOf(board_height));
        ele.setAttribute("stem-height", String.valueOf(stem_height));
        if (imageUrl != null)
            ele.setAttribute("image", imageUrl.toString());
        else if (text != null)
            ele.setAttribute("text", text);
        return ele;
    }
}
 /*
              Material material = new Material();
              material.setTransparency(0.5f);
  
              app.setMaterial(material);
  */

     /*
              Matrix4f board_mx = new Matrix4f();
              board_mx.setIdentity();
          board_mx.setColumn(3, 0, 0, stem_height/2, 1);
          TransformGroup boardTrans = new TransformGroup(stem_mx);
      */
 /*
          Text2D t2d = new Text2D();
          String[] tarr = {text};
          t2d.setText(tarr, 0);
         // t2d.setSize(board_height);
          t2d.setHorizontalJustification(Text2D.JUSTIFY_MIDDLE);
          Shape3D s3d = new Shape3D();
          s3d.setGeometry(t2d);
          s3d.setAppearance(new Appearance());
          boardTrans.addChild(s3d);
  */