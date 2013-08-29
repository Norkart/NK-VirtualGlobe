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
import com.norkart.virtualglobe.util.GJK;
import com.norkart.virtualglobe.util.GJKBody;
import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.CullFrustum;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;

import java.lang.ref.WeakReference;
import java.util.*;
import java.nio.*;


import javax.vecmath.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */


public final class BttSurfaceView extends GLCleanup  implements Runnable, OriginUpdateListener {
    final int thread_priority = Thread.NORM_PRIORITY-2;
    
    private boolean has_new_frustum = false;
    boolean has_new_render_data = false;
    private boolean stop_updating = false;
    
    // Bounding box indexes
    // private final static short [] box_index = {0, 3, 1, 7, 0, 6, 4, 7, 5, 3, 4, 2, 0};
    // Bounding box buffer
    // private FloatBuffer box_buffer;
    
    private int use_vbo  = GLSettings.NONE;
    // private boolean vbo_off  = false;
    // private int num_slow_frames = 0;
    // private boolean vbo_init = false;
    //  private boolean has_updated = false;
    // private boolean use_occlusion = false;
    
    // Timestamp for this surface view
    int ts         = BttSurface.TS_INC;
    int render_ts  = BttSurface.TS_INC;
    
    // Update counter before next yield
    private int ycnt = 0;
    
    // The common surface data
    private BttSurface surface;
    
    // Node status array
    private int[]   status;
    
    // Normal vector array
    private float[]     normals;
    
    // Hi-res Texture coordinates
    private int[]         int_tex_coo;
    
    // Node index compression
    private int[] index;
    private int max_node;
    private int max_antipode_node;
    
    // Vertex, normal and texcoo coordinate buffer
    private int base_sz;
    private ByteBuffer update_geometry_buffer;
    private ByteBuffer render_geometry_buffer;
    private int geometry_buffer_id = 0;
    private int geometry_buffer_sz = 0;
    
    private int render_vertex_offset;
    private int render_normal_offset;
    private int render_texcoo_offset;
    private int render_antipode_offset;
    
    private Buffer render_vertex_buffer, render_normal_buffer, render_texcoo_buffer, render_antipode_buffer;
    
    // Indexes
    private ByteBuffer   index_buffer;
    private boolean use_short;
    private int index_buffer_id = 0;
    private int index_buffer_sz = 0;
    
    // Geographic origin and other coordinate handling
    Point3d orig     = new Point3d();
    Point3d new_orig = new Point3d();
    int [] int_lonlat_orig = new int[2];
    int [] int_tex_orig    = new int[2];
    int int_s_orig;
    int int_t_orig;
    
    // private Point3d viewpoint   = new Point3d();
    // private Point3d image_point = new Point3d();
    
    static final int ORIGIN_UPDATE_COMPLETE   = 0;
    static final int ORIGIN_UPDATE_INITIATED  = 1;
    static final int ORIGIN_UPDATE_PROCESSING = 2;
    static final int ORIGIN_UPDATE_READY      = 3;
    int origin_update_status = ORIGIN_UPDATE_INITIATED;
    
    private int mem_step = 0;
    
    // The currently shown texture (to reduce GL texture switching)
    private Texture2D     curr_texture = null;
    
    // Should we stop waiting for updating to allow drawing instead?
    // private boolean not_wait_for_update = false;
    
    // Request a update now
    private boolean force_update = true;
    
    // The background updater thread
    private Thread updater;
    
    
    private Point3d p = new Point3d();
    
    
    /**
     * Constructor: allocate and initialize this surface viewer
     * @param surface The viewed surface
     */
    public BttSurfaceView(BttSurface surface) {
        this(surface, 1<<16);
    }
    /**
     * Initialize this surface viewer
     * @param surface The surface to view
     * @param base_sz Initial size of data arrays
     */
    public BttSurfaceView(BttSurface surface, int base_sz) {
        this.surface = surface;
        this.base_sz = base_sz;
        synchronized (surface.view_list) {
            surface.view_list.add(this);
        }
    /*
    for (int i=0; i<6; i++)
      new_planes[i] = new Vector4f();
     */
        
        
        // box_buffer = BufferUtils.newFloatBuffer(1024*8*3);
        int_tex_coo = new int[surface.size*2];
        status = new int[surface.size];
        index  = new int[surface.size];
        normals = new float[surface.size*3];
        
        strip_list = new StripList(surface.size/2);
        
        updater = new Thread(this, "BttSurfaceView-updater");
        updater.setPriority(thread_priority);
        updater.start();
    }
    
    public Point3d getOrigin() {
        return orig;
    }
    
    void setSize() {
        int [] new_int_tex_coo = new int[surface.size*2];
        System.arraycopy(int_tex_coo, 0, new_int_tex_coo, 0, int_tex_coo.length);
        int_tex_coo = new_int_tex_coo;
        
        int [] new_status = new int[surface.size];
        System.arraycopy(status, 0, new_status, 0, status.length);
        status = new_status;
        
        int [] new_index  = new int[surface.size];
        System.arraycopy(index, 0, new_index, 0, index.length);
        index = new_index;
        
        float [] new_normals = new float[surface.size*3];
        System.arraycopy(normals, 0, new_normals, 0, normals.length);
        normals = new_normals;
    }
    
    /**
     *
     * <p>Title: StripList</p>
     * <p>Description: </p>
     * <p>Copyright: Copyright (c) 2004</p>
     * <p>Company: SINTEF</p>
     * @author Rune Aasgaard
     * @version 1.0
     *
     * A class for storing indexed triangle strips.
     */
    private final class StripList {
        private int   parity;
        private int   num_nodes = 0;
        private int   nodes[];
        // IntBuffer list = BufferUtils.newIntBuffer(surface.size);
        private int max_element, min_element;
        // int max_h, min_h;
        
        private StripList(int sz) {
            nodes = new int[sz];
        }
        
        private void clear() {
            num_nodes = 0;
            parity = 0;
            max_element = 0;
            min_element = Integer.MAX_VALUE;
            //max_h = Integer.MIN_VALUE;
            //min_h = Integer.MAX_VALUE;
        }
        
        private void newStrip() {
            parity = 0;
            max_element = 0;
            min_element = Integer.MAX_VALUE;
            //max_h = Integer.MIN_VALUE;
            //min_h = Integer.MAX_VALUE;
        }
        
        private void append(int id) {
            if (num_nodes == nodes.length) {
                int[] new_nodes = new int[num_nodes*2];
                for (int i=num_nodes-1; i >= 0; --i)
                    new_nodes[i] = nodes[i];
                nodes = new_nodes;
            }
            nodes[num_nodes++] = id;
            // list.put(id);
            if (id > max_element) max_element = id;
            if (id < min_element) min_element = id;
        }
        
        private int back1() {
            return nodes[num_nodes - 1];
            // return list.get(list.position()-1);
        }
        
        private int back2() {
            return nodes[num_nodes - 2];
            // return list.get(list.position()-2);
        }
    }
    private StripList strip_list;
    
    private int getIndex(int id) {
        if (index[id] < 0)
            index[id] = max_node++;
        return index[id];
    }
    
    /**
     * Add points to the triangle strip
     * @param strip
     * @param id
     * @param p
     */
    private void addTriangleToStripList(StripList strip, int id, int p) {
        id = getIndex(id);
        // Add point
        if (id != strip.back1() &&
                id != strip.back2()) {
            if (p != strip.parity)
                strip.parity = p;
            else
                strip.append(strip.back2());
            strip.append(id);
        }
    }
    
