//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;

import java.net.URL;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;
import org.j3d.renderer.aviatrix3d.texture.*;

import org.xj3d.loaders.ogl.*;

// import org.web3d.ogl.loaders.Web3DLoader;

import org.ietf.uri.URI;

import com.norkart.virtualglobe.cache.CacheManager;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Xj3DLoader implements ApplicationUpdateObserver {
  private CacheManager cache_mgr;
  private boolean enabled = true;

  // List of loader requests
  private ArrayList requestList = new ArrayList();

  // Caches
  private HashMap loadedModels = new HashMap();
  private HashMap textureCache = new HashMap();

  private static TextureCreateUtils textureUtils = new TextureCreateUtils();
  /** The runtime components */
  private ArrayList runtimeComponents = new ArrayList();

  // Worker thread stuff
  private ThreadGroup tg;
  private int num_threads = 1;
  private int thread_priority = Thread.NORM_PRIORITY-2;

  public Xj3DLoader(CacheManager cache_mgr) {
    this.cache_mgr = cache_mgr;
    // Create and start threads (running more than one thead is currenty untested)
    String thread_name = "Xj3D-Loader";
    tg = new ThreadGroup(thread_name);
    for (int i=0; i<num_threads; ++i) {
      Worker w = new Worker(tg, thread_name+"-"+Integer.toString(i));
      w.setDaemon(true);
      w.setPriority(thread_priority);
      w.start();
    }
  }

  // The request record, may be overloaded for spesific requests
  static public class Xj3DRequest {
    protected URL   url;
    protected AVModel model = null;

    public Xj3DRequest(URL url) {
      this.url = url;
    }

    public void setModel(AVModel model) {
      this.model = model;
    }
  }

  // Fetch a model frm cache
  private AVModel getLoadedModel(URL url) {
    synchronized (loadedModels) {
      SoftReference ref = (SoftReference)loadedModels.get(url);
      if (ref != null) {
        AVModel model = (AVModel)ref.get();
        if (model == null)
          loadedModels.remove(url);
        return model;
      }
    }
    return null;
  }

  // Add a request from the request list
  public void requestLoad(Xj3DRequest xr) {
    AVModel model = getLoadedModel(xr.url);
    if (model != null) {
      xr.setModel(model);
      return;
    }
    synchronized (requestList) {
      requestList.add(new WeakReference(xr));
      requestList.notifyAll();
    }
  }

  /* ApplicationUpdateObeerver methods */
  public void appShutdown() {
  }


  public void updateSceneGraph() {
    synchronized (runtimeComponents) {
      Iterator it = runtimeComponents.iterator();
      while (it.hasNext()) {
        WeakReference wr = (WeakReference)it.next();
        AVRuntimeComponent rc = (AVRuntimeComponent)wr.get();
        if (rc == null)
          it.remove();
        else
          rc.executeModelBehavior();
      }
    }
  }

/*
  static private class MyWeb3DLoader extends Web3DLoader {

    void clearInternals() {
      this.parsedScene = null;
      URI.setContentHandlerFactory(null);
    }
  }
*/

  // The real workhorse of this class...
  private class Worker extends Thread {
    BaseLoader loader = new Web3DLoader();

    Worker(ThreadGroup gt, String name) {
      super(tg, name);
      loader.setLoadFlags(Web3DLoader.GEOMETRY);
    }

    public void run() {
      while (enabled) {
        // Wait for a request
        Xj3DRequest xr = null;
        synchronized (requestList) {
          while (enabled  && requestList.isEmpty()) {
            try { requestList.wait(); }
            catch (InterruptedException ex) {}
          }
          while (!requestList.isEmpty()) {
            WeakReference ref = (WeakReference)requestList.remove(0);
            Object o = ref.get();
            if (o != null && o instanceof Xj3DRequest) {
              xr = (Xj3DRequest)o;
              break;
            }
          }
        }
        if (xr == null)
          continue;

        // Check for model in cache
        AVModel model = getLoadedModel(xr.url);
        if (model != null) {
          xr.setModel(model);
          continue;
        }

        // Nothing in cache, do the loading
        try {
          // Load
          /*
          InputStream in;
          if (cache_mgr != null)
            in = cache_mgr.getInputStream(xr.url);
          else
            in = xr.url.openStream();

          if (in != null)
            in = new BufferedInputStream(in);

          model = loader.load(in);
         */
          
          System.out.println("Loading: " + xr.url);
          model = loader.load(xr.url);
          
          // Add to cache
          synchronized (loadedModels) {
            loadedModels.put(xr.url, new SoftReference(model));
          }

          // Now go off and load externals
          Map externals = model.getExternallyDefinedFiles();
          if(externals.size() != 0) {
            // System.out.println("Have " + externals.size() + " files to load");
            Set entries = externals.entrySet();
            Iterator itr = entries.iterator();

            while(itr.hasNext()) {
              Map.Entry e = (Map.Entry)itr.next();

              String tex_str = null;
              Object obj = e.getValue();

              if((obj instanceof String)) {
                tex_str = (String) obj;
              } else if (obj instanceof String[]) {
                String[] sobj = (String[]) obj;

                // Only handle one url right now
                if(sobj.length > 0)
                  tex_str = sobj[0];
              }

              if (tex_str == null)
                continue;

              Object k = e.getKey();
              if (k instanceof Texture2D) {
                Texture2D texture = (Texture2D)k;
                URL tex_url = new URL(xr.url, tex_str);

                TextureComponent[] tex_comp = {
                  loadImage(tex_url)
                };

                if(tex_comp[0] == null)
                  continue;

                int format = Texture.FORMAT_RGB;
                switch(tex_comp[0].getFormat(0))
                {
                  case TextureComponent.FORMAT_RGBA:
                    format = Texture.FORMAT_RGBA;
                    break;

                  case TextureComponent.FORMAT_INTENSITY_ALPHA:
                    format = Texture.FORMAT_INTENSITY_ALPHA;
                    break;

                  case TextureComponent.FORMAT_SINGLE_COMPONENT:
                    format = Texture.FORMAT_INTENSITY;
                    break;
                }

                texture.setSources(Texture.MODE_BASE_LEVEL,
                                   format, tex_comp, 1);
              }
            }
          }

          // Attach runtime components
          List rtComps = model.getRuntimeComponents();
          if (!rtComps.isEmpty()) {
            Iterator it = rtComps.iterator();
            synchronized (runtimeComponents) {
              while (it.hasNext())
                runtimeComponents.add(new WeakReference(it.next()));
            }
          }
          xr.setModel(model);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        // loader.clearInternals();
      }
    }
  }

  //---------------------------------------------------------------
  // Local methods
  //---------------------------------------------------------------

  /**
   * Load a single image
   */
  private TextureComponent loadImage(URL url)
  {
    TextureComponent comp = null;
    try {
      String url_str = url.toExternalForm();
      // Check cache
      WeakReference ref = (WeakReference)textureCache.get(url_str);
      if (ref != null) {
          comp = (TextureComponent)ref.get();
          if (comp != null)
              return comp;
          else
              textureCache.remove(url_str);
      }

      // System.out.println("Loading external file: " + url);
      // Load image
      BufferedImage img = /*cache_mgr!= null? ImageIO.read(cache_mgr.getInputStream(url)):*/ ImageIO.read(url);
      if(img != null) {
        // Create texture component
        comp = textureUtils.create2DTextureComponent(img);
        // Add to cache
        textureCache.put(url_str, new WeakReference(comp));
      }
    }
    catch(java.io.IOException ioe)
    {
      System.err.println("Error reading image: " + ioe);
    }

    return comp;
  }

}
