//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class Viewpoint {
  public String name;
  public URL dataset;
  public double [] data = new double[5];

  public double getLon() {
    return data[0];
  }
  public double getLat() {
    return data[1];
  }
  public double getH() {
    return data[2];
  }
  public double getAz() {
    return data[3];
  }
  public double getHa() {
    return data[4];
  }

  public void setLon(double d) {
    data[0] = d;
  }
  public void setLat(double d) {
    data[1] = d;
  }
  public void setH(double d) {
    data[2] = d;
  }
  public void setAz(double d) {
    data[3] = d;
  }
  public void setHa(double d) {
    data[4] = d;
  }

  public boolean isSamePoint(Viewpoint v) {
    if (v==null) return false;
    if (Math.abs(getH()-v.getH()) > 1)
      return false;
    if (Math.abs(getLat()-v.getLat())*6400000. > 1.)
      return false;

    double da = getAz()-v.getAz();
    while (da < -Math.PI) da += Math.PI*2;
    while (da >  Math.PI) da -= Math.PI*2;
    if (Math.abs(da) > 1e-3)
      return false;
    da = getHa()-v.getHa();
    while (da < -Math.PI) da += Math.PI*2;
    while (da >  Math.PI) da -= Math.PI*2;
    if (Math.abs(da) > 1e-3)
      return false;

    double dLon = getLon()-v.getLon();
    while (dLon < -180) dLon += Math.PI*2;
    while (dLon >  180) dLon -= Math.PI*2;
    dLon = dLon*6400000.;
    if (Math.abs(dLon) > Math.cos((getLat()+v.getLat()/2)))
      return false;
    return true;
  }

  public void save(StringBuffer strBuf) {
    if (name != null) {
      strBuf.append("name=");
      try {
        strBuf.append(URLEncoder.encode(name, "UTF-8"));
      }
      catch (UnsupportedEncodingException ex) {
        System.err.println(ex);
      }
      strBuf.append("&");
    }
    if (dataset != null) {
      strBuf.append("dataset=");
      try {
        strBuf.append(URLEncoder.encode(dataset.toString(), "UTF-8"));
      }
      catch (UnsupportedEncodingException ex) {
        System.err.println(ex);
      }
      strBuf.append("&");
    }
    strBuf.append("viewpoint=");
    strBuf.append(Math.toDegrees(data[0]));
    for (int j=1; j<data.length; ++j) {
      strBuf.append(",");
      if (j == 2)
        strBuf.append(data[j]);
      else
        strBuf.append(Math.toDegrees(data[j]));
    }
  }

  public boolean parse(String line) {
    int ix;
    ix = line.indexOf("name=");
    if (ix >= 0) {
      ix += "name=".length();
      int endIx = line.indexOf("&", ix);
      if (endIx < 0) endIx = line.length();
      try {
        name = URLDecoder.decode(line.substring(ix, endIx), "UTF-8");
      }
      catch (UnsupportedEncodingException ex) {
        System.err.println(ex);
      }
    }
    ix = line.indexOf("dataset=");
    if (ix >= 0) {
      ix += "dataset=".length();
      int endIx = line.indexOf("&", ix);
      if (endIx < 0) endIx = line.length();
      try {
        dataset = new URL(URLDecoder.decode(line.substring(ix, endIx), "UTF-8"));
      }
      catch (MalformedURLException ex) {
        System.err.println(ex);
      }
      catch (UnsupportedEncodingException ex) {
        System.err.println(ex);
      }
    }
    ix = line.indexOf("viewpoint=");
    if (ix >= 0) {
      ix += "viewpoint=".length();
      int endIx = line.indexOf("&", ix);
      if (endIx < 0) endIx = line.length();
      String[] tokens = line.substring(ix, endIx).split(",");
      if (tokens.length != 5)
        return false;
      for (int i=0; i < 5; ++i)
        data[i] = Double.parseDouble(tokens[i]);
      data[0] = Math.toRadians(data[0]);
      data[1] = Math.toRadians(data[1]);
      data[3] = Math.toRadians(data[3]);
      data[4] = Math.toRadians(data[4]);
    }
    else
      return false;

    return true;
  }
}