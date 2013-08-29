//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d;

import com.norkart.virtualglobe.viewer.Camera;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.GlobeSurfaceGraphics;
import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.virtualglobe.globesurface.BttSurfaceView;
import com.norkart.virtualglobe.viewer.CullFrustum;
import com.norkart.virtualglobe.globesurface.GLSettings;

import java.util.WeakHashMap;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.vecmath.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class AV3DGlobeSurfaceGraphics
        implements GlobeSurfaceGraphics
        // implements NodeUpdateListener
{
    BttSurface surface;
    
    GlobeSurfaceNode surface_node;
    public Group            node;
    CullFrustum cullFrustum = new CullFrustum();
    Point3f camera_center   = new Point3f();
    Vector4f[] planes       = new Vector4f[6];
    GLU glu = new GLU();
    // Matrix4f inv_view = new Matrix4f();
    
    // public  getCore() { return core; }
    WeakHashMap<Camera, BttSurfaceView> cameraViews = new WeakHashMap();
    
    
    private float transparency = 1;
    private boolean wireframe = false;
    
    
    
    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }
    
    public void      setWireframe(boolean b) { wireframe = b; }
    public boolean   isWireframe() { return wireframe; }
    
    public BttSurface getSurface() { return surface; }
    
    class GlobeSurfaceNode extends Leaf implements CustomRenderable, LeafCullable, LeafPickTarget {
        int pickFlags;
        GlobeSurfaceNode() {
            pickFlags = 0; // -1;
        }
        
        
        // PickTarget stuff
        public int getPickTargetType() {
            return PickTarget.LEAF_PICK_TYPE;
        }
        
        public BoundingVolume getPickableBounds() {
            return bounds;
        }
        
        public boolean checkPickMask(int mask) {
            return ((pickFlags & mask) != 0);
        }
        
        public boolean pickLineRay(float[] p, float[] v, boolean h, float[] result, int res_mode) {
            Point3d pd = new Point3d(ViewerManager.getInstance().getOrigin());
            pd.x += p[0];
            pd.y += p[1];
            pd.z += p[2];
            
            Vector3d vd = new Vector3d();
            vd.set(v[0], v[1], v[2]);
            Point3d resd = surface.getIntersection(pd, vd, null);
            if (resd == null)
                return false;
            resd.sub(ViewerManager.getInstance().getOrigin());
            result[0] = (float)resd.x;
            result[1] = (float)resd.y;
            result[2] = (float)resd.z;
            return true;
        }
        public boolean pickLineSegment(float[] p1, float[] p2, boolean h, float[] result, int res_mode) {
            return false;
        }
        
        public boolean hasTransparency() {
            return transparency != 1;
        }
        
        public int compareTo(Object o) {
            return hashCode() - o.hashCode();
        }
        
        public int getCullableType() {
            return LeafCullable.GEOMETRY_CULLABLE;
        }
        
        public Renderable getRenderable() {
            return this;
        }
        
        public boolean processCull(RenderableInstructions output,
                Matrix4f vworldTx,
                Matrix4f viewTransform,
                Vector4f[] frustumPlanes,
                float angularRes) {
            if (BoundingVolume.FRUSTUM_ALLOUT == bounds.checkIntersectionFrustum(frustumPlanes, vworldTx)) {
                output.hasTransform = false;
                output.instructions = null;
                return false;
            }
            
            // Camera center
            
            // camera_center.x = viewTransform.m03 - vworldTx.m03;
            // camera_center.y = viewTransform.m13 - vworldTx.m13;
            // camera_center.z = viewTransform.m23 - vworldTx.m23;
            camera_center.set(0,0,0);
            viewTransform.transform(camera_center);
            vworldTx.transform(camera_center);
            
            // Transform planes
            for (int i =0; i<6; i++) {
                vworldTx.transform(frustumPlanes[i], planes[i]);
                float x = planes[i].x;
                float y = planes[i].y;
                float z = planes[i].z;
                float d = (float)Math.sqrt(x*x+y*y+z*z);
                planes[i].scale(1/d);
            }
            
            // Resolution
            Camera camera = ((AV3DViewerManager)ViewerManager.getInstance()).getMatchingCamera(frustumPlanes);
            if (camera instanceof PerspectiveCamera) {
                angularRes *= ((PerspectiveCamera)camera).getDetailSizeFactor();
                angularRes = (float)Math.toRadians(angularRes);
            }
            else if (camera instanceof AV3DMapView) {
                // angularRes = (float)Math.toRadians(angularRes);
                angularRes = ((AV3DMapView)camera).getResolution();
                // System.out.println(angularRes + " : " + planes[0] + " : " + planes[1] + " : " + planes[2] + " : " + planes[3] + " : " + planes[4] + " : " + planes[5]);
            }
            else {
                System.err.println("Hælvett!!");
                camera = ((AV3DViewerManager)ViewerManager.getInstance()).getMatchingCamera(frustumPlanes);
            }
            if (angularRes < .0001f) angularRes = .0001f;
            
            BttSurfaceView view = cameraViews.get(camera);
            if (view == null) {
                view = new BttSurfaceView(surface);
                cameraViews.put(camera, view);
                ViewerManager.getInstance().addOriginUpdateListener(view);
            }
            
            // long start_time = System.currentTimeMillis();
            // Set frustum values
            cullFrustum.setValues(planes, camera_center, view.getOrigin(),
                    angularRes, camera instanceof PerspectiveCamera);
            
            view.initiateUpdate(cullFrustum);
            
            output.hasTransform = false;
            output.instructions = view;
            // System.err.println("Culling called " + camera_center);
            return true;
        }
        
        public void render(GL gl, Object externalData) {
            gl.glEnable(GL.GL_LIGHTING);
            if (transparency != 1) {
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA,
                        GL.GL_ONE_MINUS_SRC_ALPHA);
            } else
                gl.glDisable(GL.GL_BLEND);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,  GL.GL_MODULATE);
            GLSettings gl_cap = GLSettings.get(gl);
            
            // Set up the drawing environment
            gl.glDisable(GL.GL_COLOR_MATERIAL);
            
            if (gl_cap.hasSeparateSpecularColor())
                gl.glLightModeli(GL.GL_LIGHT_MODEL_COLOR_CONTROL, gl_cap.separateSpecularCommand());
            
            // Test for vertex buffer objects
            
            float spec_int  = .3f;
            float[] white    = {1, 1, 1, transparency};
            float[] black    = {0, 0, 0, 1};
            float[] spec_mat = {spec_int, spec_int, spec_int, 1f};
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, white, 0);
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, white, 0);
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, spec_mat, 0);
            gl.glMateriali(GL.GL_FRONT, GL.GL_SHININESS, 64);
            
            // WIREFRAME?
            gl.glPolygonMode(GL.GL_FRONT, isWireframe() ? GL.GL_LINE : GL.GL_FILL );
            /*
            float[] col = new float[4];
            gl.glGetFloatv(GL.GL_LIGHT_MODEL_AMBIENT, col, 0);
            System.out.println("Ambient: " + col[0] + " "  + col[1] + " "  + col[2] + " "  + col[3]);
             */
            BttSurfaceView v = (BttSurfaceView)externalData;
            v.render(gl, glu);
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
            if (transparency != 1)
                gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_LIGHTING);
        }
    }
    
    public AV3DGlobeSurfaceGraphics(BttSurface surface) {
        this.surface = surface;
        
        for (int i=0; i<6; ++i)
            planes[i] = new Vector4f();
        
        // view = new BttSurfaceView(surface);
        AV3DViewerManager mgr = (AV3DViewerManager)ViewerManager.getInstance();
        surface_node = new GlobeSurfaceNode();
        surface_node.setBounds(new BoundingSphere(13000000.f));
        node = new Group();
        node.addChild(surface_node);
        if (mgr.globe_root.isLive()) {
            mgr.updateNode(mgr.globe_root, new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.addChild(node);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            mgr.globe_root.addChild(node);
        
        // core.updateNode(node, this, AVCore.UPDATE_BOUNDS);
    }
/*
  public void	updateNodeBoundsChanges(java.lang.Object src) {
     ((Group)src).addChild(node);
  }
 
  public void	updateNodeDataChanges(java.lang.Object src) {
  }
 */
    public void clear() {
        AV3DViewerManager mgr = (AV3DViewerManager)ViewerManager.getInstance();
        if (mgr.globe_root.isLive()) {
            mgr.updateNode(mgr.globe_root, new NodeUpdateListener() {
                public void updateNodeBoundsChanges(Object o) {
                    Group n = (Group)o;
                    n.removeChild(node);
                }
                public void updateNodeDataChanges(Object o) {}
            }, AV3DViewerManager.UPDATE_BOUNDS);
        } else
            mgr.globe_root.removeChild(node);
    /*
    isDeleting = true;
    synchronized (cameraTable) {
      while (!cameraTable.isEmpty()) {
        try {
          cameraTable.wait();
        }
        catch (InterruptedException ex) {}
      }
    }*/
        // core.graphics.remove(this);
        surface    = null;
        // core       = null;
    }
/*
  public void drawNow() {
    if (v != null)
      v.notWaitForUpdate();
  }
 */
}