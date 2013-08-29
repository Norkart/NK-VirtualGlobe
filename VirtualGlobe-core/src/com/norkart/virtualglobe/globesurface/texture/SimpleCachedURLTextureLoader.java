//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------

package com.norkart.virtualglobe.globesurface.texture;

import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.globesurface.Texture2D;
import com.norkart.virtualglobe.globesurface.TextureLoader;
import com.norkart.virtualglobe.globesurface.TextureTile;
import com.norkart.virtualglobe.util.ApplicationSettings;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.lang.ref.WeakReference;
import java.io.*;
import java.net.*;
import java.awt.image.BufferedImage;
import java.nio.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class SimpleCachedURLTextureLoader
        implements TextureLoader {
    private boolean enabled = true;
    
    private   ThreadGroup   tg;
    final private int thread_priority = Thread.NORM_PRIORITY-2;
    final private int num_cache_threads = 10;
    final private int num_server_threads = 3;
    private String thread_name;
    
    protected boolean use_compressed = ApplicationSettings.getApplicationSettings().getUseCompressedTexture();
    // protected String suffix;
    protected boolean server_enabled = true;
    protected LoadQueue cacheQueue = new LoadQueue();
    protected LoadQueue serverQueue = new LoadQueue();
    
    // private static HashMap cacheDirMap = new HashMap();
    
    protected CacheManager cache_mgr;
    
    public SimpleCachedURLTextureLoader(String thread_name, CacheManager cache_mgr) {
        // this.suffix = suffix;
        this.cache_mgr = cache_mgr;
        
        this.thread_name = thread_name;
        tg = new ThreadGroup(thread_name);
    }
    
    public void setCacheManager(CacheManager cache_mgr) {
        this.cache_mgr = cache_mgr;
    }
    
    public void start() {
    /*
    imageCopier = new ImageCopier();
    imageCopier.setDaemon(true);
    imageCopier.setPriority(thread_priority);
    imageCopier.start();
     */
        
        for (int i=0; i<num_cache_threads; ++i) {
      /*
      ImageReader imageReader = null;
      if (!use_compressed) {
        Iterator ir_i = ImageIO.getImageReadersBySuffix("jpg");
        if (ir_i.hasNext())
          imageReader = (ImageReader)ir_i.next();
      }
       */
            Thread l = new CacheLoader(tg, thread_name+"-cache-"+Integer.toString(i));
            l.setDaemon(true);
            l.setPriority(thread_priority);
            l.start();
        }
        for (int i=0; i<num_server_threads; ++i) {
      /*
      ImageReader imageReader = null;
      if (!use_compressed) {
        Iterator ir_i = ImageIO.getImageReadersBySuffix("jpg");
        if (ir_i.hasNext())
          imageReader = (ImageReader)ir_i.next();
      }
       */
            Thread l = new ServerLoader(tg, thread_name+"-server-"+Integer.toString(i));
            l.setDaemon(true);
            l.setPriority(thread_priority);
            l.start();
        }
    }
    
    protected class LoadQueue {
        private   ArrayList    queryList     = new ArrayList();
        
        void add(TextureTile tile) {
            if (tile.waitForLoading()) {
                synchronized (this) {
                    queryList.add(new WeakReference(tile));
                    notifyAll();
                }
            }
        }
        
        synchronized boolean remove(TextureTile tile) {
            Iterator it = queryList.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                Object o = ref.get();
                if (o == null) 
                    it.remove();
                else if (o == tile) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
        
        synchronized TextureTile waitForQuery() {
            while (enabled && queryList.isEmpty()) {
                try { wait(); } catch (InterruptedException ie) { }
            }
            if (!enabled) return null;
            
            WeakReference tile_ref = null;
            TextureTile tile = null;
            Iterator it = queryList.iterator();
            while (enabled && it.hasNext()) {
                WeakReference w = (WeakReference)it.next();
                TextureTile t = (TextureTile)w.get();
                if (t == null) {
                    it.remove();
                    continue;
                }
                if (tile == null ||
                        t.getTs() >  tile.getTs() ||
                        (t.getTs() == tile.getTs() &&
                        t.getPri() < tile.getPri())) {
                                    /*
                                    t.getTs() == tile.getTs() && t.getPri() < tile.getPri() ||
                                    t.getTs() == tile.getTs() && t.getPri() == tile.getPri() && t.getPixelSize() > tile.getPixelSize()) {
                                     */
                    tile_ref = w;
                    tile = t;
                }
                t = null;
            }
            if (tile_ref != null)
                queryList.remove(tile_ref);

            return tile;
        }
    }
    
    // -------------------------------
    // Texture loader methods
    // -------------------------------
    
    /**
     * Initialte loading of a texture for this tile
     * @param tile
     */
    public void loadTextureTile(TextureTile tile) {
        
        if (cache_mgr == null)
            serverQueue.add(tile);
        else
            cacheQueue.add(tile);
            /*
            synchronized (queryList) {
                queryList.add(new WeakReference(tile));
                queryList.notifyAll();
            }
             */
        
        tile = null;
    }
    
    public boolean stopLoadingTextureTile(TextureTile tile) {
        /*
        synchronized (queryList) {
            Iterator it = queryList.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                Object o = ref.get();
                if (o == null) {
                    it.remove();
                    continue;
                }
                if (o == tile) {
                    it.remove();
                    break;
                }
            }
        }
         */
        return cacheQueue.remove(tile) || serverQueue.remove(tile);
    }
    
    //------------------------------------------------------
    // Abstract methods for overloading in derived classes
    //------------------------------------------------------
    protected abstract String getTilesetName(TextureTile tile);
    protected abstract InputStream openServerTexture(TextureTile tile) throws IOException;
    
    
    
    //-----------------------------------------
    // Local methods
    //-----------------------------------------
/*
  static public File getCacheDir(String request_str, File cacheRoot) {
    File cachedir = null;
    synchronized (cacheDirMap) {
      Object o = cacheDirMap.get(request_str);
 
      if (o != null)
        cachedir = (File)o;
      else {
        int hash = request_str.hashCode();
 
        for (;;) {
          cachedir = new File(cacheRoot, "Textures-"+Integer.toHexString(hash++));
          File namefile = new File(cachedir, "name.txt");
          if (namefile.canRead()) {
            try {
              BufferedReader in
                  = new BufferedReader(new InputStreamReader(new FileInputStream(namefile)));
              String compare_str = "", str;
              while ((str = in.readLine()) != null)
                compare_str += (str + "\n");
              in.close();
              if (request_str.equals(compare_str))
                break;
              continue;
            }
            catch (IOException ex) {}
          }
          cachedir.mkdirs();
          try {
            PrintStream out = new PrintStream(new FileOutputStream(namefile));
            out.print(request_str);
            out.close();
            cacheDirMap.put(request_str, cachedir);
            break;
          }
          catch (IOException ex) {
            System.err.println(ex.getMessage());
          }
        }
      }
    }
    return cachedir;
  }
 */
    public void close() {
        synchronized (cacheQueue) {
            enabled = false;
            cacheQueue.notifyAll();
        }
        synchronized (serverQueue) {
            enabled = false;
            serverQueue.notifyAll();
        }
        Thread[] threads = new Thread[tg.activeCount()+5];
        int num_threads;
        while (threads.length == (num_threads = tg.enumerate(threads)))
            threads = new Thread[threads.length*2];
        try {
            for (int i=0; i < num_threads; ++i)
                threads[i].join();
            // imageCopier.join();
        } catch (InterruptedException ex) {}
    }
/*
  protected BufferedImage readImage(ImageReader reader) throws java.io.IOException {
    BufferedImage image = reader.read(0);
    reader.reset();
    return image;
  }
 */
    
    protected File getImageFile(TextureTile tile) {
        if (cache_mgr != null) {
            String tileset_name  = getTilesetName(tile);
            if (tileset_name != null) {
                try {
                    return cache_mgr.getTileCacheFile(tileset_name, tile.getPosCode(), use_compressed?"dds":"jpg");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
    
    protected void loadFile(TextureTile tile) {
        if (!enabled) return;
        // Create filename
        File imagefile = getImageFile(tile);
        
        if (!enabled) return;
        if (imagefile != null && imagefile.canRead() && tile.getFileModTime() <= 0) {
            // load from file cache
            
            try {
                ImageBuffer image_buf = null;
                if (use_compressed)
                    image_buf = new DDSImageBuffer(imagefile);
                else
                    image_buf = new BufferedImageBuffer(imagefile);
                
                if (image_buf != null) {
                    synchronized (tile) {
                        Texture2D t = Texture2D.createTexture(image_buf);
                        if (server_enabled) {
                            tile.setTexture(t, imagefile.lastModified());
                            serverQueue.add(tile);
                        }
                        else
                            tile.setTexture(t, 0);
                    }
                    return;
                }
            } catch (FileNotFoundException ex) {
                ;
            } catch (OutOfMemoryError ex) {
                // Texture2D.adjustMaxTexMemory();
                System.err.println("Out of memory in loading of texture file: " + ex);
                ex.printStackTrace();
                System.err.println("Buffers : " + ImageBuffer.getBufferMemory());
                System.err.println("Textures : " + Texture2D.getTexMemory());
            } catch (Exception ex) {
                System.err.print("Error in loading of texture file : ");
                System.err.println(ex);
                System.err.print("Cache file deleted : ");
                System.err.println(imagefile.getAbsolutePath());
                imagefile.delete();
            }
        }
        tile.stopLoading();
        if (server_enabled)
            serverQueue.add(tile);
    }
    
    protected void loadServer(TextureTile tile) {
        // Load from server if server image is newer than this
        File imagefile = getImageFile(tile);
        File tmpfile = null;
        if (imagefile != null)
            tmpfile = new File(imagefile.getPath() + ".tmp");
        int num_timeouts = 0;
        int numerr = 0;
        while (enabled && server_enabled) {
            InputStream in = null;
            
            try {
                in = openServerTexture(tile);
                if (in == null) 
                    return;
                
                ImageBuffer image_buf = null;
                if (use_compressed) {
                    in = new GZIPInputStream(in);
                    if (tmpfile == null)
                        image_buf = new DDSImageBuffer(in);
                    else
                        image_buf = new DDSImageBuffer(in, tmpfile);
                } else {
                    if (tmpfile == null)
                        image_buf = new BufferedImageBuffer(in);
                    else
                        image_buf = new BufferedImageBuffer(in, tmpfile);
                }
                /*
                // Pause for å simulere slapt nettverk
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException ex) {}
                */
               
                tile.setTexture(Texture2D.createTexture(image_buf), 0);
                if (tmpfile != null && tmpfile.exists()) {
                    if (imagefile.exists()) imagefile.delete();
                    tmpfile.renameTo(imagefile);
                }
                
                return;
            } catch (ConnectException ex) {
                if (++num_timeouts >= 5) {
                    System.err.print("Lost connection to texture host");
                    server_enabled = false;
                }
            } catch (SocketTimeoutException ex) {
                if (++num_timeouts >= 5) {
                    System.err.print("Timeout, Probably lost connection to texture host");
                    server_enabled = false;
                }
            } catch (NoRouteToHostException ex) {
                System.err.print("No route to texture host");
                server_enabled = false;
            } catch (UnknownHostException ex) {
                System.err.print("Unknown texture host");
                server_enabled = false;
            } catch (IOException ex) {
                if (++numerr >= 2) {
                    System.err.print("Error in reading of texture URL : ");
                    ex.printStackTrace();
                    tile.abortLoading();
                    return;
                }
            } catch (OutOfMemoryError ex) {
                // Texture2D.adjustMaxTexMemory();
                System.err.println("Out of memory in loading of texture URL: " + ex);
                ex.printStackTrace();
                System.err.println("Buffers : " + ImageBuffer.getBufferMemory());
                System.err.println("Textures : " + Texture2D.getTexMemory());
                return;
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    
    //--------------------------------------------------
    // The Loader thread class
    //--------------------------------------------------
    private class CacheLoader extends Thread {
        public CacheLoader(ThreadGroup tg, String name) {
            super(tg, name);
        }
        
        public void run() {
            while (enabled) {
                try {
                    TextureTile tile = cacheQueue.waitForQuery();
                    if (tile == null)
                        continue;
                    if (!enabled) break;
                    tile.startLoading();
                    loadFile(tile);
                    tile = null;
                    yield();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private class ServerLoader extends Thread {
        public ServerLoader(ThreadGroup tg, String name) {
            super(tg, name);
        }
        
        public void run() {
            while (enabled) {
                try {
                    TextureTile tile = serverQueue.waitForQuery();
                    if (tile == null)
                        continue;
                    if (!enabled) break;
                    tile.startLoading();
                    if (!enabled) break;
                    loadServer(tile);
                    tile = null;
                    yield();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}


