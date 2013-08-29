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

package org.web3d.x3d.sai.text;

import org.web3d.x3d.sai.X3DFontStyleNode;

/** Defines the requirements of an X3D FontStyle node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface FontStyle extends X3DFontStyleNode {

/** Return the number of MFString items in the family field. 
 * @return the number of MFString items in the family field.  */
public int getNumFamily();

/** Return the family value in the argument String[]
 * @param val The String[] to initialize.  */
public void getFamily(String[] val);

/** Set the family field. 
 * @param val The String[] to set.  */
public void setFamily(String[] val);

/** Return the horizontal boolean value. 
 * @return The horizontal boolean value.  */
public boolean getHorizontal();

/** Set the horizontal field. 
 * @param val The boolean to set.  */
public void setHorizontal(boolean val);

/** Return the number of MFString items in the justify field. 
 * @return the number of MFString items in the justify field.  */
public int getNumJustify();

/** Return the justify value in the argument String[]
 * @param val The String[] to initialize.  */
public void getJustify(String[] val);

/** Set the justify field. 
 * @param val The String[] to set.  */
public void setJustify(String[] val);

/** Return the language String value. 
 * @return The language String value.  */
public String getLanguage();

/** Set the language field. 
 * @param val The String to set.  */
public void setLanguage(String val);

/** Return the leftToRight boolean value. 
 * @return The leftToRight boolean value.  */
public boolean getLeftToRight();

/** Set the leftToRight field. 
 * @param val The boolean to set.  */
public void setLeftToRight(boolean val);

/** Return the size float value. 
 * @return The size float value.  */
public float getSize();

/** Set the size field. 
 * @param val The float to set.  */
public void setSize(float val);

/** Return the spacing float value. 
 * @return The spacing float value.  */
public float getSpacing();

/** Set the spacing field. 
 * @param val The float to set.  */
public void setSpacing(float val);

/** Return the style String value. 
 * @return The style String value.  */
public String getStyle();

/** Set the style field. 
 * @param val The String to set.  */
public void setStyle(String val);

/** Return the topToBottom boolean value. 
 * @return The topToBottom boolean value.  */
public boolean getTopToBottom();

/** Set the topToBottom field. 
 * @param val The boolean to set.  */
public void setTopToBottom(boolean val);

}
