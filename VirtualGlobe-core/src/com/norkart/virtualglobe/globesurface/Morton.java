//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class Morton {
  private static byte [][] m_ix = new byte[16][16];
  static {
    for (int x=0; x<16; ++x) {
      for (int y=0; y<16; ++y) {
        m_ix[x][y] = (byte)comp_code(x,y);
      }
    }
  }
  private static byte comp_code(int x, int y) {
    x &= 0xf;
    y &= 0xf;
    byte result = 0;
    for (int shift = 3; shift >= 0; --shift) {
      result <<= 1;
      result |= ((y >> shift) & 0x1);
      result <<= 1;
      result |= ((x >> shift) & 0x1);
    }

    return result;
  }

    /**
     * Compute bit interleaved Morton code
     * @param xi integer X coordinate
     * @param yi integer Y coordinate
     * @return The bit interleaved Morton code
     */
  static byte[] code(int xi, int yi) {
    xi = xi/4-Integer.MIN_VALUE/4;
    long x = 1<<31; x += xi;
    long y = 1<<31; y += yi;
    byte[] result = null;
    for (int shift = 0, bytes = 8; shift < 32; shift += 4, --bytes) {
      xi = (int)(x >> shift);
      yi = (int)(y >> shift);
      byte b = m_ix[xi&0xf][yi&0xf];
      if (b != 0) {
        if (result == null)
          result = new byte[bytes];
        result[bytes-1] = b;
      }
    }
    return result;
  }

   /**
     * Compute bit interleaved Morton code
     * @param result Store result in this byte array
     * @param xi integer X coordinate
     * @param yi integer Y coordinate
     */
  static void code(int xi, int yi, byte[] result) {
    code(xi, yi, result, 0);
  }

   /**
     * Compute bit interleaved Morton code
     * @param result Store result in this byte array
     * @param off Start position of result in result array
     * @param xi integer X coordinate
     * @param yi integer Y coordinate
     */
  static void code(int xi, int yi, byte[] result, int off) {
    xi = xi/4-Integer.MIN_VALUE/4;
    long x = 1<<31; x += xi;
    long y = 1<<31; y += yi;
    result[off] = 0;
    for (int shift = 0, bytes = 8; shift < 32; shift += 4, --bytes) {
      xi = (int)(x >> shift);
      yi = (int)(y >> shift);
      byte b = m_ix[xi&0xf][yi&0xf];
      result[off+bytes] = b;
      if (b != 0 && result[off] == 0)
        result[off] = (byte)bytes;
    }
  }

    /**
     * Compare Morton code value of two pairs of coordinates
     * @param x1i X value of first point
     * @param y1i Y value of first point
     * @param x2i X value of second point
     * @param y2i Y value of second point
     * @return Integer result, as in an ordinary Java Comparator
     */
  static int compareCode(int x1i, int y1i, int x2i, int y2i) {
    x1i = x1i/4-Integer.MIN_VALUE/4;
    long x1 = 1<<31; x1 += x1i;
    long y1 = 1<<31; y1 += y1i;
    x2i = x2i/4-Integer.MIN_VALUE/4;
    long x2 = 1<<31; x2 += x2i;
    long y2 = 1<<31; y2 += y2i;
    int shift = 0;
    for (; shift < 32; shift += 4) {
      x1i = (int)(x1 >> shift);
      y1i = (int)(y1 >> shift);
      byte b1 = m_ix[x1i&0xf][y1i&0xf];
      x2i = (int)(x2 >> shift);
      y2i = (int)(y2 >> shift);
      byte b2 = m_ix[x2i&0xf][y2i&0xf];
      if (b1 != 0 && b2 != 0) break;
      if (b1 != 0) return  1;
      if (b2 != 0) return -1;
    }
    int end_shift = shift;
    for (shift = 28; shift >= end_shift; shift -= 4) {
      x1i = (int)(x1 >> shift);
      y1i = (int)(y1 >> shift);
      byte b1 = m_ix[x1i&0xf][y1i&0xf];
      x2i = (int)(x2 >> shift);
      y2i = (int)(y2 >> shift);
      byte b2 = m_ix[x2i&0xf][y2i&0xf];
      if ((0xff&b1) > (0xff&b2)) return  1;
      if ((0xff&b1) < (0xff&b2)) return -1;
    }
    return 0;
  }
}