/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PointMarker.java
 *
 * Created on 1. juni 2007, 16:40
 *
 */

package com.norkart.virtualglobe.viewer.av3d.nodes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.geom.*;
import org.j3d.renderer.aviatrix3d.nodes.Billboard;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;

import javax.vecmath.*;

import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

/**
 *
 * @author runaas
 */
public class PointMarker extends GlobeSurfaceGroup {
    
    public final static Appearance WHITE_APPEARANCE = new Appearance();
    public final static Appearance RED_APPEARANCE   = new Appearance();
    public final static Appearance GREEN_APPEARANCE = new Appearance();
    public final static Appearance BLUE_APPEARANCE  = new Appearance();
    
    static {
        Material material = new Material();
        material.setLightingEnabled(true);
        material.setDiffuseColor(new float [] {1,1,1,1});
        WHITE_APPEARANCE.setMaterial(material);
        
        material = new Material();
        material.setDiffuseColor(new float [] {1,0,0,1});
        material.setLightingEnabled(true);
        RED_APPEARANCE.setMaterial(material);
        
        material = new Material();
        material.setDiffuseColor(new float [] {0,1,0,1});
        material.setLightingEnabled(true);
        GREEN_APPEARANCE.setMaterial(material);
        
        material = new Material();
        material.setDiffuseColor(new float [] {0,0,1,1});
        material.setLightingEnabled(true);
        BLUE_APPEARANCE.setMaterial(material);
    }
    
    
    /** Creates a new instance of PointMarker */
    public PointMarker(GlobeElevationModel globe, double lon, double lat,  double h, int vert_ref, double az) {
        super(globe, lon, lat,  h, vert_ref, az);
    }
    
    public PointMarker(GlobeElevationModel globe, double lon, double lat) {
        super(globe, lon, lat,  0, GlobeSurfaceGroup.VERT_REF_TERRAIN, 0);
    }
    
    
    public void addPin(Appearance stem_app, float stem_height, float stem_diameter, Appearance head_app, float head_diameter) {
        addChild(createStem(stem_app, stem_height, stem_diameter));
        addChild(createPinhead(head_app, stem_height, head_diameter));
    }
    
    public void addPin(Appearance stem_app, Appearance head_app,  GlobeNavigator navigator, float h_scale) {
        float stem_height = 1;//00000;
        float stem_diameter = stem_height/20;
        float sphere_diameter = stem_diameter*5;
        
        NavigatorElevationRescaleGroup scaleGroup = new NavigatorElevationRescaleGroup(navigator, h_scale);
        scaleGroup.addChild(createStem(stem_app, stem_height, stem_diameter));
        scaleGroup.addChild(createPinhead(head_app, stem_height, sphere_diameter));
        
        addChild(scaleGroup);
    }
    
    public void addTextBoard(String title, Appearance stem_app,  float stem_height, float stem_diameter, float line_height, boolean point_mode) {
        String[] text_arr = title.split("\\\\n");
        
        addChild(createStem(stem_app, stem_height, stem_diameter));
        addChild(createBoard(createText(text_arr, null, line_height), line_height*text_arr.length, stem_height, point_mode));
    }
    
    public void addTextBoard(String title, Appearance stem_app,  GlobeNavigator navigator, float h_scale, boolean point_mode) {
        float line_height = .3f;
        float stem_height  = 1f;
        float stem_diameter = stem_height/40;
        String[] text_arr = title.split("\\\\n");
        
        NavigatorElevationRescaleGroup scaleGroup = new NavigatorElevationRescaleGroup(navigator, h_scale);
        scaleGroup.addChild(createStem(stem_app, stem_height, stem_diameter));
        scaleGroup.addChild(createBoard(createText(text_arr, null, line_height), line_height*text_arr.length, stem_height, point_mode));
        addChild(scaleGroup);
    }
    
    
    static public Node createStem(Appearance app, float height, float diameter) {
        Matrix4f mx = new Matrix4f();
        mx.setIdentity();
        mx.setColumn(3, 0, height/2, 0, 1);
        TransformGroup trans = new TransformGroup(mx);
        trans.addChild(new Cylinder(height, diameter, app));
        return trans;
    }
    
    static public Node createPinhead(Appearance app, float height, float diameter) {
        Matrix4f mx = new Matrix4f();
        mx.setIdentity();
        mx.setColumn(3, 0, height, 0, 1);
        TransformGroup trans = new TransformGroup(mx);
        trans.addChild(new Sphere(diameter/2, app));
        return trans;
    }
    
    
    
    static public Node createText(String[] text_arr, Appearance app, float line_height) {
        Text2D t2d = new Text2D();
        t2d.setText(text_arr, text_arr.length);
        t2d.setHorizontalJustification(Text2D.JUSTIFY_MIDDLE);
        t2d.setSize(line_height);
        Shape3D s3d = new Shape3D();
        s3d.setGeometry(t2d);
        s3d.setAppearance(app);
        
        return s3d;
    }
    
    static public Node createBoard(Node content, float board_height, float stem_height, boolean point_mode) {
        Matrix4f mx = new Matrix4f();
        mx.setIdentity();
        mx.setColumn(3, 0, .5f*board_height, 0, 1);
        TransformGroup board1Trans = new TransformGroup(mx);
        board1Trans.addChild(content);
        
        Billboard bb =  new Billboard(point_mode);
        bb.addChild(board1Trans);
        
        mx.setColumn(3, 0, stem_height + .5f*board_height, 0, 1);
        TransformGroup board2Trans = new TransformGroup(mx);
        board2Trans.addChild(bb);
        
        return board2Trans;
    }
    
    static public Node createTexture(BufferedImage img, float board_height) {
        Texture2D texture = null;
        int width = -1, height = -1;
        int h = 1, w = 1;
        
        
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
        
        float board_width = board_height * width / height;
        TriangleStripArray tsa = new TriangleStripArray();
        tsa.setVertices(TriangleStripArray.COORDINATE_2, new float [] {
            -board_width/2, -board_height,
            board_width/2, -board_height,
            -board_width/2, 0,
            board_width/2, 0
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
        return s3d;
    }
    
}
