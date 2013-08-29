//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.components.Universe;
import com.norkart.virtualglobe.util.ApplicationSettings;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.text.NumberFormatter;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ProgressMonitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.HeadlessException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class SettingsDialog extends JDialog {
  private JComboBox texFilterSettings;
  private JComboBox multisampleSettings;
  private JCheckBox useVBO;
  private JCheckBox useCompressedTextures;

  private JCheckBox useHaze;
  private JCheckBox useSkyColor;

  // private JCheckBox useOcclusion;
  private JFormattedTextField fpsTextField;
  private JFormattedTextField texMemTextField;

  private JCheckBox useCache;
  private JTextField cacheDirField;
  private JFileChooser cacheDirChooser = new JFileChooser();
  private JComboBox cache_size;

  private Universe universe;

  static private final int FPS_MIN = 10;
  static private final int FPS_MAX = 70;
  static private final int TEX_MEM_MIN = 8;
  static private final int TEX_MEM_MAX = 128;

  static private Long   [] cache_size_obj = {
    new Long(400),
    new Long(800),
    new Long(1200),
    new Long(2000),
    new Long(3200),
    new Long(5200),
    new Long(8400),
    new Long(13600)};

  public SettingsDialog(JFrame frame, Universe u) throws HeadlessException {
    super(frame, ApplicationSettings.getApplicationSettings().getResourceString("SETTINGS"));
    universe = u;
    ApplicationSettings settings = ApplicationSettings.getApplicationSettings();

    Box box = Box.createVerticalBox();
    JTabbedPane tabPane = new JTabbedPane();
    tabPane.addTab(settings.getResourceString("CACHE"), new JScrollPane(createCacheSettingsPanel()));
    tabPane.addTab(settings.getResourceString("GRAPHICS"), new JScrollPane(createGraphicsSettingsPanel()));
    tabPane.addTab(settings.getResourceString("ENVIRONMENT"), new JScrollPane(createEnvironmentSettingsPanel()));
    /// tabPane.addTab(settings.getResourceString("SYSTEM"), new JScrollPane(createSystemSettingsPanel()));

    tabPane.setToolTipTextAt(0, settings.getResourceString("TOOL_CACHE"));
    tabPane.setToolTipTextAt(1, settings.getResourceString("TOOL_GRAPHICS"));
    tabPane.setToolTipTextAt(2, settings.getResourceString("TOOL_ENVIRONMENT"));

    box.add(tabPane);
    box.add(new JSeparator());

    Box button_box = Box.createHorizontalBox();
    button_box.add(Box.createHorizontalGlue());
    JButton ok_button = new JButton(settings.getResourceString("OK"));
    ok_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        applySettings();
        setVisible(false);
      }
    });
    button_box.add(ok_button);

    JButton apply_button = new JButton(settings.getResourceString("APPLY"));
    apply_button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent ev) {
       applySettings();
     }
    });
    button_box.add(apply_button);
    JButton close_button = new JButton(settings.getResourceString("CANCEL"));
    close_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        setVisible(false);
      }
    });
    button_box.add(close_button);
    box.add(button_box);

    getContentPane().add(box);
    pack();
  }

  private JPanel createSystemSettingsPanel() {
      JPanel panel = new JPanel(new BorderLayout());
    // Create all internal components
    Box box = Box.createVerticalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    
    
    return panel;
  }
  
  private JPanel createGraphicsSettingsPanel() {
    ApplicationSettings as = ApplicationSettings.getApplicationSettings();
    JPanel panel = new JPanel(new BorderLayout());
    // Create all internal components
    Box box = Box.createVerticalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    texFilterSettings = new JComboBox(as.getTexFilterSettingsStrings());
    texFilterSettings.setSelectedIndex(as.getTexFilterSettings());
    texFilterSettings.setToolTipText(as.getResourceString("TOOL_TEX_FILTER"));
    texFilterSettings.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(texFilterSettings);

    multisampleSettings = new JComboBox(as.getMultisampleSettingsStrings());
    multisampleSettings.setSelectedIndex(as.getMultisampleSettings());
    multisampleSettings.setToolTipText(as.getResourceString("TOOL_MULTISAMPLE"));
    multisampleSettings.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(multisampleSettings);

    //Where the components are created:
    Box fps_box = Box.createHorizontalBox();
    fps_box.add(new JLabel(as.getResourceString("MAX_FPS_LABEL")));
    fps_box.add(Box.createHorizontalGlue());

    java.text.NumberFormat numberFormat =
        java.text.NumberFormat.getIntegerInstance();
    NumberFormatter formatter = new NumberFormatter(numberFormat);
    formatter.setMinimum(new Integer(FPS_MIN));
    formatter.setMaximum(new Integer(FPS_MAX));
    fpsTextField = new JFormattedTextField(formatter);
    fpsTextField.setValue(new Integer(as.getMaxFPS()));
    fpsTextField.setColumns(5); //get some space
    fps_box.add(fpsTextField);
    fps_box.setToolTipText(as.getResourceString("TOOL_MAX_FPS"));
    fps_box.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(fps_box);

    Box tex_mem_box = Box.createHorizontalBox();
    tex_mem_box.add(new JLabel(as.getResourceString("TEX_MEM_LABEL")));
    tex_mem_box.add(Box.createHorizontalGlue());

    formatter = new NumberFormatter(numberFormat);
    formatter.setMinimum(new Integer(TEX_MEM_MIN));
    formatter.setMaximum(new Integer(TEX_MEM_MAX));
    texMemTextField = new JFormattedTextField(formatter);
    texMemTextField.setValue(new Integer(as.getTextureMemMB()));
    texMemTextField.setColumns(5); //get some space
    tex_mem_box.add(texMemTextField);
    tex_mem_box.setToolTipText(as.getResourceString("TOOL_MAX_TEX_MEM"));
    tex_mem_box.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(tex_mem_box);

    useCompressedTextures = new JCheckBox(as.getResourceString("USE_COMPRESSED_TEXTURE"), as.getUseCompressedTexture());
    useCompressedTextures.setToolTipText(as.getResourceString("TOOL_COMPRESS_TEX"));
    useCompressedTextures.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(useCompressedTextures);

    useVBO = new JCheckBox(as.getResourceString("USE_VBO"), as.useVBO());
    useVBO.setToolTipText(as.getResourceString("TOOL_USE_VBO"));
    useVBO.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(useVBO);
/*
    useOcclusion = new JCheckBox(as.getResourceString("USE_OCCLUSION"), as.useOcclusion());
    box.add(useOcclusion);
*/
    panel.add(box);
    return panel;
  }

  private JPanel createCacheSettingsPanel() {
    ApplicationSettings as = ApplicationSettings.getApplicationSettings();
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    // Cache directory
    cacheDirField = new JTextField(20);
    cacheDirField.setToolTipText(as.getResourceString("TOOL_CACHE_DIR"));
    File cache_dir = universe.getCacheDir();
    cacheDirField.setText(cache_dir != null ? cache_dir.toString() : "");

    cacheDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    // Cache enabled
    Box use_box =  Box.createHorizontalBox();
    useCache =
        new JCheckBox(as.getResourceString("CACHE_ENABLED"));
    useCache.setSelected(universe.isCacheEnabled());
    useCache.setToolTipText(as.getResourceString("TOOL_ENABLE_CACHE"));
    useCache.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (useCache.isSelected() && cacheDirField.getText().equals("")) {
          cacheDirChooser.setSelectedFile(null);
          int retval = cacheDirChooser.showDialog(SettingsDialog.this, null);
          if (retval == JFileChooser.APPROVE_OPTION)
            cacheDirField.setText(cacheDirChooser.getSelectedFile().toString());
        }
      }
    });
    use_box.add(useCache);
    use_box.add(Box.createHorizontalGlue());
    JButton clear = new JButton(as.getResourceString("CACHE_DELETE"));
    clear.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (JOptionPane.YES_OPTION !=
            JOptionPane.showOptionDialog(SettingsDialog.this,
            "Delete "+universe.getCacheDir(),
            "Clear Cache",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE, null, null, null))
          return;
        new Thread() {
          public void run() {
            ProgressMonitor prog = new ProgressMonitor(SettingsDialog.this, "Deleting", null, 0, 1);
            prog.setProgress(0);
            universe.clearCache();
            prog.setProgress(1);
            if (universe.isCacheEnabled())
              javax.swing.JOptionPane.showMessageDialog(null,
                  ApplicationSettings.getApplicationSettings().getResourceString("RESTART_MESSAGE"));
          }
        }.start();
      }
    });
    use_box.add(clear);
    use_box.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(use_box);

    Box size_box = Box.createHorizontalBox();
    size_box.add(new JLabel(as.getResourceString("CACHE_SIZE")));
    size_box.add(Box.createHorizontalGlue());
    cache_size = new JComboBox(cache_size_obj);
    long init_cache_size = universe.getCacheManager().getCacheSize();
    for (int i=0; i<cache_size_obj.length; ++i) {
      if (cache_size_obj[i].longValue()*1024*1024 >= init_cache_size) {
        cache_size.setSelectedIndex(i);
        break;
      }
    }
    size_box.add(cache_size);
    size_box.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(size_box);

    Box dir_box = Box.createHorizontalBox();
    dir_box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        as.getResourceString("CACHE_DIR")));
    dir_box.add(cacheDirField);
    final JButton cacheDir = new JButton("...");
    cacheDir.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cacheDirChooser.setSelectedFile(new File(cacheDirField.getText()));
        int retval = cacheDirChooser.showDialog(SettingsDialog.this, null);
        if (retval == JFileChooser.APPROVE_OPTION)
          cacheDirField.setText(cacheDirChooser.getSelectedFile().toString());
      }
    });
    dir_box.add(cacheDir);
    dir_box.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(dir_box);
    panel.add(Box.createVerticalGlue());
    return panel;
  }

  private JPanel createEnvironmentSettingsPanel() {
     ApplicationSettings as = ApplicationSettings.getApplicationSettings();
     JPanel panel = new JPanel(new BorderLayout());
     // Create all internal components
    Box box = Box.createVerticalBox();
    useHaze = new JCheckBox(as.getResourceString("USE_HAZE"), as.getUseHaze());
    useHaze.setToolTipText(as.getResourceString("TOOL_HAZE"));
    box.add(useHaze);
    useSkyColor = new JCheckBox(as.getResourceString("USE_SKY_COLOR"), as.getUseSkyColor());
    useSkyColor.setToolTipText(as.getResourceString("TOOL_SKY_COLOR"));
    box.add(useSkyColor);
    panel.add(box);
    return panel;
  }

  private void applySettings() {
    ApplicationSettings as = ApplicationSettings.getApplicationSettings();
    as.setTexFilterSettings(texFilterSettings.getSelectedIndex());
    as.setMultisampleSettings(multisampleSettings.getSelectedIndex());
    as.setUseHaze(useHaze.isSelected());
    as.setUseSkyColor(useSkyColor.isSelected());
    as.setUseCompressedTexture(useCompressedTextures.isSelected());
    as.setUseVBO(useVBO.isSelected());
    // as.setUseOcclusion(useOcclusion.isSelected());
    as.setMaxFPS(((Integer)fpsTextField.getValue()).intValue());
    as.setTextureMemMB(((Integer)texMemTextField.getValue()).intValue());

    if (cacheDirField.getText().equals(""))
      useCache.setSelected(false);
    universe.setCache(new File(cacheDirField.getText()), useCache.isSelected(), ((Long)cache_size.getSelectedItem()).longValue()*1024*1024);
  }
}