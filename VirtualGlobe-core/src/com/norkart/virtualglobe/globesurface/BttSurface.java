//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import com.norkart.geopos.Ellipsoid;

import com.norkart.virtualglobe.globesurface.texture.ImageBuffer;
import com.norkart.virtualglobe.util.ApplicationSettings;


import java.lang.ref.WeakReference;

import java.util.*;
// import java.nio.FloatBuffer;
// import javax.media.opengl.util.BufferUtils;
import javax.vecmath.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: Norkart AS</p>
 * 
 * A representation of a triangulated surface based on 
 * a set of right-isosceles triangles.
 * @author Rune Aasgaard
 * @version 1.0
 */

final public class BttSurface implements GlobeElevationModel {
    // The main datastructure
    int     size = 0;
    int[]   index;
    int[]   status;
    
    int[]   int_lonlat, int_h, int_dh;
    int[]   int_cartesian;
    float[] own_obj_radius;
    float[] tot_obj_radius;
    float[] tot_dev;
    
    private int free = -1, top_free = 0, fill = 0;
    
    // Timestamp
    static final int TS_INC = 6;
    static final int TS_HAS_NORMAL   = 1;
    static final int TS_IS_INVISIBLE = 2;
    static final int TS_IS_VISIBLE   = 4;
    
    private int ts = TS_INC;
    private int terrain_cut_ts = 0;
    private int texture_cut_ts = 0;
    private int uppercut_ts = TS_INC;
    private int gc_id = 0;
    
    // Node index constants
    static final int ERROR = -1;
    static final int RIGHT = 0;
    static final int LEFT  = 1;
    static final int TOP   = 2;
    static final int BASE  = 3;
    static final int IX_MASK = 0x3;
    static final int ID_SHIFT = 2;
    
    
    double scale = 0.01;
    private Ellipsoid ellps;
    private double int_lat_scale;
    private double lat_scale;
    
    // Visualization oriented parameters
    float texture_res_factor = 1.f;
    
    private double h_scale = 1;
    private double h_scale_new = 1;
    private Point3d p = new Point3d();
    
    // The roots of the data hierarchies
    int[]    base_mesh;
    String[] base_code;
    
    // List of views
    Vector<BttSurfaceView> view_list = new Vector();
    
    // The Elevation data source
    private ElevationSource source;
    
    // Texture coverages
    TextureCoverage[] textures2D = new TextureCoverage[4];
    int               num_textures2D = 0;
    
    
    /**
     * Return the ellipsoid defining the datum surface for this globe
     * @return The datum ellipsoid
     */
    public Ellipsoid getEllipsoid() { return ellps; }
    /**
     * Return the elevation scale, a value used for displaying exaggerated elevations
     * @return the elevation scale
     */
    public double    getElevationScale() { return h_scale; }
    /**
     * Set the elevation scale, a value used for displaying exaggerated elevations
     * @param v The value
     */
    public void      setElevationScale(double v) { h_scale_new = v; }
    
    private Vector elevation_update_listeners = new Vector();
    /**
     * Add an elevation update listener. The listener is called whenever the globe 
     * surface has been updated with new data
     * @param gel A new globe update listener
     */
    public void addGlobeElevationUpdateListener(GlobeElevationUpdateListener gel) {
        synchronized (elevation_update_listeners) {
            elevation_update_listeners.add(new WeakReference(gel));
        }
    }
    
