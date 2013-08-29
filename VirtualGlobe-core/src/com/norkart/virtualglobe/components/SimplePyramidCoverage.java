//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.virtualglobe.globesurface.NativeTextureCoosys;
import com.norkart.virtualglobe.globesurface.texture.SimpleCachedURLTextureLoader;
import com.norkart.virtualglobe.globesurface.TextureCoverage;
import com.norkart.virtualglobe.globesurface.TextureTile;
import com.norkart.virtualglobe.components.GlobeCoverage;
import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.virtualglobe.components.Universe;
import org.w3c.dom.Element;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import com.norkart.virtualglobe.globesurface.texture.DDSImageBuffer;
import com.norkart.virtualglobe.globesurface.texture.BufferedImageBuffer;
import com.norkart.virtualglobe.globesurface.Texture2D;
import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.components.DataTreeNode;


import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.Component;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.tree.DefaultTreeModel;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class SimplePyramidCoverage extends GlobeCoverage  {
    private TextureCoverage textureCoverage;
    private URL serverURL;
    private String wms_url;
    
    // private File cacheRoot;
    
    private JLabel urlLabel;
    
    // protected static final int NUM_THREADS = 10;
    // private final int THREAD_PRIORITY = Thread.NORM_PRIORITY-2;
    
    private TextureSource source = null;
    
    public SimplePyramidCoverage(GlobeSurface parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        ((DefaultTreeModel)parent.getUniverse().getDataTreeModel()).insertNodeInto(node, parent.node, parent.node.getChildCount());
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        setTitle(settings.getResourceString("SIMPLE_COVERAGE_TITLE"));
        
        // Create GUI
        Border b = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createTitledBorder(b, settings.getResourceString("SIMPLE_COVERAGE_TITLE")));
        
        // URL label
        Box sub_box = Box.createHorizontalBox();
        sub_box.setBorder(BorderFactory.createTitledBorder(b, settings.getResourceString("SIMPLE_COVERAGE_SERVER")));
        sub_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        urlLabel = new JLabel(serverURL == null? "" : serverURL.toString());
        sub_box.add(urlLabel);
        sub_box.add(Box.createHorizontalGlue());
        panel.add(sub_box);
        
        node.getInfoPanel().add(panel);
    }
    
    public void clear() {
        if (source != null)
            source.close();
        source = null;
        super.clear();
    }
    
    public String getTagName() {
        return "simple-pyramid-coverage";
    }
    
    public void load(Element domElement) throws com.norkart.virtualglobe.components.WorldComponent.LoadException {
        if (!domElement.getNodeName().equals("simple-pyramid-coverage"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        Universe u = getUniverse();
        
        String serverUrlStr = domElement.getAttribute("href");
        serverURL = null;
        try {
            serverURL = new URL(getBaseUrl(), serverUrlStr);
        } catch (MalformedURLException ex) {}
        urlLabel.setText(serverURL == null? "" : serverURL.toString());
        
        wms_url  = domElement.getAttribute("wms-url");
        
        // suffix      = domElement.getAttribute("suffix");
        // suffix = "dds.gz";
        // if (suffix == null || suffix.equals("")) suffix = "jpg";
    /*
    if (elevationUrlLabel != null)
    elevationUrlLabel.setText(elevationsUrlStr);
     */
        
        if (serverURL == null) return;
        
        // Wait for surface
        GlobeSurface gs = (GlobeSurface)parent;
        BttSurface bs = gs.getSurface();
        source = new TextureSource(u.isCacheEnabled()?u.getCacheManager():null);
        textureCoverage = new TextureCoverage(bs, source, new NativeTextureCoosys(bs));
        source.start();
    }
    
    void updateCache() {
        Universe u = getUniverse();
        source.setCacheManager(u.isCacheEnabled()?u.getCacheManager():null);
        super.updateCache();
    }
    
/*
  public void setCacheRoot(File cr) {
    if (cr == null) {
      cacheRoot = null;
      return;
    }
 
    cacheRoot = new File(cr, serverURL.getHost());
    cacheRoot = new File(cacheRoot, serverURL.getPath());
    if (wms_url != null)
      cacheRoot = SimpleCachedURLTextureLoader.getCacheDir(wms_url, cacheRoot);
    cacheRoot.mkdirs();
  }
 */
    
    
    private class TextureSource extends SimpleCachedURLTextureLoader {
        private String urlBase;
        private boolean isWms = false;
        private TextureSource(CacheManager cache_mgr) {
            super("TextureLoader", cache_mgr);
            
            urlBase = serverURL.toString();
            if (wms_url != null && wms_url.length() > 0) {
                isWms = true;
                try {
                    urlBase += "?request-type=get-texture&wms-url="+URLEncoder.encode(wms_url, "UTF-8")+"&texture-format="+(use_compressed?"dds.gz":"jpg")+"&pos-code=";
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
            }
            if (urlBase.lastIndexOf("/") == urlBase.length()-1)
                urlBase = urlBase.substring(0, urlBase.length()-1);
        }
        
        protected  String getTilesetName(TextureTile tile) {
            String retval = serverURL.toString();
            retval += "\nwms-url="+wms_url;
            return retval;
        }
        
        protected  URL  getURL(TextureTile tile) {
            String posCode = tile.getPosCode();
            String imageCode = "";
            for (int i = 0; ; i += 3) {
                if (i+3 >= posCode.length()) {
                    imageCode += posCode.substring(i);
                    break;
                }
                imageCode += posCode.substring(i, i+3) + "/";
            }
            if (!isWms) imageCode += "."+(use_compressed?"dds.gz":"jpg");
            try {
                return new URL(urlBase + "/" + imageCode);
            } catch (MalformedURLException ex) { }
            tile = null;
            return null;
        }
        
        protected InputStream openServerTexture(TextureTile tile) throws IOException {
            if (!server_enabled) return null;
            // Create URL
            URL url = getURL(tile);
            InputStream in = null;
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            if (tile.getFileModTime() > 0)
                connection.setIfModifiedSince(tile.getFileModTime());
            connection.connect();
            
            int response = connection.getResponseCode();
            switch (response) {
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    tile.stopLoading();
                    return null;
                case HttpURLConnection.HTTP_OK:
                    in = connection.getInputStream();
                    break;
                default:
                    tile.setOutsideOfResolution();
            }
            tile = null;
            return in;
        }
        
    }
}