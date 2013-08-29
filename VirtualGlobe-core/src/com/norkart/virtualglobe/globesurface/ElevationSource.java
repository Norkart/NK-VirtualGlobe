//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.cache.ElevationCache;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Properties;
import java.util.Arrays;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;

import javax.swing.JOptionPane;
import com.norkart.geopos.Ellipsoid;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class ElevationSource {
    private BttSurface surface;
    private final int NUM_SERVER_THREADS = 2;
    private final int NUM_CACHE_THREADS = 1;
    private final int THREAD_PRIORITY = Thread.NORM_PRIORITY-1;
    private IntegerArray  replyList = new IntegerArray(3, 30000);
    
    private float hScale = 1000.f;
    private Ellipsoid ellps = null;
    
    private ServerSource serverSource = null;
    private CacheSource  cacheSource  = null;
    
    private ThreadGroup group = new ThreadGroup("ElevationSource-Threads");
    
    public static class ByteArraySizeComp implements Comparator, Serializable {
        public static final ByteArraySizeComp INSTANCE = new ByteArraySizeComp();
        
        public int compare(Object o1, Object o2) {
            byte[] ba1 = (byte[])o1;
            byte[] ba2 = (byte[])o2;
            int off1 = 0;
            int len1 = ba1.length;
            if (len1 == 9) {
                off1 = 1;
                len1 = ba1[0];
            }
            int off2 = 0;
            int len2 = ba2.length;
            if (len2 == 9) {
                off2 = 1;
                len2 = ba2[0];
            }
            if (len1 < len2)
                return -1;
            if (len1 > len2)
                return 1;
            for (int i=0; i<len1; ++i) {
                if ((0xff&ba1[off1+i]) < (0xff&ba2[off2+i]))
                    return -1;
                if ((0xff&ba1[off1+i]) > (0xff&ba2[off2+i]))
                    return 1;
            }
            return 0;
        }
    }
    
    private class QueryKeyComparator implements IntegerArray.Comparator {
        private byte[] code = new byte[9];
        
        public int compare(int ix1, int [] arr1, int ix2, int [] arr2, int rec_size) {
            int node1 = arr1[ix1*rec_size];
            int node2 = arr2[ix2*rec_size];
            
            if (node1 == node2) return 0;
            if (node1 < 0) return  1;
            if (node2 < 0) return -1;
            return Morton.compareCode(surface.int_lonlat[node1*2+0],
                    surface.int_lonlat[node1*2+1],
                    surface.int_lonlat[node2*2+0],
                    surface.int_lonlat[node2*2+1]);
        }
    }
    private QueryKeyComparator queryKeyComparator = new QueryKeyComparator();
    
    private class CacheKeyComparator implements IntegerArray.Comparator {
        private byte[] code = new byte[9];
        
        public int compare(int ix1, int [] arr1, int ix2, int [] arr2, int rec_size) {
            int lon1 = arr1[ix1*rec_size];
            int lat1 = arr1[ix1*rec_size+1];
            int lon2 = arr2[ix2*rec_size];
            int lat2 = arr2[ix2*rec_size+1];
            
            return Morton.compareCode(lon2, lat2, lon1, lat1);
        }
    }
    private CacheKeyComparator cacheKeyComparator = new CacheKeyComparator();
    
    
    private class QueryTsComparator implements IntegerArray.Comparator {
        public int compare(int ix1, int [] arr1, int ix2, int [] arr2, int rec_size) {
            int node1 = arr1[ix1*rec_size];
            int node2 = arr2[ix2*rec_size];
            
            if (node1 == node2) return 0;
            if (node1 < 0) return 1;
            if (node2 < 0) return -1;
            if (surface.status[node1] == surface.status[node2])
                return Morton.compareCode(surface.int_lonlat[node1*2+0],
                        surface.int_lonlat[node1*2+1],
                        surface.int_lonlat[node2*2+0],
                        surface.int_lonlat[node2*2+1]);
            return surface.status[node1] - surface.status[node2];
        }
    }
    private QueryTsComparator queryTsComparator = new QueryTsComparator();
    
    /**
     * query for a model
     */
    public final static class ModelQuery implements Serializable {
        static public final int MODEL4x2 = 1;
        static public final int MODEL4x8 = 2;
        public int modelType = -1;
        public double a = -1.;
        public double f = -1.;
        public float  hScale = 1000.f;
        public int version = 0;
    }
    
    /**
     * Base class for the special elevation sources (URL or local file)
     * Mainly handles the query list
     */
    private class SourceBase {
        protected IntegerArray  queryList = new IntegerArray(1, 30000);
        protected boolean    enabled   = true;
        
        public SourceBase() {
            super();
        }
        
        void close() {
            enabled = false;
            synchronized (queryList) {
                queryList.notifyAll();
            }
        }
        
        /**
         * Add a new query
         * @param rec
         * @return
         */
        boolean addQuery(int value) {
            if (!enabled) return false;
            synchronized (queryList) {
                queryList.set(queryList.size(), 0, value);
                queryList.notify();
            }
            return true;
        }
        
        boolean addQueries(IntegerArray queries, int fromIx, int toIx) {
            if (!enabled) return false;
            synchronized (queryList) {
                for (int i=fromIx; i<toIx; ++i)
                    queryList.set(queryList.size(), 0, queries.get(i, 0));
                queryList.notify();
            }
            return true;
        }
        
        /**
         * Get a list of (maximum maxnum) queries
         * @param queries
         * @param maxnum
         * @return
         */
        protected IntegerArray getQueries(IntegerArray queries, int maxnum) {
            synchronized (queryList) {
                if (!enabled) return null;
                // Find query set and remove from list
                
                if (queries == null)
                    queries = new IntegerArray(1, maxnum);
                else {
                    queries.clear();
                    queries.ensureCapacity(maxnum);
                }
                int i = queryList.size();
                if (i > maxnum)
                    queryList.sort(queryTsComparator);
                // Collections.sort(queryList, queryTsComparator);
                while (--i >= 0 ) {
                    int node = queryList.get(i, 0);
                    queryList.remove(i);
                    if (node >= 0) {
                        queries.set(queries.size(), 0, node);
                        if (--maxnum < 0)
                            break;
                    }
                }
                return queries;
            }
        }
    }
    
    /**
     * Elevation source sub object for handling data from an URL specified server
     */
    private final class ServerSource extends SourceBase {
        private URL url;
        
        // Protocole codes
        private static final int OPCODE_HEADER      = 0x1000;
        private static final int OPCODE_HEADER_VERSION = 0x1001;
        private static final int OPCODE_ELEVATION32 = 0x2000;
        private static final int OPCODE_ELEVATION16 = 0x3000;
        private static final int OPCODE_ELEVATION_BYTE = 0x4000;
        private static final int MODEL4x2           = 0x2042;
        private static final int MODEL4x8           = 0x2048;
        
        private Worker[] worker = new Worker[NUM_SERVER_THREADS];
        
        /**
         * Initialize a server connection, start worker threads
         * @param url
         */
        ServerSource(URL url) {
            super();
            this.url   = url;
            for (int i=0; i< NUM_SERVER_THREADS; ++i) {
                worker[i] = new Worker();
                worker[i].setPriority(THREAD_PRIORITY);
                worker[i].start();
            }
        }
        
        /**
         * Connect to the server, query about the elevation model
         * @return
         */
        ModelQuery getModelQuery() {
            if (!enabled) return null;
            ModelQuery mq = null;
            try {
                URLConnection con = url.openConnection();
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestProperty("Content-Type", "application/octet-stream");
                DataOutputStream out =
                        new DataOutputStream(
                        new BufferedOutputStream(con.getOutputStream()));
                
                // Query for model
                out.writeInt(OPCODE_HEADER_VERSION);
                out.flush();
                out.close();
                
                DataInputStream in  =
                        new DataInputStream(new BufferedInputStream(con.getInputStream()));
                // Read model parameters
                int modelType = in.readInt();
                int ia     = in.readInt();
                int ib     = in.readInt();
                int hScale = in.readInt();
                int version = in.readInt();
                in.close();
                
                double a = (double)ia / 100.;
                double f = ((double)ia - (double)ib) / (double)ia;
                mq = new ModelQuery();
                mq.a = a;
                mq.f = f;
                mq.hScale = hScale/1000.f;
                mq.version = version;
                switch (modelType) {
                    case MODEL4x2:
                        mq.modelType = ModelQuery.MODEL4x2; break;
                    case MODEL4x8:
                        mq.modelType = ModelQuery.MODEL4x8; break;
                    default:
                        mq = null;
                }
            } catch (ConnectException ex) {
                System.err.println("Elevation server connection failed");
                enabled = false;
                mq = null;
            } catch (NoRouteToHostException ex) {
                System.err.println("Elevation server connection failed");
                enabled = false;
                mq = null;
            } catch (UnknownHostException ex) {
                System.err.println("Elevation server connection failed");
                enabled = false;
                mq = null;
            } catch (SocketTimeoutException ex) {
                System.err.println("Elevation server connection failed");
                enabled = false;
                mq = null;
            } catch (IOException ex) {
                System.err.println(ex);
                mq = null;
            }
            if (mq != null) {
                System.out.println("Opened connection to elevation server: " + url);
            }
            return mq;
        }
        
        
        
        
        /**
         * A server elevation source worker
         */
        private final class Worker extends Thread {
            private int [] rec = new int[4];
            private final int buf_size = 1<<15;
            // IO buffers
            private ByteBuffer ibuf = ByteBuffer.allocateDirect(buf_size);
            private ByteBuffer obuf = ByteBuffer.allocateDirect(buf_size);
            Worker() {
                super(group, "URLSource");
            }
            
            public void run() {
                byte[] code = new byte[9];
                int queryNr = 0;
                IntegerArray queries = null;
                
                while (enabled) {
                    try {
                        synchronized (queryList) {
                            while (enabled && queryList.isEmpty()) {
                                try { queryList.wait(); } catch (InterruptedException ie) { }
                            }
                        }
                        queries = getQueries(queries, (obuf.capacity()-12)/9);
                        if (queries == null || queries.isEmpty()) continue;
                        queries.sort(queryKeyComparator);
                        
                        // sending server query
                        int querySz = queries.size();
                        int opCode = OPCODE_ELEVATION_BYTE;
                        obuf.clear();
                        obuf.putInt(opCode);
                        obuf.putInt(++queryNr);
                        obuf.putInt(querySz);
                        
                        // Query data
                        for (int i=0; enabled && i<queries.size(); ++i) {
                            int node = queries.get(i, 0);
                            Morton.code(surface.int_lonlat[2*node], surface.int_lonlat[2*node+1], code);
                            obuf.put(code, 0, code[0]+1);
                        }
                        obuf.flip();
                        
                        boolean okReply = false;
                        int numTries = 0;
                        do {
                            try {
                                // System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
                                // System.setProperty("sun.net.client.defaultReadTimeout", "15000");
                                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                                con.setDoOutput(true);
                                con.setUseCaches(false);
                                con.setRequestProperty("Content-Type", "application/octet-stream");
                                
                                // Write
                                obuf.rewind();
                                {
                                    OutputStream out = con.getOutputStream();
                                    WritableByteChannel oc = Channels.newChannel(out);
                                    do {
                                        oc.write(obuf);
                                    } while (obuf.position() < obuf.limit());
                                    oc.close();
                                    out.flush();
                                    out.close();
                                }
                                
                                // Get the reply
                                if (con.getResponseCode() >= 400)
                                    continue;
                                // throw new ProtocolException("Bad responce code : " + con.getResponseMessage());
                                ibuf.clear();
                                {
                                    InputStream in  = con.getInputStream();
                                    ReadableByteChannel ic = Channels.newChannel(in);
                                    while (ic.read(ibuf) >= 0);
                                    ic.close();
                                    in.close();
                                }
                                if (ibuf.position() != 12+8*querySz)
                                    continue;
                                ibuf.flip();
                            } catch (NoRouteToHostException ex) {
                                System.err.println("Unable to connect to elevation server, no route to: " + url.toString());
                                enabled = false;
                                break;
                            } catch (UnknownHostException ex) {
                                System.err.println("Unable to connect to elevation server, unknown host: " + url.toString());
                                enabled = false;
                                break;
                            } catch (ConnectException ex) {
                                if (++numTries > 5 && enabled) {
                                    System.err.println("5 failed attempts in elevation server, disconnecting");
                                    enabled = false;
                                    break;
                                }
                                continue;
                            } catch (SocketTimeoutException ex) {
                                if (++numTries > 5 && enabled) {
                                    System.err.println("5 failed attempts in elevation server, disconnecting");
                                    enabled = false;
                                    break;
                                }
                                continue;
                            } catch (IOException ex) {
                                System.err.print("Server source : ");
                                System.err.println(ex);
                                continue;
                            }
                            okReply = true;
                        } while (enabled && !okReply);
                        if (!enabled) break;
                        if (numTries > 0)
                            System.err.println("Attempts to connect : " + Integer.toString(numTries));
                        
                        int opc     = ibuf.getInt();
                        int replyNr = ibuf.getInt();
                        int replySz = ibuf.getInt();
                        
                        if (opc != opCode)
                            System.err.println("Bad opcode in reply");
                        if (querySz != replySz)
                            System.err.println("Different size query - reply");
                        if (queryNr != replyNr)
                            System.err.println("Error in query sequence");
                        
                        // Read heights and deviations
                        // q_it = queries.iterator();
                        // while (enabled && q_it.hasNext()) {
                        // QueryRecord rec = (QueryRecord)q_it.next();
                        
                        for (int i=0; enabled && i<queries.size(); ++i) {
                            // QueryRecord rec = new QueryRecord();
                            int node = queries.get(i, 0);
                            int h    = ibuf.getInt();
                            int dh   = ibuf.getInt();
                            rec[0] = node;
                            rec[1] = h;
                            rec[2] = dh;
                            synchronized (replyList) {
                                // Storing replies
                                replyList.add(rec);
                            }
                            if (cacheSource != null) {
                                rec[0] = surface.int_lonlat[node*2];
                                rec[1] = surface.int_lonlat[node*2+1];
                                rec[2] = h;
                                rec[3] = dh;
                                synchronized (cacheSource.toCache) {
                                    cacheSource.toCache.add(rec);
                                }
                            }
                        }
                        if (cacheSource != null) {
                            synchronized (cacheSource.queryList) {
                                cacheSource.queryList.notify();
                            }
                            
                        }
                        queries.clear();
                        // Sucess!
                        // long endtime = System.currentTimeMillis();
                        // System.out.println(" tot : " + Long.toString(endtime-stattime));
                        // sum_time += endtime-stattime;
                        // System.out.println("points pr. millis : " + ((float)sum_num/sum_time));
                        yield();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    
    /**
     * Elevation source sub object for handling of the local cache
     */
    private final class CacheSource extends SourceBase {
        private ElevationCache cache;
        private Worker[] worker = new Worker[NUM_CACHE_THREADS];
        // private ModelQuery model_query;
        
        private IntegerArray toCache = new IntegerArray(4, 30000);
        
        /**
         * Open the local elevation cache
         * @param cachefile
         */
        CacheSource(ElevationCache cache)  {
            super();
            this.cache = cache;
            
            // open database and setup an object cache
            for (int i=0; i< NUM_CACHE_THREADS; ++i) {
                worker[i] = new Worker();
                worker[i].setPriority(THREAD_PRIORITY);
                worker[i].start();
            }
        }
        
        
        /**
         * The cache source worker thread
         */
        private final class Worker extends Thread {
            // int [] node_arr = new int[1];
            int [] rec      = new int[4];
            final static int MAX_QUERY_SIZE = 2000;
            final static long MAX_QUERY_TIME = 2000;
            
            Worker() {
                super(group, "CacheSource");
            }
            
            /**
             * The source workloop
             */
            public void run() {
                byte [] key = new byte[9];
                IntegerArray queries = null;
                
                int comcnt = 0;
                int ycnt = 0;
                while (enabled) {
                    
                    try {
                        synchronized (queryList) {
                            while (enabled && queryList.isEmpty() && toCache.isEmpty()) {
                                try { queryList.wait(); } catch (InterruptedException ie) { }
                            }
                        }
                        long query_start_time = System.currentTimeMillis();
                        queries = getQueries(queries, MAX_QUERY_SIZE);
                        if (!enabled) break;
                        if (queries == null) continue;
                        queries.sort(queryKeyComparator);
                        // Collections.sort(queries, queryKeyComparator);
                        synchronized (replyList) {
                            replyList.ensureCapacity(replyList.size()+queries.size());
                        }
                        
                        // boolean interrupted = false;
                        try {
                            if (cache.lock()) {
                                
                                // Fetch elevations from cache
                                // System.out.println("Queries: " + queries.size());
                                
                                for (int i=0;enabled && i<queries.size(); ++i) {
                                    if (!enabled) break;
                                    if (i > MAX_QUERY_SIZE/20 && i%50 == 0 && System.currentTimeMillis() - query_start_time > MAX_QUERY_TIME) {
                                        // queries.removeRange(0, i);
                                        addQueries(queries, i, queries.size());
                                        // interrupted = true;
                                        break;
                                    }
                                    int node = queries.get(i, 0);
                                    Morton.code(surface.int_lonlat[2*node], surface.int_lonlat[2*node+1], key);
                                    ElevationCache.Record db_rec = cache.get(key);
                                    if (db_rec != null) {
                                        rec[0] = node;
                                        rec[1] = db_rec.h;
                                        rec[2] = db_rec.dh;
                                        synchronized (replyList) {
                                            // Storing replies
                                            replyList.add(rec);
                                        }
                                    } else if (serverSource != null) {
                                        // Add to url queries
                                        serverSource.addQuery(node);
                                    } else {
                                        rec[0] = node;
                                        rec[1] = Integer.MAX_VALUE;
                                        rec[2] = 0;
                                        // rec.key    = null;
                                        synchronized (replyList) {
                                            replyList.add(rec);
                                        }
                                    }
                                }
                            }
                        } finally {
                            cache.unlock();
                        }
                        // System.out.print("Num queries: " + i + " time: " + (System.currentTimeMillis()-query_start_time));
                        
                        if (!enabled) break;
                        
                        // To cache
                        // long to_cache_time = System.currentTimeMillis();
                        if (enabled && System.currentTimeMillis() - query_start_time < MAX_QUERY_TIME && queries.size() < MAX_QUERY_SIZE) {
                            synchronized (toCache) {
                                // long start_cache_time = System.currentTimeMillis();
                                toCache.sort(cacheKeyComparator);
                                int num_left = MAX_QUERY_SIZE-queries.size();
                                // int num_to_cache = 0;
                                // System.out.print(" num tocache: " + Math.min(num_left, toCache.size()) + " time: ");
                                try {
                                    
                                    if (cache.lock()) {
                                        for (int i=toCache.size(); enabled && --i >= 0 && num_left-- > 0; ) {
                                            if (!enabled) break;
                                            toCache.get(i, rec);
                                            toCache.remove(i);
                                            byte [] k = Morton.code(rec[0], rec[1]);
                                            cache.set(k, rec[2], rec[3]);
                                            // ++num_to_cache;
                                            if (i%50 == 0 && System.currentTimeMillis() - query_start_time > MAX_QUERY_TIME)
                                                break;
                                        }
                                    }
                                } finally {
                                    cache.unlock();
                                }
                                // System.out.print(" Num to cache: "+ num_to_cache+ " time :" + (System.currentTimeMillis()-to_cache_time));
                            }
                        }
                        if (!enabled) break;
                        
                        // long start_commit_time = System.currentTimeMillis();
                        queries.clear();
                        if (cache.getCacheManager().isOpen()) {
                            cache.getCacheManager().newTs();
                            cache.getCacheManager().commit();
                        }
                        // System.out.println(" Commit: " + (System.currentTimeMillis()-start_commit_time));
                        yield();
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Create and initialize an elevation source, consisting of possibly both
     * a server source and a cache source. The elevation source accepts queries 
     * for elevations of nodes in the surface triangulation and collects replies from 
     * the elevation server and elevation cache.
     * @param cache_mgr The cache manager
     * @param serverURL An URL to the server
     */
    public ElevationSource(URL serverURL, CacheManager cache_mgr) {
        if (serverURL == null)
            return;
        serverSource = new ServerSource(serverURL);
        setCacheManager(cache_mgr);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    close();
                } catch(Exception ex) {}
            }
        });
    }
    
    /**
     * Set the surface
     * @param surface
     */
    void setElevationSurface(BttSurface surface) {
        this.surface = surface;
    }
    
    /**
     * Do a model query and create a globe ellipsoid
     * @return
     */
    
    Ellipsoid getEllipsoid() {
        return ellps;
    }
    
    /**
     * Set a (new) cache database
     * @param cache_mgr The cache manager
     */
    public void setCacheManager(CacheManager cache_mgr) {
        ModelQuery cache_mq = null, server_mq = serverSource.getModelQuery();
        if (cacheSource != null)
            cacheSource.close();
        cacheSource = null;
        
        if (cache_mgr != null) {
            try {
                ElevationCache cache = cache_mgr.getElevationCache(serverSource.url.toString(), server_mq);
                if (cache != null) {
                    cacheSource = new CacheSource(cache);
                    cache_mq = cache.getModelQuery();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (server_mq != null) {
            hScale = server_mq.hScale;
            ellps = new Ellipsoid(server_mq.a, server_mq.f);
        } else if (cache_mq != null) {
            hScale = cache_mq.hScale;
            ellps = new Ellipsoid(cache_mq.a, cache_mq.f);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Unable to open neither cache nor server connection for elevations.",
                    "No data source",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Process the replies from both server and cache source and integrate
     * them into the surface model
     */
    // private IntegerArray process_list = new IntegerArray(3);
    public void processReplies() {
        synchronized (replyList) {
            for (int i = 0; i < replyList.size(); ++i) {
                int node = replyList.get(i, 0);
                int h    = replyList.get(i, 1);
                int dh   = replyList.get(i, 2);
                if (node < 0);
                else if (h != Integer.MAX_VALUE)
                    surface.setNodeValues(node, h, dh);
                else
                    surface.setNodeValues(node, Integer.MIN_VALUE+1, dh);
            }
            replyList.clear();
        }
    }
    
    
    
    /**
     * Add a query for a surface node
     * @param node The node index
     */
    public void addQuery(int node) {
        if (cacheSource != null && cacheSource.addQuery(node));
        else if (serverSource != null && serverSource.addQuery(node));
    }
    
    
    /**
     * Remove a set of nodes from the query list
     * @param gc_nodes A list of node indices
     * @param gc_nodes_num The size of the list
     * @return The number of removed queries
     */
    public int removeQueries(int [] gc_nodes, int gc_nodes_num) {
        int [] gc_nodes_sorted = new int [gc_nodes_num];
        System.arraycopy(gc_nodes, 0, gc_nodes_sorted, 0, gc_nodes_num);
        Arrays.sort(gc_nodes_sorted);
        int num_removed = 0;
        if (cacheSource != null) {
            synchronized (cacheSource.queryList) {
                for (int i=0; i<cacheSource.queryList.size(); ++i) {
                    int node = cacheSource.queryList.get(i, 0);
                    if (Arrays.binarySearch(gc_nodes_sorted, node) >= 0) {
                        gc_nodes[num_removed++] = node;
                        cacheSource.queryList.set(i, 0, -1);
                    }
                }
            }
        }
        if (serverSource != null) {
            synchronized (serverSource.queryList) {
                for (int i=0; i<serverSource.queryList.size(); ++i) {
                    int node = serverSource.queryList.get(i, 0);
                    if (Arrays.binarySearch(gc_nodes_sorted, node) >= 0) {
                        gc_nodes[num_removed++] = node;
                        serverSource.queryList.set(i, 0, -1);
                    }
                }
            }
        }
        
        synchronized (replyList) {
            for (int i=0; i<replyList.size(); ++i) {
                int node = replyList.get(i, 0);
                if (Arrays.binarySearch(gc_nodes_sorted, node) >= 0) {
                    gc_nodes[num_removed++] = node;
                    replyList.set(i, 0, -1);
                }
            }
        }
        return num_removed;
    }
    
    /**
     * Close the elevation source
     * @throws java.io.IOException Throws an exception on IO errors
     */
    public void close() throws java.io.IOException {
        if (serverSource != null)
            serverSource.close();
        if (cacheSource != null)
            cacheSource.close();
    }
}