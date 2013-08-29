//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;



import com.norkart.virtualglobe.util.ApplicationSettings;
import java.util.ArrayList;
import java.util.Iterator;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.norkart.virtualglobe.globesurface.texture.ImageBuffer;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Texture2D extends GLCleanup {
    static ArrayList free_list = new ArrayList();
    
  protected int textureId = -1;
  protected static boolean checked_compressed_texture = false;

  protected ImageBuffer imageBuffer = null;
  static protected int tex_memory = 0;
  protected int        memsize = 0;
  protected int width, height, format;

  public static Texture2D createTexture(ImageBuffer ib) {
      synchronized (free_list) {
        Iterator it = free_list.iterator();
        while (it.hasNext()) {
            Texture2D tx = (Texture2D)it.next();
            if (tx.width == ib.getWidth() && tx.height == ib.getHeight() && 
                tx.format == ib.getFormat()) 
             {
                // System.out.println("Re-using texture");
                it.remove();
                tx.imageBuffer = ib;
                tx.requestInit();
                return tx;
            }
        }
      }
      return new Texture2D(ib);
  }
  
  public void deleteTexture() {
      synchronized (free_list) {
          if (free_list.size() >= 16) {
              Texture2D tx = (Texture2D)free_list.remove(0);
              tx.requestCleanup();
          }
          free_list.add(this);
      }
  }
  
  private Texture2D(ImageBuffer ib) {
    imageBuffer = ib;
    width  = ib.getWidth();
    height = ib.getHeight();
    format = ib.getFormat();
    requestInit();
  }

  public ImageBuffer getImageBuffer() {
    return imageBuffer;
  }

  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }

  static public int getTexMemory() {
    return tex_memory;
  }
  
  

   public  void requestCleanup() {
     synchronized (this) {
     if (imageBuffer != null) {
       imageBuffer.clear();
       imageBuffer = null;
     }
     tex_memory -= memsize;
     memsize = 0;
     }
     if(textureId >= 0)
       super.requestCleanup();
  }


  protected void cleanup(GL gl, GLU glu) {
    if(textureId >= 0) {
      int tex_id_tmp[] = { textureId };
      gl.glDeleteTextures(1, tex_id_tmp, 0);
      textureId = -1;
    }
  }


  public void render(GL gl, GLU glu) {
    if(textureId >= 0) {
      // Bind an old texture
      gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
      /*
      if (request_buffer) {
        int pixel_bytes = 0;
        if (srcPixelFormat == GL.GL_RGBA)
          pixel_bytes = 4;
        else if (srcPixelFormat == GL.GL_RGB)
          pixel_bytes = 3;
        imageBuffer = new ByteBuffer[1];
        imageBuffer[0] = ByteBuffer.allocateDirect(pixel_bytes*width*height);
        imageBuffer[0].order(ByteOrder.nativeOrder());
        gl.glGetTexImage(GL.GL_TEXTURE_2D, 0,
                         srcPixelFormat,
                         GL.GL_UNSIGNED_BYTE,
                         imageBuffer[0]);
        request_buffer = false;
      }
      */
      return;
    }
    init(gl, glu);
  }

  protected  boolean init(GL gl, GLU glu) {
    synchronized (this) {
   
    if (imageBuffer == null) {
      // request_buffer = true;
      return false;
    }

    // Bind a new texture
    boolean is_old = true;
    if(textureId < 0) {
        int[] tex_id_tmp = new int[1];
        gl.glGenTextures(1, tex_id_tmp, 0);
        textureId = tex_id_tmp[0];
        is_old = false;
    }
    
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);

    GLSettings glCap = GLSettings.get(gl);
    ApplicationSettings as = ApplicationSettings.getApplicationSettings();

    // Set texture parameters
    gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

    gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );

    gl.glTexParameteri(GL.GL_TEXTURE_2D,
                       GL.GL_TEXTURE_WRAP_S,
                       glCap.edgeClampCommand());

    gl.glTexParameteri(GL.GL_TEXTURE_2D,
                       GL.GL_TEXTURE_WRAP_T,
                       glCap.edgeClampCommand());

    if (as.getTexFilterSettings() == as.TEXFILTER_SETTINGS_NEAREST)
      gl.glTexParameteri(GL.GL_TEXTURE_2D,
                         GL.GL_TEXTURE_MAG_FILTER,
                         GL.GL_NEAREST);
    else
      gl.glTexParameteri(GL.GL_TEXTURE_2D,
                         GL.GL_TEXTURE_MAG_FILTER,
                         GL.GL_LINEAR);

    boolean useMipmap = true;
    if (imageBuffer.getBuffers().length == 1 &&
        !glCap.hasGenerateMipmap() &&
        (imageBuffer.getFormat() == GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT ||
         imageBuffer.getFormat() == GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT ||
         imageBuffer.getFormat() == GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT))
      useMipmap = false;

    if (useMipmap) {
      if (as.getTexFilterSettings() == as.TEXFILTER_SETTINGS_NEAREST)
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_MIN_FILTER,
                           GL.GL_NEAREST_MIPMAP_NEAREST );
      else if (as.getTexFilterSettings() >= as.TEXFILTER_SETTINGS_BILINEAR) {
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_MIN_FILTER,
                           GL.GL_LINEAR_MIPMAP_LINEAR );
        if (as.getTexFilterSettings() > as.TEXFILTER_SETTINGS_BILINEAR &&
            glCap.hasAnisotropic()) {
          float max_aniso = (1 << (as.getTexFilterSettings() - as.TEXFILTER_SETTINGS_BILINEAR));
          if (max_aniso > glCap.maxAnisotropic())
            max_aniso = glCap.maxAnisotropic();
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_aniso);
        }
      }
    }
    else if (as.getTexFilterSettings() == as.TEXFILTER_SETTINGS_NEAREST)
      gl.glTexParameteri(GL.GL_TEXTURE_2D,
                         GL.GL_TEXTURE_MIN_FILTER,
                         GL.GL_NEAREST);
    else
      gl.glTexParameteri(GL.GL_TEXTURE_2D,
                         GL.GL_TEXTURE_MIN_FILTER,
                         GL.GL_LINEAR);
    tex_memory -= memsize;
    memsize = 0;
    switch (imageBuffer.getFormat()) {
     case GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
     case GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
     case GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
     {
       if (glCap.hasCompressedTexture()) {
         if (imageBuffer.getBuffers().length == 1) {
           if (glCap.hasGenerateMipmap())
             gl.glTexParameteri(GL.GL_TEXTURE_2D, glCap.generateMipmapCommand(), GL.GL_TRUE);

         }
         else gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_LEVEL, imageBuffer.getBuffers().length-1);

         int w = imageBuffer.getWidth();
         int h = imageBuffer.getHeight();

         for (int i = 0; i < imageBuffer.getBuffers().length && (w > 0 || h > 0); ++i) {
           if (w == 0) w = 1;
           if (h == 0) h = 1;
           if (is_old)
             gl.glCompressedTexSubImage2D(GL.GL_TEXTURE_2D, i, 0,0, w, h,
                                          imageBuffer.getFormat(), 
                                          imageBuffer.getBuffers()[i].limit(),
                                          imageBuffer.getBuffers()[i]);
           else
             gl.glCompressedTexImage2D(GL.GL_TEXTURE_2D, i, imageBuffer.getFormat(), w, h,
                                      0, imageBuffer.getBuffers()[i].limit(),
                                      imageBuffer.getBuffers()[i]);
           memsize += imageBuffer.getBuffers()[i].limit();
           w >>= 1;
           h >>= 1;
         }
       }
       else if (!checked_compressed_texture) {
         checked_compressed_texture = true;
         javax.swing.JOptionPane.showMessageDialog(null,
             as.getResourceString("RECONFIGURE_MESSAGE"));
         as.setUseCompressedTexture(false);
       }
     } break;
     case GL.GL_RGB:
     case GL.GL_RGBA:
       if (glCap.hasGenerateMipmap()) {
         gl.glTexParameteri(GL.GL_TEXTURE_2D, glCap.generateMipmapCommand(), GL.GL_TRUE);
         if (is_old)
             gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0,
                                imageBuffer.getWidth(), imageBuffer.getHeight(), imageBuffer.getFormat(),
                                GL.GL_UNSIGNED_BYTE,
                                imageBuffer.getBuffers()[0]);
         else
         gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, imageBuffer.getFormat(),
                         imageBuffer.getWidth(), imageBuffer.getHeight(), 0, imageBuffer.getFormat(),
                         GL.GL_UNSIGNED_BYTE,
                         imageBuffer.getBuffers()[0]);
       }
       else {
         glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, imageBuffer.getFormat(),
                               imageBuffer.getWidth(), imageBuffer.getHeight(), imageBuffer.getFormat(),
                               GL.GL_UNSIGNED_BYTE,
                               imageBuffer.getBuffers()[0]);
       }
       memsize = (int)(imageBuffer.getBuffers()[0].limit()*1.33);
    }
    tex_memory += memsize;
    imageBuffer.clear();
    imageBuffer = null;
    return true;
    }
  }
}