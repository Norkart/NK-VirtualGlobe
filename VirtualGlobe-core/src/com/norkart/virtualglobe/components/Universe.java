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
import com.norkart.virtualglobe.components.ApplicationLoader;
import com.norkart.virtualglobe.components.DomLoadable;
import com.norkart.virtualglobe.viewer.ViewerManager;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//import javax.swing.*;
import javax.swing.JOptionPane;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
// import javax.swing.event.TreeModelListener;
// import javax.swing.event.TreeModelEvent;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.PrintStream;
import org.w3c.dom.*;
import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.cache.CacheManagerFactory;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class Universe implements /*TreeModel,*/ DomLoadable {
    private Vector treeModelListeners = new Vector();
    private Vector<World> worlds = new Vector();
    private URL url;
    private ApplicationLoader loader;
    // private GraphicsCore graphics = GraphicsCore.getGraphics();
    private File cache_root;
    private boolean cache_enabled;
    
    private CacheManager cache_mgr = CacheManagerFactory.getInstance().createCacheManager();
    
    // Preference key name
    private static final String PREF_CACHEROOT    = "cacheRoot";
    private static final String PREF_CACHEENABLED = "cacheEnabled";
    private static final String PREF_CACHESIZE    = "cacheSize";
    private static final String PREF_LAST_DATA_SET_LIST = "lastDataSetList";
    static final private String PREF_NODE_NAME     = "/com/norkart/VirtualGlobe/WorldComponents";
    static final private String PREF_NODE_OLD_NAME = "/com/sintef/VirtualGlobe";

    // The data set pane
    // private Box         panel;
    
    // private JComboBox   dataSetList;
    private Vector      dataSets = new Vector();
    
    private DefaultTreeModel     dataTreeModel;
    // private DataTreeNode dataTreeRoot;
    
    HashMap<String, WorldComponent> loadedComponents = new HashMap();
    
    public Universe() {
        try {
            Preferences user_root = Preferences.userRoot();
            
            Preferences old_prefs = null;
            if (user_root.nodeExists(PREF_NODE_OLD_NAME)) 
                old_prefs = user_root.node(PREF_NODE_OLD_NAME);
            Preferences prefs = user_root.node(PREF_NODE_NAME);
            
            String cache_root_str = old_prefs != null ? old_prefs.get(PREF_CACHEROOT, null) : null;
            cache_root_str = prefs.get(PREF_CACHEROOT, cache_root_str);
            if (cache_root_str == null) {
                cache_root_str = System.getProperty("java.io.tmpdir");
                if (cache_root_str == null)
                    cache_root_str = System.getProperty("deployment.user.tmp");
                if (cache_root_str != null) {
                    File cr = new File(cache_root_str, "globe");
                    cache_root_str = cr.getPath();
                }
            }
            cache_enabled = true;
            if (old_prefs != null)
                cache_enabled = old_prefs.getBoolean(PREF_CACHEENABLED, cache_enabled);
            cache_enabled = prefs.getBoolean(PREF_CACHEENABLED, cache_enabled);
            if (cache_root_str != null)
                cache_root = new File(cache_root_str);
            else if (cache_enabled) {
                prefs.putBoolean(PREF_CACHEENABLED, false);
                cache_enabled = false;
            }
            
            long cache_size = cache_mgr.getCacheSize();
            if (old_prefs != null)
                cache_size = old_prefs.getLong(PREF_CACHESIZE, cache_size);
            cache_size = prefs.getLong(PREF_CACHESIZE, cache_size);
            cache_mgr.setCache(cache_enabled?cache_root:null, cache_size);
            String lastDataStr = prefs.get(PREF_LAST_DATA_SET_LIST, "");
            String[] lastDataTokens = lastDataStr.split("\n");
            for (int i=0; i<lastDataTokens.length; ++i)
                dataSets.add(lastDataTokens[i]);
        } catch (Exception ex) {
            System.err.print("Error in reading of universe preferences: ");
            ex.printStackTrace();
        }
        
        dataTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        
        // createGui();
        
        loader = new ApplicationLoader();
        loader.start();
    }
    
    
    public DefaultTreeModel getDataTreeModel() {
        return dataTreeModel;
    }
    
/*
  private void createGui() {
    // Create Data selector pane
    panel = Box.createVerticalBox();
 
    Box dataSetPanel = Box.createHorizontalBox();
    dataSetPanel.setBorder(BorderFactory.createTitledBorder(getResourceString("DATA_SET_BOX_TITLE")));
    dataSetList = new JComboBox(dataSets);
    dataSetList.setEditable(true);
    dataSetList.addActionListener(this);
    dataSetPanel.add(dataSetList);
 
    final JFileChooser dataChooser = new JFileChooser();
    final JButton openButton = new JButton();
    openButton.setText("...");
    openButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataChooser.setSelectedFile(getCacheDirectory());
        int retval = dataChooser.showDialog(openButton, null);
        if (retval == JFileChooser.APPROVE_OPTION) {
          try {
            setDataSelection(dataChooser.getSelectedFile().toURL().toString());
          }
          catch (MalformedURLException ex) {}
       }
     }
    });
    dataSetPanel.add(openButton);
 
 
    dataSetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(dataSetPanel);
 
    final JCheckBox useCacheButton = new JCheckBox();
    final JFileChooser cacheDirChooser = new JFileChooser();
    final JButton cacheDirNameButton = new JButton();
 
    Box cacheBox = Box.createHorizontalBox();
    cacheBox.setBorder(BorderFactory.createTitledBorder(getResourceString("CACHE_BOX_TITLE")));
 
    cacheDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    cacheDirChooser.setApproveButtonText("Cache directory");
 
    useCacheButton.setText(getResourceString("CACHE_ENABLED_TITLE"));
    useCacheButton.setSelected(isCacheEnabled());
    useCacheButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == useCacheButton) {
          File cacheRoot = getCacheDirectory();
          if (useCacheButton.isSelected() && cacheRoot == null) {
            cacheDirChooser.setSelectedFile(null);
            int retval = cacheDirChooser.showDialog(cacheDirNameButton, null);
            if (retval == JFileChooser.APPROVE_OPTION) {
              cacheRoot = cacheDirChooser.getSelectedFile();
              cacheDirNameButton.setText(cacheRoot.getAbsolutePath());
            }
          }
          if (cacheRoot == null)
            useCacheButton.setSelected(false);
          setCache(cacheRoot, useCacheButton.isSelected());
        }
      }
    });
    useCacheButton.setToolTipText(getResourceString("CACHE_ENABLED_TIPS"));
    cacheBox.add(useCacheButton);
 
 
    File cacheDir = getCacheDirectory();
    if (cacheDir != null)
      cacheDirNameButton.setText(cacheDir.getAbsolutePath());
    else
      cacheDirNameButton.setText("Cache Directory");
    cacheBox.add(cacheDirNameButton);
 
    cacheDirNameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cacheDirChooser.setSelectedFile(getCacheDirectory());
        int retval = cacheDirChooser.showDialog(cacheDirNameButton, null);
        if (retval == JFileChooser.APPROVE_OPTION) {
          cacheDirNameButton.setText(cacheDirChooser.getSelectedFile().getAbsolutePath());
          setCache(cacheDirChooser.getSelectedFile(), useCacheButton.isSelected());
        }
      }
    });
    cacheBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(cacheBox);
  }
 */
    
    
    public URL getUrl() {
        return url;
    }
    
    public Vector getLastDataSets() {
        return dataSets;
    }
    
    public void clear() {
        for (World currentWorld : worlds) {
            int [] ch_ix_arr =new int[currentWorld.getNumChildren()];
            Object [] ch_arr = new Object [currentWorld.getNumChildren()];
            for (int i=0; i<currentWorld.getNumChildren(); ++i) {
                ch_ix_arr[i] = i;
                ch_arr[i] = currentWorld.getChild(i);
            }
            
            /*
            TreeModelEvent e = new TreeModelEvent(this, new Object[] {currentWorld}, ch_ix_arr, ch_arr);
            for (int i = 0; i < treeModelListeners.size(); i++) {
                ((TreeModelListener)treeModelListeners.elementAt(i)).treeNodesRemoved(e);
            }
             */
            currentWorld.clear();
        }
        worlds.clear();
        loadedComponents.clear();
        url = null;
    }
    
    public void load(Element domElement) throws LoadException {
        if (!domElement.getNodeName().equals("universe"))
            throw new LoadException("Invalid element name");
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (!(ch instanceof Element)) continue;
            Element chEle = (Element)ch;
            if (chEle.getNodeName().equals("world")) {
                World currentWorld = new World(this);
                /*
                TreeModelEvent e = new TreeModelEvent(this, new Object[] {currentWorld});
                for (int i = 0; i < treeModelListeners.size(); i++)
                    ((TreeModelListener)treeModelListeners.elementAt(i)).treeStructureChanged(e);
                 */
                currentWorld.load(chEle);
                worlds.add(currentWorld);
            }
        }
        // dataTree.expandAll(true);
    }
    
    public Element save(Document doc) {
        Element ele = doc.createElement("universe");
        for (World currentWorld : worlds)
            ele.appendChild(currentWorld.save(doc));
        return ele;
    }
    
    public void save(PrintStream out, String  prefix) {
        
    }
    
    public final ApplicationLoader getApplicationLoader() {
        return loader;
    }
    /*
    public final GraphicsCore getGraphics() {
        return graphics;
    }
     */
    
    public final World getWorld() {
        return worlds.get(0);
    }
    
    public void clearCache() {
        if (cache_root == null)
            return;
        boolean ch_en = cache_enabled;
        if (ch_en) {
            cache_enabled = false;
            cache_mgr.setCache(null, cache_mgr.getCacheSize());
            for (World currentWorld : worlds)
                currentWorld.updateCache();
        }
        
        while (cache_root.exists())
            CacheManager.killAll(cache_root);
        
        if (ch_en) {
            cache_enabled = true;
            cache_mgr.setCache(cache_root, cache_mgr.getCacheSize());
            for (World currentWorld : worlds)
                currentWorld.updateCache(); 
        }
    }
    
    public void setCache(File cacheDirectory, boolean enabled, long max_size) {
        
        if (cache_enabled != enabled ||
                (cache_root != null && !cache_root.equals(cacheDirectory)) ||
                (cacheDirectory != null && !cacheDirectory.equals(cache_root))) {
            if (enabled)
                javax.swing.JOptionPane.showMessageDialog(null,
                        ApplicationSettings.getApplicationSettings().getResourceString("RESTART_MESSAGE"));
            cache_root = cacheDirectory;
            cache_enabled = enabled;
            
            cache_mgr.setCache(cache_enabled?cache_root:null, max_size);
            for (World currentWorld : worlds)
                currentWorld.updateCache();
            
            try {
                Preferences root = Preferences.userRoot();
                Preferences prefs = root.node(PREF_NODE_NAME);
                //Preferences prefs = Preferences.userNodeForPackage(getClass());
                if (cacheDirectory != null) {
                    prefs.put(PREF_CACHEROOT, cacheDirectory.getPath());
                    prefs.putBoolean(PREF_CACHEENABLED, cache_enabled);
                    prefs.putLong(PREF_CACHESIZE, max_size);
                } else {
                    prefs.remove(PREF_CACHEROOT);
                    prefs.remove(PREF_CACHEENABLED);
                    prefs.remove(PREF_CACHESIZE);
                }
            } catch (SecurityException ex) {
                System.err.print("Error in writing of cache preferences: ");
                System.err.println(ex);
            }
        } else if (cache_mgr.getCacheSize() != max_size) {
            cache_mgr.setCacheSize(max_size);
            try {
                Preferences root = Preferences.userRoot();
                Preferences prefs = root.node(PREF_NODE_NAME);
                // Preferences prefs = Preferences.userNodeForPackage(getClass());
                prefs.putLong(PREF_CACHESIZE, max_size);
            } catch (SecurityException ex) {
                System.err.print("Error in writing of cache preferences: ");
                System.err.println(ex);
            }
        }
    }
    
    public boolean isCacheEnabled() {
        return cache_enabled;
    }
    
    public File getCacheDir() {
        return cache_root;
    }
    
    public CacheManager getCacheManager() {
        return cache_mgr;
    }
    
    public void setDataSet(String urlStr) {
        if (urlStr == null) return;
        try {
            setDataSet(new URL(urlStr));
        } catch (MalformedURLException ex) { }
    }
    
    public void setDataSet(URL newUrl) {
        if (newUrl == null) return;
        if (newUrl.equals(url)) return;
        String urlStr = newUrl.toString();
        dataSets.remove(urlStr);
        dataSets.add(0, urlStr);
        if (dataSets.size() > 10)
            dataSets.remove(dataSets.size()-1);
        
        StringBuffer strBuf = new StringBuffer();
        strBuf.append((String)dataSets.get(0));
        for (int i=1; i<dataSets.size(); ++i) {
            strBuf.append("\n");
            strBuf.append((String)dataSets.get(i));
        }
        try {
            Preferences root = Preferences.userRoot();
            Preferences prefs = root.node(PREF_NODE_NAME);
            // Preferences prefs = Preferences.userNodeForPackage(getClass());
            prefs.put(PREF_LAST_DATA_SET_LIST, strBuf.toString());
        } catch (SecurityException ex) {
            System.err.print("Error in writing of last data sets: ");
            System.err.println(ex);
        }
        clear();
        
        URL oldUrl = url;
        url = newUrl;
        try {
            loader.requestLoading(this, url, false);
        } catch (Exception ex) {
            Object arg[] = {urlStr};
            String message = MessageFormat.format(ApplicationSettings.getApplicationSettings().getResourceString("NO_OPEN_MESSAGE"), arg);
            JOptionPane.showMessageDialog(null,
                    message, null,
                    JOptionPane.WARNING_MESSAGE);
            
            dataSets.remove(urlStr);
            clear();
            if (oldUrl == null)
                url = null;
            else
                setDataSet(oldUrl);
        }
    }
