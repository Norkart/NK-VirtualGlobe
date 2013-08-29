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

package org.web3d.x3d.sai.shape;

import org.web3d.x3d.sai.X3DAppearanceNode;
import org.web3d.x3d.sai.X3DMaterialNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureNode;
import org.web3d.x3d.sai.X3DTextureTransformNode;

/** Defines the requirements of an X3D Appearance node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Appearance extends X3DAppearanceNode {

/** Return the material X3DNode value. 
 * @return The material X3DNode value.  */
public X3DNode getMaterial();

/** Set the material field. 
 * @param val The X3DMaterialNode to set.  */
public void setMaterial(X3DMaterialNode val);

/** Set the material field. 
 * @param val The X3DProtoInstance to set.  */
public void setMaterial(X3DProtoInstance val);

/** Return the texture X3DNode value. 
 * @return The texture X3DNode value.  */
public X3DNode getTexture();

/** Set the texture field. 
 * @param val The X3DTextureNode to set.  */
public void setTexture(X3DTextureNode val);

/** Set the texture field. 
 * @param val The X3DProtoInstance to set.  */
public void setTexture(X3DProtoInstance val);

/** Return the textureTransform X3DNode value. 
 * @return The textureTransform X3DNode value.  */
public X3DNode getTextureTransform();

/** Set the textureTransform field. 
 * @param val The X3DTextureTransformNode to set.  */
public void setTextureTransform(X3DTextureTransformNode val);

/** Set the textureTransform field. 
 * @param val The X3DProtoInstance to set.  */
public void setTextureTransform(X3DProtoInstance val);

/** Return the lineProperties X3DNode value. 
 * @return The lineProperties X3DNode value.  */
public X3DNode getLineProperties();

/** Set the lineProperties field. 
 * @param val The X3DNode to set.  */
public void setLineProperties(X3DNode val);

/** Return the fillProperties X3DNode value. 
 * @return The fillProperties X3DNode value.  */
public X3DNode getFillProperties();

/** Set the fillProperties field. 
 * @param val The X3DNode to set.  */
public void setFillProperties(X3DNode val);

/** Return the textureProperties X3DNode value. 
 * @return The textureProperties X3DNode value.  */
public X3DNode getTextureProperties();

/** Set the textureProperties field. 
 * @param val The X3DNode to set.  */
public void setTextureProperties(X3DNode val);

}
