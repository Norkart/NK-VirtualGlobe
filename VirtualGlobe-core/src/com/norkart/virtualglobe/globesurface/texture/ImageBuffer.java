//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface.texture;

import java.nio.ByteBuffer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ImageBuffer {
  private static ArrayList free = new ArrayList();
  private static int allocated_sz = 0;

  protected int width, height;
  protected int format;
  protected ByteBuffer buffer;
  protected ByteBuffer[] buffers;

  final public int getWidth()  { return width; }
  final public int getHeight() { return height; }
  final public int getFormat() { return format; }

  final public ByteBuffer[] getBuffers() { return buffers; }

  public static synchronized int getBufferMemory() {
    return allocated_sz;
  }

  protected ByteBuffer allocate(int sz) {
    synchronized (getClass()) {
      /*
      while (max_allocated_sz + 5*sz < allocated_sz) {
        try { getClass().wait(); }
        catch (InterruptedException ex) {}
      } */
      allocated_sz += sz;
    }

    synchronized (free) {
      Iterator it = free.iterator();
     while (it.hasNext()) {
        WeakReference w = (WeakReference)it.next();
        Object o = w.get();
        if (o == null) {
          it.remove();
        }
        else {
          ByteBuffer bb = (ByteBuffer)o;
          if (bb.capacity() == sz) {
            w.clear();
            it.remove();
            // System.err.println("buffer recirculated " + sz);
            return buffer = bb;
          }
        }
      }
    }

    return buffer = ByteBuffer.allocate(sz);
  }

  public void clear() {
    if (buffers != null) {
      for (int i=0; i< buffers.length; ++i) {
        if (buffers[i] != null) {
          buffers[i].clear();
          buffers[i] = null;
        }
      }
    }
    if (buffer != null) {
      synchronized (getClass()) {
        allocated_sz -= buffer.capacity();
//        getClass().notifyAll();
      }
      buffer.clear();
      synchronized (free) {
        free.add(new WeakReference(buffer));
      }
    }
    buffers = null;
    buffer = null;
    width = height = format = 0;
  }
}