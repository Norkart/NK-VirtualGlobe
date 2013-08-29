//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;

import org.w3c.dom.*;

import org.j3d.aviatrix3d.Group;

import com.norkart.virtualglobe.components.FeatureSet;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class Feature3DGroup extends Feature3D {

  protected org.j3d.aviatrix3d.Node node;

  public Feature3DGroup(Feature3DGroup parent) {
    super(parent);
  }

  public Feature3DGroup(FeatureSet featureSet) {
    super(featureSet);
  }

  public void load(Element domElement) throws LoadException {
    super.load(domElement);
  }

   synchronized public org.j3d.aviatrix3d.Node getNode() {
     return node;
   }


}
