//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.util;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class PathPoint {
  public double [] data = new double[5];
  public long movetime;

  public String toString() {
    return Math.toDegrees(data[0]) + "," + Math.toDegrees(data[1]) + "," + data[2] + "," + Math.toDegrees(data[3]) + "," + Math.toDegrees(data[4]) + "," + movetime;
  }
}