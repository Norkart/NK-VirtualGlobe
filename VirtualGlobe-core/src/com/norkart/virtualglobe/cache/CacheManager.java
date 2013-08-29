//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * CacheManager.java
 *
 * Created on 2. november 2006, 13:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.cache;

import com.norkart.virtualglobe.globesurface.ElevationSource;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author runaas
 */
public abstract class CacheManager {
    protected File cache_dir;
    protected long max_cache_size = 2000*1024*1024;
    
    public File getCacheDir() {
        return cache_dir;
    }
    
    public long getCacheSize() {
        return max_cache_size;
    }
    
    public void setCacheSize(long sz) {
        max_cache_size = sz;
    }
    
    public static void killAll(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (int i=0; i<files.length; ++i)
                killAll(files[i]);
        }
        f.delete();
    }
    
    protected static void killEmpty(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (int i=0; i<files.length; ++i)
                killEmpty(files[i]);
            files = f.listFiles();
            if (files.length == 0)
                f.delete();
        } else if (f.length() == 0)
            f.delete();
    }
    
    public void setCache(File cache_dir, long size) {
        this.max_cache_size = size;
        if (this.cache_dir == cache_dir)
            return;
        
        try {
            if (this.cache_dir != null)
                close(true);
            open(cache_dir);
        } catch (Throwable ex) {
                System.err.println("Bad cache at: " + cache_dir + " creating new");
                recreate();
        }
    }
    
    protected synchronized void recreate() {
        try {
            delete();
            open(cache_dir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static File getFile(File root, String pos_code) {
        return getFile(root, pos_code, null);
    }
    
    public static File getFile(File root, String pos_code, String suffix) {
        String path = "";
        for (int i = 0; ; i += 3) {
            if (i+3 >= pos_code.length()) {
                path += pos_code.substring(i);
                break;
            }
            path += pos_code.substring(i, i+3) + File.separator;
        }
        if (suffix != null && suffix.length() > 0)
            path += "."+suffix;
        return new File(root, path);
    }
    
    protected class TsSize {
        private long [] ts_size;
        private int max_ts;
        private int min_ts;
        private long size;
        
        public TsSize(int ts_size_sz) {
            ts_size = new long[ts_size_sz];
        }
        
        public void init(int min_ts, int max_ts) {
            this.max_ts = max_ts;
            this.min_ts = min_ts;
            size = 0;
            Arrays.fill(ts_size, 0);
        }
        
        public long getSize() {
            return size;
        }
        
        
        public void add(long ts, long size) {
            int i = (int)Math.ceil((ts - min_ts)*(double)ts_size.length/(max_ts - min_ts));
            if (i < 0) i = 0;
            if (i >= ts_size.length) i = ts_size.length-1;
            ts_size[i] += size;
            this.size +=size;
        }
        
        public int getCutTs(long max_size) {
            long sum = 0;
            for (int i=ts_size.length-1; i >= 0; --i) {
                sum += ts_size[i];
                if (sum >= max_size) {
                    return ((int)Math.ceil(((double)(i+1))*(max_ts - min_ts)/ts_size.length)) + min_ts;
                }
            }
            return Integer.MIN_VALUE;
        }
    }
    
    abstract public int ts();
    abstract public int newTs();
    
    abstract protected void open(File cache_dir) throws IOException;
    abstract protected void close(boolean wait) throws IOException;
    abstract public void commit() throws IOException;
    abstract public boolean isOpen();
    abstract protected void delete() throws IOException;
    
    abstract public ElevationCache getElevationCache(String name, ElevationSource.ModelQuery server_mq) throws IOException;
    abstract public File           getTileCacheFile(String name, String pos_code, String suffix) throws IOException;
    
    abstract public void deleteFile(URL url);
    abstract public InputStream getInputStream(URL url) throws IOException;
}
