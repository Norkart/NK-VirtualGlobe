//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;


import java.util.HashMap;
import java.util.prefs.*;
import java.lang.Float;
import javax.swing.JOptionPane;

import javax.media.opengl.GL;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class GLSettings {
  static private HashMap glCapMap = new HashMap(4);

  static public GLSettings get(GL gl) {
    GLSettings c = (GLSettings)glCapMap.get(gl);
    if (c != null) return c;
    c = new GLSettings();
    glCapMap.put(gl, c);
    String version = gl.glGetString(GL.GL_VERSION);
    String extensions = gl.glGetString(GL.GL_EXTENSIONS);
    if (version.compareTo("1.1") < 0) {
      JOptionPane.showMessageDialog(null,
                                    "Your computer has a very weak or outdated 3D graphics system\n" +
                                    "and may be unable to run the Virtual Globe.\n" +
                                    "This may be wrongly reported if your graphics drivers are out of date.\n" +
                                    "Try to upgrade the graphics drivers and then try again." +
                                    "If this doesn't help, you need a hardware upgrade.",
                                    "Weak 3D graphics system capabilities", JOptionPane.WARNING_MESSAGE);
    }

    if (version.compareTo("1.2") >= 0)
        c.hasRangeElements = true;
    else
        c.hasRangeElements = false;

    /**
     * Check separate specular color
     */
    if (version.compareTo("1.2") >= 0) {
      c.hasSeparateSpecularColor = true;
      c.separateSpecularCommand = GL.GL_SEPARATE_SPECULAR_COLOR;
    }
    else {
      c.hasSeparateSpecularColor = false;
      c.separateSpecularCommand = -1;
    }

    /**
     * Check edge clamp
     */
    if (version.compareTo("1.2") >= 0) {
      c.edgeClampCommand = GL.GL_CLAMP_TO_EDGE;
    }
    else {
      c.edgeClampCommand = GL.GL_CLAMP;
    }

    /**
     * Check mipmap generation
     */
    if (version.compareTo("1.4") >= 0) {
      c.hasGenerateMipmap = true;
      c.generateMipmapCommand = GL.GL_GENERATE_MIPMAP;
    }
    else if (extensions.indexOf("GL_SGIS_generate_mipmap") >= 0) {
      c.hasGenerateMipmap = true;
      c.generateMipmapCommand = GL.GL_GENERATE_MIPMAP_SGIS;
    }
    else
      c.hasGenerateMipmap = false;

    /**
     * Check anisotropic filtering
     */
    if (extensions.indexOf("GL_EXT_texture_filter_anisotropic") >= 0) {
      c.hasAnisotropic = true;
      float [] fv = new float[1];
      gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, fv, 0);
      c.maxAnisotropic = fv[0];
    }
    else
      c.hasAnisotropic = false;


    /**
     * Check texture compression
     */
    if (extensions.indexOf("GL_EXT_texture_compression_s3tc") >= 0)
      c.hasCompressedTexture = true;
    else
      c.hasCompressedTexture = false;

    if (c.hasCompressedTexture && version.compareTo("1.3") >= 0)
      c.useTextureCompressionARB = false;
    else if (c.hasCompressedTexture && extensions.indexOf("GL_ARB_texture_compression") >= 0)
      c.useTextureCompressionARB = true;
    else
      c.hasCompressedTexture = false;
    /*
     * Check VBOs
     */
    c.VBO_type = NONE;
    if (extensions.indexOf("GL_ARB_vertex_buffer_object") >= 0)
        c.VBO_type = ARB;
    else if (version.compareTo("1.5") >= 0)
        c.VBO_type = STANDARD;

    /*
     * Check occlusion query
     */
    int [] q = new int[1];
    c.occlusion_query_type = NONE;
    /*
    if (version.compareTo("1.5") >= 0) {
      gl.glGetQueryiv(GL.GL_SAMPLES_PASSED, GL.GL_QUERY_COUNTER_BITS, q, 0);
      if (q[0] > 0)
        c.occlusion_query_type = STANDARD;
    }
    else if (extensions.indexOf("GL_NV_occlusion_query") >= 0) {
      c.occlusion_query_type = NV;
    }
*/
    return c;
  }

  private boolean hasGenerateMipmap;
  private int generateMipmapCommand;
  private boolean hasCompressedTexture;
  private boolean useTextureCompressionARB;
  private boolean hasSeparateSpecularColor;
  private boolean hasRangeElements;
  private int separateSpecularCommand;
  private int edgeClampCommand;
  private boolean hasAnisotropic;
  private float maxAnisotropic;
  private int VBO_type;
  private int occlusion_query_type;

  public static final int NONE     = 0;
  public static final int STANDARD = 1;
  public static final int ARB      = 2;
  public static final int NV       = 3;

  public boolean hasCompressedTexture() {
    return hasCompressedTexture;
  }
  public boolean useTextureCompressionARB() {
    return useTextureCompressionARB;
  }

  public boolean hasSeparateSpecularColor() {
    return hasSeparateSpecularColor;
  }

  public boolean hasRangeElements() {
    return hasRangeElements;
  }

  public int separateSpecularCommand() {
    return separateSpecularCommand;
  }

  public int edgeClampCommand() {
    return edgeClampCommand;
  }
  public boolean hasGenerateMipmap() {
     return hasGenerateMipmap;
  }
  public int generateMipmapCommand() {
    return generateMipmapCommand;
  }
  public boolean hasAnisotropic() {
    return hasAnisotropic;
  }
  public float maxAnisotropic() {
    return maxAnisotropic;
  }

  public int getVBOType() {
    return VBO_type;
  }

  public int getOcclusionQuery() {
    return occlusion_query_type;
  }
}