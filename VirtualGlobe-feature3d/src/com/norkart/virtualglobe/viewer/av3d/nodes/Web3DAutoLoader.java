//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * Web3DAutoLoader.java
 *
 * Created on 4. september 2006, 10:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer.av3d.nodes;

import java.net.URL;

import java.util.*;

import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import java.net.URL;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.vecmath.*;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;
import org.j3d.renderer.aviatrix3d.texture.*;
import org.j3d.renderer.aviatrix3d.loader.discreet.MaxLoader;

import org.web3d.browser.BrowserCore;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;

import org.xj3d.loaders.ogl.Web3DLoader;

import org.ietf.uri.URI;
import org.ietf.uri.ContentHandlerFactory;

import com.norkart.virtualglobe.cache.CacheManager;
import javax.media.opengl.*;

// import org.xj3d.core.loading.WorldLoaderManager;
// import org.web3d.browser.BrowserCore;

/**
 *
 * @author runaas
 */
public class Web3DAutoLoader {
    protected boolean   enabled       = true;
    protected ArrayList<Web3DNodeLoader>                       model_queue   = new ArrayList();
    protected ArrayList<TextureRec>                            texture_queue = new ArrayList();
    protected HashMap<String, WeakReference<Node>>             loaded_models = new HashMap();
    protected HashMap<String, WeakReference<TextureComponent>> texture_cache = new HashMap();
    protected ConcurrentLinkedQueue<WeakReference<AVRuntimeComponent>> runtime_components = new ConcurrentLinkedQueue();
    protected ConcurrentLinkedQueue<TextureRec>                        update_textures = new ConcurrentLinkedQueue();
    
    protected int ts = Integer.MIN_VALUE;
    
    protected CacheManager cache_manager = null;
    
    protected AppearanceOptimizer optimizer = new AppearanceOptimizer();
    
    static final double LOG_2 = Math.log(2);
    
    protected class Web3DNodeLoader implements NodeLoader {
        protected boolean        is_loading;
        protected URL            url;
        protected BoundingVolume bounds;
        protected SoftReference  node_ref;
        protected Matrix4f       transform;
        protected int            ts =  Integer.MIN_VALUE;
        
        Web3DNodeLoader(URL url, BoundingVolume bounds, Matrix4f transform) {
            this.bounds = bounds;
            this.url    = url;
            this.transform = transform;
            is_loading  = false;
            node_ref    = null;
        }
        
        public void           requestLoad() {
            synchronized (this) {
                Node node = node_ref == null ? null : (Node)node_ref.get();
                if (node != null) return;
                ts = ++(Web3DAutoLoader.this.ts);
                if (is_loading) return;
                is_loading = true;
            }
            
            synchronized (model_queue) {
                model_queue.add(this);
                model_queue.notify();
            }
        }
        
        public Node takeNode() {
            synchronized (this) {
                Node node = node_ref == null ? null : (Node)node_ref.get();
                if (is_loading || node == null)
                    return null;
                
                // Beacuse the stupid SharedNode refuses other parents than Group or other SharedNodes
                Group g = null;
                if (transform == null)
                    g = new Group();
                else {
                    g = new TransformGroup();
                    ((TransformGroup)g).setTransform(transform);
                }
                g.addChild(node);
                node = null;
                return g;
            }
        }
        
        public BoundingVolume getBounds() {
            return bounds;
        }
        
        protected void setNode(Node node) {
            synchronized (this) {
                if (!is_loading) return;
                node_ref = new SoftReference(node);
                is_loading = false;
            }
        }
    }
    
    protected class TextureRec implements NodeUpdateListener {
        int                format;
        TextureComponent   comp;
        WeakReference<Texture> texture;
        URL                url;
        
        public void	updateNodeBoundsChanges(java.lang.Object src) {}
        public void	updateNodeDataChanges(java.lang.Object src) {
            Texture2D texture = (Texture2D)src;
            TextureComponent [] comp_arr = { comp };
            texture.setSources(comp.getNumLevels() > 1 ? Texture.MODE_MIPMAP : Texture.MODE_BASE_LEVEL,
                    format, comp_arr, 1);
        }
    }
    
    static class MyWeb3DLoader extends Web3DLoader {
        static protected ContentHandlerFactory c_fac = null;
        protected void setupPropertiesProtected(BrowserCore core,
                WorldLoaderManager wlm) {
            super.setupPropertiesProtected(core, wlm);
            
            if (c_fac == null)
                c_fac = URI.getContentHandlerFactory();
            if(!(c_fac instanceof AWTContentHandlerFactory)) {
                c_fac = new AWTContentHandlerFactory(core, wlm, c_fac);
                URI.setContentHandlerFactory(c_fac);
            }
        }
    }
    