    /**
     * Recursively add points to the triangle strip
     * @param strip
     * @param top
     * @param ix
     * @param n
     */
    private void subMeshRefine(StripList strip, int top, int ix, int n) {
        if ((ycnt++)%1000 == 0)
            Thread.yield();
        int p = n%2;
        int base = surface.index[8*top+ix];
        boolean active_tri = (base >= 0 && status[base] >= ts+BttSurface.TS_IS_VISIBLE);
        
        // Descend into children
        if (active_tri)
            subMeshRefine(strip, base, (ix + (p==0?BttSurface.LEFT:BttSurface.RIGHT))%4, n+1);
        
        // Add point
        addTriangleToStripList(strip, top, p);
        
        // Descend into children
        if (active_tri)
            subMeshRefine(strip, base, (ix + (p==1?BttSurface.LEFT:BttSurface.RIGHT))%4, n+1);
    }
    
    /**
     * Generate triangle strips for the quadrangle centered at the center_node
     * @param strip
     * @param center_node
     * @return
     */
    private int meshRefine(StripList strip, int center_node) {
        if (center_node < 0 || status[center_node] < ts+BttSurface.TS_IS_VISIBLE)
            return 0;
        
        int old_strip_len = strip.num_nodes;
        strip.append(getIndex(surface.index[center_node*8+4]));
        strip.append(getIndex(surface.index[center_node*8+4]));
        subMeshRefine(strip, center_node, 1, 0);
        addTriangleToStripList(strip, surface.index[center_node*8+5], 1);
        subMeshRefine(strip, center_node, 2, 0);
        addTriangleToStripList(strip, surface.index[center_node*8+6], 1);
        subMeshRefine(strip, center_node, 3, 0);
        addTriangleToStripList(strip, surface.index[center_node*8+7], 1);
        subMeshRefine(strip, center_node, 0, 0);
        strip.append(getIndex(surface.index[center_node*8+4]));
        strip.parity = (strip.parity+1)%2;
        
        return strip.num_nodes - old_strip_len;
    }
    
