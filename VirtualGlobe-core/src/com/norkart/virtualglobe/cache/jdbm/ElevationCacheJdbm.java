//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.cache.jdbm;

import com.norkart.virtualglobe.globesurface.ElevationSource;
import com.norkart.virtualglobe.cache.ElevationCache;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.btree.BTree;
import jdbm.helper.Serializer;



/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */

public class ElevationCacheJdbm extends ElevationCache {
    protected ElevationSource.ModelQuery    server_mq  = null;
    RecordManager                           recman     = null;
    CacheManagerJdbm.ElevationSurfaceRecord rec        = null;
    BTree                                   elevations = null;
    
    // private byte[] code = new byte[9];
    public static class Record extends ElevationCache.Record {
        public int ts;
    }
    
    static final class RecordSerializer implements Serializer {
        final static RecordSerializer INSTANCE = new RecordSerializer();
        
        public Object deserialize(byte[] buf) {
            if (buf == null)
                return null;
            Record rec = new Record();
            int offset = 0;
            rec.h = ( buf[ offset++ ] << 24 )
            | ( ( buf[ offset++ ] << 16 ) & 0x00FF0000 )
            | ( ( buf[ offset++ ] << 8 ) & 0x0000FF00 )
            | ( ( buf[ offset++ ] << 0 ) & 0x000000FF );
            rec.h ^= 0x80000000;
            rec.dh = ( buf[ offset++ ] << 24 )
            | ( ( buf[ offset++ ] << 16 ) & 0x00FF0000 )
            | ( ( buf[ offset++ ] << 8 ) & 0x0000FF00 )
            | ( ( buf[ offset++ ] << 0 ) & 0x000000FF );
            rec.dh ^= 0x80000000;
            rec.ts = ( buf[ offset++ ] << 24 )
            | ( ( buf[ offset++ ] << 16 ) & 0x00FF0000 )
            | ( ( buf[ offset++ ] << 8 ) & 0x0000FF00 )
            | ( ( buf[ offset++ ] << 0 ) & 0x000000FF );
            rec.ts ^= 0x80000000;
            return rec;
        }
        public byte[] serialize(Object obj) {
            Record rec = (Record) obj;
            byte [] data = new byte[12];
            int offs = 0;
            int val = rec.h ^ 0x80000000;
            data[offs++] = (byte) ( val >> 24 );
            data[offs++] = (byte) ( val >> 16 );
            data[offs++] = (byte) ( val >> 8 );
            data[offs++] = (byte) val;
            val = rec.dh ^ 0x80000000;
            data[offs++] = (byte) ( val >> 24 );
            data[offs++] = (byte) ( val >> 16 );
            data[offs++] = (byte) ( val >> 8 );
            data[offs++] = (byte) val;
            val = rec.ts ^ 0x80000000;
            data[offs++] = (byte) ( val >> 24 );
            data[offs++] = (byte) ( val >> 16 );
            data[offs++] = (byte) ( val >> 8 );
            data[offs++] = (byte) val;
            return data;
        }
    }
    
    ElevationCacheJdbm(CacheManagerJdbm cache_mgr, String name, ElevationSource.ModelQuery server_mq) throws IOException {
        super(cache_mgr, name);
        this.server_mq = server_mq;
        cache_mgr.initElevationCache(this);
    }
    
    
    String getName() {
        return name;
    }
    
    void setCacheManager(CacheManagerJdbm cache_mgr) {
        this.cache_mgr = cache_mgr;
    }
    
    
    public ElevationSource.ModelQuery getModelQuery() {
        if (rec == null)
            return null;
        ElevationSource.ModelQuery mq = new ElevationSource.ModelQuery();
        mq.a = rec.a;
        mq.f = rec.f;
        mq.hScale = rec.hScale;
        mq.version = rec.version;
        mq.modelType = rec.model_type;
        
        return mq;
    }
    
    
    public ElevationCache.Record get(byte [] key) {
        synchronized (rec) {
            // byte [] key = q_rec.key;
            // if (key == null) {
            // q_rec.getKey(code);
            //key = code;
            // }
            Record db_rec = null;
            try {
                if (((CacheManagerJdbm)cache_mgr).recman != recman)
                    ((CacheManagerJdbm)cache_mgr).initElevationCache(this);
                if (cache_mgr.isOpen())
                    db_rec = (Record)elevations.find(key);
                if (db_rec != null) {
                    db_rec.ts = cache_mgr.ts();
                    if (cache_mgr.isOpen())
                        elevations.insert(key, db_rec, true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                db_rec = null;
            } catch (Throwable ex) {
                ++((CacheManagerJdbm)cache_mgr).bad_warnings;
                System.err.println("Possibly bad cache database");
                ex.printStackTrace();
                db_rec = null;
                /*
                try {
                    ((CacheManagerJdbm)cache_mgr).initElevationCache(this);
                } catch (IOException io_ex) {
                    io_ex.printStackTrace();
                }
                 */
            }
            return db_rec;
        }
    }
    
    
    public void set(byte [] key, int h, int dh) {
        synchronized (rec) {
            Record db_rec = new Record();
            db_rec.h  = h;
            db_rec.dh = dh;
            db_rec.ts = cache_mgr.ts();
            try {
                if (((CacheManagerJdbm)cache_mgr).recman != recman)
                    ((CacheManagerJdbm)cache_mgr).initElevationCache(this);
                
                if (cache_mgr.isOpen())
                    elevations.insert(key, db_rec, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Throwable ex) {
                ++((CacheManagerJdbm)cache_mgr).bad_warnings;
                System.err.println("Possibly bad cache database");
                ex.printStackTrace();
/*
                try {
                    ((CacheManagerJdbm)cache_mgr).initElevationCache(this);
                } catch (IOException io_ex) {
                    io_ex.printStackTrace();
                }
 */
            }
        }
    }
    
    public  boolean lock() {
        if (!cache_mgr.isOpen())
            return false;
        synchronized (rec) {
            rec.busy = true;
        }
        return true;
    }
    public  void unlock() {
        if (rec != null) {
            synchronized (rec) {
                rec.busy = false;
                rec.notifyAll();
            }
        }
    }
}
