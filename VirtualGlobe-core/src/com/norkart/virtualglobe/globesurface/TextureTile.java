//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import java.lang.ref.SoftReference;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 *
 * A tile covering a "rectangular" part of the terrain with a texture
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class TextureTile {
    // Load status
    private final static int NOT_LOADED = 0;
    private final static int WAIT_FOR_LOADING = 1;
    private final static int IS_LOADING = 2;
    private final static int IS_LOADED  = 3;
    private final static int OUTSIDE_OF_RESOLUTION = 4;
    private final static int OUTSIDE_OF_AREA = 5;
    private final static int IS_DELETED = 6;
    private final static int LOADING_ERROR = 7;
    
    private float pixelSize;
    
    int s, d_s, t, d_t;
    int lat, d_lat, lon, d_lon;
    private int ts;
    private float pri;
    private String posCode;
    
    private int status = NOT_LOADED;
    private long file_mod_time = 0;
    
    private Texture2D texture;
    
    private TextureTile   parent;
    private SoftReference[] children = new SoftReference[4];
    private TextureCoverage coverage;
    
    /**
     * Construct and initialize a base texture tile
     * @param coverage
     * @param lon
     * @param lat
     * @param d_lon
     * @param d_lat
     * @param posCode
     * @param ts
     */
    TextureTile(TextureCoverage coverage, int lon, int lat, int d_lon, int d_lat, String posCode, int ts) {
        this.coverage = coverage;
        this.lat   = lat;
        this.lon   = lon;
        this.d_lat = d_lat;
        this.d_lon = d_lon;
        this.posCode = posCode;
        this.ts = ts;
        this.pri = Float.MAX_VALUE;
        
        double len =
                coverage.getSurface().intToLat(lat+d_lat) -
                coverage.getSurface().intToLat(lat);
        len *= coverage.getSurface().getEllipsoid().getA();
        pixelSize = (float)(len/1000);
        
        coverage.load(this);
    }
    
    /**
     * Construct and initialize a child texture tile
     * @param parent
     * @param ch_ix
     * @param ts
     */
    private TextureTile(TextureTile parent, int ch_ix, int ts) {
        this.parent = parent;
        coverage = parent.coverage;
        d_lat = parent.d_lat/2;
        d_lon = parent.d_lon/2;
        lat = parent.lat;
        lon = parent.lon;
        if ((ch_ix & 0x1) != 0)
            lon += d_lon;
        if ((ch_ix & 0x2) != 0)
            lat += d_lat;
        posCode = parent.posCode + String.valueOf(ch_ix);
        this.ts = ts;
        this.pri = Float.MAX_VALUE;
        
        if (parent.status == OUTSIDE_OF_RESOLUTION ||
                parent.status == OUTSIDE_OF_AREA)
            status = parent.status;
        
        pixelSize = parent.pixelSize/2;
        
        coverage.load(this);
    }
    
    /**
     *
     * @throws java.lang.Throwable
     */
    protected void finalize() throws Throwable  {
        try {
            parent = null;
            coverage = null;
            for (int i=0; i< children.length; ++i)
                if (children[i] != null)
                    children[i].clear();
            if (texture != null) {
                texture.requestCleanup();
                texture = null ;
            }
        } finally {
            super.finalize();
        }
    }
    
    int getS() {
        return s;
    }
    
    int getT() {
        return t;
    }
    
    int getDS() {
        return d_s;
    }
    
    int getDT() {
        return d_t;
    }
    
    public int getIntLat() {
        return lat;
    }
    
    public int getIntLon() {
        return lon;
    }
    
    public int getIntLatDim() {
        return d_lat;
    }
    
    public int getIntLonDim() {
        return d_lon;
    }
    
    float getLoadedTextureCoverage() {
        if (texture != null)
            return 1;
        float retval = 0;
        for (int i = 0; i<4;++i) {
            if (children[i] != null) {
                Object o = children[i].get();
                if (o != null)
                    retval += ((TextureTile)o).getLoadedTextureCoverage()/4;
            }
        }
        return retval;
    }
    
    TextureTile getChild(int ch_ix) {
        
        return null;
    }
    
    TextureTile getChild(int ch_ix, int new_ts) {
        if (children[ch_ix] != null) {
            Object o = children[ch_ix].get();
            if (o != null)
                return (TextureTile)o;
        }
        TextureTile tt = new TextureTile(this, ch_ix, new_ts);
        children[ch_ix] = new SoftReference(tt);
        
        return tt;
    }
    
    TextureTile getParent() {
        return parent;
    }
    
    synchronized public boolean waitForLoading() {
        if (status == WAIT_FOR_LOADING ||
                status == IS_LOADING) return false;
        status = WAIT_FOR_LOADING;
        return true;
    }
    
    static private class Counter {
        int num = 0;
        int group_num = 10;
        synchronized void count() {
            if ((++num % group_num) == 0) {
                // System.out.println("Teksturer lastet: " + num);
            }
        }
    }
    
    private static Counter counter = new Counter();
    
    /**
     * Set the texture object of the texture tile, completes loading
     * @param texture
     */
    synchronized public void setTexture(Texture2D texture, long file_mod_time) {
        if (this.texture != null && this.texture != texture)
            this.texture.deleteTexture();
        this.texture = texture;
        this.file_mod_time = file_mod_time;
        
        if (this.texture != null) {
            double len =
                    coverage.getSurface().intToLat(lat+d_lat) -
                    coverage.getSurface().intToLat(lat);
            len *= coverage.getSurface().getEllipsoid().getA();
            pixelSize = (float)(len/texture.getHeight());
            counter.count();
        }
        
        stopLoading();
    }
    
    
    synchronized public void setOutsideOfArea() {
        if (status != IS_LOADING)
            throw new IllegalStateException("The tile is not in the IS_LOADING state");
        status = OUTSIDE_OF_AREA;
        notifyAll();
    }
    synchronized public void setOutsideOfResolution() {
        if (status != IS_LOADING)
            throw new IllegalStateException("The tile is not in the IS_LOADING state");
        status = OUTSIDE_OF_RESOLUTION;
        notifyAll();
    }
    
    synchronized public void startLoading() {
        if (status != WAIT_FOR_LOADING)
            throw new IllegalStateException("The tile is not in the WAIT_FOR_LOADING state");
        status = IS_LOADING;
        notifyAll();
    }
    
    synchronized public void stopLoading() {
        if (status == IS_LOADING /* || status == LOADING_ERROR*/) {
            status = IS_LOADED;
            notifyAll();
        } else if (status == LOADING_ERROR) {
            status = NOT_LOADED;
            notifyAll();
        } else
            throw new IllegalStateException("The tile is not in the IS_LOADING state");
    }
    
    synchronized public void abortLoading() {
        if (status == IS_LOADING) {
            status = LOADING_ERROR;
            notifyAll();
        }
    }
    
    synchronized public boolean isOutsideOfArea() {
        return status == OUTSIDE_OF_AREA;
    }
    synchronized public boolean isOutsideOfResolution() {
        return status == OUTSIDE_OF_RESOLUTION;
    }
    
    public int getTs() {
        return ts;
    }
    
    public float getPri() {
        return pri;
    }
    
    public long getFileModTime() {
        return file_mod_time;
    }
    
    /**
     * Use this texture tile for texturing,
     * set the timestamp to the current value and
     * (if necessary and requested) start loading.
     *
     * @param ts
     * @param do_load
     * @return
     */
    synchronized Texture2D useTexture(int ts, float pri, boolean do_load) {
        if (ts > this.ts && (texture != null || do_load)) {
            this.ts = ts;
            this.pri = pri;
        }
        if (status == NOT_LOADED && do_load)
            coverage.load(this);
        return getTexture();
    }
    
    public float getPixelSize() {
        return pixelSize;
    }
    
    public Texture2D getTexture() {
        return texture;
    }
    
    public String getPosCode() {
        return posCode;
    }
    
    /**
     * Run through the texture hierarchy,
     * delete the textures that are older than cut_ts
     *
     * @param cut_ts
     */
    
    private int ycnt = 0;
    int gc(int cut_ts) {
        if ((++ycnt)%1000 == 0) {
            ycnt = 0;
            Thread.yield();
        }
        int visible_set_mem = 0;
        synchronized (this) {
            if (ts >= cut_ts) {
                if (texture != null)
                    visible_set_mem += texture.getTexMemory();
            } else if (status != IS_LOADING) {
                if (texture != null) {
                    // Unload data
                    texture.deleteTexture();
                    texture = null;
                    file_mod_time = 0;
                    if (status == IS_LOADED)
                        status = NOT_LOADED;
                }
                if (status == WAIT_FOR_LOADING && coverage.stopLoadingTextureTile(this))
                    status = NOT_LOADED;
                
            }
        }
        // Gc the children
        for (int i = 0; i < 4; ++i) {
            if (children[i] != null) {
                Object o = children[i].get();
                if (o != null)
                    visible_set_mem += ((TextureTile)o).gc(cut_ts);
            }
        }
    /*
    for (int i = 0; i < 4; ++i)
      if (children[i] != null)
        children[i].gc(cut_ts);
     
    // Unlink this tile
    if (ts < cut_ts &&
        parent != null &&
        status != IS_LOADED &&
        status != IS_LOADING &&
        children[0] == null &&
        children[1] == null &&
        children[2] == null &&
        children[3] == null) {
      for (int i = 0; i < 4; ++i)
        if (parent.children[i] == this)
          parent.children[i] = null;
      parent = null;
      coverage = null;
      status = IS_DELETED;
    }
     */
        return visible_set_mem;
    }
    
    synchronized void clearTextures() {
        if (texture != null) {
            // Unload data
            file_mod_time = 0;
            texture.requestCleanup();
            texture = null;
            status = NOT_LOADED;
        }
        // clear the children
        for (int i = 0; i < 4; ++i) {
            if (children[i] != null) {
                Object o = children[i].get();
                if (o != null)
                    ((TextureTile)o).clearTextures();
            }
            children[i] = null;
        }
        // Unlink this tile
        parent = null;
        coverage = null;
        status = IS_DELETED;
    }
    
    synchronized void reloadTextures() {
        // clear the children
        for (int i = 0; i < 4; ++i) {
            if (children[i] != null) {
                Object o = children[i].get();
                if (o != null)
                    ((TextureTile)o).reloadTextures();
            }
        }
        file_mod_time = 0;
        if (status == IS_LOADING) {
            new Thread() {
                public void run() {
                    synchronized (TextureTile.this) {
                        while (status == IS_LOADING) {
                            try { TextureTile.this.wait(); } catch (InterruptedException ex)  {}
                        }
                        if (status != NOT_LOADED)
                            coverage.load(TextureTile.this);
                    }
                }
            }.start();
        } else if (status != NOT_LOADED)
            coverage.load(this);
    }
}

