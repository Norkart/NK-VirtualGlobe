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

package org.xj3d.sai.internal.node.networking;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.networking.LoadSensor;

/** A concrete implementation of the LoadSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILoadSensor extends BaseNode implements LoadSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The watchList inputOutput field */
private MFNode watchList;

/** The timeOut inputOutput field */
private SFTime timeOut;

/** The loadTime outputOnly field */
private SFTime loadTime;

/** The isLoaded outputOnly field */
private SFBool isLoaded;

/** The progress outputOnly field */
private SFFloat progress;

/** Constructor */ 
public SAILoadSensor ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled() {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  return( enabled.getValue( ) );
}

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val) {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  enabled.setValue( val );
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the number of MFNode items in the watchList field. 
 * @return the number of MFNode items in the watchList field.  */
public int getNumWatchList() {
  if ( watchList == null ) { 
    watchList = (MFNode)getField( "watchList" ); 
  }
  return( watchList.getSize( ) );
}

/** Return the watchList value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getWatchList(X3DNode[] val) {
  if ( watchList == null ) { 
    watchList = (MFNode)getField( "watchList" ); 
  }
  watchList.getValue( val );
}

/** Set the watchList field. 
 * @param val The X3DNode[] to set.  */
public void setWatchList(X3DNode[] val) {
  if ( watchList == null ) { 
    watchList = (MFNode)getField( "watchList" ); 
  }
  watchList.setValue( val.length, val );
}

/** Return the timeOut double value. 
 * @return The timeOut double value.  */
public double getTimeOut() {
  if ( timeOut == null ) { 
    timeOut = (SFTime)getField( "timeOut" ); 
  }
  return( timeOut.getValue( ) );
}

/** Set the timeOut field. 
 * @param val The double to set.  */
public void setTimeOut(double val) {
  if ( timeOut == null ) { 
    timeOut = (SFTime)getField( "timeOut" ); 
  }
  timeOut.setValue( val );
}

/** Return the loadTime double value. 
 * @return The loadTime double value.  */
public double getLoadTime() {
  if ( loadTime == null ) { 
    loadTime = (SFTime)getField( "loadTime" ); 
  }
  return( loadTime.getValue( ) );
}

/** Return the isLoaded boolean value. 
 * @return The isLoaded boolean value.  */
public boolean getIsLoaded() {
  if ( isLoaded == null ) { 
    isLoaded = (SFBool)getField( "isLoaded" ); 
  }
  return( isLoaded.getValue( ) );
}

/** Return the progress float value. 
 * @return The progress float value.  */
public float getProgress() {
  if ( progress == null ) { 
    progress = (SFFloat)getField( "progress" ); 
  }
  return( progress.getValue( ) );
}

}