/*
  public Component getGui() {
    return panel;
  }
 */
  /*
   * Tree model stuff
   */
    /*
    public Object getRoot() {
        return currentWorld;
    }
    public Object getChild(Object parent, int index) {
        return ((WorldComponent)parent).getChild(index);
    }
    public int getChildCount(Object parent) {
        return ((WorldComponent)parent).getNumChildren();
    }
    public boolean isLeaf(Object node) {
        return ((WorldComponent)node).getNumChildren() == 0;
    }
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
                + path + " --> " + newValue);
    }
    public int getIndexOfChild(Object parent, Object child) {
        return ((WorldComponent)parent).getIndexOfChild((WorldComponent)child);
    }
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }
    
    public void fireTreeNodesInserted(TreeModelEvent e) {
        Iterator it = treeModelListeners.iterator();
        while (it.hasNext())
            ((TreeModelListener)it.next()).treeNodesInserted(e);
    }
    
     public void fireTreeNodesChanged(TreeModelEvent e) {
        Iterator it = treeModelListeners.iterator();
        while (it.hasNext())
            ((TreeModelListener)it.next()).treeNodesChanged(e);
    }
    */
/*
  private void notifyTreeChange() {
    for (Enumeration e = treeModelListeners.elements() ; e.hasMoreElements() ;) {
      TreeModelListener listener = (TreeModelListener)e.nextElement();
      // listener.
    }
  }
 */
    
    // ActionListener
  /*
 public void actionPerformed(ActionEvent e) {
   if (e.getSource() == dataSetList) {
     String newSelection = (String)dataSetList.getSelectedItem();
     setDataSelection(newSelection);
   }
   else
     System.err.println("Unknown action source");
 }
   */
}