    /**
     * Generate triangle strips for the triangle
     * @param strip
     * @param id
     * @param ix
     * @return
     */
    private int meshRefine(StripList strip, int id, int ix) {
        if (id < 0 || status[id] < ts+BttSurface.TS_IS_VISIBLE)
            return 0;
        
        int old_strip_len = strip.num_nodes;
        strip.append(getIndex(surface.index[id*8+4+(ix+3)%4]));
        strip.append(getIndex(surface.index[id*8+4+(ix+3)%4]));
        subMeshRefine(strip, id, (ix+0)%4, 0);
        strip.append(getIndex(surface.index[id*8+4+(ix+0)%4]));
        strip.append(getIndex(surface.index[id*8+4+(ix+0)%4]));
        return strip.num_nodes - old_strip_len;
    }
    
    
    // Cull frustum related data
    private CullFrustum cull_frustum = new CullFrustum();
    private CullFrustum new_frustum  = new CullFrustum();
    
    
    /**
     * The update thread work loop
     */
    public void run() {
        while (!stop_updating) {
            try {
                // Wait for start update order
                synchronized (this) {
                    while (new_frustum.getResolution() <= 0 ||
                            ((origin_update_status == ORIGIN_UPDATE_COMPLETE || origin_update_status == ORIGIN_UPDATE_READY) &&
                            (has_new_render_data || !has_new_frustum))) {
                        if (stop_updating) break;
                        try { wait(); } catch (InterruptedException ex) {}
                    }
                    if (stop_updating) break;
                    try {
                        if (origin_update_status == ORIGIN_UPDATE_INITIATED) {
                            orig.set(new_orig);
                            Ellipsoid.LatLonH llh = new Ellipsoid.LatLonH();
                            surface.getEllipsoid().fromCartesian(orig.x, orig.y, orig.z, llh);
                            int_lonlat_orig[0] = surface.lonToInt(llh.lon);
                            int_lonlat_orig[1] = surface.latToInt(llh.lat);
                            surface.textures2D[0].getTextureCoosys().computeTexCoo(0, int_lonlat_orig, int_tex_orig);
                            origin_update_status = ORIGIN_UPDATE_PROCESSING;
                        }
                    } catch (Exception ex) { continue; }
                    if (stop_updating) break;
                    has_new_frustum = false;
                }
                
                // Do the updating of the terrain model
                if (!update(new_frustum))
                    continue;
                
                // Tell the world we are finished and redy to render
                synchronized (this) {
                    if (stop_updating) break;
                    if (origin_update_status == ORIGIN_UPDATE_PROCESSING)
                        origin_update_status = ORIGIN_UPDATE_READY;
                    
                    has_new_render_data = true;
                    force_update = false;
                    notifyAll();
                }
                Thread.yield();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // Clear data
        free_tiles.clear();
        render_normal_tiles.clear();
        render_antipode_tiles.clear();
        update_normal_tiles.clear();
        update_antipode_tiles.clear();
        normals = null;
        status = null;
        index = null;
        strip_list = null;
        render_geometry_buffer = null;
        update_geometry_buffer = null;
        index_buffer = null;
        
        requestCleanup();
    }
    
    /**
     * Close open GL resources on deletion
     * @param gl The GL object
     * @param glu The GLU object
     */
    protected void cleanup(GL gl, GLU glu) {
        if (use_vbo != GLSettings.NONE) {
            int[] iarr = new int [1];
            if (geometry_buffer_id > 0) {
                iarr[0] = geometry_buffer_id;
                if (use_vbo == GLSettings.ARB)
                    gl.glDeleteBuffersARB(1, iarr, 0);
                else
                    gl.glDeleteBuffers(1, iarr, 0);
            }
            if (index_buffer_id > 0) {
                iarr[0] = index_buffer_id;
                if (use_vbo == GLSettings.ARB)
                    gl.glDeleteBuffersARB(1, iarr, 0);
                else
                    gl.glDeleteBuffers(1, iarr, 0);
            }
        }
    }
    
    /**
     * Turn off waiting for update
     */
  /*
  public synchronized void notWaitForUpdate() {
    if (!not_wait_for_update) {
      not_wait_for_update = true;
      notifyAll();
    }
  }
   */
    
    /**
     * Send this viewer an order to start updating with the given view parameters
     * @param frustum The view frustum
     */
    public synchronized void initiateUpdate(CullFrustum frustum) {
        new_frustum.setValues(frustum);
        has_new_frustum = true;
        notifyAll();
    }
    
    /**
     * Force an update
     */
    public synchronized void forceUpdate() {
        force_update = true;
        notifyAll();
    }
    
    /**
     * Stop the updating thread, clean up and prepare for deletion
     */
    public void close() {
        synchronized (this) {
            stop_updating = true;
            notifyAll();
        }
        while (updater.isAlive()) {
            try {
                updater.join();
            } catch (Exception ex) {
                System.err.print("Surface Viewer closing error : ");
                System.err.println(ex);
            }
        }
        synchronized (surface.view_list) {
            surface.view_list.remove(this);
        }
        if (update_geometry_buffer != null)
            update_geometry_buffer.clear();
        update_geometry_buffer = null;
        if (render_geometry_buffer != null)
            render_geometry_buffer.clear();
        render_geometry_buffer = null;
        if (index_buffer != null)
            index_buffer.clear();
        index_buffer = null;
    }
    
    
    /**
     * Set a new view origin. This method should only be called by the {@link ViewerManager}
     * @param p The new origin
     */
    public synchronized void      updateOrigin(Point3d p) {
        origin_update_status = ORIGIN_UPDATE_COMPLETE;
        notifyAll();
    }
    
    /**
     * Ask if the surface View is ready to accept a new origin.
     * This method shold only be called by the {@link ViewerManager}.
     * @param p The proposed origin
     * @return True if we are ready to change origin
     */
    public synchronized boolean requestUpdateOrigin(Point3d p) {
        if (!new_orig.equals(p)) {
            new_orig.set(p);
            origin_update_status = ORIGIN_UPDATE_INITIATED;
            notifyAll();
        }
        return stop_updating ||
                origin_update_status == ORIGIN_UPDATE_READY;
    }
    /**
     * The workhorse of this class, is called in the run loop as a response to new view data.
     * Updates the surface viewer and prepares renderable data.
     *
     * @param planes
     * @param camera
     * @param angular_resolution
     */
    private boolean update(CullFrustum frustum) /* Vector4f[] planes, Point3f camera, float angular_resolution) */ {
        ycnt = 0;
        
        // Set values of cull frustum
        cull_frustum.setValues(frustum);
        

        // Clear datastructures
        {
            Iterator t_it = update_normal_tiles.iterator();
            while (t_it.hasNext())
                free_tiles.add(new WeakReference(t_it.next()));
            t_it = update_antipode_tiles.iterator();
            while (t_it.hasNext())
                free_tiles.add(new WeakReference(t_it.next()));
            update_normal_tiles.clear();
            update_antipode_tiles.clear();
            strip_list.clear();
        }
        
        // Set new timestamp
        ts = surface.newTs();
        
        // Update base structures
        surface.updateData();
        // boolean completed = false;
        // while (!completed) {
        // Check if we should clean up some data
        surface.cleanupData();
        
        // Scaled angular resolution, dependent on the surface resolution
        // reduction due to little free memory
        // this.angular_resolution = angular_resolution * surface.res_factor;
        // cull_frustum.setTerrainResFactor(surface.terrain_res_factor);
        cull_frustum.setTextureResFactor(surface.texture_res_factor);
        
        // For each triangle in basemesh, do update
        // try {
        for (int i = 0; i < surface.base_mesh.length; ++i) {
            int id = surface.base_mesh[i];
            if (status[id] > ts && status[id]%2 == BttSurface.TS_HAS_NORMAL)
                status[id] = ts+BttSurface.TS_IS_VISIBLE+BttSurface.TS_HAS_NORMAL;
            else
                status[id] = ts+BttSurface.TS_IS_VISIBLE;
            for (int ix=0; ix < 4; ix++) {
                int p_id = surface.index[id*8+4+ix];
                if (p_id >= 0 && surface.index[p_id*8+ix] == id)
                    update(p_id << BttSurface.ID_SHIFT | ix);
            }
        }
        
        // Update objects that are dependent on updated elevations
        surface.fireGlobeElevationUpdateListeners();
        
        //use_occlusion = ApplicationSettings.getApplicationSettings().useOcclusion();
        
        // Compute render tiles
        for (int ix = 0; ix < surface.base_mesh.length; ix++) {
            int base = surface.base_mesh[ix];
            
            
            // Create/use tile
            RenderTile tile = newRenderTile(base, surface.num_textures2D);

            float min_pix_sz = Float.MAX_VALUE;
            for (int i=0; i < surface.num_textures2D; ++i) {
                if (surface.textures2D[i].base_tiles[ix] == null ||
                        surface.textures2D[i].base_tiles[ix].isOutsideOfArea())
                    tile.texture_tiles[i] = null;
                else {
                    tile.texture_tiles[i] = surface.textures2D[i].base_tiles[ix];
                    min_pix_sz = Math.min(min_pix_sz, tile.texture_tiles[i].getPixelSize());
                }
            }
            tile.addSupportPoints(base, min_pix_sz);
            
            GJK gjk = GJK.getInstance();
            int   intersect = cull_frustum.checkIntersection(tile);
            boolean show_me = true;
            if (intersect != CullFrustum.TOTALLY_OUT) {
                double camera_dist = cull_frustum.isPerspective() ? gjk.dist(cull_frustum.getCameraCenter(), tile) : 1;
                if (min_pix_sz > camera_dist*cull_frustum.getResolution()*cull_frustum.getTextureResFactor())
                    show_me = false;
            }
            
            float pri = (float)Math.max(tile.getRadius(), gjk.dist(cull_frustum.getViewAxis(), tile));
            for (int i=0; show_me && i < surface.num_textures2D; ++i) {
                if (tile.texture_tiles[i] != null &&
                        !tile.texture_tiles[i].isOutsideOfResolution() &&
                        tile.texture_tiles[i].useTexture(ts, pri, true) == null &&
                        tile.texture_tiles[i].getLoadedTextureCoverage() > .15)
                    show_me = false;
            }
            
            if (show_me || tile.updateShowMe(intersect))
                tile.useRenderTile(pri, true);
            else
                freeRenderTile(tile);
        }
        
        // Stripifiser tiles her, antipoder først
        {
            max_node = 0;
            Iterator t_it = update_antipode_tiles.iterator();
            while (t_it.hasNext()) {
                RenderTile t = (RenderTile)t_it.next();
                t.meshRefine();
            }
            // max_node er nå antall noder som skal være i antipode texcoo arrayen
            max_antipode_node = max_node;
            t_it = update_normal_tiles.iterator();
            while (t_it.hasNext()) {
                RenderTile t = (RenderTile)t_it.next();
                t.meshRefine();
            }
        }
        
        // surface.cleanupData();
        
        // Normalizing normals, computing local vertex coordinates
        
        
        // Ekspander arrays
        int sz = (2*max_antipode_node + 8*max_node)*4;
        if (update_geometry_buffer == null || update_geometry_buffer.capacity() < sz) {
            int increment = base_sz*32/4;
            sz = ((sz-1)/increment)+1;
            if (sz < 4) sz = 4;
            sz *= increment;
            // System.out.println("Geometry buffer increases to " + sz);
            if (use_vbo != GLSettings.NONE) {
                update_geometry_buffer = ByteBuffer.allocate(sz);
            } else {
                update_geometry_buffer = ByteBuffer.allocateDirect(sz);
            }
            update_geometry_buffer.order(ByteOrder.nativeOrder());
        }
        update_geometry_buffer.clear();
        synchronized (surface) {
            double orig_x = orig.x;
            double orig_y = orig.y;
            double orig_z = orig.z;
            int    orig_s = int_tex_orig[0];
            int    orig_t = int_tex_orig[1];
            double scale  = surface.scale;
            FloatBuffer local_buffer = update_geometry_buffer.asFloatBuffer();
            int [] local_status = status;
            int local_ts = ts;
            float[] local_normals = normals;
            int [] local_int_cartesian = surface.int_cartesian;
            int [] local_int_tex_coo = int_tex_coo;
            int [] local_surface_int_tex_coo = surface.textures2D[0].int_tex_coo;
            int [] local_index = index;
            int local_ycnt = 1;
            int local_max_node = max_node;
            int local_max_antipode = max_antipode_node;
            int id, pos;
            float x, y, z, ss;
            for (int i = surface.size-1; i >= 0; --i) {
                if (local_status[i] > local_ts && local_status[i]%2 == BttSurface.TS_HAS_NORMAL) {
                    if ((local_ycnt++)%1000 == 0)
                        Thread.yield();
                    
                    if (local_int_cartesian[i*3] == Integer.MAX_VALUE) {
                        System.err.println("Undefined Cartesian value");
                        surface.getEllipsoid().toCartesian(surface.intToLat(surface.int_lonlat[i*2+1]),
                                surface.intToLon(surface.int_lonlat[i*2+0]),
                                surface.intToH(surface.getIntH(i)), p);
                        
                        local_int_cartesian[i*3+0] = (int)(p.x/scale);
                        local_int_cartesian[i*3+1] = (int)(p.y/scale);
                        local_int_cartesian[i*3+2] = (int)(p.z/scale);
                    }
                    
                    x = local_normals[i*3];
                    y = local_normals[i*3+1];
                    z = local_normals[i*3+2];
                    ss = x*x+y*y+z*z;
                    if (ss < 0.01) {
                        x = local_int_cartesian[i*3];
                        y = local_int_cartesian[i*3+1];
                        z = local_int_cartesian[i*3+2];
                        ss = x*x+y*y+z*z;
                    }
                    ss = (float)Math.sqrt(ss);
                    
                    id = local_index[i];
                    if (id < 0)
                        continue;
                    // throw new IllegalStateException("Impossible vertex index");
                    
                    pos = local_max_node*3 + id*3;
                    local_buffer.put(pos,   x/ss);
                    local_buffer.put(pos+1, y/ss);
                    local_buffer.put(pos+2, z/ss);
                    
                    // Compute scaled and shifted (float) cartesian coordinates for this node
                    pos = id*3;
                    local_buffer.put(pos,   (float)(local_int_cartesian[i*3  ]*scale-orig_x));
                    local_buffer.put(pos+1, (float)(local_int_cartesian[i*3+1]*scale-orig_y));
                    local_buffer.put(pos+2, (float)(local_int_cartesian[i*3+2]*scale-orig_z));
                    
                    pos = local_max_node*6 + id*2;
                    local_buffer.put(pos,   local_surface_int_tex_coo[i*2+0] - orig_s);
                    local_buffer.put(pos+1, local_surface_int_tex_coo[i*2+1] - orig_t);
                    
                    if (id < local_max_antipode) {
                        pos = local_max_node*8 + id*2;
                        local_buffer.put(pos,   local_surface_int_tex_coo[i*2+0] - orig_s - Integer.MIN_VALUE);
                        local_buffer.put(pos+1, local_surface_int_tex_coo[i*2+1] - orig_t);
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check this node for visibility, descend down into hierarchy
     * @param t
     * @param totally_in
     * @throws BttSurface.OutOfNodeMemory
     */
    private int[] t_stack = new int[65];
    private boolean [] in_stack = new boolean[65];
    private void update(int t) {
        boolean totally_in = false;
        int top = 0;
        int local_ts   = ts;
        int local_ycnt = 1;
        double scale = surface.scale;
        do {
            if ((local_ycnt++)%1000 == 0)
                Thread.yield();
            if (stop_updating) return;
            int bn = surface.getBaseNode(t);
            int bn_status = status[bn];
            if (bn_status < ts+BttSurface.TS_IS_INVISIBLE) {
                float tot_obj_radius = surface.tot_obj_radius[bn];
                // Find out if this is outside or inside
                int norm = (bn_status == local_ts+BttSurface.TS_HAS_NORMAL)?BttSurface.TS_HAS_NORMAL:0;
                double x = scale*surface.int_cartesian[bn*3];
                double y = scale*surface.int_cartesian[bn*3+1];
                double z = scale*surface.int_cartesian[bn*3+2];
                int intersect = CullFrustum.TOTALLY_IN;
                if (!totally_in)
                    intersect = cull_frustum.checkIntersection(x, y, z, tot_obj_radius);
                
                if (intersect == CullFrustum.TOTALLY_OUT)
                    bn_status = local_ts+BttSurface.TS_IS_INVISIBLE+norm;
                else {
                    if (intersect == CullFrustum.TOTALLY_IN)
                        totally_in = true;
                    
                    int top_n   = surface.getNode(t, BttSurface.TOP);
                    int left_n  = surface.getNode(t, BttSurface.LEFT);
                    int right_n = surface.getNode(t, BttSurface.RIGHT);
                    
                    int lon = surface.int_lonlat[2*bn];
                    int lat = surface.int_lonlat[2*bn+1];
                    double dev2D = 2;
                    dev2D = Math.max(dev2D,
                            surface.dist2D(lon, lat,
                            surface.int_lonlat[2*top_n],
                            surface.int_lonlat[2*top_n+1]));
                    dev2D = Math.max(dev2D,
                            surface.dist2D(lon, lat,
                            surface.int_lonlat[2*left_n],
                            surface.int_lonlat[2*left_n+1]));
                    dev2D = Math.max(dev2D,
                            surface.dist2D(lon, lat,
                            surface.int_lonlat[2*right_n],
                            surface.int_lonlat[2*right_n+1]));
                    float tot_dev = surface.tot_dev[bn];
                    if (!cull_frustum.isVisible(x, y, z, tot_obj_radius,
                            tot_dev, dev2D))
                        bn_status = local_ts+BttSurface.TS_IS_INVISIBLE+norm;
                    else
                        bn_status = local_ts+BttSurface.TS_IS_VISIBLE+norm;
                }
                status[bn] = bn_status;
            }
            if (bn_status < local_ts+BttSurface.TS_IS_VISIBLE) {
                // This triangle shall be shown, but not any of its children. Compute normals
                int tn = surface.getNode(t, BttSurface.TOP);
                int ln = surface.getNode(t, BttSurface.LEFT);
                int rn = surface.getNode(t, BttSurface.RIGHT);
                float dxl, dyl, dzl, dxr, dyr, dzr;
                dxl = dxr = -surface.int_cartesian[tn*3+0];
                dyl = dyr = -surface.int_cartesian[tn*3+1];
                dzl = dzr = -surface.int_cartesian[tn*3+2];
                dxl += surface.int_cartesian[ln*3+0];
                dyl += surface.int_cartesian[ln*3+1];
                dzl += surface.int_cartesian[ln*3+2];
                dxr += surface.int_cartesian[rn*3+0];
                dyr += surface.int_cartesian[rn*3+1];
                dzr += surface.int_cartesian[rn*3+2];
                
                float dx = dyr*dzl-dzr*dyl;
                float dy = dzr*dxl-dxr*dzl;
                float dz = dxr*dyl-dyr*dxl;
                
                addNormal(tn, dx, dy, dz);
                addNormal(ln, dx, dy, dz);
                addNormal(rn, dx, dy, dz);
                t = -1;
            } else {
                // Go to children instead
                t_stack[top] = surface.getChild(t, BttSurface.LEFT);
                in_stack[top] = totally_in;
                ++top;
                t = surface.getChild(t, BttSurface.RIGHT);
                // update(surface.getChild(t, BttSurface.LEFT),  totally_in);
                // update(surface.getChild(t, BttSurface.RIGHT), totally_in);
            }
            if (t < 0 && --top >= 0) {
                t          = t_stack[top];
                totally_in = in_stack[top];
            }
        } while (t >= 0);
    }
    
    /**
     * Add normal components for this vertex
     *
     * @param id
     * @param x
     * @param y
     * @param z
     */
    private void addNormal(int id, float x, float y, float z) {
        if (status[id] > ts && status[id]%2 == BttSurface.TS_HAS_NORMAL) {
            normals[id*3+0] += x;
            normals[id*3+1] += y;
            normals[id*3+2] += z;
        } else {
            normals[id*3+0] = x;
            normals[id*3+1] = y;
            normals[id*3+2] = z;
            index[id] = -1;
            if (status[id] < ts)
                status[id] = ts+BttSurface.TS_HAS_NORMAL;
            else
                status[id] += BttSurface.TS_HAS_NORMAL;
        }
    }
    
    
    //private TextureTile[] curr_textures = new TextureTile[4];
    //private int num_curr_textures = 0;
    
    
    
    /**
     * Render tile stuff
     */
    private ArrayList free_tiles = new ArrayList();
    private ArrayList render_normal_tiles   = new ArrayList();
    private ArrayList render_antipode_tiles = new ArrayList();
    private ArrayList update_normal_tiles   = new ArrayList();
    private ArrayList update_antipode_tiles = new ArrayList();
    private final class RenderTile implements GJKBody /* implements Comparable */{
        private int center_node;
        private TextureTile[] texture_tiles = null;
        private int num_textures;
        private int[] tris;
        private int num_tris;
        private Buffer tile_index_buffer = null;
        
        private int min_element, max_element;
        private int start_index, num_elements;
        
        //int min_h, max_h;
        
        private boolean is_antipode;
        
        
        private Point3d center = new Point3d();
        private double radius;
        private int num_support = 0;
        private Vector3d[] support_points = new Vector3d[9];
        
        private static final int SUPPORT_GRID_MAX = 1<<3;
        private static final int SUPPORT_GRID_DIM = SUPPORT_GRID_MAX + 1;
        private static final int SUPPORT_GRID_MID = SUPPORT_GRID_MAX/2;
        private double[][][] support_grid =  new double[SUPPORT_GRID_DIM][SUPPORT_GRID_DIM][4];
        
        private RenderTile(int center_node, int num_textures) {
            initialize(center_node, num_textures);
        }
        
        private void initialize(int center_node, int num_textures) {
            this.center_node = center_node;
            num_tris = 0;
            //tile.view_dist = 0;
            if (texture_tiles == null || texture_tiles.length < num_textures)
                texture_tiles = new TextureTile[num_textures];
            this.num_textures = num_textures;
            for (int i=0; i < num_textures; ++i)
                texture_tiles[i] = null;
            center.x = surface.scale*surface.int_cartesian[center_node*3];
            center.y = surface.scale*surface.int_cartesian[center_node*3+1];
            center.z = surface.scale*surface.int_cartesian[center_node*3+2];
        }
        
        private double getRadius() {
            return radius;
        }
        
        private Point3d getCenterPoint() {
            return center;
        }
        
        private void addSupportPoints(int t, int left_i, int left_j, int right_i, int right_j, int top_i, int top_j, float dh_tol) {
            int bn = surface.getBaseNode(t, false);
            int base_i = (left_i + right_i);
            int base_j = (left_j + right_j);
            
            float dh = bn < 0 ? 0 : surface.tot_dev[bn];
            if (dh > dh_tol && bn >= 0 && base_i % 2 == 0 && base_j % 2 == 0) {
                base_i /= 2;
                base_j /= 2;
                int left_ch  = surface.getChild(t, BttSurface.LEFT,  false);
                int right_ch = surface.getChild(t, BttSurface.RIGHT, false);
                
                addSupportPoints(left_ch,  top_i,   top_j,   left_i, left_j, base_i, base_j, dh_tol);
                addSupportPoints(right_ch, right_i, right_j, top_i,  top_j,  base_i, base_j, dh_tol);
            } else {
                if (support_grid[top_i][top_j][3] < 0) {
                    int n = surface.getNode(t, BttSurface.TOP);
                    support_grid[top_i][top_j][0] =
                            surface.scale*surface.int_cartesian[n*3];
                    support_grid[top_i][top_j][1] =
                            surface.scale*surface.int_cartesian[n*3+1];
                    support_grid[top_i][top_j][2] =
                            surface.scale*surface.int_cartesian[n*3+2];
                }
                support_grid[top_i][top_j][3] = Math.max(support_grid[top_i][top_j][3], dh);
                
                if (support_grid[left_i][left_j][3] < 0) {
                    int n = surface.getNode(t, BttSurface.LEFT);
                    support_grid[left_i][left_j][0] =
                            surface.scale*surface.int_cartesian[n*3];
                    support_grid[left_i][left_j][1] =
                            surface.scale*surface.int_cartesian[n*3+1];
                    support_grid[left_i][left_j][2] =
                            surface.scale*surface.int_cartesian[n*3+2];
                }
                support_grid[left_i][left_j][3] = Math.max(support_grid[left_i][left_j][3], dh);
                
                if (support_grid[right_i][right_j][3] < 0) {
                    int n = surface.getNode(t, BttSurface.RIGHT);
                    support_grid[right_i][right_j][0] =
                            surface.scale*surface.int_cartesian[n*3];
                    support_grid[right_i][right_j][1] =
                            surface.scale*surface.int_cartesian[n*3+1];
                    support_grid[right_i][right_j][2] =
                            surface.scale*surface.int_cartesian[n*3+2];
                }
                support_grid[right_i][right_j][3] = Math.max(support_grid[right_i][right_j][3], dh);
            }
        }
        
        private void addSupportPoints(int cn, float dh_tol) {
            if (cn < 0) return;
            
            for (int i=0; i<SUPPORT_GRID_DIM; ++i) {
                for (int j=0; j<SUPPORT_GRID_DIM; ++j) {
                    support_grid[i][j][3] = -1;
                }
            }
            
            addSupportPoints((cn << BttSurface.ID_SHIFT) | 0,                0,                0,                0, SUPPORT_GRID_MAX, SUPPORT_GRID_MID, SUPPORT_GRID_MID, dh_tol);
            addSupportPoints((cn << BttSurface.ID_SHIFT) | 1,                0, SUPPORT_GRID_MAX, SUPPORT_GRID_MAX, SUPPORT_GRID_MAX, SUPPORT_GRID_MID, SUPPORT_GRID_MID, dh_tol);
            addSupportPoints((cn << BttSurface.ID_SHIFT) | 2, SUPPORT_GRID_MAX, SUPPORT_GRID_MAX, SUPPORT_GRID_MAX,                0, SUPPORT_GRID_MID, SUPPORT_GRID_MID, dh_tol);
            addSupportPoints((cn << BttSurface.ID_SHIFT) | 3, SUPPORT_GRID_MAX,                0,                0,                0, SUPPORT_GRID_MID, SUPPORT_GRID_MID, dh_tol);
            
            radius = 0;
            num_support = 0;
            for (int i=0; i<SUPPORT_GRID_DIM; ++i) {
                for (int j=0; j<SUPPORT_GRID_DIM; ++j) {
                    if (support_grid[i][j][3] >= 0) {
                        Vector3d p_up   = addSupportPoint();
                        p_up.set(support_grid[i][j]);
                        
                        double x, y, z;
                        if (support_grid[i][j][3] > 0) {
                            Vector3d p_down = addSupportPoint();
                            p_down.set(p_up);
                            
                            double d = support_grid[i][j][3]/p_up.length();
                            p_up.scale(1+d);
                            p_down.scale(1-d);
                            x = p_down.x - center.x;
                            y = p_down.y - center.y;
                            z = p_down.z - center.z;
                            radius = Math.max(radius, Math.sqrt(x*x+y*y+z*z));
                        }
                        x = p_up.x - center.x;
                        y = p_up.y - center.y;
                        z = p_up.z - center.z;
                        radius = Math.max(radius, Math.sqrt(x*x+y*y+z*z));
                    }
                }
            }
        }
        
        
        private Vector3d addSupportPoint() {
            if (num_support >= support_points.length) {
                Vector3d[] new_support_points = new Vector3d[num_support * 2];
                System.arraycopy(support_points, 0, new_support_points, 0, num_support);
                support_points = new_support_points;
            }
            if (support_points[num_support] == null)
                support_points[num_support] = new Vector3d();
            
            return support_points[num_support++];
        }
        
        public void support(Vector3d v, Vector3d w) {
            if (v == null || (v.x == 0 && v.y == 0 && v.z == 0)) {
                w.set(center);
                return;
            }
            
            int ix = num_support-1;
            double dist = v.dot(support_points[ix]);
            for (int i=ix; --i>=0; ) {
                double d = v.dot(support_points[i]);
                if (d > dist) {
                    ix = i;
                    dist = d;
                }
            }
            w.set(support_points[ix]);
        }
        
        /**
         * Add a triangle to this render tile
         * @param t
         */
        private void addTri(int t) {
            if (tris == null)
                tris = new int[8];
            if (tris.length == num_tris) {
                int [] tmp = new int[num_tris*2];
                for (int i=0; i<num_tris; ++i)
                    tmp[i] = tris[i];
                tris = tmp;
            }
            tris[num_tris++] = t;
        }
        
        /**
         * Add this render tile to the list of tiles to be rendered.
         * If this render tile has texture data, use it.
         * Else; find a coarser tile that has texture data loaded
         * @param tile
         * @param do_load
         */
        private void useRenderTile(float pri, boolean do_load) {
            for (int i=0; i < num_textures; ++i) {
                if (texture_tiles[i] == null ||
                        texture_tiles[i].isOutsideOfArea()) {
                    texture_tiles[i] = null;
                } else {
                    while (texture_tiles[i] != null && texture_tiles[i].isOutsideOfResolution())
                        texture_tiles[i] = texture_tiles[i].getParent();
                    if (texture_tiles[i] != null && null == texture_tiles[i].useTexture(ts, pri, do_load)) {
                        do {
                            texture_tiles[i] = texture_tiles[i].getParent();
                        } while (texture_tiles[i] != null &&  null == texture_tiles[i].useTexture(ts, pri, false));
                    }
                }
            }
            
            // Test for antipode
            is_antipode = texture_tiles[0] != null &&
                    texture_tiles[0].s + texture_tiles[0].d_s - int_tex_orig[0] <=
                    texture_tiles[0].s - int_tex_orig[0];
            
            if (is_antipode)
                update_antipode_tiles.add(this);
            else
                update_normal_tiles.add(this);
        }
        
        private void meshRefine() {
            // Collect indexes
            strip_list.newStrip();
            start_index = strip_list.num_nodes;
            if (num_tris == 0)
                // If there are no independent triangles,
                // render the whole quadrangle centered at the Center_node
                BttSurfaceView.this.meshRefine(strip_list, center_node);
            else {
                // Else, render the individual sub triangles
                for (int i=0; i<num_tris; ++i)
                    BttSurfaceView.this.meshRefine(strip_list,
                            tris[i] >> BttSurface.ID_SHIFT,
                            tris[i] &  BttSurface.IX_MASK);
            }
            num_elements = strip_list.num_nodes - start_index;
            max_element = strip_list.max_element;
            min_element = strip_list.min_element;
            tile_index_buffer = null;
            //max_h = strip_list.max_h;
            //min_h = strip_list.min_h;
        }
        
        
        private void renderTexture(GL gl, GLU glu) {
            // Test texture states and enable/disable accordingly
            if (num_textures > 0 &&  texture_tiles[0] != null && texture_tiles[0].getTexture() != null) {
                if (curr_texture == null)
                    gl.glEnable(GL.GL_TEXTURE_2D);
                if (curr_texture != texture_tiles[0].getTexture()) {
                    curr_texture = texture_tiles[0].getTexture();
                    curr_texture.render(gl, glu);
                }
            } else {
                if (curr_texture != null)
                    gl.glDisable(GL.GL_TEXTURE_2D);
                curr_texture = null;
            }
        }
        
        /**
         * Do the rendering of all data contained in this tile
         * @param gl
         * @param glu
         */
        private void renderGeometry(GL gl, GLU glu, boolean use_range_elements) {
            
            if (curr_texture != null) {
                // Sett texcoo transform
                float s = texture_tiles[0].getS() - int_s_orig - (is_antipode ? Integer.MIN_VALUE : 0);
                float t = texture_tiles[0].getT() - int_t_orig;
                
                gl.glLoadIdentity();
                gl.glScalef(1.f/texture_tiles[0].getDS(), 1.f/texture_tiles[0].getDT(), 0f);
                gl.glTranslatef(-s, -t, 0);
            }
            
            // Do the drawing
            if (tile_index_buffer == null && index_buffer_id == 0) {
                index_buffer.position(start_index*(use_short?2:4));
                tile_index_buffer = index_buffer.slice();
            }
            if (use_range_elements) {
                if (index_buffer_id > 0)
                    gl.glDrawRangeElements(GL.GL_TRIANGLE_STRIP,
                            min_element,
                            max_element,
                            num_elements,
                            use_short?GL.GL_UNSIGNED_SHORT:GL.GL_UNSIGNED_INT,
                            start_index*(use_short?2:4));
                else
                    gl.glDrawRangeElements(GL.GL_TRIANGLE_STRIP,
                            min_element,
                            max_element,
                            num_elements,
                            use_short?GL.GL_UNSIGNED_SHORT:GL.GL_UNSIGNED_INT,
                            tile_index_buffer);
            } else {
                if (index_buffer_id > 0)
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP,
                            num_elements,
                            use_short?GL.GL_UNSIGNED_SHORT:GL.GL_UNSIGNED_INT,
                            start_index*(use_short?2:4));
                else
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP,
                            num_elements,
                            use_short?GL.GL_UNSIGNED_SHORT:GL.GL_UNSIGNED_INT,
                            tile_index_buffer);
            }
        }
        
        /**
         * Update this Render tile
         * @param cn
         * @param totally_in
         * @return
         */
        private boolean updateChild(int cn, int intersect) {
            if ((ycnt++)%100 == 0)
                Thread.yield();
            
            int ch_ix = 0;
            if (surface.int_lonlat[center_node*2+0] < surface.int_lonlat[cn*2+0])
                ch_ix += 1;
            if (surface.int_lonlat[center_node*2+1] < surface.int_lonlat[cn*2+1])
                ch_ix += 2;
            
            // Find appropriate textures and find out if we can benefit from
            // using even more detailed texture tiles (try_children)
            float min_pix_sz = Float.MAX_VALUE;
            RenderTile tile = newRenderTile(cn, num_textures);
            for (int i=0; i < num_textures; ++i) {
                if (texture_tiles[i] == null ||
                        texture_tiles[i].isOutsideOfArea())
                    tile.texture_tiles[i] = null;
                else if (texture_tiles[i].isOutsideOfResolution())
                    tile.texture_tiles[i] = texture_tiles[i];
                else {
                    tile.texture_tiles[i] = texture_tiles[i].getChild(ch_ix, ts);
                    min_pix_sz = Math.min(min_pix_sz, tile.texture_tiles[i].getPixelSize());
                }
            }
            tile.addSupportPoints(cn, min_pix_sz);

            GJK gjk = GJK.getInstance();
            
            // Point3d tile_center = tile.getCenterPoint();
            if (intersect != CullFrustum.TOTALLY_IN)
                intersect = cull_frustum.checkIntersection(tile);

            
            boolean show_me = true;
            if (intersect != CullFrustum.TOTALLY_OUT) {
                double camera_dist = cull_frustum.isPerspective() ? gjk.dist(cull_frustum.getCameraCenter(), tile) : 1;
                if (min_pix_sz > camera_dist*cull_frustum.getResolution()*cull_frustum.getTextureResFactor())
                    show_me = false;
            }
            
            float pri = (float)Math.max(tile.getRadius(), gjk.dist(cull_frustum.getViewAxis(), tile));
            for (int i=0; show_me && i < num_textures; ++i) {
                if (tile.texture_tiles[i] != null &&
                        !tile.texture_tiles[i].isOutsideOfResolution() &&
                        tile.texture_tiles[i].useTexture(ts, pri, true) == null &&
                        tile.texture_tiles[i].getLoadedTextureCoverage() > .15)
                    show_me = false;
            }
            
            if (show_me || tile.updateShowMe(intersect))
                tile.useRenderTile(pri, true);
            else
                freeRenderTile(tile);
            return true;
        }
        
        /**
         * Update this render tile and test if this is the tile to show
         * @param totally_in
         * @return
         */
        private boolean updateShowMe(int intersect) {
            int cn0 = -1, cn1 = -1, cn2 = -1, cn3 = -1;
            // Find children
            int ch0 = surface.index[center_node*8];
            int ch1 = surface.index[center_node*8+1];
            int ch2 = surface.index[center_node*8+2];
            int ch3 = surface.index[center_node*8+3];
            
            
            boolean show_me = false;
            if (ch0 >= 0 && status[ch0] >= ts+BttSurface.TS_IS_VISIBLE) {
                cn3 = surface.index[ch0*8+1];
                cn0 = surface.index[ch0*8];
            } else {
                addTri((center_node << BttSurface.ID_SHIFT) | 0);
                show_me = true;
            }
            if (ch1 >= 0 && status[ch1] >= ts+BttSurface.TS_IS_VISIBLE) {
                cn0 = surface.index[ch1*8+2];
                cn1 = surface.index[ch1*8+1];
            } else {
                addTri((center_node << BttSurface.ID_SHIFT) | 1);
                show_me = true;
            }
            if (ch2 >= 0 && status[ch2] >= ts+BttSurface.TS_IS_VISIBLE) {
                cn1 = surface.index[ch2*8+3];
                cn2 = surface.index[ch2*8+2];
            } else {
                addTri((center_node << BttSurface.ID_SHIFT) | 2);
                show_me = true;
            }
            if (ch3 >= 0 && status[ch3] >= ts+BttSurface.TS_IS_VISIBLE) {
                cn2 = surface.index[ch3*8];
                cn3 = surface.index[ch3*8+3];
            } else {
                addTri((center_node << BttSurface.ID_SHIFT) | 3);
                show_me = true;
            }
            
            if (cn0 >= 0 && status[cn0] >= ts+BttSurface.TS_IS_VISIBLE &&
                    updateChild(cn0, intersect));
            else {
                show_me = true;
                if (ch0 >= 0 && status[ch0] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch0 << BttSurface.ID_SHIFT) | 0);
                if (ch1 >= 0 && status[ch1] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch1 << BttSurface.ID_SHIFT) | 2);
            }
            if (cn1 >= 0 && status[cn1] >= ts+BttSurface.TS_IS_VISIBLE &&
                    updateChild(cn1, intersect));
            else {
                show_me = true;
                if (ch1 >= 0 && status[ch1] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch1 << BttSurface.ID_SHIFT) | 1);
                if (ch2 >= 0 && status[ch2] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch2 << BttSurface.ID_SHIFT) | 3);
            }
            if (cn2 >= 0 && status[cn2] >= ts+BttSurface.TS_IS_VISIBLE &&
                    updateChild(cn2, intersect));
            else {
                show_me = true;
                if (ch2 >= 0 && status[ch2] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch2 << BttSurface.ID_SHIFT) | 2);
                if (ch3 >= 0 && status[ch3] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch3 << BttSurface.ID_SHIFT) | 0);
            }
            if (cn3 >= 0 && status[cn3] >= ts+BttSurface.TS_IS_VISIBLE &&
                    updateChild(cn3, intersect));
            else {
                show_me = true;
                if (ch0 >= 0 && status[ch0] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch0 << BttSurface.ID_SHIFT) | 1);
                if (ch3 >= 0 && status[ch3] >= ts+BttSurface.TS_IS_VISIBLE)
                    addTri((ch3 << BttSurface.ID_SHIFT) | 3);
            }
            return show_me;
        }
    }
    
    /**
     * Reuse or create a render tile, initialize
     * @param center_node
     * @param num_textures
     * @return
     */
    private RenderTile newRenderTile(int center_node, int num_textures) {
        RenderTile tile = null;
        while (!free_tiles.isEmpty()) {
            WeakReference w = (WeakReference)free_tiles.get(free_tiles.size()-1);
            free_tiles.remove(w);
            Object o = w.get();
            if (o != null) {
                tile = (RenderTile)o;
                break;
            }
        }
        if (tile == null)
            tile = new RenderTile(center_node, num_textures);
        else
            tile.initialize(center_node, num_textures);
        
        return tile;
    }
    
    /**
     * Free a render tile for later use
     * @param tile
     */
    private void freeRenderTile(RenderTile tile) {
        tile.tile_index_buffer = null;
        for (int i=0; i < tile.texture_tiles.length; ++i)
            tile.texture_tiles[i] = null;
        free_tiles.add(new WeakReference(tile));
    }
    
    private void swapGeometryBuffers(GL gl, GLU glu) {
        GLSettings gl_cap = GLSettings.get(gl);
        
        // Compute offsets
        render_vertex_offset = 0;
        render_normal_offset = max_node*3;
        render_texcoo_offset = max_node*6;
        render_antipode_offset = max_node*8;
        
        // Swap tile lists
        ArrayList tmp_array_list;
        tmp_array_list = render_normal_tiles;
        render_normal_tiles = update_normal_tiles;
        update_normal_tiles = tmp_array_list;
        tmp_array_list = render_antipode_tiles;
        render_antipode_tiles = update_antipode_tiles;
        update_antipode_tiles = tmp_array_list;
        
        int_s_orig = int_tex_orig[0];
        int_t_orig = int_tex_orig[1];
        
        if (use_vbo != GLSettings.NONE && (!ApplicationSettings.getApplicationSettings().useVBO()/* || vbo_off*/)) {
      /*
      if (vbo_off)
        System.err.println("Something is wrong with VBOs, disabeling");
       */
            // If vbos have been turned off
            cleanup(gl, glu);
            
            geometry_buffer_id = 0;
            index_buffer_id = 0;
            geometry_buffer_sz = 0;
            index_buffer_sz = 0;
            ByteBuffer new_geometry_buffer = ByteBuffer.allocateDirect(update_geometry_buffer.limit());
            new_geometry_buffer.order(ByteOrder.nativeOrder());
            new_geometry_buffer.put(update_geometry_buffer);
            update_geometry_buffer = new_geometry_buffer;
        }
        if (ApplicationSettings.getApplicationSettings().useVBO())
            use_vbo = gl_cap.getVBOType();
        else
            use_vbo = GLSettings.NONE;
        
        // Fill index buffer
        // Resize index buffer
        use_short = max_node <= (1<<16);
        if (index_buffer == null || index_buffer.capacity() < strip_list.num_nodes*(use_short?2:4)) {
            int increment = base_sz/2;
            int sz = ((strip_list.num_nodes-1)/increment)+1;
            if (sz < 9) sz = 9;
            sz *= increment*(use_short ? 2 : 4);
            // System.out.println("Index buffer increases to " + sz);
            if (use_vbo != GLSettings.NONE) {
                index_buffer = ByteBuffer.allocate(sz);
            } else {
                index_buffer = ByteBuffer.allocateDirect(sz);
            }
            index_buffer.order(ByteOrder.nativeOrder());
        }
        index_buffer.clear();
        
        if (use_short) {
            int local_num = strip_list.num_nodes;
            int [] local_nodes = strip_list.nodes;
            ShortBuffer local_index_buffer = index_buffer.asShortBuffer();
            for (int i=0; i< local_num; ++i)
                local_index_buffer.put(i, (short)local_nodes[i]);
        } else
            index_buffer.asIntBuffer().put(strip_list.nodes, 0, strip_list.num_nodes);
        
        if (use_vbo != GLSettings.NONE) {
            // Generate buffers
            if (geometry_buffer_id <= 0) {
                int[] iarr = new int[1];
                if (use_vbo == GLSettings.ARB)
                    gl.glGenBuffersARB(1, iarr, 0);
                else
                    gl.glGenBuffers(1, iarr, 0);
                geometry_buffer_id = iarr[0];
            }
            if (index_buffer_id <= 0) {
                int[] iarr = new int[1];
                if (use_vbo == GLSettings.ARB)
                    gl.glGenBuffersARB(1, iarr, 0);
                else
                    gl.glGenBuffers(1, iarr, 0);
                index_buffer_id = iarr[0];
            }
        }
        
        if (geometry_buffer_id > 0) {
            // Copy the geometry buffer
            gl.glGetError();
            if (use_vbo == GLSettings.ARB) {
                gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, geometry_buffer_id);
                gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, max_node*8*4 + max_antipode_node*2*4, update_geometry_buffer, GL.GL_DYNAMIC_DRAW_ARB);
            } else {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, geometry_buffer_id);
                gl.glBufferData(GL.GL_ARRAY_BUFFER, max_node*8*4 + max_antipode_node*2*4, update_geometry_buffer, GL.GL_DYNAMIC_DRAW);
            }
            if (gl.glGetError() != GL.GL_NO_ERROR) {
                int [] iarr = new int[1];
                iarr[0] = geometry_buffer_id;
                if (use_vbo == GLSettings.ARB)
                    gl.glDeleteBuffersARB(1, iarr, 0);
                else
                    gl.glDeleteBuffers(1, iarr, 0);
                geometry_buffer_id = 0;
                // vbo_off = true;
            }
        }
        
        if (geometry_buffer_id == 0) {
            // Swap geometry buffers
            ByteBuffer tmp_buffer  = render_geometry_buffer;
            render_geometry_buffer = update_geometry_buffer;
            update_geometry_buffer = tmp_buffer;
            
            render_geometry_buffer.position(render_vertex_offset*4);
            render_vertex_buffer = render_geometry_buffer.slice();
            render_geometry_buffer.position(render_normal_offset*4);
            render_normal_buffer = render_geometry_buffer.slice();
            render_geometry_buffer.position(render_texcoo_offset*4);
            render_texcoo_buffer = render_geometry_buffer.slice();
            render_geometry_buffer.position(render_antipode_offset*4);
            render_antipode_buffer = render_geometry_buffer.slice();
        }
        
        if (index_buffer_id > 0) {
            // Copy the index buffer
            gl.glGetError();
            if (use_vbo == GLSettings.ARB) {
                gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, index_buffer_id);
                gl.glBufferDataARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, strip_list.num_nodes*(use_short?2:4), index_buffer, GL.GL_DYNAMIC_DRAW_ARB);
            } else {
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, index_buffer_id);
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, strip_list.num_nodes*(use_short?2:4), index_buffer, GL.GL_DYNAMIC_DRAW);
            }
            if (gl.glGetError() != GL.GL_NO_ERROR) {
                int [] iarr = new int[1];
                iarr[0] = index_buffer_id;
                if (use_vbo == GLSettings.ARB)
                    gl.glDeleteBuffersARB(1, iarr, 0);
                else
                    gl.glDeleteBuffers(1, iarr, 0);
                index_buffer_id = 0;
                // vbo_off = true;
            }
            
        }
    }
    
