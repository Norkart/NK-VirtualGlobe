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

import org.web3d.x3d.sai.X3DAppearanceNode;
import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/** Defines the requirements of an X3DShapeNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.5 $ */
public interface X3DShapeNode extends X3DChildNode, X3DBoundedObject {

/** Return the appearance X3DNode value. 
 * @return The appearance X3DNode value.  */
public X3DNode getAppearance();

/** Set the appearance field. 
 * @param val The X3DAppearanceNode to set.  */
public void setAppearance(X3DAppearanceNode val);

/** Set the appearance field. 
 * @param val The X3DProtoInstance to set.  */
public void setAppearance(X3DProtoInstance val);

/** Return the geometry X3DNode value. 
 * @return The geometry X3DNode value.  */
public X3DNode getGeometry();

/** Set the geometry field. 
 * @param val The X3DGeometryNode to set.  */
public void setGeometry(X3DGeometryNode val);

/** Set the geometry field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry(X3DProtoInstance val);

}
