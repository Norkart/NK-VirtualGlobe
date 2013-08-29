//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface.texture;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

import javax.media.opengl.GL;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class DDSImageBuffer extends ImageBuffer {
    private static final long DDSD_MIPMAPCOUNT = 0x00020000l;
    private static final long DDSCAPS_COMPLEX = 0x00000008l;
    private static final long DDSCAPS2_CUBEMAP = 0x00000200l;
    private static final long DDSCAPS2_CUBEMAPSIDE[] = {0x00000400L, 0x00000800L, 0x00001000L, 0x00002000L, 0x00004000L, 0x00008000L};
    
    private static final int HEADER_SIZE = 128;
    
    /** (DDS )            */
    private String filetype;
    /** size of the DDSURFACEDESC structure */
    private int size;
    /** determines what fields are valid*/
    private int flags;
    /** formless optimized surface size */
    private int linearSize;
    /** the depth, if volume texture    */
    private int depth;
    /** number of mip-map levels */
    private int mipMapCount;
    
    /** depth of alpha buffer   */
    private int dwAlphaBitDepth;
    //      DDPIXELFORMAT pixelFormat pixel format ddsheader of the surface
    /** size of structure       */
    private int size2;
    /** pixel format flags      */
    private int flags2;
    /**(FOURCC     )            */
    private String fourCC;
    /** how many bits per pixel */
    private int rgbBitCount;
    /** mask for red bit        */
    private int rBitMask;
    /** mask for green bits     */
    private int gBitMask;
    /** mask for blue bits      */
    private int bBitMask;
    /** mask for alpha channel  */
    private int rgbAlphaBitMask;
    //        DDPIXELFORMAT pixelFormat end
    
    //      DDSCAPS2 ddsCaps direct draw surface capabilities
    private int caps1;
    private int caps2;
    private int caps3;
    /* dwVolumeDepth */
    private int caps4;
    //      DDSCAPS2 end
    private int dwTextureStage;         // stage in multitexture cascade
    
    int reserved1;
    int surface;
    int colorSpaceLowValue;
    int colorSpaceHighValue;
    int destBltColorSpaceLowValue;
    int destBltColorSpaceHighValue;
    int srcOverlayColorSpaceLowValue;
    int srcOverlayColorSpaceHighValue;
    int srcBltColorSpaceLowValue;
    int srcBltColorSpaceHighValue;
    
    private int blocksize;
    private int total_size;
    
    private byte[] fourbytes1 = new byte[4];
    private byte[] fourbytes2 = new byte[4];
    private class Header {
        private void get(ByteBuffer head_buf) throws IOException {
            head_buf.get(fourbytes1);
            filetype = new String(fourbytes1);
            
            size                          = head_buf.getInt();
            flags                         = head_buf.getInt();
            height                        = head_buf.getInt();
            width                         = head_buf.getInt();
            linearSize                    = head_buf.getInt();
            depth                         = head_buf.getInt();
            mipMapCount                   = head_buf.getInt();
            dwAlphaBitDepth               = head_buf.getInt();
            
            reserved1                     = head_buf.getInt();
            surface                       = head_buf.getInt();
            colorSpaceLowValue            = head_buf.getInt();
            colorSpaceHighValue           = head_buf.getInt();
            destBltColorSpaceLowValue     = head_buf.getInt();
            destBltColorSpaceHighValue    = head_buf.getInt();
            srcOverlayColorSpaceLowValue  = head_buf.getInt();
            srcOverlayColorSpaceHighValue = head_buf.getInt();
            srcBltColorSpaceLowValue      = head_buf.getInt();
            srcBltColorSpaceHighValue     = head_buf.getInt();
            
            // DDPIXELFORMAT 2
            size2                         = head_buf.getInt();
            flags2                        = head_buf.getInt();
            head_buf.get(fourbytes2);
            fourCC = new String(fourbytes2);
            rgbBitCount                   = head_buf.getInt();
            rBitMask                      = head_buf.getInt();
            gBitMask                      = head_buf.getInt();
            bBitMask                      = head_buf.getInt();
            rgbAlphaBitMask               = head_buf.getInt();
            // DDCAPS2
            caps1                         = head_buf.getInt();
            caps2                         = head_buf.getInt();
            caps3                         = head_buf.getInt();
            caps4                         = head_buf.getInt();
            // DDCAPS2 end
            dwTextureStage                = head_buf.getInt();
            
            if ((caps1 & (DDSCAPS_COMPLEX)) > 0) {
                if ((caps2 & DDSCAPS2_CUBEMAP) > 0)
                    throw new IOException("DDS image is cube map, can not handle");
            }
            
            if ("DXT1".equals(fourCC)) {
                format = GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                // System.out.println("is dxt1");
            } else if ("DXT3".equals(fourCC)) {
                format = GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                // System.out.println("is dxt3");
            } else if ("DXT5".equals(fourCC)) {
                format = GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                // System.out.println("is dxt5");
            } else if ("DXT4".equals(fourCC)) {
                throw new IOException("DDS dxt4 not supported");
            } else if ("DXT2".equals(fourCC)) {
                throw new IOException("DDS dxt2 not supported");
            } else {
                throw new IOException("DDS unknown format");
            }
            
            
            if (depth==0) depth=1;
            
            blocksize=
                    (format == GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT ||
                    format == GL.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT)?8:16;
            
            
            if (/*((flags & DDSD_MIPMAPCOUNT) == 0) ||*/ mipMapCount == 0)
                mipMapCount = 1;
            
            int mmc = 0;
            total_size = 0;
            for (int w = width, h = height;
            mmc <= mipMapCount && w >= 16 && h >= 16;
            ++mmc, w >>= 1, h >>= 1)
                total_size += ((w + 3) / 4) * ((h + 3) / 4) * ((depth + 3) / 4) * blocksize;
            
            mipMapCount = mmc;
        }
        
        private void put(ByteBuffer head_buf) throws IOException {
            head_buf.put(fourbytes1);
            head_buf.putInt(size);
            head_buf.putInt(flags);
            head_buf.putInt(height);
            head_buf.putInt(width);
            head_buf.putInt(linearSize);
            head_buf.putInt(depth);
            head_buf.putInt(mipMapCount);
            head_buf.putInt(dwAlphaBitDepth);
            
            head_buf.putInt(reserved1);
            head_buf.putInt(surface);
            head_buf.putInt(colorSpaceLowValue);
            head_buf.putInt(colorSpaceHighValue);
            head_buf.putInt(destBltColorSpaceLowValue);
            head_buf.putInt(destBltColorSpaceHighValue);
            head_buf.putInt(srcOverlayColorSpaceLowValue);
            head_buf.putInt(srcOverlayColorSpaceHighValue);
            head_buf.putInt(srcBltColorSpaceLowValue);
            head_buf.putInt(srcBltColorSpaceHighValue);
            
            // DDPIXELFORMAT 2
            head_buf.putInt(size2);
            head_buf.putInt(flags2);
            head_buf.put(fourbytes2);
            head_buf.putInt(rgbBitCount);
            head_buf.putInt(rBitMask);
            head_buf.putInt(gBitMask);
            head_buf.putInt(bBitMask);
            head_buf.putInt(rgbAlphaBitMask);
            // DDCAPS2
            head_buf.putInt(caps1);
            head_buf.putInt(caps2);
            head_buf.putInt(caps3);
            head_buf.putInt(caps4);
            // DDCAPS2 end
            head_buf.putInt(dwTextureStage);
        }
    }
    
    private void fillBuffers() {
        buffers = new ByteBuffer[mipMapCount];
        int prev_sz = 0;
        for (int level = 0, h = height, w = width; level < mipMapCount && (w > 0 || h > 0); level++) {
            if (h == 0) h = 1;
            if (w == 0) w = 1;
            int sz = ((w + 3) / 4) * ((h + 3) / 4) * ((depth + 3) / 4) * blocksize;
            buffer.position(buffer.position()+prev_sz);
            buffers[level] = buffer.slice();
            buffers[level].limit(sz);
            prev_sz = sz;
            h >>= 1;
            w >>= 1;
        }
    }
    
    public DDSImageBuffer(InputStream stream) throws IOException {
        ReadableByteChannel rch = Channels.newChannel(stream);
        ByteBuffer bis = ByteBuffer.allocate(HEADER_SIZE);
        bis.order(ByteOrder.LITTLE_ENDIAN);
        do { rch.read(bis); } while (bis.position() < HEADER_SIZE);
        bis.flip();
        
        Header head = new Header();
        head.get(bis);
        
        allocate(total_size);
        while (rch.read(buffer) >= 0 && buffer.hasRemaining()) Thread.yield();
        buffer.flip();
        
        fillBuffers();
    }
    
    public DDSImageBuffer(InputStream stream, File file) throws IOException {
        ReadableByteChannel rch = Channels.newChannel(stream);
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        do { rch.read(buf); } while (buf.position() < HEADER_SIZE);
        buf.flip();
        
        Header head = new Header();
        head.get(buf);
        
        allocate(total_size);
        while (rch.read(buffer) >= 0 && buffer.hasRemaining()) Thread.yield();
        buffer.flip();
        
        fillBuffers();
        
        if (file == null) return;
        // Write to cache
        try {
            file.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(file);  
            WritableByteChannel wch = Channels.newChannel(os);
            
            buf.rewind();
            do { wch.write(buf); } while (buf.remaining() > 0);
            
            buffer.rewind();
            do {wch.write(buffer); } while (buffer.remaining() > 0);
            
            wch.close();
            os.close();
        } catch (IOException ex) {
            System.err.println("Couldn't write to cache file: " + file);
        }
    }
    
    
    // private FileInputStream fis = null;
    // private FileChannel     chan = null;
    
    public DDSImageBuffer(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        ReadableByteChannel rch = Channels.newChannel(is);
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        do { rch.read(buf); } while (buf.position() < HEADER_SIZE);
        buf.flip();
        
        Header head = new Header();
        head.get(buf);
        
        allocate(total_size);
        while (rch.read(buffer) >= 0 && buffer.hasRemaining()) Thread.yield();
        buffer.flip();
        rch.close();
        
        fillBuffers();
    /*
    fis = new FileInputStream(file);
    chan = fis.getChannel();
    MappedByteBuffer buf = chan.map(FileChannel.MapMode.READ_ONLY,
                              0, (int) file.length());
    buf.order(ByteOrder.LITTLE_ENDIAN);
    buf.load();
     
    Header head = new Header();
    head.get(buf);
     
    buffer = buf.slice();
    fillBuffers();
     */
    }
    
    
    public void clear() {
     /*
     try {
       if (chan != null) {
         buffer = null;
         chan.close();
         chan = null;
       }
       if (fis != null) {
         buffer = null;
         fis.close();
         fis = null;
       }
     }
     catch (IOException e) {
       e.printStackTrace();
     }*/
        super.clear();
    }
}