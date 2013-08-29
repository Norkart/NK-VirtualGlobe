/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.x3d.sai.geospatial;

import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;

/** Defines the requirements of an X3D GeoElevationGrid node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoElevationGrid extends X3DGeometryNode {

/** Return the color X3DNode value. 
 * @return The color X3DNode value.  */
public X3DNode getColor();

/** Set the color field. 
 * @param val The X3DColorNode to set.  */
public void setColor(X3DColorNode val);

/** Set the color field. 
 * @param val The X3DProtoInstance to set.  */
public void setColor(X3DProtoInstance val);

/** Return the normal X3DNode value. 
 * @return The normal X3DNode value.  */
public X3DNode getNormal();

/** Set the normal field. 
 * @param val The X3DNormalNode to set.  */
public void setNormal(X3DNormalNode val);

/** Set the normal field. 
 * @param val The X3DProtoInstance to set.  */
public void setNormal(X3DProtoInstance val);

/** Return the texCoord X3DNode value. 
 * @return The texCoord X3DNode value.  */
public X3DNode getTexCoord();

/** Set the texCoord field. 
 * @param val The X3DTextureCoordinateNode to set.  */
public void setTexCoord(X3DTextureCoordinateNode val);

/** Set the texCoord field. 
 * @param val The X3DProtoInstance to set.  */
public void setTexCoord(X3DProtoInstance val);

/** Return the ccw boolean value. 
 * @return The ccw boolean value.  */
public boolean getCcw();

/** Set the ccw field. 
 * @param val The boolean to set.  */
public void setCcw(boolean val);

/** Return the colorPerVertex boolean value. 
 * @return The colorPerVertex boolean value.  */
public boolean getColorPerVertex();

/** Set the colorPerVertex field. 
 * @param val The boolean to set.  */
public void setColorPerVertex(boolean val);

/** Return the creaseAngle double value. 
 * @return The creaseAngle double value.  */
public double getCreaseAngle();

/** Set the creaseAngle field. 
 * @param val The double to set.  */
public void setCreaseAngle(double val);

/** Return the geoGridOrigin value in the argument double[]
 * @param val The double[] to initialize.  */
public void getGeoGridOrigin(double[] val);

/** Set the geoGridOrigin field. 
 * @param val The double[] to set.  */
public void setGeoGridOrigin(double[] val);

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
public X3DNode getGeoOrigin();

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
public void setGeoOrigin(X3DNode val);

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem();

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val);

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val);

/** Return the number of MFDouble items in the height field. 
 * @return the number of MFDouble items in the height field.  */
public int getNumHeight();

/** Return the height value in the argument double[]
 * @param val The double[] to initialize.  */
public void getHeight(double[] val);

/** Set the height field. 
 * @param val The double[] to set.  */
public void setHeight(double[] val);

/** Return the normalPerVertex boolean value. 
 * @return The normalPerVertex boolean value.  */
public boolean getNormalPerVertex();

/** Set the normalPerVertex field. 
 * @param val The boolean to set.  */
public void setNormalPerVertex(boolean val);

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

/** Return the xDimension int value. 
 * @return The xDimension int value.  */
public int getXDimension();

/** Set the xDimension field. 
 * @param val The int to set.  */
public void setXDimension(int val);

/** Return the xSpacing double value. 
 * @return The xSpacing double value.  */
public double getXSpacing();

/** Set the xSpacing field. 
 * @param val The double to set.  */
public void setXSpacing(double val);

/** Return the zDimension int value. 
 * @return The zDimension int value.  */
public int getZDimension();

/** Set the zDimension field. 
 * @param val The int to set.  */
public void setZDimension(int val);

/** Return the zSpacing double value. 
 * @return The zSpacing double value.  */
public double getZSpacing();

/** Set the zSpacing field. 
 * @param val The double to set.  */
public void setZSpacing(double val);

/** Return the yScale float value. 
 * @return The yScale float value.  */
public float getYScale();

/** Set the yScale field. 
 * @param val The float to set.  */
public void setYScale(float val);

}
