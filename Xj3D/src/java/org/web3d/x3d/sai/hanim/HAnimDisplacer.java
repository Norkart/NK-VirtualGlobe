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

package org.web3d.x3d.sai.hanim;

import org.web3d.x3d.sai.X3DGeometricPropertyNode;

/** Defines the requirements of an X3D HAnimDisplacer node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface HAnimDisplacer extends X3DGeometricPropertyNode {

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
public int getNumCoordIndex();

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getCoordIndex(int[] val);

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
public void setCoordIndex(int[] val);

/** Return the number of MFFloat items in the displacements field. 
 * @return the number of MFFloat items in the displacements field.  */
public int getNumDisplacements();

/** Return the displacements value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDisplacements(float[] val);

/** Set the displacements field. 
 * @param val The float[] to set.  */
public void setDisplacements(float[] val);

/** Return the name String value. 
 * @return The name String value.  */
public String getName();

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val);

}
