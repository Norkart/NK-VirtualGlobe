//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer;

import javax.vecmath.*;
import com.norkart.virtualglobe.util.GJK;
import com.norkart.virtualglobe.util.GJKBody;
import com.norkart.virtualglobe.util.GJKPoint3d;
import com.norkart.virtualglobe.util.GJKPoint3dArray;
import com.norkart.virtualglobe.util.GJKRaySegment3d;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */


/**
 *
 *
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 *
 *  Auxilliary class for view frustum culling.
 *  Contains the frustum clipping planes and data to determine
 *  the view system resolution.
 * @author Rune Aasgaard
 * @version 1.0
 */
public final class CullFrustum  {
    /**
     * The object is totally inside the cull frustum
     */
    public static final int TOTALLY_IN  = 0;
    /**
     * The object is totally outside the view frustum
     */
    public static final int TOTALLY_OUT = 2;
    /**
     * The object is partially covered by the view frustum
     */
    public static final int PARTIAL     = 1;
    
    /**
     * The six planes delimiting the view frustum
     */
    protected Vector4f[] planes = new Vector4f[6];
    /**
     * The position of the camera center
     */
    protected GJKPoint3d camera = new GJKPoint3d();
    /**
     * The system origin
     */
    protected Point3d origin = new Point3d();
    
    protected GJKRaySegment3d viewAxis;
    protected GJKPoint3dArray     polytope;
    
    /**
     * The view system resolution, in radians pr. pixel.
     */
    protected float resolution;
    /**
     * Is this a perspective projection?
     */
    protected boolean perspective;
    /**
     * Texture resolution factor, reducing the tolerance for textures and thereby
     * preserveing texture memory.
     */
    protected float texture_res_factor = 1;
    
    /**
     * Allocate data structures
     */
    public CullFrustum() {
        for (int i=0;i<6;i++)
            planes[i] = new Vector4f();
    }
    
    /**
     * Copy values from other frustum object
     * @param f The other frustum
     */
    public void setValues(CullFrustum f) {
        this.origin.set(f.origin);
        this.camera.set(f.camera);
        this.resolution  = f.resolution;
        this.perspective = f.perspective;
        this.texture_res_factor  = f.texture_res_factor;
        
        // Transform planes
        for (int i =0; i<6; i++)
            this.planes[i].set(f.planes[i]);
        
        viewAxis = null;
        polytope = null;
    }
    
    /**
     * Initialize frustum with camera center point and delimiting planes
     * @param planes The planes delimiting this frustum
     * @param camera The camera center point
     */
    public void setValues(Vector4f[] planes, Point3f camera, Point3d origin, float resolution, boolean perspective) {
        this.camera.set(camera);
        this.origin.set(origin);
        this.camera.add(origin);
        this.resolution  = resolution;
        this.perspective = perspective;
        this.texture_res_factor  = 1;
        // Transform planes
        for (int i =0; i<6; i++)
            this.planes[i].set(planes[i]);
        
        viewAxis = null;
        polytope = null;
    }
    
    public void setTextureResFactor(float res_factor) {
        this.texture_res_factor = res_factor;
    }
    
    public float getTextureResFactor() {
        return texture_res_factor;
    }
    public GJKPoint3d getCameraCenter() {
        return camera;
    }
    
    public Point3d getOrigin() {
        return origin;
    }
    
    public float getResolution() {
        return resolution;
    }
    
    public Vector4f[] getPlanes() {
        return planes;
    }
    
    public boolean isPerspective() {
        return perspective;
    }
    
    /**
     * Test if sphere intersects with view frustum
     * @return
     * @param x
     * @param y
     * @param z
     * @param obj_rad
     */
    public int checkIntersection(double x, double y, double z, double obj_rad) {
        int retval = TOTALLY_IN;
        x -= origin.x;
        y -= origin.y;
        z -= origin.z;
        for (int i =5; i>=0; i--) {
            double d = planes[i].x*x+planes[i].y*y+planes[i].z*z+planes[i].w;
            if (d + obj_rad < 0)
                return TOTALLY_OUT;
            if (d - obj_rad < 0)
                retval = PARTIAL;
        }
        return retval;
    }
    
