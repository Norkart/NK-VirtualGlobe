//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import com.norkart.virtualglobe.globesurface.TextureLoader;


/**
 * A texture coverage is a container for the TextureLoader and TextureCoosys
 * associated with a TextureCoverage for a surface.
 *
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class TextureCoverage {
  int   [] int_tex_coo;
  TextureTile[] base_tiles;

  private BttSurface surface;
  private TextureCoosys coosys;
  private TextureLoader loader;

  boolean renew_textures = false;

  public TextureCoverage(BttSurface surface, TextureLoader loader, TextureCoosys coosys) {
    this.surface = surface;
    this.loader  = loader;
    this.coosys  = coosys;
    base_tiles   = new TextureTile[surface.base_mesh.length];
    int_tex_coo = new int[surface.size*2];
    for (int i=0; i<base_tiles.length; ++i) {
      int cn = surface.base_mesh[i];
      final int d_lon = 1<<30;
      final int d_lat = 1<<28;
      int lon = surface.int_lonlat[cn*2+0]-d_lon/2;
      int lat = surface.int_lonlat[cn*2+1]-d_lat/2;
      base_tiles[i] = new TextureTile(this, lon, lat, d_lon, d_lat, surface.base_code[i], surface.getTs());
    }

    surface.addTexture(this);

    // Compute texture coordinatres for all existing nodes
    for (int id=0; id<surface.size; id++)
      if (surface.status[id] > 0)
        computeTexCoo(id);
  }

  void setSize() {
       int [] new_int_tex_coo = new int[surface.size*2];
       System.arraycopy(int_tex_coo, 0, new_int_tex_coo, 0, int_tex_coo.length);
       int_tex_coo = new_int_tex_coo;
  }
  
  void computeTexCoo(int n) {
    coosys.computeTexCoo(n, surface.int_lonlat, int_tex_coo);
  }

  void load(TextureTile tile) {
    // Beregn koordinater for tile'n
    coosys.computeTexCoo(tile);

    // Start loading
    loader.loadTextureTile(tile);
    tile = null;
  }

  boolean stopLoadingTextureTile(TextureTile tile) {
    return loader.stopLoadingTextureTile(tile);
  }

  public BttSurface getSurface() {
    return surface;
  }

  TextureCoosys getTextureCoosys() {
    return coosys;
  }

  void clearTextures() {
    for (int i=0; i<base_tiles.length; ++i)
      base_tiles[i].clearTextures();
  }

  public void reloadTextures() {
      renew_textures = true;
  }
}