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
import com.norkart.virtualglobe.globesurface.Texture2D;
import com.norkart.virtualglobe.globesurface.TextureCoverage;
import com.norkart.virtualglobe.globesurface.TextureTile;
import com.norkart.virtualglobe.globesurface.texture.ImageBuffer;
import com.norkart.virtualglobe.globesurface.texture.SimpleCachedURLTextureLoader;

import com.norkart.virtualglobe.util.SpringUtilities;
import com.norkart.virtualglobe.util.IsoCalendarParser;


import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import com.norkart.virtualglobe.cache.CacheManager;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class LayeredPyramidCoverage extends GlobeCoverage   {
    private TextureCoverage textureCoverage;
    private URL serverURL;
    private String serverUrlStr;
    // private File cacheRoot;
    
    private JLabel urlLabel;
    private Box panel;
    private DateFormat iso_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    final static private int DEFAULT_TILE_SIZE = 512;
    private int tile_size = DEFAULT_TILE_SIZE/2;
    
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
    cacheRoot.mkdirs();
  }
 */
    
    //-----------------------------------------------------------------------
    // The texture source inner class
    //-----------------------------------------------------------------------
    class TextureSource extends SimpleCachedURLTextureLoader {
        TextureSource(CacheManager cache_mgr) {
            super("TexureLoader", cache_mgr);
            iso_format.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        
        
        protected  String getTilesetName(TextureTile tile) {
            double min_lon = Math.toDegrees(textureCoverage.getSurface().intToLon(tile.getIntLon()));
            double min_lat = Math.toDegrees(textureCoverage.getSurface().intToLat(tile.getIntLat()));
            double max_lon = Math.toDegrees(textureCoverage.getSurface().intToLon(tile.getIntLon()+tile.getIntLonDim()));
            double max_lat = Math.toDegrees(textureCoverage.getSurface().intToLat(tile.getIntLat()+tile.getIntLatDim()));
            while (min_lon < -180) min_lon += 360;
            while (min_lon >= 180) min_lon -= 360;
            while (max_lon < -180) max_lon += 360;
            while (max_lon >= 180) max_lon -= 360;
            if (max_lon < min_lon) max_lon += 360;
            
            
            // Create request
            StringBuffer request = new StringBuffer();
            request.append(serverURL.toString());
            request.append("\n");
            if (tile_size != DEFAULT_TILE_SIZE)
                request.append("tile-size="+tile_size+"\n");
            
            boolean visible = false;
            for (int i=0; i<textureSets.size(); ++i) {
                TextureSet ts = (TextureSet)textureSets.get(i);
                if (ts.composeRequest(min_lon, min_lat, max_lon, max_lat, tile.getPixelSize(), request)) visible = true;
            }
            
            if (!visible || request.length() == 0) return null;
            
            return request.toString();
        }
        
        protected InputStream openServerTexture(TextureTile tile) throws IOException {
            if (!server_enabled) {
                tile.abortLoading();
                return null;
            }
            
            double min_lon = Math.toDegrees(textureCoverage.getSurface().intToLon(tile.getIntLon()));
            double min_lat = Math.toDegrees(textureCoverage.getSurface().intToLat(tile.getIntLat()));
            double max_lon = Math.toDegrees(textureCoverage.getSurface().intToLon(tile.getIntLon()+tile.getIntLonDim()));
            double max_lat = Math.toDegrees(textureCoverage.getSurface().intToLat(tile.getIntLat()+tile.getIntLatDim()));
            while (min_lon < -180) min_lon += 360;
            while (min_lon >= 180) min_lon -= 360;
            while (max_lon < -180) max_lon += 360;
            while (max_lon >= 180) max_lon -= 360;
            if (max_lon < min_lon) max_lon += 360;
            
            
            // Create request
            StringBuffer request = new StringBuffer();
            boolean visible = false;
            for (int i=0; i<textureSets.size(); ++i) {
                TextureSet ts = (TextureSet)textureSets.get(i);
                if (ts.composeRequest(min_lon, min_lat, max_lon, max_lat, tile.getPixelSize(), request)) visible = true;
            }
            
            if (!visible || request.length() == 0) {
                tile.setTexture(null, 0);
                return null;
            }
            
            String posCode = tile.getPosCode();
            String imageCode = "";
            for (int i = 0; ; i += 3) {
                if (i+3 >= posCode.length()) {
                    imageCode += posCode.substring(i);
                    break;
                }
                imageCode += posCode.substring(i, i+3) + "/";
            }
            
            // Send request
            HttpURLConnection connection = (HttpURLConnection)serverURL.openConnection();
            connection.setUseCaches(false);
            if (tile.getFileModTime() > 0)
                connection.setIfModifiedSince(tile.getFileModTime());
            
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            PrintStream out = new PrintStream(connection.getOutputStream(), false, "UTF-8");
            
            // Write the xml request
            out.print("request-type=get-texture");
            out.print("&pos-code=");
            out.print(imageCode);
            out.print("&texture-format=");
            out.print((use_compressed?"dds.gz":"jpg"));
            if (tile_size != DEFAULT_TILE_SIZE)
                out.print("&tile-size="+tile_size);
            out.print('\n');
            out.print(request);
            out.flush();
            out.close();
            
            // System.out.print(request);
            
            int response = connection.getResponseCode();
            switch (response) {
                case HttpURLConnection.HTTP_OK:
                    return connection.getInputStream();
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    tile.stopLoading();
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    tile.setOutsideOfResolution();
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    tile.abortLoading();
                    loadTextureTile(tile);
                    break;
                default: {
                    System.err.println("Texture server: " + serverURL.toString() + ", Error response: " + response);
                    tile.abortLoading();
                    loadTextureTile(tile);
                    break;
                }
            }
            return null;
        }
        
        
        protected  URL  getURL(TextureTile tile) {
            return serverURL;
        }
    }
    
    private TextureSource source = null;
    
    private class WMSLayer {
        String  layer_title;
        String  name = "";
        String  style = "";
        boolean enabled = false;
        double  minx = -180, miny = -90, maxx = 180, maxy = 90;
        float   min_pixel_size = 0, max_pixel_size = Float.MAX_VALUE;
        long    renew = -1;
        WMSTextureSet wmsSet;
        
        WMSLayer(WMSTextureSet wmsSet) {
            this.wmsSet = wmsSet;
        }
    }
    
    abstract private class TextureSet {
        protected float transparency = 0;
        protected float unicolour=2;
        // abstract protected JComponent createGUI();
        abstract protected void load(Element domElement)
        throws DomLoadable.LoadException;
        abstract protected Element save(Document doc);
        abstract protected boolean composeRequest(double min_lon,  double min_lat,
                double max_lon, double max_lat,
                float pixel_size, StringBuffer out);
        
        LayeredPyramidCoverage coverage() { return LayeredPyramidCoverage.this; }
    }
    
    private class WMSTextureSet extends TextureSet {
        // String host_name;
        // String command;
        // String version;
        // String srs;
        // boolean transparent = false;
        String wms_url;
        long    renew = -1;
        Vector<WMSLayer> layers = new Vector();
        
        /*
        protected JComponent createGUI() {
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
         
            Box panel = Box.createVerticalBox();
            panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            Box url_box = Box.createHorizontalBox();
            url_box.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            url_box.add(new JLabel(settings.getResourceString("WMS_URL") + ": " + wms_url));
            url_box.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(url_box);
         
            if (!layers.isEmpty()) {
                JPanel p = new JPanel(new SpringLayout());
                p.setAlignmentX(Component.LEFT_ALIGNMENT);
                Iterator i = layers.iterator();
                while (i.hasNext()) {
                    final WMSLayer l = (WMSLayer)i.next();
                    final JCheckBox cb = new JCheckBox(l.layer_title != null && l.layer_title.length() > 0 ? l.layer_title : l.name);
                    cb.setSelected(l.enabled);
                    cb.setToolTipText(settings.getResourceString("TOOL_TOGGLE_WMS_LAYER"));
                    cb.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent e) {
                            if (e.getSource() == cb && cb.isSelected() != l.enabled) {
                                l.enabled = cb.isSelected();
                                textureCoverage.reloadTextures();
                            }
                        }
                    });
         
                    p.add(cb);
                    p.add(new JLabel(l.style));
                }
         
                SpringUtilities.makeCompactGrid(p, //parent
                        layers.size(), 2,
                        3, 3,  //initX, initY
                        3, 3); //xPad, yPad
                panel.add(p);
            }
            return panel;
        }*/
        
        protected void load(Element domElement)
        throws DomLoadable.LoadException {
            String tr_str = domElement.getAttribute("transparency");
            if (tr_str != null & tr_str.length() > 0)
                transparency = Float.parseFloat(tr_str);
            
            String uc_str = domElement.getAttribute("unicolour");
            if (uc_str != null & uc_str.length() > 0)
                unicolour = Float.parseFloat(uc_str);
            
            String renew_str = domElement.getAttribute("renew");
            if (renew_str.length() > 0) {
                try {
                    renew = IsoCalendarParser.getCalendar(renew_str).getTimeInMillis(); // iso_format.parse(renew_str).getTime();
                } catch (NumberFormatException ex) {
                    System.out.println(ex);
                }
            }
            
            wms_url = domElement.getAttribute("wms-url");
            int i;
            for (i = 0; i < wms_url.length(); ++i) {
                int chr = wms_url.charAt(i);
                if (chr != ' ' &&
                        chr != '\t' &&
                        chr != '\n')
                    break;
            }
            if (i > 0)
                wms_url = wms_url.substring(i);
            for (i = wms_url.length()-1; i >= 0; --i) {
                int chr = wms_url.charAt(i);
                if (chr != ' ' &&
                        chr != '\t' &&
                        chr != '\n')
                    break;
            }
            if (i < wms_url.length()-1)
                wms_url = wms_url.substring(0, i+1);
            
            if (wms_url.indexOf('?') == -1)
                wms_url += "?";
            if (wms_url.charAt(wms_url.length()-1) != '?' &&
                    wms_url.charAt(wms_url.length()-1) != '&')
                wms_url += "&";
            // host_name = domElement.getAttribute("host");
            // command   = domElement.getAttribute("command");
            // srs       = domElement.getAttribute("srs");
            // version   = domElement.getAttribute("version");
            /// transparent = "true".equalsIgnoreCase(domElement.getAttribute("transparent"));
            
            for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if (!(ch instanceof Element)) continue;
                Element chEle = (Element)ch;
                if (chEle.getNodeName().equals("layer")) {
                    WMSLayer l = new WMSLayer(this);
                    layers.add(l);
                    l.layer_title   = chEle.getAttribute("title");
                    l.name    = chEle.getAttribute("name");
                    l.style   = chEle.getAttribute("style");
                    l.enabled = "true".equalsIgnoreCase(chEle.getAttribute("enabled"));
                    
                    String bbox_string = chEle.getAttribute("bbox");
                    if (bbox_string.length() > 0) {
                        String[] bbox_split = bbox_string.split(",");
                        l.minx = Double.parseDouble(bbox_split[0]);
                        l.miny = Double.parseDouble(bbox_split[1]);
                        l.maxx = Double.parseDouble(bbox_split[2]);
                        l.maxy = Double.parseDouble(bbox_split[3]);
                    }
                    String max_pixel_str = chEle.getAttribute("max-pix-size");
                    String min_pixel_str = chEle.getAttribute("min-pix-size");
                    
                    if (max_pixel_str.length() > 0)
                        l.max_pixel_size = Float.parseFloat(max_pixel_str);
                    if (min_pixel_str.length() > 0)
                        l.min_pixel_size = Float.parseFloat(min_pixel_str);
                    
                    renew_str = chEle.getAttribute("renew");
                    if (renew_str.length() > 0) {
                        try {
                            l.renew = IsoCalendarParser.getCalendar(renew_str).getTimeInMillis(); // iso_format.parse(renew_str).getTime();
                            // System.out.println(l.renew);
                        } catch (NumberFormatException ex) {
                            System.out.println(ex);
                        }
                    }
                }
            }
        }
        
        protected Element save(Document doc) {
            Element ele = doc.createElement("wms-texture-set");
            
            if (transparency > 0)
                ele.setAttribute("transparency", Float.toString(transparency));
            
            
            if (unicolour < 1 && unicolour >= 0)
                ele.setAttribute("unicolour", Float.toString(unicolour));
            
            if (renew > 0)
                ele.setAttribute("renew", iso_format.format(new Date(renew)));
            
            ele.setAttribute("wms-url", wms_url);
            
            for (WMSLayer l : layers) {
                Element layer_ele = doc.createElement("layer");
                layer_ele.setAttribute("name", l.name);
                layer_ele.setAttribute("enabled", Boolean.toString(l.enabled));
                if (l.layer_title != null && l.layer_title.length() > 0)
                    layer_ele.setAttribute("title", l.layer_title);
                if (l.style != null && l.style.length() > 0)
                    layer_ele.setAttribute("style", l.style);
                
                if (l.minx != -180 || l.miny != -90 || l.maxx != 180 || l.maxy != 90) {
                    String bbox_string = l.minx + "," + l.miny + "," + l.maxx + "," + l.maxy;
                    layer_ele.setAttribute("bbox", bbox_string);
                }
                if (l.min_pixel_size != 0)
                    layer_ele.setAttribute("min-pix-size", Float.toString(l.min_pixel_size));
                if (l.max_pixel_size != Float.MAX_VALUE)
                    layer_ele.setAttribute("max-pix-size", Float.toString(l.max_pixel_size));
                
                if (l.renew > 0)
                    layer_ele.setAttribute("renew", iso_format.format(new Date(l.renew)));
                
                ele.appendChild(layer_ele);
            }
            
            return ele;
        }
        
        protected boolean composeRequest(double min_lon,  double min_lat,
                double max_lon, double max_lat,
                float pixel_size, StringBuffer out) {
            String layers_str = "";
            String styles_str = "";
            // Finn aktive layers
            // Sjekk hvordan det passer med størrelse og plassering
            
            long renew_time = renew;
            boolean bottom_reached = true;
            for (int i=layers.size()-1; i >= 0; --i) {
                WMSLayer l = (WMSLayer)layers.get(i);
                if (!l.enabled) continue;
                if (min_lon > l.maxx || min_lat > l.maxy ||
                        max_lon < l.minx || max_lat < l.miny)
                    continue;
                if (pixel_size > l.max_pixel_size)
                    continue;
                if (pixel_size > l.min_pixel_size)
                    bottom_reached = false;
                if (layers_str.length() > 0 || styles_str.length() > 0) {
                    layers_str += ",";
                    styles_str += ",";
                }
                layers_str += l.name;
                styles_str += l.style;
                if (l.renew > renew_time)
                    renew_time = l.renew;
            }
            
            if (layers_str.length() == 0)
                return false;
            
      /*
      out.append("wms-host=");
      out.append(host_name);
      out.append("&wms-path=");
      out.append(command);
      if (srs != null && srs.length() > 0) {
        out.append("&wms-srs=");
        out.append(srs);
      }
      if (version != null && version.length() > 0) {
        out.append("&wms-version=");
        out.append(version);
      }
      if (transparent) {
        out.append("&wms-transparent=true");
      }
       */
            
            
            String arg = "LAYERS="+layers_str+"&STYLES="+styles_str;
            out.append("wms-url=");
            try {
                out.append(URLEncoder.encode(wms_url, "UTF-8"));
                out.append(URLEncoder.encode(arg, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {}
            if (transparency > 0) {
                out.append("&transparency=");
                out.append(transparency);
            }
            if (unicolour < 1 && unicolour >= 0) {
                out.append("&unicolour=");
                out.append(unicolour);
            }
            if (renew_time > 0) {
                out.append("&renew=");
                out.append(iso_format.format(new Date(renew_time)));
            }
            out.append("\n");
            return !bottom_reached;
        }
    }
    
    private class LocalPyramid extends TextureSet {
        String layer_title;
        String path;
        boolean enabled = false;
        double  minx = -180, miny = -90, maxx = 180, maxy = 90;
        float   min_pixel_size = 0, max_pixel_size = Float.MAX_VALUE;
        
        /*
        protected JComponent createGUI() {
            Box panel = Box.createVerticalBox();
            final JCheckBox cb = new JCheckBox(layer_title != null && layer_title.length() > 0 ? layer_title : path);
            cb.setSelected(enabled);
            cb.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    if (e.getSource() == cb && cb.isSelected() != enabled) {
                        enabled = cb.isSelected();
                        textureCoverage.reloadTextures();
                    }
                }
            });
            panel.add(cb);
            return panel;
        }
         */
        
        protected void load(Element domElement)
        throws DomLoadable.LoadException {
            String tr_str = domElement.getAttribute("transparency");
            if (tr_str != null & tr_str.length() > 0)
                transparency = Float.parseFloat(tr_str);
            layer_title   = domElement.getAttribute("title");
            path = domElement.getAttribute("path");
            enabled = "true".equalsIgnoreCase(domElement.getAttribute("enabled"));
            String bbox_string = domElement.getAttribute("bbox");
            if (bbox_string.length() > 0) {
                String[] bbox_split = bbox_string.split(",");
                minx = Double.parseDouble(bbox_split[0]);
                miny = Double.parseDouble(bbox_split[1]);
                maxx = Double.parseDouble(bbox_split[2]);
                maxy = Double.parseDouble(bbox_split[3]);
            }
            String max_pixel_str = domElement.getAttribute("max-pix-size");
            String min_pixel_str = domElement.getAttribute("min-pix-size");
            if (max_pixel_str.length() > 0)
                max_pixel_size = Float.parseFloat(max_pixel_str);
            if (min_pixel_str.length() > 0)
                min_pixel_size = Float.parseFloat(min_pixel_str);
        }
        
        protected Element save(Document doc) {
            Element ele = doc.createElement("local-texture-set");
            ele.setAttribute("path", path);
            ele.setAttribute("enabled", Boolean.toString(enabled));
            if (layer_title != null && layer_title.length() > 0)
                ele.setAttribute("title", layer_title);
            if (transparency > 0)
                ele.setAttribute("transparency", Float.toString(transparency));
            if (minx != -180 || miny != -90 || maxx != 180 || maxy != 90) {
                String bbox_string = minx + "," + miny + "," + maxx + "," + maxy;
                ele.setAttribute("bbox", bbox_string);
            }
            if (min_pixel_size != 0)
                ele.setAttribute("min-pix-size", Float.toString(min_pixel_size));
            if (max_pixel_size != Float.MAX_VALUE)
                ele.setAttribute("max-pix-size", Float.toString(max_pixel_size));
            
            return ele;
        }
        
        
        protected boolean composeRequest(double min_lon,  double min_lat,
                double max_lon, double max_lat,
                float pixel_size, StringBuffer out) {
            
            if (!enabled) return false;
            if (min_lon > maxx || min_lat > maxy ||
                    max_lon < minx || max_lat < miny)
                return false;
            if (pixel_size > max_pixel_size)
                return false;
            
            out.append("local-path=");
            out.append(path);
            if (transparency > 0) {
                out.append("&transparency=");
                out.append(transparency);
            }
            out.append("\n");
            if (pixel_size < min_pixel_size)
                return false;
            return true;
        }
    }
    
    private class DTNodeListener implements DataTreeNodeListener {
        public void nodeSelected(DataTreeNode node) {
            Object user_obj = node.getUserObject();
            
            if (user_obj instanceof WMSLayer) {
                WMSLayer layer = (WMSLayer)user_obj;
                if (layer.enabled != node.isSelected()) {
                    layer.enabled = node.isSelected();
                    textureCoverage.reloadTextures();
                }
            } else if (user_obj instanceof LocalPyramid) {
                LocalPyramid layer = (LocalPyramid)user_obj;
                if (layer.enabled != node.isSelected()) {
                    layer.enabled = node.isSelected();
                    textureCoverage.reloadTextures();
                }
            }
        }
        
        public void childrenChanged(DataTreeNode n) {
            Object user_obj = n.getUserObject();
            if (user_obj == LayeredPyramidCoverage.this) {
                textureSets.clear();
                Enumeration e = n.children();
                while (e.hasMoreElements()) {
                    DataTreeNode ch_n = (DataTreeNode)e.nextElement();
                    textureSets.add((TextureSet)ch_n.getUserObject());
                }
            } else if (user_obj instanceof WMSTextureSet) {
                WMSTextureSet tex_set = (WMSTextureSet)user_obj;
                tex_set.layers.clear();
                Enumeration e = n.children();
                while (e.hasMoreElements()) {
                    DataTreeNode ch_n = (DataTreeNode)e.nextElement();
                    tex_set.layers.add((WMSLayer)ch_n.getUserObject());
                }
            } else if (user_obj instanceof TextureSet) {
            } else
                throw new IllegalStateException("Impossible node type for childrenChanged");
            
            textureCoverage.reloadTextures();
        }
        
        public DataTreeNodeListener.AcceptType acceptAsChild(DataTreeNode parent, DataTreeNode child) {
            Object child_user = child.getUserObject();
            Object parent_user = parent.getUserObject();
            if (child_user instanceof WMSLayer && parent_user instanceof WMSTextureSet) {
                WMSLayer layer = (WMSLayer)child_user;
                WMSTextureSet wms_set = (WMSTextureSet)parent_user;
                if (wms_set.coverage() == LayeredPyramidCoverage.this && layer.wmsSet == wms_set)
                    return AcceptType.ACCEPT_MOVE;
            } else if (child_user instanceof TextureSet  && parent_user == LayeredPyramidCoverage.this) {
                TextureSet layer = (TextureSet)child_user;
                if (LayeredPyramidCoverage.this == layer.coverage())
                    return AcceptType.ACCEPT_MOVE;
            }
            return AcceptType.REJECT;
        }
    }
    
    private DTNodeListener dataTreeNodeListener = new DTNodeListener();
    private Vector<TextureSet> textureSets          = new Vector();
    
    public LayeredPyramidCoverage(WorldComponent parent) {
        super(parent);
        
        node = new DataTreeNode();
        node.setUserObject(this);
        parent.getUniverse().getDataTreeModel().insertNodeInto(node, parent.node, parent.node.getChildCount());
        node.setDataTreeNodeListener(dataTreeNodeListener);
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        setTitle(settings.getResourceString("LAYERED_COVERAGE_TITLE"));
        
        // Create GUI
        Border b = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        panel = Box.createVerticalBox();
        // panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder(b, settings.getResourceString("LAYERED_COVERAGE_TITLE")));
        
        // URL label
        Box url_box = Box.createHorizontalBox();
        url_box.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        url_box.setAlignmentX(Component.LEFT_ALIGNMENT);
        url_box.add(new JLabel(settings.getResourceString("LAYERED_COVERAGE_SERVER")));
        url_box.add(Box.createHorizontalGlue());
        urlLabel = new JLabel("");
        url_box.add(urlLabel);
        panel.add(url_box);
        // panel.add(Box.createVerticalGlue());
        node.getInfoPanel().add(panel);
        
        /*
        JMenuItem new_layer_mi = new JMenuItem(settings.getResourceString("NEW_WMS_TITLE"));
        new_layer_mi.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
                WMSTextureSet wms = new WMSTextureSet();
                textureSets.add(wms);
           }
        });
         
        node.getPopup().add(new_layer_mi);
         */
    }
    
    public void clear() {
        if (source != null)
            source.close();
        source = null;
        super.clear();
    }
    
    public String getTagName() {
        return "layered-pyramid-coverage";
    }
    
    public void load(Element domElement) throws DomLoadable.LoadException {
        if (!domElement.getNodeName().equals("layered-pyramid-coverage"))
            throw new LoadException("Invalid element name");
        super.load(domElement);
        // 
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        serverUrlStr = domElement.getAttribute("href");
        serverURL = null;
        try {
            serverURL = new URL(getBaseUrl(), serverUrlStr);
        } catch (MalformedURLException ex) {}
        
        if (serverURL == null) return;
        urlLabel.setText(serverUrlStr);
        
        // Read layer info
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("wms-texture-set")) {
                WMSTextureSet wms = new WMSTextureSet();
                textureSets.add(wms);
                wms.load(chEle);
            } else if (chEle.getNodeName().equals("local-texture-set")) {
                LocalPyramid ts = new LocalPyramid();
                textureSets.add(ts);
                ts.load(chEle);
            }
        }
        
        // Open cache
        
        // Wait for surface
        new Thread() {
            public void run() {
                Universe u = getUniverse();
                GlobeSurface gs = (GlobeSurface)parent;
                BttSurface bs = gs.getSurface();
                source = new TextureSource(u.isCacheEnabled()?u.getCacheManager():null);
                textureCoverage = new TextureCoverage(bs, source, new NativeTextureCoosys(bs));
                source.start();
            }
        }.start();
        
        // GUI Stuff
        // Data sets
        
        DefaultTreeModel dtm = getUniverse().getDataTreeModel();
        for (TextureSet ts : textureSets) {
            DataTreeNode ts_dtn = new DataTreeNode();
            ts_dtn.setUserObject(ts);
            ts_dtn.setMode(DataTreeNode.Mode.CHECK);
            ts_dtn.setDataTreeNodeListener(dataTreeNodeListener);
            
            if (ts instanceof LocalPyramid) {
                LocalPyramid lts = (LocalPyramid)ts;
                ts_dtn.setTitle(lts.layer_title != null && lts.layer_title.length() > 0 ? lts.layer_title : lts.path);
                ts_dtn.select(lts.enabled);
            } else if (ts instanceof WMSTextureSet) {
                WMSTextureSet wts = (WMSTextureSet)ts;
                ts_dtn.setTitle("WMS");
                for (WMSLayer wms_layer : wts.layers) {
                    DataTreeNode layer_dtn = new DataTreeNode();
                    layer_dtn.setMode(DataTreeNode.Mode.CHECK);
                    layer_dtn.setUserObject(wms_layer);
                    layer_dtn.setTitle(wms_layer.layer_title != null && wms_layer.layer_title.length() > 0 ?
                        wms_layer.layer_title : (wms_layer.name + ":" + wms_layer.style));
                    layer_dtn.select(wms_layer.enabled);
                    layer_dtn.setDataTreeNodeListener(dataTreeNodeListener);
                    ts_dtn.add(layer_dtn);
                }
                Box url_box = Box.createHorizontalBox();
                url_box.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                url_box.add(new JLabel(settings.getResourceString("WMS_URL") + ": " + wts.wms_url));
                ts_dtn.getInfoPanel().add(url_box);
            }
            dtm.insertNodeInto(ts_dtn, node, node.getChildCount());
        }
        
        /*
        Iterator lay_it = textureSets.iterator();
        while (lay_it.hasNext()) {
            Object o = lay_it.next();
            if (o instanceof TextureSet) {
                TextureSet ts = (TextureSet)o;
                JComponent tsc = ts.createGUI();
                if (tsc != null) {
                    // panel.add(new JSeparator(SwingConstants.HORIZONTAL));
                    tsc.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel.add(tsc);
                }
            }
         */
       /*
       else if (o instanceof
       final WMSLayer l = (WMSLayer)o;
       final JCheckBox cb = new JCheckBox(l.name);
       cb.setSelected(l.enabled);
       cb.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
       if (cb.isSelected() != l.enabled) {
       l.enabled = cb.isSelected();
       createLayerList();
       }
       }
       });
        */
        //}
    }
    
    public Element save(Document doc) {
        Element ele = super.save(doc);
        
        ele.setAttribute("href", serverUrlStr);
        
        for (TextureSet ts : textureSets) {
            ele.appendChild(ts.save(doc));
        }
        
        return ele;
    }
    
}