    protected class ModelLoaderThread extends Thread {
        AVLoader w3d_loader = null;
        AVLoader max_loader = null;
        private TextureCreateUtils textureUtils = new TextureCreateUtils();
        
        
        
        ModelLoaderThread(String name) {
            super(name);
            
            // The Web3D AV loader currently doesn't  support dynamic contents
        }
        
        /**
         * Load a Web3D file from an URL and return the root node
         */
        private Node loadModel(URL url)  {
            System.out.println("Loading model: " + url);
            AVModel model = null;
            int rounds = 0;
            while (model == null && ++rounds <= 2) {
                InputStream in = null;
                try {
                    if (cache_manager != null)
                        in = cache_manager.getInputStream(url);
                    else
                        in = url.openStream();
                    
                    if (in == null) {
                        System.out.println("Model: " + url + " not found");
                        return null;
                    }
                    
                    String base_url = url.toExternalForm();
                    if (base_url.endsWith(".GZ") || base_url.endsWith(".gz"))
                        in = new GZIPInputStream(in);
                    
                    in = new BufferedInputStream(in);
                    System.setProperty("user.dir", base_url.substring(0, base_url.lastIndexOf('/')+1));
                    
                    if (base_url.endsWith(".3ds") || base_url.endsWith(".3ds.gz")) {
                        if (max_loader == null) {
                            max_loader = new MaxLoader();
                            max_loader.setLoadFlags(AVLoader.GEOMETRY);
                        }
                        model = max_loader.load(in);
                    } else {
                        if (w3d_loader == null) {
                            w3d_loader = new MyWeb3DLoader();
                            w3d_loader.setLoadFlags(AVLoader.LOAD_ALL);
                        }
                        model = w3d_loader.load(in);
                    }
                } catch (Throwable ex) {
                    System.err.println("Error during loading of model: " + url);
                    ex.printStackTrace();
                    if (cache_manager != null)
                        cache_manager.deleteFile(url);
                } finally {
                    if (in != null) {
                        try { in.close(); } catch (Throwable ex) { ex.printStackTrace(); }
                    }
                }
                
            }
            
            // AVModel model = loader.load(url);
            if (model == null)
                return null;
            
            Node root = model.getModelRoot();
            // optimizer.optimize(root);
            
            // Attach runtime components
            List<AVRuntimeComponent> rtComps = model.getRuntimeComponents();
            if (!rtComps.isEmpty()) {
                // Assure that the runtime components and textures are not garbage collected before the graphics
                root.setUserData(rtComps);
                for (AVRuntimeComponent rt_comp : rtComps) {
                    runtime_components.add(new WeakReference(rt_comp));
                }
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
                        /*
                        String cwd_str = System.getProperty("user.dir");
                        int start_ix = tex_str.indexOf(cwd_str);
                        String orig_tex_str= tex_str;
                        if (start_ix >= 0)
                            tex_str = tex_str.substring(start_ix + cwd_str.length());
                         */
                        Texture2D texture = (Texture2D)k;
                        URL tex_url = null;
                        try {
                            tex_url = new URL(url, tex_str);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        String url_str = url.toExternalForm();
                        texture.setMinFilter(Texture2D.MINFILTER_NICEST);
                        texture.setMagFilter(Texture2D.MAGFILTER_NICEST);
                        
                        TextureRec rec = new TextureRec();
                        rec.texture = new WeakReference(texture);
                        rec.url = tex_url;
                        synchronized (texture_queue) {
                            texture_queue.add(rec);
                            texture_queue.notify();
                        }
                    }
                }
            }
            
            
            return root;
        }
        