    public int checkIntersection(GJKBody B) {
        int plane_intersections = 0;
        Vector3d w = new Vector3d();
        Vector3d v = new Vector3d();
        for (int i =5; i>=0; i--) {
            v.set(planes[i].x, planes[i].y, planes[i].z);
            B.support(v, w);
            w.sub(origin);
            
            double d = planes[i].x*w.x+planes[i].y*w.y+planes[i].z*w.z+planes[i].w;
            if (d  < 0)
                return TOTALLY_OUT;
            v.negate();
            B.support(v, w);
            w.sub(origin);
            d = planes[i].x*w.x+planes[i].y*w.y+planes[i].z*w.z+planes[i].w;
            if (d  < 0)
                plane_intersections++;
        }
        
        if (plane_intersections > 1) {
            GJK gjk = GJK.getInstance();
            if (!gjk.intersect(getBody(), B))
                return TOTALLY_OUT;
        }
        
        return plane_intersections > 0 ? PARTIAL : TOTALLY_IN;
    }
    
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param obj_radius
     * @param tot_dev
     * @return
     */
    /*
    public boolean isTextureVisible(double x, double y, double z, double obj_radius, double tot_dev) {
        float rr = resolution*texture_res_factor;
        if (!perspective)
            return rr < tot_dev;
     
        double radius = obj_radius + tot_dev/rr;
        x -= camera.x;
        y -= camera.y;
        z -= camera.z;
        double dist = x*x+y*y+z*z;
        return dist < radius*radius;
    }
     */
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param obj_radius
     * @param tot_dev
     * @param radius2D
     * @return
     */
    public boolean isVisible(double x, double y, double z, double obj_radius, double tot_dev, double radius2D) {
        float rr = resolution;
        if (!perspective)
            return rr < tot_dev && rr < radius2D;
        
        double radius = obj_radius + tot_dev/rr;
        x -= camera.x;
        y -= camera.y;
        z -= camera.z;
        double dist = x*x+y*y+z*z;
        radius2D += 2*radius2D/rr;
        
        return dist < radius*radius && dist < radius2D*radius2D;
    }
    
    /**
     * Compute the distance between the given point and the camera view axis.
     * @param x Point coordinate value
     * @param y Point coordinate value
     * @param z Point coordinate value
     * @return Distance
     */
    /*
    public double distToViewAxis(double x, double y, double z) {
        double t = planes[5].x * (x - camera.x) + planes[5].y * (y - camera.y) + planes[5].z * (z - camera.z);
     
        double d_near = -(planes[5].x*camera.x+planes[5].y*camera.y+planes[5].z*camera.z+planes[5].w);
        double d_far  =  (planes[4].x*camera.x+planes[4].y*camera.y+planes[4].z*camera.z+planes[4].w);
        if (t < d_near) t = d_near;
        d_far = d_near + (d_far - d_near)*0.2;
        if (t > d_far) t = d_far;
     
        x -= t*planes[5].x + camera.x;
        y -= t*planes[5].y + camera.y;
        z -= t*planes[5].z + camera.z;
     
        return Math.sqrt(x*x+y*y+z*z);
    }
     */
    public GJKBody getViewAxis() {
        if (viewAxis == null) {
            double d_near = -(planes[5].x*camera.x+planes[5].y*camera.y+planes[5].z*camera.z+planes[5].w);
            double d_far  =  (planes[4].x*camera.x+planes[4].y*camera.y+planes[4].z*camera.z+planes[4].w);
            d_far = d_near + (d_far - d_near)*0.2;
            viewAxis = new GJKRaySegment3d(camera, new Vector3d(planes[5].x, planes[5].y, planes[5].z), d_near, d_far);
        }
        return viewAxis;
    }
    
    private static final int[] plane_points = {
        1, 2, 5,
        0, 2, 5,
        0, 3, 5,
        1, 3, 5,
        1, 3, 4,
        0, 3, 4,
        0, 2, 4,
        1, 2, 4,
    };
    
    public GJKBody getBody() {
        if (polytope == null) {
            Point3d[] points = new Point3d[8];
            Matrix3d m = new Matrix3d();
            for (int i = 0; i < 8; ++i) {
                points[i] = new Point3d();
                m.setRow(0, planes[plane_points[3*i+0]].x, planes[plane_points[3*i+0]].y, planes[plane_points[3*i+0]].z);
                m.setRow(1, planes[plane_points[3*i+1]].x, planes[plane_points[3*i+1]].y, planes[plane_points[3*i+1]].z);
                m.setRow(2, planes[plane_points[3*i+2]].x, planes[plane_points[3*i+2]].y, planes[plane_points[3*i+2]].z);
                points[i].set(-planes[plane_points[3*i+0]].w, -planes[plane_points[3*i+1]].w, -planes[plane_points[3*i+2]].w);
                m.invert();
                m.transform(points[i]);
                points[i].add(origin);
            }
            polytope = new GJKPoint3dArray(points);
        }
        return polytope;
    }
}