    void fireGlobeElevationUpdateListeners() {
        synchronized (elevation_update_listeners) {
            Iterator it = elevation_update_listeners.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                GlobeElevationUpdateListener gel = (GlobeElevationUpdateListener)ref.get();
                if (gel != null)
                    gel.updateElevation(this);
                else
                    it.remove();
            }
        }
    }
    
    
    
    /**
     * Construct a surface data structure fetching data from the given ElevationSource
     * Having a node cache of the given size
     * @param elevation_source The elevation source; fetches elevationvalues from server and/or cache
     */
    public BttSurface(ElevationSource elevation_source) {
        source = elevation_source;
        source.setElevationSurface(this);
        ellps = source.getEllipsoid();
        initializeCoosys();
        
        size = 256*1024;
        free = -1;
        top_free = 0;
        
        index   = new int[size*8];
        status  = new int[size];
        
        int_lonlat = new int[size*2];
        int_h      = new int[size];
        int_dh     = new int[size];
        
        int_cartesian = new int[size*3];
        
        own_obj_radius = new float[size];
        tot_obj_radius = new float[size];
        tot_dev = new float[size];
        
        createBasemesh();
    }
    
    /**
     * Clear the datastructures, free all resources
     */
    public void clear() {
        ArrayList<BttSurfaceView> tmp_view_list = new  ArrayList<BttSurfaceView>();
        synchronized (view_list) {
            tmp_view_list.addAll(view_list);
            /*
            Iterator view_it = view_list.iterator();
            while (view_it.hasNext()) {
                BttSurfaceView v = (BttSurfaceView)view_it.next();
                view_it.remove();
                tmp_view_list.add(v);
             
            }*/
            view_list.clear();
        }
        
        Iterator<BttSurfaceView> view_it = tmp_view_list.iterator();
        while (view_it.hasNext()) {
            BttSurfaceView v = view_it.next();
            // view_it.remove();
            v.close();
        }
        tmp_view_list.clear();
        
        for (int i=0; i<num_textures2D; ++i) {
            textures2D[i].clearTextures();
            textures2D[i] = null;
        }
        
        index   = null;
        status  = null;
        int_lonlat = null;
        int_h      = null;
        int_dh     = null;
        int_cartesian = null;
        own_obj_radius = null;
        tot_obj_radius = null;
        tot_dev = null;
        source = null;
        ellps = null;
    }
    
    static private class Htab {
        private int [] tab = new int [1024];
        private int size;
        
        Htab() {
            clear();
        }
        
        void clear() {
            size = 0;
            Arrays.fill(tab, -1);
        }
        
        boolean add(int v) {
            if (10*size >= 9*tab.length) {
                int [] old_tab = tab;
                tab = new int[old_tab.length*2];
                clear();
                for (int i = old_tab.length; --i >= 0; )
                    if (old_tab[i] >= 0)
                        add(old_tab[i]);
            }
            int pos = v%tab.length;
            int ins_pos = -1;
            for (int i = tab.length; tab[pos] != -1 && --i>=0;) {
                if (tab[pos] == v)
                    return false;
                if (ins_pos == -1 && tab[pos] == -2)
                    ins_pos = pos;
                pos = (pos+1)%tab.length;
            }
            if (ins_pos == -1)
                tab[pos] = v;
            else
                tab[ins_pos] = v;
            ++size;
            return true;
        }
    }
    
    private class IntersectData {
        static final int INTERSECT_SORTED  = 0;
        static final int INTERSECT_ALL     = 1;
        static final int INTERSECT_NEAREST = 2;
        int intersect_type;
        
        Vector3d  tmp_v = new Vector3d();
        Matrix3d  tmp_m = new Matrix3d();
        double[]  intersect_dist = new double[4];
        int       num_intersect;
        Point3d   p = new Point3d();
        Vector3d  v = new Vector3d();
        double    t1,  t2;
        Htab   visited = new Htab();
        
        
        void intersect(int n) {
            // if (!visited.add(new Integer(n)))
            if (!visited.add(n))
                return;
            tmp_v.set(scale*int_cartesian[n*3  ] - p.x,
                    scale*int_cartesian[n*3+1] - p.y,
                    scale*int_cartesian[n*3+2] - p.z);
            
            double t = v.dot(tmp_v);
            if (t < t1) t = t1;
            if (t > t2) t = t2;
            tmp_v.x -= v.x*t;
            tmp_v.y -= v.y*t;
            tmp_v.z -= v.z*t;
            double dist = tmp_v.length();
            
            if (dist > tot_obj_radius[n])
                return;
            
            for (int ix = 0; ix < 4; ++ix) {
                int ch_n = index[8*n+ix];
                if (ch_n >= 0)
                    intersect(ch_n);
                else if (dist > own_obj_radius[n])
                    return;
                else {
                    int l_n = index[8*n+4+(3*LEFT+ix)%4];
                    int r_n = index[8*n+4+(3*RIGHT+ix)%4];
                    
                    // Sjekk flateskjæring
                    tmp_m.setColumn(0,
                            scale*(int_cartesian[l_n*3  ] - int_cartesian[n*3  ]),
                            scale*(int_cartesian[l_n*3+1] - int_cartesian[n*3+1]),
                            scale*(int_cartesian[l_n*3+2] - int_cartesian[n*3+2]));
                    tmp_m.setColumn(1,
                            scale*(int_cartesian[r_n*3  ] - int_cartesian[n*3  ]),
                            scale*(int_cartesian[r_n*3+1] - int_cartesian[n*3+1]),
                            scale*(int_cartesian[r_n*3+2] - int_cartesian[n*3+2]));
                    tmp_m.setColumn(2, v);
                    tmp_m.invert();
                    tmp_v.set(p.x - scale*int_cartesian[n*3  ],
                            p.y - scale*int_cartesian[n*3+1],
                            p.z - scale*int_cartesian[n*3+2]);
                    tmp_m.transform(tmp_v);
                    tmp_v.z = -tmp_v.z;
                    if (tmp_v.x >= 0 && tmp_v.y >= 0 &&
                            tmp_v.x + tmp_v.y <= 1 &&
                            tmp_v.z >= t1 && tmp_v.z <= t2) {
                        if (intersect_type == INTERSECT_NEAREST) {
                            intersect_dist[0] = tmp_v.z;
                            num_intersect = 1;
                            t2 = tmp_v.z;
                        } else {
                            if (num_intersect >= intersect_dist.length) {
                                double[] tmp = new double[intersect_dist.length*2];
                                for (int i = 0; i < num_intersect; ++i)
                                    tmp[i] = intersect_dist[i];
                                intersect_dist = tmp;
                            }
                            if (intersect_type == INTERSECT_ALL)
                                intersect_dist[num_intersect++] = tmp_v.z;
                            else {
                                int insertpos = 0;
                                while (insertpos < num_intersect && intersect_dist[num_intersect++] < tmp_v.z)
                                    ++insertpos;
                                for (int i = insertpos; i < num_intersect; ++i)
                                    intersect_dist[i+1] = intersect_dist[i];
                                intersect_dist[insertpos] = tmp_v.z;
                                num_intersect++;
                            }
                        }
                    }
                }
            }
        }
    }
    private ArrayList intersect_data = new ArrayList();
    
    /**
     * Compute the intersection between a line segment between p1 and p2 and this surface
     * @param p1 The line segment starting point
     * @param p2 The line segment end point
     * @param result The result point, automatically created if the input value is null
     * @return The result point
     */
    public Point3d   getIntersection(Point3d p1, Point3d p2, Point3d result) {
        IntersectData id;
        synchronized (intersect_data) {
            if (intersect_data.isEmpty())
                id = new IntersectData();
            else
                id = (IntersectData)intersect_data.remove(intersect_data.size()-1);
        }
        id.intersect_type = id.INTERSECT_NEAREST;
        id.num_intersect = 0;
        id.p.set(p1);
        id.v.sub(p2, p1);
        id.t1 = 0;
        id.t2 = id.v.length();
        id.v.scale(1/id.t2);
        id.visited.clear();
        for (int i = 0; i < base_mesh.length; ++i)
            id.intersect(base_mesh[i]);
        if (id.num_intersect > 0) {
            if (result == null) result = new Point3d();
            result.x = id.p.x + id.v.x*id.intersect_dist[0];
            result.y = id.p.y + id.v.y*id.intersect_dist[0];
            result.z = id.p.z + id.v.z*id.intersect_dist[0];
        } else
            result = null;
        synchronized (intersect_data) {
            intersect_data.add(id);
        }
        return result;
    }
    /**
     * Compute the intersection between a line ray from p and in the direction of the vector v; and this surface
     * @param p The start point 
     * @param v The direction vector
     * @param result The result point, automatically created if the input value is null
     * @return The result point
     */
    public Point3d   getIntersection(Point3d p,  Vector3d v, Point3d result) {
        IntersectData id;
        synchronized (intersect_data) {
            if (intersect_data.isEmpty())
                id = new IntersectData();
            else
                id = (IntersectData)intersect_data.remove(intersect_data.size()-1);
        }
        
        id.intersect_type = id.INTERSECT_NEAREST;
        id.num_intersect = 0;
        id.p.set(p);
        id.v.set(v);
        id.t1 = 0;
        id.t2 = id.v.length();
        id.v.scale(1/id.t2);
        id.t2 = Double.MAX_VALUE;
        id.visited.clear();
        for (int i = 0; i < base_mesh.length; ++i)
            id.intersect(base_mesh[i]);
        if (id.num_intersect > 0) {
            if (result == null) result = new Point3d();
            result.x = id.p.x + id.v.x*id.intersect_dist[0];
            result.y = id.p.y + id.v.y*id.intersect_dist[0];
            result.z = id.p.z + id.v.z*id.intersect_dist[0];
        } else
            result = null;
        synchronized (intersect_data) {
            intersect_data.add(id);
        }
        return result;
    }
    
    /**
     * Find the triangle that contains this point
     * @param lon The longitude (radians)
     * @param lat The latitude (radians)
     * @return The triangle index
     */
    public int getTriangle(double lon, double lat) {
        int int_lon = this.lonToInt(lon);
        int int_lat = this.latToInt(lat);
        
        for (int i=0; i<base_mesh.length; ++i) {
            for (int j=0; j<4; j++) {
                int t = getTriangle((base_mesh[i]<<ID_SHIFT)|j, int_lon, int_lat);
                if (t >= 0)
                    return t;
            }
        }
        return ERROR;
    }
    
    private int getTriangle(int t_id, int lon, int lat) {
        if (t_id < 0) return ERROR;
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        int id_l  = index[8*id+4+(ix+3)%4];
        int id_r  = index[8*id+4+ix];
        if (id_l < 0 || id_r < 0) return ERROR;
        int lon_t = int_lonlat[2*id];
        int lat_t = int_lonlat[2*id+1];
        int lon_l = int_lonlat[2*id_l]   - lon_t;
        int lat_l = int_lonlat[2*id_l+1] - lat_t;
        int lon_r = int_lonlat[2*id_r]   - lon_t;
        int lat_r = int_lonlat[2*id_r+1] - lat_t;
        
        double d = (double)lat_l*lon_r - (double)lat_r*lon_l;
        if (d == 0)
            return ERROR;
        double u = lon_r/d*(lat-lat_t) - lat_r/d*(lon-lon_t);
        double v = lat_l/d*(lon-lon_t) - lon_l/d*(lat-lat_t);
        
        if (u < 0 || v < 0 || u+v > 1)
            return ERROR;
        
        int bn = index[8*id+ix];
        if (bn < 0)
            return t_id;
        ix = (ix+(u>v?LEFT:RIGHT))%4;
        return getTriangle((bn << ID_SHIFT) | ix , lon, lat);
    }
    
    
    
    /**
     * NOT COMPLETED !!!
     * @param lon1 
     * @param lat1 
     * @param lon2 
     * @param lat2 
     * @return 
     */
    public ArrayList project(double lon1, double lat1, double lon2, double lat2) {
        ArrayList retval = new ArrayList();
        Point p = new Point(lon1, lat1, getElevation(lon1, lat1));
        retval.add(p);
        
        Ellipsoid.DistAz daz = new Ellipsoid.DistAz();
        double az = ellps.inverseGeodesic(lon1, lat1, lon2, lat2, daz).az12;
        
        // Finn starttriangel
        int t_id = getTriangle(lon1, lat1);
        
        
        
        // Beregn retning mot sluttpunkt
        //  Bruk ellipsoid klasse
        // Finn kant ut av starttrekant,
        //  Beregn retning til alle hjørnepunkter og finn den som "passer"
        //  Beregn krysning med kant
        // Hvis den ikke finnes så er vi ferdig.
        
        
        
        
        p = new Point(lon2, lat2, getElevation(lon2, lat2));
        retval.add(p);
        return retval;
    }
    
    
    /**
     * Compute terrain elevation for position (From interface GlobeElevationModel)
     *
     * @param lon longitude
     * @param lat latitude
     * @return terrain elevation
     */
    public double    getElevation(double lon, double lat) {
        int int_lon = this.lonToInt(lon);
        int int_lat = this.latToInt(lat);
        
        for (int i=0; i<base_mesh.length; ++i) {
            for (int j=0; j<4; j++) {
                int h = getTriangleH((base_mesh[i]<<ID_SHIFT)|j, int_lon, int_lat);
                if (h != Integer.MIN_VALUE)
                    return h*scale;
            }
        }
        return 0;
    }
    
    /**
     * Compute elevation at position in this triangle.
     * If more refined triangles exist, continue down the triangle hierarchy
     * @param t Triangle id
     * @param lon Integer longitude
     * @param lat Integer latitude
     * @return
     */
    private int getTriangleH(int t, int lon, int lat) {
        if (t < 0) return Integer.MIN_VALUE;
        int id = t >> ID_SHIFT;
        int ix = t & IX_MASK;
        int id_l  = index[8*id+4+(ix+3)%4];
        int id_r  = index[8*id+4+ix];
        if (id_l < 0 || id_r < 0) return Integer.MIN_VALUE;
        int lon_t = int_lonlat[2*id];
        int lat_t = int_lonlat[2*id+1];
        int lon_l = int_lonlat[2*id_l]   - lon_t;
        int lat_l = int_lonlat[2*id_l+1] - lat_t;
        int lon_r = int_lonlat[2*id_r]   - lon_t;
        int lat_r = int_lonlat[2*id_r+1] - lat_t;
        
        double d = (double)lat_l*lon_r - (double)lat_r*lon_l;
        if (d == 0) return Integer.MIN_VALUE;
        double u = lon_r/d*(lat-lat_t) - lat_r/d*(lon-lon_t);
        double v = lat_l/d*(lon-lon_t) - lon_l/d*(lat-lat_t);
        
        if (u < 0 || v < 0 || u+v > 1)
            return Integer.MIN_VALUE;
        
        int bn = index[8*id+ix];
        if (bn < 0)
            return (int)((1-u-v)*getIntH(id) + u*getIntH(id_l) + v*getIntH(id_r));
        ix = (ix+(u>v?LEFT:RIGHT))%4;
        return getTriangleH((bn << ID_SHIFT) | ix , lon, lat);
        
    }
    
    /**
     * Get the next timestamp
     * @return
     */
    int newTs() {
        return (ts += TS_INC);
    }
    
    /**
     *
     * @return the current timestamp
     */
    int getTs() {
        return ts;
    }
    
    /**
     * Get the lowest timestamp currently in use by a view
     * @return
     */
    int getUppercutTs() {
        return uppercut_ts;
    }
    
    /**
     * Allocate a new node
     * @return
     * @throws OutOfNodeMemory
     */
    private int allocNode() {
        int retval = -1;
        
        if (fill >= size) {
            int fill_red_target = Math.min(10000, (int)(size*.05));
            // System.out.print("Out of nodes, GC'ing");
            // System.out.println(" Terrain GC Cut: " + terrain_cut_ts + " Upper cut: " + uppercut_ts);
            gc((int)(size-2*fill_red_target));
            while (terrain_cut_ts < uppercut_ts && fill > size-fill_red_target) {
                // System.out.print("Gc - fill før : " + fill);
                terrain_cut_ts += 10+(uppercut_ts - terrain_cut_ts)/2;
                if (terrain_cut_ts >= uppercut_ts) terrain_cut_ts = uppercut_ts;
                gc((int)(size-fill_red_target));
                // System.out.print(" etter : " + fill);
                // System.out.println(" Cut: " + terrain_cut_ts);
            }
        }
    /*
    while (terrain_cut_ts < uppercut_ts && fill >= size) {
      // System.out.print("Gc - fill før : " + fill);
      terrain_cut_ts += TS_INC;
      if (terrain_cut_ts >= uppercut_ts) terrain_cut_ts = uppercut_ts;
      gc(size-1000);
      // System.out.println(" etter : " + fill);
    }*/
        if (free >= 0) {
            retval = free;
            free = index[8*free];
        } else {
            if (top_free >= size)
                setSize(size*3/2);
            retval = top_free;
            ++top_free;
        }
        
        // if (retval < 0) ; // throw new OutOfNodeMemory();
        for (int i=8*retval; i<8*(retval+1); ++i)
            index[i] = -1;
        status[retval] = ts;
        int_h[retval]  = Integer.MIN_VALUE;
        int_dh[retval] = 0;
        
        ++fill;
        return retval;
    }
    
    /**
     * Deallocate a node
     * @param id
     */
    private void freeNode(int id) {
        for (int i=0; i<4; i++) {
            int p_id = index[id*8+4+i];
            if (p_id >= 0 && index[p_id*8+i] == id)
                index[p_id*8+i] = -1;
        }
        status[id] = 0;
        index[id*8] = free;
        free = id;
        --fill;
    }
    
    /**
     * Preform garbage collection of nodes,
     * free all nodes that has not been used for a while
     */
    private int [] gc_nodes = new int [1 << 12];
    private synchronized void gc(int fill_target) {
        // System.out.print("GC: ");
        // long start_time = System.currentTimeMillis();
        // int num_hi_ts = 0;
        // int num_has_children = 0;
        // int num_removed = 0;
        int gc_nodes_num = 0;
        for (int i = size; i >= 0; --i) {
            if (++gc_id >= size) gc_id = 0;
            if (fill - gc_nodes_num < fill_target) break;
            if (i%1000 == 0) {
        /*
        if (System.currentTimeMillis() - start_time > 100)
          return; */
                Thread.yield();
            }
            if (status[gc_id] == 0);
            else if (status[gc_id] >= terrain_cut_ts - TS_INC)
                ; //num_hi_ts++;
            else if (!(index[gc_id*8] < 0 &&
                    index[gc_id*8+1] < 0 &&
                    index[gc_id*8+2] < 0 &&
                    index[gc_id*8+3] < 0))
                ; //num_has_children++;
            else if (int_h[gc_id] == Integer.MIN_VALUE) {
                if (gc_nodes_num >= gc_nodes.length) {
                    int [] tmp = new int [gc_nodes.length*2];
                    System.arraycopy(gc_nodes, 0, tmp, 0, gc_nodes.length);
                    gc_nodes = tmp;
                }
                gc_nodes[gc_nodes_num++] = gc_id;
/*
        if (i < size && source.removeQuery(gc_id)) {
          num_removed++;
          freeNode(gc_id);
        }
 */
            } else {
                // num_removed++;
                freeNode(gc_id);
            }
        }
        if (gc_nodes_num > 0) {
            gc_nodes_num = source.removeQueries(gc_nodes, gc_nodes_num);
            for (int i = 0; i < gc_nodes_num; ++i)
                freeNode(gc_nodes[i]);
        }
        // System.out.println((System.currentTimeMillis() - start_time) + " ms");
        
    }
    
    /**
     * Get child triangle, create base node if necessary.
     *
     * @param t_id
     * @param ch_ix Child direction, LEFT or RIGHT as seen from top node
     * @return
     */
    int getChild(int t_id, int ch_ix)  {
        return getChild(t_id, ch_ix, true);
    }
    int getChild(int t_id, int ch_ix, boolean create)  {
        if (t_id < 0 || !(ch_ix == LEFT || ch_ix == RIGHT)) return ERROR;
        int id = getBaseNode(t_id, create);
        // return from getBaseNode == -1 only if it is impossible to allocate baseNode
        if (id < 0) return ERROR;
        
        int ix = ((t_id & IX_MASK)+ch_ix)%4;
        int ch = (id << ID_SHIFT) | ix;
        return ch;
    }
    
    /**
     * Get parent of triangle (if exists)
     *
     * @param t_id
     * @return
     */
    int getParent(int t_id) {
        if (t_id < 0) return ERROR;
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        
        int tmp_id = index[8*id+4+ix];
        if (tmp_id >= 0 && index[8*tmp_id+ix] == id) {
            return (tmp_id << ID_SHIFT) | ix;
        }
        int tmp_ix = (ix+3)%4;
        tmp_id = index[8*id+4+tmp_ix];
        if (tmp_id >= 0 && index[8*tmp_id+tmp_ix] == id) {
            return (tmp_id << ID_SHIFT) | tmp_ix;
        }
        return ERROR;
    }
    
    /**
     * Get neighbour of triangle, if possible.
     *
     * @param t_id
     * @param n_ix Direction as seen from top node
     * @return
     */
    int getNeighbour(int t_id, int n_ix) {
        if (t_id < 0) return ERROR;
        
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        
        
        if (n_ix == LEFT || n_ix == RIGHT) {
            if (index[8*id+4+(n_ix+1+ix)%4] < 0) return ERROR;
            ix = (ix+2*n_ix+1)%4;
            t_id =  (id << ID_SHIFT) | ix;
            int n_id = getChild(t_id, (n_ix+1)%2, false);
            return n_id >= 0 ? n_id : t_id;
        }
        if (n_ix == BASE) {
            int ch_ix = ERROR;
            int p_ix  = ix;
            int p_id  = index[8*id+4+p_ix];
            if (p_id >= 0 && index[8*p_id+p_ix] == id)
                ch_ix = RIGHT;
            else {
                p_ix = (ix+3)%4;
                p_id = index[8*id+4+p_ix];
                if (p_id >= 0 && index[8*p_id+p_ix] == id)
                    ch_ix = LEFT;
            }
            if (ch_ix == ERROR) return ERROR;
            if (index[8*p_id+4+(ch_ix+1+p_ix)%4] < 0) return ERROR;
            p_ix = (p_ix+2*ch_ix+1)%4;
            p_id = (p_id<<ID_SHIFT) | p_ix;
            int n_id = getChild(p_id, (ch_ix+1)%2, false);
            return n_id >=0 ? n_id : p_id;
            
        /*
            int p_id = ERROR;
            int ch_ix = ERROR;
         
            int tmp_id = index[8*id+4+ix];
            if (tmp_id >= 0 && index[8*tmp_id+ix] == id) {
                p_id = (tmp_id << ID_SHIFT) | ix;
                ch_ix = RIGHT;
            } else {
                int tmp_ix = (ix+3)%4;
                tmp_id = index[8*id+4+tmp_ix];
                if (tmp_id >= 0 && index[8*tmp_id+tmp_ix] == id) {
                    p_id = (tmp_id << ID_SHIFT) | tmp_ix;
                    ch_ix = LEFT;
                }
            }
            p_id = getNeighbour(p_id, ch_ix);
            return getChild(p_id, (ch_ix+1)%2);
         */
        }
        return ERROR;
    }
    
    /**
     * Get neighbour of edge, if possible.
     *
     * @param t_id
     * @param n_ix Direction as seen from top node
     * @return
     */
    int getNeighbourEdge(int e_id) {
        if (e_id < 0) return ERROR;
        
        int t_id = e_id >> ID_SHIFT;
        int n_ix = e_id & IX_MASK;
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        
        if (n_ix == LEFT || n_ix == RIGHT) {
            if (index[8*id+4+(n_ix+1+ix)%4] < 0) return ERROR;
            ix = (ix+2*n_ix+1)%4;
            t_id =  (id << ID_SHIFT) | ix;
            int n_id = getChild(t_id, (n_ix+1)%2, false);
            if (n_id >= 0)
                return (n_id << ID_SHIFT) | BASE;
            return (t_id << ID_SHIFT) | ((n_ix+1)%2);
        }
        if (n_ix == BASE) {
            int ch_ix = ERROR;
            int p_ix  = ix;
            int p_id  = index[8*id+4+p_ix];
            if (p_id >= 0 && index[8*p_id+p_ix] == id)
                ch_ix = RIGHT;
            else {
                p_ix = (ix+3)%4;
                p_id = index[8*id+4+p_ix];
                if (p_id >= 0 && index[8*p_id+p_ix] == id)
                    ch_ix = LEFT;
            }
            if (ch_ix == ERROR) return ERROR;
            if (index[8*p_id+4+(ch_ix+1+p_ix)%4] < 0) return ERROR;
            p_ix = (p_ix+2*ch_ix+1)%4;
            p_id = (p_id<<ID_SHIFT) | p_ix;
            int n_id = getChild(p_id, (ch_ix+1)%2, false);
            if (n_id >= 0)
                return (n_id << ID_SHIFT) | BASE;
            return (p_id << ID_SHIFT) | ((ch_ix+1)%2);
        }
        return ERROR;
    }
    
    
    /**
     * Get  base neighbour of triangle, if possible.
     * Create base point etc..  as necessary
     *
     * @param t_id
     * @param n_ix Direction as seen from top node
     * @return
     */
    int getBaseNeighbour(int t_id) {
        if (t_id < 0) return ERROR;
        
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        
        int ch_ix = ERROR;
        int p_ix  = ix;
        int p_id  = index[8*id+4+p_ix];
        if (p_id >= 0 && index[8*p_id+p_ix] == id)
            ch_ix = RIGHT;
        else {
            p_ix = (ix+3)%4;
            p_id = index[8*id+4+p_ix];
            if (p_id >= 0 && index[8*p_id+p_ix] == id)
                ch_ix = LEFT;
        }
        if (ch_ix == ERROR) return ERROR;
        if (index[8*p_id+4+(ch_ix+1+p_ix)%4] < 0) return ERROR;
        p_ix = (p_ix+2*ch_ix+1)%4;
        return getChild((p_id<<ID_SHIFT) | p_ix, (ch_ix+1)%2);
    }
    
    /**
     * Get vertex of triangle
     * @param t_id Triangle id
     * @param n_ix Node direction, as seen from top vertex
     * @return
     */
    int getNode(int t_id, int n_ix) {
        if (t_id < 0) return ERROR;
        
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        if (n_ix == TOP) return id;
        if (n_ix == LEFT || n_ix == RIGHT)
            return index[8*id+4+(3*n_ix+ix)%4];
        return ERROR;
    }
    
    /**
     * Get, and possibly create base node
     * @param t_id
     * @return
     */
    int getBaseNode(int t_id) {
        return getBaseNode(t_id, true);
    }
    
    synchronized int getBaseNode(int t_id, boolean create) {
        if (t_id < 0) return ERROR;
        
        int id = t_id >> ID_SHIFT;
        int ix = t_id & IX_MASK;
        int bn = index[8*id+ix];
        if (bn >= 0) {
            if (status[bn] < ts)
                status[bn] = ts;
            return bn;
        }
        if (!create)
            return ERROR;
        
        // Search for neighbour to share basenode with
        int bt_id = getBaseNeighbour(t_id);
        
        // Create new base node
        bn = allocNode();
        if (bt_id != ERROR) {
            int p_id = bt_id >> ID_SHIFT;
            int p_ix = bt_id & IX_MASK;
            // t child is bn
            index[8*p_id+p_ix] = bn;
            // bn parent is t
            index[8*bn+4+p_ix] = p_id;
        }
        
        // Fill new node with data
        // This child is bn
        index[8*id+ix] = bn;
        // Bn parent is this
        index[8*bn+4+ix] = id;
        int right_n = getNode(t_id, RIGHT);
        int left_n  = getNode(t_id, LEFT);
        index[8*bn+4+(ix+1)%4] = left_n;
        index[8*bn+4+(ix+3)%4] = right_n;
        
        // Compute coordinates from left and right node
        int left_lon = int_lonlat[left_n*2];
        int left_lat = int_lonlat[left_n*2+1];
        int right_lon = int_lonlat[right_n*2];
        int right_lat = int_lonlat[right_n*2+1];
        // int top_lon = int_lonlat[id*2];
        // int top_lat = int_lonlat[id*2+1];
        
        int lon = (left_lon - right_lon)/2 + right_lon;
        int lat = (left_lat - right_lat)/2 + right_lat;
        int_lonlat[bn*2] = lon;
        int_lonlat[bn*2+1] = lat;
        // int_dh[bn] = (int)(.1*(int_dh[left_n]+int_dh[right_n])/2);
        
        computeCartesian(bn);
        computeObjectRadius(bn);
        computeTextureCoordinates2D(bn);
        
        // Post query for updated data
        source.addQuery(bn);
        return bn;
    }
    
    /**
     * Set the height and height uncertainty for a node
     *
     * @param id
     * @param h
     * @param dh
     */
    void setNodeValues(int id, int h, int dh) {
        if (int_h[id] == h && int_dh[id] == dh) return;
        int_h[id]  = h;
        int_dh[id] = dh;
        
        // Set cartesian invalid
        int_cartesian[id*3] = Integer.MAX_VALUE;
        
        clearGrandchildCartesian(id);
        
        // computeCartesian(id);
        // computeObjectRadius(id);
    }
    
    private void clearParentRadies(int id) {
        for (int i=0; i<4; ++i) {
            int p_id = index[8*id+4+i];
            if (p_id >= 0 && index[8*p_id+i] == id) {
                if (own_obj_radius[p_id] == 0 && tot_obj_radius[p_id] == 0)
                    continue;
                own_obj_radius[p_id] = 0;
                tot_obj_radius[p_id] = 0;
                clearParentRadies(p_id);
            }
        }
    }
    
    private void clearGrandchildCartesian(int id) {
        for (int i=0; i<4; ++i) {
            int ch_id = index[8*id+i];
            if (ch_id >= 0) {
                for (int j=0; j<4; ++j) {
                    int gch_id = index[8*ch_id+j];
                    if (gch_id >= 0 && int_h[gch_id] <= Integer.MIN_VALUE+1 &&
                            (index[8*gch_id+4+(j+1)%4] == id || index[8*gch_id+4+(j+3)%4] == id)) {
                        int_cartesian[gch_id*3] = Integer.MAX_VALUE;
                        clearGrandchildCartesian(gch_id);
                    }
                }
            }
        }
    }
    
    /**
     * Compute longitude (in radians) from internal integer coordinate
     * @param intLon The integer easting in projected coordinates
     * @return The longitude (radians)
     */
    public double intToLon(int intLon) {
        return intLon * -Math.PI / Integer.MIN_VALUE;
    }
    
    /**
     * Compute latitude (in radians) from internal integer coordinate
     * @param intLat The integer northing in projected coordinates
     * @return The latitude in radians)\
     */
    public double intToLat(int intLat) {
        double expval = Math.exp(intLat*int_lat_scale);
        return (expval-1.)/(expval+1.)/lat_scale;
    }
    
    /**
     * Compute intenal integer coordinate from longitude (in radians)
     * @param lon The longitide in radians
     * @return The projected integer easting 
     */
    public int lonToInt(double lon) {
        return  (int)(lon*Integer.MIN_VALUE/-Math.PI);
    }
    
    /**
     * Compute internal integer coordinate from latitude (in radians)
     * @param lat The latitude in radians
     * @return The projected integer northing
     */
    public int latToInt(double lat) {
        lat *= lat_scale;
        double expval = (1.+lat)/(1.-lat);
        double intLatD = Math.log(expval)/int_lat_scale;
        return (int)(intLatD);
    }
    
    /**
     * The X - Y aspect ratio. The number to multiply a X (east - west)
     * coordinate difference with to get it into the same scale as the Y.
     * @param lat The latitude of the position (radians)
     * @return The scale ratio
     */
    public double getAspect(double lat) {
        double v = Math.sin(lat);
        v *= v * ellps.getE2();
        v = (1.-v)*Math.cos(lat);
        return 1./v;
    }
    
    /**
     * Compute the distance between two projected positions
     * @param int_lon1 Easting of first point 
     * @param int_lat1 Northing of second point 
     * @param int_lon2 Easting of second point 
     * @param int_lat2 Northing of second point 
     * @return The distance
     */
    public double dist2D(int int_lon1, int int_lat1, int int_lon2, int int_lat2) {
        
        double dy = int_lat2 - int_lat1;
        double dx = (int_lon2 - int_lon1)/8;
        double r = ellps.getA();
        double dist = r*Math.sqrt(dx*dx+dy*dy)*int_lat_scale/(8*lat_scale);
        double cosh_y = int_lat_scale*(int_lat1+dy/2)/4;
        cosh_y = Math.exp(cosh_y);
        cosh_y = (cosh_y + 1/cosh_y)/2;
        dist /= (cosh_y*cosh_y);
        return dist;
/*
    double lon1 = intToLon(int_lon1);
    double lat1 = intToLon(int_lat1);
    double lon2 = intToLon(int_lon2);
    double lat2 = intToLon(int_lat2);
 
    double dlon = ellps.adjlon(lon2-lon1)*Math.cos((lat2+lat1)/2);
    double dlat = lat2-lat1;
    double dist2 = ellps.getA()*Math.sqrt(dlon*dlon+dlat*dlat);
 
    if (dist2 < 10000.) {
      System.out.println("Dist1 : " + dist + " dist2 : " + dist2 + " dist1/dist2 : " + dist/dist2);
    }
 
    return dist2;
 */
    }
    
    
    /**
     * Compute height value from internal coordinate,
     * use scaling factor (elevation exaggeration)
     *
     * @param h
     * @return
     */
    double intToH(int h) {
        return h*scale*h_scale;
    }
    
    /**
     * Find the internal coordinate height for a node,
     * if the node has "undefined" height;
     * interpolate from the height of parents
     *
     * @param id
     * @return
     */
    int getIntH(int id) {
        if (int_h[id] > Integer.MIN_VALUE+1)
            return int_h[id];
        
        for (int i = 0; i < 2; i++) {
            int p_id_1 = index[8*id+4+i];
            int p_id_2 = index[8*id+4+(i+2)%4];
            if (p_id_1 >= 0 && index[8*p_id_1+i] != id &&
                    p_id_2 >= 0 && index[8*p_id_2+(i+2)%4] != id) {
                int h_1 = getIntH(p_id_1);
                int h_2 = getIntH(p_id_2);
                
                return (h_1 + h_2)/2;
            }
        }
        
        return 0;
    }
    
    /**
     * Compute the cartesian, earth centered position of this node
     * @param id
     */
    private void computeCartesian(int id) {
        ellps.toCartesian(intToLat(int_lonlat[id*2+1]),
                intToLon(int_lonlat[id*2+0]),
                intToH(getIntH(id)), p);
        
        int_cartesian[id*3+0] = (int)(p.x/scale);
        int_cartesian[id*3+1] = (int)(p.y/scale);
        int_cartesian[id*3+2] = (int)(p.z/scale);
        own_obj_radius[id] = 0;
        tot_obj_radius[id] = 0;
        // Compute perspective texture coordinates
    }
    
    /**
     * Compute integer base texture coordinates for this node
     * @param id
     */
    private void computeTextureCoordinates2D(int id) {
        // Compute non-perspective texture coordinates
        for (int i=0; i<num_textures2D; i++)
            textures2D[i].computeTexCoo(id);
    }
    
    
    /**
     * Compute distance between two nodes
     * @param id_1
     * @param id_2
     * @return
     */
    private double distanceSqr(int id_1, int id_2) {
        double x = int_cartesian[id_1*3+0] - int_cartesian[id_2*3+0];
        double y = int_cartesian[id_1*3+1] - int_cartesian[id_2*3+1];
        double z = int_cartesian[id_1*3+2] - int_cartesian[id_2*3+2];
        return (x*x+y*y+z*z)*scale*scale;
    }
    
    /**
     * Compute object radius for the triangle pair that has this node as a base node
     * @param id
     */
    private void computeObjectRadius(int id) {
        for (int i = 0; i < 4; i++) {
            int n_id = index[8*id+4+i];
            if (n_id >= 0) {
                double dist_sqr = distanceSqr(id, n_id);
                if (dist_sqr > own_obj_radius[id]*own_obj_radius[id])
                    own_obj_radius[id] = (float)Math.sqrt(dist_sqr);
            }
        }
        if (own_obj_radius[id] > tot_obj_radius[id])
            tot_obj_radius[id] = own_obj_radius[id];
        
        for (int i = 0; i < 4; i++) {
            int ch_id = index[8*id+i];
            if (ch_id >= 0) {
                if (int_dh[ch_id] > int_dh[id])
                    int_dh[id] = int_dh[ch_id];
                float dist = (float)Math.sqrt(distanceSqr(id, ch_id));
                if (tot_obj_radius[id] < dist + tot_obj_radius[ch_id])
                    tot_obj_radius[id] = dist + tot_obj_radius[ch_id];
            }
        }
        
        // updateParentObjectRadius(id);
        float dist = own_obj_radius[id]*2;
        float r = (float)ellps.getA();
        tot_dev[id] = dist*dist/(8*r) + (float)(int_dh[id]*scale*h_scale);
    }
    
    private void computeAllObjectRadius(int id, int p_id, float p_dist) {
        if (tot_obj_radius[id] > 0)
            return;
        if (p_dist > own_obj_radius[id])
            own_obj_radius[id] = p_dist;
        for (int i = 0; i < 4; i++) {
            int n_id = index[8*id+4+i];
            if (n_id >= 0 && n_id != p_id) {
                double dist_sqr = distanceSqr(id, n_id);
                if (dist_sqr > own_obj_radius[id]*own_obj_radius[id])
                    own_obj_radius[id] = (float)Math.sqrt(dist_sqr);
            }
        }
        tot_obj_radius[id] = own_obj_radius[id];
        for (int i = 0; i < 4; i++) {
            int ch_id = index[8*id+i];
            if (ch_id >= 0) {
                if (int_dh[ch_id] > int_dh[id])
                    int_dh[id] = int_dh[ch_id];
                float dist = (float)Math.sqrt(distanceSqr(id, ch_id));
                computeAllObjectRadius(ch_id, id, dist);
                if (tot_obj_radius[id] < dist + tot_obj_radius[ch_id])
                    tot_obj_radius[id] = dist + tot_obj_radius[ch_id];
            }
        }
        float dist = own_obj_radius[id]*2;
        float r = (float)ellps.getA();
        tot_dev[id] = dist*dist/(8*r) + (float)(int_dh[id]*scale*h_scale);
    }
    
    
    /**
     * Update the object radies for the parents of this node
     * @param id
     */
  /*
  private void updateParentObjectRadius(int id) {
   //  int num_parents = 0;
    // float parent_h = 0;
   
    for (int i = 0; i < 4; i++) {
      int p_id = index[8*id+4+i];
      if (p_id >= 0 && index[8*p_id+i] == id) {
        float dist = (float)Math.sqrt(distanceSqr(id, p_id)) + tot_obj_radius[id];
        boolean p_update = false;
        if (dist > tot_obj_radius[p_id]) {
          tot_obj_radius[p_id] = dist;
          p_update = true;
        }
        if (int_dh[id] > int_dh[p_id]) {
          int_dh[p_id] = int_dh[id];
          p_update = true;
        }
        if (p_update)
          updateParentObjectRadius(p_id);
      }
    }
    float dist = own_obj_radius[id]*2;
    float r = (float)ellps.getA();
    tot_dev[id]  = dist*dist/(8*r);
    tot_dev[id] += int_dh[id]*scale*h_scale;
  }
   */
    /**
     * Create and fill the basemesh data structures
     */
    private void createBasemesh() {
        // Create basemesh
        int [][] p = new int [4][9];
        for (int i = 0, lon = Integer.MIN_VALUE; i < 4; i++, lon += (1<<30)) {
            for (int j = 0, lat = -(1<<30); j <= 8; j++, lat += (1<<28)) {
                int p_id = allocNode();
                int_lonlat[p_id*2]   = lon;
                int_lonlat[p_id*2+1] = lat;
                status[p_id] = Integer.MAX_VALUE;
                p[i][j] = p_id;
                
                computeCartesian(p_id);
                computeTextureCoordinates2D(p_id);
                source.addQuery(p_id);
            }
        }
        
        // Create all connections
        
        // Level 0
        // Children of 2, 2
        index[p[2][2]*8+0] = p[2][4];
        index[p[2][2]*8+1] = p[0][2];
        index[p[2][2]*8+2] = p[2][0];
        index[p[2][2]*8+3] = p[0][2];
        
        // Children of 2, 6
        index[p[2][6]*8+0] = p[2][8];
        index[p[2][6]*8+1] = p[0][6];
        index[p[2][6]*8+2] = p[2][4];
        index[p[2][6]*8+3] = p[0][6];
        
        // Level 1
        // Parents of 2, 0
        index[p[2][0]*8+4+0] = -1;
        index[p[2][0]*8+4+1] = p[0][0];
        index[p[2][0]*8+4+2] = p[2][2];
        index[p[2][0]*8+4+3] = p[0][0];
        
        // Children of 2, 0
        index[p[2][0]*8+0] = -1;
        index[p[2][0]*8+1] = -1;
        index[p[2][0]*8+2] = p[1][1];
        index[p[2][0]*8+3] = p[3][1];
        
        // Parents of 0, 2
        index[p[0][2]*8+4+0] = p[0][0];
        index[p[0][2]*8+4+1] = p[2][2];
        index[p[0][2]*8+4+2] = p[0][4];
        index[p[0][2]*8+4+3] = p[2][2];
        
        // Children of 0, 2
        index[p[0][2]*8+0] = p[1][1];
        index[p[0][2]*8+1] = p[3][1];
        index[p[0][2]*8+2] = p[3][3];
        index[p[0][2]*8+3] = p[1][3];
        
        // Parents of 2, 4
        index[p[2][4]*8+4+0] = p[2][2];
        index[p[2][4]*8+4+1] = p[0][4];
        index[p[2][4]*8+4+2] = p[2][6];
        index[p[2][4]*8+4+3] = p[0][4];
        
        // Children of 2, 4
        index[p[2][4]*8+0] = p[3][3];
        index[p[2][4]*8+1] = p[1][3];
        index[p[2][4]*8+2] = p[1][5];
        index[p[2][4]*8+3] = p[3][5];
        
        // Parents of 0, 6
        index[p[0][6]*8+4+0] = p[0][4];
        index[p[0][6]*8+4+1] = p[2][6];
        index[p[0][6]*8+4+2] = p[0][8];
        index[p[0][6]*8+4+3] = p[2][6];
        
        // Children of 0, 6
        index[p[0][6]*8+0] = p[1][5];
        index[p[0][6]*8+1] = p[3][5];
        index[p[0][6]*8+2] = p[3][7];
        index[p[0][6]*8+3] = p[1][7];
        
        // Parents of 2, 8
        index[p[2][8]*8+4+0] = p[2][6];
        index[p[2][8]*8+4+1] = p[0][8];
        index[p[2][8]*8+4+2] = -1;
        index[p[2][8]*8+4+3] = p[0][8];
        
        // Children of 2, 8
        index[p[2][8]*8+0] = p[3][7];
        index[p[2][8]*8+1] = p[1][7];
        index[p[2][8]*8+2] = -1;
        index[p[2][8]*8+3] = -1;
        
        // Level 2
        // Parents of 1, 1
        index[p[1][1]*8+4+0] = p[0][2];
        index[p[1][1]*8+4+1] = p[2][2];
        index[p[1][1]*8+4+2] = p[2][0];
        index[p[1][1]*8+4+3] = p[0][0];
        
        // Children of 1, 1
        index[p[1][1]*8+0] = p[0][1];
        index[p[1][1]*8+1] = p[1][2];
        index[p[1][1]*8+2] = p[2][1];
        index[p[1][1]*8+3] = p[1][0];
        
        // Parents of 3, 1
        index[p[3][1]*8+4+0] = p[2][2];
        index[p[3][1]*8+4+1] = p[0][2];
        index[p[3][1]*8+4+2] = p[0][0];
        index[p[3][1]*8+4+3] = p[2][0];
        
        // Children of 3, 1
        index[p[3][1]*8+0] = p[2][1];
        index[p[3][1]*8+1] = p[3][2];
        index[p[3][1]*8+2] = p[0][1];
        index[p[3][1]*8+3] = p[3][0];
        
        // Parents of 1, 3
        index[p[1][3]*8+4+0] = p[0][4];
        index[p[1][3]*8+4+1] = p[2][4];
        index[p[1][3]*8+4+2] = p[2][2];
        index[p[1][3]*8+4+3] = p[0][2];
        
        // Children of 1, 3
        index[p[1][3]*8+0] = p[0][3];
        index[p[1][3]*8+1] = p[1][4];
        index[p[1][3]*8+2] = p[2][3];
        index[p[1][3]*8+3] = p[1][2];
        
        // Parents of 3, 3
        index[p[3][3]*8+4+0] = p[2][4];
        index[p[3][3]*8+4+1] = p[0][4];
        index[p[3][3]*8+4+2] = p[0][2];
        index[p[3][3]*8+4+3] = p[2][2];
        
        // Children of 3, 3
        index[p[3][3]*8+0] = p[2][3];
        index[p[3][3]*8+1] = p[3][4];
        index[p[3][3]*8+2] = p[0][3];
        index[p[3][3]*8+3] = p[3][2];
        
        // Parents of 1, 5
        index[p[1][5]*8+4+0] = p[0][6];
        index[p[1][5]*8+4+1] = p[2][6];
        index[p[1][5]*8+4+2] = p[2][4];
        index[p[1][5]*8+4+3] = p[0][4];
        
        // Children of 1, 5
        index[p[1][5]*8+0] = p[0][5];
        index[p[1][5]*8+1] = p[1][6];
        index[p[1][5]*8+2] = p[2][5];
        index[p[1][5]*8+3] = p[1][4];
        
        // Parents of 3, 5
        index[p[3][5]*8+4+0] = p[2][6];
        index[p[3][5]*8+4+1] = p[0][6];
        index[p[3][5]*8+4+2] = p[0][4];
        index[p[3][5]*8+4+3] = p[2][4];
        
        // Children of 3, 5
        index[p[3][5]*8+0] = p[2][5];
        index[p[3][5]*8+1] = p[3][6];
        index[p[3][5]*8+2] = p[0][5];
        index[p[3][5]*8+3] = p[3][4];
        
        // Parents of 1, 7
        index[p[1][7]*8+4+0] = p[0][8];
        index[p[1][7]*8+4+1] = p[2][8];
        index[p[1][7]*8+4+2] = p[2][6];
        index[p[1][7]*8+4+3] = p[0][6];
        
        // Children of 1, 7
        index[p[1][7]*8+0] = p[0][7];
        index[p[1][7]*8+1] = p[1][8];
        index[p[1][7]*8+2] = p[2][7];
        index[p[1][7]*8+3] = p[1][6];
        
        // Parents of 3, 7
        index[p[3][7]*8+4+0] = p[2][8];
        index[p[3][7]*8+4+1] = p[0][8];
        index[p[3][7]*8+4+2] = p[0][6];
        index[p[3][7]*8+4+3] = p[2][6];
        
        // Children of 3, 7
        index[p[3][7]*8+0] = p[2][7];
        index[p[3][7]*8+1] = p[3][8];
        index[p[3][7]*8+2] = p[0][7];
        index[p[3][7]*8+3] = p[3][6];
        
        // Level 3
        // Parents of 1, 0
        index[p[1][0]*8+4+0] = p[2][0];
        index[p[1][0]*8+4+1] = -1;
        index[p[1][0]*8+4+2] = p[0][0];
        index[p[1][0]*8+4+3] = p[1][1];
        
        // Parents of 3, 0
        index[p[3][0]*8+4+0] = p[0][0];
        index[p[3][0]*8+4+1] = -1;
        index[p[3][0]*8+4+2] = p[2][0];
        index[p[3][0]*8+4+3] = p[3][1];
        
        // Parents of 0, 1
        index[p[0][1]*8+4+0] = p[1][1];
        index[p[0][1]*8+4+1] = p[0][0];
        index[p[0][1]*8+4+2] = p[3][1];
        index[p[0][1]*8+4+3] = p[0][2];
        
        // Parents of 2, 1
        index[p[2][1]*8+4+0] = p[3][1];
        index[p[2][1]*8+4+1] = p[2][0];
        index[p[2][1]*8+4+2] = p[1][1];
        index[p[2][1]*8+4+3] = p[2][2];
        
        // Parents of 1, 2
        index[p[1][2]*8+4+0] = p[2][2];
        index[p[1][2]*8+4+1] = p[1][1];
        index[p[1][2]*8+4+2] = p[0][2];
        index[p[1][2]*8+4+3] = p[1][3];
        
        // Parents of 3, 2
        index[p[3][2]*8+4+0] = p[0][2];
        index[p[3][2]*8+4+1] = p[3][1];
        index[p[3][2]*8+4+2] = p[2][2];
        index[p[3][2]*8+4+3] = p[3][3];
        
        // Parents of 0, 3
        index[p[0][3]*8+4+0] = p[1][3];
        index[p[0][3]*8+4+1] = p[0][2];
        index[p[0][3]*8+4+2] = p[3][3];
        index[p[0][3]*8+4+3] = p[0][4];
        
        // Parents of 2, 3
        index[p[2][3]*8+4+0] = p[3][3];
        index[p[2][3]*8+4+1] = p[2][2];
        index[p[2][3]*8+4+2] = p[1][3];
        index[p[2][3]*8+4+3] = p[2][4];
        
        // Parents of 1, 4
        index[p[1][4]*8+4+0] = p[2][4];
        index[p[1][4]*8+4+1] = p[1][3];
        index[p[1][4]*8+4+2] = p[0][4];
        index[p[1][4]*8+4+3] = p[1][5];
        
        // Parents of 3, 4
        index[p[3][4]*8+4+0] = p[0][4];
        index[p[3][4]*8+4+1] = p[3][3];
        index[p[3][4]*8+4+2] = p[2][4];
        index[p[3][4]*8+4+3] = p[3][5];
        
        // Parents of 0, 5
        index[p[0][5]*8+4+0] = p[1][5];
        index[p[0][5]*8+4+1] = p[0][4];
        index[p[0][5]*8+4+2] = p[3][5];
        index[p[0][5]*8+4+3] = p[0][6];
        
        // Parents of 2, 5
        index[p[2][5]*8+4+0] = p[3][5];
        index[p[2][5]*8+4+1] = p[2][4];
        index[p[2][5]*8+4+2] = p[1][5];
        index[p[2][5]*8+4+3] = p[2][6];
        
        // Parents of 1, 6
        index[p[1][6]*8+4+0] = p[2][6];
        index[p[1][6]*8+4+1] = p[1][5];
        index[p[1][6]*8+4+2] = p[0][6];
        index[p[1][6]*8+4+3] = p[1][7];
        
        // Parents of 3, 6
        index[p[3][6]*8+4+0] = p[0][6];
        index[p[3][6]*8+4+1] = p[3][5];
        index[p[3][6]*8+4+2] = p[2][6];
        index[p[3][6]*8+4+3] = p[3][7];
        
        // Parents of 0, 7
        index[p[0][7]*8+4+0] = p[1][7];
        index[p[0][7]*8+4+1] = p[0][6];
        index[p[0][7]*8+4+2] = p[3][7];
        index[p[0][7]*8+4+3] = p[0][8];
        
        // Parents of 2, 7
        index[p[2][7]*8+4+0] = p[3][7];
        index[p[2][7]*8+4+1] = p[2][6];
        index[p[2][7]*8+4+2] = p[1][7];
        index[p[2][7]*8+4+3] = p[2][8];
        
        // Parents of 1, 8
        index[p[1][8]*8+4+0] = p[2][8];
        index[p[1][8]*8+4+1] = p[1][7];
        index[p[1][8]*8+4+2] = p[0][8];
        index[p[1][8]*8+4+3] = -1;
        
        // Parents of 3, 8
        index[p[3][8]*8+4+0] = p[0][8];
        index[p[3][8]*8+4+1] = p[3][7];
        index[p[3][8]*8+4+2] = p[2][8];
        index[p[3][8]*8+4+3] = -1;
        
        
        // Fill the base mesh with base nodes
        base_mesh = new int [32];
        
        base_mesh[0]  = getBaseNode(p[1][0] << ID_SHIFT | 0);
        base_mesh[1]  = getBaseNode(p[1][0] << ID_SHIFT | 3);
        base_mesh[2]  = getBaseNode(p[3][0] << ID_SHIFT | 0);
        base_mesh[3]  = getBaseNode(p[3][0] << ID_SHIFT | 3);
        
        base_mesh[4]  = getBaseNode(p[1][2] << ID_SHIFT | 0);
        base_mesh[5]  = getBaseNode(p[1][2] << ID_SHIFT | 1);
        base_mesh[6]  = getBaseNode(p[1][2] << ID_SHIFT | 2);
        base_mesh[7]  = getBaseNode(p[1][2] << ID_SHIFT | 3);
        base_mesh[8]  = getBaseNode(p[3][2] << ID_SHIFT | 0);
        base_mesh[9]  = getBaseNode(p[3][2] << ID_SHIFT | 1);
        base_mesh[10] = getBaseNode(p[3][2] << ID_SHIFT | 2);
        base_mesh[11] = getBaseNode(p[3][2] << ID_SHIFT | 3);
        
        base_mesh[12] = getBaseNode(p[1][4] << ID_SHIFT | 0);
        base_mesh[13] = getBaseNode(p[1][4] << ID_SHIFT | 1);
        base_mesh[14] = getBaseNode(p[1][4] << ID_SHIFT | 2);
        base_mesh[15] = getBaseNode(p[1][4] << ID_SHIFT | 3);
        base_mesh[16] = getBaseNode(p[3][4] << ID_SHIFT | 0);
        base_mesh[17] = getBaseNode(p[3][4] << ID_SHIFT | 1);
        base_mesh[18] = getBaseNode(p[3][4] << ID_SHIFT | 2);
        base_mesh[19] = getBaseNode(p[3][4] << ID_SHIFT | 3);
        
        base_mesh[20] = getBaseNode(p[1][6] << ID_SHIFT | 0);
        base_mesh[21] = getBaseNode(p[1][6] << ID_SHIFT | 1);
        base_mesh[22] = getBaseNode(p[1][6] << ID_SHIFT | 2);
        base_mesh[23] = getBaseNode(p[1][6] << ID_SHIFT | 3);
        base_mesh[24] = getBaseNode(p[3][6] << ID_SHIFT | 0);
        base_mesh[25] = getBaseNode(p[3][6] << ID_SHIFT | 1);
        base_mesh[26] = getBaseNode(p[3][6] << ID_SHIFT | 2);
        base_mesh[27] = getBaseNode(p[3][6] << ID_SHIFT | 3);
        
        base_mesh[28] = getBaseNode(p[1][8] << ID_SHIFT | 2);
        base_mesh[29] = getBaseNode(p[1][8] << ID_SHIFT | 1);
        base_mesh[30] = getBaseNode(p[3][8] << ID_SHIFT | 2);
        base_mesh[31] = getBaseNode(p[3][8] << ID_SHIFT | 1);
        
        base_code = new String[32];
        base_code[0]  = "1211";
        base_code[1]  = "1210";
        base_code[2]  = "1201";
        base_code[3]  = "1200";
        
        base_code[4]  = "1231";
        base_code[5]  = "1213";
        base_code[6]  = "1212";
        base_code[7]  = "1230";
        
        base_code[8]  = "1221";
        base_code[9]  = "1203";
        base_code[10] = "1202";
        base_code[11] = "1220";
        
        base_code[12] = "3011";
        base_code[13] = "1233";
        base_code[14] = "1232";
        base_code[15] = "3010";
        
        base_code[16] = "3001";
        base_code[17] = "1223";
        base_code[18] = "1222";
        base_code[19] = "3000";
        
        base_code[20] = "3031";
        base_code[21] = "3013";
        base_code[22] = "3012";
        base_code[23] = "3030";
        
        base_code[24] = "3021";
        base_code[25] = "3003";
        base_code[26] = "3002";
        base_code[27] = "3020";
        
        base_code[28] = "3032";
        base_code[29] = "3033";
        base_code[30] = "3022";
        base_code[31] = "3023";
        
        for (int i=0; i< base_mesh.length; i++)
            status[base_mesh[i]] = Integer.MAX_VALUE;
    }
    
    /**
     * Initialize data arrays
     * @param size
     */
    private synchronized void setSize(int sz) {
        
        if (sz < size)
            return;
        
        size = sz;
        
        int [] new_index   = new int[size*8];
        System.arraycopy(index, 0, new_index, 0, index.length);
        index = new_index;
        
        int [] new_status  = new int[size];
        System.arraycopy(status, 0, new_status, 0, status.length);
        status = new_status;
        
        int [] new_int_lonlat = new int[size*2];
        System.arraycopy(int_lonlat, 0, new_int_lonlat, 0, int_lonlat.length);
        int_lonlat = new_int_lonlat;
        
        int [] new_int_h      = new int[size];
        System.arraycopy(int_h, 0, new_int_h, 0, int_h.length);
        int_h = new_int_h;
        
        int [] new_int_dh     = new int[size];
        System.arraycopy(int_dh, 0, new_int_dh, 0, int_dh.length);
        int_dh = new_int_dh;
        
        int [] new_int_cartesian = new int[size*3];
        System.arraycopy(int_cartesian, 0, new_int_cartesian, 0, int_cartesian.length);
        int_cartesian = new_int_cartesian;
        
        float [] new_own_obj_radius = new float[size];
        System.arraycopy(own_obj_radius, 0, new_own_obj_radius, 0, own_obj_radius.length);
        own_obj_radius = new_own_obj_radius;
        
        float [] new_tot_obj_radius = new float[size];
        System.arraycopy(tot_obj_radius, 0, new_tot_obj_radius, 0, tot_obj_radius.length);
        tot_obj_radius = new_tot_obj_radius;
        
        float [] new_tot_dev = new float[size];
        System.arraycopy(tot_dev, 0, new_tot_dev, 0, tot_dev.length);
        tot_dev = new_tot_dev;
        
        for (BttSurfaceView v : view_list)
            v.setSize();
        
        for (int i = 0; i < this.num_textures2D; ++i)
            textures2D[i].setSize();
        
    }
    
    /**
     * Initialize projection parameters
     */
    private void initializeCoosys() {
        double d = 0.;
        for(;;) {
            double old_d = d;
            d = 4./(1.+Math.exp(4.*(2.-d)));
            if (Math.abs(old_d - d) < 1e-12)
                break;
        }
        lat_scale = (2.-d)/Math.PI;
        int_lat_scale = (2.-d)/(1 << 28);
    }
    
    void addTexture(TextureCoverage texture) {
        for (int i=0; i<num_textures2D; i++)
            if (textures2D[i] == texture) return;
        
        if (textures2D.length == num_textures2D) {
            TextureCoverage[] new_textures = new TextureCoverage[textures2D.length*2];
            for (int i=0; i<num_textures2D; i++)
                new_textures[i] = textures2D[i];
            textures2D = new_textures;
        }
        textures2D[num_textures2D++] = texture;
    }
    
    void addTexture(int pos, TextureCoverage texture) {
        for (int i=0; i<num_textures2D; i++)
            if (textures2D[i] == texture) return;
        
        if (textures2D.length == num_textures2D) {
            TextureCoverage[] new_textures = new TextureCoverage[textures2D.length*2];
            for (int i=0; i<num_textures2D; i++)
                new_textures[i] = textures2D[i];
            textures2D = new_textures;
        }
        for (int i=num_textures2D; i > pos; i--)
            textures2D[i] = textures2D[i-1];
        textures2D[pos] = texture;
        num_textures2D++;
    }
    
    void removeTexture(TextureCoverage texture) {
        int i = 0;
        for (; i<num_textures2D; i++)
            if (textures2D[i] == texture) break;
        num_textures2D--;
        while ((i++) < num_textures2D)
            textures2D[i-1] = textures2D[i];
    }
    
    /**
     * Update internal data, call in a 'bounds update' listener in a per frame basis
     */
    int upd_cnt = 0;
    synchronized void updateData() {
        /*
        if (terrain_res_factor > 1 && fill <= size*.95) {
            terrain_res_factor /= 1.05;
            if (terrain_res_factor < 1.05) terrain_res_factor = 1;
            // System.out.println("Lav fill - ResFac ned : " + terrain_res_factor);
        }
         */
        // Update the internal structures of the surface
        
        // Hent inn nye høydedata
        source.processReplies();
        boolean h_scale_changed = false;
        if (h_scale_new != h_scale) {
            h_scale = h_scale_new;
            h_scale_changed = true;
        }
        if (h_scale_changed || ++upd_cnt%30 == 0) {
            for (int id=size-1; id >= 0; --id)
                if (status[id] > 0)
                    computeCartesian(id);
        } else {
            for (int id=size-1; id >= 0; --id) {
                if (status[id] > 0 && int_cartesian[id*3] == Integer.MAX_VALUE) {
                    computeCartesian(id);
                    clearParentRadies(id);
                    
                    // Set child radies invalid
                    for (int i=0; i<4; ++i) {
                        int ch_id = index[8*id+i];
                        if (ch_id >= 0) {
                            own_obj_radius[ch_id] = 0;
                            tot_obj_radius[ch_id] = 0;
                            clearParentRadies(ch_id);
                        }
                    }
                }
            }
        }
        // }
        for (int i = 0; i < base_mesh.length; ++i)
            computeAllObjectRadius(i, -2, 0);
    }
    
    synchronized void cleanupData() {
        uppercut_ts = ts;
        // System.out.print("Ny GC:");
        synchronized (view_list) {
            for (int i=0; i<view_list.size(); i++) {
                BttSurfaceView v = (BttSurfaceView)view_list.get(i);
                // System.out.print(" " + v.ts);
                if (v.ts < uppercut_ts)
                    uppercut_ts = v.ts;
            }
        }
        
        
        // kjør gc
        /*
        if (terrain_res_factor > 1) {
            // System.out.println("Terrain GC Cut: " + terrain_cut_ts + " Upper cut: " + uppercut_ts);
            gc((int)(size*.94));
            while (terrain_cut_ts < uppercut_ts && fill > size*.95) {
                // System.out.print("Gc - fill før : " + fill);
                terrain_cut_ts += 10+(uppercut_ts - terrain_cut_ts)/2;
                if (terrain_cut_ts >= uppercut_ts) terrain_cut_ts = uppercut_ts;
                gc((int)(size*.94));
                // System.out.print(" etter : " + fill);
                // System.out.println(" Cut: " + terrain_cut_ts);
            }
        }
        */
        
        // Gc av texturer
        uppercut_ts = ts;
        // System.out.print("Ny GC:");
        synchronized (view_list) {
            for (int i=0; i<view_list.size(); i++) {
                BttSurfaceView v = (BttSurfaceView)view_list.get(i);
                // System.out.print(" " + v.ts);
                if (v.render_ts < uppercut_ts)
                    uppercut_ts = v.render_ts;
            }
        }
        
        // System.out.println("Texture gc fill  : " + Texture2D.getTexMemory());
        ApplicationSettings as = ApplicationSettings.getApplicationSettings();
        boolean renew_textures = false;
        for (int i = 0; i < num_textures2D; ++i)
            if (textures2D[i].renew_textures) renew_textures = true;
        
        int visible_set_mem = 0;
        while (texture_cut_ts < uppercut_ts && (renew_textures ||
                ImageBuffer.getBufferMemory() + Texture2D.getTexMemory() > as.getTextureMemMB()*(1<<20))) {
            // System.out.print("Texture gc fill før : " + Texture2D.getTexMemory());
            
            texture_cut_ts += TS_INC+(uppercut_ts - texture_cut_ts)/10;
            if (texture_cut_ts > uppercut_ts) texture_cut_ts = uppercut_ts;
            visible_set_mem = 0;
            for (int i = 0; i < num_textures2D; ++i)
                for (int j = 0; j < textures2D[i].base_tiles.length; ++j)
                    visible_set_mem += textures2D[i].base_tiles[j].gc((textures2D[i].renew_textures ? uppercut_ts : texture_cut_ts) - TS_INC);
            // System.out.print(" etter : " + Texture2D.getTexMemory());
            // System.out.println(" Cut: " + texture_cut_ts + " Upper cut: " + uppercut_ts);
        }
        if (renew_textures) {
            for (int i = 0; i < num_textures2D; ++i) {
                if (textures2D[i].renew_textures) {
                    textures2D[i].renew_textures = false;
                    for (int j = 0; j < textures2D[i].base_tiles.length; ++j)
                        textures2D[i].base_tiles[j].reloadTextures();
                }
            }
        }
        
        if (/*ImageBuffer.getBufferMemory() + Texture2D.getTexMemory()*/ visible_set_mem > as.getTextureMemMB()*(1<<20)) {
            texture_res_factor *=1.01;
            // System.out.println("Res factor up: " +  texture_res_factor);
            /*
            double mem = ImageBuffer.getBufferMemory() + Texture2D.getTexMemory();
            mem /= (1<<20);
            mem += as.getTextureMemMB();
            mem /= 2;
            mem = Math.ceil(mem);
            System.out.println("Increasing texture memory from: " + as.getTextureMemMB() + " to: " + (int)mem + " MB");
            as.setTextureMemMB((int)mem);
             */
            /*
            int mem = as.getTextureMemMB();
            ++mem;
            System.out.println("Increasing texture memory from: " + as.getTextureMemMB() + " to: " + (int)mem + " MB");
            as.setTextureMemMB(mem);
             */
        } else if (texture_res_factor > 1) {
            texture_res_factor /= 1.01;
            if (texture_res_factor <= 1.01)
                texture_res_factor = 1;
            // System.out.println("Res factor down: " +  texture_res_factor);
        }
    }
}
