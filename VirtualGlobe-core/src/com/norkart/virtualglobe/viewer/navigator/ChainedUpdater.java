//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */

public abstract class ChainedUpdater extends NavigatorUpdater {
  protected ChainedUpdater next;

  public ChainedUpdater(GlobeNavigator navigator) {
    super(navigator);
    this.next      = null;
  }
  public ChainedUpdater(ChainedUpdater next) {
   super(next.navigator);
   this.next      = next;
  }
}