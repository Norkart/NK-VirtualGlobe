//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.cache.jdbm;

import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.cache.CacheUtil;
import com.norkart.virtualglobe.cache.ElevationCache;
import com.norkart.virtualglobe.globesurface.ElevationSource;

import jdbm.*;
import jdbm.helper.*;
import jdbm.btree.BTree;
import jdbm.htree.HTree;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.ref.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */

public final class CacheManagerJdbm extends CacheManager {
    final private static String HEADER_NAME = "Header";
    final private static String DB_VERSION = "Virtual Globe Cache DB 1.01";
    
    final private static String TS_NAME                    = "Ts";
    final private static String ELEVATION_SURFACE_SET_NAME = "ElevationSurfaceSet";
    final private static String TILE_PYRAMID_SET_NAME      = "TilePyramidSet";
    final private static String FILE_SET_NAME              = "FileSet";
    final private static String TILE_SET_NAME              = "TileSet";
    
    private boolean enabled = false;
    RecordManager recman = null;
    
    private File tile_dir;
    private File file_dir;
    
    private String db_name;
    
    private long ts_recid;
    private TimeStamp ts;
    
    // private long size = 0;
    
    final static int MAX_BAD_WARNINGS = 20;
    int bad_warnings = 0;
    
    
    private FileGC file_gc;
    private ElevationGC el_gc;
    
    private long                elevation_surface_set_recid;
    private ElevationSurfaceSet elevation_surface_set;
    
    private long                tile_pyramid_set_recid;
    private TilePyramidSet      tile_pyramid_set;
    
    private long                file_set_recid;
    private HTree               file_set;
    
    private long                tile_set_recid;
    private BTree               tile_set;
    
    static class Lock {
        boolean locked = false;
    }
    private Lock tile_set_lock = new Lock();
    
    /**
     * Database header contains all userdefined database record classes
     * to assure that changes in class signature is discovered at database open time
     *
     * <p>Title: Virtual Globe</p>
     * <p>Description: </p>
     * <p>Copyright: Copyright (c) 2004</p>
     * <p>Company: SINTEF</p>
     * @author unascribed
     * @version 1.0
     */
    
    private static class Header implements Serializable {
        String                            version = DB_VERSION;
        ElevationSurfaceRecord            surf_rec = new ElevationSurfaceRecord();
        TilePyramidRecord                 tile_rec = new TilePyramidRecord();
        FileRecord                        file_rec = new FileRecord();
        ElevationSource.ByteArraySizeComp ele_key_comp = ElevationSource.ByteArraySizeComp.INSTANCE;
        ElevationCacheJdbm.RecordSerializer   ele_rec = ElevationCacheJdbm.RecordSerializer.INSTANCE;
    }
    
    private static class TimeStamp implements Serializable {
        transient boolean dirty = false;
        int ts      = Integer.MIN_VALUE + 1;
        int gc_ts   = Integer.MIN_VALUE;
        int file_id = Integer.MIN_VALUE;
        long size   = 0;
    }
    /*
    static class DatasetRecord implements Serializable{
        transient SoftReference btree_ref;
        transient boolean busy;
        long recid;
    }
     */
    static class ElevationSurfaceSet implements Serializable {
        transient boolean dirty = false;
        Hashtable surface_set = new Hashtable();
        Vector deleted_recid  = new Vector();
    }
    
    static class ElevationSurfaceRecord implements Serializable {
        transient boolean busy = false;
        transient SoftReference btree_ref;
        long recid;
        double a;
        double f;
        float  hScale;
        int    version;
        int    model_type;
        int ts = Integer.MAX_VALUE;
    }
    
    static class TilePyramidSet implements Serializable {
        transient boolean dirty = false;
        Hashtable name_to_id = new Hashtable();
        Hashtable id_to_name = new Hashtable();
    }
    
    static class TilePyramidRecord implements Serializable {
        Integer  id;
        String   name;
    }
    
    static private class FileRecord implements Serializable {
        int ts;
        int id;
    }
    
