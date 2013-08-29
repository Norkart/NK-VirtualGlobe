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

package org.web3d.x3d.sai;


/** Defines the requirements of an X3DBoundedObject abstract node type
 * @author Rex Melton
 * @version $Revision: 1.4 $ */
public interface X3DBoundedObject {

/** Return the bboxCenter value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBboxCenter(float[] val);

/** Set the bboxCenter field. 
 * @param val The float[] to set.  */
public void setBboxCenter(float[] val);

/** Return the bboxSize value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBboxSize(float[] val);

/** Set the bboxSize field. 
 * @param val The float[] to set.  */
public void setBboxSize(float[] val);

}
