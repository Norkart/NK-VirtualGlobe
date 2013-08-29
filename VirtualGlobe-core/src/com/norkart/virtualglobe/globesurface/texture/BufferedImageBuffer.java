//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface.texture;

import java.util.Hashtable;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class BufferedImageBuffer extends ImageBuffer {

  static private ColorModel glAlphaColorModel;
  static private ColorModel glColorModel;

  static {
    glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
        new int[] {8,8,8,8},
        true,
        false,
        ComponentColorModel.TRANSLUCENT,
        DataBuffer.TYPE_BYTE);

    glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
        new int[] {8,8,8,0},
        false,
        false,
        ComponentColorModel.OPAQUE,
        DataBuffer.TYPE_BYTE);
  }

  static public int get2Fold(int fold) {
    int ret = 2;
    while (ret < fold) {
      ret *= 2;
    }
    return ret;
  }

  public BufferedImageBuffer(File file) throws IOException {
    BufferedImage img = ImageIO.read(file);
    if (img == null)
      throw new IOException("File not readable: " + file);
    createFromImage(img);
  }

  public BufferedImageBuffer(InputStream stream) throws IOException {
    BufferedImage img = ImageIO.read(stream);
    if (img == null)
      throw new IOException("Image stream not readable");
    createFromImage(img);
  }

  public BufferedImageBuffer(InputStream stream, File file) throws IOException {
    if (file != null) {
      file.getParentFile().mkdirs();
      RandomAccessFile f = new RandomAccessFile(file, "rw");
      f.setLength(0);
      f.seek(0);

      // Copying
      ReadableByteChannel source = Channels.newChannel(stream);
      FileChannel destination = f.getChannel();

      ByteBuffer buf = ByteBuffer.allocate(1024);
      // Read from source file into the byte buffer using the source file channel.
      while (source.read(buf) != -1) { // EOF?
        // Prepare to drain the buffer
        buf.flip();
          // Drain the buffer using the destination file channel
        while (buf.hasRemaining()) {
          destination.write(buf);
        }
        // Clear the buffer for reuse
        buf.clear();
      }
      // Completing
      source.close();
      destination.force(true);
      
      f.seek(0);
      stream = new FileInputStream(f.getFD());
    }

    BufferedImage img = ImageIO.read(stream);
    if (img == null)
      throw new IOException("Image stream not readable");
    createFromImage(img);
  }

  public BufferedImageBuffer(BufferedImage bufferedImage) {
    createFromImage(bufferedImage);
  }

  private void createFromImage(BufferedImage bufferedImage) {
   // Flip Image
   /*
   AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
   tx.translate(0, -bufferedImage.getHeight(null));
   AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
   bufferedImage = op.filter(bufferedImage, null);
   */

   // Set texture data
   width  = get2Fold(bufferedImage.getWidth());
   height = get2Fold(bufferedImage.getHeight());

   if (bufferedImage.getHeight() != height ||
       bufferedImage.getWidth()  != width)
     throw new IllegalArgumentException("Non power of two width and height in texture");

   int[] pixel_tmp = new int[width];

   switch (bufferedImage.getType()) {
     case BufferedImage.TYPE_3BYTE_BGR:
     case BufferedImage.TYPE_INT_RGB:
     case BufferedImage.TYPE_USHORT_555_RGB:
     case BufferedImage.TYPE_USHORT_565_RGB:
       format         = GL.GL_RGB;

       buffers = new ByteBuffer[1];
       buffers[0] = allocate(width * height * 3);
       for(int i = height - 1; i >= 0; --i) {
         bufferedImage.getRGB(0, i, width, 1, pixel_tmp, 0, width);
         for(int j = 0; j < width; j++) {
           int tmp = pixel_tmp[j];

           buffers[0].put((byte)((tmp >> 16) & 0xFF));
           buffers[0].put((byte)((tmp >> 8) & 0xFF));
           buffers[0].put((byte)(tmp & 0xFF));
         }
       }
       break;
     case BufferedImage.TYPE_4BYTE_ABGR:
     case BufferedImage.TYPE_INT_ARGB:
       // OpenGL wants RGBA, so swap the byte order around as part of
       // the conversion process.
       format         = GL.GL_RGBA;

       buffers = new ByteBuffer[1];
       buffers[0] = allocate(width * height * 4);

       for(int i = height - 1; i >= 0; --i) {
         bufferedImage.getRGB(0, i, width, 1, pixel_tmp, 0, width);

         for(int j = 0; j < width; j++) {
           int tmp = pixel_tmp[j];
           buffers[0].put((byte)((tmp >> 16) & 0xFF));
           buffers[0].put((byte)((tmp >> 8) & 0xFF));
           buffers[0].put((byte)(tmp & 0xFF));
           buffers[0].put((byte)((tmp >> 24) & 0xFF));
         }
       }
       break;
     case BufferedImage.TYPE_BYTE_INDEXED:
     case BufferedImage.TYPE_CUSTOM:
     case BufferedImage.TYPE_BYTE_BINARY:
       // Force the format change.
       ColorModel cm = bufferedImage.getColorModel();
       int num_comp = cm.getNumComponents();
       boolean has_alpha = cm.hasAlpha();

       switch(num_comp) {
         case 2:
           format         = GL.GL_LUMINANCE_ALPHA;
           buffers = new ByteBuffer[1];
           buffers[0] = allocate(width * height * 2);

           for(int i = height - 1; i >= 0; --i) {
             bufferedImage.getRGB(0, i, width, 1, pixel_tmp, 0, width);

             for(int j = 0; j < width; j++) {
               int tmp = pixel_tmp[j];

               buffers[0].put((byte)((tmp >> 8) & 0xFF));
               buffers[0].put((byte)(tmp & 0xFF));
             }
           }
           break;

         case 3:
           format         = GL.GL_RGB;

           buffers = new ByteBuffer[1];
           buffers[0] = allocate(width * height * 3);

           for(int i = height - 1; i >=0; --i) {
             bufferedImage.getRGB(0, i, width, 1, pixel_tmp, 0, width);

             for(int j = 0; j < width; j++) {
               int tmp = pixel_tmp[j];

               buffers[0].put((byte)((tmp >> 16) & 0xFF));
               buffers[0].put((byte)((tmp >> 8) & 0xFF));
               buffers[0].put((byte)(tmp & 0xFF));
             }
           }
           break;

         case 4:
           format         = GL.GL_RGBA;

           buffers = new ByteBuffer[1];
           buffers[0] = allocate(width * height * 4);

           for(int i = height - 1; i >= 0; --i) {
             bufferedImage.getRGB(0, i, width, 1, pixel_tmp, 0, width);

             for(int j = 0; j < width; j++) {
               int tmp = pixel_tmp[j];

               buffers[0].put((byte)((tmp >> 16) & 0xFF));
               buffers[0].put((byte)((tmp >> 8) & 0xFF));
               buffers[0].put((byte)(tmp & 0xFF));
               buffers[0].put((byte)((tmp >> 24) & 0xFF));
             }
           }
           break;
       }
       break;
   }


   if (buffers == null) {
     // Fallback method that writes the image into an image with known structure
     int pixel_size;
     ColorModel cm;
     if (bufferedImage.getColorModel().hasAlpha()) {
       pixel_size     = 4;
       format         = GL.GL_RGBA;
       cm             = glAlphaColorModel;
     }
     else {
       pixel_size     = 3;
       format         = GL.GL_RGB;
       cm             = glColorModel;
     }
     WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, pixel_size, null);
     BufferedImage texImage = new BufferedImage(cm, raster, false, new Hashtable());
     Graphics g = texImage.getGraphics();
     g.drawImage(bufferedImage,0,0,null);

     byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
     buffers = new ByteBuffer[1];
     buffers[0] = allocate(data.length);

     for (int i = height-1; i >= 0; --i)
       buffers[0].put(data, i*width*pixel_size, width*pixel_size);
   }
   buffers[0].rewind();
   bufferedImage = null;
  }
}