    public CacheManagerJdbm() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try { close(false); } catch (Throwable ex) {}
            }
        });
    }
    
    
    protected void open(File cache_dir) throws IOException {
        this.cache_dir = cache_dir;
        if (cache_dir == null)
            return;
        
        db_name = new File(cache_dir, "cache").toString();
        tile_dir = new File(cache_dir, "Tiles");
        file_dir = new File(cache_dir, "Files");
        
        long db_sz = new File(db_name+".db").length();
        File del_file = new File(db_name+".delete");
        if (db_sz > max_cache_size/2 || del_file.exists()) {
            del_file.delete();
            delete();
        } else {
            final File old_db_file = new File(db_name+".db.old");
            final File old_lg_file = new File(db_name+".lg.old");
            final File old_tile_dir = new File(tile_dir.toString() + ".old");
            final File old_file_dir = new File(file_dir.toString() + ".old");
            if (old_db_file.exists() ||
                old_lg_file.exists() ||
                old_tile_dir.exists() ||
                old_file_dir.exists()) {
                new Thread() {
                    public void run() {
                        
                        old_db_file.delete();
                        old_lg_file.delete();
                        killAll(old_tile_dir);
                        killAll(old_file_dir);
                    }
                }.start();
            }
        }
        
        cache_dir.mkdirs();
        tile_dir.mkdirs();
        file_dir.mkdirs();
        
        // Open database
        Properties opt = new Properties();
        opt.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "true");
        // opt.setProperty(RecordManagerOptions.CACHE_SIZE, "10000");
        recman = RecordManagerFactory.createRecordManager(db_name, opt);
        
        // Test cache database version
        long header_id = recman.getNamedObject(HEADER_NAME);
        if (header_id != 0) {
            Header header = (Header)recman.fetch(header_id);
            if (!header.version.equals(DB_VERSION))
                throw new IOException("Bad database version");
        } else {
            Header header = new Header();
            header_id = recman.insert(header);
            recman.setNamedObject(HEADER_NAME, header_id);
        }
        
        // Load timestamps
        ts_recid = recman.getNamedObject(TS_NAME);
        if (ts_recid != 0)
            ts = (TimeStamp)recman.fetch(ts_recid);
        else {
            ts = new TimeStamp();
            ts_recid = recman.insert(ts);
            recman.setNamedObject(TS_NAME, ts_recid);
        }
        
        // Load Elevation Surface set
        elevation_surface_set_recid = recman.getNamedObject(ELEVATION_SURFACE_SET_NAME);
        if (elevation_surface_set_recid != 0)
            elevation_surface_set = (ElevationSurfaceSet)recman.fetch(elevation_surface_set_recid);
        else {
            elevation_surface_set = new ElevationSurfaceSet();
            elevation_surface_set_recid = recman.insert(elevation_surface_set);
            recman.setNamedObject(ELEVATION_SURFACE_SET_NAME, elevation_surface_set_recid);
        }
        
        // Load Tile Pyramid set
        tile_pyramid_set_recid = recman.getNamedObject(TILE_PYRAMID_SET_NAME);
        if (tile_pyramid_set_recid != 0)
            tile_pyramid_set = (TilePyramidSet)recman.fetch(tile_pyramid_set_recid);
        else {
            tile_pyramid_set = new TilePyramidSet();
            tile_pyramid_set_recid = recman.insert(tile_pyramid_set);
            recman.setNamedObject(TILE_PYRAMID_SET_NAME, tile_pyramid_set_recid);
        }
        
        // Load file sets
        file_set_recid = recman.getNamedObject(FILE_SET_NAME);
        if (file_set_recid != 0)
            file_set = HTree.load(recman, file_set_recid);
        else {
            file_set = HTree.createInstance(recman);
            file_set_recid = file_set.getRecid();
            recman.setNamedObject(FILE_SET_NAME, file_set_recid);
        }
        
        // Load tile sets
        tile_set_recid = recman.getNamedObject(TILE_SET_NAME);
        if (tile_set_recid != 0)
            tile_set = BTree.load(recman, tile_set_recid);
        else {
            tile_set = BTree.createInstance(recman,
                    new jdbm.helper.ByteArrayComparator(),
                    new jdbm.helper.ByteArraySerializer(),
                    new jdbm.helper.IntegerSerializer());
            tile_set_recid = tile_set.getRecid();
            recman.setNamedObject(TILE_SET_NAME, tile_set_recid);
        }
        
        recman.commit();
        enabled = true;
        
        file_gc = new FileGC();
        file_gc.setPriority(Thread.MIN_PRIORITY);
        file_gc.start();
        
        el_gc = new ElevationGC();
        el_gc.setPriority(Thread.MIN_PRIORITY);
        el_gc.start();
        
        // Delete old stuff
        final File old_db_file = new File(db_name+".db.old");
        final File old_lg_file = new File(db_name+".lg.old");
        final File old_tile_dir = new File(tile_dir.toString() + ".old");
        final File old_file_dir = new File(file_dir.toString() + ".old");
        
        new Thread() {
            public void run(){
                if (old_db_file.exists())
                    old_db_file.delete();
                if (old_lg_file.exists())
                    old_lg_file.delete();
                if (old_tile_dir.exists())
                    killAll(old_tile_dir);
                if (old_file_dir.exists())
                    killAll(old_file_dir);
            }
        }.start();
    }
    
    public boolean isOpen() {
        return enabled && recman != null;
    }
    
    protected void close(boolean wait) throws IOException {
        if (enabled) {
            enabled = false;
            if (el_gc != null)
                el_gc.interrupt();
            if (file_gc != null)
                file_gc.interrupt();
        }
        if (wait) {
            try {
                if (el_gc != null)
                    el_gc.join();
                if (file_gc != null)
                    file_gc.join();
            } catch (InterruptedException ex) {}
        }
        if (recman != null) {
            try {
                recman.commit();
                recman.close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            recman = null;
        }
        el_gc = null;
        file_gc = null;
        elevation_surface_set = null;
        elevation_surface_set_recid = 0;
        tile_pyramid_set = null;
        tile_pyramid_set_recid = 0;
        file_set = null;
        tile_set = null;
        file_set_recid = 0;
        ts = null;
        ts_recid = 0;
    }
    
    public void commit() throws IOException {
        if (recman == null) return;
        if (ts.dirty)
            recman.update(ts_recid, ts);
        if (elevation_surface_set.dirty)
            recman.update(elevation_surface_set_recid, elevation_surface_set);
        if (tile_pyramid_set.dirty)
            recman.update(tile_pyramid_set_recid, tile_pyramid_set);
        recman.commit();
    }
    
    protected void delete() throws IOException {
        close(true);
        File db_file = new File(db_name+".db");
        File lg_file = new File(db_name+".lg");
        final File old_db_file = new File(db_name+".db.old");
        final File old_lg_file = new File(db_name+".lg.old");
        final File old_tile_dir = new File(tile_dir.toString() + ".old");
        final File old_file_dir = new File(file_dir.toString() + ".old");
        final File del_file = new File(db_name+".delete");
        
        boolean db_file_error = db_file.exists() && !db_file.renameTo(old_db_file);
        boolean lg_file_error = lg_file.exists() && !lg_file.renameTo(old_lg_file);
        boolean tile_dir_error = tile_dir.exists() && !tile_dir.renameTo(old_tile_dir);
        boolean file_dir_error = file_dir.exists() && !file_dir.renameTo(old_file_dir);
        if (db_file_error  || lg_file_error || tile_dir_error || file_dir_error) {
            FileOutputStream fout = new FileOutputStream(del_file);
            fout.close();
            javax.swing.JOptionPane.showMessageDialog(null,
                    com.norkart.virtualglobe.util.ApplicationSettings.getApplicationSettings().getResourceString("RESTART_MESSAGE"));
        }
        new Thread() {
            public void run() {
                old_db_file.delete();
                old_lg_file.delete();
                killAll(old_tile_dir);
                killAll(old_file_dir);
            }
        }.start();
    }
    
    public ElevationCache getElevationCache(String name, ElevationSource.ModelQuery server_mq) throws IOException {
        return new ElevationCacheJdbm(this, name, server_mq);
    }
    public File getTileCacheFile(String name, String pos_code, String suffix) throws IOException {
        if (!enabled)
            return null;
        
        TilePyramidRecord rec = null;
        File root = null;
        synchronized (tile_pyramid_set) {
            
            Object o = tile_pyramid_set.name_to_id.get(name);
            if (o != null) {
                rec = (TilePyramidRecord)o;
            } else {
                rec = new TilePyramidRecord();
                rec.name = name;
                
                rec.id = new Integer(name.hashCode());
                while (tile_pyramid_set.id_to_name.containsKey(rec.id))
                    rec.id = new Integer(rec.id.intValue()+1);
                
                tile_pyramid_set.name_to_id.put(rec.name, rec);
                tile_pyramid_set.id_to_name.put(rec.id, rec);
                
                tile_pyramid_set.dirty = true;
                commit();
            }
            
            root = new File(tile_dir, Integer.toHexString(rec.id.intValue()));
        }
        
        byte [] pos_bytes = pos_code.getBytes("UTF-8");
        byte [] key = new byte[pos_bytes.length + 4];
        CacheUtil.serializeInt4(rec.id.intValue(), key, 0);
        for (int i=0; i<pos_bytes.length;++i)
            key[i+4] = pos_bytes[i];
        
        synchronized (tile_set_lock) {
            while (enabled && tile_set_lock.locked) {
                try {
                    tile_set_lock.wait();
                } catch (InterruptedException ex) {}
            }
            
            if (!enabled) return null;
            
            try {
                tile_set.insert(key, new Integer(newTs()), true);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (Throwable ex) {
                ++bad_warnings;
                System.err.println("Possibly bad cache database");
                ex.printStackTrace();
                return null;
            }
            File f = getFile(root, pos_code, suffix);
            return f;
        }
    }
    
    void initElevationCache(ElevationCacheJdbm el_set) throws IOException {
        el_set.elevations = null;
        el_set.rec = null;
        if (!enabled)
            return;
        
        synchronized (elevation_surface_set) {
            el_set.recman    = recman;
            el_set.setCacheManager(this);
            Object o = elevation_surface_set.surface_set.get(el_set.getName());
            if (o != null)
                el_set.rec = (ElevationSurfaceRecord)o;
            
            if (el_set.server_mq != null && el_set.rec != null && el_set.rec.version != el_set.server_mq.version) {
                elevation_surface_set.deleted_recid.add(new Long(el_set.rec.recid));
                elevation_surface_set.surface_set.remove(el_set.getName());
                elevation_surface_set.dirty = true;
                el_set.rec = null;
            }
            
            if (el_set.rec == null) {
                if (el_set.server_mq == null) {
                    commit();
                    return;
                }
                
                
                el_set.rec =  new ElevationSurfaceRecord();
                el_set.rec.a = el_set.server_mq.a;
                el_set.rec.f = el_set.server_mq.f;
                el_set.rec.hScale = el_set.server_mq.hScale;
                el_set.rec.version = el_set.server_mq.version;
                el_set.rec.model_type = el_set.server_mq.modelType;
                
                el_set.elevations = BTree.createInstance(recman,
                        ElevationSource.ByteArraySizeComp.INSTANCE,
                        ByteArraySerializer.INSTANCE,
                        ElevationCacheJdbm.RecordSerializer.INSTANCE, 128);
                
                el_set.rec.recid = el_set.elevations.getRecid();
                elevation_surface_set.surface_set.put(el_set.getName(), el_set.rec);
                elevation_surface_set.dirty = true;
            } else  if (el_set.rec.btree_ref != null && (o = el_set.rec.btree_ref.get()) != null)
                el_set.elevations = (BTree)o;
            else {
                el_set.elevations = BTree.load(recman, el_set.rec.recid);
                elevation_surface_set.dirty = true;
            }
            
            el_set.setCacheManager(this);
            if (el_set.rec.btree_ref == null || el_set.rec.btree_ref.get() != el_set.elevations)
                el_set.rec.btree_ref = new SoftReference(el_set.elevations);
            
            el_set.rec.ts = ts();
            commit();
        }
    }
    
    private  File getFile(String name) throws IOException {
        if (!enabled) return null;
        
        FileRecord rec = (FileRecord)file_set.get(name);
        if (rec == null) {
            rec = new FileRecord();
            rec.id = newFileId();
            commit();
        }
        rec.ts = newTs();
        file_set.put(name, rec);
        
        return new File(file_dir, Integer.toHexString(rec.id^0x80000000));
    }
    
    public void deleteFile(URL url) {
        if (!enabled) return;
        try {
            String name = url.toString();
            FileRecord rec = (FileRecord)file_set.get(name);
            if (rec == null) return;
            file_set.remove(name);
            
            File f = new File(file_dir, Integer.toHexString(rec.id^0x80000000));
            if (f.exists()) {
                File old_f = new File(f.getAbsolutePath() + ".old");
                f.renameTo(old_f);
                old_f.delete();
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public InputStream getInputStream(URL url) throws IOException {
        File f = getFile(url.toString());
        InputStream in = null;
        try {
            URLConnection connection = (URLConnection)url.openConnection();
            connection.setUseCaches(false);
            if (f != null && f.exists())
                connection.setIfModifiedSince(f.lastModified());
            connection.connect();
            if (connection instanceof HttpURLConnection && ((HttpURLConnection)connection).getResponseCode() == HttpURLConnection.HTTP_OK)
                in = connection.getInputStream();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
        
        if (f == null)
            return in;
        
        if (in == null) {
            if (f.exists() && f.canRead())
                return new FileInputStream(f);
            return null;
        }
        
        f.getParentFile().mkdirs();
        RandomAccessFile rf = new RandomAccessFile(f, "rw");
        rf.setLength(0);
        rf.seek(0);
        
        // Copying
        ReadableByteChannel source = Channels.newChannel(in);
        FileChannel destination = rf.getChannel();
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        // Read from source file into the byte buffer using the source file channel.
        while (source.read(buf) != -1) { // EOF?
            // Prepare to drain the buffer
            buf.flip();
            // Drain the buffer using the destination file channel
            while (buf.hasRemaining()) {
                destination.write(buf);
            }
            // Clear the buffer for reuse
            buf.clear();
        }
        // Completing
        source.close();
        destination.force(true);
        
        rf.seek(0);
        return new FileInputStream(rf.getFD());
    }
    
    /****************************************************************
     * Timestamping
     ****************************************************************/
    public int newTs() {
        if (ts == null) return Integer.MIN_VALUE;
        ts.dirty = true;
        return ++ts.ts;
    }
    
    public int ts() {
        return ts.ts;
    }
    
    public int newFileId() {
        ++ts.file_id;
        ts.dirty = true;
        
        return ts.file_id;
    }
    
    
    
    /****************************************************************
     * Things related to the cache garbage collector
     ****************************************************************/
    
    
    
    class ElevationGC extends Thread {
        public ElevationGC() {
            super("Elevation GC");
        }
        
        
        
        public void run() {
            // Wait for the application to start properly
            try { sleep(30000); } catch (InterruptedException ex) { }
            if (!enabled)
                return;
            
            int prev_gc_ts = ts.gc_ts;
            // int num_deleted = 0;
            // int num_gced = 0;
            while (recman != null) {
                if (bad_warnings > MAX_BAD_WARNINGS) {
                    System.err.println("More than "+MAX_BAD_WARNINGS+" warnings for bad database, recreating cache database");
                    bad_warnings = 0;
                    new Thread() {
                        public void run() {
                            recreate();
                        }
                    }.start();
                    return;
                }
                
                try {
                    // Clear deleted structures, prepare for reuse
                    Iterator it = elevation_surface_set.deleted_recid.iterator();
                    while (bad_warnings <= MAX_BAD_WARNINGS && enabled && it.hasNext()) {
                        Long recid_obj = (Long)it.next();
                        BTree btree = null;
                        try {
                            btree = BTree.load(recman, recid_obj.longValue());
                            byte [] key = {0};
                            Tuple t;
                            long starttime = System.currentTimeMillis();
                            while (bad_warnings <= MAX_BAD_WARNINGS && enabled && (t = btree.findGreaterOrEqual(key)) != null && recman != null) {
                                yield();
                                btree.remove(t.getKey());
                                
                                // if (++num_deleted % 1000 == 0)
                                //   System.out.println("Deleted: " + num_deleted);
                                /*
                                if (System.currentTimeMillis() - starttime > 30) {
                                    try { sleep(5); } catch (InterruptedException ex) {}
                                    starttime = System.currentTimeMillis();
                                }
                                 */
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (Throwable ex) {
                            if (enabled) {
                                ++bad_warnings;
                                System.err.println("Possibly bad cache:");
                                ex.printStackTrace();
                                /*
                                new Thread() {
                                    public void run() {
                                        recreate();
                                    }
                                }.start();
                                 
                                return;
                                 * **/
                            }
                        }
                        if (!enabled) break;
                        if (btree != null && btree.size() == 0) {
                            it.remove();
                            recman.delete(recid_obj.longValue());
                            recman.update(elevation_surface_set_recid, elevation_surface_set);
                            recman.commit();
                        }
                    }
                    
                    
                    
                    // Take a break if we have plenty of room still
                    if (!enabled) break;
                    if (bad_warnings > MAX_BAD_WARNINGS) continue;
                    if (ts.size < max_cache_size && prev_gc_ts == ts.gc_ts) {
                        try { sleep(5000); } catch (InterruptedException ex) {}
                        continue;
                    }
                    if (!enabled) break;
                    if (bad_warnings > MAX_BAD_WARNINGS) continue;
                    prev_gc_ts = ts.gc_ts;
                    
                    // Run through all elevations and remove old elevations
                    
                    Tuple tuple = new Tuple();
                    it = elevation_surface_set.surface_set.entrySet().iterator();
                    while (bad_warnings <= MAX_BAD_WARNINGS && enabled && it.hasNext() && recman != null) {
                        Map.Entry entry = (Map.Entry)it.next();
                        ElevationSurfaceRecord rec = (ElevationSurfaceRecord)entry.getValue();
                        try {
                            BTree elevations = null;
                            if (rec.btree_ref == null || (elevations = (BTree)rec.btree_ref.get()) == null) {
                                elevations = BTree.load(recman, rec.recid);
                                rec.btree_ref = new SoftReference(elevations);
                            }
                            
                            // System.out.print(entry.getKey() + " høyder før: "+elevations.size());
                            byte [] key = {0};
                            // long prev_sleep_time = System.currentTimeMillis();
                            while (bad_warnings <= MAX_BAD_WARNINGS && key != null && enabled) {
                                synchronized (rec) {
                                    while (rec.busy) {
                                        try {
                                            rec.wait();
                                        } catch (InterruptedException ex) {}
                                    }
                                    try {
                                        TupleBrowser browser = elevations.browse(key);
                                        long starttime = System.currentTimeMillis();
                                        
                                        while (bad_warnings <= MAX_BAD_WARNINGS && System.currentTimeMillis() - starttime < 30 && enabled) {
                                            yield();
                                            if (!browser.getNext(tuple)) {
                                                key = null;
                                                break;
                                            }
                                            key = (byte[])tuple.getKey();
                                            ElevationCacheJdbm.Record el_rec = (ElevationCacheJdbm.Record)tuple.getValue();
                                            if (el_rec.ts < ts.gc_ts) {
                                                elevations.remove(key);
                                                //     if (++num_gced % 1000 == 0)
                                                //       System.out.println("GCed: " + num_gced);
                                                break;
                                            }
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (Throwable ex) {
                                        if (enabled) {
                                            ++bad_warnings;
                                            System.err.println("Possibly bad cache database");
                                            ex.printStackTrace();
                                        /*
                                        new Thread() {
                                            public void run() {
                                                recreate();
                                            }
                                        }.start();
                                         *
                                        return;
                                         */
                                        }
                                    }
                                }
                                if (!enabled) break;
                                if (bad_warnings > MAX_BAD_WARNINGS) continue;
                                    /*
                                    if (System.currentTimeMillis() - prev_sleep_time > 30) {
                                        try { sleep(5); } catch (InterruptedException ex) {}
                                        prev_sleep_time = System.currentTimeMillis();
                                    }
                                    if (recman == null) break;
                                     */
                            }
                            // System.out.println(" etter: "+elevations.size());
                            synchronized (rec) {
                                while (rec.busy) {
                                    try {
                                        rec.wait();
                                    } catch (InterruptedException ex) {}
                                }
                                try {
                                    if (elevations.size() == 0 && rec.ts < ts.gc_ts) {
                                        recman.delete(rec.recid);
                                        it.remove();
                                        recman.update(elevation_surface_set_recid, elevation_surface_set);
                                        recman.commit();
                                    }
                                } catch (Throwable ex) {
                                    if (recman != null) {
                                        ++bad_warnings;
                                        System.err.println("Possibly bad cache:");
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException ex) { ex.printStackTrace(); }
                    }
                } catch (ConcurrentModificationException ex) {
                    // } catch (IllegalStateException ex) {
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void purgeTiles(File currdir, int set_id, String pos_code) throws Exception {
        File [] ch_arr = currdir.listFiles();
        
        for (int i=0; enabled && ch_arr != null && i<ch_arr.length; ++i) {
            String basename = ch_arr[i].getName();
            int pointpos = basename.indexOf('.');
            if (pointpos >= 0)
                basename = basename.substring(0, pointpos);
            String ch_poscode = pos_code + basename;
            if (ch_arr[i].isDirectory()) {
                purgeTiles(ch_arr[i], set_id, ch_poscode);
                ch_arr[i].delete();
            } else {
                if (!enabled)
                    return;
                
                byte [] pos_bytes = pos_code.getBytes("UTF-8");
                byte [] key = new byte[pos_bytes.length + 4];
                CacheUtil.serializeInt4(set_id, key, 0);
                for (int j=0; j<pos_bytes.length;++j)
                    key[j+4] = pos_bytes[j];
                
                Integer ts_int = (Integer)tile_set.find(key);
                if (ts_int == null)
                    ch_arr[i].delete();
                else
                    ts_size.add(ts_int.intValue(), ch_arr[i].length());
            }
        }
    }
    
    private TsSize ts_size = new TsSize(10000);
    
    class FileGC extends Thread {
        public FileGC() {
            super("File GC");
        }
        
        public void run() {
            // Wait for the application to start properly
            try { sleep(30000); } catch (InterruptedException ex) { }
            while (enabled) {
                try {
                    // Prepare age_size array
                    ts_size.init(ts.gc_ts, ts.ts);
                    
                    // Find old tiles
                    try {
                        byte[] key = new byte[] {0};
                        long prev_sleep_time = System.currentTimeMillis();
                        while (key != null && enabled) {
                            synchronized (tile_set_lock) {
                                tile_set_lock.locked = true;
                            }
                            try {
                                Tuple tuple = new Tuple();
                                TupleBrowser browser = tile_set.browse(key);
                                long starttime = System.currentTimeMillis();
                                while (System.currentTimeMillis() - starttime < 10 && enabled) {
                                    yield();
                                    if (!browser.getNext(tuple)) {
                                        key = null;
                                        break;
                                    }
                                    key = (byte[])tuple.getKey();
                                    if (((Integer)tuple.getValue()).intValue() < ts.gc_ts) {
                                        tile_set.remove(key);
                                        break;
                                    }
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (Throwable ex) {
                                if (enabled) {
                                    ++bad_warnings;
                                    System.err.println("Possibly bad cache database");
                                    ex.printStackTrace();
                                }
                            }
                            synchronized (tile_set_lock) {
                                tile_set_lock.locked = false;
                                tile_set_lock.notifyAll();
                            }
                            
                            if (!enabled) break;
                            if (System.currentTimeMillis() - prev_sleep_time > 30) {
                                try { sleep(10); } catch (InterruptedException ex) {}
                                prev_sleep_time = System.currentTimeMillis();
                            }
                            if (!enabled) break;
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                    
                    try {
                        File[] tile_sets = tile_dir.listFiles();
                        for (int i=0; i<tile_sets.length; ++i) {
                            int set_id = (int)Long.parseLong(tile_sets[i].getName(), 16);
                            purgeTiles(tile_sets[i], set_id, "");
                            tile_sets[i].delete();
                        }
                    } catch (Throwable ex) {
                        ++bad_warnings;
                        System.err.println("Possibly bad cache database");
                        ex.printStackTrace();
                    }
                    /*
                    try {
                        // System.out.print(rec.name + " tiles før: "+tiles.size());
                        byte[] key = new byte[] {0};
                        long prev_sleep_time = System.currentTimeMillis();
                        while (key != null && recman != null) {
                            synchronized (tile_set_lock) {
                                tile_set_lock.locked = true;
                            }
                            try {
                                Tuple tuple = new Tuple();
                                TupleBrowser browser = tile_set.browse(key);
                                long starttime = System.currentTimeMillis();
                                while (System.currentTimeMillis() - starttime < 10 && recman != null) {
                                    yield();
                                    if (!browser.getNext(tuple)) {
                                        key = null;
                                        break;
                                    }
                                    key = (byte[])tuple.getKey();
                                    int id = CacheUtil.deserializeInt4(key, 0);
                                    String pos_code = new String(key, 4, key.length-4, "UTF-8");
                                    File f = new File(tile_dir, Integer.toHexString(id));
                                    f = getFile(f, pos_code);
                                    File [] cand = f.getParentFile().listFiles();
                                    String basename = f.getName();
                                    if (((Integer)tuple.getValue()).intValue() < ts.gc_ts) {
                                        for (int k = 0; cand != null && k< cand.length; ++k) {
                                            if (cand[k].getName().equals(basename) ||
                                                    (cand[k].getName().indexOf(basename) == 0 &&
                                                    cand[k].getName().charAt(basename.length()) == '.'))
                                                cand[k].delete();
                                        }
                                        tile_set.remove(key);
                                        break;
                                    } else {
                                        for (int k = 0; cand != null &&k< cand.length; ++k) {
                                            if (cand[k].getName().equals(basename) ||
                                                    (cand[k].getName().indexOf(basename) == 0 &&
                                                    cand[k].getName().charAt(basename.length()) == '.'))
                                                ts_size.add(((Integer)tuple.getValue()).intValue(), cand[k].length());
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (Exception ex) {
                                if (recman != null) {
                                    System.err.println("Possibly bad cache database");
                                    ex.printStackTrace();
                                }
                            }
                            synchronized (tile_set_lock) {
                                tile_set_lock.locked = false;
                                tile_set_lock.notifyAll();
                            }
                     
                            if (recman == null) break;
                            if (System.currentTimeMillis() - prev_sleep_time > 30) {
                                try { sleep(10); } catch (InterruptedException ex) {}
                                prev_sleep_time = System.currentTimeMillis();
                            }
                            if (recman == null) break;
                     
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                     */
                    
                    // Cleanup independent files
                    try {
                        FastIterator it = file_set.keys();
                        String name;
                        long starttime = System.currentTimeMillis();
                        while (enabled && (name = (String)it.next()) != null) {
                            yield();
                            FileRecord rec = (FileRecord)file_set.get(name);
                            File f = new File(file_dir, Integer.toHexString(rec.id^0x80000000));
                            if (rec.ts < ts.gc_ts) {
                                f.delete();
                                file_set.remove(name);
                            } else
                                ts_size.add(rec.ts, f.length());
                            
                            if (System.currentTimeMillis() - starttime >= 10) {
                                try { sleep(10); } catch (InterruptedException ex) {}
                                starttime = System.currentTimeMillis();
                            }
                            
                        }
                    } catch (ConcurrentModificationException ex) {}
                    
                    if (!enabled)
                        break;
                    
                    // Compute new cut timestamp
                    ts_size.add(ts.ts, new File(cache_dir, "cache.db").length());
                    ts.size  = ts_size.getSize();
                    ts.gc_ts = Math.max(ts_size.getCutTs(max_cache_size), ts.gc_ts);
                    ts.dirty = true;
                    if (ts.size < max_cache_size) {
                        try {
                            sleep(5000);
                        } catch (InterruptedException ex) {}
                    } else
                        Thread.yield();
                }
                
                catch (IllegalStateException ex) {
                }
                
                catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}