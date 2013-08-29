//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * CacheUtil.java
 *
 * Created on 3. november 2006, 12:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.cache;

/**
 *
 * @author runaas
 */
public final class CacheUtil {
    
    public static void serializeInt4(int val, byte[] buf, int off) {
        val = val ^ 0x80000000;
        buf[off++] = (byte) ( val >> 24 );
        buf[off++] = (byte) ( val >> 16 );
        buf[off++] = (byte) ( val >> 8 );
        buf[off++] = (byte) val;
    }
    
    public static int deserializeInt4(byte[] buf, int off) {
        int val = ( buf[ off++ ] << 24 )
        | ( ( buf[ off++ ] << 16 ) & 0x00FF0000 )
        | ( ( buf[ off++ ] << 8 ) & 0x0000FF00 )
        | ( ( buf[ off++ ] << 0 ) & 0x000000FF );
        val ^= 0x80000000;
        return val;
    }
    public static void serializeInt8(long val, byte[] buf, int off) {
        val ^= (0x80 << 56);
        buf[off++] = (byte) ( val >> 56 );
        buf[off++] = (byte) ( val >> 48 );
        buf[off++] = (byte) ( val >> 40 );
        buf[off++] = (byte) ( val >> 32 );
        buf[off++] = (byte) ( val >> 24 );
        buf[off++] = (byte) ( val >> 16 );
        buf[off++] = (byte) ( val >> 8 );
        buf[off++] = (byte) val;
    }
    
    public static long deserializeInt8(byte[] buf, int off) {
        long val_up = ( buf[ off++ ] << 24 )
        | ( ( buf[ off++ ] << 16 ) & 0x00FF0000 )
        | ( ( buf[ off++ ] << 8 ) & 0x0000FF00 )
        | ( ( buf[ off++ ] << 0 ) & 0x000000FF );
        long val_down = ( buf[ off++ ] << 24 )
        | ( ( buf[ off++ ] << 16 ) & 0x00FF0000 )
        | ( ( buf[ off++ ] << 8 ) & 0x0000FF00 )
        | ( ( buf[ off++ ] << 0 ) & 0x000000FF );
        long val = (val_up << 32) | val_down;
        val ^= (0x80 << 56);
        return val;
    }
}
