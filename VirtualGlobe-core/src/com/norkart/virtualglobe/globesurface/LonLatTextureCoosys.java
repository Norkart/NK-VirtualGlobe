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

class LonLatTextureCoosys extends TextureCoosys {
  LonLatTextureCoosys(BttSurface surface) {
    super(surface);
  }

  void computeTexCoo(int node, int[] int_lonlat, int[] int_texcoo) {
    int_texcoo[node*2+0] = int_lonlat[node*2+0];
    int_texcoo[node*2+1] = (int)(surface.intToLat(int_lonlat[node*2+1])*Integer.MIN_VALUE/-Math.PI);
  }

  void computeTexCoo(TextureTile tile) {
    tile.s = tile.lon;
    tile.d_s = tile.d_lon;
    double min_t = surface.intToLat(tile.lat           )*Integer.MIN_VALUE/-Math.PI;
    double max_t = surface.intToLat(tile.lat+tile.d_lat)*Integer.MIN_VALUE/-Math.PI;
    tile.t   = (int)min_t;
    tile.d_t = (int)(max_t-min_t);
    tile = null;
  }
}