//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * ElevationCache.java
 *
 * Created on 2. november 2006, 13:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.cache;

import com.norkart.virtualglobe.globesurface.ElevationSource;
import java.io.IOException;

/**
 *
 * @author runaas
 */
public abstract class ElevationCache {
    public static class Record  {
        public int h;
        public int dh;
    }
    
    protected CacheManager   cache_mgr;
  
    protected String                              name;
    
    protected ElevationCache(CacheManager cache_mgr, String name) throws IOException {
        this.cache_mgr = cache_mgr;
        this.name      = name;
    }
    
    public CacheManager getCacheManager() {
        return cache_mgr;
    }
    
    public abstract ElevationSource.ModelQuery getModelQuery();
    public abstract Record get(byte [] key);
    public abstract void set(byte [] key, int h, int dh);
    public abstract boolean lock();
    public abstract void unlock();
}
