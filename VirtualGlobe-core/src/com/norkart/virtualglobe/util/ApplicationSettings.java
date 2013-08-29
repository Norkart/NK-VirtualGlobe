//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.util;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ApplicationSettings {
    static private ApplicationSettings settings;
    static public ApplicationSettings getApplicationSettings() {
        if (settings == null)
            settings = new ApplicationSettings();
        return settings;
    }
    
    // Graphic settings
    static final private String PREF_GRAPHIC_SETTINGS     = "graphicSettings";
    static final private String PREF_TEXFILTER_SETTINGS   = "texFilterSettings";
    static final private String PREF_MULTISAMPLE_SETTINGS = "multisampleSettings";
    
/*
  static final public int GRAPHIC_SETTINGS_NEAREST     = 0;
  static final public int GRAPHIC_SETTINGS_BILINEAR    = 1;
  static final public int GRAPHIC_SETTINGS_TRILINEAR   = 2;
  static final public int GRAPHIC_SETTINGS_AF_2        = 3;
  static final public int GRAPHIC_SETTINGS_AF_4_AA_2   = 4;
  static final public int GRAPHIC_SETTINGS_AF_8_AA_4   = 5;
  static final public int GRAPHIC_SETTINGSR_AF_16_AA_6 = 6;
 */
    static final public int TEXFILTER_SETTINGS_NEAREST  = 0;
    static final public int TEXFILTER_SETTINGS_BILINEAR = 1;
    static final public int TEXFILTER_SETTINGS_AF_2     = 2;
    static final public int TEXFILTER_SETTINGS_AF_4     = 3;
    static final public int TEXFILTER_SETTINGS_AF_8     = 4;
    static final public int TEXFILTER_SETTINGS_AF_16    = 5;
    static final public int TEXFILTER_SETTINGS_AF_32    = 6;
    
    static final public int MULTISAMPLE_NONE = 0;
    static final public int MULTISAMPLE_AA_2 = 1;
    static final public int MULTISAMPLE_AA_4 = 2;
    static final public int MULTISAMPLE_AA_6 = 3;
    static final public int MULTISAMPLE_AA_8 = 4;
    
    static final private String[] texFilterSettingsStrings =
    {
        "Nearest",
        "Bilinear",
        "Anisotropic X2",
        "Anisotropic X4",
        "Anisotropic X8",
        "Anisotropic X16",
        "Anisotropic X32",
    };
    static final private String[] multisampleSettingsStrings =
    {
        "No antialiasing",
        "Antialiasing X2",
        "Antialiasing X4",
        "Antialiasing X6",
        "Antialiasing X8",
    };
    private int texFilterSettings = TEXFILTER_SETTINGS_AF_4;
    private int multisampleSettings = MULTISAMPLE_AA_2;
    
    static final private String PREF_USE_COMPRESSED_TEXTURE = "useCompressedTexture";
    private boolean useCompressedTexture = true;
    
    // Environmental effects
    static final private String PREF_USE_HAZE = "useHaze";
    private boolean useHaze = true;
    static final private String PREF_USE_SKY_COLOR = "useSkyColor";
    private boolean useSkyColor = true;
    
    static final private String PREF_USE_VBO = "useVBO";
    private boolean use_vbo = true;
    
    static final private String PREF_MAX_FPS = "maxFPS";
    private int max_fps = 25;
    
    static final private String PREF_TEXTURE_MEM_MB = "texMemMB";
    private int texture_mem_MB = 64;
    
    static final private String PREF_NODE_NAME     = "/com/norkart/VirtualGlobe";
    static final private String PREF_NODE_OLD_NAME = "/com/sintef/VirtualGlobe";
    
    Preferences preferences = null;
    // private boolean use_occlusion = false;
    
    // Resources for internationalization
    
    
    private ResourceBundle resources = ResourceBundle.getBundle("com.norkart.virtualglobe.resources.TextResources");
    
    private ApplicationSettings() {
        try {
            Preferences user_root = Preferences.userRoot();
            
            Preferences old_pref = null;
            if (user_root.nodeExists(PREF_NODE_OLD_NAME)) {
                old_pref = user_root.node(PREF_NODE_OLD_NAME);
                getPreferences(old_pref);
            }
            
            preferences = user_root.node(PREF_NODE_NAME);
            getPreferences(preferences);
            
            if (old_pref != null) {
                old_pref.removeNode();
                putPreferences(preferences);
            }
            
        } catch (Exception ex) {
            System.err.println("Error in Application Settings: ");
            ex.printStackTrace();
        }
    }
    
    private void getPreferences(Preferences prefs) {
        int graphicSettings = prefs.getInt(PREF_GRAPHIC_SETTINGS, -1);
        if (graphicSettings >= 0) {
            prefs.remove(PREF_GRAPHIC_SETTINGS);
            texFilterSettings = graphicSettings > 1 ? graphicSettings-1 : graphicSettings;
            multisampleSettings = graphicSettings > 3 ? graphicSettings-3 : 0;
            prefs.putInt(PREF_TEXFILTER_SETTINGS, texFilterSettings);
            prefs.putInt(PREF_MULTISAMPLE_SETTINGS, multisampleSettings);
        } else {
            texFilterSettings =  prefs.getInt(PREF_TEXFILTER_SETTINGS, texFilterSettings);
            multisampleSettings =  prefs.getInt(PREF_MULTISAMPLE_SETTINGS, multisampleSettings);
        }
        useCompressedTexture = prefs.getBoolean(PREF_USE_COMPRESSED_TEXTURE, useCompressedTexture);
        useHaze = prefs.getBoolean(PREF_USE_HAZE, useHaze);
        useSkyColor = prefs.getBoolean(PREF_USE_SKY_COLOR, useSkyColor);
        use_vbo  = prefs.getBoolean(PREF_USE_VBO, use_vbo);
        max_fps = prefs.getInt(PREF_MAX_FPS, max_fps);
        texture_mem_MB = prefs.getInt(PREF_TEXTURE_MEM_MB, texture_mem_MB);
    }
    
    private void putPreferences(Preferences prefs) {
        prefs.putInt(PREF_TEXFILTER_SETTINGS, texFilterSettings);
        prefs.putInt(PREF_MULTISAMPLE_SETTINGS, multisampleSettings);
        prefs.putBoolean(PREF_USE_COMPRESSED_TEXTURE, useCompressedTexture);
        prefs.putBoolean(PREF_USE_HAZE, useHaze);
        prefs.putBoolean(PREF_USE_SKY_COLOR, useSkyColor);
        prefs.putBoolean(PREF_USE_VBO, use_vbo);
        prefs.putInt(PREF_MAX_FPS, max_fps);
        prefs.putInt(PREF_TEXTURE_MEM_MB, texture_mem_MB);
    }
    
    public String getResourceString(String key) {
        try {
            return resources.getString(key);
        } catch (Exception ex) {}
        return key;
    }
    
    public String getGlobeStarterURLString() {
        return "http://www.virtual-globe.info/VirtualGlobeStarter.php?";
    }
    
    public int getTexFilterSettings() {
        return texFilterSettings;
    }
    
    public int getMultisampleSettings() {
        return multisampleSettings;
    }
    
    public void setMultisampleSettings(int gs) {
        if (gs == multisampleSettings) return;
        multisampleSettings = gs;
        preferences.putInt(PREF_MULTISAMPLE_SETTINGS, multisampleSettings);
        javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    
    public void setTexFilterSettings(int gs) {
        if (gs == texFilterSettings) return;
        texFilterSettings = gs;
        preferences.putInt(PREF_TEXFILTER_SETTINGS, texFilterSettings);
        javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    
    public String[] getTexFilterSettingsStrings() {
        return texFilterSettingsStrings;
    }
    public String[] getMultisampleSettingsStrings() {
        return multisampleSettingsStrings;
    }
    
    public void setUseCompressedTexture(boolean useCompressedTexture) {
        if (this.useCompressedTexture == useCompressedTexture) return;
        this.useCompressedTexture = useCompressedTexture;
        preferences.putBoolean(PREF_USE_COMPRESSED_TEXTURE, useCompressedTexture);
        javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    public boolean getUseCompressedTexture() {
        return useCompressedTexture;
    }
    
    public void setUseHaze(boolean useHaze) {
        if (this.useHaze == useHaze) return;
        this.useHaze = useHaze;
        preferences.putBoolean(PREF_USE_HAZE, useHaze);
    }
    public boolean getUseHaze() {
        return useHaze;
    }
    
    public void setUseSkyColor(boolean useSkyColor) {
        if (this.useSkyColor == useSkyColor) return;
        this.useSkyColor = useSkyColor;
        preferences.putBoolean(PREF_USE_SKY_COLOR, useSkyColor);
    }
    public boolean getUseSkyColor() {
        return useSkyColor;
    }
    
    public void setUseVBO(boolean use_vbo) {
        if (this.use_vbo == use_vbo) return;
        this.use_vbo = use_vbo;
        preferences.putBoolean(PREF_USE_VBO, use_vbo);
        // javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    
    public boolean useVBO() {
        return use_vbo;
    }
/*
  public boolean useOcclusion() {
    return use_occlusion;
  }
  public void setUseOcclusion(boolean b) {
    use_occlusion = b;
  }
 */
    public void setMaxFPS(int max_fps) {
        if (this.max_fps == max_fps) return;
        this.max_fps = max_fps;
        preferences.putInt(PREF_MAX_FPS, max_fps);
        // javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    
    public int getMaxFPS() {
        return max_fps;
    }
    
    public void setTextureMemMB(int tex_mem) {
        if (this.texture_mem_MB == tex_mem) return;
        this.texture_mem_MB = tex_mem;
        preferences.putInt(PREF_TEXTURE_MEM_MB, tex_mem);
        // javax.swing.JOptionPane.showMessageDialog(null, getResourceString("RESTART_MESSAGE"));
    }
    
    public int getTextureMemMB() {
        return texture_mem_MB;
    }
}


