//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import java.util.ArrayList;
import java.util.Iterator;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class GLCleanup {
  private static ArrayList init_objects    = new ArrayList();
  private static ArrayList cleanup_objects = new ArrayList();
  private static Object lock = new Object();

  public void requestCleanup() {
    synchronized (lock) {
      cleanup_objects.add(this);
    }
  }
  public void requestInit() {
    synchronized (lock) {
      init_objects.add(this);
    }
  }
  protected boolean init(GL gl, GLU glu) { return true; }
  protected abstract void cleanup(GL gl, GLU glu);
  public static void cleanupAll(GL gl, GLU glu) {
    synchronized (lock) {
      // Delete old stuff
      Iterator it = cleanup_objects.iterator();
      while (it.hasNext()) {
        GLCleanup t = (GLCleanup)it.next();
        t.cleanup(gl, glu);
      }
      cleanup_objects.clear();

      // Init new stuff
      it = init_objects.iterator();
      while (it.hasNext()) {
        GLCleanup t = (GLCleanup)it.next();
        if (t.init(gl, glu))
          it.remove();
      }
    }
  }
}