/*
  public void render(GL gl, GLU glu) {
    render(gl, glu, 10);
  }
 */
    /**
     * Do the real rendering
     * @param gl The GL object
     * @param glu The GLU object
     */
    
    public void render(GL gl, GLU glu) {
        synchronized (this) {
            
            while (force_update && !stop_updating) {
                try { wait(); } catch (InterruptedException ex) {}
            }
            
            if (stop_updating) return;
            
            // Copy data from update to render structures
            if (has_new_render_data && origin_update_status == ORIGIN_UPDATE_COMPLETE) {
                swapGeometryBuffers(gl, glu);
                render_ts = ts;
                has_new_render_data = false;
            }
            if (stop_updating) return;
            notifyAll();
        }
        
        // Set and enable data arrays
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        
        if (geometry_buffer_id > 0) {
            if (use_vbo == GLSettings.ARB)
                gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, geometry_buffer_id);
            else
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, geometry_buffer_id);
            
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, render_vertex_offset*4);
            gl.glNormalPointer(GL.GL_FLOAT, 0, render_normal_offset*4);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, render_texcoo_offset*4);
        } else {
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, render_vertex_buffer);
            gl.glNormalPointer(GL.GL_FLOAT, 0, render_normal_buffer);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, render_texcoo_buffer);
        }
        
        if (index_buffer_id > 0) {
            if (use_vbo == GLSettings.ARB)
                gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, index_buffer_id);
            else
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, index_buffer_id);
        }
        curr_texture = null;
        GLSettings gl_cap = GLSettings.get(gl);
        // long start_draw_time = System.currentTimeMillis();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        Iterator t_it = render_normal_tiles.iterator();
        while (t_it.hasNext()) {
            RenderTile t = (RenderTile)t_it.next();
            t.renderTexture(gl, glu);
            t.renderGeometry(gl, glu, gl_cap.hasRangeElements());
        }
        
        if (geometry_buffer_id > 0)
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, render_antipode_offset*4);
        else
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, render_antipode_buffer);
        
        t_it = render_antipode_tiles.iterator();
        while (t_it.hasNext()) {
            RenderTile t = (RenderTile)t_it.next();
            t.renderTexture(gl, glu);
            t.renderGeometry(gl, glu, gl_cap.hasRangeElements());
        }
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        
        // Cleanup OpenGL resources
        if (use_vbo == GLSettings.ARB) {
            if (geometry_buffer_id > 0)
                gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
            if (index_buffer_id > 0)
                gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
        } else {
            if (geometry_buffer_id > 0)
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            if (index_buffer_id > 0)
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        if (curr_texture != null) {
            gl.glDisable(GL.GL_TEXTURE_2D);
            curr_texture = null;
        }
        
    }
}
