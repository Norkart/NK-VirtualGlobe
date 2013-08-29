/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GlobeSurfaceLine.java
 *
 * Created on 25. mai 2007, 10:13
 *
 */

package com.norkart.virtualglobe.viewer.av3d.nodes;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.Iterator;

import java.nio.ByteBuffer;

import javax.vecmath.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

// import org.j3d.aviatrix3d.NodeUpdateListener;
// import org.j3d.aviatrix3d.LineStripArray;

import com.norkart.geopos.*;
import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.globesurface.GlobeElevationUpdateListener;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;
import com.norkart.virtualglobe.viewer.CullFrustum;
import com.norkart.virtualglobe.viewer.Camera;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.globesurface.GLSettings;

import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DMapView;

import com.norkart.virtualglobe.components.GlobeSurface;

/**
 *
 * @author runaas
 */
public class GlobeSurfaceLine
        extends BaseNode implements CustomRenderable, LeafCullable, // LeafPickTarget,
        OriginUpdateListener, NodeUpdateListener, GlobeElevationUpdateListener
//        extends LineStripArray implements NodeUpdateListener, OriginUpdateListener
{
    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_MSG =
            "This node has been marked as not pickable by the user";
    
    
    private GlobeSurface globe;
    private LineString geometry;
    private Appearance app;
    
    private Point3d origin = new Point3d();
    private Point3f camera_center = new Point3f();
    private Vector4f[] planes       = new Vector4f[6];
    private CullFrustum cullFrustum = new CullFrustum();
    
    private int pickFlags;
    
    private WeakHashMap<Camera, View> cameraViews = new WeakHashMap();
    
    
    private class View {
        float [] new_vertices = new float [64*1024*3];
        int   [] new_strip_cnt = new int[8];
        int num_strip;
        int num_vtx;
        int curr_vtx;
        private float [] colv = new float[4];
        ByteBuffer vertexBuffer;
        CullFrustum cullFrustum = new CullFrustum();
        boolean valid_frustum = false;
        boolean has_new = false;
        
        synchronized void initiateUpdate(CullFrustum frustum) {
            if (frustum != null) {
                cullFrustum.setValues(frustum);
                valid_frustum = true;
            } else
                valid_frustum = false;
        }
        
        void render(GL gl) {
            if (has_new) {
                num_strip = 1;
                num_vtx = curr_vtx;
                new_strip_cnt[0] = curr_vtx;
                
                vertexBuffer = ByteBuffer.allocateDirect(num_vtx*3*4);
                vertexBuffer.order(java.nio.ByteOrder.nativeOrder());
                vertexBuffer.asFloatBuffer().put(new_vertices, 0, num_vtx*3);
                has_new = false;
            }
            
            
            if (app != null)  {
                app.render(gl);
                Material mat = app.getMaterial();
                if (mat != null && !mat.isLightingEnabled()) {
                    mat.getDiffuseColor(colv);
                    gl.glColor4fv(colv, 0);
                }
            }
            
            gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(GL.GL_LINE_STRIP,
                    0,
                    num_vtx);
            gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
            if (app != null)
                app.postRender(gl);
            
        }
        
        synchronized void updateGraphics() {
            if (!valid_frustum || has_new || geometry == null ||
                    geometry.getPositionList() == null ||
                    geometry.getPositionList().size() < 2) return;
            curr_vtx  = 0;
            
            
            Ellipsoid ellps = globe.getSurface().getEllipsoid();
            Ellipsoid.DistAz daz = new Ellipsoid.DistAz();
            PositionList pos_list = geometry.getPositionList();
            ArrayList<Point3d> point_list = new ArrayList();
            for (int i = 0; i < pos_list.size(); ++i) {
                double lat = Math.toRadians(pos_list.getLatitude(i));
                double lon = Math.toRadians(pos_list.getLongitude(i));
                double h = globe.getSurface().getElevation(lon, lat);
                Point3d p = new Point3d();
                
                ellps.toCartesian(lat, lon, h, p);
                double tol = cullFrustum.getCameraCenter().distance(p)*cullFrustum.getResolution();
                ellps.toCartesian(lat, lon, h+tol, p);
                point_list.add(p);
            }
            generalize(cullFrustum, point_list, 0, point_list.size()-1);
            int prev_i = 0;
            for (int i = 1; i < pos_list.size(); ++i) {
                Point3d next_p = point_list.get(i);
                if (next_p == null)
                    continue;
                Point3d prev_p = point_list.get(prev_i);
                double prev_lat = Math.toRadians(pos_list.getLatitude(prev_i));
                double prev_lon = Math.toRadians(pos_list.getLongitude(prev_i));
                double next_lat = Math.toRadians(pos_list.getLatitude(i));
                double next_lon = Math.toRadians(pos_list.getLongitude(i));
                
                globe.getSurface().getEllipsoid().inverseGeodesic(prev_lat, prev_lon, next_lat, next_lon, daz);
                genLine(true, prev_lon, prev_lat, next_lon, next_lat, daz.dist, daz.az12, prev_p, next_p);
                prev_i = i;
            }
            
            addVertex(point_list.get(point_list.size()-1));
            
            has_new = true;
        }
        
        
        private boolean genLine(boolean insert_first, double lon1, double lat1, double lon2, double lat2,
                double len, double az, Point3d p1, Point3d p2) {
        /*
        if (len < 10)
            System.err.println("Ka fasjken");
         */
            
            double x = (p1.x+p2.x)/2;
            double y = (p1.y+p2.y)/2;
            double z = (p1.z+p2.z)/2;
            if (cullFrustum.checkIntersection(x, y, z, len) == CullFrustum.TOTALLY_OUT) {
                if (insert_first)
                    addVertex(p1);
                return false;
            }
            
            x -= cullFrustum.getCameraCenter().x;
            y -= cullFrustum.getCameraCenter().y;
            z -= cullFrustum.getCameraCenter().z;
            double cam_dist = Math.sqrt(x*x+y*y+z*z);
            double tol = cam_dist*cullFrustum.getResolution();
            
            // radius2D += 2*radius2D/rr;
// double radius = obj_radius + tot_dev/rr;
            // return dist < radius*radius && dist < radius2D*radius2D;
            
            if (tol > len) {
                if (insert_first)
                    addVertex(p1);
                return false;
            }
            Ellipsoid.LatLonAz llaz =
                    globe.getSurface().getEllipsoid().forwGeodesic(lat1, lon1, len/2, az, null);
            Point3d p_mid = new Point3d();
            globe.getSurface().getEllipsoid().toCartesian(llaz.lat, llaz.lon, globe.getSurface().getElevation(llaz.lon, llaz.lat)+tol, p_mid);
            boolean retval =  deviation(p1, p2, p_mid) > tol;
            
            retval |= genLine(insert_first, lon1, lat1, llaz.lon, llaz.lat, len/2, az, p1, p_mid);
            retval |= genLine(retval, llaz.lon, llaz.lat, lon2, lat2, len/2, llaz.az+Math.PI, p_mid, p2);
            
            return retval;
        }
        
        private void addVertex(Point3d p) {
            if (new_vertices.length < curr_vtx*3+3) {
                float[] tmp = new float [new_vertices.length*2 + 3];
                System.arraycopy(new_vertices, 0, tmp, 0, curr_vtx*3);
                new_vertices = tmp;
            }
            new_vertices[curr_vtx*3+0] = (float)(p.x-origin.x);
            new_vertices[curr_vtx*3+1] = (float)(p.y-origin.y);
            new_vertices[curr_vtx*3+2] = (float)(p.z-origin.z);
            curr_vtx++;
        }
        
    }
    
    /** Creates a new instance of GlobeSurfaceLine */
    public GlobeSurfaceLine(GlobeSurface globe, LineString g, Appearance app) {
        this.globe = globe;
        this.geometry = g;
        this.app = app;
        
        for (int i =0; i<planes.length; ++i)
            planes[i] = new Vector4f();
        
        pickFlags = -1;
        globe.getSurface().addGlobeElevationUpdateListener(this);
        /*
        int list_len = geometry.getPositionList().size();
        for (int i =0; i< list_len; ++i)
            point_list.add(new Point3d());
         */
        // updateGraphics();
    }
    
    public GlobeSurfaceLine(GlobeSurface globe, LineString g) {
        this(globe, g, null);
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
        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);
        return false;
    }
    public boolean pickLineSegment(float[] p1, float[] p2, boolean h, float[] result, int res_mode) {
        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);
        return false;
    }
    
    
    public boolean hasTransparency() {
        return false;
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
    
    public void render(GL gl, Object externalData) {
        View v = (View)externalData;
        v.render(gl);
    }
    
    public boolean processCull(RenderableInstructions output,
            Matrix4f vworldTx,
            Matrix4f viewTransform,
            Vector4f[] frustumPlanes,
            float angularRes) {
        
        
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
        
        Camera camera = ((AV3DViewerManager)ViewerManager.getInstance()).getMatchingCamera(frustumPlanes);
        if (camera instanceof PerspectiveCamera) {
            angularRes *= ((PerspectiveCamera)camera).getDetailSizeFactor();
            angularRes = (float)Math.toRadians(angularRes);
        } else if (camera instanceof AV3DMapView) {
            angularRes = (float)Math.toRadians(angularRes);
            // angularRes = ((AV3DMapView)camera).getResolution();
            // System.out.println(angularRes + " : " + planes[0] + " : " + planes[1] + " : " + planes[2] + " : " + planes[3] + " : " + planes[4] + " : " + planes[5]);
        } else {
            System.err.println("Hælvett!!");
            camera = ((AV3DViewerManager)ViewerManager.getInstance()).getMatchingCamera(frustumPlanes);
        }
        if (angularRes < .0001f) angularRes = .0001f;
        
        View view = cameraViews.get(camera);
        if (view == null) {
            view = new View();
            synchronized (cameraViews) {
                cameraViews.put(camera, view);
            }
        }
        
        if (BoundingVolume.FRUSTUM_ALLOUT == bounds.checkIntersectionFrustum(frustumPlanes, vworldTx)) {
            output.hasTransform = false;
            output.instructions = null;
            view.initiateUpdate(null);
            return false;
        }
        
        
        // long start_time = System.currentTimeMillis();
        // Set frustum values
        cullFrustum.setValues(planes, camera_center, origin,
                angularRes, camera instanceof PerspectiveCamera);
        
        view.initiateUpdate(cullFrustum);
        
        output.hasTransform = false;
        output.instructions = view;
        // System.err.println("Culling called " + camera_center);
        return true;
    }
    
    
    
    //-------------------------------------------------------
    // NodeUpdateListener methods
    //-------------------------------------------------------
    public void updateNodeBoundsChanges(java.lang.Object src) {    }
    
    public void	updateNodeDataChanges(java.lang.Object src) {}
    
    
    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root.
     */
    protected void updateBounds() {
        recomputeBounds();
        
        if(parent != null)
            updateParentBounds();
    }
    
    
    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds() {
        if(!implicitBounds)
            return;
        
        if(geometry == null || geometry.getPositionList().size() == 0) {
            bounds = INVALID_BOUNDS;
        } else {
            if((bounds == null) || (bounds == INVALID_BOUNDS))
                bounds = new BoundingBox();
            
            BoundingBox bbox = (BoundingBox)bounds;
            
            float[] min = new float[3];
            float[] max = new float[3];
            min[0] = min[1] = min[2] =  Float.MAX_VALUE;
            max[0] = max[1] = max[2] = -Float.MAX_VALUE;
            
            PositionList pos_list = geometry.getPositionList();
            Point3d p = new Point3d();
            for (int i=0; i<pos_list.size(); ++i) {
                double lat = pos_list.getLatitude(i);
                double lon = pos_list.getLongitude(i);
                
                globe.getSurface().getEllipsoid().toCartesian(Math.toRadians(lat), Math.toRadians(lon), 0, p);
                p.sub(origin);
                if (p.x < min[0])
                    min[0] = (float)p.x;
                if (p.y < min[1])
                    min[1] = (float)p.y;
                if (p.z < min[2])
                    min[2] = (float)p.z;
                if (p.x > max[0])
                    max[0] = (float)p.x;
                if (p.y > max[1])
                    max[1] = (float)p.y;
                if (p.z > max[2])
                    max[2] = (float)p.z;
            }
            
            bbox.setMinimum(min);
            bbox.setMaximum(max);
        }
    }
    
    
    /*
    public void updateNodeBoundsChanges(Object src) {
        if (!has_new) return;
        setVertices(LineStripArray.COORDINATE_3, new_vertices, num_vtx);
        setStripCount(new_strip_cnt, num_strip);
        has_new = false;
    }
     
     
    public void updateNodeDataChanges(Object src) {
     
    }
     */
    public void updateOrigin(Point3d p) {
        origin.set(p);
        if (isLive()) {
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(this, this, AV3DViewerManager.UPDATE_BOUNDS);
        }
        
        // updateGraphics();
    }
    
    public boolean requestUpdateOrigin(Point3d p) {
        return true;
    }
    
    public void updateElevation(GlobeElevationModel globe) {
        synchronized (cameraViews) {
            for (View v : cameraViews.values()) {
                v.updateGraphics();
            }
        }
    }
    
    private static double deviation(Point3d p1, Point3d p2, Point3d p) {
        
        double vx = p2.x-p1.x;
        double vy = p2.y-p1.y;
        double vz = p2.z-p1.z;
        
        double d = Math.sqrt(vx*vx+vy*vy+vz*vz);
        
        double ppx = p.x-p1.x;
        double ppy = p.y-p1.y;
        double ppz = p.z-p1.z;
        
        if (d == 0)
            return Math.sqrt(ppx*ppx+ppy*ppy+ppz*ppz);
        
        double x = vy*ppz-vz*ppy;
        double y = vz*ppx-vx*ppz;
        double z = vx*ppy-vy*ppx;
        
        return Math.sqrt(x*x+y*y+z*z)/d;
    }
    
    static private void generalize(CullFrustum cull_frustum, ArrayList<Point3d> pl, int from_ix, int to_ix) {
        double max_dev = 0;
        int    mid_ix = -1;
        Point3d from_p = pl.get(from_ix);
        Point3d to_p   = pl.get(to_ix);
        
        double[] min = new double[3];
        double[] max = new double[3];
        min[0] = Math.min(from_p.x, to_p.x);
        min[1] = Math.min(from_p.y, to_p.y);
        min[2] = Math.min(from_p.z, to_p.z);
        max[0] = Math.max(from_p.x, to_p.x);
        max[1] = Math.max(from_p.y, to_p.y);
        max[2] = Math.max(from_p.z, to_p.z);
        for (int i = from_ix+1; i < to_ix; ++i) {
            Point3d p = pl.get(i);
            double dev = deviation(from_p, to_p, p);
            double tol = cull_frustum.getCameraCenter().distance(p)*cull_frustum.getResolution();
            dev /= tol;
            if (dev > max_dev) {
                max_dev = dev;
                mid_ix = i;
            }
            if (p.x < min[0])
                min[0] = p.x;
            if (p.y < min[1])
                min[1] = p.y;
            if (p.z < min[2])
                min[2] = p.z;
            if (p.x > max[0])
                max[0] = p.x;
            if (p.y > max[1])
                max[1] = p.y;
            if (p.z > max[2])
                max[2] = p.z;
        }
        
        
        
        if (max_dev > 1 &&
                cull_frustum.checkIntersection((min[0]+max[0])/2, (min[1]+max[1])/2, (min[2]+max[2])/2,
                Math.sqrt((max[0]-min[0])*(max[0]-min[0])+(max[1]-min[1])*(max[1]-min[1])+ (max[2]-min[2])*(max[2]-min[2]))/2)
                != CullFrustum.TOTALLY_OUT) {
            generalize(cull_frustum, pl, from_ix, mid_ix);
            generalize(cull_frustum, pl, mid_ix, to_ix);
            return;
        }
        
        for (int i = from_ix+1; i < to_ix; ++i)
            pl.set(i, null);
    }
    
    
    
    
}