        Comparator modelQueueComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Web3DNodeLoader)o2).ts - ((Web3DNodeLoader)o1).ts;
            }
        };
        
        
        public void run() {
            while (enabled) {
                try {
                    Web3DNodeLoader loader = null;
                    synchronized (model_queue) {
                        while (enabled && model_queue.isEmpty()) {
                            try {
                                model_queue.wait();
                            } catch (InterruptedException ex) {}
                        }
                        Collections.sort(model_queue, modelQueueComp);
                        loader = model_queue.remove(0);
                    }
                    if (loader == null) continue;
                    
                    String url_str = loader.url.toExternalForm();
                    synchronized (loaded_models) {
                        WeakReference<Node> node_ref = loaded_models.get(url_str);
                        if (node_ref != null) {
                            Node node = node_ref.get();
                            if (node != null) {
                                loader.setNode(node);
                                continue;
                            } else
                                loaded_models.remove(url_str);
                        }
                    }
                    
                    if (!enabled)
                        break;
                    
                    
                    Node node = loadModel(loader.url);
                    if (node == null)
                        continue;
                    
                    SharedNode shared_node = new SharedNode();
                    shared_node.setChild(node);
                    
                    synchronized (loaded_models) {
                        loaded_models.put(url_str, new WeakReference(shared_node));
                    }
                    loader.setNode(shared_node);
                } catch (Exception ex) {
                    System.err.println("Error in loading of 3D model");
                    ex.printStackTrace();
                }
            }
        }
    }
    
    
    protected class TextureLoaderThread extends Thread {
        protected TextureCreateUtils textureUtils = new TextureCreateUtils();
        
        TextureLoaderThread(String name) {
            super(name);
        }
        
        protected BufferedImage loadImage(URL url)  {
            System.out.println("Loading texture: " + url);
            BufferedImage img = null;
            int rounds = 0;
            while (++rounds <= 2) {
                try {
                    if (cache_manager != null) {
                        InputStream in = cache_manager.getInputStream(url);
                        if (in != null) {
                            in = new BufferedInputStream(in);
                            img = ImageIO.read(in);
                            in.close();
                        }
                    } else
                        img = ImageIO.read(url);
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (cache_manager != null)
                        cache_manager.deleteFile(url);
                    else
                        break;
                }
            }
            if (img == null)
                System.err.println("Error during loading of texture " + url);
            return img;
        }
        
        public void run() {
            while (enabled) {
                try {
                    TextureRec rec = null;
                    // Fetch a texture request from the queue
                    synchronized (texture_queue) {
                        while (enabled && texture_queue.isEmpty()) {
                            try {
                                texture_queue.wait();
                            } catch (InterruptedException ex) {}
                        }
                        rec = texture_queue.remove(0);
                    }
                    if (rec == null) continue;
                    
                    String url_str = rec.url.toExternalForm();
                    TextureComponent comp = null;
                    
                    // Check cache
                    WeakReference<TextureComponent> cache_rec = texture_cache.get(url_str);
                    // If texture has been loaded earlier
                    if (cache_rec != null) {
                        // Fetch data
                        comp = cache_rec.get();
                        if (comp == null) {
                            // If texture is expired
                            // is_loaded = false;
                            texture_cache.remove(url_str);
                        }
                        System.err.println("Texture from cache: " + url_str);
                    }
                    
                    if (!enabled) break;
                    
                    if (comp == null) {
                        // Not loaded, do it here
                        
                        BufferedImage img = loadImage(rec.url);
                        if(img != null) {
                            // Create texture component
                            /*
                            int int_form = img.getColorModel().hasAlpha() ? GL.GL_RGBA : GL.GL_RGB;
                            com.sun.opengl.util.texture.TextureData tex_data = 
                                    new com.sun.opengl.util.texture.TextureData(int_form, 0, true, img);
                            */
                            int img_width = img.getWidth(null);
                            int img_height = img.getHeight(null);
                            
                            int level_w = (int) Math.ceil(Math.log(img_width) / LOG_2);
                            int level_h = (int) Math.ceil(Math.log(img_height) / LOG_2);
                            
                            int tex_width  = (int)Math.pow(2, level_w);
                            int tex_height = (int)Math.pow(2, level_h);
                            
                            // float xScale = (float)tex_width / (float)img_width;
                            // float yScale = (float)tex_height / (float)img_height;
                            
                            // AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
                            // AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                            
                            int img_type = img.getType();
                            if (img_type == BufferedImage.TYPE_CUSTOM) {
                                if (img.getColorModel().hasAlpha())
                                    img_type = BufferedImage.TYPE_INT_ARGB;
                                else
                                    img_type = BufferedImage.TYPE_INT_RGB;
                            }
                            BufferedImage dest_img = new BufferedImage(tex_width, tex_height, img_type);
                            dest_img.getGraphics().drawImage(img, 0, 0, tex_width, tex_height, null);
                            // img = atop.filter(img, null);
                            img = dest_img;
                            
                            ColorModel cm = img.getColorModel();
                            boolean alpha = cm.hasAlpha();
                            int format = TextureComponent.FORMAT_RGBA;
                            img_type = img.getType();
                            switch (img_type) {
                                case BufferedImage.TYPE_3BYTE_BGR:
                                case BufferedImage.TYPE_BYTE_BINARY:
                                case BufferedImage.TYPE_INT_BGR:
                                case BufferedImage.TYPE_INT_RGB:
                                case BufferedImage.TYPE_USHORT_555_RGB:
                                case BufferedImage.TYPE_USHORT_565_RGB:
                                    format = TextureComponent.FORMAT_RGB;
                                    break;
                                    
                                case BufferedImage.TYPE_CUSTOM:
                                    // no idea what this should be, so default to RGBA
                                case BufferedImage.TYPE_INT_ARGB:
                                case BufferedImage.TYPE_INT_ARGB_PRE:
                                case BufferedImage.TYPE_4BYTE_ABGR:
                                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                                    format = TextureComponent.FORMAT_RGBA;
                                    break;
                                    
                                case BufferedImage.TYPE_BYTE_GRAY:
                                case BufferedImage.TYPE_USHORT_GRAY:
                                    format = TextureComponent.FORMAT_SINGLE_COMPONENT;
                                    break;
                                    
                                case BufferedImage.TYPE_BYTE_INDEXED:
                                    if(alpha)
                                        format = TextureComponent.FORMAT_RGBA;
                                    else
                                        format = TextureComponent.FORMAT_RGB;
                                    break;
                            }
                            
                            BufferedImage [] img_arr = new BufferedImage[1]; // Math.max(level_w, level_h)+1];
                            img_arr[0] = img;
                            
                            /*
                            at = AffineTransform.getScaleInstance(.5f, .5f);
                            atop =  new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                            for (int i=1; i < img_arr.length; ++i) {
                                img = atop.filter(img, null);
                                img_arr[i] = img;
                            }
                            */
                            comp = new ImageTextureComponent2D(format, img_arr);
                            /*
                            int format = int_form == GL.GL_RGBA ? TextureComponent.FORMAT_RGBA : TextureComponent.FORMAT_RGB;
                            Buffer[] buf = tex_data.getMipmapData();
                            ByteBuffer[] bbuf = new ByteBuffer[buf.length];
                            for (int i = 0; i < buf.length; ++i) {
                                bbuf[i] = (ByteBuffer)buf[i];
                            }
                            comp = new ByteBufferTextureComponent2D(format, tex_data.getWidth(), tex_data.getHeight(), bbuf);
                            */
                            // Add to cache
                            texture_cache.put(url_str, new WeakReference(comp));
                        }
                    }
                    
                    if (!enabled || comp == null)
                        continue;
                    
                    rec.format = textureUtils.getTextureFormat(comp);
                    rec.comp = comp;
                    update_textures.add(rec);
                } catch (Exception ex) {
                    System.err.println("Error during model texture loading: ");
                    ex.printStackTrace();
                }
            }
        }
    }
    
    protected Thread model_thread;
    protected Thread texture_thread;
    
    /** Creates a new instance of Web3DAutoLoader */
    public Web3DAutoLoader(CacheManager cache_manager) {
        this.cache_manager = cache_manager;
        
        model_thread = new ModelLoaderThread("Web3DModelLoader");
        model_thread.setPriority(Thread.NORM_PRIORITY-1);
        model_thread.start();
        
        texture_thread = new TextureLoaderThread("Web3DTextureLoader");
        texture_thread.setPriority(Thread.NORM_PRIORITY-1);
        texture_thread.start();
    }
    
    public NodeLoader createNodeLoader(URL url, BoundingVolume bounds) {
        return new Web3DNodeLoader(url, bounds, null);
    }
    public NodeLoader createNodeLoader(URL url, BoundingVolume bounds, Matrix4f transform) {
        return new Web3DNodeLoader(url, bounds, transform);
    }
    
    /**
     * Run the runtime parts of dynamic contents, should be called in the
     * updateScenegraph callback of an ApplicationUpdateObserver.
     */
    public void executeModelBehavior() {
        for (WeakReference<AVRuntimeComponent> rt_ref : runtime_components) {
            AVRuntimeComponent rc = rt_ref.get();
            if (rc == null)
                runtime_components.remove(rt_ref);
            else {
                try {
                    rc.executeModelBehavior();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        }
        
        for (TextureRec rec : update_textures) {
            Texture texture = rec.texture.get();
            if (texture == null)
                update_textures.remove(rec);
            else if (texture.isLive()) {
                texture.dataChanged(rec);
                update_textures.remove(rec);
            }
        }
    }
    
    public void shutdown() {
        enabled = false;
        model_queue.notifyAll();
        texture_queue.notifyAll();
    }
}
